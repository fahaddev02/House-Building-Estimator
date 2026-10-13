package com.example.paintcalculatorestimator.utils

import android.content.Context

class PrefUtil(private val context: Context) {

    companion object {
        val PREFS_NAME = "my_prefs"
        const val PREMIUM_KEY = "IS_PREMIUM_USER"
    }

    fun setPremium(value: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, 0)
        val editor = prefs.edit()
        editor.putBoolean(PREMIUM_KEY, value)
        editor.apply()
    }

    fun getPremium(): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, 0)
        return prefs.getBoolean(PREMIUM_KEY, false)
    }

}