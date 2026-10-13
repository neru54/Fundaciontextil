package cl.textilcircular.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.TravelExplore
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material.icons.outlined.TravelExplore
import androidx.compose.ui.graphics.vector.ImageVector

/** Rutas de navegación (Anexo C del ERS). */
object Rutas {
    const val BIENVENIDA = "bienvenida"
    const val EXPLORAR = "explorar"
    const val DETALLE = "publicacion/{id}"
    const val TIENDA = "tienda/{sociaId}?vistaPrevia={vistaPrevia}"
    const val CONSULTA = "consulta/{sociaId}?publicacionId={publicacionId}"
    const val LOGIN = "login"
    const val REGISTRO = "registro"
    const val RECUPERAR = "recuperar"
    const val MI_TIENDA = "mi_tienda"
    const val EDITOR = "editor?id={id}"
    const val MENSAJES = "mensajes"
    const val CHAT = "chat/{id}"
    const val PERFIL = "perfil"
    const val EDITAR_PERFIL = "editar_perfil"
    const val NOVEDADES = "novedades"

    fun detalle(id: Long) = "publicacion/$id"
    fun tienda(sociaId: Long, vistaPrevia: Boolean = false) = "tienda/$sociaId?vistaPrevia=$vistaPrevia"
    fun consulta(sociaId: Long, publicacionId: Long?) =
        "consulta/$sociaId" + (publicacionId?.let { "?publicacionId=$it" } ?: "")
    fun editor(id: Long? = null) = if (id == null) "editor" else "editor?id=$id"
    fun chat(id: Long) = "chat/$id"
}

enum class Pestana(val ruta: String, val etiqueta: String, val icono: ImageVector, val iconoActivo: ImageVector) {
    EXPLORAR(Rutas.EXPLORAR, "Explorar", Icons.Outlined.TravelExplore, Icons.Filled.TravelExplore),
    MI_TIENDA(Rutas.MI_TIENDA, "Mi tienda", Icons.Outlined.Storefront, Icons.Filled.Storefront),
    MENSAJES(Rutas.MENSAJES, "Mensajes", Icons.AutoMirrored.Outlined.Chat, Icons.AutoMirrored.Filled.Chat),
    PERFIL(Rutas.PERFIL, "Perfil", Icons.Outlined.Person, Icons.Filled.Person)
}
