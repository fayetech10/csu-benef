package com.example.sencsu.components

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import com.example.sencsu.configs.ApiConfig
import com.example.sencsu.data.remote.dto.AdherentDto
import com.example.sencsu.theme.AppColors
import com.example.sencsu.theme.AppShapes

/**
 * Carte de membre virtuelle premium.
 * Ressemble à une vraie carte d'assurance santé avec
 * gradient, QR code intégré, et informations personnelles.
 */
@Composable
fun MembershipCard(
    adherent: AdherentDto,
    modifier: Modifier = Modifier
) {
    val name = "${adherent.prenoms ?: ""} ${adherent.nom ?: ""}".trim().ifEmpty { "—" }
    val matricule = adherent.matricule ?: "—"
    val regime = adherent.regime ?: "CONTRIBUTIF"
    val isActive = adherent.actif != false

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(210.dp),
        shape = AppShapes.LargeRadius,
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        listOf(
                            AppColors.CardGradientStart,
                            AppColors.CardGradientEnd
                        )
                    )
                )
        ) {
            // ── Motif décoratif ──
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .offset(x = 220.dp, y = (-40).dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.05f))
            )
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .offset(x = 280.dp, y = 100.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.03f))
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // ── HAUT : Logo + Type ──
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Rounded.HealthAndSafety,
                            contentDescription = null,
                            tint = AppColors.GoldAccent,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text(
                                "SenCSU",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp
                            )
                            Text(
                                "Carte de Membre",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 9.sp,
                                letterSpacing = 1.sp
                            )
                        }
                    }

                    // Badge statut
                    Surface(
                        shape = AppShapes.CircleRadius,
                        color = if (isActive) AppColors.StatusGreen.copy(alpha = 0.2f)
                        else AppColors.StatusRed.copy(alpha = 0.2f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(if (isActive) AppColors.StatusGreen else AppColors.StatusRed)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                if (isActive) "ACTIF" else "INACTIF",
                                color = if (isActive) AppColors.StatusGreen else AppColors.StatusRed,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }

                // ── MILIEU : Nom ──
                Column {
                    Text(
                        name.uppercase(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        regime.uppercase(),
                        color = AppColors.GoldAccent,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 1.sp
                    )
                }

                // ── BAS : Matricule + QR Code ──
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            "MATRICULE",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        )
                        Text(
                            matricule,
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 2.sp
                        )
                    }

                    // QR Code intégré
                    Surface(
                        modifier = Modifier.size(60.dp),
                        shape = AppShapes.SmallRadius,
                        color = Color.White
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            SubcomposeAsyncImage(
                                model = ApiConfig.getQrCodeUrl(matricule),
                                contentDescription = "QR Code Membre",
                                modifier = Modifier.padding(4.dp),
                                loading = {
                                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            strokeWidth = 1.5.dp,
                                            color = AppColors.BrandBlue
                                        )
                                    }
                                },
                                error = {
                                    Icon(
                                        Icons.Rounded.QrCode,
                                        null,
                                        tint = AppColors.TextSub.copy(0.3f),
                                        modifier = Modifier.size(30.dp)
                                    )
                                }
                            )
                        }
                    }
                }
            }

            // ── Barre dorée en bas ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                AppColors.GoldAccent.copy(alpha = 0.3f),
                                AppColors.GoldAccent,
                                AppColors.GoldAccent.copy(alpha = 0.3f)
                            )
                        )
                    )
            )
        }
    }
}
