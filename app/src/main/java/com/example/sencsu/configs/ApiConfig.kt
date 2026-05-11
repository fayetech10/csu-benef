package com.example.sencsu.configs

/**
 * Configuration centralisée des URLs API.
 * Toutes les URLs doivent passer par cette classe.
 */
object ApiConfig {
    // ── URL du backend local ──
    const val BASE_URL = "http://192.168.1.252:8080/"
//    const val BASE_URL = "http://localhost:8080/"

    private const val FILES_ENDPOINT = "api/files/"
    private const val QR_CODE_ENDPOINT = "api/adherents/qr/"
    const val IMAGE_BASE_URL = "${BASE_URL}${FILES_ENDPOINT}"

    fun getImageUrl(filename: String?): String? {
        if (filename.isNullOrBlank()) {
            return null
        }
        // Si c'est déjà une URL complète, la retourner telle quelle
        if (filename.startsWith("http")) {
            return filename
        }
        return IMAGE_BASE_URL + filename
    }

    fun getQrCodeUrl(matricule: String?): String? {
        if (matricule.isNullOrBlank()) return null
        val encodedMatricule = java.net.URLEncoder.encode(matricule, "UTF-8")
        return "${BASE_URL}${QR_CODE_ENDPOINT}${encodedMatricule}"
    }
}
