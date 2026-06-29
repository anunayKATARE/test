package com.lifeos.app.feature.backup.domain

interface BackupRepository {
    suspend fun exportSnapshot(): String
    suspend fun importSnapshot(json: String)
}
