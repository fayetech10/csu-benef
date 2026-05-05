package com.example.sencsu.domain.viewmodel

import android.net.Uri
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

    /**
     * Traite le résultat brut du scan QR.
     * Le QR peut contenir :
     *   - Une URL complète : http://localhost:8080/adherent?matricule=XXXXX
     *   - Un matricule brut  : CSU-2025-XXXX
     *
     * Dans tous les cas, on extrait le matricule et on interroge le backend.
     */
    fun onScanResult(rawValue: String) {
        if (_uiState.value.isLoading) return

        val matricule = extractMatricule(rawValue)

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

    /**
     * Extrait le matricule depuis la valeur brute du QR code.
     * - Si c'est une URL avec paramètre "matricule", on l'extrait.
     * - Sinon, on considère la valeur entière comme un matricule.
     */
    private fun extractMatricule(rawValue: String): String {
        return try {
            val uri = Uri.parse(rawValue)
            // Vérifie que c'est bien une URL avec un paramètre "matricule"
            uri.getQueryParameter("matricule")?.takeIf { it.isNotBlank() } ?: rawValue.trim()
        } catch (e: Exception) {
            rawValue.trim()
        }
    }
}

data class ScannerUiState(
    val scannedAdherent: AdherentDto? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)
