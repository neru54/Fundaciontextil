package cl.textilcircular.app.ui.navigation

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import cl.textilcircular.app.TextilApp
import cl.textilcircular.app.ui.EstadoSesion
import cl.textilcircular.app.ui.SesionViewModel
import cl.textilcircular.app.ui.appViewModel
import cl.textilcircular.app.ui.components.AccesoRequerido
import cl.textilcircular.app.ui.components.Cargando
import cl.textilcircular.app.ui.screens.auth.LoginScreen
import cl.textilcircular.app.ui.screens.auth.RecuperarScreen
import cl.textilcircular.app.ui.screens.auth.RegistroScreen
import cl.textilcircular.app.ui.screens.bienvenida.BienvenidaScreen
import cl.textilcircular.app.ui.screens.mensajes.ChatScreen
import cl.textilcircular.app.ui.screens.mensajes.MensajesScreen
import cl.textilcircular.app.ui.screens.mitienda.EditorScreen
import cl.textilcircular.app.ui.screens.mitienda.MiTiendaScreen
import cl.textilcircular.app.ui.screens.perfil.EditarPerfilScreen
import cl.textilcircular.app.ui.screens.perfil.NovedadesScreen
import cl.textilcircular.app.ui.screens.perfil.PerfilScreen
import cl.textilcircular.app.ui.screens.vitrina.ConsultaScreen
import cl.textilcircular.app.ui.screens.vitrina.DetalleScreen
import cl.textilcircular.app.ui.screens.vitrina.ExplorarScreen
import cl.textilcircular.app.ui.screens.vitrina.TiendaScreen

/** Intención pendiente que llega desde una notificación. */
sealed interface DestinoExterno {
    data class Chat(val conversacionId: Long) : DestinoExterno
    data object Novedades : DestinoExterno
}

@Composable
fun AppRaiz(destinoExterno: DestinoExterno?, alConsumirDestino: () -> Unit) {
    val sesionVm = appViewModel { c, _ -> SesionViewModel(c.authRepository, c.chatRepository) }
    val sesion by sesionVm.estado.collectAsStateWithLifecycle()
    val noLeidos by sesionVm.noLeidos.collectAsStateWithLifecycle()

    if (sesion == EstadoSesion.Cargando) {
        Cargando()
        return
    }
    val nav = rememberNavController()

    // La vitrina es la raíz del grafo; a quien entra sin sesión se le muestra primero la bienvenida.
    var bienvenidaMostrada by rememberSaveable { mutableStateOf(false) }

    // Recurso nativo 2: permiso de notificaciones (Android 13+) pedido al ingresar como socia.
    val context = LocalContext.current
    val notificaciones = remember { (context.applicationContext as TextilApp).container.notificationHelper }
    var notificacionesActivas by remember { mutableStateOf(notificaciones.tienePermiso()) }
    val permisoNotificaciones = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        notificacionesActivas = it
    }
    val pedirPermiso = {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permisoNotificaciones.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
    LaunchedEffect(sesion) {
        if (sesion is EstadoSesion.Socia && !notificacionesActivas) pedirPermiso()
    }

    val uriHandler = LocalUriHandler.current
    val abrirEnlace: (String) -> Unit = { url -> runCatching { uriHandler.openUri(url) } }

    val entrada by nav.currentBackStackEntryAsState()
    val rutaActual = entrada?.destination?.route
    val enPestana = Pestana.entries.any { it.ruta == rutaActual }

    Scaffold(
        bottomBar = {
            AnimatedVisibility(
                visible = enPestana,
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut()
            ) {
                NavigationBar {
                    Pestana.entries.forEach { p ->
                        val activa = rutaActual == p.ruta
                        NavigationBarItem(
                            selected = activa,
                            onClick = { nav.irAPestana(p.ruta) },
                            icon = {
                                BadgedBox(badge = {
                                    if (p == Pestana.MENSAJES && noLeidos > 0) Badge { Text("$noLeidos") }
                                }) {
                                    Icon(if (activa) p.iconoActivo else p.icono, contentDescription = null)
                                }
                            },
                            label = { Text(p.etiqueta) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        val idSocia = (sesion as? EstadoSesion.Socia)?.id
        val alIngresar = { nav.navigate(Rutas.LOGIN) }
        val alInscribirse = { nav.navigate(Rutas.REGISTRO) }

        NavHost(
            navController = nav,
            startDestination = Rutas.EXPLORAR,
            modifier = Modifier.padding(padding).consumeWindowInsets(padding),
            // Transiciones animadas entre pantallas (RNF07)
            enterTransition = { slideInHorizontally { it / 4 } + fadeIn() },
            exitTransition = { fadeOut() },
            popEnterTransition = { fadeIn() },
            popExitTransition = { slideOutHorizontally { it / 4 } + fadeOut() }
        ) {
            composable(Rutas.BIENVENIDA) {
                BienvenidaScreen(
                    alExplorar = { nav.popBackStack() },
                    alSoySocia = { nav.navigate(Rutas.LOGIN) { popUpTo(Rutas.BIENVENIDA) { inclusive = true } } }
                )
            }

            // ---------------------------------------------------------------- Vitrina (RF06)
            composable(Rutas.EXPLORAR) {
                ExplorarScreen(alAbrirPublicacion = { nav.navigate(Rutas.detalle(it)) })
            }
            composable(Rutas.DETALLE, arguments = listOf(navArgument("id") { type = NavType.LongType })) {
                DetalleScreen(
                    alVolver = { nav.popBackStack() },
                    alVerTienda = { nav.navigate(Rutas.tienda(it)) },
                    alConsultar = { sociaId, pubId -> nav.navigate(Rutas.consulta(sociaId, pubId)) },
                    alEditar = { nav.navigate(Rutas.editor(it)) }
                )
            }
            composable(
                Rutas.TIENDA,
                arguments = listOf(
                    navArgument("sociaId") { type = NavType.LongType },
                    navArgument("vistaPrevia") { type = NavType.BoolType; defaultValue = false }
                )
            ) {
                TiendaScreen(
                    alVolver = { nav.popBackStack() },
                    alAbrirPublicacion = { nav.navigate(Rutas.detalle(it)) },
                    alAbrirEnlace = abrirEnlace
                )
            }
            composable(
                Rutas.CONSULTA,
                arguments = listOf(
                    navArgument("sociaId") { type = NavType.LongType },
                    navArgument("publicacionId") { type = NavType.LongType; defaultValue = -1L }
                )
            ) {
                ConsultaScreen(alVolver = { nav.popBackStack() })
            }

            // ---------------------------------------------------------------- Acceso de socias (RF01)
            composable(Rutas.LOGIN) {
                LoginScreen(
                    alVolver = { nav.popBackStack() },
                    alIngresar = { nav.navigate(Rutas.MI_TIENDA) { popUpTo(Rutas.EXPLORAR) } },
                    alInscribirse = { nav.navigate(Rutas.REGISTRO) },
                    alRecuperar = { nav.navigate(Rutas.RECUPERAR) }
                )
            }
            composable(Rutas.REGISTRO) {
                RegistroScreen(
                    alVolver = { nav.popBackStack() },
                    alTerminar = { nav.navigate(Rutas.MI_TIENDA) { popUpTo(Rutas.EXPLORAR) } },
                    alAbrirEnlace = abrirEnlace
                )
            }
            composable(Rutas.RECUPERAR) {
                RecuperarScreen(alVolver = { nav.popBackStack() }, alTerminar = { nav.popBackStack() })
            }

            // ---------------------------------------------------------------- Apartado de la socia (RF07)
            composable(Rutas.MI_TIENDA) {
                if (idSocia == null) AccesoRequerido("Mi tienda", alIngresar, alInscribirse)
                else MiTiendaScreen(
                    alNueva = { nav.navigate(Rutas.editor()) },
                    alEditar = { nav.navigate(Rutas.editor(it)) },
                    alVerPublicacion = { nav.navigate(Rutas.detalle(it)) },
                    alVistaPrevia = { nav.navigate(Rutas.tienda(it, vistaPrevia = true)) },
                    alIrPerfil = { nav.irAPestana(Rutas.PERFIL) }
                )
            }
            composable(
                Rutas.EDITOR,
                arguments = listOf(navArgument("id") { type = NavType.LongType; defaultValue = -1L })
            ) {
                if (idSocia == null) AccesoRequerido("Mis publicaciones", alIngresar, alInscribirse)
                else EditorScreen(alVolver = { nav.popBackStack() })
            }

            // ---------------------------------------------------------------- Chat (RF04)
            composable(Rutas.MENSAJES) {
                if (idSocia == null) AccesoRequerido("Mensajes", alIngresar, alInscribirse)
                else MensajesScreen(alAbrirChat = { nav.navigate(Rutas.chat(it)) })
            }
            composable(Rutas.CHAT, arguments = listOf(navArgument("id") { type = NavType.LongType })) {
                if (idSocia == null) AccesoRequerido("Mensajes", alIngresar, alInscribirse)
                else ChatScreen(alVolver = { nav.popBackStack() }, alVerPublicacion = { nav.navigate(Rutas.detalle(it)) })
            }

            // ---------------------------------------------------------------- Perfil
            composable(Rutas.PERFIL) {
                if (idSocia == null) AccesoRequerido("Perfil", alIngresar, alInscribirse)
                else PerfilScreen(
                    alEditar = { nav.navigate(Rutas.EDITAR_PERFIL) },
                    alNovedades = { nav.navigate(Rutas.NOVEDADES) },
                    pedirPermisoNotificaciones = pedirPermiso,
                    notificacionesActivas = notificacionesActivas
                )
            }
            composable(Rutas.EDITAR_PERFIL) {
                if (idSocia == null) AccesoRequerido("Perfil", alIngresar, alInscribirse)
                else EditarPerfilScreen(alVolver = { nav.popBackStack() })
            }
            composable(Rutas.NOVEDADES) {
                if (idSocia == null) AccesoRequerido("Novedades", alIngresar, alInscribirse)
                else NovedadesScreen(alVolver = { nav.popBackStack() })
            }
        }

        // Efectos que navegan: van después del NavHost para que el grafo ya esté definido.
        LaunchedEffect(Unit) {
            if (!bienvenidaMostrada && sesion is EstadoSesion.Invitada) nav.navigate(Rutas.BIENVENIDA)
            bienvenidaMostrada = true
        }

        // Abrir la conversación o las novedades desde la notificación (RF04.1)
        LaunchedEffect(destinoExterno, sesion) {
            if (destinoExterno != null && sesion is EstadoSesion.Socia) {
                when (destinoExterno) {
                    is DestinoExterno.Chat -> nav.navigate(Rutas.chat(destinoExterno.conversacionId))
                    DestinoExterno.Novedades -> nav.navigate(Rutas.NOVEDADES)
                }
                alConsumirDestino()
            }
        }
    }
}

/** Navegación entre pestañas conservando el estado de cada una. */
private fun NavHostController.irAPestana(ruta: String) {
    navigate(ruta) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
