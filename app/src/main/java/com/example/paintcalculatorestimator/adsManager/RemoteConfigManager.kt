package com.example.paintcalculatorestimator.adsManager

import android.util.Log
import com.example.paintcalculatorestimator.BuildConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull
import org.json.JSONObject

data class AdConfig(
    val enabled: Boolean = true,
    val adId: String = ""
)

object RemoteConfigManager {
    private const val TAG = "RemoteConfigManager"

    // Remote Config Parameter Keys
    const val REMOTE_CONFIG_TEMPLATE = "remote_config_template"

    const val IS_ALL_ADS_ENABLED = "isAllAdsEnabled"
    const val BANNER_SPLASH = "banner_splash"
    const val APP_OPEN_SPLASH = "app_open_splash"
    const val INTERSTITIAL_HOME = "interstitial_home"
    const val BANNER_HOME = "banner_home"
    const val INTERSTITIAL_GENERAL = "interstitial_general"
    const val NATIVE_GENERAL = "native_general"

    // Google Sample Test Ad Unit IDs (Only used when BuildConfig.DEBUG is true)
    private const val SAMPLE_TEST_BANNER_ID = "ca-app-pub-3940256099942544/6300978111"
    private const val SAMPLE_TEST_APP_OPEN_ID = "ca-app-pub-3940256099942544/9257395921"
    private const val SAMPLE_TEST_INTERSTITIAL_ID = "ca-app-pub-3940256099942544/1033173712"
    private const val SAMPLE_TEST_NATIVE_ID = "ca-app-pub-3940256099942544/2247696110"

    val TEST_BANNER_ID: String = if (BuildConfig.DEBUG) SAMPLE_TEST_BANNER_ID else ""
    val TEST_APP_OPEN_ID: String = if (BuildConfig.DEBUG) SAMPLE_TEST_APP_OPEN_ID else ""
    val TEST_INTERSTITIAL_ID: String = if (BuildConfig.DEBUG) SAMPLE_TEST_INTERSTITIAL_ID else ""
    val TEST_NATIVE_ID: String = if (BuildConfig.DEBUG) SAMPLE_TEST_NATIVE_ID else ""

    private val _isConfigLoaded = MutableStateFlow(false)
    val isConfigLoaded: StateFlow<Boolean> = _isConfigLoaded.asStateFlow()

    private val remoteConfig: FirebaseRemoteConfig by lazy {
        FirebaseRemoteConfig.getInstance()
    }

    fun init(onComplete: (() -> Unit)? = null) {
        try {
            val configSettings = FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(if (BuildConfig.DEBUG) 0 else 3600)
                .build()
            remoteConfig.setConfigSettingsAsync(configSettings)

            val defaultTemplateJson = """
                {
                  "isAllAdsEnabled": true,
                  "banner_splash": {"enabled": true, "ad_id": "$TEST_BANNER_ID"},
                  "app_open_splash": {"enabled": true, "ad_id": "$TEST_APP_OPEN_ID"},
                  "interstitial_home": {"enabled": true, "ad_id": "$TEST_INTERSTITIAL_ID"},
                  "banner_home": {"enabled": true, "ad_id": "$TEST_BANNER_ID"},
                  "interstitial_general": {"enabled": true, "ad_id": "$TEST_INTERSTITIAL_ID"},
                  "native_general": {"enabled": true, "ad_id": "$TEST_NATIVE_ID"}
                }
            """.trimIndent()

            val defaults = mapOf<String, Any>(
                REMOTE_CONFIG_TEMPLATE to defaultTemplateJson,
                IS_ALL_ADS_ENABLED to true,
                BANNER_SPLASH to """{"enabled": true, "ad_id": "$TEST_BANNER_ID"}""",
                APP_OPEN_SPLASH to """{"enabled": true, "ad_id": "$TEST_APP_OPEN_ID"}""",
                INTERSTITIAL_HOME to """{"enabled": true, "ad_id": "$TEST_INTERSTITIAL_ID"}""",
                BANNER_HOME to """{"enabled": true, "ad_id": "$TEST_BANNER_ID"}""",
                INTERSTITIAL_GENERAL to """{"enabled": true, "ad_id": "$TEST_INTERSTITIAL_ID"}""",
                NATIVE_GENERAL to """{"enabled": true, "ad_id": "$TEST_NATIVE_ID"}"""
            )
            remoteConfig.setDefaultsAsync(defaults)

            remoteConfig.fetchAndActivate()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.e(TAG, "RemoteConfig fetch and activate succeeded")
                    } else {
                        Log.e(TAG, "RemoteConfig fetch failed: ${task.exception}")
                    }
                    _isConfigLoaded.value = true
                    onComplete?.invoke()
                }
        } catch (e: Exception) {
            Log.e(TAG, "RemoteConfig init exception: $e")
            _isConfigLoaded.value = true
            onComplete?.invoke()
        }
    }

    suspend fun ensureInitialized(timeoutMs: Long = 4000L) {
        if (_isConfigLoaded.value) return
        withTimeoutOrNull(timeoutMs) {
            _isConfigLoaded.first { it }
        }
    }

    private fun getDebugTestAdId(key: String, fallback: String): String {
        return when (key) {
            BANNER_SPLASH, BANNER_HOME -> SAMPLE_TEST_BANNER_ID
            APP_OPEN_SPLASH -> SAMPLE_TEST_APP_OPEN_ID
            INTERSTITIAL_HOME, INTERSTITIAL_GENERAL -> SAMPLE_TEST_INTERSTITIAL_ID
            NATIVE_GENERAL -> SAMPLE_TEST_NATIVE_ID
            else -> fallback
        }
    }

    val isAllAdsEnabled: Boolean
        get() {
            try {
                // First check inside "remote_config_template" JSON parameter if present from remote
                val templateValue = remoteConfig.getValue(REMOTE_CONFIG_TEMPLATE)
                if (templateValue.source == FirebaseRemoteConfig.VALUE_SOURCE_REMOTE) {
                    val templateStr = templateValue.asString()
                    if (templateStr.isNotBlank()) {
                        val rootJson = JSONObject(templateStr)
                        if (rootJson.has(IS_ALL_ADS_ENABLED)) {
                            return rootJson.getBoolean(IS_ALL_ADS_ENABLED)
                        }
                    }
                }
                // Check top-level parameter "isAllAdsEnabled" if present from remote
                val topLevelValue = remoteConfig.getValue(IS_ALL_ADS_ENABLED)
                if (topLevelValue.source == FirebaseRemoteConfig.VALUE_SOURCE_REMOTE) {
                    return topLevelValue.asBoolean()
                }
                // Fallback to any template or top-level boolean
                val templateStr = remoteConfig.getString(REMOTE_CONFIG_TEMPLATE)
                if (templateStr.isNotBlank()) {
                    val rootJson = JSONObject(templateStr)
                    if (rootJson.has(IS_ALL_ADS_ENABLED)) {
                        return rootJson.getBoolean(IS_ALL_ADS_ENABLED)
                    }
                }
                return remoteConfig.getBoolean(IS_ALL_ADS_ENABLED)
            } catch (e: Exception) {
                return true
            }
        }

    fun getAdConfig(key: String, defaultAdId: String = ""): AdConfig {
        val fallbackAdId = if (BuildConfig.DEBUG) getDebugTestAdId(key, defaultAdId) else defaultAdId

        if (!isAllAdsEnabled) {
            return AdConfig(enabled = false, adId = fallbackAdId)
        }

        var parsedConfig: AdConfig? = null

        // 1. Try reading from individual remote parameter for key first if configured remotely
        try {
            val keyValue = remoteConfig.getValue(key)
            if (keyValue.source == FirebaseRemoteConfig.VALUE_SOURCE_REMOTE) {
                val jsonString = keyValue.asString()
                if (jsonString.isNotBlank()) {
                    val json = JSONObject(jsonString)
                    val enabled = json.optBoolean("enabled", true)
                    val remoteAdId = json.optString("ad_id", "").trim()
                    if (remoteAdId.isNotBlank() || !enabled) {
                        parsedConfig = AdConfig(enabled = enabled, adId = remoteAdId)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing remote JSON for key $key: $e")
        }

        // 2. Try reading from "remote_config_template" parameter JSON
        if (parsedConfig == null) {
            try {
                val templateStr = remoteConfig.getString(REMOTE_CONFIG_TEMPLATE)
                if (templateStr.isNotBlank()) {
                    val rootJson = JSONObject(templateStr)
                    if (rootJson.has(key)) {
                        val adObj = rootJson.optJSONObject(key)
                        if (adObj != null) {
                            val enabled = adObj.optBoolean("enabled", true)
                            val remoteAdId = adObj.optString("ad_id", "").trim()
                            if (remoteAdId.isNotBlank() || !enabled) {
                                parsedConfig = AdConfig(enabled = enabled, adId = remoteAdId)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing key $key from $REMOTE_CONFIG_TEMPLATE: $e")
            }
        }

        // 3. Fallback to reading individual parameter for key
        if (parsedConfig == null) {
            parsedConfig = try {
                val jsonString = remoteConfig.getString(key)
                if (jsonString.isNotBlank()) {
                    val json = JSONObject(jsonString)
                    val enabled = json.optBoolean("enabled", true)
                    val remoteAdId = json.optString("ad_id", "").trim()
                    AdConfig(enabled = enabled, adId = remoteAdId)
                } else {
                    AdConfig(enabled = true, adId = fallbackAdId)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing JSON for key $key: $e")
                AdConfig(enabled = true, adId = fallbackAdId)
            }
        }

        val finalAdId = if (BuildConfig.DEBUG) {
            getDebugTestAdId(key, defaultAdId)
        } else {
            parsedConfig.adId.ifBlank { fallbackAdId }
        }

        return AdConfig(
            enabled = parsedConfig.enabled,
            adId = finalAdId
        )
    }

    fun isAdEnabled(key: String): Boolean {
        return getAdConfig(key).enabled
    }

    fun getAdId(key: String, defaultId: String): String {
        return getAdConfig(key, defaultId).adId
    }
}
