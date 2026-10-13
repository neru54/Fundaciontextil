package cl.textilcircular.app.data.repository

import cl.textilcircular.app.data.local.RubroDao
import cl.textilcircular.app.data.local.SociaDao
import cl.textilcircular.app.data.local.toDomain
import cl.textilcircular.app.data.local.toTiendaPublica
import cl.textilcircular.app.domain.model.EstadoFicha
import cl.textilcircular.app.domain.model.EstadoSync
import cl.textilcircular.app.domain.model.Socia
import cl.textilcircular.app.domain.model.TiendaPublica
import cl.textilcircular.app.domain.validation.Instagram
import cl.textilcircular.app.domain.validation.Validators
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/** Datos editables del perfil (RF01.3). */
data class CambiosPerfil(
    val nombre: String,
    val telefono: String,
    val region: String,
    val comuna: String,
    val nombreEmprendimiento: String,
    val rubroIds: Set<Long>,
    val instagram: String
)

/** Perfil de la socia, Modo Pausa y ficha pública de su tienda. */
class SociaRepository(
    private val sociaDao: SociaDao,
    private val rubroDao: RubroDao
) {
    fun observar(id: Long): Flow<Socia?> = sociaDao.observar(id).map { it?.toDomain() }

    /** RF06.4: ficha pública de la tienda, sin datos privados (RF01.5). */
    fun tiendaPublica(id: Long): Flow<TiendaPublica?> =
        combine(sociaDao.observar(id), rubroDao.observarTodos()) { socia, rubros ->
            socia?.toTiendaPublica(rubros.associate { it.id to it.nombre })
        }

    suspend fun actualizarPerfil(id: Long, c: CambiosPerfil): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val actual = sociaDao.porId(id) ?: throw ErrorNegocio("No encontramos tu cuenta")
            sociaDao.actualizar(
                actual.copy(
                    nombre = Validators.normalizarNombre(c.nombre),
                    telefono = Validators.normalizarTelefono(c.telefono),
                    region = c.region,
                    comuna = c.comuna,
                    nombreEmprendimiento = c.nombreEmprendimiento.trim(),
                    rubroIds = c.rubroIds.joinToString(","),
                    instagram = Instagram.extraerUsuario(c.instagram),
                    estadoSync = EstadoSync.PENDIENTE
                )
            )
        }
    }

    /** RF01.3 / RN24: oculta la tienda completa sin cambiar el estado de sus publicaciones. */
    suspend fun cambiarModoPausa(id: Long, pausa: Boolean) = withContext(Dispatchers.IO) {
        sociaDao.cambiarModoPausa(id, pausa)
    }

    /** RF01.12: en la demostración la validación entre pares se simula. */
    suspend fun simularValidacion(id: Long) = withContext(Dispatchers.IO) {
        sociaDao.cambiarEstadoFicha(id, EstadoFicha.PUBLICADA)
    }
}
