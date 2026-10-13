package cl.textilcircular.app.data.media

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * Manejo de fotos del catálogo (RF02.1, RF02.3): crea el archivo temporal para la cámara,
 * comprime las imágenes antes de guardarlas y las elimina cuando corresponde (RN23).
 */
class ImageStorage(private val context: Context) {

    private val carpetaFotos: File get() = File(context.filesDir, "fotos").apply { mkdirs() }
    private val carpetaCamara: File get() = File(context.cacheDir, "camara").apply { mkdirs() }

    /** Uri temporal donde la app de cámara deja la foto (TakePicture). */
    fun crearUriParaCamara(): Uri {
        val archivo = File(carpetaCamara, "captura_${System.currentTimeMillis()}.jpg")
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", archivo)
    }

    /**
     * Comprime la imagen a un máximo de [LADO_MAXIMO] px en el lado mayor y JPEG [CALIDAD] %
     * y la guarda en el almacenamiento interno. Devuelve la ruta del archivo.
     */
    suspend fun comprimirYGuardar(origen: Uri): String = withContext(Dispatchers.IO) {
        val bitmap = decodificar(origen)
        val escala = LADO_MAXIMO.toFloat() / max(bitmap.width, bitmap.height)
        val final = if (escala < 1f) {
            Bitmap.createScaledBitmap(
                bitmap, (bitmap.width * escala).roundToInt(), (bitmap.height * escala).roundToInt(), true
            )
        } else bitmap
        val destino = File(carpetaFotos, "${UUID.randomUUID()}.jpg")
        destino.outputStream().use { final.compress(Bitmap.CompressFormat.JPEG, CALIDAD, it) }
        if (final !== bitmap) bitmap.recycle()
        final.recycle()
        limpiarCamara()
        destino.absolutePath
    }

    private fun decodificar(uri: Uri): Bitmap =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val fuente = ImageDecoder.createSource(context.contentResolver, uri)
            ImageDecoder.decodeBitmap(fuente) { decoder, info, _ ->
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                val mayor = max(info.size.width, info.size.height)
                if (mayor > LADO_MAXIMO * 2) {
                    val factor = mayor / (LADO_MAXIMO * 2)
                    decoder.setTargetSize(info.size.width / factor, info.size.height / factor)
                }
            }
        } else {
            val opciones = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, opciones) }
            var muestreo = 1
            while (max(opciones.outWidth, opciones.outHeight) / (muestreo * 2) >= LADO_MAXIMO) muestreo *= 2
            val opcionesFinales = BitmapFactory.Options().apply { inSampleSize = muestreo }
            context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, opcionesFinales) }
                ?: error("No se pudo leer la imagen")
        }

    /** Copia fotos guardadas por la app; las de demostración se reutilizan tal cual. */
    suspend fun copiar(rutas: List<String>): List<String> = withContext(Dispatchers.IO) {
        rutas.mapNotNull { ruta ->
            if (ruta.startsWith(SEED_PREFIX)) return@mapNotNull ruta
            val origen = File(ruta)
            if (!origen.exists()) return@mapNotNull null
            File(carpetaFotos, "${UUID.randomUUID()}.jpg").also { origen.copyTo(it) }.absolutePath
        }
    }

    /** Borra fotos guardadas por la app (ignora las de demostración "seed:"). */
    suspend fun eliminar(rutas: Collection<String>) = withContext(Dispatchers.IO) {
        rutas.filterNot { it.startsWith(SEED_PREFIX) }.forEach { File(it).delete() }
    }

    /** RNF04: limpieza de capturas temporales de la cámara. */
    private fun limpiarCamara() {
        carpetaCamara.listFiles()?.forEach { it.delete() }
    }

    companion object {
        const val LADO_MAXIMO = 1280
        const val CALIDAD = 80
        const val SEED_PREFIX = "seed:"
    }
}
