package com.example.paintcalculatorestimator.presentation.premium

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.constrainHeight
import androidx.compose.ui.unit.constrainWidth
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.paintcalculatorestimator.R
import com.example.paintcalculatorestimator.ui.theme.PaintCalculatorEstimatorTheme
import java.util.Locale

enum class PremiumPlan { MONTHLY, YEARLY }

/**
 * Premium presentation only. Pass localized prices from Google Play and enable
 * purchasing once product details are ready. The caller owns billing and link actions.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumScreen(
    onClose: () -> Unit,
    onContinue: (PremiumPlan) -> Unit,
    onCancelSubscription: () -> Unit,
    onPrivacyClick: () -> Unit,
    modifier: Modifier = Modifier,
    monthlyPrice: String? = null,
    yearlyPrice: String? = null,
    yearlyPerMonthPrice: String? = null,
    purchaseEnabled: Boolean = false,
    isProcessing: Boolean = false,
    errorMessage: String? = null,
    onRetry: () -> Unit = {}
) {
    var selectedPlan by rememberSaveable { mutableStateOf(PremiumPlan.YEARLY) }
    val selectedPrice = if (selectedPlan == PremiumPlan.MONTHLY) monthlyPrice else yearlyPrice
    val planName = stringResource(
        if (selectedPlan == PremiumPlan.MONTHLY) R.string.premium_monthly else R.string.premium_yearly
    )
    val colors = MaterialTheme.colorScheme

    Scaffold(
        modifier = modifier,
        containerColor = colors.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(stringResource(R.string.premium_title), color = colors.primary,
                        style = MaterialTheme.typography.titleLarge)
                },
                navigationIcon = {
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.padding(start = 12.dp).background(colors.surfaceVariant, CircleShape)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = stringResource(R.string.premium_close),
                            tint = colors.onSurfaceVariant)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = colors.background)
            )
        }
    ) { insets ->
        PremiumResponsiveLayout(
            modifier = Modifier.fillMaxSize().padding(insets)
        ) { availableWidth, availableHeight ->
            val compactHeight = availableHeight < 700.dp
            Column(
                modifier = Modifier.widthIn(max = 560.dp).fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = if (availableWidth < 360.dp) 16.dp else 20.dp)
                    .padding(top = 12.dp, bottom = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_premium_hero),
                    contentDescription = null,
                    modifier = Modifier.size(if (compactHeight) 88.dp else 112.dp)
                )
                Spacer(Modifier.height(12.dp))
                Surface(shape = RoundedCornerShape(50), color = colors.primaryContainer) {
                    Text(
                        stringResource(R.string.app_name).uppercase(Locale.getDefault()),
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                        color = colors.primary, textAlign = TextAlign.Center
                    )
                }
                Spacer(Modifier.height(16.dp))
                Text(
                    stringResource(R.string.premium_headline),
                    modifier = Modifier.semantics { heading() },
                    style = MaterialTheme.typography.headlineLarge,
                    color = colors.onPrimaryContainer, textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(8.dp))
                Text(stringResource(R.string.premium_subtitle), style = MaterialTheme.typography.bodyLarge,
                    color = colors.onSurfaceVariant, textAlign = TextAlign.Center)
                Spacer(Modifier.height(20.dp))
                PremiumBenefits()
                Spacer(Modifier.height(20.dp))
                Text(
                    stringResource(R.string.premium_choose_plan),
                    modifier = Modifier.fillMaxWidth().semantics { heading() },
                    style = MaterialTheme.typography.titleMedium, color = colors.onPrimaryContainer
                )
                Spacer(Modifier.height(10.dp))
                PremiumPlans(
                    selectedPlan = selectedPlan,
                    monthlyPrice = monthlyPrice,
                    yearlyPrice = yearlyPrice,
                    yearlyPerMonthPrice = yearlyPerMonthPrice,
                    enabled = !isProcessing,
                    onSelect = { selectedPlan = it }
                )
                Spacer(Modifier.height(16.dp))
                if (!errorMessage.isNullOrBlank()) {
                    Surface(color = colors.errorContainer, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(14.dp)) {
                            Text(errorMessage, color = colors.onErrorContainer, style = MaterialTheme.typography.bodyMedium)
                            TextButton(onClick = onRetry, enabled = !isProcessing) { Text(stringResource(R.string.premium_retry)) }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                }
                Button(
                    onClick = { onContinue(selectedPlan) },
                    enabled = purchaseEnabled && !selectedPrice.isNullOrBlank() && !isProcessing,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp)
                ) {
                    Text(
                        stringResource(if (isProcessing) R.string.premium_processing else R.string.premium_continue, planName),
                        modifier = Modifier.weight(1f), textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(Modifier.width(10.dp))
                    if (isProcessing) {
                        CircularProgressIndicator(Modifier.size(20.dp), color = colors.primary, strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(20.dp))
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(stringResource(R.string.premium_auto_renew), textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                Spacer(Modifier.height(if (compactHeight) 20.dp else 32.dp))
                TextButton(onClick = onCancelSubscription) {
                    Text(stringResource(R.string.premium_cancel_subscription), textAlign = TextAlign.Center,
                        textDecoration = TextDecoration.Underline)
                }
                TextButton(onClick = onPrivacyClick) {
                    Text(stringResource(R.string.settings_privacy), textAlign = TextAlign.Center,
                        color = colors.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

/** Composes against the measured space without relying on a constraint receiver. */
@Composable
private fun PremiumResponsiveLayout(
    modifier: Modifier = Modifier,
    content: @Composable (availableWidth: Dp, availableHeight: Dp) -> Unit
) {
    SubcomposeLayout(modifier) { constraints ->
        val availableWidth = if (constraints.hasBoundedWidth) constraints.maxWidth.toDp() else Dp.Infinity
        val availableHeight = if (constraints.hasBoundedHeight) constraints.maxHeight.toDp() else Dp.Infinity
        val placeables = subcompose("content") {
            content(availableWidth, availableHeight)
        }.map { it.measure(constraints.copy(minWidth = 0, minHeight = 0)) }
        val width = constraints.constrainWidth(placeables.maxOfOrNull { it.width } ?: 0)
        val height = constraints.constrainHeight(placeables.maxOfOrNull { it.height } ?: 0)
        layout(width, height) {
            placeables.forEach { it.placeRelative((width - it.width) / 2, 0) }
        }
    }
}

@Composable
private fun PremiumBenefits() {
    Card(
        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            PremiumBenefit(R.drawable.ic_premium_ad_free, R.string.premium_remove_ads, R.string.premium_remove_ads_description)
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            PremiumBenefit(R.drawable.ic_premium_unlock, R.string.premium_unlock_features, R.string.premium_unlock_features_description)
        }
    }
}

@Composable
private fun PremiumBenefit(icon: Int, title: Int, description: Int) {
    val colors = MaterialTheme.colorScheme
    val largeText = LocalDensity.current.fontScale >= 1.5f
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(
            Modifier.size(if (largeText) 36.dp else 48.dp)
                .background(colors.primaryContainer, RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(painterResource(icon), contentDescription = null, tint = colors.primary,
                modifier = Modifier.size(if (largeText) 24.dp else 30.dp))
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(stringResource(title), style = MaterialTheme.typography.titleMedium, color = colors.onPrimaryContainer)
            Text(stringResource(description), style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
        }
        if (!largeText) {
            Box(Modifier.size(24.dp).background(colors.primary, CircleShape), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Check, contentDescription = null, tint = colors.onPrimary, modifier = Modifier.size(17.dp))
            }
        }
    }
}

@Composable
private fun PremiumPlans(
    selectedPlan: PremiumPlan,
    monthlyPrice: String?,
    yearlyPrice: String?,
    yearlyPerMonthPrice: String?,
    enabled: Boolean,
    onSelect: (PremiumPlan) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth().selectableGroup(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        PremiumPlanCard(
            plan = PremiumPlan.YEARLY,
            price = yearlyPrice,
            perMonthPrice = yearlyPerMonthPrice ?: deriveMonthlyPriceFromYearly(yearlyPrice),
            selectedPlan = selectedPlan,
            enabled = enabled,
            onSelect = onSelect,
            modifier = Modifier.fillMaxWidth()
        )
        PremiumPlanCard(
            plan = PremiumPlan.MONTHLY,
            price = monthlyPrice,
            perMonthPrice = monthlyPrice,
            selectedPlan = selectedPlan,
            enabled = enabled,
            onSelect = onSelect,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun PremiumPlanCard(
    plan: PremiumPlan,
    price: String?,
    perMonthPrice: String?,
    selectedPlan: PremiumPlan,
    enabled: Boolean,
    onSelect: (PremiumPlan) -> Unit,
    modifier: Modifier = Modifier
) {
    val selected = selectedPlan == plan
    val colors = MaterialTheme.colorScheme
    val monthly = plan == PremiumPlan.MONTHLY
    val tbdText = stringResource(R.string.premium_price_tbd)
    val hasValidYearlyPrice = !price.isNullOrBlank() && price != tbdText

    val subtitleText = if (monthly) {
        stringResource(R.string.premium_billed_monthly)
    } else if (hasValidYearlyPrice) {
        stringResource(R.string.premium_just_per_year, price!!)
    } else {
        stringResource(R.string.premium_billed_yearly)
    }

    val rightPriceText = perMonthPrice?.takeIf { it.isNotBlank() }
        ?: price?.takeIf { it.isNotBlank() }
        ?: tbdText

    Surface(
        modifier = modifier.semantics(mergeDescendants = true) {}
            .selectable(selected = selected, enabled = enabled, role = Role.RadioButton, onClick = { onSelect(plan) }),
        shape = RoundedCornerShape(20.dp),
        color = if (selected) colors.primaryContainer else colors.surface,
        border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) colors.primary else colors.outlineVariant)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier.size(24.dp).clearAndSetSemantics {}
                    .then(if (selected) Modifier.background(colors.primary, CircleShape)
                        else Modifier.border(1.5.dp, colors.outline, CircleShape)),
                contentAlignment = Alignment.Center
            ) {
                if (selected) Icon(Icons.Default.Check, contentDescription = null,
                    tint = colors.onPrimary, modifier = Modifier.size(17.dp))
            }
            Spacer(Modifier.width(14.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = stringResource(if (monthly) R.string.premium_monthly else R.string.premium_yearly),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = colors.onPrimaryContainer
                )
                Text(
                    text = subtitleText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = rightPriceText,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = colors.onPrimaryContainer,
                    textAlign = TextAlign.End
                )
                Text(
                    text = stringResource(R.string.premium_per_month),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant,
                    textAlign = TextAlign.End
                )
            }
        }
    }
}

private fun deriveMonthlyPriceFromYearly(yearlyPrice: String?): String? {
    if (yearlyPrice.isNullOrBlank()) return null
    val match = Regex("""([0-9]+(?:,[0-9]{3})*(?:\.[0-9]+)?)""").find(yearlyPrice) ?: return null
    val rawNumber = match.value.replace(",", "").toDoubleOrNull() ?: return null
    val monthly = rawNumber / 12.0
    val formattedMonthly = if (monthly % 1.0 == 0.0) {
        String.format(Locale.US, "%.0f", monthly)
    } else {
        String.format(Locale.US, "%.1f", monthly)
    }
    return yearlyPrice.replaceRange(match.range, formattedMonthly)
}

@Preview(name = "Phone", widthDp = 390, heightDp = 844, showBackground = true, showSystemUi = true)
@Preview(name = "Small phone", widthDp = 320, heightDp = 568, showBackground = true, showSystemUi = true)
@Preview(name = "Large text", widthDp = 360, heightDp = 800, fontScale = 1.8f, showBackground = true)
@Preview(name = "Tablet", widthDp = 800, heightDp = 1280, showBackground = true)
@Preview(name = "Landscape", widthDp = 800, heightDp = 360, showBackground = true)
@Composable
fun PremiumScreenPreview() {
    PaintCalculatorEstimatorTheme(darkTheme = false) { PreviewPremiumContent() }
}

@Preview(name = "Dark", widthDp = 390, heightDp = 844, showBackground = true, showSystemUi = true)
@Composable
fun PremiumScreenPreviewDark() {
    PaintCalculatorEstimatorTheme(darkTheme = true) { PreviewPremiumContent() }
}

@Composable
private fun PreviewPremiumContent() {
    PremiumScreen(
        onClose = {}, onContinue = {}, onCancelSubscription = {}, onPrivacyClick = {},
        monthlyPrice = "$6.99",
        yearlyPrice = "$50",
        yearlyPerMonthPrice = "$4.1",
        purchaseEnabled = true
    )
}
