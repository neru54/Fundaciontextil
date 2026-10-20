package com.example.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.domain.util.NotificationHelper
import com.example.domain.util.VibrationHelper
import com.example.ui.permissions.rememberNotificationPermissionGate
import com.example.ui.components.SociaRequiredScreen
import com.example.ui.screens.AnnouncementsScreen
import com.example.ui.screens.CartScreen
import com.example.ui.screens.CatalogScreen
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.CheckoutScreen
import com.example.ui.screens.MessagesScreen
import com.example.ui.screens.ProductDetailScreen
import com.example.ui.screens.WorkshopProfileScreen
import com.example.ui.screens.auth.LoginScreen
import com.example.ui.screens.auth.RegisterScreen
import com.example.ui.theme.TurquesaTextil

/**
 * Mapa de navegación de la app: qué pantalla corresponde a cada ruta
 * y cómo se pasa de una a otra.
 */
@Composable
fun AppNavGraph(
    navController: NavHostController,
    isSociaLoggedIn: Boolean,
    isSessionLoading: Boolean,
    showSnackbar: (String) -> Unit,
    /** Muestra un aviso con el botón "Ajustes" cuando se niega un permiso. */
    onPermissionDenied: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Pide el permiso de notificaciones (Android 13+) explicando antes para qué se usa
    val notificationGate = rememberNotificationPermissionGate(
        onDenied = { onPermissionDenied("Notificaciones desactivadas. Puedes activarlas en Ajustes.") }
    )

    // Protege las rutas exclusivas para socias: sin sesión se ofrece iniciarla o registrarse
    val sociaOnly: @Composable (title: String, description: String, content: @Composable () -> Unit) -> Unit =
        { title, description, content ->
            when {
                isSessionLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = TurquesaTextil)
                }
                isSociaLoggedIn -> content()
                else -> SociaRequiredScreen(
                    title = title,
                    description = description,
                    onLoginClick = { navController.navigate(Routes.LOGIN) },
                    onRegisterClick = { navController.navigate(Routes.REGISTER) }
                )
            }
        }

    NavHost(
        navController = navController,
        startDestination = Routes.CATALOG,
        modifier = modifier,
        // Transiciones: entre pestañas un fundido; al abrir un detalle, desliza desde la derecha
        enterTransition = { enterAnimation() },
        exitTransition = { exitAnimation() },
        popEnterTransition = { popEnterAnimation() },
        popExitTransition = { popExitAnimation() }
    ) {
        // ---------- Pestañas principales ----------

        composable(Routes.CATALOG) {
            CatalogScreen(
                onProductClick = { id -> navController.navigate(Routes.productDetail(id)) },
                onAddedToCart = { product -> showSnackbar("Agregado al carrito: ${product.title}") }
            )
        }

        composable(Routes.ANNOUNCEMENTS) {
            sociaOnly(
                "Anuncios Exclusivos para Socias",
                "Las convocatorias a ferias, talleres y entregas de fardos textiles son solo para socias de la fundación."
            ) { AnnouncementsScreen() }
        }

        composable(Routes.MESSAGES) {
            sociaOnly(
                "Mensajes de Clientas",
                "Inicia sesión para responder las consultas que las clientas hacen sobre tus prendas."
            ) {
                MessagesScreen(
                    onOpenChat = { chatId -> navController.navigate(Routes.chat(chatId)) },
                    onIncomingMessage = { chatId, clientName, text ->
                        // Recursos nativos: vibración + notificación que abre el chat al tocarla
                        VibrationHelper.vibrateNewMessage(context)
                        notificationGate.runWithPermission {
                            NotificationHelper.showClientMessageNotification(
                                context = context,
                                notificationId = chatId.toInt(),
                                clientName = clientName,
                                messagePreview = text,
                                chatId = chatId
                            )
                        }
                        showSnackbar("Nuevo mensaje de $clientName")
                    }
                )
            }
        }

        composable(Routes.WORKSHOP) {
            sociaOnly(
                "Mi Taller",
                "Crea tu cuenta de socia para publicar tus prendas upcycling y ver el impacto ambiental de tu taller."
            ) {
                WorkshopProfileScreen(
                    onNewProductPublished = { showSnackbar("Prenda publicada con éxito en el catálogo") },
                    onLogout = {
                        showSnackbar("Sesión cerrada")
                        navController.navigate(Routes.CATALOG) {
                            popUpTo(Routes.CATALOG) { inclusive = true }
                        }
                    }
                )
            }
        }

        // ---------- Pantallas de detalle ----------

        composable(
            route = Routes.PRODUCT_DETAIL,
            arguments = listOf(navArgument(Routes.ARG_PRODUCT_ID) { type = NavType.LongType })
        ) {
            ProductDetailScreen(
                onBack = { navController.popBackStack() },
                onAddedToCart = { product -> showSnackbar("Agregado al carrito: ${product.title}") },
                onOpenChat = { chatId, product ->
                    // Notificación nativa que avisa de la nueva consulta (si hay permiso)
                    notificationGate.runWithPermission {
                        NotificationHelper.showClientMessageNotification(
                            context = context,
                            notificationId = 1002,
                            clientName = product.partnerName,
                            messagePreview = "Se abrió un canal directo para consultar sobre: ${product.title}",
                            productTitle = product.title,
                            chatId = chatId
                        )
                    }
                    navController.navigate(Routes.chat(chatId))
                }
            )
        }

        composable(
            route = Routes.CHAT,
            arguments = listOf(navArgument(Routes.ARG_CHAT_ID) { type = NavType.LongType })
        ) { entry ->
            val chatId = entry.arguments?.getLong(Routes.ARG_CHAT_ID) ?: return@composable
            sociaOnly(
                "Conversación con Clientas",
                "Inicia sesión como socia para leer y responder esta conversación."
            ) {
                ChatScreen(chatId = chatId, onBack = { navController.popBackStack() })
            }
        }

        composable(Routes.CART) {
            CartScreen(
                onBack = { navController.popBackStack() },
                onCheckout = { navController.navigate(Routes.CHECKOUT) },
                onExploreCatalog = {
                    navController.navigate(Routes.CATALOG) {
                        popUpTo(Routes.CATALOG) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.CHECKOUT) {
            CheckoutScreen(
                onBack = { navController.popBackStack() },
                onOrderPlaced = { orderId ->
                    showSnackbar("¡Pedido #$orderId confirmado! Tu compra apoya el comercio justo.")
                    // Vuelve al catálogo y saca el carrito y el checkout del historial
                    navController.popBackStack(Routes.CATALOG, inclusive = false)
                }
            )
        }

        // ---------- Acceso de socias ----------

        composable(Routes.LOGIN) {
            LoginScreen(
                onBack = { navController.popBackStack() },
                onLoggedIn = {
                    showSnackbar("¡Hola de nuevo! Sesión iniciada")
                    navController.popBackStack()
                },
                onGoToRegister = {
                    navController.navigate(Routes.REGISTER) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.REGISTER) {
            RegisterScreen(
                onBack = { navController.popBackStack() },
                onRegistered = {
                    showSnackbar("¡Bienvenida a la Fundación! Tu cuenta fue creada")
                    navController.navigate(Routes.WORKSHOP) {
                        popUpTo(Routes.CATALOG)
                    }
                },
                onGoToLogin = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.REGISTER) { inclusive = true }
                    }
                }
            )
        }
    }
}

// ---------- Animaciones de transición entre pantallas ----------

private const val TRANSITION_MS = 320

/** true cuando se cambia de una pestaña principal a otra (no hay "profundidad"). */
private fun AnimatedContentTransitionScope<NavBackStackEntry>.isTabSwitch(): Boolean =
    initialState.destination.route in Routes.topLevel && targetState.destination.route in Routes.topLevel

private fun AnimatedContentTransitionScope<NavBackStackEntry>.enterAnimation(): EnterTransition =
    if (isTabSwitch()) fadeIn(tween(220))
    else slideIntoContainer(SlideDirection.Start, tween(TRANSITION_MS)) + fadeIn(tween(TRANSITION_MS))

private fun AnimatedContentTransitionScope<NavBackStackEntry>.exitAnimation(): ExitTransition =
    if (isTabSwitch()) fadeOut(tween(180))
    else slideOutOfContainer(SlideDirection.Start, tween(TRANSITION_MS)) { it / 4 } + fadeOut(tween(TRANSITION_MS))

private fun AnimatedContentTransitionScope<NavBackStackEntry>.popEnterAnimation(): EnterTransition =
    if (isTabSwitch()) fadeIn(tween(220))
    else slideIntoContainer(SlideDirection.End, tween(TRANSITION_MS)) { it / 4 } + fadeIn(tween(TRANSITION_MS))

private fun AnimatedContentTransitionScope<NavBackStackEntry>.popExitAnimation(): ExitTransition =
    if (isTabSwitch()) fadeOut(tween(180))
    else slideOutOfContainer(SlideDirection.End, tween(TRANSITION_MS)) + fadeOut(tween(TRANSITION_MS))
