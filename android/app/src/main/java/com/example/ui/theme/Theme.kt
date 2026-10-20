package com.example.ui.theme

import android.app.Activity
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
    primary = TurquesaTextil,
    onPrimary = Color.White,
    primaryContainer = TurquesaOscuro,
    onPrimaryContainer = TurquesaClaro,
    secondary = AmarilloMostaza,
    onSecondary = NegroCarbon,
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    onBackground = Color(0xFFE6E1E5),
    onSurface = Color(0xFFE6E1E5)
)

private val LightColorScheme = lightColorScheme(
    primary = TurquesaTextil,
    onPrimary = Color.White,
    primaryContainer = TurquesaClaro,
    onPrimaryContainer = TurquesaOscuro,
    secondary = AmarilloMostaza,
    onSecondary = NegroCarbon,
    background = BlancoMarfil,
    surface = Color.White,
    onBackground = NegroCarbon,
    onSurface = NegroCarbon
)

@Composable
fun TextilCircularTheme(
    // La app usa siempre la paleta clara de la fundación: las tarjetas y fondos están
    // diseñados sobre blanco, y así el texto mantiene buen contraste en cualquier teléfono.
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
