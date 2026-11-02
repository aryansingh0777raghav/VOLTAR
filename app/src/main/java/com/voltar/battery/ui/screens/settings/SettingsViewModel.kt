package com.voltar.battery.ui.screens.settings

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.voltar.battery.core.datastore.ThemeMode
import com.voltar.battery.core.datastore.UserPreferences
import com.voltar.battery.core.datastore.UserPreferencesRepository
import com.voltar.battery.core.model.DeviceProfile
import com.voltar.battery.core.repository.BatteryRepository
import com.voltar.battery.monitoring.BatteryMonitoringScheduler
import com.voltar.battery.widget.VoltarWidgetUpdater
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

data class SettingsUiState(
    val userPreferences: UserPreferences = UserPreferences(
        themeMode = ThemeMode.LIGHT,
        isMonitoringEnabled = true,
        collectionIntervalMinutes = 15,
        customDesignCapacityMah = null,
        firstLaunchTimestamp = 0L,
        isOnboardingCompleted = true,
        widgetGlassOpacity = 75
    ),
    val deviceProfile: DeviceProfile? = null,
    val isClearHistoryDialogVisible: Boolean = false,
    val isResetAnalysisDialogVisible: Boolean = false,
    val isCustomCapacityDialogVisible: Boolean = false,
    val isCreatorSheetVisible: Boolean = false,
    val exportStatusMessage: String? = null
)

class SettingsViewModel(
    private val preferencesRepository: UserPreferencesRepository,
    private val batteryRepository: BatteryRepository,
    private val monitoringScheduler: BatteryMonitoringScheduler
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        preferencesRepository.userPreferencesFlow.onEach { prefs ->
            _uiState.update { it.copy(userPreferences = prefs) }
        }.launchIn(viewModelScope)

        viewModelScope.launch {
            val profile = batteryRepository.getDeviceProfile()
            _uiState.update { it.copy(deviceProfile = profile) }
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            preferencesRepository.setThemeMode(mode)
        }
    }

    fun setMonitoringEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setMonitoringEnabled(enabled)
            if (enabled) {
                val interval = _uiState.value.userPreferences.collectionIntervalMinutes
                monitoringScheduler.scheduleMonitoring(interval)
            } else {
                monitoringScheduler.cancelMonitoring()
            }
        }
    }

    fun setCollectionInterval(minutes: Int) {
        viewModelScope.launch {
            preferencesRepository.setCollectionInterval(minutes)
            if (_uiState.value.userPreferences.isMonitoringEnabled) {
                monitoringScheduler.scheduleMonitoring(minutes)
            }
        }
    }

    fun setCustomDesignCapacity(capacityMah: Float?) {
        viewModelScope.launch {
            preferencesRepository.setCustomDesignCapacity(capacityMah)
            val updatedProfile = batteryRepository.getDeviceProfile()
            _uiState.update { it.copy(deviceProfile = updatedProfile) }
            batteryRepository.recalculateHealth()
        }
    }

    fun setWidgetGlassOpacity(context: Context, opacity: Int) {
        viewModelScope.launch {
            preferencesRepository.setWidgetGlassOpacity(opacity)
            VoltarWidgetUpdater.updateAllWidgets(context)
        }
    }

    fun showClearHistoryDialog(show: Boolean) {
        _uiState.update { it.copy(isClearHistoryDialogVisible = show) }
    }

    fun showResetAnalysisDialog(show: Boolean) {
        _uiState.update { it.copy(isResetAnalysisDialogVisible = show) }
    }

    fun showCustomCapacityDialog(show: Boolean) {
        _uiState.update { it.copy(isCustomCapacityDialogVisible = show) }
    }

    fun showCreatorSheet(show: Boolean) {
        _uiState.update { it.copy(isCreatorSheetVisible = show) }
    }

    fun clearExportStatus() {
        _uiState.update { it.copy(exportStatusMessage = null) }
    }

    fun clearAllData() {
        viewModelScope.launch {
            batteryRepository.clearAllData()
            _uiState.update {
                it.copy(
                    isClearHistoryDialogVisible = false,
                    exportStatusMessage = "All battery logs and history successfully cleared."
                )
            }
        }
    }

    fun resetAnalysisOnly() {
        viewModelScope.launch {
            batteryRepository.resetHealthAnalysisOnly()
            _uiState.update {
                it.copy(
                    isResetAnalysisDialogVisible = false,
                    exportStatusMessage = "Diagnostic health model reset and recalculated."
                )
            }
        }
    }

    fun exportData(context: Context, asCsv: Boolean) {
        viewModelScope.launch {
            try {
                val content = if (asCsv) {
                    batteryRepository.exportDataAsCsv()
                } else {
                    batteryRepository.exportDataAsJson()
                }
                val filename = if (asCsv) "voltar_battery_logs.csv" else "voltar_diagnostics_export.json"
                val mimeType = if (asCsv) "text/csv" else "application/json"

                val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
                val file = File(exportDir, filename).apply { writeText(content) }
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )

                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                    type = mimeType
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_SUBJECT, "VOLTAR Diagnostic Export: $filename")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                val chooser = Intent.createChooser(sendIntent, "Share VOLTAR Export").apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(chooser)

                _uiState.update { it.copy(exportStatusMessage = "Export ready: $filename") }
            } catch (e: Exception) {
                _uiState.update { it.copy(exportStatusMessage = "Export failed: ${e.message}") }
            }
        }
    }
}

class SettingsViewModelFactory(
    private val preferencesRepository: UserPreferencesRepository,
    private val batteryRepository: BatteryRepository,
    private val monitoringScheduler: BatteryMonitoringScheduler
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            return SettingsViewModel(preferencesRepository, batteryRepository, monitoringScheduler) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
