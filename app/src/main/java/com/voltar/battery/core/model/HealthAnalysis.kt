package com.voltar.battery.core.model

data class HealthAnalysis(
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val healthPercentage: Float?, // null if insufficient data to estimate
    val category: BatteryHealthCategory,
    val estimatedCapacityMah: Float?, // e.g. 4650 mAh
    val designCapacityMah: Float, // e.g. 5000 mAh
    val confidenceScore: Float, // 0.0 to 1.0
    val confidenceLabel: String, // e.g. "Low (Preliminary)", "Medium (7-Day)", "High (14-Day)"
    val milestone: CollectionMilestone,
    val validObservationCount: Int,
    val validChargingSessionCount: Int,
    val isPreliminary: Boolean,
    val methodologyNote: String
)
