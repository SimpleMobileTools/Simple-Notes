package com.simplemobiletools.notes

import android.content.Context

object Utils {
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
}
