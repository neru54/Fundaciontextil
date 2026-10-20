package com.example.ui.screens

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.domain.model.Product
import com.example.domain.model.ProductCategories
import com.example.ui.components.AnimatedSuccessIcon
import com.example.ui.components.PopIn
import com.example.ui.components.ProductImage
import com.example.ui.viewmodel.AppViewModelProvider
import com.example.domain.validation.ProductValidator
import com.example.ui.components.ValidatedTextField
import com.example.ui.permissions.hasPermission
import com.example.ui.permissions.openAppSettings
import com.example.ui.viewmodel.PublishFormState
import com.example.ui.viewmodel.WorkshopViewModel
import com.example.ui.theme.NegroCarbon
import com.example.ui.theme.TurquesaClaro
import com.example.ui.theme.TurquesaOscuro
import com.example.ui.theme.TurquesaTextil

@Composable
fun WorkshopProfileScreen(
    onNewProductPublished: (Product) -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: WorkshopViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    // Las prendas vienen de Room a través del ViewModel
    val myProducts by viewModel.myProducts.collectAsStateWithLifecycle()
    val partner by viewModel.currentPartner.collectAsStateWithLifecycle()

    // Métricas calculadas a partir de las prendas guardadas en Room
    val totalKgSaved = myProducts.sumOf { it.kgSaved }
    val totalLitersSaved = myProducts.sumOf { it.litersWater }

    var showPublishDialog by remember { mutableStateOf(false) }

    var publishSuccessMsg by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF9F9F8)),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Cabecera Perfil de Socia
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(TurquesaTextil),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(partner?.initials.orEmpty(), color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    partner?.name.orEmpty(),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = NegroCarbon
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    Icons.Default.Verified,
                                    contentDescription = "Socia Verificada",
                                    tint = TurquesaTextil,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Text(
                                "${partner?.workshopName.orEmpty()} · ${partner?.comuna.orEmpty()}",
                                fontSize = 13.sp,
                                color = TurquesaOscuro,
                                fontWeight = FontWeight.Medium
                            )
                            Surface(
                                color = TurquesaClaro,
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.padding(top = 4.dp)
                            ) {
                                Text(
                                    "Socia Acreditada #${partner?.memberCode.orEmpty()}",
                                    fontSize = 11.sp,
                                    color = TurquesaOscuro,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 14.dp), color = Color(0xFFF0F0EE))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Email, contentDescription = null, tint = TurquesaOscuro, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(partner?.email.orEmpty(), fontSize = 12.sp, color = NegroCarbon.copy(alpha = 0.8f))
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                        Icon(Icons.Default.Phone, contentDescription = null, tint = TurquesaOscuro, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(partner?.phone.orEmpty(), fontSize = 12.sp, color = NegroCarbon.copy(alpha = 0.8f))
                    }

                    OutlinedButton(
                        // El ViewModel cierra la sesión y luego avisa para navegar
                        onClick = { viewModel.logout(onLoggedOut = onLogout) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Cerrar sesión")
                    }
                }
            }
        }

        // Métricas de Impacto Ambiental
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Impacto Ambiental de tu Taller",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = NegroCarbon
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ImpactStatCard(
                        emoji = "⚖️",
                        value = "%.1f kg".format(totalKgSaved),
                        label = "Tela Rescatada",
                        modifier = Modifier.weight(1f)
                    )
                    ImpactStatCard(
                        emoji = "🧥",
                        value = "${myProducts.size}",
                        label = "Prendas Publicadas",
                        modifier = Modifier.weight(1f)
                    )
                    ImpactStatCard(
                        emoji = "💧",
                        value = if (totalLitersSaved >= 1000) "${totalLitersSaved / 1000}k L" else "$totalLitersSaved L",
                        label = "Agua Ahorrada",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Botón Publicar Nueva Prenda
        item {
            Button(
                onClick = {
                    viewModel.resetPublishForm()
                    showPublishDialog = true
                },
                colors = ButtonDefaults.buttonColors(containerColor = TurquesaTextil),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Icon(Icons.Default.AddCircle, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Publicar Nueva Prenda en el Catálogo", fontWeight = FontWeight.Bold)
            }
        }

        // Mis Prendas Publicadas
        item {
            Text(
                "Tus Prendas en Catálogo (${myProducts.size})",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = NegroCarbon
            )
        }

        items(myProducts, key = { it.id }) { prod ->
            Card(
                // La prenda recién publicada aparece arriba con animación
                modifier = Modifier
                    .animateItem()
                    .fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    prod.imagePath?.let { path ->
                        ProductImage(
                            imagePath = path,
                            contentDescription = prod.title,
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(prod.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Categoría: ${prod.category} · ${prod.ecoTag}", fontSize = 11.sp, color = NegroCarbon.copy(alpha = 0.6f))
                        Text(
                            "$${"%,d".format(prod.price).replace(',', '.')}",
                            color = TurquesaOscuro,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    Surface(
                        color = Color(0xFFE8F5E9),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            "En Venta",
                            color = Color(0xFF2E7D32),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }

    // Modal para publicar nueva prenda con validación de formulario
    if (showPublishDialog) {
        PublishProductDialog(
            form = viewModel.publishForm,
            onTitleChange = viewModel::onTitleChange,
            onPriceChange = viewModel::onPriceChange,
            onCategoryChange = viewModel::onCategoryChange,
            onEcoTagChange = viewModel::onEcoTagChange,
            onDescriptionChange = viewModel::onDescriptionChange,
            onCreateCameraUri = viewModel::createCameraUri,
            onPhotoUriSelected = viewModel::onPhotoUriSelected,
            onPhotoRemoved = viewModel::onPhotoRemoved,
            onPhotoError = viewModel::onPhotoError,
            onDismiss = { showPublishDialog = false },
            onSubmit = {
                // El ViewModel valida; el diálogo solo se cierra si la prenda se guardó
                viewModel.submitPublishForm { newProd ->
                    showPublishDialog = false
                    onNewProductPublished(newProd)
                    publishSuccessMsg = "¡Prenda '${newProd.title}' publicada exitosamente en el catálogo!"
                }
            }
        )
    }

    publishSuccessMsg?.let { msg ->
        AlertDialog(
            onDismissRequest = { publishSuccessMsg = null },
            icon = { AnimatedSuccessIcon(tint = TurquesaTextil) },
            title = { Text("Prenda Publicada", fontWeight = FontWeight.Bold) },
            text = { Text(msg) },
            confirmButton = {
                Button(onClick = { publishSuccessMsg = null }, colors = ButtonDefaults.buttonColors(containerColor = TurquesaTextil)) {
                    Text("Aceptar")
                }
            }
        )
    }
}

@Composable
fun ImpactStatCard(
    emoji: String,
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(emoji, fontSize = 20.sp)
            Text(
                value,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 16.sp,
                color = TurquesaOscuro,
                modifier = Modifier.padding(top = 2.dp)
            )
            Text(
                label,
                fontSize = 10.sp,
                color = NegroCarbon.copy(alpha = 0.7f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

/**
 * Formulario para publicar una prenda.
 * No contiene lógica de validación: recibe el estado ya validado desde WorkshopViewModel
 * y le avisa de cada cambio. Solo maneja los recursos nativos (cámara y galería).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublishProductDialog(
    form: PublishFormState,
    onTitleChange: (String) -> Unit,
    onPriceChange: (String) -> Unit,
    onCategoryChange: (String) -> Unit,
    onEcoTagChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onCreateCameraUri: () -> Uri,
    onPhotoUriSelected: (Uri) -> Unit,
    onPhotoRemoved: () -> Unit,
    onPhotoError: (String) -> Unit,
    onDismiss: () -> Unit,
    onSubmit: () -> Unit
) {
    val context = LocalContext.current

    // URI donde la cámara guardará la foto (rememberSaveable: sobrevive si Android recrea la pantalla)
    var pendingCameraUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    // true si la usuaria negó el permiso de cámara: se ofrece ir a Ajustes
    var cameraPermissionDenied by rememberSaveable { mutableStateOf(false) }

    // Recurso nativo 1: cámara en resolución completa (TakePicture + FileProvider)
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        val uri = pendingCameraUri
        if (success && uri != null) onPhotoUriSelected(uri)
        pendingCameraUri = null
    }

    val launchCamera = {
        val uri = onCreateCameraUri()
        pendingCameraUri = uri
        cameraLauncher.launch(uri)
    }

    // Recurso nativo 2: selector de fotos del sistema (no requiere permiso de almacenamiento)
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) onPhotoUriSelected(uri)
    }

    // Permiso de cámara solicitado en tiempo de ejecución
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        cameraPermissionDenied = !isGranted
        if (isGranted) {
            launchCamera()
        } else {
            onPhotoError("Se necesita permiso de cámara para tomar fotos. También puedes elegir una de la galería.")
        }
    }

    AlertDialog(
        onDismissRequest = { if (!form.isSubmitting) onDismiss() },
        title = {
            Text("Publicar Prenda como Socia", fontWeight = FontWeight.Bold, fontSize = 17.sp)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // --- Foto (Cámara / Galería) ---
                Text("Fotografía de la Prenda *", fontSize = 12.sp, fontWeight = FontWeight.Bold)

                val photo = form.photo
                if (photo != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.LightGray),
                        contentAlignment = Alignment.TopEnd
                    ) {
                        Image(
                            bitmap = photo.asImageBitmap(),
                            contentDescription = "Foto de la prenda",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        IconButton(
                            onClick = onPhotoRemoved,
                            modifier = Modifier
                                .padding(6.dp)
                                .size(28.dp)
                                .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Eliminar foto",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                if (hasPermission(context, Manifest.permission.CAMERA)) {
                                    launchCamera()
                                } else {
                                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                }
                            },
                            enabled = !form.isLoadingPhoto,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = TurquesaTextil),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = "Cámara", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Tomar Foto", fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                galleryLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            enabled = !form.isLoadingPhoto,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = "Galería", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Galería", fontSize = 12.sp)
                        }
                    }
                }
                if (form.isLoadingPhoto) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = TurquesaTextil)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Procesando foto...", fontSize = 11.sp)
                    }
                }
                form.photoError?.let { PopIn { FieldErrorText(it) } }
                // Si negó el permiso, acceso directo a los Ajustes de la app
                if (cameraPermissionDenied && form.photo == null) {
                    TextButton(onClick = { openAppSettings(context) }) {
                        Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Activar cámara en Ajustes", fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // --- Campos de texto con validación por campo ---
                ValidatedTextField(
                    field = form.title,
                    onValueChange = onTitleChange,
                    label = "Nombre de la Prenda *",
                    placeholder = "Ej: Chaqueta Bomber Mezclilla",
                    leadingIcon = Icons.Default.Checkroom,
                    maxLength = ProductValidator.TITLE_MAX
                )

                ValidatedTextField(
                    field = form.price,
                    onValueChange = onPriceChange,
                    label = "Precio (CLP) *",
                    placeholder = "35000",
                    leadingIcon = Icons.Default.AttachMoney,
                    helperText = "Entre $1.000 y $2.000.000",
                    keyboardType = KeyboardType.Number
                )

                // --- Categoría ---
                Text("Categoría *", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ProductCategories.values.forEach { cat ->
                        FilterChip(
                            selected = form.category == cat,
                            onClick = { onCategoryChange(cat) },
                            label = { Text(cat, fontSize = 11.sp) },
                            leadingIcon = if (form.category == cat) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                            } else null
                        )
                    }
                }
                form.categoryError?.let { PopIn { FieldErrorText(it) } }

                ValidatedTextField(
                    field = form.ecoTag,
                    onValueChange = onEcoTagChange,
                    label = "Material Textil Rescatado *",
                    leadingIcon = Icons.Default.Recycling,
                    maxLength = ProductValidator.ECO_TAG_MAX
                )

                ValidatedTextField(
                    field = form.description,
                    onValueChange = onDescriptionChange,
                    label = "Detalle de la Confección",
                    placeholder = "Describe la procedencia de la tela y detalles únicos...",
                    leadingIcon = Icons.Default.Description,
                    helperText = "Opcional",
                    maxLength = ProductValidator.DESCRIPTION_MAX,
                    singleLine = false,
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onSubmit,
                enabled = !form.isSubmitting,
                colors = ButtonDefaults.buttonColors(containerColor = TurquesaTextil)
            ) {
                if (form.isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Publicando...")
                } else {
                    Text("Publicar")
                }
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss, enabled = !form.isSubmitting) {
                Text("Cancelar")
            }
        }
    )
}

/** Mensaje de error con ícono para validaciones que no son campos de texto (foto, categoría). */
@Composable
private fun FieldErrorText(message: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            Icons.Default.Error,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = message,
            color = MaterialTheme.colorScheme.error,
            fontSize = 11.sp
        )
    }
}
