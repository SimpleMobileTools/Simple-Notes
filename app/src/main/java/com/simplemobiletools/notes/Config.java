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

    public boolean getIsAutosaveEnabled() {
        return mPrefs.getBoolean(Constants.AUTOSAVE, false);
    }

    public void setIsAutosaveEnabled(boolean enabled) {
        mPrefs.edit().putBoolean(Constants.AUTOSAVE, enabled).apply();
    }

    public boolean getShouldPromptAutosave() {
        return mPrefs.getBoolean(Constants.PROMPT_AUTOSAVE, true);
    }

    public void setShouldPromptAutosave(boolean prompt) {
        mPrefs.edit().putBoolean(Constants.PROMPT_AUTOSAVE, prompt).apply();
    }

    public int getFontSize() {
        return mPrefs.getInt(Constants.FONT_SIZE, Constants.FONT_SIZE_MEDIUM);
    }

    public void setFontSize(int size) {
        mPrefs.edit().putInt(Constants.FONT_SIZE, size).apply();
    }
}
