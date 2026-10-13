package com.example.paintcalculatorestimator.presentation.project

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.ui.semantics.*
import com.example.paintcalculatorestimator.ui.theme.*
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.paintcalculatorestimator.data.preferences.AppPreferences
import com.example.paintcalculatorestimator.domain.model.UnitType
import com.example.paintcalculatorestimator.presentation.navigation.Screen
import com.example.paintcalculatorestimator.presentation.wall.ResultRow
import com.example.paintcalculatorestimator.utils.HouseRoomInput
import androidx.compose.ui.tooling.preview.Preview
import java.util.Locale
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectWizardScreen(
    navController: NavController,
    projectId: Long = 0,
    appPreferences: AppPreferences,
    viewModel: ProjectViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val currentStep by viewModel.currentStep.collectAsState()
    val validationError by viewModel.validationError.collectAsState()
    val currency by appPreferences.currencyFlow.collectAsState(initial = "PKR")
    val focusRequesters = remember { mutableMapOf<String, FocusRequester>() }
    fun getFocusRequester(key: String) = focusRequesters.getOrPut(key) { FocusRequester() }
    LaunchedEffect(Unit) {
        viewModel.firstInvalidField.collect { fieldId -> focusRequesters[fieldId]?.requestFocus() }
    }
    LaunchedEffect(projectId) { if (projectId > 0) viewModel.loadProject(projectId) }
    LaunchedEffect(validationError) {
        validationError?.let { Toast.makeText(context, it, Toast.LENGTH_SHORT).show(); viewModel.clearError() }
    }
    BackHandler(enabled = currentStep > 1) { viewModel.previousStep() }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (projectId > 0) "Edit project" else "New project", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = { if (currentStep > 1) viewModel.previousStep() else navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        bottomBar = {
            Column(Modifier.navigationBarsPadding().imePadding()) {
                if (currentStep < 6) {
                    Surface(color = MaterialTheme.colorScheme.background) {
                        Column(Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
                            Button(onClick = { viewModel.nextStep() }, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp), shape = CircleShape) {
                                Text(if (currentStep == 5) "Review estimate  ›" else "Continue  ›")
                            }
                            if (currentStep == 1) {
                                Text("You can change this before saving your project.",
                                    Modifier.fillMaxWidth().padding(top = 8.dp), textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
                com.example.paintcalculatorestimator.adsManager.NativeAdView()
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp)) {
            WizardStepper(currentStep)
            Spacer(Modifier.height(20.dp))
            AnimatedContent(currentStep, modifier = Modifier.weight(1f), label = "projectStep") { step ->
                when (step) {
                    1 -> ProjectTypeSelectionStep(viewModel)
                    2 -> HouseDetailsStep(viewModel, ::getFocusRequester)
                    3 -> RoomBuilderStep(viewModel)
                    4 -> BoundaryWallStep(viewModel, ::getFocusRequester)
                    5 -> MaterialPricingStep(viewModel, ::getFocusRequester)
                    6 -> HouseEstimateResultStep(viewModel, currency, navController)
                }
            }
        }
    }
}

@Composable
fun WizardStepper(currentStep: Int) {
    val steps = listOf("Type", "Info", "Rooms", "Boundary", "Pricing", "Summary")
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(Modifier.fillMaxWidth().semantics {
            progressBarRangeInfo = ProgressBarRangeInfo(currentStep.toFloat(), 1f..6f, 4)
        }, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            steps.indices.forEach { index ->
                Box(Modifier.weight(1f).height(5.dp).clip(CircleShape)
                    .background(if (index < currentStep) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant))
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Step $currentStep of 6", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            Text(steps[(currentStep - 1).coerceIn(steps.indices)], style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun ProjectTypeSelectionStep(viewModel: ProjectViewModel) {
    val selectedType by viewModel.projectType.collectAsState()
    val types = listOf("House", "Market", "Plaza", "Guest House", "Office", "Shop", "Warehouse", "School", "Other / Custom")
    val accents = listOf(Color(0xFF7662CB), Color(0xFFC77864), Color(0xFF4586B9), Color(0xFFB99040), Color(0xFF358F76))
    Column(Modifier.fillMaxSize()) {
        Text("What are you building?", style = MaterialTheme.typography.headlineSmall)
        Text("Pick a project type to get started with rooms, materials and labor.",
            Modifier.padding(top = 6.dp, bottom = 18.dp), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        LazyVerticalGrid(columns = GridCells.Fixed(2), horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 16.dp)) {
            items(types.size, span = { index -> GridItemSpan(if (index == types.lastIndex) 2 else 1) }) { index ->
                val type = types[index]
                val selected = type == selectedType
                val symbol = when(type) {
                    "House", "Guest House" -> EstimatorSymbol.HOUSE
                    "Market", "Shop" -> EstimatorSymbol.SHOP
                    "Other / Custom" -> EstimatorSymbol.PLAN
                    else -> EstimatorSymbol.BUILDING
                }
                Surface(
                    modifier = Modifier.fillMaxWidth().selectable(selected, role = Role.RadioButton) { viewModel.setProjectType(type) },
                    shape = RoundedCornerShape(18.dp),
                    color = if (selected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f) else MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant)
                ) {
                    if (index == types.lastIndex) {
                        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            IconTile(symbol)
                            Text(type, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                            if (selected) Icon(Icons.Default.CheckCircle, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        }
                    } else {
                        Column(Modifier.padding(14.dp).heightIn(min = 90.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                IconTile(symbol, accents[index % accents.size])
                                if (selected) Icon(Icons.Default.CheckCircle, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            }
                            Text(type, style = MaterialTheme.typography.titleSmall)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HouseDetailsStep(viewModel: ProjectViewModel, getFocusRequester: (String) -> FocusRequester) {
    val name by viewModel.projectName.collectAsState()
    val location by viewModel.projectLocation.collectAsState()
    val notes by viewModel.projectNotes.collectAsState()
    val ut by viewModel.unitType.collectAsState()
    val laborRate by viewModel.laborRatePerSqFt.collectAsState()
    val globalSlab by viewModel.slabThicknessGlobal.collectAsState()
    val projectNameError by viewModel.projectNameError.collectAsState()
    val projectLocationError by viewModel.projectLocationError.collectAsState()
    val laborRatePerSqFtError by viewModel.laborRatePerSqFtError.collectAsState()
    val slabThicknessGlobalError by viewModel.slabThicknessGlobalError.collectAsState()
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Text("Tell us about the project", style = MaterialTheme.typography.headlineSmall)
            Text("Keep your project details and construction preferences together.",
                Modifier.padding(top = 6.dp), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item {
            EstimatePanel {
                Text("PROJECT", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                EstimateTextField(name, { viewModel.projectName.value = it }, label = { Text("Project name") },
                    modifier = Modifier.fillMaxWidth().focusRequester(getFocusRequester("projectName")), singleLine = true,
                    isError = projectNameError != null, supportingText = projectNameError?.let { { Text(it) } })
                EstimateTextField(location, { viewModel.projectLocation.value = it }, label = { Text("Site / location") },
                    modifier = Modifier.fillMaxWidth().focusRequester(getFocusRequester("projectLocation")), singleLine = true,
                    isError = projectLocationError != null, supportingText = projectLocationError?.let { { Text(it) } })
                EstimateTextField(notes, { viewModel.projectNotes.value = it }, label = { Text("Project notes (optional)") },
                    modifier = Modifier.fillMaxWidth(), minLines = 3)
            }
        }
        item {
            EstimatePanel {
                Text("MEASUREMENTS", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Unit system", style = MaterialTheme.typography.titleSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    FilterChip(selected = ut == UnitType.METRIC, onClick = { viewModel.unitType.value = UnitType.METRIC },
                        label = { Text("Metric · m / cm") }, modifier = Modifier.weight(1f))
                    FilterChip(selected = ut == UnitType.IMPERIAL, onClick = { viewModel.unitType.value = UnitType.IMPERIAL },
                        label = { Text("Imperial · ft / in") }, modifier = Modifier.weight(1f))
                }
                EstimateTextField(globalSlab, { viewModel.slabThicknessGlobal.value = it },
                    label = { Text("Roof slab thickness (${if (ut == UnitType.METRIC) "cm" else "in"})") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().focusRequester(getFocusRequester("slabThicknessGlobal")), singleLine = true,
                    isError = slabThicknessGlobalError != null, supportingText = slabThicknessGlobalError?.let { { Text(it) } })
            }
        }
        item {
            EstimatePanel {
                Text("LABOR", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                EstimateTextField(laborRate, { viewModel.laborRatePerSqFt.value = it }, label = { Text("Labor rate per sq ft") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().focusRequester(getFocusRequester("laborRatePerSqFt")), singleLine = true,
                    isError = laborRatePerSqFtError != null, supportingText = laborRatePerSqFtError?.let { { Text(it) } })
            }
        }
    }
}

@Composable
fun RoomBuilderStep(viewModel: ProjectViewModel) {
    val rooms by viewModel.houseRooms.collectAsState()
    val projectType by viewModel.projectType.collectAsState()
    val ut by viewModel.unitType.collectAsState()
    var showAddRoomDialog by remember { mutableStateOf(false) }
    val unitStr = if (ut == UnitType.METRIC) "m" else "ft"
    val thicknessUnitStr = if (ut == UnitType.METRIC) "cm" else "in"
    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("Add your rooms", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.weight(1f))
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surfaceVariant) {
                Text("${rooms.size} added", Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelMedium)
            }
        }
        Text("Plan each space, including openings and optional roof slabs.", Modifier.padding(top = 6.dp, bottom = 16.dp),
            style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 16.dp)) {
            if (rooms.isEmpty()) {
                item {
                    EstimatePanel {
                        IconTile(EstimatorSymbol.HOUSE)
                        Text("Make room for your first space", style = MaterialTheme.typography.titleMedium)
                        Text("Add a bedroom, shop, office or custom area to begin your estimate.",
                            style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            items(rooms.size) { index ->
                val rm = rooms[index]
                OutlinedCard(colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant), modifier = Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        IconTile(EstimatorSymbol.HOUSE, when(index % 3) { 0 -> Color(0xFFC77864); 1 -> Color(0xFF4586B9); else -> Color(0xFFB99040) })
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(if (rm.quantity > 1) "${rm.name} × ${rm.quantity}" else rm.name, style = MaterialTheme.typography.titleSmall)
                            Text("${rm.length} × ${rm.width} × ${rm.height} $unitStr",
                                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${rm.materialType} · ${rm.thickness} $thicknessUnitStr${if(rm.hasRoof) " · Roof" else ""}${if(rm.isWashroomEnabled) " · Washroom" else ""}",
                                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        IconButton(onClick = { viewModel.removeRoom(index) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Remove ${rm.name}", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
            item {
                OutlinedButton(onClick = { showAddRoomDialog = true }, modifier = Modifier.fillMaxWidth().heightIn(min = 50.dp),
                    shape = RoundedCornerShape(14.dp), border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))) {
                    Icon(Icons.Default.Add, null, Modifier.size(18.dp)); Spacer(Modifier.width(8.dp))
                    Text(if (rooms.isEmpty()) "Add a room or area" else "Add another room")
                }
            }
        }
        Surface(shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.primary, modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
            Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                Column(Modifier.weight(1f)) {
                    Text("ROOMS / AREAS", style = MaterialTheme.typography.labelSmall)
                    Text(rooms.sumOf { it.quantity }.toString(), style = MaterialTheme.typography.titleMedium)
                }
                Column(Modifier.weight(2f)) {
                    Text("TOTAL FLOOR AREA", style = MaterialTheme.typography.labelSmall)
                    Text("${String.format(Locale.US, "%.1f", rooms.sumOf { it.length * it.width * it.quantity })} ${unitStr}²", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
    if (showAddRoomDialog) {
        AddRoomDialog(projectType = projectType, existingRooms = rooms, unitStr = unitStr, thicknessUnitStr = thicknessUnitStr,
            onDismiss = { showAddRoomDialog = false }, onAdd = { viewModel.addRoom(it); showAddRoomDialog = false })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRoomDialog(
    projectType: String,
    existingRooms: List<HouseRoomInput>,
    unitStr: String,
    thicknessUnitStr: String,
    onDismiss: () -> Unit,
    onAdd: (HouseRoomInput) -> Unit
) {
    val categories = when (projectType) {
        "House" -> listOf("Master Bedroom", "Bedroom", "Living Room", "TV Lounge", "Drawing Room", "Kitchen", "Dining Room", "Washrooms", "Store", "Garage", "Veranda", "Other Custom Room")
        "Market" -> listOf("Shops", "Offices", "Washrooms", "Common Areas", "Stairs", "Parking", "Other Custom Room")
        "Plaza" -> listOf("Shops", "Offices", "Apartments", "Common Areas", "Washrooms", "Stairs", "Lift Area", "Parking", "Other Custom Room")
        "Guest House" -> listOf("Guest Rooms", "Attached Washrooms", "TV Lounge", "Kitchen", "Dining Area", "Reception", "Common Washrooms", "Veranda", "Parking", "Other Custom Room")
        "Office" -> listOf("Executive Office", "General Office Cabin", "Meeting Room", "Reception", "Kitchenette/Pantry", "Washrooms", "Common Areas", "Server Room", "Other Custom Room")
        "Shop" -> listOf("Retail Floor", "Storage/Backroom", "Washrooms", "Office", "Other Custom Room")
        "Warehouse" -> listOf("Storage Bay", "Loading Dock", "Office", "Washrooms", "Guard Room", "Utility Area", "Other Custom Room")
        "School" -> listOf("Classroom", "Staff Room", "Science Lab", "Computer Lab", "Library", "Washrooms", "Principal's Office", "Canteen", "Playground Area", "Other Custom Room")
        else -> listOf("Custom Room", "Hall", "Corridor", "Washrooms", "Custom Area")
    }

    var type by remember { mutableStateOf(categories.first()) }
    var showDropdown by remember { mutableStateOf(false) }

    val initialCount = remember(type, existingRooms) { existingRooms.count { it.type == type } }
    var name by remember { mutableStateOf("$type no ${initialCount + 1}") }

    var length by remember { mutableStateOf("") }
    var width by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var thickness by remember { mutableStateOf(if (unitStr == "m") "23" else "9") }
    var materialType by remember { mutableStateOf("Brick") }
    var doorsCount by remember { mutableStateOf("1") }
    val isMetric = unitStr == "m"
    var doorsWidth by remember { mutableStateOf(if (isMetric) "0.9" else "3.0") }
    var doorsHeight by remember { mutableStateOf(if (isMetric) "2.1" else "7.0") }
    var windowsCount by remember { mutableStateOf("1") }
    var windowsWidth by remember { mutableStateOf(if (isMetric) "1.2" else "4.0") }
    var windowsHeight by remember { mutableStateOf(if (isMetric) "1.2" else "4.0") }
    var hasRoof by remember { mutableStateOf(true) }
    var quantity by remember { mutableStateOf("1") }

    var isWashroomEnabled by remember { mutableStateOf(false) }
    var washroomLength by remember { mutableStateOf(if (unitStr == "m") "2.4" else "8.0") }
    var washroomWidth by remember { mutableStateOf(if (unitStr == "m") "1.5" else "5.0") }
    var washroomHeight by remember { mutableStateOf(if (unitStr == "m") "2.4" else "8.0") }
    var washroomThickness by remember { mutableStateOf(if (unitStr == "m") "11.5" else "4.5") }
    var washroomDoorsCount by remember { mutableStateOf("1") }
    var washroomDoorsWidth by remember { mutableStateOf(if (isMetric) "0.75" else "2.5") }
    var washroomDoorsHeight by remember { mutableStateOf(if (isMetric) "2.0" else "6.5") }
    var washroomWindowsCount by remember { mutableStateOf("1") }
    var washroomWindowsWidth by remember { mutableStateOf(if (isMetric) "0.6" else "2.0") }
    var washroomWindowsHeight by remember { mutableStateOf(if (isMetric) "0.6" else "2.0") }
    var washroomMaterialType by remember { mutableStateOf("Brick") }

    // Validation State
    var isValidationActive by remember { mutableStateOf(false) }
    var nameError by remember { mutableStateOf<String?>(null) }
    var lengthError by remember { mutableStateOf<String?>(null) }
    var widthError by remember { mutableStateOf<String?>(null) }
    var heightError by remember { mutableStateOf<String?>(null) }
    var thicknessError by remember { mutableStateOf<String?>(null) }
    var quantityError by remember { mutableStateOf<String?>(null) }
    var doorsCountError by remember { mutableStateOf<String?>(null) }
    var doorsWidthError by remember { mutableStateOf<String?>(null) }
    var doorsHeightError by remember { mutableStateOf<String?>(null) }
    var windowsCountError by remember { mutableStateOf<String?>(null) }
    var windowsWidthError by remember { mutableStateOf<String?>(null) }
    var windowsHeightError by remember { mutableStateOf<String?>(null) }

    var washroomLengthError by remember { mutableStateOf<String?>(null) }
    var washroomWidthError by remember { mutableStateOf<String?>(null) }
    var washroomHeightError by remember { mutableStateOf<String?>(null) }
    var washroomThicknessError by remember { mutableStateOf<String?>(null) }
    var washroomDoorsCountError by remember { mutableStateOf<String?>(null) }
    var washroomDoorsWidthError by remember { mutableStateOf<String?>(null) }
    var washroomDoorsHeightError by remember { mutableStateOf<String?>(null) }
    var washroomWindowsCountError by remember { mutableStateOf<String?>(null) }
    var washroomWindowsWidthError by remember { mutableStateOf<String?>(null) }
    var washroomWindowsHeightError by remember { mutableStateOf<String?>(null) }

    val focusRequesters = remember { List(22) { FocusRequester() } }

    fun validate(): Boolean {
        var isValid = true
        var firstInvalidIdx = -1

        fun check(idx: Int, value: String, errorSetter: (String?) -> Unit, validator: (String) -> com.example.paintcalculatorestimator.utils.ValidationResult) {
            val res = validator(value)
            if (res is com.example.paintcalculatorestimator.utils.ValidationResult.Invalid) {
                errorSetter(res.message)
                if (isValid) {
                    isValid = false
                    firstInvalidIdx = idx
                }
            } else {
                errorSetter(null)
            }
        }

        check(0, name, { nameError = it }) { com.example.paintcalculatorestimator.utils.Validator.validateRequiredField(it, "name") }
        check(1, length, { lengthError = it }) { com.example.paintcalculatorestimator.utils.Validator.validatePositiveNumber(it, "length") }
        check(2, width, { widthError = it }) { com.example.paintcalculatorestimator.utils.Validator.validatePositiveNumber(it, "width") }
        check(3, height, { heightError = it }) { com.example.paintcalculatorestimator.utils.Validator.validatePositiveNumber(it, "wall height") }
        check(4, thickness, { thicknessError = it }) { com.example.paintcalculatorestimator.utils.Validator.validatePositiveNumber(it, "wall thickness") }
        check(5, quantity, { quantityError = it }) { com.example.paintcalculatorestimator.utils.Validator.validatePositiveInteger(it, "quantity") }
        check(6, doorsCount, { doorsCountError = it }) { com.example.paintcalculatorestimator.utils.Validator.validateNonNegativeInteger(it, "doors count") }
        if ((doorsCount.toIntOrNull() ?: 0) > 0) {
            check(7, doorsWidth, { doorsWidthError = it }) { com.example.paintcalculatorestimator.utils.Validator.validatePositiveNumber(it, "door width") }
            check(8, doorsHeight, { doorsHeightError = it }) { com.example.paintcalculatorestimator.utils.Validator.validatePositiveNumber(it, "door height") }
        } else {
            doorsWidthError = null
            doorsHeightError = null
        }
        check(9, windowsCount, { windowsCountError = it }) { com.example.paintcalculatorestimator.utils.Validator.validateNonNegativeInteger(it, "windows count") }
        if ((windowsCount.toIntOrNull() ?: 0) > 0) {
            check(10, windowsWidth, { windowsWidthError = it }) { com.example.paintcalculatorestimator.utils.Validator.validatePositiveNumber(it, "window width") }
            check(11, windowsHeight, { windowsHeightError = it }) { com.example.paintcalculatorestimator.utils.Validator.validatePositiveNumber(it, "window height") }
        } else {
            windowsWidthError = null
            windowsHeightError = null
        }

        if (isWashroomEnabled) {
            check(12, washroomLength, { washroomLengthError = it }) { com.example.paintcalculatorestimator.utils.Validator.validatePositiveNumber(it, "washroom length") }
            check(13, washroomWidth, { washroomWidthError = it }) { com.example.paintcalculatorestimator.utils.Validator.validatePositiveNumber(it, "washroom width") }
            check(14, washroomHeight, { washroomHeightError = it }) { com.example.paintcalculatorestimator.utils.Validator.validatePositiveNumber(it, "washroom height") }
            check(15, washroomThickness, { washroomThicknessError = it }) { com.example.paintcalculatorestimator.utils.Validator.validatePositiveNumber(it, "washroom wall thickness") }
            check(16, washroomDoorsCount, { washroomDoorsCountError = it }) { com.example.paintcalculatorestimator.utils.Validator.validateNonNegativeInteger(it, "washroom doors count") }
            if ((washroomDoorsCount.toIntOrNull() ?: 0) > 0) {
                check(17, washroomDoorsWidth, { washroomDoorsWidthError = it }) { com.example.paintcalculatorestimator.utils.Validator.validatePositiveNumber(it, "washroom door width") }
                check(18, washroomDoorsHeight, { washroomDoorsHeightError = it }) { com.example.paintcalculatorestimator.utils.Validator.validatePositiveNumber(it, "washroom door height") }
            } else {
                washroomDoorsWidthError = null
                washroomDoorsHeightError = null
            }
            check(19, washroomWindowsCount, { washroomWindowsCountError = it }) { com.example.paintcalculatorestimator.utils.Validator.validateNonNegativeInteger(it, "washroom windows count") }
            if ((washroomWindowsCount.toIntOrNull() ?: 0) > 0) {
                check(20, washroomWindowsWidth, { washroomWindowsWidthError = it }) { com.example.paintcalculatorestimator.utils.Validator.validatePositiveNumber(it, "washroom vent/window width") }
                check(21, washroomWindowsHeight, { washroomWindowsHeightError = it }) { com.example.paintcalculatorestimator.utils.Validator.validatePositiveNumber(it, "washroom vent/window height") }
            } else {
                washroomWindowsWidthError = null
                washroomWindowsHeightError = null
            }
        }

        if (!isValid && firstInvalidIdx != -1) {
            focusRequesters[firstInvalidIdx].requestFocus()
        }

        return isValid
    }

    LaunchedEffect(name, length, width, height, thickness, quantity, doorsCount, doorsWidth, doorsHeight, windowsCount, windowsWidth, windowsHeight,
        isWashroomEnabled, washroomLength, washroomWidth, washroomHeight, washroomThickness, washroomDoorsCount, washroomDoorsWidth, washroomDoorsHeight, washroomWindowsCount, washroomWindowsWidth, washroomWindowsHeight) {
        if (isValidationActive) {
            validate()
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        OutlinedCard(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            shape = MaterialTheme.shapes.large
        ) {
            LazyColumn(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Text("Add Project Area", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
                }

                item {
                    Text("Select Area Category", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Box(modifier = Modifier.fillMaxWidth()) {
                        EstimateTextField(
                            value = type,
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier.fillMaxWidth().clickable { showDropdown = true },
                            trailingIcon = {
                                IconButton(onClick = { showDropdown = true }) {
                                    Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "Dropdown")
                                }
                            }
                        )
                        DropdownMenu(expanded = showDropdown, onDismissRequest = { showDropdown = false }) {
                            categories.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat) },
                                    onClick = {
                                        type = cat
                                        val count = existingRooms.count { it.type == cat }
                                        name = "$cat no ${count + 1}"
                                        showDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }

                item {
                    EstimateTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Name / Label") },
                        modifier = Modifier.fillMaxWidth().focusRequester(focusRequesters[0]),
                        singleLine = true,
                        isError = nameError != null,
                        supportingText = if (nameError != null) { { Text(nameError!!) } } else null
                    )
                }

                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        EstimateTextField(
                            value = length,
                            onValueChange = { length = it },
                            label = { Text("Length ($unitStr)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f).focusRequester(focusRequesters[1]),
                            singleLine = true,
                            isError = lengthError != null,
                            supportingText = if (lengthError != null) { { Text(lengthError!!) } } else null
                        )
                        EstimateTextField(
                            value = width,
                            onValueChange = { width = it },
                            label = { Text("Width ($unitStr)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f).focusRequester(focusRequesters[2]),
                            singleLine = true,
                            isError = widthError != null,
                            supportingText = if (widthError != null) { { Text(widthError!!) } } else null
                        )
                    }
                }

                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        EstimateTextField(
                            value = height,
                            onValueChange = { height = it },
                            label = { Text("Wall Height ($unitStr)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f).focusRequester(focusRequesters[3]),
                            singleLine = true,
                            isError = heightError != null,
                            supportingText = if (heightError != null) { { Text(heightError!!) } } else null
                        )
                        EstimateTextField(
                            value = thickness,
                            onValueChange = { thickness = it },
                            label = { Text("Wall Thickness ($thicknessUnitStr)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f).focusRequester(focusRequesters[4]),
                            singleLine = true,
                            isError = thicknessError != null,
                            supportingText = if (thicknessError != null) { { Text(thicknessError!!) } } else null
                        )
                    }
                }

                item {
                    EstimateTextField(
                        value = quantity,
                        onValueChange = { quantity = it },
                        label = { Text("Quantity (Number of identical areas)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth().focusRequester(focusRequesters[5]),
                        singleLine = true,
                        isError = quantityError != null,
                        supportingText = if (quantityError != null) { { Text(quantityError!!) } } else null
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Wall Material", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = materialType == "Brick", onClick = { materialType = "Brick" })
                            Text("Brick", modifier = Modifier.clickable { materialType = "Brick" }, fontSize = 10.sp)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = materialType == "Block", onClick = { materialType = "Block" })
                            Text("Block", modifier = Modifier.clickable { materialType = "Block" }, fontSize = 10.sp)
                        }
                    }
                }

                item {
                    Text("Openings (Doors & Windows)", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        EstimateTextField(
                            value = doorsCount,
                            onValueChange = { doorsCount = it },
                            label = { Text("Doors Count") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f).focusRequester(focusRequesters[6]),
                            singleLine = true,
                            isError = doorsCountError != null,
                            supportingText = if (doorsCountError != null) { { Text(doorsCountError!!) } } else null
                        )
                        EstimateTextField(
                            value = windowsCount,
                            onValueChange = { windowsCount = it },
                            label = { Text("Windows Count") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f).focusRequester(focusRequesters[9]),
                            singleLine = true,
                            isError = windowsCountError != null,
                            supportingText = if (windowsCountError != null) { { Text(windowsCountError!!) } } else null
                        )
                    }
                }

                if ((doorsCount.toIntOrNull() ?: 0) > 0) {
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            EstimateTextField(
                                value = doorsWidth,
                                onValueChange = { doorsWidth = it },
                                label = { Text("Door Width ($unitStr)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f).focusRequester(focusRequesters[7]),
                                singleLine = true,
                                isError = doorsWidthError != null,
                                supportingText = if (doorsWidthError != null) { { Text(doorsWidthError!!) } } else null
                            )
                            EstimateTextField(
                                value = doorsHeight,
                                onValueChange = { doorsHeight = it },
                                label = { Text("Door Height ($unitStr)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f).focusRequester(focusRequesters[8]),
                                singleLine = true,
                                isError = doorsHeightError != null,
                                supportingText = if (doorsHeightError != null) { { Text(doorsHeightError!!) } } else null
                            )
                        }
                    }
                }

                if ((windowsCount.toIntOrNull() ?: 0) > 0) {
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            EstimateTextField(
                                value = windowsWidth,
                                onValueChange = { windowsWidth = it },
                                label = { Text("Window Width ($unitStr)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f).focusRequester(focusRequesters[10]),
                                singleLine = true,
                                isError = windowsWidthError != null,
                                supportingText = if (windowsWidthError != null) { { Text(windowsWidthError!!) } } else null
                            )
                            EstimateTextField(
                                value = windowsHeight,
                                onValueChange = { windowsHeight = it },
                                label = { Text("Window Height ($unitStr)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f).focusRequester(focusRequesters[11]),
                                singleLine = true,
                                isError = windowsHeightError != null,
                                supportingText = if (windowsHeightError != null) { { Text(windowsHeightError!!) } } else null
                            )
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(checked = hasRoof, onCheckedChange = { hasRoof = it })
                        Text("Include Slab / Roof Concrete", modifier = Modifier.clickable { hasRoof = !hasRoof }, fontSize = 13.sp)
                    }
                }

                item {
                    OutlinedCard(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(checked = isWashroomEnabled, onCheckedChange = { isWashroomEnabled = it })
                                Text("Attached Washroom option", fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.clickable { isWashroomEnabled = !isWashroomEnabled })
                            }

                            if (isWashroomEnabled) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    EstimateTextField(
                                        value = washroomLength,
                                        onValueChange = { washroomLength = it },
                                        label = { Text("L ($unitStr)") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.weight(1f).focusRequester(focusRequesters[12]),
                                        singleLine = true,
                                        isError = washroomLengthError != null,
                                        supportingText = if (washroomLengthError != null) { { Text(washroomLengthError!!) } } else null
                                    )
                                    EstimateTextField(
                                        value = washroomWidth,
                                        onValueChange = { washroomWidth = it },
                                        label = { Text("W ($unitStr)") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.weight(1f).focusRequester(focusRequesters[13]),
                                        singleLine = true,
                                        isError = washroomWidthError != null,
                                        supportingText = if (washroomWidthError != null) { { Text(washroomWidthError!!) } } else null
                                    )
                                    EstimateTextField(
                                        value = washroomHeight,
                                        onValueChange = { washroomHeight = it },
                                        label = { Text("H ($unitStr)") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.weight(1f).focusRequester(focusRequesters[14]),
                                        singleLine = true,
                                        isError = washroomHeightError != null,
                                        supportingText = if (washroomHeightError != null) { { Text(washroomHeightError!!) } } else null
                                    )
                                }
                                EstimateTextField(
                                    value = washroomThickness,
                                    onValueChange = { washroomThickness = it },
                                    label = { Text("Washroom Wall Thickness ($thicknessUnitStr)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.fillMaxWidth().focusRequester(focusRequesters[15]),
                                    singleLine = true,
                                    isError = washroomThicknessError != null,
                                    supportingText = if (washroomThicknessError != null) { { Text(washroomThicknessError!!) } } else null
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    EstimateTextField(
                                        value = washroomDoorsCount,
                                        onValueChange = { washroomDoorsCount = it },
                                        label = { Text("Dr Count") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.weight(1f).focusRequester(focusRequesters[16]),
                                        singleLine = true,
                                        isError = washroomDoorsCountError != null,
                                        supportingText = if (washroomDoorsCountError != null) { { Text(washroomDoorsCountError!!) } } else null
                                    )
                                    EstimateTextField(
                                        value = washroomWindowsCount,
                                        onValueChange = { washroomWindowsCount = it },
                                        label = { Text("Wd Count") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.weight(1f).focusRequester(focusRequesters[19]),
                                        singleLine = true,
                                        isError = washroomWindowsCountError != null,
                                        supportingText = if (washroomWindowsCountError != null) { { Text(washroomWindowsCountError!!) } } else null
                                    )
                                }

                                if ((washroomDoorsCount.toIntOrNull() ?: 0) > 0) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        EstimateTextField(
                                            value = washroomDoorsWidth,
                                            onValueChange = { washroomDoorsWidth = it },
                                            label = { Text("Dr Width ($unitStr)") },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            modifier = Modifier.weight(1f).focusRequester(focusRequesters[17]),
                                            singleLine = true,
                                            isError = washroomDoorsWidthError != null,
                                            supportingText = if (washroomDoorsWidthError != null) { { Text(washroomDoorsWidthError!!) } } else null
                                        )
                                        EstimateTextField(
                                            value = washroomDoorsHeight,
                                            onValueChange = { washroomDoorsHeight = it },
                                            label = { Text("Dr Height ($unitStr)") },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            modifier = Modifier.weight(1f).focusRequester(focusRequesters[18]),
                                            singleLine = true,
                                            isError = washroomDoorsHeightError != null,
                                            supportingText = if (washroomDoorsHeightError != null) { { Text(washroomDoorsHeightError!!) } } else null
                                        )
                                    }
                                }

                                if ((washroomWindowsCount.toIntOrNull() ?: 0) > 0) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        EstimateTextField(
                                            value = washroomWindowsWidth,
                                            onValueChange = { washroomWindowsWidth = it },
                                            label = { Text("Vent/Win Width ($unitStr)") },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            modifier = Modifier.weight(1f).focusRequester(focusRequesters[20]),
                                            singleLine = true,
                                            isError = washroomWindowsWidthError != null,
                                            supportingText = if (washroomWindowsWidthError != null) { { Text(washroomWindowsWidthError!!) } } else null
                                        )
                                        EstimateTextField(
                                            value = washroomWindowsHeight,
                                            onValueChange = { washroomWindowsHeight = it },
                                            label = { Text("Vent/Win Height ($unitStr)") },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            modifier = Modifier.weight(1f).focusRequester(focusRequesters[21]),
                                            singleLine = true,
                                            isError = washroomWindowsHeightError != null,
                                            supportingText = if (washroomWindowsHeightError != null) { { Text(washroomWindowsHeightError!!) } } else null
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Washroom Material", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        RadioButton(selected = washroomMaterialType == "Brick", onClick = { washroomMaterialType = "Brick" })
                                        Text("Brick", modifier = Modifier.clickable { washroomMaterialType = "Brick" }, fontSize = 12.sp)
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        RadioButton(selected = washroomMaterialType == "Block", onClick = { washroomMaterialType = "Block" })
                                        Text("Block", modifier = Modifier.clickable { washroomMaterialType = "Block" }, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onDismiss) { Text("Cancel") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                isValidationActive = true
                                if (validate()) {
                                    onAdd(
                                        HouseRoomInput(
                                            name = name,
                                            type = type,
                                            length = length.toDouble(),
                                            width = width.toDouble(),
                                            height = height.toDouble(),
                                            thickness = thickness.toDouble(),
                                            materialType = materialType,
                                            doorsCount = doorsCount.toInt(),
                                            doorsWidth = doorsWidth.toDoubleOrNull() ?: if (isMetric) 0.9 else 3.0,
                                            doorsHeight = doorsHeight.toDoubleOrNull() ?: if (isMetric) 2.1 else 7.0,
                                            windowsCount = windowsCount.toInt(),
                                            windowsWidth = windowsWidth.toDoubleOrNull() ?: if (isMetric) 1.2 else 4.0,
                                            windowsHeight = windowsHeight.toDoubleOrNull() ?: if (isMetric) 1.2 else 4.0,
                                            hasRoof = hasRoof,
                                            isWashroomEnabled = isWashroomEnabled,
                                            washroomLength = washroomLength.toDoubleOrNull() ?: 0.0,
                                            washroomWidth = washroomWidth.toDoubleOrNull() ?: 0.0,
                                            washroomHeight = washroomHeight.toDoubleOrNull() ?: 0.0,
                                            washroomThickness = washroomThickness.toDoubleOrNull() ?: 0.0,
                                            washroomDoorsCount = washroomDoorsCount.toIntOrNull() ?: 0,
                                            washroomDoorsWidth = washroomDoorsWidth.toDoubleOrNull() ?: if (isMetric) 0.75 else 2.5,
                                            washroomDoorsHeight = washroomDoorsHeight.toDoubleOrNull() ?: if (isMetric) 2.0 else 6.5,
                                            washroomWindowsCount = washroomWindowsCount.toIntOrNull() ?: 0,
                                            washroomWindowsWidth = washroomWindowsWidth.toDoubleOrNull() ?: if (isMetric) 0.6 else 2.0,
                                            washroomWindowsHeight = washroomWindowsHeight.toDoubleOrNull() ?: if (isMetric) 0.6 else 2.0,
                                            washroomMaterialType = washroomMaterialType,
                                            quantity = quantity.toInt()
                                        )
                                    )
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
}

@Composable
fun BoundaryWallStep(viewModel: ProjectViewModel, getFocusRequester: (String) -> FocusRequester) {
    val enabled by viewModel.isBoundaryWallEnabled.collectAsState()
    val length by viewModel.boundaryWallLength.collectAsState()
    val height by viewModel.boundaryWallHeight.collectAsState()
    val thickness by viewModel.boundaryWallThickness.collectAsState()
    val gWidth by viewModel.gateWidth.collectAsState()
    val gHeight by viewModel.gateHeight.collectAsState()
    val matType by viewModel.boundaryMaterialType.collectAsState()
    val ut by viewModel.unitType.collectAsState()
    val lengthError by viewModel.boundaryWallLengthError.collectAsState()
    val heightError by viewModel.boundaryWallHeightError.collectAsState()
    val thicknessError by viewModel.boundaryWallThicknessError.collectAsState()
    val gateWidthError by viewModel.gateWidthError.collectAsState()
    val gateHeightError by viewModel.gateHeightError.collectAsState()
    val unitStr = if (ut == UnitType.METRIC) "m" else "ft"
    val thicknessUnitStr = if (ut == UnitType.METRIC) "cm" else "in"
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Text("Boundary walls", style = MaterialTheme.typography.headlineSmall)
            Text("Include the outer compound or boundary wall if it is part of this project.",
                Modifier.padding(top = 6.dp), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item {
            EstimatePanel {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    IconTile(EstimatorSymbol.HOUSE)
                    Column(Modifier.weight(1f)) {
                        Text("Include boundary wall", style = MaterialTheme.typography.titleSmall)
                        Text("Materials and concrete pillars", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(enabled, onCheckedChange = { viewModel.isBoundaryWallEnabled.value = it },
                        modifier = Modifier.semantics { contentDescription = "Include boundary wall" })
                }
            }
        }
        if (enabled) {
            item {
                EstimatePanel {
                    Text("DIMENSIONS", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    EstimateTextField(length, { viewModel.boundaryWallLength.value = it }, label = { Text("Perimeter length ($unitStr)") },
                        modifier = Modifier.fillMaxWidth().focusRequester(getFocusRequester("boundaryWallLength")),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true,
                        isError = lengthError != null, supportingText = lengthError?.let { { Text(it) } })
                    EstimateTextField(height, { viewModel.boundaryWallHeight.value = it }, label = { Text("Wall height ($unitStr)") },
                        modifier = Modifier.fillMaxWidth().focusRequester(getFocusRequester("boundaryWallHeight")),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true,
                        isError = heightError != null, supportingText = heightError?.let { { Text(it) } })
                    EstimateTextField(thickness, { viewModel.boundaryWallThickness.value = it }, label = { Text("Wall thickness ($thicknessUnitStr)") },
                        modifier = Modifier.fillMaxWidth().focusRequester(getFocusRequester("boundaryWallThickness")),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true,
                        isError = thicknessError != null, supportingText = thicknessError?.let { { Text(it) } })
                }
            }
            item {
                EstimatePanel {
                    Text("MATERIAL", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        listOf("Brick", "Block").forEach { material ->
                            Surface(modifier = Modifier.weight(1f).selectable(matType == material, role = Role.RadioButton) { viewModel.boundaryMaterialType.value = material },
                                color = if (matType == material) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                                shape = RoundedCornerShape(14.dp), border = BorderStroke(1.dp,
                                    if (matType == material) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant)) {
                                Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    EstimatorIcon(EstimatorSymbol.WALL, tint = if (material == "Brick") Color(0xFFB75F3D) else MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(if (material == "Block") "Concrete block" else material, style = MaterialTheme.typography.labelLarge)
                                }
                            }
                        }
                    }
                    Text("Gate opening", style = MaterialTheme.typography.titleSmall)
                    Text("Deduct the gate from the wall area.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        EstimateTextField(gWidth, { viewModel.gateWidth.value = it }, label = { Text("Width ($unitStr)") },
                            modifier = Modifier.weight(1f).focusRequester(getFocusRequester("gateWidth")),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true,
                            isError = gateWidthError != null, supportingText = gateWidthError?.let { { Text(it) } })
                        EstimateTextField(gHeight, { viewModel.gateHeight.value = it }, label = { Text("Height ($unitStr)") },
                            modifier = Modifier.weight(1f).focusRequester(getFocusRequester("gateHeight")),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true,
                            isError = gateHeightError != null, supportingText = gateHeightError?.let { { Text(it) } })
                    }
                }
            }
        }
    }
}

@Composable
fun MaterialPricingStep(
    viewModel: ProjectViewModel,
    getFocusRequester: (String) -> FocusRequester
) {
    val ut by viewModel.unitType.collectAsState()
    val volUnitStr = if (ut == UnitType.METRIC) "m³" else "cft"
    val thicknessUnitStr = if (ut == UnitType.METRIC) "cm" else "in"

    val prBrick by viewModel.brickTripPrice.collectAsState()
    val qtyBrick by viewModel.brickTripQty.collectAsState()
    val brL by viewModel.brickLength.collectAsState()
    val brW by viewModel.brickWidth.collectAsState()
    val brH by viewModel.brickHeight.collectAsState()

    val prBlock by viewModel.blockTripPrice.collectAsState()
    val qtyBlock by viewModel.blockTripQty.collectAsState()
    val blL by viewModel.blockLength.collectAsState()
    val blW by viewModel.blockWidth.collectAsState()
    val blH by viewModel.blockHeight.collectAsState()

    val prCement by viewModel.cementTripPrice.collectAsState()
    val qtyCement by viewModel.cementTripQty.collectAsState()

    val prSand by viewModel.sandTripPrice.collectAsState()
    val qtySand by viewModel.sandTripQty.collectAsState()

    val prAgg by viewModel.aggregateTripPrice.collectAsState()
    val qtyAgg by viewModel.aggregateTripQty.collectAsState()

    val prSteel by viewModel.steelTripPrice.collectAsState()
    val qtySteel by viewModel.steelTripQty.collectAsState()

    val prTile by viewModel.tilePricePerSqFt.collectAsState()
    val prPlumb by viewModel.plumbingFixtureCost.collectAsState()

    // Validation state flows
    val brickTripPriceError by viewModel.brickTripPriceError.collectAsState()
    val blockTripPriceError by viewModel.blockTripPriceError.collectAsState()
    val cementTripPriceError by viewModel.cementTripPriceError.collectAsState()
    val sandTripPriceError by viewModel.sandTripPriceError.collectAsState()
    val sandTripQtyError by viewModel.sandTripQtyError.collectAsState()
    val aggregateTripPriceError by viewModel.aggregateTripPriceError.collectAsState()
    val aggregateTripQtyError by viewModel.aggregateTripQtyError.collectAsState()
    val steelTripPriceError by viewModel.steelTripPriceError.collectAsState()
    val tilePricePerSqFtError by viewModel.tilePricePerSqFtError.collectAsState()
    val plumbingFixtureCostError by viewModel.plumbingFixtureCostError.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(text = "Set your pricing", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
            Text(text = "Define material unit prices, capacities, and unit dimensions.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        item {
            TripPricingCard(
                title = "Cement (Bags)",
                price = prCement,
                onPriceChange = { viewModel.cementTripPrice.value = it },
                qty = qtyCement,
                onQtyChange = { viewModel.cementTripQty.value = it },
                qtyLabel = "Bags per Trip",
                priceLabel = "Price per Bag ($)",
                priceError = cementTripPriceError,
                priceFocusRequester = getFocusRequester("cementTripPrice")
            )
        }

        item {
            OutlinedCard(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Bricks Configuration", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        EstimateTextField(
                            value = prBrick,
                            onValueChange = { viewModel.brickTripPrice.value = it },
                            label = { Text("Price per Brick ($)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f).focusRequester(getFocusRequester("brickTripPrice")),
                            singleLine = true,
                            isError = brickTripPriceError != null,
                            supportingText = if (brickTripPriceError != null) { { Text(brickTripPriceError!!) } } else null
                        )
                        EstimateTextField(
                            value = qtyBrick,
                            onValueChange = { viewModel.brickTripQty.value = it },
                            label = { Text("Bks per Trip") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                    Text("Brick Dimensions (L x W x H)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        EstimateTextField(
                            value = brL,
                            onValueChange = { viewModel.brickLength.value = it },
                            label = { Text("L ($thicknessUnitStr)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        EstimateTextField(
                            value = brW,
                            onValueChange = { viewModel.brickWidth.value = it },
                            label = { Text("W ($thicknessUnitStr)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        EstimateTextField(
                            value = brH,
                            onValueChange = { viewModel.brickHeight.value = it },
                            label = { Text("H ($thicknessUnitStr)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                }
            }
        }

        item {
            OutlinedCard(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Concrete Blocks Configuration", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        EstimateTextField(
                            value = prBlock,
                            onValueChange = { viewModel.blockTripPrice.value = it },
                            label = { Text("Price per Block ($)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f).focusRequester(getFocusRequester("blockTripPrice")),
                            singleLine = true,
                            isError = blockTripPriceError != null,
                            supportingText = if (blockTripPriceError != null) { { Text(blockTripPriceError!!) } } else null
                        )
                        EstimateTextField(
                            value = qtyBlock,
                            onValueChange = { viewModel.blockTripQty.value = it },
                            label = { Text("Blks per Trip") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                    Text("Block Dimensions (L x W x H)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        EstimateTextField(
                            value = blL,
                            onValueChange = { viewModel.blockLength.value = it },
                            label = { Text("L ($thicknessUnitStr)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        EstimateTextField(
                            value = blW,
                            onValueChange = { viewModel.blockWidth.value = it },
                            label = { Text("W ($thicknessUnitStr)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        EstimateTextField(
                            value = blH,
                            onValueChange = { viewModel.blockHeight.value = it },
                            label = { Text("H ($thicknessUnitStr)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                }
            }
        }

        item {
            TripPricingCard(
                title = "Sand ($volUnitStr)",
                price = prSand,
                onPriceChange = { viewModel.sandTripPrice.value = it },
                qty = qtySand,
                onQtyChange = { viewModel.sandTripQty.value = it },
                qtyLabel = "$volUnitStr per Trip",
                priceLabel = "Price per $volUnitStr ($)",
                priceError = sandTripPriceError,
                qtyError = sandTripQtyError,
                priceFocusRequester = getFocusRequester("sandTripPrice"),
                qtyFocusRequester = getFocusRequester("sandTripQty")
            )
        }

        item {
            TripPricingCard(
                title = "Aggregate / Crush ($volUnitStr)",
                price = prAgg,
                onPriceChange = { viewModel.aggregateTripPrice.value = it },
                qty = qtyAgg,
                onQtyChange = { viewModel.aggregateTripQty.value = it },
                qtyLabel = "$volUnitStr per Trip",
                priceLabel = "Price per $volUnitStr ($)",
                priceError = aggregateTripPriceError,
                qtyError = aggregateTripQtyError,
                priceFocusRequester = getFocusRequester("aggregateTripPrice"),
                qtyFocusRequester = getFocusRequester("aggregateTripQty")
            )
        }

        item {
            TripPricingCard(
                title = "Steel / Rebar (kg)",
                price = prSteel,
                onPriceChange = { viewModel.steelTripPrice.value = it },
                qty = qtySteel,
                onQtyChange = { viewModel.steelTripQty.value = it },
                qtyLabel = "kg per Trip",
                priceLabel = "Price per kg ($)",
                priceError = steelTripPriceError,
                priceFocusRequester = getFocusRequester("steelTripPrice")
            )
        }

        item {
            OutlinedCard(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Tiling & plumbing Unit Pricing", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    EstimateTextField(
                        value = prTile,
                        onValueChange = { viewModel.tilePricePerSqFt.value = it },
                        label = { Text("Tile Price per ${if (ut == UnitType.METRIC) "m²" else "sq ft"} ($)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth().focusRequester(getFocusRequester("tilePricePerSqFt")),
                        singleLine = true,
                        isError = tilePricePerSqFtError != null,
                        supportingText = if (tilePricePerSqFtError != null) { { Text(tilePricePerSqFtError!!) } } else null
                    )
                    EstimateTextField(
                        value = prPlumb,
                        onValueChange = { viewModel.plumbingFixtureCost.value = it },
                        label = { Text("Plumbing & Bathroom Fixtures Package Cost ($)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth().focusRequester(getFocusRequester("plumbingFixtureCost")),
                        singleLine = true,
                        isError = plumbingFixtureCostError != null,
                        supportingText = if (plumbingFixtureCostError != null) { { Text(plumbingFixtureCostError!!) } } else null
                    )
                }
            }
        }
    }
}

@Composable
fun TripPricingCard(
    title: String,
    price: String,
    onPriceChange: (String) -> Unit,
    qty: String,
    onQtyChange: (String) -> Unit,
    qtyLabel: String,
    priceLabel: String = "Price per Unit ($)",
    priceError: String? = null,
    qtyError: String? = null,
    priceFocusRequester: FocusRequester? = null,
    qtyFocusRequester: FocusRequester? = null
) {
    OutlinedCard(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                EstimateTextField(
                    value = price,
                    onValueChange = onPriceChange,
                    label = { Text(priceLabel) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f).let { if (priceFocusRequester != null) it.focusRequester(priceFocusRequester) else it },
                    singleLine = true,
                    isError = priceError != null,
                    supportingText = if (priceError != null) { { Text(priceError) } } else null
                )
                EstimateTextField(
                    value = qty,
                    onValueChange = onQtyChange,
                    label = { Text(qtyLabel) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f).let { if (qtyFocusRequester != null) it.focusRequester(qtyFocusRequester) else it },
                    singleLine = true,
                    isError = qtyError != null,
                    supportingText = if (qtyError != null) { { Text(qtyError) } } else null
                )
            }
        }
    }
}

@Composable
fun HouseEstimateResultStep(
    viewModel: ProjectViewModel,
    currency: String,
    navController: NavController
) {
    val context = LocalContext.current
    val isBoundaryWallEnabled by viewModel.isBoundaryWallEnabled.collectAsState()
    val totalFloorAreaSqFt by viewModel.totalFloorAreaSqFt.collectAsState()
    val projectType by viewModel.projectType.collectAsState()

    val cementBags by viewModel.totalCement.collectAsState()
    val sandQty by viewModel.totalSand.collectAsState()
    val aggregateQty by viewModel.totalAggregate.collectAsState()
    val steelQty by viewModel.totalSteel.collectAsState()
    val brickQty by viewModel.totalBricks.collectAsState()
    val blockQty by viewModel.totalBlocks.collectAsState()
    val tileArea by viewModel.totalTileArea.collectAsState()

    val cementCost by viewModel.cementCost.collectAsState()
    val sandCost by viewModel.sandCost.collectAsState()
    val aggregateCost by viewModel.aggregateCost.collectAsState()
    val steelCost by viewModel.steelCost.collectAsState()
    val brickCost by viewModel.brickCost.collectAsState()
    val blockCost by viewModel.blockCost.collectAsState()
    val tilesCost by viewModel.tilesCost.collectAsState()
    val laborCost by viewModel.laborCost.collectAsState()
    val boundaryWallCost by viewModel.boundaryWallCost.collectAsState()
    val plumbingCost by viewModel.plumbingFixtureCost.collectAsState()
    val grandTotal by viewModel.grandTotal.collectAsState()

    val roomsList by viewModel.roomEstimates.collectAsState()
    val ut by viewModel.unitType.collectAsState()
    val volUnitStr = if (ut == UnitType.METRIC) "m³" else "cft"

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(text = "Review & save", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = MaterialTheme.colorScheme.primary)
        }

        item {
            OutlinedCard(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Applicable Floor Area", color = MaterialTheme.colorScheme.onPrimaryContainer, fontSize = 14.sp)
                        Text("${String.format(Locale.US, "%.1f", totalFloorAreaSqFt)} sq ft", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer, fontSize = 16.sp)
                    }
                    Divider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f))
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

        item {
            Text("Individual Room/Area Estimates", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.primary)
        }

        items(roomsList.size) { index ->
            val rm = roomsList[index]
            OutlinedCard(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (rm.quantityMultiplier > 1) "${rm.roomType}: ${rm.roomName} (x${rm.quantityMultiplier})" else "${rm.roomType}: ${rm.roomName}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(text = "$currency ${String.format(Locale.US, "%,.2f", rm.totalCost)}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 14.sp)
                    }
                    Divider()
                    Text("Calculated Materials:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    if (rm.bricksCount > 0) Text("- Bricks: ${rm.bricksCount} pcs", fontSize = 11.sp)
                    if (rm.blocksCount > 0) Text("- Blocks: ${rm.blocksCount} pcs", fontSize = 11.sp)
                    Text("- Cement: ${String.format(Locale.US, "%.1f Bags", rm.cementBags)}", fontSize = 11.sp)
                    Text("- Sand: ${String.format(Locale.US, "%.2f %s", rm.sandVol, volUnitStr)}", fontSize = 11.sp)
                    if (rm.hasRoof) {
                        Text("- Slab Aggregates: ${String.format(Locale.US, "%.2f %s", rm.slabAggVol, volUnitStr)}", fontSize = 11.sp)
                        Text("- Slab Rebar Steel: ${String.format(Locale.US, "%.1f kg", rm.slabSteelKg)}", fontSize = 11.sp)
                    }
                    if (rm.hasWashroom) {
                        Text("- Attached Washroom Tile: ${String.format(Locale.US, "%.1f sq ft", rm.washroomTilesSqFt)} ($currency ${String.format(Locale.US, "%,.0f", rm.washroomTilesCost)})", fontSize = 11.sp)
                        if (rm.washroomBricksCount > 0) {
                            Text("- Washroom Bricks: ${rm.washroomBricksCount} pcs", fontSize = 11.sp)
                        }
                        if (rm.washroomBlocksCount > 0) {
                            Text("- Washroom Blocks: ${rm.washroomBlocksCount} pcs", fontSize = 11.sp)
                        }
                        Text("- Washroom Cement/Sand: ${String.format(Locale.US, "%.1f Bags Cement", rm.washroomCementBags)} / ${String.format(Locale.US, "%.1f %s Sand", rm.washroomSandVol, volUnitStr)}", fontSize = 11.sp)
                    }
                }
            }
        }

        item {
            Text("Material Purchases (Trip/Load Breakdown)", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.primary)
        }

        item {
            OutlinedCard(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Material", fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(2f))
                        Text("Req", fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(1.5f), textAlign = TextAlign.Right)
                        Text("Trips", fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.Right)
                        Text("Price/Unit", fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(1.5f), textAlign = TextAlign.Right)
                        Text("Total", fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(1.5f), textAlign = TextAlign.Right)
                    }
                    Divider()

                    if (cementBags > 0) {
                        TableRowItem(
                            name = "Cement",
                            req = "${cementBags.toInt()} bags",
                            trips = viewModel.cementTrips.value.toString(),
                            price = "$currency ${viewModel.cementTripPrice.value} /bag",
                            total = "$currency ${String.format(Locale.US, "%,.0f", cementCost)}"
                        )
                    }

                    if (brickQty > 0) {
                        TableRowItem(
                            name = "Bricks",
                            req = "$brickQty pcs",
                            trips = viewModel.brickTrips.value.toString(),
                            price = "$currency ${viewModel.brickTripPrice.value} /pc",
                            total = "$currency ${String.format(Locale.US, "%,.0f", brickCost)}"
                        )
                    }

                    if (blockQty > 0) {
                        TableRowItem(
                            name = "Blocks",
                            req = "$blockQty pcs",
                            trips = viewModel.blockTrips.value.toString(),
                            price = "$currency ${viewModel.blockTripPrice.value} /pc",
                            total = "$currency ${String.format(Locale.US, "%,.0f", blockCost)}"
                        )
                    }

                    if (sandQty > 0) {
                        TableRowItem(
                            name = "Sand",
                            req = "${String.format(Locale.US, "%.0f", sandQty)} $volUnitStr",
                            trips = viewModel.sandTrips.value.toString(),
                            price = "$currency ${viewModel.sandTripPrice.value} /$volUnitStr",
                            total = "$currency ${String.format(Locale.US, "%,.0f", sandCost)}"
                        )
                    }

                    if (aggregateQty > 0) {
                        TableRowItem(
                            name = "Crush/Agg",
                            req = "${String.format(Locale.US, "%.0f", aggregateQty)} $volUnitStr",
                            trips = viewModel.aggregateTrips.value.toString(),
                            price = "$currency ${viewModel.aggregateTripPrice.value} /$volUnitStr",
                            total = "$currency ${String.format(Locale.US, "%,.0f", aggregateCost)}"
                        )
                    }

                    if (steelQty > 0) {
                        TableRowItem(
                            name = "Steel",
                            req = "${steelQty.toInt()} kg",
                            trips = viewModel.steelTrips.value.toString(),
                            price = "$currency ${viewModel.steelTripPrice.value} /kg",
                            total = "$currency ${String.format(Locale.US, "%,.0f", steelCost)}"
                        )
                    }

                    if (tileArea > 0) {
                        TableRowItem(
                            name = "Tiles",
                            req = "${tileArea.toInt()} sq ft",
                            trips = "-",
                            price = "$currency ${viewModel.tilePricePerSqFt.value}/sqft",
                            total = "$currency ${String.format(Locale.US, "%,.0f", tilesCost)}"
                        )
                    }
                }
            }
        }

        item {
            OutlinedCard(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Overall Expense Summary", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Divider()
                    RowSummary(label = "Rooms Construction Subtotal", value = "$currency ${String.format(Locale.US, "%,.2f", roomsList.sumOf { it.totalCost })}")
                    RowSummary(label = "Boundary Wall Subtotal", value = "$currency ${String.format(Locale.US, "%,.2f", boundaryWallCost)}")
                    RowSummary(label = "Labour Cost", value = "$currency ${String.format(Locale.US, "%,.2f", laborCost)}")
                    if (plumbingCost.toDoubleOrNull() ?: 0.0 > 0) {
                        val washroomCount = roomsList.sumOf { it.quantityMultiplier }
                        RowSummary(label = "Plumbing ($washroomCount washrooms)", value = "$currency ${String.format(Locale.US, "%,.2f", plumbingCost.toDoubleOrNull()!! * washroomCount)}")
                    }
                }
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        viewModel.saveProject { savedId ->
                            Toast.makeText(context, "Project estimate saved successfully!", Toast.LENGTH_SHORT).show()
                            navController.popBackStack()
                        }
                    },
                    modifier = Modifier.weight(1f).height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Save Project Plan")
                }

                IconButton(
                    onClick = {
                        var textStr = "Project Estimate Summary ($projectType):\n" +
                                "- Applicable Floor Area: ${String.format(Locale.US, "%.1f", totalFloorAreaSqFt)} sq ft\n" +
                                "- Total Materials: $currency ${String.format(Locale.US, "%,.2f", grandTotal - laborCost)}\n" +
                                "- Total Labour: $currency ${String.format(Locale.US, "%,.2f", laborCost)}\n"
                        if (isBoundaryWallEnabled) {
                            textStr += "- Boundary Wall Cost: $currency ${String.format(Locale.US, "%,.2f", boundaryWallCost)}\n"
                        }
                        textStr += "- Grand Total: $currency ${String.format(Locale.US, "%,.2f", grandTotal)}\n" +
                                "Calculated using Building Estimator App."
                        shareText(context, textStr)
                    },
                    modifier = Modifier.size(50.dp).background(MaterialTheme.colorScheme.surfaceVariant, shape = CircleShape)
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = "Share Estimate Text")
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

@Composable
fun TableRowItem(name: String, req: String, trips: String, price: String, total: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(name, fontSize = 11.sp, modifier = Modifier.weight(2f))
        Text(req, fontSize = 11.sp, modifier = Modifier.weight(1.5f), textAlign = TextAlign.Right)
        Text(trips, fontSize = 11.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.Right, fontWeight = FontWeight.Bold)
        Text(price, fontSize = 11.sp, modifier = Modifier.weight(1.5f), textAlign = TextAlign.Right)
        Text(total, fontSize = 11.sp, modifier = Modifier.weight(1.5f), textAlign = TextAlign.Right, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
fun RowSummary(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
    }
}

private fun shareText(context: android.content.Context, text: String) {
    val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(android.content.Intent.EXTRA_TEXT, text)
    }
    context.startActivity(android.content.Intent.createChooser(intent, "Share Cost Estimate"))
}

@Preview(showBackground = true)
@Composable
fun AddRoomDialogPreview() {
    MaterialTheme {
        AddRoomDialog(
            projectType = "House",
            existingRooms = emptyList(),
            unitStr = "ft",
            thicknessUnitStr = "in",
            onDismiss = {},
            onAdd = {}
        )
    }
}

@Preview(showBackground = true, name = "Project wizard")
@Composable
private fun ProjectWizardScreenPreview() {
    com.example.paintcalculatorestimator.ui.theme.PaintCalculatorEstimatorTheme {
        Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("New project", style = MaterialTheme.typography.headlineSmall)
            WizardStepper(1)
            Text("What are you building?", style = MaterialTheme.typography.headlineMedium)
            Text("Pick a project type to get started with rooms, materials and labor.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            com.example.paintcalculatorestimator.ui.theme.EstimatePanel {
                Text("House", style = MaterialTheme.typography.titleMedium)
                Text("Residential construction estimate", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Button(onClick = {}, modifier = Modifier.fillMaxWidth()) { Text("Continue  ›",
                fontSize = 20.sp) }
        }
    }
}
