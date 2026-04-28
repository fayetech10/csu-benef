package com.example.sencsu.screen.enrolement

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sencsu.theme.AppColors
import com.example.sencsu.theme.AppShapes

@Composable
fun IdentityStep(
    nom: String, onNomChange: (String) -> Unit,
    prenom: String, onPrenomChange: (String) -> Unit,
    date: String, onDateChange: (String) -> Unit,
    sexe: String, onSexeChange: (String) -> Unit,
    lieuNaissance: String, onLieuChange: (String) -> Unit,
    commune: String, onCommuneChange: (String) -> Unit,
    departement: String, onDepartementChange: (String) -> Unit,
    nin: String, onNinChange: (String) -> Unit,
    isFromOcr: Boolean
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        StepTitle("Étape 2 : Identité", "Vérifiez et complétez vos informations personnelles.")

        if (isFromOcr) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = AppColors.StatusGreen.copy(0.06f),
                shape = AppShapes.MediumRadius,
                border = BorderStroke(1.dp, AppColors.StatusGreen.copy(0.15f))
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.AutoAwesome, null, tint = AppColors.StatusGreen, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Pré-rempli par OCR — vérifiez et corrigez si nécessaire", fontSize = 12.sp, color = AppColors.StatusGreen)
                }
            }
        }

        PremiumTextField(value = prenom, onValueChange = onPrenomChange, label = "Prénom(s)", placeholder = "Ex: Moussa", icon = Icons.Rounded.Person)
        PremiumTextField(value = nom, onValueChange = onNomChange, label = "Nom", placeholder = "Ex: Diouf", icon = Icons.Rounded.Person)
        PremiumTextField(value = date, onValueChange = onDateChange, label = "Date de naissance", placeholder = "JJ/MM/AAAA", icon = Icons.Rounded.CalendarToday)
        PremiumTextField(value = lieuNaissance, onValueChange = onLieuChange, label = "Lieu de naissance", placeholder = "Ex: Dakar", icon = Icons.Rounded.LocationOn)
        
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            PremiumTextField(value = departement, onValueChange = onDepartementChange, label = "Département", placeholder = "Ex: Dakar", icon = Icons.Rounded.Map, modifier = Modifier.weight(1f))
            PremiumTextField(value = commune, onValueChange = onCommuneChange, label = "Commune", placeholder = "Ex: Dakar Plateau", icon = Icons.Rounded.LocationCity, modifier = Modifier.weight(1f))
        }

        PremiumTextField(value = nin, onValueChange = onNinChange, label = "NIN (Numéro d'Identification)", placeholder = "Ex: 2650200001409", icon = Icons.Rounded.Pin)

        Text("Sexe", fontWeight = FontWeight.SemiBold, color = AppColors.TextMain)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SexeOption("Masculin", sexe == "M", { onSexeChange("M") }, Modifier.weight(1f))
            SexeOption("Féminin", sexe == "F", { onSexeChange("F") }, Modifier.weight(1f))
        }
    }
}
