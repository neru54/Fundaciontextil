package com.example.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.AuthRepository
import com.example.data.repository.RegisterResult
import com.example.domain.model.NewPartner
import com.example.domain.validation.LoginValidator
import com.example.domain.validation.RegisterValidator
import com.example.ui.viewmodel.form.FieldState
import kotlinx.coroutines.launch

data class LoginFormState(
    val email: FieldState = FieldState(),
    val password: FieldState = FieldState(),
    /** Error que no pertenece a un campo (ej: credenciales incorrectas). */
    val generalError: String? = null,
    val isSubmitting: Boolean = false
) {
    val isValid: Boolean get() = email.isValid && password.isValid
}

data class RegisterFormState(
    val name: FieldState = FieldState(),
    val rut: FieldState = FieldState(),
    val email: FieldState = FieldState(),
    val phone: FieldState = FieldState(),
    val comuna: FieldState = FieldState(),
    val workshopName: FieldState = FieldState(),
    val password: FieldState = FieldState(),
    val passwordConfirmation: FieldState = FieldState(),
    val acceptedTerms: Boolean = false,
    val termsError: String? = null,
    val generalError: String? = null,
    val isSubmitting: Boolean = false
) {
    val isValid: Boolean
        get() = listOf(name, rut, email, phone, comuna, workshopName, password, passwordConfirmation)
            .all { it.isValid } && termsError == null
}

class AuthViewModel(private val authRepository: AuthRepository) : ViewModel() {

    var loginForm by mutableStateOf(LoginFormState())
        private set

    var registerForm by mutableStateOf(RegisterFormState())
        private set

    fun resetForms() {
        loginForm = LoginFormState()
        registerForm = RegisterFormState()
    }

    // ---------- Inicio de sesión ----------

    fun onLoginEmailChange(value: String) {
        loginForm = loginForm.copy(
            email = loginForm.email.update(value, LoginValidator::validateEmail),
            generalError = null
        )
    }

    fun onLoginPasswordChange(value: String) {
        loginForm = loginForm.copy(
            password = loginForm.password.update(value, LoginValidator::validatePassword),
            generalError = null
        )
    }

    fun submitLogin(onSuccess: () -> Unit) {
        val form = loginForm
        if (form.isSubmitting) return
        val validated = form.copy(
            email = form.email.validate(LoginValidator::validateEmail),
            password = form.password.validate(LoginValidator::validatePassword)
        )
        loginForm = validated
        if (!validated.isValid) return

        loginForm = validated.copy(isSubmitting = true)
        viewModelScope.launch {
            val ok = authRepository.login(validated.email.value, validated.password.value)
            if (ok) {
                resetForms()
                onSuccess()
            } else {
                loginForm = loginForm.copy(
                    isSubmitting = false,
                    generalError = "Correo o contraseña incorrectos"
                )
            }
        }
    }

    // ---------- Registro ----------

    fun onNameChange(value: String) {
        registerForm = registerForm.copy(name = registerForm.name.update(value, RegisterValidator::validateName))
    }

    fun onRutChange(value: String) {
        registerForm = registerForm.copy(
            rut = registerForm.rut.update(value, RegisterValidator::validateRut),
            generalError = null
        )
    }

    fun onRegisterEmailChange(value: String) {
        registerForm = registerForm.copy(
            email = registerForm.email.update(value, RegisterValidator::validateEmail),
            generalError = null
        )
    }

    fun onPhoneChange(value: String) {
        registerForm = registerForm.copy(phone = registerForm.phone.update(value, RegisterValidator::validatePhone))
    }

    fun onComunaChange(value: String) {
        registerForm = registerForm.copy(comuna = registerForm.comuna.update(value, RegisterValidator::validateComuna))
    }

    fun onWorkshopNameChange(value: String) {
        registerForm = registerForm.copy(
            workshopName = registerForm.workshopName.update(value, RegisterValidator::validateWorkshopName)
        )
    }

    fun onRegisterPasswordChange(value: String) {
        val form = registerForm
        val confirmation = form.passwordConfirmation
        registerForm = form.copy(
            password = form.password.update(value, RegisterValidator::validatePassword),
            // Si ya escribió la confirmación, se vuelve a comparar con la nueva contraseña
            passwordConfirmation = if (confirmation.touched) {
                confirmation.validate { RegisterValidator.validatePasswordConfirmation(value, it) }
            } else confirmation
        )
    }

    fun onPasswordConfirmationChange(value: String) {
        val password = registerForm.password.value
        registerForm = registerForm.copy(
            passwordConfirmation = registerForm.passwordConfirmation.update(value) {
                RegisterValidator.validatePasswordConfirmation(password, it)
            }
        )
    }

    fun onTermsChange(accepted: Boolean) {
        registerForm = registerForm.copy(
            acceptedTerms = accepted,
            termsError = RegisterValidator.validateTermsAccepted(accepted).errorMessage
        )
    }

    fun submitRegister(onSuccess: () -> Unit) {
        val form = registerForm
        if (form.isSubmitting) return
        val password = form.password.value
        val validated = form.copy(
            name = form.name.validate(RegisterValidator::validateName),
            rut = form.rut.validate(RegisterValidator::validateRut),
            email = form.email.validate(RegisterValidator::validateEmail),
            phone = form.phone.validate(RegisterValidator::validatePhone),
            comuna = form.comuna.validate(RegisterValidator::validateComuna),
            workshopName = form.workshopName.validate(RegisterValidator::validateWorkshopName),
            password = form.password.validate(RegisterValidator::validatePassword),
            passwordConfirmation = form.passwordConfirmation.validate {
                RegisterValidator.validatePasswordConfirmation(password, it)
            },
            termsError = RegisterValidator.validateTermsAccepted(form.acceptedTerms).errorMessage
        )
        registerForm = validated
        if (!validated.isValid) return

        registerForm = validated.copy(isSubmitting = true)
        viewModelScope.launch {
            val result = authRepository.register(
                NewPartner(
                    name = validated.name.value,
                    rut = validated.rut.value,
                    email = validated.email.value,
                    phone = validated.phone.value,
                    comuna = validated.comuna.value,
                    workshopName = validated.workshopName.value,
                    password = validated.password.value
                )
            )
            when (result) {
                is RegisterResult.Success -> {
                    resetForms()
                    onSuccess()
                }
                RegisterResult.EmailAlreadyUsed -> registerForm = registerForm.copy(
                    isSubmitting = false,
                    email = registerForm.email.copy(error = "Ya existe una socia registrada con este correo")
                )
                RegisterResult.RutAlreadyUsed -> registerForm = registerForm.copy(
                    isSubmitting = false,
                    rut = registerForm.rut.copy(error = "Este RUT ya está registrado")
                )
            }
        }
    }

    fun logout() {
        viewModelScope.launch { authRepository.logout() }
    }
}
