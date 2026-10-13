package cl.textilcircular.app.ui.screens.mensajes

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import cl.textilcircular.app.data.repository.ChatRepository
import cl.textilcircular.app.domain.model.Conversacion
import cl.textilcircular.app.domain.model.Mensaje
import cl.textilcircular.app.ui.appViewModel
import cl.textilcircular.app.ui.components.MensajeError
import cl.textilcircular.app.ui.components.PantallaSecundaria
import cl.textilcircular.app.ui.components.sacudirSiError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ChatUi(val texto: String = "", val error: String? = null, val intentos: Int = 0)

/** Conversación de texto simple (RF04.2, RN09). */
class ChatViewModel(savedState: SavedStateHandle, private val chat: ChatRepository) : ViewModel() {
    private val id: Long = checkNotNull(savedState["id"])

    val conversacion: StateFlow<Conversacion?> = chat.conversacion(id)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /** Al llegar mensajes con la conversación abierta, se marcan como leídos. */
    val mensajes: StateFlow<List<Mensaje>> = chat.mensajes(id)
        .onEach { chat.marcarLeida(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _ui = MutableStateFlow(ChatUi())
    val ui: StateFlow<ChatUi> = _ui.asStateFlow()

    fun escribir(t: String) = _ui.update { it.copy(texto = t, error = null) }

    fun usarPlantilla(t: String) = _ui.update { it.copy(texto = t, error = null) }

    fun enviar() {
        val texto = _ui.value.texto
        viewModelScope.launch {
            chat.responder(id, texto)
                .onSuccess { _ui.update { it.copy(texto = "") } }
                .onFailure { e -> _ui.update { it.copy(error = e.message, intentos = it.intentos + 1) } }
        }
    }
}

private val plantillas = listOf(
    "¡Hola! Gracias por escribir.",
    "Sí, está disponible.",
    "Te lo puedo tener en 3 días.",
    "¿Te acomoda coordinar la entrega?"
)

@Composable
fun ChatScreen(alVolver: () -> Unit, alVerPublicacion: (Long) -> Unit) {
    val vm = appViewModel { c, s -> ChatViewModel(s, c.chatRepository) }
    val conversacion by vm.conversacion.collectAsStateWithLifecycle()
    val mensajes by vm.mensajes.collectAsStateWithLifecycle()
    val ui by vm.ui.collectAsStateWithLifecycle()
    val lista = rememberLazyListState()

    LaunchedEffect(mensajes.size) { if (mensajes.isNotEmpty()) lista.animateScrollToItem(mensajes.size - 1) }

    PantallaSecundaria(
        titulo = conversacion?.clienteNombre ?: "Conversación",
        alVolver = alVolver,
        barraInferior = {
            Surface(tonalElevation = 3.dp) {
                Column(Modifier.navigationBarsPadding().imePadding().padding(horizontal = 12.dp, vertical = 8.dp)) {
                    // Respuestas rápidas (RF04.4)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                        plantillas.take(2).forEach { p ->
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                modifier = Modifier.clickable { vm.usarPlantilla(p) }
                            ) {
                                Text(p, style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp))
                            }
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 6.dp)) {
                        OutlinedTextField(
                            value = ui.texto,
                            onValueChange = { if (it.length <= 500) vm.escribir(it) },
                            placeholder = { Text("Escribe tu respuesta") },
                            isError = ui.error != null,
                            maxLines = 4,
                            modifier = Modifier.weight(1f).sacudirSiError(ui.error, ui.intentos)
                        )
                        FilledIconButton(onClick = vm::enviar, modifier = Modifier.padding(start = 8.dp)) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Enviar")
                        }
                    }
                    MensajeError(ui.error)
                }
            }
        }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            // RF04.6: referencia al producto desde el que escribió la clienta
            conversacion?.publicacionTitulo?.let { titulo ->
                val pubId = conversacion?.publicacionId
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.fillMaxWidth().then(
                        if (pubId != null) Modifier.clickable { alVerPublicacion(pubId) } else Modifier
                    )
                ) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Sell, contentDescription = null)
                        Text(
                            if (pubId != null) "  Consulta sobre: $titulo" else "  Consulta sobre: $titulo (publicación eliminada)",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
            LazyColumn(
                state = lista,
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(mensajes, key = { it.id }) { m -> Burbuja(m) }
            }
        }
    }
}

/** Burbuja con animación de entrada al enviar o recibir un mensaje (RNF07). */
@Composable
private fun Burbuja(m: Mensaje) {
    val entrada = remember(m.id) { Animatable(0f) }
    LaunchedEffect(m.id) { entrada.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow)) }
    Box(Modifier.fillMaxWidth(), contentAlignment = if (m.deSocia) Alignment.CenterEnd else Alignment.CenterStart) {
        Surface(
            color = if (m.deSocia) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (m.deSocia) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
            shape = RoundedCornerShape(
                topStart = 16.dp, topEnd = 16.dp,
                bottomStart = if (m.deSocia) 16.dp else 4.dp,
                bottomEnd = if (m.deSocia) 4.dp else 16.dp
            ),
            modifier = Modifier
                .widthIn(max = 300.dp)
                .graphicsLayer {
                    alpha = entrada.value.coerceIn(0f, 1f)
                    translationY = (1f - entrada.value) * 40f
                    val escala = 0.85f + 0.15f * entrada.value
                    scaleX = escala
                    scaleY = escala
                    transformOrigin = androidx.compose.ui.graphics.TransformOrigin(if (m.deSocia) 1f else 0f, 1f)
                }
        ) {
            Column(Modifier.padding(horizontal = 14.dp, vertical = 8.dp)) {
                Text(m.texto, style = MaterialTheme.typography.bodyLarge)
                Text(
                    horaCorta(m.enviadoEn),
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}
