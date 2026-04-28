package com.example.sencsu.screen.enrolement

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sencsu.theme.AppColors

@Composable
fun ReviewStep(
    nom: String, prenom: String, tel: String, mail: String, addr: String,
    dateNaissance: String, nin: String,
    commune: String, departement: String,
    personnesCharge: List<PersonneChargeForm>
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        StepTitle("Étape 5 : Récapitulatif", "Vérifiez toutes vos informations avant de soumettre.")

        InfoCard("Identité", listOf(
            "Nom complet" to "$prenom $nom",
            "Date de naissance" to dateNaissance,
            "NIN" to nin.ifBlank { "Non renseigné" }
        ))
        InfoCard("Contact", listOf(
            "Téléphone" to "+221 $tel",
            "Email" to mail.ifBlank { "Non renseigné" },
            "Adresse" to addr.ifBlank { "Non renseigné" },
            "Département" to departement.ifBlank { "Non renseigné" },
            "Commune" to commune.ifBlank { "Non renseigné" }
        ))

        if (personnesCharge.isNotEmpty()) {
            Text("Personnes à charge (${personnesCharge.size})", fontWeight = FontWeight.Bold, color = AppColors.BrandBlue, fontSize = 14.sp)
            personnesCharge.forEachIndexed { i, p ->
                InfoCard("${p.lienParent} #${i + 1}", listOf(
                    "Nom complet" to "${p.prenoms} ${p.nom}",
                    "Date de naissance" to p.dateNaissance.ifBlank { "—" },
                    "Sexe" to if (p.sexe == "M") "Masculin" else "Féminin"
                ))
            }
        }
    }
}
