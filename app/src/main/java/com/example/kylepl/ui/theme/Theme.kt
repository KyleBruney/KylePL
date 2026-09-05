package com.example.kylepl.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val DarkColors = darkColorScheme(
    primary = IronRed,
    onPrimary = ChalkWhite,
    primaryContainer = IronRedDark,
    onPrimaryContainer = ChalkWhite,
    secondary = MutedGrey,
    background = Charcoal,
    onBackground = ChalkWhite,
    surface = SteelGrey,
    onSurface = ChalkWhite,
    surfaceVariant = SteelGreyLight,
    onSurfaceVariant = ChalkWhite,
    error = IronRedLight,
)

private val LightColors = lightColorScheme(
    primary = IronRed,
    onPrimary = ChalkWhite,
    primaryContainer = IronRedLight,
    onPrimaryContainer = Charcoal,
    secondary = SteelGrey,
    background = ChalkWhite,
    onBackground = Charcoal,
    surface = Color(0xFFFFFFFF),
    onSurface = Charcoal,
)

private val PowerLiftShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp),
)

@Composable
fun PowerLiftTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colorScheme,
        typography = PowerLiftTypography,
        shapes = PowerLiftShapes,
        content = content,
    )
}
