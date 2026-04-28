package com.example.sencsu.screen

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.sencsu.domain.viewmodel.BeneficiaryLoginViewModel
import com.example.sencsu.theme.AppColors
import com.example.sencsu.theme.AppShapes
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BeneficiaryLoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToEnrollment: () -> Unit,
    viewModel: BeneficiaryLoginViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var passwordVisible by remember { mutableStateOf(false) }
    var showContent by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(Unit) {
        delay(150)
        showContent = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.AppBackground)
    ) {
        // ── FOND DÉCORATIF ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.45f)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            AppColors.BrandBlueDark,
                            AppColors.BrandBlue,
                            AppColors.BrandBlue.copy(alpha = 0.3f),
                            Color.Transparent
                        )
                    )
                )
        )

        // Cercle décoratif subtil
        Box(
            modifier = Modifier
                .size(200.dp)
                .offset(x = (-50).dp, y = (-30).dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.05f))
        )
        Box(
            modifier = Modifier
                .size(140.dp)
                .align(Alignment.TopEnd)
                .offset(x = 40.dp, y = 60.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.04f))
        )

        // ── CONTENU PRINCIPAL ──
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp)
                .statusBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(48.dp))

            // ── LOGO & EN-TÊTE ──
            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn(tween(600)) + scaleIn(tween(600), initialScale = 0.8f)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Logo circulaire premium
                    Surface(
                        modifier = Modifier.size(100.dp),
                        shape = CircleShape,
                        color = Color.White,
                        shadowElevation = 16.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            // Anneau aux couleurs du Sénégal
                            Box(
                                modifier = Modifier
                                    .size(90.dp)
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
                                Surface(
                                    modifier = Modifier.size(78.dp),
                                    shape = CircleShape,
                                    color = Color.White
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.Rounded.HealthAndSafety,
                                            contentDescription = null,
                                            modifier = Modifier.size(40.dp),
                                            tint = AppColors.BrandBlue
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    Text(
                        "RÉPUBLIQUE DU SÉNÉGAL",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.7f),
                        letterSpacing = 3.sp
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "SenCSU",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Text(
                        "Couverture Santé Universelle",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.8f),
                        fontWeight = FontWeight.Light,
                        letterSpacing = 1.sp
                    )
                }
            }

            Spacer(Modifier.height(40.dp))

            // ── CARTE DE CONNEXION ──
            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn(tween(500, delayMillis = 200)) + slideInVertically(
                    tween(500, delayMillis = 200),
                    initialOffsetY = { 60 }
                )
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = AppShapes.LargeRadius,
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        // Titre
                        Column {
                            Text(
                                "Espace Bénéficiaire",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = AppColors.TextMain
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "Connectez-vous pour consulter vos droits",
                                style = MaterialTheme.typography.bodySmall,
                                color = AppColors.TextSub
                            )
                        }

                        // Champ Matricule / NIN
                        OutlinedTextField(
                            value = uiState.matricule,
                            onValueChange = { viewModel.onMatriculeChange(it) },
                            label = { Text("Numéro matricule ou NIN") },
                            placeholder = { Text("Ex: PLVL02AQ ou 2619196203249", color = AppColors.TextDisabled) },
                            leadingIcon = {
                                Icon(Icons.Rounded.Badge, null, tint = AppColors.BrandBlue, modifier = Modifier.size(20.dp))
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = AppShapes.MediumRadius,
                            singleLine = true,
                            isError = uiState.error != null,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Down) }
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AppColors.BrandBlue,
                                unfocusedBorderColor = AppColors.BorderColor,
                                focusedLabelColor = AppColors.BrandBlue,
                                cursorColor = AppColors.BrandBlue
                            )
                        )

                        // Champ Mot de passe
                        OutlinedTextField(
                            value = uiState.password,
                            onValueChange = { viewModel.onPasswordChange(it) },
                            label = { Text("Mot de passe") },
                            placeholder = { Text("Votre mot de passe", color = AppColors.TextDisabled) },
                            leadingIcon = {
                                Icon(Icons.Rounded.Lock, null, tint = AppColors.BrandBlue, modifier = Modifier.size(20.dp))
                            },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        if (passwordVisible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                        contentDescription = if (passwordVisible) "Masquer" else "Afficher",
                                        tint = AppColors.TextSub
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = AppShapes.MediumRadius,
                            singleLine = true,
                            isError = uiState.error != null,
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    focusManager.clearFocus()
                                    viewModel.login(onLoginSuccess)
                                }
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AppColors.BrandBlue,
                                unfocusedBorderColor = AppColors.BorderColor,
                                focusedLabelColor = AppColors.BrandBlue,
                                cursorColor = AppColors.BrandBlue
                            )
                        )

                        // Message d'erreur
                        AnimatedVisibility(visible = uiState.error != null) {
                            Surface(
                                color = AppColors.StatusRed.copy(alpha = 0.08f),
                                shape = AppShapes.SmallRadius
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Rounded.ErrorOutline, null,
                                        tint = AppColors.StatusRed,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        uiState.error ?: "",
                                        color = AppColors.StatusRed,
                                        fontSize = 13.sp,
                                        lineHeight = 18.sp
                                    )
                                }
                            }
                        }

                        // Bouton de connexion avec gradient
                        Button(
                            onClick = {
                                focusManager.clearFocus()
                                viewModel.login(onLoginSuccess)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                            shape = AppShapes.MediumRadius,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent,
                                disabledContainerColor = AppColors.BorderColor
                            ),
                            contentPadding = PaddingValues(),
                            enabled = uiState.matricule.isNotBlank() && uiState.password.isNotBlank() && !uiState.isLoading
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(AppColors.BrandBlue, AppColors.BrandBlueDark)
                                        ),
                                        shape = AppShapes.MediumRadius
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (uiState.isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = Color.White,
                                        strokeWidth = 2.5.dp
                                    )
                                } else {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            "Se connecter",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = Color.White
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Icon(
                                            Icons.Rounded.ArrowForward,
                                            null,
                                            tint = Color.White.copy(alpha = 0.7f),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(28.dp))

            // ── LIEN D'ENRÔLEMENT ──
            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn(tween(400, delayMillis = 400))
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "Vous n'êtes pas encore bénéficiaire ?",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColors.TextSub
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = onNavigateToEnrollment,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = AppShapes.MediumRadius,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AppColors.GoldAccent),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, AppColors.GoldAccent)
                    ) {
                        Icon(Icons.Rounded.AppRegistration, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("S'enrôler à la CMU", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(Modifier.height(40.dp))

            // ── FOOTER ──
            Text(
                "République du Sénégal\nMinistère de la Santé — CMU",
                style = MaterialTheme.typography.labelSmall,
                color = AppColors.TextDisabled,
                textAlign = TextAlign.Center,
                lineHeight = 16.sp
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}
