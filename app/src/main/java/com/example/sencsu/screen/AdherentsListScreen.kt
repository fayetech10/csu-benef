package com.example.sencsu.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Group
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material.icons.rounded.WorkHistory
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.sencsu.components.design.EmptyStateBlock
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
fun AdherentsListScreen(
    rootNavController: NavController,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val state by viewModel.dashboardState.collectAsState()
    val authState by viewModel.authState.collectAsState()
    val adherents = state.data?.data ?: emptyList()
    var query by remember { mutableStateOf("") }
    var visible by remember { mutableStateOf(false) }

    val filtered = remember(adherents, query) {
        val search = query.trim()
        if (search.isBlank()) {
            adherents
        } else {
            adherents.filter {
                listOfNotNull(it.matricule, it.nom, it.prenoms, it.whatsapp, it.regime)
                    .any { value -> value.contains(search, ignoreCase = true) }
            }
        }
    }

    val activeCount = adherents.count { it.actif == true }
    val inactiveCount = adherents.size - activeCount
    val userName = remember(authState.user) {
        authState.user?.let { "${it.prenom.orEmpty()} ${it.name.orEmpty()}".trim() }
            ?.ifEmpty { null }
            ?: "Agent terrain"
    }

    LaunchedEffect(Unit) { visible = true }

    Scaffold(
        containerColor = AppColors.AppBackground,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { rootNavController.navigate(Screen.AddMember.route) },
                containerColor = AppColors.BrandBlue,
                contentColor = Color.White,
                shape = AppShapes.CircleRadius,
                icon = { Icon(Icons.Rounded.PersonAdd, null) },
                text = { Text("Nouvel adhérent", fontWeight = FontWeight.Bold) }
            )
        }
    ) { scaffoldPadding ->
        PullToRefreshBox(
            isRefreshing = state.isLoading,
            onRefresh = { viewModel.refresh() },
            modifier = Modifier
                .fillMaxSize()
                .padding(scaffoldPadding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(AppColors.AppBackground),
                contentPadding = PaddingValues(bottom = 120.dp)
            ) {
                item {
                    SenCsuHeader(
                        title = "Mes adhérents",
                        subtitle = "$userName - ${adherents.size} dossier(s) en portefeuille"
                    )
                }

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MetricCard("Actifs", activeCount.toString(), Icons.Rounded.Verified, AppColors.StatusGreen, Modifier.weight(1f))
                        MetricCard("Inactifs", inactiveCount.toString(), Icons.Rounded.Pause, AppColors.StatusOrange, Modifier.weight(1f))
                        MetricCard("Dossiers", adherents.size.toString(), Icons.Rounded.WorkHistory, AppColors.ActionBlue, Modifier.weight(1f))
                    }
                }

                item {
                    SearchBox(
                        query = query,
                        onQueryChange = { query = it }
                    )
                }

                if (state.error != null && adherents.isEmpty()) {
                    item {
                        InfoBanner(
                            title = "Chargement impossible",
                            message = state.error ?: "Erreur de connexion",
                            icon = Icons.Rounded.ErrorOutline,
                            color = AppColors.StatusRed,
                            actionLabel = "Réessayer",
                            onAction = { viewModel.refresh() }
                        )
                    }
                }

                if (filtered.isNotEmpty()) {
                    item {
                        SectionHeader(
                            title = if (query.isBlank()) "Liste complète" else "${filtered.size} résultat(s)",
                            subtitle = "Touchez un dossier pour consulter les détails"
                        )
                    }
                }

                if (!state.isLoading && filtered.isEmpty()) {
                    item {
                        EmptyStateBlock(
                            title = if (query.isBlank()) "Aucun adhérent" else "Aucun résultat",
                            subtitle = if (query.isBlank()) {
                                "Ajoutez un premier dossier depuis le bouton d'action."
                            } else {
                                "Essayez un nom, un matricule ou un numéro différent."
                            },
                            icon = Icons.Rounded.Group
                        )
                    }
                }

                itemsIndexed(filtered, key = { _, item -> item.id ?: item.hashCode() }) { index, adherent ->
                    AnimatedVisibility(
                        visible = visible,
                        enter = fadeIn(tween(260, delayMillis = (index * 35).coerceAtMost(350))) +
                            slideInVertically(tween(260, delayMillis = (index * 35).coerceAtMost(350))) { 28 }
                    ) {
                        AdherentListCard(
                            adherent = adherent,
                            index = index,
                            onClick = {
                                adherent.id?.let {
                                    rootNavController.navigate(Screen.AdherentDetails.createRoute(it))
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchBox(query: String, onQueryChange: (String) -> Unit) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        placeholder = { Text("Rechercher un adhérent...") },
        leadingIcon = { Icon(Icons.Rounded.Search, null, tint = AppColors.TextSub) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Rounded.Close, null, tint = AppColors.TextSub)
                }
            }
        },
        shape = AppShapes.MediumRadius,
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = AppColors.SurfaceBackground,
            unfocusedContainerColor = AppColors.SurfaceBackground,
            unfocusedBorderColor = AppColors.BorderColorLight,
            focusedBorderColor = AppColors.BrandBlue
        )
    )
}

private val avatarPalettes = listOf(
    Pair(Color(0xFF08745F), Color(0xFFE7F5F1)),
    Pair(Color(0xFF2563EB), Color(0xFFEFF6FF)),
    Pair(Color(0xFFD97706), Color(0xFFFFF4DE)),
    Pair(Color(0xFF7C3AED), Color(0xFFF5F3FF)),
    Pair(Color(0xFFDB2777), Color(0xFFFDF2F8)),
)

@Composable
private fun AdherentListCard(adherent: AdherentDto, index: Int, onClick: () -> Unit) {
    val name = "${adherent.prenoms.orEmpty()} ${adherent.nom.orEmpty()}".trim().ifEmpty { "Sans nom" }
    val initials = name.split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .mapNotNull { it.firstOrNull()?.uppercase() }
        .joinToString("")
        .ifEmpty { "?" }
    val isActive = adherent.actif == true
    val (avatarColor, avatarBg) = avatarPalettes[index % avatarPalettes.size]

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp),
        shape = AppShapes.MediumRadius,
        colors = CardDefaults.cardColors(containerColor = AppColors.SurfaceBackground),
        border = BorderStroke(1.dp, AppColors.BorderColorLight),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(modifier = Modifier.size(48.dp), shape = CircleShape, color = avatarBg) {
                Box(contentAlignment = Alignment.Center) {
                    Text(initials, color = avatarColor, fontWeight = FontWeight.Black)
                }
            }

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.TextMain,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (!adherent.matricule.isNullOrBlank()) {
                        SmallChip(adherent.matricule, AppColors.BrandBlue, AppColors.BrandBlueLite)
                    }
                    if (!adherent.regime.isNullOrBlank()) {
                        SmallChip(adherent.regime, AppColors.TextSub, AppColors.SurfaceAlt)
                    }
                }

                if (!adherent.whatsapp.isNullOrBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Rounded.Phone, null, tint = AppColors.TextDisabled, modifier = Modifier.size(12.dp))
                        Text(
                            text = adherent.whatsapp,
                            style = MaterialTheme.typography.labelSmall,
                            color = AppColors.TextDisabled,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
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

@Composable
private fun SmallChip(text: String, textColor: Color, bgColor: Color) {
    Surface(shape = AppShapes.ExtraSmallRadius, color = bgColor) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = textColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
