package com.voltar.battery.ui.screens.charging

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.voltar.battery.core.model.BatteryObservation
import com.voltar.battery.core.model.ChargingSession
import com.voltar.battery.core.repository.BatteryRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.abs

data class ChargingLabUiState(
    val liveObservation: BatteryObservation? = null,
    val activeSession: ChargingSession? = null,
    val chargingPowerWatts: Float? = null,
    val isCharging: Boolean = false,
    val sessionDurationFormatted: String = "00:00",
    val percentageGained: Int = 0,
    val energyGainedMah: Float? = null
)

class ChargingLabViewModel(
    private val batteryRepository: BatteryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChargingLabUiState())
    val uiState: StateFlow<ChargingLabUiState> = _uiState.asStateFlow()

    init {
        combine(
            batteryRepository.liveBatteryFlow,
            batteryRepository.activeChargingSessionFlow
        ) { live, session ->
            val isCharging = live.isCharging
            val currentAmps = live.currentMilliAmperes?.let { abs(it) / 1000f }
            val voltageVolts = live.voltageVolts

            // Power in Watts = V * I (only calculate when current is valid and charging)
            val powerWatts = if (isCharging && currentAmps != null && currentAmps > 0.05f && voltageVolts > 2.5f) {
                voltageVolts * currentAmps
            } else {
                null
            }

            val gain = if (session != null) {
                (live.batteryLevel - session.startLevel).coerceAtLeast(0)
            } else 0

            val energy = if (session?.startChargeCounterMahi != null && live.chargeCounterMilliAmpHours != null) {
                (live.chargeCounterMilliAmpHours - session.startChargeCounterMahi).coerceAtLeast(0f)
            } else null

            val durationMillis = session?.durationMillis ?: 0L
            val minutes = (durationMillis / (1000 * 60)) % 60
            val hours = (durationMillis / (1000 * 60 * 60))
            val durationText = String.format(java.util.Locale.US, "%02d:%02d", hours, minutes)

            _uiState.update {
                it.copy(
                    liveObservation = live,
                    activeSession = session,
                    chargingPowerWatts = powerWatts,
                    isCharging = isCharging,
                    sessionDurationFormatted = durationText,
                    percentageGained = gain,
                    energyGainedMah = energy
                )
            }
        }.launchIn(viewModelScope)

        // Timer for session duration ticking
        viewModelScope.launch {
            while (true) {
                delay(1000)
                val session = _uiState.value.activeSession
                if (session != null && _uiState.value.isCharging) {
                    val durationMillis = System.currentTimeMillis() - session.startTime
                    val minutes = (durationMillis / (1000 * 60)) % 60
                    val hours = (durationMillis / (1000 * 60 * 60))
                    val durationText = String.format(java.util.Locale.US, "%02d:%02d", hours, minutes)
                    _uiState.update { it.copy(sessionDurationFormatted = durationText) }
                }
            }
        }
    }
}

class ChargingLabViewModelFactory(
    private val batteryRepository: BatteryRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ChargingLabViewModel(batteryRepository) as T
    }
}
