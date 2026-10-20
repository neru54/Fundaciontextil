package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.domain.model.Announcement
import com.example.ui.components.AnimatedSuccessIcon
import com.example.ui.viewmodel.AnnouncementsViewModel
import com.example.ui.viewmodel.AppViewModelProvider
import com.example.ui.theme.AmarilloMostaza
import com.example.ui.theme.NegroCarbon
import com.example.ui.theme.TurquesaClaro
import com.example.ui.theme.TurquesaOscuro
import com.example.ui.theme.TurquesaTextil

@Composable
fun AnnouncementsScreen(
    modifier: Modifier = Modifier,
    viewModel: AnnouncementsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Estado visual de los diálogos
    var appliedAnnouncementTitle by remember { mutableStateOf<String?>(null) }
    var selectedAnnouncementDetail by remember { mutableStateOf<Announcement?>(null) }

    // Abre el formulario de postulación; la validación y el guardado los hace el ViewModel
    val applyTo: (Announcement) -> Unit = { announcement -> viewModel.startApplication(announcement) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF9F9F8)),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Convocatorias Abiertas (${uiState.announcements.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = NegroCarbon
                )
                Surface(
                    color = AmarilloMostaza,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "SOLO SOCIAS",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = NegroCarbon,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }
        }

        items(uiState.announcements, key = { it.id }) { announcement ->
            AnnouncementCard(
                item = announcement,
                onViewDetail = { selectedAnnouncementDetail = announcement },
                onApply = { applyTo(announcement) },
                modifier = Modifier.animateItem()
            )
        }
    }

    // Formulario de postulación con validación del motivo
    if (viewModel.applicationForm.announcement != null) {
        ApplicationDialog(
            form = viewModel.applicationForm,
            onMotivationChange = viewModel::onMotivationChange,
            onDismiss = viewModel::cancelApplication,
            onSubmit = {
                viewModel.submitApplication { announcement ->
                    appliedAnnouncementTitle = announcement.title
                }
            }
        )
    }

    // Modal de confirmación de postulación
    appliedAnnouncementTitle?.let { title ->
        AlertDialog(
            onDismissRequest = { appliedAnnouncementTitle = null },
            icon = {
                AnimatedSuccessIcon(tint = TurquesaTextil)
            },
            title = {
                Text("¡Postulación Enviada con Éxito!", fontWeight = FontWeight.Bold)
            },
            text = {
                Text("Tu registro como socia para '$title' ha sido recepcionado. La coordinadora de la fundación te contactará vía WhatsApp/Correo para confirmar tu cupo.")
            },
            confirmButton = {
                Button(
                    onClick = { appliedAnnouncementTitle = null },
                    colors = ButtonDefaults.buttonColors(containerColor = TurquesaTextil)
                ) {
                    Text("Entendido")
                }
            }
        )
    }

    // Modal de detalle completo del anuncio
    selectedAnnouncementDetail?.let { item ->
        AlertDialog(
            onDismissRequest = { selectedAnnouncementDetail = null },
            title = {
                Text(item.title, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        color = TurquesaClaro,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = item.category,
                            color = TurquesaOscuro,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Text("📅 Fecha: ${item.date}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Text("📍 Ubicación: ${item.location}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Text("👥 Cupos disponibles: ${item.availableSpots}", fontSize = 12.sp, color = TurquesaOscuro)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    Text(item.description, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Requisitos:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text(item.requirements, fontSize = 12.sp, color = NegroCarbon.copy(alpha = 0.8f))
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        applyTo(item)
                        selectedAnnouncementDetail = null
                    },
                    enabled = !item.applied && item.availableSpots > 0,
                    colors = ButtonDefaults.buttonColors(containerColor = TurquesaTextil)
                ) {
                    Text(if (item.applied) "Ya postulaste" else "Postular Ahora")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { selectedAnnouncementDetail = null }) {
                    Text("Cerrar")
                }
            }
        )
    }
}

@Composable
fun AnnouncementCard(
    item: Announcement,
    onViewDetail: () -> Unit,
    onApply: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = TurquesaClaro,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = item.category,
                        color = TurquesaOscuro,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.People,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = TurquesaOscuro
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${item.availableSpots} cupos",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TurquesaOscuro
                    )
                }
            }

            Text(
                text = item.title,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = NegroCarbon,
                modifier = Modifier.padding(top = 10.dp, bottom = 6.dp)
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(14.dp), tint = NegroCarbon.copy(alpha = 0.6f))
                Spacer(modifier = Modifier.width(4.dp))
                Text(item.date, fontSize = 12.sp, color = NegroCarbon.copy(alpha = 0.8f))
            }

            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)) {
                Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(14.dp), tint = NegroCarbon.copy(alpha = 0.6f))
                Spacer(modifier = Modifier.width(4.dp))
                Text(item.location, fontSize = 12.sp, color = NegroCarbon.copy(alpha = 0.8f))
            }

            Text(
                text = item.description,
                fontSize = 12.sp,
                color = NegroCarbon.copy(alpha = 0.7f),
                modifier = Modifier.padding(vertical = 10.dp),
                maxLines = 2
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onViewDetail,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Ver Bases", fontSize = 12.sp)
                }

                Button(
                    onClick = onApply,
                    enabled = !item.applied && item.availableSpots > 0,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = TurquesaTextil),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    if (item.applied) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Postulada", fontSize = 12.sp)
                    } else {
                        Text("Postular", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
