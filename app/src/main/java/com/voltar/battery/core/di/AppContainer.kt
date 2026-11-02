package com.voltar.battery.core.di

import android.content.Context
import com.voltar.battery.battery.BatteryDiagnosticsEngine
import com.voltar.battery.battery.BatteryHealthEngine
import com.voltar.battery.battery.DeviceCompatibilityLayer
import com.voltar.battery.core.database.VoltarDatabase
import com.voltar.battery.core.datastore.UserPreferencesRepository
import com.voltar.battery.core.repository.BatteryRepository
import com.voltar.battery.monitoring.BatteryMonitoringScheduler

class AppContainer(context: Context) {

    private val appContext: Context = context.applicationContext

    val database: VoltarDatabase by lazy {
        VoltarDatabase.getInstance(appContext)
    }

    val userPreferencesRepository: UserPreferencesRepository by lazy {
        UserPreferencesRepository(appContext)
    }

    val compatibilityLayer: DeviceCompatibilityLayer by lazy {
        DeviceCompatibilityLayer(appContext)
    }

    val diagnosticsEngine: BatteryDiagnosticsEngine by lazy {
        BatteryDiagnosticsEngine(appContext)
    }

    val healthEngine: BatteryHealthEngine by lazy {
        BatteryHealthEngine()
    }

    val batteryRepository: BatteryRepository by lazy {
        BatteryRepository(
            database = database,
            diagnosticsEngine = diagnosticsEngine,
            healthEngine = healthEngine,
            compatibilityLayer = compatibilityLayer,
            preferencesRepository = userPreferencesRepository
        )
    }

    val monitoringScheduler: BatteryMonitoringScheduler by lazy {
        BatteryMonitoringScheduler(appContext)
    }
}
