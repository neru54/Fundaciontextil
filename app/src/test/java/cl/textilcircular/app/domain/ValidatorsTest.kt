package cl.textilcircular.app.domain

import cl.textilcircular.app.domain.catalog.ChileGeo
import cl.textilcircular.app.domain.validation.Instagram
import cl.textilcircular.app.domain.validation.Rut
import cl.textilcircular.app.domain.validation.Validators
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidatorsTest {

    @Test
    fun `nombre exige dos palabras de al menos dos letras`() {
        assertNull(Validators.nombreCompleto("María José Pérez"))
        assertNotNull(Validators.nombreCompleto("Ma"))
        assertNotNull(Validators.nombreCompleto("María P"))
        assertNotNull(Validators.nombreCompleto("María 123"))
        assertNotNull(Validators.nombreCompleto("A".repeat(30) + " " + "B".repeat(31)))
    }

    @Test
    fun `nombre se normaliza con mayuscula inicial`() {
        assertEquals("María José Pérez", Validators.normalizarNombre("  maría   JOSÉ pérez "))
    }

    @Test
    fun `rut valida digito verificador modulo 11`() {
        assertNull(Validators.rut("12.345.678-5"))
        assertNull(Validators.rut("12345678-5"))
        assertNull(Validators.rut("11.111.111-1"))
        assertNotNull(Validators.rut("12.345.678-9"))
        assertNotNull(Validators.rut("123-4"))
        assertEquals("K", Rut.digitoVerificador("10000013"))
        assertEquals("12345678-5", Rut.normalizar("12.345.678-5"))
    }

    @Test
    fun `telefono acepta 9 digitos que parten con 9`() {
        assertNull(Validators.telefono("912345678"))
        assertNull(Validators.telefono("+56 9 1234 5678"))
        assertNotNull(Validators.telefono("12"))
        assertNotNull(Validators.telefono("812345678"))
        assertNotNull(Validators.telefono("9123abc78"))
        assertEquals("912345678", Validators.normalizarTelefono("+56912345678"))
    }

    @Test
    fun `instagram acepta usuario arroba o enlace`() {
        assertEquals("mi.tienda", Instagram.extraerUsuario("mi.tienda"))
        assertEquals("mi.tienda", Instagram.extraerUsuario("@mi.tienda"))
        assertEquals("mi_tienda", Instagram.extraerUsuario("https://www.instagram.com/mi_tienda/?hl=es"))
        assertEquals("https://www.instagram.com/mitienda/", Instagram.enlace("instagram.com/mitienda"))
        assertNotNull(Validators.instagram("mi tienda"))
        assertNull(Validators.instagram(""))
    }

    @Test
    fun `correo y contrasena`() {
        assertNull(Validators.email("socia@textilcircular.cl"))
        assertNotNull(Validators.email("socia@"))
        assertNotNull(Validators.password("1234567"))
        assertNull(Validators.password("12345678"))
        assertNotNull(Validators.confirmarPassword("12345678", "12345679"))
    }

    @Test
    fun `comuna debe pertenecer a la region`() {
        assertEquals(16, ChileGeo.regiones.size)
        assertEquals(346, ChileGeo.comunasPorRegion.values.sumOf { it.size })
        assertNull(Validators.comuna("Valparaíso", "Viña del Mar"))
        assertNotNull(Validators.comuna("Biobío", "Viña del Mar"))
        val todas = ChileGeo.comunasPorRegion.values.flatten()
        assertTrue("No debe haber comunas repetidas", todas.size == todas.toSet().size)
    }

    @Test
    fun `rubros entre uno y dos`() {
        assertNotNull(Validators.rubros(emptySet()))
        assertNull(Validators.rubros(setOf(1L, 2L)))
        assertNotNull(Validators.rubros(setOf(1L, 2L, 3L)))
    }
}
