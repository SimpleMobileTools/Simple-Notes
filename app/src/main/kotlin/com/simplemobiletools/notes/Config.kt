package com.simplemobiletools.notes

import android.content.Context
import android.content.SharedPreferences

class Config(context: Context) {
    private val mPrefs: SharedPreferences

    companion object {
        fun newInstance(context: Context) = Config(context)
    }

    init {
        mPrefs = context.getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE)
    }

    var isFirstRun: Boolean
        get() = mPrefs.getBoolean(IS_FIRST_RUN, true)
        set(firstRun) = mPrefs.edit().putBoolean(IS_FIRST_RUN, firstRun).apply()

    var isDarkTheme: Boolean
        get() = mPrefs.getBoolean(IS_DARK_THEME, false)
        set(isDarkTheme) = mPrefs.edit().putBoolean(IS_DARK_THEME, isDarkTheme).apply()

    var fontSize: Int
        get() = mPrefs.getInt(FONT_SIZE, FONT_SIZE_MEDIUM)
        set(size) = mPrefs.edit().putInt(FONT_SIZE, size).apply()

    var currentNoteId: Int
        get() = mPrefs.getInt(CURRENT_NOTE_ID, 1)
        set(id) = mPrefs.edit().putInt(CURRENT_NOTE_ID, id).apply()

    var widgetNoteId: Int
        get() = mPrefs.getInt(WIDGET_NOTE_ID, 1)
        set(id) = mPrefs.edit().putInt(WIDGET_NOTE_ID, id).apply()
}
