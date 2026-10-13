package cl.textilcircular.app.domain.model

/** Estado de una publicación del marketplace (RF07.6, RN19). */
enum class EstadoPublicacion(val etiqueta: String) {
    BORRADOR("Borrador"),
    PUBLICADA("Publicada"),
    PAUSADA("Pausada"),
    AGOTADA("Agotada");

    /** Las publicaciones que pueden aparecer en la vitrina (RN19 a). */
    val visibleEnVitrina: Boolean get() = this == PUBLICADA || this == AGOTADA
}

/** Tipos de publicación tomados del caso (RN22). */
enum class TipoPublicacion(val etiqueta: String) {
    PRODUCTO("Producto"),
    SERVICIO("Servicio"),
    TALLER("Taller"),
    ARRIENDO("Arriendo")
}

enum class Disponibilidad(val etiqueta: String) {
    LISTA("Lista para entregar"),
    A_PEDIDO("Hecha a pedido")
}

/** Estado de la ficha de la socia (RF01.12, RN14). */
enum class EstadoFicha(val etiqueta: String) {
    PENDIENTE("Pendiente de validación"),
    PUBLICADA("Publicada")
}

/** Campo preparado para la sincronización futura (sección 1.1 del ERS). */
enum class EstadoSync { PENDIENTE, SINCRONIZADO }

enum class OrdenVitrina(val etiqueta: String) {
    RECIENTES("Más recientes"),
    PRECIO_MENOR("Menor precio"),
    PRECIO_MAYOR("Mayor precio")
}

/** Opciones de las listas cerradas del formulario de inscripción (Anexo A). */
object OpcionesFormulario {
    val rangosEtarios = listOf("18-24", "25-34", "35-44", "45-54", "55-64", "65+")
    val generos = listOf("Femenino", "Masculino", "No binario", "Prefiero no responder")
    val formalizacion = listOf("Sí", "No", "Estoy en proceso de formalización", "Prefiero no responder")
    val tiempoDedicado = listOf("Menos de 1 año", "1-3 años", "3-5 años", "Más de 5 años")

    const val INGRESO_PRINCIPAL = "Principal fuente de ingresos"
    val ingreso = listOf(
        INGRESO_PRINCIPAL,
        "Fuente complementaria",
        "Proyecto personal sin ingresos significativos"
    )
    val aspiracion = listOf(
        "Sí, es mi objetivo",
        "No, prefiero mantenerla complementaria",
        "Aún no lo he decidido"
    )

    const val RECOMENDACION = "Me recomendó otra emprendedora"
    const val OTRO = "Otro"
    val comoNosConociste = listOf(
        "Instagram", "Facebook", "TikTok", "WhatsApp", "Sitio web de la Fundación",
        "Google", RECOMENDACION, "Feria o evento", "Taller de la Fundación",
        "Municipalidad", "Prensa, radio o TV", "Universidad o instituto",
        "Organización social", "Correo electrónico", OTRO
    )
}
