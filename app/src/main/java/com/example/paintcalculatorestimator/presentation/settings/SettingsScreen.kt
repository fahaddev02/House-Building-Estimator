package com.example.paintcalculatorestimator.presentation.settings

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.example.paintcalculatorestimator.R
import com.example.paintcalculatorestimator.data.preferences.AppPreferences
import com.example.paintcalculatorestimator.ui.theme.EstimateTextField
import kotlinx.coroutines.launch

/**
 * One place to control every font size on the Settings screen.
 * Change a value here and the whole screen follows.
 */
private object SettingsFont {
    val screenTitle = 20.sp
    val cardTitle = 16.sp
    val body = 15.sp
    val caption = 12.sp
    val dialogTitle = 18.sp
}

private const val SUPPORT_EMAIL = "support.paintcalculatorestimat@gmail.com"

// ---------------------------------------------------------------------------
// Stateful entry point (uses AppPreferences, not previewable)
// ---------------------------------------------------------------------------

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

// ---------------------------------------------------------------------------
// Stateless content (this is what the previews render)
// ---------------------------------------------------------------------------

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
                title = {
                    Text(
                        text = stringResource(id = R.string.settings_title),
                        fontWeight = FontWeight.Bold,
                        fontSize = SettingsFont.screenTitle
                    )
                },
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

            // 1. Theme
            item {
                SettingsItem(
                    title = stringResource(id = R.string.settings_theme),
                    value = theme.lowercase().replaceFirstChar { it.uppercase() },
                    icon = Icons.Default.Info
                ) {
                    showThemeDialog = true
                }
            }

            // 2. Unit type
            item {
                OutlinedCard(
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
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(id = R.string.settings_units),
                                fontWeight = FontWeight.Bold,
                                fontSize = SettingsFont.cardTitle
                            )
                            Text(
                                text = "Metric vs Imperial measurements",
                                fontSize = SettingsFont.caption,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = unitType == "METRIC",
                                onClick = { onUnitTypeSelect("METRIC") },
                                label = { Text("Metric", fontSize = SettingsFont.body) }
                            )
                            FilterChip(
                                selected = unitType == "IMPERIAL",
                                onClick = { onUnitTypeSelect("IMPERIAL") },
                                label = { Text("Imperial", fontSize = SettingsFont.body) }
                            )
                        }
                    }
                }
            }

            // 3. Currency
            item {
                SettingsItem(
                    title = stringResource(id = R.string.settings_currency),
                    value = currency,
                    icon = Icons.Default.Info
                ) {
                    showCurrencyDialog = true
                }
            }

            // 4. Calculation defaults
            item {
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Calculation Defaults",
                            fontWeight = FontWeight.Bold,
                            fontSize = SettingsFont.cardTitle
                        )

                        // Coverage
                        var covInput by remember(defaultCoverage) { mutableStateOf(defaultCoverage.toString()) }
                        EstimateTextField(
                            value = covInput,
                            onValueChange = {
                                covInput = it
                                it.toDoubleOrNull()?.let { d -> onDefaultCoverageChange(d) }
                            },
                            label = {
                                Text(
                                    stringResource(id = R.string.settings_default_coverage),
                                    fontSize = SettingsFont.body
                                )
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Coats
                        var coatsInput by remember(defaultCoats) { mutableStateOf(defaultCoats.toString()) }
                        EstimateTextField(
                            value = coatsInput,
                            onValueChange = {
                                coatsInput = it
                                it.toIntOrNull()?.let { i -> onDefaultCoatsChange(i) }
                            },
                            label = {
                                Text(
                                    stringResource(id = R.string.settings_default_coats),
                                    fontSize = SettingsFont.body
                                )
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Wastage
                        var wastageInput by remember(defaultWastage) {
                            mutableStateOf(((defaultWastage * 100).toInt()).toString())
                        }
                        EstimateTextField(
                            value = wastageInput,
                            onValueChange = {
                                wastageInput = it
                                it.toDoubleOrNull()?.let { percent -> onDefaultWastageChange(percent / 100.0) }
                            },
                            label = {
                                Text(
                                    "${stringResource(id = R.string.settings_default_wastage)} (%)",
                                    fontSize = SettingsFont.body
                                )
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // 5. About
            item {
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = stringResource(id = R.string.settings_about),
                            fontWeight = FontWeight.Bold,
                            fontSize = SettingsFont.cardTitle
                        )
                        Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                        val versionName = remember {
                            try {
                                context.packageManager
                                    .getPackageInfo(context.packageName, 0)
                                    .versionName ?: "N/A"
                            } catch (e: Exception) {
                                "N/A"
                            }
                        }

                        AboutItem(
                            label = stringResource(id = R.string.settings_version),
                            value = "v$versionName"
                        )
                        AboutItem(label = stringResource(id = R.string.settings_privacy), value = "", labelFontSize = 16.sp) {
                            val intent = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://sites.google.com/view/costestimatorprivacypolicy/home")
                            )
                            context.startActivity(intent)
                        }
                        AboutItem(label = stringResource(id = R.string.settings_share), value = "", labelFontSize = 16.sp) {
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    "Check out this app: https://play.google.com/store/apps/details?id=${context.packageName}"
                                )
                            }
                            context.startActivity(Intent.createChooser(intent, "Share App"))
                        }
                        AboutItem(label = stringResource(id = R.string.settings_rate), value = "", labelFontSize = 16.sp) {
                            try {
                                context.startActivity(
                                    Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse("market://details?id=${context.packageName}")
                                    )
                                )
                            } catch (e: Exception) {
                                context.startActivity(
                                    Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse("https://play.google.com/store/apps/details?id=${context.packageName}")
                                    )
                                )
                            }
                        }
                        AboutItem(
                            label = stringResource(id = R.string.settings_contact),
                            value = SUPPORT_EMAIL
                        ) {
                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:")
                                putExtra(Intent.EXTRA_EMAIL, arrayOf(SUPPORT_EMAIL))
                                putExtra(Intent.EXTRA_SUBJECT, "Paint Calculator - Support")
                            }
                            try {
                                context.startActivity(intent)
                            } catch (e: ActivityNotFoundException) {
                                Toast.makeText(context, "No email app found", Toast.LENGTH_SHORT).show()
                            }
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

// ---------------------------------------------------------------------------
// Reusable rows
// ---------------------------------------------------------------------------

@Composable
fun SettingsItem(
    title: String,
    value: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    OutlinedCard(
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
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = SettingsFont.body)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = value,
                    fontSize = SettingsFont.body,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun AboutItem(
    label: String,
    value: String,
    labelFontSize: TextUnit = SettingsFont.body,
    valueFontSize: TextUnit = SettingsFont.body,
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
        Text(
            text = label,
            fontSize = labelFontSize,
            color = MaterialTheme.colorScheme.onSurface
        )
        if (value.isNotEmpty()) {
            Text(
                text = value,
                fontSize = valueFontSize,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(start = 12.dp)
            )
        } else {
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Dialogs
// ---------------------------------------------------------------------------

@Composable
fun ThemeDialog(
    currentTheme: String,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        OutlinedCard(shape = MaterialTheme.shapes.large, modifier = Modifier.padding(16.dp)) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Select Theme",
                    fontSize = SettingsFont.dialogTitle,
                    fontWeight = FontWeight.Bold
                )

                listOf("SYSTEM", "LIGHT", "DARK").forEach { themeOption ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(themeOption) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = currentTheme == themeOption,
                            onClick = { onSelect(themeOption) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = themeOption.lowercase().replaceFirstChar { it.uppercase() },
                            fontSize = SettingsFont.body
                        )
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
        OutlinedCard(shape = MaterialTheme.shapes.large, modifier = Modifier.padding(16.dp)) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Select Currency",
                    fontSize = SettingsFont.dialogTitle,
                    fontWeight = FontWeight.Bold
                )

                listOf("PKR", "USD", "EUR", "GBP", "AED", "SAR").forEach { curr ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(curr) }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = currentCurrency == curr,
                            onClick = { onSelect(curr) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = curr, fontSize = SettingsFont.body)
                    }
                }

                Divider(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                Text(
                    text = "Or Enter Custom Currency:",
                    fontSize = SettingsFont.body,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    EstimateTextField(
                        value = customCurrency,
                        onValueChange = { customCurrency = it },
                        placeholder = { Text("e.g. CAD", fontSize = SettingsFont.body) },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    Button(onClick = {
                        if (customCurrency.trim().isNotEmpty()) {
                            onSelect(customCurrency.trim().uppercase())
                        }
                    }) {
                        Text("Apply", fontSize = SettingsFont.body)
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Previews (open this file in Android Studio and use the Split / Design view)
// ---------------------------------------------------------------------------

@Composable
private fun PreviewSettings() {
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

@Preview(name = "Light", showBackground = true, showSystemUi = true)
@Composable
fun SettingsScreenPreviewLight() {
    MaterialTheme { PreviewSettings() }
}

@Preview(name = "Dark", showBackground = true, showSystemUi = true)
@Composable
fun SettingsScreenPreviewDark() {
    MaterialTheme(colorScheme = darkColorScheme()) { PreviewSettings() }
}

@Preview(name = "Large system font (1.3x)", showBackground = true, showSystemUi = true, fontScale = 1.3f)
@Composable
fun SettingsScreenPreviewLargeFont() {
    MaterialTheme { PreviewSettings() }
}