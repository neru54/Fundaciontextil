package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.domain.model.Product
import com.example.ui.components.DetailScaffold
import com.example.ui.components.ProductImage
import com.example.ui.theme.AmarilloMostaza
import com.example.ui.theme.NegroCarbon
import com.example.ui.theme.TurquesaClaro
import com.example.ui.theme.TurquesaOscuro
import com.example.ui.theme.TurquesaTextil
import com.example.ui.viewmodel.AppViewModelProvider
import com.example.ui.viewmodel.ProductDetailViewModel

/** Pantalla de detalle de una prenda (ruta product/{productId}). */
@Composable
fun ProductDetailScreen(
    onBack: () -> Unit,
    onAddedToCart: (Product) -> Unit,
    onOpenChat: (chatId: Long, product: Product) -> Unit,
    viewModel: ProductDetailViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val product by viewModel.product.collectAsStateWithLifecycle()

    DetailScaffold(
        title = product?.title ?: "Detalle de la prenda",
        subtitle = product?.let { "Socia: ${it.partnerName}" },
        onBack = onBack,
        bottomBar = {
            product?.let {
                Surface(shadowElevation = 8.dp, color = Color.White) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.contactPartner(onOpenChat) },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Consultar")
                        }
                        Button(
                            onClick = { viewModel.addToCart(onAddedToCart) },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = TurquesaTextil)
                        ) {
                            Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Agregar")
                        }
                    }
                }
            }
        }
    ) { padding ->
        val current = product
        if (current == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = TurquesaTextil)
            }
            return@DetailScaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF9F9F8))
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // Imagen o color de la prenda
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .background(Color(current.colorHex))
            ) {
                current.imagePath?.let { path ->
                    ProductImage(
                        imagePath = path,
                        contentDescription = current.title,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = AmarilloMostaza,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp)
                ) {
                    Text(
                        "♻️ ${current.ecoTag}",
                        color = NegroCarbon,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }

            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(current.title, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = NegroCarbon)
                        Text(current.category, fontSize = 13.sp, color = TurquesaOscuro, fontWeight = FontWeight.Medium)
                    }
                    Text(
                        formatClp(current.price),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 22.sp,
                        color = TurquesaOscuro
                    )
                }

                // Socia artesana
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(TurquesaClaro, RoundedCornerShape(10.dp))
                        .padding(12.dp)
                ) {
                    Text("🧵", fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            "Confeccionado por ${current.partnerName}",
                            fontWeight = FontWeight.SemiBold,
                            color = TurquesaOscuro,
                            fontSize = 14.sp
                        )
                        Text("Taller en ${current.partnerComuna}", fontSize = 12.sp, color = NegroCarbon.copy(alpha = 0.8f))
                    }
                }

                Text(current.description, fontSize = 14.sp, color = NegroCarbon, lineHeight = 20.sp)

                // Trazabilidad ambiental
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("Trazabilidad y Sustentabilidad", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = NegroCarbon)
                        Text("• Material: ${current.ecoTag}", fontSize = 13.sp, color = NegroCarbon.copy(alpha = 0.9f))
                        Text("• Origen de la tela: ${current.fabricOrigin}", fontSize = 13.sp, color = NegroCarbon.copy(alpha = 0.9f))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(top = 6.dp)) {
                            ImpactStatCard(
                                emoji = "⚖️",
                                value = "${current.kgSaved} kg",
                                label = "Tela rescatada",
                                modifier = Modifier.weight(1f)
                            )
                            ImpactStatCard(
                                emoji = "💧",
                                value = "${current.litersWater} L",
                                label = "Agua ahorrada",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}
