package com.simplemobiletools.notes.helpers

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.RemoteViews
import com.simplemobiletools.commons.extensions.setBackgroundColor
import com.simplemobiletools.notes.R
import com.simplemobiletools.notes.activities.SplashActivity
import com.simplemobiletools.notes.extensions.config
import com.simplemobiletools.notes.services.WidgetService

class MyWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        performUpdate(context)
    }

    private fun performUpdate(context: Context) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        appWidgetManager.getAppWidgetIds(getComponentName(context)).forEach {
            val views = RemoteViews(context.packageName, R.layout.widget)
            views.setBackgroundColor(R.id.notes_widget_holder, context.config.widgetBgColor)
            setupAppOpenIntent(context, views, R.id.notes_widget_holder)

            Intent(context, WidgetService::class.java).apply {
                data = Uri.parse(this.toUri(Intent.URI_INTENT_SCHEME))
                views.setRemoteAdapter(R.id.notes_widget_listview, this)
            }

            val widgetId = context.config.widgetNoteId
            val startActivityIntent = Intent(context, SplashActivity::class.java)
            val startActivityPendingIntent = PendingIntent.getActivity(context, widgetId, startActivityIntent, PendingIntent.FLAG_UPDATE_CURRENT)
            views.setPendingIntentTemplate(R.id.notes_widget_listview, startActivityPendingIntent)

            appWidgetManager.updateAppWidget(it, views)
            appWidgetManager.notifyAppWidgetViewDataChanged(it, R.id.notes_widget_listview)
        }
    }

    private fun getComponentName(context: Context) = ComponentName(context, MyWidgetProvider::class.java)

    private fun setupAppOpenIntent(context: Context, views: RemoteViews, id: Int) {
        val widgetId = context.config.widgetNoteId
        val intent = Intent(context, SplashActivity::class.java)
        intent.putExtra(OPEN_NOTE_ID, widgetId)
        val pendingIntent = PendingIntent.getActivity(context, widgetId, intent, 0)
        views.setOnClickPendingIntent(id, pendingIntent)
    }
}
