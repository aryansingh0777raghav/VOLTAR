package com.voltar.battery.battery

import com.voltar.battery.core.model.BatteryHealthCategory
import com.voltar.battery.core.model.ChargingSession
import com.voltar.battery.core.model.CollectionMilestone
import com.voltar.battery.core.model.DeviceProfile
import com.voltar.battery.core.model.HealthAnalysis
import kotlin.math.abs
import kotlin.math.roundToInt

class BatteryHealthEngine {

    companion object {
        const val MIN_SOC_DELTA_PERCENT = 10 // Minimum 10% change required to consider a session
        const val RECOMMENDED_SOC_DELTA_PERCENT = 20 // Recommended >= 20% for reliable calculation
        const val MIN_CAPACITY_MAH = 1000f // Minimum plausible smartphone capacity
        const val MAX_CAPACITY_MAH = 12000f // Maximum plausible smartphone capacity
        const val MIN_TEMP_THRESHOLD = 10f // Celsius
        const val MAX_TEMP_THRESHOLD = 45f // Celsius
    }

    /**
     * Evaluates a completed charging session for capacity estimation.
     * Returns an updated session with calculated estimatedCapacity and validity flag.
     */
    fun evaluateChargingSession(
        session: ChargingSession,
        designCapacityMah: Float
    ): ChargingSession {
        val startLevel = session.startLevel
        val endLevel = session.endLevel ?: return session.copy(
            isValidForHealthEstimation = false,
            qualityRejectReason = "Session was interrupted before completion"
        )

        val deltaLevel = endLevel - startLevel
        if (deltaLevel < MIN_SOC_DELTA_PERCENT) {
            return session.copy(
                isValidForHealthEstimation = false,
                qualityRejectReason = "Insufficient State-of-Charge change (+$deltaLevel%). Minimum $MIN_SOC_DELTA_PERCENT% required."
            )
        }

        val startQ = session.startChargeCounterMahi
        val endQ = session.endChargeCounterMahi

        if (startQ == null || endQ == null) {
            return session.copy(
                isValidForHealthEstimation = false,
                qualityRejectReason = "Hardware charge-counter unavailable for this session."
            )
        }

        val deltaQ = endQ - startQ
        if (deltaQ <= 0) {
            return session.copy(
                isValidForHealthEstimation = false,
                qualityRejectReason = "Invalid charge counter delta ($deltaQ mAh)."
            )
        }

        // Temperature quality check
        if (session.maxTemperatureCelsius > MAX_TEMP_THRESHOLD || session.minTemperatureCelsius < MIN_TEMP_THRESHOLD) {
            return session.copy(
                isValidForHealthEstimation = false,
                qualityRejectReason = "Temperature anomaly (${session.minTemperatureCelsius}°C - ${session.maxTemperatureCelsius}°C) during session."
            )
        }

        // Estimated capacity = Delta Q / Delta SoC
        val deltaSocRatio = deltaLevel / 100f
        val estimatedCapacity = deltaQ / deltaSocRatio

        // Outlier sanity check
        val minAcceptable = designCapacityMah * 0.40f
        val maxAcceptable = designCapacityMah * 1.35f

        if (estimatedCapacity < minAcceptable || estimatedCapacity > maxAcceptable ||
            estimatedCapacity < MIN_CAPACITY_MAH || estimatedCapacity > MAX_CAPACITY_MAH
        ) {
            return session.copy(
                estimatedCapacityMahi = estimatedCapacity,
                isValidForHealthEstimation = false,
                qualityRejectReason = "Calculated capacity (${estimatedCapacity.roundToInt()} mAh) exceeds plausible limits for $designCapacityMah mAh design."
            )
        }

        return session.copy(
            estimatedCapacityMahi = estimatedCapacity,
            isValidForHealthEstimation = true,
            qualityRejectReason = null
        )
    }

    /**
     * Computes the holistic HealthAnalysis from all available historical data.
     */
    fun computeHealthAnalysis(
        deviceProfile: DeviceProfile,
        validSessions: List<ChargingSession>,
        totalObservationsCount: Int,
        firstLaunchTimestamp: Long
    ): HealthAnalysis {
        val designCapacity = deviceProfile.designCapacityMah
        val now = System.currentTimeMillis()
        val elapsedDays = ((now - firstLaunchTimestamp) / (1000L * 60 * 60 * 24)).toInt().coerceAtLeast(0)

        // 1. Direct hardware reporting (e.g. sysfs charge_full) if available
        val hwCapacity = deviceProfile.hardwareFullChargeCapacityMah
        if (hwCapacity != null && hwCapacity > 1000f && designCapacity > 1000f) {
            val pct = ((hwCapacity / designCapacity) * 100f).coerceIn(40f, 105f)
            val roundedPct = (pct * 10f).roundToInt() / 10f
            return HealthAnalysis(
                timestamp = now,
                healthPercentage = roundedPct,
                category = BatteryHealthCategory.fromPercentage(roundedPct),
                estimatedCapacityMah = hwCapacity,
                designCapacityMah = designCapacity,
                confidenceScore = 0.95f,
                confidenceLabel = "Hardware Verified",
                milestone = CollectionMilestone.FOURTEEN_DAYS,
                validObservationCount = totalObservationsCount,
                validChargingSessionCount = validSessions.size,
                isPreliminary = false,
                methodologyNote = "Direct hardware fuel-gauge reading verified by system interfaces."
            )
        }

        // 2. Progressive charging session capacity estimation
        val validCapacities = validSessions.mapNotNull { it.estimatedCapacityMahi }
            .filter { it in MIN_CAPACITY_MAH..MAX_CAPACITY_MAH }

        val validSessionCount = validCapacities.size
        val milestone = CollectionMilestone.currentMilestone(elapsedDays, validSessionCount)

        if (validCapacities.isEmpty()) {
            return HealthAnalysis(
                timestamp = now,
                healthPercentage = null,
                category = BatteryHealthCategory.INSUFFICIENT_DATA,
                estimatedCapacityMah = null,
                designCapacityMah = designCapacity,
                confidenceScore = 0.05f,
                confidenceLabel = "Awaiting Valid Charging Session",
                milestone = CollectionMilestone.INITIAL_24H,
                validObservationCount = totalObservationsCount,
                validChargingSessionCount = 0,
                isPreliminary = true,
                methodologyNote = "Real battery health requires at least one complete charging session of >= 15% to measure charge-counter delta."
            )
        }

        // Calculate robust median capacity
        val medianCapacity = calculateMedian(validCapacities)
        val rawHealthPct = (medianCapacity / designCapacity) * 100f
        val boundedHealthPct = rawHealthPct.coerceIn(50f, 102f)
        val roundedPct = (boundedHealthPct * 10f).roundToInt() / 10f

        val (confidenceScore, confidenceLabel, isPreliminary, methodology) = when {
            elapsedDays >= 14 && validSessionCount >= 6 -> {
                val variance = calculateStandardDeviation(validCapacities, medianCapacity)
                val stabilityBonus = if (variance < (medianCapacity * 0.05f)) 0.05f else 0.0f
                Quad(
                    0.88f + stabilityBonus,
                    "14-Day High Confidence",
                    false,
                    "Calculated from $validSessionCount consistent charge-counter cycles over $elapsedDays days with outlier rejection."
                )
            }
            elapsedDays >= 7 && validSessionCount >= 3 -> {
                Quad(
                    0.72f,
                    "7-Day Validated",
                    false,
                    "Calculated from $validSessionCount charging sessions over $elapsedDays days."
                )
            }
            else -> {
                Quad(
                    0.45f,
                    "24-Hour Preliminary",
                    true,
                    "Preliminary estimate based on $validSessionCount session(s). Confidence will increase with 7-14 days of typical usage."
                )
            }
        }

        return HealthAnalysis(
            timestamp = now,
            healthPercentage = roundedPct,
            category = BatteryHealthCategory.fromPercentage(roundedPct),
            estimatedCapacityMah = (medianCapacity * 10f).roundToInt() / 10f,
            designCapacityMah = designCapacity,
            confidenceScore = confidenceScore,
            confidenceLabel = confidenceLabel,
            milestone = milestone,
            validObservationCount = totalObservationsCount,
            validChargingSessionCount = validSessionCount,
            isPreliminary = isPreliminary,
            methodologyNote = methodology
        )
    }

    private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

    private fun calculateMedian(values: List<Float>): Float {
        val sorted = values.sorted()
        val size = sorted.size
        return if (size % 2 == 1) {
            sorted[size / 2]
        } else {
            (sorted[size / 2 - 1] + sorted[size / 2]) / 2f
        }
    }

    private fun calculateStandardDeviation(values: List<Float>, mean: Float): Float {
        if (values.size <= 1) return 0f
        val sumSquaredDiffs = values.sumOf { (it - mean) * (it - mean).toDouble() }
        return kotlin.math.sqrt(sumSquaredDiffs / values.size).toFloat()
    }
}
