package com.simplemobiletools.notes.activities

import android.content.res.Resources
import android.os.Bundle
import android.view.View
import com.simplemobiletools.commons.dialogs.RadioGroupDialog
import com.simplemobiletools.commons.extensions.updateTextColors
import com.simplemobiletools.commons.models.RadioItem
import com.simplemobiletools.notes.R
import com.simplemobiletools.notes.extensions.config
import com.simplemobiletools.notes.extensions.updateWidget
import com.simplemobiletools.notes.helpers.*
import com.simplemobiletools.notes.models.Note
import kotlinx.android.synthetic.main.activity_settings.*

class SettingsActivity : SimpleActivity() {
    lateinit var res: Resources

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        res = resources
    }

    override fun onResume() {
        super.onResume()

        setupCustomizeColors()
        setupDisplaySuccess()
        setupClickableLinks()
        setupFontSize()
        setupGravity()
        setupWidgetNote()
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
            val items = arrayListOf(
                    RadioItem(FONT_SIZE_SMALL, res.getString(R.string.small)),
                    RadioItem(FONT_SIZE_NORMAL, res.getString(R.string.normal)),
                    RadioItem(FONT_SIZE_LARGE, res.getString(R.string.large)),
                    RadioItem(FONT_SIZE_EXTRA_LARGE, res.getString(R.string.extra_large)))

            RadioGroupDialog(this@SettingsActivity, items, config.fontSize) {
                config.fontSize = it as Int
                settings_font_size.text = getFontSizeText()
                updateWidget()
            }
        }
    }

    private fun getFontSizeText() = getString(when (config.fontSize) {
        FONT_SIZE_SMALL -> R.string.small
        FONT_SIZE_NORMAL -> R.string.normal
        FONT_SIZE_LARGE -> R.string.large
        else -> R.string.extra_large
    })

    private fun setupGravity() {
        settings_gravity.text = getGravityText()
        settings_gravity_holder.setOnClickListener {
            val items = arrayListOf(
                    RadioItem(GRAVITY_LEFT, res.getString(R.string.left)),
                    RadioItem(GRAVITY_CENTER, res.getString(R.string.center)),
                    RadioItem(GRAVITY_RIGHT, res.getString(R.string.right)))

            RadioGroupDialog(this@SettingsActivity, items, config.gravity) {
                config.gravity = it as Int
                settings_gravity.text = getGravityText()
                updateWidget()
            }
        }
    }

    private fun getGravityText() = getString(when (config.gravity) {
        GRAVITY_LEFT -> R.string.left
        GRAVITY_CENTER -> R.string.center
        else -> R.string.right
    })

    private fun setupWidgetNote() {
        val notes = DBHelper.newInstance(this).getNotes()
        if (notes.size <= 1) {
            settings_widget_note_holder.visibility = View.GONE
            return
        }

        settings_widget_note.text = getCurrentWidgetNoteTitle(config.widgetNoteId, notes)
        settings_widget_note_holder.setOnClickListener {
            val items = notes.map { RadioItem(it.id, it.title) } as ArrayList

            RadioGroupDialog(this@SettingsActivity, items, config.widgetNoteId) {
                config.widgetNoteId = it as Int
                settings_widget_note.text = getCurrentWidgetNoteTitle(it, notes)
                updateWidget()
            }
        }
    }

    private fun getCurrentWidgetNoteTitle(currentNoteId: Int, notes: List<Note>): String {
        return notes.firstOrNull { it.id == currentNoteId }?.title ?: ""
    }
}
