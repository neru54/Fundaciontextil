package cl.textilcircular.app.data.seed

import androidx.room.withTransaction
import cl.textilcircular.app.data.local.AppDatabase
import cl.textilcircular.app.data.local.ConversacionEntity
import cl.textilcircular.app.data.local.FotoEntity
import cl.textilcircular.app.data.local.MensajeEntity
import cl.textilcircular.app.data.local.NovedadEntity
import cl.textilcircular.app.data.local.PublicacionEntity
import cl.textilcircular.app.data.local.RubroEntity
import cl.textilcircular.app.data.local.SociaEntity
import cl.textilcircular.app.data.security.PasswordHasher
import cl.textilcircular.app.domain.model.Disponibilidad
import cl.textilcircular.app.domain.model.EstadoFicha
import cl.textilcircular.app.domain.model.EstadoPublicacion
import cl.textilcircular.app.domain.model.EstadoSync
import cl.textilcircular.app.domain.model.TipoPublicacion
import cl.textilcircular.app.domain.validation.Rut

/**
 * Dataset ficticio para la demostración (RF05.3, RNF05). Ningún dato corresponde a personas reales;
 * los RUT tienen dígito verificador válido pero son inventados.
 */
class DemoSeeder(private val db: AppDatabase) {

    suspend fun sembrarSiVacia() {
        if (db.rubroDao().contar() > 0) return
        db.withTransaction { sembrar() }
    }

    private suspend fun sembrar() {
        val rubros = db.rubroDao().insertar(RUBROS.map { RubroEntity(nombre = it) })
        val r = RUBROS.zip(rubros).toMap()
        val hash = PasswordHasher.hash(DEMO_PASSWORD)
        val ahora = System.currentTimeMillis()
        val dia = 24 * 60 * 60 * 1000L

        fun socia(
            nombre: String, cuerpoRut: String, email: String, region: String, comuna: String,
            tienda: String, rubrosSocia: List<String>, instagram: String?, telefono: String,
            ficha: EstadoFicha = EstadoFicha.PUBLICADA
        ) = SociaEntity(
            nombre = nombre, rut = "$cuerpoRut-${Rut.digitoVerificador(cuerpoRut)}", email = email,
            passwordHash = hash, rangoEtario = "35-44", genero = "Femenino", telefono = telefono,
            codigoEmbajadora = null, region = region, comuna = comuna, nombreEmprendimiento = tienda,
            formalizado = "Estoy en proceso de formalización", rubroIds = rubrosSocia.map { r.getValue(it) }.joinToString(","),
            tiempoDedicado = "1-3 años", ingreso = "Fuente complementaria", aspiracion = "Sí, es mi objetivo",
            instagram = instagram, comoNosConociste = "Instagram", comoNosConocisteOtro = null, quienInvito = null,
            consentimientoEn = ahora, estadoFicha = ficha, estadoSync = EstadoSync.SINCRONIZADO
        )

        val dao = db.sociaDao()
        val demo = dao.insertar(
            socia("Rosa Valdés Fuentes", "15876543", DEMO_EMAIL, "Valparaíso", "Viña del Mar",
                "Hilos de Rosa", listOf(SUPRARRECICLAJE, REPARACION), "hilosderosa.demo", "912345678")
        )
        val s2 = dao.insertar(
            socia("Carmen Lagos Pinto", "13456789", "carmen@demo.cl", "Valparaíso", "Quilpué",
                "Remiendos Carmen", listOf(REPARACION), null, "987654321")
        )
        val s3 = dao.insertar(
            socia("Paula Inostroza Mella", "17654321", "paula@demo.cl", "Biobío", "Concepción",
                "Segunda Piel", listOf(SEGUNDA_MANO, ARRIENDO), "segundapiel.demo", "956781234")
        )
        val s4 = dao.insertar(
            socia("Valentina Aravena Soto", "19234567", "valentina@demo.cl", "Metropolitana de Santiago", "Ñuñoa",
                "Taller Hebra", listOf(TALLERES, SUPRARRECICLAJE), "tallerhebra.demo", "923456781")
        )
        val s5 = dao.insertar(
            socia("Gloria Huenchumán Paillalef", "12345670", "gloria@demo.cl", "La Araucanía", "Temuco",
                "Telar del Sur", listOf(CONFECCION), null, "934567812")
        )

        data class Pub(
            val socia: Long, val tipo: TipoPublicacion, val titulo: String, val descripcion: String,
            val rubro: String, val precio: Int?, val disp: Disponibilidad, val dias: Int?,
            val estado: EstadoPublicacion, val fotos: Int, val vistas: Int, val consultas: Int, val haceDias: Int
        )

        val pubs = listOf(
            Pub(demo, TipoPublicacion.PRODUCTO, "Chaqueta de mezclilla intervenida",
                "Chaqueta de mezclilla reutilizada con parches bordados a mano. Talla M, lista para usar.",
                SUPRARRECICLAJE, 24990, Disponibilidad.LISTA, null, EstadoPublicacion.PUBLICADA, 3, 48, 5, 1),
            Pub(demo, TipoPublicacion.SERVICIO, "Arreglo de bastas y entalle",
                "Ajusto pantalones, faldas y vestidos. Entrega en 3 días hábiles en Viña del Mar.",
                REPARACION, 4500, Disponibilidad.A_PEDIDO, 3, EstadoPublicacion.PUBLICADA, 1, 31, 3, 2),
            Pub(demo, TipoPublicacion.PRODUCTO, "Bolso tote de retazos",
                "Bolso amplio hecho con retazos de tela recuperados. Forro interior y bolsillo.",
                SUPRARRECICLAJE, 12000, Disponibilidad.LISTA, null, EstadoPublicacion.AGOTADA, 2, 22, 2, 6),
            Pub(demo, TipoPublicacion.PRODUCTO, "Delantal de cocina reciclado",
                "Delantal hecho con camisas en desuso. Pendiente de sacar las fotos finales.",
                SUPRARRECICLAJE, null, Disponibilidad.LISTA, null, EstadoPublicacion.BORRADOR, 0, 0, 0, 0),
            Pub(demo, TipoPublicacion.TALLER, "Taller de zurcido visible",
                "Aprende a reparar tus prendas con la técnica de zurcido visible. Cupos para 8 personas.",
                REPARACION, 15000, Disponibilidad.A_PEDIDO, 10, EstadoPublicacion.PAUSADA, 1, 12, 1, 9),
            Pub(s2, TipoPublicacion.SERVICIO, "Cambio de cierres",
                "Cambio cierres de chaquetas, pantalones y mochilas. Retiro en Quilpué centro.",
                REPARACION, 5000, Disponibilidad.A_PEDIDO, 2, EstadoPublicacion.PUBLICADA, 1, 19, 2, 3),
            Pub(s3, TipoPublicacion.PRODUCTO, "Vestido de fiesta segunda mano",
                "Vestido largo color esmeralda, usado una vez. Talla S. Impecable.",
                SEGUNDA_MANO, 18000, Disponibilidad.LISTA, null, EstadoPublicacion.PUBLICADA, 2, 40, 6, 2),
            Pub(s3, TipoPublicacion.ARRIENDO, "Arriendo de terno para matrimonio",
                "Terno azul marino tallas 48 a 52. Arriendo por 3 días con limpieza incluida.",
                ARRIENDO, null, Disponibilidad.LISTA, null, EstadoPublicacion.PUBLICADA, 1, 15, 1, 4),
            Pub(s4, TipoPublicacion.TALLER, "Taller de upcycling para principiantes",
                "Transforma una polera en una bolsa reutilizable. Materiales incluidos. Sábados 11:00.",
                TALLERES, 9900, Disponibilidad.A_PEDIDO, 7, EstadoPublicacion.PUBLICADA, 2, 27, 4, 5),
            Pub(s4, TipoPublicacion.PRODUCTO, "Cojines de jeans recuperados",
                "Set de 2 cojines de 40x40 cm hechos con jeans en desuso.",
                SUPRARRECICLAJE, 16500, Disponibilidad.LISTA, null, EstadoPublicacion.PUBLICADA, 1, 9, 0, 8),
            Pub(s5, TipoPublicacion.PRODUCTO, "Pie de cama tejido a telar",
                "Pie de cama tejido en telar mapuche con lana recuperada. 1,2 x 0,5 m.",
                CONFECCION, 35000, Disponibilidad.A_PEDIDO, 20, EstadoPublicacion.PUBLICADA, 2, 33, 3, 7),
            Pub(s5, TipoPublicacion.PRODUCTO, "Bajada de mesa en lana",
                "Bajada de mesa tejida a mano con restos de lana teñida con tintes naturales.",
                CONFECCION, 14000, Disponibilidad.LISTA, null, EstadoPublicacion.PUBLICADA, 1, 11, 0, 10)
        )

        val pubDao = db.publicacionDao()
        var semilla = 0
        val ids = pubs.map { p ->
            val id = pubDao.insertar(
                PublicacionEntity(
                    sociaId = p.socia, tipo = p.tipo, titulo = p.titulo, descripcion = p.descripcion,
                    rubroId = r.getValue(p.rubro), precio = p.precio, aConvenir = p.precio == null && p.estado != EstadoPublicacion.BORRADOR,
                    disponibilidad = p.disp, diasElaboracion = p.dias, estado = p.estado, vistas = p.vistas,
                    consultas = p.consultas, creadaEn = ahora - p.haceDias * dia, actualizadaEn = ahora - p.haceDias * dia,
                    estadoSync = EstadoSync.SINCRONIZADO
                )
            )
            pubDao.insertarFotos((0 until p.fotos).map { i ->
                FotoEntity(publicacionId = id, ruta = "seed:${semilla++}", orden = i)
            })
            id
        }

        val convDao = db.conversacionDao()
        val c1 = convDao.insertar(
            ConversacionEntity(
                sociaId = demo, clienteNombre = "Fernanda Castro", clienteEmail = "fernanda@ejemplo.cl",
                publicacionId = ids[0], publicacionTitulo = pubs[0].titulo,
                ultimoMensaje = "¿Tienes la misma chaqueta en talla L?", noLeidos = 1, actualizadaEn = ahora - 3_600_000
            )
        )
        convDao.insertarMensaje(MensajeEntity(conversacionId = c1, texto = "Hola, quiero saber más sobre: ${pubs[0].titulo}", deSocia = false, enviadoEn = ahora - 7_200_000))
        convDao.insertarMensaje(MensajeEntity(conversacionId = c1, texto = "¡Hola Fernanda! Claro, cuéntame qué te gustaría saber.", deSocia = true, enviadoEn = ahora - 5_400_000))
        convDao.insertarMensaje(MensajeEntity(conversacionId = c1, texto = "¿Tienes la misma chaqueta en talla L?", deSocia = false, enviadoEn = ahora - 3_600_000))
        val c2 = convDao.insertar(
            ConversacionEntity(
                sociaId = demo, clienteNombre = "Isidora Pérez", clienteEmail = "isidora@ejemplo.cl",
                publicacionId = ids[1], publicacionTitulo = pubs[1].titulo,
                ultimoMensaje = "Perfecto, te llevo el pantalón el lunes.", noLeidos = 0, actualizadaEn = ahora - 2 * dia
            )
        )
        convDao.insertarMensaje(MensajeEntity(conversacionId = c2, texto = "Hola, quiero saber más sobre: ${pubs[1].titulo}", deSocia = false, enviadoEn = ahora - 2 * dia - 600_000))
        convDao.insertarMensaje(MensajeEntity(conversacionId = c2, texto = "Perfecto, te llevo el pantalón el lunes.", deSocia = false, enviadoEn = ahora - 2 * dia))

        db.novedadDao().insertar(
            listOf(
                NovedadEntity(titulo = "Bienvenida a la nueva app", cuerpo = "Ahora puedes publicar tus trabajos con fotos y recibir consultas directamente aquí.", fecha = ahora - dia),
                NovedadEntity(titulo = "Taller gratuito de fotografía con celular", cuerpo = "Aprende a sacar mejores fotos de tus productos. Jueves 18:00, modalidad online.", fecha = ahora - 4 * dia, leida = true),
                NovedadEntity(titulo = "Encuesta de impacto 2026", cuerpo = "Ayúdanos a medir el impacto de la red respondiendo una encuesta de 5 minutos.", fecha = ahora - 9 * dia, leida = true)
            )
        )
    }

    companion object {
        const val DEMO_EMAIL = "demo@textilcircular.cl"
        const val DEMO_PASSWORD = "demo1234"

        private const val SUPRARRECICLAJE = "Suprarreciclaje (upcycling)"
        private const val REPARACION = "Reparación y arreglos"
        private const val SEGUNDA_MANO = "Segunda mano"
        private const val ARRIENDO = "Arriendo de prendas"
        private const val TALLERES = "Talleres y educación"
        private const val CONFECCION = "Confección con materiales recuperados"
        private const val RECICLAJE = "Reciclaje y gestión de residuos textiles"

        /** Catálogo inicial de 7 rubros (RN02); vive en la tabla "rubros" y puede crecer. */
        val RUBROS = listOf(SUPRARRECICLAJE, REPARACION, SEGUNDA_MANO, ARRIENDO, TALLERES, CONFECCION, RECICLAJE)
    }
}
