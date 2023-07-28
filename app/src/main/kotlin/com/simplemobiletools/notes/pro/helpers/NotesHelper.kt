package com.simplemobiletools.notes.pro.helpers

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.simplemobiletools.commons.activities.BaseSimpleActivity
import com.simplemobiletools.commons.helpers.ExportResult
import com.simplemobiletools.commons.helpers.PROTECTION_NONE
import com.simplemobiletools.commons.helpers.ensureBackgroundThread
import com.simplemobiletools.notes.pro.R
import com.simplemobiletools.notes.pro.extensions.config
import com.simplemobiletools.notes.pro.extensions.notesDB
import com.simplemobiletools.notes.pro.models.Note
import com.simplemobiletools.notes.pro.models.NoteType
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.OutputStream

class NotesHelper(val context: Context) {
    fun getNotes(callback: (notes: List<Note>) -> Unit) {
        ensureBackgroundThread {
            // make sure the initial note has enough time to be precreated
            if (context.config.appRunCount <= 1) {
                context.notesDB.getNotes()
                Thread.sleep(200)
            }

            val notes = context.notesDB.getNotes().toMutableList()
            val notesToDelete = mutableListOf<Note>()
            notes.forEach {
                if (it.path.isNotEmpty()) {
                    if (!it.path.startsWith("content://") && !File(it.path).exists()) {
                        context.notesDB.deleteNote(it)
                        notesToDelete.add(it)
                    }
                }
            }

            notes.removeAll(notesToDelete)

            if (notes.isEmpty()) {
                val generalNote = context.resources.getString(R.string.general_note)
                val note = Note(null, generalNote, "", NoteType.TYPE_TEXT, "", PROTECTION_NONE, "")
                context.notesDB.insertOrUpdate(note)
                notes.add(note)
            }

            Handler(Looper.getMainLooper()).post {
                callback(notes)
            }
        }
    }

    fun getNoteWithId(id: Long, callback: (note: Note?) -> Unit) {
        ensureBackgroundThread {
            val note = context.notesDB.getNoteWithId(id)
            Handler(Looper.getMainLooper()).post {
                callback(note)
            }
        }
    }

    fun getNoteIdWithPath(path: String, callback: (id: Long?) -> Unit) {
        ensureBackgroundThread {
            val id = context.notesDB.getNoteIdWithPath(path)
            Handler(Looper.getMainLooper()).post {
                callback(id)
            }
        }
    }

    fun insertOrUpdateNote(note: Note, callback: ((newNoteId: Long) -> Unit)? = null) {
        ensureBackgroundThread {
            val noteId = context.notesDB.insertOrUpdate(note)
            Handler(Looper.getMainLooper()).post {
                callback?.invoke(noteId)
            }
        }
    }

    fun insertOrUpdateNotes(notes: List<Note>, callback: ((newNoteIds: List<Long>) -> Unit)? = null) {
        ensureBackgroundThread {
            val noteIds = context.notesDB.insertOrUpdate(notes)
            Handler(Looper.getMainLooper()).post {
                callback?.invoke(noteIds)
            }
        }
    }

    fun importNotes(activity: BaseSimpleActivity, notes: List<Note>, callback: (ImportResult) -> Unit) {
        ensureBackgroundThread {
            val currentNotes = activity.notesDB.getNotes()
            if (currentNotes.isEmpty()) {
                insertOrUpdateNotes(notes) { savedNotes ->

                    val newCurrentNotes = activity.notesDB.getNotes()

                    val result = when {
                        currentNotes.size == newCurrentNotes.size -> ImportResult.IMPORT_NOTHING_NEW
                        notes.size == savedNotes.size -> ImportResult.IMPORT_OK
                        savedNotes.isEmpty() -> ImportResult.IMPORT_FAIL
                        else -> ImportResult.IMPORT_PARTIAL
                    }
                    callback(result)
                }
            } else {
                var imported = 0
                var skipped = 0

                notes.forEach { note ->
                    val exists = context.notesDB.getNoteIdWithTitle(note.title) != null
                    if (!exists) {
                        context.notesDB.insertOrUpdate(note)
                        imported++
                    } else {
                        skipped++
                    }
                }

                val result = when {
                    skipped == notes.size || imported == 0 -> ImportResult.IMPORT_NOTHING_NEW
                    imported == notes.size -> ImportResult.IMPORT_OK
                    else -> ImportResult.IMPORT_PARTIAL
                }
                callback(result)
            }
        }
    }

    fun exportNotes(notesToBackup: List<Note>, outputStream: OutputStream): ExportResult {
        return try {
            val jsonString = Json.encodeToString(notesToBackup)
            outputStream.use {
                it.write(jsonString.toByteArray())
            }
            ExportResult.EXPORT_OK
        } catch (_: Error) {
            ExportResult.EXPORT_FAIL
        }
    }

    enum class ImportResult {
        IMPORT_FAIL, IMPORT_OK, IMPORT_PARTIAL, IMPORT_NOTHING_NEW
    }
}
