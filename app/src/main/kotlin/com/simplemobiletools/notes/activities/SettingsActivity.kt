package com.simplemobiletools.notes.activities

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.simplemobiletools.commons.dialogs.RadioGroupDialog
import com.simplemobiletools.commons.extensions.updateTextColors
import com.simplemobiletools.commons.models.RadioItem
import com.simplemobiletools.notes.R
import com.simplemobiletools.notes.extensions.config
import com.simplemobiletools.notes.extensions.updateWidget
import com.simplemobiletools.notes.helpers.DBHelper
import com.simplemobiletools.notes.helpers.FONT_SIZE_LARGE
import com.simplemobiletools.notes.helpers.FONT_SIZE_NORMAL
import com.simplemobiletools.notes.helpers.FONT_SIZE_SMALL
import com.simplemobiletools.notes.models.Note
import kotlinx.android.synthetic.main.activity_settings.*

class SettingsActivity : SimpleActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
    }

    override fun onResume() {
        super.onResume()

        setupCustomizeColors()
        setupDisplaySuccess()
        setupClickableLinks()
        setupFontSize()
        setupWidgetNote()
        setupGravity()
        updateTextColors(settings_scrollview)
    }

    private fun setupCustomizeColors() {
        settings_customize_colors_holder.setOnClickListener {
            startCustomizationActivity()
        }
    }

    private fun setupDisplaySuccess() {
        settings_display_success.isChecked = config.displaySuccess
        settings_display_success_holder.setOnClickListener {
            settings_display_success.toggle()
            config.displaySuccess = settings_display_success.isChecked
        }
    }

    private fun setupClickableLinks() {
        settings_clickable_links.isChecked = config.clickableLinks
        settings_clickable_links_holder.setOnClickListener {
            settings_clickable_links.toggle()
            config.clickableLinks = settings_clickable_links.isChecked
        }
    }

    private fun setupFontSize() {
        settings_font_size.text = getFontSizeText()
        settings_font_size_holder.setOnClickListener {
            val items = arrayOf(
                    RadioItem(0, R.string.small),
                    RadioItem(1, R.string.normal),
                    RadioItem(2, R.string.large),
                    RadioItem(3, R.string.extra_large))

            RadioGroupDialog(this@SettingsActivity, items, config.fontSize) {
                config.fontSize = it
                settings_font_size.text = getFontSizeText()
            }
        }
    }

    private fun getFontSizeText() = getString(when (config.fontSize) {
        FONT_SIZE_SMALL -> R.string.small
        FONT_SIZE_NORMAL -> R.string.normal
        FONT_SIZE_LARGE -> R.string.large
        else -> R.string.extra_large
    })

    private fun setupWidgetNote() {
        val notes = DBHelper.newInstance(this).getNotes()
        if (notes.size <= 1) {
            settings_widget_note_holder.visibility = View.GONE
            return
        }

        val adapter = getSpinnerAdapter(notes)
        settings_widget_note.adapter = adapter

        val noteIndex = getNoteIndexWithId(config.widgetNoteId, notes)
        settings_widget_note.setSelection(noteIndex)
        settings_widget_note.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val note = notes[settings_widget_note.selectedItemPosition]
                config.widgetNoteId = note.id
                updateWidget()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
    }

    private fun setupGravity() {
        settings_gravity.setSelection(config.gravity)
        settings_gravity.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                config.gravity = settings_gravity.selectedItemPosition
                updateWidget()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
    }

    private fun getNoteIndexWithId(id: Int, notes: List<Note>): Int {
        for (i in 0..notes.count() - 1) {
            if (notes[i].id == id) {
                return i
            }
        }
        return 0
    }

    private fun getSpinnerAdapter(notes: List<Note>): ArrayAdapter<String> {
        val titles = notes.map(Note::title)
        val adapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, titles)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        return adapter
    }
}
