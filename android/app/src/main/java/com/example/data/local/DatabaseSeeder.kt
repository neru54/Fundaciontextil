package com.example.data.local

import com.example.data.SampleData
import com.example.data.local.entity.PartnerEntity
import com.example.domain.model.DemoSocia

/** Carga los datos de ejemplo si la base está vacía. */
class DatabaseSeeder(private val db: AppDatabase) {

    suspend fun seedIfEmpty() {
        val productDao = db.productDao()
        if (productDao.count() == 0) {
            productDao.insertAll(SampleData.sampleProducts.map { it.toEntity(createdAt = 0L) })
        }

        val announcementDao = db.announcementDao()
        if (announcementDao.count() == 0) {
            announcementDao.insertAll(SampleData.sampleAnnouncements.map { it.toEntity() })
        }

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
                val entities = messages.mapIndexed { index, msg -> msg.toEntity(createdAt = index.toLong()) }
                chatDao.insertChatWithMessages(chat.toEntity(), entities)
            }
        }
    }
}
