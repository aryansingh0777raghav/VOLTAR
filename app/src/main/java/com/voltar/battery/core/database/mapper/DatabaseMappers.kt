package com.voltar.battery.core.database.mapper

import com.voltar.battery.core.database.entity.BatteryObservationEntity
import com.voltar.battery.core.database.entity.ChargingSessionEntity
import com.voltar.battery.core.database.entity.DeviceProfileEntity
import com.voltar.battery.core.database.entity.HealthAnalysisEntity
import com.voltar.battery.core.model.BatteryHealthCategory
import com.voltar.battery.core.model.BatteryObservation
import com.voltar.battery.core.model.ChargingSession
import com.voltar.battery.core.model.CollectionMilestone
import com.voltar.battery.core.model.DeviceProfile
import com.voltar.battery.core.model.HealthAnalysis

fun BatteryObservationEntity.toDomain(): BatteryObservation {
    return BatteryObservation(
        id = id,
        timestamp = timestamp,
        batteryLevel = batteryLevel,
        temperatureCelsius = temperatureCelsius,
        voltageVolts = voltageVolts,
        currentMilliAmperes = currentMilliAmperes,
        chargeCounterMilliAmpHours = chargeCounterMilliAmpHours,
        isCharging = isCharging,
        plugType = plugType,
        statusText = statusText,
        isHardwareReported = isHardwareReported,
        isValid = isValid
    )
}

fun BatteryObservation.toEntity(): BatteryObservationEntity {
    return BatteryObservationEntity(
        id = id,
        timestamp = timestamp,
        batteryLevel = batteryLevel,
        temperatureCelsius = temperatureCelsius,
        voltageVolts = voltageVolts,
        currentMilliAmperes = currentMilliAmperes,
        chargeCounterMilliAmpHours = chargeCounterMilliAmpHours,
        isCharging = isCharging,
        plugType = plugType,
        statusText = statusText,
        isHardwareReported = isHardwareReported,
        isValid = isValid
    )
}

fun ChargingSessionEntity.toDomain(): ChargingSession {
    return ChargingSession(
        id = id,
        startTime = startTime,
        endTime = endTime,
        startLevel = startLevel,
        endLevel = endLevel,
        startChargeCounterMahi = startChargeCounterMahi,
        endChargeCounterMahi = endChargeCounterMahi,
        estimatedCapacityMahi = estimatedCapacityMahi,
        maxTemperatureCelsius = maxTemperatureCelsius,
        minTemperatureCelsius = minTemperatureCelsius,
        avgVoltageVolts = avgVoltageVolts,
        maxChargingCurrentMa = maxChargingCurrentMa,
        plugType = plugType,
        isComplete = isComplete,
        isValidForHealthEstimation = isValidForHealthEstimation,
        qualityRejectReason = qualityRejectReason
    )
}

fun ChargingSession.toEntity(): ChargingSessionEntity {
    return ChargingSessionEntity(
        id = id,
        startTime = startTime,
        endTime = endTime,
        startLevel = startLevel,
        endLevel = endLevel,
        startChargeCounterMahi = startChargeCounterMahi,
        endChargeCounterMahi = endChargeCounterMahi,
        estimatedCapacityMahi = estimatedCapacityMahi,
        maxTemperatureCelsius = maxTemperatureCelsius,
        minTemperatureCelsius = minTemperatureCelsius,
        avgVoltageVolts = avgVoltageVolts,
        maxChargingCurrentMa = maxChargingCurrentMa,
        plugType = plugType,
        isComplete = isComplete,
        isValidForHealthEstimation = isValidForHealthEstimation,
        qualityRejectReason = qualityRejectReason
    )
}

fun HealthAnalysisEntity.toDomain(): HealthAnalysis {
    return HealthAnalysis(
        id = id,
        timestamp = timestamp,
        healthPercentage = healthPercentage,
        category = try {
            BatteryHealthCategory.valueOf(category)
        } catch (_: Exception) {
            BatteryHealthCategory.INSUFFICIENT_DATA
        },
        estimatedCapacityMah = estimatedCapacityMah,
        designCapacityMah = designCapacityMah,
        confidenceScore = confidenceScore,
        confidenceLabel = confidenceLabel,
        milestone = try {
            CollectionMilestone.valueOf(milestone)
        } catch (_: Exception) {
            CollectionMilestone.INITIAL_24H
        },
        validObservationCount = validObservationCount,
        validChargingSessionCount = validChargingSessionCount,
        isPreliminary = isPreliminary,
        methodologyNote = methodologyNote
    )
}

fun HealthAnalysis.toEntity(): HealthAnalysisEntity {
    return HealthAnalysisEntity(
        id = id,
        timestamp = timestamp,
        healthPercentage = healthPercentage,
        category = category.name,
        estimatedCapacityMah = estimatedCapacityMah,
        designCapacityMah = designCapacityMah,
        confidenceScore = confidenceScore,
        confidenceLabel = confidenceLabel,
        milestone = milestone.name,
        validObservationCount = validObservationCount,
        validChargingSessionCount = validChargingSessionCount,
        isPreliminary = isPreliminary,
        methodologyNote = methodologyNote
    )
}

fun DeviceProfileEntity.toDomain(): DeviceProfile {
    return DeviceProfile(
        manufacturer = manufacturer,
        brand = brand,
        model = model,
        androidVersion = androidVersion,
        apiLevel = apiLevel,
        designCapacityMah = designCapacityMah,
        isDesignCapacityVerified = isDesignCapacityVerified,
        isCustomDesignCapacity = isCustomDesignCapacity,
        supportsChargeCounter = supportsChargeCounter,
        supportsInstantCurrent = supportsInstantCurrent,
        supportsAverageCurrent = supportsAverageCurrent,
        supportsEnergyCounter = supportsEnergyCounter,
        supportsHardwareCycleCount = supportsHardwareCycleCount,
        hardwareCycleCount = hardwareCycleCount,
        hardwareFullChargeCapacityMah = hardwareFullChargeCapacityMah
    )
}

fun DeviceProfile.toEntity(): DeviceProfileEntity {
    return DeviceProfileEntity(
        id = 1,
        manufacturer = manufacturer,
        brand = brand,
        model = model,
        androidVersion = androidVersion,
        apiLevel = apiLevel,
        designCapacityMah = designCapacityMah,
        isDesignCapacityVerified = isDesignCapacityVerified,
        isCustomDesignCapacity = isCustomDesignCapacity,
        supportsChargeCounter = supportsChargeCounter,
        supportsInstantCurrent = supportsInstantCurrent,
        supportsAverageCurrent = supportsAverageCurrent,
        supportsEnergyCounter = supportsEnergyCounter,
        supportsHardwareCycleCount = supportsHardwareCycleCount,
        hardwareCycleCount = hardwareCycleCount,
        hardwareFullChargeCapacityMah = hardwareFullChargeCapacityMah
    )
}
