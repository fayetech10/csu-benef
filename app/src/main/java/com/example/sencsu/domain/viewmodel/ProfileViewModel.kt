package com.example.sencsu.domain.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sencsu.data.remote.dto.AgentDto
import com.example.sencsu.data.repository.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel dédié au ProfileScreen.
 * Découplé du DashboardViewModel pour éviter les conflits de scope.
 */
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val sessionManager: SessionManager
) : ViewModel() {

    /** Agent actuellement connecté. */
    val agent: StateFlow<AgentDto?> = sessionManager.userFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /** Initiales de l'agent pour l'avatar. */
    val initials: StateFlow<String> = agent.map { user ->
        val p = user?.prenom?.firstOrNull()?.uppercase() ?: ""
        val n = user?.name?.firstOrNull()?.uppercase() ?: ""
        "$p$n".ifEmpty { "?" }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "?")

    /** Nom complet de l'agent. */
    val fullName: StateFlow<String> = agent.map { user ->
        "${user?.prenom ?: ""} ${user?.name ?: ""}".trim().ifEmpty { "Agent" }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "Agent")

    /** Rôle formaté de l'agent. */
    val role: StateFlow<String> = agent.map { user ->
        user?.role?.replaceFirstChar { it.uppercase() } ?: "Agent terrain"
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "Agent terrain")

    /**
     * Déconnexion de l'utilisateur.
     */
    fun logout() {
        viewModelScope.launch {
            sessionManager.clearSessionAndNotify()
        }
    }
}
