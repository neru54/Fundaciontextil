package cl.textilcircular.app.data.security

import org.mindrot.jbcrypt.BCrypt

/** RNF11: la contraseña se guarda como hash BCrypt, nunca en texto plano. */
object PasswordHasher {
    private const val COSTO = 10

    fun hash(password: String): String = BCrypt.hashpw(password, BCrypt.gensalt(COSTO))

    fun verificar(password: String, hash: String): Boolean =
        runCatching { BCrypt.checkpw(password, hash) }.getOrDefault(false)
}
