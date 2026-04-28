package com.example.sencsu.screen

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.SubcomposeAsyncImage
import com.example.sencsu.components.MembershipCard
import com.example.sencsu.configs.ApiConfig
import com.example.sencsu.data.remote.dto.AdherentDto
import com.example.sencsu.data.remote.dto.PersonneChargeDto
import com.example.sencsu.domain.viewmodel.BeneficiaryDashboardViewModel
import com.example.sencsu.theme.AppColors
import com.example.sencsu.theme.AppShapes
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BeneficiaryDashboardScreen(
    viewModel: BeneficiaryDashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val adherent = uiState.adherent
    var showContent by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(100)
        showContent = true
    }

    PullToRefreshBox(
        isRefreshing = uiState.isLoading,
        onRefresh = { viewModel.refresh() }
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(AppColors.AppBackground),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // ── EN-TÊTE ──
            item {
                DashboardHeader(adherent, showContent)
            }

            // ── CARTE DE MEMBRE ──
            if (adherent != null) {
                item {
                    Column {
                        AnimatedVisibility(
                            visible = showContent,
                            enter = fadeIn(tween(400, delayMillis = 200)) + slideInVertically(
                                tween(400, delayMillis = 200), initialOffsetY = { 40 }
                            )
                        ) {
                            MembershipCard(
                                adherent = adherent,
                                modifier = Modifier
                                    .padding(horizontal = 16.dp)
                                    .offset(y = (-24).dp)
                            )
                        }
                    }
                }

                // ── STATISTIQUES RAPIDES ──
                item {
                    Column {
                        AnimatedVisibility(
                            visible = showContent,
                            enter = fadeIn(tween(400, delayMillis = 300))
                        ) {
                            QuickStatsRow(adherent)
                        }
                    }
                }

                // ── INFORMATIONS PERSONNELLES ──
                item {
                    Column {
                        AnimatedVisibility(
                            visible = showContent,
                            enter = fadeIn(tween(400, delayMillis = 400)) + slideInVertically(
                                tween(400, delayMillis = 400), initialOffsetY = { 30 }
                            )
                        ) {
                            Column {
                                DashboardSectionTitle("Mes informations", Icons.Rounded.Info)
                                InfoCard(adherent)
                            }
                        }
                    }
                }

                // ── PERSONNES EN CHARGE ──
                if (!adherent.personnesCharge.isNullOrEmpty()) {
                    item {
                        DashboardSectionTitle(
                            "Mon foyer (${adherent.personnesCharge.size})",
                            Icons.Rounded.FamilyRestroom
                        )
                    }
                    itemsIndexed(adherent.personnesCharge) { index, pc ->
                        Column {
                            AnimatedVisibility(
                                visible = showContent,
                                enter = fadeIn(tween(300, delayMillis = 500 + index * 80)) + slideInHorizontally(
                                    tween(300, delayMillis = 500 + index * 80), initialOffsetX = { 60 }
                                )
                            ) {
                                DependantCard(pc)
                            }
                        }
                    }
                }
            }

            // ── ERREUR ──
            if (uiState.error != null) {
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = AppShapes.MediumRadius,
                        color = AppColors.StatusRed.copy(alpha = 0.08f)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Rounded.ErrorOutline, null, tint = AppColors.StatusRed)
                            Spacer(Modifier.width(12.dp))
                            Text(uiState.error!!, color = AppColors.StatusRed, fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }
}

// ══════════════════════════════════════════════
//  COMPOSANTS INTERNES
// ══════════════════════════════════════════════

@Composable
private fun DashboardHeader(adherent: AdherentDto?, showContent: Boolean) {
    val name = "${adherent?.prenoms ?: ""} ${adherent?.nom ?: ""}".trim().ifEmpty { "Chargement..." }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .background(
                Brush.verticalGradient(
                    listOf(AppColors.BrandBlueDark, AppColors.BrandBlue)
                )
            )
    ) {
        // Cercles décoratifs
        Box(
            modifier = Modifier
                .size(120.dp)
                .offset(x = (-30).dp, y = (-20).dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.04f))
        )

        Column {
            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn(tween(500)) + slideInVertically(tween(500), initialOffsetY = { -20 })
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                        .statusBarsPadding(),
                    verticalArrangement = Arrangement.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Avatar avec initiales
                        Surface(
                            modifier = Modifier.size(52.dp),
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.15f),
                            border = androidx.compose.foundation.BorderStroke(
                                1.5.dp, Color.White.copy(alpha = 0.3f)
                            )
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                val initials = name.split(" ")
                                    .take(2)
                                    .mapNotNull { it.firstOrNull()?.uppercase() }
                                    .joinToString("")
                                Text(
                                    initials.ifEmpty { "?" },
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 18.sp
                                )
                            }
                        }
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text(
                                "Bienvenue,",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 13.sp
                            )
                            Text(
                                name,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickStatsRow(adherent: AdherentDto) {
    val nbDependants = adherent.personnesCharge?.size ?: 0

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 4.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        StatMiniCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Rounded.Group,
            value = "$nbDependants",
            label = "Personnes",
            color = AppColors.BrandBlue
        )
        StatMiniCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Rounded.CheckCircle,
            value = if (adherent.actif != false) "Actif" else "Inactif",
            label = "Statut",
            color = if (adherent.actif != false) AppColors.StatusGreen else AppColors.StatusRed
        )
        StatMiniCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Rounded.LocalHospital,
            value = adherent.regime?.take(6) ?: "—",
            label = "Régime",
            color = AppColors.GoldAccent
        )
    }
}

@Composable
private fun StatMiniCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    value: String,
    label: String,
    color: Color
) {
    Surface(
        modifier = modifier,
        shape = AppShapes.MediumRadius,
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(22.dp))
            Spacer(Modifier.height(6.dp))
            Text(
                value,
                fontWeight = FontWeight.Black,
                fontSize = 14.sp,
                color = AppColors.TextMain,
                maxLines = 1
            )
            Text(
                label,
                fontSize = 10.sp,
                color = AppColors.TextSub,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun InfoCard(adherent: AdherentDto) {
    Surface(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth(),
        shape = AppShapes.MediumRadius,
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            InfoRow(Icons.Rounded.CreditCard, "NIN", adherent.numeroCNi ?: "—")
            HorizontalDivider(color = AppColors.BorderColorLight)
            InfoRow(Icons.Rounded.LocationCity, "Commune", adherent.commune ?: "—")
            HorizontalDivider(color = AppColors.BorderColorLight)
            InfoRow(Icons.Rounded.Map, "Région", adherent.region ?: "—")
            HorizontalDivider(color = AppColors.BorderColorLight)
            InfoRow(Icons.Rounded.Phone, "Téléphone", adherent.whatsapp ?: "—")
            HorizontalDivider(color = AppColors.BorderColorLight)
            InfoRow(Icons.Rounded.Category, "Type", adherent.typeBenef ?: "—")
        }
    }
}

@Composable
private fun InfoRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(32.dp),
            shape = CircleShape,
            color = AppColors.BrandBlueLite
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = AppColors.BrandBlue, modifier = Modifier.size(16.dp))
            }
        }
        Spacer(Modifier.width(12.dp))
        Text(label, color = AppColors.TextSub, fontSize = 13.sp, modifier = Modifier.weight(1f))
        Text(value, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = AppColors.TextMain)
    }
}

@Composable
private fun DependantCard(pc: PersonneChargeDto) {
    Surface(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .fillMaxWidth(),
        shape = AppShapes.MediumRadius,
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Surface(
                modifier = Modifier.size(44.dp),
                shape = CircleShape,
                color = AppColors.BrandBlue.copy(alpha = 0.08f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    val icon = when (pc.lienParent?.lowercase()) {
                        "époux", "épouse", "conjoint", "conjointe" -> Icons.Rounded.Favorite
                        "fils", "fille", "enfant" -> Icons.Rounded.ChildCare
                        else -> Icons.Rounded.Person
                    }
                    Icon(icon, null, tint = AppColors.BrandBlue, modifier = Modifier.size(22.dp))
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "${pc.prenoms ?: ""} ${pc.nom ?: ""}".trim(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = AppColors.TextMain
                )
                Text(
                    pc.lienParent ?: "Personne à charge",
                    color = AppColors.TextSub,
                    fontSize = 12.sp
                )
                if (pc.matricule != null) {
                    Text(
                        pc.matricule,
                        color = AppColors.BrandBlue,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Mini QR Code
            if (pc.matricule != null) {
                Surface(
                    modifier = Modifier.size(42.dp),
                    shape = AppShapes.SmallRadius,
                    color = Color.White,
                    border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.BorderColor)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        SubcomposeAsyncImage(
                            model = ApiConfig.getQrCodeUrl(pc.matricule),
                            contentDescription = "QR",
                            modifier = Modifier.padding(3.dp),
                            loading = {
                                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(12.dp),
                                        strokeWidth = 1.dp
                                    )
                                }
                            },
                            error = {
                                Icon(
                                    Icons.Rounded.QrCode, null,
                                    tint = AppColors.TextSub.copy(0.2f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DashboardSectionTitle(title: String, icon: ImageVector) {
    Row(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = AppColors.BrandBlue, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        Text(
            title,
            fontWeight = FontWeight.Black,
            color = AppColors.TextMain,
            fontSize = 16.sp
        )
    }
}
