package com.example.paintcalculatorestimator.presentation.projects

import com.example.paintcalculatorestimator.ui.theme.EstimateTextField



import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.paintcalculatorestimator.R
import com.example.paintcalculatorestimator.data.preferences.AppPreferences
import com.example.paintcalculatorestimator.domain.model.*
import com.example.paintcalculatorestimator.adsManager.NativeAdView
import com.example.paintcalculatorestimator.presentation.home.RecentProjectCard
import com.example.paintcalculatorestimator.presentation.navigation.Screen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.material3.ExperimentalMaterial3Api

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedProjectsScreen(
    navController: NavController,
    appPreferences: AppPreferences,
    viewModel: SavedProjectsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val searchQuery by viewModel.searchQuery.collectAsState()
    val projectsList by viewModel.projectsState.collectAsState()
    val currency by appPreferences.currencyFlow.collectAsState(initial = "PKR")

    SavedProjectsScreenContent(
        searchQuery = searchQuery,
        projectsList = projectsList,
        currency = currency,
        onSearchQueryChange = { viewModel.setSearchQuery(it) },
        onBackClick = { navController.popBackStack() },
        onProjectClick = { projectId -> navController.navigate(Screen.ProjectDetails.createRoute(projectId)) },
        onEditClick = { projectId -> navController.navigate(Screen.Wizard.createRoute(projectId)) },
        onDuplicateClick = { projectId ->
            viewModel.duplicateProject(projectId) {
                Toast.makeText(context, "Project duplicated!", Toast.LENGTH_SHORT).show()
            }
        },
        onDeleteClick = { projectId ->
            viewModel.deleteProject(projectId)
            Toast.makeText(context, "Project deleted!", Toast.LENGTH_SHORT).show()
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedProjectsScreenContent(
    searchQuery: String,
    projectsList: List<ProjectDetails>,
    currency: String,
    onSearchQueryChange: (String) -> Unit,
    onBackClick: () -> Unit,
    onProjectClick: (Long) -> Unit,
    onEditClick: (Long) -> Unit,
    onDuplicateClick: (Long) -> Unit,
    onDeleteClick: (Long) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.saved_projects_title), fontWeight = FontWeight.Bold) },
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
        },
        bottomBar = { NativeAdView() }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            // Search field
            EstimateTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = { Text(text = stringResource(id = R.string.search_projects)) },
                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = MaterialTheme.shapes.medium,
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (projectsList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(id = R.string.no_saved_projects),
                        color = MaterialTheme.colorScheme.secondary,
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(projectsList) { details ->
                        var showMenu by remember { mutableStateOf(false) }
                        
                        OutlinedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onProjectClick(details.project.id) },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
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
                                    Column {
                                        Text(
                                            text = details.project.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp
                                        )
                                        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                                        Text(
                                            text = sdf.format(Date(details.project.createdAt)),
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    
                                    Box {
                                        IconButton(onClick = { showMenu = true }) {
                                            Icon(imageVector = Icons.Default.MoreVert, contentDescription = null)
                                        }
                                        DropdownMenu(
                                            expanded = showMenu,
                                            onDismissRequest = { showMenu = false }
                                        ) {
                                            DropdownMenuItem(
                                                text = { Text(stringResource(id = R.string.action_open)) },
                                                onClick = {
                                                    showMenu = false
                                                    onProjectClick(details.project.id)
                                                }
                                            )
                                            DropdownMenuItem(
                                                text = { Text("Edit") },
                                                onClick = {
                                                    showMenu = false
                                                    onEditClick(details.project.id)
                                                }
                                            )
                                            DropdownMenuItem(
                                                text = { Text(stringResource(id = R.string.action_duplicate)) },
                                                onClick = {
                                                    showMenu = false
                                                    onDuplicateClick(details.project.id)
                                                }
                                            )
                                            DropdownMenuItem(
                                                text = { Text(stringResource(id = R.string.action_delete)) },
                                                onClick = {
                                                    showMenu = false
                                                    onDeleteClick(details.project.id)
                                                }
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                                Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                                Spacer(modifier = Modifier.height(12.dp))

                                val firstRoom = details.rooms.firstOrNull()?.room
                                val areaUnit = if (firstRoom?.unitType == UnitType.METRIC) "m²" else "ft²"
                                val volUnit = if (firstRoom?.unitType == UnitType.METRIC) "L" else "gal"

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(text = "Area", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(
                                            text = "${details.estimate.paintableArea} $areaUnit",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Column {
                                        Text(text = "Paint Quantity", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(
                                            text = "${details.estimate.paintRequired} $volUnit",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(text = "Estimated Cost", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(
                                            text = "$currency ${String.format(Locale.US, "%,.0f", details.estimate.totalCost)}",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun SavedProjectsScreenPreview() {
    MaterialTheme {
        SavedProjectsScreenContent(
            searchQuery = "",
            projectsList = emptyList(),
            currency = "PKR",
            onSearchQueryChange = {},
            onBackClick = {},
            onProjectClick = {},
            onEditClick = {},
            onDuplicateClick = {},
            onDeleteClick = {}
        )
    }
}
