package com.example.sencsu.domain.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sencsu.data.remote.dto.AdherentDto
import com.example.sencsu.data.remote.dto.PersonneChargeDto
import com.example.sencsu.domain.repository.IAdherentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BeneficiaryDashboardViewModel @Inject constructor(
    private val adherentRepository: IAdherentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BeneficiaryDashboardUiState())
    val uiState = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            adherentRepository.getMyInfo()
                .onSuccess { adherent ->
                    _uiState.update { it.copy(adherent = adherent, isLoading = false) }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(error = error.message ?: "Erreur de chargement", isLoading = false) }
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
}

data class BeneficiaryDashboardUiState(
    val adherent: AdherentDto? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)
