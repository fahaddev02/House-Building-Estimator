package com.example.paintcalculatorestimator.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.paintcalculatorestimator.data.preferences.AppPreferences
import com.example.paintcalculatorestimator.presentation.home.HomeScreen
import com.example.paintcalculatorestimator.presentation.onboarding.OnboardingScreen
import com.example.paintcalculatorestimator.presentation.project.ProjectWizardScreen
import com.example.paintcalculatorestimator.presentation.projects.ProjectDetailsScreen
import com.example.paintcalculatorestimator.presentation.projects.SavedProjectsScreen
import com.example.paintcalculatorestimator.presentation.settings.SettingsScreen
import com.example.paintcalculatorestimator.presentation.splash.SplashScreen
import com.example.paintcalculatorestimator.presentation.wall.WallCalculatorScreen
import com.example.paintcalculatorestimator.presentation.ceiling.CeilingCalculatorScreen
import com.example.paintcalculatorestimator.presentation.paint.PaintCalculatorScreen
import com.example.paintcalculatorestimator.presentation.cost.CostCalculatorScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    appPreferences: AppPreferences
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(route = Screen.Splash.route) {
            SplashScreen(navController = navController, appPreferences = appPreferences)
        }

        composable(route = Screen.Onboarding.route) {
            OnboardingScreen(navController = navController, appPreferences = appPreferences)
        }

        composable(route = Screen.Home.route) {
            HomeScreen(navController = navController, appPreferences = appPreferences)
        }

        composable(route = Screen.Settings.route) {
            SettingsScreen(navController = navController, appPreferences = appPreferences)
        }

        composable(route = Screen.Projects.route) {
            SavedProjectsScreen(navController = navController, appPreferences = appPreferences)
        }

        composable(
            route = Screen.ProjectDetails.route,
            arguments = listOf(
                navArgument("projectId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getLong("projectId") ?: 0L
            ProjectDetailsScreen(
                navController = navController,
                projectId = projectId,
                appPreferences = appPreferences
            )
        }

        composable(
            route = Screen.Wizard.route,
            arguments = listOf(
                navArgument("projectId") {
                    type = NavType.LongType
                    defaultValue = 0L
                }
            )
        ) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getLong("projectId") ?: 0L
            ProjectWizardScreen(
                navController = navController,
                projectId = projectId,
                appPreferences = appPreferences
            )
        }

        composable(route = Screen.WallCalculator.route) {
            WallCalculatorScreen(navController = navController)
        }

        composable(route = Screen.CeilingCalculator.route) {
            CeilingCalculatorScreen(navController = navController)
        }

        composable(
            route = Screen.PaintCalculatorScreen.route,
            arguments = listOf(
                navArgument("surfaceArea") {
                    type = NavType.StringType
                    defaultValue = "0.0"
                }
            )
        ) {
            PaintCalculatorScreen(navController = navController)
        }

        composable(
            route = Screen.CostCalculatorScreen.route,
            arguments = listOf(
                navArgument("paintQuantity") {
                    type = NavType.StringType
                    defaultValue = "0.0"
                }
            )
        ) {
            CostCalculatorScreen(navController = navController)
        }
    }
}
