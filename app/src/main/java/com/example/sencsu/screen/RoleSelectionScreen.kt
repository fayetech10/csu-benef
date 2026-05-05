package com.example.sencsu.screen

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sencsu.theme.*

@Composable
fun RoleSelectionScreen(
    onSelectAgent: () -> Unit,
    onSelectBeneficiary: () -> Unit
) {
    var showContent by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { showContent = true }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.AppBackground)
    ) {
        // IMMERSIVE TOP GRADIENT
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.5f)
                .background(
                    Brush.verticalGradient(
                        listOf(AppColors.BrandBlueDark, AppColors.BrandBlue, AppColors.AppBackground)
                    )
                )
        ) {
            // Decorative glass elements
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 80.dp, y = (-60).dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.05f))
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            // BRANDING
            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn(tween(600)) + slideInVertically(tween(600)) { -30 }
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(
                        modifier = Modifier.size(80.dp),
                        shape = AppShapes.LargeRadius,
                        color = Color.White.copy(alpha = 0.15f),
                        border = BorderStroke(1.5.dp, Color.White.copy(alpha = 0.2f))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Rounded.HealthAndSafety, null,
                                tint = Color.White,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        "SenCSU",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = 1.sp
                    )
                    Text(
                        "La plateforme numérique de la CMU",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(44.dp))

            // SELECTION BOX
            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn(tween(600, delayMillis = 200)) + slideInVertically(tween(600, delayMillis = 200)) { 40 }
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = AppColors.SurfaceBackground,
                    shape = AppShapes.LargeRadius,
                    shadowElevation = 12.dp,
                    border = BorderStroke(1.dp, AppColors.BorderColorLight)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            "Votre Espace",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = AppColors.TextMain
                        )
                        Text(
                            "Sélectionnez votre profil pour accéder à vos outils personnalisés.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AppColors.TextSub
                        )

                        Spacer(Modifier.height(4.dp))

                        RoleOptionCard(
                            title = "Agent Terrain",
                            subtitle = "Enrôlement, suivi et synchronisation.",
                            icon = Icons.Rounded.Engineering,
                            accent = AppColors.BrandBlue,
                            filled = true,
                            onClick = onSelectAgent
                        )

                        RoleOptionCard(
                            title = "Bénéficiaire",
                            subtitle = "Ma carte, mon foyer et mes soins.",
                            icon = Icons.Rounded.Person,
                            accent = AppColors.GoldAccent,
                            filled = false,
                            onClick = onSelectBeneficiary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // FEATURES HIGHLIGHT
            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn(tween(600, delayMillis = 400))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    HighlightFeature(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Rounded.Security,
                        label = "Sécurisé"
                    )
                    HighlightFeature(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Rounded.CloudSync,
                        label = "Temps Réel"
                    )
                    HighlightFeature(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Rounded.SupportAgent,
                        label = "Support 24/7"
                    )
                }
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
        shape = AppShapes.MediumRadius,
        color = if (filled) accent else AppColors.SurfaceBackground,
        border = if (filled) null else BorderStroke(1.dp, AppColors.BorderColor),
        shadowElevation = if (filled) 4.dp else 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = AppShapes.SmallRadius,
                color = if (filled) Color.White.copy(alpha = 0.2f) else accent.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (filled) Color.White else accent,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.size(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (filled) Color.White else AppColors.TextMain
                )
                Text(
                    subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (filled) Color.White.copy(alpha = 0.8f) else AppColors.TextSub
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                contentDescription = null,
                tint = if (filled) Color.White else AppColors.TextDisabled,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun HighlightFeature(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String
) {
    Surface(
        modifier = modifier,
        shape = AppShapes.MediumRadius,
        color = Color.White.copy(alpha = 0.6f),
        border = BorderStroke(0.5.dp, AppColors.BorderColorLight)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(icon, null, tint = AppColors.BrandBlue, modifier = Modifier.size(20.dp))
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = AppColors.TextMain,
                textAlign = TextAlign.Center
            )
        }
    }
}
