package com.example.sencsu.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO pour un adhérent (lecture/écriture).
 * Tous les champs sont nullable pour éviter les crash NullPointerException
 * lors du parsing JSON (le backend peut retourner null).
 */
data class AdherentDto(
    val id: String? = null,

    @SerializedName("prenoms")
    val prenoms: String? = "",

    val nom: String? = "",
    val flagUrl: String? = null,
    val logoUrl: String? = null,
    val adresse: String? = "",
    val qrCodeUrl: String? = null,
    val lieuNaissance: String? = "",
    val statut: String? = "ACTIVE",
    val createdAt: String? = "",
    val sexe: String? = "M",
    val numeroCarte: String? = "",
    val dateNaissance: String? = "",
    val lieuDeNaissance: String? = "",
    val typeBenef: String? = "",

    @SerializedName("situationMatrimoniale")
    val situationM: String? = null,

    val whatsapp: String? = "",
    val codeBarres: String? = null,
    val secteurActivite: String? = null,
    val typePiece: String? = "CNI",
    val numeroPiece: String? = "",
    val numeroCNi: String? = "",
    val departement: String? = "",
    val commune: String? = "",
    val region: String? = "Thiès",
    val photo: String? = null,
    val typeAdhesion: String? = null,
    val montantTotal: Double? = 0.0,
    val regime: String? = null,
    val photoRecto: String? = null,
    val matricule: String? = null,
    val codeBar: String? = null,
    val photoVerso: String? = null,
    val actif: Boolean? = true,
    val clientUUID: String? = "",

    val personnesCharge: List<PersonneChargeDto> = emptyList()
)

/**
 * DTO pour la mise à jour d'un adhérent (écriture uniquement).
 */
data class AdherentUpdateDto(
    val nom: String,
    val prenoms: String,
    val adresse: String,
    val lieuNaissance: String,
    val sexe: String,
    val dateNaissance: String, // yyyy-MM-dd
    val situationMatrimoniale: String,
    val whatsapp: String,
    val secteurActivite: String?,
    val region: String,
    val departement: String,
    val commune: String,
    val regime: String,
    val typeBenef: String,
    val typeAdhesion: String,
    val photo: String?,
    val photoRecto: String?,
    val photoVerso: String?,
    val personnesCharge: List<PersonneChargeDto>? = null,
    val typePiece: String,
    val numeroPiece: String,
    val numeroCNi: String,
)
