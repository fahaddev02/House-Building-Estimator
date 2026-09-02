package com.example.paintcalculatorestimator.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.paintcalculatorestimator.R
import com.example.paintcalculatorestimator.data.preferences.AppPreferences
import com.example.paintcalculatorestimator.domain.model.ProjectDetails
import com.example.paintcalculatorestimator.domain.model.UnitType
import com.example.paintcalculatorestimator.presentation.navigation.Screen
import java.util.Locale
import androidx.compose.material3.ExperimentalMaterial3Api

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    appPreferences: AppPreferences,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val recentProjects by viewModel.recentProjectsState.collectAsState()
    val currency by appPreferences.currencyFlow.collectAsState(initial = "PKR")

    HomeScreenContent(
        recentProjects = recentProjects,
        currency = currency,
        onSettingsClick = { navController.navigate(Screen.Settings.route) },
        onNewCalculationClick = { navController.navigate(Screen.Wizard.createRoute(0)) },
        onViewAllClick = { navController.navigate(Screen.Projects.route) },
        onProjectClick = { projectId -> navController.navigate(Screen.ProjectDetails.createRoute(projectId)) },
        onWallCalcClick = { navController.navigate(Screen.WallCalculator.route) },
        onCeilingCalcClick = { navController.navigate(Screen.CeilingCalculator.route) },
        onPaintCalcClick = { navController.navigate(Screen.PaintCalculatorScreen.createRoute(0.0)) },
        onCostCalcClick = { navController.navigate(Screen.CostCalculatorScreen.createRoute(0.0)) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenContent(
    recentProjects: List<ProjectDetails>,
    currency: String,
    onSettingsClick: () -> Unit,
    onNewCalculationClick: () -> Unit,
    onViewAllClick: () -> Unit,
    onProjectClick: (Long) -> Unit,
    onWallCalcClick: () -> Unit,
    onCeilingCalcClick: () -> Unit,
    onPaintCalcClick: () -> Unit,
    onCostCalcClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = stringResource(id = R.string.home_title),
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                        Text(
                            text = stringResource(id = R.string.home_subtitle),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.primary
                        )
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            
            // 1. New Calculation Card
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNewCalculationClick() },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = MaterialTheme.shapes.large
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(id = R.string.home_btn_new_calc),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Start a multi-step project estimate",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }

            // 2. Quick Tools Grid Header
            item {
                Text(
                    text = stringResource(id = R.string.home_section_quick_tools),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            // Quick Tools Cards
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuickToolCard(
                        title = stringResource(id = R.string.quick_wall_calc),
                        desc = "Walls size",
                        icon = Icons.Default.Add,
                        modifier = Modifier.weight(1f),
                        onClick = onWallCalcClick
                    )
                    QuickToolCard(
                        title = stringResource(id = R.string.quick_ceiling_calc),
                        desc = "Ceiling size",
                        icon = Icons.Default.Add,
                        modifier = Modifier.weight(1f),
                        onClick = onCeilingCalcClick
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuickToolCard(
                        title = stringResource(id = R.string.quick_paint_calc),
                        desc = "Coverage/Coats",
                        icon = Icons.Default.Add,
                        modifier = Modifier.weight(1f),
                        onClick = onPaintCalcClick
                    )
                    QuickToolCard(
                        title = stringResource(id = R.string.quick_cost_calc),
                        desc = "Total estimate",
                        icon = Icons.Default.Add,
                        modifier = Modifier.weight(1f),
                        onClick = onCostCalcClick
                    )
                }
            }

            // 3. Saved Projects Header / View All link
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(id = R.string.home_section_recent_projects),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    TextButton(onClick = onViewAllClick) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.List,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "View All",
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // 4. Recent Projects List
            if (recentProjects.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(id = R.string.home_no_projects),
                            color = MaterialTheme.colorScheme.secondary,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = onNewCalculationClick,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text(text = stringResource(id = R.string.home_create_first_estimate))
                        }
                    }
                }
            } else {
                items(recentProjects) { projectDetails ->
                    RecentProjectCard(
                        projectDetails = projectDetails,
                        currency = currency,
                        onOpen = {
                            onProjectClick(projectDetails.project.id)
                        }
                    )
                }
            }
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    MaterialTheme {
        HomeScreenContent(
            recentProjects = emptyList(),
            currency = "PKR",
            onSettingsClick = {},
            onNewCalculationClick = {},
            onViewAllClick = {},
            onProjectClick = {},
            onWallCalcClick = {},
            onCeilingCalcClick = {},
            onPaintCalcClick = {},
            onCostCalcClick = {}
        )
    }
}

@Composable
fun QuickToolCard(
    title: String,
    desc: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(100.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Column {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = desc,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun RecentProjectCard(
    projectDetails: ProjectDetails,
    currency: String,
    onOpen: () -> Unit
) {
    val firstRoom = projectDetails.rooms.firstOrNull()?.room
    val unitStr = if (firstRoom?.unitType == UnitType.METRIC) "L" else "gal"
    val areaUnitStr = if (firstRoom?.unitType == UnitType.METRIC) "m²" else "ft²"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpen() }
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = projectDetails.project.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "$currency ${String.format(Locale.US, "%,.0f", projectDetails.estimate.totalCost)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            if (projectDetails.project.location.isNotEmpty()) {
                Text(
                    text = projectDetails.project.location,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Column {
                    Text(text = "Area", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = "${projectDetails.estimate.paintableArea} $areaUnitStr",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Column {
                    Text(text = "Paint Required", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = "${projectDetails.estimate.paintRequired} $unitStr",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
