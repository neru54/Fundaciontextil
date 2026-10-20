package com.example.data.local

import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PasswordHasherTest {

    @Test
    fun `la contrasena correcta se verifica`() {
        val salt = PasswordHasher.newSalt()
        val hash = PasswordHasher.hash("telar2026", salt)
        assertTrue(PasswordHasher.verify("telar2026", salt, hash))
    }

    @Test
    fun `una contrasena incorrecta no se verifica`() {
        val salt = PasswordHasher.newSalt()
        val hash = PasswordHasher.hash("telar2026", salt)
        assertFalse(PasswordHasher.verify("telar2025", salt, hash))
    }

    @Test
    fun `el hash nunca es la contrasena y cambia con la sal`() {
        val hash1 = PasswordHasher.hash("telar2026", PasswordHasher.newSalt())
        val hash2 = PasswordHasher.hash("telar2026", PasswordHasher.newSalt())
        assertNotEquals("telar2026", hash1)
        assertNotEquals(hash1, hash2)
    }
}
