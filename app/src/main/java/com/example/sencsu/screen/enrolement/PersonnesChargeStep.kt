package com.example.sencsu.screen.enrolement

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sencsu.theme.AppColors
import com.example.sencsu.theme.AppShapes

@Composable
fun PersonnesChargeStep(
    personnes: List<PersonneChargeForm>,
    onAdd: () -> Unit,
    onRemove: (Int) -> Unit,
    onUpdate: (Int, PersonneChargeForm) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        StepTitle(
            "Étape 4 : Personnes à charge",
            "Ajoutez les membres de votre foyer qui bénéficieront de la couverture."
        )

        // Counter
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = AppColors.BrandBlueLite,
            shape = AppShapes.MediumRadius
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Rounded.Group, null, tint = AppColors.BrandBlue)
                Spacer(Modifier.width(12.dp))
                Text(
                    "${personnes.size} personne(s) à charge ajoutée(s)",
                    fontWeight = FontWeight.Bold,
                    color = AppColors.BrandBlue
                )
            }
        }

        // Liste des personnes à charge
        personnes.forEachIndexed { index, personne ->
            PersonneChargeCard(
                index = index + 1,
                personne = personne,
                onUpdate = { updated -> onUpdate(personne.id, updated) },
                onRemove = { onRemove(personne.id) }
            )
        }

        // Bouton d'ajout
        OutlinedButton(
            onClick = onAdd,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = AppShapes.MediumRadius,
            border = BorderStroke(1.dp, AppColors.BrandBlue),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = AppColors.BrandBlue)
        ) {
            Icon(Icons.Rounded.PersonAdd, null)
            Spacer(Modifier.width(12.dp))
            Text("Ajouter une personne à charge", fontWeight = FontWeight.Bold)
        }

        if (personnes.isEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = AppColors.SurfaceAlt,
                shape = AppShapes.MediumRadius
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Rounded.FamilyRestroom, null, tint = AppColors.TextDisabled, modifier = Modifier.size(48.dp))
                    Spacer(Modifier.height(12.dp))
                    Text("Aucune personne à charge", fontWeight = FontWeight.Bold, color = AppColors.TextSub)
                    Text("Vous pouvez en ajouter ou passer à l'étape suivante.", fontSize = 13.sp, color = AppColors.TextDisabled, textAlign = TextAlign.Center)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonneChargeCard(
    index: Int,
    personne: PersonneChargeForm,
    onUpdate: (PersonneChargeForm) -> Unit,
    onRemove: () -> Unit
) {
    var expanded by remember { mutableStateOf(true) }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = AppShapes.MediumRadius,
        color = Color.White,
        border = BorderStroke(1.dp, AppColors.BorderColor)
    ) {
        Column {
            // Header avec toggle et suppression
            Surface(
                onClick = { expanded = !expanded },
                color = AppColors.SurfaceAlt.copy(0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(32.dp),
                        shape = CircleShape,
                        color = AppColors.BrandBlue
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("$index", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(
                        if (personne.prenoms.isNotBlank()) "${personne.prenoms} ${personne.nom}"
                        else "Personne #$index",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onRemove, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Rounded.Close, null, tint = AppColors.StatusRed, modifier = Modifier.size(18.dp))
                    }
                    Icon(
                        if (expanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                        null,
                        tint = AppColors.TextSub
                    )
                }
            }

            // Formulaire
            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    PremiumTextField(
                        value = personne.prenoms,
                        onValueChange = { onUpdate(personne.copy(prenoms = it)) },
                        label = "Prénom(s)",
                        placeholder = "Prénom du bénéficiaire"
                    )
                    PremiumTextField(
                        value = personne.nom,
                        onValueChange = { onUpdate(personne.copy(nom = it)) },
                        label = "Nom",
                        placeholder = "Nom du bénéficiaire"
                    )
                    PremiumTextField(
                        value = personne.dateNaissance,
                        onValueChange = { onUpdate(personne.copy(dateNaissance = it)) },
                        label = "Date de naissance",
                        placeholder = "JJ/MM/AAAA",
                        icon = Icons.Rounded.CalendarToday
                    )

                    // Sexe
                    Text("Sexe", fontWeight = FontWeight.SemiBold, color = AppColors.TextMain, fontSize = 14.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SexeOption("M", personne.sexe == "M", { onUpdate(personne.copy(sexe = "M")) }, Modifier.weight(1f))
                        SexeOption("F", personne.sexe == "F", { onUpdate(personne.copy(sexe = "F")) }, Modifier.weight(1f))
                    }

                    // Lien de parenté
                    Text("Lien de parenté", fontWeight = FontWeight.SemiBold, color = AppColors.TextMain, fontSize = 14.sp)
                    val liens = listOf("Époux(se)", "Fils", "Fille", "Père", "Mère", "Autre")
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                        liens.take(3).forEach { lien ->
                            Surface(
                                onClick = { onUpdate(personne.copy(lienParent = lien)) },
                                shape = AppShapes.SmallRadius,
                                color = if (personne.lienParent == lien) AppColors.BrandBlueLite else Color.White,
                                border = BorderStroke(1.dp, if (personne.lienParent == lien) AppColors.BrandBlue else AppColors.BorderColor),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    lien,
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    textAlign = TextAlign.Center,
                                    fontWeight = if (personne.lienParent == lien) FontWeight.Bold else FontWeight.Normal,
                                    color = if (personne.lienParent == lien) AppColors.BrandBlue else AppColors.TextSub,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                        liens.drop(3).forEach { lien ->
                            Surface(
                                onClick = { onUpdate(personne.copy(lienParent = lien)) },
                                shape = AppShapes.SmallRadius,
                                color = if (personne.lienParent == lien) AppColors.BrandBlueLite else Color.White,
                                border = BorderStroke(1.dp, if (personne.lienParent == lien) AppColors.BrandBlue else AppColors.BorderColor),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    lien,
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    textAlign = TextAlign.Center,
                                    fontWeight = if (personne.lienParent == lien) FontWeight.Bold else FontWeight.Normal,
                                    color = if (personne.lienParent == lien) AppColors.BrandBlue else AppColors.TextSub,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
