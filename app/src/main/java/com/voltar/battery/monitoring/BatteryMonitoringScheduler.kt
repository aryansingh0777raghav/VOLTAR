package com.voltar.battery.monitoring

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class BatteryMonitoringScheduler(private val context: Context) {

    companion object {
        private const val WORK_NAME = "voltar_periodic_battery_monitor"
    }

    fun scheduleMonitoring(intervalMinutes: Int = 15) {
        try {
            val workManager = WorkManager.getInstance(context)

            val effectiveInterval = intervalMinutes.coerceAtLeast(15).toLong()

            val constraints = Constraints.Builder()
                .setRequiresBatteryNotLow(false)
                .build()

            val periodicRequest = PeriodicWorkRequestBuilder<BatteryMonitoringWorker>(
                effectiveInterval, TimeUnit.MINUTES
            ).setConstraints(constraints).build()

            workManager.enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                periodicRequest
            )
        } catch (_: Throwable) {
            // Safe fallback if WorkManager is unavailable on restricted ROMs
        }
    }

    fun cancelMonitoring() {
        try {
            val workManager = WorkManager.getInstance(context)
            workManager.cancelUniqueWork(WORK_NAME)
        } catch (_: Throwable) {}
    }
}
