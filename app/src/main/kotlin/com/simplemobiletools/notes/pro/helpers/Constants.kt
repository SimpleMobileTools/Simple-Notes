package com.simplemobiletools.notes.pro.helpers

import android.graphics.Color
import org.joda.time.DateTime

const val NOTE_ID = "note_id"
const val OPEN_NOTE_ID = "open_note_id"
const val DONE_CHECKLIST_ITEM_ALPHA = 0.4f
const val CUSTOMIZED_WIDGET_ID = "customized_widget_id"
const val CUSTOMIZED_WIDGET_KEY_ID = "customized_widget_key_id"
const val CUSTOMIZED_WIDGET_NOTE_ID = "customized_widget_note_id"
const val CUSTOMIZED_WIDGET_BG_COLOR = "customized_widget_bg_color"
const val CUSTOMIZED_WIDGET_TEXT_COLOR = "customized_widget_text_color"
const val CUSTOMIZED_WIDGET_SHOW_TITLE = "customized_widget_show_title"
const val SHORTCUT_NEW_TEXT_NOTE = "shortcut_new_text_note"
const val SHORTCUT_NEW_CHECKLIST = "shortcut_new_checklist"
const val NEW_TEXT_NOTE = "new_text_note"
const val NEW_CHECKLIST = "new_checklist"
val DEFAULT_WIDGET_TEXT_COLOR = Color.parseColor("#FFF57C00")

// shared preferences
const val CURRENT_NOTE_ID = "current_note_id"
const val AUTOSAVE_NOTES = "autosave_notes"
const val DISPLAY_SUCCESS = "display_success"
const val CLICKABLE_LINKS = "clickable_links"
const val WIDGET_NOTE_ID = "widget_note_id"
const val MONOSPACED_FONT = "monospaced_font"
const val SHOW_KEYBOARD = "show_keyboard"
const val SHOW_NOTE_PICKER = "show_note_picker"
const val SHOW_WORD_COUNT = "show_word_count"
const val GRAVITY = "gravity"
const val CURSOR_PLACEMENT = "cursor_placement"
const val LAST_USED_EXTENSION = "last_used_extension"
const val LAST_USED_SAVE_PATH = "last_used_save_path"
const val ENABLE_LINE_WRAP = "enable_line_wrap"
const val USE_INCOGNITO_MODE = "use_incognito_mode"
const val LAST_CREATED_NOTE_TYPE = "last_created_note_type"
const val MOVE_DONE_CHECKLIST_ITEMS = "move_undone_checklist_items"     // it has been replaced from moving undone items at the top to moving done to bottom
const val FONT_SIZE_PERCENTAGE = "font_size_percentage"
const val EXPORT_MIME_TYPE = "text/plain"
const val ADD_NEW_CHECKLIST_ITEMS_TOP = "add_new_checklist_items_top"

// auto backups
const val AUTOMATIC_BACKUP_REQUEST_CODE = 10001
const val AUTO_BACKUP_INTERVAL_IN_DAYS = 1

// 6 am is the hardcoded automatic backup time, intervals shorter than 1 day are not yet supported.
fun getNextAutoBackupTime(): DateTime {
    val now = DateTime.now()
    val sixHour = now.withHourOfDay(6)
    return if (now.millis < sixHour.millis) {
        sixHour
    } else {
        sixHour.plusDays(AUTO_BACKUP_INTERVAL_IN_DAYS)
    }
}

fun getPreviousAutoBackupTime(): DateTime {
    val nextBackupTime = getNextAutoBackupTime()
    return nextBackupTime.minusDays(AUTO_BACKUP_INTERVAL_IN_DAYS)
}

// gravity
const val GRAVITY_START = 0
const val GRAVITY_CENTER = 1
const val GRAVITY_END = 2

// mime types
const val MIME_TEXT_PLAIN = "text/plain"

// font size percentage options
const val FONT_SIZE_50_PERCENT = 50
const val FONT_SIZE_60_PERCENT = 60
const val FONT_SIZE_75_PERCENT = 75
const val FONT_SIZE_90_PERCENT = 90
const val FONT_SIZE_100_PERCENT = 100
const val FONT_SIZE_125_PERCENT = 125
const val FONT_SIZE_150_PERCENT = 150
const val FONT_SIZE_175_PERCENT = 175
const val FONT_SIZE_200_PERCENT = 200
const val FONT_SIZE_250_PERCENT = 250
const val FONT_SIZE_300_PERCENT = 300
