package com.example.sencsu.components.forms


import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.draw.clip
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.sencsu.components.SectionCard
import com.example.sencsu.components.ocr.OcrProcessingFlow
import com.example.sencsu.domain.viewmodel.AddAdherentViewModel
import com.example.sencsu.domain.viewmodel.OcrViewModel
import com.example.sencsu.theme.AppColors
import com.example.sencsu.theme.AppShapes

/**
 * Section Photos intégrée avec OCR
 * Permet le choix entre extraction OCR ou saisie manuelle
 */
@Composable
fun PhotosSectionWithOcr(
    state: com.example.sencsu.domain.viewmodel.AddAdherentUiState,
    addAdherentViewModel: AddAdherentViewModel,
    ocrViewModel: OcrViewModel = hiltViewModel()
) {
    var showOcrFlow by remember { mutableStateOf(false) }
    var hasStartedOcr by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxWidth()) {
        AnimatedVisibility(
            visible = !showOcrFlow,
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut() + slideOutVertically()
        ) {
            PhotosSectionContent(
                state = state,
                viewModel = addAdherentViewModel,
                onStartOcr = {
                    showOcrFlow = true
                    hasStartedOcr = true
                }
            )
        }

        AnimatedVisibility(
            visible = showOcrFlow,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it })
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(AppColors.SurfaceBackground)
            ) {
                OcrProcessingFlow(
                    viewModel = ocrViewModel,
                    onComplete = { extractedData ->
                        // Injecter les données dans le formulaire
                        extractedData["prenoms"]?.let { addAdherentViewModel.updatePrenoms(it) }
                        extractedData["nom"]?.let { addAdherentViewModel.updateNom(it) }
                        extractedData["dateNaissance"]?.let { addAdherentViewModel.updateDateNaissance(it) }
                        extractedData["sexe"]?.let { addAdherentViewModel.updateSexe(it) }
                        extractedData["nin"]?.let { addAdherentViewModel.updateNumeroCNI(it) }
                        extractedData["lieuNaissance"]?.let { addAdherentViewModel.updateLieuNaissance(it) }
                        extractedData["departement"]?.let { addAdherentViewModel.updateDepartement(it) }
                        extractedData["commune"]?.let { addAdherentViewModel.updateCommune(it) }

                        showOcrFlow = false
                    },
                    onCancel = {
                        showOcrFlow = false
                        ocrViewModel.resetOcr()
                    }
                )
            }
        }
    }
}

@Composable
fun PhotosSectionContent(
    state: com.example.sencsu.domain.viewmodel.AddAdherentUiState,
    viewModel: AddAdherentViewModel,
    onStartOcr: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth().padding(vertical = 16.dp)) {
        SectionCard("Documents", Icons.Outlined.CameraAlt) {
            // Bouton d'extraction OCR
            OcrInitiatorButton(onStartOcr = onStartOcr)

            Spacer(modifier = Modifier.height(16.dp))

            Divider(
                color = AppColors.BorderColor,
                thickness = 1.dp,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ✅ Photo principale
            ImagePickerComponent(
                label = "Photo d'identité*",
                imageUri = state.photoUri?.toString() ?: state.existingPhotoUrl,
                onImageSelected = viewModel::updatePhotoUri,
                required = true,
                isError = state.validationErrors.containsKey("photoUri")
            )
            Spacer(modifier = Modifier.height(12.dp))

            // ✅ Recto
            ImagePickerComponent(
                label = "CNI Recto",
                imageUri = state.rectoUri?.toString() ?: state.existingRectoUrl,
                onImageSelected = viewModel::updateRectoUri
            )
            Spacer(modifier = Modifier.height(12.dp))

            // ✅ Verso
            ImagePickerComponent(
                label = "CNI Verso",
                imageUri = state.versoUri?.toString() ?: state.existingVersoUrl,
                onImageSelected = viewModel::updateVersoUri
            )
        }
    }
}

@Composable
fun OcrInitiatorButton(
    onStartOcr: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onStartOcr,
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp),
        shape = AppShapes.MediumRadius,
        colors = ButtonDefaults.buttonColors(
            containerColor = AppColors.BrandBlue.copy(alpha = 0.1f)
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.CameraAlt,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = AppColors.BrandBlue
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "📸 Extraire les données (OCR)",
                color = AppColors.BrandBlue,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp
            )
        }
    }
}

/**
 * Composant réutilisable pour activer l'OCR dans n'importe quel formulaire
 */
@Composable
fun OcrExtractorDialog(
    visible: Boolean,
    onDismiss: () -> Unit,
    onExtractedData: (Map<String, String>) -> Unit,
    ocrViewModel: OcrViewModel = hiltViewModel()
) {
    if (visible) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                dismissOnBackPress = true,
                dismissOnClickOutside = false
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxSize(0.95f)
                        .clip(AppShapes.MediumRadius),
                    color = AppColors.SurfaceBackground
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        OcrProcessingFlow(
                            viewModel = ocrViewModel,
                            onComplete = { extractedData ->
                                onExtractedData(extractedData)
                                onDismiss()
                            },
                            onCancel = {
                                onDismiss()
                                ocrViewModel.resetOcr()
                            }
                        )

                        // Bouton fermer en haut à droite
                        IconButton(
                            onClick = {
                                onDismiss()
                                ocrViewModel.resetOcr()
                            },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Fermer",
                                tint = AppColors.TextMain
                            )
                        }
                    }
                }
            }
        }
    }
}