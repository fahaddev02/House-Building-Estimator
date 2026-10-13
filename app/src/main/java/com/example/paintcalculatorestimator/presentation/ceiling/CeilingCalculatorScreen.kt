package com.example.paintcalculatorestimator.presentation.ceiling

import com.example.paintcalculatorestimator.ui.theme.EstimateTextField



import android.content.Context
import android.widget.Toast
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
import com.example.paintcalculatorestimator.domain.model.UnitType
import com.example.paintcalculatorestimator.presentation.navigation.Screen
import com.example.paintcalculatorestimator.presentation.wall.ResultRow
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CeilingCalculatorScreen(
    navController: NavController,
    viewModel: CeilingCalculatorViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val ceilingShape by viewModel.ceilingShape.collectAsState()
    val rectLength by viewModel.rectLength.collectAsState()
    val rectWidth by viewModel.rectWidth.collectAsState()
    val squareSide by viewModel.squareSide.collectAsState()
    val customSections by viewModel.customSections.collectAsState()
    val unitType by viewModel.unitType.collectAsState()

    val ceilingArea by viewModel.ceilingArea.collectAsState()

    var showResultState by remember { mutableStateOf(false) }

    val unitStr = if (unitType == UnitType.METRIC) "m" else "ft"
    val areaUnitStr = if (unitType == UnitType.METRIC) "m²" else "ft²"

    val rectLengthError by viewModel.rectLengthError.collectAsState()
    val rectWidthError by viewModel.rectWidthError.collectAsState()
    val squareSideError by viewModel.squareSideError.collectAsState()
    val customSectionsErrors by viewModel.customSectionsErrors.collectAsState()

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
                title = { Text(text = "Ceiling Calculator", fontWeight = FontWeight.Bold) },
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
                // Inputs View
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item { Spacer(modifier = Modifier.height(8.dp)) }

                    // Ceiling Shape Tab Selector
                    item {
                        Column {
                            Text(text = "Select Ceiling Shape", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            TabRow(
                                selectedTabIndex = when (ceilingShape) {
                                    "Rectangle" -> 0
                                    "Square" -> 1
                                    else -> 2
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Tab(
                                    selected = ceilingShape == "Rectangle",
                                    onClick = { viewModel.ceilingShape.value = "Rectangle" },
                                    text = { Text("Rectangle", fontSize = 12.sp) }
                                )
                                Tab(
                                    selected = ceilingShape == "Square",
                                    onClick = { viewModel.ceilingShape.value = "Square" },
                                    text = { Text("Square", fontSize = 12.sp) }
                                )
                                Tab(
                                    selected = ceilingShape == "Custom",
                                    onClick = { viewModel.ceilingShape.value = "Custom" },
                                    text = { Text("Custom", fontSize = 12.sp) }
                                )
                            }
                        }
                    }

                    // Form Fields based on Shape
                    when (ceilingShape) {
                        "Rectangle" -> {
                            item {
                                EstimateTextField(
                                    value = rectLength,
                                    onValueChange = { viewModel.rectLength.value = it },
                                    label = { Text("Ceiling Length ($unitStr)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.fillMaxWidth().focusRequester(getFocusRequester("rectLength")),
                                    singleLine = true,
                                    isError = rectLengthError != null,
                                    supportingText = if (rectLengthError != null) { { Text(rectLengthError!!) } } else null
                                )
                            }
                            item {
                                EstimateTextField(
                                    value = rectWidth,
                                    onValueChange = { viewModel.rectWidth.value = it },
                                    label = { Text("Ceiling Width ($unitStr)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.fillMaxWidth().focusRequester(getFocusRequester("rectWidth")),
                                    singleLine = true,
                                    isError = rectWidthError != null,
                                    supportingText = if (rectWidthError != null) { { Text(rectWidthError!!) } } else null
                                )
                            }
                        }
                        "Square" -> {
                            item {
                                EstimateTextField(
                                    value = squareSide,
                                    onValueChange = { viewModel.squareSide.value = it },
                                    label = { Text("Side Length ($unitStr)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.fillMaxWidth().focusRequester(getFocusRequester("squareSide")),
                                    singleLine = true,
                                    isError = squareSideError != null,
                                    supportingText = if (squareSideError != null) { { Text(squareSideError!!) } } else null
                                )
                            }
                        }
                        "Custom" -> {
                            items(customSections.size) { index ->
                                val sec = customSections[index]
                                val secErr = customSectionsErrors.getOrNull(index)
                                OutlinedCard(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(text = sec.name, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                            if (customSections.size > 1) {
                                                IconButton(onClick = { viewModel.removeCustomSection(index) }) {
                                                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                                }
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            EstimateTextField(
                                                value = sec.length,
                                                onValueChange = { viewModel.updateCustomSection(index, it, sec.width) },
                                                label = { Text("Length ($unitStr)") },
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                modifier = Modifier.weight(1f).focusRequester(getFocusRequester("custom_length_$index")),
                                                singleLine = true,
                                                isError = secErr?.lengthError != null,
                                                supportingText = if (secErr?.lengthError != null) { { Text(secErr.lengthError) } } else null
                                            )
                                            EstimateTextField(
                                                value = sec.width,
                                                onValueChange = { viewModel.updateCustomSection(index, sec.length, it) },
                                                label = { Text("Width ($unitStr)") },
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                modifier = Modifier.weight(1f).focusRequester(getFocusRequester("custom_width_$index")),
                                                singleLine = true,
                                                isError = secErr?.widthError != null,
                                                supportingText = if (secErr?.widthError != null) { { Text(secErr.widthError) } } else null
                                            )
                                        }
                                    }
                                }
                            }
                            item {
                                Button(
                                    onClick = { viewModel.addCustomSection() },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Add Another Section")
                                }
                            }
                        }
                    }

                    // Calculate Button
                    item {
                        Button(
                            onClick = {
                                if (viewModel.validateCeilingInputs()) {
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
                            Text("Calculate Ceiling Area", fontSize = 16.sp, fontWeight = FontWeight.Bold)
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
                        text = "Ceiling Area Results",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    OutlinedCard(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            ResultRow(label = "Total Ceiling Surface Area", value = String.format(Locale.US, "%.2f %s", ceilingArea, areaUnitStr), isHighlighted = true)
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Action Buttons Row
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
                                    Toast.makeText(context, "Ceiling calculation saved successfully!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Save Result")
                        }

                        IconButton(
                            onClick = {
                                val txt = "Ceiling Painting Calculation:\n" +
                                        "- Ceiling Shape: $ceilingShape\n" +
                                        "- Total Ceiling Area: ${String.format(Locale.US, "%.2f", ceilingArea)} $areaUnitStr\n" +
                                        "Calculated using Paint Calculator App."
                                shareText(context, txt)
                            },
                            modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant, shape = MaterialTheme.shapes.medium)
                        ) {
                            Icon(imageVector = Icons.Default.Share, contentDescription = "Share")
                        }
                    }

                    // Calculate Paint Required Action
                    Button(
                        onClick = {
                            navController.navigate(Screen.PaintCalculatorScreen.createRoute(ceilingArea))
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text("Calculate Paint Required", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(16.dp))
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
    context.startActivity(android.content.Intent.createChooser(intent, "Share Estimate Summary"))
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, name = "Ceiling calculator")
@Composable
private fun CeilingCalculatorScreenPreview() {
    com.example.paintcalculatorestimator.ui.theme.PaintCalculatorEstimatorTheme {
        com.example.paintcalculatorestimator.ui.theme.CalculatorScreenPreview("Ceiling Calculator", "Choose a ceiling shape and calculate its area.", listOf("Ceiling length", "Ceiling width"), "Ceiling area results")
    }
}
