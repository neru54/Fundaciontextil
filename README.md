# Textil Circular · Marketplace de la Fundación Textil Circular

Aplicación Android (Kotlin + Jetpack Compose + Material Design 3) de estilo **Marketplace** para la
Fundación Textil Circular. Tiene dos apartados:

1. **Vitrina (Explorar):** cualquier persona ve, sin registrarse, los productos y servicios que publican las
   socias, los busca y filtra por región, comuna, rubro y tipo, revisa el detalle con carrusel de fotos, visita
   la tienda de cada socia y le escribe.
2. **Apartado de las socias (Mi tienda):** cada emprendedora se inscribe, inicia sesión y **gestiona lo que
   publica**: crea publicaciones con fotos desde la cámara o la galería, las guarda como borrador, las publica,
   pausa, marca como agotadas, duplica o elimina, y revisa vistas y consultas. Además responde a las clientas por
   chat, recibe notificaciones y administra su perfil.

Proyecto de la asignatura **DSY1105 · Desarrollo de Aplicaciones Móviles** (Evaluación Parcial 2), basado en el
caso CITT "Gestión de Perfil, Catálogo Gráfico y Contactos para la Red Textil Circular".

## Integrantes

| Nombre | Rol principal |
|---|---|
| _Completar nombre y apellido_ | _Completar_ |
| _Completar nombre y apellido_ | _Completar_ |

> Antes de entregar, reemplacen esta tabla con sus nombres y agreguen el enlace a su tablero de Trello.

**Tablero Trello:** _agregar enlace_ · Propuesta de tarjetas en [`docs/planificacion_trello.md`](docs/planificacion_trello.md).

## Documentación

- ERS actualizado (V5 Marketplace): [`docs/ERS_V8_Fundacion_Textil_Circular_Marketplace.docx`](docs/ERS_V8_Fundacion_Textil_Circular_Marketplace.docx)
  y versión legible en GitHub: [`docs/ERS.md`](docs/ERS.md).
- Los cambios de la V5 agregan RF06 (vitrina del marketplace), RF07 (gestión de publicaciones de la socia),
  RN18 a RN24, RNF13, RNF14, Anexo B (formulario de publicación) y Anexo C (mapa de navegación).

## Funcionalidades implementadas

| Módulo | Qué hace | Requisitos |
|---|---|---|
| Vitrina | Grilla de publicaciones visibles, búsqueda, filtros por región, comuna (dependiente), rubro y tipo, orden por fecha o precio | RF06.1, RF06.2, RF06.5, RN19 |
| Detalle y tienda | Carrusel de fotos, precio o "A convenir", disponibilidad, tienda pública con WhatsApp e Instagram solo si son válidos | RF06.3, RF06.4, RN10, RN11 |
| Consulta de la clienta | Registro simple (nombre y correo) recordado en el dispositivo, mensaje sugerido con referencia al producto | RF06.6, RF04.6, RN16 |
| Inscripción | Formulario de 6 pasos con progreso, validación por campo, región y comuna dependientes, máximo 2 rubros, preguntas condicionales, autorización obligatoria | RF01.1, RF01.2, RF01.6, RF01.7 |
| Acceso | Inicio de sesión persistente, ícono de ojo, recuperación de contraseña con código (envío simulado) | RF01.9, RF01.10, RNF11 |
| Mi tienda | Resumen (publicadas, vistas, consultas), filtro por estado, menú de acciones por publicación | RF07.1, RF07.2, RF07.6, RF07.8 |
| Editor de publicación | Fotos desde cámara o galería (1 a 5, portada elegible, compresión), tipo, título, descripción, rubro, precio o a convenir, disponibilidad y días; borrador o publicar | RF02, RF07.3 a RF07.5, RN06, RN21 |
| Eliminar y duplicar | Confirmación antes de eliminar, borrado de fotos, la conversación conserva el título | RF07.7, RF07.10, RN23 |
| Así te ven | Vista previa de la tienda tal como la ve la clienta, con aviso si no aparece | RF07.9 |
| Mensajes | Conversaciones con no leídos, chat de texto, respuestas rápidas, botón "Simular consulta" | RF04.2, RF04.3 |
| Notificaciones | Aviso al llegar una consulta o una novedad; al tocarlo abre la conversación | RF04.1, RF04.5 |
| Perfil | Estado de ficha (validación simulada), "Falta completar", Modo Pausa, edición, novedades, cerrar sesión y darse de baja con contraseña | RF01.3, RF01.11 a RF01.13 |

### Cómo responde a la pauta de evaluación

| Indicador | Dónde se ve |
|---|---|
| IE 2.1.1 Interfaz y navegación | Barra inferior con 4 pestañas, pantallas secundarias con barra superior y volver, tema de la Fundación (`ui/theme`), mapa de navegación en el Anexo C del ERS |
| IE 2.1.2 Formularios con validación visual | Inscripción (6 pasos), login, recuperación, editor de publicación, edición de perfil, consulta de la clienta, chat y baja. Cada campo muestra ícono de error o de correcto y mensaje (`ui/components/Fields.kt`) |
| IE 2.2.1 Validación desacoplada | Reglas en `domain/validation` y `domain/rules` (Kotlin puro, sin Android), usadas por los ViewModels; la UI solo muestra el estado |
| IE 2.2.2 Animaciones | Transición entre pasos, sacudida de campos con error, aparición escalonada de tarjetas, `animateItem` en listas, color animado de estados, indicador del carrusel, burbujas del chat, progreso al comprimir fotos, transiciones de navegación y logo de bienvenida |
| IE 2.3.1 Modularidad y persistencia | MVVM + Repository: `domain`, `data` (Room, DataStore, repositorios), `platform`, `ui` (pantallas por módulo con su ViewModel). Room es la fuente de verdad |
| IE 2.3.2 GitHub y Trello | Commits por módulo con mensajes técnicos; propuesta de tablero en `docs/planificacion_trello.md` |
| IE 2.4.1 Dos o más recursos nativos | **Cámara** (permiso en tiempo de ejecución, `TakePicture` + `FileProvider`), **galería** (selector de fotos del sistema) y **notificaciones** (permiso `POST_NOTIFICATIONS`, canales, apertura desde el aviso) |

## Arquitectura

```
app/src/main/java/cl/textilcircular/app/
├── domain/            # Kotlin puro: modelos, catálogo Región→Comuna, validadores y reglas de negocio
│   ├── catalog/       # 16 regiones y 346 comunas (RN01)
│   ├── model/
│   ├── rules/         # Visibilidad en vitrina, propiedad, estados, límites (RN06, RN19, RN20)
│   └── validation/    # RUT módulo 11, teléfono, Instagram, formularios de inscripción y publicación
├── data/
│   ├── local/         # Room: entidades, DAOs, base de datos y mappers
│   ├── media/         # Compresión y almacenamiento de fotos
│   ├── repository/    # Auth, Marketplace, Socia y Chat
│   ├── security/      # Hash BCrypt
│   ├── seed/          # Dataset ficticio de demostración
│   └── session/       # DataStore: sesión y datos de la clienta
├── platform/          # Notificaciones
├── ui/
│   ├── components/    # Campos validados, tarjetas, estados vacíos, animaciones
│   ├── navigation/    # Rutas y NavHost
│   ├── screens/       # bienvenida, auth, vitrina, mitienda, mensajes, perfil (pantalla + ViewModel)
│   └── theme/
├── AppContainer.kt    # Inyección de dependencias manual
├── MainActivity.kt
└── TextilApp.kt
```

## Pasos para ejecutar

1. Instalar **Android Studio** (Ladybug 2024.2 o superior) con el SDK de Android 35 y JDK 17.
2. Clonar el repositorio y abrir la carpeta raíz en Android Studio. Esperar la sincronización de Gradle.
3. Elegir un emulador o teléfono con Android 8.0 (API 26) o superior y presionar **Run**.
4. En la bienvenida:
   - **Quiero explorar la vitrina**: navega como clienta, sin cuenta.
   - **Soy socia**: en la pantalla de ingreso toca el aviso amarillo para completar la cuenta de demostración
     `demo@textilcircular.cl` / `demo1234`, o inscríbete con datos ficticios.

Para probar la cámara en el emulador, en *Extended controls → Camera* elijan una escena virtual. Para ver las
notificaciones, acepten el permiso al ingresar como socia y usen **Mensajes → Simular consulta**, o escriban a una
socia desde la vitrina.

### Pruebas unitarias

```bash
./gradlew test
```

Cubren validadores (RUT, teléfono, Instagram, nombre, correo), catálogo geográfico, formulario de publicación,
transiciones de estado, propiedad y visibilidad en la vitrina (`app/src/test`).

### APK firmado (release)

```bash
keytool -genkeypair -v -keystore textil-release.jks -alias textil -keyalg RSA -keysize 2048 -validity 10000
```

Crear `keystore.properties` en la raíz (no se sube al repositorio):

```properties
storeFile=textil-release.jks
storePassword=********
keyAlias=textil
keyPassword=********
```

Luego `./gradlew assembleRelease`. El APK queda en `app/build/outputs/apk/release/`.

## Datos y privacidad

La app usa **solo datos ficticios** (Ley N.º 21.719 y Ley N.º 19.628). Los RUT de ejemplo tienen dígito
verificador válido, pero son inventados. RUT, correo y contraseña nunca se muestran en la vitrina y la contraseña
se guarda como hash BCrypt. Sin servidor, el envío de correos, la validación entre pares y las notificaciones push
se simulan como indica la sección 1.1 del ERS.
