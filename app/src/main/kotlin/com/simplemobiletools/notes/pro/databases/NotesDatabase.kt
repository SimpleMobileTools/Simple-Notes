package com.simplemobiletools.notes.pro.databases

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.simplemobiletools.notes.pro.interfaces.NotesDao
import com.simplemobiletools.notes.pro.interfaces.WidgetsDao
import com.simplemobiletools.notes.pro.models.Note
import com.simplemobiletools.notes.pro.models.Widget
import com.simplemobiletools.notes.pro.objects.MyExecutor
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
                                .setQueryExecutor(MyExecutor.myExecutor)
                                .addCallback(object : Callback() {
                                    override fun onCreate(db: SupportSQLiteDatabase) {
                                        super.onCreate(db)
                                        insertFirstNote()
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

        private fun insertFirstNote() {
            Executors.newSingleThreadExecutor().execute {

            }
        }
    }
}
