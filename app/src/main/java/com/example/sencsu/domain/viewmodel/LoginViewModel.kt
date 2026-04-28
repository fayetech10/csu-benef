package com.example.sencsu.domain.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sencsu.domain.repository.IAuthRepository
import com.example.sencsu.data.repository.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class LoginUiEvent {
    object NavigateToDashboard : LoginUiEvent()
}

data class LoginState(
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: IAuthRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _state = MutableStateFlow(LoginState())
    val state = _state.asStateFlow()

    private val _eventChannel = Channel<LoginUiEvent>()
    val uiEvent = _eventChannel.receiveAsFlow()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val response = authRepository.login(email, password)
                
                // On vérifie que les données essentielles sont présentes
                if (response.accessToken.isNullOrBlank()) {
                    // Si accessToken est vide alors que le repo n'a pas jeté d'exception,
                    // c'est que le champ n'a pas pu être extrait de la réponse "succès".
                    throw Exception("Connexion réussie mais token d'accès introuvable dans la réponse.")
                }
                
                if (response.user == null) {
                    throw Exception("Informations utilisateur manquantes dans la réponse du serveur.")
                }

                // On sauvegarde le token et l'utilisateur
                sessionManager.saveAuthToken(response.accessToken)
                sessionManager.saveUser(response.user)

                // On envoie l'événement de navigation
                _eventChannel.send(LoginUiEvent.NavigateToDashboard)
                _state.update { it.copy(isLoading = false) }

            } catch (e: Exception) {
                Log.e("LoginViewModel", "Login Failed: ${e.message}")
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Une erreur inconnue est survenue"
                    )
                }
            }
        }
    }
}