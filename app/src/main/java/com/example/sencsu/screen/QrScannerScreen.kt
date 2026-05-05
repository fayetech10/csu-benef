package com.example.sencsu.screen

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Badge
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.sencsu.data.remote.dto.AdherentDto
import com.example.sencsu.domain.viewmodel.ScannerViewModel
import com.example.sencsu.theme.AppColors
import com.example.sencsu.theme.AppGradients
import com.example.sencsu.theme.AppShapes
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

@Composable
fun QrScannerScreen(
    onDismiss: () -> Unit,
    onNavigateToDetails: (adherentId: String) -> Unit = {},
    viewModel: ScannerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Gestion des permissions
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.CAMERA
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasCameraPermission = granted
        }
    )

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(android.Manifest.permission.CAMERA)
        }
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        // Scanner View
        if (uiState.scannedAdherent == null) {
            if (hasCameraPermission) {
                CameraPreview(
                    onBarcodeDetected = { barcodes ->
                        barcodes.firstOrNull()?.rawValue?.let { value ->
                            viewModel.onScanResult(value)
                        }
                    }
                )
            } else {
                // Écran en attendant la permission
                Box(
                    modifier = Modifier.fillMaxSize().background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "L'accès à la caméra est requis pour scanner.",
                        color = Color.White,
                        modifier = Modifier.padding(16.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
            
            // Scanner Overlay
            ScannerOverlay(onClose = onDismiss)
        }

        // Loading Overlay
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = Color.White)
                    Spacer(Modifier.height(12.dp))
                    Text("Identification en cours...", color = Color.White, fontSize = 14.sp)
                }
            }
        }

        // Error Dialog
        if (uiState.error != null) {
            AlertDialog(
                onDismissRequest = { viewModel.reset() },
                title = { Text("Erreur de scan") },
                text = { Text(uiState.error!!) },
                confirmButton = {
                    TextButton(onClick = { viewModel.reset() }) {
                        Text("Réessayer")
                    }
                }
            )
        }

        // Result Card - Modern overlay instead of dialog
        if (uiState.scannedAdherent != null) {
            AdherentResultOverlay(
                adherent = uiState.scannedAdherent!!,
                onDismiss = { viewModel.reset() },
                onViewDetails = {
                    val adherent = uiState.scannedAdherent!!
                    val id = adherent.id
                    if (id != null) {
                        viewModel.reset()
                        onNavigateToDetails(id)
                    }
                },
                onClose = {
                    viewModel.reset()
                    onDismiss()
                }
            )
        }
    }
}

@Composable
private fun CameraPreview(onBarcodeDetected: (List<Barcode>) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    
    val executor = remember { Executors.newSingleThreadExecutor() }
    val scanner = remember {
        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()
        BarcodeScanning.getClient(options)
    }

    AndroidView(
        factory = { ctx ->
            PreviewView(ctx).apply {
                scaleType = PreviewView.ScaleType.FILL_CENTER
            }
        },
        modifier = Modifier.fillMaxSize(),
        update = { previewView ->
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also { analysis ->
                        analysis.setAnalyzer(executor) { imageProxy ->
                            processImageProxy(scanner, imageProxy, onBarcodeDetected)
                        }
                    }

                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        imageAnalysis
                    )
                } catch (e: Exception) {
                    Log.e("CameraPreview", "Binding failed", e)
                }
            }, ContextCompat.getMainExecutor(context))
        }
    )
}

@androidx.annotation.OptIn(ExperimentalGetImage::class)
private fun processImageProxy(
    scanner: com.google.mlkit.vision.barcode.BarcodeScanner,
    imageProxy: ImageProxy,
    onBarcodeDetected: (List<Barcode>) -> Unit
) {
    val mediaImage = imageProxy.image
    if (mediaImage != null) {
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        scanner.process(image)
            .addOnSuccessListener { barcodes ->
                if (barcodes.isNotEmpty()) {
                    onBarcodeDetected(barcodes)
                }
            }
            .addOnFailureListener {
                Log.e("Scanner", "Scan failed", it)
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    } else {
        imageProxy.close()
    }
}

@Composable
private fun ScannerOverlay(onClose: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Back Button
        IconButton(
            onClick = onClose,
            modifier = Modifier
                .padding(16.dp)
                .statusBarsPadding()
                .background(Color.Black.copy(alpha = 0.3f), CircleShape)
        ) {
            Icon(Icons.Rounded.Close, contentDescription = null, tint = Color.White)
        }
        
        // Scan area indicator
        Surface(
            modifier = Modifier
                .size(240.dp)
                .align(Alignment.Center),
            color = Color.Transparent,
            shape = AppShapes.LargeRadius,
            border = androidx.compose.foundation.BorderStroke(2.dp, Color.White.copy(alpha = 0.5f))
        ) {}
        
        Text(
            "Scannez le QR Code de l'adhérent",
            color = Color.White,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp),
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
    }
}

/**
 * Overlay moderne qui s'affiche après un scan réussi.
 * Remplace le dialog basique par une carte premium avec navigation vers les détails.
 */
@Composable
private fun AdherentResultOverlay(
    adherent: AdherentDto,
    onDismiss: () -> Unit,
    onViewDetails: () -> Unit,
    onClose: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            shape = AppShapes.LargeRadius,
            colors = CardDefaults.cardColors(containerColor = AppColors.SurfaceBackground),
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
        ) {
            Column {
                // ── En-tête dégradé ──
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.horizontalGradient(AppGradients.Brand))
                        .padding(20.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.2f),
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Rounded.CheckCircle,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                        Spacer(Modifier.width(14.dp))
                        Column {
                            Text(
                                "Bénéficiaire identifié",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp
                            )
                            Text(
                                "Scan QR réussi",
                                color = Color.White.copy(alpha = 0.75f),
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                // ── Contenu ──
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Nom complet
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = AppColors.BrandBlueLite,
                            modifier = Modifier.size(52.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    "${adherent.prenoms?.firstOrNull()?.uppercase() ?: ""}${adherent.nom?.firstOrNull()?.uppercase() ?: ""}",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 18.sp,
                                    color = AppColors.BrandBlue
                                )
                            }
                        }
                        Spacer(Modifier.width(14.dp))
                        Column {
                            Text(
                                "${adherent.prenoms ?: ""} ${adherent.nom ?: ""}".trim().ifEmpty { "Inconnu" },
                                fontWeight = FontWeight.Black,
                                fontSize = 17.sp,
                                color = AppColors.TextMain
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.Badge, null, tint = AppColors.TextSub, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    adherent.matricule ?: "N/A",
                                    color = AppColors.TextSub,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = AppColors.BorderColorLight)

                    // Statut
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Statut couverture", color = AppColors.TextSub, fontSize = 13.sp)
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (adherent.actif == true) AppColors.StatusGreen.copy(alpha = 0.1f) else AppColors.StatusRed.copy(alpha = 0.1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(
                                            if (adherent.actif == true) AppColors.StatusGreen else AppColors.StatusRed,
                                            CircleShape
                                        )
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    if (adherent.actif == true) "ACTIF" else "INACTIF",
                                    color = if (adherent.actif == true) AppColors.StatusGreen else AppColors.StatusRed,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    // Période de couverture
                    if (adherent.coveragePeriod != null) {
                        InfoRow("Validité", adherent.coveragePeriod)
                    }
                    InfoRow("CNI", adherent.numeroCNi ?: "N/A")

                    Spacer(Modifier.height(4.dp))

                    // ── Boutons d'action ──
                    Button(
                        onClick = onViewDetails,
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = AppShapes.MediumRadius,
                        colors = ButtonDefaults.buttonColors(containerColor = AppColors.BrandBlue),
                        enabled = adherent.id != null
                    ) {
                        Icon(Icons.Rounded.OpenInNew, null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(10.dp))
                        Text("Voir le profil complet", fontWeight = FontWeight.Black, fontSize = 15.sp)
                    }

                    OutlinedButton(
                        onClick = onClose,
                        modifier = Modifier.fillMaxWidth().height(46.dp),
                        shape = AppShapes.MediumRadius,
                        border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.BorderColor)
                    ) {
                        Text("Fermer", color = AppColors.TextSub, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = AppColors.TextSub, fontSize = 13.sp)
        Text(value, color = AppColors.TextMain, fontWeight = FontWeight.Bold, fontSize = 13.sp)
    }
}
