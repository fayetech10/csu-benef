package com.example.sencsu.navigation.tab

import android.annotation.SuppressLint
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

@Composable
fun SubscriberBottomNavigation(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Dependents,
        BottomNavItem.Profile
    )

    val selectedIndex = items.indexOfFirst { it.route == currentRoute }.coerceAtLeast(0)

    Surface(
        modifier = Modifier
            .padding(horizontal = 24.dp, vertical = 20.dp)
            .height(68.dp)
            .shadow(16.dp, CircleShape, ambientColor = AppColors.BrandBlue.copy(alpha = 0.4f)),
        shape = CircleShape,
        color = Color.White.copy(alpha = 0.98f),
        border = BorderStroke(1.dp, AppColors.BrandBlue.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEachIndexed { index, item ->
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
                            label = "scale"
                        )
                        
                        Box(contentAlignment = Alignment.Center) {
                            if (isSelected) {
                                // Background glow circle
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .background(
                                            AppColors.BrandBlue.copy(alpha = 0.1f),
                                            CircleShape
                                        )
                                )
                            }
                            Icon(
                                imageVector = if (isSelected) item.icon else item.iconOutlined,
                                contentDescription = item.label,
                                tint = if (isSelected) AppColors.BrandBlue else AppColors.TextSub.copy(alpha = 0.6f),
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