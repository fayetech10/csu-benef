package com.example.sencsu.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import com.example.sencsu.components.forms.*
import com.example.sencsu.components.modals.AddDependantModalContent
import com.example.sencsu.data.remote.dto.AdherentDto
import com.example.sencsu.data.remote.dto.FormConstants
import com.example.sencsu.domain.viewmodel.AddAdherentUiEvent
import com.example.sencsu.domain.viewmodel.AddAdherentUiState
import com.example.sencsu.domain.viewmodel.AddAdherentViewModel
import com.example.sencsu.utils.toLocaleString
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import com.example.sencsu.components.ocr.OcrProcessingFlow
import com.example.sencsu.data.remote.dto.PersonneChargeDto
import com.example.sencsu.domain.viewmodel.OcrStep
import com.example.sencsu.domain.viewmodel.OcrViewModel
import com.example.sencsu.theme.AppColors
import com.example.sencsu.theme.AppGradients
import com.example.sencsu.theme.AppShapes
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun AddAdherentScreen(
    onBack: () -> Unit,
    onNavigateToPayment: (adherentId: String?, localAdherentId: Long?, montantTotal: Int) -> Unit,
    agentId: String?,
    viewModel: AddAdherentViewModel = hiltViewModel(),
    ocrViewModel: OcrViewModel = hiltViewModel(),
    onNavigateBack: (() -> Unit)? = null,
) {
    val state by viewModel.uiState.collectAsState()
    val ocrState by ocrViewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val isEditMode = viewModel.isEditMode

    // ✅ CORRIGÉ: États clairs et explicites
    var screenState by remember {
        mutableStateOf<ScreenState>(ScreenState.ChoiceMode)
    }
    var currentFormStep by remember { mutableStateOf(0) }

    val formSteps = listOf("Identité", "Contact", "Zone", "Photos", "Bénéficiaires", "Résumé")

    var showAlertDialog by remember { mutableStateOf(false) }
    var alertTitle by remember { mutableStateOf("") }
    var alertMessage by remember { mutableStateOf("") }
    var alertAction by remember { mutableStateOf<(() -> Unit)?>(null) }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is AddAdherentUiEvent.NavigateToPayment -> {
                    onNavigateToPayment(event.adherentId, event.localAdherentId, event.montantTotal)
                }
                is AddAdherentUiEvent.NavigateToPasswordUpdate -> {
                    // Cet événement est généralement géré par le parent (EnrolementScreen)
                    // mais nous l'ajoutons ici pour l'exhaustivité du when
                }
                is AddAdherentUiEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
                is AddAdherentUiEvent.NavigateBack -> {
                    (onNavigateBack ?: onBack).invoke()
                }
            }
        }
    }

    Scaffold(
        containerColor = AppColors.AppBackground,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            if (screenState == ScreenState.FormMode) {
                ModernHeader(
                    title = if (isEditMode) "Modifier Adhérent" else "Nouvel Adhérent",
                    currentStep = currentFormStep,
                    totalSteps = formSteps.size,
                    totalCost = state.totalCost,
                    totalBeneficiaries = state.dependants.size + 1,
                    onBack = onBack
                )
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {

            // ════════════════════════════════════════════════════════
            // ÉTAT 1: CHOIX - OCR ou Saisie manuelle
            // ════════════════════════════════════════════════════════
            if (screenState == ScreenState.ChoiceMode) {
                OcrModeChoiceScreen(
                    onChooseOcr = {
                        // ✅ IMMÉDIAT: Lancer l'OCR (saute le sélecteur interne)
                        ocrViewModel.startOcrMode()
                        screenState = ScreenState.OcrMode
                    },
                    onChooseManual = {
                        // ✅ Saisie manuelle directe
                        screenState = ScreenState.FormMode
                        currentFormStep = 0
                    },
                    onBack = onBack
                )
            }
            // ════════════════════════════════════════════════════════
            // ÉTAT 2: OCR (recto + verso)
            // ════════════════════════════════════════════════════════
            else if (screenState == ScreenState.OcrMode) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .background(AppColors.SurfaceBackground)
                ) {
                    OcrProcessingFlow(
                        viewModel = ocrViewModel,
                        onComplete = { extractedData ->
                            // ✅ Injecter les données extraites
                            extractedData["prenoms"]?.takeIf { it.isNotEmpty() }?.let {
                                viewModel.updatePrenoms(it)
                            }
                            extractedData["nom"]?.takeIf { it.isNotEmpty() }?.let {
                                viewModel.updateNom(it)
                            }
                            extractedData["dateNaissance"]?.takeIf { it.isNotEmpty() }?.let {
                                viewModel.updateDateNaissance(it)
                            }
                            extractedData["sexe"]?.takeIf { it.isNotEmpty() }?.let {
                                viewModel.updateSexe(it)
                            }
                            extractedData["nin"]?.takeIf { it.isNotEmpty() }?.let {
                                viewModel.updateNumeroCNI(it)
                            }
                            extractedData["lieuNaissance"]?.takeIf { it.isNotEmpty() }?.let {
                                viewModel.updateLieuNaissance(it)
                            }
                            extractedData["departement"]?.takeIf { it.isNotEmpty() }?.let {
                                viewModel.updateDepartement(it)
                            }
                            extractedData["commune"]?.takeIf { it.isNotEmpty() }?.let {
                                viewModel.updateCommune(it)
                            }
                            
                            extractedData["rectoUri"]?.let {
                                viewModel.updateRectoUri(android.net.Uri.parse(it))
                            }
                            extractedData["versoUri"]?.let {
                                viewModel.updateVersoUri(android.net.Uri.parse(it))
                            }

                            // ✅ Passer au formulaire avec données injectées
                            screenState = ScreenState.FormMode
                            currentFormStep = 0
                        },
                        onCancel = {
                            // ✅ Retour au choix
                            screenState = ScreenState.ChoiceMode
                            ocrViewModel.resetOcr()
                        }
                    )
                }
            }
            // ════════════════════════════════════════════════════════
            // ÉTAT 3: FORMULAIRE PRINCIPAL (6 étapes)
            // ════════════════════════════════════════════════════════
            else if (screenState == ScreenState.FormMode) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                ) {
                    StepProgressBar(
                        totalSteps = formSteps.size,
                        currentStep = currentFormStep,
                        modifier = Modifier.padding(20.dp)
                    )

                    AnimatedContent(
                        targetState = currentFormStep,
                        transitionSpec = { fadeIn() with fadeOut() },
                        label = "form_step"
                    ) { step ->
                        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                            when (step) {
                                0 -> IdentitySection(state, viewModel)
                                1 -> ContactSection(state, viewModel)
                                2 -> LocationSection(state, viewModel)
                                3 -> PhotosSection(state, viewModel)
                                4 -> BeneficiariesSection(
                                    state = state,
                                    viewModel = viewModel,
                                    onShowAlert = { title, msg, action ->
                                        alertTitle = title
                                        alertMessage = msg
                                        alertAction = action
                                        showAlertDialog = true
                                    }
                                )
                                5 -> SummarySection(state, viewModel)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    SimpleNavigation(
                        currentStep = currentFormStep,
                        totalSteps = formSteps.size,
                        isLoading = state.isLoading,
                        isEditMode = isEditMode,
                        onPrevious = { if (currentFormStep > 0) currentFormStep-- },
                        onNext = {
                            if (viewModel.validateStep(currentFormStep)) {
                                if (currentFormStep < formSteps.size - 1) currentFormStep++
                            }
                        },
                        onSubmit = {
                            if (isEditMode) viewModel.submitEdit(context)
                            else viewModel.submitWithUpload(context)
                        },
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )

                    Spacer(modifier = Modifier.height(40.dp))
                }

                // Modal pour ajouter/modifier un dépendant
                if (state.isModalVisible) {
                    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

                    ModalBottomSheet(
                        sheetState = sheetState,
                        onDismissRequest = { viewModel.hideModal() },
                        containerColor = AppColors.SurfaceBackground
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            AddDependantModalContent(
                                viewModel = viewModel,
                                onSave = { viewModel.saveDependant() },
                                onCancel = { viewModel.hideModal() }
                            )
                        }
                    }
                }

                // Dialog de confirmation suppression
                if (showAlertDialog) {
                    SimpleAlertDialog(
                        title = alertTitle,
                        message = alertMessage,
                        onConfirm = {
                            alertAction?.invoke()
                            showAlertDialog = false
                        },
                        onDismiss = { showAlertDialog = false }
                    )
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// 🔧 ENUM pour gérer les états d'écran
// ═══════════════════════════════════════════════════════════════════════════════

enum class ScreenState {
    ChoiceMode,   // Écran de choix initial
    OcrMode,      // Écran OCR (recto + verso)
    FormMode      // Formulaire principal (6 étapes)
}

// ═══════════════════════════════════════════════════════════════════════════════
// 🎯 ÉCRAN INITIAL: CHOIX OCR vs SAISIE MANUELLE
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun OcrModeChoiceScreen(
    onChooseOcr: () -> Unit,
    onChooseManual: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    AppGradients.BrandSoft
                )
            )
    ) {
        // En-tête minimalist
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp, start = 16.dp, end = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(44.dp)
                    .background(Color.White, CircleShape)
                    .shadow(2.dp, CircleShape)
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = null, tint = AppColors.TextMain)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Illustration / Icon
            Surface(
                modifier = Modifier.size(80.dp),
                shape = CircleShape,
                color = AppColors.BrandBlue.copy(0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.PersonAdd,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = AppColors.BrandBlue
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Ajouter un adhérent",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.sp
                ),
                color = AppColors.TextMain
            )

            Text(
                text = "Choisissez la méthode d'enrôlement",
                style = MaterialTheme.typography.bodyLarge,
                color = AppColors.TextSub.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(48.dp))

            // ── OPTIONS CARDS ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // OPTION: SCAN OCR
                SelectionCard(
                    modifier = Modifier.weight(1f),
                    title = "Scan OCR",
                    description = "Extraction rapide via photo",
                    icon = Icons.Default.CameraAlt,
                    accentColor = AppColors.BrandBlue,
                    bgColor = AppColors.ActionBlueSoft,
                    onClick = onChooseOcr,
                    tag = "2 min",
                    isRecommended = true
                )

                // OPTION: MANUEL
                SelectionCard(
                    modifier = Modifier.weight(1f),
                    title = "Manuel",
                    description = "Saisie classique pas à pas",
                    icon = Icons.Default.Edit,
                    accentColor = AppColors.TextSub,
                    bgColor = Color.White,
                    onClick = onChooseManual,
                    tag = "5 min"
                )
            }

            Spacer(modifier = Modifier.height(56.dp))
            
            // Note
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .alpha(0.6f)
                    .background(Color.White.copy(0.5f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = AppColors.TextSub,
                )

                Spacer(Modifier.width(8.dp))
                Text(
                    "L'OCR est recommandé pour gagner du temps",
                    style = MaterialTheme.typography.labelSmall,
                    color = AppColors.TextSub
                )
            }
        }
    }
}

@Composable
fun SelectionCard(
    modifier: Modifier = Modifier,
    title: String,
    description: String,
    icon: ImageVector,
    accentColor: Color,
    bgColor: Color,
    onClick: () -> Unit,
    tag: String,
    isRecommended: Boolean = false
) {
    val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    
    Card(
        modifier = modifier
            .height(220.dp)
            .shadow(
                elevation = 8.dp,
                shape = AppShapes.MediumRadius,
                spotColor = accentColor.copy(alpha = 0.2f)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(
                    bounded = true,
                    color = accentColor
                ),
                onClick = onClick
            ),
        shape = AppShapes.MediumRadius,
        colors = CardDefaults.cardColors(containerColor = bgColor),
        border = if (isRecommended) BorderStroke(2.dp, accentColor) else null
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (isRecommended) {
                Surface(
                    color = accentColor,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 12.dp, end = 12.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        "RECOMMANDÉ",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    modifier = Modifier.size(56.dp),
                    shape = CircleShape,
                    color = if (isRecommended) accentColor else accentColor.copy(0.1f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            modifier = Modifier.size(28.dp),
                            tint = if (isRecommended) Color.White else accentColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = title,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    color = AppColors.TextMain
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = AppColors.TextSub.copy(0.8f),
                    textAlign = TextAlign.Center,
                    lineHeight = 16.sp
                )
                
                Spacer(modifier = Modifier.weight(1f))

                Surface(
                    color = AppColors.SurfaceAlt,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "⏱ $tag",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.TextSub
                    )
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// COMPOSANTS RESTANTS (identiques à votre fichier original)
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun ModernHeader(
    title: String,
    currentStep: Int,
    totalSteps: Int,
    totalCost: Int,
    totalBeneficiaries: Int,
    onBack: () -> Unit
) {
    Surface(
        color = AppColors.SurfaceBackground,
        shadowElevation = 1.dp,
        modifier = Modifier
            .padding(vertical = 30.dp)
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Surface(
                    onClick = onBack,
                    shape = CircleShape,
                    color = AppColors.SurfaceAlt,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null, tint = AppColors.TextMain, modifier = Modifier.size(20.dp))
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(title, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = AppColors.TextMain, letterSpacing = 0.sp)
                    Text("Étape ${currentStep + 1} sur $totalSteps", fontSize = 12.sp, color = AppColors.TextSub, fontWeight = FontWeight.Medium)
                }
            }

            Surface(
                color = AppColors.BrandBlue.copy(alpha = 0.1f),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, AppColors.BrandBlue.copy(alpha = 0.2f))
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "${totalCost.toLocaleString()} F",
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp,
                        color = AppColors.BrandBlue
                    )
                    Text(
                        text = "$totalBeneficiaries bénéficiaire(s)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.BrandBlue.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

@Composable
fun StepProgressBar(totalSteps: Int, currentStep: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        repeat(totalSteps) { index ->
            val isActive = index <= currentStep
            val weight by animateFloatAsState(
                targetValue = if (index == currentStep) 2f else 1f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                label = "weight"
            )
            Box(
                modifier = Modifier
                    .weight(weight)
                    .height(6.dp)
                    .clip(CircleShape)
                    .background(
                        if (isActive) AppColors.BrandBlue 
                        else AppColors.BorderColor.copy(alpha = 0.5f)
                    )
            )
        }
    }
}

@Composable
fun SectionCard(title: String, icon: ImageVector, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        shape = AppShapes.MediumRadius,
        colors = CardDefaults.cardColors(containerColor = AppColors.SurfaceBackground),
        border = BorderStroke(1.dp, AppColors.BorderColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = AppColors.BrandBlue, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(title, fontWeight = FontWeight.Bold, color = AppColors.TextMain, fontSize = 15.sp)
            }
            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDatePickerField(
    label: String,
    value: String?,
    onDateSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    errorMessage: String? = null
) {
    var showDialog by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    Column(modifier = modifier) {
        Box {
            OutlinedTextField(
                value = value ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text(label, color = if (isError) AppColors.StatusRed else AppColors.TextSub) },
                placeholder = { Text("JJ/MM/AAAA", color = AppColors.TextSub.copy(alpha = 0.5f)) },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    Icon(
                        Icons.Default.DateRange,
                        contentDescription = "Sélectionner une date",
                        tint = if (isError) AppColors.StatusRed else AppColors.BrandBlue
                    )
                },
                isError = isError,
                shape = AppShapes.MediumRadius,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = AppColors.SurfaceBackground,
                    unfocusedContainerColor = AppColors.SurfaceBackground,
                    focusedIndicatorColor = if (isError) AppColors.StatusRed else AppColors.BrandBlue,
                    unfocusedIndicatorColor = if (isError) AppColors.StatusRed else AppColors.BorderColor,
                    errorIndicatorColor = AppColors.StatusRed
                )
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable { showDialog = true }
            )
        }
        if (isError && errorMessage != null) {
            Text(
                text = errorMessage,
                color = AppColors.StatusRed,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(start = 8.dp, top = 4.dp)
            )
        }
    }

    if (showDialog) {
        DatePickerDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val formattedDate = SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE)
                                .format(Date(millis))
                            onDateSelected(formattedDate)
                        }
                        showDialog = false
                    }
                ) { Text("OK", color = AppColors.BrandBlue) }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("Annuler", color = AppColors.TextSub) }
            },
            colors = DatePickerDefaults.colors(containerColor = AppColors.SurfaceBackground)
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
fun IdentitySection(state: AddAdherentUiState, viewModel: AddAdherentViewModel) {
    SectionCard("Informations personnelles", Icons.Outlined.Person) {
        AppTextField(
            value = state.prenoms,
            onValueChange = viewModel::updatePrenoms,
            label = "Prénoms*",
            placeholder = "Ex: Mamadou",
            isError = state.validationErrors.containsKey("prenoms"),
            errorMessage = state.validationErrors["prenoms"]
        )
        Spacer(modifier = Modifier.height(12.dp))
        AppTextField(
            value = state.nom,
            onValueChange = viewModel::updateNom,
            label = "Nom*",
            placeholder = "Ex: Diop",
            isError = state.validationErrors.containsKey("nom"),
            errorMessage = state.validationErrors["nom"]
        )
        Spacer(modifier = Modifier.height(12.dp))
        AppDatePickerField(
            label = "Date de naissance*",
            value = state.dateNaissance,
            onDateSelected = viewModel::updateDateNaissance,
            isError = state.validationErrors.containsKey("dateNaissance"),
            errorMessage = state.validationErrors["dateNaissance"]
        )
        Spacer(modifier = Modifier.height(12.dp))
        AppDropdown(
            label = "Sexe*",
            options = FormConstants.SEXES,
            selected = state.sexe,
            onSelect = viewModel::updateSexe
        )
        Spacer(modifier = Modifier.height(12.dp))
        AppTextField(
            value = state.lieuNaissance,
            onValueChange = viewModel::updateLieuNaissance,
            label = "Lieu de naissance*",
            placeholder = "Ex: Dakar",
            isError = state.validationErrors.containsKey("lieuNaissance"),
            errorMessage = state.validationErrors["lieuNaissance"]
        )
        Spacer(modifier = Modifier.height(12.dp))
        AppDropdown(
            label = "Situation Matrimoniale*",
            options = FormConstants.SITUATIONS,
            selected = state.situationMatrimoniale,
            onSelect = viewModel::updateSituationMatrimoniale
        )
        Spacer(modifier = Modifier.height(12.dp))
        AppDropdown(
            label = "Régime*",
            options = FormConstants.REGIMES,
            selected = state.regime,
            onSelect = viewModel::updateRegime
        )
        Spacer(modifier = Modifier.height(12.dp))
        AppDropdown(
            label = "Type de Bénéficiaire*",
            options = FormConstants.TYPES_BENEF,
            selected = state.typeBenef,
            onSelect = viewModel::updateTypeBenef
        )
        Spacer(modifier = Modifier.height(12.dp))
        AppDropdown(
            label = "Type d'Adhésion*",
            options = FormConstants.TYPES_ADHESION,
            selected = state.typeAdhesion,
            onSelect = viewModel::updateTypeAdhesion
        )
    }
}

@Composable
fun ContactSection(state: AddAdherentUiState, viewModel: AddAdherentViewModel) {
    SectionCard("Contact & ID", Icons.Outlined.Phone) {
        AppTextField(
            value = state.whatsapp,
            onValueChange = viewModel::updateWhatsapp,
            label = "WhatsApp*",
            placeholder = "Ex: 77 123 45 67",
            keyboardType = KeyboardType.Phone,
            isError = state.validationErrors.containsKey("whatsapp"),
            errorMessage = state.validationErrors["whatsapp"]
        )
        Spacer(modifier = Modifier.height(12.dp))
        AppDropdown(
            label = "Type de pièce*",
            options = FormConstants.TYPES_PIECE,
            selected = state.typePiece,
            onSelect = viewModel::updateTypePiece
        )
        Spacer(modifier = Modifier.height(12.dp))
        AppTextField(
            value = state.secteurActivite,
            onValueChange = viewModel::updateSecteurActivite,
            label = "Secteur d'activité*",
            placeholder = "Ex: Agriculture, Commerce...",
            isError = state.validationErrors.containsKey("secteurActivite"),
            errorMessage = state.validationErrors["secteurActivite"]
        )
        Spacer(modifier = Modifier.height(12.dp))
        if (state.typePiece == "CNI") {
            AppTextField(
                value = state.numeroCNI,
                onValueChange = viewModel::updateNumeroCNI,
                label = "Numéro CNI*",
                placeholder = "Entrez le numéro",
                isError = state.validationErrors.containsKey("numeroCNI"),
                errorMessage = state.validationErrors["numeroCNI"]
            )
        } else {
            AppTextField(
                value = state.numeroExtrait,
                onValueChange = viewModel::updateNumeroExtrait,
                label = "Numéro Extrait*",
                placeholder = "Entrez le numéro",
                isError = state.validationErrors.containsKey("numeroExtrait"),
                errorMessage = state.validationErrors["numeroExtrait"]
            )
        }
    }
}

@Composable
fun LocationSection(state: AddAdherentUiState, viewModel: AddAdherentViewModel) {
    SectionCard("Localisation", Icons.Outlined.LocationOn) {
        AppDropdown(
            label = "Région*",
            options = FormConstants.REGIONS,
            selected = state.region,
            onSelect = viewModel::updateRegion
        )
        Spacer(modifier = Modifier.height(12.dp))
        AppDropdown(
            label = "Département*",
            options = FormConstants.DEPARTEMENTS,
            selected = state.departement,
            onSelect = viewModel::updateDepartement
        )
        Spacer(modifier = Modifier.height(12.dp))
        AppTextField(
            value = state.commune,
            onValueChange = viewModel::updateCommune,
            label = "Commune*",
            placeholder = "Ville/Commune",
            isError = state.validationErrors.containsKey("commune"),
            errorMessage = state.validationErrors["commune"]
        )
        Spacer(modifier = Modifier.height(12.dp))
        AppTextField(
            value = state.adresse,
            onValueChange = viewModel::updateAdresse,
            label = "Adresse*",
            placeholder = "Quartier, rue...",
            isError = state.validationErrors.containsKey("adresse"),
            errorMessage = state.validationErrors["adresse"]
        )
    }
}

@Composable
fun PhotosSection(state: AddAdherentUiState, viewModel: AddAdherentViewModel) {
    SectionCard("Documents", Icons.Outlined.CameraAlt) {
        ImagePickerComponent(
            label = "Photo d'identité*",
            imageUri = state.photoUri?.toString() ?: state.existingPhotoUrl,
            onImageSelected = viewModel::updatePhotoUri,
            required = true,
            isError = state.validationErrors.containsKey("photoUri")
        )
        Spacer(modifier = Modifier.height(12.dp))

        ImagePickerComponent(
            label = "CNI Recto",
            imageUri = state.rectoUri?.toString() ?: state.existingRectoUrl,
            onImageSelected = viewModel::updateRectoUri
        )
        Spacer(modifier = Modifier.height(12.dp))

        ImagePickerComponent(
            label = "CNI Verso",
            imageUri = state.versoUri?.toString() ?: state.existingVersoUrl,
            onImageSelected = viewModel::updateVersoUri
        )
    }
}

@Composable
fun BeneficiariesSection(
    state: AddAdherentUiState,
    viewModel: AddAdherentViewModel,
    onShowAlert: (String, String, () -> Unit) -> Unit
) {
    SectionCard("Bénéficiaires", Icons.Outlined.Group) {
        state.dependants.forEachIndexed { index, dep ->
            AppDependantCard(
                dependant = dep,
                onEdit = { viewModel.showEditDependantModal(index, dep) },
                onDelete = {
                    onShowAlert("Supprimer", "Retirer ce bénéficiaire ?") {
                        viewModel.removeDependant(index)
                    }
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        OutlinedButton(
            onClick = { viewModel.showAddDependantModal() },
            modifier = Modifier.fillMaxWidth(),
            shape = AppShapes.MediumRadius,
            border = BorderStroke(1.dp, AppColors.BrandBlue.copy(0.2f)),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = AppColors.BrandBlue)
        ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Ajouter une personne")
        }
    }
}

@Composable
fun SummarySection(state: AddAdherentUiState, viewModel: AddAdherentViewModel) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            "Résumé de l'adhésion",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = AppColors.TextMain,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        SummaryCard("Identité", Icons.Outlined.Person) {
            SummaryRow("Nom complet", "${state.prenoms} ${state.nom}")
            SummaryRow("Date & Lieu", "${state.dateNaissance} à ${state.lieuNaissance}")
            SummaryRow("Sexe", state.sexe)
            SummaryRow("Situation", state.situationMatrimoniale)
            SummaryRow("Régime", state.regime)
            SummaryRow("Type Bénéf.", state.typeBenef)
            SummaryRow("Type Adhésion", state.typeAdhesion)
        }

        SummaryCard("Contact & ID", Icons.Outlined.Phone) {
            SummaryRow("WhatsApp", state.whatsapp)
            SummaryRow("Pièce", "${state.typePiece}: ${if (state.typePiece == "CNI") state.numeroCNI else state.numeroExtrait}")
            SummaryRow("Activité", state.secteurActivite)
        }

        SummaryCard("Localisation", Icons.Outlined.LocationOn) {
            SummaryRow("Région", state.region)
            SummaryRow("Département", state.departement)
            SummaryRow("Commune", state.commune)
            SummaryRow("Adresse", state.adresse)
        }

        SummaryCard("Bénéficiaires", Icons.Outlined.Group) {
            SummaryRow("Nombre", "${state.dependants.size} personne(s) à charge")
            SummaryRow("Coût Total", "${state.totalCost.toLocaleString()} F")
        }
    }
}

@Composable
private fun SummaryCard(title: String, icon: ImageVector, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.SurfaceBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
        border = BorderStroke(1.dp, AppColors.BorderColor.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = AppColors.BrandBlue, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(title, fontWeight = FontWeight.Bold, color = AppColors.BrandBlue)
            }
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = AppColors.TextSub, fontSize = 14.sp)
        Text(
            value, 
            fontWeight = FontWeight.Medium, 
            color = AppColors.TextMain, 
            fontSize = 14.sp, 
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f).padding(start = 16.dp)
        )
    }
}

@Composable
fun AppDependantCard(
    dependant: PersonneChargeDto,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp),
        colors = CardDefaults.cardColors(
            containerColor = AppColors.BrandBlue.copy(alpha = 0.05f)
        ),
        shape = AppShapes.SmallRadius
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${dependant.prenoms} ${dependant.nom}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.TextMain,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    Text(
                        text = "${dependant.sexe}",
                        fontSize = 12.sp,
                        color = AppColors.TextSub,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                    Text(
                        text = "Né(e) le: ${dependant.dateNaissance}",
                        fontSize = 12.sp,
                        color = AppColors.TextSub
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(
                        imageVector = Icons.Default.Create,
                        contentDescription = "Modifier",
                        tint = AppColors.TextSub,
                        modifier = Modifier
                            .size(24.dp)
                            .clickable { onEdit() }
                    )

                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Supprimer",
                        tint = AppColors.StatusRed,
                        modifier = Modifier
                            .size(24.dp)
                            .clickable { onDelete() }
                    )
                }
            }
        }
    }
}

@Composable
fun SimpleNavigation(
    currentStep: Int,
    totalSteps: Int,
    isLoading: Boolean,
    isEditMode: Boolean = false,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isLastStep = currentStep == totalSteps - 1
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (currentStep > 0) {
            OutlinedButton(
                onClick = onPrevious,
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                shape = AppShapes.MediumRadius,
                border = BorderStroke(1.dp, AppColors.BorderColor),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = AppColors.TextSub)
            ) {
                Text("Précédent")
            }
        }

        Button(
            onClick = if (isLastStep) onSubmit else onNext,
            modifier = Modifier
                .weight(1f)
                .height(50.dp),
            enabled = !isLoading,
            shape = AppShapes.MediumRadius,
            colors = ButtonDefaults.buttonColors(containerColor = AppColors.BrandBlue)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    when {
                        !isLastStep -> "Suivant"
                        isEditMode -> "Mettre à jour"
                        else -> "Enregistrer"
                    }
                )
            }
        }
    }
}

@Composable
fun SimpleAlertDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold, color = AppColors.TextMain) },
        text = { Text(message, color = AppColors.TextMain) },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text("Confirmer", color = AppColors.StatusRed) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuler", color = AppColors.TextSub) }
        },
        containerColor = AppColors.SurfaceBackground,
        shape = AppShapes.MediumRadius
    )
}

@Composable
fun AppProgressIndicator(
    progress: Float? = null,
    modifier: Modifier = Modifier.size(24.dp),
    color: Color = AppColors.BrandBlue,
    trackColor: Color = AppColors.BrandBlue.copy(alpha = 0.1f),
    strokeWidth: Dp = 3.dp
) {
    if (progress != null) {
        val animatedProgress by animateFloatAsState(
            targetValue = progress.coerceIn(0f, 1f),
            animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec,
            label = "progress"
        )

        Canvas(modifier = modifier) {
            val stroke = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            drawCircle(color = trackColor, style = stroke)
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = animatedProgress * 360f,
                useCenter = false,
                style = stroke
            )
        }
    } else {
        val infiniteTransition = rememberInfiniteTransition(label = "loading")
        val angle by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = keyframes { durationMillis = 1000 },
                repeatMode = RepeatMode.Restart
            ),
            label = "angle"
        )

        Canvas(modifier = modifier) {
            val stroke = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            drawCircle(color = trackColor, style = stroke)
            drawArc(
                color = color,
                startAngle = angle,
                sweepAngle = 90f,
                useCenter = false,
                style = stroke
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTextField(
    value: String?,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    isError: Boolean = false,
    errorMessage: String? = null,
    readOnly: Boolean = false,
    trailingIcon: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    Column(modifier = modifier) {
        if (value != null) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                label = { Text(label, color = if (isError) AppColors.StatusRed else AppColors.TextSub) },
                placeholder = { Text(placeholder, color = AppColors.TextSub.copy(alpha = 0.5f)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
                enabled = onClick == null,
                readOnly = readOnly,
                isError = isError,
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                shape = AppShapes.MediumRadius,
                trailingIcon = trailingIcon,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = AppColors.SurfaceBackground,
                    unfocusedContainerColor = AppColors.SurfaceBackground,
                    disabledContainerColor = AppColors.SurfaceBackground,
                    focusedIndicatorColor = if (isError) AppColors.StatusRed else AppColors.BrandBlue,
                    unfocusedIndicatorColor = if (isError) AppColors.StatusRed else AppColors.BorderColor,
                    errorIndicatorColor = AppColors.StatusRed,
                    cursorColor = AppColors.BrandBlue,
                    focusedLabelColor = AppColors.BrandBlue,
                    unfocusedLabelColor = AppColors.TextSub,
                )
            )
        }
        if (isError && errorMessage != null) {
            Text(
                text = errorMessage,
                color = AppColors.StatusRed,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(start = 8.dp, top = 4.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDropdown(
    label: String,
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    errorMessage: String? = null
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selected,
                onValueChange = {},
                readOnly = true,
                label = {
                    Text(
                        label,
                        color = if (isError) AppColors.StatusRed else AppColors.TextSub
                    )
                },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                isError = isError,
                shape = AppShapes.MediumRadius,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = AppColors.SurfaceBackground,
                    unfocusedContainerColor = AppColors.SurfaceBackground,
                    focusedIndicatorColor = if (isError) AppColors.StatusRed else AppColors.BrandBlue,
                    unfocusedIndicatorColor = if (isError) AppColors.StatusRed else AppColors.BorderColor,
                    errorIndicatorColor = AppColors.StatusRed
                )
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(AppColors.SurfaceBackground)
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option, color = AppColors.TextMain) },
                        onClick = {
                            onSelect(option)
                            expanded = false
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                    )
                }
            }
        }
        if (isError && errorMessage != null) {
            Text(
                text = errorMessage,
                color = AppColors.StatusRed,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(start = 8.dp, top = 4.dp)
            )
        }
    }
}
