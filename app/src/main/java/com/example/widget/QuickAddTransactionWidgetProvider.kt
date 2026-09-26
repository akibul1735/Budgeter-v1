package com.example.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent

class QuickAddTransactionWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        WidgetUpdateHelper.updateAllWidgets(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == WidgetUpdateHelper.ACTION_REFRESH_WIDGETS ||
            intent.action == Intent.ACTION_CONFIGURATION_CHANGED
        ) {
            WidgetUpdateHelper.updateAllWidgets(context)
        }
    }
}
