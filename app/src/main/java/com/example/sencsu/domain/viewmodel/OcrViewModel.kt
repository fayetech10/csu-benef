package com.example.sencsu.domain.viewmodel


import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

data class OcrExtractionResult(
    val nom: String = "",
    val prenoms: String = "",
    val dateNaissance: String = "",
    val nin: String = "",
    val sexe: String = "",
    val lieuNaissance: String = "",
    val commune: String = "",
    val departement: String = "",
    val rawText: String = "",
    val confidence: Float = 0f,
    val isValid: Boolean = false,
    val errors: List<String> = emptyList()
)

data class OcrUiState(
    val isLoading: Boolean = false,
    val isProcessing: Boolean = false,
    val rectoResult: OcrExtractionResult? = null,
    val versoResult: OcrExtractionResult? = null,
    val rectoUri: Uri? = null,
    val versoUri: Uri? = null,
    val error: String? = null,
    val successMessage: String? = null,
    val currentStep: OcrStep = OcrStep.SELECT_MODE
)

enum class OcrStep {
    SELECT_MODE,           // Choisir entre OCR et saisie manuelle
    CAPTURE_RECTO,        // Capturer/charger le recto
    CAPTURE_VERSO,        // Capturer/charger le verso
    REVIEW_RESULTS,       // Vérifier et corriger les résultats
    COMPLETED             // Terminé, données prêtes
}

@HiltViewModel
class OcrViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(OcrUiState())
    val uiState: StateFlow<OcrUiState> = _uiState

    private val textRecognizer by lazy {
        TextRecognition.getClient(TextRecognizerOptions.Builder().build())
    }

    // ──────────────────────────────────────────────────────────────
    // Gestion de la navigation dans l'OCR
    // ──────────────────────────────────────────────────────────────

    fun startOcrMode() {
        _uiState.value = _uiState.value.copy(
            currentStep = OcrStep.CAPTURE_RECTO,
            error = null,
            successMessage = null
        )
    }

    fun skipOcr() {
        _uiState.value = _uiState.value.copy(
            currentStep = OcrStep.SELECT_MODE,
            rectoResult = null,
            versoResult = null,
            rectoUri = null,
            versoUri = null,
            error = null
        )
    }

    fun setRectoUri(uri: Uri?) {
        _uiState.value = _uiState.value.copy(rectoUri = uri)
    }

    fun setVersoUri(uri: Uri?) {
        _uiState.value = _uiState.value.copy(versoUri = uri)
    }

    fun goToVersoCapture() {
        _uiState.value = _uiState.value.copy(
            currentStep = OcrStep.CAPTURE_VERSO,
            error = null
        )
    }

    fun goBackToRecto() {
        _uiState.value = _uiState.value.copy(
            currentStep = OcrStep.CAPTURE_RECTO,
            rectoResult = null,
            rectoUri = null,
            error = null
        )
    }

    fun goToReviewResults() {
        _uiState.value = _uiState.value.copy(
            currentStep = OcrStep.REVIEW_RESULTS,
            error = null
        )
    }

    fun completeOcr() {
        _uiState.value = _uiState.value.copy(
            currentStep = OcrStep.COMPLETED,
            successMessage = "Données extraites avec succès!"
        )
    }

    fun resetOcr() {
        _uiState.value = OcrUiState()
    }

    // ──────────────────────────────────────────────────────────────
    // Extraction OCR
    // ──────────────────────────────────────────────────────────────

    fun processRectoImage(context: Context, imageUri: Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isProcessing = true, error = null)
            try {
                // Use InputImage.fromFilePath to automatically handle EXIF orientation
                val inputImage = InputImage.fromFilePath(context, imageUri)
                val result = extractRectoData(inputImage)

                _uiState.value = _uiState.value.copy(
                    rectoResult = result,
                    isProcessing = false,
                    successMessage = if (result.isValid)
                        "Recto analysé avec succès"
                    else
                        "Vérifiez les informations extraites"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isProcessing = false,
                    error = "Erreur lors du traitement du recto: ${e.message}"
                )
            }
        }
    }

    fun processVersoImage(context: Context, imageUri: Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isProcessing = true, error = null)
            try {
                // Use InputImage.fromFilePath to automatically handle EXIF orientation
                val inputImage = InputImage.fromFilePath(context, imageUri)
                val result = extractVersoData(inputImage)

                _uiState.value = _uiState.value.copy(
                    versoResult = result,
                    isProcessing = false,
                    successMessage = if (result.isValid)
                        "Verso analysé avec succès"
                    else
                        "Vérifiez le NIN extrait"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isProcessing = false,
                    error = "Erreur lors du traitement du verso: ${e.message}"
                )
            }
        }
    }

    // ──────────────────────────────────────────────────────────────
    // Extraction des données du RECTO
    // ──────────────────────────────────────────────────────────────

    private suspend fun extractRectoData(inputImage: InputImage): OcrExtractionResult {
        return try {
            val visionText = textRecognizer.process(inputImage).await()
            val fullText = visionText.text

            val errors = mutableListOf<String>()
            var prenoms = ""
            var nom = ""
            var dateNaissance = ""
            var sexe = ""
            var lieuNaissance = ""
            var commune = ""
            var departement = ""
            var confidence = 0.8f

            // Extraction du texte en analysant les blocs
            val blocks = visionText.textBlocks
            val allText = blocks.flatMap { block ->
                block.lines.map { line -> line.text }
            }

            // Recherche des champs du recto (Carte CEDEAO Sénégal)
            prenoms = extractField(allText, "Prénoms", fullText)
            if (prenoms.isEmpty()) errors.add("Prénom non trouvé")

            nom = extractField(allText, "Nom", fullText)
            if (nom.isEmpty()) errors.add("Nom non trouvé")

            dateNaissance = extractDateOfBirth(allText, fullText)
            if (dateNaissance.isEmpty()) errors.add("Date de naissance non trouvée")

            sexe = extractGender(fullText)
            if (sexe.isEmpty()) errors.add("Sexe non détecté")

            lieuNaissance = extractField(allText, "lieu de naissance", fullText)
            if (lieuNaissance.isEmpty()) lieuNaissance = extractField(allText, "Date et lieu de naissance", fullText)
            if (lieuNaissance.isEmpty()) lieuNaissance = extractField(allText, "Lieu Nais", fullText)
            if (lieuNaissance.isEmpty()) lieuNaissance = extractField(allText, "Lieu naiss", fullText)
            
            // Nettoyer s'il y a une date collée (ex: "12/12/1990 Dakar")
            lieuNaissance = lieuNaissance.replace(Regex("""\b\d{2}[/-]\d{2}[/-]\d{4}\b"""), "").trim()
            // Nettoyer "A " au début (ex: "A Dakar" -> "Dakar")
            if (lieuNaissance.uppercase().startsWith("A ")) {
                lieuNaissance = lieuNaissance.substring(2).trim()
            }

            // Extraction commune et département
            commune = extractField(allText, "Commune", fullText)
            if (commune.isEmpty()) commune = extractField(allText, "commune de", fullText)
            if (commune.isEmpty()) commune = extractField(allText, "Com de", fullText)

            departement = extractField(allText, "Département", fullText)
            if (departement.isEmpty()) departement = extractField(allText, "Departement", fullText)
            if (departement.isEmpty()) departement = extractField(allText, "Dept", fullText)
            if (departement.isEmpty()) departement = extractField(allText, "Dépt", fullText)

            // Si commune/département non trouvés, tenter une inférence depuis le lieu de naissance
            if (lieuNaissance.isNotEmpty() && (commune.isEmpty() || departement.isEmpty())) {
                val locationInfo = inferLocationFromCity(lieuNaissance)
                if (commune.isEmpty()) commune = locationInfo.first
                if (departement.isEmpty()) departement = locationInfo.second
            }

            val isValid = errors.isEmpty() && prenoms.isNotEmpty() && nom.isNotEmpty()

            OcrExtractionResult(
                prenoms = prenoms.trim(),
                nom = nom.trim(),
                dateNaissance = dateNaissance,
                sexe = sexe,
                lieuNaissance = lieuNaissance.trim(),
                commune = commune.trim(),
                departement = departement.trim(),
                rawText = fullText,
                confidence = confidence,
                isValid = isValid,
                errors = errors
            )
        } catch (e: Exception) {
            OcrExtractionResult(
                errors = listOf("Erreur d'extraction: ${e.message}"),
                rawText = e.message ?: "Erreur inconnue"
            )
        }
    }

    // ──────────────────────────────────────────────────────────────
    // Extraction des données du VERSO
    // ──────────────────────────────────────────────────────────────

    private suspend fun extractVersoData(inputImage: InputImage): OcrExtractionResult {
        return try {
            val visionText = textRecognizer.process(inputImage).await()
            val fullText = visionText.text

            val errors = mutableListOf<String>()
            var nin = ""
            var confidence = 0.8f

            // Extraction du NIN (motif: 2 650 2000 01409 ou 2650200001409)
            nin = extractNIN(fullText)
            if (nin.isEmpty()) {
                errors.add("NIN non trouvé")
                confidence = 0.3f
            }

            val isValid = nin.isNotEmpty()

            OcrExtractionResult(
                nin = nin,
                rawText = fullText,
                confidence = confidence,
                isValid = isValid,
                errors = errors
            )
        } catch (e: Exception) {
            OcrExtractionResult(
                errors = listOf("Erreur d'extraction: ${e.message}"),
                rawText = e.message ?: "Erreur inconnue"
            )
        }
    }

    // ──────────────────────────────────────────────────────────────
    // Fonctions utilitaires d'extraction
    // ──────────────────────────────────────────────────────────────

    private fun extractField(lines: List<String>, fieldName: String, fullText: String): String {
        // Cherche le champ dans les lignes
        for (i in lines.indices) {
            val line = lines[i]
            
            // Pour éviter que la recherche de "Nom" ne corresponde à "Prénoms"
            val isMatch = if (fieldName.equals("Nom", ignoreCase = true)) {
                line.equals("Nom", ignoreCase = true) || 
                line.equals("Nom:", ignoreCase = true) ||
                (line.contains("Nom", ignoreCase = true) && !line.contains("Prénom", ignoreCase = true))
            } else {
                line.contains(fieldName, ignoreCase = true)
            }
            
            if (isMatch) {
                // Vérifier si la valeur est sur la même ligne (ex: "Commune Dakar")
                // On évite ça pour "Nom" s'il est suivi directement du nom sans séparateur clair sauf si c'est sûr
                val regex = Regex("(?i)$fieldName\\s*[:\\-]?\\s+(.+)")
                val match = regex.find(line)
                if (match != null && match.groupValues[1].isNotBlank()) {
                    val inlineValue = match.groupValues[1].trim()
                    if (!inlineValue.equals("Prénoms", ignoreCase = true) && !inlineValue.equals("Nom", ignoreCase = true)) {
                        return inlineValue
                    }
                }

                // Le contenu est généralement sur la ligne suivante
                if (i + 1 < lines.size) {
                    val value = lines[i + 1].trim()
                    // Si la valeur suivante est elle-même un titre (par ex "Prénoms", on passe)
                    if (value.equals("Prénoms", ignoreCase = true) || value.equals("Nom", ignoreCase = true)) {
                        continue
                    }
                    return value
                }
            }
        }
        return ""
    }

    private fun extractDateOfBirth(lines: List<String>, fullText: String): String {
        // Motif: JJ/MM/AAAA ou JJMMAAAA
        val datePattern = Regex("""(\d{2})[/-]?(\d{2})[/-]?(\d{4})""")
        val matches = datePattern.findAll(fullText)

        for (match in matches) {
            val day = match.groupValues[1]
            val month = match.groupValues[2]
            val year = match.groupValues[3]

            // Validation simple
            val dayInt = day.toIntOrNull() ?: 0
            val monthInt = month.toIntOrNull() ?: 0
            val yearInt = year.toIntOrNull() ?: 0

            if (dayInt in 1..31 && monthInt in 1..12 && yearInt >= 1900 && yearInt <= 2024) {
                return "$day/$month/$year"
            }
        }
        return ""
    }

    private fun extractGender(fullText: String): String {
        // Pattern to look for Sexe, Sex, Sexe / Sex, etc., followed by M or F
        val regex = Regex("""Sexe?(?:\s*/\s*Sex)?[\s:/-]*([MF])\b""", RegexOption.IGNORE_CASE)
        val match = regex.find(fullText)
        if (match != null) {
            return match.groupValues[1].uppercase()
        }

        // Fallback 1: distance search (at most 15 characters after Sexe or Sex)
        val regexNoise = Regex("""Sexe?[\s\S]{0,15}?\b([MF])\b""", RegexOption.IGNORE_CASE)
        val matchNoise = regexNoise.find(fullText)
        if (matchNoise != null) {
            return matchNoise.groupValues[1].uppercase()
        }

        // Fallback 2: Check for isolated M or F in the whole text
        val hasF = Regex("""\bF\b""", RegexOption.IGNORE_CASE).containsMatchIn(fullText)
        val hasM = Regex("""\bM\b""", RegexOption.IGNORE_CASE).containsMatchIn(fullText)
        if (hasM && !hasF) return "M"
        if (hasF && !hasM) return "F"
        
        return ""
    }

    private fun extractNIN(fullText: String): String {
        // Motif NIN sénégalais: 2 650 2000 01409 ou variantes
        // Généralement format: X XXX XXXX XXXXX
        val ninPattern = Regex("""(\d{1,2}\s?\d{3}\s?\d{4}\s?\d{5,6})|(\d{13,14})""")
        val match = ninPattern.find(fullText)

        return if (match != null) {
            // Nettoyer les espaces
            match.value.replace(" ", "").replace("\\D".toRegex(), "")
        } else {
            ""
        }
    }

    // ──────────────────────────────────────────────────────────────
    // Correction manuelle des champs
    // ──────────────────────────────────────────────────────────────

    fun updateRectoField(field: String, value: String) {
        val current = _uiState.value.rectoResult ?: return
        val updated = when (field) {
            "prenoms" -> current.copy(prenoms = value)
            "nom" -> current.copy(nom = value)
            "dateNaissance" -> current.copy(dateNaissance = value)
            "sexe" -> current.copy(sexe = value)
            "lieuNaissance" -> current.copy(lieuNaissance = value)
            "commune" -> current.copy(commune = value)
            "departement" -> current.copy(departement = value)
            else -> current
        }
        _uiState.value = _uiState.value.copy(rectoResult = updated)
    }

    fun updateVersoField(field: String, value: String) {
        val current = _uiState.value.versoResult ?: return
        val updated = when (field) {
            "nin" -> current.copy(nin = value)
            else -> current
        }
        _uiState.value = _uiState.value.copy(versoResult = updated)
    }

    // ──────────────────────────────────────────────────────────────
    // Données extraites pour injecter dans le formulaire
    // ──────────────────────────────────────────────────────────────

    fun getExtractedDataForForm(): Map<String, String> {
        val state = _uiState.value
        val recto = state.rectoResult ?: return emptyMap()
        val verso = state.versoResult

        val map = mutableMapOf(
            "prenoms" to recto.prenoms,
            "nom" to recto.nom,
            "dateNaissance" to recto.dateNaissance,
            "sexe" to recto.sexe,
            "lieuNaissance" to recto.lieuNaissance,
            "commune" to recto.commune,
            "departement" to recto.departement,
            "nin" to (verso?.nin ?: "")
        )
        
        state.rectoUri?.let { map["rectoUri"] = it.toString() }
        state.versoUri?.let { map["versoUri"] = it.toString() }
        
        return map
    }

    // ──────────────────────────────────────────────────────────────
    // Inférence commune/département à partir du lieu de naissance
    // ──────────────────────────────────────────────────────────────

    private fun inferLocationFromCity(city: String): Pair<String, String> {
        val cityLower = city.lowercase().trim()
        // Base de données simplifiée des villes sénégalaises principales
        val locationMap = mapOf(
            // Dakar
            "dakar" to Pair("Dakar", "Dakar"),
            "pikine" to Pair("Pikine", "Pikine"),
            "guédiawaye" to Pair("Guédiawaye", "Guédiawaye"),
            "guediawaye" to Pair("Guédiawaye", "Guédiawaye"),
            "rufisque" to Pair("Rufisque", "Rufisque"),
            "keur massar" to Pair("Keur Massar", "Keur Massar"),
            "bargny" to Pair("Bargny", "Rufisque"),
            "diamniadio" to Pair("Diamniadio", "Rufisque"),
            // Thiès
            "thiès" to Pair("Thiès", "Thiès"),
            "thies" to Pair("Thiès", "Thiès"),
            "mbour" to Pair("Mbour", "Mbour"),
            "tivaouane" to Pair("Tivaouane", "Tivaouane"),
            "saly" to Pair("Saly", "Mbour"),
            // Saint-Louis
            "saint-louis" to Pair("Saint-Louis", "Saint-Louis"),
            "saint louis" to Pair("Saint-Louis", "Saint-Louis"),
            "dagana" to Pair("Dagana", "Dagana"),
            "podor" to Pair("Podor", "Podor"),
            "richard toll" to Pair("Richard Toll", "Dagana"),
            // Diourbel
            "diourbel" to Pair("Diourbel", "Diourbel"),
            "touba" to Pair("Touba", "Mbacké"),
            "mbacké" to Pair("Mbacké", "Mbacké"),
            "mbacke" to Pair("Mbacké", "Mbacké"),
            "bambey" to Pair("Bambey", "Bambey"),
            // Kaolack
            "kaolack" to Pair("Kaolack", "Kaolack"),
            "nioro du rip" to Pair("Nioro du Rip", "Nioro du Rip"),
            "guinguinéo" to Pair("Guinguinéo", "Guinguinéo"),
            // Ziguinchor
            "ziguinchor" to Pair("Ziguinchor", "Ziguinchor"),
            "bignona" to Pair("Bignona", "Bignona"),
            "oussouye" to Pair("Oussouye", "Oussouye"),
            // Fatick
            "fatick" to Pair("Fatick", "Fatick"),
            "foundiougne" to Pair("Foundiougne", "Foundiougne"),
            "gossas" to Pair("Gossas", "Gossas"),
            // Kolda
            "kolda" to Pair("Kolda", "Kolda"),
            "vélingara" to Pair("Vélingara", "Vélingara"),
            "médina yoro foulah" to Pair("Médina Yoro Foulah", "Médina Yoro Foulah"),
            // Tambacounda
            "tambacounda" to Pair("Tambacounda", "Tambacounda"),
            "bakel" to Pair("Bakel", "Bakel"),
            "kédougou" to Pair("Kédougou", "Kédougou"),
            "kedougou" to Pair("Kédougou", "Kédougou"),
            // Louga
            "louga" to Pair("Louga", "Louga"),
            "kébémer" to Pair("Kébémer", "Kébémer"),
            "linguère" to Pair("Linguère", "Linguère"),
            // Matam
            "matam" to Pair("Matam", "Matam"),
            "kanel" to Pair("Kanel", "Kanel"),
            "ranérou" to Pair("Ranérou", "Ranérou"),
            // Kaffrine
            "kaffrine" to Pair("Kaffrine", "Kaffrine"),
            "birkelane" to Pair("Birkelane", "Birkelane"),
            "koungheul" to Pair("Koungheul", "Koungheul"),
            // Sédhiou
            "sédhiou" to Pair("Sédhiou", "Sédhiou"),
            "sedhiou" to Pair("Sédhiou", "Sédhiou"),
            "bounkiling" to Pair("Bounkiling", "Bounkiling"),
            "goudomp" to Pair("Goudomp", "Goudomp")
        )

        return locationMap[cityLower] ?: Pair("", "")
    }

    // ──────────────────────────────────────────────────────────────
    // Utilitaire: Charger une bitmap depuis URI
    // ──────────────────────────────────────────────────────────────

    // Utilitaire: Charger une bitmap depuis URI (supprimée car InputImage gère ça mieux)
}