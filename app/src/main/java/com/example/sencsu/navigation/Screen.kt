package com.example.sencsu.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Splash : Screen("splash", "Splash", Icons.Default.FlashOn)
    object RoleSelection : Screen("role_selection", "Sélection Profil", Icons.Default.VerifiedUser)
    object BeneficiaryLogin : Screen("beneficiary_login", "Connexion Bénéficiaire", Icons.Default.FamilyRestroom)
    object Login : Screen("login", "Login", Icons.Default.Lock)
    object Enrollment : Screen("enrollment", "Enrôlement", Icons.Default.AppRegistration)
    
    object Dashboard : Screen("dashboard", "Accueil", Icons.Default.Home)
    object BeneficiaryDashboard : Screen("beneficiary_dashboard", "Accueil", Icons.Default.Home)
    object Dependents : Screen("dependents", "Foyer", Icons.Default.Group)
    object Renewal : Screen("renewal", "Renouvellement", Icons.Default.Autorenew)
    object Documents : Screen("documents", "Documents", Icons.Default.Folder)
    object Notifications : Screen("notifications", "Notifications", Icons.Default.Notifications)
    
    object AdherentDetails {
        fun createRoute(adherentId: String) = "adherent_details/$adherentId"
    }

    object Members : Screen("members", "Adhérents", Icons.Default.Person)
    object AddMember : Screen("add_member", "Ajouter", Icons.Default.Add)
    object Profile : Screen("profile", "Profil", Icons.Default.AccountCircle)
    object PasswordUpdate : Screen("password_update", "Mot de passe", Icons.Default.Password)
}