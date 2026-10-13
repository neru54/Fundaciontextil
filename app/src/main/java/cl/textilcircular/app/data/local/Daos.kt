package cl.textilcircular.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import cl.textilcircular.app.domain.model.EstadoFicha
import cl.textilcircular.app.domain.model.EstadoPublicacion
import kotlinx.coroutines.flow.Flow

@Dao
interface RubroDao {
    @Query("SELECT * FROM rubros ORDER BY nombre")
    fun observarTodos(): Flow<List<RubroEntity>>

    @Query("SELECT * FROM rubros ORDER BY nombre")
    suspend fun todos(): List<RubroEntity>

    @Query("SELECT COUNT(*) FROM rubros")
    suspend fun contar(): Int

    @Insert
    suspend fun insertar(rubros: List<RubroEntity>): List<Long>
}

@Dao
interface SociaDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertar(socia: SociaEntity): Long

    @Update
    suspend fun actualizar(socia: SociaEntity)

    @Query("SELECT * FROM socias WHERE id = :id")
    fun observar(id: Long): Flow<SociaEntity?>

    @Query("SELECT * FROM socias WHERE id = :id")
    suspend fun porId(id: Long): SociaEntity?

    @Query("SELECT * FROM socias WHERE email = :email COLLATE NOCASE LIMIT 1")
    suspend fun porEmail(email: String): SociaEntity?

    @Query("SELECT * FROM socias WHERE rut = :rut LIMIT 1")
    suspend fun porRut(rut: String): SociaEntity?

    @Query("UPDATE socias SET modoPausa = :pausa, estadoSync = 'PENDIENTE' WHERE id = :id")
    suspend fun cambiarModoPausa(id: Long, pausa: Boolean)

    @Query("UPDATE socias SET estadoFicha = :estado, estadoSync = 'PENDIENTE' WHERE id = :id")
    suspend fun cambiarEstadoFicha(id: Long, estado: EstadoFicha)

    @Query("UPDATE socias SET passwordHash = :hash, estadoSync = 'PENDIENTE' WHERE id = :id")
    suspend fun cambiarPassword(id: Long, hash: String)

    @Query("DELETE FROM socias WHERE id = :id")
    suspend fun eliminar(id: Long)

    @Query("SELECT COUNT(*) FROM socias")
    suspend fun contar(): Int
}

@Dao
interface PublicacionDao {

    @Query(
        """
        SELECT p.id, p.sociaId, p.tipo, p.titulo, p.descripcion, p.rubroId, r.nombre AS rubroNombre,
               p.precio, p.disponibilidad, p.diasElaboracion, p.estado, p.vistas, p.consultas,
               p.actualizadaEn, s.nombreEmprendimiento AS nombreTienda, s.region, s.comuna,
               (SELECT f.ruta FROM fotos f WHERE f.publicacionId = p.id ORDER BY f.orden LIMIT 1) AS portada
        FROM publicaciones p
        INNER JOIN socias s ON s.id = p.sociaId
        LEFT JOIN rubros r ON r.id = p.rubroId
        WHERE p.estado IN ('PUBLICADA', 'AGOTADA') AND s.estadoFicha = 'PUBLICADA' AND s.modoPausa = 0
          AND (:region IS NULL OR s.region = :region)
          AND (:comuna IS NULL OR s.comuna = :comuna)
          AND (:rubroId IS NULL OR p.rubroId = :rubroId)
          AND (:tipo IS NULL OR p.tipo = :tipo)
          AND (:sociaId IS NULL OR p.sociaId = :sociaId)
          AND (:texto = '' OR p.titulo LIKE '%' || :texto || '%'
               OR p.descripcion LIKE '%' || :texto || '%'
               OR s.nombreEmprendimiento LIKE '%' || :texto || '%')
        ORDER BY CASE WHEN p.estado = 'AGOTADA' THEN 1 ELSE 0 END,
                 CASE WHEN :orden = 'PRECIO_MENOR' THEN IFNULL(p.precio, 999999999) END ASC,
                 CASE WHEN :orden = 'PRECIO_MAYOR' THEN IFNULL(p.precio, -1) END DESC,
                 p.actualizadaEn DESC
        """
    )
    fun vitrina(
        texto: String,
        region: String?,
        comuna: String?,
        rubroId: Long?,
        tipo: String?,
        sociaId: Long?,
        orden: String
    ): Flow<List<PublicacionConTienda>>

    @Query(
        """
        SELECT p.id, p.sociaId, p.tipo, p.titulo, p.descripcion, p.rubroId, r.nombre AS rubroNombre,
               p.precio, p.disponibilidad, p.diasElaboracion, p.estado, p.vistas, p.consultas,
               p.actualizadaEn, s.nombreEmprendimiento AS nombreTienda, s.region, s.comuna,
               (SELECT f.ruta FROM fotos f WHERE f.publicacionId = p.id ORDER BY f.orden LIMIT 1) AS portada
        FROM publicaciones p
        INNER JOIN socias s ON s.id = p.sociaId
        LEFT JOIN rubros r ON r.id = p.rubroId
        WHERE p.sociaId = :sociaId
        ORDER BY p.actualizadaEn DESC
        """
    )
    fun deSocia(sociaId: Long): Flow<List<PublicacionConTienda>>

    @Query(
        """
        SELECT p.id, p.sociaId, p.tipo, p.titulo, p.descripcion, p.rubroId, r.nombre AS rubroNombre,
               p.precio, p.disponibilidad, p.diasElaboracion, p.estado, p.vistas, p.consultas,
               p.actualizadaEn, s.nombreEmprendimiento AS nombreTienda, s.region, s.comuna,
               (SELECT f.ruta FROM fotos f WHERE f.publicacionId = p.id ORDER BY f.orden LIMIT 1) AS portada
        FROM publicaciones p
        INNER JOIN socias s ON s.id = p.sociaId
        LEFT JOIN rubros r ON r.id = p.rubroId
        WHERE p.id = :id
        """
    )
    fun observarDetalle(id: Long): Flow<PublicacionConTienda?>

    @Query("SELECT * FROM publicaciones WHERE id = :id")
    suspend fun porId(id: Long): PublicacionEntity?

    @Query("SELECT ruta FROM fotos WHERE publicacionId = :publicacionId ORDER BY orden")
    fun observarFotos(publicacionId: Long): Flow<List<String>>

    @Query("SELECT ruta FROM fotos WHERE publicacionId = :publicacionId ORDER BY orden")
    suspend fun fotos(publicacionId: Long): List<String>

    @Query("SELECT f.ruta FROM fotos f INNER JOIN publicaciones p ON p.id = f.publicacionId WHERE p.sociaId = :sociaId")
    suspend fun fotosDeSocia(sociaId: Long): List<String>

    @Query("SELECT COUNT(*) FROM publicaciones WHERE sociaId = :sociaId")
    suspend fun contarDeSocia(sociaId: Long): Int

    @Insert
    suspend fun insertar(publicacion: PublicacionEntity): Long

    @Update
    suspend fun actualizar(publicacion: PublicacionEntity)

    @Insert
    suspend fun insertarFotos(fotos: List<FotoEntity>)

    @Query("DELETE FROM fotos WHERE publicacionId = :publicacionId")
    suspend fun eliminarFotos(publicacionId: Long)

    /** Reemplaza las fotos de una publicación respetando el orden elegido (la primera es la portada). */
    @Transaction
    suspend fun reemplazarFotos(publicacionId: Long, rutas: List<String>) {
        eliminarFotos(publicacionId)
        insertarFotos(rutas.mapIndexed { i, ruta -> FotoEntity(publicacionId = publicacionId, ruta = ruta, orden = i) })
    }

    @Query("UPDATE publicaciones SET estado = :estado, actualizadaEn = :ahora, estadoSync = 'PENDIENTE' WHERE id = :id")
    suspend fun cambiarEstado(id: Long, estado: EstadoPublicacion, ahora: Long)

    @Query("UPDATE publicaciones SET vistas = vistas + 1 WHERE id = :id")
    suspend fun sumarVista(id: Long)

    @Query("UPDATE publicaciones SET consultas = consultas + 1 WHERE id = :id")
    suspend fun sumarConsulta(id: Long)

    @Query("DELETE FROM publicaciones WHERE id = :id")
    suspend fun eliminar(id: Long)
}

@Dao
interface ConversacionDao {
    @Query("SELECT * FROM conversaciones WHERE sociaId = :sociaId ORDER BY actualizadaEn DESC")
    fun deSocia(sociaId: Long): Flow<List<ConversacionEntity>>

    @Query("SELECT * FROM conversaciones WHERE id = :id")
    fun observar(id: Long): Flow<ConversacionEntity?>

    @Query("SELECT * FROM conversaciones WHERE id = :id")
    suspend fun porId(id: Long): ConversacionEntity?

    @Query(
        """
        SELECT * FROM conversaciones
        WHERE sociaId = :sociaId AND clienteEmail = :email COLLATE NOCASE
          AND ((:publicacionId IS NULL AND publicacionId IS NULL) OR publicacionId = :publicacionId)
        LIMIT 1
        """
    )
    suspend fun buscar(sociaId: Long, email: String, publicacionId: Long?): ConversacionEntity?

    @Query("SELECT IFNULL(SUM(noLeidos), 0) FROM conversaciones WHERE sociaId = :sociaId")
    fun noLeidos(sociaId: Long): Flow<Int>

    @Insert
    suspend fun insertar(conversacion: ConversacionEntity): Long

    @Update
    suspend fun actualizar(conversacion: ConversacionEntity)

    @Query("UPDATE conversaciones SET noLeidos = 0 WHERE id = :id")
    suspend fun marcarLeida(id: Long)

    /** RN23: al eliminar la publicación la conversación conserva solo el título. */
    @Query("UPDATE conversaciones SET publicacionId = NULL WHERE publicacionId = :publicacionId")
    suspend fun desvincularPublicacion(publicacionId: Long)

    @Query("SELECT * FROM mensajes WHERE conversacionId = :conversacionId ORDER BY enviadoEn")
    fun mensajes(conversacionId: Long): Flow<List<MensajeEntity>>

    @Insert
    suspend fun insertarMensaje(mensaje: MensajeEntity): Long
}

@Dao
interface NovedadDao {
    @Query("SELECT * FROM novedades ORDER BY fecha DESC")
    fun todas(): Flow<List<NovedadEntity>>

    @Query("SELECT COUNT(*) FROM novedades WHERE leida = 0")
    fun noLeidas(): Flow<Int>

    @Query("UPDATE novedades SET leida = 1")
    suspend fun marcarTodasLeidas()

    @Insert
    suspend fun insertar(novedades: List<NovedadEntity>)

    @Query("SELECT COUNT(*) FROM novedades")
    suspend fun contar(): Int
}
