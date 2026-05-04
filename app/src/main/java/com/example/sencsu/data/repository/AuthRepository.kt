package com.example.sencsu.data.repository

import com.example.sencsu.data.remote.ApiService
import com.example.sencsu.data.remote.dto.LoginRequest
import com.example.sencsu.data.remote.dto.LoginResponse
import com.example.sencsu.domain.repository.IAuthRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository gérant l'authentification des agents et des adhérents.
 * Centralise les appels vers l'API d'authentification.
 */
@Singleton
class AuthRepository @Inject constructor(
    private val apiService: ApiService
) : BaseRepository(), IAuthRepository {

    override suspend fun login(email: String, password: String): LoginResponse {
        val result = safeApiCall {
            val apiResponse = apiService.login(LoginRequest(email, password))
            if (!apiResponse.success || apiResponse.data == null) {
                throw Exception(apiResponse.message ?: "Identifiants incorrects")
            }
            val data = apiResponse.data
            LoginResponse(
                accessToken = data.accessToken,
                refreshToken = data.refreshToken,
                tokenType = data.tokenType ?: "Bearer",
                expiresIn = data.expiresIn ?: 0L,
                user = data.user
            )
        }
        return result.getOrThrow()
    }

    override suspend fun adherentLogin(matricule: String, password: String): LoginResponse {
        val result = safeApiCall {
            val apiResponse = apiService.adherentLogin(LoginRequest(matricule, password))
            if (!apiResponse.success || apiResponse.data == null) {
                throw Exception(apiResponse.message ?: "Matricule ou mot de passe incorrect")
            }
            val data = apiResponse.data
            LoginResponse(
                accessToken = data.accessToken,
                refreshToken = data.refreshToken,
                tokenType = data.tokenType ?: "Bearer",
                expiresIn = data.expiresIn ?: 0L,
                user = data.user
            )
        }
        return result.getOrThrow()
    }
}