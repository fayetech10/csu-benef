package com.example.sencsu.screen.enrolement

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun CoordinatesStep(
    tel: String, onTelChange: (String) -> Unit,
    mail: String, onMailChange: (String) -> Unit,
    addr: String, onAddrChange: (String) -> Unit,
    commune: String, departement: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        StepTitle("Étape 3 : Coordonnées", "Comment pouvons-nous vous contacter ?")

        PremiumTextField(
            value = tel, onValueChange = onTelChange,
            label = "Téléphone", placeholder = "77 000 00 00",
            prefix = "+221 ", icon = Icons.Rounded.Phone
        )
        PremiumTextField(value = mail, onValueChange = onMailChange, label = "Email", placeholder = "Ex: moussa@example.com", icon = Icons.Rounded.Email)
        PremiumTextField(value = addr, onValueChange = onAddrChange, label = "Adresse", placeholder = "Quartier, Rue...", icon = Icons.Rounded.LocationOn)

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            PremiumTextField(value = departement, onValueChange = {}, label = "Département", placeholder = "Département", modifier = Modifier.weight(1f), readOnly = true)
            PremiumTextField(value = commune, onValueChange = {}, label = "Commune", placeholder = "Commune", modifier = Modifier.weight(1f), readOnly = true)
        }
    }
}
