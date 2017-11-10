package com.simplemobiletools.notes.helpers

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import com.simplemobiletools.commons.extensions.setBackgroundColor
import com.simplemobiletools.commons.extensions.setText
import com.simplemobiletools.commons.extensions.setTextSize
import com.simplemobiletools.notes.R
import com.simplemobiletools.notes.activities.SplashActivity
import com.simplemobiletools.notes.extensions.config
import com.simplemobiletools.notes.extensions.dbHelper
import com.simplemobiletools.notes.extensions.getNoteStoredValue
import com.simplemobiletools.notes.extensions.getTextSize

class MyWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        performUpdate(context)
    }

    private fun performUpdate(context: Context) {
        val config = context.config
        val widgetBgColor = config.widgetBgColor
        val widgetTextColor = config.widgetTextColor

        val textIds = arrayOf(R.id.notes_view_left, R.id.notes_view_center, R.id.notes_view_right)
        val appWidgetManager = AppWidgetManager.getInstance(context)
        appWidgetManager.getAppWidgetIds(getComponentName(context)).forEach {
            val views = RemoteViews(context.packageName, R.layout.widget)
            setupAppOpenIntent(context, views, R.id.notes_holder)

            val note = context.dbHelper.getNote(context.config.widgetNoteId)
            for (id in textIds) {
                if (note != null) {
                    views.apply {
                        setText(id, context.getNoteStoredValue(note)!!)
                        setBackgroundColor(id, widgetBgColor)
                        setTextColor(id, widgetTextColor)
                        setTextSize(id, context.getTextSize() / context.resources.displayMetrics.density)
                        setViewVisibility(id, View.GONE)
                    }
                }
            }

            views.setViewVisibility(getProperTextView(context), View.VISIBLE)
            appWidgetManager.updateAppWidget(it, views)
        }
    }

    private fun getComponentName(context: Context) = ComponentName(context, MyWidgetProvider::class.java)

    private fun getProperTextView(context: Context) = when (context.config.gravity) {
        GRAVITY_CENTER -> R.id.notes_view_center
        GRAVITY_RIGHT -> R.id.notes_view_right
        else -> R.id.notes_view_left
    }

    private fun setupAppOpenIntent(context: Context, views: RemoteViews, id: Int) {
        val widgetId = context.config.widgetNoteId
        val intent = Intent(context, SplashActivity::class.java)
        intent.putExtra(OPEN_NOTE_ID, widgetId)
        val pendingIntent = PendingIntent.getActivity(context, widgetId, intent, 0)
        views.setOnClickPendingIntent(id, pendingIntent)
    }
}
