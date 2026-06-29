package com.lifeos.app.feature.backup.domain

import javax.inject.Inject

class ExportBackupUseCase @Inject constructor(
    private val repository: BackupRepository,
) {
    suspend operator fun invoke(): String = repository.exportSnapshot()
}
