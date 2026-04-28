package com.example.sencsu.screen

import androidx.compose.animation.*
import androidx.compose.foundation.background
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.sencsu.data.remote.dto.AdherentDto
import com.example.sencsu.domain.viewmodel.DashboardViewModel
import com.example.sencsu.theme.AppColors
import com.example.sencsu.theme.AppShapes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    rootNavController: NavController,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val dashboardState by viewModel.dashboardState.collectAsState()
    val authState by viewModel.authState.collectAsState()
    val pendingCount by viewModel.pendingCount.collectAsState()
    val showSyncPrompt by viewModel.showSyncPrompt.collectAsState()

    val user = authState.user
    val adherents = dashboardState.data?.data ?: emptyList()
    val isLoading = dashboardState.isLoading

    // Initiales de l'agent
    val initials = remember(user) {
        val p = user?.prenom?.firstOrNull()?.uppercase() ?: ""
        val n = user?.name?.firstOrNull()?.uppercase() ?: ""
        "$p$n".ifEmpty { "?" }
    }

    val fullName = remember(user) {
        "${user?.prenom ?: ""} ${user?.name ?: ""}".trim().ifEmpty { "Agent" }
    }

    PullToRefreshBox(
        isRefreshing = isLoading,
        onRefresh = { viewModel.refresh() }
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(AppColors.AppBackground),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // HEADER
            item {
                DashboardHeader(
                    initials = initials,
                    fullName = fullName,
                    adherentsCount = adherents.size,
                    onNotificationsClick = { /* TODO */ }
                )
            }

            // SYNC PROMPT
            if (showSyncPrompt) {
                item {
                    SyncPromptBanner(
                        pendingCount = pendingCount,
                        onSync = { viewModel.syncData() },
                        onDismiss = { viewModel.dismissSyncPrompt() }
                    )
                }
            }

            // ERROR
            if (dashboardState.error != null && !isLoading) {
                item {
                    ErrorBanner(
                        message = dashboardState.error ?: "Erreur inconnue",
                        onRetry = { viewModel.refresh() }
                    )
                }
            }

            // STATS
            item {
                val activeCount = adherents.count { it.actif == true }
                StatsGrid(
                    totalAdherents = adherents.size,
                    activeAdherents = activeCount,
                    pendingSync = pendingCount
                )
            }

            // RECENT ADHERENTS (Top 5)
            if (adherents.isNotEmpty()) {
                item {
                    SectionTitle("Adhérents récents")
                }

                items(adherents.take(5)) { adherent ->
                    AdherentRow(adherent = adherent)
                }

                if (adherents.size > 5) {
                    item {
                        TextButton(
                            onClick = { /* Navigate to full list */ },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                        ) {
                            Text(
                                "Voir tous les ${adherents.size} adhérents",
                                fontWeight = FontWeight.Bold,
                                color = AppColors.BrandBlue
                            )
                        }
                    }
                }
            }

            // EMPTY STATE
            if (adherents.isEmpty() && !isLoading && dashboardState.error == null) {
                item {
                    EmptyState()
                }
            }
        }
    }
}

@Composable
private fun DashboardHeader(
    initials: String,
    fullName: String,
    adherentsCount: Int,
    onNotificationsClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(AppColors.BrandBlue, AppColors.BrandBlueDark)
                )
            )
            .padding(24.dp)
            .padding(top = 20.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(50.dp),
                    shape = CircleShape,
                    color = Color.White.copy(0.2f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(initials, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("Bonjour,", color = Color.White.copy(0.7f), fontSize = 14.sp)
                    Text(fullName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
                Spacer(Modifier.weight(1f))
                IconButton(onClick = onNotificationsClick) {
                    Icon(Icons.Rounded.Notifications, null, tint = Color.White)
                }
            }

            Spacer(Modifier.height(24.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = AppShapes.MediumRadius,
                color = Color.White.copy(0.1f)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "ADHÉRENTS GÉRÉS",
                            color = Color.White.copy(0.6f),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "$adherentsCount adhérents enregistrés",
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    Surface(color = AppColors.StatusGreen, shape = CircleShape) {
                        Text(
                            "$adherentsCount",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SyncPromptBanner(
    pendingCount: Int,
    onSync: () -> Unit,
    onDismiss: () -> Unit
) {
    Surface(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
        color = AppColors.ActionBlue.copy(0.1f),
        shape = AppShapes.MediumRadius,
        border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.ActionBlue.copy(0.3f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Rounded.CloudUpload, null, tint = AppColors.ActionBlue)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Données en attente",
                    fontWeight = FontWeight.Bold,
                    color = AppColors.ActionBlue,
                    fontSize = 14.sp
                )
                Text(
                    "$pendingCount enregistrement(s) à synchroniser",
                    fontSize = 12.sp,
                    color = AppColors.TextSub
                )
            }
            Column {
                TextButton(onClick = onSync) {
                    Text("Sync", fontWeight = FontWeight.Black, color = AppColors.ActionBlue)
                }
                TextButton(onClick = onDismiss) {
                    Text("Plus tard", fontSize = 11.sp, color = AppColors.TextSub)
                }
            }
        }
    }
}

@Composable
private fun ErrorBanner(message: String, onRetry: () -> Unit) {
    Surface(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
        color = AppColors.StatusRed.copy(0.1f),
        shape = AppShapes.MediumRadius,
        border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.StatusRed.copy(0.3f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Rounded.ErrorOutline, null, tint = AppColors.StatusRed)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Erreur", fontWeight = FontWeight.Bold, color = AppColors.StatusRed, fontSize = 14.sp)
                Text(message, fontSize = 12.sp, color = AppColors.TextSub)
            }
            TextButton(onClick = onRetry) {
                Text("Réessayer", fontWeight = FontWeight.Black, color = AppColors.StatusRed)
            }
        }
    }
}

@Composable
private fun StatsGrid(
    totalAdherents: Int,
    activeAdherents: Int,
    pendingSync: Int
) {
    Row(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SummaryCard("Total", "$totalAdherents", Icons.Rounded.Group, AppColors.ActionBlue, Modifier.weight(1f))
        SummaryCard("Actifs", "$activeAdherents", Icons.Rounded.CheckCircle, AppColors.StatusGreen, Modifier.weight(1f))
        SummaryCard("En attente", "$pendingSync", Icons.Rounded.CloudUpload, AppColors.StatusOrange, Modifier.weight(1f))
    }
}

@Composable
private fun SummaryCard(label: String, value: String, icon: ImageVector, color: Color, modifier: Modifier) {
    Surface(
        modifier = modifier,
        shape = AppShapes.MediumRadius,
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(icon, null, tint = color, modifier = Modifier.size(24.dp))
            Spacer(Modifier.height(12.dp))
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
            Text(label, style = MaterialTheme.typography.labelSmall, color = AppColors.TextSub)
        }
    }
}

@Composable
private fun AdherentRow(adherent: AdherentDto) {
    val name = "${adherent.prenoms ?: ""} ${adherent.nom ?: ""}".trim()
    val initials = remember(name) {
        val parts = name.split(" ")
        if (parts.size >= 2) "${parts[0].first()}${parts[1].first()}".uppercase()
        else name.take(2).uppercase()
    }

    Surface(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .fillMaxWidth()
            .clickable { /* Navigate to details */ },
        shape = AppShapes.MediumRadius,
        color = Color.White
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(44.dp),
                shape = CircleShape,
                color = AppColors.BrandBlue.copy(0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        initials,
                        color = AppColors.BrandBlue,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    name.ifEmpty { "Sans nom" },
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    adherent.whatsapp ?: adherent.commune ?: "",
                    color = AppColors.TextSub,
                    fontSize = 12.sp,
                    maxLines = 1
                )
            }

            // Status badge
            val isActive = adherent.actif == true
            Surface(
                color = if (isActive) AppColors.StatusGreen.copy(0.1f) else AppColors.StatusRed.copy(0.1f),
                shape = AppShapes.CircleRadius
            ) {
                Text(
                    if (isActive) "Actif" else "Inactif",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    color = if (isActive) AppColors.StatusGreen else AppColors.StatusRed,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier.size(80.dp),
            shape = CircleShape,
            color = AppColors.SurfaceAlt
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Rounded.PersonAdd,
                    null,
                    tint = AppColors.TextSub,
                    modifier = Modifier.size(36.dp)
                )
            }
        }
        Spacer(Modifier.height(16.dp))
        Text(
            "Aucun adhérent",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = AppColors.TextMain
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "Commencez par ajouter votre premier adhérent",
            style = MaterialTheme.typography.bodySmall,
            color = AppColors.TextSub
        )
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        title,
        modifier = Modifier.padding(16.dp),
        fontWeight = FontWeight.Black,
        color = AppColors.TextMain,
        fontSize = 16.sp
    )
}