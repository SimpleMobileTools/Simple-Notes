package com.simplemobiletools.notes.pro.databases

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.simplemobiletools.notes.pro.R
import com.simplemobiletools.notes.pro.helpers.TYPE_NOTE
import com.simplemobiletools.notes.pro.interfaces.NotesDao
import com.simplemobiletools.notes.pro.interfaces.WidgetsDao
import com.simplemobiletools.notes.pro.models.Note
import com.simplemobiletools.notes.pro.models.Widget
import java.util.concurrent.Executors

@Database(entities = [Note::class, Widget::class], version = 1)
abstract class NotesDatabase : RoomDatabase() {

    abstract fun NotesDao(): NotesDao

    abstract fun WidgetsDao(): WidgetsDao

    companion object {
        private var db: NotesDatabase? = null

        fun getInstance(context: Context): NotesDatabase {
            if (db == null) {
                synchronized(NotesDatabase::class) {
                    if (db == null) {
                        db = Room.databaseBuilder(context.applicationContext, NotesDatabase::class.java, "notes.db")
                                .addCallback(object : Callback() {
                                    override fun onCreate(db: SupportSQLiteDatabase) {
                                        super.onCreate(db)
                                        insertFirstNote(context)
                                    }
                                })
                                .build()
                        db!!.openHelper.setWriteAheadLoggingEnabled(true)
                    }
                }
            }
            return db!!
        }

        fun destroyInstance() {
            db = null
        }

        private fun insertFirstNote(context: Context) {
            Executors.newSingleThreadScheduledExecutor().execute {
                val generalNote = context.resources.getString(R.string.general_note)
                val note = Note(null, generalNote, "", TYPE_NOTE)
                db!!.NotesDao().insertOrUpdate(note)
            }
        }
    }
}
