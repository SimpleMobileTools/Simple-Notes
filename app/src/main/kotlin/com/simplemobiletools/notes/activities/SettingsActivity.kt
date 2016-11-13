package com.simplemobiletools.notes.activities

import android.os.Bundle
import android.support.v4.app.TaskStackBuilder
import android.view.View
import android.widget.AdapterView
import com.simplemobiletools.notes.R
import kotlinx.android.synthetic.main.activity_settings.*

class SettingsActivity : SimpleActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        setupDarkTheme()
        setupFontSize()
    }

    private fun setupDarkTheme() {
        settings_dark_theme.isChecked = config.isDarkTheme
        settings_dark_theme_holder.setOnClickListener {
            settings_dark_theme.toggle()
            config.isDarkTheme = settings_dark_theme.isChecked
            restartActivity()
        }
    }

    private fun setupFontSize() {
        settings_font_size.setSelection(config.fontSize)
        settings_font_size.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                config.fontSize = settings_font_size.selectedItemPosition
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
    }

    private fun restartActivity() {
        TaskStackBuilder.create(applicationContext).addNextIntentWithParentStack(intent).startActivities()
    }
}
