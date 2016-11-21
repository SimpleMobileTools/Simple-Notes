package com.simplemobiletools.notes.extensions

import android.content.Context
import com.simplemobiletools.notes.*

fun Context.getTextSize() =
        when (Config.newInstance(this).fontSize) {
            FONT_SIZE_SMALL -> resources.getDimension(R.dimen.small_text_size)
            FONT_SIZE_LARGE -> resources.getDimension(R.dimen.large_text_size)
            FONT_SIZE_EXTRA_LARGE -> resources.getDimension(R.dimen.extra_large_text_size)
            else -> resources.getDimension(R.dimen.medium_text_size)
        }
