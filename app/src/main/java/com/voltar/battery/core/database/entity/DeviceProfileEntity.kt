package com.voltar.battery.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "device_profile")
data class DeviceProfileEntity(
    @PrimaryKey
    val id: Int = 1,
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
    val hardwareCycleCount: Int?,
    val hardwareFullChargeCapacityMah: Float?
)
