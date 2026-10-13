package cl.textilcircular.app.domain.catalog

/**
 * Catálogo cerrado y jerárquico Región 1..N Comuna (RN01, RNF06).
 * Reemplaza las columnas de comuna por región de Google Forms, causa raíz de las fichas invisibles.
 */
object ChileGeo {

    val comunasPorRegion: Map<String, List<String>> = linkedMapOf(
        "Arica y Parinacota" to listOf("Arica", "Camarones", "General Lagos", "Putre"),
        "Tarapacá" to listOf("Alto Hospicio", "Camiña", "Colchane", "Huara", "Iquique", "Pica", "Pozo Almonte"),
        "Antofagasta" to listOf(
            "Antofagasta", "Calama", "María Elena", "Mejillones", "Ollagüe", "San Pedro de Atacama",
            "Sierra Gorda", "Taltal", "Tocopilla"
        ),
        "Atacama" to listOf(
            "Alto del Carmen", "Caldera", "Chañaral", "Copiapó", "Diego de Almagro", "Freirina",
            "Huasco", "Tierra Amarilla", "Vallenar"
        ),
        "Coquimbo" to listOf(
            "Andacollo", "Canela", "Combarbalá", "Coquimbo", "Illapel", "La Higuera", "La Serena",
            "Los Vilos", "Monte Patria", "Ovalle", "Paiguano", "Punitaqui", "Río Hurtado", "Salamanca", "Vicuña"
        ),
        "Valparaíso" to listOf(
            "Algarrobo", "Cabildo", "Calle Larga", "Cartagena", "Casablanca", "Catemu", "Concón",
            "El Quisco", "El Tabo", "Hijuelas", "Isla de Pascua", "Juan Fernández", "La Calera", "La Cruz",
            "La Ligua", "Limache", "Llaillay", "Los Andes", "Nogales", "Olmué", "Panquehue", "Papudo",
            "Petorca", "Puchuncaví", "Putaendo", "Quillota", "Quilpué", "Quintero", "Rinconada",
            "San Antonio", "San Esteban", "San Felipe", "Santa María", "Santo Domingo", "Valparaíso",
            "Villa Alemana", "Viña del Mar", "Zapallar"
        ),
        "Metropolitana de Santiago" to listOf(
            "Alhué", "Buin", "Calera de Tango", "Cerrillos", "Cerro Navia", "Colina", "Conchalí", "Curacaví",
            "El Bosque", "El Monte", "Estación Central", "Huechuraba", "Independencia", "Isla de Maipo",
            "La Cisterna", "La Florida", "La Granja", "La Pintana", "La Reina", "Lampa", "Las Condes",
            "Lo Barnechea", "Lo Espejo", "Lo Prado", "Macul", "Maipú", "María Pinto", "Melipilla", "Ñuñoa",
            "Padre Hurtado", "Paine", "Pedro Aguirre Cerda", "Peñaflor", "Peñalolén", "Pirque", "Providencia",
            "Pudahuel", "Puente Alto", "Quilicura", "Quinta Normal", "Recoleta", "Renca", "San Bernardo",
            "San Joaquín", "San José de Maipo", "San Miguel", "San Pedro", "San Ramón", "Santiago",
            "Talagante", "Tiltil", "Vitacura"
        ),
        "Libertador General Bernardo O'Higgins" to listOf(
            "Chépica", "Chimbarongo", "Codegua", "Coinco", "Coltauco", "Doñihue", "Graneros", "La Estrella",
            "Las Cabras", "Litueche", "Lolol", "Machalí", "Malloa", "Marchigüe", "Mostazal", "Nancagua",
            "Navidad", "Olivar", "Palmilla", "Paredones", "Peralillo", "Peumo", "Pichidegua", "Pichilemu",
            "Placilla", "Pumanque", "Quinta de Tilcoco", "Rancagua", "Rengo", "Requínoa", "San Fernando",
            "San Vicente", "Santa Cruz"
        ),
        "Maule" to listOf(
            "Cauquenes", "Chanco", "Colbún", "Constitución", "Curepto", "Curicó", "Empedrado", "Hualañé",
            "Licantén", "Linares", "Longaví", "Maule", "Molina", "Parral", "Pelarco", "Pelluhue", "Pencahue",
            "Rauco", "Retiro", "Río Claro", "Romeral", "Sagrada Familia", "San Clemente", "San Javier",
            "San Rafael", "Talca", "Teno", "Vichuquén", "Villa Alegre", "Yerbas Buenas"
        ),
        "Ñuble" to listOf(
            "Bulnes", "Chillán", "Chillán Viejo", "Cobquecura", "Coelemu", "Coihueco", "El Carmen", "Ninhue",
            "Ñiquén", "Pemuco", "Pinto", "Portezuelo", "Quillón", "Quirihue", "Ránquil", "San Carlos",
            "San Fabián", "San Ignacio", "San Nicolás", "Treguaco", "Yungay"
        ),
        "Biobío" to listOf(
            "Alto Biobío", "Antuco", "Arauco", "Cabrero", "Cañete", "Chiguayante", "Concepción", "Contulmo",
            "Coronel", "Curanilahue", "Florida", "Hualpén", "Hualqui", "Laja", "Lebu", "Los Álamos",
            "Los Ángeles", "Lota", "Mulchén", "Nacimiento", "Negrete", "Penco", "Quilaco", "Quilleco",
            "San Pedro de la Paz", "San Rosendo", "Santa Bárbara", "Santa Juana", "Talcahuano", "Tirúa",
            "Tomé", "Tucapel", "Yumbel"
        ),
        "La Araucanía" to listOf(
            "Angol", "Carahue", "Cholchol", "Collipulli", "Cunco", "Curacautín", "Curarrehue", "Ercilla",
            "Freire", "Galvarino", "Gorbea", "Lautaro", "Loncoche", "Lonquimay", "Los Sauces", "Lumaco",
            "Melipeuco", "Nueva Imperial", "Padre Las Casas", "Perquenco", "Pitrufquén", "Pucón", "Purén",
            "Renaico", "Saavedra", "Temuco", "Teodoro Schmidt", "Toltén", "Traiguén", "Victoria", "Vilcún",
            "Villarrica"
        ),
        "Los Ríos" to listOf(
            "Corral", "Futrono", "La Unión", "Lago Ranco", "Lanco", "Los Lagos", "Máfil", "Mariquina",
            "Paillaco", "Panguipulli", "Río Bueno", "Valdivia"
        ),
        "Los Lagos" to listOf(
            "Ancud", "Calbuco", "Castro", "Chaitén", "Chonchi", "Cochamó", "Curaco de Vélez", "Dalcahue",
            "Fresia", "Frutillar", "Futaleufú", "Hualaihué", "Llanquihue", "Los Muermos", "Maullín", "Osorno",
            "Palena", "Puerto Montt", "Puerto Octay", "Puerto Varas", "Puqueldón", "Purranque", "Puyehue",
            "Queilén", "Quellón", "Quemchi", "Quinchao", "Río Negro", "San Juan de la Costa", "San Pablo"
        ),
        "Aysén del General Carlos Ibáñez del Campo" to listOf(
            "Aysén", "Chile Chico", "Cisnes", "Cochrane", "Coyhaique", "Guaitecas", "Lago Verde", "O'Higgins",
            "Río Ibáñez", "Tortel"
        ),
        "Magallanes y de la Antártica Chilena" to listOf(
            "Antártica", "Cabo de Hornos", "Laguna Blanca", "Natales", "Porvenir", "Primavera",
            "Punta Arenas", "Río Verde", "San Gregorio", "Timaukel", "Torres del Paine"
        )
    )

    val regiones: List<String> = comunasPorRegion.keys.toList()

    fun comunasDe(region: String?): List<String> = region?.let { comunasPorRegion[it] }.orEmpty()

    /** RN01: una comuna solo es válida si pertenece a la región elegida. */
    fun esComunaDeRegion(region: String?, comuna: String?): Boolean =
        !comuna.isNullOrBlank() && comuna in comunasDe(region)
}
