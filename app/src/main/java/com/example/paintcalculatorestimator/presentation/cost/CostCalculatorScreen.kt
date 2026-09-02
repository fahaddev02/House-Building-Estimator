package com.example.paintcalculatorestimator.presentation.cost

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
import androidx.compose.ui.window.Dialog
import androidx.core.content.FileProvider
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.paintcalculatorestimator.domain.model.UnitType
import com.example.paintcalculatorestimator.presentation.wall.ResultRow
import java.io.File
import java.util.Locale
import com.example.paintcalculatorestimator.utils.Validator
import com.example.paintcalculatorestimator.utils.ValidationResult

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CostCalculatorScreen(
    navController: NavController,
    viewModel: CostCalculatorViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val paintQuantity by viewModel.paintQuantity.collectAsState()
    val paintPrice by viewModel.paintPrice.collectAsState()
    val includePrimer by viewModel.includePrimer.collectAsState()
    val primerQuantity by viewModel.primerQuantity.collectAsState()
    val primerPrice by viewModel.primerPrice.collectAsState()

    val isLaborManual by viewModel.isLaborManual.collectAsState()
    val paintableArea by viewModel.paintableArea.collectAsState()
    val laborRate by viewModel.laborRate.collectAsState()
    val laborManualCost by viewModel.laborManualCost.collectAsState()

    val materials by viewModel.materials.collectAsState()
    val currency by viewModel.currency.collectAsState()
    val unitType by viewModel.unitType.collectAsState()

    val paintCost by viewModel.paintCost.collectAsState()
    val primerCost by viewModel.primerCost.collectAsState()
    val laborCost by viewModel.laborCost.collectAsState()
    val materialsCost by viewModel.materialsCost.collectAsState()
    val totalCost by viewModel.totalCost.collectAsState()

    var showResultState by remember { mutableStateOf(false) }
    var showAddMaterialDialog by remember { mutableStateOf(false) }

    val volUnitStr = if (unitType == UnitType.METRIC) "L" else "gal"
    val areaUnitStr = if (unitType == UnitType.METRIC) "m²" else "ft²"

    val paintQuantityError by viewModel.paintQuantityError.collectAsState()
    val paintPriceError by viewModel.paintPriceError.collectAsState()

    val primerQuantityError by viewModel.primerQuantityError.collectAsState()
    val primerPriceError by viewModel.primerPriceError.collectAsState()

    val paintableAreaError by viewModel.paintableAreaError.collectAsState()
    val laborRateError by viewModel.laborRateError.collectAsState()
    val laborManualCostError by viewModel.laborManualCostError.collectAsState()

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
                title = { Text(text = "Cost Calculator", fontWeight = FontWeight.Bold) },
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

                    // Section 1: Paint Cost Setup
                    item {
                        Text(text = "Paint Purchase Details", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                    }

                    item {
                        OutlinedTextField(
                            value = paintQuantity,
                            onValueChange = { viewModel.paintQuantity.value = it },
                            label = { Text("Paint Required ($volUnitStr)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth().focusRequester(getFocusRequester("paintQuantity")),
                            singleLine = true,
                            isError = paintQuantityError != null,
                            supportingText = if (paintQuantityError != null) { { Text(paintQuantityError!!) } } else null
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = paintPrice,
                            onValueChange = { viewModel.paintPrice.value = it },
                            label = { Text("Paint Price per $volUnitStr ($currency)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth().focusRequester(getFocusRequester("paintPrice")),
                            singleLine = true,
                            isError = paintPriceError != null,
                            supportingText = if (paintPriceError != null) { { Text(paintPriceError!!) } } else null
                        )
                    }

                    // Section 2: Optional Primer
                    item {
                        Divider()
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = "Optional Primer Cost", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text(text = "Include primer estimates", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(
                                checked = includePrimer,
                                onCheckedChange = { viewModel.includePrimer.value = it }
                            )
                        }
                    }

                    if (includePrimer) {
                        item {
                            OutlinedTextField(
                                value = primerQuantity,
                                onValueChange = { viewModel.primerQuantity.value = it },
                                label = { Text("Primer Required ($volUnitStr)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth().focusRequester(getFocusRequester("primerQuantity")),
                                singleLine = true,
                                isError = primerQuantityError != null,
                                supportingText = if (primerQuantityError != null) { { Text(primerQuantityError!!) } } else null
                            )
                        }

                        item {
                            OutlinedTextField(
                                value = primerPrice,
                                onValueChange = { viewModel.primerPrice.value = it },
                                label = { Text("Primer Price per $volUnitStr ($currency)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth().focusRequester(getFocusRequester("primerPrice")),
                                singleLine = true,
                                isError = primerPriceError != null,
                                supportingText = if (primerPriceError != null) { { Text(primerPriceError!!) } } else null
                            )
                        }
                    }

                    // Section 3: Labor
                    item {
                        Divider()
                        Column {
                            Text(text = "Labor Setup", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FilterChip(
                                    selected = !isLaborManual,
                                    onClick = { viewModel.isLaborManual.value = false },
                                    label = { Text("Calculate by Rate") }
                                )
                                FilterChip(
                                    selected = isLaborManual,
                                    onClick = { viewModel.isLaborManual.value = true },
                                    label = { Text("Enter Total Cost") }
                                )
                            }
                        }
                    }

                    if (!isLaborManual) {
                        item {
                            OutlinedTextField(
                                value = paintableArea,
                                onValueChange = { viewModel.paintableArea.value = it },
                                label = { Text("Paintable Area ($areaUnitStr)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth().focusRequester(getFocusRequester("paintableArea")),
                                singleLine = true,
                                isError = paintableAreaError != null,
                                supportingText = if (paintableAreaError != null) { { Text(paintableAreaError!!) } } else null
                            )
                        }

                        item {
                            OutlinedTextField(
                                value = laborRate,
                                onValueChange = { viewModel.laborRate.value = it },
                                label = { Text("Labor Rate per $areaUnitStr ($currency)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth().focusRequester(getFocusRequester("laborRate")),
                                singleLine = true,
                                isError = laborRateError != null,
                                supportingText = if (laborRateError != null) { { Text(laborRateError!!) } } else null
                            )
                        }
                    } else {
                        item {
                            OutlinedTextField(
                                value = laborManualCost,
                                onValueChange = { viewModel.laborManualCost.value = it },
                                label = { Text("Total Labor Cost ($currency)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth().focusRequester(getFocusRequester("laborManualCost")),
                                singleLine = true,
                                isError = laborManualCostError != null,
                                supportingText = if (laborManualCostError != null) { { Text(laborManualCostError!!) } } else null
                            )
                        }
                    }

                    // Section 4: Materials
                    item {
                        Divider()
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = "Additional Expenses", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text(text = "Brushes, tape, putty, etc.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Button(
                                onClick = { showAddMaterialDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer)
                            ) {
                                Text("Add Item")
                            }
                        }
                    }

                    if (materials.isEmpty()) {
                        item {
                            Text(text = "No additional materials added.", fontSize = 13.sp, color = MaterialTheme.colorScheme.secondary)
                        }
                    } else {
                        items(materials.size) { index ->
                            val mat = materials[index]
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(text = mat.name, fontWeight = FontWeight.SemiBold)
                                        Text(text = "${mat.quantity} x $currency ${mat.unitPrice} = $currency ${mat.quantity * mat.unitPrice}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    IconButton(onClick = { viewModel.removeMaterial(index) }) {
                                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }

                    // Main Estimate Button
                    item {
                        Button(
                            onClick = {
                                if (viewModel.validateCostInputs()) {
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
                            Text("Generate Estimate", fontSize = 16.sp, fontWeight = FontWeight.Bold)
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
                        text = "Estimate Invoice Summary",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            ResultRow(label = "Paint Cost", value = "$currency ${String.format(Locale.US, "%,.2f", paintCost)}", isHighlighted = false)
                            if (includePrimer) {
                                ResultRow(label = "Primer Cost", value = "$currency ${String.format(Locale.US, "%,.2f", primerCost)}", isHighlighted = false)
                            }
                            ResultRow(label = "Labor Cost", value = "$currency ${String.format(Locale.US, "%,.2f", laborCost)}", isHighlighted = false)
                            ResultRow(label = "Additional Materials", value = "$currency ${String.format(Locale.US, "%,.2f", materialsCost)}", isHighlighted = false)
                            Divider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f))
                            ResultRow(label = "Total Estimated Cost", value = "$currency ${String.format(Locale.US, "%,.2f", totalCost)}", isHighlighted = true)
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Buttons Layout
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
                                viewModel.saveEstimate { savedId ->
                                    Toast.makeText(context, "Estimate saved successfully!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Save Estimate")
                        }

                        IconButton(
                            onClick = {
                                val matsText = if (materials.isEmpty()) "None" else materials.joinToString("\n") { "- ${it.name}: Qty ${it.quantity} @ ${it.unitPrice}" }
                                val txt = "Painting Project Cost Estimate:\n" +
                                        "- Paint Cost: $currency ${String.format(Locale.US, "%,.2f", paintCost)}\n" +
                                        (if (includePrimer) "- Primer Cost: $currency ${String.format(Locale.US, "%,.2f", primerCost)}\n" else "") +
                                        "- Labor Cost: $currency ${String.format(Locale.US, "%,.2f", laborCost)}\n" +
                                        "- Additional Materials: \n$matsText\n" +
                                        "- Total Estimate: $currency ${String.format(Locale.US, "%,.2f", totalCost)}\n" +
                                        "Calculated using Paint Calculator App."
                                shareText(context, txt)
                            },
                            modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant, shape = MaterialTheme.shapes.medium)
                        ) {
                            Icon(imageVector = Icons.Default.Share, contentDescription = "Share Text")
                        }
                    }

                    // PDF Export Button
                    Button(
                        onClick = {
                            try {
                                val file = viewModel.generatePdfReport(context)
                                sharePdfFile(context, file)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Error generating PDF: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Export PDF Estimate", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }

    if (showAddMaterialDialog) {
        AddMaterialDialog(
            onDismiss = { showAddMaterialDialog = false },
            onAdd = { name, q, price ->
                viewModel.addMaterial(name, q, price)
                showAddMaterialDialog = false
            }
        )
    }
}

@Composable
fun AddMaterialDialog(
    onDismiss: () -> Unit,
    onAdd: (String, Double, Double) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("1") }
    var price by remember { mutableStateOf("") }

    var nameError by remember { mutableStateOf<String?>(null) }
    var quantityError by remember { mutableStateOf<String?>(null) }
    var priceError by remember { mutableStateOf<String?>(null) }

    val focusRequesters = remember { List(3) { FocusRequester() } }
    var isValidationActive by remember { mutableStateOf(false) }

    LaunchedEffect(name) {
        if (isValidationActive) {
            val res = Validator.validateRequiredField(name, "item name", "Please enter item name")
            nameError = if (res is ValidationResult.Invalid) res.message else null
        }
    }
    LaunchedEffect(quantity) {
        if (isValidationActive) {
            val res = Validator.validatePositiveNumber(quantity, "quantity", "Please enter quantity")
            quantityError = if (res is ValidationResult.Invalid) res.message else null
        }
    }
    LaunchedEffect(price) {
        if (isValidationActive) {
            val res = Validator.validatePositiveNumber(price, "unit price", "Please enter unit price")
            priceError = if (res is ValidationResult.Invalid) res.message else null
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
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
                Text(text = "Add Additional Expense Item", fontSize = 18.sp, fontWeight = FontWeight.Bold)

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Item Name (e.g. Brush, Roller)") },
                    modifier = Modifier.fillMaxWidth().focusRequester(focusRequesters[0]),
                    singleLine = true,
                    isError = nameError != null,
                    supportingText = if (nameError != null) { { Text(nameError!!) } } else null
                )

                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Quantity") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().focusRequester(focusRequesters[1]),
                    singleLine = true,
                    isError = quantityError != null,
                    supportingText = if (quantityError != null) { { Text(quantityError!!) } } else null
                )

                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Unit Price") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().focusRequester(focusRequesters[2]),
                    singleLine = true,
                    isError = priceError != null,
                    supportingText = if (priceError != null) { { Text(priceError!!) } } else null
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
                            val nRes = Validator.validateRequiredField(name, "item name", "Please enter item name")
                            val qRes = Validator.validatePositiveNumber(quantity, "quantity", "Please enter quantity")
                            val pRes = Validator.validatePositiveNumber(price, "unit price", "Please enter unit price")

                            nameError = if (nRes is ValidationResult.Invalid) nRes.message else null
                            quantityError = if (qRes is ValidationResult.Invalid) qRes.message else null
                            priceError = if (pRes is ValidationResult.Invalid) pRes.message else null

                            if (nRes is ValidationResult.Invalid) {
                                focusRequesters[0].requestFocus()
                            } else if (qRes is ValidationResult.Invalid) {
                                focusRequesters[1].requestFocus()
                            } else if (pRes is ValidationResult.Invalid) {
                                focusRequesters[2].requestFocus()
                            } else {
                                val q = quantity.toDouble()
                                val p = price.toDouble()
                                onAdd(name, q, p)
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

private fun sharePdfFile(context: Context, file: File) {
    try {
        val uri = FileProvider.getUriForFile(
            context,
            "com.example.paintcalculatorestimator.fileprovider",
            file
        )
        val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(android.content.Intent.EXTRA_STREAM, uri)
            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(android.content.Intent.createChooser(intent, "Share Estimate PDF"))
    } catch (e: Exception) {
        Toast.makeText(context, "Error sharing PDF: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}
