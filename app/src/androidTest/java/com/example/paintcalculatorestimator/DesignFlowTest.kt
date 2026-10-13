package com.example.paintcalculatorestimator

import android.graphics.Bitmap
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.Density
import androidx.navigation.compose.rememberNavController
import androidx.test.platform.app.InstrumentationRegistry
import com.example.paintcalculatorestimator.presentation.home.HomeScreenContent
import com.example.paintcalculatorestimator.presentation.onboarding.OnboardingScreenContent
import com.example.paintcalculatorestimator.presentation.onboarding.OnboardingSlide
import com.example.paintcalculatorestimator.ui.theme.PaintCalculatorEstimatorTheme
import com.example.paintcalculatorestimator.presentation.project.ProjectViewModel
import com.example.paintcalculatorestimator.presentation.project.ProjectWizardScreen
import com.example.paintcalculatorestimator.domain.model.ProjectDetails
import com.example.paintcalculatorestimator.domain.repository.ProjectRepository
import com.example.paintcalculatorestimator.domain.usecase.*
import com.example.paintcalculatorestimator.data.preferences.AppPreferences
import kotlinx.coroutines.flow.flowOf
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class DesignFlowTest {
    @get:Rule val compose = createComposeRule()

    private fun saveScreenshot(name: String) {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val folder = File(context.getExternalFilesDir(null), "design-preview").apply { mkdirs() }
        val bitmap = compose.onRoot().captureToImage().asAndroidBitmap()
        File(folder, "$name.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }

    @Test fun wizardSelectionWaitsForContinueAndRetainsValidation() {
        val repository = object : ProjectRepository {
            override fun getAllProjects() = flowOf(emptyList<ProjectDetails>())
            override fun getRecentProjects() = flowOf(emptyList<ProjectDetails>())
            override suspend fun getProjectById(id: Long): ProjectDetails? = null
            override suspend fun saveProjectDetails(projectDetails: ProjectDetails) = 1L
            override suspend fun deleteProjectById(id: Long) = Unit
            override suspend fun duplicateProject(id: Long) = 2L
        }
        val model = ProjectViewModel(ProjectUseCases(GetAllProjectsUseCase(repository), GetRecentProjectsUseCase(repository),
            GetProjectByIdUseCase(repository), SaveProjectUseCase(repository), DeleteProjectUseCase(repository), DuplicateProjectUseCase(repository)))
        val prefs = AppPreferences(InstrumentationRegistry.getInstrumentation().targetContext)
        compose.setContent {
            PaintCalculatorEstimatorTheme(darkTheme = false) {
                ProjectWizardScreen(rememberNavController(), appPreferences = prefs, viewModel = model)
            }
        }
        saveScreenshot("wizard-type")
        compose.onNodeWithText("Market", useUnmergedTree = true).performClick()
        compose.runOnIdle { assertEquals("Market", model.projectType.value); assertEquals(1, model.currentStep.value) }
        compose.onNodeWithText("Continue", substring = true).performClick()
        compose.onNodeWithText("Tell us about the project").assertIsDisplayed()
        saveScreenshot("wizard-info")
        compose.onNodeWithText("Continue", substring = true).performClick()
        compose.runOnIdle { assertEquals(2, model.currentStep.value) }
        compose.onNodeWithText("Please enter project name").assertExists()
        compose.runOnIdle {
            model.projectName.value = "Market renovation"
            model.projectLocation.value = "Islamabad"
            model.slabThicknessGlobal.value = "12"
            model.laborRatePerSqFt.value = "250"
        }
        compose.onNodeWithText("Continue", substring = true).performClick()
        compose.onNodeWithText("Add your rooms").assertIsDisplayed()
        saveScreenshot("wizard-rooms")
        compose.onNodeWithContentDescription("Back").performClick()
        compose.onNodeWithText("Market renovation").assertExists()
    }

    @Test fun dashboardKeepsCalculatorAndProjectActions() {
        var action = ""
        compose.setContent {
            PaintCalculatorEstimatorTheme(darkTheme = false) {
                HomeScreenContent(emptyList(), "PKR", { action = "settings" }, { action = "new" },
                    { action = "projects" }, {}, { action = "wall" }, { action = "ceiling" },
                    { action = "paint" }, { action = "cost" })
            }
        }
        saveScreenshot("dashboard-light")
        compose.onNodeWithContentDescription("Settings").performClick()
        compose.runOnIdle { assertEquals("settings", action) }
        compose.onNodeWithText("Start a new project").performClick()
        compose.runOnIdle { assertEquals("new", action) }
        listOf("Wall Calculator" to "wall", "Ceiling Calculator" to "ceiling", "Paint Calculator" to "paint", "Cost Calculator" to "cost").forEach { (label, expected) ->
            compose.onNodeWithText(label).performScrollTo().performClick()
            compose.runOnIdle { assertEquals(expected, action) }
        }
        compose.onNodeWithText("View all").performScrollTo().performClick()
        compose.runOnIdle { assertEquals("projects", action) }
    }

    @Test fun darkDashboardWithLargeTextStillReachesCreateAction() {
        var clicked = false
        compose.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, 1.5f)) {
                PaintCalculatorEstimatorTheme(darkTheme = true) {
                    HomeScreenContent(emptyList(), "PKR", {}, { clicked = true }, {}, {}, {}, {}, {}, {})
                }
            }
        }
        saveScreenshot("dashboard-dark-large-text")
        compose.onNodeWithText("Create your first estimate").performScrollTo().performClick()
        compose.runOnIdle { assertEquals(true, clicked) }
    }

    @Test fun onboardingLastPageFinishesAndCanBeSkipped() {
        var action = ""
        val slides = listOf(
            OnboardingSlide(R.string.onboarding_title_1, R.string.onboarding_desc_1, R.drawable.img_first_onboarding),
            OnboardingSlide(R.string.onboarding_title_2, R.string.onboarding_desc_2, R.drawable.img_second_onboarding),
            OnboardingSlide(R.string.onboarding_title_3, R.string.onboarding_desc_3, R.drawable.img_3rd_onboarding)
        )
        compose.setContent {
            PaintCalculatorEstimatorTheme(darkTheme = false) {
                OnboardingScreenContent(2, slides, { action = "next" }, { action = "skip" }, { action = "finish" })
            }
        }
        saveScreenshot("onboarding-save")
        compose.onNodeWithText("Get Started", substring = true).performClick()
        compose.runOnIdle { assertEquals("finish", action) }
        compose.onNodeWithText("Skip").performClick()
        compose.runOnIdle { assertEquals("skip", action) }
    }
}
