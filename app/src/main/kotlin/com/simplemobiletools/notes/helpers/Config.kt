package com.simplemobiletools.notes.helpers

import android.content.Context
import com.simplemobiletools.commons.helpers.BaseConfig

class Config(context: Context) : BaseConfig(context) {
    companion object {
        fun newInstance(context: Context) = Config(context)
    }

    var fontSize: Int
        get() = prefs.getInt(FONT_SIZE, FONT_SIZE_MEDIUM)
        set(size) = prefs.edit().putInt(FONT_SIZE, size).apply()

    var gravity: Int
        get() = prefs.getInt(GRAVITY, GRAVITY_LEFT)
        set(size) = prefs.edit().putInt(GRAVITY, size).apply()

    var currentNoteId: Int
        get() = prefs.getInt(CURRENT_NOTE_ID, 1)
        set(id) = prefs.edit().putInt(CURRENT_NOTE_ID, id).apply()

    var widgetNoteId: Int
        get() = prefs.getInt(WIDGET_NOTE_ID, 1)
        set(id) = prefs.edit().putInt(WIDGET_NOTE_ID, id).apply()
}
