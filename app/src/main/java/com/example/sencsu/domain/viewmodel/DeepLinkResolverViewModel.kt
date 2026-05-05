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

data class DeepLinkUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val resolvedData: AdherentDto? = null,
    val targetRoute: String? = null
)

@HiltViewModel
class DeepLinkResolverViewModel @Inject constructor(
    private val adherentRepository: IAdherentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DeepLinkUiState())
    val uiState = _uiState.asStateFlow()

    /**
     * Résout un matricule en interrogeant le backend.
     * Le scan endpoint retourne l'adhérent ou la personne à charge.
     * On utilise l'ID retourné pour construire la route de navigation.
     */
    fun resolveMatricule(matricule: String) {
        if (matricule.isBlank()) {
            _uiState.update { it.copy(error = "Matricule invalide") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = adherentRepository.scanAdherent(matricule)
            result.onSuccess { adherent ->
                val id = adherent.id
                if (id != null) {
                    val route = "adherent_details/$id"
                    _uiState.update { it.copy(
                        isLoading = false,
                        resolvedData = adherent,
                        targetRoute = route
                    ) }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "Bénéficiaire introuvable") }
                }
            }.onFailure { error ->
                _uiState.update { it.copy(isLoading = false, error = "Erreur : ${error.message}") }
            }
        }
    }
}
