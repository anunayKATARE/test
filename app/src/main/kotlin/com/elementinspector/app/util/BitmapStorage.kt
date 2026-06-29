package com.elementinspector.app.util

import android.content.Context
import android.graphics.Bitmap
import java.io.File
import java.io.FileOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** File-system layout for captures: filesDir/captures/<id>/{screenshot.png, detail.json},
 *  plus a single filesDir/captures/index.json with lightweight summaries for fast listing. */
object BitmapStorage {
    private const val CAPTURES_DIR = "captures"

    suspend fun saveScreenshot(context: Context, captureId: String, bitmap: Bitmap): String =
        withContext(Dispatchers.IO) {
            val dir = captureDir(context, captureId).apply { mkdirs() }
            val file = File(dir, "screenshot.png")
            FileOutputStream(file).use { out -> bitmap.compress(Bitmap.CompressFormat.PNG, 100, out) }
            file.absolutePath
        }

    fun captureDir(context: Context, captureId: String): File =
        File(context.filesDir, "$CAPTURES_DIR/$captureId")

    fun detailFile(context: Context, captureId: String): File =
        File(captureDir(context, captureId), "detail.json")

    fun indexFile(context: Context): File =
        File(context.filesDir, "$CAPTURES_DIR/index.json").also { it.parentFile?.mkdirs() }
}
