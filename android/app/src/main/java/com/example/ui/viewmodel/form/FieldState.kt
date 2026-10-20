package com.example.ui.viewmodel.form

import com.example.domain.validation.ValidationResult

/** Valor, error y estado "tocado" de un campo de formulario. */
data class FieldState(
    val value: String = "",
    val error: String? = null,
    val touched: Boolean = false
) {
    val isValid: Boolean get() = error == null
    val showSuccess: Boolean get() = touched && isValid && value.isNotBlank()

    fun update(newValue: String, validator: (String) -> ValidationResult): FieldState =
        copy(value = newValue, error = validator(newValue).errorMessage, touched = true)

    /** Valida al enviar. */
    fun validate(validator: (String) -> ValidationResult): FieldState =
        copy(error = validator(value).errorMessage, touched = true)
}
