package com.voltar.battery

import com.voltar.battery.battery.BatteryHealthEngine
import com.voltar.battery.core.model.BatteryHealthCategory
import com.voltar.battery.core.model.ChargingSession
import com.voltar.battery.core.model.CollectionMilestone
import com.voltar.battery.core.model.DeviceProfile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class BatteryHealthEngineTest {

    private lateinit var engine: BatteryHealthEngine
    private val designCapacity = 5000f

    @Before
    fun setup() {
        engine = BatteryHealthEngine()
    }

    @Test
    fun `evaluateChargingSession calculates capacity accurately when delta SoC is valid`() {
        // Delta SoC: 20% to 80% (60% change = 0.60)
        // Charge counter change: 2700 mAh
        // Expected capacity: 2700 / 0.60 = 4500 mAh
        val session = ChargingSession(
            startTime = 1000L,
            endTime = 5000L,
            startLevel = 20,
            endLevel = 80,
            startChargeCounterMahi = 1000f,
            endChargeCounterMahi = 3700f,
            maxTemperatureCelsius = 35f,
            minTemperatureCelsius = 25f,
            avgVoltageVolts = 4.1f,
            isComplete = true
        )

        val result = engine.evaluateChargingSession(session, designCapacity)

        assertTrue(result.isValidForHealthEstimation)
        assertNull(result.qualityRejectReason)
        assertNotNull(result.estimatedCapacityMahi)
        assertEquals(4500f, result.estimatedCapacityMahi!!, 1.0f)
    }

    @Test
    fun `evaluateChargingSession rejects sessions with delta SoC below minimum threshold`() {
        // Only 5% change (e.g. 50% to 55%)
        val session = ChargingSession(
            startTime = 1000L,
            endTime = 2000L,
            startLevel = 50,
            endLevel = 55,
            startChargeCounterMahi = 2000f,
            endChargeCounterMahi = 2250f,
            isComplete = true
        )

        val result = engine.evaluateChargingSession(session, designCapacity)

        assertFalse(result.isValidForHealthEstimation)
        assertNotNull(result.qualityRejectReason)
        assertTrue(result.qualityRejectReason!!.contains("Insufficient State-of-Charge"))
    }

    @Test
    fun `evaluateChargingSession rejects sessions with missing charge counter`() {
        val session = ChargingSession(
            startTime = 1000L,
            endTime = 5000L,
            startLevel = 20,
            endLevel = 80,
            startChargeCounterMahi = null,
            endChargeCounterMahi = null,
            isComplete = true
        )

        val result = engine.evaluateChargingSession(session, designCapacity)

        assertFalse(result.isValidForHealthEstimation)
        assertTrue(result.qualityRejectReason!!.contains("charge-counter unavailable"))
    }

    @Test
    fun `evaluateChargingSession rejects sessions with extreme temperature anomalies`() {
        val session = ChargingSession(
            startTime = 1000L,
            endTime = 5000L,
            startLevel = 20,
            endLevel = 80,
            startChargeCounterMahi = 1000f,
            endChargeCounterMahi = 3700f,
            maxTemperatureCelsius = 49f, // Exceeds 45°C limit
            minTemperatureCelsius = 25f,
            isComplete = true
        )

        val result = engine.evaluateChargingSession(session, designCapacity)

        assertFalse(result.isValidForHealthEstimation)
        assertTrue(result.qualityRejectReason!!.contains("Temperature anomaly"))
    }

    @Test
    fun `computeHealthAnalysis returns Insufficient Data when no valid sessions exist`() {
        val profile = createMockDeviceProfile()
        val analysis = engine.computeHealthAnalysis(
            deviceProfile = profile,
            validSessions = emptyList(),
            totalObservationsCount = 10,
            firstLaunchTimestamp = System.currentTimeMillis()
        )

        assertNull(analysis.healthPercentage)
        assertEquals(BatteryHealthCategory.INSUFFICIENT_DATA, analysis.category)
        assertTrue(analysis.isPreliminary)
        assertEquals(CollectionMilestone.INITIAL_24H, analysis.milestone)
    }

    @Test
    fun `computeHealthAnalysis calculates health and milestone correctly across 7 days`() {
        val profile = createMockDeviceProfile()
        val sevenDaysAgo = System.currentTimeMillis() - (8L * 24 * 60 * 60 * 1000)

        // 3 valid sessions averaging 4600 mAh on a 5000 mAh battery
        val sessions = listOf(
            createValidSession(4550f),
            createValidSession(4600f),
            createValidSession(4650f)
        )

        val analysis = engine.computeHealthAnalysis(
            deviceProfile = profile,
            validSessions = sessions,
            totalObservationsCount = 100,
            firstLaunchTimestamp = sevenDaysAgo
        )

        assertNotNull(analysis.healthPercentage)
        assertEquals(92.0f, analysis.healthPercentage!!, 0.5f)
        assertEquals(BatteryHealthCategory.GOOD, analysis.category)
        assertEquals(CollectionMilestone.SEVEN_DAYS, analysis.milestone)
        assertEquals("7-Day Validated", analysis.confidenceLabel)
        assertFalse(analysis.isPreliminary)
    }

    @Test
    fun `computeHealthAnalysis achieves 14-Day High Confidence milestone with 6+ sessions`() {
        val profile = createMockDeviceProfile()
        val fourteenDaysAgo = System.currentTimeMillis() - (15L * 24 * 60 * 60 * 1000)

        val sessions = (1..6).map { createValidSession(4500f) }

        val analysis = engine.computeHealthAnalysis(
            deviceProfile = profile,
            validSessions = sessions,
            totalObservationsCount = 300,
            firstLaunchTimestamp = fourteenDaysAgo
        )

        assertNotNull(analysis.healthPercentage)
        assertEquals(90.0f, analysis.healthPercentage!!, 0.5f)
        assertEquals(CollectionMilestone.FOURTEEN_DAYS, analysis.milestone)
        assertEquals("14-Day High Confidence", analysis.confidenceLabel)
    }

    private fun createValidSession(capacity: Float): ChargingSession {
        return ChargingSession(
            startTime = System.currentTimeMillis() - 500000,
            endTime = System.currentTimeMillis(),
            startLevel = 20,
            endLevel = 80,
            startChargeCounterMahi = 1000f,
            endChargeCounterMahi = 1000f + (capacity * 0.6f),
            estimatedCapacityMahi = capacity,
            isComplete = true,
            isValidForHealthEstimation = true
        )
    }

    private fun createMockDeviceProfile(): DeviceProfile {
        return DeviceProfile(
            manufacturer = "Samsung",
            brand = "Samsung",
            model = "Galaxy A36 5G",
            androidVersion = "15",
            apiLevel = 35,
            designCapacityMah = 5000f,
            isDesignCapacityVerified = true,
            isCustomDesignCapacity = false,
            supportsChargeCounter = true,
            supportsInstantCurrent = true,
            supportsAverageCurrent = true,
            supportsEnergyCounter = true,
            supportsHardwareCycleCount = false
        )
    }
}
