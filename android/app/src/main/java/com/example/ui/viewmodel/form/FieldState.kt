package com.example.ui.viewmodel.form

import com.example.domain.validation.ValidationResult

/**
 * Estado de un campo de formulario: su valor, su error y si la usuaria ya lo tocó.
 * El error solo se muestra después de que el campo fue editado o se intentó enviar.
 */
data class FieldState(
    val value: String = "",
    val error: String? = null,
    val touched: Boolean = false
) {
    val isValid: Boolean get() = error == null
    /** Mostrar el check verde solo cuando el campo fue editado y es válido. */
    val showSuccess: Boolean get() = touched && isValid && value.isNotBlank()

    /** Actualiza el valor y lo valida en el momento. */
    fun update(newValue: String, validator: (String) -> ValidationResult): FieldState =
        copy(value = newValue, error = validator(newValue).errorMessage, touched = true)

    /** Fuerza la validación (por ejemplo, al presionar "Enviar"). */
    fun validate(validator: (String) -> ValidationResult): FieldState =
        copy(error = validator(value).errorMessage, touched = true)
}
