package com.example.domain.validation

import com.example.domain.model.ProductCategories

/** Validadores por formulario. */

object ProductValidator {
    const val TITLE_MAX = 60
    const val ECO_TAG_MAX = 60
    const val DESCRIPTION_MAX = 300
    const val PRICE_MIN = 1_000
    const val PRICE_MAX = 2_000_000

    fun validateTitle(value: String) = firstError(
        ValidationRules.required(value, "El nombre de la prenda"),
        ValidationRules.length(value, 3, TITLE_MAX, "El nombre de la prenda")
    )

    fun validatePrice(value: String): ValidationResult {
        if (value.isBlank()) return ValidationResult.error("El precio es obligatorio")
        val price = parsePrice(value)
            ?: return ValidationResult.error("El precio solo puede contener números")
        return when {
            price < PRICE_MIN -> ValidationResult.error("El precio mínimo es $1.000")
            price > PRICE_MAX -> ValidationResult.error("El precio máximo es $2.000.000")
            else -> ValidationResult.Valid
        }
    }

    fun parsePrice(value: String): Int? {
        val digits = value.trim().replace(".", "")
        if (digits.isEmpty() || !digits.all { it.isDigit() }) return null
        return digits.toIntOrNull()
    }

    fun validateCategory(value: String) =
        if (value in ProductCategories.values) ValidationResult.Valid
        else ValidationResult.error("Selecciona una categoría")

    fun validateEcoTag(value: String) = firstError(
        ValidationRules.required(value, "El material rescatado"),
        ValidationRules.length(value, 3, ECO_TAG_MAX, "El material rescatado")
    )

    /** Opcional. */
    fun validateDescription(value: String) = when {
        value.isBlank() -> ValidationResult.Valid
        value.trim().length < 10 -> ValidationResult.error("Describe la prenda con al menos 10 caracteres")
        else -> ValidationRules.maxLength(value, DESCRIPTION_MAX, "La descripción")
    }

    fun validatePhoto(hasPhoto: Boolean) =
        if (hasPhoto) ValidationResult.Valid
        else ValidationResult.error("Agrega una foto de la prenda con la cámara o la galería")
}

object LoginValidator {
    fun validateEmail(value: String) = ValidationRules.email(value)
    fun validatePassword(value: String) =
        if (value.isEmpty()) ValidationResult.error("La contraseña es obligatoria")
        else ValidationResult.Valid
}

object RegisterValidator {
    fun validateName(value: String) = ValidationRules.personName(value, "El nombre")
    fun validateRut(value: String) = ValidationRules.rut(value)
    fun validateEmail(value: String) = ValidationRules.email(value)
    fun validatePhone(value: String) = ValidationRules.chileanPhone(value)
    fun validateComuna(value: String) = ValidationRules.required(value, "La comuna")
    fun validateWorkshopName(value: String) = firstError(
        ValidationRules.required(value, "El nombre del taller"),
        ValidationRules.length(value, 3, 60, "El nombre del taller")
    )
    fun validatePassword(value: String) = ValidationRules.password(value)
    fun validateTermsAccepted(accepted: Boolean) =
        if (accepted) ValidationResult.Valid
        else ValidationResult.error("Debes aceptar el reglamento de socias para registrarte")
    fun validatePasswordConfirmation(password: String, confirmation: String) = when {
        confirmation.isEmpty() -> ValidationResult.error("Repite la contraseña")
        confirmation != password -> ValidationResult.error("Las contraseñas no coinciden")
        else -> ValidationResult.Valid
    }
}

object CheckoutValidator {
    fun validateFullName(value: String) = ValidationRules.personName(value, "El nombre")
    fun validateAddress(value: String) = firstError(
        ValidationRules.required(value, "La dirección"),
        ValidationRules.length(value, 5, 100, "La dirección")
    )
    fun validateComuna(value: String) = ValidationRules.required(value, "La comuna")
    fun validatePhone(value: String) = ValidationRules.chileanPhone(value)
}

object ApplicationValidator {
    const val MOTIVATION_MIN = 20
    const val MOTIVATION_MAX = 300

    fun validateMotivation(value: String) = firstError(
        ValidationRules.required(value, "El motivo"),
        ValidationRules.length(value, MOTIVATION_MIN, MOTIVATION_MAX, "El motivo")
    )
}

object ChatMessageValidator {
    const val MAX_LENGTH = 500

    fun validate(value: String) = when {
        value.isBlank() -> ValidationResult.error("Escribe un mensaje antes de enviar")
        value.trim().length > MAX_LENGTH -> ValidationResult.error("El mensaje no puede superar los $MAX_LENGTH caracteres")
        else -> ValidationResult.Valid
    }
}
