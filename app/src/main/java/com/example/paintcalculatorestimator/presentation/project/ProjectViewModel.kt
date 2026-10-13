package com.example.paintcalculatorestimator.presentation.project

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.paintcalculatorestimator.domain.model.*
import com.example.paintcalculatorestimator.domain.usecase.ProjectUseCases
import com.example.paintcalculatorestimator.utils.HouseEstimator
import com.example.paintcalculatorestimator.utils.HouseRoomInput
import com.example.paintcalculatorestimator.utils.WallCalculator
import com.example.paintcalculatorestimator.utils.SlabCalculator
import com.example.paintcalculatorestimator.utils.Validator
import com.example.paintcalculatorestimator.utils.ValidationResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.math.ceil
import kotlin.math.max

data class IndividualRoomEstimate(
    val roomName: String,
    val roomType: String,
    val floorAreaSqFt: Double,
    val wallVolumeM3: Double,
    val bricksCount: Int,
    val blocksCount: Int = 0,
    val cementBags: Double,
    val sandVol: Double,
    val hasRoof: Boolean,
    val slabCementBags: Double,
    val slabSandVol: Double,
    val slabAggVol: Double,
    val slabSteelKg: Double,
    val hasWashroom: Boolean,
    val washroomFloorAreaSqFt: Double,
    val washroomBricksCount: Int,
    val washroomBlocksCount: Int = 0,
    val washroomCementBags: Double,
    val washroomSandVol: Double,
    val washroomTilesSqFt: Double,
    val washroomTilesCost: Double,
    val totalCost: Double,
    val quantityMultiplier: Int
)

@HiltViewModel
class ProjectViewModel @Inject constructor(
    private val useCases: ProjectUseCases
) : ViewModel() {

    private val _currentStep = MutableStateFlow(1)
    val currentStep: StateFlow<Int> = _currentStep.asStateFlow()

    private var editingProjectId: Long = 0

    // Step 1: Project Type Selection
    val projectType = MutableStateFlow("House")

    // Step 2: House Info
    val projectName = MutableStateFlow("")
    val projectLocation = MutableStateFlow("")
    val projectNotes = MutableStateFlow("")
    val unitType = MutableStateFlow(UnitType.METRIC)
    val laborRatePerSqFt = MutableStateFlow("")
    val slabThicknessGlobal = MutableStateFlow("") // cm in Metric, inches in Imperial

    // Step 3: Room Builder State
    val houseRooms = MutableStateFlow<List<HouseRoomInput>>(emptyList())

    // Step 4: Boundary Wall State
    val isBoundaryWallEnabled = MutableStateFlow(false)
    val boundaryWallLength = MutableStateFlow("")
    val boundaryWallHeight = MutableStateFlow("")
    val boundaryWallThickness = MutableStateFlow("")
    val gateWidth = MutableStateFlow("")
    val gateHeight = MutableStateFlow("")
    val boundaryMaterialType = MutableStateFlow("Brick")

    // Step 5: Material Pricing & Sizes (Trip-Based)
    val brickTripPrice = MutableStateFlow("") // now treated as Price per Piece
    val brickTripQty = MutableStateFlow("1000") // pcs per trip
    val brickLength = MutableStateFlow("") // 9 in or 22.86 cm
    val brickWidth = MutableStateFlow("")  // 4.5 in or 11.43 cm
    val brickHeight = MutableStateFlow("") // 3 in or 7.62 cm
    
    val blockTripPrice = MutableStateFlow("") // now treated as Price per Block
    val blockTripQty = MutableStateFlow("300") // pcs per trip
    val blockLength = MutableStateFlow("") // 16 in or 40.64 cm
    val blockWidth = MutableStateFlow("")  // 8 in or 20.32 cm
    val blockHeight = MutableStateFlow("") // 6 in or 15.24 cm

    val cementTripPrice = MutableStateFlow("") // now treated as Price per Bag
    val cementTripQty = MutableStateFlow("100") // bags per trip

    val sandTripPrice = MutableStateFlow("") // now treated as Price per unit (cft or m3)
    val sandTripQty = MutableStateFlow("") 

    val aggregateTripPrice = MutableStateFlow("") // now treated as Price per unit (cft or m3)
    val aggregateTripQty = MutableStateFlow("") 

    val steelTripPrice = MutableStateFlow("") // now treated as Price per kg
    val steelTripQty = MutableStateFlow("1000") // kg per trip

    val tilePricePerSqFt = MutableStateFlow("")
    val plumbingFixtureCost = MutableStateFlow("")

    // Step 6: Aggregated Outputs
    val totalBricks = MutableStateFlow(0)
    val totalBlocks = MutableStateFlow(0)
    val totalCement = MutableStateFlow(0.0)
    val totalSand = MutableStateFlow(0.0)
    val totalAggregate = MutableStateFlow(0.0)
    val totalSteel = MutableStateFlow(0.0)
    val totalTileArea = MutableStateFlow(0.0)
    val totalTiles = MutableStateFlow(0.0)
    val totalFloorAreaSqFt = MutableStateFlow(0.0)

    val roomEstimates = MutableStateFlow<List<IndividualRoomEstimate>>(emptyList())
    val brickTrips = MutableStateFlow(0)
    val brickCost = MutableStateFlow(0.0)
    val blockTrips = MutableStateFlow(0)
    val blockCost = MutableStateFlow(0.0)
    val cementTrips = MutableStateFlow(0)
    val cementCost = MutableStateFlow(0.0)
    val sandTrips = MutableStateFlow(0)
    val sandCost = MutableStateFlow(0.0)
    val aggregateTrips = MutableStateFlow(0)
    val aggregateCost = MutableStateFlow(0.0)
    val steelTrips = MutableStateFlow(0)
    val steelCost = MutableStateFlow(0.0)
    val tilesCost = MutableStateFlow(0.0)
    val laborCost = MutableStateFlow(0.0)
    val boundaryWallCost = MutableStateFlow(0.0)
    val grandTotal = MutableStateFlow(0.0)

    private val _validationError = MutableStateFlow<String?>(null)
    val validationError: StateFlow<String?> = _validationError.asStateFlow()

    // --- VALIDATION STATES ---
    val isValidationActive = MutableStateFlow(false)
    val firstInvalidField = MutableSharedFlow<String>(replay = 0)

    val projectNameError = MutableStateFlow<String?>(null)
    val projectLocationError = MutableStateFlow<String?>(null)
    val laborRatePerSqFtError = MutableStateFlow<String?>(null)
    val slabThicknessGlobalError = MutableStateFlow<String?>(null)

    val boundaryWallLengthError = MutableStateFlow<String?>(null)
    val boundaryWallHeightError = MutableStateFlow<String?>(null)
    val boundaryWallThicknessError = MutableStateFlow<String?>(null)
    val gateWidthError = MutableStateFlow<String?>(null)
    val gateHeightError = MutableStateFlow<String?>(null)

    val brickTripPriceError = MutableStateFlow<String?>(null)
    val blockTripPriceError = MutableStateFlow<String?>(null)
    val cementTripPriceError = MutableStateFlow<String?>(null)
    val sandTripPriceError = MutableStateFlow<String?>(null)
    val sandTripQtyError = MutableStateFlow<String?>(null)
    val aggregateTripPriceError = MutableStateFlow<String?>(null)
    val aggregateTripQtyError = MutableStateFlow<String?>(null)
    val steelTripPriceError = MutableStateFlow<String?>(null)
    val tilePricePerSqFtError = MutableStateFlow<String?>(null)
    val plumbingFixtureCostError = MutableStateFlow<String?>(null)

    init {
        viewModelScope.launch {
            unitType.collect { ut ->
                if (ut == UnitType.METRIC) {
                    if (slabThicknessGlobal.value.isEmpty()) slabThicknessGlobal.value = "12" // 12 cm
                    if (sandTripQty.value.isEmpty() || sandTripQty.value == "150") sandTripQty.value = "4.25" // 4.25 m3
                    if (aggregateTripQty.value.isEmpty() || aggregateTripQty.value == "150") aggregateTripQty.value = "4.25"

                    brickLength.value = "22.86"
                    brickWidth.value = "11.43"
                    brickHeight.value = "7.62"

                    blockLength.value = "40.64"
                    blockWidth.value = "20.32"
                    blockHeight.value = "15.24"
                } else {
                    if (slabThicknessGlobal.value.isEmpty()) slabThicknessGlobal.value = "5" // 5 inches
                    if (sandTripQty.value.isEmpty() || sandTripQty.value == "4.25") sandTripQty.value = "150" // 150 cft
                    if (aggregateTripQty.value.isEmpty() || aggregateTripQty.value == "4.25") aggregateTripQty.value = "150"

                    brickLength.value = "9.0"
                    brickWidth.value = "4.5"
                    brickHeight.value = "3.0"

                    blockLength.value = "16.0"
                    blockWidth.value = "8.0"
                    blockHeight.value = "6.0"
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

        setupRealtimeValidation(projectName, projectNameError) { Validator.validateRequiredField(it, "project name") }
        setupRealtimeValidation(projectLocation, projectLocationError) { Validator.validateRequiredField(it, "project location") }
        setupRealtimeValidation(laborRatePerSqFt, laborRatePerSqFtError) { Validator.validatePositiveNumber(it, "labor rate") }
        setupRealtimeValidation(slabThicknessGlobal, slabThicknessGlobalError) { Validator.validatePositiveNumber(it, "slab thickness") }

        setupRealtimeValidation(boundaryWallLength, boundaryWallLengthError) { Validator.validatePositiveNumber(it, "boundary wall length") }
        setupRealtimeValidation(boundaryWallHeight, boundaryWallHeightError) { Validator.validatePositiveNumber(it, "boundary wall height") }
        setupRealtimeValidation(boundaryWallThickness, boundaryWallThicknessError) { Validator.validatePositiveNumber(it, "boundary wall thickness") }
        setupRealtimeValidation(gateWidth, gateWidthError) { Validator.validatePositiveNumber(it, "gate width") }
        setupRealtimeValidation(gateHeight, gateHeightError) { Validator.validatePositiveNumber(it, "gate height") }

        setupRealtimeValidation(brickTripPrice, brickTripPriceError) { Validator.validatePositiveNumber(it, "brick price") }
        setupRealtimeValidation(blockTripPrice, blockTripPriceError) { Validator.validatePositiveNumber(it, "block price") }
        setupRealtimeValidation(cementTripPrice, cementTripPriceError) { Validator.validatePositiveNumber(it, "cement price") }
        setupRealtimeValidation(sandTripPrice, sandTripPriceError) { Validator.validatePositiveNumber(it, "sand price") }
        setupRealtimeValidation(sandTripQty, sandTripQtyError) { Validator.validatePositiveNumber(it, "sand quantity") }
        setupRealtimeValidation(aggregateTripPrice, aggregateTripPriceError) { Validator.validatePositiveNumber(it, "aggregate price") }
        setupRealtimeValidation(aggregateTripQty, aggregateTripQtyError) { Validator.validatePositiveNumber(it, "aggregate quantity") }
        setupRealtimeValidation(steelTripPrice, steelTripPriceError) { Validator.validatePositiveNumber(it, "steel price") }
        setupRealtimeValidation(tilePricePerSqFt, tilePricePerSqFtError) { Validator.validatePositiveNumber(it, "tile price") }
        setupRealtimeValidation(plumbingFixtureCost, plumbingFixtureCostError) { Validator.validatePositiveNumber(it, "plumbing cost") }

        viewModelScope.launch {
            merge(
                projectType.map { }, projectName.map { }, projectLocation.map { }, projectNotes.map { }, unitType.map { },
                laborRatePerSqFt.map { }, slabThicknessGlobal.map { }, houseRooms.map { },
                isBoundaryWallEnabled.map { }, boundaryWallLength.map { }, boundaryWallHeight.map { },
                boundaryWallThickness.map { }, gateWidth.map { }, gateHeight.map { }, boundaryMaterialType.map { },
                brickTripPrice.map { }, brickTripQty.map { }, brickLength.map { }, brickWidth.map { }, brickHeight.map { },
                blockTripPrice.map { }, blockTripQty.map { }, blockLength.map { }, blockWidth.map { }, blockHeight.map { },
                cementTripPrice.map { }, cementTripQty.map { }, sandTripPrice.map { }, sandTripQty.map { },
                aggregateTripPrice.map { }, aggregateTripQty.map { }, steelTripPrice.map { }, steelTripQty.map { },
                tilePricePerSqFt.map { }, plumbingFixtureCost.map { }
            ).collect {
                calculateAll()
            }
        }
    }

    fun validateCurrentStep(): Boolean {
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

        when (_currentStep.value) {
            2 -> {
                check("projectName", projectName.value, projectNameError) { Validator.validateRequiredField(it, "project name") }
                check("projectLocation", projectLocation.value, projectLocationError) { Validator.validateRequiredField(it, "project location") }
                check("laborRatePerSqFt", laborRatePerSqFt.value, laborRatePerSqFtError) { Validator.validatePositiveNumber(it, "labor rate") }
                check("slabThicknessGlobal", slabThicknessGlobal.value, slabThicknessGlobalError) { Validator.validatePositiveNumber(it, "slab thickness") }
            }
            3 -> {
                if (houseRooms.value.isEmpty()) {
                    _validationError.value = "Please add at least one room before continuing"
                    isValid = false
                } else {
                    _validationError.value = null
                }
            }
            4 -> {
                if (isBoundaryWallEnabled.value) {
                    check("boundaryWallLength", boundaryWallLength.value, boundaryWallLengthError) { Validator.validatePositiveNumber(it, "boundary wall length") }
                    check("boundaryWallHeight", boundaryWallHeight.value, boundaryWallHeightError) { Validator.validatePositiveNumber(it, "boundary wall height") }
                    check("boundaryWallThickness", boundaryWallThickness.value, boundaryWallThicknessError) { Validator.validatePositiveNumber(it, "boundary wall thickness") }
                    check("gateWidth", gateWidth.value, gateWidthError) { Validator.validatePositiveNumber(it, "gate width") }
                    check("gateHeight", gateHeight.value, gateHeightError) { Validator.validatePositiveNumber(it, "gate height") }
                }
            }
            5 -> {
                val hasBricks = houseRooms.value.any { it.materialType == "Brick" || (it.isWashroomEnabled && it.washroomMaterialType == "Brick") } || (isBoundaryWallEnabled.value && boundaryMaterialType.value == "Brick")
                val hasBlocks = houseRooms.value.any { it.materialType == "Block" || (it.isWashroomEnabled && it.washroomMaterialType == "Block") } || (isBoundaryWallEnabled.value && boundaryMaterialType.value == "Block")
                val hasSlab = houseRooms.value.any { it.hasRoof }
                val hasWashroom = houseRooms.value.any { it.isWashroomEnabled }

                if (hasBricks) {
                    check("brickTripPrice", brickTripPrice.value, brickTripPriceError) { Validator.validatePositiveNumber(it, "brick price") }
                }
                if (hasBlocks) {
                    check("blockTripPrice", blockTripPrice.value, blockTripPriceError) { Validator.validatePositiveNumber(it, "block price") }
                }
                check("cementTripPrice", cementTripPrice.value, cementTripPriceError) { Validator.validatePositiveNumber(it, "cement price") }
                check("sandTripPrice", sandTripPrice.value, sandTripPriceError) { Validator.validatePositiveNumber(it, "sand price") }
                check("sandTripQty", sandTripQty.value, sandTripQtyError) { Validator.validatePositiveNumber(it, "sand quantity") }
                
                if (hasSlab) {
                    check("aggregateTripPrice", aggregateTripPrice.value, aggregateTripPriceError) { Validator.validatePositiveNumber(it, "aggregate price") }
                    check("aggregateTripQty", aggregateTripQty.value, aggregateTripQtyError) { Validator.validatePositiveNumber(it, "aggregate quantity") }
                    check("steelTripPrice", steelTripPrice.value, steelTripPriceError) { Validator.validatePositiveNumber(it, "steel price") }
                }
                if (hasWashroom) {
                    check("tilePricePerSqFt", tilePricePerSqFt.value, tilePricePerSqFtError) { Validator.validatePositiveNumber(it, "tile price") }
                    check("plumbingFixtureCost", plumbingFixtureCost.value, plumbingFixtureCostError) { Validator.validatePositiveNumber(it, "plumbing cost") }
                }
            }
        }

        if (!isValid && firstInvalid != null) {
            viewModelScope.launch {
                firstInvalidField.emit(firstInvalid!!)
            }
        }

        return isValid
    }

    fun nextStep() {
        if (validateCurrentStep()) {
            if (_currentStep.value < 6) {
                _currentStep.value += 1
                isValidationActive.value = false
            }
        }
    }

    fun previousStep() {
        if (_currentStep.value > 1) {
            _currentStep.value -= 1
            isValidationActive.value = false
            _validationError.value = null
        }
    }

    fun setProjectType(type: String) {
        projectType.value = type
    }

    fun clearError() {
        _validationError.value = null
    }

    fun addRoom(room: HouseRoomInput) {
        houseRooms.value = houseRooms.value + room.copy(id = houseRooms.value.size.toLong() + 1)
    }

    fun removeRoom(index: Int) {
        val current = houseRooms.value.toMutableList()
        if (index in current.indices) {
            current.removeAt(index)
            houseRooms.value = current.mapIndexed { idx, rm -> rm.copy(id = idx.toLong() + 1) }
        }
    }

    fun calculateAll() {
        val isImperial = unitType.value == UnitType.IMPERIAL
        val globalSlabThick = slabThicknessGlobal.value.toDoubleOrNull() ?: 0.0

        var overallBricks = 0
        var overallBlocks = 0
        var overallCement = 0.0
        var overallSand = 0.0
        var overallSteel = 0.0
        var overallTileArea = 0.0
        var overallTiles = 0.0
        var overallFloorAreaSqFt = 0.0

        val prBrickTrip = brickTripPrice.value.toDoubleOrNull() ?: 0.0 // Unit price per brick
        val qtyBrickTrip = brickTripQty.value.toDoubleOrNull().let { if (it == null || it <= 0.0) 1000.0 else it }

        val prBlockTrip = blockTripPrice.value.toDoubleOrNull() ?: 0.0 // Unit price per block
        val qtyBlockTrip = blockTripQty.value.toDoubleOrNull().let { if (it == null || it <= 0.0) 300.0 else it }

        val prCementTrip = cementTripPrice.value.toDoubleOrNull() ?: 0.0 // Unit price per bag
        val qtyCementTrip = cementTripQty.value.toDoubleOrNull().let { if (it == null || it <= 0.0) 100.0 else it }

        val prSandTrip = sandTripPrice.value.toDoubleOrNull() ?: 0.0 // Unit price per cft/m3
        val qtySandTrip = sandTripQty.value.toDoubleOrNull().let { if (it == null || it <= 0.0) 150.0 else it }

        val prAggTrip = aggregateTripPrice.value.toDoubleOrNull() ?: 0.0 // Unit price per cft/m3
        val qtyAggTrip = aggregateTripQty.value.toDoubleOrNull().let { if (it == null || it <= 0.0) 150.0 else it }

        val prSteelTrip = steelTripPrice.value.toDoubleOrNull() ?: 0.0 // Unit price per kg
        val qtySteelTrip = steelTripQty.value.toDoubleOrNull().let { if (it == null || it <= 0.0) 1000.0 else it }

        val prTileSqFt = tilePricePerSqFt.value.toDoubleOrNull() ?: 0.0
        val laborRate = laborRatePerSqFt.value.toDoubleOrNull() ?: 0.0
        val plumbingCost = plumbingFixtureCost.value.toDoubleOrNull() ?: 0.0

        val brL = brickLength.value.toDoubleOrNull() ?: 9.0
        val brW = brickWidth.value.toDoubleOrNull() ?: 4.5
        val brH = brickHeight.value.toDoubleOrNull() ?: 3.0

        val blL = blockLength.value.toDoubleOrNull() ?: 16.0
        val blW = blockWidth.value.toDoubleOrNull() ?: 8.0
        val blH = blockHeight.value.toDoubleOrNull() ?: 6.0

        val roomEstimatesList = houseRooms.value.map { rm ->
            val floorAreaRaw = rm.length * rm.width
            val floorAreaSqFt = (if (isImperial) floorAreaRaw else floorAreaRaw * 10.7639) * rm.quantity
            overallFloorAreaSqFt += floorAreaSqFt

            val wallVol = HouseEstimator.calculateRoomWallVolume(rm, isImperial) * rm.quantity
            var bricks = 0
            var blocks = 0
            var wallCement = 0.0
            var wallSand = 0.0

            if (rm.materialType == "Brick") {
                bricks = HouseEstimator.calculateUnitQuantity(wallVol, brL, brW, brH, isImperial)
                wallCement = HouseEstimator.calculateCementBags(wallVol, bricks, brL, brW, brH, isImperial)
                wallSand = HouseEstimator.calculateSandVolume(wallVol, bricks, brL, brW, brH, isImperial)
                overallBricks += bricks
            } else {
                blocks = HouseEstimator.calculateUnitQuantity(wallVol, blL, blW, blH, isImperial)
                wallCement = HouseEstimator.calculateCementBags(wallVol, blocks, blL, blW, blH, isImperial)
                wallSand = HouseEstimator.calculateSandVolume(wallVol, blocks, blL, blW, blH, isImperial)
                overallBlocks += blocks
            }

            var slabCement = 0.0
            var slabSand = 0.0
            var slabAgg = 0.0
            var slabSteel = 0.0
            if (rm.hasRoof) {
                val slabVol = HouseEstimator.calculateSlabVolume(rm, globalSlabThick, isImperial) * rm.quantity
                slabCement = SlabCalculator.calculateCementBags(slabVol)
                slabSand = SlabCalculator.calculateSandVolume(slabVol)
                slabAgg = SlabCalculator.calculateAggregateVolume(slabVol)
                slabSteel = SlabCalculator.calculateSteelWeight(slabVol)

                overallSteel += slabSteel
            }

            var wBricks = 0
            var wBlocks = 0
            var wCement = 0.0
            var wSand = 0.0
            var wTileA = 0.0
            var wTilesReq = 0.0
            var wTileCement = 0.0
            var wTileSand = 0.0
            var wFloorSqFt = 0.0

            if (rm.isWashroomEnabled) {
                val wVol = HouseEstimator.calculateWashroomWallVolume(rm, isImperial) * rm.quantity
                if (rm.washroomMaterialType == "Brick") {
                    wBricks = HouseEstimator.calculateUnitQuantity(wVol, brL, brW, brH, isImperial)
                    wCement = HouseEstimator.calculateCementBags(wVol, wBricks, brL, brW, brH, isImperial)
                    wSand = HouseEstimator.calculateSandVolume(wVol, wBricks, brL, brW, brH, isImperial)
                    overallBricks += wBricks
                } else {
                    wBlocks = HouseEstimator.calculateUnitQuantity(wVol, blL, blW, blH, isImperial)
                    wCement = HouseEstimator.calculateCementBags(wVol, wBlocks, blL, blW, blH, isImperial)
                    wSand = HouseEstimator.calculateSandVolume(wVol, wBlocks, blL, blW, blH, isImperial)
                    overallBlocks += wBlocks
                }

                val wGross = rm.washroomLength * rm.washroomWidth
                val wWall = max(0.0, 2 * (rm.washroomLength + rm.washroomWidth) * rm.washroomHeight - (rm.washroomDoorsCount * rm.washroomDoorsWidth * rm.washroomDoorsHeight) - (rm.washroomWindowsCount * rm.washroomWindowsWidth * rm.washroomWindowsHeight))
                val totalTileA = (wGross + wWall) * rm.quantity
                wTileA = if (isImperial) totalTileA else totalTileA * 10.7639
                wTilesReq = totalTileA * 1.10
                wTileCement = totalTileA * 0.2
                wTileSand = totalTileA * 0.015

                wFloorSqFt = (if (isImperial) wGross else wGross * 10.7639) * rm.quantity
                overallFloorAreaSqFt += wFloorSqFt
                overallTileArea += wTileA
                overallTiles += if (isImperial) wTilesReq else wTilesReq * 10.7639
            }

            overallCement += wallCement + slabCement + wCement + wTileCement
            
            val totalSandM3 = wallSand + slabSand + wSand + wTileSand
            overallSand += if (isImperial) totalSandM3 * 35.3147 else totalSandM3

            // Cost Calculations based directly on UNIT prices
            val rBrickCost = if (rm.materialType == "Brick") bricks * prBrickTrip else 0.0
            val rBlockCost = if (rm.materialType == "Block") blocks * prBlockTrip else 0.0
            val rCementCost = (wallCement + slabCement) * prCementTrip
            val rSandCost = (if (isImperial) (wallSand + slabSand) * 35.3147 else wallSand + slabSand) * prSandTrip
            val rAggCost = slabAgg * prAggTrip
            val rSteelCost = slabSteel * prSteelTrip

            val wBrickCost = if (rm.washroomMaterialType == "Brick") wBricks * prBrickTrip else 0.0
            val wBlockCost = if (rm.washroomMaterialType == "Block") wBlocks * prBlockTrip else 0.0
            val wCementCost = (wCement + wTileCement) * prCementTrip
            val wSandCost = (if (isImperial) (wSand + wTileSand) * 35.3147 else wSand + wTileSand) * prSandTrip
            val wTileCost = wTileA * prTileSqFt
            
            val washTotalCost = wBrickCost + wBlockCost + wCementCost + wSandCost + wTileCost + (plumbingCost * rm.quantity) + (wFloorSqFt * laborRate)

            val roomTotalMatCost = rBrickCost + rBlockCost + rCementCost + rSandCost + rAggCost + rSteelCost + washTotalCost
            val roomLaborCost = floorAreaSqFt * laborRate

            IndividualRoomEstimate(
                roomName = rm.name,
                roomType = rm.type,
                floorAreaSqFt = floorAreaSqFt,
                wallVolumeM3 = wallVol,
                bricksCount = bricks,
                blocksCount = blocks,
                cementBags = wallCement,
                sandVol = if (isImperial) wallSand * 35.3147 else wallSand,
                hasRoof = rm.hasRoof,
                slabCementBags = slabCement,
                slabSandVol = if (isImperial) slabSand * 35.3147 else slabSand,
                slabAggVol = if (isImperial) slabAgg * 35.3147 else slabAgg,
                slabSteelKg = slabSteel,
                hasWashroom = rm.isWashroomEnabled,
                washroomFloorAreaSqFt = wFloorSqFt,
                washroomBricksCount = wBricks,
                washroomBlocksCount = wBlocks,
                washroomCementBags = wCement + wTileCement,
                washroomSandVol = if (isImperial) (wSand + wTileSand) * 35.3147 else wSand + wTileSand,
                washroomTilesSqFt = wTileA,
                washroomTilesCost = wTileCost + (plumbingCost * rm.quantity),
                totalCost = roomTotalMatCost + roomLaborCost,
                quantityMultiplier = rm.quantity
            )
        }

        roomEstimates.value = roomEstimatesList

        var overallSlabAggM3 = houseRooms.value.sumOf { rm ->
            if (rm.hasRoof) {
                val slabVol = HouseEstimator.calculateSlabVolume(rm, globalSlabThick, isImperial) * rm.quantity
                SlabCalculator.calculateAggregateVolume(slabVol)
            } else 0.0
        }

        var bWallCost = 0.0
        if (isBoundaryWallEnabled.value) {
            val bLength = boundaryWallLength.value.toDoubleOrNull() ?: 0.0
            val bHeight = boundaryWallHeight.value.toDoubleOrNull() ?: 0.0
            val bThick = boundaryWallThickness.value.toDoubleOrNull() ?: 0.0
            val gW = gateWidth.value.toDoubleOrNull() ?: 0.0
            val gH = gateHeight.value.toDoubleOrNull() ?: 0.0

            val bVol = HouseEstimator.calculateBoundaryWallVolume(bLength, bHeight, bThick, gW, gH, isImperial)
            var bBricks = 0
            var bBlocks = 0
            var bCement = 0.0
            var bSand = 0.0

            if (boundaryMaterialType.value == "Brick") {
                bBricks = HouseEstimator.calculateUnitQuantity(bVol, brL, brW, brH, isImperial)
                bCement = HouseEstimator.calculateCementBags(bVol, bBricks, brL, brW, brH, isImperial)
                bSand = HouseEstimator.calculateSandVolume(bVol, bBricks, brL, brW, brH, isImperial)
                overallBricks += bBricks
            } else {
                bBlocks = HouseEstimator.calculateUnitQuantity(bVol, blL, blW, blH, isImperial)
                bCement = HouseEstimator.calculateCementBags(bVol, bBlocks, blL, blW, blH, isImperial)
                bSand = HouseEstimator.calculateSandVolume(bVol, bBlocks, blL, blW, blH, isImperial)
                overallBlocks += bBlocks
            }

            val pVol = HouseEstimator.calculateBoundaryPillarsVolume(bLength, bHeight, bThick, isImperial)
            val pCement = SlabCalculator.calculateCementBags(pVol)
            val pSand = SlabCalculator.calculateSandVolume(pVol)
            val pAgg = SlabCalculator.calculateAggregateVolume(pVol)
            val pSteel = SlabCalculator.calculateSteelWeight(pVol)

            overallCement += bCement + pCement
            
            val totalBSandM3 = bSand + pSand
            overallSand += if (isImperial) totalBSandM3 * 35.3147 else totalBSandM3
            overallSlabAggM3 += pAgg
            overallSteel += pSteel

            val bBrickCost = if (boundaryMaterialType.value == "Brick") bBricks * prBrickTrip else 0.0
            val bBlockCost = if (boundaryMaterialType.value == "Block") bBlocks * prBlockTrip else 0.0
            val bCementCost = (bCement + pCement) * prCementTrip
            val bSandCost = (if (isImperial) totalBSandM3 * 35.3147 else totalBSandM3) * prSandTrip
            val bAggCost = pAgg * prAggTrip
            val bSteelCost = pSteel * prSteelTrip

            bWallCost = bBrickCost + bBlockCost + bCementCost + bSandCost + bAggCost + bSteelCost
        }

        boundaryWallCost.value = bWallCost

        totalBricks.value = overallBricks
        totalBlocks.value = overallBlocks
        totalCement.value = overallCement
        totalSand.value = overallSand
        totalAggregate.value = if (isImperial) overallSlabAggM3 * 35.3147 else overallSlabAggM3
        totalSteel.value = overallSteel
        totalTileArea.value = overallTileArea
        totalTiles.value = overallTiles
        totalFloorAreaSqFt.value = overallFloorAreaSqFt

        val bTrips = ceil(overallBricks / qtyBrickTrip).toInt()
        brickTrips.value = bTrips
        brickCost.value = overallBricks * prBrickTrip

        val blTrips = ceil(overallBlocks / qtyBlockTrip).toInt()
        blockTrips.value = blTrips
        blockCost.value = overallBlocks * prBlockTrip

        val cTrips = ceil(overallCement / qtyCementTrip).toInt()
        cementTrips.value = cTrips
        cementCost.value = overallCement * prCementTrip

        val sTrips = ceil(overallSand / qtySandTrip).toInt()
        sandTrips.value = sTrips
        sandCost.value = overallSand * prSandTrip

        val aggregateVal = if (isImperial) overallSlabAggM3 * 35.3147 else overallSlabAggM3
        val aTrips = ceil(aggregateVal / qtyAggTrip).toInt()
        aggregateTrips.value = aTrips
        aggregateCost.value = aggregateVal * prAggTrip

        val stTrips = ceil(overallSteel / qtySteelTrip).toInt()
        steelTrips.value = stTrips
        steelCost.value = overallSteel * prSteelTrip

        tilesCost.value = overallTileArea * prTileSqFt
        laborCost.value = overallFloorAreaSqFt * laborRate

        val washroomCount = houseRooms.value.sumOf { it.quantity }
        grandTotal.value = brickCost.value + blockCost.value + cementCost.value + sandCost.value +
                aggregateCost.value + steelCost.value + tilesCost.value + laborCost.value + (plumbingCost * washroomCount)
    }

    fun loadProject(projectId: Long) {
        if (projectId <= 0 || editingProjectId == projectId) return
        editingProjectId = projectId
        viewModelScope.launch {
            useCases.getProjectById(projectId)?.let { details ->
                projectName.value = details.project.name
                projectLocation.value = details.project.location

                val loadedNotes = details.project.notes
                if (loadedNotes.startsWith("[Type: ")) {
                    val endIdx = loadedNotes.indexOf("]")
                    if (endIdx != -1) {
                        projectType.value = loadedNotes.substring(7, endIdx)
                        projectNotes.value = loadedNotes.substring(endIdx + 1).trim()
                    } else {
                        projectNotes.value = loadedNotes
                    }
                } else {
                    projectNotes.value = loadedNotes
                }

                if (details.rooms.isNotEmpty()) {
                    unitType.value = details.rooms.first().room.unitType
                }

                val loadedRooms = mutableListOf<HouseRoomInput>()
                details.rooms.forEach { rd ->
                    val r = rd.room
                    if (r.name == "Boundary Wall") {
                        isBoundaryWallEnabled.value = true
                        boundaryWallLength.value = r.length.toString()
                        boundaryWallHeight.value = r.height.toString()
                    } else if (!r.name.contains("Washroom")) {
                        val doors = rd.openings.filter { it.type == OpeningType.DOOR }
                        val windows = rd.openings.filter { it.type == OpeningType.WINDOW }
                        
                        val washroomRoom = details.rooms.firstOrNull { it.room.name == "${r.name} - Attached Washroom" || it.room.name.startsWith("${r.name} - Attached Washroom") }
                        val hasWashroom = washroomRoom != null
                        val washDoors = washroomRoom?.openings?.filter { it.type == OpeningType.DOOR } ?: emptyList()
                        val washWindows = washroomRoom?.openings?.filter { it.type == OpeningType.WINDOW } ?: emptyList()

                        var qty = 1
                        var baseName = r.name
                        if (r.name.contains(" (x")) {
                            val startIdx = r.name.lastIndexOf(" (x")
                            val endIdx = r.name.indexOf(")", startIdx)
                            if (startIdx != -1 && endIdx != -1) {
                                qty = r.name.substring(startIdx + 4, endIdx).toIntOrNull() ?: 1
                                baseName = r.name.substring(0, startIdx).trim()
                            }
                        }

                        val defaultDoorW = if (unitType.value == UnitType.METRIC) 0.9 else 3.0
                        val defaultDoorH = if (unitType.value == UnitType.METRIC) 2.1 else 7.0
                        val defaultWinW = if (unitType.value == UnitType.METRIC) 1.2 else 4.0
                        val defaultWinH = if (unitType.value == UnitType.METRIC) 1.2 else 4.0

                        val defaultWashDoorW = if (unitType.value == UnitType.METRIC) 0.75 else 2.5
                        val defaultWashDoorH = if (unitType.value == UnitType.METRIC) 2.0 else 6.5
                        val defaultWashWinW = if (unitType.value == UnitType.METRIC) 0.6 else 2.0
                        val defaultWashWinH = if (unitType.value == UnitType.METRIC) 0.6 else 2.0

                        loadedRooms.add(
                            HouseRoomInput(
                                id = r.id,
                                name = baseName,
                                type = if (r.name.contains("Kitchen")) "Kitchen" else "Bedroom",
                                length = r.length,
                                width = r.width,
                                height = r.height,
                                thickness = if (unitType.value == UnitType.METRIC) 23.0 else 9.0,
                                materialType = "Brick",
                                doorsCount = doors.sumOf { it.quantity },
                                doorsWidth = doors.firstOrNull()?.width ?: defaultDoorW,
                                doorsHeight = doors.firstOrNull()?.height ?: defaultDoorH,
                                windowsCount = windows.sumOf { it.quantity },
                                windowsWidth = windows.firstOrNull()?.width ?: defaultWinW,
                                windowsHeight = windows.firstOrNull()?.height ?: defaultWinH,
                                hasRoof = r.includeCeiling,
                                isWashroomEnabled = hasWashroom,
                                washroomLength = washroomRoom?.room?.length ?: 0.0,
                                washroomWidth = washroomRoom?.room?.width ?: 0.0,
                                washroomHeight = washroomRoom?.room?.height ?: 0.0,
                                washroomThickness = if (unitType.value == UnitType.METRIC) 11.5 else 4.5,
                                washroomDoorsCount = washDoors.sumOf { it.quantity },
                                washroomDoorsWidth = washDoors.firstOrNull()?.width ?: defaultWashDoorW,
                                washroomDoorsHeight = washDoors.firstOrNull()?.height ?: defaultWashDoorH,
                                washroomWindowsCount = washWindows.sumOf { it.quantity },
                                washroomWindowsWidth = washWindows.firstOrNull()?.width ?: defaultWashWinW,
                                washroomWindowsHeight = washWindows.firstOrNull()?.height ?: defaultWashWinH,
                                washroomMaterialType = "Brick",
                                quantity = qty
                            )
                        )
                    }
                }
                houseRooms.value = loadedRooms

                details.materials.forEach { m ->
                    when {
                        m.name.contains("Brick") -> brickTripPrice.value = m.unitPrice.toString()
                        m.name.contains("Block") -> blockTripPrice.value = m.unitPrice.toString()
                        m.name.contains("Cement") -> cementTripPrice.value = m.unitPrice.toString()
                        m.name.contains("Sand") -> sandTripPrice.value = m.unitPrice.toString()
                        m.name.contains("Aggregate") -> aggregateTripPrice.value = m.unitPrice.toString()
                        m.name.contains("Steel") -> steelTripPrice.value = m.unitPrice.toString()
                        m.name.contains("Tile") -> tilePricePerSqFt.value = m.unitPrice.toString()
                        m.name.contains("Plumbing") -> plumbingFixtureCost.value = m.unitPrice.toString()
                    }
                }

                laborRatePerSqFt.value = if (details.estimate.laborRatePerUnit > 0) details.estimate.laborRatePerUnit.toString() else ""
                calculateAll()
            }
        }
    }

    fun saveProject(onSuccess: (Long) -> Unit) {
        viewModelScope.launch {
            calculateAll()
            val format = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
            val dateStr = format.format(Date())

            val finalProjectName = if (projectName.value.trim().isEmpty()) "${projectType.value} Estimate - $dateStr" else projectName.value.trim()
            val finalNotes = "[Type: ${projectType.value}] ${projectNotes.value}"

            val project = Project(
                id = editingProjectId,
                name = finalProjectName,
                location = projectLocation.value,
                notes = finalNotes
            )

            val roomDetailsList = mutableListOf<RoomDetails>()
            houseRooms.value.forEach { hr ->
                val dbName = if (hr.quantity > 1) "${hr.name} (x${hr.quantity})" else hr.name
                val roomEntity = Room(
                    id = 0,
                    projectId = editingProjectId,
                    name = dbName,
                    width = hr.width,
                    length = hr.length,
                    height = hr.height,
                    wallCount = 4,
                    includeCeiling = hr.hasRoof,
                    unitType = unitType.value
                )
                
                val openingEntities = mutableListOf<Opening>()
                if (hr.doorsCount > 0) {
                    openingEntities.add(Opening(0, 0, OpeningType.DOOR, hr.doorsWidth, hr.doorsHeight, hr.doorsCount))
                }
                if (hr.windowsCount > 0) {
                    openingEntities.add(Opening(0, 0, OpeningType.WINDOW, hr.windowsWidth, hr.windowsHeight, hr.windowsCount))
                }

                roomDetailsList.add(RoomDetails(room = roomEntity, openings = openingEntities))

                if (hr.isWashroomEnabled) {
                    val washName = if (hr.quantity > 1) "${hr.name} - Attached Washroom (x${hr.quantity})" else "${hr.name} - Attached Washroom"
                    val washEntity = Room(
                        id = 0,
                        projectId = editingProjectId,
                        name = washName,
                        width = hr.washroomWidth,
                        length = hr.washroomLength,
                        height = hr.washroomHeight,
                        wallCount = 4,
                        includeCeiling = false,
                        unitType = unitType.value
                    )
                    val washOpenings = mutableListOf<Opening>()
                    if (hr.washroomDoorsCount > 0) {
                        washOpenings.add(Opening(0, 0, OpeningType.DOOR, hr.washroomDoorsWidth, hr.washroomDoorsHeight, hr.washroomDoorsCount))
                    }
                    if (hr.washroomWindowsCount > 0) {
                        washOpenings.add(Opening(0, 0, OpeningType.WINDOW, hr.washroomWindowsWidth, hr.washroomWindowsHeight, hr.washroomWindowsCount))
                    }
                    roomDetailsList.add(RoomDetails(washEntity, washOpenings))
                }
            }

            if (isBoundaryWallEnabled.value) {
                val bLength = boundaryWallLength.value.toDoubleOrNull() ?: 0.0
                val bHeight = boundaryWallHeight.value.toDoubleOrNull() ?: 0.0
                val roomEntity = Room(
                    id = 0,
                    projectId = editingProjectId,
                    name = "Boundary Wall",
                    width = 0.0,
                    length = bLength,
                    height = bHeight,
                    wallCount = 1,
                    includeCeiling = false,
                    unitType = unitType.value
                )
                roomDetailsList.add(RoomDetails(roomEntity, emptyList()))
            }

            val savedMaterials = mutableListOf<Material>()
            
            if (totalBricks.value > 0) {
                savedMaterials.add(Material(0, 0, "Bricks (${totalBricks.value} pcs in ${brickTrips.value} trips)", totalBricks.value.toDouble(), brickTripPrice.value.toDoubleOrNull() ?: 0.0))
            }
            if (totalBlocks.value > 0) {
                savedMaterials.add(Material(0, 0, "Blocks (${totalBlocks.value} pcs in ${blockTrips.value} trips)", totalBlocks.value.toDouble(), blockTripPrice.value.toDoubleOrNull() ?: 0.0))
            }
            if (totalCement.value > 0) {
                savedMaterials.add(Material(0, 0, "Cement Bags (${totalCement.value.toInt()} bags in ${cementTrips.value} trips)", totalCement.value, cementTripPrice.value.toDoubleOrNull() ?: 0.0))
            }
            if (totalSand.value > 0) {
                val unit = if (unitType.value == UnitType.METRIC) "m³" else "cft"
                savedMaterials.add(Material(0, 0, "Sand (${String.format(Locale.US, "%.1f", totalSand.value)} $unit in ${sandTrips.value} trips)", totalSand.value, sandTripPrice.value.toDoubleOrNull() ?: 0.0))
            }
            if (totalAggregate.value > 0) {
                val unit = if (unitType.value == UnitType.METRIC) "m³" else "cft"
                savedMaterials.add(Material(0, 0, "Aggregate (${String.format(Locale.US, "%.1f", totalAggregate.value)} $unit in ${aggregateTrips.value} trips)", totalAggregate.value, aggregateTripPrice.value.toDoubleOrNull() ?: 0.0))
            }
            if (totalSteel.value > 0) {
                savedMaterials.add(Material(0, 0, "Steel (${totalSteel.value.toInt()} kg in ${steelTrips.value} trips)", totalSteel.value, steelTripPrice.value.toDoubleOrNull() ?: 0.0))
            }
            if (totalTileArea.value > 0) {
                savedMaterials.add(Material(0, 0, "Washroom Tiles (Area: ${totalTileArea.value.toInt()} sq ft)", totalTileArea.value, tilePricePerSqFt.value.toDoubleOrNull() ?: 0.0))
            }
            
            val totalPlumbCost = plumbingFixtureCost.value.toDoubleOrNull() ?: 0.0
            if (totalPlumbCost > 0) {
                val washroomCount = houseRooms.value.sumOf { it.quantity }
                savedMaterials.add(Material(0, 0, "Plumbing & Fixtures", washroomCount.toDouble(), totalPlumbCost))
            }

            val estimate = Estimate(
                id = 0,
                projectId = editingProjectId,
                totalSurfaceArea = totalFloorAreaSqFt.value,
                paintableArea = totalFloorAreaSqFt.value,
                paintRequired = 0.0,
                primerRequired = 0.0,
                paintCost = 0.0,
                primerCost = 0.0,
                materialsCost = grandTotal.value - laborCost.value,
                laborCost = laborCost.value,
                totalCost = grandTotal.value,
                laborRatePerUnit = laborRatePerSqFt.value.toDoubleOrNull() ?: 0.0
            )

            val details = ProjectDetails(
                project = project,
                rooms = roomDetailsList,
                paintSettings = PaintSettings(0, 0, 10.0, 1, 0.0, false, 10.0, 0),
                materials = savedMaterials,
                estimate = estimate
            )

            val savedId = useCases.saveProject(details)
            onSuccess(savedId)
        }
    }
}
