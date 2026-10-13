package cl.textilcircular.app.ui.screens.vitrina

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import cl.textilcircular.app.data.repository.ChatRepository
import cl.textilcircular.app.data.repository.MarketplaceRepository
import cl.textilcircular.app.domain.model.Clienta
import cl.textilcircular.app.domain.validation.Validators
import cl.textilcircular.app.ui.appViewModel
import cl.textilcircular.app.ui.components.Aviso
import cl.textilcircular.app.ui.components.BotonCarga
import cl.textilcircular.app.ui.components.CampoTexto
import cl.textilcircular.app.ui.components.ColumnaFormulario
import cl.textilcircular.app.ui.components.MensajeError
import cl.textilcircular.app.ui.components.PantallaSecundaria
import cl.textilcircular.app.ui.components.TecladoEmail
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ConsultaUiState(
    val nombre: String = "",
    val email: String = "",
    val mensaje: String = "",
    val tituloPublicacion: String? = null,
    val registrada: Boolean = false,
    val errores: Map<String, String> = emptyMap(),
    val errorGeneral: String? = null,
    val intentos: Int = 0,
    val cargando: Boolean = false,
    val enviada: Boolean = false
)

/**
 * RF06.6: registro simple de la clienta (nombre y correo) y primer mensaje con el texto sugerido
 * y la referencia a la publicación (RF04.6, RN16).
 */
class ConsultaViewModel(
    savedState: SavedStateHandle,
    private val chat: ChatRepository,
    repo: MarketplaceRepository
) : ViewModel() {
    private val sociaId: Long = checkNotNull(savedState["sociaId"])
    private val publicacionId: Long? = savedState.get<Long>("publicacionId")?.takeIf { it > 0 }

    private val _ui = MutableStateFlow(ConsultaUiState())
    val ui: StateFlow<ConsultaUiState> = _ui.asStateFlow()

    init {
        viewModelScope.launch {
            val clienta = chat.clienta.first()
            val titulo = publicacionId?.let { repo.detalle(it).first()?.titulo }
            _ui.update {
                it.copy(
                    nombre = clienta?.nombre.orEmpty(),
                    email = clienta?.email.orEmpty(),
                    registrada = clienta != null,
                    tituloPublicacion = titulo,
                    mensaje = if (titulo != null) "Hola, quiero saber más sobre: $titulo" else "Hola, me interesa tu trabajo."
                )
            }
        }
    }

    fun editar(campo: String, cambio: (ConsultaUiState) -> ConsultaUiState) =
        _ui.update { cambio(it).copy(errores = it.errores - campo, errorGeneral = null) }

    fun enviar() {
        val s = _ui.value
        val errores = buildMap {
            Validators.nombreClienta(s.nombre)?.let { put("nombre", it) }
            Validators.email(s.email)?.let { put("email", it) }
            Validators.mensaje(s.mensaje)?.let { put("mensaje", it) }
        }
        if (errores.isNotEmpty()) {
            _ui.update { it.copy(errores = errores, intentos = it.intentos + 1) }
            return
        }
        _ui.update { it.copy(cargando = true) }
        viewModelScope.launch {
            chat.enviarConsulta(Clienta(s.nombre, s.email), sociaId, publicacionId, s.mensaje)
                .onSuccess { _ui.update { it.copy(cargando = false, enviada = true) } }
                .onFailure { e -> _ui.update { it.copy(cargando = false, errorGeneral = e.message) } }
        }
    }
}

@Composable
fun ConsultaScreen(alVolver: () -> Unit) {
    val vm = appViewModel { c, s -> ConsultaViewModel(s, c.chatRepository, c.marketplaceRepository) }
    val ui by vm.ui.collectAsStateWithLifecycle()

    PantallaSecundaria(titulo = "Consultar a la socia", alVolver = alVolver) { padding ->
        AnimatedContent(
            targetState = ui.enviada,
            transitionSpec = {
                (fadeIn() + scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy), initialScale = 0.7f)) togetherWith fadeOut()
            },
            label = "consultaEnviada",
            modifier = Modifier.padding(padding)
        ) { enviada ->
            if (enviada) {
                Column(
                    Modifier.fillMaxSize().padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(96.dp))
                    Spacer(Modifier.height(16.dp))
                    Text("¡Mensaje enviado!", style = MaterialTheme.typography.headlineSmall)
                    Text(
                        "La socia recibió un aviso y te responderá pronto a ${ui.email}.",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(Modifier.height(24.dp))
                    Button(onClick = alVolver) { Text("Seguir explorando") }
                }
            } else {
                ColumnaFormulario(Modifier.imePadding().verticalScroll(rememberScrollState())) {
                    Spacer(Modifier.height(8.dp))
                    ui.tituloPublicacion?.let { Aviso("Consulta sobre: $it") }
                    Text(
                        if (ui.registrada) "Te reconocimos. Revisa tus datos y escribe tu mensaje."
                        else "Para escribirle a la socia, cuéntanos quién eres. Solo te lo pediremos una vez.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    CampoTexto(
                        valor = ui.nombre, alCambiar = { v -> vm.editar("nombre") { it.copy(nombre = v) } },
                        etiqueta = "Tu nombre", error = ui.errores["nombre"], disparador = ui.intentos,
                        icono = Icons.Filled.Person, maxCaracteres = 60
                    )
                    CampoTexto(
                        valor = ui.email, alCambiar = { v -> vm.editar("email") { it.copy(email = v) } },
                        etiqueta = "Tu correo", error = ui.errores["email"], disparador = ui.intentos,
                        icono = Icons.Filled.Email, teclado = TecladoEmail, ayuda = "No se muestra en la vitrina"
                    )
                    CampoTexto(
                        valor = ui.mensaje, alCambiar = { v -> vm.editar("mensaje") { it.copy(mensaje = v) } },
                        etiqueta = "Mensaje", error = ui.errores["mensaje"], disparador = ui.intentos,
                        lineas = 4, maxCaracteres = 500, mostrarOk = false
                    )
                    MensajeError(ui.errorGeneral)
                    BotonCarga("Enviar mensaje", ui.cargando, vm::enviar, icono = Icons.AutoMirrored.Filled.Send)
                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }
}
