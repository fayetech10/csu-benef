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

    private val _canUseBiometric = MutableStateFlow(false)
    val canUseBiometric = _canUseBiometric.asStateFlow()

    init {
        viewModelScope.launch {
            sessionManager.isBiometricEnabledFlow.collect { enabled ->
                _canUseBiometric.value = enabled
            }
        }
    }

    suspend fun getSavedMatricule(): String? = sessionManager.getSavedMatricule()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val response = authRepository.login(email, password)
                
                if (response.accessToken.isNullOrBlank()) {
                    throw Exception("Connexion réussie mais token d'accès introuvable.")
                }
                
                if (response.user == null) {
                    throw Exception("Informations utilisateur manquantes.")
                }

                sessionManager.saveAuthToken(response.accessToken)
                sessionManager.saveUser(response.user)
                sessionManager.saveMatricule(email)
                sessionManager.setBiometricEnabled(true) // Activer après succès manuel

                _eventChannel.send(LoginUiEvent.NavigateToDashboard)
                _state.update { it.copy(isLoading = false) }

            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message ?: "Erreur") }
            }
        }
    }

    fun loginWithBiometric() {
        viewModelScope.launch {
            // Dans une vraie app, on utiliserait un token sécurisé.
            // Ici pour la démo, si la biométrie réussit, on considère l'utilisateur connecté 
            // s'il a déjà été connecté une fois (flag biometricEnabled).
            val savedUser = sessionManager.getUser()
            val savedToken = sessionManager.getToken()

            if (savedUser != null && savedToken != null) {
                _eventChannel.send(LoginUiEvent.NavigateToDashboard)
            } else {
                _state.update { it.copy(error = "Veuillez vous connecter manuellement une première fois.") }
            }
        }
    }
}