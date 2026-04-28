package com.example.sencsu.data.remote

import com.example.sencsu.data.remote.dto.*
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @POST("/api/auth/login")
    suspend fun login(@Body request: LoginRequest): LoginApiResponse

    @POST("/api/auth/adherent/login")
    suspend fun adherentLogin(@Body request: LoginRequest): LoginApiResponse

    @GET("/api/adherents/me")
    suspend fun getMyInfo(): AdherentDto

    @GET("/api/adherents/all")
    suspend fun getDashboardData(): DashboardResponseDto
    @POST("/api/adherents/create")
    suspend fun createAdherent(
        @Body adherent: AdherentDto
    ): CreateAdherentResponse

    @GET("/api/paiements/adherent/{id}")
    suspend fun getPaiementsByAdherentId(@Path("id") adherentId: String): List<PaiementDto>

    @GET("/api/cotisation/adherent/{id}")
    suspend fun getCotisationByAdherentId(
        @Path("id")
        adherentId: String
    ): List<CotisationDto>

    @Multipart
    @POST("/api/files/upload")
    suspend fun uploadFile(
        @Part file: MultipartBody.Part
    ): Response<UploadResponse>

    @POST("/api/paiements/add")
    suspend fun addPaiement(
        @Body paiement: PaiementDto
    ): Response<ApiResponseP>

    @GET("/api/adherents/{id}")
    suspend fun getAdherentById(
        @Path("id") id: String
    ): ApiResponse<AdherentDto>

    @GET("/api/adherents/by-agent/{agentId}")
    suspend fun getAdherentsByAgentId(
        @Path("agentId") agentId: String
    ): ApiResponse<List<AdherentDto>>

    @DELETE("/api/adherents/{id}")
    suspend fun deleteAdherent(
        @Path("id") id: String
    ) : Response<Unit>

    // Ajouter une personne en charge
    @POST("/api/adherents/{adherentId}/personnes-charge")
    suspend fun addPersonneCharge(
        @Path("adherentId") adherentId: String,
        @Body personne: PersonneChargeDto
    ): PersonneChargeDto

    // Supprimer une personne en charge (avec adherentId)
    @DELETE("/api/adherents/{adherentId}/personnes-charge/{pcId}")
    suspend fun deletePersonneCharge(
        @Path("adherentId") adherentId: String,
        @Path("pcId") pcId: String
    )

    // Modifier une personne en charge
    @PUT("/api/adherents/{adherentId}/personnes-charge/{pcId}")
    suspend fun updatePersonneCharge(
        @Path("adherentId") adherentId: String,
        @Path("pcId") pcId: String,
        @Body personne: PersonneChargeDto
    ): PersonneChargeDto

    @PUT("/api/adherents/update/{id}")
    suspend fun updateAdherent(
        @Path("id") id: String,
        @Body adherent: AdherentUpdateDto
    ): ApiResponse<AdherentUpdateDto>
    @POST("/api/adherents/{id}/update-password")
    suspend fun updatePassword(
        @Path("id") id: String,
        @Body request: Map<String, String>
    ): Response<Map<String, Any>>

}
