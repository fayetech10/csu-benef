package com.example.sencsu.screen

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RenewalScreen(onFinish: () -> Unit) {
    var step by remember { mutableIntStateOf(1) }
    var selectedPlan by remember { mutableStateOf("Mensuel") }
    var selectedMethod by remember { mutableStateOf<String?>(null) }
    var isProcessing by remember { mutableStateOf(false) }
    var showSuccess by remember { mutableStateOf(false) }

    if (showSuccess) {
        SuccessRenewalScreen(onClose = onFinish)
        return
    }

    Scaffold(
        containerColor = AppColors.AppBackground,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Renouvellement", fontWeight = FontWeight.Black) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp)) {
            // STEP INDICATOR
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Formule", "Paiement", "Confirmation").forEachIndexed { index, label ->
                    val isActive = index + 1 <= step
                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape).background(if (isActive) AppColors.BrandBlue else AppColors.BorderColor))
                        Spacer(Modifier.height(8.dp))
                        Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (isActive) AppColors.BrandBlue else AppColors.TextSub)
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            AnimatedContent(targetState = step, label = "renewal_steps") { s ->
                when (s) {
                    1 -> ChoiceStep(selectedPlan) { selectedPlan = it }
                    2 -> PaymentStep(selectedMethod) { selectedMethod = it }
                    3 -> SummaryStep(selectedPlan, selectedMethod!!)
                }
            }

            Spacer(Modifier.weight(1f))

            Button(
                onClick = {
                    if (step < 3) step++
                    else {
                        isProcessing = true
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = AppShapes.MediumRadius,
                colors = ButtonDefaults.buttonColors(containerColor = AppColors.BrandBlue),
                enabled = (step == 1) || (step == 2 && selectedMethod != null) || (step == 3 && !isProcessing)
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text(if (step == 3) "Confirmer le paiement" else "Suivant", fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    LaunchedEffect(isProcessing) {
        if (isProcessing) {
            delay(2500)
            isProcessing = false
            showSuccess = true
        }
    }
}

@Composable
private fun ChoiceStep(selected: String, onSelect: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Choisissez votre formule", fontWeight = FontWeight.Black, fontSize = 20.sp)
        
        PlanCard("Mensuel", "5.000 FCFA / mois", "Idéal pour tester la couverture", selected == "Mensuel") { onSelect("Mensuel") }
        PlanCard("Trimestriel", "13.500 FCFA / 3 mois", "-10% de réduction immédiate", selected == "Trimestriel") { onSelect("Trimestriel") }
        PlanCard("Annuel", "50.000 FCFA / an", "2 mois offerts — Recommandé", selected == "Annuel") { onSelect("Annuel") }
    }
}

@Composable
private fun PaymentStep(selected: String?, onSelect: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Mode de paiement", fontWeight = FontWeight.Black, fontSize = 20.sp)
        
        PaymentOption("Wave", Icons.Rounded.WaterfallChart, selected == "Wave") { onSelect("Wave") }
        PaymentOption("Orange Money", Icons.Rounded.Smartphone, selected == "OM") { onSelect("OM") }
        PaymentOption("Free Money", Icons.Rounded.FlashOn, selected == "Free") { onSelect("Free") }
    }
}

@Composable
private fun SummaryStep(plan: String, method: String) {
    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
        Text("Vérifiez vos choix", fontWeight = FontWeight.Black, fontSize = 20.sp)
        
        Surface(modifier = Modifier.fillMaxWidth(), shape = AppShapes.MediumRadius, color = Color.White) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text("Formule choisie", color = AppColors.TextSub)
                    Text(plan, fontWeight = FontWeight.Bold)
                }
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text("Mode de paiement", color = AppColors.TextSub)
                    Text(method, fontWeight = FontWeight.Bold)
                }
                Divider(color = AppColors.BorderColorLight)
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text("Total à payer", fontWeight = FontWeight.Black)
                    Text(if (plan == "Mensuel") "5.000 FCFA" else "...", fontWeight = FontWeight.Black, color = AppColors.BrandBlue, fontSize = 18.sp)
                }
            }
        }
    }
}

@Composable
private fun PlanCard(title: String, price: String, desc: String, isActive: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = AppShapes.MediumRadius,
        color = if (isActive) AppColors.BrandBlueLite else Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isActive) AppColors.BrandBlue else AppColors.BorderColor)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Black, color = if (isActive) AppColors.BrandBlue else AppColors.TextMain)
                Text(price, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(desc, fontSize = 12.sp, color = AppColors.TextSub)
            }
            if (isActive) Icon(Icons.Rounded.CheckCircle, null, tint = AppColors.BrandBlue)
        }
    }
}

@Composable
private fun PaymentOption(label: String, icon: ImageVector, isActive: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = AppShapes.MediumRadius,
        color = if (isActive) AppColors.BrandBlueLite else Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isActive) AppColors.BrandBlue else AppColors.BorderColor)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = if (isActive) AppColors.BrandBlue else AppColors.ActionBlue)
            Spacer(Modifier.width(16.dp))
            Text(label, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            RadioButton(selected = isActive, onClick = onClick, colors = RadioButtonDefaults.colors(selectedColor = AppColors.BrandBlue))
        }
    }
}

@Composable
private fun SuccessRenewalScreen(onClose: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().background(Color.White).padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Rounded.Verified, null, modifier = Modifier.size(80.dp), tint = AppColors.StatusGreen)
        Spacer(Modifier.height(24.dp))
        Text("Paiement Confirmé", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
        Text("Votre couverture a été prolongée avec succès.", textAlign = androidx.compose.ui.text.style.TextAlign.Center, color = AppColors.TextSub)
        
        Spacer(Modifier.height(40.dp))
        
        OutlinedButton(
            onClick = {}, 
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = AppShapes.MediumRadius,
            border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.ActionBlue)
        ) {
            Icon(Icons.Rounded.Download, null)
            Spacer(Modifier.width(8.dp))
            Text("Télécharger le reçu PDF")
        }
        
        Spacer(Modifier.height(12.dp))
        
        Button(
            onClick = onClose,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = AppShapes.MediumRadius,
            colors = ButtonDefaults.buttonColors(containerColor = AppColors.BrandBlue)
        ) {
            Text("Retour à l'accueil")
        }
    }
}
