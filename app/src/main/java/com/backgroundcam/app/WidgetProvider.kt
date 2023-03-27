package com.backgroundcam.app

import android.annotation.SuppressLint
import android.app.Activity
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.RemoteViews
import androidx.core.content.ContextCompat

class WidgetProvider: AppWidgetProvider() {

    private val TAG = "WidgetProvider"

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context != null) {
            if (CameraService.running) {
                context.stopService(intent)
            }
            else {
                context.startForegroundService(intent)
            }
        }
    }


    override fun onUpdate(
        context: Context?,
        appWidgetManager: AppWidgetManager?,
        appWidgetIds: IntArray?
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        Log.d(TAG, "WidgetProvider onUpdate method called")
        appWidgetIds?.forEach { appWidgetId ->
            // Create an Intent to launch ExampleActivity.
            val intent = Intent(context,CameraService::class.java)
            val pendingIntent: PendingIntent = PendingIntent.getBroadcast(context,1,intent,PendingIntent.FLAG_MUTABLE)


            // Get the layout for the widget and attach an on-click listener
            // to the button.
            val views = RemoteViews(
                context?.packageName,
                R.layout.widgetlayout
            )

            views.setOnClickPendingIntent(R.id.widget_button, pendingIntent)

            // Tell the AppWidgetManager to perform an update on the current
            // widget.
            appWidgetManager?.updateAppWidget(appWidgetId, views)
        }
    }
}