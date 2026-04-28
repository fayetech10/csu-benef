package com.example.sencsu.data.repository

import com.example.sencsu.data.remote.ApiService
import com.example.sencsu.data.remote.dto.CotisationDto
import com.example.sencsu.domain.repository.ICotisationRepository
import javax.inject.Inject

class CotisationRepository @Inject constructor(
   private val apiService: ApiService
) : ICotisationRepository {
    override suspend fun getCotisationByIdahderent(adherentId: String): List<CotisationDto> {
        return apiService.getCotisationByAdherentId(adherentId)
    }
}
