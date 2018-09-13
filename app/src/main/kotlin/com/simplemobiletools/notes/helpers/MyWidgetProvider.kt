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
import com.simplemobiletools.notes.services.WidgetService

class MyWidgetProvider : AppWidgetProvider() {
    private fun performUpdate(context: Context, intent: Intent) {
        val noteId = intent.getIntExtra(NOTE_ID, -1)
        val widgetId = intent.getIntExtra(WIDGET_ID, -1)
        if (noteId == -1 || widgetId == -1) {
            return
        }

        val appWidgetManager = AppWidgetManager.getInstance(context)
        val views = RemoteViews(context.packageName, R.layout.widget)
        views.setBackgroundColor(R.id.notes_widget_holder, context.config.widgetBgColor)
        setupAppOpenIntent(context, views, R.id.notes_widget_holder)

        Intent(context, WidgetService::class.java).apply {
            putExtra(NOTE_ID, noteId)
            data = Uri.parse(this.toUri(Intent.URI_INTENT_SCHEME))
            views.setRemoteAdapter(R.id.notes_widget_listview, this)
        }

        val startActivityIntent = context.getLaunchIntent() ?: Intent(context, SplashActivity::class.java)
        startActivityIntent.putExtra(OPEN_NOTE_ID, widgetId)
        val startActivityPendingIntent = PendingIntent.getActivity(context, widgetId, startActivityIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        views.setPendingIntentTemplate(R.id.notes_widget_listview, startActivityPendingIntent)

        appWidgetManager.updateAppWidget(widgetId, views)
        appWidgetManager.notifyAppWidgetViewDataChanged(widgetId, R.id.notes_widget_listview)
    }

    private fun setupAppOpenIntent(context: Context, views: RemoteViews, id: Int) {
        val widgetId = context.config.widgetNoteId
        val intent = context.getLaunchIntent() ?: Intent(context, SplashActivity::class.java)
        intent.putExtra(OPEN_NOTE_ID, widgetId)
        val pendingIntent = PendingIntent.getActivity(context, widgetId, intent, 0)
        views.setOnClickPendingIntent(id, pendingIntent)
    }

    // use only this way of updating widgets instead of onUpdate, so that we can pass a widget ID too
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        when (action) {
            UPDATE_WIDGET -> performUpdate(context, intent)
            else -> super.onReceive(context, intent)
        }
    }
}
