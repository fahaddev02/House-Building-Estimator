package com.example.paintcalculatorestimator.presentation.settings

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.example.paintcalculatorestimator.R
import com.example.paintcalculatorestimator.data.preferences.AppPreferences
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.ExperimentalMaterial3Api

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    appPreferences: AppPreferences
) {
    val coroutineScope = rememberCoroutineScope()

    val theme by appPreferences.themeFlow.collectAsState(initial = "SYSTEM")
    val unitType by appPreferences.unitTypeFlow.collectAsState(initial = "METRIC")
    val currency by appPreferences.currencyFlow.collectAsState(initial = "PKR")
    val defaultCoverage by appPreferences.defaultCoverageFlow.collectAsState(initial = 10.0)
    val defaultCoats by appPreferences.defaultCoatsFlow.collectAsState(initial = 2)
    val defaultWastage by appPreferences.defaultWastageFlow.collectAsState(initial = 0.10)

    SettingsScreenContent(
        theme = theme,
        unitType = unitType,
        currency = currency,
        defaultCoverage = defaultCoverage,
        defaultCoats = defaultCoats,
        defaultWastage = defaultWastage,
        onBackClick = { navController.popBackStack() },
        onThemeSelect = { coroutineScope.launch { appPreferences.setTheme(it) } },
        onUnitTypeSelect = { coroutineScope.launch { appPreferences.setUnitType(it) } },
        onCurrencySelect = { coroutineScope.launch { appPreferences.setCurrency(it) } },
        onDefaultCoverageChange = { coroutineScope.launch { appPreferences.setDefaultCoverage(it) } },
        onDefaultCoatsChange = { coroutineScope.launch { appPreferences.setDefaultCoats(it) } },
        onDefaultWastageChange = { coroutineScope.launch { appPreferences.setDefaultWastage(it) } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreenContent(
    theme: String,
    unitType: String,
    currency: String,
    defaultCoverage: Double,
    defaultCoats: Int,
    defaultWastage: Double,
    onBackClick: () -> Unit,
    onThemeSelect: (String) -> Unit,
    onUnitTypeSelect: (String) -> Unit,
    onCurrencySelect: (String) -> Unit,
    onDefaultCoverageChange: (Double) -> Unit,
    onDefaultCoatsChange: (Int) -> Unit,
    onDefaultWastageChange: (Double) -> Unit
) {
    val context = LocalContext.current
    var showThemeDialog by remember { mutableStateOf(false) }
    var showCurrencyDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.settings_title), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            // 1. Theme Configuration
            item {
                SettingsItem(
                    title = stringResource(id = R.string.settings_theme),
                    value = theme.lowercase().replaceFirstChar { it.uppercase() },
                    icon = Icons.Default.Info
                ) {
                    showThemeDialog = true
                }
            }

            // 2. Unit Type
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = stringResource(id = R.string.settings_units), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(text = "Metric vs Imperial measurements", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = unitType == "METRIC",
                                onClick = { onUnitTypeSelect("METRIC") },
                                label = { Text("Metric") }
                            )
                            FilterChip(
                                selected = unitType == "IMPERIAL",
                                onClick = { onUnitTypeSelect("IMPERIAL") },
                                label = { Text("Imperial") }
                            )
                        }
                    }
                }
            }

            // 3. Currency configuration
            item {
                SettingsItem(
                    title = stringResource(id = R.string.settings_currency),
                    value = currency,
                    icon = Icons.Default.Info
                ) {
                    showCurrencyDialog = true
                }
            }

            // 4. Default configurations
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(text = "Calculation Defaults", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        
                        // Coverage
                        var covInput by remember(defaultCoverage) { mutableStateOf(defaultCoverage.toString()) }
                        OutlinedTextField(
                            value = covInput,
                            onValueChange = {
                                covInput = it
                                it.toDoubleOrNull()?.let { d -> onDefaultCoverageChange(d) }
                            },
                            label = { Text(stringResource(id = R.string.settings_default_coverage)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Coats
                        var coatsInput by remember(defaultCoats) { mutableStateOf(defaultCoats.toString()) }
                        OutlinedTextField(
                            value = coatsInput,
                            onValueChange = {
                                coatsInput = it
                                it.toIntOrNull()?.let { i -> onDefaultCoatsChange(i) }
                            },
                            label = { Text(stringResource(id = R.string.settings_default_coats)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Wastage
                        var wastageInput by remember(defaultWastage) { mutableStateOf(((defaultWastage * 100).toInt()).toString()) }
                        OutlinedTextField(
                            value = wastageInput,
                            onValueChange = {
                                wastageInput = it
                                it.toDoubleOrNull()?.let { percent -> onDefaultWastageChange(percent / 100.0) }
                            },
                            label = { Text("${stringResource(id = R.string.settings_default_wastage)} (%)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // 5. About Screen Links
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(text = stringResource(id = R.string.settings_about), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                        AboutItem(label = stringResource(id = R.string.settings_version), value = "v1.0.0")
                        AboutItem(label = stringResource(id = R.string.settings_privacy), value = "") {
                            Toast.makeText(context, "Opening Privacy Policy...", Toast.LENGTH_SHORT).show()
                        }
                        AboutItem(label = stringResource(id = R.string.settings_terms), value = "") {
                            Toast.makeText(context, "Opening Terms...", Toast.LENGTH_SHORT).show()
                        }
                        AboutItem(label = stringResource(id = R.string.settings_contact), value = "support@paintcalc.example.com") {
                            Toast.makeText(context, "Opening Email...", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }

    // Dialogs
    if (showThemeDialog) {
        ThemeDialog(
            currentTheme = theme,
            onDismiss = { showThemeDialog = false },
            onSelect = {
                onThemeSelect(it)
                showThemeDialog = false
            }
        )
    }

    if (showCurrencyDialog) {
        CurrencyDialog(
            currentCurrency = currency,
            onDismiss = { showCurrencyDialog = false },
            onSelect = {
                onCurrencySelect(it)
                showCurrencyDialog = false
            }
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    MaterialTheme {
        SettingsScreenContent(
            theme = "SYSTEM",
            unitType = "METRIC",
            currency = "PKR",
            defaultCoverage = 10.0,
            defaultCoats = 2,
            defaultWastage = 0.10,
            onBackClick = {},
            onThemeSelect = {},
            onUnitTypeSelect = {},
            onCurrencySelect = {},
            onDefaultCoverageChange = {},
            onDefaultCoatsChange = {},
            onDefaultWastageChange = {}
        )
    }
}



@Composable
fun SettingsItem(
    title: String,
    value: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(16.dp))
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = value, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.width(4.dp))
                Icon(imageVector = Icons.Default.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun AboutItem(
    label: String,
    value: String,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
        if (value.isNotEmpty()) {
            Text(text = value, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            Icon(imageVector = Icons.Default.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
fun ThemeDialog(
    currentTheme: String,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(shape = MaterialTheme.shapes.large, modifier = Modifier.padding(16.dp)) {
            Column(modifier = Modifier.padding(24.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(text = "Select Theme", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                
                listOf("SYSTEM", "LIGHT", "DARK").forEach { themeOption ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(themeOption) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = currentTheme == themeOption, onClick = { onSelect(themeOption) })
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = themeOption.lowercase().replaceFirstChar { it.uppercase() })
                    }
                }
            }
        }
    }
}

@Composable
fun CurrencyDialog(
    currentCurrency: String,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    var customCurrency by remember { mutableStateOf("") }
    Dialog(onDismissRequest = onDismiss) {
        Card(shape = MaterialTheme.shapes.large, modifier = Modifier.padding(16.dp)) {
            Column(modifier = Modifier.padding(24.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(text = "Select Currency", fontSize = 18.sp, fontWeight = FontWeight.Bold)

                listOf("PKR", "USD", "EUR", "GBP", "AED", "SAR").forEach { curr ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(curr) }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = currentCurrency == curr, onClick = { onSelect(curr) })
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = curr)
                    }
                }

                Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f), modifier = Modifier.padding(vertical = 8.dp))
                Text(text = "Or Enter Custom Currency:", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = customCurrency,
                        onValueChange = { customCurrency = it },
                        placeholder = { Text("e.g. CAD") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    Button(onClick = {
                        if (customCurrency.trim().isNotEmpty()) {
                            onSelect(customCurrency.trim().uppercase())
                        }
                    }) {
                        Text("Apply")
                    }
                }
            }
        }
    }
}
