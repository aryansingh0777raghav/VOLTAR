package com.voltar.battery.battery

import android.content.Context
import android.os.BatteryManager
import android.os.Build
import com.voltar.battery.core.model.DeviceProfile
import java.io.File

class DeviceCompatibilityLayer(private val context: Context) {

    private val knownCapacityDatabase: Map<String, Float> = mapOf(
        "samsung:sm-a366b" to 5000f,
        "samsung:sm-a366u" to 5000f,
        "samsung:sm-a3660" to 5000f,
        "samsung:galaxy a36 5g" to 5000f,
        "samsung:galaxy a36" to 5000f,
        "samsung:sm-a546b" to 5000f,
        "samsung:galaxy a54" to 5000f,
        "samsung:sm-a556b" to 5000f,
        "samsung:galaxy a55" to 5000f,
        "samsung:sm-s911b" to 3900f,
        "samsung:galaxy s23" to 3900f,
        "samsung:sm-s916b" to 4700f,
        "samsung:galaxy s23+" to 4700f,
        "samsung:sm-s918b" to 5000f,
        "samsung:galaxy s23 ultra" to 5000f,
        "samsung:sm-s921b" to 4000f,
        "samsung:galaxy s24" to 4000f,
        "samsung:sm-s926b" to 4900f,
        "samsung:galaxy s24+" to 4900f,
        "samsung:sm-s928b" to 5000f,
        "samsung:galaxy s24 ultra" to 5000f,
        "google:pixel 7" to 4355f,
        "google:pixel 7 pro" to 5000f,
        "google:pixel 8" to 4575f,
        "google:pixel 8 pro" to 5050f,
        "google:pixel 9" to 4700f,
        "google:pixel 9 pro" to 4700f,
        "oneplus:cph2449" to 5000f,
        "oneplus:oneplus 11" to 5000f,
        "oneplus:cph2581" to 5400f,
        "oneplus:oneplus 12" to 5400f,
        "xiaomi:23127pn0cc" to 4610f,
        "xiaomi:xiaomi 14" to 4610f,
        "xiaomi:23117pn61c" to 5000f,
        "redmi:note 13" to 5000f
    )

    fun inspectDevice(customDesignCapacityMah: Float? = null): DeviceProfile {
        val batteryManager = try {
            context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
        } catch (_: Throwable) {
            null
        }

        val supportsChargeCounter = try {
            batteryManager?.let {
                val counter = it.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER)
                counter > 0
            } ?: false
        } catch (_: Throwable) {
            false
        }

        val supportsInstantCurrent = try {
            batteryManager?.let {
                val current = it.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
                current != Int.MIN_VALUE && current != 0
            } ?: false
        } catch (_: Throwable) {
            false
        }

        val supportsAverageCurrent = try {
            batteryManager?.let {
                val avg = it.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_AVERAGE)
                avg != Int.MIN_VALUE && avg != 0
            } ?: false
        } catch (_: Throwable) {
            false
        }

        val supportsEnergyCounter = try {
            batteryManager?.let {
                val energy = it.getLongProperty(BatteryManager.BATTERY_PROPERTY_ENERGY_COUNTER)
                energy > 0
            } ?: false
        } catch (_: Throwable) {
            false
        }

        val hardwareCycleCount = readSysfsInt(
            listOf(
                "/sys/class/power_supply/battery/cycle_count",
                "/sys/class/power_supply/bms/cycle_count"
            )
        )

        val hardwareChargeFull = readSysfsFloat(
            listOf(
                "/sys/class/power_supply/battery/charge_full",
                "/sys/class/power_supply/bms/charge_full"
            )
        )?.let { raw ->
            if (raw > 10000f) raw / 1000f else raw
        }

        val hardwareChargeFullDesign = readSysfsFloat(
            listOf(
                "/sys/class/power_supply/battery/charge_full_design",
                "/sys/class/power_supply/bms/charge_full_design"
            )
        )?.let { raw ->
            if (raw > 10000f) raw / 1000f else raw
        }

        val lookupKey1 = "${Build.MANUFACTURER.lowercase().trim()}:${Build.MODEL.lowercase().trim()}"
        val lookupKey2 = "${Build.BRAND.lowercase().trim()}:${Build.MODEL.lowercase().trim()}"

        val dbCapacity = knownCapacityDatabase[lookupKey1]
            ?: knownCapacityDatabase[lookupKey2]
            ?: hardwareChargeFullDesign

        val finalDesignCapacity = when {
            customDesignCapacityMah != null && customDesignCapacityMah > 500f -> customDesignCapacityMah
            dbCapacity != null && dbCapacity > 500f -> dbCapacity
            else -> 5000f
        }

        val isVerified = customDesignCapacityMah != null || dbCapacity != null

        return DeviceProfile(
            manufacturer = Build.MANUFACTURER.replaceFirstChar { it.uppercase() },
            brand = Build.BRAND.replaceFirstChar { it.uppercase() },
            model = Build.MODEL,
            androidVersion = Build.VERSION.RELEASE,
            apiLevel = Build.VERSION.SDK_INT,
            designCapacityMah = finalDesignCapacity,
            isDesignCapacityVerified = isVerified,
            isCustomDesignCapacity = customDesignCapacityMah != null,
            supportsChargeCounter = supportsChargeCounter,
            supportsInstantCurrent = supportsInstantCurrent,
            supportsAverageCurrent = supportsAverageCurrent,
            supportsEnergyCounter = supportsEnergyCounter,
            supportsHardwareCycleCount = hardwareCycleCount != null,
            hardwareCycleCount = hardwareCycleCount,
            hardwareFullChargeCapacityMah = hardwareChargeFull
        )
    }

    private fun readSysfsInt(candidates: List<String>): Int? {
        for (path in candidates) {
            try {
                val file = File(path)
                if (file.exists() && file.canRead()) {
                    val line = file.readText().trim()
                    val value = line.toIntOrNull()
                    if (value != null && value >= 0) return value
                }
            } catch (_: Throwable) {
                // Non-root sandbox & SELinux permission handling
            }
        }
        return null
    }

    private fun readSysfsFloat(candidates: List<String>): Float? {
        for (path in candidates) {
            try {
                val file = File(path)
                if (file.exists() && file.canRead()) {
                    val line = file.readText().trim()
                    val value = line.toFloatOrNull()
                    if (value != null && value > 0f) return value
                }
            } catch (_: Throwable) {
                // Non-root sandbox & SELinux permission handling
            }
        }
        return null
    }
}
