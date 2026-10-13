package cl.textilcircular.app.ui.screens.auth

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.material3.SnackbarHostState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import cl.textilcircular.app.data.repository.AuthRepository
import cl.textilcircular.app.data.seed.DemoSeeder
import cl.textilcircular.app.domain.validation.Validators
import cl.textilcircular.app.ui.appViewModel
import cl.textilcircular.app.ui.components.Aviso
import cl.textilcircular.app.ui.components.BotonCarga
import cl.textilcircular.app.ui.components.CampoTexto
import cl.textilcircular.app.ui.components.ColumnaFormulario
import cl.textilcircular.app.ui.components.MensajeError
import cl.textilcircular.app.ui.components.PantallaSecundaria
import cl.textilcircular.app.ui.components.TecladoEmail
import cl.textilcircular.app.ui.components.TecladoPassword
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val errorEmail: String? = null,
    val errorPassword: String? = null,
    val errorGeneral: String? = null,
    val intentos: Int = 0,
    val cargando: Boolean = false,
    val listo: Boolean = false
)

/** RF01.9: inicio de sesión con correo y contraseña. */
class LoginViewModel(private val auth: AuthRepository) : ViewModel() {
    private val _ui = MutableStateFlow(LoginUiState())
    val ui: StateFlow<LoginUiState> = _ui.asStateFlow()

    fun alCambiarEmail(v: String) = _ui.update { it.copy(email = v, errorEmail = null, errorGeneral = null) }
    fun alCambiarPassword(v: String) = _ui.update { it.copy(password = v, errorPassword = null, errorGeneral = null) }

    fun usarCuentaDemo() = _ui.update {
        it.copy(email = DemoSeeder.DEMO_EMAIL, password = DemoSeeder.DEMO_PASSWORD, errorEmail = null, errorPassword = null)
    }

    fun ingresar() {
        val s = _ui.value
        val errorEmail = Validators.email(s.email)
        val errorPassword = if (s.password.isEmpty()) "Escribe tu contraseña" else null
        if (errorEmail != null || errorPassword != null) {
            _ui.update { it.copy(errorEmail = errorEmail, errorPassword = errorPassword, intentos = it.intentos + 1) }
            return
        }
        _ui.update { it.copy(cargando = true) }
        viewModelScope.launch {
            auth.iniciarSesion(s.email, s.password)
                .onSuccess { _ui.update { it.copy(cargando = false, listo = true) } }
                .onFailure { e -> _ui.update { it.copy(cargando = false, errorGeneral = e.message, intentos = it.intentos + 1) } }
        }
    }
}

@Composable
fun LoginScreen(
    alVolver: () -> Unit,
    alIngresar: () -> Unit,
    alInscribirse: () -> Unit,
    alRecuperar: () -> Unit
) {
    val vm = appViewModel { c, _ -> LoginViewModel(c.authRepository) }
    val ui by vm.ui.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(ui.listo) { if (ui.listo) alIngresar() }

    PantallaSecundaria(titulo = "Ingreso de socias", alVolver = alVolver, snackbar = snackbar) { padding ->
        ColumnaFormulario(
            Modifier
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(8.dp))
            Text("Hola de nuevo", style = MaterialTheme.typography.headlineSmall)
            Text(
                "Ingresa con el correo y la contraseña que usaste al inscribirte. Tu sesión queda abierta para usar la app sin conexión.",
                style = MaterialTheme.typography.bodyMedium
            )
            CampoTexto(
                valor = ui.email, alCambiar = vm::alCambiarEmail, etiqueta = "Correo electrónico",
                error = ui.errorEmail, disparador = ui.intentos, icono = Icons.Filled.Email, teclado = TecladoEmail
            )
            CampoTexto(
                valor = ui.password, alCambiar = vm::alCambiarPassword, etiqueta = "Contraseña",
                error = ui.errorPassword, disparador = ui.intentos, icono = Icons.Filled.Lock,
                teclado = TecladoPassword, esPassword = true
            )
            MensajeError(ui.errorGeneral)
            BotonCarga("Ingresar", ui.cargando, vm::ingresar, icono = Icons.AutoMirrored.Filled.Login)
            TextButton(onClick = alRecuperar, modifier = Modifier.fillMaxWidth()) { Text("Olvidé mi contraseña") }
            OutlinedButton(onClick = alInscribirse, modifier = Modifier.fillMaxWidth()) { Text("Aún no soy socia, inscribirme") }
            Spacer(Modifier.height(8.dp))
            Aviso(
                texto = "Demostración: toca aquí para usar la cuenta ${DemoSeeder.DEMO_EMAIL} / ${DemoSeeder.DEMO_PASSWORD}",
                alTocar = vm::usarCuentaDemo
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}
