package com.example.sencsu.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Enveloppe générique pour les réponses API standard.
 */
data class ApiResponse<T>(
    val success: Boolean,
    val message: String? = null,
    val data: T
)

/**
 * Réponse spécifique pour les listes d'adhérents (Dashboard).
 */
data class DashboardResponseDto(
    val success: Boolean = false,
    val message: String = "",
    val data: List<AdherentDto> = emptyList()
)

/**
 * Réponse après création réussie d'un adhérent.
 */
data class CreateAdherentResponse(
    val success: Boolean,
    val message: String,
    val data: AdherentIdResponse
)

/**
 * Détails de l'adhérent créé (ID, matricule, mot de passe).
 */
data class AdherentIdResponse(
    @SerializedName("adherentId")
    val adherentId: String,
    val matricule: String? = null,
    @SerializedName("passwordInfo")
    val defaultPassword: String? = null
)

/**
 * Réponse pour l'upload de fichiers.
 */
data class UploadResponse(
    val filename: String?,
    val url: String?
)

/**
 * Structure d'erreur standard renvoyée par le backend.
 */
data class ErrorResponse(
    val message: String? = null,
    val error: String? = null,
    val errors: List<String>? = null
) {
    /**
     * Extrait le message d'erreur le plus pertinent.
     */
    fun getErrorMessage(): String {
        return message
            ?: error
            ?: errors?.joinToString(", ")
            ?: "Une erreur inconnue s'est produite"
    }
}

/**
 * Réponse spécifique pour les paiements (Legacy).
 */
data class ApiResponseP(
    val success: Boolean = true,
    val message: String = "",
    val data: Any? = null
)

/**
 * Exception personnalisée pour les erreurs de validation côté client.
 */
class ValidationException(message: String) : Exception(message)
