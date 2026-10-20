package com.example.ui.screens.auth

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.domain.model.DemoSocia
import com.example.ui.components.FormErrorBanner
import com.example.ui.components.ValidatedTextField
import com.example.ui.theme.NegroCarbon
import com.example.ui.theme.TurquesaClaro
import com.example.ui.theme.TurquesaOscuro
import com.example.ui.theme.TurquesaTextil
import com.example.ui.viewmodel.AppViewModelProvider
import com.example.ui.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    onBack: () -> Unit,
    onLoggedIn: () -> Unit,
    onGoToRegister: () -> Unit,
    viewModel: AuthViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val form = viewModel.loginForm
    BackHandler(onBack = onBack)

    AuthScaffold(title = "Ingreso de Socias", onBack = onBack) {
        AuthHeader(
            emoji = "🧵",
            title = "Bienvenida de vuelta",
            subtitle = "Ingresa para publicar prendas, responder consultas y postular a convocatorias."
        )

        form.generalError?.let { FormErrorBanner(it) }

        ValidatedTextField(
            field = form.email,
            onValueChange = viewModel::onLoginEmailChange,
            label = "Correo electrónico *",
            placeholder = "nombre@correo.cl",
            leadingIcon = Icons.Default.Email,
            keyboardType = KeyboardType.Email
        )

        ValidatedTextField(
            field = form.password,
            onValueChange = viewModel::onLoginPasswordChange,
            label = "Contraseña *",
            leadingIcon = Icons.Default.Lock,
            isPassword = true
        )

        SubmitButton(
            text = "Ingresar",
            loadingText = "Ingresando...",
            isLoading = form.isSubmitting,
            onClick = { viewModel.submitLogin(onSuccess = onLoggedIn) }
        )

        TextButton(onClick = onGoToRegister, modifier = Modifier.fillMaxWidth()) {
            Text("¿Aún no eres socia? Regístrate aquí", color = TurquesaOscuro)
        }

        // Cuenta de prueba
        Card(
            colors = CardDefaults.cardColors(containerColor = TurquesaClaro),
            shape = RoundedCornerShape(10.dp)
        ) {
            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Info, contentDescription = null, tint = TurquesaOscuro, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Cuenta de prueba: ${DemoSocia.EMAIL} / ${DemoSocia.PASSWORD}",
                    fontSize = 12.sp,
                    color = TurquesaOscuro
                )
            }
        }
    }
}

@Composable
fun RegisterScreen(
    onBack: () -> Unit,
    onRegistered: () -> Unit,
    onGoToLogin: () -> Unit,
    viewModel: AuthViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val form = viewModel.registerForm
    BackHandler(onBack = onBack)

    AuthScaffold(title = "Registro de Socia", onBack = onBack) {
        AuthHeader(
            emoji = "♻️",
            title = "Únete a la Fundación",
            subtitle = "Completa tus datos para vender tus prendas upcycling en el catálogo."
        )

        form.generalError?.let { FormErrorBanner(it) }

        SectionLabel("Datos personales")
        ValidatedTextField(
            field = form.name,
            onValueChange = viewModel::onNameChange,
            label = "Nombre completo *",
            placeholder = "Ej: Valentina Silva",
            leadingIcon = Icons.Default.Person,
            maxLength = 60
        )
        ValidatedTextField(
            field = form.rut,
            onValueChange = viewModel::onRutChange,
            label = "RUT *",
            placeholder = "12.345.678-5",
            leadingIcon = Icons.Default.Badge,
            helperText = "Con dígito verificador"
        )
        ValidatedTextField(
            field = form.email,
            onValueChange = viewModel::onRegisterEmailChange,
            label = "Correo electrónico *",
            placeholder = "nombre@correo.cl",
            leadingIcon = Icons.Default.Email,
            keyboardType = KeyboardType.Email
        )
        ValidatedTextField(
            field = form.phone,
            onValueChange = viewModel::onPhoneChange,
            label = "Celular *",
            placeholder = "+56 9 1234 5678",
            leadingIcon = Icons.Default.Phone,
            keyboardType = KeyboardType.Phone
        )

        SectionLabel("Tu taller")
        ValidatedTextField(
            field = form.workshopName,
            onValueChange = viewModel::onWorkshopNameChange,
            label = "Nombre del taller *",
            placeholder = "Ej: Taller Manos del Telar",
            leadingIcon = Icons.Default.Storefront,
            maxLength = 60
        )
        ValidatedTextField(
            field = form.comuna,
            onValueChange = viewModel::onComunaChange,
            label = "Comuna *",
            placeholder = "Ej: Conchalí",
            leadingIcon = Icons.Default.LocationOn
        )

        SectionLabel("Seguridad")
        ValidatedTextField(
            field = form.password,
            onValueChange = viewModel::onRegisterPasswordChange,
            label = "Contraseña *",
            leadingIcon = Icons.Default.Lock,
            helperText = "Mínimo 8 caracteres, con letras y números",
            isPassword = true
        )
        ValidatedTextField(
            field = form.passwordConfirmation,
            onValueChange = viewModel::onPasswordConfirmationChange,
            label = "Repetir contraseña *",
            leadingIcon = Icons.Default.Lock,
            isPassword = true
        )

        // Reglamento
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .clickable { viewModel.onTermsChange(!form.acceptedTerms) }
        ) {
            Checkbox(
                checked = form.acceptedTerms,
                onCheckedChange = viewModel::onTermsChange,
                colors = CheckboxDefaults.colors(
                    checkedColor = TurquesaTextil,
                    uncheckedColor = if (form.termsError != null) MaterialTheme.colorScheme.error
                    else NegroCarbon.copy(alpha = 0.6f)
                )
            )
            Text(
                "Acepto el reglamento de socias y el uso de mis datos para la fundación",
                fontSize = 12.sp,
                color = NegroCarbon
            )
        }
        form.termsError?.let { message ->
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 12.dp)) {
                Icon(Icons.Default.Error, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(message, color = MaterialTheme.colorScheme.error, fontSize = 11.sp)
            }
        }

        SubmitButton(
            text = "Crear cuenta de socia",
            loadingText = "Creando cuenta...",
            isLoading = form.isSubmitting,
            onClick = { viewModel.submitRegister(onSuccess = onRegistered) }
        )

        TextButton(onClick = onGoToLogin, modifier = Modifier.fillMaxWidth()) {
            Text("¿Ya tienes cuenta? Inicia sesión", color = TurquesaOscuro)
        }
    }
}

// Componentes compartidos

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AuthScaffold(
    title: String,
    onBack: () -> Unit,
    content: @Composable () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title, fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = TurquesaTextil)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF9F9F8))
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            content()
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun AuthHeader(emoji: String, title: String, subtitle: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(TurquesaClaro),
            contentAlignment = Alignment.Center
        ) {
            Text(emoji, fontSize = 30.sp)
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(title, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = NegroCarbon)
        Text(
            subtitle,
            fontSize = 13.sp,
            color = NegroCarbon.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text,
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp,
        color = TurquesaOscuro,
        modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
    )
}

@Composable
private fun SubmitButton(
    text: String,
    loadingText: String,
    isLoading: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = !isLoading,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .height(50.dp),
        colors = ButtonDefaults.buttonColors(containerColor = TurquesaTextil),
        shape = RoundedCornerShape(12.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = Color.White)
            Spacer(modifier = Modifier.width(10.dp))
            Text(loadingText)
        } else {
            Text(text, fontWeight = FontWeight.Bold)
        }
    }
}
