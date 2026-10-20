package com.example.ui.viewmodel

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.TextilCircularApp

/**
 * Fábrica central de ViewModels: entrega a cada uno los repositorios que necesita
 * desde el AppContainer.
 * Uso en Compose: viewModel(factory = AppViewModelProvider.Factory)
 */
object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            MainViewModel(
                authRepository = app().container.authRepository,
                chatRepository = app().container.chatRepository,
                cartRepository = app().container.cartRepository
            )
        }
        initializer {
            CatalogViewModel(
                productRepository = app().container.productRepository,
                cartRepository = app().container.cartRepository
            )
        }
        initializer {
            CartViewModel(
                cartRepository = app().container.cartRepository,
                orderRepository = app().container.orderRepository
            )
        }
        initializer {
            AnnouncementsViewModel(
                announcementRepository = app().container.announcementRepository
            )
        }
        initializer {
            MessagesViewModel(
                chatRepository = app().container.chatRepository,
                authRepository = app().container.authRepository
            )
        }
        initializer {
            WorkshopViewModel(
                productRepository = app().container.productRepository,
                imageStorage = app().container.imageStorage,
                authRepository = app().container.authRepository
            )
        }
        initializer {
            AuthViewModel(authRepository = app().container.authRepository)
        }
        initializer {
            ProductDetailViewModel(
                savedStateHandle = createSavedStateHandle(),
                productRepository = app().container.productRepository,
                cartRepository = app().container.cartRepository,
                chatRepository = app().container.chatRepository
            )
        }
    }
}

private fun CreationExtras.app(): TextilCircularApp =
    this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as TextilCircularApp
