package com.example.sencsu.screen

import android.annotation.SuppressLint
import com.example.sencsu.components.cartes.HealthInsuranceCard
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Message
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.sencsu.components.ServerImage
import com.example.sencsu.components.modals.AddPersonneChargeModal
import com.example.sencsu.data.remote.dto.*
import com.example.sencsu.data.repository.SessionManager
import com.example.sencsu.domain.viewmodel.AddAdherentViewModel
import com.example.sencsu.domain.viewmodel.AdherentDetailsViewModel
import com.example.sencsu.domain.viewmodel.DetailsUiEvent
import com.example.sencsu.theme.AppColors
import com.example.sencsu.theme.AppShapes
import com.example.sencsu.theme.withAlpha
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.math.min

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdherentDetailsScreen(
    viewModel: AdherentDetailsViewModel = hiltViewModel(),
    viewModelP: AddAdherentViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (String) -> Unit = {},
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberLazyListState()
    
    // Header height calculation for parallax/sticky effect
    val headerHeight = 240.dp
    val scrollOffset = remember { derivedStateOf { scrollState.firstVisibleItemScrollOffset } }
    val isHeaderCollapsed by remember { derivedStateOf { scrollState.firstVisibleItemIndex > 0 } }

    // État des modales
    var showAddPersonneModal by remember { mutableStateOf(false) }
    var showPersonneDetailsModal by remember { mutableStateOf<PersonneChargeDto?>(null) }
    var showMenu by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is DetailsUiEvent.AdherentDeleted -> onNavigateBack()
                is DetailsUiEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    Scaffold(
        containerColor = AppColors.AppBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (state.adherent != null) {
                ExtendedFloatingActionButton(
                    text = { Text("Bénéficiaire", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Rounded.PersonAdd, null) },
                    onClick = { showAddPersonneModal = true },
                    containerColor = AppColors.BrandBlue,
                    contentColor = Color.White,
                    shape = AppShapes.MediumRadius,
                    expanded = !isHeaderCollapsed
                )
            }
        }
    ) { padding ->
        val context = LocalContext.current
        
        // Modals (Existing logic preserved)
        if (state.showDeleteAdherentDialog) {
            ConfirmationDialog(
                title = "Supprimer l'adhérent",
                message = "Cette action est irréversible. Voulez-vous continuer ?",
                onConfirm = { viewModel.confirmDeleteAdherent() },
                onDismiss = { viewModel.cancelDeleteAdherent() }
            )
        }

        // Add Personne Modal
        if (showAddPersonneModal) {
            Dialog(
                onDismissRequest = { showAddPersonneModal = false },
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                AddPersonneChargeModal(
                    viewModel = viewModelP,
                    onSave = {
                        val formData = viewModelP.uiState.value.currentDependant
                        viewModel.onNewPersonneChange(formData)
                        viewModel.onSaveNewPersonne(context)
                        showAddPersonneModal = false
                    },
                    onCancel = { showAddPersonneModal = false }
                )
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            
            // ── MAIN CONTENT ──
            if (state.adherent != null) {
                LazyColumn(
                    state = scrollState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 100.dp)
                ) {
                    // Transparent space for header
                    item { Spacer(modifier = Modifier.height(headerHeight - 10.dp)) }

                    // Content Body
                    item {
                        DetailBody(
                            adherent = state.adherent!!,
                            paiements = state.paiements,
                            cotisations = state.cotisations,
                            sessionManager = viewModel.sessionManager,
                            viewModel = viewModel,
                            onPersonneClick = { showPersonneDetailsModal = it },
                            onEdit = { onNavigateToEdit(state.adherent!!.id!!) }
                        )
                    }
                }
            } else if (state.isLoading) {
                LoadingState()
            } else if (state.error != null) {
                ErrorStates(message = state.error!!, onRetry = { viewModel.refresh() })
            }

            // ── PARALLAX HEADER ──
            if (state.adherent != null) {
                PremiumHeader(
                    adherent = state.adherent!!,
                    scrollOffset = scrollOffset.value,
                    maxHeight = headerHeight,
                    sessionManager = viewModel.sessionManager,
                    onBack = onNavigateBack,
                    onMenuClick = { showMenu = true }
                )
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun DetailBody(
    adherent: AdherentDto,
    paiements: List<PaiementDto>,
    cotisations: List<CotisationDto>,
    sessionManager: SessionManager,
    viewModel: AdherentDetailsViewModel,
    onPersonneClick: (PersonneChargeDto) -> Unit,
    onEdit: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
            .background(AppColors.AppBackground)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // ── QUICK ACTIONS DOCK ──
        QuickActionDock(adherent)

        // ── HEALTH CARD ──
        HealthInsuranceCard(
            data = adherent,
            sessionManager = sessionManager,
            modifier = Modifier.shadow(8.dp, AppShapes.LargeRadius)
        )

        // ── STATS CARDS ──
        FinanceOverview(adherent, paiements)

        // ── INFORMATION SECTIONS ──
        DetailSection(
            title = "Détails Personnels",
            icon = Icons.Rounded.Badge
        ) {
            InfoCard {
                InfoLine(Icons.Rounded.Fingerprint, "N° CNI", adherent.numeroCNi ?: "---")
                InfoLine(Icons.Rounded.Cake, "Naissance", "${adherent.dateNaissance} à ${adherent.lieuNaissance}")
                InfoLine(Icons.Rounded.Wc, "Sexe", adherent.sexe ?: "---")
                InfoLine(Icons.Rounded.Work, "Activité", adherent.secteurActivite ?: "PARTICULIER")
            }
        }

        DetailSection(
            title = "Coordonnées",
            icon = Icons.Rounded.LocationOn
        ) {
            InfoCard {
                InfoLine(Icons.Rounded.Phone, "Téléphone / WhatsApp", adherent.whatsapp ?: "---")
                InfoLine(Icons.Rounded.Home, "Adresse", adherent.adresse ?: "---")
                InfoLine(Icons.Rounded.Public, "Localité", "${adherent.commune}, ${adherent.departement}")
            }
        }

        // ── BENEFICIARIES CAROUSEL ──
        if (adherent.personnesCharge.isNotEmpty()) {
            DetailSection(
                title = "Bénéficiaires (${adherent.personnesCharge.size})",
                icon = Icons.Rounded.Group
            ) {
                BeneficiaryCarousel(adherent.personnesCharge, sessionManager, onPersonneClick)
            }
        }

        // ── DOCUMENTS ──
        DetailSection(
            title = "Documents Justificatifs",
            icon = Icons.Rounded.Description
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                DocumentPreview(
                    title = "CNI Recto",
                    imageUrl = adherent.photoRecto,
                    sessionManager = sessionManager,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.openImagePreview(adherent.photoRecto) }
                )
                DocumentPreview(
                    title = "CNI Verso",
                    imageUrl = adherent.photoVerso,
                    sessionManager = sessionManager,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.openImagePreview(adherent.photoVerso) }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun PremiumHeader(
    adherent: AdherentDto,
    scrollOffset: Int,
    maxHeight: androidx.compose.ui.unit.Dp,
    sessionManager: SessionManager,
    onBack: () -> Unit,
    onMenuClick: () -> Unit
) {
    val alpha = (1f - (scrollOffset / 200f)).coerceIn(0f, 1f)
    val scale = (1f - (scrollOffset / 1000f)).coerceIn(0.8f, 1f)
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(maxHeight)
    ) {
        // Blurred Background
        ServerImage(
            filename = adherent.photo,
            sessionManager = sessionManager,
            modifier = Modifier
                .fillMaxWidth()
                .height(maxHeight - 40.dp)
                .blur(20.dp)
                .alpha(0.6f),
            contentScale = ContentScale.Crop
        )

        // Gradient Overlay
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(maxHeight - 40.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            AppColors.BrandBlue.withAlpha(0.8f),
                            AppColors.BrandBlueDark.withAlpha(0.9f)
                        )
                    )
                )
        )

        // Profile Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(bottom = 40.dp)
                .graphicsLayer {
                    this.alpha = alpha
                    this.scaleX = scale
                    this.scaleY = scale
                },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(contentAlignment = Alignment.BottomEnd) {
                ServerImage(
                    filename = adherent.photo,
                    sessionManager = sessionManager,
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .border(4.dp, Color.White.withAlpha(0.3f), CircleShape)
                        .shadow(12.dp, CircleShape),
                    contentScale = ContentScale.Crop
                )
                
                // Status Badge (Dynamic)
                val endDate = com.example.sencsu.utils.Formatters.getCoverageEndDate(adherent.coveragePeriod)
                val isExpired = endDate != null && endDate.isBefore(java.time.LocalDate.now())
                val statusColor = if (isExpired) AppColors.StatusRed else AppColors.StatusGreen
                
                Surface(
                    color = statusColor,
                    shape = CircleShape,
                    modifier = Modifier
                        .size(32.dp)
                        .border(3.dp, Color.White, CircleShape)
                ) {
                    Icon(
                        if (isExpired) Icons.Rounded.ErrorOutline else Icons.Rounded.Verified,
                        null,
                        tint = Color.White,
                        modifier = Modifier.padding(6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                "${adherent.prenoms} ${adherent.nom}",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                fontWeight = FontWeight.ExtraBold
            )
            
            Surface(
                color = Color.White.withAlpha(0.15f),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text(
                    adherent.matricule ?: "MATRICULE...",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    color = Color.White,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Floating Back Button
        Row(
            modifier = Modifier
                .statusBarsPadding()
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            HeaderButton(Icons.AutoMirrored.Rounded.ArrowBack, onBack)
            HeaderButton(Icons.Rounded.MoreVert, onMenuClick)
        }
    }
}

@Composable
private fun HeaderButton(icon: ImageVector, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = Color.Black.withAlpha(0.2f),
        contentColor = Color.White,
        modifier = Modifier.size(40.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, null, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun QuickActionDock(adherent: AdherentDto) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .offset(y = (-30).dp)
            .shadow(12.dp, AppShapes.MediumRadius)
            .background(Color.White, AppShapes.MediumRadius)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        ActionIcon(Icons.Rounded.Phone, "Appeler", AppColors.StatusGreen) {}
        ActionIcon(Icons.Rounded.Chat, "WhatsApp", Color(0xFF25D366)) {}
        ActionIcon(Icons.AutoMirrored.Rounded.Message, "SMS", AppColors.BrandBlue) {}
        ActionIcon(Icons.Rounded.Share, "Partager", Color(0xFF6C63FF)) {}
    }
}

@Composable
private fun ActionIcon(icon: ImageVector, label: String, color: Color, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Surface(
            shape = CircleShape,
            color = color.withAlpha(0.1f),
            modifier = Modifier.size(44.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = color, modifier = Modifier.size(22.dp))
            }
        }
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = AppColors.TextSub,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
private fun FinanceOverview(adherent: AdherentDto, paiements: List<PaiementDto>) {
    val totalPaid = paiements.sumOf { it.montant }
    val remaining = (adherent.montantTotal ?: 0.0) - totalPaid

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = AppShapes.LargeRadius,
        colors = CardDefaults.cardColors(containerColor = AppColors.SurfaceBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Bilan Cotisations", style = MaterialTheme.typography.labelMedium, color = AppColors.TextSub)
                Text(
                    "${adherent.montantTotal?.toInt()} FCFA",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = AppColors.BrandBlue
                )
            }
            
            VerticalDivider(modifier = Modifier.height(40.dp).padding(horizontal = 16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                FinanceStat("Payé", "${totalPaid.toInt()} F", AppColors.StatusGreen)
                FinanceStat("Restant", "${remaining.toInt()} F", AppColors.StatusOrange)
            }
        }
    }
}

@Composable
private fun FinanceStat(label: String, value: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
        Spacer(modifier = Modifier.width(8.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = AppColors.TextSub, modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = AppColors.TextMain)
    }
}

@Composable
private fun BeneficiaryCarousel(
    beneficiaries: List<PersonneChargeDto>,
    sessionManager: SessionManager,
    onPersonneClick: (PersonneChargeDto) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(beneficiaries) { person ->
            Card(
                onClick = { onPersonneClick(person) },
                modifier = Modifier.width(160.dp),
                shape = AppShapes.MediumRadius,
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, AppColors.BorderColorLight)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    ServerImage(
                        filename = person.photo,
                        sessionManager = sessionManager,
                        modifier = Modifier.size(60.dp).clip(CircleShape).background(AppColors.SurfaceAlt),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        person.prenoms ?: "",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        person.lienParent ?: "Bénéficiaire",
                        style = MaterialTheme.typography.labelSmall,
                        color = AppColors.BrandBlue,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun DocumentPreview(
    title: String,
    imageUrl: String?,
    sessionManager: SessionManager,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Column(modifier = modifier) {
        Card(
            onClick = onClick,
            modifier = Modifier.height(100.dp).fillMaxWidth(),
            shape = AppShapes.MediumRadius,
            border = BorderStroke(1.dp, AppColors.BorderColorLight)
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (!imageUrl.isNullOrBlank()) {
                    ServerImage(
                        filename = imageUrl,
                        sessionManager = sessionManager,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(Icons.Rounded.Image, null, tint = AppColors.TextDisabled, modifier = Modifier.size(32.dp))
                }
            }
        }
        Text(
            title,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(top = 4.dp, start = 4.dp),
            color = AppColors.TextSub
        )
    }
}

@Composable
private fun InfoCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = AppShapes.MediumRadius,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, AppColors.BorderColorLight)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            content()
        }
    }
}

@Composable
private fun InfoLine(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = AppColors.BrandBlue.withAlpha(0.6f), modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = AppColors.TextSub)
            Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = AppColors.TextMain)
        }
    }
}

@Composable
private fun DetailSection(title: String, icon: ImageVector, content: @Composable () -> Unit) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 12.dp)) {
            Icon(icon, null, tint = AppColors.BrandBlue, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
        }
        content()
    }
}

@Composable
private fun LoadingState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = AppColors.BrandBlue)
    }
}

@Composable
private fun ConfirmationDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = { Text(message) },
        confirmButton = {
            Button(onClick = onConfirm, colors = ButtonDefaults.buttonColors(containerColor = AppColors.StatusRed)) {
                Text("Confirmer", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuler") }
        },
        containerColor = Color.White,
        shape = AppShapes.MediumRadius
    )
}

@Composable
fun PersonneDetailsModal(
    personne: PersonneChargeDto,
    sessionManager: SessionManager,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(vertical = 24.dp)
                .clip(AppShapes.LargeRadius),
            color = Color.White
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Box(contentAlignment = Alignment.TopEnd, modifier = Modifier.fillMaxWidth()) {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Rounded.Close, null, tint = AppColors.TextSub)
                    }
                }
                
                ServerImage(
                    filename = personne.photo,
                    sessionManager = sessionManager,
                    modifier = Modifier.size(100.dp).clip(CircleShape).background(AppColors.SurfaceAlt),
                    contentScale = ContentScale.Crop
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    "${personne.prenoms} ${personne.nom}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    personne.lienParent ?: "Bénéficiaire",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppColors.BrandBlue,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Details Grid
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    ModalInfoRow(Icons.Rounded.Fingerprint, "N° Pièce", personne.numeroCNi ?: personne.numeroExtrait ?: "---")
                    ModalInfoRow(Icons.Rounded.Cake, "Naissance", "${personne.dateNaissance} à ${personne.lieuNaissance}")
                    ModalInfoRow(Icons.Rounded.Wc, "Sexe", personne.sexe ?: "---")
                }
                
                Spacer(modifier = Modifier.height(40.dp))
                
                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDelete,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AppColors.StatusRed),
                        border = BorderStroke(1.dp, AppColors.StatusRed.withAlpha(0.3f))
                    ) {
                        Icon(Icons.Rounded.Delete, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Supprimer")
                    }
                    
                    Button(
                        onClick = onEdit,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = AppColors.BrandBlue)
                    ) {
                        Icon(Icons.Rounded.Edit, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Modifier")
                    }
                }
            }
        }
    }
}

@Composable
private fun ModalInfoRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = AppColors.TextSub, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = AppColors.TextSub)
            Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ImagePreviewDialog(
    imageUrl: String,
    onDismiss: () -> Unit,
    sessionManager: SessionManager
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .clickable { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            ServerImage(
                filename = imageUrl,
                sessionManager = sessionManager,
                modifier = Modifier.fillMaxWidth().aspectRatio(1f),
                contentScale = ContentScale.Fit
            )
            
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.align(Alignment.TopEnd).padding(16.dp).statusBarsPadding()
            ) {
                Icon(Icons.Rounded.Close, null, tint = Color.White, modifier = Modifier.size(32.dp))
            }
        }
    }
}

@Composable
fun ErrorStates(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Rounded.ErrorOutline, null, tint = AppColors.StatusRed, modifier = Modifier.size(64.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Oups ! Quelque chose s'est mal passé",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Text(
            message,
            style = MaterialTheme.typography.bodyMedium,
            color = AppColors.TextSub,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = AppColors.BrandBlue)
        ) {
            Text("Réessayer")
        }
    }
}