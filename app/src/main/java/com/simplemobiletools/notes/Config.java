package com.simplemobiletools.notes;

import android.content.Context;
import android.content.SharedPreferences;

public class Config {
    private SharedPreferences mPrefs;

    public static Config newInstance(Context context) {
        return new Config(context);
    }

    private Config(Context context) {
        mPrefs = context.getSharedPreferences(Constants.PREFS_KEY, Context.MODE_PRIVATE);
    }

    public boolean getIsFirstRun() {
        return mPrefs.getBoolean(Constants.IS_FIRST_RUN, true);
    }

    public void setIsFirstRun(boolean firstRun) {
        mPrefs.edit().putBoolean(Constants.IS_FIRST_RUN, firstRun).apply();
    }

    public boolean getIsDarkTheme() {
        return mPrefs.getBoolean(Constants.IS_DARK_THEME, false);
    }

    public void setIsDarkTheme(boolean isDarkTheme) {
        mPrefs.edit().putBoolean(Constants.IS_DARK_THEME, isDarkTheme).apply();
    }

    public int getFontSize() {
        return mPrefs.getInt(Constants.FONT_SIZE, Constants.FONT_SIZE_MEDIUM);
    }

    public void setFontSize(int size) {
        mPrefs.edit().putInt(Constants.FONT_SIZE, size).apply();
    }

    public int getCurrentNoteId() {
        return mPrefs.getInt(Constants.CURRENT_NOTE_ID, 1);
    }

    public void setCurrentNoteId(int id) {
        mPrefs.edit().putInt(Constants.CURRENT_NOTE_ID, id).apply();
    }

    public int getWidgetNoteId() {
        return mPrefs.getInt(Constants.WIDGET_NOTE_ID, 1);
    }

    public void setWidgetNoteId(int id) {
        mPrefs.edit().putInt(Constants.WIDGET_NOTE_ID, id).apply();
    }
}
