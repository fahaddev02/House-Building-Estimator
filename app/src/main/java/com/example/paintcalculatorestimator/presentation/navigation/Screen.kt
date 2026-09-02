package com.example.paintcalculatorestimator.presentation.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Onboarding : Screen("onboarding")
    object Home : Screen("home")
    object Settings : Screen("settings")
    object Projects : Screen("projects")
    
    object ProjectDetails : Screen("project_details/{projectId}") {
        fun createRoute(projectId: Long) = "project_details/$projectId"
    }
    
    object Wizard : Screen("wizard?projectId={projectId}") {
        fun createRoute(projectId: Long = 0) = "wizard?projectId=$projectId"
    }

    object WallCalculator : Screen("wall_calculator")
    
    object CeilingCalculator : Screen("ceiling_calculator")
    
    object PaintCalculatorScreen : Screen("paint_calculator?surfaceArea={surfaceArea}") {
        fun createRoute(surfaceArea: Double = 0.0) = "paint_calculator?surfaceArea=$surfaceArea"
    }
    
    object CostCalculatorScreen : Screen("cost_calculator?paintQuantity={paintQuantity}") {
        fun createRoute(paintQuantity: Double = 0.0) = "cost_calculator?paintQuantity=$paintQuantity"
    }
}
