package com.example.sencsu.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.sencsu.navigation.tab.MainScreen
import com.example.sencsu.navigation.tab.BeneficiaryMainScreen
import com.example.sencsu.screen.*

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screen.Splash.route) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToLogin = { navController.navigate(Screen.BeneficiaryLogin.route) { popUpTo(Screen.Splash.route) { inclusive = true } } },
                onNavigateToDashboard = { navController.navigate("main_tabs") { popUpTo(Screen.Splash.route) { inclusive = true } } }
            )
        }

        composable(Screen.BeneficiaryLogin.route) {
            BeneficiaryLoginScreen(
                onLoginSuccess = { navController.navigate("beneficiary_tabs") { popUpTo(Screen.BeneficiaryLogin.route) { inclusive = true } } },
                onNavigateToEnrollment = { navController.navigate(Screen.Enrollment.route) }
            )
        }

        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = { navController.navigate("main_tabs") { popUpTo(Screen.Login.route) { inclusive = true } } },
                onNavigateToEnrollment = { navController.navigate(Screen.Enrollment.route) }
            )
        }

        composable(Screen.Enrollment.route) {
            EnrolementScreen(
                onSuccess = { event -> 
                    navController.navigate(Screen.PasswordUpdate.route + "/${event.adherentId}/${event.matricule}/${event.defaultPassword}") {
                        popUpTo(Screen.Enrollment.route) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.PasswordUpdate.route + "/{adherentId}/{matricule}/{defaultPassword}"
        ) { backStackEntry ->
            val adherentId = backStackEntry.arguments?.getString("adherentId") ?: ""
            val matricule = backStackEntry.arguments?.getString("matricule") ?: ""
            val defaultPassword = backStackEntry.arguments?.getString("defaultPassword") ?: ""
            
            PasswordUpdateScreen(
                adherentId = adherentId,
                matricule = matricule,
                defaultPassword = defaultPassword,
                onSuccess = { 
                    navController.navigate(Screen.BeneficiaryLogin.route) {
                        popUpTo(Screen.PasswordUpdate.route) { inclusive = true }
                    }
                }
            )
        }

        composable("main_tabs") {
            MainScreen(rootNavController = navController)
        }

        composable("beneficiary_tabs") {
            BeneficiaryMainScreen(rootNavController = navController)
        }
        
        composable(Screen.Notifications.route) {
            NotificationsScreen()
        }
    }
}
