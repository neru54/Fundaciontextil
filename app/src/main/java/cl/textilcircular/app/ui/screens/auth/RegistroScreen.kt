package cl.textilcircular.app.ui.screens.auth

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.textilcircular.app.domain.catalog.ChileGeo
import cl.textilcircular.app.domain.model.OpcionesFormulario
import cl.textilcircular.app.domain.model.Rubro
import cl.textilcircular.app.domain.validation.CampoRegistro
import cl.textilcircular.app.domain.validation.Instagram
import cl.textilcircular.app.domain.validation.RegistroValidator
import cl.textilcircular.app.ui.appViewModel
import cl.textilcircular.app.ui.components.BotonCarga
import cl.textilcircular.app.ui.components.CampoLista
import cl.textilcircular.app.ui.components.CampoTexto
import cl.textilcircular.app.ui.components.CasillaVerificacion
import cl.textilcircular.app.ui.components.ColumnaFormulario
import cl.textilcircular.app.ui.components.MensajeError
import cl.textilcircular.app.ui.components.PantallaSecundaria
import cl.textilcircular.app.ui.components.SelectorChips
import cl.textilcircular.app.ui.components.TecladoEmail
import cl.textilcircular.app.ui.components.TecladoPassword
import cl.textilcircular.app.ui.components.TecladoTelefono

const val TEXTO_AUTORIZACION =
    "Autorizo a la Fundación Textil Circular a compartir mis datos de contacto, a incluirme en la base " +
        "de datos del Directorio Textil Circular y a enviarme información de sus actividades."

@Composable
fun RegistroScreen(alVolver: () -> Unit, alTerminar: () -> Unit, alAbrirEnlace: (String) -> Unit) {
    val vm = appViewModel { c, _ -> RegistroViewModel(c.authRepository, c.marketplaceRepository) }
    val ui by vm.ui.collectAsStateWithLifecycle()
    val rubros by vm.rubros.collectAsStateWithLifecycle()

    LaunchedEffect(ui.registrada) { if (ui.registrada) alTerminar() }
    BackHandler { if (!vm.anterior()) alVolver() }

    val progreso by animateFloatAsState(
        targetValue = (ui.paso + 1f) / RegistroValidator.TOTAL_PASOS,
        animationSpec = tween(400),
        label = "progresoRegistro"
    )

    PantallaSecundaria(
        titulo = "Inscripción",
        alVolver = { if (!vm.anterior()) alVolver() },
        barraInferior = {
            Surface(tonalElevation = 3.dp) {
                Row(
                    Modifier.fillMaxWidth().navigationBarsPadding().imePadding().padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (ui.paso > 0) {
                        OutlinedButton(onClick = { vm.anterior() }, modifier = Modifier.weight(1f).padding(top = 2.dp)) {
                            Text("Atrás")
                        }
                    }
                    BotonCarga(
                        texto = if (ui.paso == RegistroValidator.TOTAL_PASOS - 1) "Crear mi cuenta" else "Siguiente",
                        cargando = ui.cargando,
                        alTocar = vm::siguiente,
                        icono = Icons.AutoMirrored.Filled.ArrowForward,
                        modifier = Modifier.weight(2f)
                    )
                }
            }
        }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            // Indicador de progreso de 6 pasos (RF01.6)
            Column(Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                Text(
                    "Paso ${ui.paso + 1} de ${RegistroValidator.TOTAL_PASOS}: ${RegistroValidator.titulosPasos[ui.paso]}",
                    style = MaterialTheme.typography.titleMedium
                )
                LinearProgressIndicator(
                    progress = { progreso },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                )
            }
            // Transición animada entre pasos (RNF07)
            AnimatedContent(
                targetState = ui.paso,
                transitionSpec = {
                    val dir = if (ui.avanzando) 1 else -1
                    (slideInHorizontally(tween(300)) { it * dir } + fadeIn(tween(300))) togetherWith
                        (slideOutHorizontally(tween(300)) { -it * dir } + fadeOut(tween(200)))
                },
                label = "pasoRegistro",
                modifier = Modifier.weight(1f)
            ) { paso ->
                ColumnaFormulario(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(bottom = 24.dp)) {
                    when (paso) {
                        0 -> PasoDatosPersonales(ui, vm)
                        1 -> PasoPassword(ui, vm)
                        2 -> PasoUbicacion(ui, vm)
                        3 -> PasoEmprendimiento(ui, vm, rubros)
                        4 -> PasoTrayectoria(ui, vm, alAbrirEnlace)
                        else -> PasoAutorizacion(ui, vm)
                    }
                    MensajeError(ui.errorGeneral)
                }
            }
        }
    }
}

@Composable
private fun PasoDatosPersonales(ui: RegistroUiState, vm: RegistroViewModel) {
    val e = ui.errores
    CampoTexto(
        valor = ui.form.nombre, alCambiar = { v -> vm.editar(CampoRegistro.NOMBRE) { it.copy(nombre = v) } },
        etiqueta = "Nombre y apellido", error = e[CampoRegistro.NOMBRE], disparador = ui.intentos,
        icono = Icons.Filled.Person, maxCaracteres = 60
    )
    CampoTexto(
        valor = ui.form.rut, alCambiar = { v -> vm.editar(CampoRegistro.RUT) { it.copy(rut = v) } },
        etiqueta = "RUT", error = e[CampoRegistro.RUT], disparador = ui.intentos, icono = Icons.Filled.Badge,
        ayuda = "Se usa solo para identificarte. No se publica.", maxCaracteres = 12
    )
    CampoTexto(
        valor = ui.form.email, alCambiar = { v -> vm.editar(CampoRegistro.EMAIL) { it.copy(email = v) } },
        etiqueta = "Correo electrónico", error = e[CampoRegistro.EMAIL], disparador = ui.intentos,
        icono = Icons.Filled.Email, teclado = TecladoEmail, ayuda = "Será tu usuario para ingresar"
    )
}

@Composable
private fun PasoPassword(ui: RegistroUiState, vm: RegistroViewModel) {
    Text("Crea una contraseña fácil de recordar, de al menos 8 caracteres.", style = MaterialTheme.typography.bodyMedium)
    CampoTexto(
        valor = ui.form.password, alCambiar = { v -> vm.editar(CampoRegistro.PASSWORD) { it.copy(password = v) } },
        etiqueta = "Contraseña", error = ui.errores[CampoRegistro.PASSWORD], disparador = ui.intentos,
        icono = Icons.Filled.Lock, teclado = TecladoPassword, esPassword = true
    )
    CampoTexto(
        valor = ui.form.confirmacion,
        alCambiar = { v -> vm.editar(CampoRegistro.CONFIRMACION) { it.copy(confirmacion = v) } },
        etiqueta = "Repite la contraseña", error = ui.errores[CampoRegistro.CONFIRMACION], disparador = ui.intentos,
        icono = Icons.Filled.Lock, teclado = TecladoPassword, esPassword = true
    )
}

@Composable
private fun PasoUbicacion(ui: RegistroUiState, vm: RegistroViewModel) {
    val e = ui.errores
    val f = ui.form
    CampoLista(
        "Rango de edad", OpcionesFormulario.rangosEtarios, f.rangoEtario,
        { v -> vm.editar(CampoRegistro.RANGO_ETARIO) { it.copy(rangoEtario = v) } },
        e[CampoRegistro.RANGO_ETARIO], ui.intentos, icono = Icons.Filled.Cake
    )
    CampoLista(
        "Género", OpcionesFormulario.generos, f.genero,
        { v -> vm.editar(CampoRegistro.GENERO) { it.copy(genero = v) } }, e[CampoRegistro.GENERO], ui.intentos
    )
    CampoTexto(
        valor = f.telefono, alCambiar = { v -> vm.editar(CampoRegistro.TELEFONO) { it.copy(telefono = v) } },
        etiqueta = "Teléfono (WhatsApp)", error = e[CampoRegistro.TELEFONO], disparador = ui.intentos,
        icono = Icons.Filled.Phone, teclado = TecladoTelefono, prefijo = "+56 ", maxCaracteres = 12,
        ayuda = "9 dígitos, por ejemplo 912345678"
    )
    CampoLista(
        "Región", ChileGeo.regiones, f.region, vm::elegirRegion,
        e[CampoRegistro.REGION], ui.intentos, icono = Icons.Filled.Map
    )
    CampoLista(
        "Comuna", ChileGeo.comunasDe(f.region), f.comuna,
        { v -> vm.editar(CampoRegistro.COMUNA) { it.copy(comuna = v) } },
        e[CampoRegistro.COMUNA], ui.intentos, icono = Icons.Filled.Place, habilitado = f.region != null,
        ayuda = if (f.region == null) "Primero elige tu región" else null
    )
    CampoTexto(
        valor = f.codigoEmbajadora,
        alCambiar = { v -> vm.editar(CampoRegistro.CODIGO_EMBAJADORA) { it.copy(codigoEmbajadora = v) } },
        etiqueta = "Código de Embajadora (opcional)", error = e[CampoRegistro.CODIGO_EMBAJADORA],
        disparador = ui.intentos, maxCaracteres = 20, mostrarOk = false,
        ayuda = "Déjalo en blanco si no tienes uno"
    )
}

@Composable
private fun PasoEmprendimiento(ui: RegistroUiState, vm: RegistroViewModel, rubros: List<Rubro>) {
    val e = ui.errores
    CampoTexto(
        valor = ui.form.nombreEmprendimiento,
        alCambiar = { v -> vm.editar(CampoRegistro.NOMBRE_EMPRENDIMIENTO) { it.copy(nombreEmprendimiento = v) } },
        etiqueta = "Nombre del emprendimiento", error = e[CampoRegistro.NOMBRE_EMPRENDIMIENTO],
        disparador = ui.intentos, icono = Icons.Filled.Storefront, maxCaracteres = 60,
        ayuda = "Si no tiene nombre, escribe tu propio nombre"
    )
    CampoLista(
        "¿Está formalizado ante el SII?", OpcionesFormulario.formalizacion, ui.form.formalizado,
        { v -> vm.editar(CampoRegistro.FORMALIZADO) { it.copy(formalizado = v) } },
        e[CampoRegistro.FORMALIZADO], ui.intentos
    )
    Text("¿Qué haces? Elige 1 o 2 rubros", style = MaterialTheme.typography.titleSmall)
    SelectorChips(
        opciones = rubros.map { it.id },
        seleccionados = ui.form.rubros,
        alAlternar = vm::alternarRubro,
        error = e[CampoRegistro.RUBROS],
        disparador = ui.intentos,
        texto = { id -> rubros.firstOrNull { it.id == id }?.nombre.orEmpty() },
        maximo = 2
    )
}

@Composable
private fun PasoTrayectoria(ui: RegistroUiState, vm: RegistroViewModel, alAbrirEnlace: (String) -> Unit) {
    val e = ui.errores
    val f = ui.form
    CampoLista(
        "Tiempo dedicado a esto", OpcionesFormulario.tiempoDedicado, f.tiempo,
        { v -> vm.editar(CampoRegistro.TIEMPO) { it.copy(tiempo = v) } }, e[CampoRegistro.TIEMPO], ui.intentos
    )
    CampoLista(
        "La actividad representa…", OpcionesFormulario.ingreso, f.ingreso,
        { v -> vm.editar(CampoRegistro.INGRESO, CampoRegistro.ASPIRACION) { it.copy(ingreso = v) } },
        e[CampoRegistro.INGRESO], ui.intentos
    )
    // RF01.6: pregunta condicional
    AnimatedVisibility(visible = f.muestraAspiracion) {
        CampoLista(
            "¿Aspiras a que sea tu principal ingreso?", OpcionesFormulario.aspiracion, f.aspiracion,
            { v -> vm.editar(CampoRegistro.ASPIRACION) { it.copy(aspiracion = v) } }, e[CampoRegistro.ASPIRACION], ui.intentos
        )
    }
    CampoTexto(
        valor = f.instagram, alCambiar = { v -> vm.editar(CampoRegistro.INSTAGRAM) { it.copy(instagram = v) } },
        etiqueta = "Instagram (opcional)", error = e[CampoRegistro.INSTAGRAM], disparador = ui.intentos,
        icono = Icons.Filled.PhotoCamera, ayuda = "Puedes escribir usuario, @usuario o el enlace"
    )
    val enlace = Instagram.enlace(f.instagram)
    AnimatedVisibility(visible = enlace != null && e[CampoRegistro.INSTAGRAM] == null) {
        OutlinedButton(onClick = { enlace?.let(alAbrirEnlace) }) { Text("Abrir para comprobar") }
    }
    CampoLista(
        "¿Cómo nos conociste?", OpcionesFormulario.comoNosConociste, f.comoConociste,
        { v ->
            vm.editar(CampoRegistro.COMO_CONOCISTE, CampoRegistro.COMO_CONOCISTE_OTRO, CampoRegistro.QUIEN_INVITO) {
                it.copy(comoConociste = v)
            }
        },
        e[CampoRegistro.COMO_CONOCISTE], ui.intentos
    )
    AnimatedVisibility(visible = f.muestraOtro) {
        CampoTexto(
            valor = f.comoConocisteOtro,
            alCambiar = { v -> vm.editar(CampoRegistro.COMO_CONOCISTE_OTRO) { it.copy(comoConocisteOtro = v) } },
            etiqueta = "Cuéntanos cómo", error = e[CampoRegistro.COMO_CONOCISTE_OTRO], disparador = ui.intentos,
            maxCaracteres = 100
        )
    }
    AnimatedVisibility(visible = f.muestraQuienInvito) {
        CampoTexto(
            valor = f.quienInvito,
            alCambiar = { v -> vm.editar(CampoRegistro.QUIEN_INVITO) { it.copy(quienInvito = v) } },
            etiqueta = "¿Quién te invitó? (nombre y apellido)", error = e[CampoRegistro.QUIEN_INVITO],
            disparador = ui.intentos, icono = Icons.Filled.Person, maxCaracteres = 60
        )
    }
}

@Composable
private fun PasoAutorizacion(ui: RegistroUiState, vm: RegistroViewModel) {
    Text("Autorización de datos", style = MaterialTheme.typography.titleMedium)
    Text(TEXTO_AUTORIZACION, style = MaterialTheme.typography.bodyMedium)
    Text(
        "Tu RUT, correo y contraseña nunca se publican. Tu ficha quedará \"Pendiente de validación\" " +
            "hasta que otras socias confirmen tus datos.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    CasillaVerificacion(
        marcada = ui.form.consentimiento,
        alCambiar = { v -> vm.editar(CampoRegistro.CONSENTIMIENTO) { it.copy(consentimiento = v) } },
        texto = "Acepto la autorización de datos",
        error = ui.errores[CampoRegistro.CONSENTIMIENTO],
        disparador = ui.intentos
    )
}
