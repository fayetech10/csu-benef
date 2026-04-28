package com.example.sencsu.domain.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sencsu.data.local.dao.AdherentDao
import com.example.sencsu.data.remote.dto.AdherentDto
import com.example.sencsu.data.remote.dto.CotisationDto
import com.example.sencsu.data.remote.dto.PaiementDto
import com.example.sencsu.data.remote.dto.PersonneChargeDto
import com.example.sencsu.domain.repository.*
import com.example.sencsu.data.repository.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * État UI pour l'écran de détails d'un adhérent
 */
data class AdherentDetailsState(
    val isLoading: Boolean = false,
    val adherent: AdherentDto? = null,
    val error: String? = null,
    val paiements: List<PaiementDto> = emptyList(),
    val cotisations: List<CotisationDto> = emptyList(),
    val showDeleteAdherentDialog: Boolean = false,
    val personToDelete: PersonneChargeDto? = null,
    val showAddPersonneModal: Boolean = false,
    val selectedImageUrl: String? = null,
    val newPersonne: PersonneChargeDto = PersonneChargeDto(),
    val newPersonnePhotoUri: Uri? = null,
    val newPersonneRectoUri: Uri? = null,
    val newPersonneVersoUri: Uri? = null,
)

/**
 * Événements UI pour l'écran de détails
 */
sealed class DetailsUiEvent {
    data class ShowSnackbar(val message: String) : DetailsUiEvent()
    data object AdherentDeleted : DetailsUiEvent()
}

@HiltViewModel
class AdherentDetailsViewModel @Inject constructor(
    private val adherentRepository: IAdherentRepository,
    savedStateHandle: SavedStateHandle,
    val sessionManager: SessionManager,
    private val paiementRepository: IPaiementRepository,
    private val cotisationRepository: ICotisationRepository,
    private val dashboardRepository: IDashboardRepository,
    private val fileRepository: IFileRepository,
    private val adherentDao: AdherentDao
) : ViewModel() {

    private val _state = MutableStateFlow(AdherentDetailsState())
    val state: StateFlow<AdherentDetailsState> = _state.asStateFlow()

    private val _uiEvent = MutableSharedFlow<DetailsUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    private val adherentId: String? = savedStateHandle["id"]

    init {
        refresh()
    }

    /**
     * ✅ Rafraîchit les données de l'adhérent
     */
    fun refresh() {
        if (adherentId != null) {
            fetchAdherentDetails(adherentId)
        } else {
            _state.update { it.copy(error = "Identifiant manquant.") }
        }
    }

    /**
     * ✅ Récupère les détails de l'adhérent depuis l'API
     */
    private fun fetchAdherentDetails(id: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            adherentRepository.getAdherentById(id).fold(
                onSuccess = { adherent ->
                    _state.update { it.copy(isLoading = false, adherent = adherent) }
                    loadPaiementByIdadherent()
                    loadCotisationsByIdadherent()
                },
                onFailure = { error ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Erreur réseau"
                        )
                    }
                    Log.e("AdherentDetailsVM", "Erreur chargement", error)
                }
            )
        }
    }

    fun loadCotisationsByIdadherent() {
        val id = adherentId ?: return
        viewModelScope.launch {
            try {
                val result = cotisationRepository.getCotisationByIdahderent(id)
                _state.update { it.copy(cotisations = result) }
            } catch (e: Exception) {
                Log.e("AdherentDetailsVM", "Erreur cotisations", e)
            }
        }
    }

    fun loadPaiementByIdadherent() {
        val id = adherentId ?: return
        viewModelScope.launch {
            try {
                val result = paiementRepository.getPaiementsByAdherentId(id)
                _state.update { it.copy(paiements = result) }
            } catch (e: Exception) {
                Log.e("AdherentDetailsVM", "Erreur paiements", e)
            }
        }
    }

    fun showDeleteAdherentConfirmation() {
        _state.update { it.copy(showDeleteAdherentDialog = true) }
    }

    fun cancelDeleteAdherent() {
        _state.update { it.copy(showDeleteAdherentDialog = false) }
    }

    fun confirmDeleteAdherent() {
        val id = adherentId ?: return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, showDeleteAdherentDialog = false) }
            try {
                adherentRepository.deleteAdherent(id).fold(
                    onSuccess = {
                        // Supprimer du cache local (remote id est String maintenant)
                        adherentDao.deleteByRemoteId(id)

                        viewModelScope.launch {
                            try {
                                dashboardRepository.getDashboardData()
                            } catch (e: Exception) {
                                Log.e("AdherentDetailsVM", "Erreur MAJ dashboard", e)
                            }
                        }

                        _uiEvent.emit(DetailsUiEvent.ShowSnackbar("Adhérent supprimé"))
                        _uiEvent.emit(DetailsUiEvent.AdherentDeleted)
                    },
                    onFailure = { e ->
                        _state.update { it.copy(isLoading = false) }
                        Log.e("AdherentDetailsVM", "Erreur suppression adhérent", e)
                        _uiEvent.emit(DetailsUiEvent.ShowSnackbar("Erreur: ${e.message}"))
                    }
                )
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false) }
                _uiEvent.emit(DetailsUiEvent.ShowSnackbar("Erreur inattendue"))
            }
        }
    }

    fun showDeletePersonneConfirmation(personne: PersonneChargeDto) {
        _state.update { it.copy(personToDelete = personne) }
    }

    fun cancelDeletePersonne() {
        _state.update { it.copy(personToDelete = null) }
    }

    fun confirmDeletePersonne() {
        val currentAdherentId = adherentId ?: return
        val pcId = _state.value.personToDelete?.id ?: return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                adherentRepository.deletePersonneCharge(currentAdherentId, pcId).fold(
                    onSuccess = {
                        _uiEvent.emit(DetailsUiEvent.ShowSnackbar("Bénéficiaire supprimé"))
                        refresh()
                    },
                    onFailure = { e ->
                        _state.update { it.copy(isLoading = false) }
                        _uiEvent.emit(DetailsUiEvent.ShowSnackbar("Erreur: ${e.message}"))
                    }
                )
            } finally {
                cancelDeletePersonne()
            }
        }
    }

    fun openImagePreview(url: String?) {
        if (!url.isNullOrBlank()) {
            _state.update { it.copy(selectedImageUrl = url) }
        }
    }

    fun closeImagePreview() {
        _state.update { it.copy(selectedImageUrl = null) }
    }

    fun onAddPersonneClicked() {
        _state.update {
            it.copy(
                showAddPersonneModal = true,
                newPersonne = PersonneChargeDto(),
                newPersonnePhotoUri = null,
                newPersonneRectoUri = null,
                newPersonneVersoUri = null
            )
        }
    }

    fun onDismissAddPersonneModal() {
        _state.update {
            it.copy(
                showAddPersonneModal = false,
                newPersonne = PersonneChargeDto(),
                newPersonnePhotoUri = null,
                newPersonneRectoUri = null,
                newPersonneVersoUri = null
            )
        }
    }

    fun onNewPersonneChange(p: PersonneChargeDto) {
        _state.update { it.copy(newPersonne = p) }
    }

    fun updateNewPersonnePhotoUri(uri: Uri?) {
        _state.update { it.copy(newPersonnePhotoUri = uri) }
    }

    fun updateNewPersonneRectoUri(uri: Uri?) {
        _state.update { it.copy(newPersonneRectoUri = uri) }
    }

    fun updateNewPersonneVersoUri(uri: Uri?) {
        _state.update { it.copy(newPersonneVersoUri = uri) }
    }

    fun onSaveNewPersonne(context: Context) {
        val currentAdherentId = adherentId ?: return
        val state = _state.value

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            try {
                val photoUrl = state.newPersonnePhotoUri?.let { uploadImage(context, it) }
                val rectoUrl = state.newPersonneRectoUri?.let { uploadImage(context, it) }
                val versoUrl = state.newPersonneVersoUri?.let { uploadImage(context, it) }

                val personneToAdd = state.newPersonne.copy(
                    photo = photoUrl ?: state.newPersonne.photo,
                    photoRecto = rectoUrl ?: state.newPersonne.photoRecto,
                    photoVerso = versoUrl ?: state.newPersonne.photoVerso
                )

                adherentRepository.addPersonneCharge(currentAdherentId, personneToAdd).fold(
                    onSuccess = {
                        _uiEvent.emit(DetailsUiEvent.ShowSnackbar("Bénéficiaire ajouté avec succès"))
                        onDismissAddPersonneModal()
                        refresh()
                    },
                    onFailure = { e ->
                        _state.update { it.copy(isLoading = false) }
                        _uiEvent.emit(DetailsUiEvent.ShowSnackbar("Erreur: ${e.message}"))
                    }
                )
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false) }
                _uiEvent.emit(DetailsUiEvent.ShowSnackbar("Erreur lors de l'upload: ${e.message}"))
            }
        }
    }

    private suspend fun uploadImage(context: Context, uri: Uri): String? {
        return try {
            fileRepository.uploadImage(context, uri).getOrNull()
        } catch (e: Exception) {
            Log.e("ImageUpload", "Erreur upload", e)
            null
        }
    }
}