package com.simplemobiletools.notes.helpers

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteDatabase.CONFLICT_IGNORE
import android.database.sqlite.SQLiteOpenHelper
import com.simplemobiletools.commons.extensions.getIntValue
import com.simplemobiletools.commons.extensions.getStringValue
import com.simplemobiletools.notes.R
import com.simplemobiletools.notes.extensions.config
import com.simplemobiletools.notes.models.Note
import com.simplemobiletools.notes.models.Widget
import java.io.File
import java.util.*

class DBHelper private constructor(private val mContext: Context) : SQLiteOpenHelper(mContext, DB_NAME, null, DB_VERSION) {
    private val mDb: SQLiteDatabase = writableDatabase

    companion object {
        private const val DB_NAME = "notes.db"
        private const val DB_VERSION = 4
        private const val NOTES_TABLE_NAME = "notes"
        private const val WIDGETS_TABLE_NAME = "widgets"

        private const val COL_ID = "id"
        private const val COL_TITLE = "title"
        private const val COL_VALUE = "value"
        private const val COL_TYPE = "type"
        private const val COL_PATH = "path"

        private const val COL_WIDGET_ID = "widget_id"
        private const val COL_NOTE_ID = "note_id"

        fun newInstance(context: Context) = DBHelper(context)
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE $NOTES_TABLE_NAME ($COL_ID INTEGER PRIMARY KEY AUTOINCREMENT, $COL_TITLE TEXT UNIQUE, $COL_VALUE TEXT, $COL_TYPE INTEGER DEFAULT 0, $COL_PATH TEXT)")
        db.execSQL("CREATE TABLE $WIDGETS_TABLE_NAME ($COL_ID INTEGER PRIMARY KEY AUTOINCREMENT, $COL_WIDGET_ID INTEGER DEFAULT 0, $COL_NOTE_ID INTEGER DEFAULT 0)")
        insertFirstNote(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE $NOTES_TABLE_NAME ADD COLUMN $COL_TYPE INTEGER DEFAULT 0")
        }

        if (oldVersion < 3) {
            db.execSQL("ALTER TABLE $NOTES_TABLE_NAME ADD COLUMN $COL_PATH TEXT DEFAULT ''")
        }

        if (oldVersion < 4) {
            db.execSQL("CREATE TABLE $WIDGETS_TABLE_NAME ($COL_ID INTEGER PRIMARY KEY AUTOINCREMENT, $COL_WIDGET_ID INTEGER DEFAULT 0, $COL_NOTE_ID INTEGER DEFAULT 0)")
            insertFirstWidget(db)
        }
    }

    private fun insertFirstNote(db: SQLiteDatabase) {
        val generalNote = mContext.resources.getString(R.string.general_note)
        val note = Note(1, generalNote, "", TYPE_NOTE)
        insertNote(note, db)
    }

    // if a user has exactly 1 widget active, prefill it. Can happen only at upgrading from older app versions
    private fun insertFirstWidget(db: SQLiteDatabase) {
        val widgetIDs = AppWidgetManager.getInstance(mContext).getAppWidgetIds(ComponentName(mContext, MyWidgetProvider::class.java))
        if (widgetIDs.size == 1) {
            val widget = Widget(widgetIDs.first(), mContext.config.widgetNoteId)
            insertWidget(widget, db)
        }
    }

    private fun insertNote(note: Note, db: SQLiteDatabase) {
        val values = fillNoteContentValues(note)
        db.insert(NOTES_TABLE_NAME, null, values)
    }

    private fun insertWidget(widget: Widget, db: SQLiteDatabase) {
        val values = fillWidgetContentValues(widget)
        db.insert(WIDGETS_TABLE_NAME, null, values)
    }

    fun insertNote(note: Note): Int {
        val values = fillNoteContentValues(note)
        return mDb.insertWithOnConflict(NOTES_TABLE_NAME, null, values, CONFLICT_IGNORE).toInt()
    }

    fun insertWidget(widget: Widget): Int {
        val values = fillWidgetContentValues(widget)
        return mDb.insertWithOnConflict(WIDGETS_TABLE_NAME, null, values, CONFLICT_IGNORE).toInt()
    }

    private fun fillNoteContentValues(note: Note): ContentValues {
        return ContentValues().apply {
            put(COL_TITLE, note.title)
            put(COL_VALUE, note.value)
            put(COL_PATH, note.path)
            put(COL_TYPE, 0)
        }
    }

    private fun fillWidgetContentValues(widget: Widget): ContentValues {
        return ContentValues().apply {
            put(COL_WIDGET_ID, widget.widgetId)
            put(COL_NOTE_ID, widget.noteId)
        }
    }

    fun deleteNote(id: Int) {
        mDb.delete(NOTES_TABLE_NAME, "$COL_ID = $id", null)
        mDb.delete(WIDGETS_TABLE_NAME, "$COL_NOTE_ID = $id", null)
    }

    fun doesNoteTitleExist(title: String): Boolean {
        val cols = arrayOf(COL_ID)
        val selection = "$COL_TITLE = ? COLLATE NOCASE"
        val selectionArgs = arrayOf(title)
        var cursor: Cursor? = null
        try {
            cursor = mDb.query(NOTES_TABLE_NAME, cols, selection, selectionArgs, null, null, null)
            return cursor.count == 1
        } finally {
            cursor?.close()
        }
    }

    fun getNotes(): ArrayList<Note> {
        val notes = ArrayList<Note>()
        val cols = arrayOf(COL_ID, COL_TITLE, COL_VALUE, COL_TYPE, COL_PATH)
        var cursor: Cursor? = null
        try {
            cursor = mDb.query(NOTES_TABLE_NAME, cols, null, null, null, null, "$COL_TITLE COLLATE NOCASE ASC")
            if (cursor?.moveToFirst() == true) {
                do {
                    try {
                        val id = cursor.getIntValue(COL_ID)
                        val title = cursor.getStringValue(COL_TITLE)
                        val value = cursor.getStringValue(COL_VALUE)
                        val type = cursor.getIntValue(COL_TYPE)
                        val path = cursor.getStringValue(COL_PATH) ?: ""
                        if (path.isNotEmpty() && !File(path).exists()) {
                            deleteNote(id)
                            continue
                        }

                        val note = Note(id, title, value, type, path)
                        notes.add(note)
                    } catch (e: Exception) {
                        continue
                    }
                } while (cursor.moveToNext())
            }
        } finally {
            cursor?.close()
        }

        return notes
    }

    fun getNoteWithId(id: Int): Note? {
        val cols = arrayOf(COL_TITLE, COL_VALUE, COL_TYPE, COL_PATH)
        val selection = "$COL_ID = ?"
        val selectionArgs = arrayOf(id.toString())
        var note: Note? = null
        var cursor: Cursor? = null
        try {
            cursor = mDb.query(NOTES_TABLE_NAME, cols, selection, selectionArgs, null, null, null)
            if (cursor?.moveToFirst() == true) {
                val title = cursor.getStringValue(COL_TITLE)
                val value = cursor.getStringValue(COL_VALUE)
                val type = cursor.getIntValue(COL_TYPE)
                val path = cursor.getStringValue(COL_PATH) ?: ""
                note = Note(id, title, value, type, path)
            }
        } finally {
            cursor?.close()
        }
        return note
    }

    fun getNoteId(path: String): Int {
        val cols = arrayOf(COL_ID)
        val selection = "$COL_PATH = ?"
        val selectionArgs = arrayOf(path)
        var cursor: Cursor? = null
        try {
            cursor = mDb.query(NOTES_TABLE_NAME, cols, selection, selectionArgs, null, null, null)
            if (cursor?.moveToFirst() == true) {
                return cursor.getIntValue(COL_ID)
            }
        } finally {
            cursor?.close()
        }
        return 0
    }

    fun updateNoteValue(note: Note) {
        val values = ContentValues().apply { put(COL_VALUE, note.value) }
        updateNote(note.id, values)
    }

    fun updateNoteTitle(note: Note) {
        val values = ContentValues().apply { put(COL_TITLE, note.title) }
        updateNote(note.id, values)
    }

    fun updateNotePath(note: Note) {
        val values = ContentValues().apply { put(COL_PATH, note.path) }
        updateNote(note.id, values)
    }

    private fun updateNote(id: Int, values: ContentValues) {
        val selection = "$COL_ID = ?"
        val selectionArgs = arrayOf(id.toString())
        mDb.update(NOTES_TABLE_NAME, values, selection, selectionArgs)
    }

    fun isValidId(id: Int) = id > 0

    fun getNoteWidgetIds(noteId: Int): ArrayList<Int> {
        val widgetIds = ArrayList<Int>()
        val cols = arrayOf(COL_WIDGET_ID)
        val selection = "$COL_NOTE_ID = ?"
        val selectionArgs = arrayOf(noteId.toString())
        var cursor: Cursor? = null
        try {
            cursor = mDb.query(WIDGETS_TABLE_NAME, cols, selection, selectionArgs, null, null, null)
            if (cursor?.moveToFirst() == true) {
                val widgetId = cursor.getIntValue(COL_WIDGET_ID)
                widgetIds.add(widgetId)
            }
        } finally {
            cursor?.close()
        }
        return widgetIds
    }
}
