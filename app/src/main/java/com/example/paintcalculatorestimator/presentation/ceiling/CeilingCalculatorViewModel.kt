package com.example.paintcalculatorestimator.presentation.ceiling

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.paintcalculatorestimator.data.preferences.AppPreferences
import com.example.paintcalculatorestimator.domain.model.*
import com.example.paintcalculatorestimator.domain.usecase.ProjectUseCases
import com.example.paintcalculatorestimator.utils.CeilingCalculator
import com.example.paintcalculatorestimator.utils.Validator
import com.example.paintcalculatorestimator.utils.ValidationResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

data class CeilingSectionEntry(
    val name: String,
    val length: String,
    val width: String
)

data class CeilingSectionError(
    val lengthError: String? = null,
    val widthError: String? = null
)

@HiltViewModel
class CeilingCalculatorViewModel @Inject constructor(
    private val useCases: ProjectUseCases,
    private val appPreferences: AppPreferences
) : ViewModel() {

    // Shape: "Rectangle", "Square", "Custom"
    val ceilingShape = MutableStateFlow("Rectangle")

    // Rectangle
    val rectLength = MutableStateFlow("")
    val rectWidth = MutableStateFlow("")

    // Square
    val squareSide = MutableStateFlow("")

    // Custom sections
    val customSections = MutableStateFlow<List<CeilingSectionEntry>>(listOf(CeilingSectionEntry("Section 1", "", "")))

    // Calculation Results
    private val _ceilingArea = MutableStateFlow(0.0)
    val ceilingArea: StateFlow<Double> = _ceilingArea.asStateFlow()

    // Unit Type preference (Metric/Imperial)
    val unitType = MutableStateFlow(UnitType.METRIC)

    // --- VALIDATION FLOWS ---
    val isValidationActive = MutableStateFlow(false)
    val firstInvalidField = MutableSharedFlow<String>(replay = 0)

    val rectLengthError = MutableStateFlow<String?>(null)
    val rectWidthError = MutableStateFlow<String?>(null)
    val squareSideError = MutableStateFlow<String?>(null)
    val customSectionsErrors = MutableStateFlow<List<CeilingSectionError>>(emptyList())

    init {
        viewModelScope.launch {
            appPreferences.unitTypeFlow.collect { unitTypeStr ->
                unitType.value = try {
                    UnitType.valueOf(unitTypeStr.uppercase())
                } catch (e: Exception) {
                    UnitType.METRIC
                }
            }
        }

        // Initialize custom sections errors matching size
        viewModelScope.launch {
            customSections.collect { sections ->
                if (customSectionsErrors.value.size != sections.size) {
                    customSectionsErrors.value = List(sections.size) { CeilingSectionError() }
                }
            }
        }

        // Setup real-time validators
        fun setupRealtimeValidation(
            fieldFlow: Flow<String>,
            errorFlow: MutableStateFlow<String?>,
            fieldName: String
        ) {
            viewModelScope.launch {
                fieldFlow.collect { value ->
                    if (isValidationActive.value || errorFlow.value != null) {
                        val res = Validator.validatePositiveNumber(value, fieldName)
                        errorFlow.value = if (res is ValidationResult.Invalid) res.message else null
                    }
                }
            }
        }

        setupRealtimeValidation(rectLength, rectLengthError, "ceiling length")
        setupRealtimeValidation(rectWidth, rectWidthError, "ceiling width")
        setupRealtimeValidation(squareSide, squareSideError, "side length")

        // Custom sections real-time validation
        viewModelScope.launch {
            customSections.collect { sections ->
                if (isValidationActive.value) {
                    val currentErrors = customSectionsErrors.value.toMutableList()
                    while (currentErrors.size < sections.size) currentErrors.add(CeilingSectionError())
                    while (currentErrors.size > sections.size) currentErrors.removeAt(currentErrors.lastIndex)

                    var changed = false
                    sections.forEachIndexed { index, section ->
                        val lRes = Validator.validatePositiveNumber(section.length, "length")
                        val wRes = Validator.validatePositiveNumber(section.width, "width")

                        val newWError = if (wRes is ValidationResult.Invalid) wRes.message else null
                        val newLError = if (lRes is ValidationResult.Invalid) lRes.message else null

                        if (currentErrors[index].widthError != newWError || currentErrors[index].lengthError != newLError) {
                            currentErrors[index] = CeilingSectionError(newLError, newWError)
                            changed = true
                        }
                    }
                    if (changed) {
                        customSectionsErrors.value = currentErrors
                    }
                }
            }
        }

        // Auto-run calculation on input changes
        val inputFlows = combine(
            ceilingShape, rectLength, rectWidth, squareSide, customSections
        ) { _, _, _, _, _ -> Unit }

        viewModelScope.launch {
            inputFlows.collect {
                calculate()
            }
        }
    }

    fun addCustomSection() {
        val current = customSections.value.toMutableList()
        val newIndex = current.size + 1
        current.add(CeilingSectionEntry("Section $newIndex", "", ""))
        customSections.value = current
    }

    fun removeCustomSection(index: Int) {
        val current = customSections.value.toMutableList()
        if (index in current.indices) {
            current.removeAt(index)
            customSections.value = current.mapIndexed { idx, entry ->
                entry.copy(name = "Section ${idx + 1}")
            }
        }
    }

    fun updateCustomSection(index: Int, length: String, width: String) {
        val current = customSections.value.toMutableList()
        if (index in current.indices) {
            current[index] = current[index].copy(length = length, width = width)
            customSections.value = current
        }
    }

    fun calculate() {
        val shape = ceilingShape.value
        val area = when (shape) {
            "Rectangle" -> {
                val l = rectLength.value.toDoubleOrNull() ?: 0.0
                val w = rectWidth.value.toDoubleOrNull() ?: 0.0
                CeilingCalculator.calculateRectangleArea(l, w)
            }
            "Square" -> {
                val s = squareSide.value.toDoubleOrNull() ?: 0.0
                CeilingCalculator.calculateSquareArea(s)
            }
            "Custom" -> {
                val sections = customSections.value.map {
                    (it.length.toDoubleOrNull() ?: 0.0) to (it.width.toDoubleOrNull() ?: 0.0)
                }
                CeilingCalculator.calculateCustomSectionsArea(sections)
            }
            else -> 0.0
        }
        _ceilingArea.value = area
    }

    fun validateCeilingInputs(): Boolean {
        isValidationActive.value = true
        var isValid = true
        var firstInvalid: String? = null

        fun check(fieldId: String, value: String, fieldName: String, errorFlow: MutableStateFlow<String?>): Boolean {
            val res = Validator.validatePositiveNumber(value, fieldName)
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

        when (ceilingShape.value) {
            "Rectangle" -> {
                check("rectLength", rectLength.value, "ceiling length", rectLengthError)
                check("rectWidth", rectWidth.value, "ceiling width", rectWidthError)
            }
            "Square" -> {
                check("squareSide", squareSide.value, "side length", squareSideError)
            }
            "Custom" -> {
                val sections = customSections.value
                val currentErrors = MutableList(sections.size) { CeilingSectionError() }
                sections.forEachIndexed { index, section ->
                    val lRes = Validator.validatePositiveNumber(section.length, "length")
                    val wRes = Validator.validatePositiveNumber(section.width, "width")

                    var lErr: String? = null
                    var wErr: String? = null

                    if (lRes is ValidationResult.Invalid) {
                        lErr = lRes.message
                        if (isValid) {
                            isValid = false
                            firstInvalid = "custom_length_$index"
                        }
                    }
                    if (wRes is ValidationResult.Invalid) {
                        wErr = wRes.message
                        if (isValid) {
                            isValid = false
                            firstInvalid = "custom_width_$index"
                        }
                    }
                    currentErrors[index] = CeilingSectionError(lErr, wErr)
                }
                customSectionsErrors.value = currentErrors
            }
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
            if (!validateCeilingInputs()) return@launch
            calculate()
            val format = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
            val dateStr = format.format(Date())

            val project = Project(
                id = 0,
                name = "Ceiling Calc - $dateStr",
                location = ceilingShape.value,
                notes = "Saved from Ceiling Calculator"
            )

            // Construct room entity details based on input
            val roomName = when (ceilingShape.value) {
                "Rectangle" -> "Rectangle Ceiling"
                "Square" -> "Square Ceiling"
                else -> "Custom Ceiling"
            }

            val room = when (ceilingShape.value) {
                "Rectangle" -> {
                    val l = rectLength.value.toDoubleOrNull() ?: 0.0
                    val w = rectWidth.value.toDoubleOrNull() ?: 0.0
                    Room(id = 0, projectId = 0, name = roomName, length = l, width = w, height = 0.0, wallCount = 0, includeCeiling = true, unitType = unitType.value)
                }
                "Square" -> {
                    val s = squareSide.value.toDoubleOrNull() ?: 0.0
                    Room(id = 0, projectId = 0, name = roomName, length = s, width = s, height = 0.0, wallCount = 0, includeCeiling = true, unitType = unitType.value)
                }
                else -> {
                    val totalA = _ceilingArea.value
                    Room(id = 0, projectId = 0, name = roomName, length = totalA, width = 1.0, height = 0.0, wallCount = 0, includeCeiling = true, unitType = unitType.value)
                }
            }

            val roomDetails = RoomDetails(
                room = room,
                openings = emptyList()
            )

            val paintSettings = PaintSettings(
                id = 0,
                projectId = 0,
                coverage = 10.0,
                coats = 1,
                wastage = 0.0,
                includePrimer = false,
                primerCoverage = 10.0,
                primerCoats = 0
            )

            val estimate = Estimate(
                id = 0,
                projectId = 0,
                totalSurfaceArea = _ceilingArea.value,
                paintableArea = _ceilingArea.value,
                paintRequired = 0.0,
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
