package com.example.data.repository

import com.example.data.local.PasswordHasher
import com.example.data.local.SessionDataStore
import com.example.data.local.dao.AnnouncementDao
import com.example.data.local.dao.CartDao
import com.example.data.local.dao.ChatDao
import com.example.data.local.dao.OrderDao
import com.example.data.local.dao.PartnerDao
import com.example.data.local.dao.ProductDao
import com.example.data.local.entity.CartItemEntity
import com.example.data.local.entity.MessageEntity
import com.example.data.local.entity.OrderEntity
import com.example.data.local.entity.PartnerEntity
import com.example.data.local.toDomain
import com.example.data.local.toEntity
import com.example.domain.model.Announcement
import com.example.domain.model.CartLine
import com.example.domain.model.Chat
import com.example.domain.model.ChatMessage
import com.example.domain.model.NewPartner
import com.example.domain.model.Partner
import com.example.domain.model.Product
import com.example.domain.model.ShippingInfo
import com.example.domain.validation.ValidationRules
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Los repositorios son la única puerta de entrada a los datos.
 * Los ViewModels no saben si los datos vienen de Room, DataStore o una API.
 */

class ProductRepository(private val dao: ProductDao) {

    val products: Flow<List<Product>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    fun product(id: Long): Flow<Product?> =
        dao.observeById(id).map { it?.toDomain() }

    fun productsByPartner(partnerName: String): Flow<List<Product>> =
        dao.observeByPartner(partnerName).map { list -> list.map { it.toDomain() } }

    /** Guarda una prenda nueva y devuelve su id generado. */
    suspend fun publish(product: Product): Long =
        dao.insert(product.copy(id = 0).toEntity())
}

class CartRepository(private val dao: CartDao) {

    val lines: Flow<List<CartLine>> =
        dao.observeLines().map { list -> list.map { it.toDomain() } }

    val count: Flow<Int> = lines.map { it.size }

    suspend fun add(productId: Long) {
        dao.insert(CartItemEntity(productId = productId))
    }

    suspend fun remove(cartId: Long) = dao.deleteById(cartId)

    suspend fun clear() = dao.clear()
}

class AnnouncementRepository(private val dao: AnnouncementDao) {

    val announcements: Flow<List<Announcement>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    /** Devuelve true si la postulación se registró (false si ya postuló o no hay cupos). */
    suspend fun applyTo(announcementId: Long, motivation: String): Boolean =
        dao.markApplied(announcementId, motivation.trim()) > 0
}

class ChatRepository(private val dao: ChatDao) {

    val chats: Flow<List<Chat>> =
        dao.observeChats().map { list -> list.map { it.toDomain() } }

    val totalUnread: Flow<Int> = dao.observeTotalUnread()

    fun messages(chatId: Long): Flow<List<ChatMessage>> =
        dao.observeMessages(chatId).map { list -> list.map { it.toDomain() } }

    suspend fun sendReply(chatId: Long, sender: String, text: String) {
        val time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        dao.sendMessage(
            MessageEntity(
                chatId = chatId,
                sender = sender,
                isClient = false,
                text = text,
                time = time
            )
        )
    }

    suspend fun markAsRead(chatId: Long) = dao.markAsRead(chatId)

    /** Registra un mensaje que llega de la clienta del chat. Devuelve el chat actualizado. */
    suspend fun receiveClientMessage(chatId: Long, text: String): Chat? {
        val chat = dao.findById(chatId) ?: return null
        val time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        dao.receiveMessage(
            MessageEntity(chatId = chatId, sender = chat.clientName, isClient = true, text = text, time = time)
        )
        return chat.toDomain()
    }

    /** Busca el chat asociado a una prenda; si no existe, devuelve el primero. */
    suspend fun findChatIdForProduct(productTitle: String): Long? =
        dao.findIdByProductTitle(productTitle) ?: dao.firstId()
}

/** Resultado del registro de una socia. */
sealed interface RegisterResult {
    data class Success(val partnerId: Long) : RegisterResult
    data object EmailAlreadyUsed : RegisterResult
    data object RutAlreadyUsed : RegisterResult
}

/** Registro, inicio y cierre de sesión de socias. */
@OptIn(ExperimentalCoroutinesApi::class)
class AuthRepository(
    private val partnerDao: PartnerDao,
    private val session: SessionDataStore
) {
    /** Socia con sesión activa, o null si se navega como visitante. */
    val currentPartner: Flow<Partner?> = session.loggedPartnerId.flatMapLatest { id ->
        if (id == null) flowOf(null)
        else partnerDao.observeById(id).map { it?.toDomain() }
    }

    suspend fun register(input: NewPartner): RegisterResult {
        val email = input.email.trim().lowercase()
        val rut = ValidationRules.normalizeRut(input.rut)
        if (partnerDao.countByEmail(email) > 0) return RegisterResult.EmailAlreadyUsed
        if (partnerDao.countByRut(rut) > 0) return RegisterResult.RutAlreadyUsed

        val salt = PasswordHasher.newSalt()
        val id = partnerDao.insert(
            PartnerEntity(
                name = input.name.trim(),
                rut = rut,
                email = email,
                phone = input.phone.replace(" ", "").replace("-", ""),
                comuna = input.comuna.trim(),
                workshopName = input.workshopName.trim(),
                passwordHash = PasswordHasher.hash(input.password, salt),
                passwordSalt = salt
            )
        )
        session.setLoggedPartner(id)
        return RegisterResult.Success(id)
    }

    /** Devuelve true si el correo y la contraseña coinciden. */
    suspend fun login(email: String, password: String): Boolean {
        val partner = partnerDao.findByEmail(email.trim().lowercase()) ?: return false
        if (!PasswordHasher.verify(password, partner.passwordSalt, partner.passwordHash)) return false
        session.setLoggedPartner(partner.id)
        return true
    }

    suspend fun logout() = session.setLoggedPartner(null)
}

/** Pedidos confirmados desde el carrito. */
class OrderRepository(private val orderDao: OrderDao) {

    val orderCount: Flow<Int> = orderDao.observeCount()

    /** Guarda el pedido y vacía el carrito. Devuelve el número de pedido. */
    suspend fun placeOrder(shipping: ShippingInfo, lines: List<CartLine>): Long =
        orderDao.placeOrder(
            OrderEntity(
                buyerName = shipping.fullName.trim(),
                address = shipping.address.trim(),
                comuna = shipping.comuna.trim(),
                phone = shipping.phone.replace(" ", "").replace("-", ""),
                total = lines.sumOf { it.price },
                itemCount = lines.size
            )
        )
}
