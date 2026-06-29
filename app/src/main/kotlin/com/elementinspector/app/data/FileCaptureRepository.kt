package com.elementinspector.app.data

import android.content.Context
import com.elementinspector.app.util.BitmapStorage
import com.elementinspector.domain.model.CaptureDetail
import com.elementinspector.domain.model.CaptureSummary
import com.elementinspector.domain.repository.CaptureRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * File-system backed [CaptureRepository]: each capture's screenshot and full
 * detail JSON live under filesDir/captures/<id>/, with a single index.json
 * holding lightweight summaries so the Captured tab never has to parse every
 * detail file just to render a list. Swapping this for a Room-backed
 * implementation later only requires implementing the same interface.
 */
class FileCaptureRepository(private val context: Context) : CaptureRepository {

    private val json = Json { ignoreUnknownKeys = true }
    private val mutex = Mutex()

    override suspend fun save(detail: CaptureDetail): CaptureSummary = withContext(Dispatchers.IO) {
        mutex.withLock {
            val detailFile = BitmapStorage.detailFile(context, detail.id)
            detailFile.parentFile?.mkdirs()
            detailFile.writeText(json.encodeToString(detail))

            val summary = detail.toSummary()
            val index = readIndexLocked().toMutableList()
            index.removeAll { it.id == summary.id }
            index.add(0, summary)
            writeIndexLocked(index)
            summary
        }
    }

    override suspend fun getAllSummaries(): List<CaptureSummary> = withContext(Dispatchers.IO) {
        mutex.withLock { readIndexLocked() }
    }

    override suspend fun getDetail(id: String): CaptureDetail? = withContext(Dispatchers.IO) {
        val file = BitmapStorage.detailFile(context, id)
        if (!file.exists()) return@withContext null
        runCatching { json.decodeFromString<CaptureDetail>(file.readText()) }.getOrNull()
    }

    override suspend fun delete(id: String) = withContext(Dispatchers.IO) {
        mutex.withLock {
            BitmapStorage.captureDir(context, id).deleteRecursively()
            val index = readIndexLocked().toMutableList()
            index.removeAll { it.id == id }
            writeIndexLocked(index)
        }
    }

    private fun readIndexLocked(): List<CaptureSummary> {
        val file = BitmapStorage.indexFile(context)
        if (!file.exists()) return emptyList()
        return runCatching { json.decodeFromString<List<CaptureSummary>>(file.readText()) }.getOrDefault(emptyList())
    }

    private fun writeIndexLocked(index: List<CaptureSummary>) {
        BitmapStorage.indexFile(context).writeText(json.encodeToString(index))
    }
}
