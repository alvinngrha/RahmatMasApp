package com.example.rahmatmas.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.rahmatmas.data.supabase.SupabaseModule
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class PhotoUploadRepository(private val context: Context) {

    private val supabaseClient = SupabaseModule.client
    private val bucketName = "transaction-photos" // Nama bucket Supabase

    /**
     * Upload foto ke Supabase Storage
     * @param photoUri URI foto dari galeri/kamera
     * @param transactionId ID transaksi untuk nama file
     * @return Result dengan URL foto atau error
     */
    suspend fun uploadPhoto(photoUri: Uri, transactionId: String): Result<String> {
        return try {
            withContext(Dispatchers.IO) {
                // Generate unique filename
                val fileExtension = getFileExtension(photoUri)
                val fileName = "${transactionId}.$fileExtension"

                // Convert URI to ByteArray
                val photoBytes = uriToByteArray(photoUri)
                    ?: return@withContext Result.failure(Exception("Gagal membaca file foto"))

                // Upload to Supabase Storage
                val bucket = supabaseClient.storage.from(bucketName)
                bucket.upload(fileName, photoBytes) {
                    upsert = false
                }

                // Get public URL
                val publicUrl = bucket.publicUrl(fileName)

                Log.d("PhotoUpload", "Photo uploaded successfully: $publicUrl")
                Result.success(publicUrl)
            }
        } catch (e: Exception) {
            Log.e("PhotoUpload", "Error uploading photo", e)
            Result.failure(e)
        }
    }

    /**
     * Save photo locally and return local path
     * @param photoUri URI foto dari galeri/kamera
     * @param transactionId ID transaksi untuk nama file
     * @return Result dengan path lokal atau error
     */
    suspend fun savePhotoLocally(photoUri: Uri, transactionId: String): Result<String> {
        return try {
            withContext(Dispatchers.IO) {
                val fileExtension = getFileExtension(photoUri)
                val fileName = "${transactionId}_${System.currentTimeMillis()}.$fileExtension"

                // Create photos directory if not exists
                val photosDir = File(context.filesDir, "transaction_photos")
                if (!photosDir.exists()) {
                    photosDir.mkdirs()
                }

                val localFile = File(photosDir, fileName)

                // Copy file from URI to local storage
                context.contentResolver.openInputStream(photoUri)?.use { inputStream ->
                    FileOutputStream(localFile).use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }

                Log.d("PhotoUpload", "Photo saved locally: ${localFile.absolutePath}")
                Result.success(localFile.absolutePath)
            }
        } catch (e: Exception) {
            Log.e("PhotoUpload", "Error saving photo locally", e)
            Result.failure(e)
        }
    }

    /**
     * Upload and save photo (both cloud and local)
     * @param photoUri URI foto dari galeri/kamera
     * @param transactionId ID transaksi
     * @param forceLocal Jika true, hanya simpan lokal meski online
     * @return Result dengan PhotoUploadResult
     */
    suspend fun uploadAndSavePhoto(
        photoUri: Uri,
        transactionId: String,
        isOnline: Boolean = true
    ): Result<PhotoUploadResult> {
        return try {
            // Always save locally first
            val localResult = savePhotoLocally(photoUri, transactionId)
            val localPath = localResult.getOrNull()

            if (!isOnline) {
                // Offline mode - only save locally
                return if (localPath != null) {
                    Result.success(PhotoUploadResult(localPath = localPath, cloudUrl = null))
                } else {
                    Result.failure(localResult.exceptionOrNull() ?: Exception("Gagal menyimpan foto lokal"))
                }
            }

            // Online mode - try to upload to cloud
            val cloudResult = uploadPhoto(photoUri, transactionId)
            val cloudUrl = cloudResult.getOrNull()

            // Return result with both paths (local always available as backup)
            Result.success(PhotoUploadResult(localPath = localPath, cloudUrl = cloudUrl))

        } catch (e: Exception) {
            Log.e("PhotoUpload", "Error in uploadAndSavePhoto", e)
            Result.failure(e)
        }
    }

    /**
     * Delete photo from Supabase Storage
     */
    suspend fun deletePhotoFromCloud(photoUrl: String): Result<Unit> {
        return try {
            // Extract filename from URL
            val fileName = photoUrl.substringAfterLast("/")
            val bucket = supabaseClient.storage.from(bucketName)
            bucket.delete(fileName)

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("PhotoUpload", "Error deleting photo from cloud", e)
            Result.failure(e)
        }
    }

    /**
     * Delete local photo file
     */
    suspend fun deleteLocalPhoto(localPath: String): Result<Unit> {
        return try {
            val file = File(localPath)
            if (file.exists() && file.delete()) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("File tidak ditemukan atau gagal dihapus"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun uriToByteArray(uri: Uri): ByteArray? {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            inputStream?.readBytes()
        } catch (e: Exception) {
            Log.e("PhotoUpload", "Error converting URI to ByteArray", e)
            null
        }
    }

    private fun getFileExtension(uri: Uri): String {
        val contentResolver = context.contentResolver
        val mimeType = contentResolver.getType(uri)
        return when (mimeType) {
            "image/jpeg" -> "jpg"
            "image/jpg" -> "jpg"
            "image/png" -> "png"
            "image/webp" -> "webp"
            else -> "jpg" // default
        }
    }
}

/**
 * Data class untuk hasil upload foto
 */
data class PhotoUploadResult(
    val localPath: String?,
    val cloudUrl: String?
)