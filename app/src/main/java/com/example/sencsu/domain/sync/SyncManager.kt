package com.example.sencsu.domain.sync

import android.util.Log
import com.example.sencsu.data.local.dao.AdherentDao
import com.example.sencsu.data.local.dao.PaiementDao
import com.example.sencsu.data.remote.dto.AdherentDto
import com.example.sencsu.data.remote.dto.PaiementDto
import com.example.sencsu.data.repository.SessionManager
import com.example.sencsu.domain.repository.IAdherentRepository
import com.example.sencsu.domain.repository.IDashboardRepository
import com.example.sencsu.domain.repository.IPaiementRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Résultat de la synchronisation, permettant de notifier l'UI
 * du nombre de succès et d'erreurs.
 */
data class SyncResult(
    val adherentsSynced: Int = 0,
    val adherentsFailed: Int = 0,
    val paiementsSynced: Int = 0,
    val paiementsFailed: Int = 0,
    val errors: List<String> = emptyList()
) {
    val totalSynced get() = adherentsSynced + paiementsSynced
    val totalFailed get() = adherentsFailed + paiementsFailed
    val isFullySuccessful get() = totalFailed == 0 && totalSynced > 0
    val hasPartialFailure get() = totalFailed > 0 && totalSynced > 0
}

@Singleton
class SyncManager @Inject constructor(
    private val adherentDao: AdherentDao,
    private val paiementDao: PaiementDao,
    private val dashboardRepository: IDashboardRepository,
    private val paiementRepository: IPaiementRepository,
    private val sessionManager: SessionManager
) {
    companion object {
        private const val TAG = "SyncManager"
        private const val MAX_RETRIES = 2
        private const val RETRY_DELAY_MS = 1000L
    }

    /**
     * Synchronise toutes les données locales non synchronisées vers le backend.
     * Retourne un [SyncResult] avec le nombre de succès/erreurs.
     */
    suspend fun syncAllData(): SyncResult = withContext(Dispatchers.IO) {
        var adherentsSynced = 0
        var adherentsFailed = 0
        var paiementsSynced = 0
        var paiementsFailed = 0
        val errors = mutableListOf<String>()

        try {
            Log.d(TAG, "Starting sync process...")
            // 2. Sync Adherents
            val unsyncedAdherents = adherentDao.getUnsyncedAdherents()
            Log.d(TAG, "Found ${unsyncedAdherents.size} unsynced adherents")
            for (localAdherent in unsyncedAdherents) {
                val success = syncAdherentWithRetry(localAdherent)
                if (success) {
                    adherentsSynced++
                } else {
                    adherentsFailed++
                    errors.add("Échec sync adhérent: ${localAdherent.prenoms} ${localAdherent.nom}")
                }
            }

            // 3. Sync Paiements
            val unsyncedPaiements = paiementDao.getUnsyncedPaiements()
            Log.d(TAG, "Found ${unsyncedPaiements.size} unsynced payments")
            for (localPaiement in unsyncedPaiements) {
                val success = syncPaiementWithRetry(localPaiement)
                if (success) {
                    paiementsSynced++
                } else {
                    paiementsFailed++
                    errors.add("Échec sync paiement: ${localPaiement.reference}")
                }
            }

            Log.d(TAG, "Sync finished: $adherentsSynced/$adherentsFailed adherents, $paiementsSynced/$paiementsFailed paiements")
        } catch (e: Exception) {
            Log.e(TAG, "Sync process encountered fatal error", e)
            errors.add("Erreur fatale: ${e.message}")
        }

        SyncResult(
            adherentsSynced = adherentsSynced,
            adherentsFailed = adherentsFailed,
            paiementsSynced = paiementsSynced,
            paiementsFailed = paiementsFailed,
            errors = errors
        )
    }

    /**
     * Synchronise un adhérent avec retry (backoff).
     */
    private suspend fun syncAdherentWithRetry(
        localAdherent: com.example.sencsu.data.local.entity.AdherentEntity,
        retries: Int = MAX_RETRIES
    ): Boolean {
        repeat(retries + 1) { attempt ->
            try {
                val dto = AdherentDto(
                    prenoms = localAdherent.prenoms,
                    nom = localAdherent.nom,
                    adresse = localAdherent.adresse,
                    lieuNaissance = localAdherent.lieuNaissance,
                    sexe = localAdherent.sexe,
                    numeroCarte = localAdherent.numeroCarte,
                    dateNaissance = localAdherent.dateNaissance,
                    situationM = localAdherent.situationM,
                    whatsapp = localAdherent.whatsapp,
                    secteurActivite = localAdherent.secteurActivite,
                    typePiece = localAdherent.typePiece,
                    numeroPiece = localAdherent.numeroPiece,
                    numeroCNi = localAdherent.numeroCNi,
                    departement = localAdherent.departement,
                    commune = localAdherent.commune,
                    region = localAdherent.region,
                    typeAdhesion = localAdherent.typeAdhesion,
                    montantTotal = localAdherent.montantTotal,
                    regime = localAdherent.regime,
                    matricule = localAdherent.matricule,
                    photo = localAdherent.photo,
                    photoRecto = localAdherent.photoRecto,
                    photoVerso = localAdherent.photoVerso,
                    actif = localAdherent.actif,
                    clientUUID = localAdherent.localUuid
                )

                val responseResult = dashboardRepository.ajouterAdherent(dto)
                if (responseResult.isSuccess) {
                    val remoteId = responseResult.getOrThrow().adherentId
                    adherentDao.markAsSynced(localAdherent.localId, remoteId)
                    Log.d(TAG, "Synced adherent ${localAdherent.localId} → remoteId $remoteId")
                    return true
                }
            } catch (e: Exception) {
                Log.e(TAG, "Attempt ${attempt + 1} failed for adherent ${localAdherent.localId}", e)
                if (attempt < retries) {
                    delay(RETRY_DELAY_MS * (attempt + 1)) // backoff
                }
            }
        }
        return false
    }

    /**
     * Synchronise un paiement avec retry (backoff).
     */
    private suspend fun syncPaiementWithRetry(
        localPaiement: com.example.sencsu.data.local.entity.PaiementEntity,
        retries: Int = MAX_RETRIES
    ): Boolean {
        repeat(retries + 1) { attempt ->
            try {
                var remoteAdherentId = localPaiement.adherentId

                // Résoudre l'ID remote depuis l'adhérent local si nécessaire
                if (remoteAdherentId == null && localPaiement.localAdherentId != null) {
                    val updatedAdherent = adherentDao.getAdherentById(localPaiement.localAdherentId)
                    if (updatedAdherent?.isSynced == true && updatedAdherent.id != null) {
                        remoteAdherentId = updatedAdherent.id
                    }
                }

                if (remoteAdherentId == null) {
                    Log.w(TAG, "Skipping payment ${localPaiement.localId} - missing adherentId")
                    return false // Pas de retry, l'adhérent n'est pas encore sync
                }

                val dto = PaiementDto(
                    reference = localPaiement.reference,
                    montant = localPaiement.montant,
                    modePaiement = localPaiement.modePaiement,
                    photoPaiement = localPaiement.photoPaiement,
                    adherentId = remoteAdherentId,
                    photos = listOfNotNull(localPaiement.photoPaiement),
                    datePaiement = localPaiement.datePaiement
                )

                val responseResult = paiementRepository.addPaiement(dto)
                if (responseResult.isSuccess) {
                    paiementDao.markAsSynced(localPaiement.localId, localPaiement.localId)
                    Log.d(TAG, "Synced payment ${localPaiement.localId}")
                    return true
                } else {
                    Log.e(TAG, "Repo error for payment ${localPaiement.localId}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Attempt ${attempt + 1} failed for payment ${localPaiement.localId}", e)
                if (attempt < retries) {
                    delay(RETRY_DELAY_MS * (attempt + 1))
                }
            }
        }
        return false
    }
}
