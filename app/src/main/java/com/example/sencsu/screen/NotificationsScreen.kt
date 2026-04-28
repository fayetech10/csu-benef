package com.example.sencsu.screen

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sencsu.theme.AppColors
import com.example.sencsu.theme.AppShapes
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen() {
    var showContent by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        delay(100)
        showContent = true
    }

    val notes = listOf(
        NotificationItemDto("Renouvellement Proche", "Votre couverture expire dans 7 jours. Pensez à renouveler pour conserver vos droits.", Icons.Rounded.AccessTime, AppColors.StatusOrange, false, "Il y a 2 heures"),
        NotificationItemDto("Validation de Membre", "Le dossier de Aminata Diop a été validé avec succès par l'agent CSU.", Icons.Rounded.Verified, AppColors.StatusGreen, true, "Hier"),
        NotificationItemDto("Reçu de Paiement", "Votre paiement de 5,000 FCFA pour Décembre a été enregistré.", Icons.Rounded.Payments, AppColors.BrandBlue, true, "Il y a 3 jours")
    )

    Scaffold(
        containerColor = AppColors.AppBackground,
        topBar = {
            TopAppBar(
                title = { Text("Notifications", fontWeight = FontWeight.Black) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            itemsIndexed(notes) { index, note ->
                Column {
                    AnimatedVisibility(
                        visible = showContent,
                        enter = fadeIn(tween(300, delayMillis = index * 100)) + slideInVertically(
                            tween(300, delayMillis = index * 100), initialOffsetY = { 20 }
                        )
                    ) {
                        NotificationCard(note)
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationCard(note: NotificationItemDto) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = AppShapes.MediumRadius,
        color = if (note.isRead) Color.White else AppColors.BrandBlueLite.copy(alpha = 0.4f),
        shadowElevation = if (note.isRead) 1.dp else 0.dp,
        border = if (!note.isRead) androidx.compose.foundation.BorderStroke(1.dp, AppColors.BrandBlue.copy(0.1f)) else null
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
            Surface(
                modifier = Modifier.size(42.dp),
                shape = CircleShape,
                color = note.color.copy(0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(note.icon, null, tint = note.color, modifier = Modifier.size(20.dp))
                }
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        note.title,
                        fontWeight = if (note.isRead) FontWeight.Bold else FontWeight.Black,
                        fontSize = 14.sp,
                        modifier = Modifier.weight(1f),
                        color = AppColors.TextMain
                    )
                    if (!note.isRead) {
                        Surface(modifier = Modifier.size(8.dp), shape = CircleShape, color = AppColors.BrandBlue) {}
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    note.body,
                    color = AppColors.TextSub,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    note.time,
                    fontSize = 11.sp,
                    color = AppColors.TextDisabled,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

private data class NotificationItemDto(
    val title: String,
    val body: String,
    val icon: ImageVector,
    val color: Color,
    val isRead: Boolean,
    val time: String
)
