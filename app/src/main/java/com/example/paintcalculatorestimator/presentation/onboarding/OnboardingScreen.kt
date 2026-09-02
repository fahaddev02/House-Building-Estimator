package com.example.paintcalculatorestimator.presentation.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.paintcalculatorestimator.R
import com.example.paintcalculatorestimator.data.preferences.AppPreferences
import com.example.paintcalculatorestimator.presentation.navigation.Screen
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(
    navController: androidx.navigation.NavController,
    appPreferences: AppPreferences
) {
    val coroutineScope = rememberCoroutineScope()
    var currentSlide by rememberSaveable { mutableIntStateOf(0) }

    val slides = listOf(
        OnboardingSlide(
            titleRes = R.string.onboarding_title_1,
            descRes = R.string.onboarding_desc_1,
            icon = Icons.Default.Create
        ),
        OnboardingSlide(
            titleRes = R.string.onboarding_title_2,
            descRes = R.string.onboarding_desc_2,
            icon = Icons.Default.Info
        ),
        OnboardingSlide(
            titleRes = R.string.onboarding_title_3,
            descRes = R.string.onboarding_desc_3,
            icon = Icons.Default.Star
        )
    )

    fun finishOnboarding() {
        coroutineScope.launch {
            appPreferences.setOnboardingCompleted(true)
            navController.navigate(Screen.Home.route) {
                popUpTo(Screen.Onboarding.route) { inclusive = true }
            }
        }
    }

    OnboardingScreenContent(
        currentSlide = currentSlide,
        slides = slides,
        onNextClick = { currentSlide += 1 },
        onSkipClick = { finishOnboarding() },
        onGetStartedClick = { finishOnboarding() }
    )
}

@Composable
fun OnboardingScreenContent(
    currentSlide: Int,
    slides: List<OnboardingSlide>,
    onNextClick: () -> Unit,
    onSkipClick: () -> Unit,
    onGetStartedClick: () -> Unit
) {
    Scaffold(
        topBar = {
//            Row(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .statusBarsPadding()
//                    .padding(16.dp),
//                horizontalArrangement = Arrangement.End
//            ) {
//                if (currentSlide < 2) {
//                    TextButton(onClick = onSkipClick) {
//                        Text(
//                            text = stringResource(id = R.string.onboarding_skip),
//                            color = MaterialTheme.colorScheme.secondary,
//                            fontWeight = FontWeight.SemiBold
//                        )
//                    }
//                }
//            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            
            // Slide Content
            AnimatedContent(
                targetState = currentSlide,
                label = "slideTransition",
                modifier = Modifier.weight(1f)
            ) { slideIdx ->
                val slide = slides[slideIdx]
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Illustration Placeholder
                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = slide.icon,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(48.dp))
                    
                    Text(
                        text = stringResource(id = slide.titleRes),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = stringResource(id = slide.descRes),
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.secondary,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    )
                }
            }

            // Bottom UI
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Indicators
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 24.dp)
                ) {
                    for (i in slides.indices) {
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .size(width = if (i == currentSlide) 20.dp else 8.dp, height = 8.dp)
                                .clip(CircleShape)
                                .background(
                                    if (i == currentSlide) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)
                                )
                        )
                    }
                }

                // Action Buttons
                if (currentSlide < 2) {
                    Button(
                        onClick = onNextClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text(
                            text = stringResource(id = R.string.onboarding_next),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Button(
                        onClick = onGetStartedClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text(
                            text = stringResource(id = R.string.onboarding_get_started),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

data class OnboardingSlide(
    val titleRes: Int,
    val descRes: Int,
    val icon: ImageVector
)

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun OnboardingScreenPreview() {
    val slides = listOf(
        OnboardingSlide(
            titleRes = R.string.onboarding_title_1,
            descRes = R.string.onboarding_desc_1,
            icon = Icons.Default.Create
        ),
        OnboardingSlide(
            titleRes = R.string.onboarding_title_2,
            descRes = R.string.onboarding_desc_2,
            icon = Icons.Default.Info
        ),
        OnboardingSlide(
            titleRes = R.string.onboarding_title_3,
            descRes = R.string.onboarding_desc_3,
            icon = Icons.Default.Star
        )
    )
    MaterialTheme {
        OnboardingScreenContent(
            currentSlide = 0,
            slides = slides,
            onNextClick = {},
            onSkipClick = {},
            onGetStartedClick = {}
        )
    }
}
