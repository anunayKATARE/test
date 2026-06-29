package com.elementinspector.app.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Loads a downsampled bitmap for a screenshot file, caching by path so the
 *  same capture doesn't get decoded from disk on every scroll. */
object ThumbnailLoader {
    private val cache = LruCache<String, Bitmap>(20)

    suspend fun load(path: String, reqWidth: Int): Bitmap? = withContext(Dispatchers.IO) {
        cache.get(path)?.let { return@withContext it }

        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(path, bounds)
        if (bounds.outWidth <= 0) return@withContext null

        var sampleSize = 1
        while (bounds.outWidth / (sampleSize * 2) >= reqWidth) sampleSize *= 2

        val options = BitmapFactory.Options().apply { inSampleSize = sampleSize }
        BitmapFactory.decodeFile(path, options)?.also { cache.put(path, it) }
    }
}
