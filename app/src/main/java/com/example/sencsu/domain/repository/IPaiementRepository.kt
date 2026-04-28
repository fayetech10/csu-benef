package com.example.sencsu.domain.repository

import com.example.sencsu.data.remote.dto.PaiementDto

interface IPaiementRepository {
    suspend fun getPaiementsByAdherentId(adherentId: String): List<PaiementDto>
    suspend fun addPaiement(paiement: PaiementDto): Result<Unit>
}
