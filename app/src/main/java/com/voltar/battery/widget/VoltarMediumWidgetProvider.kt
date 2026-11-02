package com.voltar.battery.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context

class VoltarMediumWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        VoltarWidgetUpdater.updateMediumWidgets(context, appWidgetManager, appWidgetIds)
    }

    override fun onEnabled(context: Context) {
        VoltarWidgetUpdater.updateAllWidgets(context)
    }
}
