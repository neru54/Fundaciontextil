package cl.textilcircular.app.ui.screens.mensajes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.NotificationAdd
import androidx.compose.material3.Badge
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.clickable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import cl.textilcircular.app.data.repository.ChatRepository
import cl.textilcircular.app.data.session.SessionStore
import cl.textilcircular.app.domain.model.Conversacion
import cl.textilcircular.app.ui.appViewModel
import cl.textilcircular.app.ui.components.Cargando
import cl.textilcircular.app.ui.components.EstadoVacio
import cl.textilcircular.app.ui.theme.Amarillo
import cl.textilcircular.app.ui.theme.Negro
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Lista de conversaciones con indicador de no leídos (RF04.2). */
@OptIn(ExperimentalCoroutinesApi::class)
class MensajesViewModel(private val session: SessionStore, private val chat: ChatRepository) : ViewModel() {
    val conversaciones: StateFlow<List<Conversacion>?> = session.sociaId.filterNotNull()
        .flatMapLatest { chat.conversaciones(it) }
        .map<List<Conversacion>, List<Conversacion>?> { it }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /** Sección 1.1 del ERS: sin servidor, simula una consulta entrante y su notificación. */
    fun simularConsulta() {
        viewModelScope.launch { chat.simularConsulta(session.sociaId.filterNotNull().first()) }
    }
}

@Composable
fun MensajesScreen(alAbrirChat: (Long) -> Unit, modifier: Modifier = Modifier) {
    val vm = appViewModel { c, _ -> MensajesViewModel(c.session, c.chatRepository) }
    val conversaciones by vm.conversaciones.collectAsStateWithLifecycle()

    Column(modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(start = 16.dp, end = 8.dp, top = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("Mensajes", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.weight(1f))
            TextButton(onClick = vm::simularConsulta) {
                Icon(Icons.Filled.NotificationAdd, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("Simular consulta")
            }
        }
        val lista = conversaciones
        when {
            lista == null -> Cargando()
            lista.isEmpty() -> EstadoVacio(
                Icons.Filled.Forum, "Aún no tienes mensajes",
                "Cuando una clienta te escriba desde la vitrina, verás aquí la conversación."
            )
            else -> LazyColumn(contentPadding = PaddingValues(vertical = 8.dp)) {
                items(lista, key = { it.id }) { c ->
                    FilaConversacion(c, { alAbrirChat(c.id) }, Modifier.animateItem())
                    HorizontalDivider(Modifier.padding(start = 72.dp))
                }
            }
        }
    }
}

@Composable
private fun FilaConversacion(c: Conversacion, alTocar: () -> Unit, modifier: Modifier = Modifier) {
    val noLeida = c.noLeidos > 0
    ListItem(
        modifier = modifier.clickable(onClick = alTocar),
        leadingContent = {
            Box(Modifier.size(44.dp).clip(CircleShape).background(Amarillo), contentAlignment = Alignment.Center) {
                Text(c.clienteNombre.take(1).uppercase(), color = Negro, style = MaterialTheme.typography.titleMedium)
            }
        },
        headlineContent = {
            Text(c.clienteNombre, fontWeight = if (noLeida) FontWeight.ExtraBold else FontWeight.Normal)
        },
        supportingContent = {
            Column {
                c.publicacionTitulo?.let {
                    Text("Sobre: $it", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Text(
                    c.ultimoMensaje, maxLines = 1, overflow = TextOverflow.Ellipsis,
                    fontWeight = if (noLeida) FontWeight.Bold else FontWeight.Normal
                )
            }
        },
        trailingContent = {
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(horaCorta(c.actualizadaEn), style = MaterialTheme.typography.labelSmall)
                if (noLeida) Badge { Text("${c.noLeidos}") }
            }
        }
    )
}

fun horaCorta(ms: Long): String {
    val hoy = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
    val dia = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date(ms))
    val formato = if (hoy == dia) "HH:mm" else "dd/MM"
    return SimpleDateFormat(formato, Locale.getDefault()).format(Date(ms))
}
