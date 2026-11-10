package com.example.paintcalculatorestimator.adsManager

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import com.example.paintcalculatorestimator.billing.EntitlementStore

/** The same observable visibility gate is used by banner and native placements. */
@Composable
internal fun rememberAdAccess(): Boolean {
    if (LocalInspectionMode.current) return false
    val context = LocalContext.current
    val store = remember(context) { EntitlementStore.from(context) }
    val entitlement by store.state.collectAsState()
    return entitlement.canShowAds
}
