package com.example.sencsu.domain.repository

import com.example.sencsu.data.remote.dto.CotisationDto

interface ICotisationRepository {
    suspend fun getCotisationByIdahderent(adherentId: String): List<CotisationDto>
}
