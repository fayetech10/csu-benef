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
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.sencsu.R
import com.example.sencsu.domain.viewmodel.SplashViewModel
import kotlinx.coroutines.delay
import kotlin.math.sin
import kotlin.math.cos
import kotlin.math.atan2
import kotlin.random.Random

@Composable
fun SplashScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToDashboard: (String) -> Unit,
    viewModel: SplashViewModel = hiltViewModel()
) {
    var startAnimation by remember { mutableStateOf(false) }
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val userRole by viewModel.userRole.collectAsState()

    // Animations plus poussées
    val logoScale by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.3f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "logoScale"
    )

    // Rotation continue de l'anneau (infinie tant que splash est actif)
    val infiniteRotation = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        infiniteRotation.animateTo(
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(3000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            )
        )
    }

    // Dégradé de fond animé (couleurs qui se déplacent)
    val gradientOffset by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "gradientOffset"
    )

    // Particules flottantes
    val particles = remember {
        List(12) { index ->
            Particle(
                startX = Random.nextFloat(),
                startY = Random.nextFloat(),
                size = Random.nextInt(4, 12).dp,
                speed = Random.nextFloat() * 0.5f + 0.2f,
                angle = Random.nextFloat() * 360f
            )
        }
    }

    LaunchedEffect(Unit) {
        startAnimation = true
    }

    // Navigation après délai
    LaunchedEffect(isLoggedIn, userRole) {
        if (isLoggedIn != null) {
            delay(2500) // légèrement prolongé pour profiter des animations
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
                        Color(0xFF0A1C2A),
                        Color(0xFF1A3B4D),
                        Color(0xFF2C5A6E)
                    )
                )
            )
    ) {
        // ── Background Image (Full Screen Watermark) ──
        androidx.compose.foundation.Image(
            painter = painterResource(id = R.drawable.logo_sencsu),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .scale(2.2f)
                .alpha(0.04f)
                .rotate(-15f),
            contentScale = ContentScale.Fit
        )

        // Dégradé animé en overlay (ondulations)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.05f * (0.5f + 0.5f * sin(gradientOffset * Math.PI.toFloat()))),
                            Color.Transparent,
                            Color.White.copy(alpha = 0.05f * (0.5f + 0.5f * cos(gradientOffset * Math.PI.toFloat())))
                        )
                    )
                )
        )

        // Particules flottantes
        particles.forEach { particle ->
            key(particle) {
                val particleX by animateFloatAsState(
                    targetValue = (particle.startX + 0.2f * sin(gradientOffset * particle.speed)) % 1f,
                    animationSpec = tween(5000),
                    label = "particleX"
                )
                val particleY by animateFloatAsState(
                    targetValue = (particle.startY + 0.03f * gradientOffset * particle.speed) % 1f,
                    animationSpec = tween(5000),
                    label = "particleY"
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .offset(
                            x = (particleX * 400).dp - 200.dp,
                            y = (particleY * 800).dp - 400.dp
                        )
                        .size(particle.size)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.1f + 0.05f * particle.speed))
                )
            }
        }

        // Contenu principal
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            // Anneau rotatif autour du logo
            Box(
                modifier = Modifier.size(280.dp),
                contentAlignment = Alignment.Center
            ) {
                // Cercle extérieur décoratif
                Box(
                    modifier = Modifier
                        .size(280.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.05f))
                )

                // Anneau plein en rotation
                androidx.compose.foundation.Canvas(
                    modifier = Modifier
                        .size(260.dp)
                        .rotate(infiniteRotation.value)
                ) {
                    drawCircle(
                        color = Color.White.copy(alpha = 0.3f),
                        radius = size.minDimension / 2,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
                    )
                    // Petit point lumineux sur l'anneau
                    val angleRad = Math.toRadians(infiniteRotation.value.toDouble())
                    val x = (size.minDimension / 2) * cos(angleRad).toFloat()
                    val y = (size.minDimension / 2) * sin(angleRad).toFloat()
                    drawCircle(
                        color = Color.White.copy(alpha = 0.8f),
                        radius = 6.dp.toPx(),
                        center = Offset(x + size.width / 2, y + size.height / 2)
                    )
                }

                // Logo
                androidx.compose.foundation.Image(
                    painter = painterResource(id = R.drawable.logo_sencsu),
                    contentDescription = "Logo SenCSU",
                    modifier = Modifier
                        .size(200.dp)
                        .scale(logoScale)
                        .alpha(if (startAnimation) 1f else 0f),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Animations des textes
            AnimatedVisibility(
                visible = startAnimation,
                enter = fadeIn(tween(800, delayMillis = 400)) +
                        slideInVertically(tween(800, delayMillis = 400)) { it / 2 }
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "RÉPUBLIQUE DU SÉNÉGAL",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.7f),
                        letterSpacing = 4.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "SenCSU",
                        fontSize = 42.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp,
                        style = MaterialTheme.typography.displayMedium.copy(
                            brush = Brush.verticalGradient(
                                listOf(Color.White, Color.White.copy(alpha = 0.6f))
                            )
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Couverture santé universelle",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Light,
                        color = Color.White.copy(alpha = 0.9f),
                        letterSpacing = 1.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Indicateur de chargement élégant (visible après un court délai)
            AnimatedVisibility(
                visible = startAnimation,
                enter = fadeIn(tween(600, delayMillis = 1200)),
                exit = fadeOut()
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(32.dp),
                    color = Color.White.copy(alpha = 0.7f),
                    strokeWidth = 2.dp
                )
            }
        }

        // Pied de page avec animation différée
        AnimatedVisibility(
            visible = startAnimation,
            enter = fadeIn(tween(800, delayMillis = 1000)) +
                    slideInVertically(tween(800, delayMillis = 1000)) { it }
        ) {
            Text(
                text = "MINISTÈRE DE LA SANTÉ ET DE L'ACTION SOCIALE",
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 48.dp)
                    .alpha(0.6f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                letterSpacing = 2.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

// Data class pour les particules
private data class Particle(
    val startX: Float,
    val startY: Float,
    val size: androidx.compose.ui.unit.Dp,
    val speed: Float,
    val angle: Float
)