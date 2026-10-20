package com.example.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.CartRepository
import com.example.data.repository.ProductRepository
import com.example.domain.model.Product
import com.example.domain.model.ProductCategories
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CatalogUiState(
    val selectedCategory: String = ProductCategories.ALL,
    val products: List<Product> = emptyList(),
    val categories: List<String> = ProductCategories.withAll
)

class CatalogViewModel(
    productRepository: ProductRepository,
    private val cartRepository: CartRepository
) : ViewModel() {

    /** Texto del buscador. Se usa estado de Compose para que el cursor del campo no salte. */
    var searchQuery by mutableStateOf("")
        private set

    private val selectedCategory = MutableStateFlow(ProductCategories.ALL)

    val uiState: StateFlow<CatalogUiState> = combine(
        productRepository.products,
        snapshotFlow { searchQuery },
        selectedCategory
    ) { products, query, category ->
        CatalogUiState(
            selectedCategory = category,
            products = filterProducts(products, query, category)
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CatalogUiState()
    )

    fun onSearchQueryChange(query: String) {
        searchQuery = query
    }

    fun onCategorySelected(category: String) {
        selectedCategory.value = category
    }

    fun addToCart(product: Product) {
        viewModelScope.launch { cartRepository.add(product.id) }
    }

    private fun filterProducts(products: List<Product>, query: String, category: String): List<Product> =
        products.filter { product ->
            val matchesCategory = category == ProductCategories.ALL ||
                product.category.equals(category, ignoreCase = true)
            val matchesSearch = query.isBlank() ||
                product.title.contains(query, ignoreCase = true) ||
                product.partnerName.contains(query, ignoreCase = true) ||
                product.partnerComuna.contains(query, ignoreCase = true) ||
                product.ecoTag.contains(query, ignoreCase = true)
            matchesCategory && matchesSearch
        }
}
