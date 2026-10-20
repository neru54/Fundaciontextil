package com.example.domain.validation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidationRulesTest {

    // --- RUT ---

    @Test
    fun `rut valido con puntos y guion`() {
        assertTrue(ValidationRules.rut("12.345.678-5").isValid)
    }

    @Test
    fun `rut valido sin formato`() {
        assertTrue(ValidationRules.rut("111111111").isValid)
    }

    @Test
    fun `rut valido con digito K en minuscula`() {
        assertTrue(ValidationRules.rut("10.000.013-k").isValid)
    }

    @Test
    fun `rut con digito verificador incorrecto`() {
        val result = ValidationRules.rut("12.345.678-9")
        assertFalse(result.isValid)
        assertEquals("El dígito verificador del RUT no es correcto", result.errorMessage)
    }

    @Test
    fun `rut vacio o con letras es invalido`() {
        assertFalse(ValidationRules.rut("").isValid)
        assertFalse(ValidationRules.rut("12.34A.678-5").isValid)
        assertFalse(ValidationRules.rut("123").isValid)
    }

    @Test
    fun `calculo del digito verificador`() {
        assertEquals('5', ValidationRules.calculateRutDv("12345678"))
        assertEquals('K', ValidationRules.calculateRutDv("10000013"))
        assertEquals('1', ValidationRules.calculateRutDv("11111111"))
    }

    // --- Correo ---

    @Test
    fun `correos validos e invalidos`() {
        assertTrue(ValidationRules.email("valentina.silva@gmail.com").isValid)
        assertTrue(ValidationRules.email("socia+taller@textil.cl").isValid)
        assertFalse(ValidationRules.email("valentina@").isValid)
        assertFalse(ValidationRules.email("valentina.gmail.com").isValid)
        assertEquals("El correo es obligatorio", ValidationRules.email(" ").errorMessage)
    }

    // --- Teléfono ---

    @Test
    fun `celular chileno con y sin prefijo`() {
        assertTrue(ValidationRules.chileanPhone("+56 9 1234 5678").isValid)
        assertTrue(ValidationRules.chileanPhone("912345678").isValid)
        assertTrue(ValidationRules.chileanPhone("56-9-1234-5678").isValid)
    }

    @Test
    fun `telefono fijo o incompleto es invalido`() {
        assertFalse(ValidationRules.chileanPhone("22345678").isValid)
        assertFalse(ValidationRules.chileanPhone("+56 9 1234").isValid)
    }

    // --- Contraseña ---

    @Test
    fun `contrasena debe tener 8 caracteres con letras y numeros`() {
        assertTrue(ValidationRules.password("telar2026").isValid)
        assertFalse(ValidationRules.password("corta1").isValid)
        assertFalse(ValidationRules.password("sololetras").isValid)
        assertFalse(ValidationRules.password("12345678").isValid)
    }

    // --- Nombre ---

    @Test
    fun `nombre con tildes es valido y con numeros no`() {
        assertTrue(ValidationRules.personName("María José Núñez").isValid)
        assertFalse(ValidationRules.personName("Ana123").isValid)
        assertFalse(ValidationRules.personName("Al").isValid)
    }
}
