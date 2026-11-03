package com.voltar.battery.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.view.View
import android.widget.RemoteViews
import com.voltar.battery.MainActivity
import com.voltar.battery.R
import com.voltar.battery.core.datastore.ThemeMode
import com.voltar.battery.core.di.AppContainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.abs

object VoltarWidgetUpdater {

    fun updateAllWidgets(context: Context) {
        val appWidgetManager = AppWidgetManager.getInstance(context) ?: return

        // 1. Medium 4x2 Health Widget
        val mediumIds = appWidgetManager.getAppWidgetIds(
            ComponentName(context, VoltarMediumWidgetProvider::class.java)
        )
        if (mediumIds.isNotEmpty()) {
            updateMediumWidgets(context, appWidgetManager, mediumIds)
        }

        // 2. Small 2x2 Health Widget
        val smallIds = appWidgetManager.getAppWidgetIds(
            ComponentName(context, VoltarSmallWidgetProvider::class.java)
        )
        if (smallIds.isNotEmpty()) {
            updateSmallWidgets(context, appWidgetManager, smallIds)
        }

        // 3. Large 4x3 Health Diagnostics Suite
        val largeIds = appWidgetManager.getAppWidgetIds(
            ComponentName(context, VoltarLargeWidgetProvider::class.java)
        )
        if (largeIds.isNotEmpty()) {
            updateLargeWidgets(context, appWidgetManager, largeIds)
        }

        // 4. One UI 9 Battery Percentage Dial (2x2)
        val dialIds = appWidgetManager.getAppWidgetIds(
            ComponentName(context, VoltarBatteryDialWidgetProvider::class.java)
        )
        if (dialIds.isNotEmpty()) {
            updateBatteryDialWidgets(context, appWidgetManager, dialIds)
        }

        // 5. One UI 9 Battery Capsule Pill (2x1)
        val pillIds = appWidgetManager.getAppWidgetIds(
            ComponentName(context, VoltarBatteryPillWidgetProvider::class.java)
        )
        if (pillIds.isNotEmpty()) {
            updateBatteryPillWidgets(context, appWidgetManager, pillIds)
        }

        // 6. Charging Lab (4x2)
        val chargingIds = appWidgetManager.getAppWidgetIds(
            ComponentName(context, VoltarChargingLabWidgetProvider::class.java)
        )
        if (chargingIds.isNotEmpty()) {
            updateChargingLabWidgets(context, appWidgetManager, chargingIds)
        }

        // 7. Thermal & Safety Monitor (2x1)
        val thermalIds = appWidgetManager.getAppWidgetIds(
            ComponentName(context, VoltarThermalWidgetProvider::class.java)
        )
        if (thermalIds.isNotEmpty()) {
            updateThermalWidgets(context, appWidgetManager, thermalIds)
        }
    }

    fun updateMediumWidgets(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val telemetry = getLatestTelemetry(context)
            val isDark = isDarkTheme(context, telemetry.themeMode)

            val bgBitmap = WidgetGaugeRenderer.renderGlassBackground(
                context = context,
                widthDp = 280,
                heightDp = 130,
                opacityPercent = telemetry.opacity,
                isDark = isDark
            )
            val gaugeBitmap = WidgetGaugeRenderer.renderDonutGauge(
                context = context,
                sizeDp = 104,
                healthPercent = telemetry.healthPercent,
                levelPercent = telemetry.levelPercent,
                isDark = isDark
            )

            val textColorPrimary = if (isDark) Color.WHITE else Color.parseColor("#111111")
            val textColorSecondary = if (isDark) Color.parseColor("#A0A0A0") else Color.parseColor("#444444")

            val clickIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                context, 0, clickIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            for (widgetId in appWidgetIds) {
                val views = RemoteViews(context.packageName, R.layout.widget_voltar_medium).apply {
                    setImageViewBitmap(R.id.widget_background, bgBitmap)
                    setImageViewBitmap(R.id.widget_gauge_image, gaugeBitmap)

                    val healthText = if (telemetry.healthPercent != null && telemetry.healthPercent > 0f) {
                        "${telemetry.healthPercent.toInt()}%"
                    } else {
                        "--%"
                    }
                    setTextViewText(R.id.widget_health_value, healthText)
                    setTextColor(R.id.widget_health_value, textColorPrimary)

                    setTextViewText(R.id.widget_gauge_center_text, "${telemetry.levelPercent}%")
                    setTextColor(R.id.widget_gauge_center_text, textColorPrimary)

                    val capText = if (telemetry.estimatedCapMah != null && telemetry.estimatedCapMah > 0f) {
                        "${telemetry.estimatedCapMah.toInt()} mAh (Est.)"
                    } else {
                        "Learning Cycles..."
                    }
                    setTextViewText(R.id.widget_capacity_text, capText)
                    setTextColor(R.id.widget_capacity_text, textColorSecondary)

                    setTextViewText(R.id.widget_voltage_text, "${String.format(Locale.US, "%.2f", telemetry.voltageV)} V (${telemetry.voltageStatus})")
                    setTextColor(R.id.widget_voltage_text, textColorSecondary)

                    setTextViewText(R.id.widget_temp_text, "${telemetry.tempCelsius}°C (${telemetry.tempStatus})")
                    setTextColor(R.id.widget_temp_text, textColorSecondary)

                    setOnClickPendingIntent(R.id.widget_root, pendingIntent)
                }
                appWidgetManager.updateAppWidget(widgetId, views)
            }
        }
    }

    fun updateSmallWidgets(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val telemetry = getLatestTelemetry(context)
            val isDark = isDarkTheme(context, telemetry.themeMode)

            val bgBitmap = WidgetGaugeRenderer.renderGlassBackground(
                context = context,
                widthDp = 140,
                heightDp = 140,
                opacityPercent = telemetry.opacity,
                isDark = isDark
            )
            val gaugeBitmap = WidgetGaugeRenderer.renderDonutGauge(
                context = context,
                sizeDp = 82,
                healthPercent = telemetry.healthPercent,
                levelPercent = telemetry.levelPercent,
                isDark = isDark
            )

            val textColorPrimary = if (isDark) Color.WHITE else Color.parseColor("#111111")
            val textColorSecondary = if (isDark) Color.parseColor("#B0B0B0") else Color.parseColor("#555555")

            val clickIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                context, 1, clickIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            for (widgetId in appWidgetIds) {
                val views = RemoteViews(context.packageName, R.layout.widget_voltar_small).apply {
                    setImageViewBitmap(R.id.widget_background, bgBitmap)
                    setImageViewBitmap(R.id.widget_gauge_image, gaugeBitmap)

                    val healthText = if (telemetry.healthPercent != null && telemetry.healthPercent > 0f) {
                        "${telemetry.healthPercent.toInt()}%"
                    } else {
                        "--%"
                    }
                    setTextViewText(R.id.widget_health_value, healthText)
                    setTextColor(R.id.widget_health_value, textColorPrimary)

                    setTextViewText(
                        R.id.widget_metrics_summary,
                        "${telemetry.levelPercent}% • ${telemetry.tempCelsius}°C • ${String.format(Locale.US, "%.2f", telemetry.voltageV)}V"
                    )
                    setTextColor(R.id.widget_metrics_summary, textColorSecondary)

                    setOnClickPendingIntent(R.id.widget_root, pendingIntent)
                }
                appWidgetManager.updateAppWidget(widgetId, views)
            }
        }
    }

    fun updateLargeWidgets(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val telemetry = getLatestTelemetry(context)
            val isDark = isDarkTheme(context, telemetry.themeMode)

            val bgBitmap = WidgetGaugeRenderer.renderGlassBackground(
                context = context,
                widthDp = 280,
                heightDp = 200,
                opacityPercent = telemetry.opacity,
                isDark = isDark
            )
            val gaugeBitmap = WidgetGaugeRenderer.renderDonutGauge(
                context = context,
                sizeDp = 96,
                healthPercent = telemetry.healthPercent,
                levelPercent = telemetry.levelPercent,
                isDark = isDark
            )

            val textColorPrimary = if (isDark) Color.WHITE else Color.parseColor("#111111")
            val textColorSecondary = if (isDark) Color.parseColor("#A0A0A0") else Color.parseColor("#444444")

            val clickIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                context, 2, clickIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            for (widgetId in appWidgetIds) {
                val views = RemoteViews(context.packageName, R.layout.widget_voltar_large).apply {
                    setImageViewBitmap(R.id.widget_background, bgBitmap)
                    setImageViewBitmap(R.id.widget_gauge_image, gaugeBitmap)

                    val healthText = if (telemetry.healthPercent != null && telemetry.healthPercent > 0f) {
                        "${telemetry.healthPercent.toInt()}%"
                    } else {
                        "--%"
                    }
                    setTextViewText(R.id.widget_health_value, healthText)
                    setTextColor(R.id.widget_health_value, textColorPrimary)

                    setTextViewText(R.id.widget_gauge_center_text, "${telemetry.levelPercent}%")
                    setTextColor(R.id.widget_gauge_center_text, textColorPrimary)

                    val capText = if (telemetry.estimatedCapMah != null && telemetry.estimatedCapMah > 0f) {
                        "${telemetry.estimatedCapMah.toInt()} mAh (Estimated)"
                    } else {
                        "Learning Cycles..."
                    }
                    setTextViewText(R.id.widget_capacity_text, capText)
                    setTextColor(R.id.widget_capacity_text, textColorSecondary)

                    setTextViewText(R.id.widget_voltage_text, "${String.format(Locale.US, "%.2f", telemetry.voltageV)} V")
                    setTextColor(R.id.widget_voltage_text, textColorPrimary)

                    setTextViewText(R.id.widget_temp_text, "${telemetry.tempCelsius}°C (${telemetry.tempStatus})")
                    setTextColor(R.id.widget_temp_text, textColorPrimary)

                    setTextViewText(R.id.widget_status_text, telemetry.milestoneTitle)

                    setOnClickPendingIntent(R.id.widget_root, pendingIntent)
                }
                appWidgetManager.updateAppWidget(widgetId, views)
            }
        }
    }

    fun updateBatteryDialWidgets(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val telemetry = getLatestTelemetry(context)
            val isDark = isDarkTheme(context, telemetry.themeMode)

            val bgBitmap = WidgetGaugeRenderer.renderGlassBackground(
                context = context,
                widthDp = 140,
                heightDp = 140,
                opacityPercent = telemetry.opacity,
                isDark = isDark
            )
            val gaugeBitmap = WidgetGaugeRenderer.renderBatteryLevelDial(
                context = context,
                sizeDp = 88,
                levelPercent = telemetry.levelPercent,
                isCharging = telemetry.isCharging,
                isDark = isDark
            )

            val textColorPrimary = if (isDark) Color.WHITE else Color.parseColor("#111111")
            val textColorSecondary = if (isDark) Color.parseColor("#B0B0B0") else Color.parseColor("#555555")

            val clickIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                context, 3, clickIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val statusSubtitle = if (telemetry.isCharging) {
                "${telemetry.chargingStatusText} • ${String.format(Locale.US, "%.2f", telemetry.voltageV)}V"
            } else {
                "Discharging • ${telemetry.tempCelsius}°C"
            }

            for (widgetId in appWidgetIds) {
                val views = RemoteViews(context.packageName, R.layout.widget_battery_dial).apply {
                    setImageViewBitmap(R.id.widget_background, bgBitmap)
                    setImageViewBitmap(R.id.widget_gauge_image, gaugeBitmap)

                    setTextViewText(R.id.widget_battery_percent, "${telemetry.levelPercent}%")
                    setTextColor(R.id.widget_battery_percent, textColorPrimary)

                    setViewVisibility(
                        R.id.widget_charging_icon,
                        if (telemetry.isCharging) View.VISIBLE else View.GONE
                    )

                    setTextViewText(R.id.widget_charging_status, statusSubtitle)
                    setTextColor(
                        R.id.widget_charging_status,
                        if (telemetry.isCharging) Color.parseColor("#00E676") else textColorSecondary
                    )

                    setOnClickPendingIntent(R.id.widget_root, pendingIntent)
                }
                appWidgetManager.updateAppWidget(widgetId, views)
            }
        }
    }

    fun updateBatteryPillWidgets(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val telemetry = getLatestTelemetry(context)
            val isDark = isDarkTheme(context, telemetry.themeMode)

            val bgBitmap = WidgetGaugeRenderer.renderGlassCapsuleBackground(
                context = context,
                widthDp = 180,
                heightDp = 64,
                opacityPercent = telemetry.opacity,
                isDark = isDark
            )
            val miniDialBitmap = WidgetGaugeRenderer.renderMiniBatteryDial(
                context = context,
                sizeDp = 40,
                levelPercent = telemetry.levelPercent,
                isCharging = telemetry.isCharging,
                isDark = isDark
            )

            val textColorPrimary = if (isDark) Color.WHITE else Color.parseColor("#111111")
            val textColorSecondary = if (isDark) Color.parseColor("#B0B0B0") else Color.parseColor("#555555")

            val clickIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                context, 4, clickIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val powerStatus = if (telemetry.isCharging) {
                "${telemetry.wattageText} • ${telemetry.tempCelsius}°C"
            } else {
                "Discharging • ${telemetry.tempCelsius}°C"
            }

            for (widgetId in appWidgetIds) {
                val views = RemoteViews(context.packageName, R.layout.widget_battery_pill).apply {
                    setImageViewBitmap(R.id.widget_background, bgBitmap)
                    setImageViewBitmap(R.id.widget_mini_dial, miniDialBitmap)

                    setTextViewText(R.id.widget_battery_percent, "${telemetry.levelPercent}%")
                    setTextColor(R.id.widget_battery_percent, textColorPrimary)

                    setTextViewText(R.id.widget_power_status, powerStatus)
                    setTextColor(
                        R.id.widget_power_status,
                        if (telemetry.isCharging) Color.parseColor("#00E676") else textColorSecondary
                    )

                    setOnClickPendingIntent(R.id.widget_root, pendingIntent)
                }
                appWidgetManager.updateAppWidget(widgetId, views)
            }
        }
    }

    fun updateChargingLabWidgets(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val telemetry = getLatestTelemetry(context)
            val isDark = isDarkTheme(context, telemetry.themeMode)

            val bgBitmap = WidgetGaugeRenderer.renderGlassBackground(
                context = context,
                widthDp = 280,
                heightDp = 130,
                opacityPercent = telemetry.opacity,
                isDark = isDark
            )

            val textColorPrimary = if (isDark) Color.WHITE else Color.parseColor("#111111")
            val textColorSecondary = if (isDark) Color.parseColor("#A0A0A0") else Color.parseColor("#444444")

            val clickIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                context, 5, clickIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val powerValue = if (telemetry.isCharging) telemetry.wattageText else "Not Charging"
            val typeText = if (telemetry.isCharging) {
                "${telemetry.chargingStatusText} (${telemetry.plugType})"
            } else {
                "Discharging"
            }

            for (widgetId in appWidgetIds) {
                val views = RemoteViews(context.packageName, R.layout.widget_charging_lab).apply {
                    setImageViewBitmap(R.id.widget_background, bgBitmap)

                    setTextViewText(R.id.widget_power_value, powerValue)
                    setTextColor(R.id.widget_power_value, textColorPrimary)

                    setTextViewText(R.id.widget_charging_type, typeText)
                    setTextColor(
                        R.id.widget_charging_type,
                        if (telemetry.isCharging) Color.parseColor("#00E676") else textColorSecondary
                    )

                    val currentText = if (telemetry.currentmA != null) {
                        val sign = if (telemetry.currentmA > 0) "+" else ""
                        "$sign${telemetry.currentmA.toInt()} mA"
                    } else {
                        "Rate N/A"
                    }
                    setTextViewText(R.id.widget_current_pill, currentText)
                    setTextColor(R.id.widget_current_pill, textColorSecondary)

                    setTextViewText(R.id.widget_voltage_pill, "${String.format(Locale.US, "%.2f", telemetry.voltageV)} V")
                    setTextColor(R.id.widget_voltage_pill, textColorSecondary)

                    setTextViewText(R.id.widget_temp_pill, "${telemetry.tempCelsius}°C (${telemetry.tempStatus})")
                    setTextColor(R.id.widget_temp_pill, textColorSecondary)

                    setOnClickPendingIntent(R.id.widget_root, pendingIntent)
                }
                appWidgetManager.updateAppWidget(widgetId, views)
            }
        }
    }

    fun updateThermalWidgets(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val telemetry = getLatestTelemetry(context)
            val isDark = isDarkTheme(context, telemetry.themeMode)

            val bgBitmap = WidgetGaugeRenderer.renderGlassCapsuleBackground(
                context = context,
                widthDp = 180,
                heightDp = 64,
                opacityPercent = telemetry.opacity,
                isDark = isDark
            )
            val badgeBitmap = WidgetGaugeRenderer.renderThermalBadge(
                context = context,
                sizeDp = 38,
                tempCelsius = telemetry.tempCelsius,
                isDark = isDark
            )

            val textColorPrimary = if (isDark) Color.WHITE else Color.parseColor("#111111")

            val clickIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                context, 6, clickIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val statusColor = when {
                telemetry.tempCelsius >= 42 -> Color.parseColor("#FF5252")
                telemetry.tempCelsius >= 38 -> Color.parseColor("#FFA000")
                telemetry.tempCelsius <= 15 -> Color.parseColor("#00B0FF")
                else -> Color.parseColor("#00E676")
            }

            val tipText = when {
                telemetry.tempCelsius >= 42 -> "Thermal Throttling Alert"
                telemetry.tempCelsius >= 38 -> "Warm • Charging Slowdown"
                else -> "${String.format(Locale.US, "%.2f", telemetry.voltageV)}V • Optimal Temp"
            }

            for (widgetId in appWidgetIds) {
                val views = RemoteViews(context.packageName, R.layout.widget_thermal_monitor).apply {
                    setImageViewBitmap(R.id.widget_background, bgBitmap)
                    setImageViewBitmap(R.id.widget_thermal_badge, badgeBitmap)

                    setTextViewText(R.id.widget_temp_value, "${telemetry.tempCelsius}°C")
                    setTextColor(R.id.widget_temp_value, textColorPrimary)

                    setTextViewText(R.id.widget_temp_status, telemetry.tempStatus)
                    setTextColor(R.id.widget_temp_status, statusColor)

                    setTextViewText(R.id.widget_thermal_tip, tipText)

                    setOnClickPendingIntent(R.id.widget_root, pendingIntent)
                }
                appWidgetManager.updateAppWidget(widgetId, views)
            }
        }
    }

    private data class WidgetTelemetry(
        val healthPercent: Float?,
        val levelPercent: Int,
        val voltageV: Float,
        val voltageStatus: String,
        val tempCelsius: Int,
        val tempStatus: String,
        val estimatedCapMah: Float?,
        val milestoneTitle: String,
        val isCharging: Boolean,
        val plugType: String,
        val currentmA: Float?,
        val wattageText: String,
        val chargingStatusText: String,
        val opacity: Int,
        val themeMode: ThemeMode
    )

    private suspend fun getLatestTelemetry(context: Context): WidgetTelemetry {
        val app = context.applicationContext as? com.voltar.battery.VoltarApplication
        val container = app?.container ?: AppContainer(context.applicationContext)

        val obs = container.diagnosticsEngine.getCurrentObservation()
        val latestHealth = container.batteryRepository.latestHealthAnalysisFlow.firstOrNull()
        val prefs = container.userPreferencesRepository.userPreferencesFlow.firstOrNull()

        val healthPct = latestHealth?.healthPercentage
        val estCap = latestHealth?.estimatedCapacityMah

        val voltV = obs.voltageVolts
        val voltStatus = when {
            voltV >= 4.35f -> "High"
            voltV <= 3.70f -> "Low"
            else -> "Normal"
        }

        val tempC = obs.temperatureCelsius.toInt()
        val tempStatus = when {
            tempC >= 42 -> "Hot"
            tempC >= 38 -> "Warm"
            tempC <= 15 -> "Cold"
            else -> "Normal"
        }

        val milestone = latestHealth?.milestone?.title ?: "Learning Cycles"
        val opacity = prefs?.widgetGlassOpacity ?: 75
        val theme = prefs?.themeMode ?: ThemeMode.SYSTEM

        val isCharging = obs.isCharging
        val currentmA = obs.currentMilliAmperes
        val powerWatts = if (isCharging && currentmA != null && currentmA > 50f && voltV > 2.5f) {
            voltV * (currentmA / 1000f)
        } else null

        val wattageText = when {
            powerWatts != null -> String.format(Locale.US, "%.1f W", powerWatts)
            isCharging -> "Charging"
            else -> "Not Charging"
        }

        val chargingStatusText = when {
            powerWatts != null && powerWatts >= 20f -> "Super Fast Charging"
            powerWatts != null && powerWatts >= 15f -> "Fast Charging"
            isCharging -> "Charging"
            else -> "Discharging"
        }

        return WidgetTelemetry(
            healthPercent = healthPct,
            levelPercent = obs.batteryLevel,
            voltageV = voltV,
            voltageStatus = voltStatus,
            tempCelsius = tempC,
            tempStatus = tempStatus,
            estimatedCapMah = estCap,
            milestoneTitle = milestone,
            isCharging = isCharging,
            plugType = obs.plugType,
            currentmA = currentmA,
            wattageText = wattageText,
            chargingStatusText = chargingStatusText,
            opacity = opacity,
            themeMode = theme
        )
    }

    private fun isDarkTheme(context: Context, themeMode: ThemeMode): Boolean {
        return when (themeMode) {
            ThemeMode.DARK -> true
            ThemeMode.LIGHT -> false
            ThemeMode.SYSTEM -> {
                val nightMode = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
                nightMode == Configuration.UI_MODE_NIGHT_YES
            }
        }
    }
}
