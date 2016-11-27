package com.simplemobiletools.notes

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.view.View
import android.widget.RemoteViews
import com.simplemobiletools.notes.R.layout.widget
import com.simplemobiletools.notes.activities.MainActivity
import com.simplemobiletools.notes.databases.DBHelper
import com.simplemobiletools.notes.extensions.getTextSize

class MyWidgetProvider : AppWidgetProvider() {
    lateinit var mDb: DBHelper
    var textIds = arrayOf(R.id.notes_view_left, R.id.notes_view_center, R.id.notes_view_right)

    companion object {
        lateinit var mPrefs: SharedPreferences
        lateinit var mRemoteViews: RemoteViews
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        initVariables(context)
        val defaultColor = context.resources.getColor(R.color.dark_grey)
        val newBgColor = mPrefs.getInt(WIDGET_BG_COLOR, defaultColor)
        val newTextColor = mPrefs.getInt(WIDGET_TEXT_COLOR, Color.WHITE)

        for (id in textIds) {
            mRemoteViews.apply {
                setInt(id, "setBackgroundColor", newBgColor)
                setInt(id, "setTextColor", newTextColor)
                setFloat(id, "setTextSize", context.getTextSize() / context.resources.displayMetrics.density)
                setViewVisibility(id, View.GONE)
            }
        }

        mRemoteViews.setViewVisibility(getProperTextView(context), View.VISIBLE)

        for (widgetId in appWidgetIds) {
            updateWidget(appWidgetManager, widgetId, mRemoteViews)
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds)
    }

    private fun getProperTextView(context: Context): Int {
        return when (Config.newInstance(context).gravity) {
            GRAVITY_CENTER -> R.id.notes_view_center
            GRAVITY_RIGHT -> R.id.notes_view_right
            else -> R.id.notes_view_left
        }
    }

    private fun initVariables(context: Context) {
        mPrefs = context.getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE)
        mDb = DBHelper.newInstance(context)
        mRemoteViews = RemoteViews(context.packageName, widget)
        setupAppOpenIntent(R.id.notes_holder, context)
    }

    private fun setupAppOpenIntent(id: Int, context: Context) {
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, 0)
        mRemoteViews.setOnClickPendingIntent(id, pendingIntent)
    }

    private fun updateWidget(widgetManager: AppWidgetManager, widgetId: Int, remoteViews: RemoteViews) {
        val widgetNoteId = mPrefs.getInt(WIDGET_NOTE_ID, 1)
        val note = mDb.getNote(widgetNoteId)
        for (id in textIds)
            remoteViews.setTextViewText(id, note?.value ?: "")
        widgetManager.updateAppWidget(widgetId, remoteViews)
    }
}
