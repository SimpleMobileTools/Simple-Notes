package com.simplemobiletools.notes.extensions

import android.content.Context
import android.content.Intent
import com.simplemobiletools.notes.R
import com.simplemobiletools.notes.helpers.*

val Context.config: Config get() = Config.newInstance(applicationContext)

val Context.dbHelper: DBHelper get() = DBHelper.newInstance(applicationContext)

fun Context.getTextSize() = when (config.fontSize) {
    FONT_SIZE_SMALL -> resources.getDimension(R.dimen.smaller_text_size)
    FONT_SIZE_LARGE -> resources.getDimension(R.dimen.big_text_size)
    FONT_SIZE_EXTRA_LARGE -> resources.getDimension(R.dimen.extra_big_text_size)
    else -> resources.getDimension(R.dimen.bigger_text_size)
}

fun Context.updateWidgets() {
    dbHelper.getNotes().forEach {
        updateNoteWidget(it.id)
    }
}

fun Context.updateNoteWidget(noteId: Int) {
    val widgetIds = dbHelper.getNoteWidgetIds(noteId)
    widgetIds.forEach {
        Intent(this, MyWidgetProvider::class.java).apply {
            action = UPDATE_WIDGET
            putExtra(WIDGET_ID, it)
            putExtra(NOTE_ID, noteId)
            sendBroadcast(this)
        }
    }
}
