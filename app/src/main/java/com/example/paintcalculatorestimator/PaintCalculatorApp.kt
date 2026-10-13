package com.example.paintcalculatorestimator

import android.app.Application
import com.example.paintcalculatorestimator.adsManager.RemoteConfigManager
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class PaintCalculatorApp : Application() {
    var isAppOpenShowing = false

    override fun onCreate() {
        super.onCreate()
        RemoteConfigManager.init()
    }
}
