package cl.textilcircular.app.domain

import cl.textilcircular.app.domain.model.Disponibilidad
import cl.textilcircular.app.domain.model.EstadoFicha
import cl.textilcircular.app.domain.model.EstadoPublicacion
import cl.textilcircular.app.domain.model.OpcionesFormulario
import cl.textilcircular.app.domain.model.TipoPublicacion
import cl.textilcircular.app.domain.rules.AccionEstado
import cl.textilcircular.app.domain.rules.Limites
import cl.textilcircular.app.domain.rules.ReglasMarketplace
import cl.textilcircular.app.domain.validation.BorradorPublicacion
import cl.textilcircular.app.domain.validation.CampoPublicacion
import cl.textilcircular.app.domain.validation.CampoRegistro
import cl.textilcircular.app.domain.validation.Precio
import cl.textilcircular.app.domain.validation.PublicacionValidator
import cl.textilcircular.app.domain.validation.RegistroForm
import cl.textilcircular.app.domain.validation.RegistroValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PublicacionYReglasTest {

    private val completa = BorradorPublicacion(
        fotos = listOf("a.jpg"),
        tipo = TipoPublicacion.PRODUCTO,
        titulo = "Chaqueta de mezclilla intervenida",
        descripcion = "Chaqueta reutilizada con parches bordados a mano.",
        rubroId = 1,
        precioTexto = "24.990",
        disponibilidad = Disponibilidad.LISTA
    )

    @Test
    fun `publicacion completa es valida`() {
        assertTrue(PublicacionValidator.validarParaPublicar(completa).isEmpty())
        assertEquals(24990, PublicacionValidator.parsePrecio("24.990", false))
    }

    @Test
    fun `publicar exige foto y precio en rango`() {
        val errores = PublicacionValidator.validarParaPublicar(completa.copy(fotos = emptyList(), precioTexto = "100"))
        assertTrue(CampoPublicacion.FOTOS in errores)
        assertTrue(CampoPublicacion.PRECIO in errores)
        assertNull(PublicacionValidator.precio("", aConvenir = true))
        assertNotNull(PublicacionValidator.precio("12,5", aConvenir = false))
        assertNotNull(PublicacionValidator.precio("10.000.001", aConvenir = false))
    }

    @Test
    fun `a pedido exige dias entre 1 y 60`() {
        val aPedido = completa.copy(disponibilidad = Disponibilidad.A_PEDIDO)
        assertTrue(CampoPublicacion.DIAS in PublicacionValidator.validarParaPublicar(aPedido))
        assertTrue(PublicacionValidator.validarParaPublicar(aPedido.copy(diasTexto = "7")).isEmpty())
        assertNotNull(PublicacionValidator.dias("61", Disponibilidad.A_PEDIDO))
    }

    @Test
    fun `borrador solo exige titulo`() {
        assertTrue(PublicacionValidator.validarBorrador(BorradorPublicacion(titulo = "Bolso")).isEmpty())
        assertTrue(CampoPublicacion.TITULO in PublicacionValidator.validarBorrador(BorradorPublicacion()))
        assertNotNull(PublicacionValidator.titulo("12345"))
    }

    @Test
    fun `precio se formatea con separador de miles`() {
        assertEquals("$24.990", Precio.formatear(24990))
        assertEquals("$500", Precio.formatear(500))
        assertEquals("$10.000.000", Precio.formatear(10_000_000))
        assertEquals("A convenir", Precio.formatear(null))
    }

    @Test
    fun `visibilidad en vitrina segun RN19`() {
        assertTrue(ReglasMarketplace.visibleEnVitrina(EstadoPublicacion.PUBLICADA, EstadoFicha.PUBLICADA, false))
        assertTrue(ReglasMarketplace.visibleEnVitrina(EstadoPublicacion.AGOTADA, EstadoFicha.PUBLICADA, false))
        assertFalse(ReglasMarketplace.visibleEnVitrina(EstadoPublicacion.BORRADOR, EstadoFicha.PUBLICADA, false))
        assertFalse(ReglasMarketplace.visibleEnVitrina(EstadoPublicacion.PAUSADA, EstadoFicha.PUBLICADA, false))
        assertFalse(ReglasMarketplace.visibleEnVitrina(EstadoPublicacion.PUBLICADA, EstadoFicha.PENDIENTE, false))
        assertFalse(ReglasMarketplace.visibleEnVitrina(EstadoPublicacion.PUBLICADA, EstadoFicha.PUBLICADA, true))
    }

    @Test
    fun `transiciones de estado y propiedad`() {
        assertTrue(ReglasMarketplace.puedeCambiar(EstadoPublicacion.BORRADOR, AccionEstado.PUBLICAR))
        assertFalse(ReglasMarketplace.puedeCambiar(EstadoPublicacion.BORRADOR, AccionEstado.PAUSAR))
        assertTrue(ReglasMarketplace.puedeCambiar(EstadoPublicacion.PAUSADA, AccionEstado.REACTIVAR))
        assertTrue(ReglasMarketplace.esDuena(5, 5))
        assertFalse(ReglasMarketplace.esDuena(5, 6))
        assertFalse(ReglasMarketplace.esDuena(5, null))
        assertTrue(ReglasMarketplace.puedeCrearPublicacion(Limites.MAX_PUBLICACIONES_POR_SOCIA - 1))
        assertFalse(ReglasMarketplace.puedeCrearPublicacion(Limites.MAX_PUBLICACIONES_POR_SOCIA))
    }

    @Test
    fun `registro muestra campos condicionales`() {
        val f = RegistroForm(ingreso = OpcionesFormulario.INGRESO_PRINCIPAL, comoConociste = OpcionesFormulario.RECOMENDACION)
        assertFalse(f.muestraAspiracion)
        assertTrue(f.muestraQuienInvito)
        val errores = RegistroValidator.validarPaso(4, f)
        assertTrue(CampoRegistro.QUIEN_INVITO in errores)
        assertFalse(CampoRegistro.ASPIRACION in errores)
        assertTrue(CampoRegistro.CONSENTIMIENTO in RegistroValidator.validarPaso(5, f))
        assertEquals(2, RegistroValidator.pasoDe(CampoRegistro.COMUNA))
    }
}
