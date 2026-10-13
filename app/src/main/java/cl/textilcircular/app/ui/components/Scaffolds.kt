package cl.textilcircular.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** Estructura común de una pantalla secundaria: barra superior con volver y snackbar. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaSecundaria(
    titulo: String,
    alVolver: (() -> Unit)?,
    modifier: Modifier = Modifier,
    snackbar: SnackbarHostState = remember { SnackbarHostState() },
    acciones: @Composable RowScope.() -> Unit = {},
    barraInferior: @Composable () -> Unit = {},
    botonFlotante: @Composable () -> Unit = {},
    contenido: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(titulo) },
                navigationIcon = {
                    if (alVolver != null) {
                        IconButton(onClick = alVolver) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                        }
                    }
                },
                actions = acciones
            )
        },
        bottomBar = barraInferior,
        floatingActionButton = botonFlotante,
        snackbarHost = { SnackbarHost(snackbar) },
        content = contenido
    )
}

/** Secciones de la socia sin sesión: invita a ingresar o inscribirse (Anexo C). */
@Composable
fun AccesoRequerido(
    titulo: String,
    alIngresar: () -> Unit,
    alInscribirse: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        EstadoVacio(
            icono = Icons.Filled.Lock,
            titulo = titulo,
            mensaje = "Esta sección es para las socias de la Fundación. Ingresa o inscríbete para gestionar tu tienda."
        )
        Button(onClick = alIngresar, modifier = Modifier.padding(horizontal = 24.dp)) { Text("Ya soy socia, ingresar") }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = alInscribirse) { Text("Quiero inscribirme") }
    }
}

@Composable
fun ColumnaFormulario(modifier: Modifier = Modifier, contenido: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier.padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        content = contenido
    )
}
