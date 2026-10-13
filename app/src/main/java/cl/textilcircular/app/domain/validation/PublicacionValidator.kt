package cl.textilcircular.app.domain.validation

import cl.textilcircular.app.domain.model.Disponibilidad
import cl.textilcircular.app.domain.model.TipoPublicacion
import cl.textilcircular.app.domain.rules.Limites

enum class CampoPublicacion { FOTOS, TIPO, TITULO, DESCRIPCION, RUBRO, PRECIO, DISPONIBILIDAD, DIAS }

/** Datos crudos del formulario de publicación (Anexo B.1), tal como los escribe la socia. */
data class BorradorPublicacion(
    val fotos: List<String> = emptyList(),
    val tipo: TipoPublicacion? = null,
    val titulo: String = "",
    val descripcion: String = "",
    val rubroId: Long? = null,
    val precioTexto: String = "",
    val aConvenir: Boolean = false,
    val disponibilidad: Disponibilidad? = null,
    val diasTexto: String = ""
)

/** Validación del formulario de publicación (RF07.3, RF07.4, RN21). */
object PublicacionValidator {

    fun titulo(valor: String): String? {
        val v = valor.trim()
        return when {
            v.isEmpty() -> "Escribe un título"
            v.length < 3 -> "Debe tener al menos 3 caracteres"
            v.length > 60 -> "Máximo 60 caracteres"
            v.all { it.isDigit() || it.isWhitespace() } -> "El título no puede ser solo números"
            else -> null
        }
    }

    fun descripcion(valor: String): String? {
        val v = valor.trim()
        return when {
            v.isEmpty() -> "Describe tu publicación"
            v.length < 10 -> "Escribe al menos 10 caracteres"
            v.length > 500 -> "Máximo 500 caracteres"
            else -> null
        }
    }

    fun fotos(fotos: List<String>): String? = when {
        fotos.isEmpty() -> "Agrega al menos 1 foto"
        fotos.size > Limites.MAX_FOTOS_POR_PUBLICACION -> "Máximo ${Limites.MAX_FOTOS_POR_PUBLICACION} fotos"
        else -> null
    }

    /** Precio entero en pesos chilenos; acepta "12990" o "12.990". */
    fun precio(texto: String, aConvenir: Boolean): String? {
        if (aConvenir) return null
        val limpio = texto.trim().removePrefix("$").replace(".", "").trim()
        if (limpio.isEmpty()) return "Escribe el precio o marca \"A convenir\""
        if (!limpio.all { it.isDigit() }) return "Usa solo números, sin decimales"
        val valor = limpio.toLongOrNull() ?: return "Precio demasiado alto"
        return when {
            valor < Limites.PRECIO_MINIMO -> "El precio mínimo es ${Precio.formatearPesos(Limites.PRECIO_MINIMO)}"
            valor > Limites.PRECIO_MAXIMO -> "El precio máximo es ${Precio.formatearPesos(Limites.PRECIO_MAXIMO)}"
            else -> null
        }
    }

    fun parsePrecio(texto: String, aConvenir: Boolean): Int? =
        if (aConvenir) null else texto.trim().removePrefix("$").replace(".", "").trim().toIntOrNull()

    fun dias(texto: String, disponibilidad: Disponibilidad?): String? {
        if (disponibilidad != Disponibilidad.A_PEDIDO) return null
        val n = texto.trim().toIntOrNull() ?: return "Indica cuántos días demoras (1 a 60)"
        return if (n in 1..60) null else "Debe ser entre 1 y 60 días"
    }

    /** Publicar exige todas las validaciones (RF07.4). */
    fun validarParaPublicar(b: BorradorPublicacion): Map<CampoPublicacion, String> = buildMap {
        fotos(b.fotos)?.let { put(CampoPublicacion.FOTOS, it) }
        if (b.tipo == null) put(CampoPublicacion.TIPO, "Elige el tipo de publicación")
        titulo(b.titulo)?.let { put(CampoPublicacion.TITULO, it) }
        descripcion(b.descripcion)?.let { put(CampoPublicacion.DESCRIPCION, it) }
        if (b.rubroId == null) put(CampoPublicacion.RUBRO, "Elige un rubro")
        precio(b.precioTexto, b.aConvenir)?.let { put(CampoPublicacion.PRECIO, it) }
        if (b.disponibilidad == null) put(CampoPublicacion.DISPONIBILIDAD, "Elige la disponibilidad")
        dias(b.diasTexto, b.disponibilidad)?.let { put(CampoPublicacion.DIAS, it) }
    }

    /** Guardar como borrador solo exige el título; los demás datos, si se escribieron, deben ser válidos. */
    fun validarBorrador(b: BorradorPublicacion): Map<CampoPublicacion, String> = buildMap {
        titulo(b.titulo)?.let { put(CampoPublicacion.TITULO, it) }
        if (b.fotos.size > Limites.MAX_FOTOS_POR_PUBLICACION) put(CampoPublicacion.FOTOS, fotos(b.fotos)!!)
        if (b.descripcion.isNotBlank()) descripcion(b.descripcion)?.let { put(CampoPublicacion.DESCRIPCION, it) }
        if (b.precioTexto.isNotBlank()) precio(b.precioTexto, b.aConvenir)?.let { put(CampoPublicacion.PRECIO, it) }
        if (b.diasTexto.isNotBlank()) dias(b.diasTexto, b.disponibilidad)?.let { put(CampoPublicacion.DIAS, it) }
    }
}

object Precio {
    /** 12990 -> "$12.990"; null -> "A convenir". */
    fun formatear(valor: Int?): String = if (valor == null) "A convenir" else formatearPesos(valor.toLong())

    fun formatearPesos(valor: Long): String {
        val digitos = valor.toString()
        val conPuntos = digitos.reversed().chunked(3).joinToString(".").reversed()
        return "$$conPuntos"
    }
}
