package com.example.paintcalculatorestimator

import android.graphics.Bitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.compose.rememberNavController
import androidx.test.platform.app.InstrumentationRegistry
import com.example.paintcalculatorestimator.data.preferences.AppPreferences
import com.example.paintcalculatorestimator.domain.model.Estimate
import com.example.paintcalculatorestimator.domain.model.Material
import com.example.paintcalculatorestimator.domain.model.Opening
import com.example.paintcalculatorestimator.domain.model.OpeningType
import com.example.paintcalculatorestimator.domain.model.PaintSettings
import com.example.paintcalculatorestimator.domain.model.Project
import com.example.paintcalculatorestimator.domain.model.ProjectDetails
import com.example.paintcalculatorestimator.domain.model.Room
import com.example.paintcalculatorestimator.domain.model.RoomDetails
import com.example.paintcalculatorestimator.domain.model.UnitType
import com.example.paintcalculatorestimator.domain.repository.ProjectRepository
import com.example.paintcalculatorestimator.domain.usecase.DeleteProjectUseCase
import com.example.paintcalculatorestimator.domain.usecase.DuplicateProjectUseCase
import com.example.paintcalculatorestimator.domain.usecase.GetAllProjectsUseCase
import com.example.paintcalculatorestimator.domain.usecase.GetProjectByIdUseCase
import com.example.paintcalculatorestimator.domain.usecase.GetRecentProjectsUseCase
import com.example.paintcalculatorestimator.domain.usecase.ProjectUseCases
import com.example.paintcalculatorestimator.domain.usecase.SaveProjectUseCase
import com.example.paintcalculatorestimator.presentation.home.HomeScreenContent
import com.example.paintcalculatorestimator.presentation.onboarding.OnboardingScreenContent
import com.example.paintcalculatorestimator.presentation.onboarding.OnboardingSlide
import com.example.paintcalculatorestimator.presentation.project.ProjectViewModel
import com.example.paintcalculatorestimator.presentation.project.ProjectWizardScreen
import com.example.paintcalculatorestimator.presentation.projects.ProjectDetailsScreenContent
import com.example.paintcalculatorestimator.presentation.projects.SavedProjectsScreenContent
import com.example.paintcalculatorestimator.presentation.settings.SettingsScreenContent
import com.example.paintcalculatorestimator.ui.theme.PaintCalculatorEstimatorTheme
import java.io.File
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test

/** Creates seven Play Store-ready captures from real Compose screens with sample data. */
class PlayStoreScreenshotsTest {
    @get:Rule val compose = createComposeRule()

    private val sampleProject = ProjectDetails(
        project = Project(
            id = 1,
            name = "Modern Family Home",
            location = "Islamabad",
            notes = "Three-bedroom house estimate"
        ),
        rooms = listOf(
            RoomDetails(
                room = Room(
                    id = 1,
                    projectId = 1,
                    name = "Living room",
                    width = 6.0,
                    length = 5.0,
                    height = 3.0,
                    includeCeiling = true,
                    unitType = UnitType.METRIC
                ),
                openings = listOf(Opening(type = OpeningType.DOOR, width = 0.9, height = 2.1))
            )
        ),
        paintSettings = PaintSettings(projectId = 1, coverage = 10.0, coats = 2, wastage = 0.10),
        materials = listOf(
            Material(projectId = 1, name = "Cement", quantity = 45.0, unitPrice = 1450.0),
            Material(projectId = 1, name = "Bricks", quantity = 8000.0, unitPrice = 18.0)
        ),
        estimate = Estimate(
            projectId = 1,
            totalSurfaceArea = 210.0,
            paintableArea = 185.0,
            paintRequired = 40.7,
            paintCost = 84000.0,
            materialsCost = 209250.0,
            laborCost = 125000.0,
            totalCost = 418250.0,
            paintPricePerUnit = 2100.0,
            laborRatePerUnit = 675.0
        )
    )

    private val secondProject = sampleProject.copy(
        project = Project(id = 2, name = "Office Renovation", location = "Rawalpindi", notes = "First-floor renovation"),
        estimate = sampleProject.estimate.copy(projectId = 2, totalCost = 286900.0)
    )

    private fun capture(name: String) {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val folder = File(context.getExternalFilesDir(null), "play-store-screenshots-v2").apply { mkdirs() }
        val bitmap = compose.onRoot().captureToImage().asAndroidBitmap()
        File(folder, "$name.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }

    private fun setScreen(content: @androidx.compose.runtime.Composable () -> Unit) {
        compose.setContent { PaintCalculatorEstimatorTheme(darkTheme = false, content = content) }
        compose.waitForIdle()
    }

    @Test fun capture01_home() {
        setScreen {
            HomeScreenContent(
                recentProjects = listOf(sampleProject),
                currency = "PKR",
                onSettingsClick = {}, onNewCalculationClick = {}, onViewAllClick = {}, onProjectClick = {},
                onWallCalcClick = {}, onCeilingCalcClick = {}, onPaintCalcClick = {}, onCostCalcClick = {}
            )
        }
        capture("01-home-dashboard")
    }

    @Test fun capture02_choose_project_type() {
        val model = wizardModel()
        setScreen {
            ProjectWizardScreen(
                navController = rememberNavController(),
                appPreferences = AppPreferences(InstrumentationRegistry.getInstrumentation().targetContext),
                viewModel = model
            )
        }
        capture("02-choose-project-type")
    }

    @Test fun capture03_project_information() {
        val model = wizardModel()
        model.projectName.value = "Modern Family Home"
        model.projectLocation.value = "Islamabad"
        model.projectNotes.value = "Three-bedroom house estimate"
        model.slabThicknessGlobal.value = "12"
        model.laborRatePerSqFt.value = "650"
        model.nextStep()
        setScreen {
            ProjectWizardScreen(
                navController = rememberNavController(),
                appPreferences = AppPreferences(InstrumentationRegistry.getInstrumentation().targetContext),
                viewModel = model
            )
        }
        capture("03-project-information")
    }

    @Test fun capture04_add_rooms() {
        val model = wizardModel()
        model.projectName.value = "Modern Family Home"
        model.projectLocation.value = "Islamabad"
        model.slabThicknessGlobal.value = "12"
        model.laborRatePerSqFt.value = "650"
        model.nextStep()
        model.nextStep()
        setScreen {
            ProjectWizardScreen(
                navController = rememberNavController(),
                appPreferences = AppPreferences(InstrumentationRegistry.getInstrumentation().targetContext),
                viewModel = model
            )
        }
        capture("04-add-rooms")
    }

    @Test fun capture05_saved_projects() {
        setScreen {
            SavedProjectsScreenContent(
                searchQuery = "",
                projectsList = listOf(sampleProject, secondProject),
                currency = "PKR",
                onSearchQueryChange = {}, onBackClick = {}, onProjectClick = {}, onEditClick = {},
                onDuplicateClick = {}, onDeleteClick = {}
            )
        }
        capture("05-saved-projects")
    }

    @Test fun capture06_estimate_summary() {
        setScreen {
            ProjectDetailsScreenContent(
                projectDetails = sampleProject,
                currency = "PKR",
                onBackClick = {}, onEditClick = {}, onDeleteClick = {}, onDuplicateClick = {}
            )
        }
        capture("06-estimate-summary")
    }

    @Test fun capture07_preferences() {
        setScreen {
            SettingsScreenContent(
                theme = "LIGHT", unitType = "METRIC", currency = "PKR",
                defaultCoverage = 10.0, defaultCoats = 2, defaultWastage = 0.10,
                onBackClick = {}, onThemeSelect = {}, onUnitTypeSelect = {}, onCurrencySelect = {},
                onDefaultCoverageChange = {}, onDefaultCoatsChange = {}, onDefaultWastageChange = {}
            )
        }
        capture("07-settings")
    }

    private fun wizardModel(): ProjectViewModel {
        val repository = object : ProjectRepository {
            override fun getAllProjects() = flowOf(emptyList<ProjectDetails>())
            override fun getRecentProjects() = flowOf(emptyList<ProjectDetails>())
            override suspend fun getProjectById(id: Long): ProjectDetails? = null
            override suspend fun saveProjectDetails(projectDetails: ProjectDetails) = 1L
            override suspend fun deleteProjectById(id: Long) = Unit
            override suspend fun duplicateProject(id: Long) = 2L
        }
        return ProjectViewModel(
            ProjectUseCases(
                GetAllProjectsUseCase(repository), GetRecentProjectsUseCase(repository), GetProjectByIdUseCase(repository),
                SaveProjectUseCase(repository), DeleteProjectUseCase(repository), DuplicateProjectUseCase(repository)
            )
        )
    }
}
