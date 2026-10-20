# Textil Circular 🧵♻️

Aplicación móvil Android para la **Fundación Textil Circular**, una red de socias artesanas que confeccionan prendas y accesorios a partir de mermas y retazos textiles (*upcycling*).

La app conecta a las socias con compradoras: las socias publican sus prendas con su trazabilidad ambiental, responden consultas de clientas y postulan a las convocatorias de la fundación (ferias, talleres y entrega de fardos textiles). Las compradoras pueden recorrer el catálogo, agregar prendas al carrito y confirmar sus pedidos.

> **Asignatura:** DSY1105 – Desarrollo de Aplicaciones Móviles · Evaluación Parcial 2
>
> **Integrantes:**
> - Pablo Cabezas — arquitectura MVVM y persistencia (Room, DataStore)
> - Cristian Díaz — formularios, validaciones, seguridad y tests
> - Claudia Díaz — navegación, animaciones y recursos nativos

---

## Funcionalidades

| Módulo | Qué hace |
|---|---|
| **Catálogo** | Buscador en tiempo real, filtros por categoría, tarjetas con socia, comuna y etiqueta ecológica. Detalle de cada prenda con su trazabilidad (kg de tela rescatada, litros de agua ahorrados). |
| **Carrito y checkout** | Agregar y quitar prendas, total en CLP y formulario de datos de envío validado. Los pedidos quedan guardados en la base de datos. |
| **Registro e inicio de sesión de socias** | Registro con RUT (dígito verificador), correo, celular chileno, comuna, taller y contraseña segura. La sesión se mantiene al cerrar la app. |
| **Mi Taller** | Perfil de la socia, impacto ambiental calculado con sus prendas y formulario para **publicar prendas con foto** (cámara o galería). |
| **Anuncios (solo socias)** | Convocatorias de la fundación con cupos. Postulación con formulario de motivación validado. |
| **Mensajes (solo socias)** | Bandeja de consultas, chat con cada clienta, contador de no leídos y **notificaciones nativas** que abren la conversación. |

### Requisitos de la evaluación y dónde se cumplen

| Requisito | Implementación |
|---|---|
| Interfaz organizada y navegación clara | Navigation Compose con rutas (`ui/navigation`), barra inferior en las 4 secciones principales y barra con "volver" en las pantallas de detalle. |
| Formularios validados con íconos y mensajes | 7 formularios: login, registro, publicar prenda, checkout, postulación, chat y buscador. Cada campo muestra borde rojo, ícono de error, mensaje bajo el campo, check verde cuando es válido y contador de caracteres (`ui/components/ValidatedTextField.kt`). |
| Validaciones desacopladas de la interfaz | Toda la lógica está en `domain/validation` (Kotlin puro, sin Android), los ViewModels guardan el estado de cada campo (`FieldState`) y las pantallas solo lo muestran. Tiene **25 tests unitarios**. |
| Animaciones funcionales | Transiciones entre pantallas, campos que se sacuden al marcar error, mensajes de error animados, contador del carrito que salta, botón "Agregado ✓", listas con `animateItem()` e íconos de éxito con rebote. |
| Estructura modular, MVVM y persistencia local | Capas `data` / `domain` / `ui`, un ViewModel por pantalla, repositorios, base de datos **Room** (7 tablas, con migración 1→2) y **DataStore** para la sesión. |
| Dos o más recursos nativos | **Cámara** (permiso en tiempo de ejecución + FileProvider), **selector de fotos** del sistema, **notificaciones** (permiso POST_NOTIFICATIONS en Android 13+, con explicación previa) y **vibración**. Si se niega un permiso, se ofrece ir a Ajustes. |

---

## Arquitectura (MVVM)

```
Pantalla (Compose)  ──eventos──▶  ViewModel  ──▶  Repositorio  ──▶  Room / DataStore / Archivos
        ▲                           │
        └──── estado (StateFlow) ───┘           Validadores (domain/validation)
```

```
android/app/src/main/java/com/example/
├── data/
│   ├── local/            Room (AppDatabase, entidades, DAOs, migración), DataStore de sesión,
│   │                     ImageStorage (fotos), PasswordHasher, carga de datos iniciales
│   ├── repository/       ProductRepository, CartRepository, OrderRepository, ChatRepository,
│   │                     AnnouncementRepository, AuthRepository
│   └── SampleData.kt     Datos de ejemplo que se cargan en la primera ejecución
├── di/AppContainer.kt    Inyección de dependencias manual
├── domain/
│   ├── model/            Modelos de dominio (Product, Partner, Chat...)
│   ├── validation/       Reglas y validadores de cada formulario
│   └── util/             NotificationHelper, VibrationHelper
└── ui/
    ├── navigation/       Rutas y NavHost (con guardas para secciones de socias)
    ├── viewmodel/        Un ViewModel por pantalla + estado de formularios
    ├── screens/          Pantallas en Jetpack Compose
    ├── components/       Componentes reutilizables (campo validado, animaciones...)
    ├── permissions/      Solicitud de permisos y acceso a Ajustes
    └── theme/            Colores y tema Material 3
```

**Tecnologías:** Kotlin · Jetpack Compose · Material 3 · Navigation Compose · ViewModel + StateFlow · Room · DataStore · KSP · ExifInterface · JUnit 4.

**Seguridad:** las contraseñas se guardan con hash SHA-256 iterado y una sal aleatoria por socia (nunca en texto plano). La sesión solo almacena el id de la socia. Las fotos se comparten con la cámara mediante FileProvider, sin exponer rutas del dispositivo. La app no pide permisos de almacenamiento ni de internet.

---

## Cómo ejecutar el proyecto

1. Instalar **Android Studio** (Ladybug o superior) con **JDK 17**.
2. Abrir Android Studio → **File › Open** → seleccionar la carpeta **`android`** de este repositorio.
3. Esperar el **Gradle Sync** (descarga las dependencias la primera vez).
4. Crear o elegir un emulador (API 24 o superior; se recomienda API 33+ para probar el permiso de notificaciones).
5. Presionar **Run ▶ 'app'**.

**Cuenta de prueba (socia):**
- Correo: `valentina@textilcircular.cl`
- Contraseña: `telar2026`

También se puede crear una cuenta nueva desde "Quiero ser socia". Por ejemplo, el RUT `12.345.678-5` es válido.

**Tests unitarios:** en el panel del proyecto, clic derecho sobre `app/src/test` → **Run 'Tests in...'**, o desde la terminal:

```bash
cd android
./gradlew testDebugUnitTest
```

### Cómo probar los recursos nativos

- **Cámara y galería:** Mi Taller → "Publicar Nueva Prenda" → "Tomar Foto" o "Galería".
- **Notificaciones y vibración:** Mensajes → "Simular mensaje de una clienta". La primera vez la app explica por qué necesita el permiso; al tocar la notificación se abre el chat.
