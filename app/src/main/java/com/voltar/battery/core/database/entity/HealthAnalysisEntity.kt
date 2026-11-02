package com.voltar.battery.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "health_analyses",
    indices = [Index(value = ["timestamp"])]
)
data class HealthAnalysisEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long,
    val healthPercentage: Float?,
    val category: String,
    val estimatedCapacityMah: Float?,
    val designCapacityMah: Float,
    val confidenceScore: Float,
    val confidenceLabel: String,
    val milestone: String,
    val validObservationCount: Int,
    val validChargingSessionCount: Int,
    val isPreliminary: Boolean,
    val methodologyNote: String
)
