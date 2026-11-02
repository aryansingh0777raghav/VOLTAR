package com.voltar.battery.monitoring

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.voltar.battery.VoltarApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BatteryChangeReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        val app = context.applicationContext as? VoltarApplication ?: return
        val container = app.container

        if (action == Intent.ACTION_BOOT_COMPLETED) {
            // Restore periodic monitoring
            CoroutineScope(Dispatchers.IO).launch {
                container.monitoringScheduler.scheduleMonitoring()
                val obs = container.diagnosticsEngine.getCurrentObservation()
                container.batteryRepository.recordObservation(obs)
            }
            return
        }

        if (action == Intent.ACTION_POWER_CONNECTED ||
            action == Intent.ACTION_POWER_DISCONNECTED ||
            action == Intent.ACTION_BATTERY_LOW ||
            action == Intent.ACTION_BATTERY_OKAY
        ) {
            CoroutineScope(Dispatchers.IO).launch {
                val obs = container.diagnosticsEngine.getCurrentObservation()
                container.batteryRepository.recordObservation(obs)
            }
        }
    }
}
