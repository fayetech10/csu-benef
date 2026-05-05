package com.example.sencsu.screen

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.HealthAndSafety
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.sencsu.domain.viewmodel.SplashViewModel
import com.example.sencsu.theme.AppColors
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToDashboard: (String) -> Unit,
    viewModel: SplashViewModel = hiltViewModel()
) {
    var startAnimation by remember { mutableStateOf(false) }
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val userRole by viewModel.userRole.collectAsState()

    // Animations
    val logoScale by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.5f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 200f),
        label = "logoScale"
    )
    val ringRotation by animateFloatAsState(
        targetValue = if (startAnimation) 360f else 0f,
        animationSpec = tween(2000, easing = FastOutSlowInEasing),
        label = "ringRotation"
    )

    LaunchedEffect(Unit) {
        startAnimation = true
    }

    LaunchedEffect(isLoggedIn, userRole) {
        if (isLoggedIn != null) {
            delay(2200)
            if (isLoggedIn == true) {
                onNavigateToDashboard(userRole ?: "AGENT")
            } else {
                onNavigateToLogin()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        AppColors.BrandBlueDark,
                        AppColors.BrandBlue,
                        Color(0xFF0D4A33)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // ── Motifs décoratifs ──
        Box(
            modifier = Modifier
                .size(300.dp)
                .offset(x = (-100).dp, y = (-200).dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.03f))
        )
        Box(
            modifier = Modifier
                .size(200.dp)
                .offset(x = 120.dp, y = 250.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.02f))
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            // ── Logo avec anneau sénégalais ──
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .scale(logoScale),
                contentAlignment = Alignment.Center
            ) {
                // Anneau externe aux couleurs du drapeau
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .rotate(ringRotation)
                        .clip(CircleShape)
                        .background(
                            Brush.sweepGradient(
                                listOf(
                                    AppColors.SenegalGreen,
                                    AppColors.GoldAccent,
                                    AppColors.SenegalRed,
                                    AppColors.SenegalGreen
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    // Fond blanc intérieur
                    Surface(
                        modifier = Modifier.size(120.dp),
                        shape = CircleShape,
                        color = Color.White,
                        shadowElevation = 20.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Rounded.HealthAndSafety,
                                contentDescription = null,
                                modifier = Modifier.size(60.dp),
                                tint = AppColors.BrandBlue
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            // ── Textes ──
            AnimatedVisibility(
                visible = startAnimation,
                enter = fadeIn(tween(800, delayMillis = 400)) + slideInVertically(
                    tween(800, delayMillis = 400), initialOffsetY = { 30 }
                )
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "RÉPUBLIQUE DU SÉNÉGAL",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.5f),
                        letterSpacing = 4.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "SenCSU",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Couverture Santé Universelle",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Light,
                        letterSpacing = 2.sp
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Ligne dorée décorative
            AnimatedVisibility(
                visible = startAnimation,
                enter = fadeIn(tween(600, delayMillis = 800)) + expandHorizontally(
                    tween(600, delayMillis = 800)
                )
            ) {
                Box(
                    modifier = Modifier
                        .width(60.dp)
                        .height(3.dp)
                        .background(AppColors.GoldAccent, CircleShape)
                )
            }
        }

        // ── Barre de progression ──
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 64.dp)
                .width(160.dp)
                .height(3.dp)
                .background(Color.White.copy(0.15f), CircleShape)
        ) {
            val progress = remember { Animatable(0f) }
            LaunchedEffect(Unit) {
                progress.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(2000, easing = FastOutSlowInEasing)
                )
            }
            Box(
                Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress.value)
                    .background(
                        Brush.horizontalGradient(
                            listOf(AppColors.GoldAccent, Color.White)
                        ),
                        CircleShape
                    )
            )
        }

        // ── Footer ──
        Text(
            "Ministère de la Santé",
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
                .alpha(if (startAnimation) 0.4f else 0f),
            fontSize = 11.sp,
            color = Color.White,
            letterSpacing = 1.sp
        )
    }
}