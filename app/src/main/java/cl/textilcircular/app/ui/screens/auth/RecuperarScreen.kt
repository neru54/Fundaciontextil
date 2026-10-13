package cl.textilcircular.app.ui.screens.auth

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import cl.textilcircular.app.data.repository.AuthRepository
import cl.textilcircular.app.domain.validation.Validators
import cl.textilcircular.app.ui.appViewModel
import cl.textilcircular.app.ui.components.Aviso
import cl.textilcircular.app.ui.components.BotonCarga
import cl.textilcircular.app.ui.components.CampoTexto
import cl.textilcircular.app.ui.components.ColumnaFormulario
import cl.textilcircular.app.ui.components.MensajeError
import cl.textilcircular.app.ui.components.PantallaSecundaria
import cl.textilcircular.app.ui.components.TecladoEmail
import cl.textilcircular.app.ui.components.TecladoNumero
import cl.textilcircular.app.ui.components.TecladoPassword
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RecuperarUiState(
    val etapaCodigo: Boolean = false,
    val email: String = "",
    val codigo: String = "",
    val nueva: String = "",
    val confirmacion: String = "",
    val codigoSimulado: String? = null,
    val errores: Map<String, String> = emptyMap(),
    val errorGeneral: String? = null,
    val intentos: Int = 0,
    val cargando: Boolean = false,
    val listo: Boolean = false
)

/** RF01.10: recuperación de contraseña con código de 6 dígitos (envío simulado). */
class RecuperarViewModel(private val auth: AuthRepository) : ViewModel() {
    private val _ui = MutableStateFlow(RecuperarUiState())
    val ui: StateFlow<RecuperarUiState> = _ui.asStateFlow()

    fun editar(campo: String, cambio: (RecuperarUiState) -> RecuperarUiState) =
        _ui.update { cambio(it).copy(errores = it.errores - campo, errorGeneral = null) }

    fun enviarCodigo() {
        val s = _ui.value
        Validators.email(s.email)?.let { err ->
            _ui.update { it.copy(errores = mapOf("email" to err), intentos = it.intentos + 1) }
            return
        }
        _ui.update { it.copy(cargando = true) }
        viewModelScope.launch {
            auth.solicitarCodigo(s.email)
                .onSuccess { codigo -> _ui.update { it.copy(cargando = false, etapaCodigo = true, codigoSimulado = codigo) } }
                .onFailure { e -> _ui.update { it.copy(cargando = false, errorGeneral = e.message, intentos = it.intentos + 1) } }
        }
    }

    fun restablecer() {
        val s = _ui.value
        val errores = buildMap {
            Validators.codigoVerificacion(s.codigo)?.let { put("codigo", it) }
            Validators.password(s.nueva)?.let { put("nueva", it) }
            Validators.confirmarPassword(s.nueva, s.confirmacion)?.let { put("confirmacion", it) }
        }
        if (errores.isNotEmpty()) {
            _ui.update { it.copy(errores = errores, intentos = it.intentos + 1) }
            return
        }
        _ui.update { it.copy(cargando = true) }
        viewModelScope.launch {
            auth.restablecerPassword(s.email, s.codigo, s.nueva)
                .onSuccess { _ui.update { it.copy(cargando = false, listo = true) } }
                .onFailure { e -> _ui.update { it.copy(cargando = false, errorGeneral = e.message, intentos = it.intentos + 1) } }
        }
    }
}

@Composable
fun RecuperarScreen(alVolver: () -> Unit, alTerminar: () -> Unit) {
    val vm = appViewModel { c, _ -> RecuperarViewModel(c.authRepository) }
    val ui by vm.ui.collectAsStateWithLifecycle()
    LaunchedEffect(ui.listo) { if (ui.listo) alTerminar() }

    PantallaSecundaria(titulo = "Recuperar contraseña", alVolver = alVolver) { padding ->
        AnimatedContent(targetState = ui.etapaCodigo, label = "etapaRecuperar", modifier = Modifier.padding(padding)) { etapaCodigo ->
            ColumnaFormulario(Modifier.imePadding().verticalScroll(rememberScrollState())) {
                Spacer(Modifier.height(8.dp))
                if (!etapaCodigo) {
                    Text("Te enviaremos un código de 6 números a tu correo.", style = MaterialTheme.typography.bodyLarge)
                    CampoTexto(
                        valor = ui.email, alCambiar = { v -> vm.editar("email") { it.copy(email = v) } },
                        etiqueta = "Correo electrónico", error = ui.errores["email"], disparador = ui.intentos,
                        icono = Icons.Filled.Email, teclado = TecladoEmail
                    )
                    MensajeError(ui.errorGeneral)
                    BotonCarga("Enviar código", ui.cargando, vm::enviarCodigo)
                } else {
                    Column {
                        Aviso(
                            "Demostración: el envío de correo se simula. Tu código es ${ui.codigoSimulado}",
                            icono = Icons.Filled.MarkEmailRead
                        )
                    }
                    CampoTexto(
                        valor = ui.codigo, alCambiar = { v -> vm.editar("codigo") { it.copy(codigo = v.filter(Char::isDigit)) } },
                        etiqueta = "Código de 6 números", error = ui.errores["codigo"], disparador = ui.intentos,
                        icono = Icons.Filled.Pin, teclado = TecladoNumero, maxCaracteres = 6
                    )
                    CampoTexto(
                        valor = ui.nueva, alCambiar = { v -> vm.editar("nueva") { it.copy(nueva = v) } },
                        etiqueta = "Nueva contraseña", error = ui.errores["nueva"], disparador = ui.intentos,
                        icono = Icons.Filled.Lock, teclado = TecladoPassword, esPassword = true
                    )
                    CampoTexto(
                        valor = ui.confirmacion, alCambiar = { v -> vm.editar("confirmacion") { it.copy(confirmacion = v) } },
                        etiqueta = "Repite la contraseña", error = ui.errores["confirmacion"], disparador = ui.intentos,
                        icono = Icons.Filled.Lock, teclado = TecladoPassword, esPassword = true
                    )
                    MensajeError(ui.errorGeneral)
                    BotonCarga("Guardar contraseña", ui.cargando, vm::restablecer)
                }
            }
        }
    }
}
