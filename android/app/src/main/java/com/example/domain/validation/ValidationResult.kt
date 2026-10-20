package com.example.domain.validation

/**
 * Resultado de validar un campo.
 * Si no es válido, trae el mensaje que la interfaz debe mostrar bajo el campo.
 */
data class ValidationResult(
    val isValid: Boolean,
    val errorMessage: String? = null
) {
    companion object {
        val Valid = ValidationResult(isValid = true)
        fun error(message: String) = ValidationResult(isValid = false, errorMessage = message)
    }
}

/** Devuelve el primer resultado inválido de la lista, o Valid si todos pasan. */
fun firstError(vararg results: ValidationResult): ValidationResult =
    results.firstOrNull { !it.isValid } ?: ValidationResult.Valid
