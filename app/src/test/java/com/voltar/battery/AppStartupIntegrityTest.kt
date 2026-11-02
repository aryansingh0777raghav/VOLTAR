package com.voltar.battery

import android.content.Context
import android.os.Build
import com.voltar.battery.battery.BatteryDiagnosticsEngine
import com.voltar.battery.battery.BatteryHealthEngine
import com.voltar.battery.battery.DeviceCompatibilityLayer
import com.voltar.battery.core.datastore.ThemeMode
import com.voltar.battery.core.datastore.UserPreferences
import com.voltar.battery.core.model.BatteryHealthCategory
import com.voltar.battery.core.model.CollectionMilestone
import com.voltar.battery.core.model.MetricSource
import org.junit.Assert.assertNotNull
import org.junit.Test

class AppStartupIntegrityTest {

    @Test
    fun testHealthEngineDirect() {
        val engine = BatteryHealthEngine()
        val profile = com.voltar.battery.core.model.DeviceProfile(
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
        val analysis = engine.computeHealthAnalysis(
            deviceProfile = profile,
            validSessions = emptyList(),
            totalObservationsCount = 0,
            firstLaunchTimestamp = System.currentTimeMillis()
        )
        assertNotNull(analysis)
    }
}
