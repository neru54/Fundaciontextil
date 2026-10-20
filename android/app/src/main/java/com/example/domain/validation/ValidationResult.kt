package com.example.domain.validation

/** Resultado de validar un campo. */
data class ValidationResult(
    val isValid: Boolean,
    val errorMessage: String? = null
) {
    companion object {
        val Valid = ValidationResult(isValid = true)
        fun error(message: String) = ValidationResult(isValid = false, errorMessage = message)
    }
}

/** Primer resultado inválido, o Valid. */
fun firstError(vararg results: ValidationResult): ValidationResult =
    results.firstOrNull { !it.isValid } ?: ValidationResult.Valid
