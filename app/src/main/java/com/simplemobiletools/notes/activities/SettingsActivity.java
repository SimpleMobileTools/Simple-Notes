package com.simplemobiletools.notes.activities;

import android.os.Bundle;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.SwitchCompat;

import com.simplemobiletools.notes.Config;
import com.simplemobiletools.notes.R;
import com.simplemobiletools.notes.Utils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemSelected;

public class SettingsActivity extends SimpleActivity {
    @BindView(R.id.settings_dark_theme) SwitchCompat mDarkThemeSwitch;
    @BindView(R.id.settings_font_size) AppCompatSpinner mFontSizeSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        mConfig = Config.Companion.newInstance(getApplicationContext());
        ButterKnife.bind(this);

        setupDarkTheme();
        setupFontSize();
    }

    private void setupDarkTheme() {
        mDarkThemeSwitch.setChecked(mConfig.isDarkTheme());
    }

    private void setupFontSize() {
        mFontSizeSpinner.setSelection(mConfig.getFontSize());
    }

    @OnClick(R.id.settings_dark_theme_holder)
    public void handleDarkTheme() {
        mDarkThemeSwitch.setChecked(!mDarkThemeSwitch.isChecked());
        mConfig.setDarkTheme(mDarkThemeSwitch.isChecked());
        restartActivity();
    }

    @OnItemSelected(R.id.settings_font_size)
    public void handleFontSize() {
        mConfig.setFontSize(mFontSizeSpinner.getSelectedItemPosition());
        Utils.INSTANCE.updateWidget(getApplicationContext());
    }

    private void restartActivity() {
        TaskStackBuilder.create(getApplicationContext()).addNextIntentWithParentStack(getIntent()).startActivities();
    }
}
