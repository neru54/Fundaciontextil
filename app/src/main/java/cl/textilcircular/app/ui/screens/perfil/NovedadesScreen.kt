package cl.textilcircular.app.ui.screens.perfil

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import cl.textilcircular.app.data.repository.ChatRepository
import cl.textilcircular.app.domain.model.Novedad
import cl.textilcircular.app.ui.appViewModel
import cl.textilcircular.app.ui.components.EstadoVacio
import cl.textilcircular.app.ui.components.PantallaSecundaria
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Sección Novedades de la Fundación (RF04.5). */
class NovedadesViewModel(private val chat: ChatRepository) : ViewModel() {
    val novedades: StateFlow<List<Novedad>> = chat.novedades
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Novedades que no estaban leídas al abrir la sección; conservan la etiqueta "Nueva" mientras se ve. */
    private val _nuevas = MutableStateFlow<Set<Long>>(emptySet())
    val nuevas: StateFlow<Set<Long>> = _nuevas.asStateFlow()

    init {
        viewModelScope.launch {
            _nuevas.value = chat.novedades.first().filterNot { it.leida }.map { it.id }.toSet()
            chat.marcarNovedadesLeidas()
        }
    }
}

@Composable
fun NovedadesScreen(alVolver: () -> Unit) {
    val vm = appViewModel { c, _ -> NovedadesViewModel(c.chatRepository) }
    val novedades by vm.novedades.collectAsStateWithLifecycle()
    val nuevas by vm.nuevas.collectAsStateWithLifecycle()

    PantallaSecundaria(titulo = "Novedades", alVolver = alVolver) { padding ->
        if (novedades.isEmpty()) {
            EstadoVacio(Icons.Filled.Campaign, "Sin novedades", "Aquí verás los avisos de la Fundación.", Modifier.padding(padding))
            return@PantallaSecundaria
        }
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(padding)
        ) {
            items(novedades, key = { it.id }) { n ->
                Card(
                    modifier = Modifier.fillMaxWidth().animateItem(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (n.id !in nuevas) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(n.titulo, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                            if (n.id in nuevas) Badge { Text("Nueva") }
                        }
                        Text(SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(n.fecha)), style = MaterialTheme.typography.labelSmall)
                        Text(n.cuerpo, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}
