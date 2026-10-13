package com.example.paintcalculatorestimator.presentation.splash

import android.app.Activity
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.paintcalculatorestimator.PaintCalculatorApp
import com.example.paintcalculatorestimator.R
import com.example.paintcalculatorestimator.adsManager.BannerAdView
import com.example.paintcalculatorestimator.adsManager.InterstitialHelper
import com.example.paintcalculatorestimator.adsManager.RemoteConfigManager
import com.example.paintcalculatorestimator.data.preferences.AppPreferences
import com.example.paintcalculatorestimator.presentation.navigation.Screen
import com.example.paintcalculatorestimator.ui.theme.Lavender
import com.example.paintcalculatorestimator.ui.theme.PaintCalculatorEstimatorTheme
import com.example.paintcalculatorestimator.utils.PrefUtil
import com.example.paintcalculatorestimator.utils.logE
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(
    navController: NavController,
    appPreferences: AppPreferences
) {
    val context = LocalContext.current as? Activity
    val coroutineScope = rememberCoroutineScope()
    val scale = remember { Animatable(0f) }
    val alpha = remember { Animatable(0f) }
    val backdrop = if (MaterialTheme.colorScheme.background.luminance() > 0.5f) Lavender else MaterialTheme.colorScheme.primaryContainer

    fun navigateToNextScreen() {
        coroutineScope.launch {
            val onboardingCompleted = appPreferences.isOnboardingCompletedFlow.first()
            if (onboardingCompleted) {
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Splash.route) { inclusive = true }
                }
            } else {
                navController.navigate(Screen.Onboarding.route) {
                    popUpTo(Screen.Splash.route) { inclusive = true }
                }
            }
        }
    }

    LaunchedEffect(key1 = true) {
        // Animate logo
        launch {
            scale.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 800)
            )
        }
        launch {
            alpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 800)
            )
        }

        if (context == null) {
            delay(3000L)
            navigateToNextScreen()
            return@LaunchedEffect
        }

        val isInternetAvailable = InterstitialHelper.isNetworkAvailable(context)
        val appOpenConfig = RemoteConfigManager.getAdConfig(
            RemoteConfigManager.APP_OPEN_SPLASH,
            RemoteConfigManager.TEST_APP_OPEN_ID
        )
        val isPremium = PrefUtil(context).getPremium()

        if (!isInternetAvailable) {
            // Internet NOT available -> move to next screen after 3 sec
            context.logE("Splash: Internet NOT available. Moving to next screen in 3s.")
            delay(3000L)
            navigateToNextScreen()
        } else {
            // Internet IS available
            if (!appOpenConfig.enabled || isPremium) {
                context.logE("Splash: AppOpen Ad disabled or Premium user. Moving to next screen in 3s.")
                delay(3000L)
                navigateToNextScreen()
            } else {
                var isNavigated = false

                fun safeNavigateNext() {
                    if (!isNavigated) {
                        isNavigated = true
                        navigateToNextScreen()
                    }
                }

                // 21-second timeout timer limit
                val timeoutJob = launch {
                    delay(21000L)
                    context.logE("Splash: AppOpen Ad 21-second timeout reached.")
                    safeNavigateNext()
                }

                // Load AppOpen Ad
                val request = AdRequest.Builder().build()
                AppOpenAd.load(
                    context,
                    appOpenConfig.adId,
                    request,
                    object : AppOpenAd.AppOpenAdLoadCallback() {
                        override fun onAdLoaded(ad: AppOpenAd) {
                            context.logE("Splash: AppOpen Ad Loaded successfully.")
                            timeoutJob.cancel() // Cancel 21s timer

                            if (isNavigated) return

                            com.example.paintcalculatorestimator.utils.AdLoadingDialog.showLoadingDialog(context)

                            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                                override fun onAdDismissedFullScreenContent() {
                                    context.logE("Splash: AppOpen Ad Dismissed by user.")
                                    com.example.paintcalculatorestimator.utils.AdLoadingDialog.hideLoadingDialog()
                                    (context.applicationContext as? PaintCalculatorApp)?.isAppOpenShowing = false
                                    safeNavigateNext()
                                }

                                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                                    context.logE("Splash: AppOpen Ad Failed to show: ${adError.message}")
                                    com.example.paintcalculatorestimator.utils.AdLoadingDialog.hideLoadingDialog()
                                    (context.applicationContext as? PaintCalculatorApp)?.isAppOpenShowing = false
                                    safeNavigateNext()
                                }

                                override fun onAdShowedFullScreenContent() {
                                    context.logE("Splash: AppOpen Ad Showed.")
                                    (context.applicationContext as? PaintCalculatorApp)?.isAppOpenShowing = true
                                }
                            }
                            // Show ad and STOP moving to next screen until user dismisses ad
                            ad.show(context)
                        }

                        override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                            context.logE("Splash: AppOpen Ad Failed to load: ${loadAdError.message}")
                            timeoutJob.cancel()
                            com.example.paintcalculatorestimator.utils.AdLoadingDialog.hideLoadingDialog()
                            safeNavigateNext()
                        }
                    }
                )
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(backdrop, MaterialTheme.colorScheme.primaryContainer)))
            .safeDrawingPadding()
    ) {
        // Banner Ad on Top
        BannerAdView(
            adConfigKey = RemoteConfigManager.BANNER_SPLASH,
            defaultAdId = RemoteConfigManager.TEST_BANNER_ID,
            modifier = Modifier
                .align(Alignment.TopCenter)
        )

        // Main Splash Content in Center
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .scale(scale.value)
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        shape = MaterialTheme.shapes.extraLarge
                    ),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.foundation.Image(
                    painter = painterResource(R.drawable.ic_splash),
                    contentDescription = "App logo",
                    modifier = Modifier.size(80.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(id = R.string.app_name),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.alpha(alpha.value),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(id = R.string.home_subtitle),
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.alpha(alpha.value),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(48.dp))

            LinearProgressIndicator(modifier = Modifier.width(140.dp).height(4.dp))
        }
    }
}

/** Static preview variant: avoids navigation, DataStore and animation side effects. */
@androidx.compose.ui.tooling.preview.Preview(showBackground = true, name = "Splash screen")
@Composable
private fun SplashScreenPreview() {
    PaintCalculatorEstimatorTheme(darkTheme = false) {
        val backdrop = if (MaterialTheme.colorScheme.background.luminance() > 0.5f) {
            Lavender
        } else {
            MaterialTheme.colorScheme.primaryContainer
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(backdrop, MaterialTheme.colorScheme.primaryContainer)))
                .safeDrawingPadding()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), MaterialTheme.shapes.extraLarge),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.foundation.Image(
                        painter = painterResource(R.drawable.ic_splash),
                        contentDescription = "App logo",
                        modifier = Modifier.size(80.dp)
                    )
                }
                Spacer(Modifier.height(24.dp))
                Text(stringResource(R.string.app_name), fontSize = 28.sp, fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary, textAlign = TextAlign.Center)
                Spacer(Modifier.height(8.dp))
                Text(stringResource(R.string.home_subtitle), fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.secondary, textAlign = TextAlign.Center)
                Spacer(Modifier.height(64.dp))
                LinearProgressIndicator(Modifier.width(140.dp).height(4.dp))
            }
        }
    }
}
