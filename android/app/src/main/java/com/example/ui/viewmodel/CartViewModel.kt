package com.example.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.CartRepository
import com.example.data.repository.OrderRepository
import com.example.domain.model.CartLine
import com.example.domain.model.ShippingInfo
import com.example.domain.validation.CheckoutValidator
import com.example.ui.viewmodel.form.FieldState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CartUiState(
    val lines: List<CartLine> = emptyList()
) {
    val total: Int get() = lines.sumOf { it.price }
    val isEmpty: Boolean get() = lines.isEmpty()
}

/** Formulario de datos de envío del checkout. */
data class CheckoutFormState(
    val fullName: FieldState = FieldState(),
    val address: FieldState = FieldState(),
    val comuna: FieldState = FieldState(),
    val phone: FieldState = FieldState(),
    val generalError: String? = null,
    val isSubmitting: Boolean = false
) {
    val isValid: Boolean
        get() = fullName.isValid && address.isValid && comuna.isValid && phone.isValid
}

class CartViewModel(
    private val cartRepository: CartRepository,
    private val orderRepository: OrderRepository
) : ViewModel() {

    val uiState: StateFlow<CartUiState> = cartRepository.lines
        .map { CartUiState(lines = it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = CartUiState()
        )

    var checkoutForm by mutableStateOf(CheckoutFormState())
        private set

    fun remove(cartId: Long) {
        viewModelScope.launch { cartRepository.remove(cartId) }
    }

    fun resetCheckoutForm() {
        checkoutForm = CheckoutFormState()
    }

    fun onFullNameChange(value: String) {
        checkoutForm = checkoutForm.copy(fullName = checkoutForm.fullName.update(value, CheckoutValidator::validateFullName))
    }

    fun onAddressChange(value: String) {
        checkoutForm = checkoutForm.copy(address = checkoutForm.address.update(value, CheckoutValidator::validateAddress))
    }

    fun onComunaChange(value: String) {
        checkoutForm = checkoutForm.copy(comuna = checkoutForm.comuna.update(value, CheckoutValidator::validateComuna))
    }

    fun onPhoneChange(value: String) {
        checkoutForm = checkoutForm.copy(phone = checkoutForm.phone.update(value, CheckoutValidator::validatePhone))
    }

    /** Valida los datos de envío, guarda el pedido y vacía el carrito. Devuelve el número de pedido. */
    fun submitCheckout(onSuccess: (orderId: Long) -> Unit) {
        val form = checkoutForm
        if (form.isSubmitting) return
        val validated = form.copy(
            fullName = form.fullName.validate(CheckoutValidator::validateFullName),
            address = form.address.validate(CheckoutValidator::validateAddress),
            comuna = form.comuna.validate(CheckoutValidator::validateComuna),
            phone = form.phone.validate(CheckoutValidator::validatePhone),
            generalError = null
        )
        checkoutForm = validated
        if (!validated.isValid) return

        val lines = uiState.value.lines
        if (lines.isEmpty()) {
            checkoutForm = validated.copy(generalError = "Tu carrito está vacío")
            return
        }

        checkoutForm = validated.copy(isSubmitting = true)
        viewModelScope.launch {
            val orderId = orderRepository.placeOrder(
                shipping = ShippingInfo(
                    fullName = validated.fullName.value,
                    address = validated.address.value,
                    comuna = validated.comuna.value,
                    phone = validated.phone.value
                ),
                lines = lines
            )
            resetCheckoutForm()
            onSuccess(orderId)
        }
    }
}
