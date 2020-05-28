package com.simplemobiletools.notes.pro.extensions

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import com.simplemobiletools.notes.pro.R
import com.simplemobiletools.notes.pro.databases.NotesDatabase
import com.simplemobiletools.notes.pro.helpers.Config
import com.simplemobiletools.notes.pro.helpers.MyWidgetProvider
import com.simplemobiletools.notes.pro.interfaces.NotesDao
import com.simplemobiletools.notes.pro.interfaces.WidgetsDao

val Context.config: Config get() = Config.newInstance(applicationContext)

val Context.notesDB: NotesDao get() = NotesDatabase.getInstance(applicationContext).NotesDao()

val Context.widgetsDB: WidgetsDao get() = NotesDatabase.getInstance(applicationContext).WidgetsDao()

fun Context.updateWidgets() {
    val widgetIDs = AppWidgetManager.getInstance(applicationContext).getAppWidgetIds(ComponentName(applicationContext, MyWidgetProvider::class.java))
    if (widgetIDs.isNotEmpty()) {
        Intent(applicationContext, MyWidgetProvider::class.java).apply {
            action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetIDs)
            sendBroadcast(this)
        }
    }
}

fun Context.getPercentageFontSize() = resources.getDimension(R.dimen.middle_text_size) * (config.fontSizePercentage / 100f)
