package cl.textilcircular.app.ui.screens.perfil

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.clickable
import androidx.compose.material3.Scaffold
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import cl.textilcircular.app.data.repository.AuthRepository
import cl.textilcircular.app.data.repository.ChatRepository
import cl.textilcircular.app.data.repository.SociaRepository
import cl.textilcircular.app.data.session.SessionStore
import cl.textilcircular.app.domain.catalog.ChileGeo
import cl.textilcircular.app.domain.model.EstadoFicha
import cl.textilcircular.app.domain.model.Socia
import cl.textilcircular.app.domain.rules.ReglasMarketplace
import cl.textilcircular.app.ui.appViewModel
import cl.textilcircular.app.ui.components.Aviso
import cl.textilcircular.app.ui.components.CampoTexto
import cl.textilcircular.app.ui.components.Cargando
import cl.textilcircular.app.ui.components.TecladoPassword
import cl.textilcircular.app.ui.theme.Amarillo
import cl.textilcircular.app.ui.theme.Negro
import cl.textilcircular.app.ui.theme.TurquesaClaro
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BajaUi(val abierta: Boolean = false, val password: String = "", val error: String? = null, val intentos: Int = 0)

/** Perfil, Modo Pausa (RF01.3), "Falta completar" (RF01.11), estado de ficha (RF01.12) y baja (RF01.13). */
@OptIn(ExperimentalCoroutinesApi::class)
class PerfilViewModel(
    private val session: SessionStore,
    private val socias: SociaRepository,
    private val auth: AuthRepository,
    private val chat: ChatRepository
) : ViewModel() {

    val socia: StateFlow<Socia?> = session.sociaId.filterNotNull()
        .flatMapLatest { socias.observar(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val novedadesNoLeidas: StateFlow<Int> = chat.novedadesNoLeidas
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    private val _baja = MutableStateFlow(BajaUi())
    val baja: StateFlow<BajaUi> = _baja.asStateFlow()

    private val _mensajes = Channel<String>(Channel.BUFFERED)
    val mensajes = _mensajes.receiveAsFlow()

    private suspend fun id() = session.sociaId.filterNotNull().first()

    fun cambiarModoPausa(pausa: Boolean) = viewModelScope.launch {
        socias.cambiarModoPausa(id(), pausa)
        _mensajes.send(if (pausa) "Tu tienda quedó oculta en la vitrina" else "Tu tienda vuelve a estar visible")
    }

    fun simularValidacion() = viewModelScope.launch {
        socias.simularValidacion(id())
        _mensajes.send("¡Tu ficha fue validada! Ya apareces en la vitrina")
    }

    fun simularNovedad() = viewModelScope.launch { chat.simularNovedad() }

    fun cerrarSesion() = viewModelScope.launch { auth.cerrarSesion() }

    fun abrirBaja(abrir: Boolean) = _baja.update { BajaUi(abierta = abrir) }
    fun escribirPasswordBaja(v: String) = _baja.update { it.copy(password = v, error = null) }

    fun confirmarBaja() {
        val password = _baja.value.password
        if (password.isEmpty()) {
            _baja.update { it.copy(error = "Escribe tu contraseña para confirmar", intentos = it.intentos + 1) }
            return
        }
        viewModelScope.launch {
            auth.darseDeBaja(password)
                .onSuccess { _baja.update { BajaUi() } }
                .onFailure { e -> _baja.update { it.copy(error = e.message, intentos = it.intentos + 1) } }
        }
    }
}

@Composable
fun PerfilScreen(
    alEditar: () -> Unit,
    alNovedades: () -> Unit,
    pedirPermisoNotificaciones: () -> Unit,
    notificacionesActivas: Boolean,
    modifier: Modifier = Modifier
) {
    val vm = appViewModel { c, _ -> PerfilViewModel(c.session, c.sociaRepository, c.authRepository, c.chatRepository) }
    val socia by vm.socia.collectAsStateWithLifecycle()
    val noLeidas by vm.novedadesNoLeidas.collectAsStateWithLifecycle()
    val baja by vm.baja.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    var confirmarSalida by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { vm.mensajes.collect { snackbar.showSnackbar(it) } }

    Scaffold(snackbarHost = { SnackbarHost(snackbar) }, modifier = modifier) { padding ->
        val s = socia
        if (s == null) {
            Cargando(Modifier.padding(padding))
            return@Scaffold
        }
        Column(
            Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(64.dp).background(Amarillo, CircleShape), contentAlignment = Alignment.Center) {
                    Text(s.nombre.take(1), style = MaterialTheme.typography.headlineMedium, color = Negro)
                }
                Column(Modifier.padding(start = 12.dp).weight(1f)) {
                    Text(s.nombre, style = MaterialTheme.typography.titleLarge)
                    Text(s.nombreEmprendimiento, style = MaterialTheme.typography.bodyMedium)
                    Text("${s.comuna}, ${s.region}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            // RF01.12: estado de la ficha
            if (s.estadoFicha == EstadoFicha.PENDIENTE) {
                Aviso(
                    "Ficha pendiente de validación. Otras socias confirmarán tus datos. (Demostración: toca para simular la validación)",
                    icono = Icons.Filled.HourglassTop,
                    alTocar = { vm.simularValidacion() }
                )
            } else {
                Aviso("Ficha publicada y validada", icono = Icons.Filled.Verified, fondo = TurquesaClaro)
            }

            // RF01.11: "Falta completar", visible solo para la socia
            val faltantes = ReglasMarketplace.faltaCompletar(s)
            if (faltantes.isNotEmpty()) {
                Aviso("Falta completar: ${faltantes.joinToString(", ")}", icono = Icons.Filled.Warning, alTocar = alEditar)
            }

            HorizontalDivider()
            ListItem(
                headlineContent = { Text("Modo Pausa") },
                supportingContent = { Text("Oculta tu tienda y tus publicaciones sin borrarlas") },
                leadingContent = { Icon(Icons.Filled.PauseCircle, null) },
                trailingContent = { Switch(checked = s.modoPausa, onCheckedChange = { vm.cambiarModoPausa(it) }) }
            )
            ListItem(
                headlineContent = { Text("Editar mi perfil") },
                supportingContent = { Text("Teléfono, Instagram, ubicación y rubros") },
                leadingContent = { Icon(Icons.Filled.Edit, null) },
                modifier = Modifier.clickable(onClick = alEditar)
            )
            ListItem(
                headlineContent = { Text("Novedades de la Fundación") },
                supportingContent = { Text("Avisos, eventos y noticias") },
                leadingContent = {
                    BadgedBox(badge = { if (noLeidas > 0) Badge { Text("$noLeidas") } }) { Icon(Icons.Filled.Campaign, null) }
                },
                modifier = Modifier.clickable(onClick = alNovedades)
            )
            if (!notificacionesActivas) {
                ListItem(
                    headlineContent = { Text("Activar avisos") },
                    supportingContent = { Text("Recibe una notificación cuando una clienta te escriba") },
                    leadingContent = { Icon(Icons.Filled.NotificationsActive, null) },
                    modifier = Modifier.clickable(onClick = pedirPermisoNotificaciones)
                )
            }
            HorizontalDivider()
            Text("Datos privados (no se publican)", style = MaterialTheme.typography.titleSmall)
            Text("RUT: ${s.rut}\nCorreo: ${s.email}", style = MaterialTheme.typography.bodyMedium)
            HorizontalDivider()
            ListItem(
                headlineContent = { Text("Cerrar sesión") },
                leadingContent = { Icon(Icons.AutoMirrored.Filled.Logout, null) },
                modifier = Modifier.clickable { confirmarSalida = true }
            )
            ListItem(
                headlineContent = { Text("Darme de baja", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold) },
                supportingContent = { Text("Elimina tu cuenta, tu tienda y tus fotos") },
                leadingContent = { Icon(Icons.Filled.DeleteForever, null, tint = MaterialTheme.colorScheme.error) },
                modifier = Modifier.clickable { vm.abrirBaja(true) }
            )
            TextButton(onClick = { vm.simularNovedad() }) { Text("Demostración: simular una novedad de la Fundación") }
            Spacer(Modifier.height(24.dp))
        }
    }

    if (confirmarSalida) {
        AlertDialog(
            onDismissRequest = { confirmarSalida = false },
            title = { Text("¿Cerrar sesión?") },
            text = { Text("Tus publicaciones siguen guardadas. Podrás volver a ingresar con tu correo y contraseña.") },
            confirmButton = { TextButton(onClick = { confirmarSalida = false; vm.cerrarSesion() }) { Text("Cerrar sesión") } },
            dismissButton = { TextButton(onClick = { confirmarSalida = false }) { Text("Cancelar") } }
        )
    }

    // RF01.13 / RN15: baja con confirmación de contraseña
    if (baja.abierta) {
        AlertDialog(
            onDismissRequest = { vm.abrirBaja(false) },
            icon = { Icon(Icons.Filled.DeleteForever, null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Darme de baja") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Se borrarán tus datos personales, tu tienda, tus publicaciones, fotos y mensajes. Esta acción no se puede deshacer.")
                    Text("Si solo quieres dejar de aparecer un tiempo, usa el Modo Pausa.", style = MaterialTheme.typography.bodySmall)
                    CampoTexto(
                        valor = baja.password, alCambiar = vm::escribirPasswordBaja, etiqueta = "Tu contraseña",
                        error = baja.error, disparador = baja.intentos, teclado = TecladoPassword, esPassword = true
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = vm::confirmarBaja) { Text("Eliminar mi cuenta", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { vm.abrirBaja(false) }) { Text("Cancelar") } }
        )
    }
}

/** Región de la socia válida para precargar el formulario de edición. */
fun regionValida(region: String?): String? = region?.takeIf { it in ChileGeo.regiones }
