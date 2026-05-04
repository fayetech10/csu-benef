package com.example.sencsu.screen

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalContext
import com.example.sencsu.configs.ApiConfig
import com.example.sencsu.data.remote.dto.PersonneChargeDto
import com.example.sencsu.domain.viewmodel.BeneficiaryDashboardViewModel
import com.example.sencsu.theme.AppColors
import com.example.sencsu.theme.AppShapes
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DependentsScreen(
    onNavigateToHistory: (String, String, String) -> Unit = { _, _, _ -> },
    onNavigateToDetails: (String, String) -> Unit = { _, _ -> },
    viewModel: BeneficiaryDashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val token by viewModel.sessionManager.tokenFlow.collectAsState(initial = null)
    val context = LocalContext.current
    val adherent = uiState.adherent
    val personnesCharge = adherent?.personnesCharge ?: emptyList()
    var showContent by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(100)
        showContent = true
    }

    var showAddDialog by remember { mutableStateOf(false) }
    var selectedDependant by remember { mutableStateOf<PersonneChargeDto?>(null) }
    var showDeleteConfirm by remember { mutableStateOf<PersonneChargeDto?>(null) }

    Scaffold(
        containerColor = AppColors.AppBackground,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Mon Foyer", fontWeight = FontWeight.Black, fontSize = 20.sp)
                        Text(
                            "${personnesCharge.size} personne(s) en charge",
                            fontSize = 12.sp,
                            color = AppColors.TextSub
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    selectedDependant = null
                    showAddDialog = true
                },
                containerColor = AppColors.BrandBlue,
                contentColor = Color.White
            ) {
                Icon(Icons.Rounded.Add, "Ajouter")
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (uiState.isLoading) {
                // État de chargement
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = AppColors.BrandBlue)
                        Spacer(Modifier.height(16.dp))
                        Text("Mise à jour du foyer...", color = AppColors.TextSub)
                    }
                }
            } else if (personnesCharge.isEmpty()) {
                // État vide
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyFoyerState()
                }
            } else {
                // Liste des personnes en charge
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 120.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // En-tête résumé
                    item {
                        AnimatedVisibility(
                            visible = showContent,
                            enter = fadeIn(tween(300)) + slideInVertically(tween(300), initialOffsetY = { 20 })
                        ) {
                            FamilySummaryCard(personnesCharge)
                        }
                    }

                    itemsIndexed(personnesCharge) { index, pc ->
                        AnimatedVisibility(
                            visible = showContent,
                            enter = fadeIn(tween(300, delayMillis = 100 + index * 100)) + slideInVertically(
                                tween(300, delayMillis = 100 + index * 100), initialOffsetY = { 40 }
                            )
                        ) {
                            DependentDetailCard(
                                pc = pc,
                                onEdit = {
                                    selectedDependant = pc
                                    showAddDialog = true
                                },
                                onDelete = {
                                    showDeleteConfirm = pc
                                },
                                onMedicalHistory = {
                                    adherent?.id?.let { adherentId ->
                                        pc.id?.let { pcId ->
                                            val fullName = "${pc.prenoms ?: ""} ${pc.nom ?: ""}".trim()
                                            onNavigateToHistory(adherentId, pcId, fullName)
                                        }
                                    }
                                },
                                onClick = {
                                    adherent?.id?.let { adherentId ->
                                        pc.id?.let { pcId ->
                                            onNavigateToDetails(adherentId, pcId)
                                        }
                                    }
                                },
                                token = token,
                                context = context
                            )
                        }
                    }
                }
            }

            // Erreur
            if (uiState.error != null) {
                Snackbar(
                    modifier = Modifier.padding(16.dp).align(Alignment.BottomCenter),
                    containerColor = AppColors.StatusRed
                ) {
                    Text(uiState.error!!, color = Color.White)
                }
            }
        }
    }

    if (showAddDialog) {
        DependantFormDialog(
            dependant = selectedDependant,
            onDismiss = { showAddDialog = false },
            onConfirm = { updatedPc ->
                if (selectedDependant == null) {
                    viewModel.addPersonneCharge(updatedPc)
                } else {
                    viewModel.updatePersonneCharge(selectedDependant!!.id!!, updatedPc)
                }
                showAddDialog = false
            }
        )
    }

    if (showDeleteConfirm != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = null },
            title = { Text("Retirer du foyer", fontWeight = FontWeight.Bold) },
            text = { Text("Voulez-vous vraiment retirer ${showDeleteConfirm?.prenoms} ${showDeleteConfirm?.nom} de votre couverture santé ?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deletePersonneCharge(showDeleteConfirm!!.id!!)
                        showDeleteConfirm = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = AppColors.StatusRed)
                ) {
                    Text("Supprimer", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = null }) {
                    Text("Annuler")
                }
            },
            containerColor = Color.White
        )
    }
}

@Composable
private fun FamilySummaryCard(dependants: List<PersonneChargeDto>) {
    val nbMale = dependants.count { it.sexe?.uppercase() == "M" }
    val nbFemale = dependants.size - nbMale

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = AppShapes.LargeRadius,
        color = AppColors.BrandBlueLite
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = AppColors.BrandBlue.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Rounded.FamilyRestroom, null,
                        tint = AppColors.BrandBlue,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Composition du foyer",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = AppColors.TextMain
                )
                Text(
                    "$nbMale homme(s) · $nbFemale femme(s)",
                    fontSize = 13.sp,
                    color = AppColors.TextSub
                )
            }
            Surface(
                shape = AppShapes.CircleRadius,
                color = AppColors.BrandBlue.copy(alpha = 0.1f)
            ) {
                Text(
                    "${dependants.size}",
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    color = AppColors.BrandBlue
                )
            }
        }
    }
}

@Composable
private fun DependentDetailCard(
    pc: PersonneChargeDto,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onMedicalHistory: () -> Unit,
    onClick: () -> Unit,
    token: String?,
    context: android.content.Context
) {
    val name = "${pc.prenoms ?: ""} ${pc.nom ?: ""}".trim().ifEmpty { "Inconnu" }
    val initials = name.split(" ")
        .take(2)
        .mapNotNull { it.firstOrNull()?.uppercase() }
        .joinToString("")

    Surface(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 2.dp,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Avatar avec initiales
                Surface(
                    modifier = Modifier.size(56.dp),
                    shape = CircleShape,
                    color = AppColors.BrandBlue.copy(alpha = 0.05f),
                    border = BorderStroke(1.dp, AppColors.BrandBlue.copy(alpha = 0.1f))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            initials,
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp,
                            color = AppColors.BrandBlue
                        )
                    }
                }

                Spacer(Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        name,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 17.sp,
                        color = AppColors.TextMain
                    )
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = AppColors.GoldAccent.copy(alpha = 0.12f)
                        ) {
                            Text(
                                pc.lienParent?.uppercase() ?: "DÉPENDANT",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = AppColors.GoldAccent,
                                letterSpacing = 0.5.sp
                            )
                        }
                        if (pc.matricule != null) {
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "#${pc.matricule}",
                                fontSize = 11.sp,
                                color = AppColors.TextSub,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // Mini Menu
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onMedicalHistory,
                        modifier = Modifier.size(36.dp).background(AppColors.BrandBlue.copy(0.05f), CircleShape)
                    ) {
                        Icon(Icons.Rounded.MedicalServices, null, tint = AppColors.BrandBlue, modifier = Modifier.size(18.dp))
                    }
                    Spacer(Modifier.width(6.dp))
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(36.dp).background(AppColors.BrandBlue.copy(0.05f), CircleShape)
                    ) {
                        Icon(Icons.Rounded.Edit, null, tint = AppColors.BrandBlue, modifier = Modifier.size(18.dp))
                    }
                }
            }

            if (pc.matricule != null) {
                Spacer(Modifier.height(12.dp))
                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                Spacer(Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Accès rapide aux soins", fontSize = 11.sp, color = AppColors.TextSub, fontWeight = FontWeight.Medium)
                        Text("Présentez ce code QR", fontSize = 10.sp, color = AppColors.TextSub.copy(alpha = 0.6f))
                    }
                    
                    Surface(
                        modifier = Modifier.size(44.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = Color.White,
                        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.2f)),
                        shadowElevation = 1.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            SubcomposeAsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(ApiConfig.getQrCodeUrl(pc.matricule))
                                    .apply { token?.let { addHeader("Authorization", "Bearer $it") } }
                                    .build(),
                                contentDescription = "QR",
                                modifier = Modifier.padding(4.dp),
                                loading = { CircularProgressIndicator(modifier = Modifier.size(12.dp), strokeWidth = 1.5.dp, color = AppColors.BrandBlue) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DependantFormDialog(
    dependant: PersonneChargeDto?,
    onDismiss: () -> Unit,
    onConfirm: (PersonneChargeDto) -> Unit
) {
    var prenoms by remember { mutableStateOf(dependant?.prenoms ?: "") }
    var nom by remember { mutableStateOf(dependant?.nom ?: "") }
    var lien by remember { mutableStateOf(dependant?.lienParent ?: "ENFANT") }
    var sexe by remember { mutableStateOf(dependant?.sexe ?: "M") }
    var dateNaissance by remember { mutableStateOf(dependant?.dateNaissance ?: "") }
    var lieuNaissance by remember { mutableStateOf(dependant?.lieuNaissance ?: "") }
    var adresse by remember { mutableStateOf(dependant?.adresse ?: "") }
    var situationM by remember { mutableStateOf(dependant?.situationM ?: "Célibataire") }
    var numeroExtrait by remember { mutableStateOf(dependant?.numeroExtrait ?: "") }
    var whatsapp by remember { mutableStateOf(dependant?.whatsapp ?: "") }
    var numeroCNi by remember { mutableStateOf(dependant?.numeroCNi ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (dependant == null) "Ajouter un membre" else "Modifier le membre", fontWeight = FontWeight.Black) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = prenoms,
                    onValueChange = { prenoms = it },
                    label = { Text("Prénoms") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = AppShapes.MediumRadius
                )
                OutlinedTextField(
                    value = nom,
                    onValueChange = { nom = it },
                    label = { Text("Nom") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = AppShapes.MediumRadius
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Sexe", fontSize = 12.sp, color = AppColors.TextSub)
                        Row {
                            FilterChip(
                                selected = sexe == "M",
                                onClick = { sexe = "M" },
                                label = { Text("M") }
                            )
                            Spacer(Modifier.width(8.dp))
                            FilterChip(
                                selected = sexe == "F",
                                onClick = { sexe = "F" },
                                label = { Text("F") }
                            )
                        }
                    }
                }

                Text("Lien de parenté", fontSize = 12.sp, color = AppColors.TextSub)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf("ENFANT", "CONJOINT", "PARENT", "AUTRE").forEach { type ->
                        FilterChip(
                            selected = lien == type,
                            onClick = { lien = type },
                            label = { Text(type, fontSize = 10.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                OutlinedTextField(
                    value = dateNaissance,
                    onValueChange = { dateNaissance = it },
                    label = { Text("Date naissance (AAAA-MM-JJ)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = AppShapes.MediumRadius
                )

                OutlinedTextField(
                    value = lieuNaissance,
                    onValueChange = { lieuNaissance = it },
                    label = { Text("Lieu de naissance") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = AppShapes.MediumRadius
                )

                OutlinedTextField(
                    value = adresse,
                    onValueChange = { adresse = it },
                    label = { Text("Adresse") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = AppShapes.MediumRadius
                )

                OutlinedTextField(
                    value = situationM,
                    onValueChange = { situationM = it },
                    label = { Text("Situation matrimoniale") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = AppShapes.MediumRadius
                )

                OutlinedTextField(
                    value = numeroExtrait,
                    onValueChange = { numeroExtrait = it },
                    label = { Text("Numéro Extrait de naissance") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = AppShapes.MediumRadius
                )

                OutlinedTextField(
                    value = numeroCNi,
                    onValueChange = { numeroCNi = it },
                    label = { Text("Numéro CNI (si disponible)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = AppShapes.MediumRadius
                )

                OutlinedTextField(
                    value = whatsapp,
                    onValueChange = { whatsapp = it },
                    label = { Text("Téléphone / WhatsApp") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = AppShapes.MediumRadius
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        PersonneChargeDto(
                            id = dependant?.id,
                            prenoms = prenoms,
                            nom = nom,
                            lienParent = lien,
                            sexe = sexe,
                            dateNaissance = dateNaissance,
                            lieuNaissance = lieuNaissance,
                            adresse = adresse,
                            situationM = situationM,
                            numeroExtrait = numeroExtrait,
                            whatsapp = whatsapp,
                            numeroCNi = numeroCNi,
                            photo = dependant?.photo,
                            photoRecto = dependant?.photoRecto,
                            photoVerso = dependant?.photoVerso
                        )
                    )
                },
                enabled = prenoms.isNotBlank() && nom.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = AppColors.BrandBlue),
                shape = AppShapes.MediumRadius
            ) {
                Text("Enregistrer", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        },
        containerColor = Color.White
    )
}

@Composable
private fun EmptyFoyerState() {
    Column(
        modifier = Modifier.padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier.size(80.dp),
            shape = CircleShape,
            color = AppColors.SurfaceAlt
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Rounded.GroupAdd, null,
                    tint = AppColors.TextDisabled,
                    modifier = Modifier.size(40.dp)
                )
            }
        }
        Spacer(Modifier.height(20.dp))
        Text(
            "Votre foyer est vide",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = AppColors.TextMain
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Ajoutez vos personnes en charge pour qu'elles bénéficient de votre couverture santé.",
            textAlign = TextAlign.Center,
            color = AppColors.TextSub,
            fontSize = 14.sp,
            lineHeight = 20.sp
        )
    }
}
