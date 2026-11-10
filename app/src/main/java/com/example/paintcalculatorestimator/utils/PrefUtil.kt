package com.example.paintcalculatorestimator.utils

import android.content.Context
import com.example.paintcalculatorestimator.billing.EntitlementStore

class PrefUtil(private val context: Context) {

    companion object {
        val PREFS_NAME = "my_prefs"
        const val PREMIUM_KEY = "IS_PREMIUM_USER"
    }

    fun setPremium(value: Boolean) {
        EntitlementStore.from(context).confirm(value)
    }

    fun getPremium(): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, 0)
        return prefs.getBoolean(PREMIUM_KEY, false)
    }

}
