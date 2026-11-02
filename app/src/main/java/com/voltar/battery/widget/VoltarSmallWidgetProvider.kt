package com.voltar.battery.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context

class VoltarSmallWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        VoltarWidgetUpdater.updateSmallWidgets(context, appWidgetManager, appWidgetIds)
    }

    override fun onEnabled(context: Context) {
        VoltarWidgetUpdater.updateAllWidgets(context)
    }
}
