package com.example.paintcalculatorestimator.presentation.wall

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.paintcalculatorestimator.data.preferences.AppPreferences
import com.example.paintcalculatorestimator.domain.model.*
import com.example.paintcalculatorestimator.domain.usecase.ProjectUseCases
import com.example.paintcalculatorestimator.utils.SlabCalculator
import com.example.paintcalculatorestimator.utils.WallCalculator
import com.example.paintcalculatorestimator.utils.Validator
import com.example.paintcalculatorestimator.utils.ValidationResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.math.ceil

data class WallEntry(
    val name: String,
    val width: String,
    val height: String
)

data class WallEntryError(
    val widthError: String? = null,
    val heightError: String? = null
)

@HiltViewModel
class WallCalculatorViewModel @Inject constructor(
    private val useCases: ProjectUseCases,
    private val appPreferences: AppPreferences
) : ViewModel() {

    // Room/Wall configuration type: "Single Wall", "Multiple Walls", "Complete Room"
    val roomType = MutableStateFlow("Complete Room")

    // Single Wall Dimensions
    val singleWidth = MutableStateFlow("")
    val singleHeight = MutableStateFlow("")

    // Multiple Walls Dimensions
    val multipleWalls = MutableStateFlow<List<WallEntry>>(listOf(WallEntry("Wall 1", "", "")))

    // Complete Room Dimensions
    val roomLength = MutableStateFlow("")
    val roomWidth = MutableStateFlow("")
    val roomHeight = MutableStateFlow("")

    // Openings (Optional Doors/Windows)
    val openings = MutableStateFlow<List<Opening>>(emptyList())

    // --- CONSTRUCTION ESTIMATION VARIABLES ---

    // Wall settings
    val wallThickness = MutableStateFlow("") // cm in Metric, inches in Imperial
    val materialType = MutableStateFlow("Brick") // "Brick" or "Block"
    val pricePerBrick = MutableStateFlow("")
    val pricePerCement = MutableStateFlow("")
    val pricePerSand = MutableStateFlow("")
    val laborWallCost = MutableStateFlow("")

    // Slab settings
    val isSlabEnabled = MutableStateFlow(false)
    val slabLength = MutableStateFlow("")
    val slabWidth = MutableStateFlow("")
    val slabThickness = MutableStateFlow("") // cm in Metric, inches in Imperial
    val pricePerSteel = MutableStateFlow("")
    val pricePerAggregate = MutableStateFlow("")
    val laborSlabCost = MutableStateFlow("")

    // Washroom settings
    val isWashroomEnabled = MutableStateFlow(false)
    val washroomLength = MutableStateFlow("")
    val washroomWidth = MutableStateFlow("")
    val washroomHeight = MutableStateFlow("")
    val washroomThickness = MutableStateFlow("")
    val washroomMaterialType = MutableStateFlow("Brick")
    val pricePerTile = MutableStateFlow("")
    val plumbingCost = MutableStateFlow("")
    val laborWashroomCost = MutableStateFlow("")
    
    // Washroom openings
    val washroomOpenings = MutableStateFlow<List<Opening>>(emptyList())

    // --- OUTPUT RESULTS ---

    // Area outputs
    val grossArea = MutableStateFlow(0.0)
    val deductionArea = MutableStateFlow(0.0)
    val paintableArea = MutableStateFlow(0.0)

    // Room wall outputs
    val roomBricksResult = MutableStateFlow(0)
    val roomCementResult = MutableStateFlow(0.0)
    val roomSandResult = MutableStateFlow(0.0)
    val roomMaterialCost = MutableStateFlow(0.0)
    val roomLaborCost = MutableStateFlow(0.0)
    val roomTotalCost = MutableStateFlow(0.0)

    // Slab outputs
    val slabVolumeResult = MutableStateFlow(0.0)
    val slabCementResult = MutableStateFlow(0.0)
    val slabSandResult = MutableStateFlow(0.0)
    val slabAggregateResult = MutableStateFlow(0.0)
    val slabSteelResult = MutableStateFlow(0.0)
    val slabMaterialCost = MutableStateFlow(0.0)
    val slabLaborCost = MutableStateFlow(0.0)
    val slabTotalCost = MutableStateFlow(0.0)

    // Washroom outputs
    val washroomBricksResult = MutableStateFlow(0)
    val washroomCementResult = MutableStateFlow(0.0)
    val washroomSandResult = MutableStateFlow(0.0)
    val washroomFloorArea = MutableStateFlow(0.0)
    val washroomWallArea = MutableStateFlow(0.0)
    val washroomCeilingArea = MutableStateFlow(0.0)
    val washroomTilesResult = MutableStateFlow(0.0)
    val washroomTilesCost = MutableStateFlow(0.0)
    val washroomTileCementResult = MutableStateFlow(0.0)
    val washroomPlumbingCost = MutableStateFlow(0.0)
    val washroomLaborCost = MutableStateFlow(0.0)
    val washroomTotalCost = MutableStateFlow(0.0)

    // Grand total
    val grandTotal = MutableStateFlow(0.0)

    // Preferences
    val unitType = MutableStateFlow(UnitType.METRIC)
    val currency = MutableStateFlow("PKR")

    // --- VALIDATION FLOWS ---
    val isValidationActive = MutableStateFlow(false)
    val firstInvalidField = MutableSharedFlow<String>(replay = 0)

    val singleWidthError = MutableStateFlow<String?>(null)
    val singleHeightError = MutableStateFlow<String?>(null)
    val roomLengthError = MutableStateFlow<String?>(null)
    val roomWidthError = MutableStateFlow<String?>(null)
    val roomHeightError = MutableStateFlow<String?>(null)
    val wallThicknessError = MutableStateFlow<String?>(null)
    val multipleWallsErrors = MutableStateFlow<List<WallEntryError>>(emptyList())

    val pricePerBrickError = MutableStateFlow<String?>(null)
    val pricePerCementError = MutableStateFlow<String?>(null)
    val pricePerSandError = MutableStateFlow<String?>(null)
    val laborWallCostError = MutableStateFlow<String?>(null)

    val slabLengthError = MutableStateFlow<String?>(null)
    val slabWidthError = MutableStateFlow<String?>(null)
    val slabThicknessError = MutableStateFlow<String?>(null)
    val pricePerSteelError = MutableStateFlow<String?>(null)
    val pricePerAggregateError = MutableStateFlow<String?>(null)
    val laborSlabCostError = MutableStateFlow<String?>(null)

    val washroomLengthError = MutableStateFlow<String?>(null)
    val washroomWidthError = MutableStateFlow<String?>(null)
    val washroomHeightError = MutableStateFlow<String?>(null)
    val washroomThicknessError = MutableStateFlow<String?>(null)
    val pricePerTileError = MutableStateFlow<String?>(null)
    val plumbingCostError = MutableStateFlow<String?>(null)
    val laborWashroomCostError = MutableStateFlow<String?>(null)

    init {
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
                // Set default thickness based on unit type
                if (unitType.value == UnitType.METRIC) {
                    if (wallThickness.value.isEmpty()) wallThickness.value = "23" // 23 cm
                    if (slabThickness.value.isEmpty()) slabThickness.value = "12" // 12 cm
                    if (washroomThickness.value.isEmpty()) washroomThickness.value = "11.5" // 11.5 cm
                } else {
                    if (wallThickness.value.isEmpty()) wallThickness.value = "9" // 9 inches
                    if (slabThickness.value.isEmpty()) slabThickness.value = "5" // 5 inches
                    if (washroomThickness.value.isEmpty()) washroomThickness.value = "4.5" // 4.5 inches
                }
            }
        }

        // Initialize multiple walls errors matching size
        viewModelScope.launch {
            multipleWalls.collect { walls ->
                if (multipleWallsErrors.value.size != walls.size) {
                    multipleWallsErrors.value = List(walls.size) { WallEntryError() }
                }
            }
        }

        // Setup real-time validators
        fun setupRealtimeValidation(
            fieldFlow: Flow<String>,
            errorFlow: MutableStateFlow<String?>,
            fieldName: String,
            validator: (String, String) -> ValidationResult = { value, name -> Validator.validatePositiveNumber(value, name) }
        ) {
            viewModelScope.launch {
                fieldFlow.collect { value ->
                    if (isValidationActive.value || errorFlow.value != null) {
                        val res = validator(value, fieldName)
                        errorFlow.value = if (res is ValidationResult.Invalid) res.message else null
                    }
                }
            }
        }

        setupRealtimeValidation(singleWidth, singleWidthError, "wall width")
        setupRealtimeValidation(singleHeight, singleHeightError, "wall height")
        setupRealtimeValidation(roomLength, roomLengthError, "room length")
        setupRealtimeValidation(roomWidth, roomWidthError, "room width")
        setupRealtimeValidation(roomHeight, roomHeightError, "wall height")
        setupRealtimeValidation(wallThickness, wallThicknessError, "wall thickness")

        // Dynamic brick/block pricing name
        viewModelScope.launch {
            combine(pricePerBrick, materialType) { price, type -> price to type }.collect { (price, type) ->
                if (isValidationActive.value || pricePerBrickError.value != null) {
                    val label = if (type == "Brick") "price per brick" else "price per block"
                    val res = Validator.validatePositiveNumber(price, label)
                    pricePerBrickError.value = if (res is ValidationResult.Invalid) res.message else null
                }
            }
        }

        setupRealtimeValidation(pricePerCement, pricePerCementError, "price per cement bag")
        setupRealtimeValidation(pricePerSand, pricePerSandError, "price per sand unit")
        setupRealtimeValidation(laborWallCost, laborWallCostError, "walls labor cost", { value, name -> Validator.validateNonNegativeNumber(value, name) })

        setupRealtimeValidation(slabLength, slabLengthError, "slab length")
        setupRealtimeValidation(slabWidth, slabWidthError, "slab width")
        setupRealtimeValidation(slabThickness, slabThicknessError, "slab thickness")
        setupRealtimeValidation(pricePerSteel, pricePerSteelError, "price per steel/rebar kg")
        setupRealtimeValidation(pricePerAggregate, pricePerAggregateError, "price per aggregate/crush unit")
        setupRealtimeValidation(laborSlabCost, laborSlabCostError, "slab labor cost", { value, name -> Validator.validateNonNegativeNumber(value, name) })

        setupRealtimeValidation(washroomLength, washroomLengthError, "washroom length")
        setupRealtimeValidation(washroomWidth, washroomWidthError, "washroom width")
        setupRealtimeValidation(washroomHeight, washroomHeightError, "washroom height")
        setupRealtimeValidation(washroomThickness, washroomThicknessError, "washroom wall thickness")
        setupRealtimeValidation(pricePerTile, pricePerTileError, "price per tile area unit")
        setupRealtimeValidation(plumbingCost, plumbingCostError, "plumbing/fixtures total cost", { value, name -> Validator.validateNonNegativeNumber(value, name) })
        setupRealtimeValidation(laborWashroomCost, laborWashroomCostError, "washroom labor cost", { value, name -> Validator.validateNonNegativeNumber(value, name) })

        // Realtime validation for multiple walls list
        viewModelScope.launch {
            multipleWalls.collect { walls ->
                if (isValidationActive.value) {
                    val currentErrors = multipleWallsErrors.value.toMutableList()
                    while (currentErrors.size < walls.size) currentErrors.add(WallEntryError())
                    while (currentErrors.size > walls.size) currentErrors.removeAt(currentErrors.lastIndex)

                    var changed = false
                    walls.forEachIndexed { index, wall ->
                        val wRes = Validator.validatePositiveNumber(wall.width, "wall width")
                        val hRes = Validator.validatePositiveNumber(wall.height, "wall height")

                        val newWError = if (wRes is ValidationResult.Invalid) wRes.message else null
                        val newHError = if (hRes is ValidationResult.Invalid) hRes.message else null

                        if (currentErrors[index].widthError != newWError || currentErrors[index].heightError != newHError) {
                            currentErrors[index] = WallEntryError(newWError, newHError)
                            changed = true
                        }
                    }
                    if (changed) {
                        multipleWallsErrors.value = currentErrors
                    }
                }
            }
        }

        // Setup reactive recalculation on any input change
        viewModelScope.launch {
            merge(
                roomType.map { }, singleWidth.map { }, singleHeight.map { }, multipleWalls.map { },
                roomLength.map { }, roomWidth.map { }, roomHeight.map { }, openings.map { },
                wallThickness.map { }, materialType.map { }, pricePerBrick.map { }, pricePerCement.map { },
                pricePerSand.map { }, laborWallCost.map { }, isSlabEnabled.map { }, slabLength.map { },
                slabWidth.map { }, slabThickness.map { }, pricePerSteel.map { }, pricePerAggregate.map { },
                laborSlabCost.map { }, isWashroomEnabled.map { }, washroomLength.map { }, washroomWidth.map { },
                washroomHeight.map { }, washroomThickness.map { }, washroomMaterialType.map { },
                pricePerTile.map { }, plumbingCost.map { }, laborWashroomCost.map { }, washroomOpenings.map { }
            ).collect {
                calculateAll()
            }
        }
    }

    fun validateWallInputs(): Boolean {
        isValidationActive.value = true
        var isValid = true
        var firstInvalid: String? = null

        fun check(fieldId: String, value: String, fieldName: String, errorFlow: MutableStateFlow<String?>, isNonNegative: Boolean = false): Boolean {
            val res = if (isNonNegative) {
                Validator.validateNonNegativeNumber(value, fieldName)
            } else {
                Validator.validatePositiveNumber(value, fieldName)
            }
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

        // 1. Room type specific dimensions
        when (roomType.value) {
            "Single Wall" -> {
                check("singleWidth", singleWidth.value, "wall width", singleWidthError)
                check("singleHeight", singleHeight.value, "wall height", singleHeightError)
            }
            "Multiple Walls" -> {
                val walls = multipleWalls.value
                val currentErrors = MutableList(walls.size) { WallEntryError() }
                walls.forEachIndexed { index, wall ->
                    val wRes = Validator.validatePositiveNumber(wall.width, "wall width")
                    val hRes = Validator.validatePositiveNumber(wall.height, "wall height")

                    var wErr: String? = null
                    var hErr: String? = null

                    if (wRes is ValidationResult.Invalid) {
                        wErr = wRes.message
                        if (isValid) {
                            isValid = false
                            firstInvalid = "wall_width_$index"
                        }
                    }
                    if (hRes is ValidationResult.Invalid) {
                        hErr = hRes.message
                        if (isValid) {
                            isValid = false
                            firstInvalid = "wall_height_$index"
                        }
                    }
                    currentErrors[index] = WallEntryError(wErr, hErr)
                }
                multipleWallsErrors.value = currentErrors
            }
            "Complete Room" -> {
                check("roomLength", roomLength.value, "room length", roomLengthError)
                check("roomWidth", roomWidth.value, "room width", roomWidthError)
                check("roomHeight", roomHeight.value, "wall height", roomHeightError)
            }
        }

        // 2. Wall thickness
        check("wallThickness", wallThickness.value, "wall thickness", wallThicknessError)

        // 3. Pricing fields
        check("pricePerBrick", pricePerBrick.value, if (materialType.value == "Brick") "price per brick" else "price per block", pricePerBrickError)
        check("pricePerCement", pricePerCement.value, "price per cement bag", pricePerCementError)
        check("pricePerSand", pricePerSand.value, "price per sand unit", pricePerSandError)
        check("laborWallCost", laborWallCost.value, "walls labor cost", laborWallCostError, isNonNegative = true)

        // 4. Slab (Roof) fields if enabled
        if (isSlabEnabled.value) {
            check("slabLength", slabLength.value, "slab length", slabLengthError)
            check("slabWidth", slabWidth.value, "slab width", slabWidthError)
            check("slabThickness", slabThickness.value, "slab thickness", slabThicknessError)
            check("pricePerSteel", pricePerSteel.value, "price per steel/rebar kg", pricePerSteelError)
            check("pricePerAggregate", pricePerAggregate.value, "price per aggregate/crush unit", pricePerAggregateError)
            check("laborSlabCost", laborSlabCost.value, "slab labor cost", laborSlabCostError, isNonNegative = true)
        }

        // 5. Washroom fields if enabled
        if (isWashroomEnabled.value) {
            check("washroomLength", washroomLength.value, "washroom length", washroomLengthError)
            check("washroomWidth", washroomWidth.value, "washroom width", washroomWidthError)
            check("washroomHeight", washroomHeight.value, "washroom height", washroomHeightError)
            check("washroomThickness", washroomThickness.value, "washroom wall thickness", washroomThicknessError)
            check("pricePerTile", pricePerTile.value, "price per tile area unit", pricePerTileError)
            check("plumbingCost", plumbingCost.value, "plumbing/fixtures total cost", plumbingCostError, isNonNegative = true)
            check("laborWashroomCost", laborWashroomCost.value, "washroom labor cost", laborWashroomCostError, isNonNegative = true)
        }

        if (!isValid && firstInvalid != null) {
            viewModelScope.launch {
                firstInvalidField.emit(firstInvalid!!)
            }
        }

        return isValid
    }

    fun addMultipleWall() {
        val current = multipleWalls.value.toMutableList()
        val newIndex = current.size + 1
        current.add(WallEntry("Wall $newIndex", "", ""))
        multipleWalls.value = current
    }

    fun removeMultipleWall(index: Int) {
        val current = multipleWalls.value.toMutableList()
        if (index in current.indices) {
            current.removeAt(index)
            multipleWalls.value = current.mapIndexed { idx, entry ->
                entry.copy(name = "Wall ${idx + 1}")
            }
        }
    }

    fun updateMultipleWall(index: Int, width: String, height: String) {
        val current = multipleWalls.value.toMutableList()
        if (index in current.indices) {
            current[index] = current[index].copy(width = width, height = height)
            multipleWalls.value = current
        }
    }

    fun addOpening(type: OpeningType, width: Double, height: Double, quantity: Int) {
        openings.value = openings.value + Opening(
            id = 0, roomId = 0, type = type, width = width, height = height, quantity = quantity
        )
    }

    fun removeOpening(index: Int) {
        val current = openings.value.toMutableList()
        if (index in current.indices) {
            current.removeAt(index)
            openings.value = current
        }
    }

    fun addWashroomOpening(type: OpeningType, width: Double, height: Double, quantity: Int) {
        washroomOpenings.value = washroomOpenings.value + Opening(
            id = 0, roomId = 0, type = type, width = width, height = height, quantity = quantity
        )
    }

    fun removeWashroomOpening(index: Int) {
        val current = washroomOpenings.value.toMutableList()
        if (index in current.indices) {
            current.removeAt(index)
            washroomOpenings.value = current
        }
    }

    fun calculateAll() {
        val isImperial = unitType.value == UnitType.IMPERIAL

        // 1. Room Wall Areas
        val gross = when (roomType.value) {
            "Single Wall" -> {
                val w = singleWidth.value.toDoubleOrNull() ?: 0.0
                val h = singleHeight.value.toDoubleOrNull() ?: 0.0
                WallCalculator.calculateSingleWallArea(w, h)
            }
            "Multiple Walls" -> {
                val walls = multipleWalls.value.map {
                    (it.width.toDoubleOrNull() ?: 0.0) to (it.height.toDoubleOrNull() ?: 0.0)
                }
                WallCalculator.calculateMultipleWallArea(walls)
            }
            else -> { // Complete Room
                val l = roomLength.value.toDoubleOrNull() ?: 0.0
                val w = roomWidth.value.toDoubleOrNull() ?: 0.0
                val h = roomHeight.value.toDoubleOrNull() ?: 0.0
                WallCalculator.calculateRoomWallArea(l, w, h)
            }
        }
        val deductions = openings.value.sumOf {
            WallCalculator.calculateOpeningArea(it.width, it.height, it.quantity)
        }
        val net = WallCalculator.calculatePaintableWallArea(gross, deductions)

        grossArea.value = gross
        deductionArea.value = deductions
        paintableArea.value = net

        // 2. Room Wall Masonry Estimates
        val thicknessVal = wallThickness.value.toDoubleOrNull() ?: 0.0
        val volM3 = WallCalculator.calculateWallVolume(net, thicknessVal, isImperial)

        val prBrick = pricePerBrick.value.toDoubleOrNull() ?: 0.0
        val prCement = pricePerCement.value.toDoubleOrNull() ?: 0.0
        val prSand = pricePerSand.value.toDoubleOrNull() ?: 0.0
        val wallLabor = laborWallCost.value.toDoubleOrNull() ?: 0.0

        val bricksCount: Int
        val cementBags: Double
        val sandVol: Double

        if (materialType.value == "Brick") {
            bricksCount = WallCalculator.calculateBricksCount(volM3)
            cementBags = WallCalculator.calculateCementBagsForBricks(volM3, bricksCount)
            sandVol = WallCalculator.calculateSandVolumeForBricks(volM3, bricksCount)
        } else {
            bricksCount = WallCalculator.calculateBlocksCount(volM3)
            cementBags = WallCalculator.calculateCementBagsForBlocks(volM3, bricksCount)
            sandVol = WallCalculator.calculateSandVolumeForBlocks(volM3, bricksCount)
        }

        val bricksCost = bricksCount * prBrick
        val cementCost = ceil(cementBags) * prCement
        
        // Sand pricing per cubic meter/cft depending on units
        val sandCost = sandVol * prSand
        val matCost = bricksCost + cementCost + sandCost

        roomBricksResult.value = bricksCount
        roomCementResult.value = cementBags
        roomSandResult.value = sandVol
        roomMaterialCost.value = matCost
        roomLaborCost.value = wallLabor
        roomTotalCost.value = matCost + wallLabor

        // 3. Roof / Slab (Chhat) Calculations
        if (isSlabEnabled.value) {
            val sLength = slabLength.value.toDoubleOrNull() ?: 0.0
            val sWidth = slabWidth.value.toDoubleOrNull() ?: 0.0
            val sThickness = slabThickness.value.toDoubleOrNull() ?: 0.0
            val sVol = SlabCalculator.calculateSlabVolume(sLength, sWidth, sThickness, isImperial)

            val sCement = SlabCalculator.calculateCementBags(sVol)
            val sSand = SlabCalculator.calculateSandVolume(sVol)
            val sAgg = SlabCalculator.calculateAggregateVolume(sVol)
            val sSteel = SlabCalculator.calculateSteelWeight(sVol)

            val prSteel = pricePerSteel.value.toDoubleOrNull() ?: 0.0
            val prAgg = pricePerAggregate.value.toDoubleOrNull() ?: 0.0
            val slabLabor = laborSlabCost.value.toDoubleOrNull() ?: 0.0

            val slabMatCost = (ceil(sCement) * prCement) + (sSand * prSand) + (sAgg * prAgg) + (sSteel * prSteel)

            slabVolumeResult.value = sVol
            slabCementResult.value = sCement
            slabSandResult.value = sSand
            slabAggregateResult.value = sAgg
            slabSteelResult.value = sSteel
            slabMaterialCost.value = slabMatCost
            slabLaborCost.value = slabLabor
            slabTotalCost.value = slabMatCost + slabLabor
        } else {
            slabVolumeResult.value = 0.0
            slabCementResult.value = 0.0
            slabSandResult.value = 0.0
            slabAggregateResult.value = 0.0
            slabSteelResult.value = 0.0
            slabMaterialCost.value = 0.0
            slabLaborCost.value = 0.0
            slabTotalCost.value = 0.0
        }

        // 4. Washroom inside Room Calculations
        if (isWashroomEnabled.value) {
            val wLength = washroomLength.value.toDoubleOrNull() ?: 0.0
            val wWidth = washroomWidth.value.toDoubleOrNull() ?: 0.0
            val wHeight = washroomHeight.value.toDoubleOrNull() ?: 0.0
            val wThickness = washroomThickness.value.toDoubleOrNull() ?: 0.0
            val tilePriceVal = pricePerTile.value.toDoubleOrNull() ?: 0.0
            val plumbVal = plumbingCost.value.toDoubleOrNull() ?: 0.0
            val washLabor = laborWashroomCost.value.toDoubleOrNull() ?: 0.0

            // Area calculation for washroom
            val wGross = WallCalculator.calculateRoomWallArea(wLength, wWidth, wHeight)
            val wDeductions = washroomOpenings.value.sumOf {
                WallCalculator.calculateOpeningArea(it.width, it.height, it.quantity)
            }
            val wNet = WallCalculator.calculatePaintableWallArea(wGross, wDeductions)
            val wVol = WallCalculator.calculateWallVolume(wNet, wThickness, isImperial)

            val wBricksCount: Int
            val wCementBags: Double
            val wSandVol: Double

            if (washroomMaterialType.value == "Brick") {
                wBricksCount = WallCalculator.calculateBricksCount(wVol)
                wCementBags = WallCalculator.calculateCementBagsForBricks(wVol, wBricksCount)
                wSandVol = WallCalculator.calculateSandVolumeForBricks(wVol, wBricksCount)
            } else {
                wBricksCount = WallCalculator.calculateBlocksCount(wVol)
                wCementBags = WallCalculator.calculateCementBagsForBlocks(wVol, wBricksCount)
                wSandVol = WallCalculator.calculateSandVolumeForBlocks(wVol, wBricksCount)
            }

            // Tiling
            val floorA = wLength * wWidth
            val wallA = wNet
            val ceilingA = wLength * wWidth
            val totalTileA = floorA + wallA // Tiles on floor and walls
            val tilesRequired = totalTileA * 1.10 // 10% wastage
            val tileCostVal = totalTileA * tilePriceVal
            val tileCement = totalTileA * 0.2
            val tileSand = totalTileA * 0.015

            // Sum washroom materials cost
            val wWallsMatCost = (wBricksCount * prBrick) + (ceil(wCementBags + tileCement) * prCement) + ((wSandVol + tileSand) * prSand)
            val wTotalMatCost = wWallsMatCost + tileCostVal + plumbVal

            washroomBricksResult.value = wBricksCount
            washroomCementResult.value = wCementBags
            washroomSandResult.value = wSandVol
            washroomFloorArea.value = floorA
            washroomWallArea.value = wallA
            washroomCeilingArea.value = ceilingA
            washroomTilesResult.value = tilesRequired
            washroomTilesCost.value = tileCostVal
            washroomTileCementResult.value = tileCement
            washroomPlumbingCost.value = plumbVal
            washroomLaborCost.value = washLabor
            washroomTotalCost.value = wTotalMatCost + washLabor
        } else {
            washroomBricksResult.value = 0
            washroomCementResult.value = 0.0
            washroomSandResult.value = 0.0
            washroomFloorArea.value = 0.0
            washroomWallArea.value = 0.0
            washroomCeilingArea.value = 0.0
            washroomTilesResult.value = 0.0
            washroomTilesCost.value = 0.0
            washroomTileCementResult.value = 0.0
            washroomPlumbingCost.value = 0.0
            washroomLaborCost.value = 0.0
            washroomTotalCost.value = 0.0
        }

        // 5. Grand Total Cost Estimation
        grandTotal.value = roomTotalCost.value + slabTotalCost.value + washroomTotalCost.value
    }

    fun saveResult(onSuccess: (Long) -> Unit) {
        viewModelScope.launch {
            if (!validateWallInputs()) return@launch
            calculateAll()
            val format = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
            val dateStr = format.format(Date())

            val project = Project(
                id = 0,
                name = "Room Estimate - $dateStr",
                location = "Construction Calculation",
                notes = "Bricks: ${roomBricksResult.value}, Cement: ${roomCementResult.value} bags"
            )

            // Setup Room structure for DB compat
            val roomName = if (roomType.value == "Complete Room") "Complete Room" else "Walls Estimation"
            val rLengthVal = roomLength.value.toDoubleOrNull() ?: 0.0
            val rWidthVal = roomWidth.value.toDoubleOrNull() ?: 0.0
            val rHeightVal = roomHeight.value.toDoubleOrNull() ?: 0.0

            val room = Room(
                id = 0,
                projectId = 0,
                name = roomName,
                length = rLengthVal,
                width = rWidthVal,
                height = rHeightVal,
                wallCount = if (roomType.value == "Complete Room") 4 else 1,
                includeCeiling = isSlabEnabled.value,
                unitType = unitType.value
            )

            val roomDetails = RoomDetails(
                room = room,
                openings = openings.value
            )

            // Map all calculated materials into Material list to save in database!
            val savedMaterials = mutableListOf<Material>()
            val materialsMap = mutableListOf<Pair<String, Double>>()

            // Bricks/Blocks
            val matName = if (materialType.value == "Brick") "Room Bricks" else "Room Blocks"
            val matPrice = pricePerBrick.value.toDoubleOrNull() ?: 0.0
            savedMaterials.add(Material(0, 0, matName, roomBricksResult.value.toDouble(), matPrice))

            // Cement & Sand
            val cementPrice = pricePerCement.value.toDoubleOrNull() ?: 0.0
            val sandPrice = pricePerSand.value.toDoubleOrNull() ?: 0.0
            
            var totalCementBags = roomCementResult.value
            var totalSandVol = roomSandResult.value

            if (isSlabEnabled.value) {
                totalCementBags += slabCementResult.value
                totalSandVol += slabSandResult.value
                
                // Slab aggregate & steel
                val aggPrice = pricePerAggregate.value.toDoubleOrNull() ?: 0.0
                val steelPrice = pricePerSteel.value.toDoubleOrNull() ?: 0.0
                savedMaterials.add(Material(0, 0, "Slab Aggregate", slabAggregateResult.value, aggPrice))
                savedMaterials.add(Material(0, 0, "Slab Steel Rebar", slabSteelResult.value, steelPrice))
            }

            if (isWashroomEnabled.value) {
                totalCementBags += washroomCementResult.value + washroomTileCementResult.value
                totalSandVol += washroomSandResult.value
                
                val washMatName = if (washroomMaterialType.value == "Brick") "Washroom Bricks" else "Washroom Blocks"
                savedMaterials.add(Material(0, 0, washMatName, washroomBricksResult.value.toDouble(), matPrice))
                
                // Tiles and plumbing
                val tPrice = pricePerTile.value.toDoubleOrNull() ?: 0.0
                savedMaterials.add(Material(0, 0, "Washroom Tiles", washroomTilesResult.value, tPrice))
                
                val plumbingVal = plumbingCost.value.toDoubleOrNull() ?: 0.0
                if (plumbingVal > 0) {
                    savedMaterials.add(Material(0, 0, "Plumbing & Fixtures", 1.0, plumbingVal))
                }
            }

            savedMaterials.add(Material(0, 0, "Construction Cement Bags", ceil(totalCementBags), cementPrice))
            savedMaterials.add(Material(0, 0, "Construction Sand", totalSandVol, sandPrice))

            val totalLabor = roomLaborCost.value + slabLaborCost.value + washroomLaborCost.value

            val estimate = Estimate(
                id = 0,
                projectId = 0,
                totalSurfaceArea = grossArea.value,
                paintableArea = paintableArea.value,
                paintRequired = 0.0,
                primerRequired = 0.0,
                paintCost = 0.0,
                primerCost = 0.0,
                materialsCost = savedMaterials.sumOf { it.quantity * it.unitPrice },
                laborCost = totalLabor,
                totalCost = grandTotal.value
            )

            val details = ProjectDetails(
                project = project,
                rooms = listOf(roomDetails),
                paintSettings = PaintSettings(0, 0, 10.0, 1, 0.0, false, 10.0, 0),
                materials = savedMaterials,
                estimate = estimate
            )

            val savedId = useCases.saveProject(details)
            onSuccess(savedId)
        }
    }
}
