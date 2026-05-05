package com.example.sencsu.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.sencsu.domain.viewmodel.DeepLinkResolverViewModel
import com.example.sencsu.theme.AppColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ErrorOutline

@Composable
fun DeepLinkResolverScreen(
    matricule: String,
    onNavigate: (String) -> Unit,
    onCancel: () -> Unit,
    viewModel: DeepLinkResolverViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(matricule) {
        viewModel.resolveMatricule(matricule)
    }

    LaunchedEffect(uiState.targetRoute) {
        uiState.targetRoute?.let { route ->
            onNavigate(route)
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(color = AppColors.BrandBlue)
                Text(
                    "Recherche du bénéficiaire...",
                    color = AppColors.TextSub,
                    fontSize = 14.sp
                )
                Text(
                    "Matricule : $matricule",
                    color = AppColors.BrandBlue,
                    fontWeight = FontWeight.Bold
                )
            }

            if (uiState.error != null) {
                Icon(
                    imageVector = Icons.Rounded.ErrorOutline,
                    contentDescription = null,
                    tint = AppColors.StatusRed,
                    modifier = Modifier.size(48.dp)
                )
                Text(
                    uiState.error!!,
                    color = AppColors.StatusRed,
                    fontWeight = FontWeight.Bold,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 32.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onCancel,
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.BrandBlue)
                ) {
                    Text("Retour")
                }
            }
        }
    }
}
