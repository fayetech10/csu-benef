package com.example.sencsu.screen

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Badge
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.FamilyRestroom
import androidx.compose.material.icons.rounded.HealthAndSafety
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.sencsu.components.cartes.HealthInsuranceCard
import com.example.sencsu.data.remote.dto.AdherentDto
import com.example.sencsu.data.remote.dto.PersonneChargeDto
import com.example.sencsu.domain.viewmodel.BeneficiaryDashboardViewModel
import com.example.sencsu.theme.AppColors
import com.example.sencsu.theme.AppElevation
import com.example.sencsu.theme.AppGradients
import com.example.sencsu.theme.AppShapes
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DigitalCardScreen(
    adherentId: String,
    pcId: String? = null,
    onBack: () -> Unit,
    viewModel: BeneficiaryDashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val adherent = uiState.adherent
    val dependant = adherent?.personnesCharge?.find { it.id == pcId }
    var showContent by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(100)
        showContent = true
    }

    Scaffold(
        containerColor = AppColors.AppBackground,
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(AppGradients.Brand))
            ) {
                TopAppBar(
                    title = {
                        Column {
                            Text("Ma carte digitale", fontWeight = FontWeight.Black)
                            Text(
                                if (dependant == null) "Titulaire principal" else "Personne a charge",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.78f)
                            )
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
    ) { padding ->
        when {
            adherent == null -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AppColors.BrandBlue)
                }
            }

            pcId != null && dependant == null -> {
                MissingCardState(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    message = "Cette personne a charge est introuvable."
                )
            }

            else -> {
                val cardData = remember(adherent, dependant) {
                    dependant?.toHealthCardAdherent(adherent) ?: adherent
                }
                val observation = remember(cardData.createdAt) {
                    cardData.createdAt?.let { observationState(it) }
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 18.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    AnimatedVisibility(
                        visible = showContent,
                        enter = fadeIn(tween(360)) + slideInVertically(tween(360), initialOffsetY = { 40 })
                    ) {
                        if (observation != null) {
                            ObservationNoticeCard(observation = observation)
                        } else {
                            HealthInsuranceCard(
                                data = cardData,
                                sessionManager = viewModel.sessionManager,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    CardOwnerSummary(
                        adherent = adherent,
                        dependant = dependant,
                        cardData = cardData
                    )

                    if (observation == null) {
                        UsageNotice()
                    }

                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun ObservationNoticeCard(observation: ObservationState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = AppShapes.LargeRadius,
        colors = CardDefaults.cardColors(containerColor = AppColors.GoldLight),
        elevation = CardDefaults.cardElevation(defaultElevation = AppElevation.cardRaised),
        border = BorderStroke(1.dp, AppColors.GoldAccent.copy(alpha = 0.22f))
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(shape = CircleShape, color = AppColors.GoldAccent.copy(alpha = 0.14f), modifier = Modifier.size(70.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Rounded.CalendarToday,
                        contentDescription = null,
                        tint = AppColors.GoldAccent,
                        modifier = Modifier.size(34.dp)
                    )
                }
            }
            Text(
                "Carte indisponible pour le moment",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = AppColors.TextMain,
                textAlign = TextAlign.Center
            )
            Text(
                "Ce beneficiaire est encore dans la periode d'observation d'un mois apres son ajout. La carte digitale sera disponible a la fin de cette periode.",
                style = MaterialTheme.typography.bodyMedium,
                color = AppColors.TextSub,
                textAlign = TextAlign.Center,
                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
            )
            Surface(
                shape = AppShapes.CircleRadius,
                color = AppColors.SurfaceBackground,
                border = BorderStroke(1.dp, AppColors.GoldAccent.copy(alpha = 0.18f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Rounded.Security, null, tint = AppColors.GoldAccent, modifier = Modifier.size(17.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "${observation.daysRemaining} jour(s) restant(s) - disponible le ${observation.availableOn}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.TextMain
                    )
                }
            }
        }
    }
}

@Composable
private fun CardOwnerSummary(
    adherent: AdherentDto,
    dependant: PersonneChargeDto?,
    cardData: AdherentDto
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = AppShapes.LargeRadius,
        colors = CardDefaults.cardColors(containerColor = AppColors.SurfaceBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = AppElevation.card),
        border = BorderStroke(1.dp, AppColors.BorderColorLight)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = CircleShape, color = AppColors.BrandBlueLite, modifier = Modifier.size(46.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            if (dependant == null) Icons.Rounded.Person else Icons.Rounded.FamilyRestroom,
                            contentDescription = null,
                            tint = AppColors.BrandBlue
                        )
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "${cardData.prenoms.orEmpty()} ${cardData.nom.orEmpty()}".trim().ifEmpty { "Beneficiaire" },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = AppColors.TextMain,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        if (dependant == null) "Titulaire de la couverture" else "Rattache a ${adherent.prenoms.orEmpty()} ${adherent.nom.orEmpty()}".trim(),
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColors.TextSub,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            HorizontalDivider(color = AppColors.BorderColorLight)

            InfoLine(Icons.Rounded.Badge, "Matricule", cardData.matricule.orDash())
            InfoLine(Icons.Rounded.CreditCard, "NIN / Piece", cardData.numeroCNi.orDash())
            InfoLine(Icons.Rounded.CalendarToday, "Naissance", cardData.dateNaissance.orDash())
            InfoLine(Icons.Rounded.VerifiedUser, "Regime", adherent.regime.orDash())
        }
    }
}

@Composable
private fun InfoLine(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = AppColors.BrandBlue, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(10.dp))
        Text(label, style = MaterialTheme.typography.bodySmall, color = AppColors.TextSub, modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = AppColors.TextMain)
    }
}

@Composable
private fun UsageNotice() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = AppShapes.LargeRadius,
        color = AppColors.ActionBlueSoft,
        border = BorderStroke(1.dp, AppColors.ActionBlue.copy(alpha = 0.14f))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
            Icon(Icons.Rounded.Security, contentDescription = null, tint = AppColors.ActionBlue)
            Spacer(Modifier.width(12.dp))
            Text(
                "Cette carte est strictement personnelle. Presentez-la au personnel medical pour verifier vos droits et scanner le QR code.",
                style = MaterialTheme.typography.bodySmall,
                color = AppColors.TextMain,
                lineHeight = MaterialTheme.typography.bodySmall.lineHeight
            )
        }
    }
}

@Composable
private fun MissingCardState(modifier: Modifier, message: String) {
    Column(
        modifier = modifier.padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(shape = CircleShape, color = AppColors.StatusRedSoft, modifier = Modifier.size(72.dp)) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Rounded.ErrorOutline, contentDescription = null, tint = AppColors.StatusRed, modifier = Modifier.size(34.dp))
            }
        }
        Spacer(Modifier.height(16.dp))
        Text(message, textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, color = AppColors.TextMain)
    }
}

private fun PersonneChargeDto.toHealthCardAdherent(parent: AdherentDto): AdherentDto {
    return parent.copy(
        id = id ?: parent.id,
        prenoms = prenoms,
        nom = nom,
        sexe = sexe,
        dateNaissance = dateNaissance,
        lieuNaissance = lieuNaissance,
        adresse = adresse,
        whatsapp = whatsapp,
        matricule = matricule,
        numeroCNi = numeroCNi,
        numeroPiece = numeroCNi ?: numeroExtrait.orEmpty(),
        typePiece = typePiece,
        photo = photo,
        photoRecto = photoRecto,
        photoVerso = photoVerso,
        typeBenef = lienParent ?: parent.typeBenef,
        createdAt = createdAt ?: parent.createdAt,
        personnesCharge = emptyList()
    )
}

private data class ObservationState(
    val daysRemaining: Long,
    val availableOn: String
)

@RequiresApi(Build.VERSION_CODES.O)
private fun observationState(createdAt: String): ObservationState? {
    val addedAt = parseAddedDate(createdAt) ?: return null
    val availableAt = addedAt.plusMonths(1)
    val today = LocalDate.now()

    if (!today.isBefore(availableAt)) return null

    val daysRemaining = ChronoUnit.DAYS.between(today, availableAt).coerceAtLeast(1)
    return ObservationState(
        daysRemaining = daysRemaining,
        availableOn = availableAt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
    )
}

@RequiresApi(Build.VERSION_CODES.O)
private fun parseAddedDate(value: String): LocalDate? {
    return runCatching { OffsetDateTime.parse(value).toLocalDate() }.getOrNull()
        ?: runCatching { LocalDateTime.parse(value).toLocalDate() }.getOrNull()
        ?: runCatching { LocalDate.parse(value) }.getOrNull()
}

private fun String?.orDash(): String = this?.takeIf { it.isNotBlank() } ?: "-"
