package cl.textilcircular.app.ui.screens.mitienda

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.textilcircular.app.data.repository.MarketplaceRepository
import cl.textilcircular.app.data.repository.SociaRepository
import cl.textilcircular.app.data.session.SessionStore
import cl.textilcircular.app.domain.model.EstadoPublicacion
import cl.textilcircular.app.domain.model.Publicacion
import cl.textilcircular.app.domain.model.ResumenTienda
import cl.textilcircular.app.domain.model.Socia
import cl.textilcircular.app.domain.rules.AccionEstado
import cl.textilcircular.app.domain.rules.Limites
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class MiTiendaUi(
    val socia: Socia? = null,
    val publicaciones: List<Publicacion> = emptyList(),
    val filtro: EstadoPublicacion? = null,
    val resumen: ResumenTienda = ResumenTienda(),
    val cargando: Boolean = true
) {
    val visibles: List<Publicacion> get() = if (filtro == null) publicaciones else publicaciones.filter { it.estado == filtro }
    val puedeCrear: Boolean get() = publicaciones.size < Limites.MAX_PUBLICACIONES_POR_SOCIA
}

/** Apartado de la socia "Mi tienda" (RF07.1, RF07.2, RF07.6 a RF07.8, RF07.10). */
@OptIn(ExperimentalCoroutinesApi::class)
class MiTiendaViewModel(
    private val session: SessionStore,
    private val repo: MarketplaceRepository,
    socias: SociaRepository
) : ViewModel() {

    private val filtro = MutableStateFlow<EstadoPublicacion?>(null)
    private val sociaId: Flow<Long> = session.sociaId.filterNotNull()

    private val _mensajes = Channel<String>(Channel.BUFFERED)
    /** Mensajes de una sola vez para el snackbar. */
    val mensajes = _mensajes.receiveAsFlow()

    val ui: StateFlow<MiTiendaUi> = combine(
        sociaId.flatMapLatest { socias.observar(it) },
        sociaId.flatMapLatest { repo.misPublicaciones(it) },
        filtro
    ) { socia, pubs, f ->
        MiTiendaUi(
            socia = socia,
            publicaciones = pubs,
            filtro = f,
            resumen = ResumenTienda(
                publicadas = pubs.count { it.estado == EstadoPublicacion.PUBLICADA },
                pausadas = pubs.count { it.estado == EstadoPublicacion.PAUSADA },
                borradores = pubs.count { it.estado == EstadoPublicacion.BORRADOR },
                agotadas = pubs.count { it.estado == EstadoPublicacion.AGOTADA },
                vistas = pubs.sumOf { it.vistas },
                consultas = pubs.sumOf { it.consultas }
            ),
            cargando = false
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MiTiendaUi())

    fun filtrar(estado: EstadoPublicacion?) {
        filtro.value = estado
    }

    fun cambiarEstado(p: Publicacion, accion: AccionEstado) = conSocia { id ->
        repo.cambiarEstado(id, p.id, accion)
            .onSuccess { _mensajes.send("\"${p.titulo}\": ${accion.destino.etiqueta.lowercase()}") }
            .onFailure { _mensajes.send(it.message ?: "No se pudo cambiar el estado") }
    }

    fun eliminar(p: Publicacion) = conSocia { id ->
        repo.eliminar(id, p.id)
            .onSuccess { _mensajes.send("Publicación eliminada") }
            .onFailure { _mensajes.send(it.message ?: "No se pudo eliminar") }
    }

    fun duplicar(p: Publicacion) = conSocia { id ->
        repo.duplicar(id, p.id)
            .onSuccess { _mensajes.send("Se creó una copia como borrador") }
            .onFailure { _mensajes.send(it.message ?: "No se pudo duplicar") }
    }

    private fun conSocia(accion: suspend (Long) -> Unit) {
        viewModelScope.launch { accion(sociaId.first()) }
    }
}
