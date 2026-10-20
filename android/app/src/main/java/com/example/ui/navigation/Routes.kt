package com.example.ui.navigation

/**
 * Rutas de navegación de la app.
 * Las pantallas principales aparecen en la barra inferior; el resto se abre encima con flecha "volver".
 */
object Routes {
    // Pestañas principales (barra inferior)
    const val CATALOG = "catalog"
    const val ANNOUNCEMENTS = "announcements"
    const val MESSAGES = "messages"
    const val WORKSHOP = "workshop"

    // Pantallas de detalle
    const val ARG_PRODUCT_ID = "productId"
    const val PRODUCT_DETAIL = "product/{$ARG_PRODUCT_ID}"
    fun productDetail(productId: Long) = "product/$productId"

    const val ARG_CHAT_ID = "chatId"
    const val CHAT = "chat/{$ARG_CHAT_ID}"
    fun chat(chatId: Long) = "chat/$chatId"

    const val CART = "cart"
    const val CHECKOUT = "checkout"

    // Acceso de socias
    const val LOGIN = "login"
    const val REGISTER = "register"

    /** Rutas que muestran la barra superior de la marca y la barra inferior. */
    val topLevel = setOf(CATALOG, ANNOUNCEMENTS, MESSAGES, WORKSHOP)
}
