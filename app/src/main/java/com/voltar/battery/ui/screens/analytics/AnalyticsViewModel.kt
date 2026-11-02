package com.voltar.battery.ui.screens.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.voltar.battery.core.model.BatteryObservation
import com.voltar.battery.core.model.ChargingSession
import com.voltar.battery.core.model.HealthAnalysis
import com.voltar.battery.core.repository.BatteryRepository
import com.voltar.battery.ui.components.ChartPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.update

enum class AnalyticsTab(val label: String) {
    HEALTH("Health"),
    TEMP("Temp"),
    VOLTAGE("Voltage"),
    CAPACITY("Capacity")
}

data class AnalyticsUiState(
    val selectedTab: AnalyticsTab = AnalyticsTab.HEALTH,
    val observations: List<BatteryObservation> = emptyList(),
    val healthAnalyses: List<HealthAnalysis> = emptyList(),
    val completedSessions: List<ChargingSession> = emptyList(),
    val chartPoints: List<ChartPoint> = emptyList(),
    val averageMetric: String = "--",
    val minMetric: String = "--",
    val maxMetric: String = "--",
    val validObservationCount: Int = 0,
    val validSessionCount: Int = 0,
    val confidenceLabel: String = "Collecting Data"
)

class AnalyticsViewModel(
    private val batteryRepository: BatteryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AnalyticsUiState())
    val uiState: StateFlow<AnalyticsUiState> = _uiState.asStateFlow()

    init {
        combine(
            batteryRepository.getRecentObservations(100),
            batteryRepository.allHealthAnalysesFlow,
            batteryRepository.completedChargingSessionsFlow
        ) { observations, analyses, sessions ->
            val validObsCount = observations.count { it.isValid }
            val validSessions = sessions.filter { it.isValidForHealthEstimation }

            _uiState.update { current ->
                current.copy(
                    observations = observations,
                    healthAnalyses = analyses,
                    completedSessions = sessions,
                    validObservationCount = validObsCount,
                    validSessionCount = validSessions.size,
                    confidenceLabel = analyses.lastOrNull()?.confidenceLabel ?: "Initial Baseline"
                )
            }
            updateChartForTab(_uiState.value.selectedTab)
        }.launchIn(viewModelScope)
    }

    fun selectTab(tab: AnalyticsTab) {
        _uiState.update { it.copy(selectedTab = tab) }
        updateChartForTab(tab)
    }

    private fun updateChartForTab(tab: AnalyticsTab) {
        val state = _uiState.value
        when (tab) {
            AnalyticsTab.HEALTH -> {
                val points = state.healthAnalyses.mapNotNull {
                    it.healthPercentage?.let { pct -> ChartPoint(it.timestamp, pct) }
                }
                val vals = points.map { it.value }
                val avg = if (vals.isNotEmpty()) "${(vals.average()).toInt()}%" else "--%"
                val min = if (vals.isNotEmpty()) "${vals.minOrNull()?.toInt()}%" else "--%"
                val max = if (vals.isNotEmpty()) "${vals.maxOrNull()?.toInt()}%" else "--%"
                _uiState.update { it.copy(chartPoints = points, averageMetric = avg, minMetric = min, maxMetric = max) }
            }
            AnalyticsTab.TEMP -> {
                val points = state.observations.reversed().map {
                    ChartPoint(it.timestamp, it.temperatureCelsius)
                }
                val vals = points.map { it.value }
                val avg = if (vals.isNotEmpty()) String.format(java.util.Locale.US, "%.1f°C", vals.average()) else "--°C"
                val min = if (vals.isNotEmpty()) "${vals.minOrNull()?.toInt()}°C" else "--°C"
                val max = if (vals.isNotEmpty()) "${vals.maxOrNull()?.toInt()}°C" else "--°C"
                _uiState.update { it.copy(chartPoints = points, averageMetric = avg, minMetric = min, maxMetric = max) }
            }
            AnalyticsTab.VOLTAGE -> {
                val points = state.observations.reversed().map {
                    ChartPoint(it.timestamp, it.voltageVolts)
                }
                val vals = points.map { it.value }
                val avg = if (vals.isNotEmpty()) String.format(java.util.Locale.US, "%.2f V", vals.average()) else "-- V"
                val min = if (vals.isNotEmpty()) String.format(java.util.Locale.US, "%.2f V", vals.minOrNull() ?: 0f) else "-- V"
                val max = if (vals.isNotEmpty()) String.format(java.util.Locale.US, "%.2f V", vals.maxOrNull() ?: 0f) else "-- V"
                _uiState.update { it.copy(chartPoints = points, averageMetric = avg, minMetric = min, maxMetric = max) }
            }
            AnalyticsTab.CAPACITY -> {
                val points = state.completedSessions.mapNotNull { session ->
                    session.estimatedCapacityMahi?.let { cap ->
                        ChartPoint(session.startTime, cap)
                    }
                }
                val vals = points.map { it.value }
                val avg = if (vals.isNotEmpty()) "${(vals.average()).toInt()} mAh" else "-- mAh"
                val min = if (vals.isNotEmpty()) "${vals.minOrNull()?.toInt()} mAh" else "-- mAh"
                val max = if (vals.isNotEmpty()) "${vals.maxOrNull()?.toInt()} mAh" else "-- mAh"
                _uiState.update { it.copy(chartPoints = points, averageMetric = avg, minMetric = min, maxMetric = max) }
            }
        }
    }
}

class AnalyticsViewModelFactory(
    private val batteryRepository: BatteryRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AnalyticsViewModel(batteryRepository) as T
    }
}
