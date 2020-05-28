package com.simplemobiletools.notes.pro.extensions

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import com.simplemobiletools.commons.extensions.baseConfig
import com.simplemobiletools.commons.helpers.FONT_SIZE_LARGE
import com.simplemobiletools.commons.helpers.FONT_SIZE_MEDIUM
import com.simplemobiletools.commons.helpers.FONT_SIZE_SMALL
import com.simplemobiletools.notes.pro.R
import com.simplemobiletools.notes.pro.databases.NotesDatabase
import com.simplemobiletools.notes.pro.helpers.*
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

fun Context.getEditorFontSize(): Float {
    val defaultFontSizeDimension = when(resources.getInteger(R.integer.default_font_size)) {
        FONT_SIZE_SMALL -> resources.getDimension(R.dimen.small_text_size)
        FONT_SIZE_MEDIUM -> resources.getDimension(R.dimen.middle_text_size)
        FONT_SIZE_LARGE -> resources.getDimension(R.dimen.big_text_size)
        else -> resources.getDimension(R.dimen.extra_big_text_size)
    }
    return when(baseConfig.fontSize) {
        resources.getInteger(R.integer.default_font_size) -> defaultFontSizeDimension
        else -> defaultFontSizeDimension * baseConfig.fontSize / 100
    }
}
