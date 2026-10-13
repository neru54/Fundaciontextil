package cl.textilcircular.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.textilcircular.app.R

/*
 * Identidad visual de la Fundación (RNF12): blanco, negro, amarillo y turquesa, estilo minimalista.
 * El turquesa se oscurece para cumplir contraste WCAG 2.1 AA con texto blanco (RNF02).
 * Pendiente con la Fundación: códigos oficiales y licencia de la tipografía Monument.
 */
val Turquesa = Color(0xFF00737A)
val TurquesaClaro = Color(0xFFB8E8E6)
val TurquesaOscuro = Color(0xFF004F54)
val Amarillo = Color(0xFFFFC629)
val AmarilloClaro = Color(0xFFFFF1C2)
val Negro = Color(0xFF1A1A1A)
val Blanco = Color(0xFFFFFFFF)
val GrisFondo = Color(0xFFF6F7F7)
val RojoError = Color(0xFFB3261E)
val VerdeOk = Color(0xFF2E7D32)

private val ColoresClaros = lightColorScheme(
    primary = Turquesa,
    onPrimary = Blanco,
    primaryContainer = TurquesaClaro,
    onPrimaryContainer = TurquesaOscuro,
    secondary = Amarillo,
    onSecondary = Negro,
    secondaryContainer = AmarilloClaro,
    onSecondaryContainer = Negro,
    tertiary = Negro,
    onTertiary = Blanco,
    background = Blanco,
    onBackground = Negro,
    surface = Blanco,
    onSurface = Negro,
    surfaceVariant = GrisFondo,
    onSurfaceVariant = Color(0xFF4A4F50),
    surfaceContainer = GrisFondo,
    error = RojoError,
    onError = Blanco,
    outline = Color(0xFF8A9192)
)

private val ColoresOscuros = darkColorScheme(
    primary = Color(0xFF6FD3CF),
    onPrimary = Color(0xFF00363A),
    primaryContainer = TurquesaOscuro,
    onPrimaryContainer = TurquesaClaro,
    secondary = Amarillo,
    onSecondary = Negro,
    secondaryContainer = Color(0xFF5C4600),
    onSecondaryContainer = AmarilloClaro,
    background = Color(0xFF121414),
    onBackground = Color(0xFFE3E3E3),
    surface = Color(0xFF121414),
    onSurface = Color(0xFFE3E3E3),
    surfaceVariant = Color(0xFF2A2E2E),
    onSurfaceVariant = Color(0xFFC4C8C8),
    error = Color(0xFFF2B8B5),
    onError = Color(0xFF601410)
)

@OptIn(ExperimentalTextApi::class)
private fun nunito(peso: Int) = Font(
    R.font.nunito,
    weight = FontWeight(peso),
    variationSettings = FontVariation.Settings(FontVariation.weight(peso))
)

val Nunito = FontFamily(nunito(400), nunito(600), nunito(700), nunito(800))

private val base = Typography()

val TipografiaTextil = Typography(
    displaySmall = base.displaySmall.copy(fontFamily = Nunito, fontWeight = FontWeight.ExtraBold),
    headlineMedium = base.headlineMedium.copy(fontFamily = Nunito, fontWeight = FontWeight.ExtraBold),
    headlineSmall = base.headlineSmall.copy(fontFamily = Nunito, fontWeight = FontWeight.Bold),
    titleLarge = base.titleLarge.copy(fontFamily = Nunito, fontWeight = FontWeight.Bold),
    titleMedium = base.titleMedium.copy(fontFamily = Nunito, fontWeight = FontWeight.Bold),
    titleSmall = base.titleSmall.copy(fontFamily = Nunito, fontWeight = FontWeight.Bold),
    bodyLarge = base.bodyLarge.copy(fontFamily = Nunito, fontSize = 17.sp),
    bodyMedium = base.bodyMedium.copy(fontFamily = Nunito, fontSize = 15.sp),
    bodySmall = base.bodySmall.copy(fontFamily = Nunito, fontSize = 13.sp),
    labelLarge = base.labelLarge.copy(fontFamily = Nunito, fontWeight = FontWeight.Bold, fontSize = 15.sp),
    labelMedium = base.labelMedium.copy(fontFamily = Nunito, fontWeight = FontWeight.SemiBold),
    labelSmall = base.labelSmall.copy(fontFamily = Nunito, fontWeight = FontWeight.SemiBold)
)

val FormasTextil = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(20.dp)
)

val EstiloPrecio = TextStyle(fontFamily = Nunito, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)

@Composable
fun TextilTheme(oscuro: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (oscuro) ColoresOscuros else ColoresClaros,
        typography = TipografiaTextil,
        shapes = FormasTextil,
        content = content
    )
}
