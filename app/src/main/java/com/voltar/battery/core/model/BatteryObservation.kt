package com.voltar.battery.core.model

data class BatteryObservation(
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val batteryLevel: Int, // 0 - 100 %
    val temperatureCelsius: Float, // e.g. 32.5 °C
    val voltageVolts: Float, // e.g. 4.18 V
    val currentMilliAmperes: Float?, // positive when charging, negative when discharging (or null if unavailable)
    val chargeCounterMilliAmpHours: Float?, // current available charge counter in mAh
    val isCharging: Boolean,
    val plugType: String, // AC, USB, Wireless, None
    val statusText: String, // Charging, Discharging, Full, Not Charging
    val isHardwareReported: Boolean = true,
    val isValid: Boolean = true
)
