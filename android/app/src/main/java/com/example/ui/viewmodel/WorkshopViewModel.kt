package com.example.ui.viewmodel

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.ImageStorage
import com.example.data.repository.AuthRepository
import com.example.data.repository.ProductRepository
import com.example.domain.model.Partner
import com.example.domain.model.Product
import com.example.domain.model.ProductCategories
import com.example.domain.validation.ProductValidator
import com.example.ui.viewmodel.form.FieldState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Estado completo del formulario "Publicar prenda". */
data class PublishFormState(
    val title: FieldState = FieldState(),
    val price: FieldState = FieldState(),
    val category: String = ProductCategories.values.first(),
    val categoryError: String? = null,
    val ecoTag: FieldState = FieldState(value = "100% Denim Upcycling"),
    val description: FieldState = FieldState(),
    val photo: Bitmap? = null,
    val photoError: String? = null,
    val isLoadingPhoto: Boolean = false,
    val isSubmitting: Boolean = false
) {
    val isValid: Boolean
        get() = title.isValid && price.isValid && categoryError == null &&
            ecoTag.isValid && description.isValid && photoError == null
}

@OptIn(ExperimentalCoroutinesApi::class)
class WorkshopViewModel(
    private val productRepository: ProductRepository,
    private val imageStorage: ImageStorage,
    private val authRepository: AuthRepository
) : ViewModel() {

    /** Cierra la sesión y avisa cuando terminó de guardarse. */
    fun logout(onLoggedOut: () -> Unit) {
        viewModelScope.launch {
            authRepository.logout()
            onLoggedOut()
        }
    }

    /** Socia con sesión iniciada (null si es visitante). */
    val currentPartner: StateFlow<Partner?> = authRepository.currentPartner
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )

    /** Prendas publicadas por la socia actual (se actualiza sola al publicar). */
    val myProducts: StateFlow<List<Product>> = currentPartner
        .flatMapLatest { partner ->
            if (partner == null) flowOf(emptyList())
            else productRepository.productsByPartner(partner.name)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    /** Estado del formulario. Vive en el ViewModel, así no se pierde al girar la pantalla. */
    var publishForm by mutableStateOf(PublishFormState())
        private set

    fun resetPublishForm() {
        publishForm = PublishFormState()
    }

    // --- Eventos de cada campo: se validan en el momento con ProductValidator ---

    fun onTitleChange(value: String) {
        publishForm = publishForm.copy(title = publishForm.title.update(value, ProductValidator::validateTitle))
    }

    fun onPriceChange(value: String) {
        publishForm = publishForm.copy(price = publishForm.price.update(value, ProductValidator::validatePrice))
    }

    fun onCategoryChange(value: String) {
        publishForm = publishForm.copy(
            category = value,
            categoryError = ProductValidator.validateCategory(value).errorMessage
        )
    }

    fun onEcoTagChange(value: String) {
        publishForm = publishForm.copy(ecoTag = publishForm.ecoTag.update(value, ProductValidator::validateEcoTag))
    }

    fun onDescriptionChange(value: String) {
        publishForm = publishForm.copy(
            description = publishForm.description.update(value, ProductValidator::validateDescription)
        )
    }

    fun onPhotoSelected(photo: Bitmap) {
        publishForm = publishForm.copy(photo = photo, photoError = null)
    }

    fun onPhotoRemoved() {
        publishForm = publishForm.copy(
            photo = null,
            photoError = ProductValidator.validatePhoto(hasPhoto = false).errorMessage
        )
    }

    /** URI temporal para que la cámara guarde la foto (se crea justo antes de abrirla). */
    fun createCameraUri(): Uri = imageStorage.createCameraUri()

    /** Carga en segundo plano la foto tomada con la cámara o elegida en la galería. */
    fun onPhotoUriSelected(uri: Uri) {
        publishForm = publishForm.copy(isLoadingPhoto = true, photoError = null)
        viewModelScope.launch {
            val bitmap = imageStorage.loadBitmap(uri)
            publishForm = if (bitmap != null) {
                publishForm.copy(photo = bitmap, photoError = null, isLoadingPhoto = false)
            } else {
                publishForm.copy(
                    isLoadingPhoto = false,
                    photoError = "No se pudo leer la imagen seleccionada. Prueba con otra."
                )
            }
        }
    }

    /** Errores que vienen de los recursos nativos (permiso denegado, imagen ilegible...). */
    fun onPhotoError(message: String) {
        publishForm = publishForm.copy(photoError = message)
    }

    /**
     * Valida todos los campos. Si todo está bien guarda la foto y la prenda en Room.
     * onPublished se llama solo cuando la prenda quedó guardada.
     */
    fun submitPublishForm(onPublished: (Product) -> Unit) {
        val form = publishForm
        if (form.isSubmitting) return

        val validated = form.copy(
            title = form.title.validate(ProductValidator::validateTitle),
            price = form.price.validate(ProductValidator::validatePrice),
            categoryError = ProductValidator.validateCategory(form.category).errorMessage,
            ecoTag = form.ecoTag.validate(ProductValidator::validateEcoTag),
            description = form.description.validate(ProductValidator::validateDescription),
            photoError = form.photoError ?: ProductValidator.validatePhoto(form.photo != null).errorMessage
        )
        publishForm = validated
        if (!validated.isValid) return

        val price = ProductValidator.parsePrice(validated.price.value) ?: return
        val partner = currentPartner.value ?: return
        publishForm = validated.copy(isSubmitting = true)

        viewModelScope.launch {
            val imagePath = validated.photo?.let { imageStorage.saveBitmap(it) }
            val product = Product(
                title = validated.title.value.trim(),
                price = price,
                category = validated.category,
                partnerName = partner.name,
                partnerComuna = partner.comuna,
                ecoTag = validated.ecoTag.value.trim(),
                kgSaved = 0.9,
                litersWater = 10000,
                description = validated.description.value.trim()
                    .ifBlank { "Prenda confeccionada de manera artesanal y sustentable." },
                fabricOrigin = "Mermas textiles clasificadas en taller",
                colorHex = 0xFF00A896,
                imagePath = imagePath
            )
            val newId = productRepository.publish(product)
            resetPublishForm()
            onPublished(product.copy(id = newId))
        }
    }
}
