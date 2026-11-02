package com.voltar.battery.monitoring

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.voltar.battery.VoltarApplication
import com.voltar.battery.widget.VoltarWidgetUpdater

class BatteryMonitoringWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        return try {
            val app = applicationContext as? VoltarApplication ?: return Result.failure()
            val container = app.container

            val observation = container.diagnosticsEngine.getCurrentObservation()
            container.batteryRepository.recordObservation(observation)
            VoltarWidgetUpdater.updateAllWidgets(applicationContext)

            Result.success()
        } catch (_: Exception) {
            Result.retry()
        }
    }
}
