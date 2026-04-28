package com.example.sencsu.data.remote.dto

/**
 * Constantes pour les formulaires d'enrôlement.
 */
object FormConstants {
    val SITUATIONS = listOf("Célibataire", "Marié(e)", "Divorcé(e)", "Veuf(ve)")
    val TYPES_PIECE = listOf("CNI", "Extrait de naissance")
    val DEPARTEMENTS = listOf("Thiès", "Mbour", "Tivaouane")
    val SEXES = listOf("M", "F")
    val LIENS_PARENTE = listOf("Conjoint(e)", "Enfant", "Parent", "Frère/Soeur", "Autre")
    val REGIONS = listOf("Thiès", "Dakar", "Diourbel", "Fatick", "Kaffrine", "Kaolack", "Kédougou", "Kolda", "Louga", "Matam", "Saint-Louis", "Sédhiou", "Tambacounda", "Ziguinchor")
    val REGIMES = listOf("CONTRIBUTIF", "NON_CONTRIBUTIF")
    val TYPES_BENEF = listOf("CLASSIQUE", "ETUDIANT", "INDIGENT")
    val TYPES_ADHESION = listOf("FAMALE", "INDIVIDUELLE")
}
