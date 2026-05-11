package com.example.sencsu.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AppRegistration
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.rounded.QrCode
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Edit
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Définition centralisée de toutes les routes de navigation.
 * Chaque Screen est un singleton typé avec route, titre et icône.
 */
sealed class Screen(val route: String, val title: String, val icon: ImageVector) {

    // ── Authentification & Onboarding ──
    object Splash : Screen("splash", "Splash", Icons.Default.FlashOn)
    object RoleSelection : Screen("role_selection", "Selection Profil", Icons.Default.VerifiedUser)
    object BeneficiaryLogin : Screen("beneficiary_login", "Connexion Beneficiaire", Icons.Default.FamilyRestroom)
    object Login : Screen("login", "Login", Icons.Default.Lock)
    object Enrollment : Screen("enrollment", "Enrolement", Icons.Default.AppRegistration)

    // ── Tableaux de bord ──
    object Dashboard : Screen("dashboard", "Accueil", Icons.Default.Home)
    object BeneficiaryDashboard : Screen("beneficiary_dashboard", "Accueil", Icons.Default.Home)

    // ── Navigation bénéficiaire ──
    object Dependents : Screen("dependents", "Foyer", Icons.Default.FamilyRestroom)
    object Renewal : Screen("renewal", "Renouvellement", Icons.Default.Autorenew)
    object Documents : Screen("documents", "Documents", Icons.Default.Folder)
    object Notifications : Screen("notifications", "Notifications", Icons.Default.Notifications)

    // ── Navigation agent ──
    object Members : Screen("members", "Adherents", Icons.Default.Person)
    object AddMember : Screen("add_member", "Ajouter", Icons.Default.Add)

    // ── Détails (routes paramétrées) ──
    object AdherentDetails : Screen(
        "adherent_details/{adherentId}",
        "Détails Adhérent",
        Icons.Default.Person
    ) {
        fun createRoute(adherentId: String) = "adherent_details/$adherentId"
    }

    object MedicalHistory : Screen(
        "medical_history/{matricule}?pcId={pcId}&pcName={pcName}",
        "Historique Médical",
        Icons.Default.LocalHospital
    ) {
        fun createRoute(matricule: String, pcId: String? = null, pcName: String? = null) =
            if (pcId != null) "medical_history/$matricule?pcId=$pcId&pcName=${pcName ?: ""}"
            else "medical_history/$matricule"
    }

    object DependentDetails : Screen(
        "dependent_details/{adherentId}/{pcId}",
        "Détails Dépendent",
        Icons.Default.Person
    ) {
        fun createRoute(adherentId: String, pcId: String) = "dependent_details/$adherentId/$pcId"
    }

    object DigitalCard : Screen(
        "digital_card/{adherentId}?pcId={pcId}",
        "Carte Digitale",
        Icons.Default.Person
    ) {
        fun createRoute(adherentId: String, pcId: String? = null) =
            if (pcId != null) "digital_card/$adherentId?pcId=$pcId"
            else "digital_card/$adherentId"
    }

    // ── Profil & Paramètres ──
    object Profile : Screen("profile", "Profil", Icons.Default.AccountCircle)
    object PasswordUpdate : Screen("password_update", "Mot de passe", Icons.Default.Password) {
        fun createRoute(adherentId: String, matricule: String, defaultPassword: String) =
            "password_update/${adherentId}/${matricule}/${android.net.Uri.encode(defaultPassword)}"
    }

    object QRScanner : Screen("qr_scanner", "Scanner QR", Icons.Rounded.QrCode)

    object EditProfile : Screen(
        "edit_profile/{adherentId}?pcId={pcId}",
        "Modifier Profil",
        Icons.Default.Edit
    ) {
        fun createRoute(adherentId: String, pcId: String? = null) =
            if (pcId != null) "edit_profile/$adherentId?pcId=$pcId"
            else "edit_profile/$adherentId"
    }

    // ── Deep Linking & Scanning ──
    object DeepLink : Screen(
        "adherent?matricule={matricule}",
        "Vérification",
        Icons.Rounded.QrCode
    )

    object Payment : Screen(
        "paiement/{adherentId}/{localAdherentId}/{montantTotal}?nextRoute={nextRoute}",
        "Paiement",
        Icons.Default.Add
    ) {
        fun createRoute(
            adherentId: String? = null,
            localAdherentId: Long? = null,
            montantTotal: Int = 0,
            nextRoute: String? = null
        ) = "paiement/${adherentId ?: "null"}/${localAdherentId ?: "null"}/$montantTotal" +
                if (nextRoute != null) "?nextRoute=${android.net.Uri.encode(nextRoute)}" else ""
    }
}
