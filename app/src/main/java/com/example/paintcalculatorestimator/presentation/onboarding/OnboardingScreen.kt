package com.example.paintcalculatorestimator.presentation.onboarding

import android.view.Gravity
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.paintcalculatorestimator.R
import com.example.paintcalculatorestimator.data.preferences.AppPreferences
import com.example.paintcalculatorestimator.presentation.navigation.Screen
import com.example.paintcalculatorestimator.ui.theme.*
import kotlinx.coroutines.launch

private val slides = listOf(
    OnboardingSlide(R.string.onboarding_title_1, R.string.onboarding_desc_1, R.drawable.img_first_onboarding),
    OnboardingSlide(R.string.onboarding_title_2, R.string.onboarding_desc_2, R.drawable.img_second_onboarding),
    OnboardingSlide(R.string.onboarding_title_3, R.string.onboarding_desc_3, R.drawable.img_3rd_onboarding)
)

@Composable
fun OnboardingScreen(navController: NavController, appPreferences: AppPreferences) {
    val scope = rememberCoroutineScope()
    var currentSlide by rememberSaveable { mutableIntStateOf(0) }
    fun finish() {
        scope.launch {
            appPreferences.setOnboardingCompleted(true)
            navController.navigate(Screen.Home.route) { popUpTo(Screen.Onboarding.route) { inclusive = true } }
        }
    }
    OnboardingScreenContent(currentSlide, slides, { currentSlide = (currentSlide + 1).coerceAtMost(slides.lastIndex) }, { finish() }, { finish() })
}

@Composable
fun OnboardingScreenContent(
    currentSlide: Int, slides: List<OnboardingSlide>,
    onNextClick: () -> Unit, onSkipClick: () -> Unit, onGetStartedClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val backdrop = if (colors.background.luminance() > 0.5f) Lavender else colors.primaryContainer
    Column(Modifier.fillMaxSize()
        .background(Brush.verticalGradient(listOf(backdrop, backdrop, colors.primaryContainer)))
        .safeDrawingPadding().padding(horizontal = 28.dp, vertical = 12.dp)) {

        AnimatedContent(currentSlide, modifier = Modifier.weight(1f), label = "onboarding") { index ->
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                androidx.compose.foundation.Image(
                    painter = painterResource(slides[index].imageRes),
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth().widthIn(max = 340.dp).height(260.dp),
                    contentScale = androidx.compose.ui.layout.ContentScale.Fit
                )
                Spacer(Modifier.height(28.dp))
                Text(stringResource(slides[index].titleRes), style = MaterialTheme.typography.headlineLarge,
                    textAlign = TextAlign.Center, color = colors.onBackground)
                Spacer(Modifier.height(12.dp))
                Text(stringResource(slides[index].descRes), style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center, color = colors.onSurfaceVariant)
                Spacer(Modifier.height(16.dp))
            }
        }
        Row(Modifier.fillMaxWidth().padding(vertical = 24.dp), horizontalArrangement = Arrangement.Center) {
            slides.indices.forEach { index ->
                Box(Modifier.padding(horizontal = 4.dp).size(if (index == currentSlide) 26.dp else 8.dp, 6.dp)
                    .clip(CircleShape).background(if (index == currentSlide) colors.primary else colors.primary.copy(alpha = 0.2f)))
            }
        }
        Button(onClick = if (currentSlide == slides.lastIndex) onGetStartedClick else onNextClick,
            modifier = Modifier.fillMaxWidth().heightIn(min = 54.dp), shape = CircleShape) {
            Text(stringResource(if (currentSlide == slides.lastIndex) R.string.onboarding_get_started else R.string.onboarding_next) + "  →",
                fontSize = 20.sp,
                textAlign = TextAlign.Center)
        }
        Spacer(Modifier.height(16.dp))
        com.example.paintcalculatorestimator.adsManager.NativeAdView()
    }
}

data class OnboardingSlide(val titleRes: Int, val descRes: Int, val imageRes: Int)

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun OnboardingScreenPreview() {
    PaintCalculatorEstimatorTheme { OnboardingScreenContent(0, slides, {}, {}, {}) }
}
     
