package com.example.sencsu.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.sencsu.ui.theme.Typography

object AppColors {
    // Palette institutionnelle, medicale et sobre.
    val BrandBlue = Color(0xFF08745F)
    val BrandBlueDark = Color(0xFF064E40)
    val BrandBlueLite = Color(0xFFE7F5F1)
    val BrandBlueSoft = Color(0xFFBFE7DD)

    val AppBackground = Color(0xFFF6F8F7)
    val SurfaceBackground = Color(0xFFFFFFFF)
    val SurfaceAlt = Color(0xFFEFF4F2)
    val SurfaceMuted = Color(0xFFF8FBFA)
    val SurfacePressed = Color(0xFFE4ECE9)

    val GoldAccent = Color(0xFFD1A339)
    val GoldLight = Color(0xFFFFF6DD)
    val SenegalRed = Color(0xFFE0363E)
    val SenegalGreen = Color(0xFF00853F)

    val ActionBlue = Color(0xFF2563EB)
    val ActionBlueSoft = Color(0xFFEFF6FF)
    val StatusGreen = Color(0xFF0F9F6E)
    val StatusGreenSoft = Color(0xFFE7F8F0)
    val StatusOrange = Color(0xFFD97706)
    val StatusOrangeSoft = Color(0xFFFFF4DE)
    val StatusRed = Color(0xFFDC2626)
    val StatusRedSoft = Color(0xFFFDECEC)
    val StatusGrey = Color(0xFF8A98A8)

    val TextMain = Color(0xFF111827)
    val TextSub = Color(0xFF52606D)
    val TextDisabled = Color(0xFF9AA6B2)
    val TextOnDark = Color(0xFFFFFFFF)

    val BorderColor = Color(0xFFDCE5E1)
    val BorderColorLight = Color(0xFFEEF3F1)

    val CardGradientStart = Color(0xFF08745F)
    val CardGradientEnd = Color(0xFF05392F)

    val LogoYellow = Color(0xFFFBBF24)

    val DarkBackground = Color(0xFF0C1210)
    val DarkSurface = Color(0xFF151D1A)
    val DarkSurfaceAlt = Color(0xFF1F2A26)
    val DarkBrandGreen = Color(0xFF38D6AA)
    val DarkBrandGreenDark = Color(0xFF0C8C72)
    val DarkTextMain = Color(0xFFE9F0ED)
    val DarkTextSub = Color(0xFF9AA8A3)
    val DarkBorder = Color(0xFF2B3934)
    val DarkBorderLight = Color(0xFF22302B)
    val DarkCardStart = Color(0xFF38D6AA)
    val DarkCardEnd = Color(0xFF07614F)
}

object AppShapes {
    val LargeRadius: RoundedCornerShape = RoundedCornerShape(8.dp)
    val MediumRadius: RoundedCornerShape = RoundedCornerShape(8.dp)
    val SmallRadius: RoundedCornerShape = RoundedCornerShape(8.dp)
    val ExtraSmallRadius: RoundedCornerShape = RoundedCornerShape(4.dp)
    val CircleRadius: RoundedCornerShape = RoundedCornerShape(50)
}

object AppDimensions {
    val paddingXSmall: Dp = 4.dp
    val paddingSmall: Dp = 8.dp
    val paddingMedium: Dp = 16.dp
    val paddingLarge: Dp = 20.dp
    val paddingXLarge: Dp = 24.dp
    val paddingXXLarge: Dp = 32.dp

    val buttonHeight: Dp = 56.dp
    val buttonSmallHeight: Dp = 40.dp
    val appBarHeight: Dp = 64.dp

    val cardElevation: Dp = 2.dp
    val borderWidth: Dp = 1.dp
    val dividerThickness: Dp = 0.5.dp

    val iconSmall: Dp = 16.dp
    val iconMedium: Dp = 24.dp
    val iconLarge: Dp = 32.dp
    val iconXLarge: Dp = 48.dp

    val profileImageSize: Dp = 110.dp
    val avatarSize: Dp = 50.dp
    val thumbnailSize: Dp = 100.dp
}

object AppElevation {
    val card: Dp = 0.dp
    val cardRaised: Dp = 3.dp
    val button: Dp = 2.dp
    val fab: Dp = 6.dp
    val dialog: Dp = 24.dp
}

object AppDurations {
    const val SHORT: Long = 150L
    const val MEDIUM: Long = 300L
    const val LONG: Long = 500L
}

object AppGradients {
    val Brand = listOf(AppColors.BrandBlue, AppColors.BrandBlueDark)
    val BrandSoft = listOf(AppColors.SurfaceMuted, AppColors.BrandBlueLite)
    val Card = listOf(AppColors.CardGradientStart, AppColors.CardGradientEnd)
    val Senegal = listOf(AppColors.SenegalGreen, AppColors.GoldAccent, AppColors.SenegalRed)
}

private val LightColorScheme = lightColorScheme(
    primary = AppColors.BrandBlue,
    onPrimary = Color.White,
    primaryContainer = AppColors.BrandBlueLite,
    onPrimaryContainer = AppColors.BrandBlueDark,
    secondary = AppColors.BrandBlueDark,
    onSecondary = Color.White,
    secondaryContainer = AppColors.SurfaceAlt,
    onSecondaryContainer = AppColors.TextMain,
    tertiary = AppColors.GoldAccent,
    onTertiary = AppColors.TextMain,
    tertiaryContainer = AppColors.GoldLight,
    onTertiaryContainer = AppColors.TextMain,
    error = AppColors.StatusRed,
    onError = Color.White,
    errorContainer = AppColors.StatusRedSoft,
    onErrorContainer = AppColors.StatusRed,
    background = AppColors.AppBackground,
    onBackground = AppColors.TextMain,
    surface = AppColors.SurfaceBackground,
    onSurface = AppColors.TextMain,
    surfaceVariant = AppColors.SurfaceAlt,
    onSurfaceVariant = AppColors.TextSub,
    inverseSurface = AppColors.TextMain,
    inverseOnSurface = Color.White,
    surfaceTint = AppColors.BrandBlue,
    outline = AppColors.BorderColor,
    outlineVariant = AppColors.BorderColorLight,
)

private val DarkColorScheme = darkColorScheme(
    primary = AppColors.DarkBrandGreen,
    onPrimary = AppColors.DarkBackground,
    primaryContainer = AppColors.DarkSurfaceAlt,
    onPrimaryContainer = AppColors.DarkBrandGreen,
    secondary = AppColors.DarkBrandGreenDark,
    onSecondary = Color.White,
    secondaryContainer = AppColors.DarkSurfaceAlt,
    onSecondaryContainer = AppColors.DarkTextMain,
    tertiary = AppColors.GoldAccent,
    onTertiary = AppColors.DarkBackground,
    tertiaryContainer = Color(0xFF3A2B0F),
    onTertiaryContainer = AppColors.GoldLight,
    error = AppColors.StatusRed,
    onError = AppColors.DarkBackground,
    errorContainer = Color(0xFF3F1616),
    onErrorContainer = Color(0xFFFFDADA),
    background = AppColors.DarkBackground,
    onBackground = AppColors.DarkTextMain,
    surface = AppColors.DarkSurface,
    onSurface = AppColors.DarkTextMain,
    surfaceVariant = AppColors.DarkSurfaceAlt,
    onSurfaceVariant = AppColors.DarkTextSub,
    outline = AppColors.DarkBorder,
    outlineVariant = AppColors.DarkBorderLight,
)

private val AppMaterialShapes = Shapes(
    extraSmall = AppShapes.ExtraSmallRadius,
    small = AppShapes.SmallRadius,
    medium = AppShapes.MediumRadius,
    large = AppShapes.LargeRadius,
    extraLarge = RoundedCornerShape(24.dp)
)

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = AppMaterialShapes,
        content = content
    )
}

fun Color.withAlpha(alpha: Float): Color = copy(alpha = alpha)

fun Color.getComplementary(): Color {
    val r = 255 - (red * 255).toInt()
    val g = 255 - (green * 255).toInt()
    val b = 255 - (blue * 255).toInt()
    return Color(r, g, b)
}

fun Color.darken(factor: Float = 0.2f): Color {
    return Color(
        red = (red * (1 - factor)).coerceIn(0f, 1f),
        green = (green * (1 - factor)).coerceIn(0f, 1f),
        blue = (blue * (1 - factor)).coerceIn(0f, 1f),
        alpha = alpha
    )
}

fun Color.lighten(factor: Float = 0.2f): Color {
    return Color(
        red = (red + (1 - red) * factor).coerceIn(0f, 1f),
        green = (green + (1 - green) * factor).coerceIn(0f, 1f),
        blue = (blue + (1 - blue) * factor).coerceIn(0f, 1f),
        alpha = alpha
    )
}
