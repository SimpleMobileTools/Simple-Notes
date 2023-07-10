package com.simplemobiletools.notes.pro.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import androidx.activity.result.contract.ActivityResultContracts
import com.simplemobiletools.commons.dialogs.RadioGroupDialog
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.helpers.*
import com.simplemobiletools.commons.models.RadioItem
import com.simplemobiletools.notes.pro.R
import com.simplemobiletools.notes.pro.dialogs.ExportNotesDialog
import com.simplemobiletools.notes.pro.extensions.config
import com.simplemobiletools.notes.pro.extensions.requestUnlockNotes
import com.simplemobiletools.notes.pro.extensions.updateWidgets
import com.simplemobiletools.notes.pro.extensions.widgetsDB
import com.simplemobiletools.notes.pro.helpers.*
import com.simplemobiletools.notes.pro.models.Note
import com.simplemobiletools.notes.pro.models.Widget
import kotlinx.android.synthetic.main.activity_settings.*
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.Locale
import kotlin.system.exitProcess

class SettingsActivity : SimpleActivity() {
    private val notesFileType = "application/json"

    override fun onCreate(savedInstanceState: Bundle?) {
        isMaterialActivity = true
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        updateMaterialActivityViews(settings_coordinator, settings_holder, useTransparentNavigation = true, useTopSearchMenu = false)
        setupMaterialScrollListener(settings_nested_scrollview, settings_toolbar)
    }

    override fun onResume() {
        super.onResume()
        setupToolbar(settings_toolbar, NavigationIcon.Arrow)

        setupCustomizeColors()
        setupUseEnglish()
        setupLanguage()
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
        setupIncognitoMode()
        setupCustomizeWidgetColors()
        setupNotesExport()
        setupNotesImport()
        updateTextColors(settings_nested_scrollview)

        arrayOf(
            settings_color_customization_section_label,
            settings_general_settings_label,
            settings_text_label,
            settings_startup_label,
            settings_saving_label,
            settings_migrating_label,
        ).forEach {
            it.setTextColor(getProperPrimaryColor())
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        updateMenuItemColors(menu)
        return super.onCreateOptionsMenu(menu)
    }

    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            toast(R.string.importing)
            importNotes(uri)
        }
    }

    private val saveDocument = registerForActivityResult(ActivityResultContracts.CreateDocument(notesFileType)) { uri ->
        if (uri != null) {
            toast(R.string.exporting)
            NotesHelper(this).getNotes { notes ->
                requestUnlockNotes(notes) { unlockedNotes ->
                    val notLockedNotes = notes.filterNot { it.isLocked() }
                    val notesToExport = unlockedNotes + notLockedNotes
                    exportNotes(notesToExport, uri)
                }
            }
        }
    }

    private fun setupCustomizeColors() {
        settings_color_customization_holder.setOnClickListener {
            startCustomizationActivity()
        }
    }

    private fun setupUseEnglish() {
        settings_use_english_holder.beVisibleIf((config.wasUseEnglishToggled || Locale.getDefault().language != "en") && !isTiramisuPlus())
        settings_use_english.isChecked = config.useEnglish
        settings_use_english_holder.setOnClickListener {
            settings_use_english.toggle()
            config.useEnglish = settings_use_english.isChecked
            exitProcess(0)
        }
    }

    private fun setupLanguage() {
        settings_language.text = Locale.getDefault().displayLanguage
        settings_language_holder.beVisibleIf(isTiramisuPlus())
        settings_language_holder.setOnClickListener {
            launchChangeAppLanguageIntent()
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
            updateWidgets()
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
                RadioItem(FONT_SIZE_60_PERCENT, getFontSizePercentText(FONT_SIZE_60_PERCENT)),
                RadioItem(FONT_SIZE_75_PERCENT, getFontSizePercentText(FONT_SIZE_75_PERCENT)),
                RadioItem(FONT_SIZE_90_PERCENT, getFontSizePercentText(FONT_SIZE_90_PERCENT)),
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
                RadioItem(GRAVITY_RIGHT, getString(R.string.right))
            )

            RadioGroupDialog(this@SettingsActivity, items, config.gravity) {
                config.gravity = it as Int
                settings_gravity.text = getGravityText()
                updateWidgets()
            }
        }
    }

    private fun getGravityText() = getString(
        when (config.gravity) {
            GRAVITY_LEFT -> R.string.left
            GRAVITY_CENTER -> R.string.center
            else -> R.string.right
        }
    )

    private fun setupCursorPlacement() {
        settings_cursor_placement.isChecked = config.placeCursorToEnd
        settings_cursor_placement_holder.setOnClickListener {
            settings_cursor_placement.toggle()
            config.placeCursorToEnd = settings_cursor_placement.isChecked
        }
    }

    private fun setupCustomizeWidgetColors() {
        var widgetToCustomize: Widget? = null

        settings_widget_color_customization_holder.setOnClickListener {
            Intent(this, WidgetConfigureActivity::class.java).apply {
                putExtra(IS_CUSTOMIZING_COLORS, true)

                widgetToCustomize?.apply {
                    putExtra(CUSTOMIZED_WIDGET_ID, widgetId)
                    putExtra(CUSTOMIZED_WIDGET_KEY_ID, id)
                    putExtra(CUSTOMIZED_WIDGET_NOTE_ID, noteId)
                    putExtra(CUSTOMIZED_WIDGET_BG_COLOR, widgetBgColor)
                    putExtra(CUSTOMIZED_WIDGET_TEXT_COLOR, widgetTextColor)
                    putExtra(CUSTOMIZED_WIDGET_SHOW_TITLE, widgetShowTitle)
                }

                startActivity(this)
            }
        }

        ensureBackgroundThread {
            val widgets = widgetsDB.getWidgets().filter { it.widgetId != 0 }
            if (widgets.size == 1) {
                widgetToCustomize = widgets.first()
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

    private fun setupNotesExport() {
        settings_export_notes_holder.setOnClickListener {
            ExportNotesDialog(this) { filename ->
                saveDocument.launch(filename)
            }
        }
    }

    private fun setupNotesImport() {
        settings_import_notes_holder.setOnClickListener {
            getContent.launch(notesFileType)
        }
    }

    private fun exportNotes(notes: List<Note>, uri: Uri) {
        if (notes.isEmpty()) {
            toast(R.string.no_entries_for_exporting)
        } else {
            try {
                val outputStream = contentResolver.openOutputStream(uri)!!

                val jsonString = Json.encodeToString(notes)
                outputStream.use {
                    it.write(jsonString.toByteArray())
                }
                toast(R.string.exporting_successful)
            } catch (e: Exception) {
                showErrorToast(e)
            }
        }
    }

    private fun importNotes(uri: Uri) {
        try {
            val jsonString = contentResolver.openInputStream(uri)!!.use { inputStream ->
                inputStream.bufferedReader().readText()
            }
            val objects = Json.decodeFromString<List<Note>>(jsonString)
            if (objects.isEmpty()) {
                toast(R.string.no_entries_for_importing)
                return
            }
            NotesHelper(this).importNotes(this, objects) { importResult ->
                when (importResult) {
                    NotesHelper.ImportResult.IMPORT_OK -> toast(R.string.importing_successful)
                    NotesHelper.ImportResult.IMPORT_PARTIAL -> toast(R.string.importing_some_entries_failed)
                    else -> toast(R.string.importing_failed)
                }
            }
        } catch (_: SerializationException) {
            toast(R.string.invalid_file_format)
        } catch (_: IllegalArgumentException) {
            toast(R.string.invalid_file_format)
        } catch (e: Exception) {
            showErrorToast(e)
        }
    }
}
