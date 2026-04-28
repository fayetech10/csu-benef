package com.example.sencsu.utils

import android.annotation.TargetApi
import android.os.Build
import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale


object Formatters {

    /**
     * Convertit un timestamp en millisecondes vers une date formatée "dd/MM/yyyy".
     */
    @TargetApi(Build.VERSION_CODES.O)
    fun formatMillisToDate(millis: Long): String {
        val date = Instant.ofEpochMilli(millis)
            .atZone(ZoneId.of("UTC"))
            .toLocalDate()
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        return date.format(formatter)
    }

    /**
     * Convertit une date UI "dd/MM/yyyy" vers le format API "yyyy-MM-dd".
     * Fonction unique et canonique — les anciens doublons ont été supprimés.
     */
    @TargetApi(Build.VERSION_CODES.O)
    fun formatDateForApi(dateUi: String): String {
        return try {
            val inputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
            val outputFormatter = DateTimeFormatter.ISO_LOCAL_DATE // yyyy-MM-dd
            val localDate = LocalDate.parse(dateUi, inputFormatter)
            localDate.format(outputFormatter)
        } catch (e: Exception) {
            // Fallback : parsing manuel si le format ne correspond pas exactement
            formatDateManual(dateUi)
        }
    }

    /**
     * Alias pour compatibilité avec le code existant qui appelle formatDateForAPI.
     */
    fun formatDateForAPI(dateString: String): String = formatDateForApi(dateString)

    /**
     * Parsing manuel de date "dd/MM/yyyy" vers "yyyy-MM-dd".
     * Utilisé comme fallback si java.time n'est pas disponible.
     */
    private fun formatDateManual(dateString: String): String {
        val parts = dateString.split('/')
        return if (parts.size == 3) {
            val year = parts[2]
            val month = parts[1].padStart(2, '0')
            val day = parts[0].padStart(2, '0')
            "$year-$month-$day"
        } else {
            dateString
        }
    }

    /**
     * Formate un numéro de téléphone sénégalais (XX XXX XX XX).
     */
    fun formatPhoneNumber(number: String): String {
        val cleaned = number.replace("\\s".toRegex(), "")
        val limited = cleaned.take(10)
        val formatted = StringBuilder()

        for (i in limited.indices) {
            if (i == 2 || i == 5 || i == 7) formatted.append(' ')
            formatted.append(limited[i])
        }

        return formatted.toString()
    }

    /**
     * Formate la saisie d'une date (auto-ajout des /).
     */
    fun formatDate(text: String): String {
        val cleaned = text.replace("[^0-9]".toRegex(), "")
        val formatted = StringBuilder()

        for (i in cleaned.indices.take(8)) {
            if (i == 2 || i == 4) formatted.append('/')
            formatted.append(cleaned[i])
        }

        return formatted.toString()
    }

    /**
     * Vérifie si une date "dd/MM/yyyy" est valide.
     */
    @TargetApi(Build.VERSION_CODES.O)
    fun isValidDate(dateStr: String): Boolean {
        if (!Regex("^\\d{2}/\\d{2}/\\d{4}$").matches(dateStr)) return false

        val parts = dateStr.split('/')
        if (parts.size != 3) return false

        val day = parts[0].toIntOrNull() ?: return false
        val month = parts[1].toIntOrNull() ?: return false
        val year = parts[2].toIntOrNull() ?: return false

        if (day < 1 || day > 31) return false
        if (month < 1 || month > 12) return false
        if (year < 1900 || year > java.time.LocalDate.now().year) return false

        val months30 = listOf(4, 6, 9, 11)
        if (months30.contains(month) && day > 30) return false

        if (month == 2) {
            val isLeapYear = (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)
            if (isLeapYear && day > 29) return false
            if (!isLeapYear && day > 28) return false
        }

        return true
    }
}


fun Int.toLocaleString(): String {
    return NumberFormat.getNumberInstance(Locale.FRENCH).format(this)
}