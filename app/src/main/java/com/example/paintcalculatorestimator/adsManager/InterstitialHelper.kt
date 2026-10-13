package com.example.paintcalculatorestimator.adsManager

import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Handler
import android.os.Looper
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.example.paintcalculatorestimator.utils.PrefUtil
import com.example.paintcalculatorestimator.utils.AdLoadingDialog
import com.example.paintcalculatorestimator.utils.FirebaseEvents
import com.example.paintcalculatorestimator.utils.FirebaseEvents.setEvent
import com.example.paintcalculatorestimator.utils.logE
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.concurrent.TimeUnit

object InterstitialHelper {
    private val TAG = "InterstitialHelper"
    var interstitialTimeElapsed = 0L
    var mInterstitialAd: InterstitialAd? = null
    var mInterstitialAdSplash: InterstitialAd? = null
    private var isAdLoading = false
    var isInterAdShowing = false
    var interAdIntervalCounter = -1
    var isSplashInterTimeUp = false

    fun loadAndShowInterstitialAd(
        activity: Activity,
        adId: String,
        event: String,
        dismiss: () -> Unit = {},
        adShown: () -> Unit = {},
        failedMoveNext: () -> Unit = {}
    ) {
        activity.logE("loadAndShowInterstitialAd mInterstitialAd: $mInterstitialAd")
        if (isNetworkAvailable(activity) && !PrefUtil(activity).getPremium()
            && !activity.isFinishing && !activity.isDestroyed
        ) {
            activity.logE("loadAndShowInterstitialAd called with: $adId")
            isAdLoading = true
            AdLoadingDialog.showLoadingDialog(activity)
            if (mInterstitialAd != null) {
                showInterAd(activity, "${event}_inter_ad_shown", dismiss, adShown, failedMoveNext)
                return
            }

            activity.setEvent("${event}_inter_ad_requested")

            var isAdHandled = false

            // Create timeout handler
            val timeoutHandler = Handler(Looper.getMainLooper())
            val timeoutRunnable = Runnable {
                if (!isAdHandled && isAdLoading) {
                    activity.logE("loadAndShowInterstitialAd Ad loading timeout - time exceeded")
                    isAdHandled = true
                    isAdLoading = false
                    AdLoadingDialog.hideLoadingDialog()
                    activity.setEvent(FirebaseEvents.INTER_AD_SKIP_ALL_APP)
                    failedMoveNext()
                }
            }

            // Start timeout timer (7 seconds timeout)
            timeoutHandler.postDelayed(timeoutRunnable, 7000)

            InterstitialAd.load(activity, adId, AdRequest.Builder().build(),
                object : InterstitialAdLoadCallback() {
                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        timeoutHandler.removeCallbacks(timeoutRunnable)
                        if (!isAdHandled) {
                            activity.logE("loadAndShowInterstitialAd onAdFailedToLoad called with ID: $adId")
                            isAdHandled = true
                            activity.setEvent("${event}_inter_ad_failed")
                            activity.logE(adError.message)
                            AdLoadingDialog.hideLoadingDialog()
                            mInterstitialAd = null
                            isAdLoading = false
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
                            activity.setEvent("${event}_inter_ad_loaded")
                            mInterstitialAd = interstitialAd
                            isAdLoading = false
                            showInterAd(activity, "${event}_inter_ad_shown", dismiss, adShown, failedMoveNext)
                        }
                    }
                })
        } else {
            interAdIntervalCounter++
            failedMoveNext()
        }
    }

    fun showInterAd(
        activity: Activity,
        showEvent: String,
        dismiss: () -> Unit = {},
        adShown: () -> Unit = {},
        failedMoveNext: () -> Unit = {}
    ) {
        mInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdImpression() {
                activity.logE("loadAndShowInterstitialAd onAdImpression splash called")
                super.onAdImpression()
            }

            override fun onAdDismissedFullScreenContent() {
                activity.logE("loadAndShowInterstitialAd onAdDismissedFullScreenContent splash called")
                super.onAdDismissedFullScreenContent()
                interAdIntervalCounter = -1
                interAdIntervalCounter++
                mInterstitialAd = null
                isInterAdShowing = false

                interstitialTimeElapsed = Calendar.getInstance().timeInMillis
                AdLoadingDialog.hideLoadingDialog()
                dismiss()
            }

            override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                activity.logE("loadAndShowInterstitialAd onAdFailedToShowFullScreenContent splash called")
                super.onAdFailedToShowFullScreenContent(p0)
                mInterstitialAd = null
                isInterAdShowing = false
                AdLoadingDialog.hideLoadingDialog()
                failedMoveNext()
            }

            override fun onAdShowedFullScreenContent() {
                super.onAdShowedFullScreenContent()
                isInterAdShowing = true
            }
        }
        activity.setEvent(showEvent)
        adShown()
        mInterstitialAd?.show(activity)
    }

    fun loadInterstitialAdSplash(
        activity: Activity,
        adId: String,
        requestEvent: String,
        failedEvent: String,
        loadedEvent: String,
        showEvent: String,
        dismiss: () -> Unit = {},
        failed: () -> Unit = {},
        loaded: (InterstitialAd?) -> Unit = {},
        alreadyLoaded: () -> Unit = {}
    ) {
        if (mInterstitialAdSplash == null && isNetworkAvailable(activity) && !PrefUtil(activity).getPremium()) {
            activity.logE("loadInterstitialAd splash called with: $adId ")
            isAdLoading = true
            activity.setEvent(requestEvent)
            InterstitialAd.load(activity, adId, AdRequest.Builder().build(),
                object : InterstitialAdLoadCallback() {
                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        activity.logE("onAdFailedToLoad splash called with ID: $adId")
                        activity.logE(adError.message)
                        activity.setEvent(failedEvent)
                        failed()
                        mInterstitialAdSplash = null
                        isAdLoading = false
                        activity.logE("Failed code splash: " + adError.code + ", message: " + adError.message)
                    }

                    override fun onAdLoaded(interstitialAd: InterstitialAd) {
                        activity.logE("onAdLoaded splash called with ID: $adId")
                        mInterstitialAdSplash = interstitialAd
                        isAdLoading = false
                        activity.setEvent(loadedEvent)
                        loaded(interstitialAd)
                        showSplashInterstitial(activity, showEvent, {
                            dismiss()
                        }, failedMoveNext = {
                            failed()
                        })
                    }
                })
        } else {
            alreadyLoaded()
            activity.logE("loadInterstitialAd splash: Ad Already Loaded.")
        }
    }

    fun showSplashInterstitial(
        activity: Activity,
        showEvent: String,
        dismiss: () -> Unit = {},
        impression: () -> Unit = {},
        failedMoveNext: () -> Unit = {}
    ) {
        if (isNetworkAvailable(activity) && !isSplashInterTimeUp && !PrefUtil(activity).getPremium() && !activity.isFinishing && !activity.isDestroyed) {
            if (mInterstitialAdSplash != null) {
                AdLoadingDialog.showLoadingDialog(activity)
                CoroutineScope(IO).launch {
                    delay(2000L)
                    withContext(Main) {
                        activity.setEvent(showEvent)
                        mInterstitialAdSplash?.fullScreenContentCallback =
                            object : FullScreenContentCallback() {
                                override fun onAdImpression() {
                                    activity.logE("onAdImpression splash called")
                                    super.onAdImpression()
                                    impression()
                                }

                                override fun onAdDismissedFullScreenContent() {
                                    activity.logE("onAdDismissedFullScreenContent splash called")
                                    super.onAdDismissedFullScreenContent()
                                    AdLoadingDialog.hideLoadingDialog()
                                    dismiss()
                                    mInterstitialAdSplash = null
                                    interstitialTimeElapsed = Calendar.getInstance().timeInMillis
                                }

                                override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                                    activity.logE("onAdFailedToShowFullScreenContent splash called")
                                    super.onAdFailedToShowFullScreenContent(p0)
                                    AdLoadingDialog.hideLoadingDialog()
                                    mInterstitialAdSplash = null
                                    failedMoveNext()
                                }

                                override fun onAdShowedFullScreenContent() {
                                    super.onAdShowedFullScreenContent()
                                    isInterAdShowing = true
                                }
                            }
                        mInterstitialAdSplash?.show(activity)
                    }
                }
            } else {
                activity.logE("ad is null splash")
                failedMoveNext()
            }
        } else {
            activity.logE("go next some condition false splash")
            failedMoveNext()
        }
    }

    fun timeDifference(millis: Long): Int {
        val current = Calendar.getInstance().timeInMillis
        val elapsedTime = current - millis
        return TimeUnit.MILLISECONDS.toSeconds(elapsedTime).toInt()
    }

    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val nw = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            connectivityManager.activeNetwork ?: return false
        } else null
        val actNw = connectivityManager.getNetworkCapabilities(nw) ?: return false
        return when {
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }
    }
}
