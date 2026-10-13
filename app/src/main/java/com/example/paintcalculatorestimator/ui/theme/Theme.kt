package com.example.paintcalculatorestimator.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = BrandPurple, onPrimary = Color.White,
    primaryContainer = Color(0xFFF0EBFF), onPrimaryContainer = Color(0xFF392775),
    secondary = Color(0xFF696275), onSecondary = Color.White,
    secondaryContainer = Color(0xFFEDE8F4), onSecondaryContainer = Color(0xFF393342),
    tertiary = Color(0xFF187A69), onTertiary = Color.White,
    tertiaryContainer = Color(0xFFE2F5EE), onTertiaryContainer = Color(0xFF145C4E),
    background = BackgroundLight, onBackground = Color(0xFF23212A),
    surface = SurfaceLight, onSurface = Color(0xFF23212A),
    surfaceVariant = Color(0xFFF3F2F5), onSurfaceVariant = Color(0xFF6D6974),
    outline = Color(0xFF9A94A5), outlineVariant = Color(0xFFE2DFE8),
    surfaceTint = Color.Transparent
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFC4B5FF), onPrimary = Color(0xFF30205E),
    primaryContainer = Color(0xFF3B2E62), onPrimaryContainer = Color(0xFFE9DFFF),
    secondary = Color(0xFFC2BACD), onSecondary = Color(0xFF302936),
    secondaryContainer = Color(0xFF393342), onSecondaryContainer = Color(0xFFEDE8F4),
    tertiary = Color(0xFF8AD8BF), onTertiary = Color(0xFF09382D),
    tertiaryContainer = Color(0xFF194F42), onTertiaryContainer = Color(0xFFB7F1DD),
    background = BackgroundDark, onBackground = Color(0xFFF0ECF5),
    surface = SurfaceDark, onSurface = Color(0xFFF0ECF5),
    surfaceVariant = Color(0xFF2C2836), onSurfaceVariant = Color(0xFFC2BACD),
    outline = Color(0xFF91889F), outlineVariant = Color(0xFF453E51),
    surfaceTint = Color.Transparent
)

@Composable
fun PaintCalculatorEstimatorTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            (view.context as? Activity)?.window?.let { window ->
                WindowCompat.getInsetsController(window, view).apply {
                    isAppearanceLightStatusBars = !darkTheme
                    isAppearanceLightNavigationBars = !darkTheme
                }
            }
        }
    }
    MaterialTheme(
        colorScheme = colors,
        typography = Typography,
        shapes = Shapes(
            extraSmall = RoundedCornerShape(8.dp), small = RoundedCornerShape(12.dp),
            medium = RoundedCornerShape(16.dp), large = RoundedCornerShape(24.dp),
            extraLarge = RoundedCornerShape(28.dp)
        ),
        content = content
    )
}
