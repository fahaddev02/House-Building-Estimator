package com.example.paintcalculatorestimator.adsManager

import android.app.Activity
import android.util.DisplayMetrics
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.paintcalculatorestimator.utils.PrefUtil
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdSize.getInlineAdaptiveBannerAdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.example.paintcalculatorestimator.utils.logE
import com.example.paintcalculatorestimator.adsManager.InterstitialHelper.isNetworkAvailable

object BannerAdManager {
    var adView: AdView? = null
    fun Activity.innerLoadBanner(bannerAdId: String, adContainer: FrameLayout, shimmerEffect: ConstraintLayout) {
        if (!PrefUtil(this).getPremium() && isNetworkAvailable(this)
        ) {
            adContainer.visibility = View.VISIBLE
            adView = AdView(this)
            adView?.adUnitId = bannerAdId
//            adView?.setAdSize(getAdSize(adContainer))
            adView?.setAdSize(getInlineAdaptiveBannerAdSize(LinearLayout.LayoutParams.MATCH_PARENT,100))
            val adRequest = AdRequest.Builder().build()
            adContainer.addView(adView)
            adView?.loadAd(adRequest)
            adView?.adListener = object : AdListener() {
                override fun onAdLoaded() {
                    super.onAdLoaded()
                    shimmerEffect.visibility = View.GONE
                    logE("Banner loaded successfully")
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    super.onAdFailedToLoad(adError)
                    adContainer.visibility = View.GONE
                    shimmerEffect.visibility = View.GONE
                    logE("Banner failed to load: ${adError.message} and ${adError.code}"
                    )
                }

                override fun onAdOpened() {
                    super.onAdOpened()
                    logE("Banner clicked (opened)")
                }

                override fun onAdClicked() {
                    super.onAdClicked()
                    logE("Banner ad clicked")
                }

                override fun onAdClosed() {
                    super.onAdClosed()
                    logE("Banner closed")
                }

                override fun onAdImpression() {
                    super.onAdImpression()
                    logE("Banner impression logged")
                }
            }
        } else {
            adContainer.visibility = View.GONE
//            shimmerMain.visibility = View.GONE
        }
    }
    fun Activity.loadAdaptiveBanner(bannerAdId: String, adContainer: FrameLayout/*,shimmerEffect: LottieAnimationView*/) {
        if (!PrefUtil(this).getPremium() && isNetworkAvailable(this)
        ) {
//            shimmerEffect.visibility = View.VISIBLE
            adContainer.visibility = View.VISIBLE
            adView = AdView(this)
            adView?.adUnitId = bannerAdId
//            adView?.setAdSize(getAdSize(adContainer))
            adView?.setAdSize(getInlineAdaptiveBannerAdSize(LinearLayout.LayoutParams.MATCH_PARENT,100))
            val adRequest = AdRequest.Builder().build()
            adContainer.addView(adView)
            adView?.loadAd(adRequest)
            adView?.adListener = object : AdListener() {
                override fun onAdLoaded() {
                    super.onAdLoaded()
//                    shimmerEffect.visibility = View.GONE
                    logE("Banner loaded successfully")
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    super.onAdFailedToLoad(adError)
                    adContainer.visibility = View.GONE
//                    shimmerEffect.visibility = View.GONE
                    logE("Banner failed to load: ${adError.message} and ${adError.code}"
                    )
                }

                override fun onAdOpened() {
                    super.onAdOpened()
                    logE("Banner clicked (opened)")
                }

                override fun onAdClicked() {
                    super.onAdClicked()
                    logE("Banner ad clicked")
                }

                override fun onAdClosed() {
                    super.onAdClosed()
                    logE("Banner closed")
                }

                override fun onAdImpression() {
                    super.onAdImpression()
                    logE("Banner impression logged")
                }
            }
        } else {
            adContainer.visibility = View.GONE
//            shimmerEffect.visibility = View.GONE
        }
    }


    fun Activity.getAdSize(adContainer: FrameLayout): AdSize {
        val display = this.windowManager.defaultDisplay
        val outMetrics = DisplayMetrics()
        display.getMetrics(outMetrics)
        val density = outMetrics.density
        var adWidthPixels = adContainer.width.toFloat()
        if (adWidthPixels == 0f) {
            adWidthPixels = outMetrics.widthPixels.toFloat()
        }
        val adWidth = (adWidthPixels / density).toInt()
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this, adWidth)
    }
}
