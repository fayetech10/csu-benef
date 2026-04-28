package com.example.sencsu.screen.enrolement

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sencsu.theme.AppColors
import com.example.sencsu.theme.AppShapes
import androidx.compose.foundation.BorderStroke

@Composable
fun StepperHeader(currentStep: Int, totalSteps: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(totalSteps) { index ->
            val step = index + 1
            val isActive = step <= currentStep
            val isCurrent = step == currentStep

            Box(
                modifier = Modifier
                    .size(if (isCurrent) 32.dp else 24.dp)
                    .clip(CircleShape)
                    .background(if (isActive) AppColors.BrandBlue else AppColors.BorderColor),
                contentAlignment = Alignment.Center
            ) {
                if (step < currentStep) {
                    Icon(Icons.Rounded.Check, null, tint = Color.White, modifier = Modifier.size(16.dp))
                } else {
                    Text(
                        "$step",
                        color = if (isActive) Color.White else AppColors.TextSub,
                        fontWeight = FontWeight.Bold,
                        fontSize = if (isCurrent) 14.sp else 12.sp
                    )
                }
            }

            if (step < totalSteps) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(2.dp)
                        .padding(horizontal = 4.dp)
                        .background(if (step < currentStep) AppColors.BrandBlue else AppColors.BorderColor)
                )
            }
        }
    }
}

@Composable
fun StepTitle(title: String, sub: String) {
    Column {
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = AppColors.TextMain)
        Spacer(Modifier.height(4.dp))
        Text(sub, style = MaterialTheme.typography.bodyMedium, color = AppColors.TextSub)
    }
}

@Composable
fun SexeOption(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = AppShapes.MediumRadius,
        color = if (selected) AppColors.BrandBlueLite else Color.White,
        border = BorderStroke(1.dp, if (selected) AppColors.BrandBlue else AppColors.BorderColor)
    ) {
        Box(modifier = Modifier.padding(12.dp), contentAlignment = Alignment.Center) {
            Text(label, color = if (selected) AppColors.BrandBlue else AppColors.TextSub, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun InfoCard(title: String, infos: List<Pair<String, String>>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, fontWeight = FontWeight.Bold, color = AppColors.BrandBlue, fontSize = 14.sp)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = AppShapes.MediumRadius,
            color = Color.White,
            border = BorderStroke(1.dp, AppColors.BorderColor)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                infos.forEach { (l, v) ->
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(l, color = AppColors.TextSub, fontSize = 13.sp)
                        Text(v, fontWeight = FontWeight.SemiBold, color = AppColors.TextMain, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun SuccessProfileScreen(onFinish: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(modifier = Modifier.size(100.dp), shape = CircleShape, color = AppColors.StatusGreen.copy(0.1f)) {
            Icon(Icons.Rounded.CheckCircle, null, modifier = Modifier.padding(20.dp), tint = AppColors.StatusGreen)
        }
        Spacer(Modifier.height(32.dp))
        Text("Enrôlement réussi !", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black, color = AppColors.BrandBlue)
        Spacer(Modifier.height(12.dp))
        Text("Votre numéro provisoire :", color = AppColors.TextSub)
        Surface(color = AppColors.BrandBlueLite, shape = AppShapes.MediumRadius, modifier = Modifier.padding(vertical = 12.dp)) {
            Text("CSU-PROVISOIRE-123", modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp), fontWeight = FontWeight.ExtraBold, letterSpacing = 2.sp, color = AppColors.BrandBlue)
        }
        Text("Délai d'activation estimé : 48h", color = AppColors.TextSub, fontSize = 14.sp)
        Spacer(Modifier.height(48.dp))
        Button(
            onClick = onFinish,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = AppShapes.MediumRadius,
            colors = ButtonDefaults.buttonColors(containerColor = AppColors.BrandBlue)
        ) {
            Text("Aller au tableau de bord", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun PremiumTextField(
    value: String, onValueChange: (String) -> Unit,
    label: String, placeholder: String,
    icon: ImageVector? = null, prefix: String? = null,
    modifier: Modifier = Modifier, readOnly: Boolean = false
) {
    Column(modifier = modifier) {
        Text(label, fontWeight = FontWeight.SemiBold, color = AppColors.TextMain, modifier = Modifier.padding(bottom = 6.dp), fontSize = 14.sp)
        OutlinedTextField(
            value = value, onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder, color = AppColors.TextDisabled) },
            leadingIcon = icon?.let { { Icon(it, null, tint = AppColors.ActionBlue, modifier = Modifier.size(20.dp)) } },
            prefix = prefix?.let { { Text(it, fontWeight = FontWeight.Bold, color = AppColors.TextMain) } },
            shape = AppShapes.MediumRadius,
            readOnly = readOnly,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AppColors.BrandBlue,
                unfocusedBorderColor = AppColors.BorderColor,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            )
        )
    }
}
