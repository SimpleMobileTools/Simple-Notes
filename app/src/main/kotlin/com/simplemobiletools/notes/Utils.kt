package com.simplemobiletools.notes

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.Toast

object Utils {
    fun showToast(context: Context, resId: Int) {
        Toast.makeText(context, context.resources.getString(resId), Toast.LENGTH_SHORT).show()
    }

    fun getTextSize(context: Context): Float {
        val fontSize = Config.newInstance(context).fontSize
        val res = context.resources
        return when (fontSize) {
            FONT_SIZE_SMALL -> res.getDimension(R.dimen.small_text_size)
            FONT_SIZE_LARGE -> res.getDimension(R.dimen.large_text_size)
            FONT_SIZE_EXTRA_LARGE -> res.getDimension(R.dimen.extra_large_text_size)
            else -> res.getDimension(R.dimen.medium_text_size)
        }
    }

    fun updateWidget(context: Context) {
        val widgetManager = AppWidgetManager.getInstance(context)
        val ids = widgetManager.getAppWidgetIds(ComponentName(context, MyWidgetProvider::class.java))

        val intent = Intent(context, MyWidgetProvider::class.java)
        intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
        context.sendBroadcast(intent)
    }
}
