package com.example.sencsu.domain.viewmodel

import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sencsu.data.local.dao.AdherentDao
import com.example.sencsu.data.local.entity.AdherentEntity
import com.example.sencsu.data.remote.dto.AdherentDto
import com.example.sencsu.data.remote.dto.AdherentUpdateDto
import com.example.sencsu.data.remote.dto.FormConstants
import com.example.sencsu.data.remote.dto.PersonneChargeDto
import com.example.sencsu.domain.repository.IAdherentRepository
import com.example.sencsu.domain.repository.IDashboardRepository
import com.example.sencsu.domain.repository.IFileRepository
import com.example.sencsu.utils.Formatters
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

private const val ADHERENT_PRICE = 4500
private const val DEPENDANT_PRICE = 3500

sealed class AddAdherentUiEvent {
    data class ShowSnackbar(val message: String) : AddAdherentUiEvent()
    data class NavigateToPayment(
        val adherentId: String?,
        val localAdherentId: Long?,
        val montantTotal: Int,
        val matricule: String? = null,
        val defaultPassword: String? = null
    ) : AddAdherentUiEvent()
    data class NavigateToPasswordUpdate(val adherentId: String, val matricule: String, val defaultPassword: String) : AddAdherentUiEvent()
    object NavigateBack : AddAdherentUiEvent()
}

enum class FormType {
    ADHERENT,
    PERSONNE_CHARGE
}

/**
 * État UI pour le formulaire d'ajout/modification d'adhérent
 */
data class AddAdherentUiState(
    // ── Informations Personnelles ──
    val prenoms: String = "",
    val nom: String = "",
    val dateNaissance: String = "",
    val lieuNaissance: String = "",
    val sexe: String = "M",
    val situationMatrimoniale: String = "",

    // ── Contact & ID ──
    val whatsapp: String = "",
    val typePiece: String = "CNI",
    val numeroCNI: String = "",
    val numeroExtrait: String = "",
    val secteurActivite: String = "",

    // ── Localisation ──
    val departement: String = "",
    val commune: String = "",
    val region: String = "Thiès",
    val regime: String = "CONTRIBUTIF",
    val typeBenef: String = "CLASSIQUE",
    val typeAdhesion: String = "FAMALE",
    val adresse: String = "",
    val lienParent: String = "",                  // Pour les Personnes à Charge

    // ── Photos Adhérent Principal ──
    val photoUri: Uri? = null,                    // Nouvelle photo (local)
    val rectoUri: Uri? = null,                    // Nouveau recto (local)
    val versoUri: Uri? = null,                    // Nouveau verso (local)
    val existingPhotoUrl: String? = null,         // Photo serveur existante
    val existingRectoUrl: String? = null,         // Recto serveur existant
    val existingVersoUrl: String? = null,         // Verso serveur existant
    val photoUploadProgress: Float = 0f,          // Progrès upload photo

    // ── Personnes à Charge ──
    val dependants: List<PersonneChargeDto> = emptyList(),
    val currentDependant: PersonneChargeDto = PersonneChargeDto(
        prenoms = "",
        nom = "",
        dateNaissance = "",
        sexe = "M",
        situationM = FormConstants.SITUATIONS.firstOrNull() ?: "",
        typePiece = "CNI"
    ),
    val editingIndex: Int? = null,                 // Index du dépendant en édition
    val isModalVisible: Boolean = false,

    // ── État Global ──
    val isLoading: Boolean = false,
    val totalCost: Int = ADHERENT_PRICE,
    val validationErrors: Map<String, String> = emptyMap(),
    val formType: FormType = FormType.ADHERENT,
    val pcId: String? = null,                      // ID de la personne à charge si mode PC
    val isEditMode: Boolean = false               // Mode édition actif
)

@HiltViewModel
class AddAdherentViewModel @Inject constructor(
    private val adherentRepository: IDashboardRepository,
    private val fileRepository: IFileRepository,
    private val adherentDao: AdherentDao,
    private val adherentRepo: IAdherentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddAdherentUiState())
    val uiState: StateFlow<AddAdherentUiState> = _uiState.asStateFlow()

    private val _uiEvent = Channel<AddAdherentUiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    // ── Edit mode support ──────────────────────────────────────────────
    val isEditMode: Boolean get() = _uiState.value.isEditMode
    var editAdherentId: String? = null
        private set

    /**
     * Récupère un adhérent depuis l'API et prépare le formulaire pour l'édition
     */
    fun fetchAndLoadForEdit(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, formType = FormType.ADHERENT) }
            try {
                adherentRepo.getAdherentById(id)
                    .onSuccess { adherent -> loadAdherentForEdit(adherent) }
                    .onFailure { e ->
                        val message = "Impossible de charger l'adhérent: ${e.message}"
                        _uiEvent.send(AddAdherentUiEvent.ShowSnackbar(message))
                        Log.e("AddAdherentVM", message, e)
                    }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    /**
     * Récupère une personne à charge et prépare le formulaire pour l'édition
     */
    fun fetchAndLoadDependentForEdit(adherentId: String, pcId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, formType = FormType.PERSONNE_CHARGE, pcId = pcId) }
            try {
                adherentRepo.getAdherentById(adherentId)
                    .onSuccess { adherent ->
                        val pc = adherent.personnesCharge.find { it.id == pcId }
                        if (pc != null) {
                            loadDependentForEdit(adherentId, pc)
                        } else {
                            _uiEvent.send(AddAdherentUiEvent.ShowSnackbar("Bénéficiaire introuvable"))
                        }
                    }
                    .onFailure { e ->
                        _uiEvent.send(AddAdherentUiEvent.ShowSnackbar("Erreur: ${e.message}"))
                    }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun loadDependentForEdit(adherentId: String, pc: PersonneChargeDto) {
        editAdherentId = adherentId
        _uiState.update {
            it.copy(
                isEditMode = true,
                formType = FormType.PERSONNE_CHARGE,
                pcId = pc.id,
                prenoms = pc.prenoms ?: "",
                nom = pc.nom ?: "",
                dateNaissance = pc.dateNaissance ?: "",
                lieuNaissance = pc.lieuNaissance ?: "",
                sexe = pc.sexe ?: "M",
                situationMatrimoniale = pc.situationM ?: "",
                whatsapp = pc.whatsapp ?: "",
                adresse = pc.adresse ?: "",
                lienParent = pc.lienParent ?: "",
                numeroCNI = pc.numeroCNi ?: "",
                numeroExtrait = pc.numeroExtrait ?: "",
                typePiece = pc.typePiece ?: "CNI",
                existingPhotoUrl = pc.photo,
                existingRectoUrl = pc.photoRecto,
                existingVersoUrl = pc.photoVerso,
                photoUri = null,
                rectoUri = null,
                versoUri = null,
                dependants = emptyList() // Pas de dépendants pour une PC
            )
        }
    }

    /**
     * Remplit le formulaire avec les données d'un adhérent existant pour l'édition
     */
    fun loadAdherentForEdit(adherent: AdherentDto) {
        editAdherentId = adherent.id
        _uiState.update {
            it.copy(
                isEditMode = true,
                formType = FormType.ADHERENT,
                prenoms = adherent.prenoms ?: "",
                nom = adherent.nom ?: "",
                adresse = adherent.adresse ?: "",
                lieuNaissance = adherent.lieuNaissance ?: "",
                sexe = adherent.sexe ?: "M",
                dateNaissance = adherent.dateNaissance ?: "",
                situationMatrimoniale = adherent.situationM ?: "",
                whatsapp = adherent.whatsapp ?: "",
                secteurActivite = adherent.secteurActivite ?: "",
                typePiece = adherent.typePiece ?: "CNI",
                numeroCNI = adherent.numeroCNi ?: "",
                departement = adherent.departement ?: "",
                commune = adherent.commune ?: "",
                region = adherent.region ?: "Thiès",
                regime = adherent.regime ?: "CONTRIBUTIF",
                typeBenef = adherent.typeBenef ?: "CLASSIQUE",
                typeAdhesion = adherent.typeAdhesion ?: "FAMALE",
                existingPhotoUrl = adherent.photo,
                existingRectoUrl = adherent.photoRecto,
                existingVersoUrl = adherent.photoVerso,
                photoUri = null,
                rectoUri = null,
                versoUri = null,
                dependants = adherent.personnesCharge,
                totalCost = calculateTotalCost(adherent.personnesCharge.size)
            )
        }
    }

    // ─────────────────────────────────────────────────────────────────
    // ── MISES À JOUR DES CHAMPS
    // ─────────────────────────────────────────────────────────────────

    fun updatePrenoms(value: String) = _uiState.update {
        it.copy(prenoms = value, validationErrors = it.validationErrors - "prenoms")
    }

    fun updateNom(value: String) = _uiState.update {
        it.copy(nom = value, validationErrors = it.validationErrors - "nom")
    }

    fun updateAdresse(value: String) = _uiState.update {
        it.copy(adresse = value, validationErrors = it.validationErrors - "adresse")
    }

    fun updateLieuNaissance(value: String) = _uiState.update {
        it.copy(lieuNaissance = value, validationErrors = it.validationErrors - "lieuNaissance")
    }

    fun updateSexe(value: String) = _uiState.update { it.copy(sexe = value) }

    fun updateDateNaissance(date: String) = _uiState.update {
        it.copy(dateNaissance = date, validationErrors = it.validationErrors - "dateNaissance")
    }

    fun updateSituationMatrimoniale(value: String) = _uiState.update {
        it.copy(situationMatrimoniale = value)
    }

    fun updateWhatsapp(value: String) = _uiState.update {
        it.copy(
            whatsapp = Formatters.formatPhoneNumber(value),
            validationErrors = it.validationErrors - "whatsapp"
        )
    }

    fun updateSecteurActivite(value: String) = _uiState.update {
        it.copy(secteurActivite = value)
    }

    fun updateTypePiece(value: String) = _uiState.update { it.copy(typePiece = value) }

    fun updateNumeroCNI(value: String) = _uiState.update {
        it.copy(numeroCNI = value, validationErrors = it.validationErrors - "numeroCNI")
    }

    fun updateNumeroExtrait(value: String) = _uiState.update {
        it.copy(numeroExtrait = value, validationErrors = it.validationErrors - "numeroExtrait")
    }

    fun updateDepartement(value: String) = _uiState.update { it.copy(departement = value) }
    fun updateCommune(value: String) = _uiState.update { it.copy(commune = value, validationErrors = it.validationErrors - "commune") }
    fun updateRegion(value: String) = _uiState.update { it.copy(region = value) }
    fun updateRegime(value: String) = _uiState.update { it.copy(regime = value) }
    fun updateTypeBenef(value: String) = _uiState.update { it.copy(typeBenef = value) }
    fun updateTypeAdhesion(value: String) = _uiState.update { it.copy(typeAdhesion = value) }
    fun updateLienParent(value: String) = _uiState.update { it.copy(lienParent = value) }

    // ─────────────────────────────────────────────────────────────────
    // ── GESTION DES PHOTOS
    // ─────────────────────────────────────────────────────────────────

    fun updatePhotoUri(uri: Uri?) = _uiState.update {
        it.copy(
            photoUri = uri,
            existingPhotoUrl = if (uri != null) null else it.existingPhotoUrl,
            validationErrors = it.validationErrors - "photoUri"
        )
    }

    fun updateRectoUri(uri: Uri?) = _uiState.update {
        it.copy(
            rectoUri = uri,
            existingRectoUrl = if (uri != null) null else it.existingRectoUrl
        )
    }

    fun updateVersoUri(uri: Uri?) = _uiState.update {
        it.copy(
            versoUri = uri,
            existingVersoUrl = if (uri != null) null else it.existingVersoUrl
        )
    }

    fun updateCurrentDependant(dependant: PersonneChargeDto) {
        _uiState.update { it.copy(currentDependant = dependant) }
    }

    fun updateDependantPhotoUri(uri: Uri?) = _uiState.update {
        it.copy(currentDependant = it.currentDependant.copy(photo = uri?.toString()))
    }

    fun updateDependantRectoUri(uri: Uri?) = _uiState.update {
        it.copy(currentDependant = it.currentDependant.copy(photoRecto = uri?.toString()))
    }

    fun updateDependantVersoUri(uri: Uri?) = _uiState.update {
        it.copy(currentDependant = it.currentDependant.copy(photoVerso = uri?.toString()))
    }

    // ─────────────────────────────────────────────────────────────────
    // ── SOUMISSION
    // ─────────────────────────────────────────────────────────────────

    fun submitWithUpload(context: Context) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val state = _uiState.value
                val total = calculateTotalCost(state.dependants.size)
                val numeroPieceFinale = if (state.typePiece == "CNI") state.numeroCNI else state.numeroExtrait

                val localUuid = UUID.randomUUID().toString()
                val entity = AdherentEntity(
                    prenoms = state.prenoms,
                    nom = state.nom,
                    adresse = state.adresse,
                    lieuNaissance = state.lieuNaissance,
                    sexe = state.sexe,
                    dateNaissance = Formatters.formatDateForApi(state.dateNaissance),
                    situationM = state.situationMatrimoniale,
                    whatsapp = state.whatsapp,
                    secteurActivite = state.secteurActivite,
                    typePiece = state.typePiece,
                    numeroPiece = numeroPieceFinale,
                    numeroCNi = numeroPieceFinale,
                    departement = state.departement,
                    commune = state.commune,
                    region = state.region,
                    typeAdhesion = state.typeAdhesion,
                    montantTotal = total.toDouble(),
                    regime = state.regime,
                    typeBenef = state.typeBenef,
                    photo = state.photoUri?.toString(),
                    photoRecto = state.rectoUri?.toString(),
                    photoVerso = state.versoUri?.toString(),
                    actif = true,
                    isSynced = false,
                    localUuid = localUuid
                )
                val localId = adherentDao.insertAdherent(entity)

                try {
                    val photoUrl = state.photoUri?.let { uploadImage(context, it) }
                    val rectoUrl = state.rectoUri?.let { uploadImage(context, it) }
                    val versoUrl = state.versoUri?.let { uploadImage(context, it) }
                    val updatedDeps = uploadDependantsImages(context, state.dependants)

                    val adherentDto = buildAdherentDto(photoUrl, rectoUrl, versoUrl, updatedDeps, localUuid)

                    adherentRepository.ajouterAdherent(adherentDto)
                        .onSuccess { idResponse ->
                            adherentDao.markAsSynced(localId, idResponse.adherentId)
                            _uiEvent.send(AddAdherentUiEvent.ShowSnackbar("Adhérent ajouté !"))
                            _uiEvent.send(
                                AddAdherentUiEvent.NavigateToPayment(
                                    adherentId = idResponse.adherentId,
                                    localAdherentId = localId,
                                    montantTotal = state.totalCost,
                                    matricule = idResponse.matricule,
                                    defaultPassword = idResponse.defaultPassword
                                )
                            )
                            resetForm()
                        }
                        .onFailure { throw it }
                } catch (e: Exception) {
                    handleHttpException(e as? HttpException ?: throw e, localId)
                }
            } catch (e: Exception) {
                _uiEvent.send(AddAdherentUiEvent.ShowSnackbar(e.message ?: "Erreur"))
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun submitEdit(context: Context) {
        val id = editAdherentId ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val state = _uiState.value
                val photoUrl = if (state.photoUri != null) uploadImage(context, state.photoUri) else state.existingPhotoUrl
                val rectoUrl = if (state.rectoUri != null) uploadImage(context, state.rectoUri) else state.existingRectoUrl
                val versoUrl = if (state.versoUri != null) uploadImage(context, state.versoUri) else state.existingVersoUrl

                val result = if (state.formType == FormType.ADHERENT) {
                    val updatedDeps = uploadDependantsImages(context, state.dependants)
                    val dto = AdherentUpdateDto(
                        nom = state.nom, prenoms = state.prenoms, adresse = state.adresse,
                        lieuNaissance = state.lieuNaissance, sexe = state.sexe,
                        dateNaissance = Formatters.formatDateForAPI(state.dateNaissance),
                        situationMatrimoniale = state.situationMatrimoniale, whatsapp = state.whatsapp,
                        secteurActivite = state.secteurActivite, region = state.region,
                        departement = state.departement, commune = state.commune,
                        regime = state.regime, typeBenef = state.typeBenef,
                        typeAdhesion = state.typeAdhesion, photo = photoUrl,
                        photoRecto = rectoUrl, photoVerso = versoUrl,
                        personnesCharge = updatedDeps, typePiece = state.typePiece,
                        numeroPiece = if (state.typePiece == "CNI") state.numeroCNI else state.numeroExtrait,
                        numeroCNi = if (state.typePiece == "CNI") state.numeroCNI else state.numeroExtrait
                    )
                    adherentRepo.updateAdherent(id, dto)
                } else {
                    val pcDto = PersonneChargeDto(
                        id = state.pcId, nom = state.nom, prenoms = state.prenoms,
                        dateNaissance = Formatters.formatDateForAPI(state.dateNaissance),
                        lieuNaissance = state.lieuNaissance, sexe = state.sexe,
                        situationM = state.situationMatrimoniale, lienParent = state.lienParent,
                        whatsapp = state.whatsapp, adresse = state.adresse,
                        typePiece = state.typePiece,
                        numeroCNi = if (state.typePiece == "CNI") state.numeroCNI else null,
                        numeroExtrait = if (state.typePiece == "EXTRAIT") state.numeroExtrait else null,
                        photo = photoUrl, photoRecto = rectoUrl, photoVerso = versoUrl
                    )
                    adherentRepo.updatePersonneCharge(id, state.pcId!!, pcDto)
                }

                result.onSuccess {
                    _uiEvent.send(AddAdherentUiEvent.ShowSnackbar(if (state.formType == FormType.ADHERENT) "Adhérent mis à jour !" else "Bénéficiaire mis à jour !"))
                    _uiEvent.send(AddAdherentUiEvent.NavigateBack)
                    resetForm()
                }.onFailure { throw it }
            } catch (e: Exception) {
                sendError(e)
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private suspend fun uploadImage(context: Context, uri: Uri): String? = fileRepository.uploadImage(context, uri).getOrNull()

    private suspend fun uploadDependantsImages(context: Context, dependants: List<PersonneChargeDto>): List<PersonneChargeDto> {
        return dependants.map { dep ->
            dep.copy(
                photo = processImageUri(context, dep.photo),
                photoRecto = processImageUri(context, dep.photoRecto),
                photoVerso = processImageUri(context, dep.photoVerso)
            )
        }
    }

    private suspend fun processImageUri(context: Context, uriString: String?): String? {
        return when {
            uriString?.startsWith("content://") == true -> uploadImage(context, Uri.parse(uriString))
            uriString?.startsWith("http") == true -> uriString
            else -> null
        }
    }

    private fun buildAdherentDto(photoUrl: String?, rectoUrl: String?, versoUrl: String?, updatedDependants: List<PersonneChargeDto>, localUuid: String): AdherentDto {
        val state = _uiState.value
        return AdherentDto(
            prenoms = state.prenoms, nom = state.nom, adresse = state.adresse,
            lieuNaissance = state.lieuNaissance, sexe = state.sexe,
            dateNaissance = Formatters.formatDateForApi(state.dateNaissance),
            situationM = state.situationMatrimoniale, whatsapp = state.whatsapp,
            secteurActivite = state.secteurActivite, typePiece = state.typePiece,
            numeroPiece = if (state.typePiece == "CNI") state.numeroCNI else state.numeroExtrait,
            numeroCNi = if (state.typePiece == "CNI") state.numeroCNI else state.numeroExtrait,
            departement = state.departement, commune = state.commune, region = state.region,
            typeAdhesion = state.typeAdhesion, montantTotal = calculateTotalCost(state.dependants.size).toDouble(),
            regime = state.regime, typeBenef = state.typeBenef,
            photo = photoUrl, photoRecto = rectoUrl, photoVerso = versoUrl,
            clientUUID = localUuid,
            personnesCharge = updatedDependants.map { it.copy(dateNaissance = if (!it.dateNaissance.isNullOrBlank()) Formatters.formatDateForApi(it.dateNaissance) else it.dateNaissance) }
        )
    }

    private suspend fun handleHttpException(e: HttpException, localId: Long) {
        adherentDao.deleteByLocalId(localId)
        val msg = e.response()?.errorBody()?.string() ?: "Erreur serveur"
        _uiEvent.send(AddAdherentUiEvent.ShowSnackbar(msg))
    }

    fun showAddDependantModal() { resetDependantForm(); _uiState.update { it.copy(isModalVisible = true, editingIndex = null) } }
    fun showEditDependantModal(index: Int, dependant: PersonneChargeDto) { _uiState.update { it.copy(isModalVisible = true, editingIndex = index, currentDependant = dependant) } }
    fun hideModal() = _uiState.update { it.copy(isModalVisible = false) }

    fun saveDependant() {
        _uiState.update { state ->
            val newList = state.dependants.toMutableList()
            if (state.editingIndex != null) newList[state.editingIndex] = state.currentDependant else newList.add(state.currentDependant)
            state.copy(dependants = newList, totalCost = calculateTotalCost(newList.size), isModalVisible = false, editingIndex = null)
        }
        resetDependantForm()
    }

    fun removeDependant(index: Int) {
        _uiState.update { state ->
            val newList = state.dependants.toMutableList()
            newList.removeAt(index)
            state.copy(dependants = newList, totalCost = calculateTotalCost(newList.size))
        }
    }

    fun resetForm() { editAdherentId = null; _uiState.value = AddAdherentUiState() }

    private fun resetDependantForm() {
        _uiState.update {
            it.copy(
                currentDependant = PersonneChargeDto(prenoms = "", nom = "", dateNaissance = "", sexe = "M", situationM = FormConstants.SITUATIONS.firstOrNull() ?: "", typePiece = "CNI"),
                editingIndex = null
            )
        }
    }

    private fun calculateTotalCost(count: Int): Int = ADHERENT_PRICE + (count * DEPENDANT_PRICE)

    fun validateStep(step: Int): Boolean {
        val errors = mutableMapOf<String, String>()
        val state = _uiState.value
        when (step) {
            0 -> {
                if (state.prenoms.isBlank()) errors["prenoms"] = "Prénom requis"
                if (state.nom.isBlank()) errors["nom"] = "Nom requis"
                if (state.dateNaissance.isBlank()) errors["dateNaissance"] = "Date naissance requise"
                if (state.lieuNaissance.isBlank()) errors["lieuNaissance"] = "Lieu naissance requis"
            }
            1 -> {
                if (state.whatsapp.isBlank()) errors["whatsapp"] = "WhatsApp requis"
                if (state.typePiece == "CNI" && state.numeroCNI.length < 13) errors["numeroCNI"] = "CNI invalide"
            }
        }
        if (errors.isNotEmpty()) { _uiState.update { it.copy(validationErrors = errors) }; return false }
        _uiState.update { it.copy(validationErrors = emptyMap()) }; return true
    }

    private suspend fun sendError(e: Exception) {
        val msg = if (e is HttpException) e.response()?.errorBody()?.string() ?: "Erreur ${e.code()}" else e.message ?: "Erreur"
        _uiEvent.send(AddAdherentUiEvent.ShowSnackbar(msg))
    }

    fun updatePassword(adherentId: String, oldPassword: String, newPassword: String, onComplete: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                val response = adherentRepo.updatePassword(adherentId, oldPassword, newPassword)
                if (response.isSuccess) {
                    _uiEvent.send(AddAdherentUiEvent.ShowSnackbar("Mot de passe mis à jour !"))
                    onComplete(true, null)
                } else {
                    val errorMsg = response.exceptionOrNull()?.message ?: "Erreur"
                    _uiEvent.send(AddAdherentUiEvent.ShowSnackbar(errorMsg))
                    onComplete(false, errorMsg)
                }
            } catch (e: Exception) {
                onComplete(false, e.message)
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
}