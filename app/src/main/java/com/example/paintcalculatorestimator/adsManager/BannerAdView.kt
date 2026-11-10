package com.example.paintcalculatorestimator.adsManager

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.example.paintcalculatorestimator.billing.EntitlementStore
import androidx.compose.ui.platform.LocalInspectionMode
import com.example.paintcalculatorestimator.adsManager.InterstitialHelper.isNetworkAvailable

@Composable
fun BannerAdView(
    adConfigKey: String = RemoteConfigManager.BANNER_SPLASH,
    defaultAdId: String = RemoteConfigManager.TEST_BANNER_ID,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    if (LocalInspectionMode.current) return
    val entitlementStore = remember(context) { EntitlementStore.from(context) }
    val canShowAds = rememberAdAccess()
    val isNetwork = remember { isNetworkAvailable(context) }
    val isConfigLoaded by RemoteConfigManager.isConfigLoaded.collectAsState()
    val adConfig = remember(adConfigKey, isConfigLoaded) {
        RemoteConfigManager.getAdConfig(adConfigKey, defaultAdId)
    }

    if (canShowAds && isNetwork && adConfig.enabled && adConfig.adId.isNotBlank()) {
        val adView = remember(adConfig.adId) {
            AdView(context).apply {
                setAdSize(AdSize.LARGE_BANNER) // 320x100
                adUnitId = adConfig.adId
            }
        }

        DisposableEffect(adView) {
            if (entitlementStore.canShowAds) adView.loadAd(AdRequest.Builder().build())
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
