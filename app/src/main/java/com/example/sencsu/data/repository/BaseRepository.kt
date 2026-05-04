package com.example.sencsu.data.repository

import com.example.sencsu.data.remote.dto.ErrorResponse
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

/**
 * Classe de base pour les repositories fournissant des utilitaires de gestion d'erreurs API.
 * Centralise la logique de try-catch et le parsing des erreurs JSON du backend.
 */
abstract class BaseRepository {

    private val gson = Gson()

    /**
     * Exécute un appel API de manière sécurisée et transforme les exceptions en Result.
     * Gère les erreurs HTTP (4xx, 5xx), les erreurs réseau (IOException) et les erreurs inattendues.
     */
    protected suspend fun <T> safeApiCall(call: suspend () -> T): Result<T> {
        return withContext(Dispatchers.IO) {
            try {
                Result.success(call())
            } catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                val message = try {
                    if (errorBody != null) {
                        val errorResponse = gson.fromJson(errorBody, ErrorResponse::class.java)
                        errorResponse?.getErrorMessage() ?: "Erreur serveur (${e.code()})"
                    } else {
                        "Erreur serveur (${e.code()})"
                    }
                } catch (ex: Exception) {
                    "Erreur serveur (${e.code()})"
                }
                Result.failure(Exception(message))
            } catch (e: IOException) {
                Result.failure(Exception("Problème de connexion internet. Veuillez vérifier votre réseau."))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
