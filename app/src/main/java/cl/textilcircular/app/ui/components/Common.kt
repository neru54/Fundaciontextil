package cl.textilcircular.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Recycling
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Weekend
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cl.textilcircular.app.data.media.ImageStorage
import cl.textilcircular.app.domain.model.EstadoPublicacion
import cl.textilcircular.app.domain.model.Publicacion
import cl.textilcircular.app.domain.validation.Precio
import cl.textilcircular.app.ui.theme.Amarillo
import cl.textilcircular.app.ui.theme.AmarilloClaro
import cl.textilcircular.app.ui.theme.EstiloPrecio
import cl.textilcircular.app.ui.theme.Negro
import cl.textilcircular.app.ui.theme.Turquesa
import cl.textilcircular.app.ui.theme.TurquesaClaro
import cl.textilcircular.app.ui.theme.TurquesaOscuro
import coil.compose.AsyncImage
import java.io.File

private val iconosDemo = listOf(
    Icons.Filled.Checkroom, Icons.Filled.ContentCut, Icons.Filled.ShoppingBag,
    Icons.Filled.Recycling, Icons.Filled.School, Icons.Filled.Weekend
)
private val fondosDemo = listOf(
    listOf(TurquesaClaro, Turquesa), listOf(AmarilloClaro, Amarillo), listOf(Color(0xFFE6E6E6), Color(0xFF9E9E9E)),
    listOf(Color(0xFFD7F2EF), TurquesaOscuro), listOf(Color(0xFFFFE9B0), Color(0xFFE0A800))
)

/**
 * Foto de una publicación. Las de la base de demostración ("seed:n") se dibujan como ilustración;
 * las reales se cargan desde el almacenamiento interno con Coil.
 */
@Composable
fun FotoPublicacion(ruta: String?, descripcion: String?, modifier: Modifier = Modifier) {
    when {
        ruta == null -> PlaceholderFoto(0, modifier, sinFoto = true)
        ruta.startsWith(ImageStorage.SEED_PREFIX) ->
            PlaceholderFoto(ruta.removePrefix(ImageStorage.SEED_PREFIX).toIntOrNull() ?: 0, modifier)
        else -> AsyncImage(
            model = File(ruta),
            contentDescription = descripcion,
            contentScale = ContentScale.Crop,
            modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant)
        )
    }
}

@Composable
private fun PlaceholderFoto(n: Int, modifier: Modifier, sinFoto: Boolean = false) {
    val colores = fondosDemo[n % fondosDemo.size]
    Box(
        modifier
            .background(
                if (sinFoto) Brush.linearGradient(listOf(Color(0xFFEFEFEF), Color(0xFFDADADA)))
                else Brush.linearGradient(colores)
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            if (sinFoto) Icons.Filled.Info else iconosDemo[n % iconosDemo.size],
            contentDescription = if (sinFoto) "Sin foto" else "Foto de demostración",
            tint = Color.White.copy(alpha = 0.9f),
            modifier = Modifier.size(48.dp)
        )
    }
}

/** Etiqueta de estado con cambio de color animado (RNF07). */
@Composable
fun EtiquetaEstado(estado: EstadoPublicacion, modifier: Modifier = Modifier) {
    val (fondoObjetivo, textoObjetivo) = when (estado) {
        EstadoPublicacion.PUBLICADA -> TurquesaClaro to TurquesaOscuro
        EstadoPublicacion.PAUSADA -> Color(0xFFE0E0E0) to Negro
        EstadoPublicacion.BORRADOR -> AmarilloClaro to Negro
        EstadoPublicacion.AGOTADA -> Color(0xFFFFDAD6) to Color(0xFF8C1D18)
    }
    val fondo by animateColorAsState(fondoObjetivo, tween(400), label = "fondoEstado")
    val texto by animateColorAsState(textoObjetivo, tween(400), label = "textoEstado")
    Surface(color = fondo, contentColor = texto, shape = RoundedCornerShape(50), modifier = modifier) {
        Text(
            estado.etiqueta,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
        )
    }
}

@Composable
fun TextoPrecio(precio: Int?, modifier: Modifier = Modifier) {
    Text(
        Precio.formatear(precio),
        style = EstiloPrecio,
        color = if (precio == null) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.primary,
        modifier = modifier
    )
}

/**
 * Tarjeta de la vitrina (RF06.1). Entra con un desvanecido y leve escala, escalonado por posición (RNF07).
 */
@Composable
fun TarjetaPublicacion(p: Publicacion, indice: Int, alTocar: () -> Unit, modifier: Modifier = Modifier) {
    val aparicion = remember(p.id) { Animatable(0f) }
    LaunchedEffect(p.id) {
        aparicion.animateTo(1f, tween(durationMillis = 350, delayMillis = (indice % 8) * 45, easing = FastOutSlowInEasing))
    }
    Card(
        onClick = alTocar,
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier.graphicsLayer {
            alpha = aparicion.value
            val escala = 0.92f + 0.08f * aparicion.value
            scaleX = escala
            scaleY = escala
        }
    ) {
        Box {
            FotoPublicacion(p.portada, p.titulo, Modifier.fillMaxWidth().aspectRatio(1f))
            if (p.estado == EstadoPublicacion.AGOTADA) {
                EtiquetaEstado(p.estado, Modifier.align(Alignment.TopStart).padding(8.dp))
            }
        }
        Column(Modifier.padding(10.dp)) {
            Text(p.titulo, style = MaterialTheme.typography.titleSmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(2.dp))
            TextoPrecio(p.precio)
            Text(p.nombreTienda, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.LocationOn, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(p.comuna, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
            }
        }
    }
}

@Composable
fun EstadoVacio(
    icono: ImageVector,
    titulo: String,
    mensaje: String,
    modifier: Modifier = Modifier,
    accion: String? = null,
    alAccionar: () -> Unit = {}
) {
    Column(
        modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            Modifier.size(88.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(icono, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(44.dp))
        }
        Text(titulo, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
        Text(mensaje, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (accion != null) {
            Spacer(Modifier.height(8.dp))
            Button(onClick = alAccionar) { Text(accion) }
        }
    }
}

/** Botón principal con indicador de progreso mientras se guarda. */
@Composable
fun BotonCarga(
    texto: String,
    cargando: Boolean,
    alTocar: () -> Unit,
    modifier: Modifier = Modifier,
    icono: ImageVector? = null,
    habilitado: Boolean = true
) {
    Button(
        onClick = alTocar,
        enabled = habilitado && !cargando,
        modifier = modifier.fillMaxWidth().height(52.dp)
    ) {
        if (cargando) {
            CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.5.dp, color = MaterialTheme.colorScheme.onPrimary)
            Spacer(Modifier.width(10.dp))
            Text("Guardando…")
        } else {
            if (icono != null) {
                Icon(icono, contentDescription = null)
                Spacer(Modifier.width(8.dp))
            }
            Text(texto)
        }
    }
}

/** Aviso destacado (por ejemplo, ficha pendiente o Modo Pausa). */
@Composable
fun Aviso(
    texto: String,
    icono: ImageVector = Icons.Filled.Info,
    modifier: Modifier = Modifier,
    fondo: Color = AmarilloClaro,
    alTocar: (() -> Unit)? = null
) {
    Surface(
        color = fondo,
        contentColor = Negro,
        shape = MaterialTheme.shapes.medium,
        modifier = modifier
            .fillMaxWidth()
            .then(if (alTocar != null) Modifier.clickable(onClick = alTocar) else Modifier)
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icono, contentDescription = null)
            Spacer(Modifier.width(10.dp))
            Text(texto, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun Cargando(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
}
