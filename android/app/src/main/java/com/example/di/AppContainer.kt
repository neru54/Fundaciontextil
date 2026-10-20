package com.example.di

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.local.DatabaseSeeder
import com.example.data.local.ImageStorage
import com.example.data.local.SessionDataStore
import com.example.data.repository.AnnouncementRepository
import com.example.data.repository.AuthRepository
import com.example.data.repository.CartRepository
import com.example.data.repository.ChatRepository
import com.example.data.repository.OrderRepository
import com.example.data.repository.ProductRepository

/**
 * Contenedor de dependencias (inyección manual).
 * Crea una sola vez la base de datos y los repositorios, y los comparte con los ViewModels.
 */
class AppContainer(context: Context) {

    private val database = AppDatabase.getInstance(context)

    val productRepository = ProductRepository(database.productDao())
    val cartRepository = CartRepository(database.cartDao())
    val announcementRepository = AnnouncementRepository(database.announcementDao())
    val chatRepository = ChatRepository(database.chatDao())
    val authRepository = AuthRepository(
        partnerDao = database.partnerDao(),
        session = SessionDataStore(context.applicationContext)
    )
    val orderRepository = OrderRepository(database.orderDao())
    val imageStorage = ImageStorage(context.applicationContext)

    val databaseSeeder = DatabaseSeeder(database)
}
