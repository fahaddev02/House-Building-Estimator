package com.example.paintcalculatorestimator.presentation.projects

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.example.paintcalculatorestimator.pdf.PdfGenerator
import com.example.paintcalculatorestimator.presentation.navigation.Screen
import com.example.paintcalculatorestimator.presentation.project.RowSummary
import com.example.paintcalculatorestimator.adsManager.NativeAdView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.material3.ExperimentalMaterial3Api

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectDetailsScreen(
    navController: NavController,
    projectId: Long,
    appPreferences: AppPreferences,
    viewModel: ProjectDetailsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val projectDetails by viewModel.projectDetails.collectAsState()
    val currency by appPreferences.currencyFlow.collectAsState(initial = "PKR")

    LaunchedEffect(key1 = projectId) {
        viewModel.loadProject(projectId)
    }

    ProjectDetailsScreenContent(
        projectDetails = projectDetails,
        currency = currency,
        onBackClick = { navController.popBackStack() },
        onEditClick = { projectDetails?.let { navController.navigate(Screen.Wizard.createRoute(it.project.id)) } },
        onDeleteClick = {
            viewModel.deleteProject {
                Toast.makeText(context, "Project deleted!", Toast.LENGTH_SHORT).show()
                navController.popBackStack()
            }
        },
        onDuplicateClick = {
            viewModel.duplicateProject {
                Toast.makeText(context, "Duplicated project successfully!", Toast.LENGTH_SHORT).show()
                navController.popBackStack()
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectDetailsScreenContent(
    projectDetails: ProjectDetails?,
    currency: String,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onDuplicateClick: () -> Unit
) {
    val context = LocalContext.current
    val details = projectDetails
    if (details == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        val roomDetails = details.rooms.firstOrNull()
        val room = roomDetails?.room
        val areaUnitStr = if (room?.unitType == UnitType.METRIC) "m²" else "ft²"
        val volUnitStr = if (room?.unitType == UnitType.METRIC) "L" else "gal"
        val unitStr = if (room?.unitType == UnitType.METRIC) "m" else "ft"

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(text = details.project.name, fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = onEditClick) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit")
                        }
                        IconButton(onClick = onDeleteClick) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
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
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Main Total Card
                item {
                    OutlinedCard(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
                        shape = MaterialTheme.shapes.large,
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = stringResource(id = R.string.est_cost_total).uppercase(),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "$currency ${String.format(Locale.US, "%,.2f", details.estimate.totalCost)}",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                // Project Details Meta Card
                item {
                    OutlinedCard(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(text = "Project Meta Info", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                            
                            val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
                            RowSummary(label = "Created Date", value = sdf.format(Date(details.project.createdAt)))
                            if (details.project.location.isNotEmpty()) {
                                RowSummary(label = "Location", value = details.project.location)
                            }
                            if (details.project.notes.isNotEmpty()) {
                                Text(text = "Notes:", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(text = details.project.notes, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                }

                // Measurements Card
                if (room != null) {
                    item {
                        OutlinedCard(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(text = "Measurements", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                                RowSummary(label = "Room Name", value = room.name)
                                RowSummary(label = "Dimensions", value = "${room.width} x ${room.length} x ${room.height} $unitStr")
                                RowSummary(label = "Walls Count", value = "${room.wallCount}")
                                RowSummary(label = "Ceiling Included", value = if (room.includeCeiling) "Yes" else "No")
                            }
                        }
                    }
                }

                // Deductions Card
                if (roomDetails != null && roomDetails.openings.isNotEmpty()) {
                    item {
                        OutlinedCard(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(text = "Deductions", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                                for (opening in roomDetails.openings) {
                                    val area = opening.width * opening.height * opening.quantity
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "${opening.type.name.lowercase().replaceFirstChar { it.uppercase() }} (x${opening.quantity})",
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "-${String.format(Locale.US, "%.2f", area)} $areaUnitStr",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Paint specifications Card
                item {
                    OutlinedCard(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(text = "Specifications", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                            RowSummary(label = "Coverage Value", value = "${details.paintSettings.coverage} $areaUnitStr/$volUnitStr")
                            RowSummary(label = "Coats Count", value = "${details.paintSettings.coats}")
                            RowSummary(label = "Wastage Allowance", value = "${(details.paintSettings.wastage * 100).toInt()}%")
                            RowSummary(label = "Paint Required", value = "${details.estimate.paintRequired} $volUnitStr")
                            if (details.paintSettings.includePrimer) {
                                RowSummary(label = "Primer Required", value = "${details.estimate.primerRequired} $volUnitStr")
                            }
                        }
                    }
                }

                // Cost Breakdown Card
                item {
                    OutlinedCard(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(text = "Expenses Breakdown", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                            RowSummary(label = "Paint Cost", value = "$currency ${String.format(Locale.US, "%,.2f", details.estimate.paintCost)}")
                            if (details.paintSettings.includePrimer) {
                                RowSummary(label = "Primer Cost", value = "$currency ${String.format(Locale.US, "%,.2f", details.estimate.primerCost)}")
                            }
                            RowSummary(label = "Labor Cost", value = "$currency ${String.format(Locale.US, "%,.2f", details.estimate.laborCost)}")
                            RowSummary(label = "Additional Materials", value = "$currency ${String.format(Locale.US, "%,.2f", details.estimate.materialsCost)}")
                        }
                    }
                }

                // Share Buttons
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                val file = PdfGenerator.generatePdf(context, details, currency)
                                sharePdfFile(context, file)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Icon(imageVector = Icons.Default.Share, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Share PDF")
                        }

                        OutlinedButton(
                            onClick = {
                                val textSummary = buildTextSummary(
                                    projectName = details.project.name,
                                    paintableArea = details.estimate.paintableArea,
                                    areaUnit = areaUnitStr,
                                    paint = details.estimate.paintRequired,
                                    volUnit = volUnitStr,
                                    totalCost = details.estimate.totalCost,
                                    currency = currency
                                )
                                shareTextSummary(context, textSummary)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Icon(imageVector = Icons.Default.Share, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Share Text")
                        }
                    }
                }

                // Duplication Button
                item {
                    OutlinedButton(
                        onClick = onDuplicateClick,
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Duplicate Project")
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun ProjectDetailsScreenPreview() {
    MaterialTheme {
        ProjectDetailsScreenContent(
            projectDetails = null,
            currency = "PKR",
            onBackClick = {},
            onEditClick = {},
            onDeleteClick = {},
            onDuplicateClick = {}
        )
    }
}

private fun sharePdfFile(context: android.content.Context, file: java.io.File) {
    try {
        val uri = androidx.core.content.FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(android.content.Intent.EXTRA_STREAM, uri)
            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(android.content.Intent.createChooser(intent, "Share Estimate PDF"))
    } catch (e: java.lang.Exception) {
        Toast.makeText(context, "Error sharing PDF: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

private fun shareTextSummary(context: android.content.Context, summary: String) {
    val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(android.content.Intent.EXTRA_TEXT, summary)
    }
    context.startActivity(android.content.Intent.createChooser(intent, "Share Estimate Summary"))
}

private fun buildTextSummary(
    projectName: String,
    paintableArea: Double,
    areaUnit: String,
    paint: Double,
    volUnit: String,
    totalCost: Double,
    currency: String
): String {
    return """
        Painting Project Details: $projectName
        Paintable Wall Area: $paintableArea $areaUnit
        Recommended Paint: $paint $volUnit
        Estimated Total Cost: $currency ${String.format(Locale.US, "%,.2f", totalCost)}
        
        Calculated using Paint Calculator app.
    """.trimIndent()
}
