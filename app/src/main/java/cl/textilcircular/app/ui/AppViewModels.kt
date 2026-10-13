package cl.textilcircular.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import cl.textilcircular.app.AppContainer
import cl.textilcircular.app.TextilApp
import cl.textilcircular.app.data.repository.AuthRepository
import cl.textilcircular.app.data.repository.ChatRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Crea un ViewModel con acceso al contenedor de dependencias y a los argumentos de navegación.
 * Mantiene la UI desacoplada de cómo se construyen los repositorios (MVVM + Repository).
 */
@Composable
inline fun <reified VM : ViewModel> appViewModel(
    key: String? = null,
    crossinline crear: (AppContainer, SavedStateHandle) -> VM
): VM {
    val app = LocalContext.current.applicationContext as TextilApp
    return viewModel(
        key = key,
        factory = viewModelFactory { initializer { crear(app.container, createSavedStateHandle()) } }
    )
}

sealed interface EstadoSesion {
    data object Cargando : EstadoSesion
    data object Invitada : EstadoSesion
    data class Socia(val id: Long) : EstadoSesion
}

/** Estado de sesión global: decide qué ve la barra inferior y las secciones protegidas. */
@OptIn(ExperimentalCoroutinesApi::class)
class SesionViewModel(
    private val auth: AuthRepository,
    chat: ChatRepository
) : ViewModel() {

    val estado: StateFlow<EstadoSesion> = auth.sociaId
        .map { id -> if (id == null) EstadoSesion.Invitada else EstadoSesion.Socia(id) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, EstadoSesion.Cargando)

    /** Insignia de no leídos en la pestaña Mensajes (RF04.2). */
    val noLeidos: StateFlow<Int> = auth.sociaId
        .flatMapLatest { id -> if (id == null) flowOf(0) else chat.noLeidos(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    fun cerrarSesion() {
        viewModelScope.launch { auth.cerrarSesion() }
    }
}
