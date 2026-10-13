package cl.textilcircular.app.ui.screens.mitienda

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.RemoveShoppingCart
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.textilcircular.app.domain.model.EstadoFicha
import cl.textilcircular.app.domain.model.EstadoPublicacion
import cl.textilcircular.app.domain.model.Publicacion
import cl.textilcircular.app.domain.rules.AccionEstado
import cl.textilcircular.app.domain.rules.Limites
import cl.textilcircular.app.domain.rules.ReglasMarketplace
import cl.textilcircular.app.ui.appViewModel
import cl.textilcircular.app.ui.components.Aviso
import cl.textilcircular.app.ui.components.Cargando
import cl.textilcircular.app.ui.components.EstadoVacio
import cl.textilcircular.app.ui.components.EtiquetaEstado
import cl.textilcircular.app.ui.components.FotoPublicacion
import cl.textilcircular.app.ui.components.TextoPrecio
import androidx.compose.material3.Scaffold

@Composable
fun MiTiendaScreen(
    alNueva: () -> Unit,
    alEditar: (Long) -> Unit,
    alVerPublicacion: (Long) -> Unit,
    alVistaPrevia: (Long) -> Unit,
    alIrPerfil: () -> Unit
) {
    val vm = appViewModel { c, _ -> MiTiendaViewModel(c.session, c.marketplaceRepository, c.sociaRepository) }
    val ui by vm.ui.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    var porEliminar by remember { mutableStateOf<Publicacion?>(null) }

    LaunchedEffect(Unit) { vm.mensajes.collect { snackbar.showSnackbar(it) } }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        floatingActionButton = {
            if (ui.puedeCrear) {
                ExtendedFloatingActionButton(
                    onClick = alNueva,
                    icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                    text = { Text("Nueva publicación") }
                )
            }
        }
    ) { padding ->
        if (ui.cargando) {
            Cargando(Modifier.padding(padding))
            return@Scaffold
        }
        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(padding).fillMaxSize()
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Mi tienda", style = MaterialTheme.typography.headlineSmall)
                        Text(
                            ui.socia?.nombreEmprendimiento.orEmpty(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    TextButton(onClick = { ui.socia?.let { alVistaPrevia(it.id) } }) {
                        Icon(Icons.Filled.Visibility, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("Así te ven")
                    }
                }
            }
            val socia = ui.socia
            if (socia?.estadoFicha == EstadoFicha.PENDIENTE) {
                item {
                    Aviso("Tu ficha está pendiente de validación. Puedes preparar tus publicaciones; aparecerán en la vitrina cuando te validen.", alTocar = alIrPerfil)
                }
            } else if (socia?.modoPausa == true) {
                item {
                    Aviso("Modo Pausa activo: tu tienda está oculta en la vitrina. Desactívalo en tu perfil.", alTocar = alIrPerfil)
                }
            }
            item { TarjetasResumen(ui) }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val opciones = listOf<EstadoPublicacion?>(null) + EstadoPublicacion.entries
                    items(opciones) { estado ->
                        val cantidad = if (estado == null) ui.publicaciones.size else ui.publicaciones.count { it.estado == estado }
                        FilterChip(
                            selected = ui.filtro == estado,
                            onClick = { vm.filtrar(estado) },
                            label = { Text("${estado?.let { plural(it) } ?: "Todas"} ($cantidad)") }
                        )
                    }
                }
            }
            if (ui.visibles.isEmpty()) {
                item {
                    EstadoVacio(
                        Icons.Filled.Inventory2,
                        if (ui.publicaciones.isEmpty()) "Aún no tienes publicaciones" else "No hay publicaciones en este estado",
                        if (ui.publicaciones.isEmpty()) "Sube una foto de tu trabajo y cuéntale a las clientas qué ofreces." else "Prueba con otro filtro.",
                        accion = if (ui.publicaciones.isEmpty()) "Crear mi primera publicación" else null,
                        alAccionar = alNueva
                    )
                }
            }
            items(ui.visibles, key = { it.id }) { p ->
                FilaPublicacion(
                    p = p,
                    alAbrir = { alVerPublicacion(p.id) },
                    alEditar = { alEditar(p.id) },
                    alAccion = { vm.cambiarEstado(p, it) },
                    alDuplicar = { vm.duplicar(p) },
                    alEliminar = { porEliminar = p },
                    modifier = Modifier.animateItem()
                )
            }
            if (!ui.puedeCrear) {
                item { Aviso("Llegaste al máximo de ${Limites.MAX_PUBLICACIONES_POR_SOCIA} publicaciones. Elimina alguna para crear otra.") }
            }
        }
    }

    // RF07.7: confirmación antes de eliminar
    porEliminar?.let { p ->
        AlertDialog(
            onDismissRequest = { porEliminar = null },
            icon = { Icon(Icons.Filled.Delete, contentDescription = null) },
            title = { Text("¿Eliminar publicación?") },
            text = { Text("\"${p.titulo}\" y sus fotos se borrarán de forma permanente. Si solo quieres ocultarla, usa Pausar.") },
            confirmButton = {
                TextButton(onClick = { vm.eliminar(p); porEliminar = null }) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { porEliminar = null }) { Text("Cancelar") } }
        )
    }
}

private fun plural(e: EstadoPublicacion) = when (e) {
    EstadoPublicacion.BORRADOR -> "Borradores"
    EstadoPublicacion.PUBLICADA -> "Publicadas"
    EstadoPublicacion.PAUSADA -> "Pausadas"
    EstadoPublicacion.AGOTADA -> "Agotadas"
}

@Composable
private fun TarjetasResumen(ui: MiTiendaUi) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        TarjetaDato("Publicadas", ui.resumen.publicadas.toString(), Icons.Filled.PlayCircle, Modifier.weight(1f))
        TarjetaDato("Vistas", ui.resumen.vistas.toString(), Icons.Filled.Visibility, Modifier.weight(1f))
        TarjetaDato("Consultas", ui.resumen.consultas.toString(), Icons.AutoMirrored.Filled.Chat, Modifier.weight(1f))
    }
}

@Composable
private fun TarjetaDato(etiqueta: String, valor: String, icono: ImageVector, modifier: Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(Modifier.padding(12.dp)) {
            Icon(icono, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Text(valor, style = MaterialTheme.typography.headlineSmall)
            Text(etiqueta, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun FilaPublicacion(
    p: Publicacion,
    alAbrir: () -> Unit,
    alEditar: () -> Unit,
    alAccion: (AccionEstado) -> Unit,
    alDuplicar: () -> Unit,
    alEliminar: () -> Unit,
    modifier: Modifier = Modifier
) {
    var menu by remember { mutableStateOf(false) }
    Card(onClick = alAbrir, modifier = modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(1.dp)) {
        Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
            FotoPublicacion(p.portada, p.titulo, Modifier.size(84.dp).clip(MaterialTheme.shapes.small))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                EtiquetaEstado(p.estado)
                Text(p.titulo, style = MaterialTheme.typography.titleSmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
                TextoPrecio(p.precio)
                Text(
                    "${p.vistas} vistas · ${p.consultas} consultas",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Box {
                IconButton(onClick = { menu = true }) { Icon(Icons.Filled.MoreVert, contentDescription = "Opciones de ${p.titulo}") }
                DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                    DropdownMenuItem(
                        text = { Text("Editar") },
                        leadingIcon = { Icon(Icons.Filled.Edit, null) },
                        onClick = { menu = false; alEditar() }
                    )
                    ReglasMarketplace.accionesDisponibles(p.estado).forEach { accion ->
                        DropdownMenuItem(
                            text = { Text(accion.etiqueta) },
                            leadingIcon = { Icon(iconoAccion(accion), null) },
                            onClick = { menu = false; alAccion(accion) }
                        )
                    }
                    DropdownMenuItem(
                        text = { Text("Duplicar") },
                        leadingIcon = { Icon(Icons.Filled.ContentCopy, null) },
                        onClick = { menu = false; alDuplicar() }
                    )
                    HorizontalDivider()
                    DropdownMenuItem(
                        text = { Text("Eliminar", color = MaterialTheme.colorScheme.error) },
                        leadingIcon = { Icon(Icons.Filled.Delete, null, tint = MaterialTheme.colorScheme.error) },
                        onClick = { menu = false; alEliminar() }
                    )
                }
            }
        }
    }
}

private fun iconoAccion(a: AccionEstado): ImageVector = when (a) {
    AccionEstado.PUBLICAR, AccionEstado.REACTIVAR -> Icons.Filled.PlayCircle
    AccionEstado.PAUSAR -> Icons.Filled.PauseCircle
    AccionEstado.MARCAR_AGOTADA -> Icons.Filled.RemoveShoppingCart
}
