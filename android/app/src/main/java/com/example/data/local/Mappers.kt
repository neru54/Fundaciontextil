package com.example.data.local

import com.example.data.local.entity.AnnouncementEntity
import com.example.data.local.entity.CartLineView
import com.example.data.local.entity.ChatEntity
import com.example.data.local.entity.MessageEntity
import com.example.data.local.entity.PartnerEntity
import com.example.data.local.entity.ProductEntity
import com.example.domain.model.Announcement
import com.example.domain.model.CartLine
import com.example.domain.model.Chat
import com.example.domain.model.ChatMessage
import com.example.domain.model.Partner
import com.example.domain.model.Product

/** Conversión entre entidades de Room y modelos de dominio. */

fun ProductEntity.toDomain() = Product(
    id = id,
    title = title,
    price = price,
    category = category,
    partnerName = partnerName,
    partnerComuna = partnerComuna,
    ecoTag = ecoTag,
    kgSaved = kgSaved,
    litersWater = litersWater,
    description = description,
    fabricOrigin = fabricOrigin,
    colorHex = colorHex,
    imagePath = imagePath
)

fun Product.toEntity(createdAt: Long = System.currentTimeMillis()) = ProductEntity(
    id = id,
    title = title,
    price = price,
    category = category,
    partnerName = partnerName,
    partnerComuna = partnerComuna,
    ecoTag = ecoTag,
    kgSaved = kgSaved,
    litersWater = litersWater,
    description = description,
    fabricOrigin = fabricOrigin,
    colorHex = colorHex,
    imagePath = imagePath,
    createdAt = createdAt
)

fun AnnouncementEntity.toDomain() = Announcement(
    id = id,
    title = title,
    date = date,
    location = location,
    category = category,
    description = description,
    requirements = requirements,
    availableSpots = availableSpots,
    isExclusiveSocias = isExclusiveSocias,
    applied = applied
)

fun Announcement.toEntity() = AnnouncementEntity(
    id = id,
    title = title,
    date = date,
    location = location,
    category = category,
    description = description,
    requirements = requirements,
    availableSpots = availableSpots,
    isExclusiveSocias = isExclusiveSocias,
    applied = applied
)

fun ChatEntity.toDomain() = Chat(
    id = id,
    clientName = clientName,
    clientEmail = clientEmail,
    lastMessage = lastMessage,
    time = time,
    productTitle = productTitle,
    unreadCount = unreadCount
)

fun Chat.toEntity() = ChatEntity(
    id = id,
    clientName = clientName,
    clientEmail = clientEmail,
    lastMessage = lastMessage,
    time = time,
    productTitle = productTitle,
    unreadCount = unreadCount
)

fun MessageEntity.toDomain() = ChatMessage(
    id = id,
    chatId = chatId,
    sender = sender,
    isClient = isClient,
    text = text,
    time = time
)

fun ChatMessage.toEntity(createdAt: Long = System.currentTimeMillis()) = MessageEntity(
    id = id,
    chatId = chatId,
    sender = sender,
    isClient = isClient,
    text = text,
    time = time,
    createdAt = createdAt
)

fun CartLineView.toDomain() = CartLine(
    cartId = cartId,
    productId = productId,
    title = title,
    price = price,
    partnerName = partnerName
)

fun PartnerEntity.toDomain() = Partner(
    id = id,
    name = name,
    rut = rut,
    email = email,
    phone = phone,
    comuna = comuna,
    workshopName = workshopName
)
