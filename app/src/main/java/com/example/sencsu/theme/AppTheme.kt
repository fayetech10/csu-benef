package com.example.sencsu.theme


import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.sencsu.ui.theme.Typography

// ==============================================================================
// DÉFINITION DES COULEURS
// ==============================================================================

object AppColors {
    // ── Palette Médicale & Sobres ──
    val BrandBlue = Color(0xFF1A7F5A)          // Vert Santé (Primaire)
    val BrandBlueDark = Color(0xFF115C41)      // Vert Profond
    val BrandBlueLite = Color(0xFFE8F3EF)      // Fond vert très clair
    
    val AppBackground = Color(0xFFF8FAFA)      // Blanc Cassé Médical
    val SurfaceBackground = Color(0xFFFFFFFF)  // Blanc Pur
    val SurfaceAlt = Color(0xFFEEF2F2)         // Gris-Bleu très clair

    // ── Couleurs Institutionnelles du Sénégal ──
    val GoldAccent = Color(0xFFD4A843)         // Or Sénégalais (accents premium)
    val GoldLight = Color(0xFFFDF6E3)          // Fond doré très clair
    val SenegalRed = Color(0xFFE3242B)         // Rouge du drapeau
    val SenegalGreen = Color(0xFF00853F)       // Vert du drapeau

    // Couleurs d'actions et statuts
    val ActionBlue = Color(0xFF3B9ECC)         // Bleu Actions Secondaires
    val StatusGreen = Color(0xFF10B981)        // Vert Succès
    val StatusOrange = Color(0xFFF59E0B)       // Orange Alerte
    val StatusRed = Color(0xFFEF4444)          // Rouge Erreur/Expiré
    val StatusGrey = Color(0xFF94A3B8)         // Gris Statut neutre

    // Texte
    val TextMain = Color(0xFF0F172A)           // Sombre (Slate 900)
    val TextSub = Color(0xFF475569)            // Gris (Slate 600)
    val TextDisabled = Color(0xFF94A3B8)       // Gris clair
    val TextOnDark = Color(0xFFFFFFFF)         // Blanc sur fond sombre
    
    // Bordures
    val BorderColor = Color(0xFFE2E8F0)        // Gris léger
    val BorderColorLight = Color(0xFFF1F5F9)   // Gris très léger
    
    // Carte de membre
    val CardGradientStart = Color(0xFF1A7F5A)  // Vert santé
    val CardGradientEnd = Color(0xFF0D4A33)    // Vert très profond
    
    // Legacy compatible colors
    val LogoYellow = Color(0xFFFBBF24)

    // ── Dark Mode ──
    val DarkBackground = Color(0xFF0F1419)         // Fond principal sombre
    val DarkSurface = Color(0xFF1A1F2E)            // Surface sombre
    val DarkSurfaceAlt = Color(0xFF232A3B)         // Surface alternative sombre
    val DarkBrandGreen = Color(0xFF22D393)         // Vert lumineux pour dark mode
    val DarkBrandGreenDark = Color(0xFF1A7F5A)     // Vert profond en dark
    val DarkTextMain = Color(0xFFE8ECF1)           // Texte clair
    val DarkTextSub = Color(0xFF94A3B8)            // Texte secondaire
    val DarkBorder = Color(0xFF2A3441)             // Bordure sombre
    val DarkBorderLight = Color(0xFF1E2733)        // Bordure très sombre
    val DarkCardStart = Color(0xFF22D393)          // Gradient carte dark
    val DarkCardEnd = Color(0xFF115C41)             // Gradient carte dark fin
}

object AppShapes {
    val LargeRadius: RoundedCornerShape = RoundedCornerShape(16.dp)
    val MediumRadius: RoundedCornerShape = RoundedCornerShape(12.dp)
    val SmallRadius: RoundedCornerShape = RoundedCornerShape(8.dp)
    val ExtraSmallRadius: RoundedCornerShape = RoundedCornerShape(4.dp)
    val CircleRadius: RoundedCornerShape = RoundedCornerShape(50)
}

// ==============================================================================
// DIMENSIONS ET ESPACEMENTS
// ==============================================================================

object AppDimensions {
    // Espacements
    val paddingXSmall: Dp = 4.dp
    val paddingSmall: Dp = 8.dp
    val paddingMedium: Dp = 16.dp
    val paddingLarge: Dp = 20.dp
    val paddingXLarge: Dp = 24.dp
    val paddingXXLarge: Dp = 32.dp

    // Hauteurs
    val buttonHeight: Dp = 56.dp
    val buttonSmallHeight: Dp = 40.dp
    val appBarHeight: Dp = 64.dp

    // Largeurs/Tailles
    val cardElevation: Dp = 2.dp
    val borderWidth: Dp = 1.dp
    val dividerThickness: Dp = 0.5.dp

    // Icônes
    val iconSmall: Dp = 16.dp
    val iconMedium: Dp = 24.dp
    val iconLarge: Dp = 32.dp
    val iconXLarge: Dp = 48.dp

    // Images
    val profileImageSize: Dp = 110.dp
    val avatarSize: Dp = 50.dp
    val thumbnailSize: Dp = 100.dp
}

// ==============================================================================
// ÉLÉVATION ET OMBRES
// ==============================================================================

object AppElevation {
    val card: Dp = 0.dp
    val button: Dp = 4.dp
    val fab: Dp = 6.dp
    val dialog: Dp = 24.dp
}

// ==============================================================================
// DURATIONS (Animations)
// ==============================================================================

object AppDurations {
    const val SHORT: Long = 150L
    const val MEDIUM: Long = 300L
    const val LONG: Long = 500L
}

// ==============================================================================
// SCHÉMA DE COULEUR MATERIAL 3
// ==============================================================================

private val LightColorScheme = lightColorScheme(
    primary = AppColors.BrandBlue,
    onPrimary = Color.White,
    secondary = AppColors.BrandBlueDark,
    onSecondary = Color.White,
    tertiary = AppColors.StatusOrange,
    onTertiary = Color.White,
    error = AppColors.StatusRed,
    onError = Color.White,
    background = AppColors.AppBackground,
    onBackground = AppColors.TextMain,
    surface = AppColors.SurfaceBackground,
    onSurface = AppColors.TextMain,
    surfaceVariant = AppColors.SurfaceAlt,
    onSurfaceVariant = AppColors.TextSub,
    outline = AppColors.BorderColor,
    outlineVariant = AppColors.BorderColorLight,
)

private val DarkColorScheme = darkColorScheme(
    primary = AppColors.DarkBrandGreen,
    onPrimary = AppColors.DarkBackground,
    secondary = AppColors.DarkBrandGreenDark,
    onSecondary = Color.White,
    tertiary = AppColors.StatusOrange,
    onTertiary = AppColors.DarkBackground,
    error = AppColors.StatusRed,
    onError = AppColors.DarkBackground,
    background = AppColors.DarkBackground,
    onBackground = AppColors.DarkTextMain,
    surface = AppColors.DarkSurface,
    onSurface = AppColors.DarkTextMain,
    surfaceVariant = AppColors.DarkSurfaceAlt,
    onSurfaceVariant = AppColors.DarkTextSub,
    outline = AppColors.DarkBorder,
    outlineVariant = AppColors.DarkBorderLight,
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
        content = content
    )
}

// ==============================================================================
// UTILITAIRES DE COULEUR
// ==============================================================================

/**
 * Extension pour faciliter l'utilisation des couleurs avec transparence
 */
fun Color.withAlpha(alpha: Float): Color = this.copy(alpha = alpha)

/**
 * Extension pour obtenir la couleur complémentaire
 */
fun Color.getComplementary(): Color {
    val r = 255 - (this.red * 255).toInt()
    val g = 255 - (this.green * 255).toInt()
    val b = 255 - (this.blue * 255).toInt()
    return Color(r, g, b)
}

/**
 * Extension pour assombrir une couleur
 */
fun Color.darken(factor: Float = 0.2f): Color {
    return Color(
        red = (this.red * (1 - factor)).coerceIn(0f, 1f),
        green = (this.green * (1 - factor)).coerceIn(0f, 1f),
        blue = (this.blue * (1 - factor)).coerceIn(0f, 1f),
        alpha = this.alpha
    )
}

/**
 * Extension pour éclaircir une couleur
 */
fun Color.lighten(factor: Float = 0.2f): Color {
    return Color(
        red = (this.red + (1 - this.red) * factor).coerceIn(0f, 1f),
        green = (this.green + (1 - this.green) * factor).coerceIn(0f, 1f),
        blue = (this.blue + (1 - this.blue) * factor).coerceIn(0f, 1f),
        alpha = this.alpha
    )
}