package com.voltar.battery

import android.app.Application
import android.content.Intent
import android.util.Log
import com.voltar.battery.core.di.AppContainer
import com.voltar.battery.ui.CrashDisplayActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class VoltarApplication : Application() {

    val container: AppContainer by lazy {
        AppContainer(applicationContext)
    }

    override fun onCreate() {
        super.onCreate()

        // Global Exception Catcher
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e("VoltarCrash", "Fatal crash in thread ${thread.name}", throwable)
            try {
                val intent = Intent(this, CrashDisplayActivity::class.java).apply {
                    putExtra("error", Log.getStackTraceString(throwable))
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(intent)
            } catch (e: Throwable) {
                e.printStackTrace()
            }
            android.os.Process.killProcess(android.os.Process.myPid())
            System.exit(10)
        }

        try {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    container.userPreferencesRepository.initFirstLaunchIfEmpty()
                    val prefs = container.userPreferencesRepository.userPreferencesFlow.first()

                    if (prefs.isMonitoringEnabled) {
                        try {
                            container.monitoringScheduler.scheduleMonitoring(prefs.collectionIntervalMinutes)
                        } catch (e: Throwable) {
                            Log.w("VoltarApp", "WorkManager schedule skipped: ${e.message}")
                        }
                    }

                    // Capture initial baseline observation safely
                    try {
                        val initialObservation = container.diagnosticsEngine.getCurrentObservation()
                        container.batteryRepository.recordObservation(initialObservation)
                        container.batteryRepository.recalculateHealth()
                    } catch (e: Throwable) {
                        Log.w("VoltarApp", "Initial observation skipped: ${e.message}")
                    }
                } catch (e: Throwable) {
                    Log.w("VoltarApp", "Background init skipped: ${e.message}")
                }
            }
        } catch (e: Throwable) {
            Log.e("VoltarApp", "Application init error", e)
        }
    }
}
