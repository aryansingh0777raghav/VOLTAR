package com.voltar.battery.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "battery_observations",
    indices = [Index(value = ["timestamp"])]
)
data class BatteryObservationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long,
    val batteryLevel: Int,
    val temperatureCelsius: Float,
    val voltageVolts: Float,
    val currentMilliAmperes: Float?,
    val chargeCounterMilliAmpHours: Float?,
    val isCharging: Boolean,
    val plugType: String,
    val statusText: String,
    val isHardwareReported: Boolean,
    val isValid: Boolean
)
