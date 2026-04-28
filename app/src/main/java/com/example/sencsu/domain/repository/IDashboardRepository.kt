package com.example.sencsu.domain.repository

import com.example.sencsu.data.remote.dto.AdherentDto
import com.example.sencsu.data.remote.dto.AdherentIdResponse
import com.example.sencsu.data.remote.dto.AdherentUpdateDto
import com.example.sencsu.data.remote.dto.DashboardResponseDto

interface IDashboardRepository {
    suspend fun getDashboardData(): DashboardResponseDto
    suspend fun getAdherentsByAgentId(agentId: String): Result<List<AdherentDto>>
    suspend fun ajouterAdherent(adherent: AdherentDto): Result<AdherentIdResponse>
    suspend fun updatePassword(id: String, oldPassword: String, password: String): Result<Unit>
    suspend fun updateAdherent(id: String, adherent: AdherentUpdateDto): Result<AdherentUpdateDto>
}
