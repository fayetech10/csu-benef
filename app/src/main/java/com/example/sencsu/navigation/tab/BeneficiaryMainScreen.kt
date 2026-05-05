package com.example.sencsu.navigation.tab

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
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
            BeneficiaryBottomNavigation(
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
            modifier = Modifier.padding(
                top = padding.calculateTopPadding(),
                bottom = padding.calculateBottomPadding()
            ),
            enterTransition = { fadeIn(tween(300)) },
            exitTransition = { fadeOut(tween(250)) }
        ) {
            composable(BottomNavItem.Home.route) {
                BeneficiaryDashboardScreen(
                    onNavigateToHistory = { adherentId ->
                        rootNavController.navigate(Screen.MedicalHistory.createRoute(adherentId))
                    },
                    onNavigateToCard = { adherentId ->
                        rootNavController.navigate(Screen.DigitalCard.createRoute(adherentId))
                    }
                )
            }

            composable(BottomNavItem.Dependents.route) {
                DependentsScreen(
                    onNavigateToHistory = { adherentId, pcId, pcName ->
                        rootNavController.navigate(Screen.MedicalHistory.createRoute(adherentId, pcId, pcName))
                    },
                    onNavigateToDetails = { adherentId, pcId ->
                        rootNavController.navigate(Screen.DependentDetails.createRoute(adherentId, pcId))
                    }
                )
            }

            composable(BottomNavItem.Profile.route) {
                ProfileScreen(
                    onNavigateToHistory = { adherentId ->
                        rootNavController.navigate(Screen.MedicalHistory.createRoute(adherentId))
                    },
                    onNavigateToEdit = { adherentId ->
                        rootNavController.navigate(Screen.EditProfile.createRoute(adherentId))
                    },
                    onLogout = {
                        appNavViewModel.logout()
                        rootNavController.navigate(Screen.RoleSelection.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Barre de navigation DÉDIÉE à l'espace BÉNÉFICIAIRE
// Uniquement : Accueil | Foyer | Profil
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun BeneficiaryBottomNavigation(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Dependents,
        BottomNavItem.Profile
    )

    Surface(
        modifier = Modifier
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .height(68.dp)
            .shadow(
                14.dp, CircleShape,
                ambientColor = AppColors.BrandBlue.copy(alpha = 0.12f),
                spotColor = AppColors.BrandBlue.copy(alpha = 0.16f)
            ),
        shape = CircleShape,
        color = Color.Transparent,
        border = BorderStroke(1.dp, AppColors.BorderColorLight)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(AppColors.SurfaceBackground, AppColors.SurfaceMuted)
                    )
                )
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEach { item ->
                    val isSelected = currentRoute == item.route

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { onNavigate(item.route) },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            val iconScale by animateFloatAsState(
                                targetValue = if (isSelected) 1.25f else 1f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessLow
                                ),
                                label = "beneficiary_nav_scale"
                            )

                            Box(contentAlignment = Alignment.Center) {
                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .background(AppColors.BrandBlueLite, CircleShape)
                                    )
                                }
                                Icon(
                                    imageVector = if (isSelected) item.icon else item.iconOutlined,
                                    contentDescription = item.label,
                                    tint = if (isSelected) AppColors.BrandBlue else AppColors.TextSub,
                                    modifier = Modifier
                                        .size(24.dp)
                                        .scale(iconScale)
                                )
                            }

                            AnimatedVisibility(
                                visible = isSelected,
                                enter = expandVertically(expandFrom = Alignment.Top) + fadeIn(),
                                exit = shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut()
                            ) {
                                Text(
                                    text = item.label,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = AppColors.BrandBlue,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
