package com.example.sencsu.screen

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.sencsu.configs.ApiConfig
import com.example.sencsu.domain.viewmodel.BeneficiaryDashboardViewModel
import com.example.sencsu.theme.AppColors
import kotlinx.coroutines.delay

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
    val token by viewModel.sessionManager.tokenFlow.collectAsState(initial = null)
    val context = LocalContext.current
    
    val adherent = uiState.adherent
    val pc = adherent?.personnesCharge?.find { it.id == pcId }
    
    val name = if (pc != null) "${pc.prenoms} ${pc.nom}" else "${adherent?.prenoms} ${adherent?.nom}"
    val matricule = if (pc != null) pc.matricule else adherent?.matricule
    val photo = if (pc != null) pc.photo else adherent?.photo
    val dateNais = if (pc != null) pc.dateNaissance else adherent?.dateNaissance
    val sexe = if (pc != null) pc.sexe else adherent?.sexe
    val regime = adherent?.regime ?: "-"

    var showContent by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(100)
        showContent = true
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
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
                    title = { Text("Ma Carte Digitale", fontWeight = FontWeight.Black) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Retour")
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
        if (adherent == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AppColors.BrandBlue)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(16.dp))
                
                Text(
                    "CARTE DE COUVERTURE SANTÉ",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = AppColors.BrandBlue,
                    letterSpacing = 1.2.sp
                )

                Spacer(Modifier.height(32.dp))

                // La Carte Portrait (Format Plein Écran)
                AnimatedVisibility(
                    visible = showContent,
                    enter = fadeIn(tween(600)) + expandVertically(tween(600))
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(24.dp, RoundedCornerShape(28.dp)),
                        shape = RoundedCornerShape(28.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Photo agrandie
                            Surface(
                                modifier = Modifier
                                    .size(160.dp)
                                    .shadow(8.dp, RoundedCornerShape(20.dp)),
                                shape = RoundedCornerShape(20.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                if (photo != null) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(context)
                                            .data(ApiConfig.getImageUrl(photo))
                                            .apply { token?.let { addHeader("Authorization", "Bearer $it") } }
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = "Photo",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Icon(
                                        Icons.Rounded.Person,
                                        contentDescription = null,
                                        modifier = Modifier.size(80.dp),
                                        tint = AppColors.BrandBlue.copy(alpha = 0.5f)
                                    )
                                }
                            }
                            
                            Spacer(Modifier.height(32.dp))
                            
                            // QR Code géant pour le scan
                            Surface(
                                modifier = Modifier
                                    .size(240.dp)
                                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                                    .padding(16.dp),
                                color = Color.White,
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                 AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(ApiConfig.getQrCodeUrl(matricule.orEmpty()))
                                        .apply { token?.let { addHeader("Authorization", "Bearer $it") } }
                                        .build(),
                                    contentDescription = "QR Code",
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            
                            Spacer(Modifier.height(32.dp))
                            
                            // Informations Identité
                            Text(
                                text = name ?: "",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Black,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            
                            Surface(
                                modifier = Modifier.padding(top = 12.dp),
                                color = AppColors.BrandBlue.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = matricule ?: "SANS MATRICULE",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = AppColors.BrandBlue,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                                )
                            }
                            
                            Spacer(Modifier.height(32.dp))
                            
                            HorizontalDivider(
                                modifier = Modifier.fillMaxWidth(),
                                thickness = 0.5.dp,
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            )
                            
                            Spacer(Modifier.height(24.dp))
                            
                            // Détails techniques
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                CardInfoRow("Date de Naissance", dateNais ?: "-")
                                CardInfoRow("Sexe", if(sexe == "M") "Masculin" else if(sexe == "F") "Féminin" else "-")
                                CardInfoRow("Régime", regime ?: "-")
                                CardInfoRow("Statut", "ACTIF", AppColors.StatusGreen)
                            }
                        }
                    }
                }
                
                Spacer(Modifier.height(40.dp))
                
                Text(
                    "Cette carte est strictement personnelle.\nEn cas d'urgence, présentez ce code au personnel médical.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
private fun CardInfoRow(label: String, value: String, valueColor: Color = MaterialTheme.colorScheme.onSurface) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = valueColor
        )
    }
}
