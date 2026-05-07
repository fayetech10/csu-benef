package com.example.sencsu

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.example.sencsu.domain.repository.IAdherentRepository
import com.example.sencsu.data.repository.SessionManager
import com.example.sencsu.navigation.AppNavigation
import com.example.sencsu.theme.AppTheme
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    @Inject
    lateinit var adherentRepository: IAdherentRepository

    @Inject
    lateinit var sessionManager: SessionManager

    companion object {
        private const val TAG = "MainActivity"
    }

    // Gestionnaire de demande de permission pour Android 13+ (API 33)
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d(TAG, "Permission de notification accordée par l'utilisateur.")
            fetchAndSyncFcmToken()
        } else {
            Log.w(TAG, "Permission de notification refusée par l'utilisateur.")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Demande de permission pour Android 13+
        askNotificationPermission()

        // Récupération et synchronisation réactive du token FCM
        observeUserSessionAndSyncToken()

        setContent {
            AppTheme {
                AppNavigation()
            }
        }
    }

    /**
     * Demande la permission de notification pour Android 13 (Tiramisu) et versions supérieures.
     */
    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                Log.d(TAG, "La permission de notification est déjà accordée.")
                fetchAndSyncFcmToken()
            } else {
                Log.d(TAG, "Demande explicite de la permission de notification...")
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            // Pas besoin de demander la permission avant l'API 33, c'est accordé par défaut
            fetchAndSyncFcmToken()
        }
    }

    /**
     * Récupère le token FCM de l'appareil et tente de le synchroniser si un utilisateur est déjà connecté.
     */
    private fun fetchAndSyncFcmToken() {
        lifecycleScope.launch {
            try {
                Log.d(TAG, "Récupération du token FCM en cours...")
                val token = FirebaseMessaging.getInstance().token.await()
                Log.d(TAG, "Token FCM récupéré avec succès : $token")
                
                // Sauvegarder localement dans le matricule ou journaliser
                val user = sessionManager.getUser()
                if (user != null && user.id != null) {
                    syncTokenWithServer(user.id, token)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Impossible de récupérer le token FCM", e)
            }
        }
    }

    /**
     * Observe réactivement l'état de la session utilisateur. 
     * Dès qu'un utilisateur se connecte, son token FCM lui est automatiquement rattaché.
     */
    private fun observeUserSessionAndSyncToken() {
        lifecycleScope.launch {
            sessionManager.userFlow.collectLatest { user ->
                if (user != null && user.id != null) {
                    try {
                        Log.d(TAG, "Session active détectée pour l'utilisateur ${user.id}. Récupération et liaison du token FCM...")
                        val token = FirebaseMessaging.getInstance().token.await()
                        syncTokenWithServer(user.id, token)
                    } catch (e: Exception) {
                        Log.e(TAG, "Échec de récupération du token FCM lors de l'observation de session", e)
                    }
                }
            }
        }
    }

    /**
     * Effectue l'appel API réseau pour lier le token FCM à l'adhérent côté backend.
     */
    private suspend fun syncTokenWithServer(userId: String, token: String) {
        val result = adherentRepository.registerFcmToken(userId, token)
        if (result.isSuccess) {
            Log.d(TAG, "Félicitations ! Le token FCM a été lié à l'utilisateur $userId côté backend.")
        } else {
            Log.e(TAG, "Échec de la liaison du token FCM pour l'utilisateur $userId", result.exceptionOrNull())
        }
    }
}
