package com.example.paintcalculatorestimator.billing

import android.content.Context
import android.content.SharedPreferences
import com.example.paintcalculatorestimator.utils.PrefUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withTimeoutOrNull

data class EntitlementState(val isPremium: Boolean, val isResolved: Boolean = false) {
    val canShowAds: Boolean get() = isResolved && !isPremium
}

/** Shared by billing, Compose placements and SDK callbacks, including during restoration. */
class EntitlementStore private constructor(private val preferences: SharedPreferences) {
    companion object {
        @Volatile private var instance: EntitlementStore? = null
        fun from(context: Context): EntitlementStore = instance ?: synchronized(this) {
            instance ?: EntitlementStore(context.applicationContext.getSharedPreferences(
                PrefUtil.PREFS_NAME, Context.MODE_PRIVATE
            )).also { instance = it }
        }
    }

    private val mutableState = MutableStateFlow(EntitlementState(preferences.getBoolean(PrefUtil.PREMIUM_KEY, false)))
    val state = mutableState.asStateFlow()
    val canShowAds: Boolean get() = state.value.canShowAds

    fun beginRefresh() { mutableState.update { it.copy(isResolved = false) } }

    fun confirm(isPremium: Boolean) {
        // Publish first so an arriving ad callback cannot use the old entitlement.
        mutableState.value = EntitlementState(isPremium, isResolved = true)
        preferences.edit().putBoolean(PrefUtil.PREMIUM_KEY, isPremium).apply()
    }

    suspend fun awaitResolution(timeoutMillis: Long = 5_000): EntitlementState? =
        withTimeoutOrNull(timeoutMillis) { state.first { it.isResolved } }
}
