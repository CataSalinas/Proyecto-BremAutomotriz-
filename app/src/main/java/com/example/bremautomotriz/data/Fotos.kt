package com.example.bremautomotriz.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

object Fotos {
    private const val CARPETA = "evidencias"

    fun nuevoArchivo(context: Context, otId: Long): File {
        val carpeta = File(context.filesDir, CARPETA)
        carpeta.mkdirs()
        return File(carpeta, "ot_${otId}_${System.currentTimeMillis()}.jpg")
    }

    fun uriParaCamara(context: Context, archivo: File): Uri =
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", archivo)

    fun cargar(ruta: String, ladoMaximo: Int = 800): Bitmap? {
        val archivo = File(ruta)
        if (!archivo.exists() || archivo.length() == 0L) return null

        val limites = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(ruta, limites)
        var muestra = 1
        while (limites.outWidth / muestra > ladoMaximo * 2 || limites.outHeight / muestra > ladoMaximo * 2) {
            muestra *= 2
        }

        val opciones = BitmapFactory.Options().apply { inSampleSize = muestra }
        val bitmap = BitmapFactory.decodeFile(ruta, opciones) ?: return null

        val orientacion = runCatching {
            ExifInterface(ruta).getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
        }.getOrDefault(ExifInterface.ORIENTATION_NORMAL)

        val grados = when (orientacion) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90f
            ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            ExifInterface.ORIENTATION_ROTATE_270 -> 270f
            else -> 0f
        }
        if (grados == 0f) return bitmap

        val matriz = Matrix().apply { postRotate(grados) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matriz, true)
    }
}