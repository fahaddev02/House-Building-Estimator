package com.example.paintcalculatorestimator.presentation.home

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.paintcalculatorestimator.R
import com.example.paintcalculatorestimator.adsManager.InterstitialHelper
import com.example.paintcalculatorestimator.adsManager.NativeAdView
import com.example.paintcalculatorestimator.adsManager.RemoteConfigManager
import com.example.paintcalculatorestimator.data.preferences.AppPreferences
import com.example.paintcalculatorestimator.domain.model.ProjectDetails
import com.example.paintcalculatorestimator.domain.model.UnitType
import com.example.paintcalculatorestimator.presentation.navigation.Screen
import com.example.paintcalculatorestimator.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(navController: NavController, appPreferences: AppPreferences, viewModel: HomeViewModel = hiltViewModel()) {
    val recentProjects by viewModel.recentProjectsState.collectAsState()
    val currency by appPreferences.currencyFlow.collectAsState(initial = "PKR")
    val context = LocalContext.current as? Activity

    fun showHomeAdAndNavigate(action: () -> Unit) {
        val adConfig = RemoteConfigManager.getAdConfig(
            RemoteConfigManager.INTERSTITIAL_HOME,
            RemoteConfigManager.TEST_INTERSTITIAL_ID
        )
        if (context != null && adConfig.enabled) {
            InterstitialHelper.loadAndShowInterstitialAd(
                activity = context,
                adId = adConfig.adId,
                event = "home_click",
                dismiss = action,
                failedMoveNext = action
            )
        } else {
            action()
        }
    }

    HomeScreenContent(
        recentProjects = recentProjects,
        currency = currency,
        // Settings click is EXEMPT from Interstitial Ads as requested
        onSettingsClick = { navController.navigate(Screen.Settings.route) },
        onNewCalculationClick = { showHomeAdAndNavigate { navController.navigate(Screen.Wizard.createRoute(0)) } },
        onViewAllClick = { showHomeAdAndNavigate { navController.navigate(Screen.Projects.route) } },
        onProjectClick = { projectId -> showHomeAdAndNavigate { navController.navigate(Screen.ProjectDetails.createRoute(projectId)) } },
        onWallCalcClick = { showHomeAdAndNavigate { navController.navigate(Screen.WallCalculator.route) } },
        onCeilingCalcClick = { showHomeAdAndNavigate { navController.navigate(Screen.CeilingCalculator.route) } },
        onPaintCalcClick = { showHomeAdAndNavigate { navController.navigate(Screen.PaintCalculatorScreen.createRoute(0.0)) } },
        onCostCalcClick = { showHomeAdAndNavigate { navController.navigate(Screen.CostCalculatorScreen.createRoute(0.0)) } }
    )
}
@Composable
fun HomeScreenContent(
    recentProjects: List<ProjectDetails>, currency: String,
    onSettingsClick: () -> Unit, onNewCalculationClick: () -> Unit,
    onViewAllClick: () -> Unit, onProjectClick: (Long) -> Unit,
    onWallCalcClick: () -> Unit, onCeilingCalcClick: () -> Unit,
    onPaintCalcClick: () -> Unit, onCostCalcClick: () -> Unit
) {
    var showExitDialog by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current
    BackHandler { showExitDialog = true }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = { NativeAdView() }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    BrandMark()
                    Column(Modifier.weight(1f)) {
                        Text(stringResource(R.string.app_name), style = MaterialTheme.typography.titleMedium)
                        Text("Plan your next building project", style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    OutlinedIconButton(onClick = onSettingsClick, shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", modifier = Modifier.size(22.dp))
                    }
                }
            }
            item {
                OutlinedCard(onClick = onNewCalculationClick, shape = RoundedCornerShape(24.dp)) {
                    Box(Modifier.fillMaxWidth().background(Brush.linearGradient(listOf(BrandPurpleDeep, BrandPurple)))) {
                        androidx.compose.foundation.Image(
                            painter = painterResource(R.drawable.ic_paint_new),
                            contentDescription = null,
                            modifier = Modifier.align(Alignment.TopEnd).padding(20.dp).size(52.dp)
                        )
                        Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("MULTI-STEP ESTIMATE", style = MaterialTheme.typography.labelSmall, color = Color(0xFFE4DCFF))
                            Text("Start a new project", style = MaterialTheme.typography.headlineSmall, color = Color.White)
                            Text("Walk through rooms, boundary walls and material pricing in one guided flow.",
                                style = MaterialTheme.typography.bodyMedium, color = Color(0xFFE4DCFF),
                                modifier = Modifier.fillMaxWidth(0.88f))
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                Surface(shape = RoundedCornerShape(10.dp), color = Color(0xFFF2EDFF)) {
                                    Text("Create  ›", Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
                                        style = MaterialTheme.typography.labelLarge, color = BrandPurpleDeep)
                                }
                            }
                        }
                    }
                }
            }
            item { Text("Quick tools", style = MaterialTheme.typography.titleSmall) }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    QuickToolCard(
                        "Wall Calculator",
                        "Rooms & walls",
                        EstimatorSymbol.WALL,
                        Color(0xFFC36651), Modifier.weight(1f),
                        onWallCalcClick)
                    QuickToolCard("Ceiling Calculator", "Ceiling area", EstimatorSymbol.ROOF, Color(0xFF3986A5), Modifier.weight(1f), onCeilingCalcClick)
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    QuickToolCard("Paint Calculator", "Coverage & coats", EstimatorSymbol.PAINT, Color(0xFFAB8127), Modifier.weight(1f), onPaintCalcClick)
                    QuickToolCard("Cost Calculator", "Total estimate", EstimatorSymbol.COST, Color(0xFF218871), Modifier.weight(1f), onCostCalcClick)
                }
            }
            item {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Recent projects", style = MaterialTheme.typography.titleSmall)
                    TextButton(onClick = onViewAllClick) { Text("View all") }
                }
            }
            if (recentProjects.isEmpty()) {
                item {
                    EstimatePanel {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            IconTile(EstimatorSymbol.PLAN)
                            Column(Modifier.weight(1f)) {
                                Text("Your next project starts here", style = MaterialTheme.typography.titleSmall)
                                Text("Saved estimates will appear here.", style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        OutlinedButton(onClick = onNewCalculationClick, modifier = Modifier.fillMaxWidth()) { Text("Create your first estimate") }
                    }
                }
            } else {
                items(recentProjects, key = { it.project.id }) { details ->
                    RecentProjectCard(details, currency) { onProjectClick(details.project.id) }
                }
            }
        }
    }

    if (showExitDialog) {
        ExitDialog(
            onDismiss = { showExitDialog = false },
            onExit = { (context as? Activity)?.finish() }
        )
    }
}

@Composable
private fun ExitDialog(onDismiss: () -> Unit, onExit: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(28.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        icon = {
            Box(
                modifier = Modifier.size(72.dp).clip(RoundedCornerShape(22.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.foundation.Image(
                    painter = painterResource(R.drawable.ic_exit_dialog),
                    contentDescription = null,
                    modifier = Modifier.size(48.dp)
                )
            }
        },
        title = { Text("Exit app?", style = MaterialTheme.typography.headlineSmall, textAlign = androidx.compose.ui.text.style.TextAlign.Center) },
        text = {
            Text(
                "Are you sure you want to leave Construction Cost Estimator?",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        },
        confirmButton = {
            Button(onClick = onExit, shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
                Text("Exit")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun QuickToolCard(title: String, desc: String, symbol: EstimatorSymbol, tint: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    OutlinedCard(onClick = onClick, modifier = modifier.heightIn(min = 140.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            IconTile(symbol, tint)
            Column {
                Text(title, style = MaterialTheme.typography.titleSmall)
                Text(desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun RecentProjectCard(projectDetails: ProjectDetails, currency: String, onOpen: () -> Unit) {
    val firstRoom = projectDetails.rooms.firstOrNull()?.room
    val metric = firstRoom?.unitType != UnitType.IMPERIAL
    val construction = projectDetails.project.notes.startsWith("[Type:")
    OutlinedCard(onClick = onOpen, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(projectDetails.project.name, modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleSmall,
                    maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text("$currency ${String.format(Locale.US, "%,.0f", projectDetails.estimate.totalCost)}",
                    style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.tertiary)
            }
            Text(SimpleDateFormat("dd MMM yyyy, h:mm a", Locale.getDefault()).format(Date(projectDetails.project.createdAt)),
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                Column(Modifier.weight(1f)) {
                    Text(if (construction) "Floor area" else "Area", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${String.format(Locale.US, "%.1f", projectDetails.estimate.paintableArea)} ${if (construction || !metric) "ft²" else "m²"}",
                        style = MaterialTheme.typography.titleSmall)
                }
                Column(Modifier.weight(1f)) {
                    Text(if (construction) "Materials" else "Paint required", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(if (construction) "$currency ${String.format(Locale.US, "%,.0f", projectDetails.estimate.materialsCost)}"
                        else "${String.format(Locale.US, "%.1f", projectDetails.estimate.paintRequired)} ${if (metric) "L" else "gal"}",
                        style = MaterialTheme.typography.titleSmall)
                }
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, name = "Home screen")
@Composable
fun HomeScreenPreview() {
    PaintCalculatorEstimatorTheme {
        HomeScreenContent(emptyList(), "PKR", {}, {}, {}, {}, {}, {}, {}, {})
    }
}
