package com.voltar.battery.core.repository

import com.voltar.battery.battery.BatteryDiagnosticsEngine
import com.voltar.battery.battery.BatteryHealthEngine
import com.voltar.battery.battery.DeviceCompatibilityLayer
import com.voltar.battery.core.database.VoltarDatabase
import com.voltar.battery.core.database.mapper.toDomain
import com.voltar.battery.core.database.mapper.toEntity
import com.voltar.battery.core.datastore.UserPreferencesRepository
import com.voltar.battery.core.model.BatteryObservation
import com.voltar.battery.core.model.ChargingSession
import com.voltar.battery.core.model.DeviceProfile
import com.voltar.battery.core.model.HealthAnalysis
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BatteryRepository(
    private val database: VoltarDatabase,
    private val diagnosticsEngine: BatteryDiagnosticsEngine,
    private val healthEngine: BatteryHealthEngine,
    private val compatibilityLayer: DeviceCompatibilityLayer,
    private val preferencesRepository: UserPreferencesRepository
) {
    private val scope = CoroutineScope(Dispatchers.IO)

    val liveBatteryFlow: Flow<BatteryObservation> = diagnosticsEngine.observeBattery()

    val latestObservationFlow: Flow<BatteryObservation?> =
        database.batteryObservationDao().getLatestObservation().map { it?.toDomain() }

    val activeChargingSessionFlow: Flow<ChargingSession?> =
        database.chargingSessionDao().getActiveSessionFlow().map { it?.toDomain() }

    val completedChargingSessionsFlow: Flow<List<ChargingSession>> =
        database.chargingSessionDao().getCompletedSessions().map { list -> list.map { it.toDomain() } }

    val allChargingSessionsFlow: Flow<List<ChargingSession>> =
        database.chargingSessionDao().getAllSessions().map { list -> list.map { it.toDomain() } }

    val latestHealthAnalysisFlow: Flow<HealthAnalysis?> =
        database.healthAnalysisDao().getLatestAnalysis().map { it?.toDomain() }

    val allHealthAnalysesFlow: Flow<List<HealthAnalysis>> =
        database.healthAnalysisDao().getAllAnalyses().map { list -> list.map { it.toDomain() } }

    fun getRecentObservations(limit: Int): Flow<List<BatteryObservation>> =
        database.batteryObservationDao().getRecentObservations(limit).map { list -> list.map { it.toDomain() } }

    fun getObservationsSince(sinceTimestamp: Long): Flow<List<BatteryObservation>> =
        database.batteryObservationDao().getObservationsSince(sinceTimestamp).map { list -> list.map { it.toDomain() } }

    suspend fun getDeviceProfile(): DeviceProfile = withContext(Dispatchers.IO) {
        val prefs = preferencesRepository.userPreferencesFlow.first()
        val cached = database.deviceProfileDao().getProfileSync()
        if (cached != null && !prefs.customDesignCapacityMah.let { it != null && it != cached.designCapacityMah }) {
            return@withContext cached.toDomain()
        }
        val inspected = compatibilityLayer.inspectDevice(prefs.customDesignCapacityMah)
        database.deviceProfileDao().insertOrUpdateProfile(inspected.toEntity())
        inspected
    }

    suspend fun recordObservation(observation: BatteryObservation) = withContext(Dispatchers.IO) {
        database.batteryObservationDao().insertObservation(observation.toEntity())
        handleChargingSessionState(observation)
    }

    private suspend fun handleChargingSessionState(observation: BatteryObservation) {
        val activeSessionEntity = database.chargingSessionDao().getActiveSession()
        val deviceProfile = getDeviceProfile()

        if (observation.isCharging) {
            if (activeSessionEntity == null) {
                // Start a new charging session
                val newSession = ChargingSession(
                    startTime = observation.timestamp,
                    startLevel = observation.batteryLevel,
                    startChargeCounterMahi = observation.chargeCounterMilliAmpHours,
                    maxTemperatureCelsius = observation.temperatureCelsius,
                    minTemperatureCelsius = observation.temperatureCelsius,
                    avgVoltageVolts = observation.voltageVolts,
                    maxChargingCurrentMa = observation.currentMilliAmperes ?: 0f,
                    plugType = observation.plugType,
                    isComplete = false
                )
                database.chargingSessionDao().insertSession(newSession.toEntity())
            } else {
                // Update active session metrics
                val currentDomain = activeSessionEntity.toDomain()
                val updatedMaxTemp = maxOf(currentDomain.maxTemperatureCelsius, observation.temperatureCelsius)
                val updatedMinTemp = minOf(currentDomain.minTemperatureCelsius, observation.temperatureCelsius)
                val updatedMaxCurrent = maxOf(currentDomain.maxChargingCurrentMa, observation.currentMilliAmperes ?: 0f)
                val updatedAvgVoltage = (currentDomain.avgVoltageVolts + observation.voltageVolts) / 2f

                val updatedSession = currentDomain.copy(
                    endLevel = observation.batteryLevel,
                    endChargeCounterMahi = observation.chargeCounterMilliAmpHours ?: currentDomain.endChargeCounterMahi,
                    maxTemperatureCelsius = updatedMaxTemp,
                    minTemperatureCelsius = updatedMinTemp,
                    avgVoltageVolts = updatedAvgVoltage,
                    maxChargingCurrentMa = updatedMaxCurrent
                )
                database.chargingSessionDao().updateSession(updatedSession.toEntity())
            }
        } else {
            // Not charging: if an active session exists, finalize it
            if (activeSessionEntity != null) {
                val currentDomain = activeSessionEntity.toDomain()
                val finalizedRaw = currentDomain.copy(
                    endTime = observation.timestamp,
                    endLevel = observation.batteryLevel,
                    endChargeCounterMahi = observation.chargeCounterMilliAmpHours ?: currentDomain.endChargeCounterMahi,
                    isComplete = true
                )

                // Evaluate session validity with BatteryHealthEngine
                val evaluatedSession = healthEngine.evaluateChargingSession(
                    finalizedRaw,
                    deviceProfile.designCapacityMah
                )
                database.chargingSessionDao().updateSession(evaluatedSession.toEntity())

                // Trigger recalculation of health
                recalculateHealthInternal(deviceProfile)
            }
        }
    }

    suspend fun recalculateHealth() = withContext(Dispatchers.IO) {
        val deviceProfile = getDeviceProfile()
        recalculateHealthInternal(deviceProfile)
    }

    private suspend fun recalculateHealthInternal(deviceProfile: DeviceProfile) {
        val validSessions = database.chargingSessionDao().getValidSessionsForHealth().map { it.toDomain() }
        val totalCount = database.batteryObservationDao().getTotalCount()
        val prefs = preferencesRepository.userPreferencesFlow.first()

        val analysis = healthEngine.computeHealthAnalysis(
            deviceProfile = deviceProfile,
            validSessions = validSessions,
            totalObservationsCount = totalCount,
            firstLaunchTimestamp = prefs.firstLaunchTimestamp
        )

        database.healthAnalysisDao().insertAnalysis(analysis.toEntity())
    }

    suspend fun clearAllData() = withContext(Dispatchers.IO) {
        database.batteryObservationDao().deleteAll()
        database.chargingSessionDao().deleteAll()
        database.healthAnalysisDao().deleteAll()
        preferencesRepository.resetAll()
        recalculateHealth()
    }

    suspend fun resetHealthAnalysisOnly() = withContext(Dispatchers.IO) {
        database.healthAnalysisDao().deleteAll()
        recalculateHealth()
    }

    suspend fun exportDataAsJson(): String = withContext(Dispatchers.IO) {
        val device = getDeviceProfile()
        val latestHealth = database.healthAnalysisDao().getLatestAnalysis()
        val recentObs = database.batteryObservationDao().getRecentObservations(200).first()
        val sessions = database.chargingSessionDao().getAllSessions().first()

        buildString {
            append("{\n")
            append("  \"app\": \"VOLTAR - Advanced Reliability\",\n")
            append("  \"exportTimestamp\": ${System.currentTimeMillis()},\n")
            append("  \"device\": {\n")
            append("    \"manufacturer\": \"${device.manufacturer}\",\n")
            append("    \"model\": \"${device.model}\",\n")
            append("    \"androidVersion\": \"${device.androidVersion}\",\n")
            append("    \"designCapacityMah\": ${device.designCapacityMah},\n")
            append("    \"isDesignCapacityVerified\": ${device.isDesignCapacityVerified}\n")
            append("  },\n")
            append("  \"chargingSessionsCount\": ${sessions.size},\n")
            append("  \"observationsCount\": ${recentObs.size}\n")
            append("}")
        }
    }

    suspend fun exportDataAsCsv(): String = withContext(Dispatchers.IO) {
        val recentObs = database.batteryObservationDao().getRecentObservations(500).first()
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
        buildString {
            append("Timestamp,DateTime,BatteryLevel,TemperatureCelsius,VoltageVolts,CurrentMa,ChargeCounterMah,IsCharging,PlugType\n")
            for (obs in recentObs) {
                append("${obs.timestamp},\"${sdf.format(Date(obs.timestamp))}\",${obs.batteryLevel},${obs.temperatureCelsius},${obs.voltageVolts},${obs.currentMilliAmperes ?: ""},${obs.chargeCounterMilliAmpHours ?: ""},${obs.isCharging},\"${obs.plugType}\"\n")
            }
        }
    }
}
