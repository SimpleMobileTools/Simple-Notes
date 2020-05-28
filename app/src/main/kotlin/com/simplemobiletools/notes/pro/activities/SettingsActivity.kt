package com.simplemobiletools.notes.pro.activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import com.simplemobiletools.commons.dialogs.RadioGroupDialog
import com.simplemobiletools.commons.extensions.beGone
import com.simplemobiletools.commons.extensions.beVisibleIf
import com.simplemobiletools.commons.extensions.getAdjustedPrimaryColor
import com.simplemobiletools.commons.extensions.updateTextColors
import com.simplemobiletools.commons.helpers.IS_CUSTOMIZING_COLORS
import com.simplemobiletools.commons.helpers.ensureBackgroundThread
import com.simplemobiletools.commons.helpers.isOreoPlus
import com.simplemobiletools.commons.models.RadioItem
import com.simplemobiletools.notes.pro.R
import com.simplemobiletools.notes.pro.extensions.config
import com.simplemobiletools.notes.pro.extensions.updateWidgets
import com.simplemobiletools.notes.pro.extensions.widgetsDB
import com.simplemobiletools.notes.pro.helpers.*
import com.simplemobiletools.notes.pro.models.Widget
import kotlinx.android.synthetic.main.activity_settings.*
import java.util.*

class SettingsActivity : SimpleActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
    }

    override fun onResume() {
        super.onResume()

        setupCustomizeColors()
        setupUseEnglish()
        setupAutosaveNotes()
        setupDisplaySuccess()
        setupClickableLinks()
        setupMonospacedFont()
        setupShowKeyboard()
        setupShowNotePicker()
        setupMoveUndoneChecklistItems()
        setupShowWordCount()
        setupEnableLineWrap()
        setupFontSize()
        setupGravity()
        setupCursorPlacement()
        setupIncognitoMode()
        setupCustomizeWidgetColors()
        updateTextColors(settings_scrollview)
        setupSectionColors()
        invalidateOptionsMenu()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        updateMenuItemColors(menu)
        return super.onCreateOptionsMenu(menu)
    }

    private fun setupSectionColors() {
        val adjustedPrimaryColor = getAdjustedPrimaryColor()
        arrayListOf(text_label, startup_label, saving_label, widgets_label).forEach {
            it.setTextColor(adjustedPrimaryColor)
        }
    }

    private fun setupCustomizeColors() {
        settings_customize_colors_holder.setOnClickListener {
            startCustomizationActivity()
        }
    }

    private fun setupUseEnglish() {
        settings_use_english_holder.beVisibleIf(config.wasUseEnglishToggled || Locale.getDefault().language != "en")
        settings_use_english.isChecked = config.useEnglish
        settings_use_english_holder.setOnClickListener {
            settings_use_english.toggle()
            config.useEnglish = settings_use_english.isChecked
            System.exit(0)
        }
    }

    private fun setupAutosaveNotes() {
        settings_autosave_notes.isChecked = config.autosaveNotes
        settings_autosave_notes_holder.setOnClickListener {
            settings_autosave_notes.toggle()
            config.autosaveNotes = settings_autosave_notes.isChecked
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

    private fun setupMonospacedFont() {
        settings_monospaced_font.isChecked = config.monospacedFont
        settings_monospaced_font_holder.setOnClickListener {
            settings_monospaced_font.toggle()
            config.monospacedFont = settings_monospaced_font.isChecked
        }
    }

    private fun setupShowKeyboard() {
        settings_show_keyboard.isChecked = config.showKeyboard
        settings_show_keyboard_holder.setOnClickListener {
            settings_show_keyboard.toggle()
            config.showKeyboard = settings_show_keyboard.isChecked
        }
    }

    private fun setupShowNotePicker() {
        NotesHelper(this).getNotes {
            settings_show_note_picker_holder.beVisibleIf(it.size > 1)
        }

        settings_show_note_picker.isChecked = config.showNotePicker
        settings_show_note_picker_holder.setOnClickListener {
            settings_show_note_picker.toggle()
            config.showNotePicker = settings_show_note_picker.isChecked
        }
    }

    private fun setupMoveUndoneChecklistItems() {
        settings_move_undone_checklist_items.isChecked = config.moveUndoneChecklistItems
        settings_move_undone_checklist_items_holder.setOnClickListener {
            settings_move_undone_checklist_items.toggle()
            config.moveUndoneChecklistItems = settings_move_undone_checklist_items.isChecked
        }
    }

    private fun setupShowWordCount() {
        settings_show_word_count.isChecked = config.showWordCount
        settings_show_word_count_holder.setOnClickListener {
            settings_show_word_count.toggle()
            config.showWordCount = settings_show_word_count.isChecked
        }
    }

    private fun setupEnableLineWrap() {
        settings_enable_line_wrap.isChecked = config.enableLineWrap
        settings_enable_line_wrap_holder.setOnClickListener {
            settings_enable_line_wrap.toggle()
            config.enableLineWrap = settings_enable_line_wrap.isChecked
        }
    }

    private fun setupFontSize() {
        settings_font_size.text = getFontSizePercentText(config.fontSizePercentage)
        settings_font_size_holder.setOnClickListener {
            val items = arrayListOf(
                RadioItem(FONT_SIZE_50_PERCENT, getFontSizePercentText(FONT_SIZE_50_PERCENT)),
                RadioItem(FONT_SIZE_75_PERCENT, getFontSizePercentText(FONT_SIZE_75_PERCENT)),
                RadioItem(FONT_SIZE_100_PERCENT, getFontSizePercentText(FONT_SIZE_100_PERCENT)),
                RadioItem(FONT_SIZE_125_PERCENT, getFontSizePercentText(FONT_SIZE_125_PERCENT)),
                RadioItem(FONT_SIZE_150_PERCENT, getFontSizePercentText(FONT_SIZE_150_PERCENT)),
                RadioItem(FONT_SIZE_175_PERCENT, getFontSizePercentText(FONT_SIZE_175_PERCENT)),
                RadioItem(FONT_SIZE_200_PERCENT, getFontSizePercentText(FONT_SIZE_200_PERCENT)),
                RadioItem(FONT_SIZE_250_PERCENT, getFontSizePercentText(FONT_SIZE_250_PERCENT)),
                RadioItem(FONT_SIZE_300_PERCENT, getFontSizePercentText(FONT_SIZE_300_PERCENT))
            )

            RadioGroupDialog(this@SettingsActivity, items, config.fontSizePercentage) {
                config.fontSizePercentage = it as Int
                settings_font_size.text = getFontSizePercentText(config.fontSizePercentage)
                updateWidgets()
            }
        }
    }

    private fun getFontSizePercentText(fontSizePercentage: Int): String = "$fontSizePercentage%"

    private fun setupGravity() {
        settings_gravity.text = getGravityText()
        settings_gravity_holder.setOnClickListener {
            val items = arrayListOf(
                RadioItem(GRAVITY_LEFT, getString(R.string.left)),
                RadioItem(GRAVITY_CENTER, getString(R.string.center)),
                RadioItem(GRAVITY_RIGHT, getString(R.string.right)))

            RadioGroupDialog(this@SettingsActivity, items, config.gravity) {
                config.gravity = it as Int
                settings_gravity.text = getGravityText()
                updateWidgets()
            }
        }
    }

    private fun getGravityText() = getString(when (config.gravity) {
        GRAVITY_LEFT -> R.string.left
        GRAVITY_CENTER -> R.string.center
        else -> R.string.right
    })

    private fun setupCursorPlacement() {
        settings_cursor_placement.isChecked = config.placeCursorToEnd
        settings_cursor_placement_holder.setOnClickListener {
            settings_cursor_placement.toggle()
            config.placeCursorToEnd = settings_cursor_placement.isChecked
        }
    }

    private fun setupCustomizeWidgetColors() {
        var widgetToCustomize: Widget? = null

        settings_customize_widget_colors_holder.setOnClickListener {
            Intent(this, WidgetConfigureActivity::class.java).apply {
                putExtra(IS_CUSTOMIZING_COLORS, true)

                widgetToCustomize?.apply {
                    putExtra(CUSTOMIZED_WIDGET_ID, widgetId)
                    putExtra(CUSTOMIZED_WIDGET_KEY_ID, id)
                    putExtra(CUSTOMIZED_WIDGET_NOTE_ID, noteId)
                    putExtra(CUSTOMIZED_WIDGET_BG_COLOR, widgetBgColor)
                    putExtra(CUSTOMIZED_WIDGET_TEXT_COLOR, widgetTextColor)
                }

                startActivity(this)
            }
        }

        ensureBackgroundThread {
            val widgets = widgetsDB.getWidgets().filter { it.widgetId != 0 }
            if (widgets.size == 1) {
                widgetToCustomize = widgets.first()
            } else if (widgets.size > 1) {
                arrayListOf(widgets_label, settings_customize_widget_colors_holder).forEach {
                    it.beGone()
                }
            }
        }
    }

    private fun setupIncognitoMode() {
        settings_use_incognito_mode_holder.beVisibleIf(isOreoPlus())
        settings_use_incognito_mode.isChecked = config.useIncognitoMode
        settings_use_incognito_mode_holder.setOnClickListener {
            settings_use_incognito_mode.toggle()
            config.useIncognitoMode = settings_use_incognito_mode.isChecked
        }
    }
}
