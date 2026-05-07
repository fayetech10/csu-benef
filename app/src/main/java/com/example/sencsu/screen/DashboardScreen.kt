package com.example.sencsu.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CloudUpload
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Group
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material.icons.rounded.QrCode
import androidx.compose.material.icons.rounded.QrCodeScanner
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.sencsu.components.design.ActionTile
import com.example.sencsu.components.design.EmptyStateBlock
import com.example.sencsu.components.design.HeaderAvatar
import com.example.sencsu.components.design.HeaderIconButton
import com.example.sencsu.components.design.InfoBanner
import com.example.sencsu.components.design.MetricCard
import com.example.sencsu.components.design.RowArrow
import com.example.sencsu.components.design.SectionHeader
import com.example.sencsu.components.design.SenCsuHeader
import com.example.sencsu.components.design.StatusPill
import com.example.sencsu.data.remote.dto.AdherentDto
import com.example.sencsu.domain.viewmodel.DashboardViewModel
import com.example.sencsu.navigation.Screen
import com.example.sencsu.theme.AppColors
import com.example.sencsu.theme.AppShapes

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
    val activeCount = adherents.count { it.actif == true }
    val inactiveCount = adherents.size - activeCount
    val isLoading = dashboardState.isLoading

    var showRows by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { showRows = true }

    val initials = remember(user) {
        val first = user?.prenom?.firstOrNull()?.uppercase() ?: ""
        val last = user?.name?.firstOrNull()?.uppercase() ?: ""
        "$first$last".ifEmpty { "CS" }
    }
    val fullName = remember(user) {
        "${user?.prenom.orEmpty()} ${user?.name.orEmpty()}".trim().ifEmpty { "Agent CSU" }
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { rootNavController.navigate(Screen.AddMember.route) },
                containerColor = AppColors.BrandBlue,
                contentColor = Color.White,
                shape = AppShapes.CircleRadius,
                icon = { Icon(Icons.Rounded.PersonAdd, contentDescription = null) },
                text = { Text("Nouvel adhérent", fontWeight = FontWeight.Bold) }
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
                item {
                    SenCsuHeader(
                        title = "Bonjour, $fullName",
                        subtitle = "Vue terrain des enrôlements, contrôles et synchronisations.",
                        leading = { HeaderAvatar(initials) },
                        trailing = {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                HeaderIconButton(
                                    icon = Icons.Rounded.QrCodeScanner,
                                    contentDescription = "Scanner",
                                    onClick = { rootNavController.navigate(Screen.QRScanner.route) }
                                )
                                HeaderIconButton(
                                    icon = Icons.Rounded.Notifications,
                                    contentDescription = "Notifications",
                                    onClick = { rootNavController.navigate(Screen.Notifications.route) }
                                )
                            }
                        }
                    )
                }

                item {
                    QuickActions(
                        onAdd = { rootNavController.navigate(Screen.AddMember.route) },
                        onScan = { rootNavController.navigate(Screen.QRScanner.route) },
                        onSync = { viewModel.syncData() },
                        pendingCount = pendingCount
                    )
                }

                if (showSyncPrompt && pendingCount > 0) {
                    item {
                        InfoBanner(
                            title = "Synchronisation",
                            message = "$pendingCount dossier(s) en attente d'envoi.",
                            icon = Icons.Rounded.CloudUpload,
                            color = AppColors.ActionBlue,
                            actionLabel = "Sync",
                            onAction = { viewModel.syncData() },
                            trailing = {
                                IconButton(onClick = { viewModel.dismissSyncPrompt() }) {
                                    Icon(Icons.Rounded.Close, null, modifier = Modifier.size(16.dp), tint = AppColors.TextSub)
                                }
                            }
                        )
                    }
                }

                if (dashboardState.error != null && !isLoading) {
                    item {
                        InfoBanner(
                            title = "Connexion",
                            message = dashboardState.error ?: "Erreur de connexion",
                            icon = Icons.Rounded.ErrorOutline,
                            color = AppColors.StatusRed,
                            actionLabel = "Réessayer",
                            onAction = { viewModel.refresh() }
                        )
                    }
                }

                item {
                    SectionHeader(
                        title = "Performance terrain",
                        subtitle = "Aperçu de vos activités d'enrôlement"
                    )
                }

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MetricCard("Dossiers", adherents.size.toString(), Icons.Rounded.Group, AppColors.BrandBlue, Modifier.weight(1f))
                        MetricCard("Actifs", activeCount.toString(), Icons.Rounded.Person, AppColors.StatusGreen, Modifier.weight(1f))
                        MetricCard("A traiter", (pendingCount + inactiveCount).toString(), Icons.Rounded.Sync, AppColors.StatusOrange, Modifier.weight(1f))
                    }
                }

                if (adherents.isNotEmpty()) {
                    item {
                        SectionHeader(
                            title = "Adhérents récents",
                            subtitle = "Derniers dossiers enregistrés",
                            actionLabel = if (adherents.size > 5) "Voir tout" else null,
                            onAction = if (adherents.size > 5) onNavigateToAdherents else null
                        )
                    }

                    items(adherents.take(5), key = { it.id ?: it.hashCode() }) { adherent ->
                        AnimatedVisibility(
                            visible = showRows,
                            enter = fadeIn(tween(350)) + slideInVertically(tween(350)) { 24 }
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
                        EmptyStateBlock(
                            title = "Aucun adhérent trouvé",
                            subtitle = "Les nouveaux enrôlements apparaîtront ici après validation.",
                            icon = Icons.Rounded.Search
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickActions(
    onAdd: () -> Unit,
    onScan: () -> Unit,
    onSync: () -> Unit,
    pendingCount: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        ActionTile("Enrôler", Icons.Rounded.PersonAdd, AppColors.BrandBlue, onAdd, Modifier.weight(1f))
        ActionTile("Vérifier", Icons.Rounded.QrCode, AppColors.GoldAccent, onScan, Modifier.weight(1f))
        ActionTile(
            label = "Synchroniser",
            icon = Icons.Rounded.Sync,
            color = if (pendingCount > 0) AppColors.ActionBlue else AppColors.StatusGrey,
            onClick = onSync,
            modifier = Modifier.weight(1f),
            badge = pendingCount.takeIf { it > 0 }?.toString()
        )
    }
}

@Composable
private fun DashboardAdherentRow(adherent: AdherentDto, onClick: () -> Unit) {
    val name = "${adherent.prenoms.orEmpty()} ${adherent.nom.orEmpty()}".trim().ifEmpty { "Sans nom" }
    val isActive = adherent.actif == true

    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp),
        shape = AppShapes.MediumRadius,
        color = AppColors.SurfaceBackground,
        border = BorderStroke(1.dp, AppColors.BorderColorLight),
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(modifier = Modifier.size(44.dp), shape = CircleShape, color = AppColors.SurfaceAlt) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Rounded.Person, null, tint = AppColors.TextSub, modifier = Modifier.size(22.dp))
                }
            }

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.TextMain,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = adherent.matricule ?: "Matricule non renseigné",
                    style = MaterialTheme.typography.labelSmall,
                    color = AppColors.TextDisabled,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                StatusPill(
                    text = if (isActive) "Actif" else "Inactif",
                    color = if (isActive) AppColors.StatusGreen else AppColors.StatusRed
                )
                RowArrow()
            }
        }
    }
}
