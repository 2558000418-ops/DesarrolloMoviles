package com.example.todolist.ui.theme

import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import com.example.todolist.model.BorderStyleMode
import com.example.todolist.model.ColorblindMode
import com.example.todolist.model.CornerStyleMode
import com.example.todolist.model.LayoutDensityMode
import com.example.todolist.model.TaskPriority

data class AppThemePalette(
    val id: Int,
    val name: String,
    val isDark: Boolean,
    val bgColor: Color,
    val cardColor: Color,
    val primaryAccent: Color,
    val secondaryAccent: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val surfaceBorder: Color,
    val borderStyle: BorderStyleMode = BorderStyleMode.SUBTLE,
    val cornerRadiusDp: Int = 18,
    val spacingDp: Int = 14
)

/**
 * 10 Paletas de Color optimizadas.
 */
val AvailableThemes = listOf(
    // --- 5 COMBINACIONES CON BLANCO PURO (MODO CLARO: IDs 0..4) ---
    AppThemePalette(
        id = 0,
        name = "Blanco + Morado Oscuro",
        isDark = false,
        bgColor = Color(0xFFFFFFFF),
        cardColor = Color(0xFFFFFFFF),
        primaryAccent = Color(0xFF4C1D95),
        secondaryAccent = Color(0xFFEDE9FE),
        textPrimary = Color(0xFF0F0A1C),
        textSecondary = Color(0xFF334155),
        surfaceBorder = Color(0xFF2E1065)
    ),
    AppThemePalette(
        id = 1,
        name = "Blanco + Azul Marino",
        isDark = false,
        bgColor = Color(0xFFFFFFFF),
        cardColor = Color(0xFFFFFFFF),
        primaryAccent = Color(0xFF1E3A8A),
        secondaryAccent = Color(0xFFDBEAFE),
        textPrimary = Color(0xFF090D16),
        textSecondary = Color(0xFF334155),
        surfaceBorder = Color(0xFF172554)
    ),
    AppThemePalette(
        id = 2,
        name = "Blanco + Esmeralda Oscuro",
        isDark = false,
        bgColor = Color(0xFFFFFFFF),
        cardColor = Color(0xFFFFFFFF),
        primaryAccent = Color(0xFF065F46),
        secondaryAccent = Color(0xFFD1FAE5),
        textPrimary = Color(0xFF041E17),
        textSecondary = Color(0xFF334155),
        surfaceBorder = Color(0xFF064E3B)
    ),
    AppThemePalette(
        id = 3,
        name = "Blanco + Terracota Oscuro",
        isDark = false,
        bgColor = Color(0xFFFFFFFF),
        cardColor = Color(0xFFFFFFFF),
        primaryAccent = Color(0xFF9A3412),
        secondaryAccent = Color(0xFFFFEDD5),
        textPrimary = Color(0xFF1F0D05),
        textSecondary = Color(0xFF334155),
        surfaceBorder = Color(0xFF431407)
    ),
    AppThemePalette(
        id = 4,
        name = "Blanco + Carmín Oscuro",
        isDark = false,
        bgColor = Color(0xFFFFFFFF),
        cardColor = Color(0xFFFFFFFF),
        primaryAccent = Color(0xFF9F1239),
        secondaryAccent = Color(0xFFFFE4E6),
        textPrimary = Color(0xFF1F040B),
        textSecondary = Color(0xFF334155),
        surfaceBorder = Color(0xFF4C0519)
    ),

    // --- 5 COMBINACIONES CON NEGRO PROFUNDO (MODO OSCURO / AMOLED: IDs 5..9) ---
    AppThemePalette(
        id = 5,
        name = "Negro + Carmín Rojo",
        isDark = true,
        bgColor = Color(0xFF050507),
        cardColor = Color(0xFF0F0D12),
        primaryAccent = Color(0xFFBE123C),
        secondaryAccent = Color(0xFF3B0718),
        textPrimary = Color(0xFFFFFFFF),
        textSecondary = Color(0xFFCBD5E1),
        surfaceBorder = Color(0xFF4C0519)
    ),
    AppThemePalette(
        id = 6,
        name = "Negro + Morado Profundo",
        isDark = true,
        bgColor = Color(0xFF050508),
        cardColor = Color(0xFF0E0D16),
        primaryAccent = Color(0xFF7E22CE),
        secondaryAccent = Color(0xFF270E4F),
        textPrimary = Color(0xFFFFFFFF),
        textSecondary = Color(0xFFCBD5E1),
        surfaceBorder = Color(0xFF3B1A70)
    ),
    AppThemePalette(
        id = 7,
        name = "Negro + Azul Noche",
        isDark = true,
        bgColor = Color(0xFF04070D),
        cardColor = Color(0xFF0B101B),
        primaryAccent = Color(0xFF1D4ED8),
        secondaryAccent = Color(0xFF15275E),
        textPrimary = Color(0xFFFFFFFF),
        textSecondary = Color(0xFFCBD5E1),
        surfaceBorder = Color(0xFF1E3A8A)
    ),
    AppThemePalette(
        id = 8,
        name = "Negro + Verde Bosque",
        isDark = true,
        bgColor = Color(0xFF040806),
        cardColor = Color(0xFF0A120E),
        primaryAccent = Color(0xFF047857),
        secondaryAccent = Color(0xFF053326),
        textPrimary = Color(0xFFFFFFFF),
        textSecondary = Color(0xFFCBD5E1),
        surfaceBorder = Color(0xFF065F46)
    ),
    AppThemePalette(
        id = 9,
        name = "Negro + Bronce Dorado",
        isDark = true,
        bgColor = Color(0xFF070604),
        cardColor = Color(0xFF12100C),
        primaryAccent = Color(0xFFB45309),
        secondaryAccent = Color(0xFF361604),
        textPrimary = Color(0xFFFFFFFF),
        textSecondary = Color(0xFFCBD5E1),
        surfaceBorder = Color(0xFF78350F)
    )
)

val AccentGreen = Color(0xFF047857)
val AccentOrange = Color(0xFFB45309)
val DangerRed = Color(0xFFB91C1C)
val CrimsonHighlight = Color(0xFFBE123C)

data class TaskColorChoice(
    val tagIndex: Int,
    val name: String,
    val hex: String,
    val color: Color,
    val symbol: String
)

val TaskColorChoices = listOf(
    TaskColorChoice(0, "Morado", "#7C3AED", Color(0xFF5B21B6), "●"),
    TaskColorChoice(1, "Azul", "#2563EB", Color(0xFF1E40AF), "▲"),
    TaskColorChoice(2, "Verde", "#059669", Color(0xFF065F46), "■"),
    TaskColorChoice(3, "Carmín", "#E11D48", Color(0xFF9F1239), "◆"),
    TaskColorChoice(4, "Naranja", "#EA580C", Color(0xFF9A3412), "★"),
    TaskColorChoice(5, "Dorado", "#CA8A04", Color(0xFF854D0E), "⬢"),
    TaskColorChoice(6, "Turquesa", "#0891B2", Color(0xFF155E75), "◉")
)

fun adaptColorForColorblindMode(original: Color, mode: ColorblindMode): Color {
    if (mode == ColorblindMode.NONE) return original
    val r = original.red
    val g = original.green
    val b = original.blue

    return when (mode) {
        ColorblindMode.NONE -> original
        ColorblindMode.PROTANOPIA -> Color(
            red = (0.56f * r + 0.44f * g).coerceIn(0f, 1f),
            green = (0.55f * r + 0.45f * g).coerceIn(0f, 1f),
            blue = (0.24f * g + 0.76f * b).coerceIn(0f, 1f),
            alpha = original.alpha
        )
        ColorblindMode.DEUTERANOPIA -> Color(
            red = (0.625f * r + 0.375f * g).coerceIn(0f, 1f),
            green = (0.70f * r + 0.30f * g).coerceIn(0f, 1f),
            blue = (0.30f * g + 0.70f * b).coerceIn(0f, 1f),
            alpha = original.alpha
        )
        ColorblindMode.TRITANOPIA -> Color(
            red = (0.95f * r + 0.05f * g).coerceIn(0f, 1f),
            green = (0.43f * g + 0.57f * b).coerceIn(0f, 1f),
            blue = (0.475f * g + 0.525f * b).coerceIn(0f, 1f),
            alpha = original.alpha
        )
        ColorblindMode.ACHROMATOPSIA -> {
            val lum = (0.299f * r + 0.587f * g + 0.114f * b).coerceIn(0.15f, 0.85f)
            Color(lum, lum, lum, original.alpha)
        }
    }
}

fun applyAccessibilityToTheme(
    base: AppThemePalette,
    colorblindMode: ColorblindMode,
    highContrast: Boolean,
    borderStyleMode: BorderStyleMode = BorderStyleMode.SUBTLE,
    cornerStyleMode: CornerStyleMode = CornerStyleMode.ROUNDED,
    layoutDensityMode: LayoutDensityMode = LayoutDensityMode.COMFORTABLE
): AppThemePalette {
    val adaptedPrimary = adaptColorForColorblindMode(base.primaryAccent, colorblindMode)
    val adaptedSecondary = adaptColorForColorblindMode(base.secondaryAccent, colorblindMode)

    val effectiveBorderStyle = if (highContrast) BorderStyleMode.STRONG else borderStyleMode
    val border = when (effectiveBorderStyle) {
        BorderStyleMode.NONE -> Color.Transparent
        BorderStyleMode.SUBTLE -> base.surfaceBorder.copy(alpha = if (base.isDark) 0.32f else 0.20f)
        BorderStyleMode.MEDIUM -> base.surfaceBorder.copy(alpha = if (base.isDark) 0.65f else 0.50f)
        BorderStyleMode.STRONG -> if (highContrast) {
            if (base.isDark) Color(0xFFFFFFFF) else Color(0xFF000000)
        } else {
            base.surfaceBorder.copy(alpha = 1.0f)
        }
    }

    val textSec = if (highContrast) {
        if (base.isDark) Color(0xFFFFFFFF) else Color(0xFF000000)
    } else {
        base.textSecondary
    }

    return base.copy(
        primaryAccent = adaptedPrimary,
        secondaryAccent = adaptedSecondary,
        surfaceBorder = border,
        textSecondary = textSec,
        borderStyle = effectiveBorderStyle,
        cornerRadiusDp = cornerStyleMode.radiusDp,
        spacingDp = layoutDensityMode.spacingDp
    )
}

fun priorityColor(priority: Int, colorblindMode: ColorblindMode = ColorblindMode.NONE): Color {
    val base = when (TaskPriority.fromLevel(priority)) {
        TaskPriority.LOW -> Color(0xFF334155)
        TaskPriority.MEDIUM -> Color(0xFF1E40AF)
        TaskPriority.HIGH -> Color(0xFF9A3412)
        TaskPriority.CRITICAL -> CrimsonHighlight
    }
    return adaptColorForColorblindMode(base, colorblindMode)
}

fun taskAccent(hex: String, colorblindMode: ColorblindMode = ColorblindMode.NONE): Color {
    val raw = TaskColorChoices.firstOrNull { it.hex.equals(hex, ignoreCase = true) }?.color
        ?: TaskColorChoices.first().color
    return adaptColorForColorblindMode(raw, colorblindMode)
}

fun taskColorSymbol(hex: String): String =
    TaskColorChoices.firstOrNull { it.hex.equals(hex, ignoreCase = true) }?.symbol ?: "●"

fun taskColorName(hex: String): String =
    TaskColorChoices.firstOrNull { it.hex.equals(hex, ignoreCase = true) }?.name ?: "Morado"

fun hexToColorTag(hex: String): Int =
    TaskColorChoices.firstOrNull { it.hex.equals(hex, ignoreCase = true) }?.tagIndex ?: 0

fun resolveThemeIdForMode(currentThemeId: Int, targetDarkMode: Boolean): Int {
    val current = AvailableThemes.find { it.id == currentThemeId } ?: AvailableThemes[0]
    if (current.isDark == targetDarkMode) return current.id
    return if (targetDarkMode) {
        (current.id + 5).coerceIn(5, 9)
    } else {
        (current.id - 5).coerceIn(0, 4)
    }
}

@Composable
fun appTextFieldColors(
    theme: AppThemePalette,
    accent: Color = theme.primaryAccent,
    isError: Boolean = false,
    containerColor: Color = theme.cardColor
): TextFieldColors = OutlinedTextFieldDefaults.colors(
    focusedTextColor = theme.textPrimary,
    unfocusedTextColor = theme.textPrimary,
    disabledTextColor = theme.textSecondary,
    errorTextColor = theme.textPrimary,
    focusedContainerColor = containerColor,
    unfocusedContainerColor = containerColor,
    disabledContainerColor = containerColor,
    errorContainerColor = containerColor,
    cursorColor = if (isError) DangerRed else accent,
    errorCursorColor = DangerRed,
    focusedBorderColor = if (isError) DangerRed else accent,
    unfocusedBorderColor = if (isError) DangerRed else if (theme.borderStyle == BorderStyleMode.NONE) accent.copy(alpha = 0.25f) else theme.surfaceBorder,
    errorBorderColor = DangerRed,
    focusedLabelColor = if (isError) DangerRed else accent,
    unfocusedLabelColor = if (isError) DangerRed else theme.textSecondary,
    errorLabelColor = DangerRed,
    focusedPlaceholderColor = theme.textSecondary,
    unfocusedPlaceholderColor = theme.textSecondary,
    errorPlaceholderColor = theme.textSecondary,
    focusedLeadingIconColor = accent,
    unfocusedLeadingIconColor = theme.textSecondary,
    focusedTrailingIconColor = accent,
    unfocusedTrailingIconColor = theme.textSecondary
)

@Composable
fun ProvideDynamicAppTheme(
    theme: AppThemePalette,
    content: @Composable () -> Unit
) {
    val colorScheme = remember(theme) {
        if (theme.isDark) {
            darkColorScheme(
                primary = theme.primaryAccent,
                onPrimary = Color.White,
                primaryContainer = theme.secondaryAccent,
                onPrimaryContainer = Color.White,
                secondary = theme.primaryAccent,
                onSecondary = Color.White,
                secondaryContainer = theme.secondaryAccent,
                onSecondaryContainer = Color.White,
                background = theme.bgColor,
                onBackground = Color.White,
                surface = theme.cardColor,
                onSurface = Color.White,
                surfaceVariant = theme.cardColor,
                onSurfaceVariant = theme.textSecondary,
                outline = theme.surfaceBorder,
                error = DangerRed,
                onError = Color.White
            )
        } else {
            lightColorScheme(
                primary = theme.primaryAccent,
                onPrimary = Color.White,
                primaryContainer = theme.secondaryAccent,
                onPrimaryContainer = theme.primaryAccent,
                secondary = theme.primaryAccent,
                onSecondary = Color.White,
                secondaryContainer = theme.secondaryAccent,
                onSecondaryContainer = theme.primaryAccent,
                background = Color.White,
                onBackground = theme.textPrimary,
                surface = Color.White,
                onSurface = theme.textPrimary,
                surfaceVariant = Color.White,
                onSurfaceVariant = theme.textSecondary,
                outline = theme.surfaceBorder,
                error = DangerRed,
                onError = Color.White
            )
        }
    }

    MaterialTheme(colorScheme = colorScheme) {
        CompositionLocalProvider(LocalContentColor provides theme.textPrimary) {
            content()
        }
    }
}
