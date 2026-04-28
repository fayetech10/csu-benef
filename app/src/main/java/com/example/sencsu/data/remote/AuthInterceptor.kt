package com.example.sencsu.data.remote

import com.example.sencsu.data.repository.SessionManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject

/**
 * Intercepteur HTTP pour ajouter le token JWT aux requêtes.
 * Utilise un cache AtomicReference pour éviter le runBlocking sur chaque requête.
 * Le token est rafraîchi uniquement quand le cache est vide.
 */
class AuthInterceptor @Inject constructor(
    private val sessionManager: SessionManager
) : Interceptor {

    private val cachedToken = AtomicReference<String?>(null)

    override fun intercept(chain: Interceptor.Chain): Response {
        // Lire depuis le cache d'abord, charger depuis DataStore seulement si nécessaire
        val token = cachedToken.get() ?: runBlocking {
            sessionManager.tokenFlow.first()?.also { cachedToken.set(it) }
        }

        val request = chain.request().newBuilder()
        if (!token.isNullOrBlank()) {
            request.addHeader("Authorization", "Bearer $token")
        }

        val response = chain.proceed(request.build())

        // Vérifier si le token a expiré (erreur 401)
        if (response.code == 401) {
            cachedToken.set(null) // Invalider le cache
            runBlocking {
                sessionManager.clearSessionAndNotify()
            }
        }

        return response
    }

    /** Appelé par SessionManager lors du login/logout pour mettre à jour le cache. */
    fun updateCachedToken(token: String?) {
        cachedToken.set(token)
    }

    /** Invalider le cache (appelé lors du logout). */
    fun clearCachedToken() {
        cachedToken.set(null)
    }
}
