package com.example.sencsu.screen

import android.util.Log
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.example.sencsu.theme.AppShapes
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

@Composable
fun QrScannerScreen(
    onDismiss: () -> Unit,
    viewModel: ScannerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Box(modifier = Modifier.fillMaxSize()) {
        // Scanner View
        if (uiState.scannedAdherent == null) {
            CameraPreview(
                onBarcodeDetected = { barcodes ->
                    barcodes.firstOrNull()?.rawValue?.let { value ->
                        viewModel.onScanResult(value)
                    }
                }
            )
            
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
                CircularProgressIndicator(color = Color.White)
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

        // Result Dialog
        if (uiState.scannedAdherent != null) {
            AdherentResultDialog(
                adherent = uiState.scannedAdherent!!,
                onDismiss = { viewModel.reset() },
                onConfirm = { 
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

@Composable
private fun AdherentResultDialog(
    adherent: AdherentDto,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Rounded.CheckCircle, 
                    contentDescription = null, 
                    tint = AppColors.StatusGreen
                )
                Spacer(Modifier.width(8.dp))
                Text("Adhérent Identifié")
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    "${adherent.prenoms ?: ""} ${adherent.nom ?: ""}".trim(), 
                    fontWeight = FontWeight.Bold, 
                    fontSize = 18.sp,
                    color = AppColors.TextMain
                )
                Text("Matricule: ${adherent.matricule ?: "N/A"}", color = AppColors.TextSub)
                Text("CNI: ${adherent.numeroCNi ?: "N/A"}", color = AppColors.TextSub)
                
                Spacer(Modifier.height(12.dp))
                
                Surface(
                    color = if (adherent.actif == true) AppColors.StatusGreen.copy(alpha = 0.1f) else AppColors.StatusRed.copy(alpha = 0.1f),
                    shape = AppShapes.MediumRadius
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(if (adherent.actif == true) AppColors.StatusGreen else AppColors.StatusRed, CircleShape)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            if (adherent.actif == true) "STATUT ACTIF" else "STATUT INACTIF",
                            color = if (adherent.actif == true) AppColors.StatusGreen else AppColors.StatusRed,
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp
                        )
                    }
                }
                
                if (adherent.coveragePeriod != null) {
                    val progress = try {
                        val created = java.time.LocalDateTime.parse(adherent.createdAt)
                        val now = java.time.LocalDateTime.now()
                        val end = created.plusYears(1)
                        val totalDays = java.time.temporal.ChronoUnit.DAYS.between(created, end).toFloat()
                        val elapsedDays = java.time.temporal.ChronoUnit.DAYS.between(created, now).toFloat()
                        (elapsedDays / totalDays).coerceIn(0f, 1f)
                    } catch (e: Exception) { 0.1f }

                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Validité: ${adherent.coveragePeriod}",
                            fontSize = 13.sp,
                            color = AppColors.TextMain,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            "${(progress * 100).toInt()}%",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.BrandBlue
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth().height(6.dp),
                        color = AppColors.BrandBlue,
                        trackColor = AppColors.BrandBlue.copy(alpha = 0.1f),
                        strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = AppColors.BrandBlue)
            ) {
                Text("Terminer")
            }
        }
    )
}
