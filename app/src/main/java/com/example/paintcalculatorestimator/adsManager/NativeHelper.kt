package com.example.paintcalculatorestimator.adsManager

import android.content.Context
import android.graphics.Color
import android.view.View
import android.widget.FrameLayout
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.example.paintcalculatorestimator.utils.PrefUtil
import com.example.paintcalculatorestimator.utils.FirebaseEvents.setEvent
import com.example.paintcalculatorestimator.adsManager.InterstitialHelper.isNetworkAvailable
import com.example.paintcalculatorestimator.utils.logE

class NativeHelper(private val context: Context) {
    val TAG = "NativeHelper"

    fun loadAndShowWithConfiguration(
        nativeContainer: ConstraintLayout,
        adMobContainer: FrameLayout,
        adMobId: String?,
        size: Int = 300,
        event: String = "",
        titleColor: Int = Color.BLACK,
        bodyColor: Int = Color.BLACK,
        backgroundColor: Int = Color.WHITE,
        borderColor: Int = Color.BLUE,
        ctaBackground: Int = Color.BLUE,
        ctaTextColor: Int = Color.WHITE,
        nativeAdObject: (NativeAd?) -> Unit = {}
    ) {
        if (!PrefUtil(context).getPremium() && isNetworkAvailable(context)) {
            if (adMobNativeAd == null && adMobId != null) {
                context.logE("load call with id: $adMobId")
                context.setEvent("${event}_native_ad_request")

                nativeContainer.visibility = View.VISIBLE
                val builder: AdLoader.Builder = AdLoader.Builder(context.applicationContext, adMobId)
                val adLoader = builder.forNativeAd { unifiedNativeAd: NativeAd? ->
                    adMobNativeAd = unifiedNativeAd
                }.withAdListener(object : AdListener() {
                    override fun onAdFailedToLoad(i: LoadAdError) {
                        super.onAdFailedToLoad(i)
                        context.setEvent("${event}_native_ad_failed")
                        context.logE("Failed; code: " + i.code + ", message: " + i.message)
                        nativeContainer.visibility = View.GONE
                        nativeAdObject(null)
                    }

                    override fun onAdLoaded() {
                        super.onAdLoaded()
                        context.setEvent("${event}_native_ad_loaded")
                        val privateAD = adMobNativeAd
                        nativeContainer.visibility = View.VISIBLE
                        adMobContainer.visibility = View.VISIBLE
                        nativeAdObject(privateAD)
                        adMobNativeAd = null
                    }
                }).withNativeAdOptions(
                    NativeAdOptions.Builder()
                        .setAdChoicesPlacement(NativeAdOptions.ADCHOICES_TOP_RIGHT).build()
                ).build()
                adLoader.loadAd(AdRequest.Builder().build())
            }
        } else {
            nativeContainer.visibility = View.GONE
        }
    }

    companion object {
        var adMobNativeAd: NativeAd? = null
        var adMobNativeAdLanguage: NativeAd? = null
        var adMobNativeAdIntro: NativeAd? = null
    }
}
