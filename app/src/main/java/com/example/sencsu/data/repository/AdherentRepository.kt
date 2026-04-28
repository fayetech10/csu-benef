package com.example.sencsu.data.repository

import com.example.sencsu.data.remote.ApiService
import com.example.sencsu.data.remote.dto.AdherentDto
import com.example.sencsu.data.remote.dto.PersonneChargeDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

import com.example.sencsu.domain.repository.IAdherentRepository

@Singleton
class AdherentRepository @Inject constructor(
    private val apiService: ApiService
) : IAdherentRepository {

    override suspend fun getAdherentById(id: String): Result<AdherentDto> {
        return try {
            val response = apiService.getAdherentById(id)
            Result.success(response.data)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    
    override suspend fun getMyInfo(): Result<AdherentDto> {
        return try {
            val response = apiService.getMyInfo()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteAdherent(id: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                apiService.deleteAdherent(id)
                Result.success(Unit)
            } catch (e: IOException) {
                // Erreur réseau (pas d'internet)
                Result.failure(Exception("Problème de connexion internet"))
            } catch (e: HttpException) {
                // Erreur API (404, 500, etc.)
                Result.failure(Exception("Erreur serveur : ${e.code()}"))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun addPersonneCharge(adherentId: String, personne: PersonneChargeDto): Result<PersonneChargeDto> {
        return withContext(Dispatchers.IO) {
            try {
                val result = apiService.addPersonneCharge(adherentId, personne)
                Result.success(result)
            } catch (e: HttpException) {
                Result.failure(Exception("Erreur HTTP: ${e.code()} - ${e.message}"))
            } catch (e: IOException) {
                Result.failure(Exception("Erreur de connexion: ${e.message}"))
            } catch (e: Exception) {
                Result.failure(Exception("Erreur inattendue: ${e.message}"))
            }
        }
    }

    override suspend fun deletePersonneCharge(adherentId: String, pcId: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                apiService.deletePersonneCharge(adherentId, pcId)
                Result.success(Unit)
            } catch (e: HttpException) {
                Result.failure(Exception("Erreur HTTP: ${e.code()} - ${e.message}"))
            } catch (e: IOException) {
                Result.failure(Exception("Erreur de connexion: ${e.message}"))
            } catch (e: Exception) {
                Result.failure(Exception("Erreur inattendue: ${e.message}"))
            }
        }
    }

    override suspend fun updatePersonneCharge(
        adherentId: String,
        pcId: String,
        personne: PersonneChargeDto
    ): Result<PersonneChargeDto> {
        return withContext(Dispatchers.IO) {
            try {
                val result = apiService.updatePersonneCharge(adherentId, pcId, personne)
                Result.success(result)
            } catch (e: HttpException) {
                Result.failure(Exception("Erreur HTTP: ${e.code()} - ${e.message}"))
            } catch (e: IOException) {
                Result.failure(Exception("Erreur de connexion: ${e.message}"))
            } catch (e: Exception) {
                Result.failure(Exception("Erreur inattendue: ${e.message}"))
            }
        }
    }
}
