package com.example.sencsu.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.sencsu.domain.viewmodel.AddAdherentViewModel
import com.example.sencsu.theme.AppColors
import com.example.sencsu.theme.AppShapes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordUpdateScreen(
    adherentId: String,
    matricule: String,
    defaultPassword: String,
    onSuccess: () -> Unit,
    viewModel: AddAdherentViewModel = hiltViewModel()
) {
    val actualDefaultPassword = remember(defaultPassword) {
        if (defaultPassword.contains("communiqué") || defaultPassword.contains("SMS") || defaultPassword.isBlank()) {
            "acmu00"
        } else {
            defaultPassword
        }
    }
    var oldPassword by remember { mutableStateOf(actualDefaultPassword) }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var defaultPasswordVisible by remember { mutableStateOf(false) }
    var newPasswordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Finalisation", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Success Illustration
            Surface(
                modifier = Modifier.size(80.dp),
                shape = RoundedCornerShape(24.dp),
                color = AppColors.StatusGreen.copy(0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Rounded.CheckCircle,
                        null,
                        tint = AppColors.StatusGreen,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }

            Text(
                "Enrôlement réussi !",
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = AppColors.TextMain
            )

            // Info Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = AppColors.SurfaceAlt,
                shape = AppShapes.MediumRadius
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text("Votre Matricule", color = AppColors.TextSub)
                        Text(matricule, fontWeight = FontWeight.Bold, color = AppColors.BrandBlue)
                    }
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text("Mot de passe par défaut", color = AppColors.TextSub)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (defaultPasswordVisible) actualDefaultPassword else "•".repeat(actualDefaultPassword.length.coerceAtLeast(6)),
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.width(8.dp))
                            IconButton(
                                onClick = { defaultPasswordVisible = !defaultPasswordVisible },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    if (defaultPasswordVisible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = AppColors.TextSub
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            Text(
                "Veuillez définir votre nouveau mot de passe pour accéder à votre espace.",
                textAlign = TextAlign.Center,
                color = AppColors.TextSub,
                fontSize = 14.sp
            )

            OutlinedTextField(
                value = newPassword,
                onValueChange = { newPassword = it },
                label = { Text("Nouveau mot de passe") },
                modifier = Modifier.fillMaxWidth(),
                shape = AppShapes.MediumRadius,
                leadingIcon = { Icon(Icons.Rounded.Lock, null, tint = AppColors.BrandBlue) },
                trailingIcon = {
                    IconButton(onClick = { newPasswordVisible = !newPasswordVisible }) {
                        Icon(
                            if (newPasswordVisible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                            contentDescription = if (newPasswordVisible) "Masquer" else "Afficher",
                            tint = AppColors.TextSub
                        )
                    }
                },
                visualTransformation = if (newPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AppColors.BrandBlue,
                    focusedLabelColor = AppColors.BrandBlue
                )
            )

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirmer le mot de passe") },
                modifier = Modifier.fillMaxWidth(),
                shape = AppShapes.MediumRadius,
                leadingIcon = { Icon(Icons.Rounded.LockReset, null, tint = AppColors.BrandBlue) },
                trailingIcon = {
                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                        Icon(
                            if (confirmPasswordVisible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                            contentDescription = if (confirmPasswordVisible) "Masquer" else "Afficher",
                            tint = AppColors.TextSub
                        )
                    }
                },
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AppColors.BrandBlue,
                    focusedLabelColor = AppColors.BrandBlue
                )
            )

            if (error != null) {
                Text(error!!, color = AppColors.StatusRed, fontSize = 12.sp)
            }

            Spacer(Modifier.weight(1f))

            Button(
                onClick = {
                    if (oldPassword.isBlank()) {
                        error = "L'ancien mot de passe est requis"
                        return@Button
                    }
                    if (newPassword.isBlank()) {
                        error = "Le nouveau mot de passe ne peut pas être vide"
                        return@Button
                    }
                    if (newPassword != confirmPassword) {
                        error = "Les nouveaux mots de passe ne correspondent pas"
                        return@Button
                    }
                    isSubmitting = true
                    viewModel.updatePassword(adherentId, matricule, oldPassword, newPassword) { success, msg ->
                        isSubmitting = false
                        if (success) {
                            onSuccess()
                        } else {
                            error = msg ?: "Échec de la mise à jour."
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = AppShapes.MediumRadius,
                colors = ButtonDefaults.buttonColors(containerColor = AppColors.BrandBlue)
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Valider et continuer", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
