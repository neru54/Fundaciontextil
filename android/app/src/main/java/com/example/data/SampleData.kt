package com.example.data

import com.example.domain.model.Announcement
import com.example.domain.model.Chat
import com.example.domain.model.ChatMessage
import com.example.domain.model.Product

/** Datos iniciales (ver DatabaseSeeder). */
object SampleData {
    val sampleProducts = listOf(
        Product(
            id = 1,
            title = "Chaqueta Denim Upcycling",
            price = 38990,
            category = "Chaquetas",
            partnerName = "Valentina Silva",
            partnerComuna = "Conchalí",
            ecoTag = "100% Jeans Recuperado",
            kgSaved = 1.2,
            litersWater = 14000,
            description = "Chaqueta única confeccionada combinando diferentes tonos de mezclilla rescatada de prendas en desuso. Corte moderno y forro interior de algodón.",
            fabricOrigin = "Fardos de mezclilla clasificados en Conchalí",
            colorHex = 0xFF1B4965
        ),
        Product(
            id = 2,
            title = "Bolso Tote de Lona y Cuero Vegano",
            price = 18500,
            category = "Bolsos",
            partnerName = "Carmen Morales",
            partnerComuna = "Paine",
            ecoTag = "Lona Industrial Rescatada",
            kgSaved = 0.7,
            litersWater = 4500,
            description = "Bolso amplio de alta durabilidad, elaborado con excedentes de tapicería y correas de algodón trenzado reforzadas.",
            fabricOrigin = "Mermas de talleres de tapicería en la zona sur",
            colorHex = 0xFF2B580C
        ),
        Product(
            id = 3,
            title = "Banano Urbano Eco-Ripstop",
            price = 14990,
            category = "Accesorios",
            partnerName = "Camila Henríquez",
            partnerComuna = "Recoleta",
            ecoTag = "Nylon Técnico Reutilizado",
            kgSaved = 0.4,
            litersWater = 3200,
            description = "Banano impermeable con tres compartimientos, confeccionado con retazos de carpas de montaña y forro de paraguas reciclados.",
            fabricOrigin = "Excedentes de talleres de equipo outdoor",
            colorHex = 0xFFD4A373
        ),
        Product(
            id = 4,
            title = "Cojín Decorativo Patchwork Telar",
            price = 12000,
            category = "Decoración",
            partnerName = "Valentina Silva",
            partnerComuna = "Conchalí",
            ecoTag = "Retazos Mixtos Algodón/Lino",
            kgSaved = 0.8,
            litersWater = 6000,
            description = "Funda de cojín con diseño geométrico contemporáneo. Relleno con borra textil triturada de descarte limpio.",
            fabricOrigin = "Retazos de talleres textiles familiares",
            colorHex = 0xFF8338EC
        ),
        Product(
            id = 5,
            title = "Bufanda Telar Tradicional en Lana",
            price = 22000,
            category = "Accesorios",
            partnerName = "Carmen Morales",
            partnerComuna = "Paine",
            ecoTag = "Lana Hilada Reclaimed",
            kgSaved = 0.5,
            litersWater = 8000,
            description = "Tejida a mano en telar de cuatro cuadros con hilado rescatado de sobrantes de hilandería en la zona central.",
            fabricOrigin = "Sobrantes de hilandería artesanal",
            colorHex = 0xFFBC6C25
        ),
        Product(
            id = 6,
            title = "Pack Creativo Retazos Clasificados 2kg",
            price = 9900,
            category = "Retazos",
            partnerName = "Valentina Silva",
            partnerComuna = "Conchalí",
            ecoTag = "Cero Desperdicio Textil",
            kgSaved = 2.0,
            litersWater = 18000,
            description = "Bolsa de 2 kg con cortes limpios de lino, gabardina y mezclilla listos para costureras, artistas y amantes del upcycling.",
            fabricOrigin = "Cortes de orilla de talleres de confección",
            colorHex = 0xFF028090
        )
    )

    val sampleAnnouncements = listOf(
        Announcement(
            id = 1,
            title = "Convocatoria Feria Sustentable Plaza Ñuñoa 2026",
            date = "12 al 15 de Diciembre, 2026",
            location = "Plaza Ñuñoa, Región Metropolitana",
            category = "Feria & Exposición",
            description = "Apertura de postulaciones para stands gratuitos para socias activas de la Fundación. Incluye toldo, mesa y punto de pago Redelcom.",
            requirements = "Socia al día, contar con al menos 10 productos etiquetados con eco-trazabilidad.",
            availableSpots = 25,
            isExclusiveSocias = true
        ),
        Announcement(
            id = 2,
            title = "Taller: Patronaje Zero Waste y Upcycling en Denim",
            date = "Sábado 24 de Octubre, 10:00 - 14:00 hrs",
            location = "Sede Central Fundación (Santiago Centro)",
            category = "Capacitación Gratuita",
            description = "Aprende técnicas avanzadas de patronaje modular sin merma y ensamble de mezclilla pesada dictado por diseñadoras invitadas.",
            requirements = "Traer tijeras de tela y cinta métrica. Las máquinas industriales y telas son provistas por la Fundación.",
            availableSpots = 15,
            isExclusiveSocias = true
        ),
        Announcement(
            id = 3,
            title = "Entrega Mensual de Fardos Textiles de Retail",
            date = "Viernes 30 de Octubre, 09:30 - 13:00 hrs",
            location = "Centro de Acopio Pudahuel",
            category = "Insumos y Telas",
            description = "Retiro gratuito de hasta 15 kg de retazos limpios de algodón, lino y gabardina donados por empresas aliadas para producción de socias.",
            requirements = "Estar inscrita en la planilla mensual y portar credencial de socia.",
            availableSpots = 40,
            isExclusiveSocias = true
        )
    )

    val sampleChats: List<Pair<Chat, List<ChatMessage>>> = listOf(
        Chat(
            clientName = "María Valenzuela",
            clientEmail = "maria.valenzuela@gmail.com",
            lastMessage = "¿Hola Valentina! ¿Tienes disponible la chaqueta de denim en talla M o confeccionas a medida?",
            time = "10:24 AM",
            productTitle = "Chaqueta Denim Upcycling",
            unreadCount = 1
        ) to listOf(
            msg("María Valenzuela", true, "¡Hola Valentina! Vi tu chaqueta en el catálogo y me encantó el trabajo de parches.", "10:20 AM"),
            msg("Valentina Silva", false, "¡Hola María! Muchas gracias por apoyar la moda circular.", "10:22 AM"),
            msg("María Valenzuela", true, "¿Tienes disponible la chaqueta de denim en talla M o confeccionas a medida?", "10:24 AM")
        ),
        Chat(
            clientName = "Francisca Gómez",
            clientEmail = "fran.gomez@live.cl",
            lastMessage = "Me llegó el bolso tote a Paine y la calidad de las costuras es maravillosa.",
            time = "Ayer",
            productTitle = "Bolso Tote de Lona y Cuero Vegano",
            unreadCount = 0
        ) to listOf(
            msg("Francisca Gómez", true, "Hola Carmen, quería avisar que ya recibí el paquete.", "Ayer 15:10"),
            msg("Carmen Morales", false, "¿Llegó todo bien con la encomienda Francisca?", "Ayer 15:30"),
            msg("Francisca Gómez", true, "Me llegó el bolso tote a Paine y la calidad de las costuras es maravillosa.", "Ayer 15:35")
        ),
        Chat(
            clientName = "Macarena Soto",
            clientEmail = "macarena.soto@gmail.com",
            lastMessage = "Hola, ¿el pack de retazos incluye telas elasticadas o solo planas?",
            time = "12 Oct",
            productTitle = "Pack Creativo Retazos Clasificados",
            unreadCount = 0
        ) to listOf(
            msg("Macarena Soto", true, "Hola, ¿el pack de retazos incluye telas elasticadas o solo planas?", "12 Oct 18:00")
        )
    )

    private fun msg(sender: String, isClient: Boolean, text: String, time: String) =
        ChatMessage(sender = sender, isClient = isClient, text = text, time = time)
}
