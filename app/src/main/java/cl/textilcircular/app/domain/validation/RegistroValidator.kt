package cl.textilcircular.app.domain.validation

import cl.textilcircular.app.domain.model.OpcionesFormulario

enum class CampoRegistro {
    NOMBRE, RUT, EMAIL,
    PASSWORD, CONFIRMACION,
    RANGO_ETARIO, GENERO, TELEFONO, CODIGO_EMBAJADORA, REGION, COMUNA,
    NOMBRE_EMPRENDIMIENTO, FORMALIZADO, RUBROS,
    TIEMPO, INGRESO, ASPIRACION, INSTAGRAM, COMO_CONOCISTE, COMO_CONOCISTE_OTRO, QUIEN_INVITO,
    CONSENTIMIENTO
}

/** Datos del formulario de inscripción en 6 pasos (RF01.1, RF01.6, Anexo A). */
data class RegistroForm(
    // Paso 1: datos personales
    val nombre: String = "",
    val rut: String = "",
    val email: String = "",
    // Paso 2: contraseña
    val password: String = "",
    val confirmacion: String = "",
    // Paso 3: sobre ti y ubicación
    val rangoEtario: String? = null,
    val genero: String? = null,
    val telefono: String = "",
    val codigoEmbajadora: String = "",
    val region: String? = null,
    val comuna: String? = null,
    // Paso 4: emprendimiento
    val nombreEmprendimiento: String = "",
    val formalizado: String? = null,
    val rubros: Set<Long> = emptySet(),
    // Paso 5: trayectoria y contacto
    val tiempo: String? = null,
    val ingreso: String? = null,
    val aspiracion: String? = null,
    val instagram: String = "",
    val comoConociste: String? = null,
    val comoConocisteOtro: String = "",
    val quienInvito: String = "",
    // Paso 6: autorización
    val consentimiento: Boolean = false
) {
    /** RF01.6: solo se pregunta si no marcó que ya es su principal ingreso. */
    val muestraAspiracion: Boolean get() = ingreso != null && ingreso != OpcionesFormulario.INGRESO_PRINCIPAL
    val muestraQuienInvito: Boolean get() = comoConociste == OpcionesFormulario.RECOMENDACION
    val muestraOtro: Boolean get() = comoConociste == OpcionesFormulario.OTRO
}

object RegistroValidator {

    const val TOTAL_PASOS = 6

    val titulosPasos = listOf(
        "Tus datos", "Tu contraseña", "Sobre ti", "Tu emprendimiento", "Tu trayectoria", "Autorización"
    )

    fun validarPaso(paso: Int, f: RegistroForm): Map<CampoRegistro, String> = buildMap {
        fun check(campo: CampoRegistro, error: String?) { if (error != null) put(campo, error) }
        when (paso) {
            0 -> {
                check(CampoRegistro.NOMBRE, Validators.nombreCompleto(f.nombre))
                check(CampoRegistro.RUT, Validators.rut(f.rut))
                check(CampoRegistro.EMAIL, Validators.email(f.email))
            }
            1 -> {
                check(CampoRegistro.PASSWORD, Validators.password(f.password))
                check(CampoRegistro.CONFIRMACION, Validators.confirmarPassword(f.password, f.confirmacion))
            }
            2 -> {
                check(CampoRegistro.RANGO_ETARIO, Validators.seleccion(f.rangoEtario, "Elige tu rango de edad"))
                check(CampoRegistro.GENERO, Validators.seleccion(f.genero))
                check(CampoRegistro.TELEFONO, Validators.telefono(f.telefono))
                check(CampoRegistro.CODIGO_EMBAJADORA, Validators.codigoEmbajadora(f.codigoEmbajadora))
                check(CampoRegistro.REGION, Validators.region(f.region))
                check(CampoRegistro.COMUNA, Validators.comuna(f.region, f.comuna))
            }
            3 -> {
                check(CampoRegistro.NOMBRE_EMPRENDIMIENTO, Validators.nombreEmprendimiento(f.nombreEmprendimiento))
                check(CampoRegistro.FORMALIZADO, Validators.seleccion(f.formalizado))
                check(CampoRegistro.RUBROS, Validators.rubros(f.rubros))
            }
            4 -> {
                check(CampoRegistro.TIEMPO, Validators.seleccion(f.tiempo))
                check(CampoRegistro.INGRESO, Validators.seleccion(f.ingreso))
                if (f.muestraAspiracion) check(CampoRegistro.ASPIRACION, Validators.seleccion(f.aspiracion))
                check(CampoRegistro.INSTAGRAM, Validators.instagram(f.instagram))
                check(CampoRegistro.COMO_CONOCISTE, Validators.seleccion(f.comoConociste))
                if (f.muestraOtro) check(CampoRegistro.COMO_CONOCISTE_OTRO, Validators.textoOtro(f.comoConocisteOtro))
                if (f.muestraQuienInvito) check(CampoRegistro.QUIEN_INVITO, Validators.nombreCompleto(f.quienInvito))
            }
            5 -> check(CampoRegistro.CONSENTIMIENTO, Validators.consentimiento(f.consentimiento))
        }
    }

    fun validarTodo(f: RegistroForm): Map<CampoRegistro, String> =
        (0 until TOTAL_PASOS).fold(emptyMap()) { acc, paso -> acc + validarPaso(paso, f) }

    /** Paso donde vive cada campo, para volver al primer paso con errores. */
    fun pasoDe(campo: CampoRegistro): Int = when (campo) {
        CampoRegistro.NOMBRE, CampoRegistro.RUT, CampoRegistro.EMAIL -> 0
        CampoRegistro.PASSWORD, CampoRegistro.CONFIRMACION -> 1
        CampoRegistro.RANGO_ETARIO, CampoRegistro.GENERO, CampoRegistro.TELEFONO,
        CampoRegistro.CODIGO_EMBAJADORA, CampoRegistro.REGION, CampoRegistro.COMUNA -> 2
        CampoRegistro.NOMBRE_EMPRENDIMIENTO, CampoRegistro.FORMALIZADO, CampoRegistro.RUBROS -> 3
        CampoRegistro.CONSENTIMIENTO -> 5
        else -> 4
    }
}
