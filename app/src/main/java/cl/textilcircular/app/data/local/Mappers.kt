package cl.textilcircular.app.data.local

import cl.textilcircular.app.domain.model.Conversacion
import cl.textilcircular.app.domain.model.Mensaje
import cl.textilcircular.app.domain.model.Novedad
import cl.textilcircular.app.domain.model.Publicacion
import cl.textilcircular.app.domain.model.Rubro
import cl.textilcircular.app.domain.model.Socia
import cl.textilcircular.app.domain.model.TiendaPublica
import cl.textilcircular.app.domain.validation.Instagram
import cl.textilcircular.app.domain.validation.Validators

fun RubroEntity.toDomain() = Rubro(id, nombre)

fun String.toRubroIds(): List<Long> = split(",").mapNotNull { it.trim().toLongOrNull() }

fun SociaEntity.toDomain() = Socia(
    id = id, nombre = nombre, rut = rut, email = email, rangoEtario = rangoEtario, genero = genero,
    telefono = telefono, codigoEmbajadora = codigoEmbajadora, region = region, comuna = comuna,
    nombreEmprendimiento = nombreEmprendimiento, formalizado = formalizado, rubroIds = rubroIds.toRubroIds(),
    tiempoDedicado = tiempoDedicado, ingreso = ingreso, aspiracion = aspiracion, instagram = instagram,
    comoNosConociste = comoNosConociste, comoNosConocisteOtro = comoNosConocisteOtro, quienInvito = quienInvito,
    consentimientoEn = consentimientoEn, estadoFicha = estadoFicha, modoPausa = modoPausa, creadaEn = creadaEn
)

/**
 * Solo datos públicos (RF01.5). RN10: si el teléfono o el Instagram no son válidos,
 * la ficha pública simplemente omite el botón.
 */
fun SociaEntity.toTiendaPublica(rubros: Map<Long, String>) = TiendaPublica(
    sociaId = id,
    nombreEmprendimiento = nombreEmprendimiento,
    nombreSocia = nombre,
    region = region,
    comuna = comuna,
    rubros = rubroIds.toRubroIds().mapNotNull { rubros[it] },
    telefono = telefono.takeIf { Validators.telefono(it) == null },
    instagramUrl = Instagram.enlace(instagram),
    estadoFicha = estadoFicha,
    modoPausa = modoPausa
)

fun PublicacionConTienda.toDomain(fotos: List<String> = listOfNotNull(portada)) = Publicacion(
    id = id, sociaId = sociaId, tipo = tipo, titulo = titulo, descripcion = descripcion,
    rubroId = rubroId ?: 0, rubroNombre = rubroNombre ?: "Sin rubro", precio = precio,
    disponibilidad = disponibilidad, diasElaboracion = diasElaboracion, estado = estado,
    vistas = vistas, consultas = consultas, fotos = fotos, nombreTienda = nombreTienda,
    region = region, comuna = comuna, actualizadaEn = actualizadaEn
)

fun ConversacionEntity.toDomain() = Conversacion(
    id = id, sociaId = sociaId, clienteNombre = clienteNombre, clienteEmail = clienteEmail,
    publicacionId = publicacionId, publicacionTitulo = publicacionTitulo, ultimoMensaje = ultimoMensaje,
    noLeidos = noLeidos, actualizadaEn = actualizadaEn
)

fun MensajeEntity.toDomain() = Mensaje(id, conversacionId, texto, deSocia, enviadoEn)

fun NovedadEntity.toDomain() = Novedad(id, titulo, cuerpo, fecha, leida)
