package com.example.sencsu.screen

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Badge
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
        delay(200)
        showContent = true
    }

    // Observer les événements de navigation
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
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            // Background Decorative
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.4f)
                    .align(Alignment.TopCenter)
                    .background(
                        Brush.verticalGradient(
                            listOf(AppColors.BrandBlue, AppColors.AppBackground)
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // LOGO & HEADER
                AnimatedVisibility(
                    visible = showContent,
                    enter = fadeIn() + slideInVertically { -20 }
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(
                            modifier = Modifier.size(80.dp),
                            shape = AppShapes.MediumRadius,
                            color = Color.White,
                            shadowElevation = 8.dp
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Rounded.VerifiedUser, null,
                                    tint = AppColors.BrandBlue,
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                        }
                        Spacer(Modifier.height(24.dp))
                        Text(
                            "SenCSU",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Black,
                            color = AppColors.TextMain
                        )
                        Text(
                            "Votre santé, notre priorité",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AppColors.TextSub
                        )
                    }
                }

                Spacer(Modifier.height(48.dp))

                // LOGIN CARD
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
                        Text(
                            "Authentification",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.TextMain
                        )

                        // Champ Email / Matricule
                        AuthTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = "Email",
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

                        // Champ Mot de passe
                        AuthTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = "Mot de passe",
                            placeholder = "Votre mot de passe",
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

                        // Message d'erreur
                        AnimatedVisibility(visible = state.error != null) {
                            Surface(
                                color = AppColors.StatusRed.copy(alpha = 0.1f),
                                shape = AppShapes.SmallRadius
                            ) {
                                Text(
                                    state.error ?: "",
                                    color = AppColors.StatusRed,
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                )
                            }
                        }

                        // Bouton de connexion
                        Button(
                            onClick = {
                                focusManager.clearFocus()
                                viewModel.login(email.trim(), password)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = AppShapes.MediumRadius,
                            colors = ButtonDefaults.buttonColors(containerColor = AppColors.BrandBlue),
                            enabled = email.isNotBlank() && password.isNotBlank() && !state.isLoading
                        ) {
                            if (state.isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text("Se connecter", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(32.dp))

                // ENROLLMENT PROMPT
                Text(
                    "Vous n'avez pas encore de compte ?",
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
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AppColors.ActionBlue),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.ActionBlue)
                ) {
                    Text("M'enrôler", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/**
 * Composant TextField premium réutilisable.
 */
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
        leadingIcon = {
            Icon(icon, null, tint = AppColors.BrandBlue, modifier = Modifier.size(20.dp))
        },
        trailingIcon = {
            if (isPassword) {
                IconButton(onClick = { onTogglePassword?.invoke() }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                        contentDescription = if (passwordVisible) "Masquer" else "Afficher",
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
            cursorColor = AppColors.BrandBlue
        )
    )
}