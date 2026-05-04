package com.example.sencsu.domain.repository

import com.example.sencsu.data.remote.dto.AdherentDto
import com.example.sencsu.data.remote.dto.PersonneChargeDto

interface IAdherentRepository {
    suspend fun getAdherentById(id: String): Result<AdherentDto>
    suspend fun deleteAdherent(id: String): Result<Unit>
    suspend fun getMyInfo(): Result<AdherentDto>
    suspend fun addPersonneCharge(adherentId: String, personne: PersonneChargeDto): Result<PersonneChargeDto>
    suspend fun deletePersonneCharge(adherentId: String, pcId: String): Result<Unit>
    suspend fun updatePersonneCharge(adherentId: String, pcId: String, personne: PersonneChargeDto): Result<PersonneChargeDto>
    suspend fun scanAdherent(matricule: String): Result<AdherentDto>
}
