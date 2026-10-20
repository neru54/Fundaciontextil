package com.example.domain.model

/**
 * Modelos de dominio usados por la interfaz.
 * No dependen de Room ni de Android, por lo que la UI no conoce
 * cómo se guardan los datos (separación de responsabilidades).
 */

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
    /** Ruta del archivo de la foto en el almacenamiento interno (null si no tiene foto). */
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
    /** true si la socia ya postuló a esta convocatoria. */
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

/** Línea del carrito: un producto agregado por la compradora. */
data class CartLine(
    val cartId: Long,
    val productId: Long,
    val title: String,
    val price: Int,
    val partnerName: String
)

/** Socia registrada en la fundación. */
data class Partner(
    val id: Long,
    val name: String,
    val rut: String,
    val email: String,
    val phone: String,
    val comuna: String,
    val workshopName: String
) {
    /** Iniciales para el avatar (ej: "Valentina Silva" -> "VS"). */
    val initials: String
        get() = name.split(" ").filter { it.isNotBlank() }.take(2)
            .joinToString("") { it.first().uppercase() }

    /** Código de socia que se muestra en el perfil (ej: TC-001). */
    val memberCode: String get() = "TC-%03d".format(id)
}

/** Datos del formulario de registro, ya validados. */
data class NewPartner(
    val name: String,
    val rut: String,
    val email: String,
    val phone: String,
    val comuna: String,
    val workshopName: String,
    val password: String
)

/** Datos de envío del checkout, ya validados. */
data class ShippingInfo(
    val fullName: String,
    val address: String,
    val comuna: String,
    val phone: String
)

/**
 * Socia de demostración que se crea la primera vez que se abre la app,
 * para poder probar el inicio de sesión sin registrarse.
 */
object DemoSocia {
    const val NAME = "Valentina Silva"
    const val RUT = "11111111-1"
    const val EMAIL = "valentina@textilcircular.cl"
    const val PHONE = "+56912345678"
    const val COMUNA = "Conchalí"
    const val WORKSHOP = "Taller Manos del Telar"
    const val PASSWORD = "telar2026"
}

/** Categorías disponibles en el catálogo. */
object ProductCategories {
    const val ALL = "Todas"
    val values = listOf("Chaquetas", "Bolsos", "Accesorios", "Decoración", "Retazos")
    val withAll = listOf(ALL) + values
}
