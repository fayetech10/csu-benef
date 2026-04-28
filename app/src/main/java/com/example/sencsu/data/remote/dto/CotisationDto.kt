package com.example.sencsu.data.remote.dto

/**
 * DTO pour une cotisation.
 */
data class CotisationDto(
    val id: String? = null,
    val dateDebut: String? = "",
    val dateFin: String? = "",
    val dateSoumission: String? = "",
    val adherentId: String? = null
)
