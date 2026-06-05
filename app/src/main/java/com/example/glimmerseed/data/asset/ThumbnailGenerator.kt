package com.example.glimmerseed.data.asset

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream

class ThumbnailGenerator(private val context: Context) {

    companion object {
        private const val THUMBNAIL_SIZE = 256
        private const val QUALITY = 80
    }

    suspend fun generateThumbnail(sourcePath: String, assetId: String): String? {
        return withContext(Dispatchers.IO) {
            try {
                val thumbnailDir = getThumbnailDir()
                if (!thumbnailDir.exists()) {
                    thumbnailDir.mkdirs()
                }

                val thumbnailPath = File(thumbnailDir, "$assetId.webp").absolutePath

                val options = BitmapFactory.Options().apply {
                    inJustDecodeBounds = true
                }
                BitmapFactory.decodeFile(sourcePath, options)

                val scale = calculateInSampleSize(options, THUMBNAIL_SIZE, THUMBNAIL_SIZE)

                val bitmapOptions = BitmapFactory.Options().apply {
                    inSampleSize = scale
                    inPreferredConfig = Bitmap.Config.ARGB_8888
                }

                val originalBitmap = BitmapFactory.decodeFile(sourcePath, bitmapOptions)
                if (originalBitmap == null) {
                    Timber.w("Failed to decode bitmap from $sourcePath")
                    return@withContext null
                }

                val scaledBitmap = scaleBitmap(originalBitmap, THUMBNAIL_SIZE, THUMBNAIL_SIZE)
                originalBitmap.recycle()

                val outputStream = FileOutputStream(thumbnailPath)
                scaledBitmap.compress(Bitmap.CompressFormat.WEBP, QUALITY, outputStream)
                outputStream.close()
                scaledBitmap.recycle()

                Timber.d("Thumbnail generated: $thumbnailPath")
                thumbnailPath
            } catch (e: Exception) {
                Timber.e(e, "Failed to generate thumbnail for $sourcePath")
                null
            }
        }
    }

    suspend fun generateThumbnailFromUri(uri: Uri, assetId: String): String? {
        return withContext(Dispatchers.IO) {
            try {
                val thumbnailDir = getThumbnailDir()
                if (!thumbnailDir.exists()) {
                    thumbnailDir.mkdirs()
                }

                val thumbnailPath = File(thumbnailDir, "$assetId.webp").absolutePath

                val inputStream = context.contentResolver.openInputStream(uri)
                if (inputStream == null) {
                    Timber.w("Failed to open input stream for $uri")
                    return@withContext null
                }

                val options = BitmapFactory.Options().apply {
                    inJustDecodeBounds = true
                }
                BitmapFactory.decodeStream(inputStream, null, options)
                inputStream.close()

                val scale = calculateInSampleSize(options, THUMBNAIL_SIZE, THUMBNAIL_SIZE)

                val inputStream2 = context.contentResolver.openInputStream(uri)
                val bitmapOptions = BitmapFactory.Options().apply {
                    inSampleSize = scale
                    inPreferredConfig = Bitmap.Config.ARGB_8888
                }

                val originalBitmap = BitmapFactory.decodeStream(inputStream2, null, bitmapOptions)
                inputStream2?.close()

                if (originalBitmap == null) {
                    Timber.w("Failed to decode bitmap from $uri")
                    return@withContext null
                }

                val scaledBitmap = scaleBitmap(originalBitmap, THUMBNAIL_SIZE, THUMBNAIL_SIZE)
                originalBitmap.recycle()

                val outputStream = FileOutputStream(thumbnailPath)
                scaledBitmap.compress(Bitmap.CompressFormat.WEBP, QUALITY, outputStream)
                outputStream.close()
                scaledBitmap.recycle()

                Timber.d("Thumbnail generated from URI: $thumbnailPath")
                thumbnailPath
            } catch (e: Exception) {
                Timber.e(e, "Failed to generate thumbnail for $uri")
                null
            }
        }
    }

    suspend fun loadThumbnail(assetId: String): Bitmap? {
        return withContext(Dispatchers.IO) {
            try {
                val thumbnailPath = File(getThumbnailDir(), "$assetId.webp").absolutePath
                if (File(thumbnailPath).exists()) {
                    BitmapFactory.decodeFile(thumbnailPath)
                } else {
                    null
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to load thumbnail for assetId: $assetId")
                null
            }
        }
    }

    suspend fun deleteThumbnail(assetId: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val thumbnailFile = File(getThumbnailDir(), "$assetId.webp")
                thumbnailFile.delete()
            } catch (e: Exception) {
                Timber.e(e, "Failed to delete thumbnail for assetId: $assetId")
                false
            }
        }
    }

    private fun getThumbnailDir(): File {
        return File(context.filesDir, "assets/thumbnails")
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2

            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    private fun scaleBitmap(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        val scale = minOf(maxWidth.toFloat() / width, maxHeight.toFloat() / height)

        if (scale >= 1f) {
            return bitmap
        }

        val matrix = Matrix().apply {
            postScale(scale, scale)
        }

        return Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true)
    }
}