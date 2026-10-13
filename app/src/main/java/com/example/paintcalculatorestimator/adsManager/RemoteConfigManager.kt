package com.example.paintcalculatorestimator.adsManager

import android.util.Log
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
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

    // Test Ad Unit IDs (AdMob Sample Test IDs)
    const val TEST_BANNER_ID = "ca-app-pub-3940256099942544/6300978111"
    const val TEST_APP_OPEN_ID = "ca-app-pub-3940256099942544/9257395921"
    const val TEST_INTERSTITIAL_ID = "ca-app-pub-3940256099942544/1033173712"
    const val TEST_NATIVE_ID = "ca-app-pub-3940256099942544/2247696110"

    private val remoteConfig: FirebaseRemoteConfig by lazy {
        FirebaseRemoteConfig.getInstance()
    }

    fun init(onComplete: (() -> Unit)? = null) {
        try {
            val configSettings = FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(0) // Fetch immediately for testing
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
                        Log.d(TAG, "RemoteConfig fetch and activate succeeded")
                    } else {
                        Log.e(TAG, "RemoteConfig fetch failed: ${task.exception}")
                    }
                    onComplete?.invoke()
                }
        } catch (e: Exception) {
            Log.e(TAG, "RemoteConfig init exception: $e")
            onComplete?.invoke()
        }
    }

    val isAllAdsEnabled: Boolean
        get() {
            try {
                // First check inside "remote_config_template" JSON parameter if present
                val templateStr = remoteConfig.getString(REMOTE_CONFIG_TEMPLATE)
                if (templateStr.isNotBlank()) {
                    val rootJson = JSONObject(templateStr)
                    if (rootJson.has(IS_ALL_ADS_ENABLED)) {
                        return rootJson.getBoolean(IS_ALL_ADS_ENABLED)
                    }
                }
                // Fallback to top-level parameter "isAllAdsEnabled"
                return remoteConfig.getBoolean(IS_ALL_ADS_ENABLED)
            } catch (e: Exception) {
                return true
            }
        }

    fun getAdConfig(key: String, defaultAdId: String = ""): AdConfig {
        if (!isAllAdsEnabled) {
            return AdConfig(enabled = false, adId = defaultAdId)
        }

        try {
            // 1. Try reading from "remote_config_template" parameter JSON
            val templateStr = remoteConfig.getString(REMOTE_CONFIG_TEMPLATE)
            if (templateStr.isNotBlank()) {
                val rootJson = JSONObject(templateStr)
                if (rootJson.has(key)) {
                    val adObj = rootJson.optJSONObject(key)
                    if (adObj != null) {
                        val enabled = adObj.optBoolean("enabled", true)
                        val adId = adObj.optString("ad_id", defaultAdId).ifBlank { defaultAdId }
                        return AdConfig(enabled = enabled, adId = adId)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing key $key from $REMOTE_CONFIG_TEMPLATE: $e")
        }

        // 2. Fallback to reading individual parameter for key
        return try {
            val jsonString = remoteConfig.getString(key)
            if (jsonString.isNotBlank()) {
                val json = JSONObject(jsonString)
                val enabled = json.optBoolean("enabled", true)
                val adId = json.optString("ad_id", defaultAdId).ifBlank { defaultAdId }
                AdConfig(enabled = enabled, adId = adId)
            } else {
                AdConfig(enabled = true, adId = defaultAdId)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing JSON for key $key: $e")
            AdConfig(enabled = true, adId = defaultAdId)
        }
    }

    fun isAdEnabled(key: String): Boolean {
        return getAdConfig(key).enabled
    }

    fun getAdId(key: String, defaultId: String): String {
        return getAdConfig(key, defaultId).adId
    }
}
