package ai.mlc.mlcchat.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = SoftPurple,
    onPrimary = Grey10,
    primaryContainer = Blue20,
    onPrimaryContainer = BlueGrey90,
    secondary = SoftPink,
    onSecondary = Grey10,
    secondaryContainer = BlueGrey30,
    onSecondaryContainer = Grey90,
    tertiary = SoftBlue,
    onTertiary = Grey10,
    tertiaryContainer = Blue20,
    onTertiaryContainer = BlueGrey90,
    error = Red80,
    onError = Red10,
    errorContainer = Red30,
    onErrorContainer = Red90,
    background = Grey10,
    onBackground = Grey90,
    surface = Grey10,
    onSurface = Grey90,
    surfaceVariant = BlueGrey30,
    onSurfaceVariant = BlueGrey80,
    outline = BlueGrey60
)

private val LightColorScheme = lightColorScheme(
    primary = AccentDeep,
    onPrimary = Color.White,
    primaryContainer = SoftLavender,
    onPrimaryContainer = Blue10,
    secondary = AccentRose,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFE4F0),
    onSecondaryContainer = Color(0xFF4D233A),
    tertiary = SoftBlue,
    onTertiary = Blue10,
    tertiaryContainer = Color(0xFFDCEBFF),
    onTertiaryContainer = Blue10,
    error = Red40,
    onError = Color.White,
    errorContainer = Red90,
    onErrorContainer = Red10,
    background = MistWhite,
    onBackground = Grey10,
    surface = CardWhite,
    onSurface = Grey10,
    surfaceVariant = GlassWhite,
    onSurfaceVariant = BlueGrey30,
    outline = BlueGrey50
)

@Composable
fun MLCChatTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
