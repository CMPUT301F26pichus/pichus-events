
package com.example.code.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.ByteArrayOutputStream

/**
 * Shared image-processing utilities.
 */
object ImageUtils {

    /**
     * Resizes and compresses an image into JPEG bytes.
     *
     * Keeps the longest dimension at or below 1024 pixels
     * and attempts to keep the image below 900 KB.
     */
    fun compressImage(
        context: Context,
        uri: Uri
    ): ByteArray {

        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw Exception("Could not open selected image.")

        val bitmap = inputStream.use {
            BitmapFactory.decodeStream(it)
        } ?: throw Exception("Could not decode selected image.")

        var resizedBitmap = bitmap

        val maxDimension = 1024

        if (bitmap.width > maxDimension || bitmap.height > maxDimension) {

            val scale = minOf(
                maxDimension.toFloat() / bitmap.width,
                maxDimension.toFloat() / bitmap.height
            )

            resizedBitmap = Bitmap.createScaledBitmap(
                bitmap,
                (bitmap.width * scale).toInt(),
                (bitmap.height * scale).toInt(),
                true
            )
        }

        var quality = 85
        var bytes: ByteArray

        do {
            val outputStream = ByteArrayOutputStream()

            resizedBitmap.compress(
                Bitmap.CompressFormat.JPEG,
                quality,
                outputStream
            )

            bytes = outputStream.toByteArray()
            quality -= 10

        } while (bytes.size > 900 * 1024 && quality >= 45)

        if (bitmap !== resizedBitmap) {
            bitmap.recycle()
        }

        resizedBitmap.recycle()

        if (bytes.size > 900 * 1024) {
            throw Exception("Image is too large. Please select a smaller image.")
        }

        return bytes
    }
}
