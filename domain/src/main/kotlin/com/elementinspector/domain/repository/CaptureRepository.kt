package com.elementinspector.domain.repository

import com.elementinspector.domain.model.CaptureDetail
import com.elementinspector.domain.model.CaptureSummary

/**
 * Persistence boundary for captures. The app module ships a file-based
 * implementation today; this interface lets that be swapped for, say, a
 * Room-backed implementation later without changing any UI or service code.
 */
interface CaptureRepository {
    suspend fun save(detail: CaptureDetail): CaptureSummary
    suspend fun getAllSummaries(): List<CaptureSummary>
    suspend fun getDetail(id: String): CaptureDetail?
    suspend fun delete(id: String)
}
