package com.example.sencsu.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Réponse API générique encapsulant un objet data.
 */
data class ApiResponse<T>(
    val success: Boolean,
    val message: String? = null,
    val data: T
)

/**
 * Réponse API pour les paiements (non-générique).
 */
@Serializable
data class ApiResponseP(
    val success: Boolean = true,
    val message: String = "",
    val data: String? = null
)

/**
 * Structure pour parser les erreurs du backend.
 * S'adapte aux formats courants : {"message": "..."} ou {"error": "..."}
 */
@Serializable
data class ErrorResponse(
    @SerialName("message")
    val message: String? = null,

    @SerialName("error")
    val error: String? = null,

    @SerialName("errors")
    val errors: List<String>? = null
) {
    fun getErrorMessage(): String {
        return message
            ?: error
            ?: errors?.joinToString(", ")
            ?: "Erreur inconnue"
    }
}

/**
 * Exception spécifique pour les erreurs de validation (400)
 */
class ValidationException(message: String) : Exception(message)

/**
 * Réponse de création d'adhérent.
 */
data class CreateAdherentResponse(
    val success: Boolean,
    val message: String,
    val data: AdherentIdResponse
)

/**
 * ID de l'adhérent retourné après création.
 */
data class AdherentIdResponse(
    @com.google.gson.annotations.SerializedName("adherent_id")
    val adherentId: String,
    
    val matricule: String? = null,
    
    @com.google.gson.annotations.SerializedName("default_password")
    val defaultPassword: String? = null
)

/**
 * Réponse du dashboard (liste d'adhérents).
 */
data class DashboardResponseDto(
    val message: String = "",
    val success: Boolean = false,
    val data: List<AdherentDto> = emptyList()
)

/**
 * Réponse d'upload de fichier.
 */
data class UploadResponse(
    @com.google.gson.annotations.SerializedName("filename") val filename: String?,
    @com.google.gson.annotations.SerializedName("url") val url: String?
)
