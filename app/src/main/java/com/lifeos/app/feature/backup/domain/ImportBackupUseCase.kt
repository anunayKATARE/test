package com.lifeos.app.feature.backup.domain

import javax.inject.Inject

class ImportBackupUseCase @Inject constructor(
    private val repository: BackupRepository,
) {
    suspend operator fun invoke(json: String) = repository.importSnapshot(json)
}
