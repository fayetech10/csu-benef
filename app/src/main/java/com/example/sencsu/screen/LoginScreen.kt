package com.example.sencsu.screen

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.sencsu.domain.viewmodel.LoginUiEvent
import com.example.sencsu.domain.viewmodel.LoginViewModel
import com.example.sencsu.theme.AppColors
import com.example.sencsu.theme.AppShapes
import com.example.sencsu.utils.BiometricHelper
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity
import com.example.sencsu.theme.withAlpha
import kotlinx.coroutines.delay

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToEnrollment: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var showContent by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(Unit) {
        viewModel.getSavedMatricule()?.let { savedEmail ->
            email = savedEmail
        }
        delay(150)
        showContent = true
    }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is LoginUiEvent.NavigateToDashboard -> onLoginSuccess()
            }
        }
    }

    Scaffold(
        containerColor = AppColors.AppBackground
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // IMMERSIVE BACKGROUND GRADIENT
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.55f)
                    .background(
                        Brush.verticalGradient(
                            listOf(AppColors.BrandBlue, AppColors.BrandBlueDark)
                        )
                    )
            ) {
                // Decorative circles
                Box(
                    modifier = Modifier
                        .size(300.dp)
                        .align(Alignment.TopEnd)
                        .offset(x = 100.dp, y = (-80).dp)
                        .clip(androidx.compose.foundation.shape.CircleShape)
                        .background(Color.White.copy(alpha = 0.05f))
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(horizontal = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(60.dp))

                // BRANDING SECTION
                AnimatedVisibility(
                    visible = showContent,
                    enter = fadeIn(tween(600)) + slideInVertically(tween(600)) { -40 }
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(
                            modifier = Modifier.size(90.dp),
                            shape = AppShapes.LargeRadius,
                            color = Color.White.copy(alpha = 0.15f),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, Color.White.copy(alpha = 0.2f))
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Rounded.VerifiedUser, null,
                                    tint = Color.White,
                                    modifier = Modifier.size(44.dp)
                                )
                            }
                        }
                        Spacer(Modifier.height(24.dp))
                        Text(
                            "SenCSU",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            letterSpacing = 1.sp
                        )
                        Text(
                            "Votre santé, notre priorité",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }

                Spacer(Modifier.height(44.dp))

                // LOGIN CARD WITH GLASS EFFECT
                AnimatedVisibility(
                    visible = showContent,
                    enter = fadeIn(tween(600, delayMillis = 200)) + slideInVertically(tween(600, delayMillis = 200)) { 40 }
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = AppShapes.LargeRadius,
                        colors = CardDefaults.cardColors(containerColor = AppColors.SurfaceBackground),
                        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(28.dp),
                            verticalArrangement = Arrangement.spacedBy(22.dp)
                        ) {
                            Text(
                                "Authentification",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = AppColors.TextMain
                            )

                            // Email/Matricule
                            AuthTextField(
                                value = email,
                                onValueChange = { email = it },
                                label = "Email ou Matricule",
                                placeholder = "agent@sencsu.sn",
                                icon = Icons.Rounded.Badge,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Email,
                                    imeAction = ImeAction.Next
                                ),
                                keyboardActions = KeyboardActions(
                                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                                )
                            )

                            // Password
                            AuthTextField(
                                value = password,
                                onValueChange = { password = it },
                                label = "Mot de passe",
                                placeholder = "••••••••",
                                icon = Icons.Rounded.Lock,
                                isPassword = true,
                                passwordVisible = passwordVisible,
                                onTogglePassword = { passwordVisible = !passwordVisible },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Password,
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        focusManager.clearFocus()
                                        if (email.isNotBlank() && password.isNotBlank()) {
                                            viewModel.login(email.trim(), password)
                                        }
                                    }
                                )
                            )

                            // Error Message
                            AnimatedVisibility(visible = state.error != null) {
                                Surface(
                                    color = AppColors.StatusRed.copy(alpha = 0.08f),
                                    shape = AppShapes.MediumRadius,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.StatusRed.copy(alpha = 0.15f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Rounded.Error, null, tint = AppColors.StatusRed, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(8.dp))
                                        Text(state.error ?: "", color = AppColors.StatusRed, fontSize = 12.sp)
                                    }
                                }
                            }

                            // Actions Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Button(
                                    onClick = {
                                        focusManager.clearFocus()
                                        viewModel.login(email.trim(), password)
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(56.dp),
                                    shape = AppShapes.MediumRadius,
                                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.BrandBlue),
                                    enabled = email.isNotBlank() && password.isNotBlank() && !state.isLoading
                                ) {
                                    if (state.isLoading) {
                                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                                    } else {
                                        Text("Se connecter", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                                    }
                                }

                                val context = LocalContext.current
                                val canUseBiometric by viewModel.canUseBiometric.collectAsState()

                                if (canUseBiometric && BiometricHelper.isBiometricAvailable(context)) {
                                    Surface(
                                        onClick = {
                                            val activity = context as? FragmentActivity
                                            if (activity != null) {
                                                BiometricHelper.showBiometricPrompt(
                                                    activity = activity,
                                                    onSuccess = { viewModel.loginWithBiometric() },
                                                    onError = { _, _ -> },
                                                    onFailed = {}
                                                )
                                            }
                                        },
                                        modifier = Modifier.size(56.dp),
                                        shape = AppShapes.MediumRadius,
                                        color = AppColors.BrandBlue.copy(alpha = 0.08f),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.BrandBlue.copy(alpha = 0.15f))
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(Icons.Rounded.Fingerprint, "Biométrie", tint = AppColors.BrandBlue, modifier = Modifier.size(30.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.weight(1f))

                // FOOTER
                AnimatedVisibility(
                    visible = showContent,
                    enter = fadeIn(tween(600, delayMillis = 400))
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(bottom = 32.dp)
                    ) {
                        Text(
                            "Pas encore enrôlé ?",
                            style = MaterialTheme.typography.bodySmall,
                            color = AppColors.TextSub
                        )
                        Spacer(Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = onNavigateToEnrollment,
                            modifier = Modifier.fillMaxWidth().height(54.dp),
                            shape = AppShapes.MediumRadius,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = AppColors.BrandBlue),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, AppColors.BrandBlue.withAlpha(0.3f))
                        ) {
                            Text("Démarrer mon enrôlement", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    icon: ImageVector,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onTogglePassword: (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = { Text(placeholder, color = AppColors.TextDisabled) },
        leadingIcon = { Icon(icon, null, tint = AppColors.BrandBlue, modifier = Modifier.size(22.dp)) },
        trailingIcon = {
            if (isPassword) {
                IconButton(onClick = { onTogglePassword?.invoke() }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                        contentDescription = null,
                        tint = AppColors.TextSub
                    )
                }
            }
        },
        visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
        modifier = Modifier.fillMaxWidth(),
        shape = AppShapes.MediumRadius,
        singleLine = true,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = AppColors.BrandBlue,
            unfocusedBorderColor = AppColors.BorderColor,
            focusedLabelColor = AppColors.BrandBlue,
            focusedContainerColor = AppColors.SurfaceMuted,
            unfocusedContainerColor = AppColors.SurfaceMuted
        )
    )
}
