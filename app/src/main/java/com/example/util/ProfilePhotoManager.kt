package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

object ProfilePhotoManager {

    private const val AVATAR_SIZE_PX = 512

    suspend fun saveProfilePhotoFromUri(context: Context, sourceUri: Uri): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val decoded = context.contentResolver.openInputStream(sourceUri)?.use { input ->
                    BitmapFactory.decodeStream(input)
                } ?: return@withContext Result.failure(IllegalArgumentException("Unable to read selected image."))

                val cropped = centerCropSquare(decoded)
                val scaled = Bitmap.createScaledBitmap(cropped, AVATAR_SIZE_PX, AVATAR_SIZE_PX, true)

                val outFile = File(context.filesDir, "profile_avatar_${System.currentTimeMillis()}.jpg")
                // Clean up older avatar files to avoid bloat
                context.filesDir.listFiles { file ->
                    file.name.startsWith("profile_avatar_") && file.name.endsWith(".jpg")
                }?.forEach { old -> old.delete() }

                FileOutputStream(outFile).use { fos ->
                    scaled.compress(Bitmap.CompressFormat.JPEG, 90, fos)
                }

                if (cropped !== decoded) cropped.recycle()
                if (scaled !== cropped && scaled !== decoded) scaled.recycle()
                decoded.recycle()

                Result.success(outFile.absolutePath)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun deleteProfilePhoto(path: String?) {
        if (path.isNullOrBlank()) return
        withContext(Dispatchers.IO) {
            try {
                val file = File(path)
                if (file.exists()) {
                    file.delete()
                }
            } catch (_: Exception) {
            }
        }
    }

    fun loadBitmap(path: String?): Bitmap? {
        if (path.isNullOrBlank()) return null
        val file = File(path)
        if (!file.exists()) return null
        return try {
            BitmapFactory.decodeFile(file.absolutePath)
        } catch (_: Exception) {
            null
        }
    }

    private fun centerCropSquare(source: Bitmap): Bitmap {
        val size = minOf(source.width, source.height)
        val xOffset = (source.width - size) / 2
        val yOffset = (source.height - size) / 2
        return Bitmap.createBitmap(source, xOffset, yOffset, size, size)
    }
}
