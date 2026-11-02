package com.voltar.battery.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
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

object VoltarWidgetUpdater {

    fun updateAllWidgets(context: Context) {
        val appWidgetManager = AppWidgetManager.getInstance(context) ?: return

        val mediumIds = appWidgetManager.getAppWidgetIds(
            ComponentName(context, VoltarMediumWidgetProvider::class.java)
        )
        if (mediumIds.isNotEmpty()) {
            updateMediumWidgets(context, appWidgetManager, mediumIds)
        }

        val smallIds = appWidgetManager.getAppWidgetIds(
            ComponentName(context, VoltarSmallWidgetProvider::class.java)
        )
        if (smallIds.isNotEmpty()) {
            updateSmallWidgets(context, appWidgetManager, smallIds)
        }

        val largeIds = appWidgetManager.getAppWidgetIds(
            ComponentName(context, VoltarLargeWidgetProvider::class.java)
        )
        if (largeIds.isNotEmpty()) {
            updateLargeWidgets(context, appWidgetManager, largeIds)
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

    private data class WidgetTelemetry(
        val healthPercent: Float?,
        val levelPercent: Int,
        val voltageV: Float,
        val voltageStatus: String,
        val tempCelsius: Int,
        val tempStatus: String,
        val estimatedCapMah: Float?,
        val milestoneTitle: String,
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

        return WidgetTelemetry(
            healthPercent = healthPct,
            levelPercent = obs.batteryLevel,
            voltageV = voltV,
            voltageStatus = voltStatus,
            tempCelsius = tempC,
            tempStatus = tempStatus,
            estimatedCapMah = estCap,
            milestoneTitle = milestone,
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
