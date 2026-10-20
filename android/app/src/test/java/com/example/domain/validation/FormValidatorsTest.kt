package com.example.domain.validation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FormValidatorsTest {

    // --- Publicar prenda ---

    @Test
    fun `titulo obligatorio y con largo valido`() {
        assertEquals("El nombre de la prenda es obligatorio", ProductValidator.validateTitle("").errorMessage)
        assertFalse(ProductValidator.validateTitle("Ab").isValid)
        assertFalse(ProductValidator.validateTitle("x".repeat(61)).isValid)
        assertTrue(ProductValidator.validateTitle("Chaqueta Denim").isValid)
    }

    @Test
    fun `precio dentro del rango permitido`() {
        assertTrue(ProductValidator.validatePrice("35000").isValid)
        assertTrue(ProductValidator.validatePrice("35.000").isValid)
        assertEquals("El precio mínimo es $1.000", ProductValidator.validatePrice("500").errorMessage)
        assertFalse(ProductValidator.validatePrice("3000000").isValid)
        assertEquals("El precio solo puede contener números", ProductValidator.validatePrice("35mil").errorMessage)
        assertFalse(ProductValidator.validatePrice("").isValid)
    }

    @Test
    fun `parsePrice acepta separador de miles`() {
        assertEquals(35000, ProductValidator.parsePrice("35.000"))
        assertNull(ProductValidator.parsePrice("-100"))
    }

    @Test
    fun `categoria debe existir`() {
        assertTrue(ProductValidator.validateCategory("Bolsos").isValid)
        assertFalse(ProductValidator.validateCategory("Zapatos").isValid)
    }

    @Test
    fun `descripcion es opcional pero no puede ser muy corta`() {
        assertTrue(ProductValidator.validateDescription("").isValid)
        assertFalse(ProductValidator.validateDescription("corta").isValid)
        assertTrue(ProductValidator.validateDescription("Hecha con mezclilla rescatada").isValid)
    }

    @Test
    fun `foto obligatoria`() {
        assertFalse(ProductValidator.validatePhoto(hasPhoto = false).isValid)
        assertTrue(ProductValidator.validatePhoto(hasPhoto = true).isValid)
    }

    // --- Registro ---

    @Test
    fun `confirmacion de contrasena debe coincidir`() {
        assertTrue(RegisterValidator.validatePasswordConfirmation("telar2026", "telar2026").isValid)
        assertEquals(
            "Las contraseñas no coinciden",
            RegisterValidator.validatePasswordConfirmation("telar2026", "telar2025").errorMessage
        )
    }

    @Test
    fun `registro exige aceptar el reglamento`() {
        assertFalse(RegisterValidator.validateTermsAccepted(false).isValid)
        assertTrue(RegisterValidator.validateTermsAccepted(true).isValid)
    }

    @Test
    fun `rut se normaliza para guardarlo`() {
        assertEquals("12345678-5", ValidationRules.normalizeRut("12.345.678-5"))
        assertEquals("10000013-K", ValidationRules.normalizeRut("10000013k"))
    }

    // --- Postulación ---

    @Test
    fun `motivo de postulacion entre 20 y 300 caracteres`() {
        assertFalse(ApplicationValidator.validateMotivation("Quiero ir").isValid)
        assertTrue(ApplicationValidator.validateMotivation("Quiero exponer mis chaquetas de denim reciclado").isValid)
    }

    // --- Chat ---

    @Test
    fun `mensaje de chat no puede estar vacio ni ser muy largo`() {
        assertFalse(ChatMessageValidator.validate("   ").isValid)
        assertFalse(ChatMessageValidator.validate("a".repeat(501)).isValid)
        assertTrue(ChatMessageValidator.validate("¡Hola! Sí, tengo talla M.").isValid)
    }
}
