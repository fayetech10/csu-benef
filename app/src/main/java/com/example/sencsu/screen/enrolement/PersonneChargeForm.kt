package com.example.sencsu.screen.enrolement

data class PersonneChargeForm(
    val id: Int,
    val prenoms: String = "",
    val nom: String = "",
    val dateNaissance: String = "",
    val sexe: String = "M",
    val lienParent: String = "Fils"
)
