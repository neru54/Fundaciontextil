package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.domain.model.Product
import com.example.ui.components.ProductImage
import com.example.ui.viewmodel.AppViewModelProvider
import com.example.ui.viewmodel.CatalogViewModel
import com.example.ui.theme.AmarilloMostaza
import com.example.ui.theme.NegroCarbon
import com.example.ui.theme.TurquesaClaro
import com.example.ui.theme.TurquesaOscuro
import com.example.ui.theme.TurquesaTextil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogScreen(
    onProductClick: (productId: Long) -> Unit,
    onAddedToCart: (Product) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CatalogViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    // El filtrado y los datos viven en el ViewModel; la pantalla solo dibuja el estado.
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val searchQuery = viewModel.searchQuery
    val filteredProducts = uiState.products

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF9F9F8)),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Banner de impacto ecológico
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = TurquesaTextil),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("♻️", fontSize = 24.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Moda Circular & Upcycling Chileno",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "Prendas únicas creadas por socias artesanas rescatando mermas textiles.",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }

        // Buscador
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = viewModel::onSearchQueryChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Buscar por prenda, socia o comuna...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Buscar", tint = TurquesaOscuro)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Limpiar")
                        }
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = TurquesaTextil,
                    unfocusedBorderColor = Color(0xFFDCDAD5),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                ),
                singleLine = true
            )
        }

        // Selector horizontal de categorías
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                items(uiState.categories) { category ->
                    val isSelected = uiState.selectedCategory == category
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.onCategorySelected(category) },
                        label = { Text(category, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = TurquesaClaro,
                            selectedLabelColor = TurquesaOscuro,
                            containerColor = Color.White,
                            labelColor = NegroCarbon
                        )
                    )
                }
            }
        }

        // Contador de resultados
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${filteredProducts.size} prendas disponibles",
                    style = MaterialTheme.typography.labelLarge,
                    color = NegroCarbon.copy(alpha = 0.7f)
                )
                Text(
                    text = "Región Metropolitana & Regiones",
                    style = MaterialTheme.typography.labelSmall,
                    color = TurquesaOscuro
                )
            }
        }

        // Mensaje animado cuando el filtro no encuentra prendas
        item(key = "empty") {
            AnimatedVisibility(
                visible = filteredProducts.isEmpty(),
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Text(
                    "No encontramos prendas con ese filtro. Prueba con otra categoría o búsqueda.",
                    fontSize = 13.sp,
                    color = NegroCarbon.copy(alpha = 0.7f),
                    modifier = Modifier.padding(vertical = 24.dp)
                )
            }
        }

        // Lista de tarjetas de producto
        items(filteredProducts, key = { it.id }) { product ->
            ProductCardItem(
                product = product,
                // Las tarjetas se reordenan/entran/salen con animación al filtrar
                modifier = Modifier.animateItem(),
                onViewDetails = { onProductClick(product.id) },
                onAddToCart = {
                    viewModel.addToCart(product)
                    onAddedToCart(product)
                }
            )
        }
    }
}

@Composable
fun ProductCardItem(
    product: Product,
    onViewDetails: () -> Unit,
    onAddToCart: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Al agregar, el botón muestra "Agregado ✓" por un momento como confirmación
    var justAdded by remember { mutableStateOf(false) }
    LaunchedEffect(justAdded) {
        if (justAdded) {
            delay(1500)
            justAdded = false
        }
    }
    val haptics = LocalHapticFeedback.current

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onViewDetails() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // Cabecera visual de la prenda con textura simulada y badge
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .background(Color(product.colorHex))
            ) {
                product.imagePath?.let { path ->
                    ProductImage(
                        imagePath = path,
                        contentDescription = product.title,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                // Etiqueta de socia y comuna
                Surface(
                    shape = RoundedCornerShape(topStart = 0.dp, bottomStart = 0.dp, topEnd = 8.dp, bottomEnd = 8.dp),
                    color = Color.Black.copy(alpha = 0.5f),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(top = 12.dp)
                ) {
                    Text(
                        text = "🧵 ${product.partnerName} · ${product.partnerComuna}",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                // Badge ecológico
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = AmarilloMostaza,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(10.dp)
                ) {
                    Text(
                        text = "♻️ ${product.ecoTag}",
                        color = NegroCarbon,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Cuerpo de información
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = product.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = NegroCarbon,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "$${"%,d".format(product.price).replace(',', '.')}",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 17.sp,
                        color = TurquesaOscuro
                    )
                }

                Text(
                    text = product.description,
                    fontSize = 12.sp,
                    color = NegroCarbon.copy(alpha = 0.7f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(vertical = 6.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onViewDetails,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Ver Detalle", fontSize = 12.sp)
                    }

                    val buttonColor by animateColorAsState(
                        targetValue = if (justAdded) TurquesaOscuro else TurquesaTextil,
                        label = "addButtonColor"
                    )
                    Button(
                        onClick = {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            onAddToCart()
                            justAdded = true
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        AnimatedContent(
                            targetState = justAdded,
                            transitionSpec = { (fadeIn() + scaleIn()) togetherWith fadeOut() },
                            label = "addButtonContent"
                        ) { added ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    if (added) Icons.Default.Check else Icons.Default.ShoppingCart,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (added) "Agregado" else "Comprar", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
