package cl.textilcircular.app.domain.validation

import cl.textilcircular.app.domain.catalog.ChileGeo

/**
 * Reglas de validación centralizadas y sin dependencias de Android (RNF08).
 * Cada función devuelve el mensaje de error en lenguaje simple, o null si el dato es válido.
 */
object Validators {

    private val LETRAS = Regex("^[A-Za-zÁÉÍÓÚÜÑáéíóúüñ' -]+$")
    private val EMAIL = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*\\.[A-Za-z]{2,}$")
    private val ALFANUMERICO = Regex("^[A-Za-z0-9]+$")
    private val SOLO_DIGITOS = Regex("^[0-9]+$")

    fun nombreCompleto(valor: String): String? {
        val v = valor.trim()
        if (v.isEmpty()) return "Escribe tu nombre y apellido"
        if (v.length > 60) return "Máximo 60 caracteres"
        if (!LETRAS.matches(v)) return "Usa solo letras, sin números ni símbolos"
        val palabras = v.split(Regex("\\s+"))
        if (palabras.size < 2) return "Escribe nombre y apellido"
        if (palabras.any { p -> p.count { it.isLetter() } < 2 }) return "Cada palabra debe tener al menos 2 letras"
        return null
    }

    /** RN17: mayúscula inicial en cada palabra. */
    fun normalizarNombre(valor: String): String =
        valor.trim().split(Regex("\\s+")).joinToString(" ") { palabra ->
            palabra.lowercase().replaceFirstChar { it.titlecase() }
        }

    fun rut(valor: String): String? {
        if (valor.isBlank()) return "Escribe tu RUT"
        return if (Rut.esValido(valor)) null else "RUT no válido. Ejemplo: 12.345.678-5"
    }

    fun email(valor: String): String? {
        val v = valor.trim()
        if (v.isEmpty()) return "Escribe tu correo"
        if (v.length > 100) return "Máximo 100 caracteres"
        return if (EMAIL.matches(v)) null else "Correo no válido. Ejemplo: nombre@correo.cl"
    }

    /** RNF11: mínimo 8 caracteres, sin exigir símbolos ni mayúsculas. */
    fun password(valor: String): String? = when {
        valor.isEmpty() -> "Crea una contraseña"
        valor.length < 8 -> "Debe tener al menos 8 caracteres"
        valor.length > 64 -> "Máximo 64 caracteres"
        else -> null
    }

    fun confirmarPassword(password: String, confirmacion: String): String? = when {
        confirmacion.isEmpty() -> "Repite la contraseña"
        password != confirmacion -> "Las contraseñas no coinciden"
        else -> null
    }

    /** Teléfono móvil chileno: 9 dígitos que parten con 9; el +56 se agrega solo. */
    fun telefono(valor: String): String? {
        val digitos = normalizarTelefono(valor)
        if (digitos.isEmpty()) return "Escribe tu número de WhatsApp"
        if (!SOLO_DIGITOS.matches(digitos)) return "Usa solo números"
        if (digitos.length != 9) return "Debe tener 9 dígitos"
        if (!digitos.startsWith("9")) return "Debe comenzar con 9"
        return null
    }

    fun normalizarTelefono(valor: String): String =
        valor.replace(" ", "").replace("-", "").removePrefix("+56").let {
            if (it.length == 11 && it.startsWith("56")) it.removePrefix("56") else it
        }

    fun codigoEmbajadora(valor: String): String? {
        val v = valor.trim()
        if (v.isEmpty()) return null
        if (v.length > 20) return "Máximo 20 caracteres"
        return if (ALFANUMERICO.matches(v)) null else "Usa solo letras y números, sin espacios"
    }

    fun seleccion(valor: String?, mensaje: String = "Elige una opción"): String? =
        if (valor.isNullOrBlank()) mensaje else null

    fun region(valor: String?): String? =
        if (valor != null && valor in ChileGeo.regiones) null else "Elige tu región"

    fun comuna(region: String?, comuna: String?): String? = when {
        comuna.isNullOrBlank() -> "Elige tu comuna"
        !ChileGeo.esComunaDeRegion(region, comuna) -> "La comuna no pertenece a la región"
        else -> null
    }

    fun nombreEmprendimiento(valor: String): String? {
        val v = valor.trim()
        return when {
            v.isEmpty() -> "Escribe el nombre de tu emprendimiento (o tu nombre)"
            v.length < 2 -> "Debe tener al menos 2 caracteres"
            v.length > 60 -> "Máximo 60 caracteres"
            else -> null
        }
    }

    /** RN02: mínimo 1 y máximo 2 rubros. */
    fun rubros(seleccionados: Collection<Long>): String? = when {
        seleccionados.isEmpty() -> "Elige al menos 1 rubro"
        seleccionados.size > 2 -> "Puedes elegir hasta 2 rubros"
        else -> null
    }

    /** RN17: acepta "usuario", "@usuario" o el enlace completo. Opcional. */
    fun instagram(valor: String): String? {
        if (valor.isBlank()) return null
        return if (Instagram.extraerUsuario(valor) != null) null
        else "Instagram no válido. Escribe tu usuario, por ejemplo @mitienda"
    }

    fun textoOtro(valor: String): String? {
        val v = valor.trim()
        return when {
            v.isEmpty() -> "Cuéntanos cómo nos conociste"
            v.length < 3 -> "Escribe al menos 3 caracteres"
            v.length > 100 -> "Máximo 100 caracteres"
            else -> null
        }
    }

    fun consentimiento(aceptado: Boolean): String? =
        if (aceptado) null else "Debes aceptar la autorización para crear tu cuenta"

    fun codigoVerificacion(valor: String): String? = when {
        valor.isBlank() -> "Escribe el código que te enviamos"
        valor.length != 6 || !SOLO_DIGITOS.matches(valor) -> "El código tiene 6 números"
        else -> null
    }

    // ------------------------------------------------------------------ Consulta de la clienta (Anexo B.2)

    fun nombreClienta(valor: String): String? {
        val v = valor.trim()
        return when {
            v.isEmpty() -> "Escribe tu nombre"
            v.length < 2 -> "Debe tener al menos 2 letras"
            v.length > 60 -> "Máximo 60 caracteres"
            !LETRAS.matches(v) -> "Usa solo letras"
            else -> null
        }
    }

    fun mensaje(valor: String): String? {
        val v = valor.trim()
        return when {
            v.isEmpty() -> "Escribe un mensaje"
            v.length > 500 -> "Máximo 500 caracteres"
            else -> null
        }
    }
}

/** Utilidades de Instagram (RN17). */
object Instagram {
    private val USUARIO = Regex("^[A-Za-z0-9._]{1,30}$")
    private val ENLACE = Regex("^(https?://)?(www\\.)?instagram\\.com/([^/?#]+)/?([?#].*)?$", RegexOption.IGNORE_CASE)

    fun extraerUsuario(valor: String): String? {
        val v = valor.trim()
        val usuario = ENLACE.matchEntire(v)?.groupValues?.get(3) ?: v.removePrefix("@")
        return usuario.takeIf { USUARIO.matches(it) && !it.startsWith(".") && !it.endsWith(".") }
    }

    fun enlace(valor: String?): String? =
        valor?.let { extraerUsuario(it) }?.let { "https://www.instagram.com/$it/" }
}

/** RUT chileno con dígito verificador módulo 11 (RN08). */
object Rut {
    private val FORMATO = Regex("^(\\d{1,2}\\.?\\d{3}\\.?\\d{3})-?([\\dkK])$")

    fun esValido(valor: String): Boolean {
        val m = FORMATO.matchEntire(valor.trim()) ?: return false
        val cuerpo = m.groupValues[1].replace(".", "")
        if (cuerpo.length !in 7..8) return false
        return digitoVerificador(cuerpo) == m.groupValues[2].uppercase()
    }

    fun digitoVerificador(cuerpo: String): String {
        var suma = 0
        var factor = 2
        for (c in cuerpo.reversed()) {
            suma += (c - '0') * factor
            factor = if (factor == 7) 2 else factor + 1
        }
        return when (val dv = 11 - suma % 11) {
            11 -> "0"
            10 -> "K"
            else -> dv.toString()
        }
    }

    /** Se guarda sin puntos y con guión: 12345678-5. */
    fun normalizar(valor: String): String {
        val limpio = valor.trim().replace(".", "").replace("-", "").uppercase()
        return limpio.dropLast(1) + "-" + limpio.last()
    }
}
