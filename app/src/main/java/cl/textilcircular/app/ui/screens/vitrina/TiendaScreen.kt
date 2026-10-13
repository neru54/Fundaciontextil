package cl.textilcircular.app.ui.screens.vitrina

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import cl.textilcircular.app.data.repository.MarketplaceRepository
import cl.textilcircular.app.data.repository.SociaRepository
import cl.textilcircular.app.domain.model.EstadoFicha
import cl.textilcircular.app.domain.model.FiltrosVitrina
import cl.textilcircular.app.domain.model.Publicacion
import cl.textilcircular.app.domain.model.TiendaPublica
import cl.textilcircular.app.ui.appViewModel
import cl.textilcircular.app.ui.components.Aviso
import cl.textilcircular.app.ui.components.Cargando
import cl.textilcircular.app.ui.components.EstadoVacio
import cl.textilcircular.app.ui.components.PantallaSecundaria
import cl.textilcircular.app.ui.components.TarjetaPublicacion
import cl.textilcircular.app.ui.theme.Amarillo
import cl.textilcircular.app.ui.theme.Negro
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/** Tienda pública de una socia (RF06.4) y vista previa "Así te ven" (RF07.9). */
class TiendaViewModel(savedState: SavedStateHandle, socias: SociaRepository, repo: MarketplaceRepository) : ViewModel() {
    private val sociaId: Long = checkNotNull(savedState["sociaId"])
    val vistaPrevia: Boolean = savedState["vistaPrevia"] ?: false

    val tienda: StateFlow<TiendaPublica?> = socias.tiendaPublica(sociaId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /** Usa la misma consulta de la vitrina: muestra exactamente lo que ve la clienta (RN19). */
    val publicaciones: StateFlow<List<Publicacion>?> = repo.vitrina(FiltrosVitrina(), sociaId = sociaId)
        .map<List<Publicacion>, List<Publicacion>?> { it }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}

@Composable
fun TiendaScreen(
    alVolver: () -> Unit,
    alAbrirPublicacion: (Long) -> Unit,
    alAbrirEnlace: (String) -> Unit
) {
    val vm = appViewModel { c, s -> TiendaViewModel(s, c.sociaRepository, c.marketplaceRepository) }
    val tienda by vm.tienda.collectAsStateWithLifecycle()
    val publicaciones by vm.publicaciones.collectAsStateWithLifecycle()

    PantallaSecundaria(titulo = if (vm.vistaPrevia) "Así te ven" else "Tienda", alVolver = alVolver) { padding ->
        val t = tienda
        val lista = publicaciones
        if (t == null || lista == null) {
            Cargando(Modifier.padding(padding))
            return@PantallaSecundaria
        }
        LazyVerticalGrid(
            columns = GridCells.Adaptive(160.dp),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(padding).fillMaxSize()
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (vm.vistaPrevia) AvisoVistaPrevia(t)
                    EncabezadoTienda(t, alAbrirEnlace)
                    Text("Publicaciones", style = MaterialTheme.typography.titleMedium)
                }
            }
            if (lista.isEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    EstadoVacio(
                        Icons.Filled.Inventory2, "Sin publicaciones visibles",
                        if (vm.vistaPrevia) "Las clientas no ven ninguna publicación tuya en este momento." else "Esta tienda aún no tiene publicaciones."
                    )
                }
            }
            itemsIndexed(lista, key = { _, p -> p.id }) { i, p ->
                TarjetaPublicacion(p, i, { alAbrirPublicacion(p.id) }, Modifier.animateItem())
            }
        }
    }
}

@Composable
private fun AvisoVistaPrevia(t: TiendaPublica) {
    val texto = when {
        t.estadoFicha == EstadoFicha.PENDIENTE ->
            "Tu ficha está pendiente de validación: todavía no apareces en la vitrina."
        t.modoPausa -> "Tienes activado el Modo Pausa: tu tienda está oculta en la vitrina."
        else -> "Así ven tu tienda las clientas. Solo se muestran tus publicaciones publicadas o agotadas."
    }
    Aviso(texto, icono = Icons.Filled.Visibility)
}

@Composable
private fun EncabezadoTienda(t: TiendaPublica, alAbrirEnlace: (String) -> Unit) {
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(
            Modifier.size(84.dp).clip(CircleShape).background(Amarillo),
            contentAlignment = Alignment.Center
        ) {
            Text(
                t.nombreEmprendimiento.split(" ").take(2).mapNotNull { it.firstOrNull()?.uppercase() }.joinToString(""),
                style = MaterialTheme.typography.headlineMedium,
                color = Negro
            )
        }
        Text(t.nombreEmprendimiento, style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
        Text("por ${t.nombreSocia}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp))
            Text("${t.comuna}, ${t.region}", style = MaterialTheme.typography.bodySmall)
        }
        if (t.rubros.isNotEmpty()) {
            Text(t.rubros.joinToString(" · "), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary, textAlign = TextAlign.Center)
        }
        // RN11: WhatsApp e Instagram como alternativa al chat; RN10: se omiten si el dato no es válido.
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            t.telefono?.let { tel ->
                OutlinedButton(onClick = { alAbrirEnlace("https://wa.me/56$tel") }) {
                    Icon(Icons.Filled.Phone, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text("  WhatsApp")
                }
            }
            t.instagramUrl?.let { url ->
                OutlinedButton(onClick = { alAbrirEnlace(url) }) {
                    Icon(Icons.Filled.PhotoCamera, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text("  Instagram")
                }
            }
        }
    }
}
