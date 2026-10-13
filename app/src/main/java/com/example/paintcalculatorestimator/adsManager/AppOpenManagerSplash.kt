package com.example.paintcalculatorestimator.adsManager

import android.app.Activity
import android.app.Application.ActivityLifecycleCallbacks
import android.os.Bundle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import com.example.paintcalculatorestimator.PaintCalculatorApp
import com.example.paintcalculatorestimator.R
import com.example.paintcalculatorestimator.utils.logE
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd


import java.util.Date

class AppOpenManagerSplash(myApplication: PaintCalculatorApp, currentActivity: Activity, val onAdLoaded: () -> Unit, val onAdFailed: () -> Unit, val onAdDismiss: () -> Unit) : LifecycleObserver, ActivityLifecycleCallbacks {
    private val myApplication: PaintCalculatorApp
    private var currentActivity: Activity
    private var isLoadingAd = false
    private var loadTime: Long = 0

    init {
        isTimeUpSplash = false
        this.currentActivity = currentActivity
        currentActivity.logE("open Ad AppOpenManagerSplash : currentActivity = $currentActivity")
        this.myApplication = myApplication
        this.myApplication.registerActivityLifecycleCallbacks(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        fetchAd()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onStart() {

    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume() {
    }

    private fun fetchAd() {
        currentActivity.logE("open Ad isAdAvailable54. $isAdAvailable")
        if (isLoadingAd || isAdAvailable) {
            return
        }
        currentActivity.logE("open Ad isAdAvailable58. $isAdAvailable")
        isLoadingAd = true
        val request = AdRequest.Builder().build()
        AppOpenAd.load(
            currentActivity,
            currentActivity.getString(R.string.open_app_ad_splash),
            request,
            object : AppOpenAd.AppOpenAdLoadCallback() {
//        AppOpenAd.load(
//            AdRequest.Builder(AppUtils.app_open_ad_id).build(),
//            object : AdLoadCallback<AppOpenAd> {
                override fun onAdLoaded(ad: AppOpenAd) {
                    // Called when an ad has loaded.
                    currentActivity.runOnUiThread {
                        appOpenAd = ad
                        loadTime = Date().time
                        isLoadingAd = false
                        currentActivity.logE("open ad Loaded")
                        onAdLoaded.invoke()
                        showAdIfAvailable()
                    }
                }
                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    isLoadingAd = false
                    currentActivity.logE("open Ad Failed To Load: " + loadAdError.message)
                    onAdFailed.invoke()
                }
            },
        )

    }

    fun showAdIfAvailable() {
            currentActivity.logE("open Ad isShowingAd. $isShowingAd")
            currentActivity.logE("open Ad isAdAvailable. $isAdAvailable")
            currentActivity.logE("open Ad isTimeUpSplash. $isTimeUpSplash")
        if (!isShowingAd && isAdAvailable && !isTimeUpSplash) {
            com.example.paintcalculatorestimator.utils.AdLoadingDialog.showLoadingDialog(currentActivity)
            val fullScreenContentCallback: FullScreenContentCallback =
                object : FullScreenContentCallback() {
                    override fun onAdShowedFullScreenContent() {
                        myApplication.isAppOpenShowing = true
                        isShowingAd = true
                    }

                    override fun onAdDismissedFullScreenContent() {
                        currentActivity.runOnUiThread {
                            com.example.paintcalculatorestimator.utils.AdLoadingDialog.hideLoadingDialog()
                            myApplication.isAppOpenShowing = false
                            appOpenAd = null
                            onAdDismiss.invoke()
                        }
                    }

                    override fun onAdFailedToShowFullScreenContent(fullScreenContentError: AdError) {
                        currentActivity.runOnUiThread {
                            com.example.paintcalculatorestimator.utils.AdLoadingDialog.hideLoadingDialog()
                            onAdDismiss.invoke()
                            currentActivity.logE("open Ad onAdFailedToShowFullScreenContent: " + fullScreenContentError.message)
                        }
                    }

                    override fun onAdImpression() {

                    }

                    override fun onAdClicked() {

                    }
                }

            isShowingAd = true
            appOpenAd!!.fullScreenContentCallback = fullScreenContentCallback
            appOpenAd?.show(currentActivity)

        } else {
            onAdFailed.invoke()
            currentActivity.logE("open Ad Can not show ad.")
        }
    }

    private fun wasLoadTimeLessThanNHoursAgo(numHours: Long): Boolean {
        val dateDifference = Date().time - loadTime
        val numMilliSecondsPerHour: Long = 3600000
        return dateDifference < numMilliSecondsPerHour * numHours
    }

    val isAdAvailable: Boolean
        get() = appOpenAd != null && wasLoadTimeLessThanNHoursAgo(4)

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        currentActivity.logE("onActivityCreated: $activity", )
    }
    override fun onActivityStarted(activity: Activity) {
        currentActivity = activity
        currentActivity.logE("showAdIfAvailable :  currentActivity = $currentActivity")
    }

    override fun onActivityResumed(activity: Activity) {
        currentActivity.logE("onActivityResumed: $activity", )
        currentActivity = activity
    }

    override fun onActivityPaused(activity: Activity) {
        currentActivity.logE("onActivityPaused: $activity", )
    }
    override fun onActivityStopped(activity: Activity) {
        currentActivity.logE("onActivityStopped: $activity", )
    }
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {
    }

    companion object {
        private const val TAG = "AppOpenManagerSplash"
        var isShowingAd = false
        var isTimeUpSplash = false
        var appOpenAd: AppOpenAd? = null
    }
}