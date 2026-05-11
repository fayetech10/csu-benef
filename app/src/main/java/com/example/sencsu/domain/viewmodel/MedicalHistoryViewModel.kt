package com.example.sencsu.domain.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sencsu.data.remote.ApiService
import com.example.sencsu.data.remote.dto.ServiceMedicalDto
import com.example.sencsu.data.remote.dto.ServicesSummaryDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MedicalHistoryViewModel @Inject constructor(
    private val apiService: ApiService,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(MedicalHistoryUiState())
    val uiState = _uiState.asStateFlow()

    private val matricule: String = savedStateHandle["matricule"] ?: ""
    private val pcId: String? = savedStateHandle["pcId"]
    private val pcName: String? = savedStateHandle["pcName"]

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                if (matricule.isNotEmpty()) {
                    // Charger les services via le matricule (avec pcId optionnel)
                    val response = apiService.getServicesMedicaux(matricule, pcId)
                    val services = response.data ?: emptyList()
                    _uiState.update {
                        it.copy(
                            services = services,
                            filteredServices = services,
                            isLoading = false,
                            isForDependent = pcId != null,
                            dependentId = pcId,
                            dependentName = pcName
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "Matricule non fourni") }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = e.message ?: "Erreur de chargement", isLoading = false)
                }
            }
        }
    }

    fun filterByType(type: String?) {
        _uiState.update { state ->
            val filtered = if (type == null) {
                state.services
            } else {
                state.services.filter { it.typeService == type }
            }
            state.copy(filteredServices = filtered, selectedFilter = type)
        }
    }
}

data class MedicalHistoryUiState(
    val services: List<ServiceMedicalDto> = emptyList(),
    val filteredServices: List<ServiceMedicalDto> = emptyList(),
    val selectedFilter: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isForDependent: Boolean = false,
    val dependentId: String? = null,
    val dependentName: String? = null
)
