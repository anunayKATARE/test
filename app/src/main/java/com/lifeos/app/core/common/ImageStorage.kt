package com.lifeos.app.core.common

import android.content.Context
import android.net.Uri
import java.io.File

/**
 * The Android Photo Picker only grants transient read access to the returned content:// Uri, so
 * the bytes must be copied into app-private storage immediately on selection to persist them.
 */
object ImageStorage {
    private const val DIRECTORY_NAME = "inspiration_images"

    fun copyToInternalStorage(context: Context, sourceUri: Uri): String {
        val targetDir = File(context.filesDir, DIRECTORY_NAME).apply { mkdirs() }
        val targetFile = File(targetDir, "${IdGenerator.newId()}.jpg")
        context.contentResolver.openInputStream(sourceUri)?.use { input ->
            targetFile.outputStream().use { output -> input.copyTo(output) }
        }
        return targetFile.absolutePath
    }
}
