package com.simplemobiletools.notes.activities

import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import com.simplemobiletools.commons.dialogs.RadioGroupDialog
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.helpers.IS_CUSTOMIZING_COLORS
import com.simplemobiletools.commons.helpers.isOreoPlus
import com.simplemobiletools.commons.models.RadioItem
import com.simplemobiletools.notes.R
import com.simplemobiletools.notes.extensions.config
import com.simplemobiletools.notes.extensions.dbHelper
import com.simplemobiletools.notes.extensions.updateWidgets
import com.simplemobiletools.notes.helpers.*
import com.simplemobiletools.notes.models.Note
import kotlinx.android.synthetic.main.activity_settings.*
import java.util.*

class SettingsActivity : SimpleActivity() {
    lateinit var res: Resources
    var notes = ArrayList<Note>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        res = resources
        notes = dbHelper.getNotes()
    }

    override fun onResume() {
        super.onResume()

        setupPurchaseThankYou()
        setupCustomizeColors()
        setupUseEnglish()
        setupAvoidWhatsNew()
        setupAutosaveNotes()
        setupDisplaySuccess()
        setupClickableLinks()
        setupMonospacedFont()
        setupShowKeyboard()
        setupShowNotePicker()
        setupShowWordCount()
        setupEnableLineWrap()
        setupFontSize()
        setupGravity()
        setupCursorPlacement()
        setupPersonalizedLearning()
        setupCustomizeWidgetColors()
        updateTextColors(settings_scrollview)
        setupSectionColors()
    }

    private fun setupSectionColors() {
        val adjustedPrimaryColor = getAdjustedPrimaryColor()
        arrayListOf(text_label, startup_label, saving_label, widgets_label).forEach {
            it.setTextColor(adjustedPrimaryColor)
        }
    }

    private fun setupPurchaseThankYou() {
        settings_purchase_thank_you_holder.beVisibleIf(config.appRunCount > 10 && !isThankYouInstalled())
        settings_purchase_thank_you_holder.setOnClickListener {
            launchPurchaseThankYouIntent()
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

    private fun setupAvoidWhatsNew() {
        settings_avoid_whats_new.isChecked = config.avoidWhatsNew
        settings_avoid_whats_new_holder.setOnClickListener {
            settings_avoid_whats_new.toggle()
            config.avoidWhatsNew = settings_avoid_whats_new.isChecked
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
        settings_show_note_picker_holder.beVisibleIf(notes.size > 1)
        settings_show_note_picker.isChecked = config.showNotePicker
        settings_show_note_picker_holder.setOnClickListener {
            settings_show_note_picker.toggle()
            config.showNotePicker = settings_show_note_picker.isChecked
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
        settings_font_size.text = getFontSizeText()
        settings_font_size_holder.setOnClickListener {
            val items = arrayListOf(
                    RadioItem(FONT_SIZE_SMALL, res.getString(R.string.small)),
                    RadioItem(FONT_SIZE_MEDIUM, res.getString(R.string.medium)),
                    RadioItem(FONT_SIZE_LARGE, res.getString(R.string.large)),
                    RadioItem(FONT_SIZE_EXTRA_LARGE, res.getString(R.string.extra_large)))

            RadioGroupDialog(this@SettingsActivity, items, config.fontSize) {
                config.fontSize = it as Int
                settings_font_size.text = getFontSizeText()
                updateWidgets()
            }
        }
    }

    private fun getFontSizeText() = getString(when (config.fontSize) {
        FONT_SIZE_SMALL -> R.string.small
        FONT_SIZE_MEDIUM -> R.string.medium
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
        settings_customize_widget_colors_holder.setOnClickListener {
            Intent(this, WidgetConfigureActivity::class.java).apply {
                putExtra(IS_CUSTOMIZING_COLORS, true)
                startActivity(this)
            }
        }
    }

    private fun setupPersonalizedLearning() {
        settings_enable_personalized_learning_holder.beVisibleIf(isOreoPlus())
        settings_enable_personalized_learning.isChecked = config.enablePersonalizedLearning
        settings_enable_personalized_learning_holder.setOnClickListener {
            settings_enable_personalized_learning.toggle()
            config.enablePersonalizedLearning = settings_enable_personalized_learning.isChecked
        }
    }
}
