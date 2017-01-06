package com.simplemobiletools.notes.helpers

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.simplemobiletools.commons.extensions.getIntValue
import com.simplemobiletools.commons.extensions.getStringValue
import com.simplemobiletools.notes.R
import com.simplemobiletools.notes.models.Note
import java.util.*

class DBHelper private constructor(private val mContext: Context) : SQLiteOpenHelper(mContext, DB_NAME, null, DB_VERSION) {
    private val mDb: SQLiteDatabase = writableDatabase

    companion object {
        private val DB_NAME = "notes.db"
        private val DB_VERSION = 3
        private val TABLE_NAME = "notes"

        private val COL_ID = "id"
        private val COL_TITLE = "title"
        private val COL_VALUE = "value"
        private val COL_TYPE = "type"
        private val COL_PATH = "path"

        fun newInstance(context: Context) = DBHelper(context)
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE ${TABLE_NAME} (${COL_ID} INTEGER PRIMARY KEY, ${COL_TITLE} TEXT UNIQUE, ${COL_VALUE} TEXT, ${COL_TYPE} INTEGER DEFAULT 0, ${COL_PATH} TEXT)")
        insertFirstNote(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2)
            db.execSQL("ALTER TABLE ${TABLE_NAME} ADD COLUMN ${COL_TYPE} INTEGER DEFAULT 0")

        if (oldVersion < 3)
            db.execSQL("ALTER TABLE ${TABLE_NAME} ADD COLUMN ${COL_PATH} TEXT")
    }

    private fun insertFirstNote(db: SQLiteDatabase) {
        val generalNote = mContext.resources.getString(R.string.general_note)
        val note = Note(1, generalNote, "", TYPE_NOTE)
        insertNote(note, db)
    }

    private fun insertNote(note: Note, db: SQLiteDatabase) {
        val values = fillContentValues(note)
        db.insert(TABLE_NAME, null, values)
    }

    fun insertNote(note: Note): Int {
        val values = fillContentValues(note)
        return mDb.insert(TABLE_NAME, null, values).toInt()
    }

    private fun fillContentValues(note: Note): ContentValues {
        return ContentValues().apply {
            put(COL_TITLE, note.title)
            put(COL_VALUE, note.value)
            put(COL_TYPE, 0)
        }
    }

    fun deleteNote(id: Int) {
        mDb.delete(TABLE_NAME, "${COL_ID} = $id", null)
    }

    fun doesTitleExist(title: String): Boolean {
        val cols = arrayOf(COL_ID)
        val selection = "${COL_TITLE} = ?"
        val selectionArgs = arrayOf(title)
        var cursor: Cursor? = null
        try {
            cursor = mDb.query(TABLE_NAME, cols, selection, selectionArgs, null, null, null)
            return cursor.count == 1
        } finally {
            cursor?.close()
        }
    }

    fun getNotes(): List<Note> {
        val notes = ArrayList<Note>()
        val cols = arrayOf(COL_ID, COL_TITLE, COL_VALUE, COL_TYPE)
        var cursor: Cursor? = null
        try {
            cursor = mDb.query(TABLE_NAME, cols, null, null, null, null, "${COL_TITLE} COLLATE NOCASE ASC")
            if (cursor?.moveToFirst() == true) {
                do {
                    val id = cursor.getIntValue(COL_ID)
                    val title = cursor.getStringValue(COL_TITLE)
                    val value = cursor.getStringValue(COL_VALUE)
                    val type = cursor.getIntValue(COL_TYPE)
                    val note = Note(id, title, value, type)
                    notes.add(note)
                } while (cursor.moveToNext())
            }
        } finally {
            cursor?.close()
        }

        return notes
    }

    fun getNote(id: Int): Note? {
        val cols = arrayOf(COL_TITLE, COL_VALUE, COL_TYPE)
        val selection = "${COL_ID} = ?"
        val selectionArgs = arrayOf(id.toString())
        var note: Note? = null
        var cursor: Cursor? = null
        try {
            cursor = mDb.query(TABLE_NAME, cols, selection, selectionArgs, null, null, null)
            if (cursor?.moveToFirst() == true) {
                val title = cursor.getStringValue(COL_TITLE)
                val value = cursor.getStringValue(COL_VALUE)
                val type = cursor.getIntValue(COL_TYPE)
                note = Note(id, title, value, type)
            }
        } finally {
            cursor?.close()
        }
        return note
    }

    fun updateNote(note: Note) {
        val values = fillContentValues(note)
        val selection = "${COL_ID} = ?"
        val selectionArgs = arrayOf(note.id.toString())
        mDb.update(TABLE_NAME, values, selection, selectionArgs)
    }
}
