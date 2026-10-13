package cl.textilcircular.app.ui.screens.vitrina

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import cl.textilcircular.app.data.repository.MarketplaceRepository
import cl.textilcircular.app.data.repository.SociaRepository
import cl.textilcircular.app.data.session.SessionStore
import cl.textilcircular.app.domain.model.Disponibilidad
import cl.textilcircular.app.domain.model.EstadoPublicacion
import cl.textilcircular.app.domain.model.Publicacion
import cl.textilcircular.app.domain.model.TiendaPublica
import cl.textilcircular.app.ui.appViewModel
import cl.textilcircular.app.ui.components.Cargando
import cl.textilcircular.app.ui.components.EstadoVacio
import cl.textilcircular.app.ui.components.EtiquetaEstado
import cl.textilcircular.app.ui.components.FotoPublicacion
import cl.textilcircular.app.ui.components.PantallaSecundaria
import cl.textilcircular.app.ui.components.TextoPrecio
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface DetalleUi {
    data object Cargando : DetalleUi
    data object NoExiste : DetalleUi
    data class Listo(val publicacion: Publicacion, val tienda: TiendaPublica?, val esMia: Boolean) : DetalleUi
}

/** Detalle de publicación (RF06.3) con registro de vistas (RF06.8). */
@OptIn(ExperimentalCoroutinesApi::class)
class DetalleViewModel(
    savedState: SavedStateHandle,
    repo: MarketplaceRepository,
    socias: SociaRepository,
    session: SessionStore
) : ViewModel() {
    private val id: Long = checkNotNull(savedState["id"])

    val ui: StateFlow<DetalleUi> = repo.detalle(id)
        .flatMapLatest { p ->
            if (p == null) flowOf<DetalleUi>(DetalleUi.NoExiste)
            else socias.tiendaPublica(p.sociaId).map<TiendaPublica?, DetalleUi> { tienda ->
                DetalleUi.Listo(p, tienda, esMia = p.sociaId == session.sociaIdActual())
            }
        }
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DetalleUi.Cargando)

    init {
        viewModelScope.launch {
            // La socia que mira su propia publicación no suma vistas.
            val p = repo.detalle(id).first()
            if (p != null && p.sociaId != session.sociaIdActual()) repo.registrarVista(id)
        }
    }
}

@Composable
fun DetalleScreen(
    alVolver: () -> Unit,
    alVerTienda: (Long) -> Unit,
    alConsultar: (sociaId: Long, publicacionId: Long) -> Unit,
    alEditar: (Long) -> Unit
) {
    val vm = appViewModel { c, s -> DetalleViewModel(s, c.marketplaceRepository, c.sociaRepository, c.session) }
    val ui by vm.ui.collectAsStateWithLifecycle()

    PantallaSecundaria(
        titulo = "Publicación",
        alVolver = alVolver,
        barraInferior = {
            val listo = ui as? DetalleUi.Listo
            if (listo != null) {
                Surface(tonalElevation = 3.dp) {
                    Row(
                        Modifier.fillMaxWidth().navigationBarsPadding().padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(onClick = { alVerTienda(listo.publicacion.sociaId) }, modifier = Modifier.weight(1f).height(52.dp)) {
                            Icon(Icons.Filled.Storefront, contentDescription = null)
                            Spacer(Modifier.width(6.dp))
                            Text("Ver tienda")
                        }
                        if (listo.esMia) {
                            Button(onClick = { alEditar(listo.publicacion.id) }, modifier = Modifier.weight(1.4f).height(52.dp)) {
                                Icon(Icons.Filled.Edit, contentDescription = null)
                                Spacer(Modifier.width(6.dp))
                                Text("Editar")
                            }
                        } else {
                            Button(
                                onClick = { alConsultar(listo.publicacion.sociaId, listo.publicacion.id) },
                                modifier = Modifier.weight(1.4f).height(52.dp)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null)
                                Spacer(Modifier.width(6.dp))
                                Text("Consultar a la socia")
                            }
                        }
                    }
                }
            }
        }
    ) { padding ->
        when (val estado = ui) {
            DetalleUi.Cargando -> Cargando(Modifier.padding(padding))
            DetalleUi.NoExiste -> EstadoVacio(
                Icons.Filled.SearchOff, "Esta publicación ya no está disponible",
                "Puede que la socia la haya eliminado.", Modifier.padding(padding)
            )
            is DetalleUi.Listo -> ContenidoDetalle(estado.publicacion, estado.tienda, estado.esMia, Modifier.padding(padding))
        }
    }
}

@Composable
private fun ContenidoDetalle(p: Publicacion, tienda: TiendaPublica?, esMia: Boolean, modifier: Modifier) {
    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        CarruselFotos(p.fotos, p.titulo)
        Column(Modifier.padding(20.dp).animateContentSize(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                AssistChip(onClick = {}, label = { Text(p.tipo.etiqueta) }, leadingIcon = { Icon(Icons.Filled.Category, null, Modifier.size(18.dp)) })
                if (p.estado == EstadoPublicacion.AGOTADA || esMia) EtiquetaEstado(p.estado)
            }
            Text(p.titulo, style = MaterialTheme.typography.headlineSmall)
            TextoPrecio(p.precio)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Inventory2, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(
                    if (p.disponibilidad == Disponibilidad.A_PEDIDO && p.diasElaboracion != null)
                        "Hecha a pedido · ${p.diasElaboracion} días aprox."
                    else p.disponibilidad.etiqueta,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            HorizontalDivider()
            Text("Descripción", style = MaterialTheme.typography.titleSmall)
            Text(p.descripcion, style = MaterialTheme.typography.bodyLarge)
            Text("Rubro: ${p.rubroNombre}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            HorizontalDivider()
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Storefront, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Column {
                    Text(p.nombreTienda, style = MaterialTheme.typography.titleMedium)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp))
                        Text("${p.comuna}, ${p.region}", style = MaterialTheme.typography.bodySmall)
                    }
                    if (tienda != null && tienda.rubros.isNotEmpty()) {
                        Text(tienda.rubros.joinToString(" · "), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            if (esMia) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Visibility, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("${p.vistas} vistas · ${p.consultas} consultas", style = MaterialTheme.typography.bodyMedium)
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

/** Carrusel de fotos con indicador de página animado (RF02.4). */
@Composable
fun CarruselFotos(fotos: List<String>, descripcion: String) {
    val paginas = fotos.ifEmpty { listOf<String?>(null) }
    val pager = rememberPagerState { paginas.size }
    Box {
        HorizontalPager(state = pager, modifier = Modifier.fillMaxWidth().aspectRatio(1f)) { i ->
            FotoPublicacion(paginas[i], "$descripcion, foto ${i + 1} de ${paginas.size}", Modifier.fillMaxSize())
        }
        if (paginas.size > 1) {
            Row(
                Modifier.align(Alignment.BottomCenter).padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                repeat(paginas.size) { i ->
                    val ancho by animateDpAsState(if (pager.currentPage == i) 22.dp else 8.dp, label = "indicador")
                    Box(
                        Modifier
                            .height(8.dp)
                            .width(ancho)
                            .clip(CircleShape)
                            .background(
                                if (pager.currentPage == i) MaterialTheme.colorScheme.secondary
                                else MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                            )
                    )
                }
            }
        }
    }
}
