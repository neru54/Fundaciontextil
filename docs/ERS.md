**Especificación de Requisitos del Sistema (ERS)**

**Versión 5.0 — Marketplace**

Proyecto: Gestión de Perfil, Catálogo Gráfico y Contactos para la Red
Textil Circular

|                     |                                              |
|---------------------|----------------------------------------------|
| **Organización**    | Fundación Textil Circular                    |
| **Asignatura**      | DSY1105 · Desarrollo de Aplicaciones Móviles |
| **Versión**         | ERS V5                                       |
| **Rol responsable** | Jefatura de Proyecto TI                      |

# **0. Historial de cambios**

## **Cambios de la V5 (esta versión)**

- Origen: definición del equipo para la Evaluación Parcial 2. El
  proyecto pasa a ser de estilo Marketplace: una vitrina de productos y
  servicios de todas las socias, más un apartado propio para que cada
  socia gestione lo que publica.

- Nuevo módulo RF06 · Marketplace (vitrina): explorar publicaciones sin
  iniciar sesión, búsqueda, filtros por región, comuna, rubro y tipo,
  detalle de publicación y tienda de la socia.

- Nuevo módulo RF07 · Panel de la socia ("Mi tienda"): crear, editar,
  guardar como borrador, publicar, pausar, marcar como agotada y
  eliminar publicaciones, con métricas de vistas y consultas.

- La clienta pasa a usar la app para explorar (perfil 2.2). Su registro
  simple antes de escribir se construye en la app (RF06.6), que
  reemplaza a RF04.7 dentro de la app.

- El catálogo gráfico (RF02) se organiza por publicación. RN06 cambia: 5
  fotos por publicación y 20 publicaciones por socia, configurables.

- Nuevas reglas RN18 a RN24 (sin pagos en línea, visibilidad en vitrina,
  propiedad de las publicaciones, tipos de publicación, eliminación y
  baja). Nuevo Anexo B (formulario de publicación y de consulta) y Anexo
  C (mapa de navegación).

## **Cambios de la V4**

- Origen: reunión con la Fundación del 25-09-2026. Se incorporan los
  puntos conversados que no estaban en la V3.

- Validación de fichas nuevas: la Fundación validará que la emprendedora
  sea quien dice ser, viva donde dice y haga lo que dice, mediante
  validación entre emprendedoras. (RF01.12, RN14).

- Baja definitiva: la emprendedora puede eliminar su cuenta ella misma,
  con la misma facilidad con que se inscribió (RF01.13, RN15).

- Sección de Novedades de la Fundación dentro de la app, en lugar de
  correos masivos que llegan a spam (RF04.5).

- Chat: mensaje inicial sugerido y referencia al producto desde el que
  se escribe (RF04.6). Registro simple de la clienta antes de escribir,
  del lado del sitio web (RF04.7, RN16).

- Identidad visual de la Fundación: colores, tipografías y estilo
  minimalista (RNF12).

- Rubros: pasan a ser un catálogo configurable que puede crecer, no una
  lista fija de 7 (RN02, RF01.1, Anexo A).

- Catálogo: cada producto exige al menos 1 foto y admite varias; sin
  video (RF02.4).

- Formulario: Instagram acepta el usuario con o sin @ o el enlace
  completo; el nombre se normaliza con mayúscula inicial; aviso si el
  RUT ya está registrado (RN17, Anexo A).

## **Cambios de la V3**

- Autenticación: correo y contraseña. Se agregan inicio de sesión
  (RF01.9) y recuperación de contraseña (RF01.10). La contraseña se crea
  como un paso propio del formulario, que pasa de 5 a 6 pasos.

- Verificación de cuentas históricas: el código se envía solo por
  correo. SMS queda como mejora futura y WhatsApp se descarta por su
  costo y complejidad. (decisión del equipo)

- Datos erróneos: se muestra el aviso "Falta completar" solo a la
  emprendedora dentro de la app, no en la ficha pública (decisión del
  equipo; RF01.11).

- Consentimiento: la Fundación indicó que el texto del sitio web es
  suficiente. Se mantiene ese texto. La RN07 pasa de "exigencia legal" a
  "riesgo identificado", con medidas simples (línea informativa bajo el
  RUT y fecha de aceptación guardada).

- Teléfono e Instagram se mantienen públicos en la ficha. La
  sincronización con el sitio web queda como etapa posterior.

- Alcance según indicaciones: la pauta no menciona servidor. Se agrega
  la sección 1.1 con dos escenarios (solo base local o con servidor) y
  se marca qué requisitos dependen de esa respuesta.

# **1. Alcance**

El proyecto consiste en una aplicación móvil Android de estilo
Marketplace para la Fundación Textil Circular. Tiene dos apartados: (1)
la vitrina, donde cualquier persona explora sin registrarse los
productos y servicios publicados por las socias, los filtra por región,
comuna, rubro y tipo, y les escribe; y (2) el apartado de las socias
("Mi tienda"), donde cada emprendedora se inscribe, inicia sesión,
administra su perfil y gestiona lo que publica: crea, edita, pausa,
marca como agotado o elimina sus publicaciones con su catálogo
fotográfico, y conversa con las clientas que la contactan.

Quedan fuera del proyecto: el panel de administración, el rediseño del
sitio WordPress, la migración real del dataset histórico y los pagos,
envíos o comisiones en línea (RN18; ver sección 7).

## **1.1 Servidor: dos escenarios** 

La pauta de presentación evalúa una app funcional, MVVM, persistencia
local, formularios, animaciones y dos recursos nativos, pero no menciona
servidor, API ni Retrofit. El equipo construye con la base local (Room)
como fuente de datos.

| **Tema**                    | **Si NO se requiere servidor**                                                                            | **Si SÍ se requiere servidor**                                                              |
|-----------------------------|-----------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------|
| **Vitrina del marketplace** | Publicaciones leídas desde Room, con socias y publicaciones de demostración precargadas.                  | Publicaciones leídas desde la API (endpoint público RF05.5) y guardadas en Room como caché. |
| **Mensajes de clientas**    | Conversaciones precargadas y un botón que simula un mensaje nuevo, guardados en Room.                     | El sitio web envía mensajes a una API; la app los recibe y sincroniza.                      |
| **Notificaciones**          | Notificación local lanzada por la propia app al llegar un mensaje simulado (usa el mismo recurso nativo). | Notificación push enviada por el servidor.                                                  |
| **Inicio de sesión**        | Se valida contra la base local, con la contraseña guardada como hash.                                     | Se valida contra el servidor.                                                               |
| **Código por correo**       | Envío simulado: el código se muestra o es fijo en el ambiente de pruebas.                                 | Servicio de correo en el servidor.                                                          |
| **Sincronización**          | Campo estado_sync preparado, sin sincronizar.                                                             | Cola de cambios pendientes y reintentos.                                                    |

# **2. Perfiles de usuario**

## **2.1 Emprendedora Textil / Socia (perfil principal: vende en el marketplace)**

Se inscribe, inicia sesión, gestiona su ficha y catálogo, recibe
notificaciones y responde consultas por chat interno. Opera con conexión
inestable y sincroniza de forma diferida.

En la V5, además, gestiona sus publicaciones desde "Mi tienda" (RF07):
las crea, edita, pausa, marca como agotadas o elimina, revisa cuántas
vistas y consultas recibe y ve su tienda tal como la ve la clienta.

## **2.2 Cliente / Consumidor (visitante del marketplace)**

Explora la vitrina de la app sin registrarse: busca, filtra, ve el
detalle de las publicaciones y la tienda de cada socia (RF06). Para
escribir a una socia hace un registro simple (nombre y correo) dentro de
la app (RF06.6, RN16). El sitio web WordPress sigue existiendo y su
integración queda para una etapa posterior (RF05.5, RF05.6).

## **2.3 Administradora del Directorio (fuera del proyecto)**

Por decisión de la Fundación, la app es solo para emprendedoras. Sus
tareas se documentan como requisitos futuros.

# **3. Requisitos Funcionales (RF)**

Prioridad: MVP = obligatorio para la presentación; Deseable = si el
tiempo lo permite; Futuro o Etapa posterior = documentado, fuera del
proyecto actual.

## **RF01 · Inscripción, Cuenta y Perfil**

| **Código**               | **Descripción**                                                                                                                                                                                                                                                   | **Prioridad**                             |
|--------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-------------------------------------------|
| **RF01.1 (actualizado)** | Inscripción de nueva emprendedora con los datos del formulario real de la Fundación, más el RUT y una contraseña (Anexo A). Región y comuna desde listas cerradas y dependientes; rubros: mínimo 1 y máximo 2 del catálogo de rubros (hoy 7 categorías, RN02).    | MVP                                       |
| **RF01.2**               | Validación previa al guardado de todos los campos, con íconos y mensajes visuales de error junto a cada campo (Anexo A).                                                                                                                                          | MVP                                       |
| **RF01.3 (actualizado)** | Edición autónoma del perfil y "Modo Pausa" para ocultar la ficha temporalmente. Una vez publicada la ficha (RF01.12), las ediciones no requieren aprobación previa.                                                                                               | MVP                                       |
| **RF01.4 (actualizado)** | Reclamación de ficha histórica: la emprendedora ingresa su correo, recibe un código de 6 dígitos por correo, lo confirma y crea su contraseña. Solo por correo. Se demuestra con datos ficticios y envío simulado.                                                | Deseable                                  |
| **RF01.5**               | Separación entre datos públicos, estadísticos y privados (Anexo A). RUT, correo, contraseña y datos estadísticos nunca se muestran en la ficha pública.                                                                                                           | MVP                                       |
| **RF01.6 (actualizado)** | Formulario de inscripción en 6 pasos con indicador de progreso. "¿Quién te invitó?" aparece solo si responde que la recomendó otra emprendedora. "¿Aspiras a que sea tu principal ingreso?" aparece solo si no marcó que la actividad ya es su principal ingreso. | MVP                                       |
| **RF01.7**               | Autorización de datos obligatoria con el texto actual de la Fundación. Sin aceptarla no se crea la cuenta. Se guarda la fecha y hora de aceptación.                                                                                                               | MVP                                       |
| **RF01.8**               | Campo opcional "Código de Embajadora". Su autocompletado desde un enlace de invitación queda como mejora.                                                                                                                                                         | Deseable                                  |
| **RF01.9 (nuevo)**       | Inicio de sesión con correo y contraseña. Ícono de ojo para ver lo escrito y sesión que se mantiene abierta, para que funcione sin conexión una vez iniciada.                                                                                                     | MVP                                       |
| **RF01.10 (nuevo)**      | Recuperación de contraseña: la emprendedora ingresa su correo, recibe un código y define una contraseña nueva. Mismo flujo de verificación que RF01.4; envío simulado en la demostración.                                                                         | MVP                                       |
| **RF01.11 (nuevo)**      | Aviso "Falta completar" en el perfil cuando un dato obligatorio está vacío o no cumple las validaciones. Visible sólo para la emprendedora en la app. Reutiliza las validaciones del formulario.                                                                  | Deseable                                  |
| **RF01.12 (nuevo)**      | Estado de publicación de la ficha: "Pendiente de validación" o "Publicada". Una ficha nueva queda pendiente hasta ser validada (RN14) y la emprendedora ve ese estado en su perfil. En la demostración la aprobación se simula.                                   | Deseable (validación entre pares: Futuro) |
| **RF01.13 (nuevo)**      | Darse de baja: la emprendedora elimina su cuenta desde la app, sin intermediarios. Pide confirmación con su contraseña, borra sus datos personales, fotos y ficha, y cierra la sesión. Es distinto del Modo Pausa (RF01.3), que solo oculta la ficha (RN15).      | MVP                                       |

## 

## 

## **RF02 · Catálogo Gráfico**

| **Código**         | **Descripción**                                                                                                                                                                                                                                         | **Prioridad** |
|--------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|---------------|
| **RF02.1**         | Integración nativa con cámara y galería para agregar fotografías.                                                                                                                                                                                       | MVP           |
| **RF02.2**         | Las fotografías se asocian a una publicación (RF07), que tiene título, descripción, rubro, tipo, precio y disponibilidad (Anexo B). Máximo 5 fotos por publicación y 20 publicaciones por socia, parámetros configurables (RN06, actualizada en la V5). | MVP           |
| **RF02.3**         | Compresión automática de imágenes en el dispositivo antes de guardarlas o subirlas.                                                                                                                                                                     | MVP           |
| **RF02.4 (nuevo)** | Cada producto exige al menos 1 foto y admite varias, que se ven como carrusel. Una sola foto basta para publicar, pensando en quienes manejan poco la tecnología. Sin video (mejora futura). El total de fotos del perfil respeta RN06.                 | MVP           |

## **RF03 · Funcionamiento Offline y Sincronización**

| **Código** | **Descripción**                                                                                    | **Prioridad**                     |
|------------|----------------------------------------------------------------------------------------------------|-----------------------------------|
| **RF03.1** | Crear y editar perfil, catálogo y mensajes sin conexión, guardando en la base local (Room/SQLite). | MVP                               |
| **RF03.2** | Detección de reconexión y sincronización automática de cambios pendientes con el servidor.         | Etapa posterior (si hay servidor) |
| **RF03.3** | Pantalla de estado de sincronización con cola de reintentos y sincronización manual.               | Deseable                          |

## **RF04 · Notificaciones y Chat Interno**

| **Código**                  | **Descripción**                                                                                                                                                                                                                                                                                                      | **Prioridad**   |
|-----------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-----------------|
| **RF04.1**                  | Notificación cuando llega un mensaje nuevo de una clienta: push si hay servidor, local si no.                                                                                                                                                                                                                        | MVP             |
| **RF04.2**                  | Chat interno de texto simple: lista de conversaciones con indicador de no leídos y pantalla de conversación para enviar y recibir texto. Sin multimedia, confirmación de lectura ni "escribiendo…".                                                                                                                  | MVP             |
| **RF04.3**                  | Los mensajes se guardan localmente en Room y se sincronizan al recuperar conexión (si hay servidor).                                                                                                                                                                                                                 | MVP             |
| **RF04.4**                  | Plantillas de respuesta rápida editables.                                                                                                                                                                                                                                                                            | Deseable        |
| **RF04.5 (nuevo)**          | Sección "Novedades" de la Fundación: avisos, eventos y noticias para todas las emprendedoras, con notificación al llegar una nueva. Reemplaza el correo masivo, que suele llegar a spam. Sin servidor: novedades precargadas en Room.                                                                                | Deseable        |
| **RF04.6 (nuevo)**          | Contexto de producto en el chat: si la clienta escribe desde un producto, el primer mensaje trae un texto sugerido ("Quiero saber más sobre este producto") y la conversación muestra a qué producto se refiere. El sitio web propone el texto; la app muestra la referencia.                                        | Deseable        |
| **RF04.7 (actualizado V5)** | Identificación de la clienta: puede ver la vitrina y las tiendas sin registrarse, pero hace un registro simple antes de escribir (RN16). En la V5 este registro se construye en la app (RF06.6); la socia ve el nombre de la clienta en la conversación. El registro en el sitio web queda para una etapa posterior. | MVP (en la app) |

## 

## 

## **RF05 · Servidor y Trazabilidad (En etapa posterior)**

| **Código**         | **Descripción**                                                                                                                                                                                                                                    | **Prioridad**            |
|--------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|--------------------------|
| **RF05.1**         | Envío real de correos (bienvenida, código de verificación, recuperación). En la demostración se simula.                                                                                                                                            | Etapa posterior          |
| **RF05.2**         | Registro de eventos de interacción para métricas futuras.                                                                                                                                                                                          | Etapa posterior          |
| **RF05.3**         | Migración del dataset histórico de 982 registros. Se reemplaza por dataset ficticio.                                                                                                                                                               | Futuro                   |
| **RF05.4**         | Panel web de moderación para la Administradora.                                                                                                                                                                                                    | Futuro                   |
| **RF05.5 (nuevo)** | Endpoint público de solo lectura que entregue las fichas con sus campos públicos (nombre del emprendimiento, ubicación, rubros, teléfono, Instagram). Define el contrato para que el sitio web lea los datos de la app en lugar del Google Sheets. | Futuro (mejora opcional) |
| **RF05.6 (nuevo)** | Que el sitio WordPress consuma ese endpoint para reflejar los cambios de teléfono e Instagram. Requiere trabajo en el sitio web, que está fuera del proyecto.                                                                                      | Futuro                   |

## **RF06 · Marketplace: Vitrina de publicaciones (nuevo V5)**

| **Código** | **Descripción**                                                                                                                                                                                                                                                                                                                                     | **Prioridad** |
|------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|---------------|
| **RF06.1** | Vitrina pública "Explorar": pantalla inicial con las publicaciones visibles de todas las socias (RN19), en grilla de tarjetas con foto, título, precio o "A convenir", nombre de la tienda y comuna. Accesible sin iniciar sesión (RN16).                                                                                                           | MVP           |
| **RF06.2** | Búsqueda por texto (título, descripción y nombre de la tienda) y filtros combinables por región, comuna (dependiente de la región, RN01), rubro y tipo de publicación (RN22). Filtros como chips, botón "Limpiar filtros" y mensaje claro cuando no hay resultados. Elimina las fichas invisibles del sistema actual.                               | MVP           |
| **RF06.3** | Detalle de publicación: carrusel de fotos, título, precio, disponibilidad, descripción, rubro, tipo, tienda y ubicación, con el botón "Consultar a la socia".                                                                                                                                                                                       | MVP           |
| **RF06.4** | Tienda de la socia: ficha pública (nombre comercial, comuna y región, rubros, botones de WhatsApp e Instagram solo si los datos son válidos, RN10 y RN11) y grilla con sus publicaciones visibles.                                                                                                                                                  | MVP           |
| **RF06.5** | Ordenar la vitrina por más recientes, menor precio o mayor precio.                                                                                                                                                                                                                                                                                  | Deseable      |
| **RF06.6** | Consulta de la clienta: antes de escribir hace un registro simple (nombre y correo, validados con íconos y mensajes) que queda guardado en el dispositivo. El primer mensaje trae el texto sugerido y la referencia a la publicación (RF04.6). La consulta crea o continúa la conversación en el chat de la socia y lanza la notificación (RF04.1). | MVP           |
| **RF06.7** | Favoritos de la clienta guardados en el dispositivo.                                                                                                                                                                                                                                                                                                | Futuro        |
| **RF06.8** | Trazabilidad local: cada apertura del detalle suma una vista y cada consulta suma un contacto a la publicación. Se sincronizarán con RF05.2 cuando exista servidor.                                                                                                                                                                                 | MVP           |

## **RF07 · Apartado de la socia: gestión de publicaciones "Mi tienda" (nuevo V5)**

| **Código**  | **Descripción**                                                                                                                                                                                                                                                                                                      | **Prioridad** |
|-------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|---------------|
| **RF07.1**  | Sección "Mi tienda", visible solo con sesión iniciada: resumen con publicaciones activas, pausadas y borradores, y total de vistas y consultas; debajo, el listado de sus publicaciones con foto, precio y estado.                                                                                                   | MVP           |
| **RF07.2**  | Filtro del listado por estado: Todas, Publicadas, Pausadas, Borradores y Agotadas.                                                                                                                                                                                                                                   | MVP           |
| **RF07.3**  | Crear publicación con el formulario del Anexo B: fotos desde cámara o galería (RF02.1 a RF02.4), tipo, título, descripción, rubro, precio o "A convenir", disponibilidad y días de elaboración si es a pedido. Cada campo muestra ícono y mensaje de error (RF01.2); las reglas viven en la capa de dominio (RNF08). | MVP           |
| **RF07.4**  | Guardar como borrador (solo exige el título) o publicar (exige todas las validaciones). Funciona sin conexión, guardando en Room (RF03.1).                                                                                                                                                                           | MVP           |
| **RF07.5**  | Editar una publicación existente. Los cambios se ven en la vitrina al guardar, sin aprobación previa una vez publicada la ficha de la socia (RN14).                                                                                                                                                                  | MVP           |
| **RF07.6**  | Cambiar el estado con una acción: Publicar, Pausar, Reactivar o Marcar como agotada. Pausar oculta la publicación sin borrarla (RN19).                                                                                                                                                                               | MVP           |
| **RF07.7**  | Eliminar una publicación con diálogo de confirmación. Se borran sus fotos del dispositivo (RN23).                                                                                                                                                                                                                    | MVP           |
| **RF07.8**  | Métricas por publicación: vistas y consultas recibidas (RF06.8).                                                                                                                                                                                                                                                     | MVP           |
| **RF07.9**  | Vista previa "Así te ven": la socia abre su tienda tal como la ve la clienta, con un aviso si no aparece en la vitrina (ficha pendiente de validación o Modo Pausa).                                                                                                                                                 | MVP           |
| **RF07.10** | Duplicar una publicación para crear variantes rápidamente (queda como borrador).                                                                                                                                                                                                                                     | Deseable      |

# **4. Requisitos No Funcionales (RNF)**

| **Código**                                            | **Descripción**                                                                                                                                                                                                                                                                                                            |
|-------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **RNF01 — Plataforma**                                | Android, compatible con gama media y baja. Kotlin + Jetpack Compose + Material Design 3. Arquitectura MVVM modular con patrón Repository. Persistencia local con Room/SQLite funcionando de forma real.                                                                                                                    |
| **RNF02 — Usabilidad**                                | Interfaz simple, lenguaje claro, alto contraste (WCAG 2.1 AA), íconos reconocibles, para usuarias de todas las edades y con poco manejo tecnológico. Un tema por pantalla en el formulario.                                                                                                                                |
| **RNF03 — Rendimiento y red**                         | Tolerancia a conexión inestable, compresión de imágenes y reintento exponencial (WorkManager) cuando haya sincronización.                                                                                                                                                                                                  |
| **RNF04 — Almacenamiento local**                      | Limpieza periódica de caché de imágenes y miniaturas, con límite de 100 MB.                                                                                                                                                                                                                                                |
| **RNF05 — Seguridad y privacidad**                    | Cumplimiento de Ley 21.719 y Ley 19.628. Pruebas solo con datos ficticios, incluidos RUT de ejemplo con dígito verificador válido pero inexistentes. RUT, correo y contraseña nunca se publican.                                                                                                                           |
| **RNF06 — Catálogo geográfico**                       | Región y comuna provienen de un catálogo cerrado y jerárquico, nunca de texto libre.                                                                                                                                                                                                                                       |
| **RNF07 — Animaciones**                               | Animaciones que aportan fluidez o retroalimentación. Propuesta mínima: transición entre pasos del formulario, animación de error en campos inválidos, animación al enviar un mensaje, aparición animada de las tarjetas de la vitrina, cambio animado de estado de una publicación y progreso animado al guardar una foto. |
| **RNF08 — Validaciones desacopladas**                 | Las reglas de validación viven en el ViewModel o en una capa de dominio, no en los componentes visuales. La interfaz solo muestra el resultado.                                                                                                                                                                            |
| **RNF09 — Recursos nativos**                          | Dos recursos nativos integrados con la interfaz: cámara/galería (RF02.1) y notificaciones (RF04.1).                                                                                                                                                                                                                        |
| **RNF10 — Control de versiones**                      | Repositorio GitHub con commits frecuentes y repartidos entre integrantes, tablero Trello.                                                                                                                                                                                                                                  |
| **RNF11 — Autenticación (nuevo)**                     | Correo y contraseña. Mínimo 8 caracteres, sin exigir símbolos ni mayúsculas, para facilitar su uso. La contraseña se guarda como hash (BCrypt o equivalente), nunca en texto plano. La sesión se mantiene abierta para operar sin conexión.                                                                                |
| **RNF12 — Identidad visual (nuevo)**                  | La app mantiene la "voz" de la Fundación: colores blancos, negro, amarillo y turquesa (con sus códigos oficiales) y tipografías Monument y Nunito. Estilo minimalista y limpio. El tema de Material Design 3 se personaliza con estos colores, cuidando el contraste exigido en RNF02.                                     |
| **RNF13 — Rendimiento de la vitrina (nuevo V5)**      | La vitrina usa listas perezosas (LazyVerticalGrid) y miniaturas comprimidas, de modo que se desplace con fluidez en gama baja. Los filtros se resuelven con consultas en Room, no recorriendo listas en memoria.                                                                                                           |
| **RNF14 — Propiedad de las publicaciones (nuevo V5)** | Toda operación de edición, cambio de estado o eliminación verifica en el repositorio que la publicación pertenezca a la socia con sesión iniciada (RN20). La vitrina nunca expone datos privados de la socia (RF01.5).                                                                                                     |

# 

# **5. Reglas de negocio**

- RN01: Región–comuna es un catálogo jerárquico cerrado (Región 1–N
  Comuna). Causa raíz confirmada del problema de fichas invisibles:
  Google Forms crea una columna distinta de comuna por cada región.

- RN02 (actualizada): Los rubros parten con las 7 categorías del
  formulario actual, máximo 2 por emprendedora. Se guardan como un
  catálogo configurable (tabla en la base local), no fijo en el código,
  porque la Fundación indicó que pueden ir creciendo.

- RN03: Ningún dato se guarda sin cumplir su validación mínima. El
  formulario actual aceptaba un nombre de 2 letras y un teléfono de 2
  dígitos.

- RN04: Se mantiene el criterio de la Fundación: los datos marcados como
  estadísticos no se publican.

- RN05 (actualizada V5): El perfil Administradora está fuera del
  proyecto. La app tiene dos usos: socia (con sesión: vende y gestiona
  su tienda) y clienta o visitante (explora sin sesión y hace un
  registro simple solo para escribir).

- RN06 (actualizada V5): Máximo 5 fotos por publicación y 20
  publicaciones por socia, sin contar las eliminadas. Reemplaza el
  límite de 10 fotos por perfil, que no calza con un marketplace.
  Decisión técnica del equipo, configurable.

- RN07 (reescrita): El formulario actual ya incluye una autorización
  para compartir contacto, incluir a la emprendedora en la base del
  sitio web y enviarle información. La Fundación indicó que ese
  consentimiento es suficiente para la app. Riesgo identificado: la app
  incorpora datos y usos que ese texto no menciona (RUT, chat, fotos,
  notificaciones). Medidas del equipo: mantener el texto de la
  Fundación, agregar una línea informativa bajo el RUT ("Se usa solo
  para identificarte. No se publica.") y guardar la fecha de aceptación.
  El equipo no redacta texto legal propio; la responsable de los datos
  es la Fundación.

- RN08: El RUT se valida con formato chileno y dígito verificador por
  módulo 11, se guarda normalizado y es un dato privado.

- RN09: El chat es de texto simple. El contacto entre clienta y
  emprendedora ocurre dentro de la app.

- RN10 (nuevo): "Falta completar" se calcula con las mismas validaciones
  del formulario y se muestra solo a la emprendedora dentro de la app.
  La ficha pública no muestra ese texto; simplemente omite el dato
  inválido (por ejemplo, el botón de Instagram si el enlace es
  incorrecto).

- RN11 (nuevo): Teléfono e Instagram son datos públicos de la ficha,
  igual que hoy en el sitio de la Fundación. El chat interno es la vía
  principal de contacto y esos enlaces son una alternativa.

- RN12 (nuevo): La verificación de identidad por código se hace solo por
  correo. El SMS queda como mejora futura (requiere proveedor externo
  con costo) y WhatsApp se descarta (requiere la API empresarial de
  WhatsApp).

- RN13 (nuevo): Una emprendedora histórica define su contraseña al
  reclamar su ficha, después de confirmar el código del correo.

- RN14 (nuevo): La Fundación validará que la emprendedora sea quien dice
  ser, viva donde dice y haga lo que dice. La validación es humana y la
  hacen otras emprendedoras, para fortalecer la comunidad. Una ficha
  nueva no se publica hasta ser validada; después, sus ediciones se
  publican sin aprobación.

- RN15 (nuevo): La emprendedora puede darse de baja por sí misma, con la
  misma facilidad con que se inscribió, en línea con la Ley 21.719. Se
  eliminan sus datos personales, fotos y ficha pública.

- RN16 (nuevo): La clienta puede ver el directorio y el catálogo sin
  registrarse. Para escribir a una emprendedora debe hacer un registro
  simple, para que la emprendedora sepa con quién habla y cuidar la
  integridad del contacto.

- RN17 (nuevo): Errores frecuentes del formulario actual: Instagram mal
  escrito y nombres con mayúsculas o minúsculas incorrectas. Instagram
  acepta "usuario", "@usuario" o el enlace completo, y la app arma el
  enlace; un botón permite abrirlo para comprobarlo. Nombre y apellido
  se guardan con mayúscula inicial en cada palabra.

- **RN18 (nuevo V5):** El marketplace no procesa pagos, envíos ni
  comisiones. El precio es referencial y la compra o el servicio se
  coordinan por el chat interno.

- **RN19 (nuevo V5):** Una publicación aparece en la vitrina solo si (a)
  su estado es "Publicada" o "Agotada" (esta última con la etiqueta
  "Agotado"), (b) la ficha de la socia está "Publicada" (RN14) y (c) la
  socia no está en Modo Pausa. Borradores y publicaciones pausadas nunca
  se muestran.

- **RN20 (nuevo V5):** Cada publicación pertenece a una sola socia. Solo
  ella puede editarla, cambiar su estado o eliminarla.

- **RN21 (nuevo V5):** Validaciones de la publicación: título de 3 a 60
  caracteres, descripción de 10 a 500, precio entero en pesos chilenos
  entre \$500 y \$10.000.000 o "A convenir", al menos 1 foto y como
  máximo 5 (Anexo B).

- **RN22 (nuevo V5):** Tipos de publicación, tomados del caso: Producto
  (prendas y accesorios), Servicio (reparación y arreglos), Taller y
  Arriendo. Igual que los rubros, se guardan como catálogo configurable.

- **RN23 (nuevo V5):** Al eliminar una publicación se borran sus fotos
  del dispositivo. Las conversaciones conservan el título como texto y
  muestran "Publicación eliminada".

- **RN24 (nuevo V5):** El Modo Pausa (RF01.3) oculta todas las
  publicaciones de la socia sin cambiar su estado. Darse de baja (RN15)
  elimina también todas sus publicaciones y fotos.

# **6. Estado de las decisiones**

## **Confirmadas con la Fundación**

- Chat interno de texto simple en lugar de redirección a WhatsApp.

- App solo para emprendedoras; sin panel de administración.

- Desarrollo con dataset ficticio; sin migración real por ahora.

- Incluir RUT en el formulario de inscripción.

- El consentimiento del sitio web es suficiente; no hace falta uno nuevo
  en la app.

- Validación de fichas nuevas entre emprendedoras antes de publicarlas
  (reunión 25-09-2026).

- La emprendedora puede darse de baja por sí misma.

- La app usa los colores y tipografías de la Fundación, con estilo
  minimalista.

- Los rubros pueden crecer; no se limitan a las 7 categorías actuales.

- Sección de novedades dentro de la app para comunicarse con las
  emprendedoras.

- La clienta ve el catálogo sin registrarse y se registra solo para
  escribir.

- Mensaje inicial sugerido en el chat (idea bien recibida por la
  Fundación).

- Se mantiene el RUT en el formulario (la Fundación lo confirmó al
  cierre de la reunión).

## **Decididas por el equipo**

- Autenticación con correo y contraseña (RNF11).

- Verificación por código solo al correo (RN12).

- Aviso "Falta completar" solo para la emprendedora (RN10).

- Teléfono e Instagram públicos; sincronización con el sitio web en
  etapa posterior (RN11, RF05.5 y RF05.6).

- Estilo Marketplace con vitrina pública y apartado "Mi tienda" para las
  socias (V5).

- Sin pagos en línea: la compra se coordina por chat (RN18).

- Límites de 5 fotos por publicación y 20 publicaciones por socia
  (RN06).

- Registro simple de la clienta dentro de la app para la demostración
  (RF06.6).

## **Pendientes de confirmar con la Fundación**

- Teléfono e Instagram públicos (RN11): en la reunión la Fundación
  reiteró que busca independizarse de Instagram y WhatsApp y que el
  contacto ocurra en la app. Confirmar si se mantienen como alternativa.

- Códigos exactos de los cuatro colores y licencia de uso de la
  tipografía Monument en la app (RNF12).

- Forma en que se entregará el dataset histórico para trabajar (la
  Fundación aún no lo sabe).

- Si la Fundación quiere mostrar precios en la vitrina o prefiere solo
  "A convenir" (RN21).

- Si la vitrina de la app reemplazará al Directorio del sitio web o
  convivirán (RF05.5, RF05.6).

# **7. Fuera de alcance**

- Panel web de administración y moderación.

- Pagos en línea, carrito de compras, despachos, comisiones y boletas
  (RN18).

- Valoraciones y reseñas de las socias, favoritos de la clienta (RF06.7)
  y bandeja de mensajes de la clienta para seguir la conversación (su
  respuesta llega por el sitio web en una etapa posterior).

- Rediseño del sitio público en WordPress y su interfaz de envío de
  mensajes (se simula).

- Migración real del dataset de 982 registros.

- Chat con imágenes, archivos, confirmación de lectura o indicador
  "escribiendo…".

- Código de verificación por SMS o WhatsApp.

- Métricas y reportería para la Administradora.

- Cola visual de sincronización (RF03.3), plantillas de respuesta
  (RF04.4), autocompletado del Código de Embajadora (RF01.8) y endpoint
  público de lectura (RF05.5).

- Flujo de validación entre emprendedoras (RF01.12; se prepara el estado
  de la ficha), registro de clientas en el sitio web (en la app se
  resuelve con RF06.6) y video en el catálogo (RF02.4).

# **Anexo A. Formulario de inscripción: campos y validaciones**

Basado en el formulario real de la Fundación (7 páginas en Google
Forms), más el RUT y la contraseña. En la app se presenta en 6 pasos:
(1) datos personales, (2) contraseña, (3) sobre ti y ubicación, (4)
emprendimiento, (5) trayectoria y contacto, (6) autorización. Verde =
público, amarillo = estadístico, rojo = privado o interno. Las
validaciones y largos máximos son propuestas del equipo.

| **Campo**                                    | **Oblig.**  | **Visibilidad** | **Tipo y validación (se ejecuta en el ViewModel)**                                                                                                                                                                                                                                    |
|----------------------------------------------|-------------|-----------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Nombre y apellido**                        | Sí          | Público         | Texto. Mínimo 2 palabras, cada una de al menos 2 letras. Solo letras, tildes, espacios, apóstrofe y guión. Máximo 60 caracteres. Se guarda con mayúscula inicial en cada palabra (RN17).                                                                                              |
| **RUT**                                      | Sí          | Privado         | Formato 12.345.678-5 o 12345678-5. Cuerpo de 7 u 8 dígitos y dígito verificador (0-9 o K) correcto por módulo 11. Se guarda sin puntos. Único por cuenta: si ya existe, muestra "Este RUT ya está registrado en otra cuenta". Aviso: "Se usa solo para identificarte. No se publica." |
| **Correo electrónico**                       | Sí          | Privado         | Formato de correo válido. Único por cuenta. Es el usuario para iniciar sesión.                                                                                                                                                                                                        |
| **Contraseña**                               | Sí          | Privado         | Mínimo 8 caracteres. Se pide dos veces y deben coincidir. Ícono de ojo para verla. Se guarda como hash, nunca en texto plano.                                                                                                                                                         |
| **Rango etario**                             | Sí          | Estadístico     | Lista: 18-24, 25-34, 35-44, 45-54, 55-64, 65+. El rango mínimo del formulario actual es 18-24.                                                                                                                                                                                        |
| **Género**                                   | Sí          | Estadístico     | Lista: Femenino, Masculino, No binario, Prefiero no responder.                                                                                                                                                                                                                        |
| **Teléfono (WhatsApp)**                      | Sí          | Público         | 9 dígitos que parten con 9, con +56 automático. Rechaza largos incorrectos y letras.                                                                                                                                                                                                  |
| **Código de Embajadora**                     | No          | Interno         | Alfanumérico, sin espacios, máximo 20 caracteres. En blanco si no aplica.                                                                                                                                                                                                             |
| **Región**                                   | Sí          | Público         | Lista de las 16 regiones de Chile.                                                                                                                                                                                                                                                    |
| **Comuna**                                   | Sí          | Público         | Lista dependiente de la región. Se reinicia si se cambia la región (RN01).                                                                                                                                                                                                            |
| **Nombre del emprendimiento**                | Sí          | Público         | Texto de 2 a 60 caracteres. Si no tiene, repite su propio nombre.                                                                                                                                                                                                                     |
| **¿Formalizado ante el SII?**                | Sí          | Estadístico     | Lista: Sí, No, Estoy en proceso de formalización, Prefiero no responder.                                                                                                                                                                                                              |
| **Producto o servicio principal**            | Sí          | Público         | Selección múltiple del catálogo de rubros (hoy 7 categorías, RN02): mínimo 1, máximo 2. Bloquea una tercera selección.                                                                                                                                                                |
| **Tiempo dedicado a esto**                   | Sí          | Interno         | Lista: menos de 1 año, 1-3, 3-5, más de 5 años.                                                                                                                                                                                                                                       |
| **La actividad representa...**               | Sí          | Estadístico     | Lista: principal fuente de ingresos, fuente complementaria, proyecto personal sin ingresos significativos.                                                                                                                                                                            |
| **¿Aspiras a que sea tu principal ingreso?** | Condicional | Estadístico     | Lista: Sí, es mi objetivo; No, prefiero mantenerla complementaria; Aún no lo he decidido. Solo aparece si en el campo anterior no marcó "principal fuente de ingresos" (RF01.6).                                                                                                      |
| **Instagram**                                | No          | Público         | Opcional. Acepta "usuario", "@usuario" o el enlace completo; la app arma el enlace https://www.instagram.com/usuario/ (usuario de letras, números, punto y guión bajo, hasta 30). Si se llena, se valida y se ofrece un botón para abrirlo y comprobarlo (RN17).                      |
| **¿Cómo nos conociste?**                     | Sí          | Interno         | Lista de 15 opciones, incluida "Otro", que exige escribir el detalle.                                                                                                                                                                                                                 |
| **Quién te invitó**                          | Condicional | Interno         | Solo aparece y es obligatorio si respondió que la recomendó otra emprendedora. Misma regla que el primer campo.                                                                                                                                                                       |
| **Autorización de datos**                    | Sí          | Interno         | Casilla obligatoria con el texto actual de la Fundación. Sin aceptar no se crea la cuenta. Se guarda fecha y hora de aceptación.                                                                                                                                                      |

# **Anexo B. Formulario de publicación y de consulta (nuevo V5)**

B.1 Publicación (apartado "Mi tienda", RF07.3). Verde = público, rojo =
interno. Las validaciones se ejecutan en la capa de dominio y el
ViewModel solo expone el resultado a la interfaz (RNF08).

| **Campo**               | **Oblig.**             | **Visibilidad** | **Tipo y validación (se ejecuta en la capa de dominio)**                                                                                                     |
|-------------------------|------------------------|-----------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Fotos**               | Sí                     | Público         | De 1 a 5 (RN06), desde cámara o galería. Se comprimen a 1280 px en el lado mayor y JPEG 80 % antes de guardarse (RF02.3). La primera es la portada.          |
| **Tipo**                | Sí                     | Público         | Lista: Producto, Servicio, Taller, Arriendo (RN22).                                                                                                          |
| **Título**              | Sí                     | Público         | Texto de 3 a 60 caracteres, no solo números. Se guarda sin espacios dobles y con mayúscula inicial.                                                          |
| **Descripción**         | Sí                     | Público         | Texto de 10 a 500 caracteres con contador visible.                                                                                                           |
| **Rubro**               | Sí                     | Público         | Uno del catálogo de rubros (RN02); los rubros de la socia aparecen primero.                                                                                  |
| **Precio**              | Sí, salvo "A convenir" | Público         | Entero en pesos chilenos entre \$500 y \$10.000.000, sin decimales ni letras; se muestra con separador de miles. La casilla "A convenir" desactiva el campo. |
| **Disponibilidad**      | Sí                     | Público         | Lista: Lista para entregar, Hecha a pedido.                                                                                                                  |
| **Días de elaboración** | Condicional            | Público         | Solo aparece y es obligatorio si es "Hecha a pedido": entero de 1 a 60.                                                                                      |
| **Estado**              | Sistema                | Interno         | Borrador, Publicada, Pausada o Agotada (RF07.6, RN19).                                                                                                       |
| **Vistas y consultas**  | Sistema                | Interno         | Contadores locales (RF06.8).                                                                                                                                 |

B.2 Consulta de la clienta (vitrina, RF06.6).

| **Campo**              | **Oblig.** | **Visibilidad** | **Tipo y validación (se ejecuta en la capa de dominio)**                                                              |
|------------------------|------------|-----------------|-----------------------------------------------------------------------------------------------------------------------|
| **Nombre**             | Sí         | Interno         | Texto de 2 a 60 caracteres, solo letras, tildes, espacios, apóstrofe y guión. Se guarda con mayúscula inicial (RN17). |
| **Correo electrónico** | Sí         | Interno         | Formato de correo válido. No se muestra en la vitrina.                                                                |
| **Mensaje**            | Sí         | Interno         | De 1 a 500 caracteres. Viene prellenado con "Hola, quiero saber más sobre: \<título\>" (RF04.6).                      |

# **Anexo C. Mapa de navegación (nuevo V5)**

- **Sin sesión:** Bienvenida → Explorar (vitrina) → Detalle de
  publicación → Tienda de la socia o Consultar (registro simple de la
  clienta).

- **Acceso de socias:** Ingresar → Iniciar sesión, Inscribirse (6 pasos)
  o Recuperar contraseña.

- **Con sesión (barra inferior):** Explorar · Mi tienda · Mensajes ·
  Perfil.

- **Mi tienda:** Resumen y listado → Nueva o editar publicación → Vista
  previa "Así te ven".

- **Mensajes:** Lista de conversaciones con no leídos → Conversación.

- **Perfil:** Editar perfil, Modo Pausa, Novedades, Cerrar sesión y
  Darse de baja.
