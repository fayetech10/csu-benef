package com.example.sencsu.navigation.tab

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val iconOutlined: ImageVector,
    val label: String
) {
    object Home : BottomNavItem(
        "dashboard_content",
        Icons.Rounded.Home,
        Icons.Outlined.Home,
        "Accueil"
    )
    object Members : BottomNavItem(
        "members_content",
        Icons.Rounded.Group,
        Icons.Outlined.Group,
        "Adhérents"
    )
    object Dependents : BottomNavItem(
        "dependents_content",
        Icons.Rounded.FamilyRestroom,
        Icons.Outlined.FamilyRestroom,
        "Foyer"
    )
    object Renewal : BottomNavItem(
        "renewal_content",
        Icons.Rounded.Autorenew,
        Icons.Outlined.Autorenew,
        "Renouveler"
    )
    object Documents : BottomNavItem(
        "documents_content",
        Icons.Rounded.Folder,
        Icons.Outlined.Folder,
        "Documents"
    )
    object Profile : BottomNavItem(
        "profile_content",
        Icons.Rounded.Person,
        Icons.Outlined.Person,
        "Profil"
    )
}