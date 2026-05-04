package com.example.sencsu.navigation.tab

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.sencsu.domain.viewmodel.AppNavigationViewModel
import com.example.sencsu.navigation.Screen
import com.example.sencsu.screen.*
import com.example.sencsu.theme.AppColors

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun BeneficiaryMainScreen(rootNavController: NavController) {
    val nestedNavController = rememberNavController()
    val navBackStackEntry by nestedNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val appNavViewModel: AppNavigationViewModel = hiltViewModel()

    Scaffold(
        bottomBar = {
            SubscriberBottomNavigation(
                currentRoute = currentRoute,
                onNavigate = { route ->
                    nestedNavController.navigate(route) {
                        popUpTo(nestedNavController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        },
        containerColor = AppColors.AppBackground
    ) { padding ->
        NavHost(
            navController = nestedNavController,
            startDestination = BottomNavItem.Home.route,
            modifier = Modifier.padding(padding),
            enterTransition = { fadeIn(tween(300)) },
            exitTransition = { fadeOut(tween(250)) }
        ) {
            composable(BottomNavItem.Home.route) {
                BeneficiaryDashboardScreen(
                    onNavigateToHistory = { adherentId ->
                        nestedNavController.navigate(Screen.MedicalHistory.createRoute(adherentId))
                    }
                )
            }
            composable(
                route = Screen.MedicalHistory.route,
                arguments = listOf(
                    navArgument("adherentId") { type = NavType.StringType },
                    navArgument("pcId") { 
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    },
                    navArgument("pcName") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) {
                MedicalHistoryScreen(
                    onBack = { nestedNavController.popBackStack() }
                )
            }
            composable(BottomNavItem.Dependents.route) {
                DependentsScreen(
                    onNavigateToHistory = { adherentId, pcId, pcName ->
                        nestedNavController.navigate(Screen.MedicalHistory.createRoute(adherentId, pcId, pcName))
                    }
                ) 
            }

            composable(BottomNavItem.Profile.route) {
                ProfileScreen(
                    onNavigateToHistory = { adherentId ->
                        nestedNavController.navigate(Screen.MedicalHistory.createRoute(adherentId))
                    },
                    onLogout = {
                        // 1. Effacer la session (DataStore + token cache HTTP)
                        appNavViewModel.logout()
                        // 2. Naviguer vers le login en vidant le backstack
                        rootNavController.navigate(Screen.BeneficiaryLogin.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}
