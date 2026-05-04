package com.example.sencsu.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO pour un service médical.
 */
data class ServiceMedicalDto(
    val id: String? = null,
    val typeService: String? = null,
    val description: String? = null,
    val etablissement: String? = null,
    val medecin: String? = null,
    val montant: Double? = 0.0,
    val montantRembourse: Double? = 0.0,
    val statut: String? = null,
    val dateService: String? = null,
    val beneficiaireNom: String? = null,
    val personneChargeId: String? = null
)

/**
 * DTO pour le résumé des services médicaux.
 */
data class ServicesSummaryDto(
    val totalServices: Int? = 0,
    val parType: Map<String, Int>? = emptyMap(),
    val montantTotal: Double? = 0.0,
    val montantRembourse: Double? = 0.0,
    val parStatut: Map<String, Int>? = emptyMap()
)
