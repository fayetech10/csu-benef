package com.example.sencsu.data.remote.dto

data class PaiementDto(
    val id: String? = null,
    val reference: String,
    val montant: Double,
    val modePaiement: String,
    val photoPaiement: String?,
    val adherentId: String,
    val photos: List<String>?,
    val datePaiement: String? = null
)

