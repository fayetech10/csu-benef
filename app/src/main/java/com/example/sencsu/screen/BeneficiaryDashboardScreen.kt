package com.example.sencsu.screen

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Badge
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.FamilyRestroom
import androidx.compose.material.icons.rounded.Group
import androidx.compose.material.icons.rounded.HealthAndSafety
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LocalHospital
import androidx.compose.material.icons.rounded.MedicalServices
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.QrCode
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.SupportAgent
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.sencsu.components.QrCodeImage
import com.example.sencsu.configs.ApiConfig
import com.example.sencsu.data.remote.dto.AdherentDto
import com.example.sencsu.data.remote.dto.PersonneChargeDto
import com.example.sencsu.data.remote.dto.ServiceMedicalDto
import com.example.sencsu.domain.viewmodel.BeneficiaryDashboardViewModel
import com.example.sencsu.theme.AppColors
import com.example.sencsu.theme.AppElevation
import com.example.sencsu.theme.AppGradients
import com.example.sencsu.theme.AppShapes
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(AppColors.AppBackground),
            contentPadding = PaddingValues(bottom = 126.dp)
        ) {
            item {
                HomeHero(
                    adherent = adherent,
                    token = token,
                    isLoading = uiState.isLoading,
                    onProfileClick = onProfileClick,
                    onRefresh = { viewModel.refresh() }
                )
            }

            if (adherent == null && uiState.isLoading) {
                item { HomeLoadingState() }
            } else if (adherent == null) {
                item {
                    HomeErrorCard(
                        message = uiState.error ?: "Impossible de charger votre espace beneficiaire.",
                        onRetry = { viewModel.refresh() }
                    )
                }
            } else {
                item {
                    AnimatedBlock(showContent, 90) {
                        CoverageCard(
                            adherent = adherent,
                            onOpenCard = { adherent.id?.let(onNavigateToCard) }
                        )
                    }
                }

                item {
                    AnimatedBlock(showContent, 150) {
                        HomeQuickActions(
                            onOpenCard = { adherent.id?.let(onNavigateToCard) },
                            onHistory = { adherent.id?.let(onNavigateToHistory) },
                            onSupport = {}
                        )
                    }
                }

                item {
                    AnimatedBlock(showContent, 210) {
                        HomeStats(adherent = adherent, services = uiState.recentServices)
                    }
                }

                if (uiState.recentServices.isNotEmpty()) {
                    item {
                        SectionHeader(
                            title = "Activite recente",
                            subtitle = "Derniers soins et remboursements",
                            actionLabel = "Tout voir",
                            onAction = { adherent.id?.let(onNavigateToHistory) }
                        )
                    }
                    items(uiState.recentServices.take(3), key = { it.id ?: it.hashCode().toString() }) { service ->
                        ServiceActivityCard(service = service)
                    }
                } else {
                    item {
                        EmptyActivityCard(onHistory = { adherent.id?.let(onNavigateToHistory) })
                    }
                }

                item {
                    SectionHeader(
                        title = "Foyer couvert",
                        subtitle = "${adherent.personnesCharge.size} personne(s) rattachee(s)",
                        actionLabel = null,
                        onAction = {}
                    )
                }

                if (adherent.personnesCharge.isEmpty()) {
                    item { FamilyEmptyPreview() }
                } else {
                    items(adherent.personnesCharge.take(3), key = { it.id ?: it.displayName }) { member ->
                        FamilyPreviewRow(member = member)
                    }
                }

                if (uiState.error != null) {
                    item {
                        HomeErrorCard(
                            message = uiState.error ?: "Erreur inconnue",
                            onRetry = { viewModel.refresh() }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AnimatedBlock(
    visible: Boolean,
    delayMillis: Int,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(280, delayMillis = delayMillis)) +
                slideInVertically(tween(280, delayMillis = delayMillis), initialOffsetY = { 28 })
    ) {
        content()
    }
}

@Composable
private fun HomeHero(
    adherent: AdherentDto?,
    token: String?,
    isLoading: Boolean,
    onProfileClick: () -> Unit,
    onRefresh: () -> Unit
) {
    val context = LocalContext.current
    val firstName = adherent?.prenoms?.split(" ")?.firstOrNull()?.ifBlank { null } ?: "Beneficiaire"
    val initials = "${adherent?.prenoms?.firstOrNull()?.uppercase() ?: ""}${adherent?.nom?.firstOrNull()?.uppercase() ?: ""}".ifEmpty { "?" }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(282.dp)
            .background(Brush.verticalGradient(AppGradients.Brand))
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 18.dp)
    ) {
        Box(
            modifier = Modifier
                .size(160.dp)
                .align(Alignment.TopEnd)
                .offset(x = 48.dp, y = (-52).dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.06f))
        )
        Box(
            modifier = Modifier
                .size(92.dp)
                .align(Alignment.BottomStart)
                .offset(x = (-28).dp, y = 20.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.05f))
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(54.dp).clickable(onClick = onProfileClick),
                    shape = CircleShape,
                    color = Color.White,
                    border = BorderStroke(2.dp, Color.White.copy(alpha = 0.35f)),
                    shadowElevation = 8.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        val photoUrl = ApiConfig.getImageUrl(adherent?.photo)
                        if (photoUrl != null) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(photoUrl)
                                    .apply { token?.let { addHeader("Authorization", "Bearer $it") } }
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Profil",
                                modifier = Modifier.fillMaxSize().clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Text(initials, fontWeight = FontWeight.Black, fontSize = 19.sp, color = AppColors.BrandBlue)
                        }
                    }
                }
                Spacer(Modifier.width(14.dp))
                Column {
                    Text("Bonjour,", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.76f))
                    Text(
                        firstName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(
                    onClick = onRefresh,
                    modifier = Modifier.size(44.dp).background(Color.White.copy(alpha = 0.14f), CircleShape)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Color.White)
                    } else {
                        Icon(Icons.Rounded.Refresh, contentDescription = "Actualiser", tint = Color.White)
                    }
                }
                IconButton(
                    onClick = {},
                    modifier = Modifier.size(44.dp).background(Color.White.copy(alpha = 0.14f), CircleShape)
                ) {
                    Icon(Icons.Rounded.Notifications, contentDescription = "Notifications", tint = Color.White)
                }
            }
        }

        Column(
            modifier = Modifier.align(Alignment.BottomStart).padding(bottom = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                shape = AppShapes.CircleRadius,
                color = Color.White.copy(alpha = 0.14f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.18f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Rounded.HealthAndSafety, null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(7.dp))
                    Text("Espace beneficiaire", color = Color.White, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
            }
            Text(
                "Votre couverture en un coup d'oeil",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
            Text(
                "Carte, droits, foyer et derniers soins regroupes dans un accueil clair.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.76f),
                lineHeight = 20.sp
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun CoverageCard(
    adherent: AdherentDto,
    onOpenCard: () -> Unit
) {
    val isActive = adherent.actif != false
    val progress = coverageProgress(adherent.createdAt)
    val remainingDays = remainingCoverageDays(adherent.createdAt)
    val progressColor = when {
        !isActive -> AppColors.StatusRed
        progress >= 0.85f -> AppColors.StatusOrange
        else -> AppColors.StatusGreen
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .offset(y = (-24).dp)
            .padding(horizontal = 16.dp)
            .clickable(onClick = onOpenCard),
        shape = AppShapes.LargeRadius,
        colors = CardDefaults.cardColors(containerColor = AppColors.SurfaceBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = AppElevation.cardRaised),
        border = BorderStroke(1.dp, AppColors.BorderColorLight)
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = AppShapes.MediumRadius,
                    color = if (isActive) AppColors.StatusGreenSoft else AppColors.StatusRedSoft
                ) {
                    Icon(
                        if (isActive) Icons.Rounded.Shield else Icons.Rounded.ErrorOutline,
                        null,
                        tint = if (isActive) AppColors.StatusGreen else AppColors.StatusRed,
                        modifier = Modifier.padding(10.dp).size(24.dp)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Couverture sante", style = MaterialTheme.typography.labelMedium, color = AppColors.TextSub)
                    Text(
                        adherent.coveragePeriod.orDash(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = AppColors.TextMain,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                StatusPill(if (isActive) "ACTIF" else "INACTIF", isActive)
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text("Validite", style = MaterialTheme.typography.labelMedium, color = AppColors.TextSub)
                    Text(
                        if (remainingDays > 0) "$remainingDays jour(s) restants" else "A verifier",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = progressColor
                    )
                }
                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(AppShapes.CircleRadius),
                    color = progressColor,
                    trackColor = AppColors.SurfaceAlt,
                    strokeCap = StrokeCap.Round
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                CoverageMiniInfo("Matricule", adherent.matricule.orDash(), Icons.Rounded.Badge, Modifier.weight(1f))
                CoverageMiniInfo("Carte", adherent.numeroCarte.orDash(), Icons.Rounded.CreditCard, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun StatusPill(label: String, active: Boolean) {
    Surface(
        shape = AppShapes.CircleRadius,
        color = if (active) AppColors.StatusGreenSoft else AppColors.StatusRedSoft,
        border = BorderStroke(1.dp, if (active) AppColors.StatusGreen.copy(alpha = 0.18f) else AppColors.StatusRed.copy(alpha = 0.18f))
    ) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Black,
            color = if (active) AppColors.StatusGreen else AppColors.StatusRed
        )
    }
}

@Composable
private fun CoverageMiniInfo(label: String, value: String, icon: ImageVector, modifier: Modifier) {
    Surface(
        modifier = modifier,
        shape = AppShapes.MediumRadius,
        color = AppColors.SurfaceMuted,
        border = BorderStroke(1.dp, AppColors.BorderColorLight)
    ) {
        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = AppColors.BrandBlue, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Column {
                Text(label, style = MaterialTheme.typography.labelSmall, color = AppColors.TextSub)
                Text(value, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Black, color = AppColors.TextMain, maxLines = 1)
            }
        }
    }
}

@Composable
private fun HomeQuickActions(onOpenCard: () -> Unit, onHistory: () -> Unit, onSupport: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        QuickActionTile("Carte", Icons.Rounded.QrCode, AppColors.BrandBlue, onOpenCard, Modifier.weight(1f))
        QuickActionTile("Historique", Icons.Rounded.History, AppColors.ActionBlue, onHistory, Modifier.weight(1f))
        QuickActionTile("Assistance", Icons.Rounded.SupportAgent, AppColors.GoldAccent, onSupport, Modifier.weight(1f))
    }
}

@Composable
private fun QuickActionTile(
    label: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(86.dp),
        shape = AppShapes.LargeRadius,
        colors = CardDefaults.cardColors(containerColor = AppColors.SurfaceBackground),
        border = BorderStroke(1.dp, AppColors.BorderColorLight)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(shape = CircleShape, color = color.copy(alpha = 0.1f)) {
                Icon(icon, null, tint = color, modifier = Modifier.padding(8.dp).size(20.dp))
            }
            Spacer(Modifier.height(7.dp))
            Text(label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Black, color = AppColors.TextMain)
        }
    }
}

@Composable
private fun HomeStats(adherent: AdherentDto, services: List<ServiceMedicalDto>) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        StatCard("Foyer", adherent.personnesCharge.size.toString(), Icons.Rounded.Group, AppColors.BrandBlue, Modifier.weight(1f))
        StatCard("Soins", services.size.toString(), Icons.Rounded.LocalHospital, AppColors.ActionBlue, Modifier.weight(1f))
        StatCard("Regime", adherent.regime?.take(8).orDash(), Icons.Rounded.VerifiedUser, AppColors.GoldAccent, Modifier.weight(1f))
    }
}

@Composable
private fun StatCard(label: String, value: String, icon: ImageVector, color: Color, modifier: Modifier) {
    Surface(
        modifier = modifier,
        shape = AppShapes.LargeRadius,
        color = AppColors.SurfaceBackground,
        border = BorderStroke(1.dp, AppColors.BorderColorLight)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            Surface(shape = AppShapes.MediumRadius, color = color.copy(alpha = 0.1f)) {
                Icon(icon, null, tint = color, modifier = Modifier.padding(8.dp).size(19.dp))
            }
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = AppColors.TextMain, maxLines = 1)
            Text(label, style = MaterialTheme.typography.labelSmall, color = AppColors.TextSub, maxLines = 1)
        }
    }
}

@Composable
private fun SectionHeader(title: String, subtitle: String, actionLabel: String?, onAction: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(top = 10.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = AppColors.TextMain)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = AppColors.TextSub)
        }
        if (actionLabel != null) {
            TextButton(onClick = onAction) {
                Text(actionLabel, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ServiceActivityCard(service: ServiceMedicalDto) {
    val statusColor = when (service.statut?.uppercase()) {
        "REMBOURSE", "REMBOURSÉ", "VALIDE", "VALIDÉ" -> AppColors.StatusGreen
        "REJETE", "REJETÉ", "ANNULE", "ANNULÉ" -> AppColors.StatusRed
        else -> AppColors.StatusOrange
    }

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 5.dp),
        shape = AppShapes.LargeRadius,
        colors = CardDefaults.cardColors(containerColor = AppColors.SurfaceBackground),
        border = BorderStroke(1.dp, AppColors.BorderColorLight)
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = AppShapes.MediumRadius, color = statusColor.copy(alpha = 0.1f)) {
                Icon(Icons.Rounded.MedicalServices, null, tint = statusColor, modifier = Modifier.padding(10.dp).size(22.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    service.typeService?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "Service medical",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Black,
                    color = AppColors.TextMain,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    service.etablissement.orDash(),
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.TextSub,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Schedule, null, tint = AppColors.TextDisabled, modifier = Modifier.size(13.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(formatDate(service.dateService), style = MaterialTheme.typography.labelSmall, color = AppColors.TextSub)
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("${String.format("%,.0f", service.montant ?: 0.0)} F", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Black)
                Spacer(Modifier.height(5.dp))
                Surface(shape = AppShapes.CircleRadius, color = statusColor.copy(alpha = 0.1f)) {
                    Text(
                        service.statut ?: "En cours",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = statusColor,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyActivityCard(onHistory: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
        shape = AppShapes.LargeRadius,
        colors = CardDefaults.cardColors(containerColor = AppColors.SurfaceBackground),
        border = BorderStroke(1.dp, AppColors.BorderColorLight)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = CircleShape, color = AppColors.ActionBlueSoft) {
                Icon(Icons.Rounded.History, null, tint = AppColors.ActionBlue, modifier = Modifier.padding(10.dp).size(22.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Aucune activite recente", fontWeight = FontWeight.Black, color = AppColors.TextMain)
                Text("Votre historique apparaitra ici apres vos soins.", style = MaterialTheme.typography.bodySmall, color = AppColors.TextSub)
            }
            IconButton(onClick = onHistory) {
                Icon(Icons.AutoMirrored.Rounded.ArrowForward, null, tint = AppColors.ActionBlue)
            }
        }
    }
}

@Composable
private fun FamilyPreviewRow(member: PersonneChargeDto) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 5.dp),
        shape = AppShapes.LargeRadius,
        colors = CardDefaults.cardColors(containerColor = AppColors.SurfaceBackground),
        border = BorderStroke(1.dp, AppColors.BorderColorLight)
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = CircleShape, color = AppColors.BrandBlueLite, modifier = Modifier.size(48.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Text(member.initials, fontWeight = FontWeight.Black, color = AppColors.BrandBlue)
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(member.displayName, fontWeight = FontWeight.Black, color = AppColors.TextMain, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(member.lienParent.orDash(), style = MaterialTheme.typography.bodySmall, color = AppColors.TextSub)
            }
            Surface(
                modifier = Modifier.size(44.dp),
                shape = AppShapes.SmallRadius,
                color = AppColors.SurfaceMuted,
                border = BorderStroke(1.dp, AppColors.BorderColorLight)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (member.matricule.isNullOrBlank()) {
                        Icon(Icons.Rounded.QrCode, null, tint = AppColors.TextDisabled, modifier = Modifier.size(24.dp))
                    } else {
                        QrCodeImage(
                            value = member.matricule,
                            modifier = Modifier.padding(5.dp).fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FamilyEmptyPreview() {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 5.dp),
        shape = AppShapes.LargeRadius,
        color = AppColors.BrandBlueLite,
        border = BorderStroke(1.dp, AppColors.BrandBlue.copy(alpha = 0.12f))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.FamilyRestroom, null, tint = AppColors.BrandBlue)
            Spacer(Modifier.width(12.dp))
            Column {
                Text("Aucun membre rattache", fontWeight = FontWeight.Black, color = AppColors.TextMain)
                Text("Ajoutez votre foyer depuis l'onglet Foyer.", style = MaterialTheme.typography.bodySmall, color = AppColors.TextSub)
            }
        }
    }
}

@Composable
private fun HomeLoadingState() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 70.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(color = AppColors.BrandBlue)
        Spacer(Modifier.height(14.dp))
        Text("Chargement de votre accueil...", color = AppColors.TextSub)
    }
}

@Composable
private fun HomeErrorCard(message: String, onRetry: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        shape = AppShapes.LargeRadius,
        colors = CardDefaults.cardColors(containerColor = AppColors.StatusRedSoft),
        border = BorderStroke(1.dp, AppColors.StatusRed.copy(alpha = 0.18f))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.ErrorOutline, null, tint = AppColors.StatusRed)
                Spacer(Modifier.width(10.dp))
                Text(message, color = AppColors.StatusRed, fontWeight = FontWeight.Bold)
            }
            OutlinedButton(
                onClick = onRetry,
                shape = AppShapes.MediumRadius,
                border = BorderStroke(1.dp, AppColors.StatusRed.copy(alpha = 0.24f)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = AppColors.StatusRed)
            ) {
                Text("Reessayer")
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun coverageProgress(createdAt: String?): Float {
    return try {
        val created = LocalDateTime.parse(createdAt)
        val end = created.plusYears(1)
        val totalDays = ChronoUnit.DAYS.between(created, end).toFloat()
        val elapsedDays = ChronoUnit.DAYS.between(created, LocalDateTime.now()).toFloat()
        (elapsedDays / totalDays).coerceIn(0f, 1f)
    } catch (e: Exception) {
        0f
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun remainingCoverageDays(createdAt: String?): Long {
    return try {
        val end = LocalDateTime.parse(createdAt).plusYears(1)
        ChronoUnit.DAYS.between(LocalDateTime.now(), end).coerceAtLeast(0)
    } catch (e: Exception) {
        0L
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun formatDate(value: String?): String {
    return try {
        LocalDateTime.parse(value).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
    } catch (e: Exception) {
        value.orDash()
    }
}

private fun String?.orDash(): String = this?.takeIf { it.isNotBlank() } ?: "-"
