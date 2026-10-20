package com.example.data.local

import com.example.data.SampleData
import com.example.data.local.entity.PartnerEntity
import com.example.domain.model.DemoSocia

/**
 * Carga los datos de ejemplo solo cuando la base de datos está vacía
 * (primera ejecución). Después, todo lo que la usuaria agregue queda guardado.
 */
class DatabaseSeeder(private val db: AppDatabase) {

    suspend fun seedIfEmpty() {
        val productDao = db.productDao()
        if (productDao.count() == 0) {
            // createdAt = 0 para que las prendas nuevas siempre queden arriba
            productDao.insertAll(SampleData.sampleProducts.map { it.toEntity(createdAt = 0L) })
        }

        val announcementDao = db.announcementDao()
        if (announcementDao.count() == 0) {
            announcementDao.insertAll(SampleData.sampleAnnouncements.map { it.toEntity() })
        }

        // Socia de demostración para probar el inicio de sesión
        val partnerDao = db.partnerDao()
        if (partnerDao.count() == 0) {
            val salt = PasswordHasher.newSalt()
            partnerDao.insert(
                PartnerEntity(
                    name = DemoSocia.NAME,
                    rut = DemoSocia.RUT,
                    email = DemoSocia.EMAIL,
                    phone = DemoSocia.PHONE,
                    comuna = DemoSocia.COMUNA,
                    workshopName = DemoSocia.WORKSHOP,
                    passwordHash = PasswordHasher.hash(DemoSocia.PASSWORD, salt),
                    passwordSalt = salt
                )
            )
        }

        val chatDao = db.chatDao()
        if (chatDao.count() == 0) {
            SampleData.sampleChats.forEach { (chat, messages) ->
                // createdAt incremental para mantener el orden original de los mensajes
                val entities = messages.mapIndexed { index, msg -> msg.toEntity(createdAt = index.toLong()) }
                chatDao.insertChatWithMessages(chat.toEntity(), entities)
            }
        }
    }
}
