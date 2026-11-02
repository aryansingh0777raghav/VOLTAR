package com.voltar.battery.battery

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.core.content.ContextCompat
import com.voltar.battery.core.model.BatteryObservation
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlin.math.abs

class BatteryDiagnosticsEngine(private val context: Context) {

    private val batteryManager = try {
        context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
    } catch (_: Throwable) {
        null
    }

    fun getCurrentObservation(): BatteryObservation {
        return try {
            val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            val batteryStatus: Intent? = context.registerReceiver(null, intentFilter)
            parseBatteryIntent(batteryStatus)
        } catch (_: Throwable) {
            fallbackObservation()
        }
    }

    fun observeBattery(): Flow<BatteryObservation> = callbackFlow {
        val receiver = object : android.content.BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == Intent.ACTION_BATTERY_CHANGED) {
                    trySend(parseBatteryIntent(intent))
                }
            }
        }
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)

        try {
            ContextCompat.registerReceiver(
                context,
                receiver,
                filter,
                ContextCompat.RECEIVER_EXPORTED
            )
        } catch (_: Throwable) {
            try {
                context.registerReceiver(receiver, filter)
            } catch (_: Throwable) {}
        }

        // Send initial reading immediately
        trySend(getCurrentObservation())

        awaitClose {
            try {
                context.unregisterReceiver(receiver)
            } catch (_: Throwable) {}
        }
    }

    fun parseBatteryIntent(intent: Intent?): BatteryObservation {
        if (intent == null) {
            return fallbackObservation()
        }

        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100)
        val batteryPct = if (level >= 0 && scale > 0) {
            (level * 100 / scale.toFloat()).toInt().coerceIn(0, 100)
        } else {
            try {
                batteryManager?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: 50
            } catch (_: Throwable) {
                50
            }
        }

        val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL

        val statusText = when (status) {
            BatteryManager.BATTERY_STATUS_CHARGING -> "Charging"
            BatteryManager.BATTERY_STATUS_DISCHARGING -> "Discharging"
            BatteryManager.BATTERY_STATUS_FULL -> "Full"
            BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "Not Charging"
            else -> "Unknown"
        }

        val plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
        val plugType = when (plugged) {
            BatteryManager.BATTERY_PLUGGED_AC -> "AC Fast Charger"
            BatteryManager.BATTERY_PLUGGED_USB -> "USB Port"
            BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Wireless Dock"
            else -> if (isCharging) "Charger Connected" else "Not Plugged"
        }

        val rawTemp = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0)
        val tempCelsius = if (rawTemp > 0) rawTemp / 10f else 28.0f

        val rawVoltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0)
        val voltageVolts = if (rawVoltage > 0) {
            if (rawVoltage > 1000) rawVoltage / 1000f else rawVoltage.toFloat()
        } else {
            3.85f
        }

        // Current in mA safely
        val currentNow = try {
            batteryManager?.let { bm ->
                val rawMicroAmps = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
                if (rawMicroAmps != Int.MIN_VALUE && rawMicroAmps != 0) {
                    val ma = rawMicroAmps / 1000f
                    if (isCharging) abs(ma) else -abs(ma)
                } else {
                    null
                }
            }
        } catch (_: Throwable) {
            null
        }

        // Charge counter in mAh safely
        val chargeCounter = try {
            batteryManager?.let { bm ->
                val rawMicroAmpHours = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER)
                if (rawMicroAmpHours > 0) {
                    rawMicroAmpHours / 1000f
                } else {
                    null
                }
            }
        } catch (_: Throwable) {
            null
        }

        return BatteryObservation(
            timestamp = System.currentTimeMillis(),
            batteryLevel = batteryPct,
            temperatureCelsius = tempCelsius,
            voltageVolts = voltageVolts,
            currentMilliAmperes = currentNow,
            chargeCounterMilliAmpHours = chargeCounter,
            isCharging = isCharging,
            plugType = plugType,
            statusText = statusText,
            isHardwareReported = true,
            isValid = true
        )
    }

    private fun fallbackObservation(): BatteryObservation {
        return BatteryObservation(
            timestamp = System.currentTimeMillis(),
            batteryLevel = 100,
            temperatureCelsius = 25f,
            voltageVolts = 4.0f,
            currentMilliAmperes = null,
            chargeCounterMilliAmpHours = null,
            isCharging = false,
            plugType = "None",
            statusText = "Idle",
            isHardwareReported = false,
            isValid = false
        )
    }
}
