package cl.textilcircular.app.data.repository

import cl.textilcircular.app.data.local.ConversacionDao
import cl.textilcircular.app.data.local.PublicacionDao
import cl.textilcircular.app.data.local.PublicacionEntity
import cl.textilcircular.app.data.local.RubroDao
import cl.textilcircular.app.data.local.toDomain
import cl.textilcircular.app.data.media.ImageStorage
import cl.textilcircular.app.domain.model.Disponibilidad
import cl.textilcircular.app.domain.model.EstadoPublicacion
import cl.textilcircular.app.domain.model.EstadoSync
import cl.textilcircular.app.domain.model.FiltrosVitrina
import cl.textilcircular.app.domain.model.Publicacion
import cl.textilcircular.app.domain.model.Rubro
import cl.textilcircular.app.domain.model.TipoPublicacion
import cl.textilcircular.app.domain.rules.AccionEstado
import cl.textilcircular.app.domain.rules.Limites
import cl.textilcircular.app.domain.rules.ReglasMarketplace
import cl.textilcircular.app.domain.validation.BorradorPublicacion
import cl.textilcircular.app.domain.validation.PublicacionValidator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/** Vitrina (RF06) y gestión de publicaciones de la socia (RF07). */
class MarketplaceRepository(
    private val publicacionDao: PublicacionDao,
    private val rubroDao: RubroDao,
    private val conversacionDao: ConversacionDao,
    private val imageStorage: ImageStorage
) {
    val rubros: Flow<List<Rubro>> = rubroDao.observarTodos().map { lista -> lista.map { it.toDomain() } }

    /** RF06.1, RF06.2, RF06.5: los filtros se resuelven en Room (RNF13). */
    fun vitrina(filtros: FiltrosVitrina, sociaId: Long? = null): Flow<List<Publicacion>> =
        publicacionDao.vitrina(
            texto = filtros.texto.trim(),
            region = filtros.region,
            comuna = filtros.comuna,
            rubroId = filtros.rubroId,
            tipo = filtros.tipo?.name,
            sociaId = sociaId,
            orden = filtros.orden.name
        ).map { lista -> lista.map { it.toDomain() } }

    fun misPublicaciones(sociaId: Long): Flow<List<Publicacion>> =
        publicacionDao.deSocia(sociaId).map { lista -> lista.map { it.toDomain() } }

    fun detalle(id: Long): Flow<Publicacion?> =
        combine(publicacionDao.observarDetalle(id), publicacionDao.observarFotos(id)) { fila, fotos ->
            fila?.toDomain(fotos)
        }

    /** RF06.8: trazabilidad local de vistas. */
    suspend fun registrarVista(id: Long) = withContext(Dispatchers.IO) { publicacionDao.sumarVista(id) }

    suspend fun cargarBorrador(id: Long, sociaId: Long): BorradorPublicacion? = withContext(Dispatchers.IO) {
        val p = publicacionDao.porId(id) ?: return@withContext null
        if (!ReglasMarketplace.esDuena(p.sociaId, sociaId)) return@withContext null
        BorradorPublicacion(
            fotos = publicacionDao.fotos(id),
            tipo = p.tipo,
            titulo = p.titulo,
            descripcion = p.descripcion,
            rubroId = p.rubroId,
            precioTexto = p.precio?.toString().orEmpty(),
            aConvenir = p.aConvenir,
            disponibilidad = p.disponibilidad,
            diasTexto = p.diasElaboracion?.toString().orEmpty()
        )
    }

    /**
     * Crea o actualiza una publicación (RF07.3 a RF07.5). Si [publicar] es true exige todas las
     * validaciones; si no, se guarda como borrador. Las fotos que ya no se usan se borran del dispositivo.
     */
    suspend fun guardar(
        sociaId: Long,
        id: Long?,
        b: BorradorPublicacion,
        publicar: Boolean
    ): Result<Long> = withContext(Dispatchers.IO) {
        runCatching {
            val errores = if (publicar) PublicacionValidator.validarParaPublicar(b)
            else PublicacionValidator.validarBorrador(b)
            if (errores.isNotEmpty()) throw ErrorNegocio(errores.values.first())

            val ahora = System.currentTimeMillis()
            val existente = id?.let { publicacionDao.porId(it) }
            if (existente != null && !ReglasMarketplace.esDuena(existente.sociaId, sociaId))
                throw ErrorNegocio("Esta publicación no te pertenece")
            if (existente == null && !ReglasMarketplace.puedeCrearPublicacion(publicacionDao.contarDeSocia(sociaId)))
                throw ErrorNegocio("Llegaste al máximo de ${Limites.MAX_PUBLICACIONES_POR_SOCIA} publicaciones")

            val estado = when {
                publicar && existente?.estado == EstadoPublicacion.AGOTADA -> EstadoPublicacion.AGOTADA
                publicar && existente?.estado == EstadoPublicacion.PAUSADA -> EstadoPublicacion.PAUSADA
                publicar -> EstadoPublicacion.PUBLICADA
                else -> existente?.estado ?: EstadoPublicacion.BORRADOR
            }
            val entidad = PublicacionEntity(
                id = existente?.id ?: 0,
                sociaId = sociaId,
                tipo = b.tipo ?: TipoPublicacion.PRODUCTO,
                titulo = b.titulo.trim().replace(Regex("\\s+"), " ").replaceFirstChar { it.titlecase() },
                descripcion = b.descripcion.trim(),
                rubroId = b.rubroId,
                precio = PublicacionValidator.parsePrecio(b.precioTexto, b.aConvenir),
                aConvenir = b.aConvenir,
                disponibilidad = b.disponibilidad ?: Disponibilidad.LISTA,
                diasElaboracion = if (b.disponibilidad == Disponibilidad.A_PEDIDO)
                    b.diasTexto.trim().toIntOrNull() else null,
                estado = estado,
                vistas = existente?.vistas ?: 0,
                consultas = existente?.consultas ?: 0,
                creadaEn = existente?.creadaEn ?: ahora,
                actualizadaEn = ahora,
                estadoSync = EstadoSync.PENDIENTE
            )
            val nuevoId = if (existente == null) publicacionDao.insertar(entidad)
            else entidad.id.also { publicacionDao.actualizar(entidad) }

            val anteriores = if (existente != null) publicacionDao.fotos(nuevoId) else emptyList()
            publicacionDao.reemplazarFotos(nuevoId, b.fotos)
            imageStorage.eliminar(anteriores - b.fotos.toSet())
            nuevoId
        }
    }

    /** RF07.6: Publicar, Pausar, Reactivar o Marcar como agotada. */
    suspend fun cambiarEstado(sociaId: Long, id: Long, accion: AccionEstado): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                val p = publicacionDao.porId(id) ?: throw ErrorNegocio("La publicación ya no existe")
                if (!ReglasMarketplace.esDuena(p.sociaId, sociaId)) throw ErrorNegocio("Esta publicación no te pertenece")
                if (!ReglasMarketplace.puedeCambiar(p.estado, accion))
                    throw ErrorNegocio("No se puede ${accion.etiqueta.lowercase()} desde \"${p.estado.etiqueta}\"")
                if (accion == AccionEstado.PUBLICAR) {
                    val b = cargarBorrador(id, sociaId)!!
                    val errores = PublicacionValidator.validarParaPublicar(b)
                    if (errores.isNotEmpty()) throw ErrorNegocio("Completa la publicación antes de publicarla: ${errores.values.first()}")
                }
                publicacionDao.cambiarEstado(id, accion.destino, System.currentTimeMillis())
            }
        }

    /** RF07.7 / RN23: elimina la publicación, sus fotos y deja la referencia textual en el chat. */
    suspend fun eliminar(sociaId: Long, id: Long): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val p = publicacionDao.porId(id) ?: return@runCatching
            if (!ReglasMarketplace.esDuena(p.sociaId, sociaId)) throw ErrorNegocio("Esta publicación no te pertenece")
            val fotos = publicacionDao.fotos(id)
            conversacionDao.desvincularPublicacion(id)
            publicacionDao.eliminar(id)
            imageStorage.eliminar(fotos)
        }
    }

    /** RF07.10: crea una copia de la publicación como borrador. */
    suspend fun duplicar(sociaId: Long, id: Long): Result<Long> = withContext(Dispatchers.IO) {
        runCatching {
            val p = publicacionDao.porId(id) ?: throw ErrorNegocio("La publicación ya no existe")
            if (!ReglasMarketplace.esDuena(p.sociaId, sociaId)) throw ErrorNegocio("Esta publicación no te pertenece")
            if (!ReglasMarketplace.puedeCrearPublicacion(publicacionDao.contarDeSocia(sociaId)))
                throw ErrorNegocio("Llegaste al máximo de ${Limites.MAX_PUBLICACIONES_POR_SOCIA} publicaciones")
            val ahora = System.currentTimeMillis()
            val titulo = ("Copia de " + p.titulo).take(60)
            val nuevoId = publicacionDao.insertar(
                p.copy(id = 0, titulo = titulo, estado = EstadoPublicacion.BORRADOR, vistas = 0, consultas = 0,
                    creadaEn = ahora, actualizadaEn = ahora, estadoSync = EstadoSync.PENDIENTE)
            )
            // Se copian los archivos para que borrar una publicación no deje sin fotos a la otra.
            publicacionDao.reemplazarFotos(nuevoId, imageStorage.copiar(publicacionDao.fotos(id)))
            nuevoId
        }
    }
}
