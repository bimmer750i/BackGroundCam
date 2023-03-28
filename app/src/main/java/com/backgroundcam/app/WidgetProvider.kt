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

    //Сначала нужно прописать в манифесте
    // Для настройки виджета смотреть @xml/example_appwidget_info
    //Два разных файла для разных версий, они незначительно отличаются

    private val TAG = "WidgetProvider"

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)
    }


    override fun onUpdate(
        context: Context?,
        appWidgetManager: AppWidgetManager?,
        appWidgetIds: IntArray?
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        Log.d(TAG, "WidgetProvider onUpdate method called")
        appWidgetIds?.forEach { appWidgetId ->

            // Задаем интент для компонента
            val intent = Intent(context,CameraService::class.java)

            // ПендингИнтент для запуска компонента, впоследствии задaется для кнопки виджета, в данном случае
            // стартует foregroundservice с интентом, который задается выше

            //val pendingIntent: PendingIntent = PendingIntent.getForegroundService(context,1,intent,PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_CANCEL_CURRENT)


            // Получение View для последующего назначения ClickListener-a
            val views = RemoteViews(
                context?.packageName,
                R.layout.widgetlayout
            )

            //Задаем ClickListener

            //views.setOnClickPendingIntent(R.id.widget_button, pendingIntent)

            //Применяем изменения при добавлении виджета
            appWidgetManager?.updateAppWidget(appWidgetId, views)
        }
    }
}