package com.example.domain.model

/** Modelos de dominio, independientes de Room y Android. */

data class Product(
    val id: Long = 0,
    val title: String,
    val price: Int,
    val category: String,
    val partnerName: String,
    val partnerComuna: String,
    val ecoTag: String,
    val kgSaved: Double,
    val litersWater: Int,
    val description: String,
    val fabricOrigin: String,
    val colorHex: Long = 0xFF00A896,
    val imagePath: String? = null
)

data class Announcement(
    val id: Long,
    val title: String,
    val date: String,
    val location: String,
    val category: String,
    val description: String,
    val requirements: String,
    val availableSpots: Int,
    val isExclusiveSocias: Boolean = true,
    val applied: Boolean = false
)

data class Chat(
    val id: Long = 0,
    val clientName: String,
    val clientEmail: String,
    val lastMessage: String,
    val time: String,
    val productTitle: String,
    val unreadCount: Int = 0
)

data class ChatMessage(
    val id: Long = 0,
    val chatId: Long = 0,
    val sender: String,
    val isClient: Boolean,
    val text: String,
    val time: String
)

data class CartLine(
    val cartId: Long,
    val productId: Long,
    val title: String,
    val price: Int,
    val partnerName: String
)

data class Partner(
    val id: Long,
    val name: String,
    val rut: String,
    val email: String,
    val phone: String,
    val comuna: String,
    val workshopName: String
) {
    /** "Valentina Silva" -> "VS" */
    val initials: String
        get() = name.split(" ").filter { it.isNotBlank() }.take(2)
            .joinToString("") { it.first().uppercase() }

    /** Ej: TC-001 */
    val memberCode: String get() = "TC-%03d".format(id)
}

data class NewPartner(
    val name: String,
    val rut: String,
    val email: String,
    val phone: String,
    val comuna: String,
    val workshopName: String,
    val password: String
)

data class ShippingInfo(
    val fullName: String,
    val address: String,
    val comuna: String,
    val phone: String
)

/** Socia de demostración para probar el login. */
object DemoSocia {
    const val NAME = "Valentina Silva"
    const val RUT = "11111111-1"
    const val EMAIL = "valentina@textilcircular.cl"
    const val PHONE = "+56912345678"
    const val COMUNA = "Conchalí"
    const val WORKSHOP = "Taller Manos del Telar"
    const val PASSWORD = "telar2026"
}

object ProductCategories {
    const val ALL = "Todas"
    val values = listOf("Chaquetas", "Bolsos", "Accesorios", "Decoración", "Retazos")
    val withAll = listOf(ALL) + values
}
