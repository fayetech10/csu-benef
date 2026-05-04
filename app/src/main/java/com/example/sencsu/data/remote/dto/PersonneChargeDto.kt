package com.example.sencsu.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO pour une personne à charge.
 * Tous les champs sont immuables (val) pour garantir la cohérence des données.
 */
data class PersonneChargeDto(
    val id: String? = null,
    val prenoms: String? = "",
    val nom: String? = "",
    val dateNaissance: String? = "",
    val sexe: String? = "M",
    val lieuNaissance: String? = "",
    val adresse: String? = "",
    val whatsapp: String? = "",
    val lienParent: String? = "",

    @SerializedName("situationM")
    val situationM: String? = null,

    val numeroCNi: String? = null,
    val typePiece: String? = "CNI",
    val numeroExtrait: String? = "",
    val photo: String? = "",
    val photoRecto: String? = "",
    val photoVerso: String? = "",
    @SerializedName("matricule")
    val matricule: String? = null,
    val createdAt: String? = null
) {
    /** Nom complet pour affichage UI */
    val displayName: String
        get() = "${prenoms ?: ""} ${nom ?: ""}".trim().ifEmpty { "Inconnu" }

    /** Initiales pour avatar */
    val initials: String
        get() = displayName.split(" ")
            .take(2)
            .mapNotNull { it.firstOrNull()?.uppercase() }
            .joinToString("")
            .ifEmpty { "?" }
}
