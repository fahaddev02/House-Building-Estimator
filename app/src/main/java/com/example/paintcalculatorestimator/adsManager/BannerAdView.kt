package com.example.paintcalculatorestimator.adsManager

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.example.paintcalculatorestimator.utils.PrefUtil
import com.example.paintcalculatorestimator.adsManager.InterstitialHelper.isNetworkAvailable

@Composable
fun BannerAdView(
    adConfigKey: String = RemoteConfigManager.BANNER_SPLASH,
    defaultAdId: String = RemoteConfigManager.TEST_BANNER_ID,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isPremium = remember { PrefUtil(context).getPremium() }
    val isNetwork = remember { isNetworkAvailable(context) }
    val adConfig = remember(adConfigKey) {
        RemoteConfigManager.getAdConfig(adConfigKey, defaultAdId)
    }

    if (!isPremium && isNetwork && adConfig.enabled) {
        val adView = remember {
            AdView(context).apply {
                setAdSize(AdSize.LARGE_BANNER) // 320x100
                adUnitId = adConfig.adId
                loadAd(AdRequest.Builder().build())
            }
        }

        DisposableEffect(adView) {
            onDispose { adView.destroy() }
        }

        AndroidView(
            modifier = modifier
                .fillMaxWidth()
                .height(100.dp), // reserves the space so layout doesn't jump
            factory = { adView }
        )
    }
}