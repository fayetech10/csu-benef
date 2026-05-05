package com.example.sencsu.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import com.example.sencsu.configs.ApiConfig

@Composable
fun QrCodeImage(
    value: String,
    modifier: Modifier = Modifier,
    size: Int = 512
) {
    val bitmap = remember(value, size) { generateQrBitmap(value, size) }
    Image(
        bitmap = bitmap.asImageBitmap(),
        contentDescription = "QR Code",
        modifier = modifier,
        contentScale = ContentScale.Fit
    )
}

/**
 * Construit l'URL dynamique pour le QR Code en utilisant un schéma personnalisé.
 * Ce schéma (sencsu://) permet d'ouvrir l'application peu importe l'adresse IP du serveur.
 * ex: sencsu://adherent?matricule=XXXXX
 */
fun buildBeneficiaryQrUrl(matricule: String): String {
    return "sencsu://adherent?matricule=$matricule"
}

private fun generateQrBitmap(value: String, size: Int): Bitmap {
    val matrix = QRCodeWriter().encode(value, BarcodeFormat.QR_CODE, size, size)
    val pixels = IntArray(size * size)
    for (y in 0 until size) {
        val offset = y * size
        for (x in 0 until size) {
            pixels[offset + x] = if (matrix[x, y]) 0xFF000000.toInt() else 0xFFFFFFFF.toInt()
        }
    }
    return Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888).apply {
        setPixels(pixels, 0, size, 0, 0, size, size)
    }
}
