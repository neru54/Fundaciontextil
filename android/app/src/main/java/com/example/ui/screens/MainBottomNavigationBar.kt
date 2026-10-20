package com.example.ui.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import com.example.ui.components.BouncingCounter
import com.example.ui.navigation.Routes
import com.example.ui.theme.TurquesaClaro
import com.example.ui.theme.TurquesaOscuro
import com.example.ui.theme.TurquesaTextil

private data class BottomTab(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

private val bottomTabs = listOf(
    BottomTab(Routes.CATALOG, "Catálogo", Icons.Filled.Storefront, Icons.Outlined.Storefront, "nav_tab_catalog"),
    BottomTab(Routes.ANNOUNCEMENTS, "Anuncios", Icons.Filled.Campaign, Icons.Outlined.Campaign, "nav_tab_announcements"),
    BottomTab(Routes.MESSAGES, "Mensajes", Icons.AutoMirrored.Filled.Chat, Icons.AutoMirrored.Outlined.Chat, "nav_tab_messages"),
    BottomTab(Routes.WORKSHOP, "Mi Taller", Icons.Filled.Person, Icons.Outlined.Person, "nav_tab_workshop")
)

/** Barra inferior con contador de no leídos. */
@Composable
fun MainBottomNavigationBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    totalUnreadMessages: Int = 0,
    modifier: Modifier = Modifier
) {
    NavigationBar(modifier = modifier) {
        bottomTabs.forEach { tab ->
            val selected = currentRoute == tab.route
            NavigationBarItem(
                selected = selected,
                onClick = { onNavigate(tab.route) },
                icon = {
                    val icon = if (selected) tab.selectedIcon else tab.unselectedIcon
                    if (tab.route == Routes.MESSAGES && totalUnreadMessages > 0) {
                        BadgedBox(badge = {
                            Badge { BouncingCounter(count = totalUnreadMessages, color = androidx.compose.ui.graphics.Color.White) }
                        }) {
                            Icon(icon, contentDescription = tab.label)
                        }
                    } else {
                        Icon(icon, contentDescription = tab.label)
                    }
                },
                label = { Text(tab.label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = TurquesaTextil,
                    selectedTextColor = TurquesaOscuro,
                    indicatorColor = TurquesaClaro
                ),
                modifier = Modifier.testTag(tab.testTag)
            )
        }
    }
}
