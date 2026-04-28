package com.example.sencsu.domain.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sencsu.data.repository.SessionManager
import com.example.sencsu.domain.repository.IAuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BeneficiaryLoginViewModel @Inject constructor(
    private val authRepository: IAuthRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(BeneficiaryLoginUiState())
    val uiState = _uiState.asStateFlow()

    fun onMatriculeChange(matricule: String) {
        _uiState.update { it.copy(matricule = matricule.uppercase(), error = null) }
    }

    fun onPasswordChange(password: String) {
        _uiState.update { it.copy(password = password, error = null) }
    }

    fun login(onSuccess: () -> Unit) {
        val state = _uiState.value
        if (state.matricule.isBlank() || state.password.isBlank()) {
            _uiState.update { it.copy(error = "Veuillez remplir tous les champs") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val response = authRepository.adherentLogin(state.matricule, state.password)
                if (response.accessToken != null && response.user != null) {
                    sessionManager.saveSession(response.accessToken, response.user)
                    // On peut aussi sauvegarder spécifiquement que c'est un bénéficiaire
                    onSuccess()
                } else {
                    _uiState.update { it.copy(error = "Réponse invalide du serveur") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message ?: "Erreur inconnue") }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
}

data class BeneficiaryLoginUiState(
    val matricule: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)
