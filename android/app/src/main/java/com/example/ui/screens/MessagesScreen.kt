package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.example.ui.viewmodel.AppViewModelProvider
import com.example.ui.viewmodel.MessagesViewModel
import com.example.domain.validation.ChatMessageValidator
import com.example.ui.theme.NegroCarbon
import com.example.ui.theme.TurquesaClaro
import com.example.ui.theme.TurquesaOscuro
import com.example.ui.theme.TurquesaTextil
import com.example.ui.components.DetailScaffold

@Composable
fun MessagesScreen(
    onOpenChat: (chatId: Long) -> Unit,
    /** Para notificar y vibrar. */
    onIncomingMessage: (chatId: Long, clientName: String, text: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MessagesViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF9F9F8)),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(14.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Bandeja de Consultas de Clientas",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = NegroCarbon
                        )
                        Badge(containerColor = TurquesaTextil) {
                            Text("${uiState.totalUnread} nuevas")
                        }
                    }
                    Text(
                        "Comunícate directamente con quienes están interesadas en tus prendas y confecciones upcycling.",
                        fontSize = 12.sp,
                        color = NegroCarbon.copy(alpha = 0.7f),
                        modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                    )

                    Button(
                        onClick = { viewModel.simulateIncomingMessage(onIncomingMessage) },
                        colors = ButtonDefaults.buttonColors(containerColor = TurquesaOscuro),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Simular mensaje de una clienta")
                    }
                }
            }
        }

        items(uiState.chats, key = { it.id }) { chat ->
            Card(
                modifier = Modifier
                    .animateItem()
                    .fillMaxWidth()
                    .clickable { onOpenChat(chat.id) },
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(TurquesaClaro),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            chat.clientName.take(1).uppercase(),
                            fontWeight = FontWeight.Bold,
                            color = TurquesaOscuro,
                            fontSize = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                chat.clientName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = NegroCarbon
                            )
                            Text(
                                chat.time,
                                fontSize = 11.sp,
                                color = NegroCarbon.copy(alpha = 0.5f)
                            )
                        }

                        Text(
                            "Prenda: ${chat.productTitle}",
                            fontSize = 11.sp,
                            color = TurquesaOscuro,
                            fontWeight = FontWeight.SemiBold
                        )

                        Text(
                            chat.lastMessage,
                            fontSize = 12.sp,
                            color = NegroCarbon.copy(alpha = 0.8f),
                            maxLines = 1,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

                    if (chat.unreadCount > 0) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Badge(containerColor = TurquesaTextil) {
                            Text("${chat.unreadCount}")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChatScreen(
    chatId: Long,
    onBack: () -> Unit,
    viewModel: MessagesViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    // Marca el chat como leído
    LaunchedEffect(chatId) { viewModel.openChat(chatId) }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val replyText = viewModel.replyText
    val chat = uiState.selectedChat
    val listState = rememberLazyListState()

    // Desplaza al último mensaje
    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) listState.animateScrollToItem(uiState.messages.lastIndex)
    }

    DetailScaffold(
        title = chat?.clientName ?: "Conversación",
        subtitle = chat?.let { "Prenda: ${it.productTitle}" },
        onBack = onBack
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF9F9F8))
                .padding(padding)
                .imePadding()
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(uiState.messages, key = { it.id }) { msg ->
                    val isClient = msg.isClient
                    Row(
                        modifier = Modifier
                            .animateItem()
                            .fillMaxWidth(),
                        horizontalArrangement = if (isClient) Arrangement.Start else Arrangement.End
                    ) {
                        Surface(
                            shape = RoundedCornerShape(
                                topStart = 12.dp,
                                topEnd = 12.dp,
                                bottomStart = if (isClient) 2.dp else 12.dp,
                                bottomEnd = if (isClient) 12.dp else 2.dp
                            ),
                            color = if (isClient) Color.White else TurquesaTextil,
                            shadowElevation = 1.dp,
                            modifier = Modifier.widthIn(max = 280.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = msg.text,
                                    fontSize = 13.sp,
                                    color = if (isClient) NegroCarbon else Color.White
                                )
                                Text(
                                    text = msg.time,
                                    fontSize = 10.sp,
                                    color = if (isClient) NegroCarbon.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.7f),
                                    modifier = Modifier
                                        .align(Alignment.End)
                                        .padding(top = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            Surface(
                color = Color.White,
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = replyText,
                        onValueChange = viewModel::onReplyChange,
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Escribe una respuesta como socia...") },
                        isError = viewModel.replyError != null,
                        supportingText = {
                            val error = viewModel.replyError
                            if (error != null) {
                                Text(error, fontSize = 11.sp)
                            } else if (replyText.isNotEmpty()) {
                                Text("${replyText.trim().length}/${ChatMessageValidator.MAX_LENGTH}", fontSize = 11.sp)
                            }
                        },
                        maxLines = 3,
                        shape = RoundedCornerShape(20.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TurquesaTextil,
                            unfocusedBorderColor = Color(0xFFDCDAD5)
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = { viewModel.sendReply() },
                        colors = IconButtonDefaults.iconButtonColors(containerColor = TurquesaTextil)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Enviar", tint = Color.White)
                    }
                }
            }
        }
    }
}
