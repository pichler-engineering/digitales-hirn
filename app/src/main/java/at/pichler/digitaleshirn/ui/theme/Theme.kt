package at.pichler.digitaleshirn.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val AppDarkColorScheme = darkColorScheme(
    primary = GreenPrimary,
    secondary = GreenSecondary,
    background = DarkBackground,
    surface = DarkSurface,
    onPrimary = TextLight,
    onSecondary = DarkBackground,
    onBackground = TextLight,
    onSurface = TextLight
)

@Composable
fun DigitalesHirnTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AppDarkColorScheme,
        typography = Typography,
        content = content
    )
}
