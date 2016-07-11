package com.simplemobiletools.notes.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;

import com.simplemobiletools.notes.Config;
import com.simplemobiletools.notes.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SettingsActivity extends AppCompatActivity {
    @BindView(R.id.settings_autosave) SwitchCompat mAutosaveSwitch;

    private static Config mConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        mConfig = Config.newInstance(getApplicationContext());
        ButterKnife.bind(this);

        setupAutosave();
    }

    private void setupAutosave() {
        mAutosaveSwitch.setChecked(mConfig.getIsAutosaveEnabled());
    }

    @OnClick(R.id.settings_autosave_holder)
    public void handleAutosave() {
        mAutosaveSwitch.setChecked(!mAutosaveSwitch.isChecked());
        mConfig.setIsAutosaveEnabled(mAutosaveSwitch.isChecked());
    }
}
