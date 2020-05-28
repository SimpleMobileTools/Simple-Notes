package com.simplemobiletools.notes.pro.helpers

import android.graphics.Color

const val NOTE_ID = "note_id"
const val OPEN_NOTE_ID = "open_note_id"
const val DONE_CHECKLIST_ITEM_ALPHA = 0.4f
const val CUSTOMIZED_WIDGET_ID = "customized_widget_id"
const val CUSTOMIZED_WIDGET_KEY_ID = "customized_widget_key_id"
const val CUSTOMIZED_WIDGET_NOTE_ID = "customized_widget_note_id"
const val CUSTOMIZED_WIDGET_BG_COLOR = "customized_widget_bg_color"
const val CUSTOMIZED_WIDGET_TEXT_COLOR = "customized_widget_text_color"
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
const val MOVE_UNDONE_CHECKLIST_ITEMS = "move_undone_checklist_items"
const val FONT_SIZE_PERCENTAGE = "font_size_percentage"

// gravity
const val GRAVITY_LEFT = 0
const val GRAVITY_CENTER = 1
const val GRAVITY_RIGHT = 2

// note types
enum class NoteType(val value: Int) { TYPE_TEXT(0), TYPE_CHECKLIST(1) }

// mime types
const val MIME_TEXT_PLAIN = "text/plain"

// font size percentage options
const val FONT_SIZE_50_PERCENT = 50
const val FONT_SIZE_75_PERCENT = 75
const val FONT_SIZE_100_PERCENT = 100
const val FONT_SIZE_125_PERCENT = 125
const val FONT_SIZE_150_PERCENT = 150
const val FONT_SIZE_175_PERCENT = 175
const val FONT_SIZE_200_PERCENT = 200
const val FONT_SIZE_250_PERCENT = 250
const val FONT_SIZE_300_PERCENT = 300
