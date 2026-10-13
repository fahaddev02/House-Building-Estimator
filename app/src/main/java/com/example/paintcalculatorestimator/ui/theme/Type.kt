package com.example.paintcalculatorestimator.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private fun style(size: Int, height: Int, weight: FontWeight = FontWeight.Normal) = TextStyle(
    fontFamily = FontFamily.SansSerif, fontSize = size.sp, lineHeight = height.sp,
    fontWeight = weight, letterSpacing = 0.sp
)

val Typography = Typography(
    headlineLarge = style(30, 36, FontWeight.Bold),
    headlineMedium = style(26, 32, FontWeight.Bold),
    headlineSmall = style(22, 28, FontWeight.Bold),
    titleLarge = style(20, 26, FontWeight.Bold),
    titleMedium = style(16, 22, FontWeight.SemiBold),
    titleSmall = style(14, 20, FontWeight.SemiBold),
    bodyLarge = style(15, 22), bodyMedium = style(13, 20), bodySmall = style(12, 18),
    labelLarge = style(14, 20, FontWeight.SemiBold),
    labelMedium = style(12, 16, FontWeight.Medium),
    labelSmall = style(10, 14, FontWeight.Medium)
)
