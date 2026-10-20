package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.AuthRepository
import com.example.data.repository.CartRepository
import com.example.data.repository.ChatRepository
import com.example.domain.model.Partner
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/** Estado global que muestran la barra superior y la barra de navegación. */
data class MainUiState(
    val currentPartner: Partner? = null,
    val unreadMessages: Int = 0,
    val cartCount: Int = 0,
    /** true mientras se lee la sesión guardada (evita mostrar "visitante" por un instante). */
    val isLoading: Boolean = true
) {
    val isSociaLoggedIn: Boolean get() = currentPartner != null
}

class MainViewModel(
    authRepository: AuthRepository,
    chatRepository: ChatRepository,
    cartRepository: CartRepository
) : ViewModel() {

    val uiState: StateFlow<MainUiState> = combine(
        authRepository.currentPartner,
        chatRepository.totalUnread,
        cartRepository.count
    ) { partner, unread, cartCount ->
        MainUiState(currentPartner = partner, unreadMessages = unread, cartCount = cartCount, isLoading = false)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = MainUiState()
    )
}
