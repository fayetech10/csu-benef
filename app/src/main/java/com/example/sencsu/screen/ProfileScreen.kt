package com.example.sencsu.screen

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.HelpOutline
import androidx.compose.material.icons.automirrored.rounded.Logout
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.sencsu.domain.viewmodel.BeneficiaryDashboardViewModel
import com.example.sencsu.theme.AppColors
import com.example.sencsu.theme.AppShapes
import kotlinx.coroutines.delay

@Composable
fun ProfileScreen(
    viewModel: BeneficiaryDashboardViewModel = hiltViewModel(),
    onLogout: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val adherent = uiState.adherent

    val fullName = "${adherent?.prenoms ?: ""} ${adherent?.nom ?: ""}".trim().ifEmpty { "Chargement..." }
    val initials = fullName.split(" ")
        .take(2)
        .mapNotNull { it.firstOrNull()?.uppercase() }
        .joinToString("")

    var showContent by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(100)
        showContent = true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(AppColors.AppBackground)
    ) {
        // ── En-tête avec gradient ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(230.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(AppColors.BrandBlueDark, AppColors.BrandBlue)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            // Cercles décoratifs
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 40.dp, y = (-30).dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.04f))
            )
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .align(Alignment.BottomStart)
                    .offset(x = (-20).dp, y = 20.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.03f))
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                AnimatedVisibility(
                    visible = showContent,
                    enter = fadeIn(tween(400)) + scaleIn(tween(400), initialScale = 0.9f)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(top = 24.dp)
                    ) {
                        // Avatar
                        Surface(
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.15f),
                            modifier = Modifier.size(92.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                2.dp, Color.White.copy(alpha = 0.3f)
                            )
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color.White,
                                    modifier = Modifier.size(82.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            initials.ifEmpty { "?" },
                                            fontSize = 30.sp,
                                            fontWeight = FontWeight.Black,
                                            color = AppColors.BrandBlue
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.height(14.dp))
                        Text(
                            fullName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        // Badge régime
                        if (adherent?.regime != null) {
                            Spacer(Modifier.height(6.dp))
                            Surface(
                                shape = AppShapes.CircleRadius,
                                color = AppColors.GoldAccent.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    adherent.regime.uppercase(),
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = AppColors.GoldAccent,
                                    letterSpacing = 1.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // ── Carte d'informations ──
        Column {
            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn(tween(300, delayMillis = 150)) + slideInVertically(
                    tween(300, delayMillis = 150), initialOffsetY = { 40 }
                )
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .offset(y = (-20).dp),
                    shape = AppShapes.LargeRadius,
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        ProfileInfoRow(
                            icon = Icons.Rounded.Badge,
                            label = "Matricule",
                            value = adherent?.matricule ?: "—"
                        )
                        HorizontalDivider(color = AppColors.BorderColor.copy(alpha = 0.3f))
                        ProfileInfoRow(
                            icon = Icons.Rounded.CreditCard,
                            label = "NIN",
                            value = adherent?.numeroCNi ?: "—"
                        )
                        HorizontalDivider(color = AppColors.BorderColor.copy(alpha = 0.3f))
                        ProfileInfoRow(
                            icon = Icons.Rounded.Phone,
                            label = "Téléphone",
                            value = adherent?.whatsapp ?: "—"
                        )
                        HorizontalDivider(color = AppColors.BorderColor.copy(alpha = 0.3f))
                        ProfileInfoRow(
                            icon = Icons.Rounded.LocationCity,
                            label = "Commune",
                            value = adherent?.commune ?: "—"
                        )
                        HorizontalDivider(color = AppColors.BorderColor.copy(alpha = 0.3f))
                        ProfileInfoRow(
                            icon = Icons.Rounded.Map,
                            label = "Région",
                            value = adherent?.region ?: "—"
                        )
                        HorizontalDivider(color = AppColors.BorderColor.copy(alpha = 0.3f))
                        ProfileInfoRow(
                            icon = Icons.Rounded.Category,
                            label = "Type bénéficiaire",
                            value = adherent?.typeBenef ?: "—"
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(4.dp))

        // ── Section paramètres ──
        Column {
            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn(tween(300, delayMillis = 250)) + slideInVertically(
                    tween(300, delayMillis = 250), initialOffsetY = { 30 }
                )
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "Paramètres",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.TextMain,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    SettingsItem(
                        icon = Icons.Rounded.Lock,
                        title = "Modifier le mot de passe",
                        subtitle = "Sécurité du compte"
                    )

                    SettingsItem(
                        icon = Icons.Rounded.Info,
                        title = "À propos",
                        subtitle = "Version 1.0.0"
                    )

                    SettingsItem(
                        icon = Icons.Rounded.Security,
                        title = "Confidentialité",
                        subtitle = "Gestion des données"
                    )

                    SettingsItem(
                        icon = Icons.AutoMirrored.Rounded.HelpOutline,
                        title = "Aide & Support",
                        subtitle = "Centre d'aide CMU"
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // ── Bouton déconnexion ──
        Column {
            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn(tween(300, delayMillis = 350))
            ) {
                Button(
                    onClick = { showLogoutDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .height(52.dp),
                    shape = AppShapes.MediumRadius,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppColors.StatusRed.copy(alpha = 0.08f),
                        contentColor = AppColors.StatusRed
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                ) {
                    Icon(Icons.AutoMirrored.Rounded.Logout, null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Se déconnecter", fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(Modifier.height(32.dp))

        // Copyright
        Text(
            "SenCSU © 2026\nCouverture Santé Universelle — Sénégal",
            style = MaterialTheme.typography.labelSmall,
            color = AppColors.TextDisabled,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        )
    }

    // ── Dialog de déconnexion ──
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            icon = {
                Icon(
                    Icons.AutoMirrored.Rounded.Logout, null,
                    tint = AppColors.StatusRed,
                    modifier = Modifier.size(28.dp)
                )
            },
            title = { Text("Déconnexion", fontWeight = FontWeight.Bold) },
            text = { Text("Êtes-vous sûr de vouloir vous déconnecter de votre espace bénéficiaire ?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        onLogout()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = AppColors.StatusRed)
                ) {
                    Text("Déconnecter", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Annuler")
                }
            },
            containerColor = Color.White,
            shape = AppShapes.LargeRadius
        )
    }
}

@Composable
private fun ProfileInfoRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Surface(
            shape = CircleShape,
            color = AppColors.BrandBlue.copy(alpha = 0.08f),
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = AppColors.BrandBlue, modifier = Modifier.size(20.dp))
            }
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = AppColors.TextSub
            )
            Text(
                value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = AppColors.TextMain
            )
        }
    }
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = AppShapes.MediumRadius,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = AppColors.SurfaceAlt,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = AppColors.TextSub, modifier = Modifier.size(20.dp))
                }
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.TextMain
                )
                Text(
                    subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = AppColors.TextSub
                )
            }
            Icon(
                Icons.Rounded.ChevronRight, null,
                tint = AppColors.TextSub.copy(alpha = 0.5f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
