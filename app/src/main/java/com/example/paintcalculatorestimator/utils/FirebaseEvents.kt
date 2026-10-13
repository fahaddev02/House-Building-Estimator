package com.example.paintcalculatorestimator.utils

import android.content.Context
import android.os.Bundle
import android.util.Log
import com.example.paintcalculatorestimator.BuildConfig
import com.google.firebase.analytics.FirebaseAnalytics
import kotlin.apply
import kotlin.text.substring


object FirebaseEvents {

    fun Context.setEvent(eventName: String) {
        var finalEventName = eventName
        if (finalEventName.length > 40) {
            finalEventName = finalEventName.substring(0, 40)
        }

        if (!BuildConfig.DEBUG) {
            val params = Bundle().apply {
                putString(FirebaseAnalytics.Param.ITEM_NAME, finalEventName)
            }
            FirebaseAnalytics.getInstance(this)
                .logEvent(finalEventName, params)
        } else{
            Log.e("FirebaseEvents", "setEvent: ${eventName}", )
        }
    }

    const val INTER_AD_SKIP_ALL_APP = "inter_ad_skip_all_app"

}