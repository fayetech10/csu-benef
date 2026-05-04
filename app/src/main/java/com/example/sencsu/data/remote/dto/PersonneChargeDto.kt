package com.example.sencsu.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO pour une personne à charge.
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
    var photo: String? = "",
    var photoRecto: String? = "",
    var photoVerso: String? = "",
    @SerializedName("matricule")
    val matricule: String? = null,
    val createdAt: String? = null
)
