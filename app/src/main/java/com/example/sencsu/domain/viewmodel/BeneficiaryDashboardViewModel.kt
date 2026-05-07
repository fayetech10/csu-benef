package com.example.sencsu.domain.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sencsu.data.remote.ApiService
import com.example.sencsu.data.remote.dto.AdherentDto
import com.example.sencsu.data.remote.dto.AdherentUpdateDto
import com.example.sencsu.data.remote.dto.PersonneChargeDto
import com.example.sencsu.data.repository.SessionManager
import com.example.sencsu.domain.repository.IAdherentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await

@HiltViewModel
class BeneficiaryDashboardViewModel @Inject constructor(
    private val adherentRepository: IAdherentRepository,
    private val apiService: ApiService,
    val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(BeneficiaryDashboardUiState())
    val uiState = _uiState.asStateFlow()

    init {
        // Observer le token de manière réactive :
        // Quand un nouvel utilisateur se connecte, le token change → on refresh
        viewModelScope.launch {
            sessionManager.tokenFlow
                .filterNotNull()
                .distinctUntilChanged()
                .collect {
                    refresh()
                }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            adherentRepository.getMyInfo()
                .onSuccess { adherent ->
                    _uiState.update { it.copy(adherent = adherent, isLoading = false) }
                    // Charger les services médicaux récents
                    adherent.id?.let { 
                        loadRecentServices(it) 
                        syncFcmToken(it) // Synchroniser le token FCM
                    }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(error = error.message ?: "Erreur de chargement", isLoading = false) }
                }
        }
    }

    private fun syncFcmToken(userId: String) {
        viewModelScope.launch {
            try {
                val token = FirebaseMessaging.getInstance().token.await()
                adherentRepository.registerFcmToken(userId, token)
            } catch (e: Exception) {
                // Silently fail for token sync
            }
        }
    }

    private fun loadRecentServices(adherentId: String) {
        viewModelScope.launch {
            try {
                val response = apiService.getRecentServicesMedicaux(adherentId)
                val services = response.data ?: emptyList()
                _uiState.update { it.copy(recentServices = services) }
            } catch (e: Exception) {
                // Silently fail — services are secondary content
                _uiState.update { it.copy(recentServices = emptyList()) }
            }
        }
    }

    fun addPersonneCharge(personne: PersonneChargeDto) {
        val adherentId = _uiState.value.adherent?.id ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            adherentRepository.addPersonneCharge(adherentId, personne)
                .onSuccess { refresh() }
                .onFailure { error ->
                    _uiState.update { it.copy(error = error.message, isLoading = false) }
                }
        }
    }

    fun deletePersonneCharge(pcId: String) {
        val adherentId = _uiState.value.adherent?.id ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            adherentRepository.deletePersonneCharge(adherentId, pcId)
                .onSuccess { refresh() }
                .onFailure { error ->
                    _uiState.update { it.copy(error = error.message, isLoading = false) }
                }
        }
    }

    fun updatePersonneCharge(pcId: String, personne: PersonneChargeDto) {
        val adherentId = _uiState.value.adherent?.id ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            adherentRepository.updatePersonneCharge(adherentId, pcId, personne)
                .onSuccess { refresh() }
                .onFailure { error -> 
                    _uiState.update { it.copy(error = error.message, isLoading = false) }
                }
        }
    }

    fun updateProfile(
        updated: AdherentDto,
        onComplete: (Boolean, String?) -> Unit
    ) {
        val adherentId = updated.id ?: return onComplete(false, "Identifiant adherent introuvable")
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val dto = AdherentUpdateDto(
                nom = updated.nom.orEmpty(),
                prenoms = updated.prenoms.orEmpty(),
                adresse = updated.adresse.orEmpty(),
                lieuNaissance = updated.lieuNaissance ?: updated.lieuDeNaissance.orEmpty(),
                sexe = updated.sexe.orEmpty(),
                dateNaissance = updated.dateNaissance.orEmpty(),
                situationMatrimoniale = updated.situationM.orEmpty(),
                whatsapp = updated.whatsapp.orEmpty(),
                secteurActivite = updated.secteurActivite,
                region = updated.region.orEmpty(),
                departement = updated.departement.orEmpty(),
                commune = updated.commune.orEmpty(),
                regime = updated.regime.orEmpty(),
                typeBenef = updated.typeBenef.orEmpty(),
                typeAdhesion = updated.typeAdhesion.orEmpty(),
                photo = updated.photo,
                photoRecto = updated.photoRecto,
                photoVerso = updated.photoVerso,
                typePiece = updated.typePiece.orEmpty(),
                numeroPiece = updated.numeroPiece.orEmpty(),
                numeroCNi = updated.numeroCNi.orEmpty(),
                personnesCharge = updated.personnesCharge
            )

            adherentRepository.updateAdherent(adherentId, dto)
                .onSuccess {
                    _uiState.update { it.copy(adherent = updated, isLoading = false) }
                    refresh()
                    onComplete(true, null)
                }
                .onFailure { error ->
                    _uiState.update { it.copy(error = error.message, isLoading = false) }
                    onComplete(false, error.message)
                }
        }
    }

    fun updatePassword(
        oldPassword: String,
        newPassword: String,
        onComplete: (Boolean, String?) -> Unit
    ) {
        val adherentId = _uiState.value.adherent?.id ?: return onComplete(false, "Identifiant adherent introuvable")
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            adherentRepository.updatePassword(adherentId, oldPassword, newPassword)
                .onSuccess {
                    _uiState.update { it.copy(isLoading = false) }
                    onComplete(true, null)
                }
                .onFailure { error ->
                    _uiState.update { it.copy(error = error.message, isLoading = false) }
                    onComplete(false, error.message)
                }
        }
    }
}
