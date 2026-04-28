package com.example.sencsu.data.repository

import android.util.Log
import com.example.sencsu.data.remote.ApiService
import com.example.sencsu.data.remote.dto.AdherentDto
import com.example.sencsu.data.remote.dto.AdherentIdResponse
import com.example.sencsu.data.remote.dto.AdherentUpdateDto
import com.example.sencsu.data.remote.dto.DashboardResponseDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

import com.example.sencsu.domain.repository.IDashboardRepository

class DashboardRepository @Inject constructor(
    private val apiService: ApiService
) : IDashboardRepository {
    override suspend fun getDashboardData(): DashboardResponseDto {
        return apiService.getDashboardData()
    }


    override suspend fun getAdherentsByAgentId(agentId: String): Result<List<AdherentDto>> {
        return try {
            val response = apiService.getAdherentsByAgentId(agentId)
            Result.success(response.data)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }




    override suspend fun ajouterAdherent(adherent: AdherentDto): Result<AdherentIdResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.createAdherent(adherent)
                if (response.success) {
                    val adherentCree = response.data
                    Result.success(adherentCree)
                } else {
                    Log.e("DashboardRepository", "Erreur lors de la création de l'adhérent")
                    Result.failure(Exception(response.message))
                }
            } catch (e: HttpException) {
                Log.e("DashboardRepository", "Erreur HTTP: ${e.message}")
                Result.failure(e)
            } catch (e: IOException) {
                Log.e("DashboardRepository", "Erreur de connexion: ${e.message}")
                Result.failure(e)
            } catch (e: Exception) {
                Log.e("DashboardRepository", "Erreur inattendue: ${e.message}")
                Result.failure(e)
            }
        }
    }

    override suspend fun updatePassword(id: String, oldPassword: String, password: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.updatePassword(id, mapOf(
                    "old_password" to oldPassword,
                    "password" to password
                ))
                if (response.isSuccessful) {
                    Result.success(Unit)
                } else {
                    val errorBody = response.errorBody()?.string()
                    val message = try {
                        val error = com.google.gson.Gson().fromJson(errorBody, com.example.sencsu.data.remote.dto.ErrorResponse::class.java)
                        error.getErrorMessage()
                    } catch (e: Exception) {
                        "Erreur serveur (${response.code()})"
                    }
                    Result.failure(Exception(message))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun updateAdherent(id: String, adherent: AdherentUpdateDto): Result<AdherentUpdateDto> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.updateAdherent(id, adherent)
                if (response.success) {
                    Result.success(response.data)
                } else {
                    Result.failure(Exception("Mise à jour échouée"))
                }
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

