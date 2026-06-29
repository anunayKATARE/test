package com.lifeos.app.feature.backup.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeos.app.feature.backup.domain.ExportBackupUseCase
import com.lifeos.app.feature.backup.domain.ImportBackupUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class BackupUiState(
    val isLoading: Boolean = false,
    val message: String? = null,
)

@HiltViewModel
class BackupViewModel @Inject constructor(
    private val exportBackupUseCase: ExportBackupUseCase,
    private val importBackupUseCase: ImportBackupUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(BackupUiState())
    val uiState: StateFlow<BackupUiState> = _uiState.asStateFlow()

    suspend fun exportJson(): String = exportBackupUseCase()

    fun restore(json: String) {
        viewModelScope.launch {
            _uiState.value = BackupUiState(isLoading = true)
            try {
                importBackupUseCase(json)
                _uiState.value = BackupUiState(message = "Backup restored")
            } catch (e: Exception) {
                _uiState.value = BackupUiState(message = "Restore failed: ${e.message}")
            }
        }
    }

    fun onExportSucceeded() {
        _uiState.value = BackupUiState(message = "Backup exported")
    }

    fun onExportFailed(reason: String?) {
        _uiState.value = BackupUiState(message = "Export failed: $reason")
    }
}
