package com.example.sencsu.domain.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sencsu.data.remote.dto.PersonneChargeDto
import com.example.sencsu.data.repository.SessionManager
import com.example.sencsu.domain.repository.IAdherentRepository
import com.example.sencsu.utils.Validators
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel dédié à la gestion des personnes en charge.
 * Séparé du BeneficiaryDashboardViewModel pour respecter le principe SRP.
 */
@HiltViewModel
class DependentsViewModel @Inject constructor(
    private val adherentRepository: IAdherentRepository,
    val sessionManager: SessionManager
) : ViewModel() {

    // ── État principal ──
    private val _uiState = MutableStateFlow(DependentsUiState())
    val uiState = _uiState.asStateFlow()

    // ── État du formulaire ──
    private val _formState = MutableStateFlow(DependentFormState())
    val formState = _formState.asStateFlow()

    // ── Événement de succès one-shot ──
    private val _successEvent = MutableSharedFlow<String>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = kotlinx.coroutines.channels.BufferOverflow.DROP_OLDEST
    )
    val successEvent: SharedFlow<String> = _successEvent.asSharedFlow()

    init {
        // Observer le token pour se rafraîchir au changement d'utilisateur
        viewModelScope.launch {
            sessionManager.tokenFlow
                .filterNotNull()
                .distinctUntilChanged()
                .collect { refresh() }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            adherentRepository.getMyInfo()
                .onSuccess { adherent ->
                    _uiState.update {
                        it.copy(
                            adherentId = adherent.id,
                            dependents = adherent.personnesCharge,
                            isLoading = false
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(error = error.message ?: "Erreur de chargement", isLoading = false)
                    }
                }
        }
    }

    // ══════════════════════════════════════════
    // FORMULAIRE — Mise à jour des champs
    // ══════════════════════════════════════════

    fun onPrenomsChange(value: String) {
        _formState.update {
            it.copy(
                prenoms = value,
                prenomsError = Validators.validatePrenom(value)
            )
        }
    }

    fun onNomChange(value: String) {
        _formState.update {
            it.copy(
                nom = value,
                nomError = Validators.validateNom(value)
            )
        }
    }

    fun onSexeChange(value: String) {
        _formState.update { it.copy(sexe = value) }
    }

    fun onLienParentChange(value: String) {
        _formState.update { it.copy(lienParent = value) }
    }

    fun onDateNaissanceChange(value: String) {
        _formState.update {
            it.copy(
                dateNaissance = value,
                dateNaissanceError = Validators.validateDateNaissance(value)
            )
        }
    }

    fun onLieuNaissanceChange(value: String) {
        _formState.update { it.copy(lieuNaissance = value) }
    }

    fun onAdresseChange(value: String) {
        _formState.update { it.copy(adresse = value) }
    }

    fun onWhatsappChange(value: String) {
        _formState.update {
            it.copy(
                whatsapp = value,
                whatsappError = Validators.validatePhone(value)
            )
        }
    }

    fun onSituationMChange(value: String) {
        _formState.update { it.copy(situationM = value) }
    }

    fun onNumeroExtraitChange(value: String) {
        _formState.update { it.copy(numeroExtrait = value) }
    }

    fun onNumeroCNiChange(value: String) {
        _formState.update {
            it.copy(
                numeroCNi = value,
                numeroCNiError = Validators.validateNumeroCNI(value)
            )
        }
    }

    /**
     * Pré-remplir le formulaire pour la modification
     */
    fun editDependant(pc: PersonneChargeDto) {
        _formState.value = DependentFormState(
            editingId = pc.id,
            prenoms = pc.prenoms ?: "",
            nom = pc.nom ?: "",
            sexe = pc.sexe ?: "M",
            lienParent = pc.lienParent ?: "ENFANT",
            dateNaissance = pc.dateNaissance ?: "",
            lieuNaissance = pc.lieuNaissance ?: "",
            adresse = pc.adresse ?: "",
            whatsapp = pc.whatsapp ?: "",
            situationM = pc.situationM ?: "",
            numeroExtrait = pc.numeroExtrait ?: "",
            numeroCNi = pc.numeroCNi ?: "",
            photo = pc.photo,
            photoRecto = pc.photoRecto,
            photoVerso = pc.photoVerso
        )
    }

    /**
     * Réinitialiser le formulaire pour un ajout
     */
    fun resetForm() {
        _formState.value = DependentFormState()
    }

    // ══════════════════════════════════════════
    // ACTIONS CRUD
    // ══════════════════════════════════════════

    fun submitForm() {
        val form = _formState.value
        val adherentId = _uiState.value.adherentId ?: return

        // Valider les champs obligatoires
        val prenomsErr = Validators.validatePrenom(form.prenoms)
        val nomErr = Validators.validateNom(form.nom)
        if (prenomsErr != null || nomErr != null) {
            _formState.update { it.copy(prenomsError = prenomsErr, nomError = nomErr) }
            return
        }

        val dto = PersonneChargeDto(
            id = form.editingId,
            prenoms = form.prenoms.trim(),
            nom = form.nom.trim(),
            sexe = form.sexe,
            lienParent = form.lienParent,
            dateNaissance = form.dateNaissance.ifBlank { null },
            lieuNaissance = form.lieuNaissance.ifBlank { null },
            adresse = form.adresse.ifBlank { null },
            whatsapp = form.whatsapp.ifBlank { null },
            situationM = form.situationM.ifBlank { null },
            numeroExtrait = form.numeroExtrait.ifBlank { null },
            numeroCNi = form.numeroCNi.ifBlank { null },
            photo = form.photo,
            photoRecto = form.photoRecto,
            photoVerso = form.photoVerso
        )

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }

            val result = if (form.isEditing) {
                adherentRepository.updatePersonneCharge(adherentId, form.editingId!!, dto)
            } else {
                adherentRepository.addPersonneCharge(adherentId, dto)
            }

            result
                .onSuccess {
                    val msg = if (form.isEditing) "Membre modifié avec succès" else "Membre ajouté avec succès"
                    _successEvent.tryEmit(msg)
                    _uiState.update { it.copy(isSubmitting = false) }
                    resetForm()
                    refresh()
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            error = error.message ?: "Erreur lors de l'opération",
                            isSubmitting = false
                        )
                    }
                }
        }
    }

    fun deleteDependant(pcId: String) {
        val adherentId = _uiState.value.adherentId ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            adherentRepository.deletePersonneCharge(adherentId, pcId)
                .onSuccess {
                    _successEvent.tryEmit("Membre retiré du foyer")
                    refresh()
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(error = error.message ?: "Erreur de suppression", isLoading = false)
                    }
                }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

// ══════════════════════════════════════════
// ÉTATS UI
// ══════════════════════════════════════════

data class DependentsUiState(
    val dependents: List<PersonneChargeDto> = emptyList(),
    val adherentId: String? = null,
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val error: String? = null
)

data class DependentFormState(
    val editingId: String? = null,
    val prenoms: String = "",
    val nom: String = "",
    val sexe: String = "M",
    val lienParent: String = "ENFANT",
    val dateNaissance: String = "",
    val lieuNaissance: String = "",
    val adresse: String = "",
    val whatsapp: String = "",
    val situationM: String = "",
    val numeroExtrait: String = "",
    val numeroCNi: String = "",
    val photo: String? = null,
    val photoRecto: String? = null,
    val photoVerso: String? = null,
    // Erreurs de validation
    val prenomsError: String? = null,
    val nomError: String? = null,
    val dateNaissanceError: String? = null,
    val whatsappError: String? = null,
    val numeroCNiError: String? = null
) {
    val isEditing: Boolean get() = editingId != null

    val isValid: Boolean
        get() = prenoms.isNotBlank() && nom.isNotBlank()
                && prenomsError == null && nomError == null
                && dateNaissanceError == null && whatsappError == null
                && numeroCNiError == null
}
