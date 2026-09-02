package com.example.paintcalculatorestimator.presentation.cost

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.paintcalculatorestimator.data.preferences.AppPreferences
import com.example.paintcalculatorestimator.domain.model.*
import com.example.paintcalculatorestimator.domain.usecase.ProjectUseCases
import com.example.paintcalculatorestimator.pdf.PdfGenerator
import com.example.paintcalculatorestimator.utils.CostCalculator
import com.example.paintcalculatorestimator.utils.Validator
import com.example.paintcalculatorestimator.utils.ValidationResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class CostCalculatorViewModel @Inject constructor(
    private val useCases: ProjectUseCases,
    private val appPreferences: AppPreferences,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // Paint Cost Inputs
    val paintQuantity = MutableStateFlow("")
    val paintPrice = MutableStateFlow("")

    // Primer Cost Inputs (Optional)
    val includePrimer = MutableStateFlow(false)
    val primerQuantity = MutableStateFlow("")
    val primerPrice = MutableStateFlow("")

    // Labor Cost Inputs
    val isLaborManual = MutableStateFlow(false)
    val paintableArea = MutableStateFlow("")
    val laborRate = MutableStateFlow("")
    val laborManualCost = MutableStateFlow("")

    // Additional Materials list
    val materials = MutableStateFlow<List<Material>>(emptyList())

    // Outputs
    private val _paintCost = MutableStateFlow(0.0)
    val paintCost: StateFlow<Double> = _paintCost.asStateFlow()

    private val _primerCost = MutableStateFlow(0.0)
    val primerCost: StateFlow<Double> = _primerCost.asStateFlow()

    private val _laborCost = MutableStateFlow(0.0)
    val laborCost: StateFlow<Double> = _laborCost.asStateFlow()

    private val _materialsCost = MutableStateFlow(0.0)
    val materialsCost: StateFlow<Double> = _materialsCost.asStateFlow()

    private val _totalCost = MutableStateFlow(0.0)
    val totalCost: StateFlow<Double> = _totalCost.asStateFlow()

    // Currency and Units preference
    val currency = MutableStateFlow("PKR")
    val unitType = MutableStateFlow(UnitType.METRIC)

    // --- VALIDATION STATES ---
    val isValidationActive = MutableStateFlow(false)
    val firstInvalidField = MutableSharedFlow<String>(replay = 0)

    val paintQuantityError = MutableStateFlow<String?>(null)
    val paintPriceError = MutableStateFlow<String?>(null)

    val primerQuantityError = MutableStateFlow<String?>(null)
    val primerPriceError = MutableStateFlow<String?>(null)

    val paintableAreaError = MutableStateFlow<String?>(null)
    val laborRateError = MutableStateFlow<String?>(null)
    val laborManualCostError = MutableStateFlow<String?>(null)

    init {
        // Retrieve quantity arg from navigation (e.g. from Paint Calculator)
        val initialQuantityStr: String? = savedStateHandle.get<String>("paintQuantity")
        val initialQuantity = initialQuantityStr?.toDoubleOrNull() ?: 0.0
        if (initialQuantity > 0.0) {
            paintQuantity.value = String.format(Locale.US, "%.2f", initialQuantity)
        }

        viewModelScope.launch {
            appPreferences.currencyFlow.collect {
                currency.value = it
            }
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

        setupRealtimeValidation(paintQuantity, paintQuantityError) { Validator.validatePositiveNumber(it, "paint quantity", "Please enter paint quantity") }
        setupRealtimeValidation(paintPrice, paintPriceError) { Validator.validatePositiveNumber(it, "paint price", "Please enter paint price") }
        setupRealtimeValidation(primerQuantity, primerQuantityError) { Validator.validatePositiveNumber(it, "primer quantity", "Please enter primer quantity") }
        setupRealtimeValidation(primerPrice, primerPriceError) { Validator.validatePositiveNumber(it, "primer price", "Please enter primer price") }
        setupRealtimeValidation(paintableArea, paintableAreaError) { Validator.validatePositiveNumber(it, "paintable area", "Please enter paintable area") }
        setupRealtimeValidation(laborRate, laborRateError) { Validator.validatePositiveNumber(it, "labor rate per area", "Please enter labor rate per area") }
        setupRealtimeValidation(laborManualCost, laborManualCostError) { Validator.validatePositiveNumber(it, "labor cost", "Please enter labor cost") }

        // Auto calculate on input changes
        viewModelScope.launch {
            merge(
                paintQuantity.map { },
                paintPrice.map { },
                includePrimer.map { },
                primerQuantity.map { },
                primerPrice.map { },
                isLaborManual.map { },
                paintableArea.map { },
                laborRate.map { },
                laborManualCost.map { },
                materials.map { }
            ).collect {
                calculate()
            }
        }
    }

    fun addMaterial(name: String, quantity: Double, unitPrice: Double) {
        materials.value = materials.value + Material(
            id = 0,
            projectId = 0,
            name = name,
            quantity = quantity,
            unitPrice = unitPrice
        )
    }

    fun removeMaterial(index: Int) {
        val current = materials.value.toMutableList()
        if (index in current.indices) {
            current.removeAt(index)
            materials.value = current
        }
    }

    fun calculate() {
        val qty = paintQuantity.value.toDoubleOrNull() ?: 0.0
        val price = paintPrice.value.toDoubleOrNull() ?: 0.0
        val pCost = CostCalculator.calculatePaintCost(qty, price)

        val prCost = if (includePrimer.value) {
            val pQty = primerQuantity.value.toDoubleOrNull() ?: 0.0
            val pPrice = primerPrice.value.toDoubleOrNull() ?: 0.0
            CostCalculator.calculatePrimerCost(pQty, pPrice)
        } else {
            0.0
        }

        val area = paintableArea.value.toDoubleOrNull() ?: 0.0
        val rate = laborRate.value.toDoubleOrNull() ?: 0.0
        val manual = laborManualCost.value.toDoubleOrNull() ?: 0.0
        val lCost = CostCalculator.calculateLaborCost(area, rate, isLaborManual.value, manual)

        val mCost = CostCalculator.calculateMaterialCost(materials.value)

        val total = CostCalculator.calculateTotalCost(pCost, prCost, lCost, mCost)

        _paintCost.value = pCost
        _primerCost.value = prCost
        _laborCost.value = lCost
        _materialsCost.value = mCost
        _totalCost.value = total
    }

    fun getProjectDetails(): ProjectDetails {
        val format = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        val dateStr = format.format(Date())

        val project = Project(
            id = 0,
            name = "Cost Estimate - $dateStr",
            location = "Standalone Estimate",
            notes = "Saved from Cost Calculator"
        )

        val areaVal = paintableArea.value.toDoubleOrNull() ?: 0.0
        val room = Room(
            id = 0,
            projectId = 0,
            name = "Calculated Area",
            length = areaVal,
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

        val paintSettings = PaintSettings(
            id = 0,
            projectId = 0,
            coverage = 10.0,
            coats = 1,
            wastage = 0.0,
            includePrimer = includePrimer.value,
            primerCoverage = 10.0,
            primerCoats = 1
        )

        val estimate = Estimate(
            id = 0,
            projectId = 0,
            totalSurfaceArea = areaVal,
            paintableArea = areaVal,
            paintRequired = paintQuantity.value.toDoubleOrNull() ?: 0.0,
            primerRequired = primerQuantity.value.toDoubleOrNull() ?: 0.0,
            paintCost = _paintCost.value,
            primerCost = _primerCost.value,
            materialsCost = _materialsCost.value,
            laborCost = _laborCost.value,
            totalCost = _totalCost.value,
            paintPricePerUnit = paintPrice.value.toDoubleOrNull() ?: 0.0,
            laborRatePerUnit = laborRate.value.toDoubleOrNull() ?: 0.0,
            laborManual = isLaborManual.value,
            laborManualCost = laborManualCost.value.toDoubleOrNull() ?: 0.0
        )

        return ProjectDetails(
            project = project,
            rooms = listOf(roomDetails),
            paintSettings = paintSettings,
            materials = materials.value,
            estimate = estimate
        )
    }

    fun validateCostInputs(): Boolean {
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

        check("paintQuantity", paintQuantity.value, paintQuantityError) { Validator.validatePositiveNumber(it, "paint quantity", "Please enter paint quantity") }
        check("paintPrice", paintPrice.value, paintPriceError) { Validator.validatePositiveNumber(it, "paint price", "Please enter paint price") }

        if (includePrimer.value) {
            check("primerQuantity", primerQuantity.value, primerQuantityError) { Validator.validatePositiveNumber(it, "primer quantity", "Please enter primer quantity") }
            check("primerPrice", primerPrice.value, primerPriceError) { Validator.validatePositiveNumber(it, "primer price", "Please enter primer price") }
        }

        if (isLaborManual.value) {
            check("laborManualCost", laborManualCost.value, laborManualCostError) { Validator.validatePositiveNumber(it, "labor cost", "Please enter labor cost") }
        } else {
            check("paintableArea", paintableArea.value, paintableAreaError) { Validator.validatePositiveNumber(it, "paintable area", "Please enter paintable area") }
            check("laborRate", laborRate.value, laborRateError) { Validator.validatePositiveNumber(it, "labor rate per area", "Please enter labor rate per area") }
        }

        if (!isValid && firstInvalid != null) {
            viewModelScope.launch {
                firstInvalidField.emit(firstInvalid!!)
            }
        }

        return isValid
    }

    fun saveEstimate(onSuccess: (Long) -> Unit) {
        viewModelScope.launch {
            if (!validateCostInputs()) return@launch
            calculate()
            val details = getProjectDetails()
            val savedId = useCases.saveProject(details)
            onSuccess(savedId)
        }
    }

    fun generatePdfReport(context: Context): File {
        val details = getProjectDetails()
        return PdfGenerator.generatePdf(context, details, currency.value)
    }
}
