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
    private const val AVATAR_PREFIX = "profile_avatar_"

    fun ownerKeyForUser(userId: String?): String {
        val trimmed = userId?.trim().orEmpty()
        if (trimmed.isBlank()) return "guest"
        val safeUid = trimmed.replace(Regex("[^a-zA-Z0-9_-]"), "_")
        return "user_$safeUid"
    }

    suspend fun saveProfilePhotoFromUri(
        context: Context,
        ownerKey: String,
        sourceUri: Uri
    ): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val decoded = context.contentResolver.openInputStream(sourceUri)?.use { input ->
                    BitmapFactory.decodeStream(input)
                } ?: return@withContext Result.failure(IllegalArgumentException("Unable to read selected image."))

                val cropped = centerCropSquare(decoded)
                val scaled = Bitmap.createScaledBitmap(cropped, AVATAR_SIZE_PX, AVATAR_SIZE_PX, true)

                val scopedPrefix = "${AVATAR_PREFIX}${ownerKey}_"
                val outFile = File(context.filesDir, "${scopedPrefix}${System.currentTimeMillis()}.jpg")
                // Clean up older avatar files belonging only to this owner (plus any legacy unscoped files)
                context.filesDir.listFiles { file ->
                    file.name.endsWith(".jpg") && (
                        file.name.startsWith(scopedPrefix) ||
                            isLegacyUnscopedAvatarFile(file.name)
                        )
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

    suspend fun deleteLegacyUnscopedPhotos(context: Context) {
        withContext(Dispatchers.IO) {
            try {
                context.filesDir.listFiles { file ->
                    isLegacyUnscopedAvatarFile(file.name)
                }?.forEach { old -> old.delete() }
            } catch (_: Exception) {
            }
        }
    }

    private fun isLegacyUnscopedAvatarFile(fileName: String): Boolean {
        return fileName.startsWith(AVATAR_PREFIX) &&
            fileName.endsWith(".jpg") &&
            !fileName.startsWith("${AVATAR_PREFIX}user_") &&
            !fileName.startsWith("${AVATAR_PREFIX}guest_")
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
