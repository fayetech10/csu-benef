package com.example.sencsu.screen

import android.annotation.SuppressLint
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.sencsu.components.ServerImage
import com.example.sencsu.data.remote.dto.AdherentDto
import com.example.sencsu.data.repository.SessionManager
import com.example.sencsu.domain.viewmodel.ListeAdherentViewModel
import com.example.sencsu.theme.AppColors
import com.example.sencsu.theme.AppShapes
import com.example.sencsu.theme.withAlpha
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.ExperimentalFoundationApi

@SuppressLint("SimpleDateFormat")
private fun formatDateHeader(raw: String?): String {
    if (raw.isNullOrBlank()) return "RÉCENTS"
    return try {
        val input = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.FRANCE).also { it.isLenient = true }
        val output = SimpleDateFormat("d MMMM yyyy", Locale.FRANCE)
        val date = input.parse(raw) ?: SimpleDateFormat("yyyy-MM-dd", Locale.FRANCE).parse(raw)
            ?: return "RÉCENTS"
        output.format(date).uppercase(Locale.FRANCE)
    } catch (e: Exception) {
        "RÉCENTS"
    }
}

private data class DateGroup(val label: String, val adherents: List<AdherentDto>)

private fun groupByDate(adherents: List<AdherentDto>): List<DateGroup> {
    return adherents
        .groupBy { formatDateHeader(it.createdAt) }
        .map { (label, list) -> DateGroup(label, list) }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListeAdherentScreen(
    viewModel: ListeAdherentViewModel = hiltViewModel(),
    sessionManager: SessionManager,
    onAdherentClick: (String) -> Unit,
    onBack: () -> Unit = {},
    onAddClick: () -> Unit = {},
) {
    val state by viewModel.state.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("Tous") }

    val filtered = remember(state.adherents, searchQuery, selectedFilter) {
        state.adherents.filter {
            val matchesSearch = if (searchQuery.isBlank()) true
            else {
                val full = "${it.prenoms} ${it.nom} ${it.matricule}".lowercase()
                searchQuery.lowercase().split(" ").all { token -> full.contains(token) }
            }
            
            val matchesFilter = when (selectedFilter) {
                "Actifs" -> true // Logique à adapter si un champ status existe
                "Nouveaux" -> true // Logique date
                else -> true
            }
            matchesSearch && matchesFilter
        }
    }

    val groups = remember(filtered) { groupByDate(filtered) }
    val listState = rememberLazyListState()
    val isScrolled by remember { derivedStateOf { listState.firstVisibleItemScrollOffset > 0 } }

    Scaffold(
        containerColor = AppColors.AppBackground,
        topBar = {
            PremiumTopBar(
                count = filtered.size,
                isScrolled = isScrolled,
                searchQuery = searchQuery,
                onSearchChange = { searchQuery = it },
                onBack = onBack
            )
        },
        floatingActionButton = {
            LargeFloatingActionButton(
                onClick = onAddClick,
                containerColor = AppColors.BrandBlue,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Rounded.Add, "Ajouter", modifier = Modifier.size(32.dp))
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            
            // Filter Chips
            FilterSection(
                selectedFilter = selectedFilter,
                onFilterSelected = { selectedFilter = it }
            )

            // Content
            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    state.isLoading -> LoadingState()
                    state.error != null -> ErrorState(state.error!!) { viewModel.refresh() }
                    groups.isEmpty() -> EmptyState(searchQuery)
                    else -> AdherentLazyList(
                        groups = groups,
                        sessionManager = sessionManager,
                        onAdherentClick = onAdherentClick,
                        listState = listState
                    )
                }
            }
        }
    }
}

@Composable
private fun PremiumTopBar(
    count: Int,
    isScrolled: Boolean,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onBack: () -> Unit
) {
    Surface(
        color = if (isScrolled) Color.White.withAlpha(0.95f) else AppColors.AppBackground,
        shadowElevation = if (isScrolled) 4.dp else 0.dp,
        modifier = Modifier.statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Adhérents",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black,
                        color = AppColors.TextMain
                    )
                    Text(
                        "$count inscrits au total",
                        style = MaterialTheme.typography.labelMedium,
                        color = AppColors.TextSub
                    )
                }
                
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(AppColors.SurfaceAlt)
                ) {
                    Icon(Icons.Rounded.Notifications, null, tint = AppColors.BrandBlue)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            // Modern Search Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(AppShapes.MediumRadius)
                    .background(AppColors.SurfaceBackground)
                    .border(1.dp, AppColors.BorderColorLight, AppShapes.MediumRadius),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Icon(Icons.Rounded.Search, null, tint = AppColors.BrandBlue, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(12.dp))
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = onSearchChange,
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyLarge.copy(color = AppColors.TextMain),
                        decorationBox = { inner ->
                            Box(contentAlignment = Alignment.CenterStart) {
                                if (searchQuery.isEmpty()) {
                                    Text("Rechercher un nom ou matricule...", color = AppColors.TextDisabled)
                                }
                                inner()
                            }
                        }
                    )
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchChange("") }) {
                            Icon(Icons.Rounded.Cancel, null, tint = AppColors.TextDisabled)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterSection(selectedFilter: String, onFilterSelected: (String) -> Unit) {
    val filters = listOf("Tous", "Actifs", "Nouveaux", "En attente")
    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(filters) { filter ->
            Surface(
                onClick = { onFilterSelected(filter) },
                shape = AppShapes.SmallRadius,
                color = if (selectedFilter == filter) AppColors.BrandBlue else AppColors.SurfaceBackground,
                contentColor = if (selectedFilter == filter) Color.White else AppColors.TextSub,
                border = BorderStroke(1.dp, if (selectedFilter == filter) Color.Transparent else AppColors.BorderColorLight)
            ) {
                Text(
                    filter,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AdherentLazyList(
    groups: List<DateGroup>,
    sessionManager: SessionManager,
    onAdherentClick: (String) -> Unit,
    listState: androidx.compose.foundation.lazy.LazyListState
) {
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        groups.forEach { group ->
            stickyHeader {
                SectionHeader(group.label)
            }
            items(group.adherents, key = { it.id ?: it.hashCode() }) { adherent ->
                AdherentSaaSRow(
                    adherent = adherent,
                    sessionManager = sessionManager,
                    onClick = { onAdherentClick(adherent.id!!) }
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(label: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppColors.AppBackground.withAlpha(0.9f))
            .padding(horizontal = 24.dp, vertical = 8.dp)
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = AppColors.TextSub,
            letterSpacing = 1.sp
        )
    }
}

@Composable
private fun AdherentSaaSRow(
    adherent: AdherentDto,
    sessionManager: SessionManager,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = AppShapes.MediumRadius,
        colors = CardDefaults.cardColors(containerColor = AppColors.SurfaceBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar with Presence Indicator
            Box(contentAlignment = Alignment.BottomEnd) {
                if (!adherent.photo.isNullOrBlank()) {
                    ServerImage(
                        filename = adherent.photo,
                        sessionManager = sessionManager,
                        modifier = Modifier.size(52.dp).clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier.size(52.dp).clip(CircleShape).background(AppColors.BrandBlueLite),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            adherent.nom?.take(1)?.uppercase() ?: "?",
                            fontWeight = FontWeight.Bold,
                            color = AppColors.BrandBlue,
                            fontSize = 18.sp
                        )
                    }
                }
                
                // Status indicator
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .padding(2.dp)
                        .clip(CircleShape)
                        .background(AppColors.StatusGreen)
                )
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "${adherent.prenoms} ${adherent.nom}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.TextMain,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    adherent.matricule ?: "N° MATRICULE INCONNU",
                    style = MaterialTheme.typography.labelSmall,
                    color = AppColors.BrandBlue,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.LocationOn, null, tint = AppColors.TextDisabled, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "${adherent.commune ?: "Localité"}, ${adherent.departement ?: "Région"}",
                        style = MaterialTheme.typography.labelSmall,
                        color = AppColors.TextSub
                    )
                }
            }

            // Dependents Badge
            Surface(
                color = AppColors.SurfaceAlt,
                shape = CircleShape,
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Rounded.Group, null, tint = AppColors.BrandBlue, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "${adherent.personnesCharge.size}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.BrandBlue
                    )
                }
            }
        }
    }
}

@Composable
private fun LoadingState() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = AppColors.BrandBlue)
    }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Rounded.CloudOff, null, tint = AppColors.StatusRed, modifier = Modifier.size(64.dp))
        Spacer(Modifier.height(16.dp))
        Text("Erreur de connexion", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(message, textAlign = TextAlign.Center, color = AppColors.TextSub, modifier = Modifier.padding(top = 8.dp))
        Spacer(Modifier.height(24.dp))
        Button(onClick = onRetry, colors = ButtonDefaults.buttonColors(containerColor = AppColors.BrandBlue)) {
            Text("Réessayer")
        }
    }
}

@Composable
private fun EmptyState(query: String) {
    Column(
        Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Rounded.PersonSearch, null, tint = AppColors.TextDisabled, modifier = Modifier.size(80.dp))
        Spacer(Modifier.height(16.dp))
        Text(
            if (query.isBlank()) "Aucun adhérent trouvé" else "Aucun résultat pour \"$query\"",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            color = AppColors.TextSub,
            textAlign = TextAlign.Center
        )
    }
}
