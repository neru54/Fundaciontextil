package cl.textilcircular.app.ui.screens.bienvenida

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Recycling
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.TravelExplore
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cl.textilcircular.app.ui.theme.Amarillo
import cl.textilcircular.app.ui.theme.Blanco
import cl.textilcircular.app.ui.theme.Negro
import cl.textilcircular.app.ui.theme.Turquesa

/** Pantalla de entrada del marketplace: explorar como clienta o ingresar como socia (Anexo C). */
@Composable
fun BienvenidaScreen(alExplorar: () -> Unit, alSoySocia: () -> Unit) {
    val entrada = remember { Animatable(0f) }
    LaunchedEffect(Unit) { entrada.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)) }
    val giro = rememberInfiniteTransition(label = "giroLogo")
    val angulo by giro.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(12_000, easing = LinearEasing), RepeatMode.Restart),
        label = "angulo"
    )

    Column(
        Modifier.fillMaxSize().background(Turquesa).systemBarsPadding().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            Modifier
                .size(140.dp)
                .graphicsLayer {
                    scaleX = entrada.value
                    scaleY = entrada.value
                }
                .background(Amarillo, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Filled.Recycling,
                contentDescription = null,
                tint = Negro,
                modifier = Modifier.size(84.dp).graphicsLayer { rotationZ = angulo }
            )
        }
        Spacer(Modifier.height(28.dp))
        Column(
            Modifier.graphicsLayer {
                alpha = entrada.value.coerceIn(0f, 1f)
                translationY = (1f - entrada.value) * 60f
            },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Textil Circular", style = MaterialTheme.typography.displaySmall, color = Blanco)
            Spacer(Modifier.height(8.dp))
            Text(
                "El marketplace de las emprendedoras textiles de la Fundación: prendas con nueva vida, arreglos, talleres y arriendo.",
                style = MaterialTheme.typography.bodyLarge,
                color = Blanco,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(36.dp))
            Button(
                onClick = alExplorar,
                colors = ButtonDefaults.buttonColors(containerColor = Amarillo, contentColor = Negro),
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Icon(Icons.Filled.TravelExplore, contentDescription = null)
                Spacer(Modifier.size(8.dp))
                Text("Quiero explorar la vitrina")
            }
            Spacer(Modifier.height(12.dp))
            OutlinedButton(
                onClick = alSoySocia,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Blanco),
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Icon(Icons.Filled.Storefront, contentDescription = null)
                Spacer(Modifier.size(8.dp))
                Text("Soy socia: gestionar mi tienda")
            }
        }
    }
}
