package com.simplemobiletools.notes.helpers

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import com.simplemobiletools.notes.R
import com.simplemobiletools.notes.R.layout.widget
import com.simplemobiletools.notes.activities.SplashActivity
import com.simplemobiletools.notes.extensions.config
import com.simplemobiletools.notes.extensions.getNoteStoredValue
import com.simplemobiletools.notes.extensions.getTextSize

class MyWidgetProvider : AppWidgetProvider() {
    lateinit var mDb: DBHelper
    var textIds = arrayOf(R.id.notes_view_left, R.id.notes_view_center, R.id.notes_view_right)

    companion object {
        lateinit var mRemoteViews: RemoteViews
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        initVariables(context)
        val config = context.config
        val widgetBgColor = config.widgetBgColor
        val widgetTextColor = config.widgetTextColor

        for (id in textIds) {
            mRemoteViews.apply {
                setInt(id, "setBackgroundColor", widgetBgColor)
                setInt(id, "setTextColor", widgetTextColor)
                setFloat(id, "setTextSize", context.getTextSize() / context.resources.displayMetrics.density)
                setViewVisibility(id, View.GONE)
            }
        }

        mRemoteViews.setViewVisibility(getProperTextView(context), View.VISIBLE)

        for (widgetId in appWidgetIds) {
            updateWidget(appWidgetManager, widgetId, mRemoteViews, context)
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds)
    }

    private fun getProperTextView(context: Context): Int {
        return when (context.config.gravity) {
            GRAVITY_CENTER -> R.id.notes_view_center
            GRAVITY_RIGHT -> R.id.notes_view_right
            else -> R.id.notes_view_left
        }
    }

    private fun initVariables(context: Context) {
        mDb = DBHelper.newInstance(context)
        mRemoteViews = RemoteViews(context.packageName, widget)
        setupAppOpenIntent(R.id.notes_holder, context)
    }

    private fun setupAppOpenIntent(id: Int, context: Context) {
        val intent = Intent(context, SplashActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, 0)
        mRemoteViews.setOnClickPendingIntent(id, pendingIntent)
    }

    private fun updateWidget(widgetManager: AppWidgetManager, widgetId: Int, remoteViews: RemoteViews, context: Context) {
        val note = mDb.getNote(context.config.widgetNoteId)
        for (id in textIds) {
            if (note != null)
                remoteViews.setTextViewText(id, context.getNoteStoredValue(note))
        }
        widgetManager.updateAppWidget(widgetId, remoteViews)
    }
}
