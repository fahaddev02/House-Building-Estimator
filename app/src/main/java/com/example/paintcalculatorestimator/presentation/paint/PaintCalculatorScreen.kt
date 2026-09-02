package com.example.paintcalculatorestimator.presentation.paint

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import com.example.paintcalculatorestimator.domain.model.UnitType
import com.example.paintcalculatorestimator.presentation.navigation.Screen
import com.example.paintcalculatorestimator.presentation.wall.ResultRow
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaintCalculatorScreen(
    navController: NavController,
    viewModel: PaintCalculatorViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val optionType by viewModel.optionType.collectAsState()
    val manualArea by viewModel.manualArea.collectAsState()
    val calculationMode by viewModel.calculationMode.collectAsState()
    val dimLength by viewModel.dimLength.collectAsState()
    val dimWidth by viewModel.dimWidth.collectAsState()
    val dimHeight by viewModel.dimHeight.collectAsState()
    val dimWalls by viewModel.dimWalls.collectAsState()

    val coveragePreset by viewModel.coveragePreset.collectAsState()
    val customCoverage by viewModel.customCoverage.collectAsState()
    
    val coatsPreset by viewModel.coatsPreset.collectAsState()
    val customCoats by viewModel.customCoats.collectAsState()
    
    val wastagePreset by viewModel.wastagePreset.collectAsState()
    val customWastage by viewModel.customWastage.collectAsState()

    val surfaceAreaResult by viewModel.surfaceAreaResult.collectAsState()
    val basePaintRequired by viewModel.basePaintRequired.collectAsState()
    val recommendedPaint by viewModel.recommendedPaint.collectAsState()

    val unitType by viewModel.unitType.collectAsState()

    var showResultState by remember { mutableStateOf(false) }

    val unitStr = if (unitType == UnitType.METRIC) "m" else "ft"
    val areaUnitStr = if (unitType == UnitType.METRIC) "m²" else "ft²"
    val volUnitStr = if (unitType == UnitType.METRIC) "L" else "gal"
    val covUnitStr = if (unitType == UnitType.METRIC) "m²/L" else "ft²/gal"

    val manualAreaError by viewModel.manualAreaError.collectAsState()
    val dimLengthError by viewModel.dimLengthError.collectAsState()
    val dimWidthError by viewModel.dimWidthError.collectAsState()
    val dimHeightError by viewModel.dimHeightError.collectAsState()
    val dimWallsError by viewModel.dimWallsError.collectAsState()

    val customCoverageError by viewModel.customCoverageError.collectAsState()
    val customCoatsError by viewModel.customCoatsError.collectAsState()
    val customWastageError by viewModel.customWastageError.collectAsState()

    val focusRequesters = remember { mutableMapOf<String, FocusRequester>() }
    fun getFocusRequester(key: String): FocusRequester {
        return focusRequesters.getOrPut(key) { FocusRequester() }
    }

    LaunchedEffect(Unit) {
        viewModel.firstInvalidField.collect { fieldId ->
            focusRequesters[fieldId]?.requestFocus()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Paint Calculator", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (!showResultState) {
                // Inputs View
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item { Spacer(modifier = Modifier.height(8.dp)) }

                    // Area Input Type Tab Selector
                    item {
                        Column {
                            Text(text = "Choose Area Method", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            TabRow(
                                selectedTabIndex = if (optionType == "Option A") 0 else 1,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Tab(
                                    selected = optionType == "Option A",
                                    onClick = { viewModel.optionType.value = "Option A" },
                                    text = { Text("Enter Area", fontSize = 12.sp) }
                                )
                                Tab(
                                    selected = optionType == "Option B",
                                    onClick = { viewModel.optionType.value = "Option B" },
                                    text = { Text("Calculate Area", fontSize = 12.sp) }
                                )
                            }
                        }
                    }
                    if (optionType == "Option A") {
                        // Option A Form
                        item {
                            OutlinedTextField(
                                value = manualArea,
                                onValueChange = { viewModel.manualArea.value = it },
                                label = { Text("Surface Area ($areaUnitStr)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth().focusRequester(getFocusRequester("manualArea")),
                                singleLine = true,
                                isError = manualAreaError != null,
                                supportingText = if (manualAreaError != null) { { Text(manualAreaError!!) } } else null
                            )
                        }
                    } else {
                        // Option B Form (Calculate Area)
                        item {
                            Column {
                                Text(text = "Select Space Type", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    FilterChip(
                                        selected = calculationMode == "Wall Area",
                                        onClick = { viewModel.calculationMode.value = "Wall Area" },
                                        label = { Text("Walls Only") }
                                    )
                                    FilterChip(
                                        selected = calculationMode == "Ceiling Area",
                                        onClick = { viewModel.calculationMode.value = "Ceiling Area" },
                                        label = { Text("Ceiling Only") }
                                    )
                                    FilterChip(
                                        selected = calculationMode == "Both",
                                        onClick = { viewModel.calculationMode.value = "Both" },
                                        label = { Text("Walls & Ceiling") }
                                    )
                                }
                            }
                        }

                        item {
                            OutlinedTextField(
                                value = dimLength,
                                onValueChange = { viewModel.dimLength.value = it },
                                label = { Text("Length ($unitStr)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth().focusRequester(getFocusRequester("dimLength")),
                                singleLine = true,
                                isError = dimLengthError != null,
                                supportingText = if (dimLengthError != null) { { Text(dimLengthError!!) } } else null
                            )
                        }

                        item {
                            OutlinedTextField(
                                value = dimWidth,
                                onValueChange = { viewModel.dimWidth.value = it },
                                label = { Text("Width ($unitStr)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth().focusRequester(getFocusRequester("dimWidth")),
                                singleLine = true,
                                isError = dimWidthError != null,
                                supportingText = if (dimWidthError != null) { { Text(dimWidthError!!) } } else null
                            )
                        }

                        if (calculationMode != "Ceiling Area") {
                            item {
                                OutlinedTextField(
                                    value = dimHeight,
                                    onValueChange = { viewModel.dimHeight.value = it },
                                    label = { Text("Wall Height ($unitStr)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.fillMaxWidth().focusRequester(getFocusRequester("dimHeight")),
                                    singleLine = true,
                                    isError = dimHeightError != null,
                                    supportingText = if (dimHeightError != null) { { Text(dimHeightError!!) } } else null
                                )
                            }
                            item {
                                OutlinedTextField(
                                    value = dimWalls,
                                    onValueChange = { viewModel.dimWalls.value = it },
                                    label = { Text("Number of Walls") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.fillMaxWidth().focusRequester(getFocusRequester("dimWalls")),
                                    singleLine = true,
                                    isError = dimWallsError != null,
                                    supportingText = if (dimWallsError != null) { { Text(dimWallsError!!) } } else null
                                )
                            }
                        }
                    }

                    // Divider
                    item { Divider() }

                    // Coverage Settings
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(text = "Paint Coverage ($covUnitStr)", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf("8", "10", "12", "Custom").forEach { preset ->
                                    val isSelected = coveragePreset == preset
                                    ElevatedFilterChip(
                                        selected = isSelected,
                                        onClick = { viewModel.coveragePreset.value = preset },
                                        label = { Text(if (preset == "Custom") "Custom" else "$preset $volUnitStr") }
                                    )
                                }
                            }
                            if (coveragePreset == "Custom") {
                                OutlinedTextField(
                                    value = customCoverage,
                                    onValueChange = { viewModel.customCoverage.value = it },
                                    label = { Text("Custom Coverage ($covUnitStr)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.fillMaxWidth().focusRequester(getFocusRequester("customCoverage")),
                                    singleLine = true,
                                    isError = customCoverageError != null,
                                    supportingText = if (customCoverageError != null) { { Text(customCoverageError!!) } } else null
                                )
                            }
                        }
                    }

                    // Coats Settings
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(text = "Number of Coats", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf("1", "2", "3", "Custom").forEach { preset ->
                                    val isSelected = coatsPreset == preset
                                    ElevatedFilterChip(
                                        selected = isSelected,
                                        onClick = { viewModel.coatsPreset.value = preset },
                                        label = { Text(preset) }
                                    )
                                }
                            }
                            if (coatsPreset == "Custom") {
                                OutlinedTextField(
                                    value = customCoats,
                                    onValueChange = { viewModel.customCoats.value = it },
                                    label = { Text("Custom Coats") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.fillMaxWidth().focusRequester(getFocusRequester("customCoats")),
                                    singleLine = true,
                                    isError = customCoatsError != null,
                                    supportingText = if (customCoatsError != null) { { Text(customCoatsError!!) } } else null
                                )
                            }
                        }
                    }

                    // Wastage Settings
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(text = "Wastage Allowance", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf("5", "10", "15", "Custom").forEach { preset ->
                                    val isSelected = wastagePreset == preset
                                    ElevatedFilterChip(
                                        selected = isSelected,
                                        onClick = { viewModel.wastagePreset.value = preset },
                                        label = { Text("$preset%") }
                                    )
                                }
                            }
                            if (wastagePreset == "Custom") {
                                OutlinedTextField(
                                    value = customWastage,
                                    onValueChange = { viewModel.customWastage.value = it },
                                    label = { Text("Custom Wastage %") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.fillMaxWidth().focusRequester(getFocusRequester("customWastage")),
                                    singleLine = true,
                                    isError = customWastageError != null,
                                    supportingText = if (customWastageError != null) { { Text(customWastageError!!) } } else null
                                )
                            }
                        }
                    }

                    // Calculate Button
                    item {
                        Button(
                            onClick = {
                                if (viewModel.validatePaintInputs()) {
                                    viewModel.calculate()
                                    showResultState = true
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .padding(vertical = 4.dp),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Text("Calculate Paint Volume", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    item { Spacer(modifier = Modifier.height(24.dp)) }
                }
            } else {
                // Results View
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Paint Volume Results",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    val cov = if (coveragePreset == "Custom") customCoverage else "$coveragePreset $covUnitStr"
                    val coats = if (coatsPreset == "Custom") customCoats else coatsPreset
                    val wastage = if (wastagePreset == "Custom") "$customWastage%" else "$wastagePreset%"

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            ResultRow(label = "Surface Area", value = String.format(Locale.US, "%.2f %s", surfaceAreaResult, areaUnitStr), isHighlighted = false)
                            ResultRow(label = "Number of Coats", value = "$coats", isHighlighted = false)
                            ResultRow(label = "Coverage Rate", value = "$cov", isHighlighted = false)
                            ResultRow(label = "Wastage", value = "$wastage", isHighlighted = false)
                            Divider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f))
                            ResultRow(label = "Net Paint Required", value = String.format(Locale.US, "%.2f %s", basePaintRequired, volUnitStr), isHighlighted = false)
                            ResultRow(label = "Recommended Quantity", value = String.format(Locale.US, "Approximately %.0f %s", Math.ceil(recommendedPaint), volUnitStr), isHighlighted = true)
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Buttons Row
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
                                    Toast.makeText(context, "Paint calculation saved successfully!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Save Result")
                        }

                        IconButton(
                            onClick = {
                                val txt = "Paint Requirement Calculation:\n" +
                                        "- Area: ${String.format(Locale.US, "%.2f", surfaceAreaResult)} $areaUnitStr\n" +
                                        "- Coats: $coats\n" +
                                        "- Coverage: $cov\n" +
                                        "- Wastage: $wastage\n" +
                                        "- Base Paint Required: ${String.format(Locale.US, "%.2f", basePaintRequired)} $volUnitStr\n" +
                                        "- Recommended: ${String.format(Locale.US, "%.1f", recommendedPaint)} $volUnitStr\n" +
                                        "Calculated using Paint Calculator App."
                                shareText(context, txt)
                            },
                            modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant, shape = MaterialTheme.shapes.medium)
                        ) {
                            Icon(imageVector = Icons.Default.Share, contentDescription = "Share")
                        }
                    }

                    // Navigate forward to Cost Calculator
                    Button(
                        onClick = {
                            navController.navigate(Screen.CostCalculatorScreen.createRoute(recommendedPaint))
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text("Calculate Cost", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

private fun shareText(context: android.content.Context, text: String) {
    val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(android.content.Intent.EXTRA_TEXT, text)
    }
    context.startActivity(android.content.Intent.createChooser(intent, "Share Estimate Summary"))
}
