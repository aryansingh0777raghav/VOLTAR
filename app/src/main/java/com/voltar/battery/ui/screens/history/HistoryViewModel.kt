package com.voltar.battery.ui.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.voltar.battery.core.model.BatteryObservation
import com.voltar.battery.core.model.ChargingSession
import com.voltar.battery.core.model.HealthAnalysis
import com.voltar.battery.core.repository.BatteryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.update

enum class HistoryFilter(val label: String) {
    ALL("All"),
    CHARGING("Charging"),
    HEALTH("Health"),
    LOGS("Logs")
}

sealed class HistoryItem(val timestamp: Long) {
    data class Charging(val session: ChargingSession) : HistoryItem(session.startTime)
    data class Health(val analysis: HealthAnalysis) : HistoryItem(analysis.timestamp)
    data class Observation(val obs: BatteryObservation) : HistoryItem(obs.timestamp)
}

data class HistoryUiState(
    val filter: HistoryFilter = HistoryFilter.ALL,
    val items: List<HistoryItem> = emptyList(),
    val selectedItem: HistoryItem? = null
)

class HistoryViewModel(
    private val batteryRepository: BatteryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    private var allChargingList: List<ChargingSession> = emptyList()
    private var allHealthList: List<HealthAnalysis> = emptyList()
    private var allObsList: List<BatteryObservation> = emptyList()

    init {
        combine(
            batteryRepository.allChargingSessionsFlow,
            batteryRepository.allHealthAnalysesFlow,
            batteryRepository.getRecentObservations(50)
        ) { sessions, analyses, obs ->
            allChargingList = sessions
            allHealthList = analyses
            allObsList = obs
            applyFilter(_uiState.value.filter)
        }.launchIn(viewModelScope)
    }

    fun setFilter(filter: HistoryFilter) {
        _uiState.update { it.copy(filter = filter) }
        applyFilter(filter)
    }

    fun selectItem(item: HistoryItem?) {
        _uiState.update { it.copy(selectedItem = item) }
    }

    private fun applyFilter(filter: HistoryFilter) {
        val combined = when (filter) {
            HistoryFilter.ALL -> {
                val list = mutableListOf<HistoryItem>()
                list.addAll(allChargingList.map { HistoryItem.Charging(it) })
                list.addAll(allHealthList.map { HistoryItem.Health(it) })
                list.addAll(allObsList.take(20).map { HistoryItem.Observation(it) })
                list.sortedByDescending { it.timestamp }
            }
            HistoryFilter.CHARGING -> allChargingList.map { HistoryItem.Charging(it) }.sortedByDescending { it.timestamp }
            HistoryFilter.HEALTH -> allHealthList.map { HistoryItem.Health(it) }.sortedByDescending { it.timestamp }
            HistoryFilter.LOGS -> allObsList.map { HistoryItem.Observation(it) }.sortedByDescending { it.timestamp }
        }
        _uiState.update { it.copy(items = combined) }
    }
}

class HistoryViewModelFactory(
    private val batteryRepository: BatteryRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return HistoryViewModel(batteryRepository) as T
    }
}
