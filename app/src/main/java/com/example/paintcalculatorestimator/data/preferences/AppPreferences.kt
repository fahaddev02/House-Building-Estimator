package com.example.paintcalculatorestimator.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "paint_calculator_prefs")

class AppPreferences(private val context: Context) {

    private val dataStore = context.dataStore

    companion object {
        val KEY_THEME = stringPreferencesKey("theme")
        val KEY_UNIT_TYPE = stringPreferencesKey("unit_type")
        val KEY_CURRENCY = stringPreferencesKey("currency")
        val KEY_DEFAULT_COVERAGE = doublePreferencesKey("default_coverage")
        val KEY_DEFAULT_COATS = intPreferencesKey("default_coats")
        val KEY_DEFAULT_WASTAGE = doublePreferencesKey("default_wastage")
        val KEY_ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
    }

    val themeFlow: Flow<String> = dataStore.data.map { preferences ->
        preferences[KEY_THEME] ?: "SYSTEM"
    }

    val unitTypeFlow: Flow<String> = dataStore.data.map { preferences ->
        preferences[KEY_UNIT_TYPE] ?: "METRIC"
    }

    val currencyFlow: Flow<String> = dataStore.data.map { preferences ->
        preferences[KEY_CURRENCY] ?: "PKR"
    }

    val defaultCoverageFlow: Flow<Double> = dataStore.data.map { preferences ->
        preferences[KEY_DEFAULT_COVERAGE] ?: 10.0
    }

    val defaultCoatsFlow: Flow<Int> = dataStore.data.map { preferences ->
        preferences[KEY_DEFAULT_COATS] ?: 2
    }

    val defaultWastageFlow: Flow<Double> = dataStore.data.map { preferences ->
        preferences[KEY_DEFAULT_WASTAGE] ?: 0.10
    }

    val isOnboardingCompletedFlow: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[KEY_ONBOARDING_COMPLETED] ?: false
    }

    suspend fun setTheme(theme: String) {
        dataStore.edit { preferences ->
            preferences[KEY_THEME] = theme
        }
    }

    suspend fun setUnitType(unitType: String) {
        dataStore.edit { preferences ->
            preferences[KEY_UNIT_TYPE] = unitType
        }
    }

    suspend fun setCurrency(currency: String) {
        dataStore.edit { preferences ->
            preferences[KEY_CURRENCY] = currency
        }
    }

    suspend fun setDefaultCoverage(coverage: Double) {
        dataStore.edit { preferences ->
            preferences[KEY_DEFAULT_COVERAGE] = coverage
        }
    }

    suspend fun setDefaultCoats(coats: Int) {
        dataStore.edit { preferences ->
            preferences[KEY_DEFAULT_COATS] = coats
        }
    }

    suspend fun setDefaultWastage(wastage: Double) {
        dataStore.edit { preferences ->
            preferences[KEY_DEFAULT_WASTAGE] = wastage
        }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_ONBOARDING_COMPLETED] = completed
        }
    }
}
