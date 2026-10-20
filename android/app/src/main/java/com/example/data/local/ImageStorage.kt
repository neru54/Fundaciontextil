package com.example.data.local

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.exifinterface.media.ExifInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

/**
 * Manejo de las fotos de las prendas:
 * - crea el archivo temporal donde la cámara guarda la foto (vía FileProvider, sin exponer rutas)
 * - lee fotos de la cámara o la galería reduciendo su tamaño y corrigiendo la rotación
 * - guarda la foto final en el almacenamiento interno; en Room solo queda la ruta
 */
class ImageStorage(private val context: Context) {

    private val imagesDir: File
        get() = File(context.filesDir, "product_images").apply { mkdirs() }

    private val cameraCacheDir: File
        get() = File(context.cacheDir, "camera").apply { mkdirs() }

    /** URI temporal y privado que se entrega a la app de cámara para que escriba la foto. */
    fun createCameraUri(): Uri {
        val file = File(cameraCacheDir, "camera_${UUID.randomUUID()}.jpg")
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    /** Lee una imagen (cámara o galería) sin cargarla completa en memoria. Devuelve null si falla. */
    suspend fun loadBitmap(uri: Uri): Bitmap? = withContext(Dispatchers.IO) {
        runCatching {
            val resolver = context.contentResolver

            // 1) Leer solo las dimensiones para calcular cuánto reducir
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
            if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return@runCatching null

            // 2) Decodificar reducida (potencia de 2 más cercana a MAX_SIZE_PX)
            var sample = 1
            while (maxOf(bounds.outWidth, bounds.outHeight) / (sample * 2) >= MAX_SIZE_PX) sample *= 2
            val options = BitmapFactory.Options().apply { inSampleSize = sample }
            val bitmap = resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, options) }
                ?: return@runCatching null

            // 3) Corregir la rotación que la cámara guarda en los metadatos EXIF
            val orientation = resolver.openInputStream(uri)?.use {
                ExifInterface(it).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
            } ?: ExifInterface.ORIENTATION_NORMAL
            val degrees = when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                else -> 0f
            }
            if (degrees == 0f) bitmap
            else Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, Matrix().apply { postRotate(degrees) }, true)
        }.getOrNull()
    }

    /** Comprime la imagen a JPEG (máx. 1280 px) y devuelve la ruta absoluta del archivo. */
    suspend fun saveBitmap(bitmap: Bitmap): String = withContext(Dispatchers.IO) {
        val scaled = scaleDown(bitmap, MAX_SIZE_PX)
        val file = File(imagesDir, "img_${UUID.randomUUID()}.jpg")
        FileOutputStream(file).use { out ->
            scaled.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, out)
        }
        // Las fotos temporales de la cámara ya no se necesitan
        cameraCacheDir.listFiles()?.forEach { it.delete() }
        file.absolutePath
    }

    private fun scaleDown(bitmap: Bitmap, maxSize: Int): Bitmap {
        val largest = maxOf(bitmap.width, bitmap.height)
        if (largest <= maxSize) return bitmap
        val ratio = maxSize.toFloat() / largest
        return Bitmap.createScaledBitmap(
            bitmap,
            (bitmap.width * ratio).toInt(),
            (bitmap.height * ratio).toInt(),
            true
        )
    }

    private companion object {
        const val MAX_SIZE_PX = 1280
        const val JPEG_QUALITY = 85
    }
}
