package cl.textilcircular.app.ui.screens.perfil

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import cl.textilcircular.app.data.repository.CambiosPerfil
import cl.textilcircular.app.data.repository.MarketplaceRepository
import cl.textilcircular.app.data.repository.SociaRepository
import cl.textilcircular.app.data.session.SessionStore
import cl.textilcircular.app.domain.catalog.ChileGeo
import cl.textilcircular.app.domain.model.Rubro
import cl.textilcircular.app.domain.validation.Validators
import cl.textilcircular.app.ui.appViewModel
import cl.textilcircular.app.ui.components.BotonCarga
import cl.textilcircular.app.ui.components.CampoLista
import cl.textilcircular.app.ui.components.CampoTexto
import cl.textilcircular.app.ui.components.Cargando
import cl.textilcircular.app.ui.components.ColumnaFormulario
import cl.textilcircular.app.ui.components.MensajeError
import cl.textilcircular.app.ui.components.PantallaSecundaria
import cl.textilcircular.app.ui.components.SelectorChips
import cl.textilcircular.app.ui.components.TecladoTelefono
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EditarPerfilUi(
    val cargando: Boolean = true,
    val nombre: String = "",
    val telefono: String = "",
    val region: String? = null,
    val comuna: String? = null,
    val nombreEmprendimiento: String = "",
    val rubros: Set<Long> = emptySet(),
    val instagram: String = "",
    val errores: Map<String, String> = emptyMap(),
    val errorGeneral: String? = null,
    val intentos: Int = 0,
    val guardando: Boolean = false,
    val guardado: Boolean = false
)

/** RF01.3: edición autónoma del perfil, con las mismas validaciones del formulario (RF01.2). */
class EditarPerfilViewModel(
    private val session: SessionStore,
    private val socias: SociaRepository,
    marketplace: MarketplaceRepository
) : ViewModel() {

    private val _ui = MutableStateFlow(EditarPerfilUi())
    val ui: StateFlow<EditarPerfilUi> = _ui.asStateFlow()

    val rubros: StateFlow<List<Rubro>> = marketplace.rubros
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        viewModelScope.launch {
            val s = socias.observar(session.sociaId.filterNotNull().first()).filterNotNull().first()
            _ui.update {
                it.copy(
                    cargando = false, nombre = s.nombre, telefono = s.telefono, region = regionValida(s.region),
                    comuna = s.comuna.takeIf { c -> ChileGeo.esComunaDeRegion(s.region, c) },
                    nombreEmprendimiento = s.nombreEmprendimiento, rubros = s.rubroIds.toSet(),
                    instagram = s.instagram.orEmpty()
                )
            }
        }
    }

    fun editar(campo: String, cambio: (EditarPerfilUi) -> EditarPerfilUi) =
        _ui.update { cambio(it).copy(errores = it.errores - campo, errorGeneral = null) }

    fun elegirRegion(r: String) = editar("region") { if (it.region == r) it else it.copy(region = r, comuna = null) }

    fun alternarRubro(id: Long) = editar("rubros") {
        it.copy(rubros = if (id in it.rubros) it.rubros - id else if (it.rubros.size < 2) it.rubros + id else it.rubros)
    }

    fun guardar() {
        val s = _ui.value
        val errores = buildMap {
            Validators.nombreCompleto(s.nombre)?.let { put("nombre", it) }
            Validators.telefono(s.telefono)?.let { put("telefono", it) }
            Validators.region(s.region)?.let { put("region", it) }
            Validators.comuna(s.region, s.comuna)?.let { put("comuna", it) }
            Validators.nombreEmprendimiento(s.nombreEmprendimiento)?.let { put("emprendimiento", it) }
            Validators.rubros(s.rubros)?.let { put("rubros", it) }
            Validators.instagram(s.instagram)?.let { put("instagram", it) }
        }
        if (errores.isNotEmpty()) {
            _ui.update { it.copy(errores = errores, intentos = it.intentos + 1) }
            return
        }
        _ui.update { it.copy(guardando = true) }
        viewModelScope.launch {
            val id = session.sociaId.filterNotNull().first()
            socias.actualizarPerfil(
                id, CambiosPerfil(s.nombre, s.telefono, s.region!!, s.comuna!!, s.nombreEmprendimiento, s.rubros, s.instagram)
            )
                .onSuccess { _ui.update { it.copy(guardando = false, guardado = true) } }
                .onFailure { e -> _ui.update { it.copy(guardando = false, errorGeneral = e.message) } }
        }
    }
}

@Composable
fun EditarPerfilScreen(alVolver: () -> Unit) {
    val vm = appViewModel { c, _ -> EditarPerfilViewModel(c.session, c.sociaRepository, c.marketplaceRepository) }
    val ui by vm.ui.collectAsStateWithLifecycle()
    val rubros by vm.rubros.collectAsStateWithLifecycle()
    LaunchedEffect(ui.guardado) { if (ui.guardado) alVolver() }

    PantallaSecundaria(titulo = "Editar perfil", alVolver = alVolver) { padding ->
        if (ui.cargando) {
            Cargando(Modifier.padding(padding))
            return@PantallaSecundaria
        }
        val e = ui.errores
        ColumnaFormulario(Modifier.padding(padding).fillMaxSize().imePadding().verticalScroll(rememberScrollState())) {
            Spacer(Modifier.height(8.dp))
            Text("Los cambios se ven en la vitrina apenas guardas.", style = MaterialTheme.typography.bodyMedium)
            CampoTexto(
                valor = ui.nombre, alCambiar = { v -> vm.editar("nombre") { it.copy(nombre = v) } },
                etiqueta = "Nombre y apellido", error = e["nombre"], disparador = ui.intentos, icono = Icons.Filled.Person, maxCaracteres = 60
            )
            CampoTexto(
                valor = ui.nombreEmprendimiento, alCambiar = { v -> vm.editar("emprendimiento") { it.copy(nombreEmprendimiento = v) } },
                etiqueta = "Nombre del emprendimiento", error = e["emprendimiento"], disparador = ui.intentos,
                icono = Icons.Filled.Storefront, maxCaracteres = 60
            )
            CampoTexto(
                valor = ui.telefono, alCambiar = { v -> vm.editar("telefono") { it.copy(telefono = v) } },
                etiqueta = "Teléfono (WhatsApp)", error = e["telefono"], disparador = ui.intentos,
                icono = Icons.Filled.Phone, teclado = TecladoTelefono, prefijo = "+56 ", maxCaracteres = 12
            )
            CampoTexto(
                valor = ui.instagram, alCambiar = { v -> vm.editar("instagram") { it.copy(instagram = v) } },
                etiqueta = "Instagram (opcional)", error = e["instagram"], disparador = ui.intentos,
                icono = Icons.Filled.PhotoCamera, ayuda = "Usuario, @usuario o el enlace"
            )
            CampoLista("Región", ChileGeo.regiones, ui.region, vm::elegirRegion, e["region"], ui.intentos, icono = Icons.Filled.Map)
            CampoLista(
                "Comuna", ChileGeo.comunasDe(ui.region), ui.comuna, { v -> vm.editar("comuna") { it.copy(comuna = v) } },
                e["comuna"], ui.intentos, icono = Icons.Filled.Place, habilitado = ui.region != null
            )
            Text("Rubros (1 o 2)", style = MaterialTheme.typography.titleSmall)
            SelectorChips(
                opciones = rubros.map { it.id }, seleccionados = ui.rubros, alAlternar = vm::alternarRubro,
                error = e["rubros"], disparador = ui.intentos,
                texto = { id -> rubros.firstOrNull { it.id == id }?.nombre.orEmpty() }, maximo = 2
            )
            MensajeError(ui.errorGeneral)
            BotonCarga("Guardar cambios", ui.guardando, vm::guardar, icono = Icons.Filled.Save)
            Spacer(Modifier.height(24.dp))
        }
    }
}
