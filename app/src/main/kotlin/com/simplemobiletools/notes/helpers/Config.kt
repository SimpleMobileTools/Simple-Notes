package com.simplemobiletools.notes.helpers

import android.content.Context
import com.simplemobiletools.commons.helpers.BaseConfig

class Config(context: Context) : BaseConfig(context) {
    companion object {
        fun newInstance(context: Context) = Config(context)
    }

    var displaySuccess: Boolean
        get() = prefs.getBoolean(DISPLAY_SUCCESS, false)
        set(displaySuccess) = prefs.edit().putBoolean(DISPLAY_SUCCESS, displaySuccess).apply()

    var clickableLinks: Boolean
        get() = prefs.getBoolean(CLICKABLE_LINKS, false)
        set(clickableLinks) = prefs.edit().putBoolean(CLICKABLE_LINKS, clickableLinks).apply()

    var fontSize: Int
        get() = prefs.getInt(FONT_SIZE, FONT_SIZE_NORMAL)
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

    var placeCursorToEnd: Boolean
        get() = prefs.getBoolean(CURSOR_PLACEMENT, true)
        set(placement) = prefs.edit().putBoolean(CURSOR_PLACEMENT, placement).apply()
}
