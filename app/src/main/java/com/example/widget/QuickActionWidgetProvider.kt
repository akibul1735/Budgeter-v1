package com.example.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import com.example.util.WidgetPreferences

class QuickActionWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        WidgetUpdateHelper.updateAllWidgets(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        when (intent.action) {
            WidgetUpdateHelper.ACTION_REFRESH_WIDGETS -> {
                WidgetUpdateHelper.updateAllWidgets(context)
            }
            WidgetUpdateHelper.ACTION_TOGGLE_PRIVACY -> {
                val widgetPrefs = WidgetPreferences.getInstance(context)
                widgetPrefs.togglePrivacy()
                WidgetUpdateHelper.updateAllWidgets(context)
            }
        }
    }
}
