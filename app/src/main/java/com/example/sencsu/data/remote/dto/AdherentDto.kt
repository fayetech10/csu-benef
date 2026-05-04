package com.example.sencsu.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO complet pour un adhérent, utilisé pour la lecture et les opérations courantes.
 * Les champs sont regroupés par thématique pour une meilleure lisibilité.
 */
data class AdherentDto(
    val id: String? = null,

    // ── Identité ──
    @SerializedName("prenoms")
    val prenoms: String? = "",
    @SerializedName("nom")
    val nom: String? = "",
    val sexe: String? = "M",
    val dateNaissance: String? = "",
    val lieuNaissance: String? = "",
    val lieuDeNaissance: String? = "",
    @SerializedName("situationMatrimoniale")
    val situationM: String? = null,

    // ── Coordonnées & Localisation ──
    val adresse: String? = "",
    val whatsapp: String? = "",
    val region: String? = "Thiès",
    val departement: String? = "",
    val commune: String? = "",

    // ── Identifiants & Cartes ──
    @SerializedName("matricule")
    val matricule: String? = null,
    @SerializedName("numeroCNi")
    val numeroCNi: String? = "",
    val numeroPiece: String? = "",
    val typePiece: String? = "CNI",
    val numeroCarte: String? = "",
    val codeBarres: String? = null,
    val codeBar: String? = null,
    val qrCodeUrl: String? = null,
    val clientUUID: String? = "",

    // ── Adhésion & Statut ──
    val typeBenef: String? = "CLASSIQUE",
    val typeAdhesion: String? = "FAMALE",
    val regime: String? = "CONTRIBUTIF",
    val secteurActivite: String? = null,
    val coveragePeriod: String? = null,
    val statut: String? = "ACTIVE",
    val actif: Boolean? = true,
    val createdAt: String? = "",

    // ── Médias & Branding ──
    val photo: String? = null,
    val photoRecto: String? = null,
    val photoVerso: String? = null,
    val flagUrl: String? = null,
    val logoUrl: String? = null,

    // ── Finances ──
    val montantTotal: Double? = 0.0,

    // ── Relations ──
    val personnesCharge: List<PersonneChargeDto> = emptyList()
)

/**
 * DTO simplifié dédié à la mise à jour des informations d'un adhérent.
 */
data class AdherentUpdateDto(
    val nom: String,
    val prenoms: String,
    val adresse: String,
    val lieuNaissance: String,
    val sexe: String,
    val dateNaissance: String, // Format attendu: yyyy-MM-dd
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
    val typePiece: String,
    val numeroPiece: String,
    val numeroCNi: String,
    val personnesCharge: List<PersonneChargeDto>? = null
)
