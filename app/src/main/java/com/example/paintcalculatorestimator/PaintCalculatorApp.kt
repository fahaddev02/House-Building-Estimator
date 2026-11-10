package com.example.paintcalculatorestimator

import android.app.Application
import com.example.paintcalculatorestimator.adsManager.RemoteConfigManager
import com.example.paintcalculatorestimator.billing.BillingManager
import dagger.hilt.android.HiltAndroidApp
import com.example.paintcalculatorestimator.adsManager.InterstitialHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@HiltAndroidApp
class PaintCalculatorApp : Application() {

    override fun onCreate() {
        super.onCreate()
        RemoteConfigManager.init()
        val billing = BillingManager.getInstance(this)
        CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate).launch {
            billing.entitlement.state.collect { state ->
                if (!state.canShowAds) {
                    InterstitialHelper.clearCachedAds()
                }
            }
        }
    }
}
