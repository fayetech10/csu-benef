package com.example.sencsu.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.CloudUpload
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Group
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.example.sencsu.theme.AppColors
import com.example.sencsu.theme.AppElevation
import com.example.sencsu.theme.AppGradients
import com.example.sencsu.theme.AppShapes
import com.example.sencsu.navigation.Screen
import androidx.compose.material.icons.rounded.QrCode

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
    val activeCount = adherents.count { it.actif == true }

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
            item {
                DashboardHeader(
                    initials = initials,
                    fullName = fullName,
                    adherentsCount = adherents.size,
                    activeCount = activeCount,
                    rootNavController = rootNavController,
                    onNotificationsClick = { }
                )
            }

            if (showSyncPrompt) {
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
                        message = dashboardState.error ?: "Erreur inconnue",
                        onRetry = { viewModel.refresh() }
                    )
                }
            }

            item {
                StatsGrid(
                    totalAdherents = adherents.size,
                    activeAdherents = activeCount,
                    pendingSync = pendingCount
                )
            }

            if (adherents.isNotEmpty()) {
                item {
                    SectionTitle(
                        title = "Adherents recents",
                        subtitle = "Les derniers dossiers suivis dans votre espace terrain"
                    )
                }

                items(adherents.take(5)) { adherent ->
                    AdherentRow(adherent = adherent)
                }

                if (adherents.size > 5) {
                    item {
                        TextButton(
                            onClick = { },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                        ) {
                            Text(
                                "Voir les ${adherents.size} adherents",
                                fontWeight = FontWeight.Bold,
                                color = AppColors.BrandBlue
                            )
                        }
                    }
                }
            }

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
    activeCount: Int,
    rootNavController: NavController,
    onNotificationsClick: () -> Unit
) {
    val coverageRate = if (adherentsCount == 0) 0 else (activeCount * 100) / adherentsCount

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    AppGradients.Brand
                )
            )
            .padding(horizontal = 20.dp, vertical = 24.dp)
            .statusBarsPadding()
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(22.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(52.dp),
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.16f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            initials,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Bonjour,", color = Color.White.copy(alpha = 0.72f), fontSize = 13.sp)
                    Text(
                        fullName,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                }
                IconButton(onClick = { rootNavController.navigate(Screen.QRScanner.route) }) {
                    Icon(Icons.Rounded.QrCode, contentDescription = "Scanner QR", tint = Color.White)
                }
                IconButton(onClick = onNotificationsClick) {
                    Icon(Icons.Rounded.Notifications, contentDescription = null, tint = Color.White)
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = AppShapes.LargeRadius,
                color = Color.White.copy(alpha = 0.1f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        "Vue d'ensemble",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.76f)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        HeaderMetricCard("Dossiers", adherentsCount.toString(), Modifier.weight(1f))
                        HeaderMetricCard("Actifs", activeCount.toString(), Modifier.weight(1f))
                        HeaderMetricCard("Couverture", "$coverageRate%", Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun HeaderMetricCard(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = AppShapes.MediumRadius,
        color = Color.White.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.14f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.72f)
            )
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
        color = AppColors.ActionBlue.copy(alpha = 0.08f),
        shape = AppShapes.LargeRadius,
        border = BorderStroke(1.dp, AppColors.ActionBlue.copy(alpha = 0.18f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(AppColors.ActionBlue.copy(alpha = 0.12f), CircleShape)
                    .border(1.dp, AppColors.ActionBlue.copy(alpha = 0.08f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.CloudUpload, contentDescription = null, tint = AppColors.ActionBlue)
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Donnees en attente",
                    fontWeight = FontWeight.Bold,
                    color = AppColors.ActionBlue,
                    fontSize = 14.sp
                )
                Text(
                    "$pendingCount enregistrement(s) a synchroniser",
                    fontSize = 12.sp,
                    color = AppColors.TextSub
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                TextButton(onClick = onSync) {
                    Text("Synchroniser", fontWeight = FontWeight.Black, color = AppColors.ActionBlue)
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
        color = AppColors.StatusRed.copy(alpha = 0.08f),
        shape = AppShapes.LargeRadius,
        border = BorderStroke(1.dp, AppColors.StatusRed.copy(alpha = 0.18f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Rounded.ErrorOutline, contentDescription = null, tint = AppColors.StatusRed)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Erreur", fontWeight = FontWeight.Bold, color = AppColors.StatusRed, fontSize = 14.sp)
                Text(message, fontSize = 12.sp, color = AppColors.TextSub)
            }
            TextButton(onClick = onRetry) {
                Text("Reessayer", fontWeight = FontWeight.Black, color = AppColors.StatusRed)
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
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SummaryCard("Total", "$totalAdherents", Icons.Rounded.Group, AppColors.ActionBlue, Modifier.weight(1f))
        SummaryCard("Actifs", "$activeAdherents", Icons.Rounded.CheckCircle, AppColors.StatusGreen, Modifier.weight(1f))
        SummaryCard("En attente", "$pendingSync", Icons.Rounded.CloudUpload, AppColors.StatusOrange, Modifier.weight(1f))
    }
}

@Composable
private fun SummaryCard(
    label: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier
) {
    Surface(
        modifier = modifier,
        shape = AppShapes.LargeRadius,
        color = AppColors.SurfaceBackground,
        tonalElevation = AppElevation.card,
        shadowElevation = AppElevation.card,
        border = BorderStroke(1.dp, AppColors.BorderColorLight)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(color.copy(alpha = 0.12f), CircleShape)
                    .border(1.dp, color.copy(alpha = 0.08f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            }
            Text(
                value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = AppColors.TextMain
            )
            Text(label, style = MaterialTheme.typography.labelSmall, color = AppColors.TextSub)
        }
    }
}

@Composable
private fun AdherentRow(adherent: AdherentDto) {
    val name = "${adherent.prenoms ?: ""} ${adherent.nom ?: ""}".trim()
    val initials = remember(name) {
        val parts = name.split(" ").filter { it.isNotBlank() }
        when {
            parts.size >= 2 -> "${parts[0].first()}${parts[1].first()}".uppercase()
            name.isNotBlank() -> name.take(2).uppercase()
            else -> "NA"
        }
    }
    val isActive = adherent.actif == true

    Surface(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 5.dp)
            .fillMaxWidth()
            .clickable { },
        shape = AppShapes.LargeRadius,
        color = AppColors.SurfaceBackground,
        tonalElevation = AppElevation.card,
        shadowElevation = AppElevation.card,
        border = BorderStroke(1.dp, AppColors.BorderColorLight)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(46.dp),
                shape = CircleShape,
                color = AppColors.BrandBlue.copy(alpha = 0.1f)
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
            Surface(
                color = if (isActive) AppColors.StatusGreen.copy(alpha = 0.1f) else AppColors.StatusRed.copy(alpha = 0.1f),
                shape = AppShapes.CircleRadius
            ) {
                Text(
                    if (isActive) "Actif" else "Inactif",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
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
                    contentDescription = null,
                    tint = AppColors.TextSub,
                    modifier = Modifier.size(36.dp)
                )
            }
        }
        Spacer(Modifier.height(16.dp))
        Text(
            "Aucun adherent",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = AppColors.TextMain
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "Commencez par ajouter votre premier adherent",
            style = MaterialTheme.typography.bodySmall,
            color = AppColors.TextSub,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun SectionTitle(title: String, subtitle: String) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
        Text(
            title,
            fontWeight = FontWeight.Black,
            color = AppColors.TextMain,
            fontSize = 16.sp
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            subtitle,
            color = AppColors.TextSub,
            style = MaterialTheme.typography.bodySmall
        )
    }
}
