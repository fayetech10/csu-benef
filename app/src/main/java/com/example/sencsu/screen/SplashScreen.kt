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
                        Color(0xFF0F2027),
                        Color(0xFF203A43),
                        Color(0xFF2C5364)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // ── Motifs décoratifs subtils ──
        Box(
            modifier = Modifier
                .size(500.dp)
                .offset(x = (-150).dp, y = (-250).dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.02f))
        )
        
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            // ── Logo Central ──
            androidx.compose.foundation.Image(
                painter = androidx.compose.ui.res.painterResource(id = com.example.sencsu.R.drawable.logo_sencsu),
                contentDescription = "Logo SenCSU",
                modifier = Modifier
                    .size(240.dp)
                    .scale(logoScale)
                    .alpha(if (startAnimation) 1f else 0f),
                contentScale = androidx.compose.ui.layout.ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ── Textes ──
            AnimatedVisibility(
                visible = startAnimation,
                enter = fadeIn(tween(1000, delayMillis = 500)) + slideInVertically(
                    tween(1000, delayMillis = 500), initialOffsetY = { 20 }
                )
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "SÉNÉGAL",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.6f),
                        letterSpacing = 6.sp
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "SenCSU",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = 2.sp
                    )
                    Text(
                        "L'assurance pour tous",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.8f),
                        fontWeight = FontWeight.Light,
                        letterSpacing = 1.sp
                    )
                }
            }
        }

        // ── Footer ──
        Text(
            "MINISTÈRE DE LA SANTÉ ET DE L'ACTION SOCIALE",
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp)
                .alpha(if (startAnimation) 0.5f else 0f),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            letterSpacing = 2.sp,
            textAlign = TextAlign.Center
        )
    }
}