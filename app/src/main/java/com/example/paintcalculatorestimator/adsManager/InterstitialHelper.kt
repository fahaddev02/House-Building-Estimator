package com.example.paintcalculatorestimator.adsManager

import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Handler
import android.os.Looper
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.example.paintcalculatorestimator.billing.EntitlementStore
import com.example.paintcalculatorestimator.utils.AdLoadingDialog
import com.example.paintcalculatorestimator.utils.FirebaseEvents
import com.example.paintcalculatorestimator.utils.FirebaseEvents.setEvent
import com.example.paintcalculatorestimator.utils.logE

object InterstitialHelper {
    private var cachedInterstitial: InterstitialAd? = null
    private val pendingSkips = mutableSetOf<() -> Unit>()

    fun loadAndShowInterstitialAd(
        activity: Activity,
        adId: String,
        event: String,
        dismiss: () -> Unit = {},
        adShown: () -> Unit = {},
        failedMoveNext: () -> Unit = {}
    ) {
        activity.logE("loadAndShowInterstitialAd cachedInterstitial: $cachedInterstitial")
        if (EntitlementStore.from(activity).canShowAds && isNetworkAvailable(activity)
            && !activity.isFinishing && !activity.isDestroyed
        ) {
            activity.logE("loadAndShowInterstitialAd called with: $adId")
            AdLoadingDialog.showLoadingDialog(activity)
            if (cachedInterstitial != null) {
                showInterAd(activity, "${event}_inter_ad_shown", dismiss, adShown, failedMoveNext)
                return
            }

            activity.setEvent("${event}_inter_ad_requested")

            var isAdHandled = false
            lateinit var skipRequest: () -> Unit

            // Create timeout handler
            val timeoutHandler = Handler(Looper.getMainLooper())
            val timeoutRunnable = Runnable {
                if (!isAdHandled) {
                    activity.logE("loadAndShowInterstitialAd Ad loading timeout - time exceeded")
                    isAdHandled = true
                    pendingSkips.remove(skipRequest)
                    AdLoadingDialog.hideLoadingDialog()
                    activity.setEvent(FirebaseEvents.INTER_AD_SKIP_ALL_APP)
                    failedMoveNext()
                }
            }
            skipRequest = {
                if (!isAdHandled) {
                    isAdHandled = true
                    timeoutHandler.removeCallbacks(timeoutRunnable)
                    pendingSkips.remove(skipRequest)
                    AdLoadingDialog.hideLoadingDialog()
                    failedMoveNext()
                }
            }
            pendingSkips.add(skipRequest)

            // Start timeout timer (7 seconds timeout)
            timeoutHandler.postDelayed(timeoutRunnable, 7000)

            InterstitialAd.load(activity, adId, AdRequest.Builder().build(),
                object : InterstitialAdLoadCallback() {
                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        timeoutHandler.removeCallbacks(timeoutRunnable)
                        if (!isAdHandled) {
                            activity.logE("loadAndShowInterstitialAd onAdFailedToLoad called with ID: $adId")
                            isAdHandled = true
                            pendingSkips.remove(skipRequest)
                            activity.setEvent("${event}_inter_ad_failed")
                            activity.logE(adError.message)
                            AdLoadingDialog.hideLoadingDialog()
                            cachedInterstitial = null
                            activity.logE("loadAndShowInterstitialAd Failed code: " + adError.code + ", message: " + adError.message)
                            failedMoveNext()
                        }
                    }

                    override fun onAdLoaded(interstitialAd: InterstitialAd) {
                        // Remove timeout callback
                        timeoutHandler.removeCallbacks(timeoutRunnable)

                        if (!isAdHandled) {
                            activity.logE("loadAndShowInterstitialAd onAdLoaded called with ID: $adId")
                            isAdHandled = true
                            pendingSkips.remove(skipRequest)
                            activity.setEvent("${event}_inter_ad_loaded")
                            cachedInterstitial = interstitialAd
                            showInterAd(activity, "${event}_inter_ad_shown", dismiss, adShown, failedMoveNext)
                        }
                    }
                })
        } else {
            failedMoveNext()
        }
    }

    private fun showInterAd(
        activity: Activity,
        showEvent: String,
        dismiss: () -> Unit = {},
        adShown: () -> Unit = {},
        failedMoveNext: () -> Unit = {}
    ) {
        if (!EntitlementStore.from(activity).canShowAds || activity.isFinishing || activity.isDestroyed || cachedInterstitial == null) {
            cachedInterstitial = null
            AdLoadingDialog.hideLoadingDialog()
            failedMoveNext()
            return
        }
        cachedInterstitial?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdImpression() {
                activity.logE("loadAndShowInterstitialAd onAdImpression splash called")
                super.onAdImpression()
            }

            override fun onAdDismissedFullScreenContent() {
                activity.logE("loadAndShowInterstitialAd onAdDismissedFullScreenContent splash called")
                super.onAdDismissedFullScreenContent()
                cachedInterstitial = null
                AdLoadingDialog.hideLoadingDialog()
                dismiss()
            }

            override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                activity.logE("loadAndShowInterstitialAd onAdFailedToShowFullScreenContent splash called")
                super.onAdFailedToShowFullScreenContent(p0)
                cachedInterstitial = null
                AdLoadingDialog.hideLoadingDialog()
                failedMoveNext()
            }

        }
        activity.setEvent(showEvent)
        adShown()
        cachedInterstitial?.show(activity)
    }

    fun clearCachedAds() {
        cachedInterstitial = null
        AdLoadingDialog.hideLoadingDialog()
        pendingSkips.toList().forEach { it() }
    }

    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val nw = connectivityManager.activeNetwork ?: return false
        val actNw = connectivityManager.getNetworkCapabilities(nw) ?: return false
        return when {
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }
    }
}
