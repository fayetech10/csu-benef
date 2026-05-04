package com.example.sencsu.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AdminPanelSettings
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.FamilyRestroom
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.HealthAndSafety
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.sencsu.theme.AppColors
import com.example.sencsu.theme.AppShapes

@Composable
fun RoleSelectionScreen(
    onSelectAgent: () -> Unit,
    onSelectBeneficiary: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.AppBackground)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.36f)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            AppColors.BrandBlueDark,
                            AppColors.BrandBlue,
                            AppColors.BrandBlueLite
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .statusBarsPadding()
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(28.dp))

            Surface(
                modifier = Modifier.size(84.dp),
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.18f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.24f))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.HealthAndSafety,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                "SenCSU",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                "Gestion des beneficiaires, adherents et foyers",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.84f)
            )

            Spacer(modifier = Modifier.height(26.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shape = AppShapes.LargeRadius,
                tonalElevation = 0.dp,
                shadowElevation = 10.dp
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    Text(
                        "Choisissez votre espace",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.TextMain
                    )
                    Text(
                        "Accedez a l'interface adaptee a votre role pour suivre les dossiers, les renouvellements et les informations des beneficiaires.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppColors.TextSub
                    )

                    RoleOptionCard(
                        title = "Espace agent",
                        subtitle = "Enrolement, suivi des adherents, synchronisation et gestion terrain.",
                        icon = Icons.Rounded.AdminPanelSettings,
                        accent = AppColors.BrandBlue,
                        filled = true,
                        onClick = onSelectAgent
                    )

                    RoleOptionCard(
                        title = "Espace beneficiaire",
                        subtitle = "Carte, foyer, documents et statut de couverture en un coup d'oeil.",
                        icon = Icons.Rounded.FamilyRestroom,
                        accent = AppColors.GoldAccent,
                        filled = false,
                        onClick = onSelectBeneficiary
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                HighlightChip(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Rounded.Groups,
                    value = "Foyers",
                    caption = "gestion centralisee"
                )
                HighlightChip(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Rounded.VerifiedUser,
                    value = "Couverture",
                    caption = "suivi plus clair"
                )
            }
        }
    }
}

@Composable
private fun RoleOptionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accent: Color,
    filled: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = AppShapes.LargeRadius,
        color = if (filled) accent else Color.White,
        border = if (filled) null else BorderStroke(1.dp, AppColors.BorderColor),
        tonalElevation = 0.dp,
        shadowElevation = if (filled) 0.dp else 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(52.dp),
                shape = CircleShape,
                color = if (filled) Color.White.copy(alpha = 0.18f) else AppColors.BrandBlueLite
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (filled) Color.White else AppColors.BrandBlue,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.size(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (filled) Color.White else AppColors.TextMain
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (filled) Color.White.copy(alpha = 0.82f) else AppColors.TextSub
                )
            }

            Icon(
                imageVector = Icons.Rounded.ArrowForward,
                contentDescription = null,
                tint = if (filled) Color.White else AppColors.TextSub
            )
        }
    }
}

@Composable
private fun HighlightChip(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    value: String,
    caption: String
) {
    Surface(
        modifier = modifier,
        shape = AppShapes.MediumRadius,
        color = Color.White.copy(alpha = 0.92f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .background(AppColors.BrandBlueLite, CircleShape)
                    .border(1.dp, AppColors.BorderColorLight, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = AppColors.BrandBlue,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.size(10.dp))
            Column {
                Text(
                    value,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.TextMain
                )
                Text(
                    caption,
                    style = MaterialTheme.typography.labelSmall,
                    color = AppColors.TextSub
                )
            }
        }
    }
}
