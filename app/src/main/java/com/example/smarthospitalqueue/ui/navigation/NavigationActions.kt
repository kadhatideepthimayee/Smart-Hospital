package com.example.smarthospitalqueue.ui.navigation

import androidx.navigation.NavController

class NavigationActions(private val navController: NavController) {

    /**
     * Navigate from Splash to Login.
     * Pops Splash off the stack so back-press doesn't return to it.
     */
    val navigateToLogin: () -> Unit = {
        navController.navigate(Screen.Login.route) {
            popUpTo(Screen.Splash.route) { inclusive = true }
        }
    }

    /**
     * Navigate to Dashboard after successful login.
     * Clears the entire back stack so back-press exits the app.
     */
    val navigateToDashboard: () -> Unit = {
        navController.navigate(Screen.Dashboard.route) {
            popUpTo(0) { inclusive = true }
        }
    }

    /** Navigate back one step in the back stack. */
    val navigateUp: () -> Unit = {
        navController.navigateUp()
    }
}