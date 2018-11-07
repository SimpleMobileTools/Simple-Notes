package com.simplemobiletools.notes.pro.helpers

import android.app.Activity
import com.simplemobiletools.notes.pro.R
import com.simplemobiletools.notes.pro.extensions.config
import com.simplemobiletools.notes.pro.extensions.notesDB
import com.simplemobiletools.notes.pro.models.Note
import java.io.File

class NotesHelper(val activity: Activity) {
    fun getNotes(callback: (notes: ArrayList<Note>) -> Unit) {
        Thread {
            // make sure the initial note has enough time to be precreated
            if (activity.config.appRunCount == 1) {
                activity.notesDB.getNotes()
                Thread.sleep(200)
            }

            val notes = activity.notesDB.getNotes() as ArrayList<Note>
            val notesToDelete = ArrayList<Note>(notes.size)
            notes.forEach {
                if (it.path.isNotEmpty() && !File(it.path).exists()) {
                    activity.notesDB.deleteNote(it)
                    notesToDelete.add(it)
                }
            }

            notes.removeAll(notesToDelete)

            if (notes.isEmpty()) {
                val generalNote = activity.resources.getString(R.string.general_note)
                val note = Note(null, generalNote, "", TYPE_NOTE)
                activity.notesDB.insertOrUpdate(note)
                notes.add(note)
            }

            activity.runOnUiThread {
                callback(notes)
            }
        }.start()
    }

    fun getNoteWithId(id: Int, callback: (note: Note?) -> Unit) {
        Thread {
            val note = activity.notesDB.getNoteWithId(id)
            activity.runOnUiThread {
                callback(note)
            }
        }.start()
    }

    fun getNoteIdWithPath(path: String, callback: (id: Long?) -> Unit) {
        Thread {
            val id = activity.notesDB.getNoteIdWithPath(path)
            activity.runOnUiThread {
                callback(id)
            }
        }.start()
    }

    fun insertOrUpdateNote(note: Note, callback: ((newNoteId: Long) -> Unit)? = null) {
        Thread {
            val noteId = activity.notesDB.insertOrUpdate(note)
            activity.runOnUiThread {
                callback?.invoke(noteId)
            }
        }.start()
    }
}
