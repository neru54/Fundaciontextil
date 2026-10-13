package cl.textilcircular.app.data.repository

import cl.textilcircular.app.data.local.ConversacionDao
import cl.textilcircular.app.data.local.ConversacionEntity
import cl.textilcircular.app.data.local.MensajeEntity
import cl.textilcircular.app.data.local.NovedadDao
import cl.textilcircular.app.data.local.NovedadEntity
import cl.textilcircular.app.data.local.PublicacionDao
import cl.textilcircular.app.data.local.toDomain
import cl.textilcircular.app.data.session.SessionStore
import cl.textilcircular.app.domain.model.Clienta
import cl.textilcircular.app.domain.model.Conversacion
import cl.textilcircular.app.domain.model.Mensaje
import cl.textilcircular.app.domain.model.Novedad
import cl.textilcircular.app.domain.validation.Validators
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/** Aviso al llegar un mensaje o una novedad (RF04.1, RF04.5). Lo implementa la capa de plataforma. */
interface Notificador {
    fun nuevaConsulta(conversacionId: Long, clienteNombre: String, texto: String)
    fun nuevaNovedad(titulo: String)
}

/** Chat interno de texto simple (RF04.2, RF04.3, RN09) y consultas desde la vitrina (RF06.6). */
class ChatRepository(
    private val conversacionDao: ConversacionDao,
    private val publicacionDao: PublicacionDao,
    private val novedadDao: NovedadDao,
    private val session: SessionStore,
    private val notificador: Notificador
) {
    val clienta: Flow<Clienta?> = session.clienta

    fun conversaciones(sociaId: Long): Flow<List<Conversacion>> =
        conversacionDao.deSocia(sociaId).map { lista -> lista.map { it.toDomain() } }

    fun conversacion(id: Long): Flow<Conversacion?> = conversacionDao.observar(id).map { it?.toDomain() }

    fun mensajes(conversacionId: Long): Flow<List<Mensaje>> =
        conversacionDao.mensajes(conversacionId).map { lista -> lista.map { it.toDomain() } }

    fun noLeidos(sociaId: Long): Flow<Int> = conversacionDao.noLeidos(sociaId)

    suspend fun marcarLeida(conversacionId: Long) = withContext(Dispatchers.IO) {
        conversacionDao.marcarLeida(conversacionId)
    }

    /** La socia responde desde la app. */
    suspend fun responder(conversacionId: Long, texto: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            Validators.mensaje(texto)?.let { throw ErrorNegocio(it) }
            val conv = conversacionDao.porId(conversacionId) ?: throw ErrorNegocio("La conversación ya no existe")
            val ahora = System.currentTimeMillis()
            conversacionDao.insertarMensaje(MensajeEntity(conversacionId = conv.id, texto = texto.trim(), deSocia = true, enviadoEn = ahora))
            conversacionDao.actualizar(conv.copy(ultimoMensaje = texto.trim(), actualizadaEn = ahora))
        }
    }

    /**
     * RF06.6: la clienta escribe desde la vitrina. Crea o continúa la conversación, suma una consulta
     * a la publicación (RF06.8) y lanza la notificación (RF04.1).
     */
    suspend fun enviarConsulta(
        clienta: Clienta,
        sociaId: Long,
        publicacionId: Long?,
        texto: String
    ): Result<Long> = withContext(Dispatchers.IO) {
        runCatching {
            Validators.nombreClienta(clienta.nombre)?.let { throw ErrorNegocio(it) }
            Validators.email(clienta.email)?.let { throw ErrorNegocio(it) }
            Validators.mensaje(texto)?.let { throw ErrorNegocio(it) }
            val c = Clienta(Validators.normalizarNombre(clienta.nombre), clienta.email.trim().lowercase())
            session.guardarClienta(c)

            val titulo = publicacionId?.let { publicacionDao.porId(it)?.titulo }
            val ahora = System.currentTimeMillis()
            val existente = conversacionDao.buscar(sociaId, c.email, publicacionId)
            val conversacionId = if (existente != null) {
                conversacionDao.actualizar(
                    existente.copy(ultimoMensaje = texto.trim(), noLeidos = existente.noLeidos + 1, actualizadaEn = ahora)
                )
                existente.id
            } else {
                conversacionDao.insertar(
                    ConversacionEntity(
                        sociaId = sociaId, clienteNombre = c.nombre, clienteEmail = c.email,
                        publicacionId = publicacionId, publicacionTitulo = titulo,
                        ultimoMensaje = texto.trim(), noLeidos = 1, actualizadaEn = ahora
                    )
                )
            }
            conversacionDao.insertarMensaje(MensajeEntity(conversacionId = conversacionId, texto = texto.trim(), deSocia = false, enviadoEn = ahora))
            publicacionId?.let { publicacionDao.sumarConsulta(it) }
            notificador.nuevaConsulta(conversacionId, c.nombre, texto.trim())
            conversacionId
        }
    }

    /** Sección 1.1 del ERS: sin servidor, un botón simula la llegada de una consulta. */
    suspend fun simularConsulta(sociaId: Long): Result<Long> {
        val ejemplos = listOf(
            Clienta("Camila Rojas", "camila.rojas@ejemplo.cl") to "Hola, ¿haces arreglos de bastas? ¿Cuánto cobras?",
            Clienta("Javiera Soto", "javiera.soto@ejemplo.cl") to "Hola, vi tu tienda. ¿Haces envíos a Quilpué?",
            Clienta("Antonia Muñoz", "antonia.munoz@ejemplo.cl") to "¿Tienes talleres de reparación este mes?"
        )
        val (clienta, texto) = ejemplos.random()
        return enviarConsulta(clienta, sociaId, null, texto)
    }

    // ---------------------------------------------------------------- Novedades (RF04.5)

    val novedades: Flow<List<Novedad>> = novedadDao.todas().map { lista -> lista.map { it.toDomain() } }
    val novedadesNoLeidas: Flow<Int> = novedadDao.noLeidas()

    suspend fun marcarNovedadesLeidas() = withContext(Dispatchers.IO) { novedadDao.marcarTodasLeidas() }

    /** Sin servidor: simula que la Fundación publica una novedad y lanza la notificación. */
    suspend fun simularNovedad() = withContext(Dispatchers.IO) {
        val titulo = "Nueva feria circular en Valparaíso"
        novedadDao.insertar(
            listOf(
                NovedadEntity(
                    titulo = titulo,
                    cuerpo = "Inscríbete para participar con tu tienda en la feria del próximo mes. " +
                        "Los cupos son limitados y se asignan por orden de inscripción.",
                    fecha = System.currentTimeMillis()
                )
            )
        )
        notificador.nuevaNovedad(titulo)
    }
}
