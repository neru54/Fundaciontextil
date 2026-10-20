package com.example.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.CartRepository
import com.example.data.repository.ChatRepository
import com.example.data.repository.ProductRepository
import com.example.domain.model.Product
import com.example.ui.navigation.Routes
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Detalle de prenda; recibe productId desde la ruta. */
class ProductDetailViewModel(
    savedStateHandle: SavedStateHandle,
    productRepository: ProductRepository,
    private val cartRepository: CartRepository,
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val productId: Long = checkNotNull(savedStateHandle[Routes.ARG_PRODUCT_ID]) {
        "La ruta del detalle necesita el id de la prenda"
    }

    val product: StateFlow<Product?> = productRepository.product(productId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )

    fun addToCart(onAdded: (Product) -> Unit) {
        val current = product.value ?: return
        viewModelScope.launch {
            cartRepository.add(current.id)
            onAdded(current)
        }
    }

    fun contactPartner(onChatReady: (chatId: Long, product: Product) -> Unit) {
        val current = product.value ?: return
        viewModelScope.launch {
            chatRepository.findChatIdForProduct(current.title)?.let { onChatReady(it, current) }
        }
    }
}
