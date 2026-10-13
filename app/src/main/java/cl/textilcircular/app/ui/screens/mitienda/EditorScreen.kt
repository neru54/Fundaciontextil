package cl.textilcircular.app.ui.screens.mitienda

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Publish
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.textilcircular.app.TextilApp
import cl.textilcircular.app.domain.model.Disponibilidad
import cl.textilcircular.app.domain.model.EstadoPublicacion
import cl.textilcircular.app.domain.model.TipoPublicacion
import cl.textilcircular.app.domain.rules.Limites
import cl.textilcircular.app.domain.validation.CampoPublicacion
import cl.textilcircular.app.ui.appViewModel
import cl.textilcircular.app.ui.components.BotonCarga
import cl.textilcircular.app.ui.components.CampoLista
import cl.textilcircular.app.ui.components.CampoTexto
import cl.textilcircular.app.ui.components.Cargando
import cl.textilcircular.app.ui.components.CasillaVerificacion
import cl.textilcircular.app.ui.components.ColumnaFormulario
import cl.textilcircular.app.ui.components.FotoPublicacion
import cl.textilcircular.app.ui.components.MensajeError
import cl.textilcircular.app.ui.components.PantallaSecundaria
import cl.textilcircular.app.ui.components.TecladoNumero
import cl.textilcircular.app.ui.components.sacudirSiError
import kotlinx.coroutines.launch

@Composable
fun EditorScreen(alVolver: () -> Unit) {
    val vm = appViewModel { c, s ->
        EditorViewModel(s, c.session, c.marketplaceRepository, c.imageStorage, c.sociaRepository)
    }
    val ui by vm.ui.collectAsStateWithLifecycle()
    val rubros by vm.rubros.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val imageStorage = remember { (context.applicationContext as TextilApp).container.imageStorage }

    LaunchedEffect(Unit) { vm.mensajes.collect { snackbar.showSnackbar(it) } }
    LaunchedEffect(ui.guardada) { if (ui.guardada) alVolver() }

    // ---------------- Recurso nativo 1: cámara y galería (RF02.1) ----------------
    var uriCamara by rememberSaveable { mutableStateOf<Uri?>(null) }
    val camara = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
        val uri = uriCamara
        if (ok && uri != null) vm.agregarFotos(listOf(uri))
    }
    fun abrirCamara() {
        val uri = imageStorage.crearUriParaCamara()
        uriCamara = uri
        camara.launch(uri)
    }
    val permisoCamara = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { concedido ->
        if (concedido) abrirCamara()
        else scope.launch {
            snackbar.showSnackbar("Sin permiso de cámara. Puedes elegir fotos de la galería o activarlo en Ajustes.")
        }
    }
    val galeria = rememberLauncherForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(Limites.MAX_FOTOS_POR_PUBLICACION)
    ) { uris -> vm.agregarFotos(uris) }

    val alTocarCamara = {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) abrirCamara()
        else permisoCamara.launch(Manifest.permission.CAMERA)
    }
    val alTocarGaleria = {
        galeria.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    PantallaSecundaria(
        titulo = if (ui.esNueva) "Nueva publicación" else "Editar publicación",
        alVolver = alVolver,
        snackbar = snackbar,
        barraInferior = {
            if (!ui.cargando) {
                Surface(tonalElevation = 3.dp) {
                    Row(
                        Modifier.fillMaxWidth().navigationBarsPadding().imePadding().padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (ui.esNueva || ui.estadoOriginal == EstadoPublicacion.BORRADOR) {
                            OutlinedButton(
                                onClick = { vm.guardar(publicar = false) },
                                enabled = !ui.guardando && !ui.procesandoFoto,
                                modifier = Modifier.weight(1f).height(52.dp)
                            ) { Text("Guardar borrador") }
                        }
                        BotonCarga(
                            texto = if (ui.esNueva || ui.estadoOriginal == EstadoPublicacion.BORRADOR) "Publicar" else "Guardar cambios",
                            cargando = ui.guardando,
                            alTocar = { vm.guardar(publicar = true) },
                            icono = Icons.Filled.Publish,
                            habilitado = !ui.procesandoFoto,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    ) { padding ->
        if (ui.cargando) {
            Cargando(Modifier.padding(padding))
            return@PantallaSecundaria
        }
        val b = ui.borrador
        val e = ui.errores
        ColumnaFormulario(
            Modifier.padding(padding).fillMaxSize().imePadding().verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(4.dp))
            // Fotos
            Text("Fotos (${b.fotos.size}/${Limites.MAX_FOTOS_POR_PUBLICACION})", style = MaterialTheme.typography.titleMedium)
            Text(
                "Con una foto basta. La primera es la portada; toca otra para usarla como portada.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Column(Modifier.sacudirSiError(e[CampoPublicacion.FOTOS], ui.intentos)) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(b.fotos, key = { it }) { ruta ->
                        MiniaturaFoto(
                            ruta = ruta,
                            esPortada = ruta == b.fotos.first(),
                            alQuitar = { vm.quitarFoto(ruta) },
                            alElegir = { vm.usarComoPortada(ruta) },
                            modifier = Modifier.animateItem()
                        )
                    }
                    if (ui.cuposFotos > 0) {
                        item { BotonAgregarFoto("Cámara", Icons.Filled.PhotoCamera, !ui.procesandoFoto, alTocarCamara) }
                        item { BotonAgregarFoto("Galería", Icons.Filled.PhotoLibrary, !ui.procesandoFoto, alTocarGaleria) }
                    }
                }
                // Progreso animado mientras se comprime la foto (RF02.3, RNF07)
                AnimatedVisibility(visible = ui.procesandoFoto) {
                    Column(Modifier.padding(top = 8.dp)) {
                        LinearProgressIndicator(Modifier.fillMaxWidth())
                        Text("Optimizando foto para ahorrar datos…", style = MaterialTheme.typography.bodySmall)
                    }
                }
                MensajeError(e[CampoPublicacion.FOTOS])
            }

            // Tipo
            Text("Tipo de publicación", style = MaterialTheme.typography.titleMedium)
            Column(Modifier.sacudirSiError(e[CampoPublicacion.TIPO], ui.intentos)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TipoPublicacion.entries.forEach { tipo ->
                        FilterChip(
                            selected = b.tipo == tipo,
                            onClick = { vm.editar(CampoPublicacion.TIPO) { it.copy(tipo = tipo) } },
                            label = { Text(tipo.etiqueta) }
                        )
                    }
                }
                MensajeError(e[CampoPublicacion.TIPO])
            }

            CampoTexto(
                valor = b.titulo, alCambiar = { v -> vm.editar(CampoPublicacion.TITULO) { it.copy(titulo = v) } },
                etiqueta = "Título", error = e[CampoPublicacion.TITULO], disparador = ui.intentos,
                icono = Icons.Filled.Title, maxCaracteres = 60, ayuda = "Ejemplo: Chaqueta de mezclilla intervenida"
            )
            CampoTexto(
                valor = b.descripcion, alCambiar = { v -> vm.editar(CampoPublicacion.DESCRIPCION) { it.copy(descripcion = v) } },
                etiqueta = "Descripción", error = e[CampoPublicacion.DESCRIPCION], disparador = ui.intentos,
                icono = Icons.Filled.Description, lineas = 4, maxCaracteres = 500, mostrarOk = false
            )
            CampoLista(
                etiqueta = "Rubro", opciones = rubros, seleccion = rubros.firstOrNull { it.id == b.rubroId },
                alElegir = { r -> vm.editar(CampoPublicacion.RUBRO) { it.copy(rubroId = r.id) } },
                error = e[CampoPublicacion.RUBRO], disparador = ui.intentos, texto = { it.nombre }
            )
            CampoTexto(
                valor = b.precioTexto,
                alCambiar = { v -> vm.editar(CampoPublicacion.PRECIO) { it.copy(precioTexto = v.filter { c -> c.isDigit() || c == '.' }) } },
                etiqueta = "Precio", error = e[CampoPublicacion.PRECIO], disparador = ui.intentos,
                icono = Icons.Filled.Sell, prefijo = "$ ", teclado = TecladoNumero, habilitado = !b.aConvenir,
                maxCaracteres = 11, ayuda = if (b.aConvenir) "Se mostrará \"A convenir\"" else "En pesos, sin decimales"
            )
            CasillaVerificacion(
                marcada = b.aConvenir,
                alCambiar = { v -> vm.editar(CampoPublicacion.PRECIO) { it.copy(aConvenir = v) } },
                texto = "Precio a convenir"
            )
            CampoLista(
                etiqueta = "Disponibilidad", opciones = Disponibilidad.entries.toList(), seleccion = b.disponibilidad,
                alElegir = { d -> vm.editar(CampoPublicacion.DISPONIBILIDAD) { it.copy(disponibilidad = d) } },
                error = e[CampoPublicacion.DISPONIBILIDAD], disparador = ui.intentos, texto = { it.etiqueta }
            )
            AnimatedVisibility(visible = b.disponibilidad == Disponibilidad.A_PEDIDO) {
                CampoTexto(
                    valor = b.diasTexto,
                    alCambiar = { v -> vm.editar(CampoPublicacion.DIAS) { it.copy(diasTexto = v.filter(Char::isDigit)) } },
                    etiqueta = "Días de elaboración", error = e[CampoPublicacion.DIAS], disparador = ui.intentos,
                    icono = Icons.Filled.Schedule, teclado = TecladoNumero, maxCaracteres = 2, ayuda = "Entre 1 y 60 días"
                )
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun MiniaturaFoto(
    ruta: String,
    esPortada: Boolean,
    alQuitar: () -> Unit,
    alElegir: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier
            .size(104.dp)
            .clip(MaterialTheme.shapes.medium)
            .then(
                if (esPortada) Modifier.border(3.dp, MaterialTheme.colorScheme.secondary, MaterialTheme.shapes.medium)
                else Modifier
            )
            .clickable(onClickLabel = "Usar como portada", onClick = alElegir)
    ) {
        FotoPublicacion(ruta, "Foto de la publicación", Modifier.fillMaxSize())
        FilledIconButton(
            onClick = alQuitar,
            colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)),
            modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).size(30.dp)
        ) {
            Icon(Icons.Filled.Close, contentDescription = "Quitar foto", modifier = Modifier.size(18.dp))
        }
        if (esPortada) {
            Surface(
                color = MaterialTheme.colorScheme.secondary,
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.align(Alignment.BottomStart).padding(4.dp)
            ) {
                Text("Portada", style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
            }
        }
    }
}

@Composable
private fun BotonAgregarFoto(texto: String, icono: ImageVector, habilitado: Boolean, alTocar: () -> Unit) {
    OutlinedCard(
        onClick = alTocar,
        enabled = habilitado,
        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary),
        modifier = Modifier.size(104.dp)
    ) {
        Column(
            Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icono, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
            Text(texto, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
        }
    }
}
