package cl.textilcircular.app.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.textilcircular.app.data.repository.AuthRepository
import cl.textilcircular.app.data.repository.ErrorNegocio
import cl.textilcircular.app.data.repository.MarketplaceRepository
import cl.textilcircular.app.domain.model.Rubro
import cl.textilcircular.app.domain.validation.CampoRegistro
import cl.textilcircular.app.domain.validation.RegistroForm
import cl.textilcircular.app.domain.validation.RegistroValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RegistroUiState(
    val form: RegistroForm = RegistroForm(),
    val paso: Int = 0,
    /** Dirección de la última transición, para animar hacia adelante o atrás. */
    val avanzando: Boolean = true,
    val errores: Map<CampoRegistro, String> = emptyMap(),
    val intentos: Int = 0,
    val cargando: Boolean = false,
    val errorGeneral: String? = null,
    val registrada: Boolean = false
)

/**
 * Formulario de inscripción en 6 pasos (RF01.1, RF01.6). Las reglas viven en [RegistroValidator]
 * (capa de dominio); este ViewModel solo coordina el estado y expone el resultado (RNF08).
 */
class RegistroViewModel(
    private val auth: AuthRepository,
    marketplace: MarketplaceRepository
) : ViewModel() {

    private val _ui = MutableStateFlow(RegistroUiState())
    val ui: StateFlow<RegistroUiState> = _ui.asStateFlow()

    val rubros: StateFlow<List<Rubro>> = marketplace.rubros
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Actualiza el formulario y limpia el error de los campos que cambiaron. */
    fun editar(vararg campos: CampoRegistro, cambio: (RegistroForm) -> RegistroForm) = _ui.update {
        it.copy(form = cambio(it.form), errores = it.errores - campos.toSet(), errorGeneral = null)
    }

    /** RN01: al cambiar la región se reinicia la comuna. */
    fun elegirRegion(region: String) = editar(CampoRegistro.REGION, CampoRegistro.COMUNA) {
        if (it.region == region) it else it.copy(region = region, comuna = null)
    }

    /** RN02: máximo 2 rubros; la tercera selección se bloquea en la interfaz. */
    fun alternarRubro(id: Long) = editar(CampoRegistro.RUBROS) {
        val actuales = it.rubros
        it.copy(rubros = if (id in actuales) actuales - id else if (actuales.size < 2) actuales + id else actuales)
    }

    fun siguiente() {
        val s = _ui.value
        val errores = RegistroValidator.validarPaso(s.paso, s.form)
        if (errores.isNotEmpty()) {
            _ui.update { it.copy(errores = errores, intentos = it.intentos + 1) }
            return
        }
        if (s.paso < RegistroValidator.TOTAL_PASOS - 1) {
            _ui.update { it.copy(paso = it.paso + 1, avanzando = true, errores = emptyMap()) }
        } else {
            enviar()
        }
    }

    fun anterior(): Boolean {
        if (_ui.value.paso == 0) return false
        _ui.update { it.copy(paso = it.paso - 1, avanzando = false, errores = emptyMap()) }
        return true
    }

    private fun enviar() {
        val form = _ui.value.form
        val errores = RegistroValidator.validarTodo(form)
        if (errores.isNotEmpty()) {
            val paso = errores.keys.minOf { RegistroValidator.pasoDe(it) }
            _ui.update { it.copy(paso = paso, avanzando = false, errores = errores, intentos = it.intentos + 1) }
            return
        }
        _ui.update { it.copy(cargando = true) }
        viewModelScope.launch {
            auth.registrar(form)
                .onSuccess { _ui.update { it.copy(cargando = false, registrada = true) } }
                .onFailure { e ->
                    val campo = (e as? ErrorNegocio)?.campo
                    _ui.update {
                        if (campo != null) it.copy(
                            cargando = false, paso = RegistroValidator.pasoDe(campo), avanzando = false,
                            errores = mapOf(campo to (e.message ?: "Revisa este dato")), intentos = it.intentos + 1
                        ) else it.copy(cargando = false, errorGeneral = e.message ?: "No pudimos crear tu cuenta")
                    }
                }
        }
    }
}
