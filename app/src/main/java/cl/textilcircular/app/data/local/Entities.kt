package cl.textilcircular.app.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import cl.textilcircular.app.domain.model.Disponibilidad
import cl.textilcircular.app.domain.model.EstadoFicha
import cl.textilcircular.app.domain.model.EstadoPublicacion
import cl.textilcircular.app.domain.model.EstadoSync
import cl.textilcircular.app.domain.model.TipoPublicacion

/** Catálogo configurable de rubros (RN02). */
@Entity(tableName = "rubros")
data class RubroEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombre: String
)

@Entity(
    tableName = "socias",
    indices = [Index(value = ["email"], unique = true), Index(value = ["rut"], unique = true)]
)
data class SociaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombre: String,
    val rut: String,
    val email: String,
    val passwordHash: String,
    val rangoEtario: String,
    val genero: String,
    val telefono: String,
    val codigoEmbajadora: String?,
    val region: String,
    val comuna: String,
    val nombreEmprendimiento: String,
    val formalizado: String,
    /** Ids de rubros separados por coma (máximo 2, RN02). */
    val rubroIds: String,
    val tiempoDedicado: String,
    val ingreso: String,
    val aspiracion: String?,
    val instagram: String?,
    val comoNosConociste: String,
    val comoNosConocisteOtro: String?,
    val quienInvito: String?,
    val consentimientoEn: Long,
    val estadoFicha: EstadoFicha,
    val modoPausa: Boolean = false,
    val estadoSync: EstadoSync = EstadoSync.PENDIENTE,
    val creadaEn: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "publicaciones",
    foreignKeys = [ForeignKey(
        entity = SociaEntity::class,
        parentColumns = ["id"],
        childColumns = ["sociaId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("sociaId"), Index("estado"), Index("rubroId")]
)
data class PublicacionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sociaId: Long,
    val tipo: TipoPublicacion,
    val titulo: String,
    val descripcion: String,
    val rubroId: Long?,
    /** null cuando es "A convenir" o el borrador aún no tiene precio. */
    val precio: Int?,
    val aConvenir: Boolean = false,
    val disponibilidad: Disponibilidad,
    val diasElaboracion: Int?,
    val estado: EstadoPublicacion,
    val vistas: Int = 0,
    val consultas: Int = 0,
    val creadaEn: Long = System.currentTimeMillis(),
    val actualizadaEn: Long = System.currentTimeMillis(),
    val estadoSync: EstadoSync = EstadoSync.PENDIENTE
)

@Entity(
    tableName = "fotos",
    foreignKeys = [ForeignKey(
        entity = PublicacionEntity::class,
        parentColumns = ["id"],
        childColumns = ["publicacionId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("publicacionId")]
)
data class FotoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val publicacionId: Long,
    /** Ruta del archivo comprimido en el almacenamiento interno, o "seed:<n>" para datos de demostración. */
    val ruta: String,
    val orden: Int
)

@Entity(
    tableName = "conversaciones",
    foreignKeys = [ForeignKey(
        entity = SociaEntity::class,
        parentColumns = ["id"],
        childColumns = ["sociaId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("sociaId"), Index(value = ["sociaId", "clienteEmail", "publicacionId"])]
)
data class ConversacionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sociaId: Long,
    val clienteNombre: String,
    val clienteEmail: String,
    /** Referencia al producto (RF04.6); queda null si la publicación se elimina (RN23). */
    val publicacionId: Long?,
    val publicacionTitulo: String?,
    val ultimoMensaje: String,
    val noLeidos: Int,
    val actualizadaEn: Long
)

@Entity(
    tableName = "mensajes",
    foreignKeys = [ForeignKey(
        entity = ConversacionEntity::class,
        parentColumns = ["id"],
        childColumns = ["conversacionId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("conversacionId")]
)
data class MensajeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val conversacionId: Long,
    val texto: String,
    val deSocia: Boolean,
    val enviadoEn: Long = System.currentTimeMillis(),
    val estadoSync: EstadoSync = EstadoSync.PENDIENTE
)

/** Sección Novedades de la Fundación (RF04.5). */
@Entity(tableName = "novedades")
data class NovedadEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val titulo: String,
    val cuerpo: String,
    val fecha: Long,
    val leida: Boolean = false
)

/** Fila de la vitrina: publicación + datos públicos de su tienda + portada. */
data class PublicacionConTienda(
    val id: Long,
    val sociaId: Long,
    val tipo: TipoPublicacion,
    val titulo: String,
    val descripcion: String,
    val rubroId: Long?,
    val rubroNombre: String?,
    val precio: Int?,
    val disponibilidad: Disponibilidad,
    val diasElaboracion: Int?,
    val estado: EstadoPublicacion,
    val vistas: Int,
    val consultas: Int,
    val actualizadaEn: Long,
    val nombreTienda: String,
    val region: String,
    val comuna: String,
    val portada: String?
)
