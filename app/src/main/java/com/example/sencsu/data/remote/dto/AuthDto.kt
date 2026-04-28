package com.example.sencsu.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO pour la requête de connexion.
 * Le backend attend email + password (/api/agent/login).
 */
data class LoginRequest(
    @SerializedName("email")
    val email: String,

    @SerializedName("password")
    val password: String
)

/**
 * DTO pour la réponse de connexion.
 * Le backend renvoie ApiResponse<AuthResponse>, donc on mappe le contenu de data.
 */
data class LoginResponse(
    @SerializedName("accessToken")
    val accessToken: String?,
    val refreshToken: String?,
    val tokenType: String?,
    val expiresIn: Long?,

    @SerializedName("user")
    val user: AgentDto?
)

/**
 * Wrapper pour la réponse de login du backend.
 * Le backend renvoie: { success: true, message: "...", data: { accessToken, user, ... } }
 */
data class LoginApiResponse(
    val success: Boolean,
    val message: String?,
    val data: LoginResponse?
)
