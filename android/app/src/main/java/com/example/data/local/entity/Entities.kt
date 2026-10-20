package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** Tabla de prendas publicadas en el catálogo. */
@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
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
    val colorHex: Long,
    val imagePath: String?,
    /** Fecha de creación: permite mostrar primero las prendas recién publicadas. */
    val createdAt: Long = System.currentTimeMillis()
)

/** Tabla del carrito. Si se elimina la prenda, se elimina del carrito (CASCADE). */
@Entity(
    tableName = "cart_items",
    foreignKeys = [
        ForeignKey(
            entity = ProductEntity::class,
            parentColumns = ["id"],
            childColumns = ["productId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("productId")]
)
data class CartItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val productId: Long,
    val addedAt: Long = System.currentTimeMillis()
)

/** Tabla de convocatorias de la fundación. */
@Entity(tableName = "announcements")
data class AnnouncementEntity(
    @PrimaryKey val id: Long,
    val title: String,
    val date: String,
    val location: String,
    val category: String,
    val description: String,
    val requirements: String,
    val availableSpots: Int,
    val isExclusiveSocias: Boolean,
    val applied: Boolean = false,
    /** Motivo que la socia escribió al postular (null si no ha postulado). */
    val applicationMotivation: String? = null
)

/** Tabla de conversaciones con clientas. */
@Entity(tableName = "chats")
data class ChatEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val clientName: String,
    val clientEmail: String,
    val lastMessage: String,
    val time: String,
    val productTitle: String,
    val unreadCount: Int = 0
)

/** Tabla de mensajes. Cada mensaje pertenece a un chat. */
@Entity(
    tableName = "messages",
    foreignKeys = [
        ForeignKey(
            entity = ChatEntity::class,
            parentColumns = ["id"],
            childColumns = ["chatId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("chatId")]
)
data class MessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val chatId: Long,
    val sender: String,
    val isClient: Boolean,
    val text: String,
    val time: String,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Tabla de socias registradas.
 * El correo y el RUT son únicos. La contraseña nunca se guarda en texto plano:
 * solo su hash con una "sal" aleatoria (ver PasswordHasher).
 */
@Entity(
    tableName = "partners",
    indices = [
        Index(value = ["email"], unique = true),
        Index(value = ["rut"], unique = true)
    ]
)
data class PartnerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val rut: String,
    val email: String,
    val phone: String,
    val comuna: String,
    val workshopName: String,
    val passwordHash: String,
    val passwordSalt: String,
    val createdAt: Long = System.currentTimeMillis()
)

/** Tabla de pedidos confirmados desde el carrito. */
@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val buyerName: String,
    val address: String,
    val comuna: String,
    val phone: String,
    val total: Int,
    val itemCount: Int,
    val createdAt: Long = System.currentTimeMillis()
)

/** Resultado de la consulta JOIN entre cart_items y products. */
data class CartLineView(
    val cartId: Long,
    val productId: Long,
    val title: String,
    val price: Int,
    val partnerName: String
)
