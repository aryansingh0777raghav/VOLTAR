package com.voltar.battery.core.model

data class ChargingSession(
    val id: Long = 0,
    val startTime: Long,
    val endTime: Long? = null,
    val startLevel: Int,
    val endLevel: Int? = null,
    val startChargeCounterMahi: Float? = null,
    val endChargeCounterMahi: Float? = null,
    val estimatedCapacityMahi: Float? = null,
    val maxTemperatureCelsius: Float = 0f,
    val minTemperatureCelsius: Float = 0f,
    val avgVoltageVolts: Float = 0f,
    val maxChargingCurrentMa: Float = 0f,
    val plugType: String = "AC",
    val isComplete: Boolean = false,
    val isValidForHealthEstimation: Boolean = false,
    val qualityRejectReason: String? = null
) {
    val durationMillis: Long
        get() = (endTime ?: System.currentTimeMillis()) - startTime

    val percentageGain: Int
        get() = (endLevel ?: startLevel) - startLevel

    val energyGainedMah: Float?
        get() {
            if (endChargeCounterMahi != null && startChargeCounterMahi != null) {
                val diff = endChargeCounterMahi - startChargeCounterMahi
                if (diff > 0) return diff
            }
            return null
        }
}
