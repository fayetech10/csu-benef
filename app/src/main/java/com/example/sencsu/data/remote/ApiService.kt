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

/**
 * Interface Retrofit pour communiquer avec l'API SenCSU Backend.
 * Les endpoints sont regroupés par domaine fonctionnel.
 */
interface ApiService {

    // =========================================================================
    // AUTHENTIFICATION
    // =========================================================================

    @POST("/api/auth/login")
    suspend fun login(@Body request: LoginRequest): LoginApiResponse

    @POST("/api/auth/adherent/login")
    suspend fun adherentLogin(@Body request: LoginRequest): LoginApiResponse

    @POST("/api/adherents/{id}/update-password")
    suspend fun updatePassword(
        @Path("id") id: String,
        @Body request: Map<String, String>
    ): Response<Map<String, Any>>


    // =========================================================================
    // ADHÉRENTS (CORE)
    // =========================================================================

    @GET("/api/adherents/me")
    suspend fun getMyInfo(): AdherentDto

    @GET("/api/adherents/all")
    suspend fun getDashboardData(): DashboardResponseDto

    @POST("/api/adherents/create")
    suspend fun createAdherent(
        @Body adherent: AdherentDto
    ): CreateAdherentResponse

    @GET("/api/adherents/{id}")
    suspend fun getAdherentById(
        @Path("id") id: String
    ): ApiResponse<AdherentDto>

    @GET("/api/adherents/by-agent/{agentId}")
    suspend fun getAdherentsByAgentId(
        @Path("agentId") agentId: String
    ): ApiResponse<List<AdherentDto>>

    @PUT("/api/adherents/{id}")
    suspend fun updateAdherent(
        @Path("id") id: String,
        @Body adherent: AdherentUpdateDto
    ): ApiResponse<AdherentUpdateDto>

    @DELETE("/api/adherents/{id}")
    suspend fun deleteAdherent(
        @Path("id") id: String
    ) : Response<Unit>

    @GET("/api/adherents/scan/{matricule}")
    suspend fun scanAdherent(
        @Path("matricule") matricule: String
    ): ApiResponse<AdherentDto>

    @POST("/api/adherents/{id}/fcm-token")
    suspend fun registerFcmToken(
        @Path("id") id: String,
        @Body tokenRequest: Map<String, String>
    ): Response<ApiResponse<Unit>>


    // =========================================================================
    // PERSONNES À CHARGE (DÉPENDANTS)
    // =========================================================================

    @POST("/api/adherents/{adherentId}/personnes-charge")
    suspend fun addPersonneCharge(
        @Path("adherentId") adherentId: String,
        @Body personne: PersonneChargeDto
    ): PersonneChargeDto

    @DELETE("/api/adherents/{adherentId}/personnes-charge/{pcId}")
    suspend fun deletePersonneCharge(
        @Path("adherentId") adherentId: String,
        @Path("pcId") pcId: String
    )

    @PUT("/api/adherents/{adherentId}/personnes-charge/{pcId}")
    suspend fun updatePersonneCharge(
        @Path("adherentId") adherentId: String,
        @Path("pcId") pcId: String,
        @Body personne: PersonneChargeDto
    ): PersonneChargeDto


    // =========================================================================
    // FINANCES (PAIEMENTS & COTISATIONS)
    // =========================================================================

    @GET("/api/paiements/adherent/{id}")
    suspend fun getPaiementsByAdherentId(@Path("id") adherentId: String): ApiResponse<List<PaiementDto>>

    @GET("/api/cotisation/adherent/{id}")
    suspend fun getCotisationByAdherentId(
        @Path("id")
        adherentId: String
    ): List<CotisationDto>

    @POST("/api/paiements/add")
    suspend fun addPaiement(
        @Body paiement: PaiementDto
    ): Response<ApiResponseP>


    // =========================================================================
    // SERVICES MÉDICAUX
    // =========================================================================

    @GET("/api/services-medicaux/adherent/{matricule}")
    suspend fun getServicesMedicaux(
        @Path("matricule") matricule: String,
        @Query("personneChargeId") personneChargeId: String? = null
    ): ApiResponse<List<ServiceMedicalDto>>

    @GET("/api/services-medicaux/recent/{matricule}")
    suspend fun getRecentServicesMedicaux(
        @Path("matricule") matricule: String
    ): ApiResponse<List<ServiceMedicalDto>>

    @GET("/api/services-medicaux/personne-charge/{pcId}")
    suspend fun getServicesByPersonneCharge(
        @Path("pcId") pcId: String
    ): ApiResponse<List<ServiceMedicalDto>>

    @GET("/api/services-medicaux/summary/{matricule}")
    suspend fun getServicesSummary(
        @Path("matricule") matricule: String
    ): ApiResponse<ServicesSummaryDto>


    // =========================================================================
    // GESTION DES FICHIERS
    // =========================================================================

    @Multipart
    @POST("/api/files/upload")
    suspend fun uploadFile(
        @Part file: MultipartBody.Part
    ): Response<UploadResponse>
}
