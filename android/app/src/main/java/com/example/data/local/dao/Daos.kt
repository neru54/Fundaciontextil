package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.data.local.entity.AnnouncementEntity
import com.example.data.local.entity.CartItemEntity
import com.example.data.local.entity.CartLineView
import com.example.data.local.entity.ChatEntity
import com.example.data.local.entity.MessageEntity
import com.example.data.local.entity.OrderEntity
import com.example.data.local.entity.PartnerEntity
import com.example.data.local.entity.ProductEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    /** Las prendas nuevas aparecen primero; las iniciales mantienen su orden. */
    @Query("SELECT * FROM products ORDER BY createdAt DESC, id ASC")
    fun observeAll(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :id")
    fun observeById(id: Long): Flow<ProductEntity?>

    @Query("SELECT * FROM products WHERE partnerName = :partnerName ORDER BY createdAt DESC, id ASC")
    fun observeByPartner(partnerName: String): Flow<List<ProductEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(product: ProductEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(products: List<ProductEntity>)

    @Query("SELECT COUNT(*) FROM products")
    suspend fun count(): Int
}

@Dao
interface CartDao {
    @Query(
        """
        SELECT c.id AS cartId, p.id AS productId, p.title AS title,
               p.price AS price, p.partnerName AS partnerName
        FROM cart_items c
        INNER JOIN products p ON p.id = c.productId
        ORDER BY c.addedAt ASC
        """
    )
    fun observeLines(): Flow<List<CartLineView>>

    @Insert
    suspend fun insert(item: CartItemEntity): Long

    @Query("DELETE FROM cart_items WHERE id = :cartId")
    suspend fun deleteById(cartId: Long)

    @Query("DELETE FROM cart_items")
    suspend fun clear()
}

@Dao
interface AnnouncementDao {
    @Query("SELECT * FROM announcements ORDER BY id ASC")
    fun observeAll(): Flow<List<AnnouncementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<AnnouncementEntity>)

    /**
     * Marca la postulación y descuenta un cupo.
     * Devuelve 1 si se aplicó, 0 si ya había postulado o no quedan cupos.
     */
    @Query(
        """
        UPDATE announcements
        SET applied = 1, availableSpots = availableSpots - 1, applicationMotivation = :motivation
        WHERE id = :id AND applied = 0 AND availableSpots > 0
        """
    )
    suspend fun markApplied(id: Long, motivation: String): Int

    @Query("SELECT COUNT(*) FROM announcements")
    suspend fun count(): Int
}

@Dao
abstract class ChatDao {
    @Query("SELECT * FROM chats ORDER BY id ASC")
    abstract fun observeChats(): Flow<List<ChatEntity>>

    @Query("SELECT COALESCE(SUM(unreadCount), 0) FROM chats")
    abstract fun observeTotalUnread(): Flow<Int>

    @Query("SELECT * FROM messages WHERE chatId = :chatId ORDER BY createdAt ASC, id ASC")
    abstract fun observeMessages(chatId: Long): Flow<List<MessageEntity>>

    @Query("SELECT id FROM chats WHERE productTitle = :productTitle LIMIT 1")
    abstract suspend fun findIdByProductTitle(productTitle: String): Long?

    @Query("SELECT id FROM chats ORDER BY id ASC LIMIT 1")
    abstract suspend fun firstId(): Long?

    @Insert
    abstract suspend fun insertChat(chat: ChatEntity): Long

    @Insert
    abstract suspend fun insertMessage(message: MessageEntity): Long

    @Insert
    abstract suspend fun insertMessages(messages: List<MessageEntity>)

    @Query("UPDATE chats SET lastMessage = :text, time = :time WHERE id = :chatId")
    abstract suspend fun updateLastMessage(chatId: Long, text: String, time: String)

    @Query("UPDATE chats SET unreadCount = unreadCount + 1 WHERE id = :chatId")
    abstract suspend fun incrementUnread(chatId: Long)

    @Query("SELECT * FROM chats WHERE id = :chatId")
    abstract suspend fun findById(chatId: Long): ChatEntity?

    /** Mensaje entrante de una clienta: se guarda, se actualiza la vista previa y suma un no leído. */
    @Transaction
    open suspend fun receiveMessage(message: MessageEntity) {
        insertMessage(message)
        updateLastMessage(message.chatId, message.text, message.time)
        incrementUnread(message.chatId)
    }

    @Query("UPDATE chats SET unreadCount = 0 WHERE id = :chatId")
    abstract suspend fun markAsRead(chatId: Long)

    @Query("SELECT COUNT(*) FROM chats")
    abstract suspend fun count(): Int

    /** Guarda el mensaje y actualiza la vista previa del chat en una sola transacción. */
    @Transaction
    open suspend fun sendMessage(message: MessageEntity) {
        insertMessage(message)
        updateLastMessage(message.chatId, message.text, message.time)
    }

    /** Inserta un chat junto a su historial de mensajes. */
    @Transaction
    open suspend fun insertChatWithMessages(chat: ChatEntity, messages: List<MessageEntity>) {
        val chatId = insertChat(chat)
        insertMessages(messages.map { it.copy(chatId = chatId) })
    }
}

@Dao
interface PartnerDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(partner: PartnerEntity): Long

    @Query("SELECT * FROM partners WHERE email = :email LIMIT 1")
    suspend fun findByEmail(email: String): PartnerEntity?

    @Query("SELECT COUNT(*) FROM partners WHERE email = :email")
    suspend fun countByEmail(email: String): Int

    @Query("SELECT COUNT(*) FROM partners WHERE rut = :rut")
    suspend fun countByRut(rut: String): Int

    @Query("SELECT * FROM partners WHERE id = :id")
    fun observeById(id: Long): Flow<PartnerEntity?>

    @Query("SELECT COUNT(*) FROM partners")
    suspend fun count(): Int
}

@Dao
abstract class OrderDao {
    @Insert
    abstract suspend fun insert(order: OrderEntity): Long

    @Query("DELETE FROM cart_items")
    abstract suspend fun clearCart()

    @Query("SELECT COUNT(*) FROM orders")
    abstract fun observeCount(): Flow<Int>

    /** Guarda el pedido y vacía el carrito en una sola transacción. */
    @Transaction
    open suspend fun placeOrder(order: OrderEntity): Long {
        val id = insert(order)
        clearCart()
        return id
    }
}
