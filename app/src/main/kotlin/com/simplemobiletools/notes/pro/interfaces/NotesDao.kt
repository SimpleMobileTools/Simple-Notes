package com.simplemobiletools.notes.pro.interfaces

import androidx.room.*
import com.simplemobiletools.notes.pro.models.Note

@Dao
interface NotesDao {
    @Query("SELECT * FROM notes ORDER BY title COLLATE NOCASE ASC")
    fun getNotes(): List<Note>

    @Query("SELECT * FROM notes WHERE id = :id")
    fun getNoteWithId(id: Int): Note?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrUpdate(note: Note): Long

    @Delete
    fun deleteNote(note: Note)
}
