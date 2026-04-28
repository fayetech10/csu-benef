package com.example.sencsu.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AdminPanelSettings
import androidx.compose.material.icons.rounded.FamilyRestroom
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sencsu.theme.AppColors
import com.example.sencsu.theme.AppShapes

@Composable
fun RoleSelectionScreen(
    onSelectAgent: () -> Unit,
    onSelectBeneficiary: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = AppColors.AppBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Bienvenue sur SenCSU",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = AppColors.TextMain
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Choisissez votre profil pour continuer",
                style = MaterialTheme.typography.bodyMedium,
                color = AppColors.TextSub
            )
            Spacer(modifier = Modifier.height(48.dp))

            Button(
                onClick = onSelectAgent,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                shape = AppShapes.MediumRadius,
                colors = ButtonDefaults.buttonColors(containerColor = AppColors.BrandBlue)
            ) {
                Icon(Icons.Rounded.AdminPanelSettings, contentDescription = null, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text("Je suis Agent", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedButton(
                onClick = onSelectBeneficiary,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                shape = AppShapes.MediumRadius,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = AppColors.BrandBlue)
            ) {
                Icon(Icons.Rounded.FamilyRestroom, contentDescription = null, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text("Je suis Bénéficiaire", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
