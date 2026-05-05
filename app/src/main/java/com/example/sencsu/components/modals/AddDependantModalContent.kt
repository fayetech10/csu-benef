package com.example.sencsu.components.modals

import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sencsu.theme.AppShapes
import com.example.sencsu.theme.AppGradients
import com.example.sencsu.components.forms.*
import com.example.sencsu.data.remote.dto.FormConstants
import com.example.sencsu.domain.viewmodel.AddAdherentViewModel
import com.example.sencsu.theme.AppColors
import com.example.sencsu.utils.Formatters

// ==================== DESIGN TOKENS ====================


@Composable
fun AddDependantModalContent(
    viewModel: AddAdherentViewModel,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    val state = viewModel.uiState.collectAsState()
    val currentDep = state.value.currentDependant
    val isEditing = state.value.editingIndex != null

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(ModalStyle.Background)
            .padding(top = 16.dp) // Petit padding haut
    ) {
        // 1. Header Premium avec Gradient
        PremiumModalHeader(
            title = if (isEditing) "Modifier le bénéficiaire" else "Nouveau bénéficiaire",
            onClose = onCancel
        )

        // 2. Contenu Scrollable
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = ModalStyle.Padding, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {

            // --- SECTION 1 : IDENTITÉ ---
            FormSection(title = "Identité Personnelle") {
                // Ligne 1 : Prénom & Nom (50/50)
                Row(horizontalArrangement = Arrangement.spacedBy(ModalStyle.Gap)) {
                    Box(Modifier.weight(1f)) {
                        FormTextField(
                            value = currentDep.prenoms,
                            onValueChange = { viewModel.updateCurrentDependant(currentDep.copy(prenoms = it)) },
                            label = "Prénoms",
                            placeholder = "Ex: Jean",
                        )
                    }
                    Box(Modifier.weight(1f)) {
                        FormTextField(
                            value = currentDep.nom,
                            onValueChange = { viewModel.updateCurrentDependant(currentDep.copy(nom = it)) },
                            label = "Nom",
                            placeholder = "Ex: Diop",
                        )
                    }
                }

                // Ligne 2 : Date & Lieu (50/50)
                Row(horizontalArrangement = Arrangement.spacedBy(ModalStyle.Gap)) {
                    Box(Modifier.weight(1f)) {
                        DatePickerField(
                            label = "Date naissance",
                            value = currentDep.dateNaissance,
                            onDateSelected = { viewModel.updateCurrentDependant(currentDep.copy(dateNaissance = it)) },
                            isError = currentDep.dateNaissance == null
                        )
                    }
                    Box(Modifier.weight(1f)) {
                        FormTextField(
                            value = currentDep.lieuNaissance ?: "",
                            onValueChange = { viewModel.updateCurrentDependant(currentDep.copy(lieuNaissance = it)) },
                            label = "Lieu naissance",
                            placeholder = "Ex: Dakar",
                        )
                    }
                }

                // Sexe & Situation
                SegmentedSelector(
                    title = "Sexe",
                    options = listOf("Masculin", "Féminin"),
                    selected = if (currentDep.sexe == "M") "Masculin" else "Féminin",
                    onSelect = { viewModel.updateCurrentDependant(currentDep.copy(sexe = if (it == "Masculin") "M" else "F")) }
                )

                DropdownSelector(
                    title = "Situation matrimoniale",
                    options = FormConstants.SITUATIONS,
                    selected = currentDep.situationM ?: "",
                    onSelect = { viewModel.updateCurrentDependant(currentDep.copy(situationM = it)) }
                )
            }

            // --- SECTION 2 : CONTACT & LIENS ---
            FormSection(title = "Contact & Relation") {
                DropdownSelector(
                    title = "Lien de parenté",
                    options = FormConstants.LIENS_PARENTE,
                    selected = currentDep.lienParent ?: "",
                    onSelect = { viewModel.updateCurrentDependant(currentDep.copy(lienParent = it)) },
                    placeholder = "Sélectionner le lien"
                )

                Row(horizontalArrangement = Arrangement.spacedBy(ModalStyle.Gap)) {
                    Box(Modifier.weight(1f)) {
                        FormTextField(
                            value = currentDep.whatsapp ?: "",
                            onValueChange = { viewModel.updateCurrentDependant(currentDep.copy(whatsapp = Formatters.formatPhoneNumber(it))) },
                            label = "Téléphone",
                            placeholder = "77 ...",
                            keyboardType = KeyboardType.Phone,
                            maxLength = 15,
                        )
                    }
                    Box(Modifier.weight(1f)) {
                        FormTextField(
                            value = currentDep.adresse ?: "",
                            onValueChange = { viewModel.updateCurrentDependant(currentDep.copy(adresse = it)) },
                            label = "Adresse",
                            placeholder = "Quartier...",
                        )
                    }
                }
            }

            // --- SECTION 3 : DOCUMENTS ---
            FormSection(title = "Documents Officiels") {
                SegmentedSelector(
                    title = "Type de pièce",
                    options = FormConstants.TYPES_PIECE,
                    selected = currentDep.typePiece ?: "CNI",
                    onSelect = { viewModel.updateCurrentDependant(currentDep.copy(typePiece = it)) }
                )

                FormTextField(
                    value = if (currentDep.typePiece == "CNI") currentDep.numeroCNi ?: "" else currentDep.numeroExtrait ?: "",
                    onValueChange = {
                        if (currentDep.typePiece == "CNI") viewModel.updateCurrentDependant(currentDep.copy(numeroCNi = it))
                        else viewModel.updateCurrentDependant(currentDep.copy(numeroExtrait = it))
                    },
                    label = if (currentDep.typePiece == "CNI") "Numéro CNI" else "Numéro Extrait",
                    placeholder = "Saisir le numéro",
                    keyboardType = KeyboardType.Number,
                )

                Text(
                    text = "Photos justificatives",
                    style = MaterialTheme.typography.labelMedium,
                    color = ModalStyle.SectionTitle,
                    modifier = Modifier.padding(top = 8.dp)
                )

                // Galerie horizontale pour les photos
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(Modifier.weight(1f)) {
                        MiniImagePicker(label = "Profil", uri = currentDep.photo) { viewModel.updateDependantPhotoUri(
                            it as Uri?
                        ) }
                    }
                    Box(Modifier.weight(1f)) {
                        MiniImagePicker(label = "Recto", uri = currentDep.photoRecto) { viewModel.updateDependantRectoUri(
                            it as Uri?
                        ) }
                    }
                    Box(Modifier.weight(1f)) {
                        MiniImagePicker(label = "Verso", uri = currentDep.photoVerso) { viewModel.updateDependantVersoUri(
                            it as Uri?
                        ) }
                    }
                }
            }
        }

        // 3. Footer d'actions (Premium)
        Surface(
            shadowElevation = 16.dp,
            color = AppColors.SurfaceBackground,
            border = BorderStroke(1.dp, AppColors.BorderColorLight)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp),
                    shape = AppShapes.MediumRadius,
                    border = BorderStroke(1.dp, AppColors.BorderColor)
                ) {
                    Text("Annuler", color = AppColors.TextSub, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onSave,
                    modifier = Modifier
                        .weight(1.5f)
                        .height(54.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.BrandBlue),
                    shape = AppShapes.MediumRadius,
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Text(
                        if (isEditing) "Enregistrer les modifications" else "Ajouter au dossier",
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}

// ==================== HELPER COMPONENTS ====================

@Composable
private fun PremiumModalHeader(title: String, onClose: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.horizontalGradient(com.example.sencsu.theme.AppGradients.Brand)
            )
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        Column {
            Text(
                text = "CSU - SÉNÉGAL",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.7f),
                fontWeight = FontWeight.Black,
                letterSpacing = 1.5.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
        }

        IconButton(
            onClick = onClose,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .background(Color.White.copy(alpha = 0.2f), CircleShape)
        ) {
            Icon(Icons.Rounded.Close, contentDescription = "Fermer", tint = Color.White)
        }
    }
}


@Composable
private fun MiniImagePicker(label: String, uri: String?, onPick: (Any?) -> Unit) {
    // Wrapper autour de ton ImagePickerComponent pour l'adapter à la grille
    // On suppose que ImagePickerComponent gère l'affichage compact si on le contraint
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        ImagePickerComponent(
            label = "",
            imageUri = uri,
            onImageSelected = onPick,
            modifier = Modifier
                .aspectRatio(1f)
                .clip(com.example.sencsu.theme.AppShapes.MediumRadius)
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = label, 
            style = MaterialTheme.typography.labelMedium, 
            color = com.example.sencsu.theme.AppColors.TextSub,
            fontWeight = FontWeight.Bold
        )
    }
}