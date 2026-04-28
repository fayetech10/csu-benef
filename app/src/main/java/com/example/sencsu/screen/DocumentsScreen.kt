package com.example.sencsu.screen

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.sencsu.domain.viewmodel.BeneficiaryDashboardViewModel
import com.example.sencsu.theme.AppColors
import com.example.sencsu.theme.AppShapes
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentsScreen(
    viewModel: BeneficiaryDashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val adherent = uiState.adherent
    val isActive = adherent?.actif != false
    
    var showContent by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(100)
        showContent = true
    }

    Scaffold(
        containerColor = AppColors.AppBackground,
        topBar = {
            TopAppBar(
                title = { Text("Mes Documents", fontWeight = FontWeight.Black) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    "Retrouvez vos attestations et reçus de paiement officiels.",
                    color = AppColors.TextSub,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            val docs = listOf(
                DocumentItem("Attestation d'Affiliation", "Preuve de couverture santé active", Icons.Rounded.Verified, true),
                DocumentItem("Carte de Membre Digitale", "Format PDF pour impression", Icons.Rounded.Badge, true),
                DocumentItem("Dernier Reçu de Paiement", "Cotisation annuelle ${adherent?.regime ?: ""}", Icons.Rounded.ReceiptLong, false),
                DocumentItem("Livret d'Accueil CMU", "Guide des prestations et soins", Icons.Rounded.MenuBook, false)
            )

            itemsIndexed(docs) { index, doc ->
                Column {
                    AnimatedVisibility(
                        visible = showContent,
                        enter = fadeIn(tween(300, delayMillis = index * 100)) + slideInHorizontally(
                            tween(300, delayMillis = index * 100), initialOffsetX = { -40 }
                        )
                    ) {
                        DocumentCard(doc, isActive)
                    }
                }
            }
            
            if (!isActive) {
                item {
                    Spacer(Modifier.height(16.dp))
                    Surface(
                        color = AppColors.StatusOrange.copy(alpha = 0.1f),
                        shape = AppShapes.MediumRadius,
                        border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.StatusOrange.copy(0.2f))
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.Info, null, tint = AppColors.StatusOrange)
                            Spacer(Modifier.width(12.dp))
                            Text(
                                "Certains documents sont verrouillés car votre compte est actuellement inactif. Contactez un agent CMU.",
                                fontSize = 13.sp,
                                color = AppColors.StatusOrange,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DocumentCard(doc: DocumentItem, isActive: Boolean) {
    val isLocked = doc.requiresActive && !isActive

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = AppShapes.MediumRadius,
        color = Color.White,
        shadowElevation = 1.dp,
        onClick = { /* Download logic */ }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = AppShapes.MediumRadius,
                color = if (isLocked) AppColors.SurfaceAlt else AppColors.BrandBlue.copy(0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        doc.icon, null,
                        tint = if (isLocked) AppColors.TextDisabled else AppColors.BrandBlue
                    )
                }
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    doc.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = if (isLocked) AppColors.TextDisabled else AppColors.TextMain
                )
                Text(
                    doc.sub,
                    color = AppColors.TextSub,
                    fontSize = 12.sp
                )
            }
            
            if (isLocked) {
                Icon(
                    Icons.Rounded.Lock, null,
                    tint = AppColors.TextDisabled,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Icon(
                    Icons.Rounded.FileDownload, null,
                    tint = AppColors.BrandBlue,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

private data class DocumentItem(
    val title: String,
    val sub: String,
    val icon: ImageVector,
    val requiresActive: Boolean
)
