package com.example.sencsu.screen.enrolement

import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.sencsu.theme.AppColors
import com.example.sencsu.theme.AppShapes

@Composable
fun OcrScanStep(
    rectoUri: Uri?,
    versoUri: Uri?,
    isProcessing: Boolean,
    rectoResult: com.example.sencsu.domain.viewmodel.OcrExtractionResult?,
    versoResult: com.example.sencsu.domain.viewmodel.OcrExtractionResult?,
    onCaptureRecto: () -> Unit,
    onGalleryRecto: () -> Unit,
    onCaptureVerso: () -> Unit,
    onGalleryVerso: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        StepTitle(
            "Étape 1 : Scanner la CNI",
            "Photographiez le recto et le verso de votre carte d'identité. Les informations seront extraites automatiquement."
        )

        // ── Info banner ──
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = AppColors.ActionBlue.copy(0.08f),
            shape = AppShapes.MediumRadius,
            border = BorderStroke(1.dp, AppColors.ActionBlue.copy(0.15f))
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.AutoAwesome, null, tint = AppColors.ActionBlue)
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("Extraction automatique (OCR)", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = AppColors.ActionBlue)
                    Text("Les données seront pré-remplies à l'étape suivante", fontSize = 12.sp, color = AppColors.TextSub)
                }
            }
        }

        // ── Recto ──
        CniCaptureCard(
            title = "Carte d'Identité — Recto",
            description = "Face avec la photo et le nom",
            imageUri = rectoUri,
            isProcessing = isProcessing && rectoUri != null && rectoResult == null,
            result = rectoResult,
            onCamera = onCaptureRecto,
            onGallery = onGalleryRecto
        )

        // ── Verso ──
        CniCaptureCard(
            title = "Carte d'Identité — Verso",
            description = "Face avec le NIN (numéro d'identification)",
            imageUri = versoUri,
            isProcessing = isProcessing && versoUri != null && versoResult == null,
            result = versoResult,
            onCamera = onCaptureVerso,
            onGallery = onGalleryVerso
        )

        // ── Skip option ──
        TextButton(
            onClick = { /* Just go to next step, fields will be empty */ },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("Passer et saisir manuellement →", color = AppColors.TextSub, fontSize = 13.sp)
        }
    }
}

@Composable
fun CniCaptureCard(
    title: String,
    description: String,
    imageUri: Uri?,
    isProcessing: Boolean,
    result: com.example.sencsu.domain.viewmodel.OcrExtractionResult?,
    onCamera: () -> Unit,
    onGallery: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = AppShapes.MediumRadius,
        color = Color.White,
        border = BorderStroke(
            1.dp,
            when {
                result?.isValid == true -> AppColors.StatusGreen
                result != null -> AppColors.StatusOrange
                else -> AppColors.BorderColor
            }
        )
    ) {
        Column {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Rounded.Badge, null,
                    tint = if (result?.isValid == true) AppColors.StatusGreen else AppColors.ActionBlue
                )
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text(description, color = AppColors.TextSub, fontSize = 12.sp)
                }
                if (result?.isValid == true) {
                    Surface(color = AppColors.StatusGreen.copy(0.1f), shape = CircleShape) {
                        Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
                            Icon(Icons.Rounded.Check, null, tint = AppColors.StatusGreen, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("OK", color = AppColors.StatusGreen, fontSize = 11.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }

            // Image preview
            if (imageUri != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .padding(horizontal = 16.dp)
                        .clip(AppShapes.SmallRadius)
                ) {
                    AsyncImage(
                        model = imageUri,
                        contentDescription = title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    if (isProcessing) {
                        Box(
                            Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(0.4f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(32.dp), strokeWidth = 3.dp)
                                Spacer(Modifier.height(8.dp))
                                Text("Analyse en cours…", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
            }

            // Status message
            if (result != null && !isProcessing) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    color = if (result.isValid) AppColors.StatusGreen.copy(0.05f) else AppColors.StatusOrange.copy(0.05f),
                    shape = AppShapes.SmallRadius
                ) {
                    Row(modifier = Modifier.padding(12.dp)) {
                        Icon(
                            if (result.isValid) Icons.Rounded.CheckCircle else Icons.Rounded.Warning,
                            null,
                            tint = if (result.isValid) AppColors.StatusGreen else AppColors.StatusOrange,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            if (result.isValid) "Données extraites avec succès"
                            else "Extraction partielle — vérifiez à l'étape suivante",
                            fontSize = 12.sp,
                            color = if (result.isValid) AppColors.StatusGreen else AppColors.StatusOrange
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
            }

            // Capture buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onCamera,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    shape = AppShapes.MediumRadius,
                    border = BorderStroke(1.dp, AppColors.BrandBlue.copy(0.4f))
                ) {
                    Icon(Icons.Rounded.CameraAlt, null, modifier = Modifier.size(18.dp), tint = AppColors.BrandBlue)
                    Spacer(Modifier.width(6.dp))
                    Text("Photo", fontSize = 13.sp, color = AppColors.BrandBlue)
                }
                OutlinedButton(
                    onClick = onGallery,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    shape = AppShapes.MediumRadius,
                    border = BorderStroke(1.dp, AppColors.BorderColor)
                ) {
                    Icon(Icons.Rounded.PhotoLibrary, null, modifier = Modifier.size(18.dp), tint = AppColors.TextSub)
                    Spacer(Modifier.width(6.dp))
                    Text("Galerie", fontSize = 13.sp, color = AppColors.TextSub)
                }
            }
        }
    }
}
