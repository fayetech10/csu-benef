package com.example.sencsu.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.HelpOutline
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.Badge
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.ImageNotSupported
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.MedicalServices
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material.icons.rounded.Work
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.runtime.DisposableEffect
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import coil.compose.SubcomposeAsyncImage
import com.example.sencsu.R
import com.example.sencsu.components.ServerImage
import com.example.sencsu.configs.ApiConfig
import com.example.sencsu.data.remote.dto.AdherentDto
import com.example.sencsu.domain.viewmodel.BeneficiaryDashboardViewModel
import com.example.sencsu.theme.AppColors
import com.example.sencsu.theme.AppElevation
import com.example.sencsu.theme.AppGradients
import com.example.sencsu.theme.AppShapes
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateToHistory: (String) -> Unit = {},
    onNavigateToEdit: (String) -> Unit = {},
    viewModel: BeneficiaryDashboardViewModel = hiltViewModel(),
    onLogout: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val adherent = uiState.adherent
    val currentUser by viewModel.sessionManager.userFlow.collectAsState(initial = null)
    val isAgent = currentUser?.role == "AGENT"
    
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var showContent by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    var showMoreDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var selectedViewerImage by remember { mutableStateOf<String?>(null) }
    var infoDialog by remember { mutableStateOf<ProfileInfoDialog?>(null) }

    val fullName = if (isAgent) {
        "${currentUser?.prenom.orEmpty()} ${currentUser?.name.orEmpty()}".trim().ifEmpty { "Profil Agent" }
    } else {
        "${adherent?.prenoms.orEmpty()} ${adherent?.nom.orEmpty()}".trim().ifEmpty { "Profil beneficiaire" }
    }

    val initials = fullName.split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .mapNotNull { it.firstOrNull()?.uppercase() }
        .joinToString("")
        .ifEmpty { "?" }

    // Refresh automatique quand l'écran revient au premier plan (ex: après une modification)
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                viewModel.refresh()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(Unit) {
        delay(100)
        showContent = true
    }

    Scaffold(
        containerColor = AppColors.AppBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .background(AppColors.AppBackground)
        ) {
            ProfileHero(
                adherent = adherent,
                fullName = fullName,
                initials = initials,
                showContent = showContent,
                isLoading = uiState.isLoading,
                sessionManager = viewModel.sessionManager,
                onEditClick = { adherent?.id?.let(onNavigateToEdit) },
                onMoreClick = { showMoreDialog = true }
            )

            Column(
                modifier = Modifier
                    .offset(y = (-22).dp)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                AnimatedVisibility(
                    visible = showContent && !isAgent,
                    enter = fadeIn(tween(350, delayMillis = 120)) + slideInVertically(
                        tween(350, delayMillis = 120),
                        initialOffsetY = { 40 }
                    )
                ) {
                    ProfileOverviewCard(adherent = adherent)
                }

                ProfileActionGrid(
                    isAgent = isAgent,
                    onEdit = { adherent?.id?.let(onNavigateToEdit) },
                    onHistory = { adherent?.matricule?.let(onNavigateToHistory) },
                    onPassword = { showPasswordDialog = true },
                    onMore = { showMoreDialog = true }
                )

                ProfileSection(title = "Informations personnelles") {
                    if (!isAgent) {
                        ProfileInfoRow(Icons.Rounded.Badge, "Matricule", adherent?.matricule.orDash())
                        HorizontalDivider(color = AppColors.BorderColorLight)
                    }
                    ProfileInfoRow(Icons.Rounded.Phone, "Telephone", if (isAgent) currentUser?.telephone.orDash() else adherent?.whatsapp.orDash())
                    if (!isAgent) {
                        HorizontalDivider(color = AppColors.BorderColorLight)
                        ProfileInfoRow(Icons.Rounded.CreditCard, "NIN / Piece", adherent?.numeroCNi.orDash())
                        HorizontalDivider(color = AppColors.BorderColorLight)
                        ProfileInfoRow(Icons.Rounded.CalendarToday, "Date de naissance", adherent?.dateNaissance.orDash())
                        HorizontalDivider(color = AppColors.BorderColorLight)
                        ProfileInfoRow(Icons.Rounded.Home, "Adresse", adherent?.adresse.orDash())
                    }
                }

                if (!isAgent) {
                    ProfileSection(title = "Localisation et adhesion") {
                        ProfileInfoRow(Icons.Rounded.Map, "Region", adherent?.region.orDash())
                        HorizontalDivider(color = AppColors.BorderColorLight)
                        ProfileInfoRow(Icons.Rounded.LocationOn, "Commune", adherent?.commune.orDash())
                        HorizontalDivider(color = AppColors.BorderColorLight)
                        ProfileInfoRow(Icons.Rounded.Work, "Secteur", adherent?.secteurActivite.orDash())
                        HorizontalDivider(color = AppColors.BorderColorLight)
                        ProfileInfoRow(Icons.Rounded.Category, "Type beneficiaire", adherent?.typeBenef.orDash())
                    }

                    DocumentsSection(
                        rectoFilename = adherent?.photoRecto,
                        versoFilename = adherent?.photoVerso,
                        sessionManager = viewModel.sessionManager,
                        onOpen = { selectedViewerImage = it }
                    )
                }

                ProfileSection(title = "Parametres") {
                    if (!isAgent) {
                        SettingsItem(
                            icon = Icons.Rounded.MedicalServices,
                            title = "Historique medical",
                            subtitle = "Consulter les soins et remboursements",
                            onClick = { adherent?.matricule?.let(onNavigateToHistory) }
                        )
                    }
                    SettingsItem(
                        icon = Icons.Rounded.Lock,
                        title = "Modifier le mot de passe",
                        subtitle = "Mettre a jour la securite du compte",
                        onClick = { showPasswordDialog = true }
                    )
                    SettingsItem(
                        icon = Icons.Rounded.Security,
                        title = "Confidentialite",
                        subtitle = "Voir les regles de protection des donnees",
                        onClick = {
                            infoDialog = ProfileInfoDialog(
                                "Confidentialite",
                                "Vos donnees servent uniquement a la gestion de votre couverture sante, de vos pieces et de vos droits."
                            )
                        }
                    )
                    SettingsItem(
                        icon = Icons.AutoMirrored.Rounded.HelpOutline,
                        title = "Aide et support",
                        subtitle = "Obtenir de l'aide sur votre dossier",
                        onClick = {
                            infoDialog = ProfileInfoDialog(
                                "Aide et support",
                                "Pour toute correction non disponible dans l'application, contactez votre agent CMU ou le support SenCSU."
                            )
                        }
                    )
                }

                OutlinedButton(
                    onClick = { showLogoutDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = AppShapes.MediumRadius,
                    border = BorderStroke(1.dp, AppColors.StatusRed.copy(alpha = 0.28f)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = AppColors.StatusRedSoft,
                        contentColor = AppColors.StatusRed
                    )
                ) {
                    Icon(Icons.AutoMirrored.Rounded.Logout, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Se deconnecter", fontWeight = FontWeight.Bold)
                }

                Text(
                    "SenCSU 2026\nCouverture Sante Universelle - Senegal",
                    style = MaterialTheme.typography.labelSmall,
                    color = AppColors.TextDisabled,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 112.dp)
                )
            }
        }
    }


    if (showPasswordDialog) {
        PasswordDialog(
            isLoading = uiState.isLoading,
            onDismiss = { showPasswordDialog = false },
            onSave = { oldPassword, newPassword ->
                viewModel.updatePassword(oldPassword, newPassword) { success, message ->
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            if (success) "Mot de passe mis a jour" else message ?: "Mise a jour impossible"
                        )
                    }
                    if (success) showPasswordDialog = false
                }
            }
        )
    }

    if (showMoreDialog) {
        MoreOptionsDialog(
            onDismiss = { showMoreDialog = false },
            onAbout = {
                showMoreDialog = false
                infoDialog = ProfileInfoDialog("A propos", "SenCSU version 1.0.0")
            },
            onRefresh = {
                showMoreDialog = false
                viewModel.refresh()
            }
        )
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            icon = { Icon(Icons.AutoMirrored.Rounded.Logout, null, tint = AppColors.StatusRed) },
            title = { Text("Deconnexion", fontWeight = FontWeight.Bold) },
            text = { Text("Voulez-vous quitter votre espace beneficiaire ?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        onLogout()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = AppColors.StatusRed)
                ) {
                    Text("Deconnecter", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Annuler")
                }
            },
            containerColor = AppColors.SurfaceBackground,
            shape = AppShapes.LargeRadius
        )
    }

    infoDialog?.let { dialog ->
        AlertDialog(
            onDismissRequest = { infoDialog = null },
            icon = { Icon(Icons.Rounded.Info, null, tint = AppColors.BrandBlue) },
            title = { Text(dialog.title, fontWeight = FontWeight.Bold) },
            text = { Text(dialog.message) },
            confirmButton = {
                TextButton(onClick = { infoDialog = null }) {
                    Text("OK")
                }
            },
            containerColor = AppColors.SurfaceBackground,
            shape = AppShapes.LargeRadius
        )
    }

    selectedViewerImage?.let { imageUrl ->
        ImageViewerDialog(
            imageUrl = imageUrl,
            onDismiss = { selectedViewerImage = null }
        )
    }
}

@Composable
private fun ProfileHero(
    adherent: AdherentDto?,
    fullName: String,
    initials: String,
    showContent: Boolean,
    isLoading: Boolean,
    sessionManager: com.example.sencsu.data.repository.SessionManager,
    onEditClick: () -> Unit,
    onMoreClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // ── Cover Photo Section ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            // Cover Image (full color, full width)
            Image(
                painter = painterResource(id = R.drawable.logo_sencsu),
                contentDescription = "Photo de couverture SenCSU",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Bottom gradient overlay for smooth transition
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color(0xFF0D1B2A)
                            )
                        )
                    )
            )

            // Top bar overlay (status bar + actions)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Mon profil",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    IconButton(
                        onClick = onEditClick,
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color.Black.copy(alpha = 0.35f), CircleShape)
                    ) {
                        Icon(Icons.Rounded.Edit, contentDescription = "Modifier", tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                    IconButton(
                        onClick = onMoreClick,
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color.Black.copy(alpha = 0.35f), CircleShape)
                    ) {
                        Icon(Icons.Rounded.MoreHoriz, contentDescription = "Autres options", tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }

        // ── User Info Section (overlaps cover photo) ──
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = (-36).dp),
            color = Color.Transparent
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                // Background card
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 36.dp),
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 8.dp
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 52.dp, bottom = 20.dp, start = 20.dp, end = 20.dp)
                        ) {
                            Text(
                                fullName,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(Modifier.height(6.dp))
                            StatusBadge(
                                label = if (adherent?.actif == true) "Compte actif" else "Compte inactif",
                                active = adherent?.actif == true
                            )
                            if (isLoading) {
                                Spacer(Modifier.height(10.dp))
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = AppColors.BrandBlue
                                )
                            }
                        }
                    }
                }

                // Avatar overlapping the cover/card boundary
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.TopCenter
                ) {
                    ProfileAvatar(
                        filename = adherent?.photo,
                        initials = initials,
                        sessionManager = sessionManager
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileAvatar(filename: String?, initials: String, sessionManager: com.example.sencsu.data.repository.SessionManager) {
    Surface(
        shape = CircleShape,
        color = Color.White.copy(alpha = 0.18f),
        border = BorderStroke(2.dp, Color.White.copy(alpha = 0.32f)),
        modifier = Modifier.size(100.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = AppColors.SurfaceBackground,
            modifier = Modifier.padding(6.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (filename != null) {
                    ServerImage(
                        filename = filename,
                        sessionManager = sessionManager,
                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    AvatarInitials(initials)
                }
            }
        }
    }
}

@Composable
private fun AvatarInitials(initials: String) {
    Text(
        initials,
        fontSize = 30.sp,
        fontWeight = FontWeight.Black,
        color = AppColors.BrandBlue
    )
}

@Composable
private fun StatusBadge(label: String, active: Boolean) {
    Surface(
        shape = AppShapes.CircleRadius,
        color = if (active) AppColors.StatusGreenSoft else AppColors.StatusRedSoft,
        border = BorderStroke(1.dp, if (active) AppColors.StatusGreen.copy(alpha = 0.22f) else AppColors.StatusRed.copy(alpha = 0.22f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (active) Icons.Rounded.CheckCircle else Icons.Rounded.ErrorOutline,
                contentDescription = null,
                tint = if (active) AppColors.StatusGreen else AppColors.StatusRed,
                modifier = Modifier.size(14.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = if (active) AppColors.StatusGreen else AppColors.StatusRed
            )
        }
    }
}

@Composable
private fun ProfileOverviewCard(adherent: AdherentDto?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = AppShapes.LargeRadius,
        colors = CardDefaults.cardColors(containerColor = AppColors.SurfaceBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = AppElevation.cardRaised),
        border = BorderStroke(1.dp, AppColors.BorderColorLight)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OverviewMetric("Regime", adherent?.regime.orDash(), Icons.Rounded.VerifiedUser, Modifier.weight(1f))
            OverviewMetric("Ayants droit", adherent?.personnesCharge?.size?.toString() ?: "0", Icons.Rounded.Groups, Modifier.weight(1f))
            OverviewMetric("Carte", adherent?.numeroCarte.orDash(), Icons.Rounded.CreditCard, Modifier.weight(1f))
        }
    }
}

@Composable
private fun OverviewMetric(label: String, value: String, icon: ImageVector, modifier: Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Surface(shape = AppShapes.MediumRadius, color = AppColors.BrandBlueLite) {
            Icon(
                icon,
                contentDescription = null,
                tint = AppColors.BrandBlue,
                modifier = Modifier.padding(8.dp).size(18.dp)
            )
        }
        Text(label, style = MaterialTheme.typography.labelSmall, color = AppColors.TextSub)
        Text(
            value,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Black,
            color = AppColors.TextMain,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun ProfileActionGrid(
    isAgent: Boolean,
    onEdit: () -> Unit,
    onHistory: () -> Unit,
    onPassword: () -> Unit,
    onMore: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (!isAgent) {
            QuickAction("Modifier", Icons.Rounded.Edit, AppColors.BrandBlue, onEdit, Modifier.weight(1f))
            QuickAction("Historique", Icons.Rounded.MedicalServices, AppColors.ActionBlue, onHistory, Modifier.weight(1f))
        }
        QuickAction("Securite", Icons.Rounded.Lock, AppColors.StatusOrange, onPassword, Modifier.weight(1f))
        QuickAction("Autres", Icons.Rounded.MoreHoriz, AppColors.TextSub, onMore, Modifier.weight(1f))
    }
}

@Composable
private fun QuickAction(
    label: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(82.dp),
        shape = AppShapes.LargeRadius,
        colors = CardDefaults.cardColors(containerColor = AppColors.SurfaceBackground),
        border = BorderStroke(1.dp, AppColors.BorderColorLight)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(shape = CircleShape, color = color.copy(alpha = 0.1f)) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.padding(7.dp).size(18.dp))
            }
            Spacer(Modifier.height(6.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = AppColors.TextMain)
        }
    }
}

@Composable
private fun ProfileSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Black,
            color = AppColors.TextMain,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = AppShapes.LargeRadius,
            colors = CardDefaults.cardColors(containerColor = AppColors.SurfaceBackground),
            border = BorderStroke(1.dp, AppColors.BorderColorLight),
            elevation = CardDefaults.cardElevation(defaultElevation = AppElevation.card)
        ) {
            Column(modifier = Modifier.padding(14.dp), content = content)
        }
    }
}

@Composable
private fun ProfileInfoRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(shape = AppShapes.MediumRadius, color = AppColors.SurfaceAlt, modifier = Modifier.size(42.dp)) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = AppColors.BrandBlue, modifier = Modifier.size(20.dp))
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = AppColors.TextSub)
            Text(
                value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = AppColors.TextMain,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun DocumentsSection(rectoFilename: String?, versoFilename: String?, sessionManager: com.example.sencsu.data.repository.SessionManager, onOpen: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            "Documents",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Black,
            color = AppColors.TextMain,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            DocumentPhotoCard("Piece recto", rectoFilename, sessionManager, onOpen, Modifier.weight(1f))
            DocumentPhotoCard("Piece verso", versoFilename, sessionManager, onOpen, Modifier.weight(1f))
        }
    }
}

@Composable
private fun DocumentPhotoCard(
    title: String,
    filename: String?,
    sessionManager: com.example.sencsu.data.repository.SessionManager,
    onOpen: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(126.dp),
        shape = AppShapes.LargeRadius,
        colors = CardDefaults.cardColors(containerColor = AppColors.SurfaceBackground),
        border = BorderStroke(1.dp, AppColors.BorderColorLight)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (filename != null) {
                ServerImage(
                    filename = filename,
                    sessionManager = sessionManager,
                    modifier = Modifier.fillMaxSize().clickable { 
                        ApiConfig.getImageUrl(filename)?.let { onOpen(it) } 
                    },
                    contentScale = ContentScale.Crop
                )
            } else {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Rounded.ImageNotSupported, contentDescription = null, tint = AppColors.TextDisabled)
                    Spacer(Modifier.height(4.dp))
                    Text("Non disponible", style = MaterialTheme.typography.labelSmall, color = AppColors.TextDisabled)
                }
            }
            Surface(
                modifier = Modifier.align(Alignment.BottomStart).padding(8.dp),
                color = AppColors.TextMain.copy(alpha = 0.72f),
                shape = AppShapes.SmallRadius
            ) {
                Text(
                    title,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun SettingsItem(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(AppShapes.MediumRadius)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(shape = AppShapes.MediumRadius, color = AppColors.SurfaceAlt, modifier = Modifier.size(42.dp)) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = AppColors.TextSub, modifier = Modifier.size(20.dp))
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = AppColors.TextMain)
            Text(subtitle, style = MaterialTheme.typography.labelSmall, color = AppColors.TextSub)
        }
        Icon(Icons.Rounded.KeyboardArrowRight, contentDescription = null, tint = AppColors.TextDisabled)
    }
}

@Composable
private fun EditProfileDialog(
    adherent: AdherentDto,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onSave: (AdherentDto) -> Unit
) {
    var prenoms by remember(adherent) { mutableStateOf(adherent.prenoms.orEmpty()) }
    var nom by remember(adherent) { mutableStateOf(adherent.nom.orEmpty()) }
    var phone by remember(adherent) { mutableStateOf(adherent.whatsapp.orEmpty()) }
    var address by remember(adherent) { mutableStateOf(adherent.adresse.orEmpty()) }
    var region by remember(adherent) { mutableStateOf(adherent.region.orEmpty()) }
    var departement by remember(adherent) { mutableStateOf(adherent.departement.orEmpty()) }
    var commune by remember(adherent) { mutableStateOf(adherent.commune.orEmpty()) }
    var secteur by remember(adherent) { mutableStateOf(adherent.secteurActivite.orEmpty()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Rounded.Edit, null, tint = AppColors.BrandBlue) },
        title = { Text("Modifier le profil", fontWeight = FontWeight.Black) },
        text = {
            Column(
                modifier = Modifier.heightIn(max = 460.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ProfileTextField("Prenoms", prenoms) { prenoms = it }
                ProfileTextField("Nom", nom) { nom = it }
                ProfileTextField("Telephone", phone) { phone = it }
                ProfileTextField("Adresse", address) { address = it }
                ProfileTextField("Region", region) { region = it }
                ProfileTextField("Departement", departement) { departement = it }
                ProfileTextField("Commune", commune) { commune = it }
                ProfileTextField("Secteur activite", secteur) { secteur = it }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        adherent.copy(
                            prenoms = prenoms.trim(),
                            nom = nom.trim(),
                            whatsapp = phone.trim(),
                            adresse = address.trim(),
                            region = region.trim(),
                            departement = departement.trim(),
                            commune = commune.trim(),
                            secteurActivite = secteur.trim().ifEmpty { null }
                        )
                    )
                },
                enabled = !isLoading && nom.isNotBlank() && prenoms.isNotBlank(),
                shape = AppShapes.MediumRadius,
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = Color.White)
                } else {
                    Icon(Icons.Rounded.Save, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Enregistrer")
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
private fun PasswordDialog(
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    val canSave = oldPassword.isNotBlank() && newPassword.length >= 6 && newPassword == confirmPassword

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Rounded.Lock, null, tint = AppColors.BrandBlue) },
        title = { Text("Modifier le mot de passe", fontWeight = FontWeight.Black) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                PasswordField("Ancien mot de passe", oldPassword) { oldPassword = it }
                PasswordField("Nouveau mot de passe", newPassword) { newPassword = it }
                PasswordField("Confirmer", confirmPassword) { confirmPassword = it }
                if (newPassword.isNotBlank() && newPassword.length < 6) {
                    Text("Minimum 6 caracteres", color = AppColors.StatusRed, style = MaterialTheme.typography.labelSmall)
                }
                if (confirmPassword.isNotBlank() && newPassword != confirmPassword) {
                    Text("Les mots de passe ne correspondent pas", color = AppColors.StatusRed, style = MaterialTheme.typography.labelSmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(oldPassword, newPassword) },
                enabled = canSave && !isLoading,
                shape = AppShapes.MediumRadius
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = Color.White)
                } else {
                    Text("Mettre a jour")
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
private fun MoreOptionsDialog(onDismiss: () -> Unit, onAbout: () -> Unit, onRefresh: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Rounded.MoreHoriz, null, tint = AppColors.BrandBlue) },
        title = { Text("Autres options", fontWeight = FontWeight.Black) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                SettingsItem(Icons.Rounded.Description, "Actualiser le profil", "Recharger les donnees du serveur", onRefresh)
                SettingsItem(Icons.Rounded.Info, "A propos", "Version et informations application", onAbout)
                SettingsItem(Icons.Rounded.Security, "Regles de confidentialite", "Gestion des donnees personnelles", onDismiss)
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
private fun ProfileTextField(label: String, value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        shape = AppShapes.MediumRadius,
        colors = profileTextFieldColors()
    )
}

@Composable
private fun PasswordField(label: String, value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        visualTransformation = PasswordVisualTransformation(),
        shape = AppShapes.MediumRadius,
        colors = profileTextFieldColors()
    )
}

@Composable
private fun profileTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = AppColors.BrandBlue,
    unfocusedBorderColor = AppColors.BorderColor,
    focusedLabelColor = AppColors.BrandBlue,
    cursorColor = AppColors.BrandBlue,
    focusedContainerColor = AppColors.SurfaceMuted,
    unfocusedContainerColor = AppColors.SurfaceMuted
)

@Composable
private fun ImageViewerDialog(imageUrl: String, onDismiss: () -> Unit) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(modifier = Modifier.fillMaxSize(), color = Color.Black.copy(alpha = 0.92f)) {
            Box(modifier = Modifier.fillMaxSize()) {
                SubcomposeAsyncImage(
                    model = imageUrl,
                    contentDescription = "Document",
                    modifier = Modifier.fillMaxSize().clickable { onDismiss() },
                    contentScale = ContentScale.Fit,
                    loading = {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Color.White)
                        }
                    }
                )
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.TopEnd).padding(18.dp)
                ) {
                    Icon(Icons.Rounded.Close, null, tint = Color.White, modifier = Modifier.size(32.dp))
                }
            }
        }
    }
}

private data class ProfileInfoDialog(
    val title: String,
    val message: String
)

private fun String?.orDash(): String = this?.takeIf { it.isNotBlank() } ?: "-"
