package com.example.data.local

import java.security.MessageDigest
import java.security.SecureRandom

/**
 * Protege las contraseñas: se guarda SHA-256(sal + contraseña) aplicado muchas veces,
 * nunca la contraseña original. La sal es aleatoria y distinta para cada socia.
 */
object PasswordHasher {
    private const val ITERATIONS = 10_000
    private const val SALT_BYTES = 16

    fun newSalt(): String {
        val bytes = ByteArray(SALT_BYTES)
        SecureRandom().nextBytes(bytes)
        return bytes.toHex()
    }

    fun hash(password: String, salt: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        var result = (salt + password).toByteArray(Charsets.UTF_8)
        repeat(ITERATIONS) {
            digest.reset()
            result = digest.digest(result)
        }
        return result.toHex()
    }

    /** Compara en tiempo constante para no filtrar información por el tiempo de respuesta. */
    fun verify(password: String, salt: String, expectedHash: String): Boolean =
        MessageDigest.isEqual(
            hash(password, salt).toByteArray(Charsets.UTF_8),
            expectedHash.toByteArray(Charsets.UTF_8)
        )

    private fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it) }
}
