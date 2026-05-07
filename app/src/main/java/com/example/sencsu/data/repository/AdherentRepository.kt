package com.example.sencsu.data.repository

import com.example.sencsu.data.remote.ApiService
import com.example.sencsu.data.remote.dto.AdherentDto
import com.example.sencsu.data.remote.dto.AdherentUpdateDto
import com.example.sencsu.data.remote.dto.PersonneChargeDto
import com.example.sencsu.domain.repository.IAdherentRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implémentation du repository pour la gestion des adhérents.
 * Utilise [BaseRepository] pour une gestion d'erreurs centralisée.
 */
@Singleton
class AdherentRepository @Inject constructor(
    private val apiService: ApiService
) : BaseRepository(), IAdherentRepository {

    override suspend fun getAdherentById(id: String): Result<AdherentDto> = safeApiCall {
        apiService.getAdherentById(id).data
    }

    override suspend fun getMyInfo(): Result<AdherentDto> = safeApiCall {
        apiService.getMyInfo()
    }

    override suspend fun deleteAdherent(id: String): Result<Unit> = safeApiCall {
        apiService.deleteAdherent(id)
        Unit
    }

    override suspend fun addPersonneCharge(
        adherentId: String,
        personne: PersonneChargeDto
    ): Result<PersonneChargeDto> = safeApiCall {
        apiService.addPersonneCharge(adherentId, personne)
    }

    override suspend fun deletePersonneCharge(adherentId: String, pcId: String): Result<Unit> = safeApiCall {
        apiService.deletePersonneCharge(adherentId, pcId)
        Unit
    }

    override suspend fun updatePersonneCharge(
        adherentId: String,
        pcId: String,
        personne: PersonneChargeDto
    ): Result<PersonneChargeDto> = safeApiCall {
        apiService.updatePersonneCharge(adherentId, pcId, personne)
    }

    override suspend fun updateAdherent(id: String, adherent: AdherentUpdateDto): Result<AdherentUpdateDto> = safeApiCall {
        apiService.updateAdherent(id, adherent).data
    }

    override suspend fun updatePassword(id: String, oldPassword: String, password: String): Result<Unit> = safeApiCall {
        val response = apiService.updatePassword(
            id,
            mapOf(
                "old_password" to oldPassword,
                "password" to password
            )
        )
        if (!response.isSuccessful) {
            throw Exception("Erreur serveur (${response.code()})")
        }
        Unit
    }

    override suspend fun scanAdherent(matricule: String): Result<AdherentDto> = safeApiCall {
        apiService.scanAdherent(matricule).data
    }

    override suspend fun registerFcmToken(id: String, token: String): Result<Unit> = safeApiCall {
        apiService.registerFcmToken(id, mapOf("token" to token))
        Unit
    }
}
