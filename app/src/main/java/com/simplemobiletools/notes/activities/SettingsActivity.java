package com.simplemobiletools.notes.activities;

import android.os.Bundle;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.widget.SwitchCompat;

import com.simplemobiletools.notes.Config;
import com.simplemobiletools.notes.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SettingsActivity extends SimpleActivity {
    @BindView(R.id.settings_dark_theme) SwitchCompat mDarkThemeSwitch;
    @BindView(R.id.settings_autosave) SwitchCompat mAutosaveSwitch;

    private static Config mConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        mConfig = Config.newInstance(getApplicationContext());
        ButterKnife.bind(this);

        setupDarkTheme();
        setupAutosave();
        mConfig.setShouldPromptAutosave(false);
    }

    private void setupDarkTheme() {
        mDarkThemeSwitch.setChecked(mConfig.getIsDarkTheme());
    }

    private void setupAutosave() {
        mAutosaveSwitch.setChecked(mConfig.getIsAutosaveEnabled());
    }

    @OnClick(R.id.settings_dark_theme_holder)
    public void handleDarkTheme() {
        mDarkThemeSwitch.setChecked(!mDarkThemeSwitch.isChecked());
        mConfig.setIsDarkTheme(mDarkThemeSwitch.isChecked());
        restartActivity();
    }

    @OnClick(R.id.settings_autosave_holder)
    public void handleAutosave() {
        mAutosaveSwitch.setChecked(!mAutosaveSwitch.isChecked());
        mConfig.setIsAutosaveEnabled(mAutosaveSwitch.isChecked());
    }

    private void restartActivity() {
        TaskStackBuilder.create(getApplicationContext()).addNextIntentWithParentStack(getIntent()).startActivities();
    }
}
