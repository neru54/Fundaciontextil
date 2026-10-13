package cl.textilcircular.app.ui.screens.vitrina

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import cl.textilcircular.app.data.repository.MarketplaceRepository
import cl.textilcircular.app.domain.catalog.ChileGeo
import cl.textilcircular.app.domain.model.FiltrosVitrina
import cl.textilcircular.app.domain.model.OrdenVitrina
import cl.textilcircular.app.domain.model.Publicacion
import cl.textilcircular.app.domain.model.Rubro
import cl.textilcircular.app.domain.model.TipoPublicacion
import cl.textilcircular.app.ui.appViewModel
import cl.textilcircular.app.ui.components.CampoLista
import cl.textilcircular.app.ui.components.Cargando
import cl.textilcircular.app.ui.components.EstadoVacio
import cl.textilcircular.app.ui.components.TarjetaPublicacion
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

/** Vitrina del marketplace (RF06.1, RF06.2, RF06.5). */
@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class ExplorarViewModel(private val repo: MarketplaceRepository) : ViewModel() {

    private val _filtros = MutableStateFlow(FiltrosVitrina())
    val filtros: StateFlow<FiltrosVitrina> = _filtros.asStateFlow()

    val rubros: StateFlow<List<Rubro>> = repo.rubros
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** null mientras carga la primera vez. */
    val resultados: StateFlow<List<Publicacion>?> = _filtros
        .debounce(250)
        .flatMapLatest { repo.vitrina(it) }
        .map<List<Publicacion>, List<Publicacion>?> { it }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun buscar(texto: String) = _filtros.update { it.copy(texto = texto) }

    /** RN01: al cambiar la región se reinicia la comuna. */
    fun elegirRegion(region: String?) = _filtros.update { it.copy(region = region, comuna = null) }
    fun elegirComuna(comuna: String?) = _filtros.update { it.copy(comuna = comuna) }
    fun elegirRubro(id: Long?) = _filtros.update { it.copy(rubroId = if (it.rubroId == id) null else id) }
    fun elegirTipo(tipo: TipoPublicacion?) = _filtros.update { it.copy(tipo = if (it.tipo == tipo) null else tipo) }
    fun ordenar(orden: OrdenVitrina) = _filtros.update { it.copy(orden = orden) }
    fun limpiar() = _filtros.update { FiltrosVitrina(texto = it.texto, orden = it.orden) }
}

private const val TODAS = "Todas"

@Composable
fun ExplorarScreen(alAbrirPublicacion: (Long) -> Unit, modifier: Modifier = Modifier) {
    val vm = appViewModel { c, _ -> ExplorarViewModel(c.marketplaceRepository) }
    val filtros by vm.filtros.collectAsStateWithLifecycle()
    val rubros by vm.rubros.collectAsStateWithLifecycle()
    val resultados by vm.resultados.collectAsStateWithLifecycle()
    var verFiltros by rememberSaveable { mutableStateOf(false) }
    var menuOrden by remember { mutableStateOf(false) }

    Column(modifier.fillMaxSize()) {
        // Encabezado con búsqueda
        Column(Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp)) {
            Text("Explora la red textil circular", style = MaterialTheme.typography.headlineSmall)
            Text(
                "Productos y servicios de socias de todo Chile",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
                OutlinedTextField(
                    value = filtros.texto,
                    onValueChange = vm::buscar,
                    placeholder = { Text("Buscar prendas, arreglos, talleres…") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    trailingIcon = {
                        if (filtros.texto.isNotEmpty()) {
                            IconButton(onClick = { vm.buscar("") }) { Icon(Icons.Filled.Clear, contentDescription = "Borrar búsqueda") }
                        }
                    },
                    singleLine = true,
                    shape = MaterialTheme.shapes.large,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { verFiltros = !verFiltros }) {
                    BadgedBox(badge = { if (filtros.cantidadActivos > 0) Badge { Text("${filtros.cantidadActivos}") } }) {
                        Icon(Icons.Filled.FilterList, contentDescription = "Filtros")
                    }
                }
                IconButton(onClick = { menuOrden = true }) {
                    Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = "Ordenar")
                    DropdownMenu(expanded = menuOrden, onDismissRequest = { menuOrden = false }) {
                        OrdenVitrina.entries.forEach { orden ->
                            DropdownMenuItem(
                                text = { Text(orden.etiqueta) },
                                onClick = { vm.ordenar(orden); menuOrden = false },
                                trailingIcon = if (orden == filtros.orden) { { Text("✓") } } else null
                            )
                        }
                    }
                }
            }
        }

        // Chips de tipo, siempre visibles
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(TipoPublicacion.entries) { tipo ->
                FilterChip(
                    selected = filtros.tipo == tipo,
                    onClick = { vm.elegirTipo(tipo) },
                    label = { Text(tipo.etiqueta) }
                )
            }
        }

        // Panel de filtros geográficos y de rubro, con aparición animada (RNF07)
        AnimatedVisibility(
            visible = verFiltros,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                CampoLista(
                    etiqueta = "Región", opciones = listOf(TODAS) + ChileGeo.regiones, seleccion = filtros.region ?: TODAS,
                    alElegir = { vm.elegirRegion(it.takeIf { r -> r != TODAS }) }, error = null, disparador = 0
                )
                CampoLista(
                    etiqueta = "Comuna", opciones = listOf(TODAS) + ChileGeo.comunasDe(filtros.region),
                    seleccion = filtros.comuna ?: TODAS, alElegir = { vm.elegirComuna(it.takeIf { c -> c != TODAS }) },
                    error = null, disparador = 0, habilitado = filtros.region != null
                )
                CampoLista(
                    etiqueta = "Rubro", opciones = listOf<Rubro?>(null) + rubros,
                    seleccion = rubros.firstOrNull { it.id == filtros.rubroId },
                    alElegir = { vm.elegirRubro(it?.id) }, error = null, disparador = 0,
                    texto = { it?.nombre ?: "Todos" }
                )
                if (filtros.cantidadActivos > 0) {
                    TextButton(onClick = vm::limpiar) { Text("Limpiar filtros") }
                }
            }
        }

        val lista = resultados
        when {
            lista == null -> Cargando()
            lista.isEmpty() -> EstadoVacio(
                icono = Icons.Filled.SearchOff,
                titulo = "No encontramos publicaciones",
                mensaje = "Prueba con otra palabra o quita algunos filtros.",
                accion = if (filtros.cantidadActivos > 0 || filtros.texto.isNotEmpty()) "Limpiar búsqueda" else null,
                alAccionar = { vm.limpiar(); vm.buscar("") }
            )
            else -> LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 160.dp),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Text(
                        "${lista.size} ${if (lista.size == 1) "publicación" else "publicaciones"}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                itemsIndexed(lista, key = { _, p -> p.id }) { indice, p ->
                    TarjetaPublicacion(
                        p = p,
                        indice = indice,
                        alTocar = { alAbrirPublicacion(p.id) },
                        modifier = Modifier.fillMaxWidth().animateItem()
                    )
                }
            }
        }
    }
}
