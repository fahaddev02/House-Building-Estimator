package com.example.paintcalculatorestimator.presentation.wall

import com.example.paintcalculatorestimator.ui.theme.EstimateTextField
import com.example.paintcalculatorestimator.ui.theme.CalculatorScreenPreview



import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.paintcalculatorestimator.domain.model.OpeningType
import com.example.paintcalculatorestimator.domain.model.UnitType
import com.example.paintcalculatorestimator.presentation.navigation.Screen
import androidx.compose.ui.window.Dialog
import java.util.Locale
import com.example.paintcalculatorestimator.utils.Validator
import com.example.paintcalculatorestimator.utils.ValidationResult

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WallCalculatorScreen(
    navController: NavController,
    viewModel: WallCalculatorViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val roomType by viewModel.roomType.collectAsState()
    val singleWidth by viewModel.singleWidth.collectAsState()
    val singleHeight by viewModel.singleHeight.collectAsState()
    val multipleWalls by viewModel.multipleWalls.collectAsState()
    val roomLength by viewModel.roomLength.collectAsState()
    val roomWidth by viewModel.roomWidth.collectAsState()
    val roomHeight by viewModel.roomHeight.collectAsState()
    val openings by viewModel.openings.collectAsState()

    // Construction state
    val wallThickness by viewModel.wallThickness.collectAsState()
    val materialType by viewModel.materialType.collectAsState()
    val pricePerBrick by viewModel.pricePerBrick.collectAsState()
    val pricePerCement by viewModel.pricePerCement.collectAsState()
    val pricePerSand by viewModel.pricePerSand.collectAsState()
    val laborWallCost by viewModel.laborWallCost.collectAsState()

    // Slab state
    val isSlabEnabled by viewModel.isSlabEnabled.collectAsState()
    val slabLength by viewModel.slabLength.collectAsState()
    val slabWidth by viewModel.slabWidth.collectAsState()
    val slabThickness by viewModel.slabThickness.collectAsState()
    val pricePerSteel by viewModel.pricePerSteel.collectAsState()
    val pricePerAggregate by viewModel.pricePerAggregate.collectAsState()
    val laborSlabCost by viewModel.laborSlabCost.collectAsState()

    // Washroom state
    val isWashroomEnabled by viewModel.isWashroomEnabled.collectAsState()
    val washroomLength by viewModel.washroomLength.collectAsState()
    val washroomWidth by viewModel.washroomWidth.collectAsState()
    val washroomHeight by viewModel.washroomHeight.collectAsState()
    val washroomThickness by viewModel.washroomThickness.collectAsState()
    val washroomMaterialType by viewModel.washroomMaterialType.collectAsState()
    val pricePerTile by viewModel.pricePerTile.collectAsState()
    val plumbingCost by viewModel.plumbingCost.collectAsState()
    val laborWashroomCost by viewModel.laborWashroomCost.collectAsState()
    val washroomOpenings by viewModel.washroomOpenings.collectAsState()

    // Unit settings
    val unitType by viewModel.unitType.collectAsState()
    val currency by viewModel.currency.collectAsState()
    val unitStr = if (unitType == UnitType.METRIC) "m" else "ft"
    val thicknessUnitStr = if (unitType == UnitType.METRIC) "cm" else "in"
    val areaUnitStr = if (unitType == UnitType.METRIC) "m²" else "ft²"
    val volUnitStr = if (unitType == UnitType.METRIC) "m³" else "cft"

    // Calculation outputs
    val grossArea by viewModel.grossArea.collectAsState()
    val deductionArea by viewModel.deductionArea.collectAsState()
    val paintableArea by viewModel.paintableArea.collectAsState()

    val roomBricksResult by viewModel.roomBricksResult.collectAsState()
    val roomCementResult by viewModel.roomCementResult.collectAsState()
    val roomSandResult by viewModel.roomSandResult.collectAsState()
    val roomMaterialCost by viewModel.roomMaterialCost.collectAsState()
    val roomLaborCost by viewModel.roomLaborCost.collectAsState()
    val roomTotalCost by viewModel.roomTotalCost.collectAsState()

    val slabVolumeResult by viewModel.slabVolumeResult.collectAsState()
    val slabCementResult by viewModel.slabCementResult.collectAsState()
    val slabSandResult by viewModel.slabSandResult.collectAsState()
    val slabAggregateResult by viewModel.slabAggregateResult.collectAsState()
    val slabSteelResult by viewModel.slabSteelResult.collectAsState()
    val slabMaterialCost by viewModel.slabMaterialCost.collectAsState()
    val slabLaborCost by viewModel.slabLaborCost.collectAsState()
    val slabTotalCost by viewModel.slabTotalCost.collectAsState()

    val washroomBricksResult by viewModel.washroomBricksResult.collectAsState()
    val washroomCementResult by viewModel.washroomCementResult.collectAsState()
    val washroomSandResult by viewModel.washroomSandResult.collectAsState()
    val washroomFloorArea by viewModel.washroomFloorArea.collectAsState()
    val washroomWallArea by viewModel.washroomWallArea.collectAsState()
    val washroomTilesResult by viewModel.washroomTilesResult.collectAsState()
    val washroomTilesCost by viewModel.washroomTilesCost.collectAsState()
    val washroomTileCementResult by viewModel.washroomTileCementResult.collectAsState()
    val washroomPlumbingCost by viewModel.washroomPlumbingCost.collectAsState()
    val washroomLaborCost by viewModel.washroomLaborCost.collectAsState()
    val washroomTotalCost by viewModel.washroomTotalCost.collectAsState()

    val grandTotal by viewModel.grandTotal.collectAsState()

    var showResultState by remember { mutableStateOf(false) }
    var showAddOpeningDialog by remember { mutableStateOf(false) }
    var showAddWashroomOpeningDialog by remember { mutableStateOf(false) }

    // Collapsible inputs sections
    var roomSectionExpanded by remember { mutableStateOf(true) }
    var pricingSectionExpanded by remember { mutableStateOf(false) }
    var slabSectionExpanded by remember { mutableStateOf(false) }
    var washroomSectionExpanded by remember { mutableStateOf(false) }

    // Validation state collection
    val singleWidthError by viewModel.singleWidthError.collectAsState()
    val singleHeightError by viewModel.singleHeightError.collectAsState()
    val roomLengthError by viewModel.roomLengthError.collectAsState()
    val roomWidthError by viewModel.roomWidthError.collectAsState()
    val roomHeightError by viewModel.roomHeightError.collectAsState()
    val wallThicknessError by viewModel.wallThicknessError.collectAsState()
    val multipleWallsErrors by viewModel.multipleWallsErrors.collectAsState()

    val pricePerBrickError by viewModel.pricePerBrickError.collectAsState()
    val pricePerCementError by viewModel.pricePerCementError.collectAsState()
    val pricePerSandError by viewModel.pricePerSandError.collectAsState()
    val laborWallCostError by viewModel.laborWallCostError.collectAsState()

    val slabLengthError by viewModel.slabLengthError.collectAsState()
    val slabWidthError by viewModel.slabWidthError.collectAsState()
    val slabThicknessError by viewModel.slabThicknessError.collectAsState()
    val pricePerSteelError by viewModel.pricePerSteelError.collectAsState()
    val pricePerAggregateError by viewModel.pricePerAggregateError.collectAsState()
    val laborSlabCostError by viewModel.laborSlabCostError.collectAsState()

    val washroomLengthError by viewModel.washroomLengthError.collectAsState()
    val washroomWidthError by viewModel.washroomWidthError.collectAsState()
    val washroomHeightError by viewModel.washroomHeightError.collectAsState()
    val washroomThicknessError by viewModel.washroomThicknessError.collectAsState()
    val pricePerTileError by viewModel.pricePerTileError.collectAsState()
    val plumbingCostError by viewModel.plumbingCostError.collectAsState()
    val laborWashroomCostError by viewModel.laborWashroomCostError.collectAsState()

    val focusRequesters = remember { mutableMapOf<String, FocusRequester>() }
    fun getFocusRequester(key: String): FocusRequester {
        return focusRequesters.getOrPut(key) { FocusRequester() }
    }

    LaunchedEffect(Unit) {
        viewModel.firstInvalidField.collect { fieldId ->
            if (fieldId.startsWith("slab")) {
                slabSectionExpanded = true
            } else if (fieldId.startsWith("washroom")) {
                washroomSectionExpanded = true
            } else if (fieldId.startsWith("price") || fieldId.startsWith("labor")) {
                pricingSectionExpanded = true
            }
            kotlinx.coroutines.delay(100)
            focusRequesters[fieldId]?.requestFocus()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Complete Room Estimator", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = { com.example.paintcalculatorestimator.adsManager.NativeAdView() }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (!showResultState) {
                // Inputs Screen
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item { Spacer(modifier = Modifier.height(8.dp)) }

                    // --- SECTION 1: ROOM WALLS & DIMENSIONS ---
                    item {
                        OutlinedCard(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().clickable { roomSectionExpanded = !roomSectionExpanded },
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("1. Room Walls & Dimensions", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                                    Text(if (roomSectionExpanded) "Hide" else "Show", color = MaterialTheme.colorScheme.secondary, fontSize = 13.sp)
                                }

                                if (roomSectionExpanded) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    TabRow(
                                        selectedTabIndex = when (roomType) {
                                            "Single Wall" -> 0
                                            "Multiple Walls" -> 1
                                            else -> 2
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Tab(
                                            selected = roomType == "Single Wall",
                                            onClick = { viewModel.roomType.value = "Single Wall" },
                                            text = { Text("Single Wall", fontSize = 11.sp) }
                                        )
                                        Tab(
                                            selected = roomType == "Multiple Walls",
                                            onClick = { viewModel.roomType.value = "Multiple Walls" },
                                            text = { Text("Multiple Walls", fontSize = 11.sp) }
                                        )
                                        Tab(
                                            selected = roomType == "Complete Room",
                                            onClick = { viewModel.roomType.value = "Complete Room" },
                                            text = { Text("Complete Room", fontSize = 11.sp) }
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    when (roomType) {
                                        "Single Wall" -> {
                                            EstimateTextField(
                                                value = singleWidth,
                                                onValueChange = { viewModel.singleWidth.value = it },
                                                label = { Text("Wall Width ($unitStr)") },
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                modifier = Modifier.fillMaxWidth().focusRequester(getFocusRequester("singleWidth")),
                                                singleLine = true,
                                                isError = singleWidthError != null,
                                                supportingText = if (singleWidthError != null) { { Text(singleWidthError!!) } } else null
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            EstimateTextField(
                                                value = singleHeight,
                                                onValueChange = { viewModel.singleHeight.value = it },
                                                label = { Text("Wall Height ($unitStr)") },
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                modifier = Modifier.fillMaxWidth().focusRequester(getFocusRequester("singleHeight")),
                                                singleLine = true,
                                                isError = singleHeightError != null,
                                                supportingText = if (singleHeightError != null) { { Text(singleHeightError!!) } } else null
                                            )
                                        }
                                        "Multiple Walls" -> {
                                            multipleWalls.forEachIndexed { index, wall ->
                                                val wallErr = multipleWallsErrors.getOrNull(index)
                                                Row(
                                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    EstimateTextField(
                                                        value = wall.width,
                                                        onValueChange = { viewModel.updateMultipleWall(index, it, wall.height) },
                                                        label = { Text("${wall.name} W ($unitStr)") },
                                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                        modifier = Modifier.weight(1f).focusRequester(getFocusRequester("wall_width_$index")),
                                                        singleLine = true,
                                                        isError = wallErr?.widthError != null,
                                                        supportingText = if (wallErr?.widthError != null) { { Text(wallErr.widthError) } } else null
                                                    )
                                                    EstimateTextField(
                                                        value = wall.height,
                                                        onValueChange = { viewModel.updateMultipleWall(index, wall.width, it) },
                                                        label = { Text("${wall.name} H ($unitStr)") },
                                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                        modifier = Modifier.weight(1f).focusRequester(getFocusRequester("wall_height_$index")),
                                                        singleLine = true,
                                                        isError = wallErr?.heightError != null,
                                                        supportingText = if (wallErr?.heightError != null) { { Text(wallErr.heightError) } } else null
                                                    )
                                                    if (multipleWalls.size > 1) {
                                                        IconButton(onClick = { viewModel.removeMultipleWall(index) }) {
                                                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                                        }
                                                    }
                                                }
                                            }
                                            TextButton(onClick = { viewModel.addMultipleWall() }) {
                                                Text("+ Add Another Wall")
                                            }
                                        }
                                        "Complete Room" -> {
                                            EstimateTextField(
                                                value = roomLength,
                                                onValueChange = { viewModel.roomLength.value = it },
                                                label = { Text("Room Length ($unitStr)") },
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                modifier = Modifier.fillMaxWidth().focusRequester(getFocusRequester("roomLength")),
                                                singleLine = true,
                                                isError = roomLengthError != null,
                                                supportingText = if (roomLengthError != null) { { Text(roomLengthError!!) } } else null
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            EstimateTextField(
                                                value = roomWidth,
                                                onValueChange = { viewModel.roomWidth.value = it },
                                                label = { Text("Room Width ($unitStr)") },
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                modifier = Modifier.fillMaxWidth().focusRequester(getFocusRequester("roomWidth")),
                                                singleLine = true,
                                                isError = roomWidthError != null,
                                                supportingText = if (roomWidthError != null) { { Text(roomWidthError!!) } } else null
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            EstimateTextField(
                                                value = roomHeight,
                                                onValueChange = { viewModel.roomHeight.value = it },
                                                label = { Text("Wall Height ($unitStr)") },
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                modifier = Modifier.fillMaxWidth().focusRequester(getFocusRequester("roomHeight")),
                                                singleLine = true,
                                                isError = roomHeightError != null,
                                                supportingText = if (roomHeightError != null) { { Text(roomHeightError!!) } } else null
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))
                                    EstimateTextField(
                                        value = wallThickness,
                                        onValueChange = { viewModel.wallThickness.value = it },
                                        label = { Text("Wall Thickness ($thicknessUnitStr)") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.fillMaxWidth().focusRequester(getFocusRequester("wallThickness")),
                                        singleLine = true,
                                        isError = wallThicknessError != null,
                                        supportingText = if (wallThicknessError != null) { { Text(wallThicknessError!!) } } else null
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text("Wall Construction Material", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            RadioButton(selected = materialType == "Brick", onClick = { viewModel.materialType.value = "Brick" })
                                            Text("Brick", modifier = Modifier.clickable { viewModel.materialType.value = "Brick" })
                                        }
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            RadioButton(selected = materialType == "Block", onClick = { viewModel.materialType.value = "Block" })
                                            Text("Concrete Block", modifier = Modifier.clickable { viewModel.materialType.value = "Block" })
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Doors & Windows Deductions", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                        Button(
                                            onClick = { showAddOpeningDialog = true },
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer)
                                        ) {
                                            Text("Add")
                                        }
                                    }

                                    if (openings.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        openings.forEachIndexed { idx, op ->
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text("- ${op.type.name.lowercase().replaceFirstChar { it.uppercase() }}: ${op.width}x${op.height}m (Qty ${op.quantity})", fontSize = 13.sp)
                                                IconButton(onClick = { viewModel.removeOpening(idx) }) {
                                                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // --- SECTION 2: CONSTRUCTION MATERIAL PRICES ---
                    item {
                        OutlinedCard(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().clickable { pricingSectionExpanded = !pricingSectionExpanded },
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("2. Material & Labor Rates", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                                    Text(if (pricingSectionExpanded) "Hide" else "Show", color = MaterialTheme.colorScheme.secondary, fontSize = 13.sp)
                                }

                                if (pricingSectionExpanded) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    EstimateTextField(
                                        value = pricePerBrick,
                                        onValueChange = { viewModel.pricePerBrick.value = it },
                                        label = { Text("Price per ${if (materialType == "Brick") "Brick" else "Block"} ($currency)") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.fillMaxWidth().focusRequester(getFocusRequester("pricePerBrick")),
                                        singleLine = true,
                                        isError = pricePerBrickError != null,
                                        supportingText = if (pricePerBrickError != null) { { Text(pricePerBrickError!!) } } else null
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    EstimateTextField(
                                        value = pricePerCement,
                                        onValueChange = { viewModel.pricePerCement.value = it },
                                        label = { Text("Price per Cement Bag ($currency)") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.fillMaxWidth().focusRequester(getFocusRequester("pricePerCement")),
                                        singleLine = true,
                                        isError = pricePerCementError != null,
                                        supportingText = if (pricePerCementError != null) { { Text(pricePerCementError!!) } } else null
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    EstimateTextField(
                                        value = pricePerSand,
                                        onValueChange = { viewModel.pricePerSand.value = it },
                                        label = { Text("Price per Sand unit ($currency / $volUnitStr)") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.fillMaxWidth().focusRequester(getFocusRequester("pricePerSand")),
                                        singleLine = true,
                                        isError = pricePerSandError != null,
                                        supportingText = if (pricePerSandError != null) { { Text(pricePerSandError!!) } } else null
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    EstimateTextField(
                                        value = laborWallCost,
                                        onValueChange = { viewModel.laborWallCost.value = it },
                                        label = { Text("Walls Labor Cost ($currency)") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.fillMaxWidth().focusRequester(getFocusRequester("laborWallCost")),
                                        singleLine = true,
                                        isError = laborWallCostError != null,
                                        supportingText = if (laborWallCostError != null) { { Text(laborWallCostError!!) } } else null
                                    )
                                }
                            }
                        }
                    }

                    // --- SECTION 3: CONCRETE ROOF / SLAB (CHHAT) ---
                    item {
                        OutlinedCard(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Checkbox(checked = isSlabEnabled, onCheckedChange = { viewModel.isSlabEnabled.value = it })
                                        Text("3. Concrete Roof / Slab", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary, modifier = Modifier.clickable { viewModel.isSlabEnabled.value = !isSlabEnabled })
                                    }
                                    if (isSlabEnabled) {
                                        Text(if (slabSectionExpanded) "Hide" else "Show", color = MaterialTheme.colorScheme.secondary, modifier = Modifier.clickable { slabSectionExpanded = !slabSectionExpanded }, fontSize = 13.sp)
                                    }
                                }

                                if (isSlabEnabled && slabSectionExpanded) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    EstimateTextField(
                                        value = slabLength,
                                        onValueChange = { viewModel.slabLength.value = it },
                                        label = { Text("Slab Length ($unitStr)") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.fillMaxWidth().focusRequester(getFocusRequester("slabLength")),
                                        singleLine = true,
                                        isError = slabLengthError != null,
                                        supportingText = if (slabLengthError != null) { { Text(slabLengthError!!) } } else null
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    EstimateTextField(
                                        value = slabWidth,
                                        onValueChange = { viewModel.slabWidth.value = it },
                                        label = { Text("Slab Width ($unitStr)") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.fillMaxWidth().focusRequester(getFocusRequester("slabWidth")),
                                        singleLine = true,
                                        isError = slabWidthError != null,
                                        supportingText = if (slabWidthError != null) { { Text(slabWidthError!!) } } else null
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    EstimateTextField(
                                        value = slabThickness,
                                        onValueChange = { viewModel.slabThickness.value = it },
                                        label = { Text("Slab Thickness ($thicknessUnitStr)") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.fillMaxWidth().focusRequester(getFocusRequester("slabThickness")),
                                        singleLine = true,
                                        isError = slabThicknessError != null,
                                        supportingText = if (slabThicknessError != null) { { Text(slabThicknessError!!) } } else null
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    EstimateTextField(
                                        value = pricePerSteel,
                                        onValueChange = { viewModel.pricePerSteel.value = it },
                                        label = { Text("Price per Steel/Rebar kg ($currency)") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.fillMaxWidth().focusRequester(getFocusRequester("pricePerSteel")),
                                        singleLine = true,
                                        isError = pricePerSteelError != null,
                                        supportingText = if (pricePerSteelError != null) { { Text(pricePerSteelError!!) } } else null
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    EstimateTextField(
                                        value = pricePerAggregate,
                                        onValueChange = { viewModel.pricePerAggregate.value = it },
                                        label = { Text("Price per Aggregate/Crush unit ($currency / $volUnitStr)") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.fillMaxWidth().focusRequester(getFocusRequester("pricePerAggregate")),
                                        singleLine = true,
                                        isError = pricePerAggregateError != null,
                                        supportingText = if (pricePerAggregateError != null) { { Text(pricePerAggregateError!!) } } else null
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    EstimateTextField(
                                        value = laborSlabCost,
                                        onValueChange = { viewModel.laborSlabCost.value = it },
                                        label = { Text("Slab Labor Cost ($currency)") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.fillMaxWidth().focusRequester(getFocusRequester("laborSlabCost")),
                                        singleLine = true,
                                        isError = laborSlabCostError != null,
                                        supportingText = if (laborSlabCostError != null) { { Text(laborSlabCostError!!) } } else null
                                    )
                                }
                            }
                        }
                    }

                    // --- SECTION 4: WASHROOM INSIDE ROOM (OPTIONAL) ---
                    item {
                        OutlinedCard(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Checkbox(checked = isWashroomEnabled, onCheckedChange = { viewModel.isWashroomEnabled.value = it })
                                        Text("4. Washroom Inside Room", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary, modifier = Modifier.clickable { viewModel.isWashroomEnabled.value = !isWashroomEnabled })
                                    }
                                    if (isWashroomEnabled) {
                                        Text(if (washroomSectionExpanded) "Hide" else "Show", color = MaterialTheme.colorScheme.secondary, modifier = Modifier.clickable { washroomSectionExpanded = !washroomSectionExpanded }, fontSize = 13.sp)
                                    }
                                }

                                if (isWashroomEnabled && washroomSectionExpanded) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    EstimateTextField(
                                        value = washroomLength,
                                        onValueChange = { viewModel.washroomLength.value = it },
                                        label = { Text("Washroom Length ($unitStr)") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.fillMaxWidth().focusRequester(getFocusRequester("washroomLength")),
                                        singleLine = true,
                                        isError = washroomLengthError != null,
                                        supportingText = if (washroomLengthError != null) { { Text(washroomLengthError!!) } } else null
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    EstimateTextField(
                                        value = washroomWidth,
                                        onValueChange = { viewModel.washroomWidth.value = it },
                                        label = { Text("Washroom Width ($unitStr)") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.fillMaxWidth().focusRequester(getFocusRequester("washroomWidth")),
                                        singleLine = true,
                                        isError = washroomWidthError != null,
                                        supportingText = if (washroomWidthError != null) { { Text(washroomWidthError!!) } } else null
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    EstimateTextField(
                                        value = washroomHeight,
                                        onValueChange = { viewModel.washroomHeight.value = it },
                                        label = { Text("Washroom Height ($unitStr)") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.fillMaxWidth().focusRequester(getFocusRequester("washroomHeight")),
                                        singleLine = true,
                                        isError = washroomHeightError != null,
                                        supportingText = if (washroomHeightError != null) { { Text(washroomHeightError!!) } } else null
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    EstimateTextField(
                                        value = washroomThickness,
                                        onValueChange = { viewModel.washroomThickness.value = it },
                                        label = { Text("Washroom Wall Thickness ($thicknessUnitStr)") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.fillMaxWidth().focusRequester(getFocusRequester("washroomThickness")),
                                        singleLine = true,
                                        isError = washroomThicknessError != null,
                                        supportingText = if (washroomThicknessError != null) { { Text(washroomThicknessError!!) } } else null
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Washroom Material Type", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            RadioButton(selected = washroomMaterialType == "Brick", onClick = { viewModel.washroomMaterialType.value = "Brick" })
                                            Text("Brick", fontSize = 13.sp, modifier = Modifier.clickable { viewModel.washroomMaterialType.value = "Brick" })
                                        }
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            RadioButton(selected = washroomMaterialType == "Block", onClick = { viewModel.washroomMaterialType.value = "Block" })
                                            Text("Concrete Block", fontSize = 13.sp, modifier = Modifier.clickable { viewModel.washroomMaterialType.value = "Block" })
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                    EstimateTextField(
                                        value = pricePerTile,
                                        onValueChange = { viewModel.pricePerTile.value = it },
                                        label = { Text("Price per Tile Area unit ($currency / $areaUnitStr)") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.fillMaxWidth().focusRequester(getFocusRequester("pricePerTile")),
                                        singleLine = true,
                                        isError = pricePerTileError != null,
                                        supportingText = if (pricePerTileError != null) { { Text(pricePerTileError!!) } } else null
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    EstimateTextField(
                                        value = plumbingCost,
                                        onValueChange = { viewModel.plumbingCost.value = it },
                                        label = { Text("Plumbing/Fixtures Total Cost ($currency)") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.fillMaxWidth().focusRequester(getFocusRequester("plumbingCost")),
                                        singleLine = true,
                                        isError = plumbingCostError != null,
                                        supportingText = if (plumbingCostError != null) { { Text(plumbingCostError!!) } } else null
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    EstimateTextField(
                                        value = laborWashroomCost,
                                        onValueChange = { viewModel.laborWashroomCost.value = it },
                                        label = { Text("Washroom Labor Cost ($currency)") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.fillMaxWidth().focusRequester(getFocusRequester("laborWashroomCost")),
                                        singleLine = true,
                                        isError = laborWashroomCostError != null,
                                        supportingText = if (laborWashroomCostError != null) { { Text(laborWashroomCostError!!) } } else null
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Washroom Door/Vent Deductions", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                        Button(
                                            onClick = { showAddWashroomOpeningDialog = true },
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer)
                                        ) {
                                            Text("Add")
                                        }
                                    }

                                    if (washroomOpenings.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        washroomOpenings.forEachIndexed { idx, op ->
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text("- ${op.type.name.lowercase().replaceFirstChar { it.uppercase() }}: ${op.width}x${op.height}m (Qty ${op.quantity})", fontSize = 12.sp)
                                                IconButton(onClick = { viewModel.removeWashroomOpening(idx) }) {
                                                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Calculate Button
                    item {
                        Button(
                            onClick = {
                                if (viewModel.validateWallInputs()) {
                                    viewModel.calculateAll()
                                    showResultState = true
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(56.dp).padding(vertical = 4.dp),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Text("Calculate Room Construction Cost", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    item { Spacer(modifier = Modifier.height(32.dp)) }
                }
            } else {
                // Results Screen
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item { Spacer(modifier = Modifier.height(8.dp)) }

                    item {
                        Text(text = "Construction Estimate Summary", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }

                    // 1. Room Walls Card
                    item {
                        OutlinedCard(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Room Walls Construction", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.primary)
                                Divider()
                                val unitLabel = if (materialType == "Brick") "Bricks" else "Blocks"
                                ResultRow(label = "Required $unitLabel", value = "$roomBricksResult pcs")
                                ResultRow(label = "Required Cement", value = String.format(Locale.US, "%.1f Bags", roomCementResult))
                                ResultRow(label = "Required Sand", value = String.format(Locale.US, "%.2f %s", roomSandResult, volUnitStr))
                                ResultRow(label = "Material Cost", value = "$currency ${String.format(Locale.US, "%,.2f", roomMaterialCost)}")
                                ResultRow(label = "Labor Cost", value = "$currency ${String.format(Locale.US, "%,.2f", roomLaborCost)}")
                                Divider()
                                ResultRow(label = "Walls Total", value = "$currency ${String.format(Locale.US, "%,.2f", roomTotalCost)}", isBold = true)
                            }
                        }
                    }

                    // 2. Concrete Slab Card
                    if (isSlabEnabled) {
                        item {
                            OutlinedCard(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("Roof Slab Concrete & Steel", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.primary)
                                    Divider()
                                    ResultRow(label = "Slab Volume", value = String.format(Locale.US, "%.2f %s", slabVolumeResult, volUnitStr))
                                    ResultRow(label = "Cement Bags", value = String.format(Locale.US, "%.1f Bags", slabCementResult))
                                    ResultRow(label = "Sand Volume", value = String.format(Locale.US, "%.2f %s", slabSandResult, volUnitStr))
                                    ResultRow(label = "Aggregate Volume", value = String.format(Locale.US, "%.2f %s", slabAggregateResult, volUnitStr))
                                    ResultRow(label = "Steel Rebar Weight", value = String.format(Locale.US, "%.1f kg", slabSteelResult))
                                    ResultRow(label = "Material Cost", value = "$currency ${String.format(Locale.US, "%,.2f", slabMaterialCost)}")
                                    ResultRow(label = "Labor Cost", value = "$currency ${String.format(Locale.US, "%,.2f", slabLaborCost)}")
                                    Divider()
                                    ResultRow(label = "Roof Total", value = "$currency ${String.format(Locale.US, "%,.2f", slabTotalCost)}", isBold = true)
                                }
                            }
                        }
                    }

                    // 3. Washroom Card
                    if (isWashroomEnabled) {
                        item {
                            OutlinedCard(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("Washroom Details", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.primary)
                                    Divider()
                                    val washUnitLabel = if (washroomMaterialType == "Brick") "Bricks" else "Blocks"
                                    ResultRow(label = "Wall $washUnitLabel", value = "$washroomBricksResult pcs")
                                    ResultRow(label = "Cement Bags (Walls & Tiles)", value = String.format(Locale.US, "%.1f Bags", washroomCementResult + washroomTileCementResult))
                                    ResultRow(label = "Sand Volume", value = String.format(Locale.US, "%.2f %s", washroomSandResult, volUnitStr))
                                    ResultRow(label = "Tile Area Required", value = String.format(Locale.US, "%.2f %s", washroomTilesResult, areaUnitStr))
                                    ResultRow(label = "Tiles Cost", value = "$currency ${String.format(Locale.US, "%,.2f", washroomTilesCost)}")
                                    ResultRow(label = "Plumbing Package Cost", value = "$currency ${String.format(Locale.US, "%,.2f", washroomPlumbingCost)}")


                                    ResultRow(label = "Labor Cost", value = "$currency ${String.format(Locale.US, "%,.2f", washroomLaborCost)}")
                                    Divider()
                                    ResultRow(label = "Washroom Total", value = "$currency ${String.format(Locale.US, "%,.2f", washroomTotalCost)}", isBold = true)
                                }
                            }
                        }
                    }

                    // 4. Grand Total prominent card
                    item {
                        OutlinedCard(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                        ) {
                            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Grand Total Cost", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                    Text("$currency ${String.format(Locale.US, "%,.2f", grandTotal)}", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }

                    // Actions
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { showResultState = false },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Recalculate")
                            }

                            Button(
                                onClick = {
                                    viewModel.saveResult { savedId ->
                                        Toast.makeText(context, "Calculation saved successfully!", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Save Result")
                            }

                            IconButton(
                                onClick = {
                                    var txt = "Painting/Construction Cost Estimate Summary:\n" +
                                            "- Room Walls: $currency ${String.format(Locale.US, "%,.2f", roomTotalCost)}\n"
                                    if (isSlabEnabled) {
                                        txt += "- Roof Slab: $currency ${String.format(Locale.US, "%,.2f", slabTotalCost)}\n"
                                    }
                                    if (isWashroomEnabled) {
                                        txt += "- Washroom: $currency ${String.format(Locale.US, "%,.2f", washroomTotalCost)}\n"
                                    }
                                    txt += "- Grand Total: $currency ${String.format(Locale.US, "%,.2f", grandTotal)}\n" +
                                            "Calculated using Paint Calculator App."
                                    shareText(context, txt)
                                },
                                modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant, shape = MaterialTheme.shapes.medium)
                            ) {
                                Icon(imageVector = Icons.Default.Share, contentDescription = "Share text")
                            }
                        }
                    }

                    // Calculate Paint Navigation
                    item {
                        Button(
                            onClick = {
                                navController.navigate(Screen.PaintCalculatorScreen.createRoute(paintableArea))
                            },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Text("Calculate Paint for Room", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    item { Spacer(modifier = Modifier.height(32.dp)) }
                }
            }
        }
    }

    if (showAddOpeningDialog) {
        AddOpeningDialog(
            unitStr = unitStr,
            onDismiss = { showAddOpeningDialog = false },
            onAdd = { type, w, h, qty ->
                viewModel.addOpening(type, w, h, qty)
                showAddOpeningDialog = false
            }
        )
    }

    if (showAddWashroomOpeningDialog) {
        AddOpeningDialog(
            unitStr = unitStr,
            onDismiss = { showAddWashroomOpeningDialog = false },
            onAdd = { type, w, h, qty ->
                viewModel.addWashroomOpening(type, w, h, qty)
                showAddWashroomOpeningDialog = false
            }
        )
    }
}

@Composable
fun ResultRow(
    label: String,
    value: String,
    isHighlighted: Boolean = false,
    isBold: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val fontWeight = if (isHighlighted || isBold) FontWeight.Bold else FontWeight.Normal
        val color = if (isHighlighted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = fontWeight,
            color = color
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
fun AddOpeningDialog(
    unitStr: String,
    onDismiss: () -> Unit,
    onAdd: (OpeningType, Double, Double, Int) -> Unit
) {
    var type by remember { mutableStateOf(OpeningType.DOOR) }
    var width by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("1") }

    var widthError by remember { mutableStateOf<String?>(null) }
    var heightError by remember { mutableStateOf<String?>(null) }
    var quantityError by remember { mutableStateOf<String?>(null) }

    val focusRequesters = remember { List(3) { FocusRequester() } }
    var isValidationActive by remember { mutableStateOf(false) }

    LaunchedEffect(width) {
        if (isValidationActive) {
            val res = Validator.validatePositiveNumber(width, "width")
            widthError = if (res is ValidationResult.Invalid) res.message else null
        }
    }
    LaunchedEffect(height) {
        if (isValidationActive) {
            val res = Validator.validatePositiveNumber(height, "height")
            heightError = if (res is ValidationResult.Invalid) res.message else null
        }
    }
    LaunchedEffect(quantity) {
        if (isValidationActive) {
            val res = Validator.validateInteger(quantity, "quantity")
            quantityError = if (res is ValidationResult.Invalid) res.message else null
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        OutlinedCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = MaterialTheme.shapes.large
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(text = "Add Door/Window Deduction", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                
                // Select type
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = type == OpeningType.DOOR,
                        onClick = { type = OpeningType.DOOR },
                        label = { Text("Door") }
                    )
                    FilterChip(
                        selected = type == OpeningType.WINDOW,
                        onClick = { type = OpeningType.WINDOW },
                        label = { Text("Window") }
                    )
                    FilterChip(
                        selected = type == OpeningType.CUSTOM,
                        onClick = { type = OpeningType.CUSTOM },
                        label = { Text("Custom") }
                    )
                }

                EstimateTextField(
                    value = width,
                    onValueChange = { width = it },
                    label = { Text("Width ($unitStr)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().focusRequester(focusRequesters[0]),
                    singleLine = true,
                    isError = widthError != null,
                    supportingText = if (widthError != null) { { Text(widthError!!) } } else null
                )

                EstimateTextField(
                    value = height,
                    onValueChange = { height = it },
                    label = { Text("Height ($unitStr)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().focusRequester(focusRequesters[1]),
                    singleLine = true,
                    isError = heightError != null,
                    supportingText = if (heightError != null) { { Text(heightError!!) } } else null
                )

                EstimateTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Quantity") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().focusRequester(focusRequesters[2]),
                    singleLine = true,
                    isError = quantityError != null,
                    supportingText = if (quantityError != null) { { Text(quantityError!!) } } else null
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            isValidationActive = true
                            val wRes = Validator.validatePositiveNumber(width, "width")
                            val hRes = Validator.validatePositiveNumber(height, "height")
                            val qRes = Validator.validateInteger(quantity, "quantity")

                            widthError = if (wRes is ValidationResult.Invalid) wRes.message else null
                            heightError = if (hRes is ValidationResult.Invalid) hRes.message else null
                            quantityError = if (qRes is ValidationResult.Invalid) qRes.message else null

                            if (wRes is ValidationResult.Invalid) {
                                focusRequesters[0].requestFocus()
                            } else if (hRes is ValidationResult.Invalid) {
                                focusRequesters[1].requestFocus()
                            } else if (qRes is ValidationResult.Invalid) {
                                focusRequesters[2].requestFocus()
                            } else {
                                val w = width.toDouble()
                                val h = height.toDouble()
                                val q = quantity.toInt()
                                onAdd(type, w, h, q)
                            }
                        }
                    ) {
                        Text("Add")
                    }
                }
            }
        }
    }
}

private fun shareText(context: Context, text: String) {
    val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(android.content.Intent.EXTRA_TEXT, text)
    }
    context.startActivity(android.content.Intent.createChooser(intent, "Share Cost Estimate"))
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, name = "Wall calculator")
@Composable
private fun WallCalculatorScreenPreview() {
    com.example.paintcalculatorestimator.ui.theme.PaintCalculatorEstimatorTheme {
        CalculatorScreenPreview("Wall Calculator", "Calculate wall area, openings and material quantities.", listOf("Wall width", "Wall height", "Material price"), "Construction estimate summary")
    }
}
