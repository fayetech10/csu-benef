package com.example.sencsu.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO pour l'agent (utilisateur connecté).
 * Aligné avec le AuthResponse.UserDto du backend.
 */
data class AgentDto(
    val id: String? = null,
    val actif: Boolean? = true,

    @SerializedName("nom")
    val name: String? = "",

    @SerializedName("prenoms")
    val prenom: String? = "",

    val email: String? = "",
    val role: String? = "",
    val telephone: String? = "",
    val photo: String? = null
)
