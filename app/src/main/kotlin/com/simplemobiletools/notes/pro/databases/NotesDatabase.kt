package com.simplemobiletools.notes.pro.databases

import android.content.Context
import android.provider.SyncStateContract.Constants
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.simplemobiletools.commons.helpers.PROTECTION_NONE
import com.simplemobiletools.notes.pro.R
import com.simplemobiletools.notes.pro.helpers.DEFAULT_WIDGET_TEXT_COLOR
import com.simplemobiletools.notes.pro.helpers.NUMBERED_LIST_NONE
import com.simplemobiletools.notes.pro.helpers.NoteType
import com.simplemobiletools.notes.pro.interfaces.NotesDao
import com.simplemobiletools.notes.pro.interfaces.WidgetsDao
import com.simplemobiletools.notes.pro.models.Note
import com.simplemobiletools.notes.pro.models.Widget
import java.util.concurrent.Executors

@Database(entities = [Note::class, Widget::class], version = 5)
abstract class NotesDatabase : RoomDatabase() {

    abstract fun NotesDao(): NotesDao

    abstract fun WidgetsDao(): WidgetsDao

    companion object {
        private var db: NotesDatabase? = null
        private var defaultWidgetBgColor = 0

        fun getInstance(context: Context): NotesDatabase {
            defaultWidgetBgColor = context.resources.getColor(R.color.default_widget_bg_color)
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
                            .addMigrations(MIGRATION_1_2)
                            .addMigrations(MIGRATION_2_3)
                            .addMigrations(MIGRATION_3_4)
                            .addMigrations(MIGRATION_4_5)
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
                val note = Note(null, generalNote, "", NoteType.TYPE_TEXT.value, NUMBERED_LIST_NONE, "", PROTECTION_NONE, "")
                db!!.NotesDao().insertOrUpdate(note)
            }
        }

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.apply {
                    execSQL("ALTER TABLE widgets ADD COLUMN widget_bg_color INTEGER NOT NULL DEFAULT $defaultWidgetBgColor")
                    execSQL("ALTER TABLE widgets ADD COLUMN widget_text_color INTEGER NOT NULL DEFAULT $DEFAULT_WIDGET_TEXT_COLOR")
                }
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.apply {
                    execSQL("ALTER TABLE notes ADD COLUMN protection_type INTEGER DEFAULT $PROTECTION_NONE NOT NULL")
                    execSQL("ALTER TABLE notes ADD COLUMN protection_hash TEXT DEFAULT '' NOT NULL")
                }
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE widgets ADD COLUMN widget_show_title INTEGER NOT NULL DEFAULT 0")
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE notes ADD COLUMN numbered_list INTEGER NOT NULL DEFAULT 0")
            }
        }
    }
}
