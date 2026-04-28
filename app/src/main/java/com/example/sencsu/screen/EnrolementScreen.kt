package com.example.sencsu.screen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.sencsu.domain.viewmodel.AddAdherentUiEvent
import com.example.sencsu.domain.viewmodel.AddAdherentViewModel
import com.example.sencsu.domain.viewmodel.OcrViewModel
import com.example.sencsu.theme.AppColors
import com.example.sencsu.theme.AppShapes
import kotlinx.coroutines.delay
import java.io.File

// Imports extraits depuis le package enrolement
import com.example.sencsu.screen.enrolement.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnrolementScreen(onSuccess: (AddAdherentUiEvent.NavigateToPasswordUpdate) -> Unit, onBack: () -> Unit) {
    val ocrViewModel: OcrViewModel = hiltViewModel()
    val addAdherentViewModel: AddAdherentViewModel = hiltViewModel()
    val ocrState by ocrViewModel.uiState.collectAsState()
    val context = LocalContext.current

    var currentStep by remember { mutableIntStateOf(1) }
    val totalSteps = 5
    val scrollState = rememberScrollState()

    // ── Données de l'adhérent ──
    var nom by remember { mutableStateOf("") }
    var prenom by remember { mutableStateOf("") }
    var dateNaissance by remember { mutableStateOf("") }
    var sexe by remember { mutableStateOf("M") }
    var lieuNaissance by remember { mutableStateOf("") }
    var commune by remember { mutableStateOf("") }
    var departement by remember { mutableStateOf("") }
    var nin by remember { mutableStateOf("") }

    var telephone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var adresse by remember { mutableStateOf("") }

    // ── Personnes à charge (liste dynamique) ──
    var personnesCharge by remember { mutableStateOf(listOf<PersonneChargeForm>()) }
    var nextPersonId by remember { mutableIntStateOf(1) }

    // ── URIs des photos CNI ──
    var rectoUri by remember { mutableStateOf<Uri?>(null) }
    var versoUri by remember { mutableStateOf<Uri?>(null) }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }
    var captureTarget by remember { mutableStateOf("recto") } // "recto" ou "verso"

    var isSubmitting by remember { mutableStateOf(false) }
    var ocrApplied by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        addAdherentViewModel.uiEvent.collect { event ->
            when (event) {
                is AddAdherentUiEvent.NavigateToPayment -> {
                    // isSubmitting = false
                    // newAdherentId = event.adherentId ?: ""
                    // showSuccess = true
                }
                is AddAdherentUiEvent.NavigateToPasswordUpdate -> {
                    isSubmitting = false
                    onSuccess(event)
                }
                is AddAdherentUiEvent.ShowSnackbar -> {
                    isSubmitting = false
                    // Handle snackbar
                }
                is AddAdherentUiEvent.NavigateBack -> {
                    onBack()
                }
            }
        }
    }

    // ── Appliquer les résultats OCR aux champs du formulaire ──
    LaunchedEffect(ocrState.rectoResult) {
        ocrState.rectoResult?.let { result ->
            if (result.isValid && !ocrApplied) {
                if (result.nom.isNotBlank()) nom = result.nom
                if (result.prenoms.isNotBlank()) prenom = result.prenoms
                if (result.dateNaissance.isNotBlank()) dateNaissance = result.dateNaissance
                if (result.sexe.isNotBlank()) sexe = result.sexe
                if (result.lieuNaissance.isNotBlank()) lieuNaissance = result.lieuNaissance
                if (result.commune.isNotBlank()) commune = result.commune
                if (result.departement.isNotBlank()) departement = result.departement
                ocrApplied = true
            }
        }
    }

    LaunchedEffect(ocrState.versoResult) {
        ocrState.versoResult?.let { result ->
            if (result.nin.isNotBlank()) nin = result.nin
        }
    }

    // ── Launchers caméra et galerie ──
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempCameraUri != null) {
            if (captureTarget == "recto") {
                rectoUri = tempCameraUri
                ocrViewModel.setRectoUri(tempCameraUri)
                ocrViewModel.processRectoImage(context, tempCameraUri!!)
            } else {
                versoUri = tempCameraUri
                ocrViewModel.setVersoUri(tempCameraUri)
                ocrViewModel.processVersoImage(context, tempCameraUri!!)
            }
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            if (captureTarget == "recto") {
                rectoUri = it
                ocrViewModel.setRectoUri(it)
                ocrViewModel.processRectoImage(context, it)
            } else {
                versoUri = it
                ocrViewModel.setVersoUri(it)
                ocrViewModel.processVersoImage(context, it)
            }
        }
    }

    fun launchCamera(target: String) {
        captureTarget = target
        val tempFile = File.createTempFile("cni_${target}_", ".jpg", context.cacheDir)
            .apply { deleteOnExit() }
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", tempFile)
        tempCameraUri = uri
        cameraLauncher.launch(uri)
    }

    fun launchGallery(target: String) {
        captureTarget = target
        galleryLauncher.launch("image/*")
    }



    Scaffold(
        containerColor = AppColors.AppBackground,
        topBar = {
            TopAppBar(
                title = { Text("Enrôlement", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = {
                        if (currentStep > 1) currentStep-- else onBack()
                    }) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        bottomBar = {
            Surface(
                color = Color.White,
                shadowElevation = 8.dp,
                modifier = Modifier.navigationBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (currentStep > 1) {
                        OutlinedButton(
                            onClick = { currentStep-- },
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp),
                            shape = AppShapes.MediumRadius
                        ) {
                            Text("Précédent")
                        }
                    }
                    Button(
                        onClick = {
                            if (currentStep < totalSteps) {
                                currentStep++
                            } else {
                                isSubmitting = true
                                // Update viewmodel with local state
                                addAdherentViewModel.updateNom(nom)
                                addAdherentViewModel.updatePrenoms(prenom)
                                addAdherentViewModel.updateDateNaissance(dateNaissance)
                                addAdherentViewModel.updateSexe(sexe)
                                addAdherentViewModel.updateLieuNaissance(lieuNaissance)
                                addAdherentViewModel.updateCommune(commune)
                                addAdherentViewModel.updateDepartement(departement)
                                addAdherentViewModel.updateNumeroCNI(nin)
                                addAdherentViewModel.updateWhatsapp(telephone)
                                // We default to CNI
                                addAdherentViewModel.updateTypePiece("CNI")
                                addAdherentViewModel.updateAdresse(adresse)
                                
                                // Map personnesCharge
                                // AddAdherentViewModel handles them via its internal state.
                                // We'll just submit the ones we have by clearing and adding them.
                                addAdherentViewModel.resetForm()
                                addAdherentViewModel.updateNom(nom)
                                addAdherentViewModel.updatePrenoms(prenom)
                                addAdherentViewModel.updateDateNaissance(dateNaissance)
                                addAdherentViewModel.updateSexe(sexe)
                                addAdherentViewModel.updateLieuNaissance(lieuNaissance)
                                addAdherentViewModel.updateCommune(commune)
                                addAdherentViewModel.updateDepartement(departement)
                                addAdherentViewModel.updateNumeroCNI(nin)
                                addAdherentViewModel.updateWhatsapp(telephone)
                                addAdherentViewModel.updateTypePiece("CNI")
                                addAdherentViewModel.updateAdresse(adresse)
                                
                                // Set URIs
                                addAdherentViewModel.updateRectoUri(rectoUri)
                                addAdherentViewModel.updateVersoUri(versoUri)

                                personnesCharge.forEach { dep ->
                                    val mapped = com.example.sencsu.data.remote.dto.PersonneChargeDto(
                                        prenoms = dep.prenoms,
                                        nom = dep.nom,
                                        dateNaissance = dep.dateNaissance,
                                        sexe = dep.sexe,
                                        lienParent = dep.lienParent,
                                        typePiece = "CNI"
                                    )
                                    addAdherentViewModel.updateCurrentDependant(mapped)
                                    addAdherentViewModel.saveDependant()
                                }
                                
                                addAdherentViewModel.submitWithUpload(context)
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = AppShapes.MediumRadius,
                        colors = ButtonDefaults.buttonColors(containerColor = AppColors.BrandBlue)
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(if (currentStep == totalSteps) "Soumettre" else "Suivant")
                        }
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
        ) {
            StepperHeader(currentStep = currentStep, totalSteps = totalSteps)

            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                AnimatedContent(
                    targetState = currentStep,
                    transitionSpec = {
                        if (targetState > initialState) {
                            slideInHorizontally { it } + fadeIn() togetherWith
                                    slideOutHorizontally { -it } + fadeOut()
                        } else {
                            slideInHorizontally { -it } + fadeIn() togetherWith
                                    slideOutHorizontally { it } + fadeOut()
                        }
                    }, label = "steps"
                ) { step ->
                    when (step) {
                        1 -> OcrScanStep(
                            rectoUri = rectoUri,
                            versoUri = versoUri,
                            isProcessing = ocrState.isProcessing,
                            rectoResult = ocrState.rectoResult,
                            versoResult = ocrState.versoResult,
                            onCaptureRecto = { launchCamera("recto") },
                            onGalleryRecto = { launchGallery("recto") },
                            onCaptureVerso = { launchCamera("verso") },
                            onGalleryVerso = { launchGallery("verso") }
                        )
                        2 -> IdentityStep(
                            nom = nom, onNomChange = { nom = it },
                            prenom = prenom, onPrenomChange = { prenom = it },
                            date = dateNaissance, onDateChange = { dateNaissance = it },
                            sexe = sexe, onSexeChange = { sexe = it },
                            lieuNaissance = lieuNaissance, onLieuChange = { lieuNaissance = it },
                            commune = commune, onCommuneChange = { commune = it },
                            departement = departement, onDepartementChange = { departement = it },
                            nin = nin, onNinChange = { nin = it },
                            isFromOcr = ocrApplied
                        )
                        3 -> CoordinatesStep(
                            tel = telephone, onTelChange = { telephone = it },
                            mail = email, onMailChange = { email = it },
                            addr = adresse, onAddrChange = { adresse = it },
                            commune = commune, departement = departement
                        )
                        4 -> PersonnesChargeStep(
                            personnes = personnesCharge,
                            onAdd = {
                                personnesCharge = personnesCharge + PersonneChargeForm(id = nextPersonId)
                                nextPersonId++
                            },
                            onRemove = { id ->
                                personnesCharge = personnesCharge.filter { it.id != id }
                            },
                            onUpdate = { id, updated ->
                                personnesCharge = personnesCharge.map {
                                    if (it.id == id) updated else it
                                }
                            }
                        )
                        5 -> ReviewStep(
                            nom = nom, prenom = prenom, tel = telephone,
                            mail = email, addr = adresse,
                            dateNaissance = dateNaissance, nin = nin,
                            commune = commune, departement = departement,
                            personnesCharge = personnesCharge
                        )
                    }
                }
            }
        }
    }
}
