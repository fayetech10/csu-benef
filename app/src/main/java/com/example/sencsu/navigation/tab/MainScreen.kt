package com.example.sencsu.navigation.tab

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.sencsu.navigation.Screen
import com.example.sencsu.screen.*
import com.example.sencsu.theme.AppColors
import com.example.sencsu.theme.AppShapes

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainScreen(rootNavController: NavController) {
    val nestedNavController = rememberNavController()
    val navBackStackEntry by nestedNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

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
                DashboardScreen(rootNavController = rootNavController)
            }
            composable(BottomNavItem.Dependents.route) {
                DependentsScreen()
            }
            composable(BottomNavItem.Renewal.route) {
                RenewalScreen(onFinish = { nestedNavController.navigate(BottomNavItem.Home.route) })
            }
            composable(BottomNavItem.Documents.route) {
                DocumentsScreen()
            }
            composable(BottomNavItem.Profile.route) {
                ProfileScreen(
                    onLogout = {
                        // Naviguer vers le login en vidant le backstack
                        rootNavController.navigate(Screen.BeneficiaryLogin.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun SubscriberBottomNavigation(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Dependents,
        BottomNavItem.Renewal,
        BottomNavItem.Documents,
        BottomNavItem.Profile
    )

    val selectedIndex = items.indexOfFirst { it.route == currentRoute }.coerceAtLeast(0)

    Surface(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 16.dp)
            .height(68.dp)
            .shadow(12.dp, AppShapes.LargeRadius),
        shape = AppShapes.LargeRadius,
        color = Color.White
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // INDICATOR PILL
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val itemWidth = maxWidth / items.size
                val pillOffset by animateDpAsState(
                    targetValue = itemWidth * selectedIndex,
                    animationSpec = spring(0.8f, Spring.StiffnessLow),
                    label = "pill"
                )

                Box(
                    modifier = Modifier
                        .offset(x = pillOffset)
                        .width(itemWidth)
                        .fillMaxHeight()
                        .padding(6.dp)
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        shape = AppShapes.MediumRadius,
                        color = AppColors.BrandBlue.copy(0.08f),
                        border = BorderStroke(1.dp, AppColors.BrandBlue.copy(0.05f))
                    ) {}
                }
            }

            // ITEMS
            Row(modifier = Modifier.fillMaxSize()) {
                items.forEachIndexed { index, item ->
                    val isSelected = index == selectedIndex
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
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = if (isSelected) item.icon else item.iconOutlined,
                                contentDescription = item.label,
                                tint = if (isSelected) AppColors.BrandBlue else AppColors.TextSub.copy(0.6f),
                                modifier = Modifier
                                    .size(22.dp)
                                    .scale(if (isSelected) 1.1f else 1f)
                            )
                            if (isSelected) {
                                Text(
                                    item.label,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    color = AppColors.BrandBlue
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}