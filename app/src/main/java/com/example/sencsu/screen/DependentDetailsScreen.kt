package com.example.sencsu.screen

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.sencsu.components.ServerImage
import com.example.sencsu.components.QrCodeImage
import com.example.sencsu.configs.ApiConfig
import com.example.sencsu.data.remote.dto.PersonneChargeDto
import com.example.sencsu.domain.viewmodel.BeneficiaryDashboardViewModel
import com.example.sencsu.theme.AppColors
import com.example.sencsu.theme.AppShapes
import kotlinx.coroutines.delay

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DependentDetailsScreen(
    adherentId: String,
    pcId: String,
    onBack: () -> Unit,
    onNavigateToHistory: (String, String, String) -> Unit,
    onNavigateToCard: (String, String) -> Unit,
    onNavigateToEdit: (String, String) -> Unit,
    viewModel: BeneficiaryDashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val token by viewModel.sessionManager.tokenFlow.collectAsState(initial = null)
    
    val pc = uiState.adherent?.personnesCharge?.find { it.id == pcId }
    var showContent by remember { mutableStateOf(false) }

    // Refresh automatique quand l'écran revient au premier plan (ex: après une modification)
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                viewModel.refresh()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(Unit) {
        delay(100)
        showContent = true
    }

    Scaffold(
        containerColor = AppColors.AppBackground,
        topBar = {
            TopAppBar(
                title = { Text("Détails du membre", fontWeight = FontWeight.Black, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Retour", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { onNavigateToEdit(adherentId, pcId) }) {
                        Icon(Icons.Rounded.Edit, "Modifier", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        if (pc == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AppColors.BrandBlue)
            }
        } else {
            // Utilisation d'une Box pour mettre le contenu sous le TopAppBar transparent
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    // ── En-tête avec Carte (Commence tout en haut) ──
                    DependentIdentitySection(
                        pc = pc, 
                        sessionManager = viewModel.sessionManager,
                        onClick = { onNavigateToCard(adherentId, pcId) }
                    )

                    Spacer(Modifier.height(24.dp))

                    // ── État de la Couverture ──
                    AnimatedVisibility(
                        visible = showContent,
                        enter = fadeIn(tween(400, 100)) + slideInVertically(tween(400, 100), initialOffsetY = { 40 })
                    ) {
                        DependentCoverageCard(pc)
                    }

                    Spacer(Modifier.height(16.dp))

                    // ── Actions Rapides ──
                    AnimatedVisibility(
                        visible = showContent,
                        enter = fadeIn(tween(400, 200)) + slideInVertically(tween(400, 200), initialOffsetY = { 40 })
                    ) {
                        DependentActionsRow(
                            onHistory = { 
                                val name = "${pc.prenoms} ${pc.nom}".trim()
                                onNavigateToHistory(adherentId, pcId, name) 
                            },
                            onQR = { /* Déjà affiché */ }
                        )
                    }

                    Spacer(Modifier.height(24.dp))

                    // ── Informations Personnelles ──
                    AnimatedVisibility(
                        visible = showContent,
                        enter = fadeIn(tween(400, 300)) + slideInVertically(tween(400, 300), initialOffsetY = { 40 })
                    ) {
                        DependentInfoSection(pc)
                    }
                    
                    Spacer(Modifier.height(50.dp))
                }
            }
        }
    }
}

@Composable
private fun DependentIdentitySection(pc: PersonneChargeDto, sessionManager: com.example.sencsu.data.repository.SessionManager, onClick: () -> Unit) {
    val context = LocalContext.current
    val fullName = "${pc.prenoms} ${pc.nom}".trim()
    
    Box(modifier = Modifier.fillMaxWidth().height(260.dp)) {
        // Fond gradient
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(AppColors.BrandBlueDark, AppColors.BrandBlue)
                    )
                )
        )
        
        // Carte d'identité "Assuré"
        Card(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .fillMaxWidth()
                .height(200.dp)
                .align(Alignment.BottomCenter)
                .clickable { onClick() },
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            listOf(AppColors.CardGradientStart, AppColors.CardGradientEnd)
                        )
                    )
            ) {
                // Filigrane / Déco
                Icon(
                    Icons.Rounded.Shield, null,
                    modifier = Modifier.size(180.dp).align(Alignment.CenterEnd).offset(x = 40.dp).alpha(0.05f),
                    tint = Color.White
                )

                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("CARTE D'ASSURÉ", color = Color.White.copy(0.7f), fontSize = 12.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                        Icon(Icons.Rounded.Verified, null, tint = AppColors.GoldAccent, modifier = Modifier.size(24.dp))
                    }
                    
                    Spacer(Modifier.weight(1f))
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Photo ou Initiales
                        Surface(
                            modifier = Modifier.size(64.dp),
                            shape = CircleShape,
                            color = Color.White.copy(0.1f),
                            border = BorderStroke(2.dp, Color.White.copy(0.3f))
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                if (pc.photo != null) {
                                    ServerImage(
                                        filename = pc.photo,
                                        sessionManager = sessionManager,
                                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    val initials = "${pc.prenoms?.firstOrNull()?.uppercase() ?: ""}${pc.nom?.firstOrNull()?.uppercase() ?: ""}"
                                    Text(initials, color = Color.White, fontWeight = FontWeight.Black, fontSize = 22.sp)
                                }
                            }
                        }
                        
                        Spacer(Modifier.width(16.dp))
                        
                        Column {
                            Text(fullName, color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp)
                            Text(pc.matricule?.let { "MATRICULE: $it" } ?: "EN ATTENTE", color = Color.White.copy(0.8f), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                    
                    Spacer(Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column {
                            Text("LIEN DE PARENTÉ", color = Color.White.copy(0.5f), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            Text(pc.lienParent?.uppercase() ?: "NON DÉFINI", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                        
                        // Petit QR Code sur la carte
                        if (pc.matricule != null) {
                            Surface(
                                modifier = Modifier.size(40.dp),
                                shape = RoundedCornerShape(8.dp),
                                color = Color.White
                            ) {
                                QrCodeImage(
                                    value = com.example.sencsu.components.buildBeneficiaryQrUrl(pc.matricule),
                                    modifier = Modifier.padding(4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun DependentCoverageCard(pc: PersonneChargeDto) {
    // On simule une validité d'un an à partir de createdAt ou aujourd'hui
    val createdAt = pc.createdAt ?: java.time.LocalDateTime.now().toString()
    
    val progress = remember(createdAt) {
        try {
            val created = java.time.LocalDateTime.parse(createdAt)
            val now = java.time.LocalDateTime.now()
            val end = created.plusYears(1)
            val totalDays = java.time.temporal.ChronoUnit.DAYS.between(created, end).toFloat()
            val elapsedDays = java.time.temporal.ChronoUnit.DAYS.between(created, now).toFloat()
            (elapsedDays / totalDays).coerceIn(0f, 1f)
        } catch (e: Exception) { 0.05f }
    }

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(1500, easing = FastOutSlowInEasing),
        label = "progress"
    )

    Surface(
        modifier = Modifier.padding(horizontal = 20.dp).fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 4.dp
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.HistoryToggleOff, null, tint = AppColors.BrandBlue, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Validité de la couverture", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = AppColors.TextMain)
            }
            
            Spacer(Modifier.height(16.dp))
            
            LinearProgressIndicator(
                progress = animatedProgress,
                modifier = Modifier.fillMaxWidth().height(10.dp).clip(CircleShape),
                color = if (progress > 0.9f) AppColors.StatusRed else AppColors.StatusGreen,
                trackColor = AppColors.BrandBlue.copy(0.05f),
                strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
            )
            
            Spacer(Modifier.height(12.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Bénéficiaire actif", fontSize = 12.sp, color = AppColors.TextSub)
                Text("1 an restant", fontWeight = FontWeight.Black, fontSize = 12.sp, color = AppColors.StatusGreen)
            }
        }
    }
}

@Composable
private fun DependentActionsRow(onHistory: () -> Unit, onQR: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            onClick = onHistory,
            modifier = Modifier.weight(1f).height(54.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AppColors.BrandBlue)
        ) {
            Icon(Icons.Rounded.MedicalServices, null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text("Historique", fontWeight = FontWeight.Bold)
        }
        
        OutlinedButton(
            onClick = onQR,
            modifier = Modifier.weight(1f).height(54.dp),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, AppColors.BrandBlue)
        ) {
            Icon(Icons.Rounded.QrCodeScanner, null, tint = AppColors.BrandBlue, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text("Scanner QR", color = AppColors.BrandBlue, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun DependentInfoSection(pc: PersonneChargeDto) {
    Card(
        modifier = Modifier.padding(horizontal = 20.dp).fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Informations complémentaires", fontWeight = FontWeight.Black, fontSize = 16.sp, color = AppColors.TextMain)
            
            InfoRow(Icons.Rounded.CalendarToday, "Date de naissance", pc.dateNaissance ?: "Non renseignée")
            HorizontalDivider(color = Color.LightGray.copy(0.2f))
            InfoRow(Icons.Rounded.LocationOn, "Lieu de naissance", pc.lieuNaissance ?: "Non renseigné")
            HorizontalDivider(color = Color.LightGray.copy(0.2f))
            InfoRow(Icons.Rounded.PersonOutline, "Sexe", if(pc.sexe == "M") "Masculin" else "Féminin")
            HorizontalDivider(color = Color.LightGray.copy(0.2f))
            InfoRow(Icons.Rounded.Badge, "Pièce d'identité", pc.numeroCNi?.let { "CNI: $it" } ?: pc.numeroExtrait?.let { "Extrait: $it" } ?: "Aucune")
        }
    }
}

@Composable
private fun InfoRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = AppColors.TextSub.copy(0.6f), modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp))
        Column {
            Text(label, fontSize = 11.sp, color = AppColors.TextSub)
            Text(value, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = AppColors.TextMain)
        }
    }
}

// End of file
