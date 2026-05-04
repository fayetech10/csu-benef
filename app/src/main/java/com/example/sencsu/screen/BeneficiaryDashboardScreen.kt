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
    onProfileClick: () -> Unit = {},
    viewModel: BeneficiaryDashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val token by viewModel.sessionManager.tokenFlow.collectAsState(initial = null)
    val context = LocalContext.current
    val adherent = uiState.adherent
    var showFullScreenCard by remember { mutableStateOf(false) }
    var showContent by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(80)
        showContent = true
    }

    PullToRefreshBox(
        isRefreshing = uiState.isLoading,
        onRefresh = { viewModel.refresh() }
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            if (adherent != null) {
                // En-tête
                item {
                    DashboardHeader(
                        adherent = adherent,
                        onProfileClick = onProfileClick,
                        token = token
                    )
                }

                // Carte de membre
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
                        Box(modifier = Modifier.clickable { showFullScreenCard = true }) {
                            HealthInsuranceCard(
                                data = adherent,
                                sessionManager = viewModel.sessionManager,
                                modifier = Modifier
                                    .padding(horizontal = 16.dp)
                                    .padding(top = 4.dp, bottom = 8.dp)
                            )
                        }
                    }
                }

                // Quick Stats
                item {
                    AnimatedVisibility(
                        visible = showContent,
                        enter = fadeIn(tween(400, delayMillis = 150)) + slideInVertically(
                            tween(400, delayMillis = 150),
                            initialOffsetY = { 20 }
                        )
                    ) {
                        QuickStatsRow(adherent)
                    }
                }

                // Couverture
                item {
                    AnimatedVisibility(
                        visible = showContent,
                        enter = fadeIn(tween(400, delayMillis = 250)) + slideInVertically(
                            tween(400, delayMillis = 250),
                            initialOffsetY = { 20 }
                        )
                    ) {
                        CoverageStatusCard(adherent)
                    }
                }

                // Services médicaux récents
                if (uiState.recentServices.isNotEmpty()) {
                    item {
                        AnimatedVisibility(
                            visible = showContent,
                            enter = fadeIn(tween(400, delayMillis = 350)) + slideInVertically(
                                tween(400, delayMillis = 350),
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

                // Personnes en charge
                if (!adherent.personnesCharge.isNullOrEmpty()) {
                    item {
                        AnimatedVisibility(
                            visible = showContent,
                            enter = fadeIn(tween(300, delayMillis = 420))
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
                            enter = fadeIn(tween(300, delayMillis = 450 + index * 60)) +
                                    slideInHorizontally(
                                        tween(300, delayMillis = 450 + index * 60),
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


            }

            // Erreur
            if (uiState.error != null) {
                item {
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
                                uiState.error!!,
                                color = AppColors.StatusRed,
                                fontSize = 14.sp,
                                lineHeight = 20.sp
                            )
                        }
                    }
                }
            }
        }

        if (showFullScreenCard && adherent != null) {
            FullScreenCardOverlay(
                adherent = adherent,
                token = token,
                onClose = { showFullScreenCard = false }
            )
        }
    }
}

// ─────────────────────────────────────────────
// CARTE PLEIN ÉCRAN (Portrait)
// ─────────────────────────────────────────────

@Composable
private fun FullScreenCardOverlay(
    adherent: AdherentDto,
    token: String?,
    onClose: () -> Unit
) {
    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Bouton fermer
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surface, CircleShape)
                            .shadow(4.dp, CircleShape)
                    ) {
                        Icon(Icons.Rounded.Close, contentDescription = "Fermer")
                    }
                }
                
                Spacer(Modifier.height(16.dp))
                
                Text(
                    "CARTE DE COUVERTURE SANTÉ",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = AppColors.BrandBlue,
                    letterSpacing = 1.2.sp
                )

                Spacer(Modifier.height(32.dp))

                // La Carte Portrait
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(24.dp, RoundedCornerShape(28.dp)),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Photo agrandie
                        Surface(
                            modifier = Modifier
                                .size(160.dp)
                                .shadow(8.dp, RoundedCornerShape(20.dp)),
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            if (adherent.photo != null) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(ApiConfig.getImageUrl(adherent.photo))
                                        .apply { token?.let { addHeader("Authorization", "Bearer $it") } }
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "Photo de profil",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(
                                    Icons.Rounded.Person,
                                    contentDescription = null,
                                    modifier = Modifier.size(80.dp),
                                    tint = AppColors.BrandBlue.copy(alpha = 0.5f)
                                )
                            }
                        }
                        
                        Spacer(Modifier.height(32.dp))
                        
                        // QR Code géant pour le scan
                        Surface(
                            modifier = Modifier
                                .size(240.dp)
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                                .padding(16.dp),
                            color = Color.White,
                            shape = RoundedCornerShape(20.dp)
                        ) {
                             AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(ApiConfig.getQrCodeUrl(adherent.matricule.orEmpty()))
                                    .apply { token?.let { addHeader("Authorization", "Bearer $it") } }
                                    .build(),
                                contentDescription = "QR Code de scan",
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        
                        Spacer(Modifier.height(32.dp))
                        
                        // Informations Identité
                        Text(
                            text = "${adherent.prenoms} ${adherent.nom}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        Surface(
                            modifier = Modifier.padding(top = 12.dp),
                            color = AppColors.BrandBlue.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = adherent.matricule ?: "SANS MATRICULE",
                                style = MaterialTheme.typography.titleMedium,
                                color = AppColors.BrandBlue,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                            )
                        }
                        
                        Spacer(Modifier.height(32.dp))
                        
                        HorizontalDivider(
                            modifier = Modifier.fillMaxWidth(),
                            thickness = 0.5.dp,
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
                        
                        Spacer(Modifier.height(24.dp))
                        
                        // Détails techniques
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            CardInfoRow("Date de Naissance", adherent.dateNaissance ?: "-")
                            CardInfoRow("Sexe", adherent.sexe ?: "-")
                            CardInfoRow("Régime", adherent.regime ?: "-")
                            CardInfoRow("Statut", "ACTIF", AppColors.StatusGreen)
                        }
                    }
                }
                
                Spacer(Modifier.height(40.dp))
                
                Text(
                    "Cette carte est strictement personnelle.\nEn cas d'urgence, présentez ce code au personnel médical.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(bottom = 32.dp)
                )
            }
        }
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
        modifier = modifier
            .shadow(2.dp, RoundedCornerShape(16.dp), clip = false),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(color.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }
            Text(
                value,
                fontWeight = FontWeight.Black,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
            Text(
                label,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium,
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
        } catch (e: Exception) {
            0.1f
        }
    }

    val progressColor = when {
        progress >= 0.85f -> AppColors.StatusRed
        progress >= 0.65f -> AppColors.StatusOrange
        else -> AppColors.StatusGreen
    }

    Surface(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(20.dp), clip = true),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape,
                    color = if (isActive) AppColors.StatusGreen.copy(0.12f) else AppColors.StatusRed.copy(0.12f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            if (isActive) Icons.Rounded.Shield else Icons.Rounded.Warning,
                            contentDescription = null,
                            tint = if (isActive) AppColors.StatusGreen else AppColors.StatusRed,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Spacer(Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Validité de la couverture",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        coveragePeriod,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (isActive) AppColors.StatusGreen.copy(0.12f) else AppColors.StatusRed.copy(0.12f)
                ) {
                    Text(
                        if (isActive) "ACTIF" else "INACTIF",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp,
                        color = if (isActive) AppColors.StatusGreen else AppColors.StatusRed
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Progression du contrat",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "${(progress * 100).toInt()}%",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    color = progressColor
                )
            }

            Spacer(Modifier.height(10.dp))

            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = progressColor,
                trackColor = progressColor.copy(alpha = 0.15f),
                strokeCap = StrokeCap.Round
            )

            Spacer(Modifier.height(8.dp))
            Text(
                "Cette carte est valable 1 an à compter de sa date d'émission.",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
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
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 20.dp)
            .statusBarsPadding(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Avatar avec gradient et ombre
            Surface(
                modifier = Modifier
                    .size(52.dp)
                    .clickable { onProfileClick() }
                    .shadow(4.dp, CircleShape),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (adherent.photo != null) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(ApiConfig.getImageUrl(adherent.photo))
                                .apply { token?.let { addHeader("Authorization", "Bearer $it") } }
                                .crossfade(true)
                                .build(),
                            contentDescription = "Profil",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            Icons.Rounded.Person,
                            contentDescription = null,
                            tint = AppColors.BrandBlue,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }
            
            Spacer(Modifier.width(14.dp))
            
            Column {
                Text(
                    text = "Bonjour,",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = adherent.prenoms?.split(" ")?.firstOrNull() ?: "Utilisateur",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        
        // Actions rapides (Notifications)
        Box {
            IconButton(
                onClick = { /* TODO: Notifications */ },
                modifier = Modifier
                    .size(44.dp)
                    .background(MaterialTheme.colorScheme.surface, CircleShape)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(
                    Icons.Rounded.Notifications,
                    contentDescription = "Notifications",
                    tint = AppColors.BrandBlue,
                    modifier = Modifier.size(24.dp)
                )
            }
            // Badge de notification (simulé)
            Surface(
                modifier = Modifier
                    .size(10.dp)
                    .align(Alignment.TopEnd)
                    .padding(top = 2.dp, end = 2.dp),
                shape = CircleShape,
                color = AppColors.StatusRed,
                border = BorderStroke(1.5.dp, Color.White)
            ) {}
        }
    }
}