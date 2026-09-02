package com.example.paintcalculatorestimator.presentation.project

import android.widget.Toast
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
    val coroutineScope = rememberCoroutineScope()
    val currentStep by viewModel.currentStep.collectAsState()
    val validationError by viewModel.validationError.collectAsState()
    val currency by appPreferences.currencyFlow.collectAsState(initial = "PKR")

    val focusRequesters = remember { mutableMapOf<String, FocusRequester>() }
    fun getFocusRequester(key: String): FocusRequester {
        return focusRequesters.getOrPut(key) { FocusRequester() }
    }

    LaunchedEffect(Unit) {
        viewModel.firstInvalidField.collect { fieldId ->
            focusRequesters[fieldId]?.requestFocus()
        }
    }

    LaunchedEffect(key1 = projectId) {
        if (projectId > 0) {
            viewModel.loadProject(projectId)
        }
    }

    LaunchedEffect(key1 = validationError) {
        validationError?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "House & Building Estimator", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = {
                        if (currentStep > 1) {
                            viewModel.previousStep()
                        } else {
                            navController.popBackStack()
                        }
                    }) {
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            WizardStepper(currentStep = currentStep)
            Spacer(modifier = Modifier.height(16.dp))

            Box(modifier = Modifier.weight(1f)) {
                AnimatedContent(
                    targetState = currentStep,
                    label = "stepTransition"
                ) { step ->
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

            // Bottom Navigation Actions
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (currentStep > 1 && currentStep < 6) {
                    OutlinedButton(
                        onClick = { viewModel.previousStep() },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text(text = "Back")
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                }

                if (currentStep > 1 && currentStep < 6) {
                    Button(
                        onClick = { viewModel.nextStep() },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text(text = "Continue")
                    }
                }
            }
        }
    }
}

@Composable
fun WizardStepper(currentStep: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val steps = listOf("Type", "Info", "Rooms", "Boundary", "Pricing", "Summary")
        steps.forEachIndexed { index, label ->
            val stepNum = index + 1
            val isActive = stepNum == currentStep
            val isCompleted = stepNum < currentStep

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(MaterialTheme.shapes.extraLarge)
                        .background(
                            when {
                                isActive -> MaterialTheme.colorScheme.primary
                                isCompleted -> MaterialTheme.colorScheme.primaryContainer
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isCompleted) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(16.dp)
                        )
                    } else {
                        Text(
                            text = stepNum.toString(),
                            color = if (isActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = label,
                    fontSize = 10.sp,
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                    color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun ProjectTypeSelectionStep(viewModel: ProjectViewModel) {
    val selectedType by viewModel.projectType.collectAsState()
    val types = listOf(
        "House", "Market", "Plaza", "Guest House", 
        "Office", "Shop", "Warehouse", "School", "Other / Custom"
    )

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "What do you want to build?",
            fontWeight = FontWeight.ExtraBold,
            fontSize = 20.sp,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(types.size) { index ->
                val type = types[index]
                val isSelected = type == selectedType
                
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(
                        width = 2.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .clickable { viewModel.setProjectType(type) }
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = when (type) {
                                    "House" -> Icons.Default.Home
                                    "Market" -> Icons.Default.ShoppingCart
                                    "Plaza" -> Icons.Default.Build
                                    "Guest House" -> Icons.Default.Star
                                    "Office" -> Icons.Default.Email
                                    "Shop" -> Icons.Default.List
                                    "Warehouse" -> Icons.Default.PlayArrow
                                    "School" -> Icons.Default.Info
                                    else -> Icons.Default.Settings
                                },
                                contentDescription = null,
                                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = type,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HouseDetailsStep(
    viewModel: ProjectViewModel,
    getFocusRequester: (String) -> FocusRequester
) {
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

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(text = "Step 2: Project Specifications", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Define basic layout and pricing preferences for the construction.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        item {
            OutlinedTextField(
                value = name,
                onValueChange = { viewModel.projectName.value = it },
                label = { Text("Project Name (e.g. Plaza Phase 1)") },
                modifier = Modifier.fillMaxWidth().focusRequester(getFocusRequester("projectName")),
                singleLine = true,
                isError = projectNameError != null,
                supportingText = if (projectNameError != null) { { Text(projectNameError!!) } } else null
            )
        }

        item {
            OutlinedTextField(
                value = location,
                onValueChange = { viewModel.projectLocation.value = it },
                label = { Text("Location") },
                modifier = Modifier.fillMaxWidth().focusRequester(getFocusRequester("projectLocation")),
                singleLine = true,
                isError = projectLocationError != null,
                supportingText = if (projectLocationError != null) { { Text(projectLocationError!!) } } else null
            )
        }

        item {
            OutlinedTextField(
                value = notes,
                onValueChange = { viewModel.projectNotes.value = it },
                label = { Text("Notes / Description") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Unit System Preference", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = ut == UnitType.METRIC,
                            onClick = { viewModel.unitType.value = UnitType.METRIC },
                            label = { Text("Metric (meters/cm)") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = ut == UnitType.IMPERIAL,
                            onClick = { viewModel.unitType.value = UnitType.IMPERIAL },
                            label = { Text("Imperial (feet/inches)") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        item {
            OutlinedTextField(
                value = globalSlab,
                onValueChange = { viewModel.slabThicknessGlobal.value = it },
                label = { Text("Global Roof Slab Thickness (${if (ut == UnitType.METRIC) "cm" else "in"})") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth().focusRequester(getFocusRequester("slabThicknessGlobal")),
                singleLine = true,
                isError = slabThicknessGlobalError != null,
                supportingText = if (slabThicknessGlobalError != null) { { Text(slabThicknessGlobalError!!) } } else null
            )
        }

        item {
            OutlinedTextField(
                value = laborRate,
                onValueChange = { viewModel.laborRatePerSqFt.value = it },
                label = { Text("Labour Rate per sq ft ($)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth().focusRequester(getFocusRequester("laborRatePerSqFt")),
                singleLine = true,
                isError = laborRatePerSqFtError != null,
                supportingText = if (laborRatePerSqFtError != null) { { Text(laborRatePerSqFtError!!) } } else null
            )
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

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "Step 3: Area/Room Planner", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
                Text(text = "${rooms.size} planning areas added", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Button(onClick = { showAddRoomDialog = true }) {
                Text("+ Add Area")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (rooms.isEmpty()) {
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "No construction areas added yet.\nTap '+ Add Area' to add layouts.", textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.secondary, fontSize = 14.sp)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(rooms.size) { index ->
                    val rm = rooms[index]
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (rm.quantity > 1) "${rm.type}: ${rm.name} (x${rm.quantity})" else "${rm.type}: ${rm.name}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = "Dimensions: ${rm.length} x ${rm.width} x ${rm.height} $unitStr | Thickness: ${rm.thickness} $thicknessUnitStr",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Material: ${rm.materialType} | Roof Slab: ${if (rm.hasRoof) "Yes" else "No"} | Washroom: ${if (rm.isWashroomEnabled) "Yes (${rm.washroomLength}x${rm.washroomWidth}$unitStr)" else "No"}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = { viewModel.removeRoom(index) }) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddRoomDialog) {
        AddRoomDialog(
            projectType = projectType,
            existingRooms = rooms,
            unitStr = unitStr,
            thicknessUnitStr = thicknessUnitStr,
            onDismiss = { showAddRoomDialog = false },
            onAdd = { newRoom ->
                viewModel.addRoom(newRoom)
                showAddRoomDialog = false
            }
        )
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
    var windowsCount by remember { mutableStateOf("1") }
    var hasRoof by remember { mutableStateOf(true) }
    var quantity by remember { mutableStateOf("1") }

    var isWashroomEnabled by remember { mutableStateOf(false) }
    var washroomLength by remember { mutableStateOf(if (unitStr == "m") "2.4" else "8.0") }
    var washroomWidth by remember { mutableStateOf(if (unitStr == "m") "1.5" else "5.0") }
    var washroomHeight by remember { mutableStateOf(if (unitStr == "m") "2.4" else "8.0") }
    var washroomThickness by remember { mutableStateOf(if (unitStr == "m") "11.5" else "4.5") }
    var washroomDoorsCount by remember { mutableStateOf("1") }
    var washroomWindowsCount by remember { mutableStateOf("1") }
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
    var windowsCountError by remember { mutableStateOf<String?>(null) }

    var washroomLengthError by remember { mutableStateOf<String?>(null) }
    var washroomWidthError by remember { mutableStateOf<String?>(null) }
    var washroomHeightError by remember { mutableStateOf<String?>(null) }
    var washroomThicknessError by remember { mutableStateOf<String?>(null) }
    var washroomDoorsCountError by remember { mutableStateOf<String?>(null) }
    var washroomWindowsCountError by remember { mutableStateOf<String?>(null) }

    val focusRequesters = remember { List(14) { FocusRequester() } }

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
        check(7, windowsCount, { windowsCountError = it }) { com.example.paintcalculatorestimator.utils.Validator.validateNonNegativeInteger(it, "windows count") }

        if (isWashroomEnabled) {
            check(8, washroomLength, { washroomLengthError = it }) { com.example.paintcalculatorestimator.utils.Validator.validatePositiveNumber(it, "washroom length") }
            check(9, washroomWidth, { washroomWidthError = it }) { com.example.paintcalculatorestimator.utils.Validator.validatePositiveNumber(it, "washroom width") }
            check(10, washroomHeight, { washroomHeightError = it }) { com.example.paintcalculatorestimator.utils.Validator.validatePositiveNumber(it, "washroom height") }
            check(11, washroomThickness, { washroomThicknessError = it }) { com.example.paintcalculatorestimator.utils.Validator.validatePositiveNumber(it, "washroom wall thickness") }
            check(12, washroomDoorsCount, { washroomDoorsCountError = it }) { com.example.paintcalculatorestimator.utils.Validator.validateNonNegativeInteger(it, "washroom doors count") }
            check(13, washroomWindowsCount, { washroomWindowsCountError = it }) { com.example.paintcalculatorestimator.utils.Validator.validateNonNegativeInteger(it, "washroom windows count") }
        }

        if (!isValid && firstInvalidIdx != -1) {
            focusRequesters[firstInvalidIdx].requestFocus()
        }

        return isValid
    }

    LaunchedEffect(name, length, width, height, thickness, quantity, doorsCount, windowsCount,
        isWashroomEnabled, washroomLength, washroomWidth, washroomHeight, washroomThickness, washroomDoorsCount, washroomWindowsCount) {
        if (isValidationActive) {
            validate()
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
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
                        OutlinedTextField(
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
                    OutlinedTextField(
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
                        OutlinedTextField(
                            value = length,
                            onValueChange = { length = it },
                            label = { Text("Length ($unitStr)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f).focusRequester(focusRequesters[1]),
                            singleLine = true,
                            isError = lengthError != null,
                            supportingText = if (lengthError != null) { { Text(lengthError!!) } } else null
                        )
                        OutlinedTextField(
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
                        OutlinedTextField(
                            value = height,
                            onValueChange = { height = it },
                            label = { Text("Wall Height ($unitStr)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f).focusRequester(focusRequesters[3]),
                            singleLine = true,
                            isError = heightError != null,
                            supportingText = if (heightError != null) { { Text(heightError!!) } } else null
                        )
                        OutlinedTextField(
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
                    OutlinedTextField(
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
                        Text("Wall Material", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = materialType == "Brick", onClick = { materialType = "Brick" })
                            Text("Brick", modifier = Modifier.clickable { materialType = "Brick" }, fontSize = 13.sp)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = materialType == "Block", onClick = { materialType = "Block" })
                            Text("Block", modifier = Modifier.clickable { materialType = "Block" }, fontSize = 13.sp)
                        }
                    }
                }

                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = doorsCount,
                            onValueChange = { doorsCount = it },
                            label = { Text("Doors Count") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f).focusRequester(focusRequesters[6]),
                            singleLine = true,
                            isError = doorsCountError != null,
                            supportingText = if (doorsCountError != null) { { Text(doorsCountError!!) } } else null
                        )
                        OutlinedTextField(
                            value = windowsCount,
                            onValueChange = { windowsCount = it },
                            label = { Text("Windows Count") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f).focusRequester(focusRequesters[7]),
                            singleLine = true,
                            isError = windowsCountError != null,
                            supportingText = if (windowsCountError != null) { { Text(windowsCountError!!) } } else null
                        )
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
                    Card(
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
                                    OutlinedTextField(
                                        value = washroomLength,
                                        onValueChange = { washroomLength = it },
                                        label = { Text("L ($unitStr)") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.weight(1f).focusRequester(focusRequesters[8]),
                                        singleLine = true,
                                        isError = washroomLengthError != null,
                                        supportingText = if (washroomLengthError != null) { { Text(washroomLengthError!!) } } else null
                                    )
                                    OutlinedTextField(
                                        value = washroomWidth,
                                        onValueChange = { washroomWidth = it },
                                        label = { Text("W ($unitStr)") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.weight(1f).focusRequester(focusRequesters[9]),
                                        singleLine = true,
                                        isError = washroomWidthError != null,
                                        supportingText = if (washroomWidthError != null) { { Text(washroomWidthError!!) } } else null
                                    )
                                    OutlinedTextField(
                                        value = washroomHeight,
                                        onValueChange = { washroomHeight = it },
                                        label = { Text("H ($unitStr)") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.weight(1f).focusRequester(focusRequesters[10]),
                                        singleLine = true,
                                        isError = washroomHeightError != null,
                                        supportingText = if (washroomHeightError != null) { { Text(washroomHeightError!!) } } else null
                                    )
                                }
                                OutlinedTextField(
                                    value = washroomThickness,
                                    onValueChange = { washroomThickness = it },
                                    label = { Text("Washroom Wall Thickness ($thicknessUnitStr)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.fillMaxWidth().focusRequester(focusRequesters[11]),
                                    singleLine = true,
                                    isError = washroomThicknessError != null,
                                    supportingText = if (washroomThicknessError != null) { { Text(washroomThicknessError!!) } } else null
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = washroomDoorsCount,
                                        onValueChange = { washroomDoorsCount = it },
                                        label = { Text("Dr Count") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.weight(1f).focusRequester(focusRequesters[12]),
                                        singleLine = true,
                                        isError = washroomDoorsCountError != null,
                                        supportingText = if (washroomDoorsCountError != null) { { Text(washroomDoorsCountError!!) } } else null
                                    )
                                    OutlinedTextField(
                                        value = washroomWindowsCount,
                                        onValueChange = { washroomWindowsCount = it },
                                        label = { Text("Wd Count") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.weight(1f).focusRequester(focusRequesters[13]),
                                        singleLine = true,
                                        isError = washroomWindowsCountError != null,
                                        supportingText = if (washroomWindowsCountError != null) { { Text(washroomWindowsCountError!!) } } else null
                                    )
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Washroom Material", fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
                                            windowsCount = windowsCount.toInt(),
                                            hasRoof = hasRoof,
                                            isWashroomEnabled = isWashroomEnabled,
                                            washroomLength = washroomLength.toDoubleOrNull() ?: 0.0,
                                            washroomWidth = washroomWidth.toDoubleOrNull() ?: 0.0,
                                            washroomHeight = washroomHeight.toDoubleOrNull() ?: 0.0,
                                            washroomThickness = washroomThickness.toDoubleOrNull() ?: 0.0,
                                            washroomDoorsCount = washroomDoorsCount.toIntOrNull() ?: 0,
                                            washroomWindowsCount = washroomWindowsCount.toIntOrNull() ?: 0,
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
fun BoundaryWallStep(
    viewModel: ProjectViewModel,
    getFocusRequester: (String) -> FocusRequester
) {
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

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = enabled, onCheckedChange = { viewModel.isBoundaryWallEnabled.value = it })
                Text(text = "Step 4: Boundary Wall (Optional)", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary, modifier = Modifier.clickable { viewModel.isBoundaryWallEnabled.value = !enabled })
            }
            Text(text = "Enable to calculate bricks/blocks and concrete pillars for the house boundary.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        if (enabled) {
            item {
                OutlinedTextField(
                    value = length,
                    onValueChange = { viewModel.boundaryWallLength.value = it },
                    label = { Text("Boundary Wall Length ($unitStr)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().focusRequester(getFocusRequester("boundaryWallLength")),
                    singleLine = true,
                    isError = lengthError != null,
                    supportingText = if (lengthError != null) { { Text(lengthError!!) } } else null
                )
            }

            item {
                OutlinedTextField(
                    value = height,
                    onValueChange = { viewModel.boundaryWallHeight.value = it },
                    label = { Text("Wall Height ($unitStr)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().focusRequester(getFocusRequester("boundaryWallHeight")),
                    singleLine = true,
                    isError = heightError != null,
                    supportingText = if (heightError != null) { { Text(heightError!!) } } else null
                )
            }

            item {
                OutlinedTextField(
                    value = thickness,
                    onValueChange = { viewModel.boundaryWallThickness.value = it },
                    label = { Text("Wall Thickness ($thicknessUnitStr)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().focusRequester(getFocusRequester("boundaryWallThickness")),
                    singleLine = true,
                    isError = thicknessError != null,
                    supportingText = if (thicknessError != null) { { Text(thicknessError!!) } } else null
                )
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = gWidth,
                        onValueChange = { viewModel.gateWidth.value = it },
                        label = { Text("Gate Width ($unitStr)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f).focusRequester(getFocusRequester("gateWidth")),
                        singleLine = true,
                        isError = gateWidthError != null,
                        supportingText = if (gateWidthError != null) { { Text(gateWidthError!!) } } else null
                    )
                    OutlinedTextField(
                        value = gHeight,
                        onValueChange = { viewModel.gateHeight.value = it },
                        label = { Text("Gate Height ($unitStr)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f).focusRequester(getFocusRequester("gateHeight")),
                        singleLine = true,
                        isError = gateHeightError != null,
                        supportingText = if (gateHeightError != null) { { Text(gateHeightError!!) } } else null
                    )
                }
            }

            item {
                Text("Boundary Wall Material Type", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = matType == "Brick", onClick = { viewModel.boundaryMaterialType.value = "Brick" })
                        Text("Brick", modifier = Modifier.clickable { viewModel.boundaryMaterialType.value = "Brick" })
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = matType == "Block", onClick = { viewModel.boundaryMaterialType.value = "Block" })
                        Text("Concrete Block", modifier = Modifier.clickable { viewModel.boundaryMaterialType.value = "Block" })
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
            Text(text = "Step 5: Material Pricing & Dimensions", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
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
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Bricks Configuration", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = prBrick,
                            onValueChange = { viewModel.brickTripPrice.value = it },
                            label = { Text("Price per Brick ($)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f).focusRequester(getFocusRequester("brickTripPrice")),
                            singleLine = true,
                            isError = brickTripPriceError != null,
                            supportingText = if (brickTripPriceError != null) { { Text(brickTripPriceError!!) } } else null
                        )
                        OutlinedTextField(
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
                        OutlinedTextField(
                            value = brL,
                            onValueChange = { viewModel.brickLength.value = it },
                            label = { Text("L ($thicknessUnitStr)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = brW,
                            onValueChange = { viewModel.brickWidth.value = it },
                            label = { Text("W ($thicknessUnitStr)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
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
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Concrete Blocks Configuration", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = prBlock,
                            onValueChange = { viewModel.blockTripPrice.value = it },
                            label = { Text("Price per Block ($)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f).focusRequester(getFocusRequester("blockTripPrice")),
                            singleLine = true,
                            isError = blockTripPriceError != null,
                            supportingText = if (blockTripPriceError != null) { { Text(blockTripPriceError!!) } } else null
                        )
                        OutlinedTextField(
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
                        OutlinedTextField(
                            value = blL,
                            onValueChange = { viewModel.blockLength.value = it },
                            label = { Text("L ($thicknessUnitStr)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = blW,
                            onValueChange = { viewModel.blockWidth.value = it },
                            label = { Text("W ($thicknessUnitStr)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
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
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Tiling & plumbing Unit Pricing", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    OutlinedTextField(
                        value = prTile,
                        onValueChange = { viewModel.tilePricePerSqFt.value = it },
                        label = { Text("Tile Price per ${if (ut == UnitType.METRIC) "m²" else "sq ft"} ($)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth().focusRequester(getFocusRequester("tilePricePerSqFt")),
                        singleLine = true,
                        isError = tilePricePerSqFtError != null,
                        supportingText = if (tilePricePerSqFtError != null) { { Text(tilePricePerSqFtError!!) } } else null
                    )
                    OutlinedTextField(
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
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = price,
                    onValueChange = onPriceChange,
                    label = { Text(priceLabel) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f).let { if (priceFocusRequester != null) it.focusRequester(priceFocusRequester) else it },
                    singleLine = true,
                    isError = priceError != null,
                    supportingText = if (priceError != null) { { Text(priceError) } } else null
                )
                OutlinedTextField(
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
            Text(text = "Final Estimate Summary ($projectType)", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = MaterialTheme.colorScheme.primary)
        }

        item {
            Card(
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
            Card(
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
                        Text("- Washroom Cement/Sand: ${String.format(Locale.US, "%.1f Bags Cement", rm.washroomCementBags)} / ${String.format(Locale.US, "%.1f %s Sand", rm.washroomSandVol, volUnitStr)}", fontSize = 11.sp)
                    }
                }
            }
        }

        item {
            Text("Material Purchases (Trip/Load Breakdown)", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.primary)
        }

        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
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
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
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
                    modifier = Modifier.size(50.dp).background(MaterialTheme.colorScheme.surfaceVariant, shape = MaterialTheme.shapes.medium)
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
