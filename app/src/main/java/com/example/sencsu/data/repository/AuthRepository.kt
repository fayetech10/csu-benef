package com.example.sencsu.data.repository

import android.util.Log
import com.example.sencsu.data.remote.ApiService
import com.example.sencsu.data.remote.dto.LoginRequest
import com.example.sencsu.data.remote.dto.LoginResponse
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

import com.example.sencsu.domain.repository.IAuthRepository

/**
 * Repository d'authentification.
 * Utilise Retrofit (apiService) pour appeler le backend via /api/agent/login.
 * L'URL est centralisée dans ApiConfig.BASE_URL.
 */
class AuthRepository @Inject constructor(
    private val apiService: ApiService
) : IAuthRepository {

    override suspend fun login(email: String, password: String): LoginResponse = withContext(Dispatchers.IO) {
        try {
            val apiResponse = apiService.login(LoginRequest(email, password))

            Log.d("AuthRepository", "Login response: success=${apiResponse.success}, message=${apiResponse.message}")

            if (!apiResponse.success || apiResponse.data == null) {
                throw Exception(apiResponse.message ?: "Échec de la connexion")
            }

            val loginData = apiResponse.data

            // Mapper vers un LoginResponse propre
            LoginResponse(
                accessToken = loginData.accessToken,
                refreshToken = loginData.refreshToken,
                tokenType = loginData.tokenType ?: "Bearer",
                expiresIn = loginData.expiresIn ?: 0L,
                user = loginData.user
            )
        } catch (e: retrofit2.HttpException) {
            handleAuthError(e)
        } catch (e: java.io.IOException) {
            throw Exception("Impossible de se connecter au serveur. Vérifiez votre connexion.")
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun adherentLogin(matricule: String, password: String): LoginResponse = withContext(Dispatchers.IO) {
        try {
            val apiResponse = apiService.adherentLogin(LoginRequest(matricule, password))
            if (!apiResponse.success || apiResponse.data == null) {
                throw Exception(apiResponse.message ?: "Échec de la connexion")
            }
            val loginData = apiResponse.data
            LoginResponse(
                accessToken = loginData.accessToken,
                refreshToken = loginData.refreshToken,
                tokenType = loginData.tokenType ?: "Bearer",
                expiresIn = loginData.expiresIn ?: 0L,
                user = loginData.user
            )
        } catch (e: retrofit2.HttpException) {
            handleAuthError(e)
        } catch (e: java.io.IOException) {
            throw Exception("Impossible de se connecter au serveur. Vérifiez votre connexion.")
        } catch (e: Exception) {
            throw e
        }
    }

    private fun handleAuthError(e: retrofit2.HttpException): Nothing {
        val errorBody = e.response()?.errorBody()?.string()
        Log.e("AuthRepository", "Auth HTTP error ${e.code()}: $errorBody")
        val errorMessage = when (e.code()) {
            401 -> "Matricule ou mot de passe incorrect"
            403 -> "Compte désactivé"
            404 -> "Compte non trouvé"
            else -> "Erreur serveur (${e.code()})"
        }
        throw Exception(errorMessage)
    }
}