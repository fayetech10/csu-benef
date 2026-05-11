package com.example.sencsu.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.sencsu.navigation.tab.BeneficiaryMainScreen
import com.example.sencsu.navigation.tab.MainScreen
import com.example.sencsu.screen.BeneficiaryLoginScreen
import com.example.sencsu.screen.EnrolementScreen
import com.example.sencsu.screen.LoginScreen
import com.example.sencsu.screen.NotificationsScreen
import com.example.sencsu.screen.PasswordUpdateScreen
import com.example.sencsu.screen.RoleSelectionScreen
import com.example.sencsu.screen.SplashScreen
import com.example.sencsu.screen.QrScannerScreen
import com.example.sencsu.screen.DeepLinkResolverScreen
import com.example.sencsu.screen.Paiement
import com.example.sencsu.screen.RenewalScreen
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavigation(
    viewModel: com.example.sencsu.domain.viewmodel.AppNavigationViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    val navController = rememberNavController()

    // Écouter l'événement de déconnexion globale (ex: 401 Unauthorized)
    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.logoutEvent.collect {
            navController.navigate(Screen.RoleSelection.route) {
                popUpTo(0) { inclusive = true }
            }
        }
    }


    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
        enterTransition = { fadeIn(tween(400)) + slideInHorizontally(initialOffsetX = { 1000 }, animationSpec = tween(400)) },
        exitTransition = { fadeOut(tween(400)) + slideOutHorizontally(targetOffsetX = { -1000 }, animationSpec = tween(400)) },
        popEnterTransition = { fadeIn(tween(400)) + slideInHorizontally(initialOffsetX = { -1000 }, animationSpec = tween(400)) },
        popExitTransition = { fadeOut(tween(400)) + slideOutHorizontally(targetOffsetX = { 1000 }, animationSpec = tween(400)) }
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToLogin = {
                    navController.navigate(Screen.RoleSelection.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToDashboard = { role ->
                    val target = if (role == "AGENT") "main_tabs" else "beneficiary_tabs"
                    navController.navigate(target) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.RoleSelection.route) {
            RoleSelectionScreen(
                onSelectAgent = { navController.navigate(Screen.Login.route) },
                onSelectBeneficiary = { navController.navigate(Screen.BeneficiaryLogin.route) }
            )
        }

        composable(Screen.BeneficiaryLogin.route) {
            BeneficiaryLoginScreen(
                onLoginSuccess = {
                    navController.navigate("beneficiary_tabs") {
                        popUpTo(Screen.BeneficiaryLogin.route) { inclusive = true }
                    }
                },
                onNavigateToEnrollment = { navController.navigate(Screen.Enrollment.route) }
            )
        }

        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate("main_tabs") {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToEnrollment = { navController.navigate(Screen.Enrollment.route) }
            )
        }

        composable(Screen.Enrollment.route) {
            EnrolementScreen(
                onSuccess = { event ->
                    navController.navigate(
                        Screen.PasswordUpdate.createRoute(
                            event.adherentId,
                            event.matricule,
                            event.defaultPassword
                        )
                    ) {
                        popUpTo(Screen.Enrollment.route) { inclusive = true }
                    }
                },
                onNavigateToPayment = { event ->
                    // On définit la route suivante comme étant la mise à jour du mot de passe
                    val nextRoute = if (event.matricule != null) {
                        Screen.PasswordUpdate.createRoute(
                            event.adherentId ?: "",
                            event.matricule,
                            event.defaultPassword ?: ""
                        )
                    } else null
                    
                    navController.navigate(
                        Screen.Payment.createRoute(
                            adherentId = event.adherentId,
                            localAdherentId = event.localAdherentId,
                            montantTotal = event.montantTotal,
                            nextRoute = nextRoute
                        )
                    ) {
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
            // Décoder car le message peut contenir des espaces/caractères spéciaux
            val defaultPasswordRaw = backStackEntry.arguments?.getString("defaultPassword") ?: ""
            val defaultPassword = android.net.Uri.decode(defaultPasswordRaw)

            PasswordUpdateScreen(
                adherentId = adherentId,
                matricule = matricule,
                defaultPassword = defaultPassword,
                onSuccess = {
                    navController.navigate("beneficiary_tabs") {
                        popUpTo(0) { inclusive = true }
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

        composable(
            route = Screen.MedicalHistory.route,
            arguments = listOf(
                androidx.navigation.navArgument("matricule") { type = androidx.navigation.NavType.StringType },
                androidx.navigation.navArgument("pcId") { 
                    type = androidx.navigation.NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                androidx.navigation.navArgument("pcName") {
                    type = androidx.navigation.NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) {
            com.example.sencsu.screen.MedicalHistoryScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.DependentDetails.route,
            arguments = listOf(
                androidx.navigation.navArgument("adherentId") { type = androidx.navigation.NavType.StringType },
                androidx.navigation.navArgument("pcId") { type = androidx.navigation.NavType.StringType }
            )
        ) { backStackEntry ->
            val adherentId = backStackEntry.arguments?.getString("adherentId") ?: ""
            val pcId = backStackEntry.arguments?.getString("pcId") ?: ""
            
            com.example.sencsu.screen.DependentDetailsScreen(
                adherentId = adherentId,
                pcId = pcId,
                onBack = { navController.popBackStack() },
                onNavigateToHistory = { rootAdherentId, rootPcId, rootPcName ->
                    navController.navigate(Screen.MedicalHistory.createRoute(rootAdherentId, rootPcId, rootPcName))
                },
                onNavigateToCard = { aId, pId ->
                    navController.navigate(Screen.DigitalCard.createRoute(aId, pId))
                },
                onNavigateToEdit = { aId, pId ->
                    navController.navigate(Screen.EditProfile.createRoute(aId, pId))
                }
            )
        }

        composable(
            route = Screen.EditProfile.route,
            arguments = listOf(
                androidx.navigation.navArgument("adherentId") { type = androidx.navigation.NavType.StringType },
                androidx.navigation.navArgument("pcId") { 
                    type = androidx.navigation.NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val adherentId = backStackEntry.arguments?.getString("adherentId") ?: ""
            val pcId = backStackEntry.arguments?.getString("pcId")
            val addAdherentViewModel: com.example.sencsu.domain.viewmodel.AddAdherentViewModel = androidx.hilt.navigation.compose.hiltViewModel()
            
            androidx.compose.runtime.LaunchedEffect(adherentId, pcId) {
                if (pcId != null) {
                    addAdherentViewModel.fetchAndLoadDependentForEdit(adherentId, pcId)
                } else {
                    addAdherentViewModel.fetchAndLoadForEdit(adherentId)
                }
            }

            com.example.sencsu.components.AddAdherentScreen(
                onBack = { navController.popBackStack() },
                onNavigateToPayment = { _, _, _ -> }, // Pas de paiement en modification
                agentId = null,
                viewModel = addAdherentViewModel
            )
        }

        composable(
            route = Screen.AdherentDetails.route,
            arguments = listOf(
                androidx.navigation.navArgument("adherentId") { type = androidx.navigation.NavType.StringType }
            )
        ) { backStackEntry ->
            val adherentId = backStackEntry.arguments?.getString("adherentId") ?: ""
            com.example.sencsu.screen.AdherentDetailsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEdit = { id ->
                    navController.navigate(Screen.EditProfile.createRoute(id))
                },
                onNavigateToHistory = { matricule ->
                    navController.navigate(Screen.MedicalHistory.createRoute(matricule))
                }
            )
        }

        composable(
            route = Screen.DigitalCard.route,
            arguments = listOf(
                androidx.navigation.navArgument("adherentId") { type = androidx.navigation.NavType.StringType },
                androidx.navigation.navArgument("pcId") { type = androidx.navigation.NavType.StringType; nullable = true; defaultValue = null }
            )
        ) { backStackEntry ->
            val adherentId = backStackEntry.arguments?.getString("adherentId") ?: ""
            val pcId = backStackEntry.arguments?.getString("pcId")
            
            com.example.sencsu.screen.DigitalCardScreen(
                adherentId = adherentId,
                pcId = pcId,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Notifications.route) {
            NotificationsScreen()
        }

        composable(Screen.Renewal.route) {
            RenewalScreen(
                onFinish = { navController.popBackStack() }
            )
        }

        composable(Screen.QRScanner.route) {
            QrScannerScreen(
                onDismiss = { navController.popBackStack() },
                onNavigateToDetails = { adherentId ->
                    navController.navigate(Screen.AdherentDetails.createRoute(adherentId)) {
                        popUpTo(Screen.QRScanner.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.AddMember.route) {
            val addAdherentViewModel: com.example.sencsu.domain.viewmodel.AddAdherentViewModel = androidx.hilt.navigation.compose.hiltViewModel()
            com.example.sencsu.components.AddAdherentScreen(
                onBack = { navController.popBackStack() },
                onNavigateToPayment = { adherentId, localAdherentId, montantTotal ->
                    navController.navigate(
                        Screen.Payment.createRoute(adherentId, localAdherentId, montantTotal)
                    ) {
                        popUpTo(Screen.AddMember.route) { inclusive = true }
                    }
                },
                agentId = null, // Récupéré par le ViewModel via SessionManager
                viewModel = addAdherentViewModel
            )
        }

        composable(
            route = Screen.Payment.route,
            arguments = listOf(
                navArgument("adherentId") { type = NavType.StringType; nullable = true },
                navArgument("localAdherentId") { type = NavType.StringType; nullable = true },
                navArgument("montantTotal") { type = NavType.IntType; defaultValue = 0 },
                navArgument("nextRoute") { type = NavType.StringType; nullable = true; defaultValue = null }
            )
        ) { backStackEntry ->
            val adherentId = backStackEntry.arguments?.getString("adherentId")
                ?.takeIf { it != "null" }
            val localAdherentIdStr = backStackEntry.arguments?.getString("localAdherentId")
                ?.takeIf { it != "null" }
            val localAdherentId = localAdherentIdStr?.toLongOrNull()
            val montantTotal = backStackEntry.arguments?.getInt("montantTotal")?.toDouble()
            // Décoder la route suivante
            val nextRoute = backStackEntry.arguments?.getString("nextRoute")
            
            Paiement(
                adherentId = adherentId,
                localAdherentId = localAdherentId,
                montantTotal = montantTotal,
                nextRoute = nextRoute,
                navController = navController
            )
        }

        composable(
            route = Screen.DeepLink.route,
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "sencsu://adherent?matricule={matricule}"
                }
            )
        ) { backStackEntry ->
            val matricule = backStackEntry.arguments?.getString("matricule") ?: ""
            DeepLinkResolverScreen(
                matricule = matricule,
                onNavigate = { route ->
                    navController.navigate(route) {
                        // On évite d'empiler le résolveur
                        popUpTo(Screen.DeepLink.route) { inclusive = true }
                    }
                },
                onCancel = { navController.popBackStack() }
            )
        }
    }
}
