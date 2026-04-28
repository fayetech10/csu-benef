package com.example.sencsu.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.sencsu.theme.AppColors
import com.example.sencsu.theme.AppShapes

/**
 * Composant shimmer réutilisable pour les états de chargement.
 * Crée un effet de brillance horizontale animée.
 */
@Composable
fun ShimmerEffect(
    modifier: Modifier = Modifier,
    baseColor: Color = AppColors.SurfaceAlt,
    highlightColor: Color = Color.White.copy(alpha = 0.4f)
) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = -300f,
        targetValue = 900f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translate"
    )

    val brush = Brush.linearGradient(
        colors = listOf(baseColor, highlightColor, baseColor),
        start = Offset(translateAnim, 0f),
        end = Offset(translateAnim + 300f, 0f)
    )

    Box(
        modifier = modifier.background(brush)
    )
}

/**
 * Placeholder shimmer rectangulaire.
 */
@Composable
fun ShimmerBox(
    modifier: Modifier = Modifier,
    height: Dp = 20.dp,
    cornerRadius: RoundedCornerShape = AppShapes.SmallRadius
) {
    ShimmerEffect(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(cornerRadius)
    )
}

/**
 * Shimmer qui simule le layout du Dashboard pendant le chargement.
 */
@Composable
fun DashboardShimmer(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Header shimmer
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ShimmerBox(
                    modifier = Modifier.width(100.dp),
                    height = 14.dp
                )
                ShimmerBox(
                    modifier = Modifier.width(140.dp),
                    height = 24.dp
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ShimmerEffect(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                )
                ShimmerEffect(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                )
            }
        }

        // Hero card shimmer
        ShimmerBox(
            modifier = Modifier.fillMaxWidth(),
            height = 140.dp,
            cornerRadius = AppShapes.LargeRadius
        )

        // Quick stats shimmer
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            repeat(3) {
                ShimmerBox(
                    modifier = Modifier.weight(1f),
                    height = 56.dp,
                    cornerRadius = AppShapes.MediumRadius
                )
            }
        }

        // Action buttons shimmer
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            repeat(4) {
                Column(
                    horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ShimmerEffect(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                    )
                    ShimmerBox(
                        modifier = Modifier.width(50.dp),
                        height = 10.dp
                    )
                }
            }
        }

        // Chart placeholder shimmer
        ShimmerBox(
            modifier = Modifier.fillMaxWidth(),
            height = 200.dp,
            cornerRadius = AppShapes.LargeRadius
        )

        // Recent activities shimmer
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            ShimmerBox(
                modifier = Modifier.width(160.dp),
                height = 18.dp
            )
            repeat(3) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ShimmerEffect(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                    )
                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        ShimmerBox(height = 14.dp)
                        ShimmerBox(
                            modifier = Modifier.fillMaxWidth(0.6f),
                            height = 10.dp
                        )
                    }
                }
            }
        }
    }
}
