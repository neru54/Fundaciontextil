package com.example.domain.validation

/** Reglas de validación en Kotlin puro. */
object ValidationRules {

    private val EMAIL_REGEX = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    private val CHILE_PHONE_REGEX = Regex("^(\\+?56)?9\\d{8}$")
    private val PERSON_NAME_REGEX = Regex("^[A-Za-zÁÉÍÓÚáéíóúÑñÜü' ]+$")

    fun required(value: String, fieldName: String): ValidationResult =
        if (value.isBlank()) ValidationResult.error("$fieldName es obligatorio")
        else ValidationResult.Valid

    fun length(value: String, min: Int, max: Int, fieldName: String): ValidationResult {
        val length = value.trim().length
        return when {
            length < min -> ValidationResult.error("$fieldName debe tener al menos $min caracteres")
            length > max -> ValidationResult.error("$fieldName no puede superar los $max caracteres")
            else -> ValidationResult.Valid
        }
    }

    fun maxLength(value: String, max: Int, fieldName: String): ValidationResult =
        if (value.trim().length > max) ValidationResult.error("$fieldName no puede superar los $max caracteres")
        else ValidationResult.Valid

    fun email(value: String): ValidationResult = firstError(
        required(value, "El correo"),
        if (EMAIL_REGEX.matches(value.trim())) ValidationResult.Valid
        else ValidationResult.error("Ingresa un correo válido (ej: nombre@correo.cl)")
    )

    /** 9XXXXXXXX, con o sin +56. */
    fun chileanPhone(value: String): ValidationResult {
        val normalized = value.replace(" ", "").replace("-", "")
        return firstError(
            required(value, "El teléfono"),
            if (CHILE_PHONE_REGEX.matches(normalized)) ValidationResult.Valid
            else ValidationResult.error("Ingresa un celular válido (ej: +56 9 1234 5678)")
        )
    }

    fun personName(value: String, fieldName: String = "El nombre"): ValidationResult = firstError(
        required(value, fieldName),
        length(value, 3, 60, fieldName),
        if (PERSON_NAME_REGEX.matches(value.trim())) ValidationResult.Valid
        else ValidationResult.error("$fieldName solo puede contener letras y espacios")
    )

    /** Mín. 8 caracteres con letras y números. */
    fun password(value: String): ValidationResult = when {
        value.isEmpty() -> ValidationResult.error("La contraseña es obligatoria")
        value.length < 8 -> ValidationResult.error("La contraseña debe tener al menos 8 caracteres")
        !value.any { it.isLetter() } || !value.any { it.isDigit() } ->
            ValidationResult.error("La contraseña debe combinar letras y números")
        else -> ValidationResult.Valid
    }

    /** RUT con dígito verificador (módulo 11), con o sin puntos y guion. */
    fun rut(value: String): ValidationResult {
        if (value.isBlank()) return ValidationResult.error("El RUT es obligatorio")
        val clean = value.replace(".", "").replace("-", "").trim().uppercase()
        if (clean.length < 8 || clean.length > 9) {
            return ValidationResult.error("Ingresa un RUT válido (ej: 12.345.678-5)")
        }
        val body = clean.dropLast(1)
        val dv = clean.last()
        if (!body.all { it.isDigit() } || !(dv.isDigit() || dv == 'K')) {
            return ValidationResult.error("Ingresa un RUT válido (ej: 12.345.678-5)")
        }
        return if (calculateRutDv(body) == dv) ValidationResult.Valid
        else ValidationResult.error("El dígito verificador del RUT no es correcto")
    }

    /** Formato "12345678-5". */
    fun normalizeRut(value: String): String {
        val clean = value.replace(".", "").replace("-", "").trim().uppercase()
        if (clean.length < 2) return clean
        return clean.dropLast(1) + "-" + clean.last()
    }

    fun calculateRutDv(body: String): Char {
        var sum = 0
        var multiplier = 2
        for (digit in body.reversed()) {
            sum += digit.digitToInt() * multiplier
            multiplier = if (multiplier == 7) 2 else multiplier + 1
        }
        return when (val result = 11 - (sum % 11)) {
            11 -> '0'
            10 -> 'K'
            else -> result.digitToChar()
        }
    }
}
