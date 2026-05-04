package com.example.sencsu.domain.viewmodel

import com.example.sencsu.data.remote.dto.AdherentDto
import com.example.sencsu.data.remote.dto.ServiceMedicalDto

/**
 * État UI du dashboard bénéficiaire.
 * Séparé du ViewModel pour la maintenabilité et la réutilisation.
 */
data class BeneficiaryDashboardUiState(
    val adherent: AdherentDto? = null,
    val recentServices: List<ServiceMedicalDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
