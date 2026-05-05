package com.example.sencsu.screen

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.sencsu.data.remote.dto.AdherentDto
import com.example.sencsu.domain.viewmodel.DashboardViewModel
import com.example.sencsu.theme.*
import com.example.sencsu.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    rootNavController: NavController,
    onNavigateToAdherents: () -> Unit = {},
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val dashboardState by viewModel.dashboardState.collectAsState()
    val authState by viewModel.authState.collectAsState()
    val pendingCount by viewModel.pendingCount.collectAsState()
    val showSyncPrompt by viewModel.showSyncPrompt.collectAsState()

    val user = authState.user
    val adherents = dashboardState.data?.data ?: emptyList()
    val isLoading = dashboardState.isLoading
    val activeCount = adherents.count { it.actif == true }

    var showContent by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { showContent = true }

    val initials = remember(user) {
        val p = user?.prenom?.firstOrNull()?.uppercase() ?: ""
        val n = user?.name?.firstOrNull()?.uppercase() ?: ""
        "$p$n".ifEmpty { "?" }
    }

    val fullName = remember(user) {
        "${user?.prenom ?: ""} ${user?.name ?: ""}".trim().ifEmpty { "Agent CSU" }
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { rootNavController.navigate(Screen.AddMember.route) },
                containerColor = AppColors.BrandBlue,
                contentColor = Color.White,
                shape = AppShapes.CircleRadius,
                icon = { Icon(Icons.Rounded.PersonAdd, null) },
                text = { Text("Nouvel Adhérent", fontWeight = FontWeight.Bold) }
            )
        },
        containerColor = AppColors.AppBackground
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = isLoading,
            onRefresh = { viewModel.refresh() },
            modifier = Modifier.padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(AppColors.AppBackground),
                contentPadding = PaddingValues(bottom = 120.dp)
            ) {
                // ── IMMERSIVE HEADER ──
                item {
                    PremiumDashboardHeader(
                        initials = initials,
                        fullName = fullName,
                        rootNavController = rootNavController
                    )
                }

                // ── QUICK ACTIONS ──
                item {
                    QuickActionGrid(
                        onAdd = { rootNavController.navigate(Screen.AddMember.route) },
                        onScan = { rootNavController.navigate(Screen.QRScanner.route) },
                        onSync = { viewModel.syncData() },
                        pendingCount = pendingCount
                    )
                }

                // ── BANNERS ──
                if (showSyncPrompt && pendingCount > 0) {
                    item {
                        SyncPromptBanner(
                            pendingCount = pendingCount,
                            onSync = { viewModel.syncData() },
                            onDismiss = { viewModel.dismissSyncPrompt() }
                        )
                    }
                }

                if (dashboardState.error != null && !isLoading) {
                    item {
                        ErrorBanner(
                            message = dashboardState.error ?: "Erreur de connexion",
                            onRetry = { viewModel.refresh() }
                        )
                    }
                }

                // ── STATS SECTION ──
                item {
                    SectionTitle(
                        title = "Performance Terrain",
                        subtitle = "Aperçu de vos activités d'enrôlement"
                    )
                }

                item {
                    StatsGrid(
                        total = adherents.size,
                        active = activeCount,
                        pending = pendingCount
                    )
                }

                // ── RECENT ACTIVITY ──
                if (adherents.isNotEmpty()) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    "Adhérents récents",
                                    fontWeight = FontWeight.Black,
                                    color = AppColors.TextMain,
                                    fontSize = 16.sp
                                )
                                Text(
                                    "Derniers dossiers enregistrés",
                                    color = AppColors.TextSub,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                            if (adherents.size > 5) {
                                TextButton(onClick = onNavigateToAdherents) {
                                    Text("Voir tout", color = AppColors.BrandBlue, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    items(adherents.take(5)) { adherent ->
                        AnimatedVisibility(
                            visible = showContent,
                            enter = fadeIn(tween(400)) + slideInHorizontally(tween(400)) { it / 10 }
                        ) {
                            DashboardAdherentRow(
                                adherent = adherent,
                                onClick = {
                                    adherent.id?.let { id ->
                                        rootNavController.navigate(Screen.AdherentDetails.createRoute(id))
                                    }
                                }
                            )
                        }
                    }
                } else if (!isLoading && dashboardState.error == null) {
                    item {
                        EmptyState()
                    }
                }
            }
        }
    }
}

@Composable
private fun PremiumDashboardHeader(
    initials: String,
    fullName: String,
    rootNavController: NavController
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(
                Brush.verticalGradient(
                    listOf(AppColors.BrandBlueDark, AppColors.BrandBlue)
                )
            )
            .statusBarsPadding()
    ) {
        // Decorative elements
        Box(
            Modifier
                .size(150.dp)
                .align(Alignment.TopEnd)
                .offset(x = 40.dp, y = (-40).dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.05f))
        )

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile Avatar
            Surface(
                modifier = Modifier.size(64.dp),
                shape = AppShapes.LargeRadius,
                color = Color.White.copy(alpha = 0.15f),
                border = BorderStroke(1.5.dp, Color.White.copy(alpha = 0.2f))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        initials,
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp
                    )
                }
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Bonjour,",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.7f)
                )
                Text(
                    fullName,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }

            // Notification/Scan icons
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(
                    onClick = { rootNavController.navigate(Screen.QRScanner.route) },
                    modifier = Modifier.background(Color.White.copy(alpha = 0.1f), CircleShape)
                ) {
                    Icon(Icons.Rounded.QrCodeScanner, null, tint = Color.White, modifier = Modifier.size(20.dp))
                }
                IconButton(
                    onClick = { /* Notifications */ },
                    modifier = Modifier.background(Color.White.copy(alpha = 0.1f), CircleShape)
                ) {
                    Icon(Icons.Rounded.Notifications, null, tint = Color.White, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@Composable
private fun QuickActionGrid(
    onAdd: () -> Unit,
    onScan: () -> Unit,
    onSync: () -> Unit,
    pendingCount: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .offset(y = (-24).dp)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        QuickActionCard(
            label = "Enrôler",
            icon = Icons.Rounded.PersonAdd,
            color = AppColors.BrandBlue,
            onClick = onAdd,
            modifier = Modifier.weight(1f)
        )
        QuickActionCard(
            label = "Vérifier",
            icon = Icons.Rounded.QrCode,
            color = AppColors.GoldAccent,
            onClick = onScan,
            modifier = Modifier.weight(1f)
        )
        QuickActionCard(
            label = "Synchroniser",
            icon = Icons.Rounded.Sync,
            color = if (pendingCount > 0) AppColors.ActionBlue else AppColors.StatusGrey,
            onClick = onSync,
            badge = if (pendingCount > 0) pendingCount.toString() else null,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun QuickActionCard(
    label: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    badge: String? = null,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = AppShapes.LargeRadius,
        color = AppColors.SurfaceBackground,
        shadowElevation = 6.dp,
        border = BorderStroke(1.dp, AppColors.BorderColorLight)
    ) {
        Box {
            Column(
                modifier = Modifier.padding(vertical = 16.dp, horizontal = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(icon, null, tint = color, modifier = Modifier.size(28.dp))
                Text(
                    label,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.TextMain
                )
            }
            if (badge != null) {
                Surface(
                    modifier = Modifier.align(Alignment.TopEnd).padding(6.dp),
                    shape = CircleShape,
                    color = AppColors.StatusRed,
                    contentColor = Color.White
                ) {
                    Text(
                        badge,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}

@Composable
private fun StatsGrid(total: Int, active: Int, pending: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        MiniMetricCard("Dossiers", total.toString(), AppColors.BrandBlue, Modifier.weight(1f))
        MiniMetricCard("Actifs", active.toString(), AppColors.StatusGreen, Modifier.weight(1f))
        MiniMetricCard("En attente", pending.toString(), AppColors.StatusOrange, Modifier.weight(1f))
    }
}

@Composable
private fun MiniMetricCard(label: String, value: String, color: Color, modifier: Modifier) {
    Surface(
        modifier = modifier,
        shape = AppShapes.MediumRadius,
        color = color.withAlpha(0.05f),
        border = BorderStroke(1.dp, color.withAlpha(0.12f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = color)
            Text(label, style = MaterialTheme.typography.labelSmall, color = AppColors.TextSub)
        }
    }
}

@Composable
private fun DashboardAdherentRow(adherent: AdherentDto, onClick: () -> Unit) {
    val name = "${adherent.prenoms ?: ""} ${adherent.nom ?: ""}".trim()
    val isActive = adherent.actif == true

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = AppShapes.MediumRadius,
        colors = CardDefaults.cardColors(containerColor = AppColors.SurfaceBackground),
        border = BorderStroke(0.5.dp, AppColors.BorderColorLight)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar simple
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = AppColors.SurfaceAlt
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Rounded.Person, null, tint = AppColors.TextSub, modifier = Modifier.size(20.dp))
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    name.ifEmpty { "Sans nom" },
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = AppColors.TextMain,
                    maxLines = 1
                )
                Text(
                    adherent.matricule ?: "Pas de matricule",
                    style = MaterialTheme.typography.labelSmall,
                    color = AppColors.TextDisabled
                )
            }

            Surface(
                shape = AppShapes.CircleRadius,
                color = if (isActive) AppColors.StatusGreenSoft else AppColors.StatusRedSoft
            ) {
                Text(
                    if (isActive) "Actif" else "Inactif",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isActive) AppColors.StatusGreen else AppColors.StatusRed
                )
            }
        }
    }
}

@Composable
private fun SyncPromptBanner(pendingCount: Int, onSync: () -> Unit, onDismiss: () -> Unit) {
    Surface(
        modifier = Modifier.padding(16.dp).fillMaxWidth(),
        color = AppColors.ActionBlue.withAlpha(0.1f),
        shape = AppShapes.LargeRadius,
        border = BorderStroke(1.dp, AppColors.ActionBlue.withAlpha(0.2f))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.CloudUpload, null, tint = AppColors.ActionBlue)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Synchronisation", fontWeight = FontWeight.Bold, color = AppColors.ActionBlue, fontSize = 14.sp)
                Text("$pendingCount dossier(s) en attente", fontSize = 12.sp, color = AppColors.TextSub)
            }
            TextButton(onClick = onSync) { Text("Sync", fontWeight = FontWeight.Black) }
            IconButton(onClick = onDismiss) { Icon(Icons.Rounded.Close, null, modifier = Modifier.size(16.dp)) }
        }
    }
}

@Composable
private fun ErrorBanner(message: String, onRetry: () -> Unit) {
    Surface(
        modifier = Modifier.padding(16.dp).fillMaxWidth(),
        color = AppColors.StatusRedSoft,
        shape = AppShapes.LargeRadius,
        border = BorderStroke(1.dp, AppColors.StatusRed.withAlpha(0.2f))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.Error, null, tint = AppColors.StatusRed)
            Spacer(Modifier.width(12.dp))
            Text(message, fontSize = 12.sp, color = AppColors.TextSub, modifier = Modifier.weight(1f))
            TextButton(onClick = onRetry) { Text("Réessayer", color = AppColors.StatusRed) }
        }
    }
}

@Composable
private fun EmptyState() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(Icons.Rounded.Search, null, modifier = Modifier.size(48.dp), tint = AppColors.TextDisabled)
        Text("Aucun adhérent trouvé", fontWeight = FontWeight.Bold, color = AppColors.TextMain)
        Text("Les nouveaux enrôlements apparaîtront ici", style = MaterialTheme.typography.bodySmall, color = AppColors.TextSub)
    }
}

@Composable
private fun SectionTitle(title: String, subtitle: String) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
        Text(title, fontWeight = FontWeight.Black, color = AppColors.TextMain, fontSize = 16.sp)
        Text(subtitle, color = AppColors.TextSub, style = MaterialTheme.typography.labelSmall)
    }
}
