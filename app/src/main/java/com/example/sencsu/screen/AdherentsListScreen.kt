package com.example.sencsu.screen

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
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
import com.example.sencsu.navigation.Screen
import com.example.sencsu.theme.*

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
        if (query.isBlank()) adherents
        else adherents.filter {
            listOfNotNull(it.matricule, it.nom, it.prenoms, it.whatsapp)
                .any { v -> v.contains(query, ignoreCase = true) }
        }
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
            modifier = Modifier.fillMaxSize().padding(scaffoldPadding)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 120.dp)
            ) {

                // ── Hero ──
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                            .background(
                                Brush.verticalGradient(
                                    listOf(AppColors.BrandBlueDark, AppColors.BrandBlue, Color(0xFF0A9070))
                                )
                            )
                            .statusBarsPadding()
                    ) {
                        // Decorative rings
                        Box(Modifier.size(220.dp).align(Alignment.TopEnd).offset(x = 70.dp, y = (-70).dp)
                            .clip(CircleShape).background(Color.White.copy(alpha = 0.04f)))
                        Box(Modifier.size(110.dp).align(Alignment.BottomStart).offset(x = (-30).dp, y = 30.dp)
                            .clip(CircleShape).background(Color.White.copy(alpha = 0.06f)))

                        Column(
                            modifier = Modifier.align(Alignment.BottomStart).padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // Role chip
                            Surface(
                                shape = AppShapes.CircleRadius,
                                color = Color.White.copy(alpha = 0.15f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Box(Modifier.size(6.dp).clip(CircleShape).background(AppColors.GoldAccent))
                                    Text(
                                        "AGENT CSU",
                                        color = Color.White,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = 1.5.sp
                                    )
                                }
                            }
                            Text(
                                "Mes adhérents",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                            authState.user?.let {
                                Text(
                                    "${it.prenom ?: ""} ${it.name ?: ""}".trim().ifEmpty { "Agent" },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.68f)
                                )
                            }
                        }

                        // Count badge top-right
                        AnimatedVisibility(
                            visible = visible && adherents.isNotEmpty(),
                            modifier = Modifier.align(Alignment.TopEnd).padding(16.dp).statusBarsPadding(),
                            enter = fadeIn() + scaleIn()
                        ) {
                            Surface(
                                shape = AppShapes.LargeRadius,
                                color = Color.White.copy(alpha = 0.18f),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.25f))
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        "${adherents.size}",
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White
                                    )
                                    Text(
                                        "total",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    }
                }

                // ── Stats Row ──
                item {
                    AnimatedVisibility(
                        visible = visible,
                        enter = fadeIn(tween(400)) + slideInVertically(tween(400)) { -30 }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .offset(y = (-20).dp)
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            val active = adherents.count { it.actif == true }
                            MiniStatCard("Actifs", active.toString(), Icons.Rounded.CheckCircle, AppColors.StatusGreen, Modifier.weight(1f))
                            MiniStatCard("Inactifs", (adherents.size - active).toString(), Icons.Rounded.Pause, AppColors.StatusOrange, Modifier.weight(1f))
                            MiniStatCard("Ce mois", adherents.takeLast(5).size.toString(), Icons.Rounded.TrendingUp, AppColors.ActionBlue, Modifier.weight(1f))
                        }
                    }
                }

                // ── Search ──
                item {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 8.dp),
                        placeholder = { Text("Rechercher un adhérent...") },
                        leadingIcon = { Icon(Icons.Rounded.Search, null, tint = AppColors.TextSub) },
                        trailingIcon = {
                            if (query.isNotEmpty()) {
                                IconButton(onClick = { query = "" }) {
                                    Icon(Icons.Rounded.Close, null, tint = AppColors.TextSub)
                                }
                            }
                        },
                        shape = AppShapes.LargeRadius,
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = AppColors.SurfaceBackground,
                            unfocusedContainerColor = AppColors.SurfaceBackground,
                            unfocusedBorderColor = AppColors.BorderColorLight,
                            focusedBorderColor = AppColors.BrandBlue
                        )
                    )
                }

                // ── Section title ──
                if (filtered.isNotEmpty()) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                if (query.isEmpty()) "Liste complète" else "${filtered.size} résultat(s)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Black,
                                color = AppColors.TextMain,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                "Tap pour les détails",
                                style = MaterialTheme.typography.labelSmall,
                                color = AppColors.TextDisabled
                            )
                        }
                    }
                }

                // ── Empty ──
                if (!state.isLoading && filtered.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(top = 48.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = AppColors.BrandBlueLite,
                                modifier = Modifier.size(72.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Rounded.Group, null, tint = AppColors.BrandBlue, modifier = Modifier.size(32.dp))
                                }
                            }
                            Text(
                                if (query.isEmpty()) "Aucun adhérent" else "Aucun résultat",
                                fontWeight = FontWeight.Bold,
                                color = AppColors.TextMain,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                if (query.isEmpty()) "Appuyez sur le bouton + pour ajouter" else "Essayez un autre terme",
                                color = AppColors.TextSub,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                // ── Error ──
                if (state.error != null && adherents.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                            shape = AppShapes.LargeRadius,
                            colors = CardDefaults.cardColors(containerColor = AppColors.StatusRedSoft),
                            border = BorderStroke(1.dp, AppColors.StatusRed.copy(alpha = 0.2f))
                        ) {
                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.ErrorOutline, null, tint = AppColors.StatusRed)
                                Spacer(Modifier.width(10.dp))
                                Text(state.error ?: "", color = AppColors.StatusRed, modifier = Modifier.weight(1f))
                                TextButton(onClick = { viewModel.refresh() }) { Text("Réessayer") }
                            }
                        }
                    }
                }

                // ── Cards ──
                itemsIndexed(filtered, key = { _, a -> a.id ?: a.hashCode() }) { i, adherent ->
                    AnimatedVisibility(
                        visible = visible,
                        enter = fadeIn(tween(280, delayMillis = (i * 50).coerceAtMost(500))) +
                                slideInVertically(tween(280, delayMillis = (i * 50).coerceAtMost(500))) { 50 }
                    ) {
                        PremiumAdherentCard(
                            adherent = adherent,
                            index = i,
                            onClick = { adherent.id?.let { rootNavController.navigate(Screen.AdherentDetails.createRoute(it)) } }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MiniStatCard(label: String, value: String, icon: ImageVector, color: Color, modifier: Modifier) {
    Surface(
        modifier = modifier,
        shape = AppShapes.LargeRadius,
        color = AppColors.SurfaceBackground,
        border = BorderStroke(1.dp, AppColors.BorderColorLight),
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(shape = AppShapes.SmallRadius, color = color.copy(alpha = 0.12f)) {
                Icon(icon, null, tint = color, modifier = Modifier.padding(5.dp).size(15.dp))
            }
            Column {
                Text(value, fontWeight = FontWeight.Black, fontSize = 16.sp, color = AppColors.TextMain)
                Text(label, style = MaterialTheme.typography.labelSmall, color = AppColors.TextSub)
            }
        }
    }
}

// Palette de couleurs d'avatar basée sur l'index
private val avatarPalettes = listOf(
    Pair(Color(0xFF08745F), Color(0xFFE7F5F1)), // Vert brand
    Pair(Color(0xFF2563EB), Color(0xFFEFF6FF)), // Bleu
    Pair(Color(0xFFD97706), Color(0xFFFFF4DE)), // Orange
    Pair(Color(0xFF7C3AED), Color(0xFFF5F3FF)), // Violet
    Pair(Color(0xFFDB2777), Color(0xFFFDF2F8)), // Rose
)

@Composable
private fun PremiumAdherentCard(adherent: AdherentDto, index: Int, onClick: () -> Unit) {
    val name = "${adherent.prenoms ?: ""} ${adherent.nom ?: ""}".trim()
    val initials = name.split(" ").filter { it.isNotBlank() }.take(2)
        .mapNotNull { it.firstOrNull()?.uppercase() }.joinToString("").ifEmpty { "?" }
    val isActive = adherent.actif == true
    val (avatarColor, avatarBg) = avatarPalettes[index % avatarPalettes.size]

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 5.dp),
        shape = AppShapes.LargeRadius,
        colors = CardDefaults.cardColors(containerColor = AppColors.SurfaceBackground),
        border = BorderStroke(1.dp, AppColors.BorderColorLight),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Avatar coloré
            Surface(modifier = Modifier.size(50.dp), shape = CircleShape, color = avatarBg) {
                Box(contentAlignment = Alignment.Center) {
                    Text(initials, color = avatarColor, fontWeight = FontWeight.Black, fontSize = 16.sp)
                }
            }

            // Infos principales
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    name.ifEmpty { "Sans nom" },
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = AppColors.TextMain,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                // Chips info
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (!adherent.matricule.isNullOrBlank()) {
                        InfoChip(adherent.matricule, AppColors.BrandBlue, AppColors.BrandBlueLite)
                    }
                    if (!adherent.regime.isNullOrBlank()) {
                        InfoChip(adherent.regime, AppColors.TextSub, AppColors.SurfaceAlt)
                    }
                }
                if (!adherent.whatsapp.isNullOrBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                        Icon(Icons.Rounded.Phone, null, tint = AppColors.TextDisabled, modifier = Modifier.size(11.dp))
                        Text(adherent.whatsapp, style = MaterialTheme.typography.labelSmall, color = AppColors.TextDisabled, maxLines = 1)
                    }
                }
            }

            // Status + Arrow
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(
                    shape = AppShapes.CircleRadius,
                    color = if (isActive) AppColors.StatusGreenSoft else AppColors.StatusRedSoft
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(Modifier.size(5.dp).clip(CircleShape)
                            .background(if (isActive) AppColors.StatusGreen else AppColors.StatusRed))
                        Text(
                            if (isActive) "Actif" else "Inactif",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isActive) AppColors.StatusGreen else AppColors.StatusRed
                        )
                    }
                }
                Icon(Icons.AutoMirrored.Rounded.ArrowForward, null, tint = AppColors.TextDisabled, modifier = Modifier.size(15.dp))
            }
        }
    }
}

@Composable
private fun InfoChip(text: String, textColor: Color, bgColor: Color) {
    Surface(shape = AppShapes.SmallRadius, color = bgColor) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = textColor,
            maxLines = 1
        )
    }
}
