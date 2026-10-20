package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.DetailScaffold
import com.example.ui.components.FormErrorBanner
import com.example.ui.components.ValidatedTextField
import com.example.ui.theme.NegroCarbon
import com.example.ui.theme.TurquesaClaro
import com.example.ui.theme.TurquesaOscuro
import com.example.ui.theme.TurquesaTextil
import com.example.ui.viewmodel.AppViewModelProvider
import com.example.ui.viewmodel.CartViewModel

/** Pantalla del carrito (ruta "cart"). */
@Composable
fun CartScreen(
    onBack: () -> Unit,
    onCheckout: () -> Unit,
    onExploreCatalog: () -> Unit,
    viewModel: CartViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val cart by viewModel.uiState.collectAsStateWithLifecycle()

    DetailScaffold(
        title = "Carrito Sustentable",
        subtitle = "${cart.lines.size} prendas",
        onBack = onBack,
        bottomBar = {
            if (!cart.isEmpty) {
                Surface(shadowElevation = 8.dp, color = Color.White) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total a pagar", fontWeight = FontWeight.Bold, color = NegroCarbon)
                            Text(
                                "${formatClp(cart.total)} CLP",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp,
                                color = TurquesaOscuro
                            )
                        }
                        Button(
                            onClick = onCheckout,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp)
                                .height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = TurquesaTextil)
                        ) {
                            Icon(Icons.Default.LocalShipping, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Continuar con la compra", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    ) { padding ->
        if (cart.isEmpty) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF9F9F8))
                    .padding(padding)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.ShoppingBag,
                        contentDescription = null,
                        tint = TurquesaTextil,
                        modifier = Modifier.size(56.dp)
                    )
                    Text("Tu carrito está vacío", fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.padding(top = 12.dp))
                    Text(
                        "Explora el catálogo y apoya a las artesanas de la fundación.",
                        fontSize = 13.sp,
                        color = NegroCarbon.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                    )
                    OutlinedButton(onClick = onExploreCatalog, shape = RoundedCornerShape(10.dp)) {
                        Text("Ir al catálogo")
                    }
                }
            }
            return@DetailScaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF9F9F8))
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(cart.lines, key = { it.cartId }) { line ->
                Card(
                    // Al quitar una prenda, el resto de la lista se desplaza con animación
                    modifier = Modifier.animateItem(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(line.title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, maxLines = 1)
                            Text("Socia: ${line.partnerName}", fontSize = 12.sp, color = NegroCarbon.copy(alpha = 0.6f))
                        }
                        Text(formatClp(line.price), fontWeight = FontWeight.Bold, color = TurquesaOscuro)
                        IconButton(onClick = { viewModel.remove(line.cartId) }) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = "Quitar ${line.title}", tint = Color.Red)
                        }
                    }
                }
            }
        }
    }
}

/** Pantalla de datos de envío y confirmación del pedido (ruta "checkout"). */
@Composable
fun CheckoutScreen(
    onBack: () -> Unit,
    onOrderPlaced: (orderId: Long) -> Unit,
    viewModel: CartViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    // Se observa el carrito para mostrar el total y para que el ViewModel tenga las líneas al confirmar
    val cart by viewModel.uiState.collectAsStateWithLifecycle()
    val form = viewModel.checkoutForm

    DetailScaffold(title = "Datos de Envío", subtitle = "Paso 2 de 2", onBack = onBack) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF9F9F8))
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            form.generalError?.let { FormErrorBanner(it) }

            ValidatedTextField(
                field = form.fullName,
                onValueChange = viewModel::onFullNameChange,
                label = "Nombre de quien recibe *",
                leadingIcon = Icons.Default.Person
            )
            ValidatedTextField(
                field = form.address,
                onValueChange = viewModel::onAddressChange,
                label = "Dirección *",
                placeholder = "Calle, número, depto.",
                leadingIcon = Icons.Default.Home,
                maxLength = 100
            )
            ValidatedTextField(
                field = form.comuna,
                onValueChange = viewModel::onComunaChange,
                label = "Comuna *",
                leadingIcon = Icons.Default.LocationOn
            )
            ValidatedTextField(
                field = form.phone,
                onValueChange = viewModel::onPhoneChange,
                label = "Celular de contacto *",
                placeholder = "+56 9 1234 5678",
                leadingIcon = Icons.Default.Phone,
                keyboardType = KeyboardType.Phone
            )

            // Resumen del pedido
            Card(
                colors = CardDefaults.cardColors(containerColor = TurquesaClaro),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Resumen del pedido", fontWeight = FontWeight.Bold, color = TurquesaOscuro)
                    cart.lines.forEach { line ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(line.title, fontSize = 13.sp, modifier = Modifier.weight(1f), maxLines = 1)
                            Text(formatClp(line.price), fontSize = 13.sp)
                        }
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = TurquesaOscuro.copy(alpha = 0.3f))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total", fontWeight = FontWeight.Bold)
                        Text("${formatClp(cart.total)} CLP", fontWeight = FontWeight.ExtraBold, color = TurquesaOscuro)
                    }
                }
            }

            Button(
                onClick = { viewModel.submitCheckout(onSuccess = onOrderPlaced) },
                enabled = !form.isSubmitting,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = TurquesaTextil)
            ) {
                if (form.isSubmitting) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Confirmando...")
                } else {
                    Text("Confirmar Pedido", fontWeight = FontWeight.Bold)
                }
            }
            Text(
                "El pago se coordina directamente con cada socia al confirmar el pedido.",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp)
            )
        }
    }
}
