package com.voltar.battery.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "charging_sessions",
    indices = [Index(value = ["startTime"]), Index(value = ["endTime"])]
)
data class ChargingSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val startTime: Long,
    val endTime: Long?,
    val startLevel: Int,
    val endLevel: Int?,
    val startChargeCounterMahi: Float?,
    val endChargeCounterMahi: Float?,
    val estimatedCapacityMahi: Float?,
    val maxTemperatureCelsius: Float,
    val minTemperatureCelsius: Float,
    val avgVoltageVolts: Float,
    val maxChargingCurrentMa: Float,
    val plugType: String,
    val isComplete: Boolean,
    val isValidForHealthEstimation: Boolean,
    val qualityRejectReason: String?
)
