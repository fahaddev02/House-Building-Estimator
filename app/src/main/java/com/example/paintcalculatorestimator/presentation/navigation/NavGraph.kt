package com.example.paintcalculatorestimator.presentation.navigation

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.content.ActivityNotFoundException
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.paintcalculatorestimator.billing.BillingManager
import com.example.paintcalculatorestimator.data.preferences.AppPreferences
import com.example.paintcalculatorestimator.presentation.ceiling.CeilingCalculatorScreen
import com.example.paintcalculatorestimator.presentation.cost.CostCalculatorScreen
import com.example.paintcalculatorestimator.presentation.home.HomeScreen
import com.example.paintcalculatorestimator.presentation.onboarding.OnboardingScreen
import com.example.paintcalculatorestimator.presentation.paint.PaintCalculatorScreen
import com.example.paintcalculatorestimator.presentation.premium.PremiumScreen
import com.example.paintcalculatorestimator.presentation.project.ProjectWizardScreen
import com.example.paintcalculatorestimator.presentation.projects.ProjectDetailsScreen
import com.example.paintcalculatorestimator.presentation.projects.SavedProjectsScreen
import com.example.paintcalculatorestimator.presentation.settings.SettingsScreen
import com.example.paintcalculatorestimator.presentation.splash.SplashScreen
import com.example.paintcalculatorestimator.presentation.wall.WallCalculatorScreen
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private const val PRIVACY_POLICY_URL = "https://sites.google.com/view/costestimatorprivacypolicy/home"

@Composable
fun NavGraph(
    navController: NavHostController,
    appPreferences: AppPreferences
) {
    val appContext = LocalContext.current
    val appBilling = remember(appContext) { BillingManager.getInstance(appContext) }
    val appBillingState by appBilling.uiState.collectAsState()
    LaunchedEffect(appBillingState.errorMessage) {
        appBillingState.errorMessage?.let { Toast.makeText(appContext, it, Toast.LENGTH_LONG).show() }
    }
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(route = Screen.Splash.route) {
            SplashScreen(
                navController = navController,
                appPreferences = appPreferences
            )
        }

        composable(route = Screen.Premium.route) {
            val context = LocalContext.current
            val activity = context as? Activity
            val coroutineScope = rememberCoroutineScope()
            val billingManager = remember { BillingManager.getInstance(context) }
            val billingState by billingManager.uiState.collectAsState()

            fun navigateAfterPremium() {
                if (navController.previousBackStackEntry != null) {
                    navController.popBackStack()
                    return
                }
                coroutineScope.launch {
                    val onboardingCompleted = appPreferences.isOnboardingCompletedFlow.first()
                    val nextRoute = if (onboardingCompleted) Screen.Home.route else Screen.Onboarding.route
                    navController.navigate(nextRoute) {
                        popUpTo(Screen.Premium.route) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }

            BackHandler {
                navigateAfterPremium()
            }

            LaunchedEffect(billingState.isPremium) {
                if (billingState.isPremium) {
                    navigateAfterPremium()
                }
            }

            PremiumScreen(
                onClose = { navigateAfterPremium() },
                onContinue = { plan ->
                    if (activity != null) {
                        billingManager.launchPurchaseFlow(activity, plan)
                    }
                },
                onCancelSubscription = {
                    billingManager.openSubscriptionManagement(context)
                },
                onPrivacyClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(PRIVACY_POLICY_URL))
                    try { context.startActivity(intent) }
                    catch (_: ActivityNotFoundException) { billingManager.reportError("No browser is available to open the privacy policy.") }
                },
                monthlyPrice = billingState.monthlyPrice,
                yearlyPrice = billingState.yearlyPrice,
                yearlyPerMonthPrice = billingState.yearlyPerMonthPrice,
                purchaseEnabled = billingState.purchaseEnabled,
                isProcessing = billingState.isProcessing,
                errorMessage = billingState.errorMessage,
                onRetry = { billingManager.refresh() }
            )
        }

        composable(route = Screen.Onboarding.route) {
            OnboardingScreen(navController = navController, appPreferences = appPreferences)
        }

        composable(route = Screen.Home.route) {
            HomeScreen(
                navController = navController,
                appPreferences = appPreferences
            )
        }

        composable(route = Screen.Settings.route) {
            SettingsScreen(
                navController = navController,
                appPreferences = appPreferences
            )
        }

        composable(route = Screen.Projects.route) {
            SavedProjectsScreen(
                navController = navController,
                appPreferences = appPreferences
            )
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
            WallCalculatorScreen(
                navController = navController
            )
        }

        composable(route = Screen.CeilingCalculator.route) {
            CeilingCalculatorScreen(
                navController = navController
            )
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
            PaintCalculatorScreen(
                navController = navController
            )
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
            CostCalculatorScreen(
                navController = navController
            )
        }
    }
}
