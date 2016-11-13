package com.simplemobiletools.notes

import android.content.Context
import android.content.SharedPreferences

class Config(context: Context) {
    private val mPrefs: SharedPreferences

    companion object {
        fun newInstance(context: Context): Config {
            return Config(context)
        }
    }

    init {
        mPrefs = context.getSharedPreferences(Constants.PREFS_KEY, Context.MODE_PRIVATE)
    }

    var isFirstRun: Boolean
        get() = mPrefs.getBoolean(Constants.IS_FIRST_RUN, true)
        set(firstRun) = mPrefs.edit().putBoolean(Constants.IS_FIRST_RUN, firstRun).apply()

    var isDarkTheme: Boolean
        get() = mPrefs.getBoolean(Constants.IS_DARK_THEME, false)
        set(isDarkTheme) = mPrefs.edit().putBoolean(Constants.IS_DARK_THEME, isDarkTheme).apply()

    var fontSize: Int
        get() = mPrefs.getInt(Constants.FONT_SIZE, Constants.FONT_SIZE_MEDIUM)
        set(size) = mPrefs.edit().putInt(Constants.FONT_SIZE, size).apply()

    var currentNoteId: Int
        get() = mPrefs.getInt(Constants.CURRENT_NOTE_ID, 1)
        set(id) = mPrefs.edit().putInt(Constants.CURRENT_NOTE_ID, id).apply()

    var widgetNoteId: Int
        get() = mPrefs.getInt(Constants.WIDGET_NOTE_ID, 1)
        set(id) = mPrefs.edit().putInt(Constants.WIDGET_NOTE_ID, id).apply()
}
