package com.simplemobiletools.notes.extensions

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.view.Gravity
import com.simplemobiletools.notes.R
import com.simplemobiletools.notes.helpers.*

fun Context.getTextSize() =
        when (config.fontSize) {
            FONT_SIZE_SMALL -> resources.getDimension(R.dimen.smaller_text_size)
            FONT_SIZE_LARGE -> resources.getDimension(R.dimen.big_text_size)
            FONT_SIZE_EXTRA_LARGE -> resources.getDimension(R.dimen.extra_big_text_size)
            else -> resources.getDimension(R.dimen.bigger_text_size)
        }

fun Context.getTextGravity() =
        when (config.gravity) {
            GRAVITY_CENTER -> Gravity.CENTER_HORIZONTAL
            GRAVITY_RIGHT -> Gravity.RIGHT
            else -> Gravity.LEFT
        }

fun Context.updateWidget() {
    val widgetManager = AppWidgetManager.getInstance(this)
    val ids = widgetManager.getAppWidgetIds(ComponentName(this, MyWidgetProvider::class.java))

    Intent(this, MyWidgetProvider::class.java).apply {
        action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
        sendBroadcast(this)
    }
}

val Context.config: Config get() = Config.newInstance(this)
