package com.example.sencsu.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Badge
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.FamilyRestroom
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Group
import androidx.compose.material.icons.rounded.GroupAdd
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.LocalHospital
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.MedicalServices
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.QrCode
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.sencsu.components.QrCodeImage
import com.example.sencsu.data.remote.dto.PersonneChargeDto
import com.example.sencsu.domain.viewmodel.BeneficiaryDashboardViewModel
import com.example.sencsu.theme.AppColors
import com.example.sencsu.theme.AppElevation
import com.example.sencsu.theme.AppGradients
import com.example.sencsu.theme.AppShapes
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun DependentsScreen(
    onNavigateToHistory: (String, String, String) -> Unit = { _, _, _ -> },
    onNavigateToDetails: (String, String) -> Unit = { _, _ -> },
    viewModel: BeneficiaryDashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val adherent = uiState.adherent
    val members = adherent?.personnesCharge ?: emptyList()
    var showContent by remember { mutableStateOf(false) }
    var showForm by remember { mutableStateOf(false) }
    var selectedMember by remember { mutableStateOf<PersonneChargeDto?>(null) }
    var moreOptionsMember by remember { mutableStateOf<PersonneChargeDto?>(null) }
    var deleteMember by remember { mutableStateOf<PersonneChargeDto?>(null) }

    LaunchedEffect(Unit) {
        delay(100)
        showContent = true
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        containerColor = AppColors.AppBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    selectedMember = null
                    showForm = true
                },
                containerColor = AppColors.BrandBlue,
                contentColor = Color.White,
                icon = { Icon(Icons.Rounded.Add, contentDescription = null) },
                text = { Text("Ajouter", fontWeight = FontWeight.Bold) }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(AppColors.AppBackground),
                contentPadding = PaddingValues(bottom = 128.dp)
            ) {
                item {
                    FoyerHero(
                        members = members,
                        isLoading = uiState.isLoading,
                        onAdd = {
                            selectedMember = null
                            showForm = true
                        }
                    )
                }

                item {
                    AnimatedVisibility(
                        visible = showContent,
                        enter = fadeIn(tween(300, delayMillis = 80)) + slideInVertically(
                            tween(300, delayMillis = 80),
                            initialOffsetY = { 32 }
                        )
                    ) {
                        FoyerSummaryCard(members = members)
                    }
                }

                if (uiState.isLoading && members.isEmpty()) {
                    item { LoadingFoyerState() }
                } else if (members.isEmpty()) {
                    item {
                        EmptyFoyerState(
                            onAdd = {
                                selectedMember = null
                                showForm = true
                            }
                        )
                    }
                } else {
                    item {
                        SectionHeader(
                            title = "Membres couverts",
                            subtitle = "Gestion des personnes rattachees a votre couverture"
                        )
                    }

                    itemsIndexed(
                        items = members,
                        key = { index, member -> member.id ?: "$index-${member.displayName}" }
                    ) { index, member ->
                        AnimatedVisibility(
                            visible = showContent,
                            enter = fadeIn(tween(260, delayMillis = 120 + index * 45)) +
                                    slideInVertically(
                                        tween(260, delayMillis = 120 + index * 45),
                                        initialOffsetY = { 28 }
                                    )
                        ) {
                            FoyerMemberCard(
                                member = member,
                                onClick = {
                                    adherent?.id?.let { adherentId ->
                                        member.id?.let { pcId -> onNavigateToDetails(adherentId, pcId) }
                                    }
                                },
                                onEdit = {
                                    selectedMember = member
                                    showForm = true
                                },
                                onHistory = {
                                    adherent?.id?.let { adherentId ->
                                        member.id?.let { pcId ->
                                            onNavigateToHistory(adherentId, pcId, member.displayName)
                                        }
                                    }
                                },
                                onMore = { moreOptionsMember = member }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showForm) {
        DependantFormDialog(
            dependant = selectedMember,
            isLoading = uiState.isLoading,
            onDismiss = { showForm = false },
            onConfirm = { updated ->
                if (selectedMember == null) {
                    viewModel.addPersonneCharge(updated)
                    scope.launch { snackbarHostState.showSnackbar("Ajout en cours...") }
                } else {
                    selectedMember?.id?.let { viewModel.updatePersonneCharge(it, updated) }
                    scope.launch { snackbarHostState.showSnackbar("Modification en cours...") }
                }
                showForm = false
            }
        )
    }

    moreOptionsMember?.let { member ->
        MemberOptionsDialog(
            member = member,
            onDismiss = { moreOptionsMember = null },
            onDetails = {
                moreOptionsMember = null
                adherent?.id?.let { adherentId ->
                    member.id?.let { pcId -> onNavigateToDetails(adherentId, pcId) }
                }
            },
            onHistory = {
                moreOptionsMember = null
                adherent?.id?.let { adherentId ->
                    member.id?.let { pcId -> onNavigateToHistory(adherentId, pcId, member.displayName) }
                }
            },
            onEdit = {
                moreOptionsMember = null
                selectedMember = member
                showForm = true
            },
            onDelete = {
                moreOptionsMember = null
                deleteMember = member
            }
        )
    }

    deleteMember?.let { member ->
        AlertDialog(
            onDismissRequest = { deleteMember = null },
            icon = { Icon(Icons.Rounded.Delete, null, tint = AppColors.StatusRed) },
            title = { Text("Retirer du foyer", fontWeight = FontWeight.Black) },
            text = { Text("Voulez-vous retirer ${member.displayName} de votre couverture sante ?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        member.id?.let { viewModel.deletePersonneCharge(it) }
                        deleteMember = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = AppColors.StatusRed)
                ) {
                    Text("Retirer", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteMember = null }) {
                    Text("Annuler")
                }
            },
            containerColor = AppColors.SurfaceBackground,
            shape = AppShapes.LargeRadius
        )
    }
}

@Composable
private fun FoyerHero(
    members: List<PersonneChargeDto>,
    isLoading: Boolean,
    onAdd: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(258.dp)
            .background(Brush.verticalGradient(AppGradients.Brand))
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 18.dp)
    ) {
        Box(
            modifier = Modifier
                .size(150.dp)
                .align(Alignment.TopEnd)
                .offset(x = 46.dp, y = (-48).dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.06f))
        )
        Box(
            modifier = Modifier
                .size(92.dp)
                .align(Alignment.BottomStart)
                .offset(x = (-28).dp, y = 22.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.05f))
        )

        Column(
            modifier = Modifier.align(Alignment.CenterStart),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                shape = AppShapes.MediumRadius,
                color = Color.White.copy(alpha = 0.16f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.18f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Rounded.Security, null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(7.dp))
                    Text("Couverture familiale", color = Color.White, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    "Mon foyer",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
                Text(
                    if (members.isEmpty()) "Ajoutez les membres couverts par votre dossier"
                    else "${members.size} personne(s) rattachee(s) a votre couverture",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.78f),
                    lineHeight = 20.sp
                )
            }

            Button(
                onClick = onAdd,
                shape = AppShapes.MediumRadius,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = AppColors.BrandBlue
                )
            ) {
                Icon(Icons.Rounded.GroupAdd, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Ajouter un membre", fontWeight = FontWeight.Black)
            }
        }

        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.TopEnd).padding(top = 58.dp).size(22.dp),
                color = Color.White,
                strokeWidth = 2.dp
            )
        }
    }
}

@Composable
private fun FoyerSummaryCard(members: List<PersonneChargeDto>) {
    val enfants = members.count { it.lienParent?.contains("ENFANT", ignoreCase = true) == true }
    val conjoints = members.count {
        val lien = it.lienParent.orEmpty()
        lien.contains("CONJOINT", ignoreCase = true) ||
                lien.contains("EPOUX", ignoreCase = true) ||
                lien.contains("EPOUSE", ignoreCase = true)
    }
    val withCards = members.count { !it.matricule.isNullOrBlank() }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .offset(y = (-22).dp)
            .padding(horizontal = 16.dp),
        shape = AppShapes.LargeRadius,
        colors = CardDefaults.cardColors(containerColor = AppColors.SurfaceBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = AppElevation.cardRaised),
        border = BorderStroke(1.dp, AppColors.BorderColorLight)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SummaryMetric("Total", members.size.toString(), Icons.Rounded.Group, AppColors.BrandBlue, Modifier.weight(1f))
            SummaryMetric("Enfants", enfants.toString(), Icons.Rounded.FamilyRestroom, AppColors.ActionBlue, Modifier.weight(1f))
            SummaryMetric("Conjoints", conjoints.toString(), Icons.Rounded.Favorite, AppColors.StatusRed, Modifier.weight(1f))
            SummaryMetric("Cartes", withCards.toString(), Icons.Rounded.Badge, AppColors.GoldAccent, Modifier.weight(1f))
        }
    }
}

@Composable
private fun SummaryMetric(
    label: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Surface(shape = CircleShape, color = color.copy(alpha = 0.1f)) {
            Icon(icon, null, tint = color, modifier = Modifier.padding(8.dp).size(18.dp))
        }
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = AppColors.TextMain)
        Text(label, style = MaterialTheme.typography.labelSmall, color = AppColors.TextSub, maxLines = 1)
    }
}

@Composable
private fun SectionHeader(title: String, subtitle: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(top = 2.dp, bottom = 10.dp)
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = AppColors.TextMain)
        Spacer(Modifier.height(2.dp))
        Text(subtitle, style = MaterialTheme.typography.bodySmall, color = AppColors.TextSub)
    }
}

@Composable
private fun FoyerMemberCard(
    member: PersonneChargeDto,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onHistory: () -> Unit,
    onMore: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable(onClick = onClick),
        shape = AppShapes.LargeRadius,
        colors = CardDefaults.cardColors(containerColor = AppColors.SurfaceBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = AppElevation.card),
        border = BorderStroke(1.dp, AppColors.BorderColorLight)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                MemberAvatar(member = member)
                Spacer(Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        member.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = AppColors.TextMain,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(5.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RelationBadge(member.lienParent.orDash())
                        if (!member.matricule.isNullOrBlank()) {
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "#${member.matricule}",
                                style = MaterialTheme.typography.labelSmall,
                                color = AppColors.TextSub,
                                maxLines = 1
                            )
                        }
                    }
                }
                IconButton(onClick = onMore) {
                    Icon(Icons.Rounded.MoreHoriz, contentDescription = "Autres options", tint = AppColors.TextSub)
                }
            }

            HorizontalDivider(color = AppColors.BorderColorLight)

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    CompactInfo(Icons.Rounded.CalendarToday, "Naissance", member.dateNaissance.orDash())
                    CompactInfo(Icons.Rounded.LocationOn, "Lieu", member.lieuNaissance.orDash())
                    CompactInfo(Icons.Rounded.Phone, "Contact", member.whatsapp.orDash())
                }

                QrPreview(member = member)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MemberActionButton(
                    label = "Modifier",
                    icon = Icons.Rounded.Edit,
                    color = AppColors.BrandBlue,
                    onClick = onEdit,
                    modifier = Modifier.weight(1f)
                )
                MemberActionButton(
                    label = "Historique",
                    icon = Icons.Rounded.MedicalServices,
                    color = AppColors.ActionBlue,
                    onClick = onHistory,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun MemberAvatar(member: PersonneChargeDto) {
    val color = when (member.sexe?.uppercase()) {
        "F" -> AppColors.StatusRed
        else -> AppColors.BrandBlue
    }

    Surface(
        modifier = Modifier.size(58.dp),
        shape = CircleShape,
        color = color.copy(alpha = 0.1f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.18f))
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                member.initials,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = color
            )
        }
    }
}

@Composable
private fun RelationBadge(label: String) {
    Surface(
        shape = AppShapes.SmallRadius,
        color = AppColors.GoldLight,
        border = BorderStroke(1.dp, AppColors.GoldAccent.copy(alpha = 0.16f))
    ) {
        Text(
            label.uppercase(),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Black,
            color = AppColors.GoldAccent,
            maxLines = 1
        )
    }
}

@Composable
private fun CompactInfo(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = AppColors.TextDisabled, modifier = Modifier.size(15.dp))
        Spacer(Modifier.width(7.dp))
        Text(
            "$label: ",
            style = MaterialTheme.typography.labelSmall,
            color = AppColors.TextSub,
            fontWeight = FontWeight.Bold
        )
        Text(
            value,
            style = MaterialTheme.typography.labelSmall,
            color = AppColors.TextMain,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun QrPreview(member: PersonneChargeDto) {
    Surface(
        modifier = Modifier.size(76.dp),
        shape = AppShapes.MediumRadius,
        color = AppColors.SurfaceMuted,
        border = BorderStroke(1.dp, AppColors.BorderColorLight)
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (member.matricule.isNullOrBlank()) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Rounded.QrCode, null, tint = AppColors.TextDisabled, modifier = Modifier.size(24.dp))
                    Text("Aucun", style = MaterialTheme.typography.labelSmall, color = AppColors.TextDisabled)
                }
            } else {
                QrCodeImage(
                    value = com.example.sencsu.components.buildBeneficiaryQrUrl(member.matricule!!),
                    modifier = Modifier.padding(8.dp).fillMaxSize()
                )
            }
        }
    }
}

@Composable
private fun MemberActionButton(
    label: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(42.dp),
        shape = AppShapes.MediumRadius,
        color = color.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.12f))
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(17.dp))
            Spacer(Modifier.width(7.dp))
            Text(label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@Composable
private fun MemberOptionsDialog(
    member: PersonneChargeDto,
    onDismiss: () -> Unit,
    onDetails: () -> Unit,
    onHistory: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Rounded.MoreHoriz, null, tint = AppColors.BrandBlue) },
        title = { Text(member.displayName, fontWeight = FontWeight.Black) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                OptionRow(Icons.Rounded.Info, "Voir les details", "Dossier complet du membre", onDetails)
                OptionRow(Icons.Rounded.History, "Historique medical", "Soins et remboursements", onHistory)
                OptionRow(Icons.Rounded.Edit, "Modifier", "Mettre a jour les informations", onEdit)
                OptionRow(Icons.Rounded.Delete, "Retirer du foyer", "Supprimer cette personne", onDelete, danger = true)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Fermer")
            }
        },
        containerColor = AppColors.SurfaceBackground,
        shape = AppShapes.LargeRadius
    )
}

@Composable
private fun OptionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    danger: Boolean = false
) {
    val color = if (danger) AppColors.StatusRed else AppColors.TextMain
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(AppShapes.MediumRadius)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(shape = AppShapes.MediumRadius, color = if (danger) AppColors.StatusRedSoft else AppColors.SurfaceAlt) {
            Icon(icon, null, tint = if (danger) AppColors.StatusRed else AppColors.BrandBlue, modifier = Modifier.padding(9.dp).size(19.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold, color = color, style = MaterialTheme.typography.bodyMedium)
            Text(subtitle, color = AppColors.TextSub, style = MaterialTheme.typography.labelSmall)
        }
        Icon(Icons.Rounded.KeyboardArrowRight, null, tint = AppColors.TextDisabled)
    }
}

@Composable
private fun DependantFormDialog(
    dependant: PersonneChargeDto?,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (PersonneChargeDto) -> Unit
) {
    var prenoms by remember(dependant) { mutableStateOf(dependant?.prenoms.orEmpty()) }
    var nom by remember(dependant) { mutableStateOf(dependant?.nom.orEmpty()) }
    var lien by remember(dependant) { mutableStateOf(dependant?.lienParent?.ifBlank { "ENFANT" } ?: "ENFANT") }
    var sexe by remember(dependant) { mutableStateOf(dependant?.sexe?.ifBlank { "M" } ?: "M") }
    var dateNaissance by remember(dependant) { mutableStateOf(dependant?.dateNaissance.orEmpty()) }
    var lieuNaissance by remember(dependant) { mutableStateOf(dependant?.lieuNaissance.orEmpty()) }
    var adresse by remember(dependant) { mutableStateOf(dependant?.adresse.orEmpty()) }
    var situationM by remember(dependant) { mutableStateOf(dependant?.situationM.orEmpty()) }
    var numeroExtrait by remember(dependant) { mutableStateOf(dependant?.numeroExtrait.orEmpty()) }
    var whatsapp by remember(dependant) { mutableStateOf(dependant?.whatsapp.orEmpty()) }
    var numeroCNi by remember(dependant) { mutableStateOf(dependant?.numeroCNi.orEmpty()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(if (dependant == null) Icons.Rounded.GroupAdd else Icons.Rounded.Edit, null, tint = AppColors.BrandBlue) },
        title = { Text(if (dependant == null) "Ajouter un membre" else "Modifier le membre", fontWeight = FontWeight.Black) },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 500.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FormField("Prenoms", prenoms) { prenoms = it }
                FormField("Nom", nom) { nom = it }

                Text("Sexe", style = MaterialTheme.typography.labelMedium, color = AppColors.TextSub)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = sexe == "M", onClick = { sexe = "M" }, label = { Text("Masculin") })
                    FilterChip(selected = sexe == "F", onClick = { sexe = "F" }, label = { Text("Feminin") })
                }

                Text("Lien de parente", style = MaterialTheme.typography.labelMedium, color = AppColors.TextSub)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                    listOf("ENFANT", "CONJOINT", "PARENT", "AUTRE").forEach { type ->
                        FilterChip(
                            selected = lien == type,
                            onClick = { lien = type },
                            label = { Text(type, fontSize = 10.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                FormField("Date naissance (AAAA-MM-JJ)", dateNaissance) { dateNaissance = it }
                FormField("Lieu de naissance", lieuNaissance) { lieuNaissance = it }
                FormField("Adresse", adresse) { adresse = it }
                FormField("Situation matrimoniale", situationM) { situationM = it }
                FormField("Numero extrait", numeroExtrait) { numeroExtrait = it }
                FormField("Numero CNI", numeroCNi) { numeroCNi = it }
                FormField("Telephone / WhatsApp", whatsapp) { whatsapp = it }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        PersonneChargeDto(
                            id = dependant?.id,
                            prenoms = prenoms.trim(),
                            nom = nom.trim(),
                            lienParent = lien,
                            sexe = sexe,
                            dateNaissance = dateNaissance.trim().ifEmpty { null },
                            lieuNaissance = lieuNaissance.trim().ifEmpty { null },
                            adresse = adresse.trim().ifEmpty { null },
                            situationM = situationM.trim().ifEmpty { null },
                            numeroExtrait = numeroExtrait.trim().ifEmpty { null },
                            whatsapp = whatsapp.trim().ifEmpty { null },
                            numeroCNi = numeroCNi.trim().ifEmpty { null },
                            photo = dependant?.photo,
                            photoRecto = dependant?.photoRecto,
                            photoVerso = dependant?.photoVerso,
                            matricule = dependant?.matricule,
                            createdAt = dependant?.createdAt
                        )
                    )
                },
                enabled = !isLoading && prenoms.isNotBlank() && nom.isNotBlank(),
                shape = AppShapes.MediumRadius
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = Color.White)
                } else {
                    Icon(Icons.Rounded.Save, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Enregistrer", fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isLoading) {
                Text("Annuler")
            }
        },
        containerColor = AppColors.SurfaceBackground,
        shape = AppShapes.LargeRadius
    )
}

@Composable
private fun FormField(label: String, value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        shape = AppShapes.MediumRadius,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = AppColors.BrandBlue,
            unfocusedBorderColor = AppColors.BorderColor,
            focusedLabelColor = AppColors.BrandBlue,
            cursorColor = AppColors.BrandBlue,
            focusedContainerColor = AppColors.SurfaceMuted,
            unfocusedContainerColor = AppColors.SurfaceMuted
        )
    )
}

@Composable
private fun LoadingFoyerState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 64.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(color = AppColors.BrandBlue)
        Spacer(Modifier.height(16.dp))
        Text("Mise a jour du foyer...", color = AppColors.TextSub)
    }
}

@Composable
private fun EmptyFoyerState(onAdd: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = AppShapes.LargeRadius,
        colors = CardDefaults.cardColors(containerColor = AppColors.SurfaceBackground),
        border = BorderStroke(1.dp, AppColors.BorderColorLight)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(modifier = Modifier.size(82.dp), shape = CircleShape, color = AppColors.BrandBlueLite) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Rounded.GroupAdd, null, tint = AppColors.BrandBlue, modifier = Modifier.size(38.dp))
                }
            }
            Spacer(Modifier.height(18.dp))
            Text("Votre foyer est vide", fontWeight = FontWeight.Black, fontSize = 20.sp, color = AppColors.TextMain)
            Spacer(Modifier.height(8.dp))
            Text(
                "Ajoutez vos personnes en charge pour leur donner un acces clair a votre couverture sante.",
                textAlign = TextAlign.Center,
                color = AppColors.TextSub,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
            Spacer(Modifier.height(18.dp))
            Button(onClick = onAdd, shape = AppShapes.MediumRadius) {
                Icon(Icons.Rounded.Add, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Ajouter un membre", fontWeight = FontWeight.Bold)
            }
        }
    }
}

private fun String?.orDash(): String = this?.takeIf { it.isNotBlank() } ?: "-"
