package com.example.sencsu.screen

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
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
import com.example.sencsu.data.remote.dto.ServiceMedicalDto
import com.example.sencsu.domain.viewmodel.MedicalHistoryViewModel
import com.example.sencsu.theme.AppColors
import com.example.sencsu.theme.AppShapes
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicalHistoryScreen(
    onBack: () -> Unit,
    viewModel: MedicalHistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showContent by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(100)
        showContent = true
    }

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(AppColors.BrandBlueDark, AppColors.BrandBlue)
                        )
                    )
            ) {
                TopAppBar(
                    title = { 
                        Column {
                            Text("Historique Médical", fontWeight = FontWeight.Black, fontSize = 18.sp)
                            if (uiState.isForDependent && !uiState.dependentName.isNullOrBlank()) {
                                Text(uiState.dependentName!!, fontSize = 12.sp, color = Color.White.copy(alpha = 0.8f))
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Retour")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White
                    ),
                    modifier = Modifier.statusBarsPadding()
                )
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(AppColors.AppBackground)
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // Stats header
            item {
                AnimatedVisibility(
                    visible = showContent,
                    enter = fadeIn(tween(400)) + slideInVertically(tween(400), initialOffsetY = { -20 })
                ) {
                    MedicalStatsHeader(uiState.services)
                }
            }

            // Filter chips
            item {
                AnimatedVisibility(
                    visible = showContent,
                    enter = fadeIn(tween(300, delayMillis = 150))
                ) {
                    ServiceFilterChips(
                        selectedFilter = uiState.selectedFilter,
                        onFilterSelected = { viewModel.filterByType(it) }
                    )
                }
            }

            // Service list
            if (uiState.isLoading) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = AppColors.BrandBlue)
                    }
                }
            }

            if (uiState.error != null) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        shape = AppShapes.MediumRadius,
                        color = AppColors.StatusRed.copy(alpha = 0.08f)
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.ErrorOutline, null, tint = AppColors.StatusRed)
                            Spacer(Modifier.width(12.dp))
                            Text(uiState.error!!, color = AppColors.StatusRed, fontSize = 14.sp)
                        }
                    }
                }
            }

            if (uiState.filteredServices.isEmpty() && !uiState.isLoading && uiState.error == null) {
                item {
                    EmptyMedicalState()
                }
            }

            itemsIndexed(uiState.filteredServices) { index, service ->
                AnimatedVisibility(
                    visible = showContent,
                    enter = fadeIn(tween(300, delayMillis = 200 + index * 60)) +
                            slideInVertically(tween(300, delayMillis = 200 + index * 60), initialOffsetY = { 40 })
                ) {
                    ServiceTimelineItem(
                        service = service,
                        isFirst = index == 0,
                        isLast = index == uiState.filteredServices.lastIndex
                    )
                }
            }
        }
    }
}

@Composable
private fun MedicalStatsHeader(services: List<ServiceMedicalDto>) {
    val totalServices = services.size
    val totalMontant = services.sumOf { it.montant ?: 0.0 }
    val totalAssurance = services.sumOf { it.partAssurance ?: 0.0 }
    val totalTicket = services.sumOf { it.ticketModerateur ?: 0.0 }
    val enCours = services.count { it.statut == "EN_COURS" }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = AppShapes.LargeRadius,
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Résumé",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = AppColors.TextMain
            )
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MiniStatCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Rounded.MedicalServices,
                    value = "$totalServices",
                    label = "Services",
                    color = AppColors.BrandBlue
                )
                MiniStatCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Rounded.Payments,
                    value = "${(totalMontant / 1000).toInt()}K",
                    label = "Total FCFA",
                    color = AppColors.GoldAccent
                )
                MiniStatCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Rounded.CheckCircle,
                    value = "${(totalAssurance / 1000).toInt()}K",
                    label = "Assurance",
                    color = AppColors.StatusGreen
                )
                MiniStatCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Rounded.Schedule,
                    value = "$enCours",
                    label = "En cours",
                    color = AppColors.StatusOrange
                )
            }
        }
    }
}

@Composable
private fun MiniStatCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    value: String,
    label: String,
    color: Color
) {
    Surface(
        modifier = modifier,
        shape = AppShapes.MediumRadius,
        color = color.copy(alpha = 0.08f)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(18.dp))
            Spacer(Modifier.height(4.dp))
            Text(value, fontWeight = FontWeight.Black, fontSize = 14.sp, color = AppColors.TextMain)
            Text(label, fontSize = 9.sp, color = AppColors.TextSub, maxLines = 1)
        }
    }
}

@Composable
private fun ServiceFilterChips(
    selectedFilter: String?,
    onFilterSelected: (String?) -> Unit
) {
    val filters = listOf(
        null to "Tous",
        "CONSULTATION" to "Consultation",
        "LABORATOIRE" to "Labo",
        "PHARMACIE" to "Pharmacie",
        "HOSPITALISATION" to "Hôpital",
        "RADIOLOGIE" to "Radio"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        filters.forEach { (type, label) ->
            val isSelected = selectedFilter == type
            FilterChip(
                selected = isSelected,
                onClick = { onFilterSelected(type) },
                label = { Text(label, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = AppColors.BrandBlue,
                    selectedLabelColor = Color.White
                ),
                leadingIcon = if (isSelected) {
                    { Icon(Icons.Rounded.Check, null, modifier = Modifier.size(14.dp)) }
                } else null
            )
        }
    }
}

@Composable
private fun ServiceTimelineItem(
    service: ServiceMedicalDto,
    isFirst: Boolean,
    isLast: Boolean
) {
    val typeColor = when (service.typeService) {
        "CONSULTATION" -> AppColors.BrandBlue
        "LABORATOIRE" -> Color(0xFF8B5CF6)
        "PHARMACIE" -> AppColors.StatusGreen
        "HOSPITALISATION" -> AppColors.StatusRed
        "RADIOLOGIE" -> AppColors.ActionBlue
        else -> AppColors.TextSub
    }

    val typeIcon = when (service.typeService) {
        "CONSULTATION" -> Icons.Rounded.MedicalServices
        "LABORATOIRE" -> Icons.Rounded.Science
        "PHARMACIE" -> Icons.Rounded.Medication
        "HOSPITALISATION" -> Icons.Rounded.LocalHospital
        "RADIOLOGIE" -> Icons.Rounded.CameraAlt
        else -> Icons.Rounded.HealthAndSafety
    }

    val statutColor = when (service.statut) {
        "PAYE" -> AppColors.StatusGreen
        "VALIDE" -> AppColors.BrandBlue
        "EN_COURS" -> AppColors.StatusOrange
        "REJETE" -> AppColors.StatusRed
        else -> AppColors.TextSub
    }

    val statutLabel = when (service.statut) {
        "PAYE" -> "Payé"
        "VALIDE" -> "Validé"
        "EN_COURS" -> "En cours"
        "REJETE" -> "Rejeté"
        else -> service.statut ?: ""
    }

    val formattedDate = try {
        val dt = java.time.LocalDateTime.parse(service.dateService)
        val formatter = java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy")
        dt.format(formatter)
    } catch (e: Exception) {
        service.dateService ?: ""
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp)
    ) {
        // Timeline line + dot
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(32.dp)
        ) {
            if (!isFirst) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(12.dp)
                        .background(AppColors.BorderColor)
                )
            } else {
                Spacer(Modifier.height(12.dp))
            }

            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(typeColor)
            )

            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .weight(1f)
                        .background(AppColors.BorderColor)
                )
            }
        }

        Spacer(Modifier.width(8.dp))

        // Card
        Surface(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = 8.dp),
            shape = AppShapes.LargeRadius,
            color = Color.White,
            shadowElevation = 1.dp
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier.size(36.dp),
                            shape = CircleShape,
                            color = typeColor.copy(alpha = 0.1f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(typeIcon, null, tint = typeColor, modifier = Modifier.size(18.dp))
                            }
                        }
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(
                                service.typeService?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "Service",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = AppColors.TextMain
                            )
                            Text(formattedDate, fontSize = 11.sp, color = AppColors.TextSub)
                        }
                    }

                    // Statut badge
                    Surface(
                        shape = AppShapes.CircleRadius,
                        color = statutColor.copy(alpha = 0.1f)
                    ) {
                        Text(
                            statutLabel,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = statutColor
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                Text(
                    service.description ?: "",
                    fontSize = 13.sp,
                    color = AppColors.TextMain,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(Modifier.height(8.dp))

                // Établissement + Médecin
                if (service.etablissement != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.LocationOn, null, tint = AppColors.TextSub, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(service.etablissement, fontSize = 11.sp, color = AppColors.TextSub, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
                if (service.medecin != null) {
                    Spacer(Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Person, null, tint = AppColors.TextSub, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(service.medecin, fontSize = 11.sp, color = AppColors.TextSub)
                    }
                }

                // Bénéficiaire (si personne en charge)
                if (service.personneChargeId != null && service.beneficiaireNom != null) {
                    Spacer(Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.ChildCare, null, tint = AppColors.BrandBlue, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "Pour: ${service.beneficiaireNom}",
                            fontSize = 11.sp,
                            color = AppColors.BrandBlue,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))
                HorizontalDivider(color = AppColors.BorderColorLight)
                Spacer(Modifier.height(8.dp))

                // Montants
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Montant", fontSize = 10.sp, color = AppColors.TextSub)
                        Text(
                            "${String.format("%,.0f", service.montant ?: 0.0)} FCFA",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = AppColors.TextMain
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Pris en charge (80%)", fontSize = 10.sp, color = AppColors.TextSub)
                        Text(
                            "${String.format("%,.0f", service.partAssurance ?: 0.0)} FCFA",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = AppColors.StatusGreen
                        )
                    }
                }
                
                Spacer(Modifier.height(4.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        // Vide pour l'alignement
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Ticket modérateur (20%)", fontSize = 10.sp, color = AppColors.TextSub)
                        Text(
                            "${String.format("%,.0f", service.ticketModerateur ?: 0.0)} FCFA",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = AppColors.BrandBlue
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyMedicalState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier.size(80.dp),
            shape = CircleShape,
            color = AppColors.BrandBlue.copy(alpha = 0.08f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Rounded.MedicalServices,
                    null,
                    tint = AppColors.BrandBlue,
                    modifier = Modifier.size(36.dp)
                )
            }
        }
        Spacer(Modifier.height(16.dp))
        Text(
            "Aucun service médical",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = AppColors.TextMain
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "Vos consultations et soins apparaîtront ici",
            fontSize = 13.sp,
            color = AppColors.TextSub,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}
