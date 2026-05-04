package com.example.sencsu.utils

/**
 * Validateurs centralisés pour les formulaires SenCSU.
 * Chaque méthode retourne un message d'erreur ou null si valide.
 */
object Validators {

    fun validatePrenom(value: String): String? = when {
        value.isBlank() -> "Le prénom est requis"
        value.trim().length < 2 -> "Minimum 2 caractères"
        else -> null
    }

    fun validateNom(value: String): String? = when {
        value.isBlank() -> "Le nom est requis"
        value.trim().length < 2 -> "Minimum 2 caractères"
        else -> null
    }

    fun validateDateNaissance(value: String): String? = when {
        value.isBlank() -> null // Optionnel
        !value.matches(Regex("\\d{4}-\\d{2}-\\d{2}")) -> "Format attendu : AAAA-MM-JJ"
        else -> null
    }

    fun validatePhone(value: String): String? = when {
        value.isBlank() -> null // Optionnel
        !value.matches(Regex("^(\\+221)?[7][0-9]{8}$")) -> "Numéro sénégalais invalide"
        else -> null
    }

    fun validateNumeroCNI(value: String): String? = when {
        value.isBlank() -> null
        value.length < 10 -> "Numéro CNI trop court"
        else -> null
    }
}
