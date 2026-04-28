package com.example.sencsu.components.ocr

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import com.example.sencsu.domain.viewmodel.OcrStep
import com.example.sencsu.domain.viewmodel.OcrViewModel
import com.example.sencsu.theme.AppColors
import com.example.sencsu.theme.AppShapes
import androidx.core.content.FileProvider
import java.io.File

// ═══════════════════════════════════════════════════════════════════════════════
// COMPOSANT: Sélecteur de mode OCR
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun OcrModeSelector(
    onStartOcr: () -> Unit,
    onSkipOcr: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.CameraAlt,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = AppColors.BrandBlue
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Extraction de données",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = AppColors.TextMain,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Choisissez comment remplir le formulaire",
            fontSize = 14.sp,
            color = AppColors.TextSub,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(40.dp))

        // Option 1: OCR automatique
        OcrOptionCard(
            icon = Icons.Default.CameraAlt,
            title = "Extraction OCR",
            description = "Photographiez votre pièce d'identité\net les données seront extraites automatiquement",
            buttonText = "Commencer l'OCR",
            onClick = onStartOcr,
            isPrimary = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Option 2: Saisie manuelle
        OcrOptionCard(
            icon = Icons.Default.Edit,
            title = "Saisie manuelle",
            description = "Remplissez les informations\nmanuellement dans le formulaire",
            buttonText = "Saisie manuelle",
            onClick = onSkipOcr,
            isPrimary = false
        )
    }
}

@Composable
fun OcrOptionCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    buttonText: String,
    onClick: () -> Unit,
    isPrimary: Boolean = false
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = false) { },
        shape = AppShapes.MediumRadius,
        colors = CardDefaults.cardColors(
            containerColor = if (isPrimary) AppColors.BrandBlue.copy(0.05f) else AppColors.SurfaceBackground
        ),
        border = BorderStroke(
            1.dp,
            if (isPrimary) AppColors.BrandBlue else AppColors.BorderColor
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = if (isPrimary) AppColors.BrandBlue else AppColors.TextSub
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.TextMain
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = description,
                fontSize = 13.sp,
                color = AppColors.TextSub,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                shape = AppShapes.MediumRadius,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isPrimary) AppColors.BrandBlue else AppColors.SurfaceAlt
                )
            ) {
                Text(
                    text = buttonText,
                    color = if (isPrimary) Color.White else AppColors.TextMain,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// COMPOSANT: Capture d'image OCR (avec permission caméra + prévisualisation)
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun OcrImageCapture(
    title: String,
    subtitle: String,
    imageFileName: String,
    stepNumber: Int,
    totalSteps: Int,
    onImageSelected: (Uri) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
    isProcessing: Boolean = false
) {
    val context = LocalContext.current
    var tempUri by remember { mutableStateOf<Uri?>(null) }
    var capturedImageUri by remember { mutableStateOf<Uri?>(null) }
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                    PackageManager.PERMISSION_GRANTED
        )
    }

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
        if (granted && tempUri != null) {
            // Permission just granted, we can't auto-launch here but the user can tap again
        }
    }

    // Caméra
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            tempUri?.let { uri ->
                capturedImageUri = uri
                onImageSelected(uri)
            }
        }
    }

    // Galerie
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            capturedImageUri = it
            onImageSelected(it)
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ── Header avec bouton retour et indicateur d'étape ──
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = onCancel,
                modifier = Modifier
                    .size(40.dp)
                    .background(AppColors.SurfaceAlt, CircleShape)
            ) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "Retour",
                    tint = AppColors.TextMain,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Step indicator
            Surface(
                color = AppColors.BrandBlue.copy(0.1f),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text(
                    text = "Étape $stepNumber/$totalSteps",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.BrandBlue
                )
            }

            // Spacer pour équilibrer la row
            Spacer(modifier = Modifier.size(40.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ── Titre ──
        Text(
            text = title,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = AppColors.TextMain
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = subtitle,
            fontSize = 14.sp,
            color = AppColors.TextSub,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(28.dp))

        // ── Zone de capture / prévisualisation ──
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = AppColors.SurfaceBackground),
            border = BorderStroke(
                2.dp,
                if (capturedImageUri != null) AppColors.StatusGreen
                else AppColors.BrandBlue.copy(0.3f)
            )
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                when {
                    isProcessing -> {
                        // État: traitement en cours
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(
                                color = AppColors.BrandBlue,
                                modifier = Modifier.size(48.dp),
                                strokeWidth = 3.dp
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "Analyse en cours…",
                                color = AppColors.TextSub,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    capturedImageUri != null -> {
                        // État: image capturée → prévisualisation
                        Box(modifier = Modifier.fillMaxSize()) {
                            SubcomposeAsyncImage(
                                model = capturedImageUri,
                                contentDescription = "Image capturée",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(20.dp)),
                                contentScale = ContentScale.Crop,
                                loading = {
                                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(24.dp),
                                            strokeWidth = 2.dp,
                                            color = AppColors.BrandBlue
                                        )
                                    }
                                }
                            )

                            // Badge de succès
                            Surface(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(12.dp),
                                color = AppColors.StatusGreen,
                                shape = CircleShape
                            ) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier
                                        .size(28.dp)
                                        .padding(4.dp)
                                )
                            }

                            // Overlay pour reprendre la photo
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(Color.Transparent, Color.Black.copy(0.5f))
                                        )
                                    ),
                                contentAlignment = Alignment.BottomCenter
                            ) {
                                Text(
                                    text = "✓ Photo capturée",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    modifier = Modifier.padding(bottom = 16.dp)
                                )
                            }
                        }
                    }

                    else -> {
                        // État: vide → invite à capturer
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .background(
                                        AppColors.BrandBlue.copy(0.08f),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = null,
                                    modifier = Modifier.size(40.dp),
                                    tint = AppColors.BrandBlue.copy(0.6f)
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "Prenez une photo ou\nchoisissez une image",
                                color = AppColors.TextSub,
                                textAlign = TextAlign.Center,
                                fontSize = 14.sp,
                                lineHeight = 20.sp
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ── Boutons d'action ──
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Bouton Galerie
            OutlinedButton(
                onClick = { galleryLauncher.launch("image/*") },
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                enabled = !isProcessing,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.5.dp, AppColors.BrandBlue.copy(0.3f)),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = AppColors.BrandBlue
                )
            ) {
                Icon(
                    Icons.Default.Image,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Galerie", fontWeight = FontWeight.SemiBold)
            }

            // Bouton Appareil photo
            Button(
                onClick = {
                    if (hasCameraPermission) {
                        try {
                            // Utilise cacheDir (interne, jamais null)
                            val cacheDir = context.cacheDir
                            val file = File(cacheDir, imageFileName)
                            file.parentFile?.mkdirs()

                            val uri = FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.fileprovider",
                                file
                            )
                            tempUri = uri
                            cameraLauncher.launch(uri)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    } else {
                        // Demander la permission
                        permissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                enabled = !isProcessing,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.BrandBlue
                )
            ) {
                Icon(
                    Icons.Default.CameraAlt,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Appareil", fontWeight = FontWeight.SemiBold)
            }
        }

        // Message d'aide
        Spacer(modifier = Modifier.height(16.dp))

        Surface(
            color = AppColors.BrandBlue.copy(0.05f),
            shape = RoundedCornerShape(10.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Outlined.Info,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = AppColors.BrandBlue
                )
                Text(
                    text = "Assurez-vous que le document est bien éclairé et lisible",
                    fontSize = 12.sp,
                    color = AppColors.TextSub,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// COMPOSANT: Révision des résultats OCR
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun OcrResultsReview(
    rectoResult: com.example.sencsu.domain.viewmodel.OcrExtractionResult?,
    versoResult: com.example.sencsu.domain.viewmodel.OcrExtractionResult?,
    onFieldChange: (field: String, value: String, isRecto: Boolean) -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        // Header
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(AppColors.StatusGreen.copy(0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = AppColors.StatusGreen,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Vérifier les informations",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.TextMain
                )
                Text(
                    text = "Corrigez les champs si nécessaire",
                    fontSize = 13.sp,
                    color = AppColors.TextSub
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Résultats du recto
        if (rectoResult != null) {
            ResultsCard(
                title = "Informations personnelles (Recto)",
                result = rectoResult,
                fields = listOf("prenoms", "nom", "dateNaissance", "sexe", "lieuNaissance", "departement", "commune"),
                onFieldChange = { field, value -> onFieldChange(field, value, true) }
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Résultats du verso
        if (versoResult != null) {
            ResultsCard(
                title = "Informations d'identification (Verso)",
                result = versoResult,
                fields = listOf("nin"),
                onFieldChange = { field, value -> onFieldChange(field, value, false) }
            )
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Bouton de confirmation
        Button(
            onClick = onConfirm,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            enabled = !isLoading && rectoResult != null,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AppColors.BrandBlue)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Confirmer et continuer", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun ResultsCard(
    title: String,
    result: com.example.sencsu.domain.viewmodel.OcrExtractionResult,
    fields: List<String>,
    onFieldChange: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.SurfaceBackground),
        border = BorderStroke(1.dp, AppColors.BorderColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = if (result.isValid) Icons.Default.CheckCircle else Icons.Default.Warning,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = if (result.isValid) AppColors.StatusGreen else AppColors.StatusRed
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.TextMain
                )
            }

            if (result.errors.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = AppColors.StatusRed.copy(0.08f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        result.errors.forEach { error ->
                            Text(
                                text = "⚠ $error",
                                fontSize = 12.sp,
                                color = AppColors.StatusRed
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Champs éditables
            fields.forEach { field ->
                val value = when (field) {
                    "prenoms" -> result.prenoms
                    "nom" -> result.nom
                    "dateNaissance" -> result.dateNaissance
                    "sexe" -> result.sexe
                    "nin" -> result.nin
                    "lieuNaissance" -> result.lieuNaissance
                    "departement" -> result.departement
                    "commune" -> result.commune
                    else -> ""
                }

                val label = when (field) {
                    "prenoms" -> "Prénoms"
                    "nom" -> "Nom"
                    "dateNaissance" -> "Date de naissance"
                    "sexe" -> "Sexe"
                    "nin" -> "NIN"
                    "lieuNaissance" -> "Lieu de naissance"
                    "departement" -> "Département"
                    "commune" -> "Commune"
                    else -> field
                }

                OcrEditableField(
                    label = label,
                    value = value,
                    onChange = { newValue -> onFieldChange(field, newValue) }
                )

                if (field != fields.last()) {
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
fun OcrEditableField(
    label: String,
    value: String,
    onChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = AppColors.TextSub,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        OutlinedTextField(
            value = value,
            onValueChange = onChange,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedIndicatorColor = AppColors.BrandBlue,
                unfocusedIndicatorColor = AppColors.BorderColor
            ),
            singleLine = true,
            textStyle = LocalTextStyle.current.copy(fontSize = 13.sp)
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// COMPOSANT: Flux principal OCR (orchestration des étapes)
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun OcrProcessingFlow(
    viewModel: OcrViewModel = hiltViewModel(),
    onComplete: (Map<String, String>) -> Unit,
    onCancel: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize()) {
        // Barre de progression en haut
        if (state.currentStep != OcrStep.SELECT_MODE && state.currentStep != OcrStep.COMPLETED) {
            OcrStepProgressBar(currentStep = state.currentStep)
        }

        when (state.currentStep) {
            OcrStep.SELECT_MODE -> {
                OcrModeSelector(
                    onStartOcr = { viewModel.startOcrMode() },
                    onSkipOcr = { onCancel() }
                )
            }

            OcrStep.CAPTURE_RECTO -> {
                OcrImageCapture(
                    title = "Photographiez le RECTO",
                    subtitle = "Face avant de votre pièce d'identité",
                    imageFileName = "ocr_recto_${System.currentTimeMillis()}.jpg",
                    stepNumber = 1,
                    totalSteps = 2,
                    onImageSelected = { uri ->
                        viewModel.setRectoUri(uri)
                        viewModel.processRectoImage(context, uri)
                        viewModel.goToVersoCapture()
                    },
                    onCancel = {
                        onCancel()
                    },
                    isProcessing = state.isProcessing
                )
            }

            OcrStep.CAPTURE_VERSO -> {
                OcrImageCapture(
                    title = "Photographiez le VERSO",
                    subtitle = "Face arrière de votre pièce d'identité",
                    imageFileName = "ocr_verso_${System.currentTimeMillis()}.jpg",
                    stepNumber = 2,
                    totalSteps = 2,
                    onImageSelected = { uri ->
                        viewModel.setVersoUri(uri)
                        viewModel.processVersoImage(context, uri)
                        viewModel.goToReviewResults()
                    },
                    onCancel = {
                        viewModel.goBackToRecto()
                    },
                    isProcessing = state.isProcessing
                )
            }

            OcrStep.REVIEW_RESULTS -> {
                OcrResultsReview(
                    rectoResult = state.rectoResult,
                    versoResult = state.versoResult,
                    onFieldChange = { field, value, isRecto ->
                        if (isRecto) {
                            viewModel.updateRectoField(field, value)
                        } else {
                            viewModel.updateVersoField(field, value)
                        }
                    },
                    onConfirm = {
                        viewModel.completeOcr()
                        onComplete(viewModel.getExtractedDataForForm())
                    },
                    isLoading = state.isLoading
                )
            }

            OcrStep.COMPLETED -> {
                LaunchedEffect(Unit) {
                    onComplete(viewModel.getExtractedDataForForm())
                }
            }
        }
    }

    // Affichage des erreurs via Snackbar
    if (state.error != null) {
        val snackbarHostState = remember { SnackbarHostState() }
        LaunchedEffect(state.error) {
            state.error?.let { snackbarHostState.showSnackbar(it) }
        }
        SnackbarHost(hostState = snackbarHostState)
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// COMPOSANT: Indicateur de progression des étapes OCR
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
private fun OcrStepProgressBar(
    currentStep: OcrStep,
    modifier: Modifier = Modifier
) {
    val steps = listOf(
        "Recto" to OcrStep.CAPTURE_RECTO,
        "Verso" to OcrStep.CAPTURE_VERSO,
        "Résultats" to OcrStep.REVIEW_RESULTS
    )

    val currentIndex = steps.indexOfFirst { it.second == currentStep }.coerceAtLeast(0)

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = AppColors.SurfaceBackground,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            steps.forEachIndexed { index, (label, _) ->
                val isActive = index <= currentIndex
                val isCurrent = index == currentIndex

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(
                                color = when {
                                    isCurrent -> AppColors.BrandBlue
                                    isActive -> AppColors.StatusGreen
                                    else -> AppColors.BorderColor
                                },
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isActive && !isCurrent) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        } else {
                            Text(
                                text = "${index + 1}",
                                color = if (isActive) Color.White else AppColors.TextSub,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = label,
                        fontSize = 11.sp,
                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                        color = if (isActive) AppColors.TextMain else AppColors.TextSub
                    )
                }

                // Connecteur entre les étapes
                if (index < steps.size - 1) {
                    Box(
                        modifier = Modifier
                            .weight(0.5f)
                            .height(2.dp)
                            .background(
                                if (index < currentIndex) AppColors.StatusGreen
                                else AppColors.BorderColor
                            )
                    )
                }
            }
        }
    }
}