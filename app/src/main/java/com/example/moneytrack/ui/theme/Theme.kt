package com.example.moneytrack.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ─── 深色方案（主体验）────────────────────────────────────────────────────

private val DarkColorScheme = darkColorScheme(
    primary              = Teal300,
    onPrimary            = Color(0xFF00201D),
    primaryContainer     = TealDark900,
    onPrimaryContainer   = Teal100,

    secondary            = Teal200,
    onSecondary          = Color(0xFF00201D),
    secondaryContainer   = TealDark800,
    onSecondaryContainer = Color(0xFFA7F3EE),

    // tertiary = 收入绿
    tertiary             = GreenIncomeDark,
    onTertiary           = Color(0xFF002200),
    tertiaryContainer    = Color(0xFF003900),
    onTertiaryContainer  = Color(0xFFB9F5B9),

    // error = 支出红
    error                = RedExpenseDark,
    onError              = Color(0xFF2D0000),
    errorContainer       = Color(0xFF5C0000),
    onErrorContainer     = Color(0xFFFFDAD6),

    background           = DarkBg,
    onBackground         = Color(0xFFCDE8E4),
    surface              = DarkSurface,
    onSurface            = Color(0xFFCDE8E4),
    surfaceVariant       = DarkVariant,
    onSurfaceVariant     = Color(0xFF8DAEB4),
    outline              = Color(0xFF4A6B70),
    outlineVariant       = Color(0xFF1A3035),
    inverseSurface       = Color(0xFFD0E8E5),
    inverseOnSurface     = DarkBg,
    inversePrimary       = Teal700,
    surfaceTint          = Teal300,
)

// ─── 浅色方案 ──────────────────────────────────────────────────────────────

private val LightColorScheme = lightColorScheme(
    primary              = Teal700,
    onPrimary            = Color(0xFFFFFFFF),
    primaryContainer     = Teal100,
    onPrimaryContainer   = Color(0xFF00201D),

    secondary            = Color(0xFF00897B),
    onSecondary          = Color(0xFFFFFFFF),
    secondaryContainer   = Color(0xFFCCF0EC),
    onSecondaryContainer = Color(0xFF00312D),

    // tertiary = 收入绿
    tertiary             = GreenIncomeLight,
    onTertiary           = Color(0xFFFFFFFF),
    tertiaryContainer    = Color(0xFFC8E6C9),
    onTertiaryContainer  = Color(0xFF00210B),

    // error = 支出红
    error                = RedExpenseLight,
    onError              = Color(0xFFFFFFFF),
    errorContainer       = Color(0xFFFFCDD2),
    onErrorContainer     = Color(0xFF4D0000),

    background           = LightBg,
    onBackground         = Color(0xFF0D1C1E),
    surface              = Color(0xFFFFFFFF),
    onSurface            = Color(0xFF0D1C1E),
    surfaceVariant       = LightVariant,
    onSurfaceVariant     = Color(0xFF3A5558),
    outline              = Color(0xFF5F8085),
    outlineVariant       = Color(0xFFBFD8DC),
    inverseSurface       = Color(0xFF1E3335),
    inverseOnSurface     = Color(0xFFECF4F3),
    inversePrimary       = Teal300,
    surfaceTint          = Teal700,
)

@Composable
fun MoneyTrackTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // 关闭 Dynamic Color，确保我们的自定义配色生效
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    // 让状态栏融入应用背景
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = Typography,
        content     = content
    )
}
