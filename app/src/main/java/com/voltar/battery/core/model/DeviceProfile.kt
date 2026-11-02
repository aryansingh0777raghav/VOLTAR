package com.voltar.battery.core.model

data class DeviceProfile(
    val manufacturer: String,
    val brand: String,
    val model: String,
    val androidVersion: String,
    val apiLevel: Int,
    val designCapacityMah: Float,
    val isDesignCapacityVerified: Boolean,
    val isCustomDesignCapacity: Boolean,
    val supportsChargeCounter: Boolean,
    val supportsInstantCurrent: Boolean,
    val supportsAverageCurrent: Boolean,
    val supportsEnergyCounter: Boolean,
    val supportsHardwareCycleCount: Boolean,
    val hardwareCycleCount: Int? = null,
    val hardwareFullChargeCapacityMah: Float? = null
)
