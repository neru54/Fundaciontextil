package cl.textilcircular.app.domain.rules

import cl.textilcircular.app.domain.model.EstadoFicha
import cl.textilcircular.app.domain.model.EstadoPublicacion
import cl.textilcircular.app.domain.model.Socia
import cl.textilcircular.app.domain.validation.Validators

/** Parámetros configurables del marketplace (RN06, RN21). */
object Limites {
    const val MAX_FOTOS_POR_PUBLICACION = 5
    const val MAX_PUBLICACIONES_POR_SOCIA = 20
    const val PRECIO_MINIMO = 500L
    const val PRECIO_MAXIMO = 10_000_000L
}

enum class AccionEstado(val etiqueta: String, val destino: EstadoPublicacion) {
    PUBLICAR("Publicar", EstadoPublicacion.PUBLICADA),
    PAUSAR("Pausar", EstadoPublicacion.PAUSADA),
    REACTIVAR("Reactivar", EstadoPublicacion.PUBLICADA),
    MARCAR_AGOTADA("Marcar como agotada", EstadoPublicacion.AGOTADA)
}

object ReglasMarketplace {

    /** RN19: estado visible + ficha publicada + sin Modo Pausa. */
    fun visibleEnVitrina(estado: EstadoPublicacion, estadoFicha: EstadoFicha, modoPausa: Boolean): Boolean =
        estado.visibleEnVitrina && estadoFicha == EstadoFicha.PUBLICADA && !modoPausa

    /** RN20: solo la dueña puede modificar su publicación. */
    fun esDuena(sociaIdPublicacion: Long, sociaIdSesion: Long?): Boolean =
        sociaIdSesion != null && sociaIdPublicacion == sociaIdSesion

    /** Acciones de estado disponibles desde cada estado (RF07.6). */
    fun accionesDisponibles(estado: EstadoPublicacion): List<AccionEstado> = when (estado) {
        EstadoPublicacion.BORRADOR -> listOf(AccionEstado.PUBLICAR)
        EstadoPublicacion.PUBLICADA -> listOf(AccionEstado.PAUSAR, AccionEstado.MARCAR_AGOTADA)
        EstadoPublicacion.PAUSADA -> listOf(AccionEstado.REACTIVAR)
        EstadoPublicacion.AGOTADA -> listOf(AccionEstado.REACTIVAR, AccionEstado.PAUSAR)
    }

    fun puedeCambiar(desde: EstadoPublicacion, accion: AccionEstado): Boolean =
        accion in accionesDisponibles(desde)

    /** RN06: tope de publicaciones por socia (las eliminadas no cuentan porque se borran). */
    fun puedeCrearPublicacion(cantidadActual: Int): Boolean =
        cantidadActual < Limites.MAX_PUBLICACIONES_POR_SOCIA

    /**
     * RF01.11 / RN10: lista de datos obligatorios que faltan o no cumplen las validaciones.
     * Reutiliza las mismas reglas del formulario de inscripción.
     */
    fun faltaCompletar(s: Socia): List<String> = buildList {
        if (Validators.nombreCompleto(s.nombre) != null) add("Nombre y apellido")
        if (Validators.telefono(s.telefono) != null) add("Teléfono (WhatsApp)")
        if (Validators.region(s.region) != null || Validators.comuna(s.region, s.comuna) != null) add("Región y comuna")
        if (Validators.nombreEmprendimiento(s.nombreEmprendimiento) != null) add("Nombre del emprendimiento")
        if (Validators.rubros(s.rubroIds) != null) add("Rubros")
        if (!s.instagram.isNullOrBlank() && Validators.instagram(s.instagram) != null) add("Instagram")
    }
}
