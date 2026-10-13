package cl.textilcircular.app.domain.model

data class Rubro(val id: Long, val nombre: String)

data class Socia(
    val id: Long,
    val nombre: String,
    val rut: String,
    val email: String,
    val rangoEtario: String,
    val genero: String,
    val telefono: String,
    val codigoEmbajadora: String?,
    val region: String,
    val comuna: String,
    val nombreEmprendimiento: String,
    val formalizado: String,
    val rubroIds: List<Long>,
    val tiempoDedicado: String,
    val ingreso: String,
    val aspiracion: String?,
    val instagram: String?,
    val comoNosConociste: String,
    val comoNosConocisteOtro: String?,
    val quienInvito: String?,
    val consentimientoEn: Long,
    val estadoFicha: EstadoFicha,
    val modoPausa: Boolean,
    val creadaEn: Long
)

/** Datos públicos de una tienda que puede ver la clienta (RF01.5, RF06.4). */
data class TiendaPublica(
    val sociaId: Long,
    val nombreEmprendimiento: String,
    val nombreSocia: String,
    val region: String,
    val comuna: String,
    val rubros: List<String>,
    val telefono: String?,
    val instagramUrl: String?,
    val estadoFicha: EstadoFicha,
    val modoPausa: Boolean
) {
    /** RN19 (b) y (c): la tienda solo aparece si su ficha está publicada y no está en pausa. */
    val visibleEnVitrina: Boolean get() = estadoFicha == EstadoFicha.PUBLICADA && !modoPausa
}

data class Publicacion(
    val id: Long,
    val sociaId: Long,
    val tipo: TipoPublicacion,
    val titulo: String,
    val descripcion: String,
    val rubroId: Long,
    val rubroNombre: String,
    val precio: Int?,
    val disponibilidad: Disponibilidad,
    val diasElaboracion: Int?,
    val estado: EstadoPublicacion,
    val vistas: Int,
    val consultas: Int,
    val fotos: List<String>,
    val nombreTienda: String,
    val region: String,
    val comuna: String,
    val actualizadaEn: Long
) {
    val portada: String? get() = fotos.firstOrNull()
    val aConvenir: Boolean get() = precio == null
}

data class FiltrosVitrina(
    val texto: String = "",
    val region: String? = null,
    val comuna: String? = null,
    val rubroId: Long? = null,
    val tipo: TipoPublicacion? = null,
    val orden: OrdenVitrina = OrdenVitrina.RECIENTES
) {
    val cantidadActivos: Int
        get() = listOfNotNull(region, comuna, rubroId, tipo).size
}

data class ResumenTienda(
    val publicadas: Int = 0,
    val pausadas: Int = 0,
    val borradores: Int = 0,
    val agotadas: Int = 0,
    val vistas: Int = 0,
    val consultas: Int = 0
) {
    val total: Int get() = publicadas + pausadas + borradores + agotadas
}

data class Conversacion(
    val id: Long,
    val sociaId: Long,
    val clienteNombre: String,
    val clienteEmail: String,
    val publicacionId: Long?,
    val publicacionTitulo: String?,
    val ultimoMensaje: String,
    val noLeidos: Int,
    val actualizadaEn: Long
)

data class Mensaje(
    val id: Long,
    val conversacionId: Long,
    val texto: String,
    val deSocia: Boolean,
    val enviadoEn: Long
)

data class Novedad(
    val id: Long,
    val titulo: String,
    val cuerpo: String,
    val fecha: Long,
    val leida: Boolean
)

data class Clienta(val nombre: String, val email: String)
