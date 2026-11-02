package com.voltar.battery.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.voltar.battery.core.model.BatteryHealthCategory
import com.voltar.battery.core.model.BatteryObservation
import com.voltar.battery.core.model.CollectionMilestone
import com.voltar.battery.core.model.DeviceProfile
import com.voltar.battery.core.model.HealthAnalysis
import com.voltar.battery.core.repository.BatteryRepository
import com.voltar.battery.ui.components.ChartPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val liveObservation: BatteryObservation? = null,
    val healthAnalysis: HealthAnalysis? = null,
    val deviceProfile: DeviceProfile? = null,
    val selectedTrendRange: String = "24h",
    val trendChartPoints: List<ChartPoint> = emptyList(),
    val isCreatorSheetVisible: Boolean = false,
    val isInfoDialogVisible: Boolean = false
)

class HomeViewModel(
    private val batteryRepository: BatteryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            val profile = batteryRepository.getDeviceProfile()
            _uiState.update { it.copy(deviceProfile = profile) }
            batteryRepository.recalculateHealth()
        }

        combine(
            batteryRepository.liveBatteryFlow,
            batteryRepository.latestHealthAnalysisFlow,
            batteryRepository.allHealthAnalysesFlow
        ) { liveObs, health, analyses ->
            // Generate points for the health trend chart
            val points = if (analyses.isNotEmpty()) {
                analyses.mapNotNull { analysis ->
                    analysis.healthPercentage?.let { pct ->
                        ChartPoint(
                            timestamp = analysis.timestamp,
                            value = pct
                        )
                    }
                }
            } else if (health?.healthPercentage != null) {
                listOf(
                    ChartPoint(System.currentTimeMillis() - 3600000L, health.healthPercentage),
                    ChartPoint(System.currentTimeMillis(), health.healthPercentage)
                )
            } else {
                emptyList()
            }

            _uiState.update { current ->
                current.copy(
                    liveObservation = liveObs,
                    healthAnalysis = health,
                    trendChartPoints = points
                )
            }
        }.launchIn(viewModelScope)
    }

    fun setSelectedTrendRange(range: String) {
        _uiState.update { it.copy(selectedTrendRange = range) }
    }

    fun showCreatorSheet(show: Boolean) {
        _uiState.update { it.copy(isCreatorSheetVisible = show) }
    }

    fun showInfoDialog(show: Boolean) {
        _uiState.update { it.copy(isInfoDialogVisible = show) }
    }

    fun triggerManualObservation() {
        val current = _uiState.value.liveObservation ?: return
        viewModelScope.launch {
            batteryRepository.recordObservation(current)
            batteryRepository.recalculateHealth()
        }
    }
}

class HomeViewModelFactory(
    private val batteryRepository: BatteryRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return HomeViewModel(batteryRepository) as T
    }
}
