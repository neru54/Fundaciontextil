package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

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
    val createdAt: Long = System.currentTimeMillis()
)

/** Se borra en cascada con la prenda. */
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
    /** null si no ha postulado. */
    val applicationMotivation: String? = null
)

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

/** Socias registradas. Correo y RUT únicos; solo se guarda el hash de la contraseña. */
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

/** JOIN cart_items + products. */
data class CartLineView(
    val cartId: Long,
    val productId: Long,
    val title: String,
    val price: Int,
    val partnerName: String
)
