package com.example.sencsu.screen

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalContext
import com.example.sencsu.components.cartes.HealthInsuranceCard
import com.example.sencsu.configs.ApiConfig
import com.example.sencsu.data.remote.dto.AdherentDto
import com.example.sencsu.data.remote.dto.PersonneChargeDto
import com.example.sencsu.data.remote.dto.ServiceMedicalDto
import com.example.sencsu.domain.viewmodel.BeneficiaryDashboardViewModel
import com.example.sencsu.theme.AppColors
import com.example.sencsu.theme.AppShapes
import kotlinx.coroutines.delay

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BeneficiaryDashboardScreen(
    onNavigateToHistory: (String) -> Unit = {},
    onNavigateToCard: (String) -> Unit = {},
    onProfileClick: () -> Unit = {},
    viewModel: BeneficiaryDashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val token by viewModel.sessionManager.tokenFlow.collectAsState(initial = null)
    val context = LocalContext.current
    val adherent = uiState.adherent
    var showContent by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(80)
        showContent = true
    }

    PullToRefreshBox(
        isRefreshing = uiState.isLoading,
        onRefresh = { viewModel.refresh() }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (adherent != null) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background),
                    contentPadding = PaddingValues(top = 100.dp, bottom = 120.dp)
                ) {
                    // 1. Carte de membre
                    item {
                        AnimatedVisibility(
                            visible = showContent,
                            enter = fadeIn(tween(500)) + slideInVertically(
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessLow
                                ),
                                initialOffsetY = { -40 }
                            )
                        ) {
                            Box(modifier = Modifier.clickable { 
                                adherent.id?.let { onNavigateToCard(it) }
                            }) {
                                HealthInsuranceCard(
                                    data = adherent,
                                    sessionManager = viewModel.sessionManager,
                                    modifier = Modifier
                                        .padding(horizontal = 16.dp)
                                        .padding(top = 12.dp, bottom = 8.dp)
                                )
                            }
                        }
                    }

                    // 2. Actions rapides
                    item {
                        AnimatedVisibility(
                            visible = showContent,
                            enter = fadeIn(tween(300, delayMillis = 100)) + slideInVertically(
                                tween(300, delayMillis = 100), initialOffsetY = { 20 }
                            )
                        ) {
                            QuickActionsRow(
                                onHistory = { adherent.id?.let { onNavigateToHistory(it) } }
                            )
                        }
                    }

                    // 3. Couverture
                    item {
                        AnimatedVisibility(
                            visible = showContent,
                            enter = fadeIn(tween(400, delayMillis = 200)) + slideInVertically(
                                tween(400, delayMillis = 200),
                                initialOffsetY = { 20 }
                            )
                        ) {
                            CoverageStatusCard(adherent)
                        }
                    }

                    // 4. Quick Stats
                    item {
                        AnimatedVisibility(
                            visible = showContent,
                            enter = fadeIn(tween(400, delayMillis = 300)) + slideInVertically(
                                tween(400, delayMillis = 300),
                                initialOffsetY = { 20 }
                            )
                        ) {
                            QuickStatsRow(adherent)
                        }
                    }

                    // 5. Services médicaux récents
                    if (uiState.recentServices.isNotEmpty()) {
                        item {
                            AnimatedVisibility(
                                visible = showContent,
                                enter = fadeIn(tween(400, delayMillis = 400)) + slideInVertically(
                                    tween(400, delayMillis = 400),
                                    initialOffsetY = { 20 }
                                )
                            ) {
                                RecentServicesSection(
                                    services = uiState.recentServices,
                                    onViewAll = { adherent.id?.let { onNavigateToHistory(it) } }
                                )
                            }
                        }
                    }

                    // 6. Personnes en charge
                    if (!adherent.personnesCharge.isNullOrEmpty()) {
                        item {
                            AnimatedVisibility(
                                visible = showContent,
                                enter = fadeIn(tween(300, delayMillis = 500))
                            ) {
                                DashboardSectionTitle(
                                    "Personnes en charge (${adherent.personnesCharge.size})",
                                    Icons.Rounded.FamilyRestroom
                                )
                            }
                        }
                        itemsIndexed(adherent.personnesCharge) { index, pc ->
                            AnimatedVisibility(
                                visible = showContent,
                                enter = fadeIn(tween(300, delayMillis = 550 + index * 60)) +
                                        slideInHorizontally(
                                            tween(300, delayMillis = 550 + index * 60),
                                            initialOffsetX = { 80 }
                                        )
                            ) {
                                Column {
                                    DependantCard(pc, token, context)
                                    if (index < adherent.personnesCharge.size - 1) {
                                        HorizontalDivider(
                                            modifier = Modifier.padding(horizontal = 24.dp),
                                            thickness = 0.5.dp,
                                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                    
                    // 7. Erreurs éventuelles
                    if (uiState.error != null) {
                        item {
                            DashboardErrorCard(uiState.error!!)
                        }
                    }
                }

                // HEADER FIXE
                DashboardHeader(
                    adherent = adherent,
                    onProfileClick = onProfileClick,
                    token = token
                )
            }
        }

        // Suppression de FullScreenCardOverlay car remplacé par DigitalCardScreen
    }
}

@Composable
private fun CardInfoRow(label: String, value: String, valueColor: Color = MaterialTheme.colorScheme.onSurface) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = valueColor
        )
    }
}

// ─────────────────────────────────────────────
// QUICK STATS (améliorés avec gradient et animations)
// ─────────────────────────────────────────────

@Composable
private fun QuickStatsRow(adherent: AdherentDto) {
    val nbDependants = adherent.personnesCharge?.size ?: 0
    val isActive = adherent.actif != false

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 4.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatMiniCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Rounded.Group,
            value = "$nbDependants",
            label = "Personnes",
            color = MaterialTheme.colorScheme.primary
        )
        StatMiniCard(
            modifier = Modifier.weight(1f),
            icon = if (isActive) Icons.Rounded.CheckCircle else Icons.Rounded.ErrorOutline,
            value = if (isActive) "Actif" else "Inactif",
            label = "Statut",
            color = if (isActive) AppColors.StatusGreen else AppColors.StatusRed
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
        shape = RoundedCornerShape(18.dp),
        color = Color.White,
        shadowElevation = 4.dp,
        border = BorderStroke(0.5.dp, color.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        Brush.radialGradient(
                            listOf(color.copy(alpha = 0.15f), color.copy(alpha = 0.05f))
                        ),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(22.dp))
            }
            Text(
                value,
                fontWeight = FontWeight.Black,
                fontSize = 17.sp,
                color = AppColors.TextMain,
                maxLines = 1
            )
            Text(
                label,
                fontSize = 11.sp,
                color = AppColors.TextSub,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
        }
    }
}

// ─────────────────────────────────────────────
// COUVERTURE (barre de progression stylisée)
// ─────────────────────────────────────────────

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun CoverageStatusCard(adherent: AdherentDto) {
    val coveragePeriod = adherent.coveragePeriod ?: "Non définie"
    val isActive = adherent.actif != false

    val progress = remember(adherent.createdAt) {
        try {
            val created = java.time.LocalDateTime.parse(adherent.createdAt)
            val now = java.time.LocalDateTime.now()
            val end = created.plusYears(1)
            val totalDays = java.time.temporal.ChronoUnit.DAYS.between(created, end).toFloat()
            val elapsedDays = java.time.temporal.ChronoUnit.DAYS.between(created, now).toFloat()
            (elapsedDays / totalDays).coerceIn(0f, 1f)
        } catch (e: Exception) { 0.1f }
    }

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(1200, easing = FastOutSlowInEasing),
        label = "coverage_progress"
    )

    val progressColor = when {
        progress >= 0.85f -> AppColors.StatusRed
        progress >= 0.65f -> AppColors.StatusOrange
        else -> AppColors.StatusGreen
    }

    val remainingDays = remember(adherent.createdAt) {
        try {
            val created = java.time.LocalDateTime.parse(adherent.createdAt)
            val end = created.plusYears(1)
            val now = java.time.LocalDateTime.now()
            java.time.temporal.ChronoUnit.DAYS.between(now, end).coerceAtLeast(0)
        } catch (e: Exception) { 0L }
    }

    Surface(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = Color.White,
        shadowElevation = 6.dp
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icône avec gradient de fond
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .background(
                            Brush.linearGradient(
                                if (isActive) listOf(AppColors.StatusGreen.copy(0.2f), AppColors.StatusGreen.copy(0.05f))
                                else listOf(AppColors.StatusRed.copy(0.2f), AppColors.StatusRed.copy(0.05f))
                            ),
                            RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (isActive) Icons.Rounded.Shield else Icons.Rounded.Warning,
                        contentDescription = null,
                        tint = if (isActive) AppColors.StatusGreen else AppColors.StatusRed,
                        modifier = Modifier.size(26.dp)
                    )
                }
                Spacer(Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Validité de la couverture", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = AppColors.TextSub)
                    Text(coveragePeriod, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = AppColors.TextMain)
                }
                // Badge statut avec gradient
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (isActive) AppColors.StatusGreen.copy(0.12f) else AppColors.StatusRed.copy(0.12f)
                ) {
                    Text(
                        if (isActive) "ACTIF" else "INACTIF",
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                        fontSize = 11.sp, fontWeight = FontWeight.Black,
                        letterSpacing = 0.8.sp,
                        color = if (isActive) AppColors.StatusGreen else AppColors.StatusRed
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // Barre de progression animée
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Progression", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = AppColors.TextSub)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("${(progress * 100).toInt()}%", fontSize = 15.sp, fontWeight = FontWeight.Black, color = progressColor)
                    if (remainingDays > 0) {
                        Spacer(Modifier.width(8.dp))
                        Surface(shape = RoundedCornerShape(6.dp), color = progressColor.copy(alpha = 0.1f)) {
                            Text(
                                "${remainingDays}j restants",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                fontSize = 10.sp, fontWeight = FontWeight.Bold, color = progressColor
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            LinearProgressIndicator(
                progress = animatedProgress,
                modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp)),
                color = progressColor,
                trackColor = progressColor.copy(alpha = 0.1f),
                strokeCap = StrokeCap.Round
            )
        }
    }
}

// ─────────────────────────────────────────────
// SERVICES MÉDICAUX RÉCENTS (design plus moderne)
// ─────────────────────────────────────────────

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun RecentServicesSection(
    services: List<ServiceMedicalDto>,
    onViewAll: () -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Rounded.MedicalServices,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    "Services récents",
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 18.sp
                )
            }
            TextButton(
                onClick = onViewAll,
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Voir tout", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Icon(Icons.Rounded.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
            }
        }

        services.take(3).forEach { service ->
            RecentServiceCard(service)
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun RecentServiceCard(service: ServiceMedicalDto) {
    val typeColor = when (service.typeService) {
        "CONSULTATION"    -> AppColors.BrandBlue
        "LABORATOIRE"     -> Color(0xFF8B5CF6)
        "PHARMACIE"       -> AppColors.StatusGreen
        "HOSPITALISATION" -> AppColors.StatusRed
        "RADIOLOGIE"      -> AppColors.ActionBlue
        else              -> AppColors.TextSub
    }
    val typeIcon = when (service.typeService) {
        "CONSULTATION"    -> Icons.Rounded.MedicalServices
        "LABORATOIRE"     -> Icons.Rounded.Science
        "PHARMACIE"       -> Icons.Rounded.Medication
        "HOSPITALISATION" -> Icons.Rounded.LocalHospital
        "RADIOLOGIE"      -> Icons.Rounded.CameraAlt
        else              -> Icons.Rounded.HealthAndSafety
    }
    val statutColor = when (service.statut) {
        "REMBOURSE" -> AppColors.StatusGreen
        "VALIDE"    -> AppColors.BrandBlue
        "EN_COURS"  -> AppColors.StatusOrange
        "REJETE"    -> AppColors.StatusRed
        else        -> AppColors.TextSub
    }
    val statutLabel = when (service.statut) {
        "REMBOURSE" -> "Remboursé"
        "VALIDE"    -> "Validé"
        "EN_COURS"  -> "En cours"
        "REJETE"    -> "Rejeté"
        else        -> service.statut ?: ""
    }
    val formattedDate = try {
        val dt = java.time.LocalDateTime.parse(service.dateService)
        dt.format(java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy"))
    } catch (e: Exception) {
        service.dateService ?: ""
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(14.dp),
                color = typeColor.copy(alpha = 0.12f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(typeIcon, contentDescription = null, tint = typeColor, modifier = Modifier.size(24.dp))
                }
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    service.typeService?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "Service",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    service.etablissement ?: "",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Rounded.Schedule,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        formattedDate,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
            Spacer(Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    "${String.format("%,.0f", service.montant ?: 0.0)} F",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = statutColor.copy(alpha = 0.12f),
                    border = BorderStroke(0.5.dp, statutColor.copy(alpha = 0.3f))
                ) {
                    Text(
                        statutLabel,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = statutColor
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
// PERSONNES EN CHARGE (carte enrichie)
// ─────────────────────────────────────────────

@Composable
private fun DependantCard(pc: PersonneChargeDto, token: String?, context: android.content.Context) {
    Card(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 0.dp)
            .fillMaxWidth(),
        shape = androidx.compose.ui.graphics.RectangleShape,
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(52.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    val icon = when (pc.lienParent?.lowercase()) {
                        "epoux", "epouse", "conjoint", "conjointe" -> Icons.Rounded.Favorite
                        "fils", "fille", "enfant"                  -> Icons.Rounded.ChildCare
                        else                                        -> Icons.Rounded.Person
                    }
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    "${pc.prenoms ?: ""} ${pc.nom ?: ""}".trim(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    pc.lienParent ?: "Personne à charge",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                if (pc.matricule != null) {
                    Text(
                        "Matricule : ${pc.matricule}",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            if (pc.matricule != null) {
                Surface(
                    modifier = Modifier.size(50.dp),
                    shape = RoundedCornerShape(10.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(0.4f))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        SubcomposeAsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(ApiConfig.getQrCodeUrl(pc.matricule))
                                .apply { token?.let { addHeader("Authorization", "Bearer $it") } }
                                .build(),
                            contentDescription = "QR",
                            modifier = Modifier.padding(6.dp),
                            loading = {
                                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                }
                            },
                            error = {
                                Icon(
                                    Icons.Rounded.QrCode,
                                    contentDescription = null,
                                    tint = Color.Red.copy(0.3f),
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
// AUTRES INFORMATIONS (carte épurée)
// ─────────────────────────────────────────────

@Composable
private fun InfoCard(adherent: AdherentDto) {
    Card(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            InfoRow(Icons.Rounded.CreditCard, "NIN", adherent.numeroCNi ?: "—")
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            InfoRow(Icons.Rounded.LocationCity, "Commune", adherent.commune ?: "—")
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            InfoRow(Icons.Rounded.Map, "Région", adherent.region ?: "—")
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            InfoRow(Icons.Rounded.Phone, "Téléphone", adherent.whatsapp ?: "—")
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
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
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(Modifier.width(14.dp))
        Text(
            label,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        Text(
            value,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

// ─────────────────────────────────────────────
// TITRE DE SECTION (plus d'espace et logo)
// ─────────────────────────────────────────────

@Composable
private fun DashboardSectionTitle(title: String, icon: ImageVector) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 20.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(12.dp))
        Text(
            title,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 16.sp,
            letterSpacing = 0.2.sp
        )
    }
}

// ─────────────────────────────────────────────
// HEADER DU DASHBOARD (nouveau)
// ─────────────────────────────────────────────

@Composable
private fun DashboardHeader(
    adherent: AdherentDto,
    onProfileClick: () -> Unit,
    token: String?
) {
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(AppColors.BrandBlueDark, AppColors.BrandBlue, AppColors.BrandBlue.copy(alpha = 0.6f), Color.Transparent)
                )
            )
    ) {
        // Cercles décoratifs subtils
        Box(
            modifier = Modifier.size(140.dp).align(Alignment.TopEnd)
                .offset(x = 40.dp, y = (-20).dp)
                .clip(CircleShape).background(Color.White.copy(alpha = 0.04f))
        )
        Box(
            modifier = Modifier.size(80.dp).align(Alignment.CenterStart)
                .offset(x = (-20).dp)
                .clip(CircleShape).background(Color.White.copy(alpha = 0.03f))
        )

        Row(
            modifier = Modifier.fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 24.dp)
                .statusBarsPadding(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(56.dp).clickable { onProfileClick() },
                    shape = CircleShape,
                    color = Color.White,
                    border = BorderStroke(2.dp, Color.White.copy(alpha = 0.4f)),
                    shadowElevation = 8.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (adherent.photo != null) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(ApiConfig.getImageUrl(adherent.photo))
                                    .apply { token?.let { addHeader("Authorization", "Bearer $it") } }
                                    .crossfade(true).build(),
                                contentDescription = "Profil",
                                modifier = Modifier.fillMaxSize().clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            val initials = "${adherent.prenoms?.firstOrNull()?.uppercase() ?: ""}${adherent.nom?.firstOrNull()?.uppercase() ?: ""}"
                            Text(initials.ifEmpty { "?" }, fontWeight = FontWeight.Black, fontSize = 20.sp, color = AppColors.BrandBlue)
                        }
                    }
                }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text("Bonjour,", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.8f))
                    Text(
                        adherent.prenoms?.split(" ")?.firstOrNull() ?: "Utilisateur",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black, color = Color.White,
                        maxLines = 1, overflow = TextOverflow.Ellipsis
                    )
                }
            }
            IconButton(
                onClick = {},
                modifier = Modifier.size(46.dp).background(Color.White.copy(alpha = 0.15f), CircleShape)
            ) {
                Icon(Icons.Rounded.Notifications, "Notifications", tint = Color.White, modifier = Modifier.size(24.dp))
            }
        }
    }
}

// ─────────────────────────────────────────────
// ACTIONS RAPIDES
// ─────────────────────────────────────────────

@Composable
private fun QuickActionsRow(
    onRenew: () -> Unit = {},
    onHistory: () -> Unit = {},
    onAssistance: () -> Unit = {}
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(top = 12.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        QuickActionChip(Modifier.weight(1f), Icons.Rounded.Autorenew, "Renouveler", AppColors.StatusGreen, onRenew)
        QuickActionChip(Modifier.weight(1f), Icons.Rounded.History, "Historique", AppColors.BrandBlue, onHistory)
        QuickActionChip(Modifier.weight(1f), Icons.Rounded.SupportAgent, "Assistance", AppColors.GoldAccent, onAssistance)
    }
}

@Composable
private fun QuickActionChip(
    modifier: Modifier, icon: ImageVector, label: String, color: Color, onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = color.copy(alpha = 0.08f),
        border = BorderStroke(0.5.dp, color.copy(alpha = 0.15f))
    ) {
        Column(
            modifier = Modifier.padding(vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(22.dp))
            Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@Composable
private fun DashboardErrorCard(error: String) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = AppShapes.MediumRadius,
        color = AppColors.StatusRed.copy(alpha = 0.08f),
        border = BorderStroke(0.5.dp, AppColors.StatusRed.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Rounded.ErrorOutline,
                contentDescription = null,
                tint = AppColors.StatusRed,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(12.dp))
            Text(
                error,
                color = AppColors.StatusRed,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
        }
    }
}