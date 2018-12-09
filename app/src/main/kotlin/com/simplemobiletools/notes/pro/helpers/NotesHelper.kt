package com.simplemobiletools.notes.pro.helpers

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.simplemobiletools.notes.pro.R
import com.simplemobiletools.notes.pro.extensions.config
import com.simplemobiletools.notes.pro.extensions.notesDB
import com.simplemobiletools.notes.pro.models.Note
import java.io.File

class NotesHelper(val context: Context) {
    fun getNotes(callback: (notes: ArrayList<Note>) -> Unit) {
        Thread {
            // make sure the initial note has enough time to be precreated
            if (context.config.appRunCount <= 1) {
                context.notesDB.getNotes()
                Thread.sleep(200)
            }

            val notes = context.notesDB.getNotes() as ArrayList<Note>
            val notesToDelete = ArrayList<Note>(notes.size)
            notes.forEach {
                if (it.path.isNotEmpty() && !File(it.path).exists()) {
                    context.notesDB.deleteNote(it)
                    notesToDelete.add(it)
                }
            }

            notes.removeAll(notesToDelete)

            if (notes.isEmpty()) {
                val generalNote = context.resources.getString(R.string.general_note)
                val note = Note(null, generalNote, "", TYPE_TEXT)
                context.notesDB.insertOrUpdate(note)
                notes.add(note)
            }

            Handler(Looper.getMainLooper()).post {
                callback(notes)
            }
        }.start()
    }

    fun getNoteWithId(id: Long, callback: (note: Note?) -> Unit) {
        Thread {
            val note = context.notesDB.getNoteWithId(id)
            Handler(Looper.getMainLooper()).post {
                callback(note)
            }
        }.start()
    }

    fun getNoteIdWithPath(path: String, callback: (id: Long?) -> Unit) {
        Thread {
            val id = context.notesDB.getNoteIdWithPath(path)
            Handler(Looper.getMainLooper()).post {
                callback(id)
            }
        }.start()
    }

    fun insertOrUpdateNote(note: Note, callback: ((newNoteId: Long) -> Unit)? = null) {
        Thread {
            val noteId = context.notesDB.insertOrUpdate(note)
            Handler(Looper.getMainLooper()).post {
                callback?.invoke(noteId)
            }
        }.start()
    }
}
