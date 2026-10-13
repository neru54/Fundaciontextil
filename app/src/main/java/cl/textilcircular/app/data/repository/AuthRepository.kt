package cl.textilcircular.app.data.repository

import android.database.sqlite.SQLiteConstraintException
import cl.textilcircular.app.data.local.PublicacionDao
import cl.textilcircular.app.data.local.SociaDao
import cl.textilcircular.app.data.local.SociaEntity
import cl.textilcircular.app.data.media.ImageStorage
import cl.textilcircular.app.data.security.PasswordHasher
import cl.textilcircular.app.data.session.SessionStore
import cl.textilcircular.app.domain.model.EstadoFicha
import cl.textilcircular.app.domain.validation.CampoRegistro
import cl.textilcircular.app.domain.validation.Instagram
import cl.textilcircular.app.domain.validation.RegistroForm
import cl.textilcircular.app.domain.validation.Rut
import cl.textilcircular.app.domain.validation.Validators
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlin.random.Random

/** Error de negocio con mensaje para la usuaria y, si aplica, el campo que lo provocó. */
class ErrorNegocio(mensaje: String, val campo: CampoRegistro? = null) : Exception(mensaje)

/** Inscripción, inicio de sesión, recuperación de contraseña y baja (RF01). */
class AuthRepository(
    private val sociaDao: SociaDao,
    private val publicacionDao: PublicacionDao,
    private val session: SessionStore,
    private val imageStorage: ImageStorage
) {
    val sociaId: Flow<Long?> = session.sociaId

    /** Códigos de verificación pendientes por correo. El envío real es RF05.1 (etapa posterior). */
    private val codigosPendientes = mutableMapOf<String, String>()

    suspend fun registrar(f: RegistroForm): Result<Long> = withContext(Dispatchers.IO) {
        runCatching {
            val rut = Rut.normalizar(f.rut)
            val email = f.email.trim().lowercase()
            if (sociaDao.porRut(rut) != null)
                throw ErrorNegocio("Este RUT ya está registrado en otra cuenta", CampoRegistro.RUT)
            if (sociaDao.porEmail(email) != null)
                throw ErrorNegocio("Este correo ya tiene una cuenta. Inicia sesión", CampoRegistro.EMAIL)
            val entidad = SociaEntity(
                nombre = Validators.normalizarNombre(f.nombre),
                rut = rut,
                email = email,
                passwordHash = PasswordHasher.hash(f.password),
                rangoEtario = f.rangoEtario!!,
                genero = f.genero!!,
                telefono = Validators.normalizarTelefono(f.telefono),
                codigoEmbajadora = f.codigoEmbajadora.trim().ifBlank { null },
                region = f.region!!,
                comuna = f.comuna!!,
                nombreEmprendimiento = f.nombreEmprendimiento.trim(),
                formalizado = f.formalizado!!,
                rubroIds = f.rubros.joinToString(","),
                tiempoDedicado = f.tiempo!!,
                ingreso = f.ingreso!!,
                aspiracion = if (f.muestraAspiracion) f.aspiracion else null,
                instagram = Instagram.extraerUsuario(f.instagram),
                comoNosConociste = f.comoConociste!!,
                comoNosConocisteOtro = if (f.muestraOtro) f.comoConocisteOtro.trim() else null,
                quienInvito = if (f.muestraQuienInvito) Validators.normalizarNombre(f.quienInvito) else null,
                consentimientoEn = System.currentTimeMillis(),
                // RN14: una ficha nueva queda pendiente hasta ser validada.
                estadoFicha = EstadoFicha.PENDIENTE
            )
            val id = try {
                sociaDao.insertar(entidad)
            } catch (e: SQLiteConstraintException) {
                throw ErrorNegocio("El correo o el RUT ya están registrados")
            }
            session.iniciar(id)
            id
        }
    }

    suspend fun iniciarSesion(email: String, password: String): Result<Long> = withContext(Dispatchers.IO) {
        runCatching {
            val socia = sociaDao.porEmail(email.trim())
            if (socia == null || !PasswordHasher.verificar(password, socia.passwordHash))
                throw ErrorNegocio("Correo o contraseña incorrectos")
            session.iniciar(socia.id)
            socia.id
        }
    }

    suspend fun cerrarSesion() = session.cerrar()

    /**
     * RF01.10: genera un código de 6 dígitos. Sin servidor el envío se simula y el código
     * se devuelve para mostrarlo en pantalla (sección 1.1 del ERS).
     */
    suspend fun solicitarCodigo(email: String): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val correo = email.trim().lowercase()
            sociaDao.porEmail(correo) ?: throw ErrorNegocio("No encontramos una cuenta con ese correo")
            val codigo = Random.nextInt(100_000, 1_000_000).toString()
            codigosPendientes[correo] = codigo
            codigo
        }
    }

    suspend fun restablecerPassword(email: String, codigo: String, nueva: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                val correo = email.trim().lowercase()
                if (codigosPendientes[correo] != codigo) throw ErrorNegocio("El código no coincide")
                val socia = sociaDao.porEmail(correo) ?: throw ErrorNegocio("No encontramos la cuenta")
                codigosPendientes.remove(correo)
                sociaDao.cambiarPassword(socia.id, PasswordHasher.hash(nueva))
            }
        }

    /** RF01.13 / RN15 / RN24: baja definitiva con confirmación de contraseña. */
    suspend fun darseDeBaja(password: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val id = session.sociaIdActual() ?: throw ErrorNegocio("No hay una sesión iniciada")
            val socia = sociaDao.porId(id) ?: throw ErrorNegocio("No encontramos la cuenta")
            if (!PasswordHasher.verificar(password, socia.passwordHash))
                throw ErrorNegocio("La contraseña no es correcta")
            val fotos = publicacionDao.fotosDeSocia(id)
            // El borrado en cascada elimina publicaciones, fotos, conversaciones y mensajes.
            sociaDao.eliminar(id)
            imageStorage.eliminar(fotos)
            session.cerrar()
        }
    }
}
