package com.example.paintcalculatorestimator.presentation.paint

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.paintcalculatorestimator.data.preferences.AppPreferences
import com.example.paintcalculatorestimator.domain.model.*
import com.example.paintcalculatorestimator.domain.usecase.ProjectUseCases
import com.example.paintcalculatorestimator.utils.PaintCalculator
import com.example.paintcalculatorestimator.utils.Validator
import com.example.paintcalculatorestimator.utils.ValidationResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class PaintCalculatorViewModel @Inject constructor(
    private val useCases: ProjectUseCases,
    private val appPreferences: AppPreferences,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // Option A: Enter Area vs Option B: Calculate Area
    val optionType = MutableStateFlow("Option A")

    // Option A Inputs
    val manualArea = MutableStateFlow("")

    // Option B Inputs
    // Mode: "Wall Area", "Ceiling Area", "Both"
    val calculationMode = MutableStateFlow("Wall Area")
    val dimLength = MutableStateFlow("")
    val dimWidth = MutableStateFlow("")
    val dimHeight = MutableStateFlow("")
    val dimWalls = MutableStateFlow("4")

    // Paint Settings
    val coveragePreset = MutableStateFlow("10") // "8", "10", "12", "Custom"
    val customCoverage = MutableStateFlow("")
    
    val coatsPreset = MutableStateFlow("2") // "1", "2", "3", "Custom"
    val customCoats = MutableStateFlow("")

    val wastagePreset = MutableStateFlow("10") // "5", "10", "15", "Custom"
    val customWastage = MutableStateFlow("")

    // Calculated Outputs
    private val _surfaceAreaResult = MutableStateFlow(0.0)
    val surfaceAreaResult: StateFlow<Double> = _surfaceAreaResult.asStateFlow()

    private val _basePaintRequired = MutableStateFlow(0.0)
    val basePaintRequired: StateFlow<Double> = _basePaintRequired.asStateFlow()

    private val _recommendedPaint = MutableStateFlow(0.0)
    val recommendedPaint: StateFlow<Double> = _recommendedPaint.asStateFlow()

    // Preferences
    val unitType = MutableStateFlow(UnitType.METRIC)

    // --- VALIDATION STATES ---
    val isValidationActive = MutableStateFlow(false)
    val firstInvalidField = MutableSharedFlow<String>(replay = 0)

    val manualAreaError = MutableStateFlow<String?>(null)
    val dimLengthError = MutableStateFlow<String?>(null)
    val dimWidthError = MutableStateFlow<String?>(null)
    val dimHeightError = MutableStateFlow<String?>(null)
    val dimWallsError = MutableStateFlow<String?>(null)

    val customCoverageError = MutableStateFlow<String?>(null)
    val customCoatsError = MutableStateFlow<String?>(null)
    val customWastageError = MutableStateFlow<String?>(null)

    init {
        // Retrieve surfaceArea arg from navigation (e.g. from Wall Calculator or Ceiling Calculator)
        val initialAreaStr: String? = savedStateHandle.get<String>("surfaceArea")
        val initialArea = initialAreaStr?.toDoubleOrNull() ?: 0.0
        if (initialArea > 0.0) {
            manualArea.value = String.format(Locale.US, "%.2f", initialArea)
        }

        viewModelScope.launch {
            appPreferences.unitTypeFlow.collect { unitTypeStr ->
                unitType.value = try {
                    UnitType.valueOf(unitTypeStr.uppercase())
                } catch (e: Exception) {
                    UnitType.METRIC
                }
            }
        }

        // Setup real-time validators
        fun setupRealtimeValidation(
            fieldFlow: Flow<String>,
            errorFlow: MutableStateFlow<String?>,
            validator: (String) -> ValidationResult
        ) {
            viewModelScope.launch {
                fieldFlow.collect { value ->
                    if (isValidationActive.value || errorFlow.value != null) {
                        val res = validator(value)
                        errorFlow.value = if (res is ValidationResult.Invalid) res.message else null
                    }
                }
            }
        }

        setupRealtimeValidation(manualArea, manualAreaError) { Validator.validatePositiveNumber(it, "surface area", "Please enter surface area") }
        setupRealtimeValidation(dimLength, dimLengthError) { Validator.validatePositiveNumber(it, "wall length", "Please enter wall length") }
        setupRealtimeValidation(dimWidth, dimWidthError) { Validator.validatePositiveNumber(it, "wall width", "Please enter wall width") }
        setupRealtimeValidation(dimHeight, dimHeightError) { Validator.validatePositiveNumber(it, "wall height", "Please enter wall height") }
        setupRealtimeValidation(dimWalls, dimWallsError) { Validator.validateInteger(it, "number of walls", "Please enter number of walls") }
        setupRealtimeValidation(customCoverage, customCoverageError) { Validator.validatePositiveNumber(it, "paint coverage", "Please enter paint coverage") }
        setupRealtimeValidation(customCoats, customCoatsError) { Validator.validateInteger(it, "number of coats", "Please select number of coats") }
        setupRealtimeValidation(customWastage, customWastageError) { Validator.validateNonNegativeNumber(it, "wastage percentage", "Please enter wastage percentage") }

        // Auto calculate on input changes
        viewModelScope.launch {
            merge(
                optionType.map { },
                manualArea.map { },
                calculationMode.map { },
                dimLength.map { },
                dimWidth.map { },
                dimHeight.map { },
                dimWalls.map { },
                coveragePreset.map { },
                customCoverage.map { },
                coatsPreset.map { },
                customCoats.map { },
                wastagePreset.map { },
                customWastage.map { }
            ).collect {
                calculate()
            }
        }
    }

    fun calculate() {
        val area = if (optionType.value == "Option A") {
            manualArea.value.toDoubleOrNull() ?: 0.0
        } else {
            val l = dimLength.value.toDoubleOrNull() ?: 0.0
            val w = dimWidth.value.toDoubleOrNull() ?: 0.0
            val h = dimHeight.value.toDoubleOrNull() ?: 0.0
            val walls = dimWalls.value.toIntOrNull() ?: 4

            when (calculationMode.value) {
                "Wall Area" -> {
                    // 2 * (L + W) * H or average
                    if (walls == 4) 2 * (l + w) * h else {
                        val avg = (l + w) / 2.0
                        walls * avg * h
                    }
                }
                "Ceiling Area" -> {
                    l * w
                }
                "Both" -> {
                    val wallsArea = if (walls == 4) 2 * (l + w) * h else {
                        val avg = (l + w) / 2.0
                        walls * avg * h
                    }
                    val ceiling = l * w
                    wallsArea + ceiling
                }
                else -> 0.0
            }
        }

        val coverage = if (coveragePreset.value == "Custom") {
            customCoverage.value.toDoubleOrNull() ?: 10.0
        } else {
            coveragePreset.value.toDoubleOrNull() ?: 10.0
        }

        val coats = if (coatsPreset.value == "Custom") {
            customCoats.value.toIntOrNull() ?: 2
        } else {
            coatsPreset.value.toIntOrNull() ?: 2
        }

        val wastage = if (wastagePreset.value == "Custom") {
            customWastage.value.toDoubleOrNull() ?: 10.0
        } else {
            wastagePreset.value.toDoubleOrNull() ?: 10.0
        }

        val basePaint = PaintCalculator.calculateBasePaintRequired(area, coverage, coats)
        // Convert wastage to fraction
        val finalPaint = PaintCalculator.calculatePaintWithWastage(basePaint, wastage / 100.0)

        _surfaceAreaResult.value = PaintCalculator.roundQuantity(area)
        _basePaintRequired.value = PaintCalculator.roundQuantity(basePaint)
        _recommendedPaint.value = PaintCalculator.roundQuantity(finalPaint)
    }

    fun validatePaintInputs(): Boolean {
        isValidationActive.value = true
        var isValid = true
        var firstInvalid: String? = null

        fun check(fieldId: String, value: String, errorFlow: MutableStateFlow<String?>, validator: (String) -> ValidationResult): Boolean {
            val res = validator(value)
            if (res is ValidationResult.Invalid) {
                errorFlow.value = res.message
                if (isValid) {
                    isValid = false
                    firstInvalid = fieldId
                }
                return false
            } else {
                errorFlow.value = null
                return true
            }
        }

        if (optionType.value == "Option A") {
            check("manualArea", manualArea.value, manualAreaError) { Validator.validatePositiveNumber(it, "surface area", "Please enter surface area") }
        } else {
            check("dimLength", dimLength.value, dimLengthError) { Validator.validatePositiveNumber(it, "wall length", "Please enter wall length") }
            check("dimWidth", dimWidth.value, dimWidthError) { Validator.validatePositiveNumber(it, "wall width", "Please enter wall width") }
            
            if (calculationMode.value == "Wall Area" || calculationMode.value == "Both") {
                check("dimHeight", dimHeight.value, dimHeightError) { Validator.validatePositiveNumber(it, "wall height", "Please enter wall height") }
                check("dimWalls", dimWalls.value, dimWallsError) { Validator.validateInteger(it, "number of walls", "Please enter number of walls") }
            }
        }

        if (coveragePreset.value == "Custom") {
            check("customCoverage", customCoverage.value, customCoverageError) { Validator.validatePositiveNumber(it, "paint coverage", "Please enter paint coverage") }
        }
        if (coatsPreset.value == "Custom") {
            check("customCoats", customCoats.value, customCoatsError) { Validator.validateInteger(it, "number of coats", "Please select number of coats") }
        }
        if (wastagePreset.value == "Custom") {
            check("customWastage", customWastage.value, customWastageError) { Validator.validateNonNegativeNumber(it, "wastage percentage", "Please enter wastage percentage") }
        }

        if (!isValid && firstInvalid != null) {
            viewModelScope.launch {
                firstInvalidField.emit(firstInvalid!!)
            }
        }

        return isValid
    }

    fun saveResult(onSuccess: (Long) -> Unit) {
        viewModelScope.launch {
            if (!validatePaintInputs()) return@launch
            calculate()
            val format = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
            val dateStr = format.format(Date())

            val project = Project(
                id = 0,
                name = "Paint Calc - $dateStr",
                location = optionType.value,
                notes = "Saved from Paint Calculator"
            )

            val room = Room(
                id = 0,
                projectId = 0,
                name = "Painting Space",
                length = _surfaceAreaResult.value,
                width = 1.0,
                height = 0.0,
                wallCount = 0,
                includeCeiling = false,
                unitType = unitType.value
            )

            val roomDetails = RoomDetails(
                room = room,
                openings = emptyList()
            )

            val cov = if (coveragePreset.value == "Custom") {
                customCoverage.value.toDoubleOrNull() ?: 10.0
            } else {
                coveragePreset.value.toDoubleOrNull() ?: 10.0
            }

            val coats = if (coatsPreset.value == "Custom") {
                customCoats.value.toIntOrNull() ?: 2
            } else {
                coatsPreset.value.toIntOrNull() ?: 2
            }

            val wastage = if (wastagePreset.value == "Custom") {
                customWastage.value.toDoubleOrNull() ?: 10.0
            } else {
                wastagePreset.value.toDoubleOrNull() ?: 10.0
            }

            val paintSettings = PaintSettings(
                id = 0,
                projectId = 0,
                coverage = cov,
                coats = coats,
                wastage = wastage / 100.0,
                includePrimer = false,
                primerCoverage = 10.0,
                primerCoats = 0
            )

            val estimate = Estimate(
                id = 0,
                projectId = 0,
                totalSurfaceArea = _surfaceAreaResult.value,
                paintableArea = _surfaceAreaResult.value,
                paintRequired = _recommendedPaint.value,
                primerRequired = 0.0,
                paintCost = 0.0,
                primerCost = 0.0,
                materialsCost = 0.0,
                laborCost = 0.0,
                totalCost = 0.0
            )

            val details = ProjectDetails(
                project = project,
                rooms = listOf(roomDetails),
                paintSettings = paintSettings,
                materials = emptyList(),
                estimate = estimate
            )

            val savedId = useCases.saveProject(details)
            onSuccess(savedId)
        }
    }
}
