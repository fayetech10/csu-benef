package com.example.sencsu.domain.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sencsu.data.remote.dto.AdherentDto
import com.example.sencsu.domain.repository.IAdherentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScannerViewModel @Inject constructor(
    private val adherentRepository: IAdherentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScannerUiState())
    val uiState = _uiState.asStateFlow()

    fun onScanResult(matricule: String) {
        if (_uiState.value.isLoading) return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, scannedAdherent = null) }
            val result = adherentRepository.scanAdherent(matricule)
            result.onSuccess { adherent ->
                _uiState.update { it.copy(isLoading = false, scannedAdherent = adherent) }
            }.onFailure { error ->
                _uiState.update { it.copy(isLoading = false, error = "Adhérent non trouvé : ${error.message}") }
            }
        }
    }

    fun reset() {
        _uiState.update { ScannerUiState() }
    }
}

data class ScannerUiState(
    val scannedAdherent: AdherentDto? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)
