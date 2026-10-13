package cl.textilcircular.app.ui.screens.mitienda

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.textilcircular.app.data.media.ImageStorage
import cl.textilcircular.app.data.repository.MarketplaceRepository
import cl.textilcircular.app.data.repository.SociaRepository
import cl.textilcircular.app.data.session.SessionStore
import cl.textilcircular.app.domain.model.EstadoPublicacion
import cl.textilcircular.app.domain.model.Rubro
import cl.textilcircular.app.domain.rules.Limites
import cl.textilcircular.app.domain.validation.BorradorPublicacion
import cl.textilcircular.app.domain.validation.CampoPublicacion
import cl.textilcircular.app.domain.validation.PublicacionValidator
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi

data class EditorUi(
    val borrador: BorradorPublicacion = BorradorPublicacion(),
    val estadoOriginal: EstadoPublicacion? = null,
    val errores: Map<CampoPublicacion, String> = emptyMap(),
    val intentos: Int = 0,
    val cargando: Boolean = true,
    val procesandoFoto: Boolean = false,
    val guardando: Boolean = false,
    val guardada: Boolean = false
) {
    val esNueva: Boolean get() = estadoOriginal == null
    val cuposFotos: Int get() = Limites.MAX_FOTOS_POR_PUBLICACION - borrador.fotos.size
}

/** Crear y editar publicaciones (RF07.3 a RF07.5) con fotos comprimidas (RF02.1 a RF02.4). */
@OptIn(ExperimentalCoroutinesApi::class)
class EditorViewModel(
    savedState: SavedStateHandle,
    private val session: SessionStore,
    private val repo: MarketplaceRepository,
    private val imageStorage: ImageStorage,
    socias: SociaRepository
) : ViewModel() {

    private val id: Long? = savedState.get<Long>("id")?.takeIf { it > 0 }

    private val _ui = MutableStateFlow(EditorUi())
    val ui: StateFlow<EditorUi> = _ui.asStateFlow()

    private val _mensajes = Channel<String>(Channel.BUFFERED)
    val mensajes = _mensajes.receiveAsFlow()

    /** Fotos agregadas en esta edición: si la socia sale sin guardar, se borran del dispositivo. */
    private val fotosNuevas = mutableSetOf<String>()

    /** Los rubros de la socia aparecen primero (Anexo B). */
    val rubros: StateFlow<List<Rubro>> = combine(
        repo.rubros,
        session.sociaId.filterNotNull().flatMapLatest { socias.observar(it) }
    ) { todos, socia ->
        val propios = socia?.rubroIds.orEmpty()
        todos.sortedByDescending { it.id in propios }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        viewModelScope.launch {
            if (id == null) {
                _ui.update { it.copy(cargando = false) }
                return@launch
            }
            val sociaId = session.sociaId.filterNotNull().first()
            val b = repo.cargarBorrador(id, sociaId)
            val estado = repo.detalle(id).first()?.estado
            if (b == null) {
                _mensajes.send("No puedes editar esta publicación")
                _ui.update { it.copy(cargando = false, guardada = true) }
            } else {
                _ui.update { it.copy(borrador = b, estadoOriginal = estado, cargando = false) }
            }
        }
    }

    fun editar(campo: CampoPublicacion, cambio: (BorradorPublicacion) -> BorradorPublicacion) =
        _ui.update { it.copy(borrador = cambio(it.borrador), errores = it.errores - campo) }

    /** Comprime y agrega fotos respetando el máximo por publicación (RN06). */
    fun agregarFotos(uris: List<Uri>) {
        if (uris.isEmpty()) return
        val cupos = _ui.value.cuposFotos
        if (cupos <= 0) {
            viewModelScope.launch { _mensajes.send("Máximo ${Limites.MAX_FOTOS_POR_PUBLICACION} fotos por publicación") }
            return
        }
        _ui.update { it.copy(procesandoFoto = true) }
        viewModelScope.launch {
            val rutas = uris.take(cupos).mapNotNull { uri ->
                runCatching { imageStorage.comprimirYGuardar(uri) }.getOrNull()
            }
            fotosNuevas += rutas
            _ui.update {
                it.copy(
                    procesandoFoto = false,
                    borrador = it.borrador.copy(fotos = it.borrador.fotos + rutas),
                    errores = it.errores - CampoPublicacion.FOTOS
                )
            }
            when {
                rutas.size < uris.take(cupos).size -> _mensajes.send("Alguna foto no se pudo leer")
                uris.size > cupos -> _mensajes.send("Se agregaron $cupos fotos: es el máximo permitido")
            }
        }
    }

    fun quitarFoto(ruta: String) {
        _ui.update { it.copy(borrador = it.borrador.copy(fotos = it.borrador.fotos - ruta)) }
        if (ruta in fotosNuevas) {
            fotosNuevas -= ruta
            viewModelScope.launch { imageStorage.eliminar(listOf(ruta)) }
        }
    }

    fun usarComoPortada(ruta: String) = _ui.update {
        it.copy(borrador = it.borrador.copy(fotos = listOf(ruta) + (it.borrador.fotos - ruta)))
    }

    fun guardar(publicar: Boolean) {
        val s = _ui.value
        val errores = if (publicar) PublicacionValidator.validarParaPublicar(s.borrador)
        else PublicacionValidator.validarBorrador(s.borrador)
        if (errores.isNotEmpty()) {
            _ui.update { it.copy(errores = errores, intentos = it.intentos + 1) }
            viewModelScope.launch { _mensajes.send("Revisa los campos marcados") }
            return
        }
        _ui.update { it.copy(guardando = true) }
        viewModelScope.launch {
            val sociaId = session.sociaId.filterNotNull().first()
            repo.guardar(sociaId, id, s.borrador, publicar)
                .onSuccess {
                    fotosNuevas.clear()
                    _ui.update { it.copy(guardando = false, guardada = true) }
                }
                .onFailure { e ->
                    _ui.update { it.copy(guardando = false) }
                    _mensajes.send(e.message ?: "No se pudo guardar")
                }
        }
    }

    override fun onCleared() {
        // Limpia fotos comprimidas que no llegaron a guardarse (RNF04).
        if (fotosNuevas.isNotEmpty()) {
            val pendientes = fotosNuevas.toList()
            CoroutineScope(Dispatchers.IO).launch { imageStorage.eliminar(pendientes) }
        }
    }
}
