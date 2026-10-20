package com.example.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.AuthRepository
import com.example.data.repository.ChatRepository
import com.example.domain.model.Chat
import com.example.domain.model.ChatMessage
import com.example.domain.model.Partner
import com.example.domain.validation.ChatMessageValidator
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class MessagesUiState(
    val chats: List<Chat> = emptyList(),
    val selectedChat: Chat? = null,
    val messages: List<ChatMessage> = emptyList()
) {
    val totalUnread: Int get() = chats.sumOf { it.unreadCount }
}

@OptIn(ExperimentalCoroutinesApi::class)
class MessagesViewModel(
    private val chatRepository: ChatRepository,
    authRepository: AuthRepository
) : ViewModel() {

    /** La socia que responde los mensajes. */
    private val currentPartner: StateFlow<Partner?> = authRepository.currentPartner
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val selectedChatId = MutableStateFlow<Long?>(null)

    /** Texto que la socia está escribiendo en el chat. */
    var replyText by mutableStateOf("")
        private set

    /** Error de validación del mensaje (null si no hay error). */
    var replyError by mutableStateOf<String?>(null)
        private set

    private val selectedMessages = selectedChatId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList()) else chatRepository.messages(id)
    }

    val uiState: StateFlow<MessagesUiState> = combine(
        chatRepository.chats,
        selectedChatId,
        selectedMessages
    ) { chats, chatId, messages ->
        MessagesUiState(
            chats = chats,
            selectedChat = chats.firstOrNull { it.id == chatId },
            messages = messages
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = MessagesUiState()
    )

    fun openChat(chatId: Long) {
        selectedChatId.value = chatId
        replyText = ""
        replyError = null
        viewModelScope.launch { chatRepository.markAsRead(chatId) }
    }

    fun closeChat() {
        selectedChatId.value = null
    }

    /**
     * Simula que una clienta escribe (para demostrar notificaciones y vibración):
     * el mensaje se guarda en Room y onReceived recibe los datos para notificar.
     */
    fun simulateIncomingMessage(onReceived: (chatId: Long, clientName: String, text: String) -> Unit) {
        val chat = uiState.value.chats.firstOrNull() ?: return
        val text = SIMULATED_MESSAGES.random()
        viewModelScope.launch {
            chatRepository.receiveClientMessage(chat.id, text)?.let { onReceived(it.id, it.clientName, text) }
        }
    }

    fun onReplyChange(text: String) {
        replyText = text
        // Mientras escribe solo se avisa si se pasa del largo máximo
        replyError = if (text.trim().length > ChatMessageValidator.MAX_LENGTH) {
            ChatMessageValidator.validate(text).errorMessage
        } else null
    }

    fun sendReply() {
        val chatId = selectedChatId.value ?: return
        val sender = currentPartner.value?.name ?: return
        val result = ChatMessageValidator.validate(replyText)
        if (!result.isValid) {
            replyError = result.errorMessage
            return
        }
        val text = replyText.trim()
        replyText = ""
        replyError = null
        viewModelScope.launch {
            chatRepository.sendReply(chatId = chatId, sender = sender, text = text)
        }
    }
}

private val SIMULATED_MESSAGES = listOf(
    "¡Hola! ¿Hacen envíos a Valparaíso? Me encantó la chaqueta de mezclilla.",
    "¿La chaqueta viene en talla L? Quiero regalarla para un cumpleaños.",
    "Hola, ¿puedo retirar el pedido en el taller este sábado?"
)
