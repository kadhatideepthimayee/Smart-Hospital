package com.example.smarthospitalqueue.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.smarthospitalqueue.ui.navigation.Screen
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState

// ─────────────────────────────────────────────────────────────────────────────
// Bottom Navigation Bar — Smart Hospital Queue Management System
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Metadata for each bottom navigation tab.
 */
data class BottomNavItem(
    val screen: Screen,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
)

/** Only Dashboard tab exists for now. More tabs added as screens are built. */
val bottomNavItems = listOf(
    BottomNavItem(
        screen         = Screen.Dashboard,
        label          = "Dashboard",
        selectedIcon   = Icons.Filled.Dashboard,
        unselectedIcon = Icons.Outlined.Dashboard,
    ),
)

/**
 * Material 3 [NavigationBar] for the main app graph.
 */
@Composable
fun HospitalBottomNavBar(
    navController: NavController,
    items: List<BottomNavItem> = bottomNavItems,
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar {
        items.forEach { item ->
            val selected = currentRoute == item.screen.route

            NavigationBarItem(
                selected = selected,
                onClick  = {
                    navController.navigate(item.screen.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState    = true
                    }
                },
                icon = {
                    Icon(
                        imageVector        = if (selected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.label,
                    )
                },
                label          = { Text(text = item.label) },
                alwaysShowLabel = true,
            )
        }
    }
}

/**
 * Returns `true` when the current route is a bottom-nav tab route.
 * Used to show/hide [HospitalBottomNavBar] from the host Scaffold.
 */
@Composable
fun NavController.isBottomNavVisible(): Boolean {
    val entry by currentBackStackEntryAsState()
    val route  = entry?.destination?.route
    return bottomNavItems.any { it.screen.route == route }
}