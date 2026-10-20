package com.example.ui.navigation

/** Rutas de navegación. */
object Routes {
    // Pestañas
    const val CATALOG = "catalog"
    const val ANNOUNCEMENTS = "announcements"
    const val MESSAGES = "messages"
    const val WORKSHOP = "workshop"

    // Detalle
    const val ARG_PRODUCT_ID = "productId"
    const val PRODUCT_DETAIL = "product/{$ARG_PRODUCT_ID}"
    fun productDetail(productId: Long) = "product/$productId"

    const val ARG_CHAT_ID = "chatId"
    const val CHAT = "chat/{$ARG_CHAT_ID}"
    fun chat(chatId: Long) = "chat/$chatId"

    const val CART = "cart"
    const val CHECKOUT = "checkout"

    // Acceso
    const val LOGIN = "login"
    const val REGISTER = "register"

    /** Rutas con barras principales. */
    val topLevel = setOf(CATALOG, ANNOUNCEMENTS, MESSAGES, WORKSHOP)
}
