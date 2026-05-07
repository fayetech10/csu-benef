package com.example.sencsu.domain.sync

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.sencsu.MainActivity
import com.example.sencsu.R
import com.example.sencsu.domain.repository.IAdherentRepository
import com.example.sencsu.data.repository.SessionManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Service de gestion des notifications push de Firebase Cloud Messaging (FCM).
 * Reçoit les tokens et les messages entrants, et affiche des notifications systèmes premium.
 */
@AndroidEntryPoint
class MyFirebaseMessagingService : FirebaseMessagingService() {

    private val job = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.IO + job)

    @Inject
    lateinit var adherentRepository: IAdherentRepository

    @Inject
    lateinit var sessionManager: SessionManager

    companion object {
        private const val TAG = "FCMService"
        private const val CHANNEL_SERVICES_ID = "medical_services"
        private const val CHANNEL_RENEWALS_ID = "renewals"
    }

    /**
     * Appelé lorsque Firebase attribue un nouveau token à l'appareil.
     * Envoie automatiquement le token au backend si l'utilisateur est connecté.
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Nouveau token FCM généré : $token")
        
        // Envoi au backend si l'utilisateur est déjà connecté
        serviceScope.launch {
            try {
                val user = sessionManager.getUser()
                if (user != null && user.id != null) {
                    Log.d(TAG, "Utilisateur connecté trouvé (${user.id}). Enregistrement du token FCM...")
                    val result = adherentRepository.registerFcmToken(user.id, token)
                    if (result.isSuccess) {
                        Log.d(TAG, "Token FCM synchronisé avec succès avec le serveur backend.")
                    } else {
                        Log.e(TAG, "Échec de la synchronisation du token FCM avec le serveur backend", result.exceptionOrNull())
                    }
                } else {
                    Log.d(TAG, "Aucun utilisateur connecté pour le moment. Le token FCM sera enregistré lors de la connexion.")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Erreur inattendue lors de l'enregistrement du token FCM", e)
            }
        }
    }

    /**
     * Appelé lors de la réception d'un message push FCM en tâche de fond ou premier plan.
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "Notification FCM reçue de : ${remoteMessage.from}")

        // 1. Lire les données du payload
        val data = remoteMessage.data
        val type = data["type"] ?: ""
        val serviceId = data["serviceId"] ?: ""
        val status = data["status"] ?: ""
        val adherentId = data["adherentId"] ?: ""

        // 2. Extraire les détails visuels (notification)
        val title = remoteMessage.notification?.title ?: data["title"] ?: "SenCSU Notification"
        val body = remoteMessage.notification?.body ?: data["body"] ?: ""

        Log.d(TAG, "Contenu de la notification - Titre : $title, Corps : $body, Type : $type")

        // 3. Déterminer le canal de notification en fonction du type
        val channelId = if (type == "renewal_reminder") CHANNEL_RENEWALS_ID else CHANNEL_SERVICES_ID

        // 4. Afficher la notification système
        sendNotification(title, body, channelId, type, serviceId, adherentId)
    }

    /**
     * Construit et affiche la notification graphique avec styles et deep linking.
     */
    private fun sendNotification(
        title: String,
        body: String,
        channelId: String,
        type: String,
        serviceId: String,
        adherentId: String
    ) {
        // Préparer l'intent pour ouvrir l'application
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            // Ajouter les extras pour le routage de deep link si nécessaire
            putExtra("notification_type", type)
            putExtra("service_id", serviceId)
            putExtra("adherent_id", adherentId)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        // Conception de la notification visuelle premium
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Utilise l'icône système par défaut robuste
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)
            .setPriority(if (channelId == CHANNEL_SERVICES_ID) NotificationCompat.PRIORITY_HIGH else NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setColor(Color.parseColor("#009688")) // Teal brand color
            .setStyle(NotificationCompat.BigTextStyle().bigText(body)) // Style extensible pour les longs textes

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Configuration des canaux de notification pour Android Oreo (API 26) et versions supérieures
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = if (channelId == CHANNEL_SERVICES_ID) "Services Médicaux" else "Renouvellements"
            val descriptionText = if (channelId == CHANNEL_SERVICES_ID) {
                "Notifications de validation de soins et remboursements"
            } else {
                "Rappels d'expiration de votre couverture santé"
            }
            val importance = if (channelId == CHANNEL_SERVICES_ID) {
                NotificationManager.IMPORTANCE_HIGH
            } else {
                NotificationManager.IMPORTANCE_DEFAULT
            }

            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
                enableLights(true)
                lightColor = Color.GREEN
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Afficher la notification avec un ID unique pour éviter l'écrasement
        val notificationId = System.currentTimeMillis().toInt()
        notificationManager.notify(notificationId, notificationBuilder.build())
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel() // Nettoyer les coroutines en cours
    }
}
