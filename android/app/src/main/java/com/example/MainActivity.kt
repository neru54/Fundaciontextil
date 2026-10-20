package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.domain.model.Partner
import com.example.domain.util.NotificationHelper
import com.example.ui.components.BouncingCounter
import com.example.ui.navigation.AppNavGraph
import com.example.ui.navigation.Routes
import com.example.ui.permissions.openAppSettings
import com.example.ui.screens.MainBottomNavigationBar
import com.example.ui.theme.AmarilloMostaza
import com.example.ui.theme.NegroCarbon
import com.example.ui.theme.TextilCircularTheme
import com.example.ui.theme.TurquesaTextil
import com.example.ui.viewmodel.AppViewModelProvider
import com.example.ui.viewmodel.MainViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    /** Chat abierto desde una notificación. */
    private var pendingChatId by mutableStateOf<Long?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NotificationHelper.createNotificationChannel(this)
        if (savedInstanceState == null) pendingChatId = intent.chatIdExtra()

        setContent {
            TextilCircularTheme {
                MainAppScreen(
                    pendingChatId = pendingChatId,
                    onDeepLinkHandled = { pendingChatId = null }
                )
            }
        }
    }

    /** Notificación tocada con la app abierta (singleTop). */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        pendingChatId = intent.chatIdExtra()
    }

    private fun Intent.chatIdExtra(): Long? =
        getLongExtra(NotificationHelper.EXTRA_CHAT_ID, -1L).takeIf { it > 0 }
}

/** Pantalla raíz: barras y NavHost. */
@Composable
fun MainAppScreen(
    pendingChatId: Long? = null,
    onDeepLinkHandled: () -> Unit = {},
    mainViewModel: MainViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val context = LocalContext.current
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val mainState by mainViewModel.uiState.collectAsStateWithLifecycle()

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val isTopLevel = currentRoute in Routes.topLevel

    val showSnackbar: (String) -> Unit = { message ->
        scope.launch { snackbarHostState.showSnackbar(message) }
    }

    val showPermissionDenied: (String) -> Unit = { message ->
        scope.launch {
            val result = snackbarHostState.showSnackbar(
                message = message,
                actionLabel = "Ajustes",
                duration = SnackbarDuration.Long
            )
            if (result == SnackbarResult.ActionPerformed) openAppSettings(context)
        }
    }

    LaunchedEffect(pendingChatId, mainState.isLoading) {
        val chatId = pendingChatId ?: return@LaunchedEffect
        if (mainState.isLoading) return@LaunchedEffect
        navController.navigate(Routes.chat(chatId)) { launchSingleTop = true }
        onDeepLinkHandled()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            AnimatedVisibility(
                visible = isTopLevel,
                enter = slideInVertically { -it } + fadeIn(),
                exit = slideOutVertically { -it } + fadeOut()
            ) {
                BrandTopBar(
                    partner = mainState.currentPartner,
                    cartCount = mainState.cartCount,
                    onCartClick = { navController.navigate(Routes.CART) }
                )
            }
        },
        bottomBar = {
            AnimatedVisibility(
                visible = isTopLevel,
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut()
            ) {
                MainBottomNavigationBar(
                    currentRoute = currentRoute,
                    totalUnreadMessages = mainState.unreadMessages,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        AppNavGraph(
            navController = navController,
            isSociaLoggedIn = mainState.isSociaLoggedIn,
            isSessionLoading = mainState.isLoading,
            showSnackbar = showSnackbar,
            onPermissionDenied = showPermissionDenied,
            // Los detalles tienen su propio Scaffold
            modifier = if (isTopLevel) Modifier.padding(paddingValues) else Modifier
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BrandTopBar(
    partner: Partner?,
    cartCount: Int,
    onCartClick: () -> Unit
) {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🧵", fontSize = 16.sp)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text("Textil Circular", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Color.White)
                    Text(
                        partner?.let { "Socia · ${it.name}" } ?: "Fundación · Modo Visitante",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }
            }
        },
        actions = {
            IconButton(onClick = onCartClick) {
                BadgedBox(
                    badge = {
                        if (cartCount > 0) {
                            Badge(containerColor = AmarilloMostaza) {
                                BouncingCounter(count = cartCount, color = NegroCarbon)
                            }
                        }
                    }
                ) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = "Carrito", tint = Color.White)
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = TurquesaTextil,
            titleContentColor = Color.White
        )
    )
}
