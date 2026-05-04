package com.example.sencsu.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sencsu.models.ActivityItem
import com.example.sencsu.models.GreenBg
import com.example.sencsu.models.GreenText
import com.example.sencsu.models.RedBg
import com.example.sencsu.models.RedText
import com.example.sencsu.models.StatItem
import com.example.sencsu.theme.AppColors
import com.example.sencsu.theme.AppElevation
import com.example.sencsu.theme.AppShapes

@Composable
fun StatCard(item: StatItem, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = AppColors.SurfaceBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = AppElevation.card),
        shape = AppShapes.LargeRadius,
        border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.BorderColorLight)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Ligne Icone + Badge Trend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icone carrée
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(AppShapes.SmallRadius)
                        .background(item.themeColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = item.iconRes,
                        contentDescription = null,
                        tint = item.themeColor,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Badge (Trend)
                // Using AppColors for trend if feasible, otherwise keeping model colors but using AppShapes
                val badgeBg = if (item.isAlert) AppColors.StatusRedSoft else AppColors.StatusGreenSoft
                val badgeText = if (item.isAlert) AppColors.StatusRed else AppColors.StatusGreen

                Box(
                    modifier = Modifier
                        .clip(AppShapes.CircleRadius)
                        .background(badgeBg)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = item.trend,
                        color = badgeText,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Valeur et Titre
            Column {
                Text(
                    text = item.value,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    color = AppColors.TextMain
                )
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.TextSub
                )
            }
        }
    }
}

@Composable
fun ActivityRow(activity: ActivityItem) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.SurfaceBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = AppElevation.card),
        shape = AppShapes.LargeRadius,
        border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.BorderColorLight)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar (Simulé avec un cercle gris ici, utilisez AsyncImage en prod)
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(AppShapes.CircleRadius)
                    .background(AppColors.BrandBlueLite)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Textes
            Column(modifier = Modifier.weight(1f)) {
                Text(text = activity.name, fontWeight = FontWeight.Bold, color = AppColors.TextMain)
                Text(
                    text = "${activity.action} • ${activity.time}",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.TextSub
                )
            }

            // Badge Status
            Box(
                modifier = Modifier
                    .clip(AppShapes.ExtraSmallRadius)
                    .background(activity.statusBg) // Keeping model provided bg for now as logic is there
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = activity.status,
                    color = activity.statusColor,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
