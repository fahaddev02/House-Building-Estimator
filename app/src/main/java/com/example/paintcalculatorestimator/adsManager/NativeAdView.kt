package com.example.paintcalculatorestimator.adsManager

import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.paintcalculatorestimator.R
import com.example.paintcalculatorestimator.utils.PrefUtil
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView as GoogleNativeAdView

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NativeAdView(
    modifier: Modifier = Modifier,
    adKey: String = RemoteConfigManager.NATIVE_GENERAL,
    defaultAdId: String = RemoteConfigManager.TEST_NATIVE_ID
) {
    val context = LocalContext.current
    val isPremium = remember { PrefUtil(context).getPremium() }
    val adConfig = remember { RemoteConfigManager.getAdConfig(adKey, defaultAdId) }

    if (isPremium || !adConfig.enabled || adConfig.adId.isBlank()) {
        return
    }

    var loadedNativeAd by remember { mutableStateOf<NativeAd?>(null) }
    var isAdFailed by remember { mutableStateOf(false) }

    DisposableEffect(adConfig.adId) {
        val adLoader = AdLoader.Builder(context.applicationContext, adConfig.adId)
            .forNativeAd { nativeAd ->
                loadedNativeAd = nativeAd
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(error: LoadAdError) {
                    isAdFailed = true
                }
            })
            .withNativeAdOptions(
                NativeAdOptions.Builder()
                    .setAdChoicesPlacement(NativeAdOptions.ADCHOICES_TOP_RIGHT)
                    .build()
            )
            .build()

        adLoader.loadAd(AdRequest.Builder().build())

        onDispose {
            loadedNativeAd?.destroy()
        }
    }

    val isKeyboardVisible = WindowInsets.isImeVisible

    if (isAdFailed || isKeyboardVisible) {
        return
    }

    if (loadedNativeAd == null) {
        NativeAdShimmer(modifier = modifier)
    } else {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .navigationBarsPadding()
                .padding(horizontal = 4.dp, vertical = 2.dp)
        ) {
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                factory = { ctx ->
                    val view = LayoutInflater.from(ctx).inflate(R.layout.layout_native_ad, null) as GoogleNativeAdView
                    populateNativeAdView(loadedNativeAd!!, view)
                    view
                },
                update = { view ->
                    loadedNativeAd?.let { populateNativeAdView(it, view) }
                }
            )
        }
    }
}

@Composable
fun NativeAdShimmer(modifier: Modifier = Modifier) {
    val shimmerColors = listOf(
        Color(0xFFE0E0E0),
        Color(0xFFF5F5F5),
        Color(0xFFE0E0E0),
    )
    val transition = rememberInfiniteTransition(label = "NativeAdShimmer")
    val translateAnim = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ShimmerAnim"
    )
    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset.Zero,
        end = Offset(x = translateAnim.value, y = translateAnim.value)
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .navigationBarsPadding()
            .padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Native Ad Media View Shimmer (150dp x 120dp)
                Box(
                    modifier = Modifier
                        .width(150.dp)
                        .height(120.dp)
                        .background(brush, shape = RoundedCornerShape(5.dp))
                )

                // Right Native Ad Details & Action Button Shimmer
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .height(120.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(width = 24.dp, height = 16.dp)
                                    .background(brush, shape = RoundedCornerShape(3.dp))
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.85f)
                                    .height(16.dp)
                                    .background(brush, shape = RoundedCornerShape(4.dp))
                            )
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.95f)
                                .height(12.dp)
                                .background(brush, shape = RoundedCornerShape(3.dp))
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.7f)
                                .height(12.dp)
                                .background(brush, shape = RoundedCornerShape(3.dp))
                        )
                    }

                    // Call To Action Button Shimmer
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .background(brush, shape = RoundedCornerShape(6.dp))
                    )
                }
            }
        }
    }
}

private fun populateNativeAdView(nativeAd: NativeAd, nativeAdView: GoogleNativeAdView) {
    nativeAdView.headlineView = nativeAdView.findViewById(R.id.ad_headline)
    nativeAdView.bodyView = nativeAdView.findViewById(R.id.ad_body)
    nativeAdView.callToActionView = nativeAdView.findViewById(R.id.ad_call_to_action)
    nativeAdView.iconView = nativeAdView.findViewById(R.id.ad_app_icon)
    nativeAdView.mediaView = nativeAdView.findViewById(R.id.ad_media)

    (nativeAdView.headlineView as? TextView)?.text = nativeAd.headline

    if (nativeAd.body.isNullOrBlank()) {
        nativeAdView.bodyView?.visibility = View.INVISIBLE
    } else {
        nativeAdView.bodyView?.visibility = View.VISIBLE
        (nativeAdView.bodyView as? TextView)?.text = nativeAd.body
    }

    if (nativeAd.callToAction.isNullOrBlank()) {
        nativeAdView.callToActionView?.visibility = View.INVISIBLE
    } else {
        nativeAdView.callToActionView?.visibility = View.VISIBLE
        (nativeAdView.callToActionView as? TextView)?.text = nativeAd.callToAction
        (nativeAdView.callToActionView as? Button)?.text = nativeAd.callToAction
    }

    val icon = nativeAd.icon
    val mediaView = nativeAdView.mediaView as? MediaView
    val iconView = nativeAdView.iconView as? ImageView

    if (nativeAd.mediaContent != null) {
        mediaView?.setMediaContent(nativeAd.mediaContent!!)
        mediaView?.visibility = View.VISIBLE
        iconView?.visibility = View.GONE
    } else if (icon != null) {
        iconView?.setImageDrawable(icon.drawable)
        iconView?.visibility = View.VISIBLE
        mediaView?.visibility = View.GONE
    }

    nativeAdView.setNativeAd(nativeAd)
}
