package com.simplemobiletools.notes.helpers

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.RemoteViews
import com.simplemobiletools.commons.extensions.getLaunchIntent
import com.simplemobiletools.commons.extensions.setBackgroundColor
import com.simplemobiletools.notes.R
import com.simplemobiletools.notes.activities.SplashActivity
import com.simplemobiletools.notes.extensions.config
import com.simplemobiletools.notes.extensions.dbHelper
import com.simplemobiletools.notes.services.WidgetService

class MyWidgetProvider : AppWidgetProvider() {
    private fun setupAppOpenIntent(context: Context, views: RemoteViews, id: Int, noteId: Int) {
        val intent = context.getLaunchIntent() ?: Intent(context, SplashActivity::class.java)
        intent.putExtra(OPEN_NOTE_ID, noteId)
        val pendingIntent = PendingIntent.getActivity(context, noteId, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        views.setOnClickPendingIntent(id, pendingIntent)
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        val widgets = context.dbHelper.getWidgets()
        widgets.forEach {
            val widgetId = it.widgetId
            val noteId = it.noteId
            val views = RemoteViews(context.packageName, R.layout.widget)
            views.setBackgroundColor(R.id.notes_widget_holder, context.config.widgetBgColor)
            setupAppOpenIntent(context, views, R.id.notes_widget_holder, noteId)

            Intent(context, WidgetService::class.java).apply {
                putExtra(NOTE_ID, noteId)
                data = Uri.parse(this.toUri(Intent.URI_INTENT_SCHEME))
                views.setRemoteAdapter(R.id.notes_widget_listview, this)
            }

            val startActivityIntent = context.getLaunchIntent() ?: Intent(context, SplashActivity::class.java)
            startActivityIntent.putExtra(OPEN_NOTE_ID, noteId)
            val startActivityPendingIntent = PendingIntent.getActivity(context, widgetId, startActivityIntent, PendingIntent.FLAG_UPDATE_CURRENT)
            views.setPendingIntentTemplate(R.id.notes_widget_listview, startActivityPendingIntent)

            appWidgetManager.updateAppWidget(widgetId, views)
            appWidgetManager.notifyAppWidgetViewDataChanged(widgetId, R.id.notes_widget_listview)
        }
    }
}
