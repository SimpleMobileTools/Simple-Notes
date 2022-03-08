package com.simplemobiletools.notes.pro.helpers

import android.content.Context
import com.google.gson.Gson
import com.google.gson.stream.JsonWriter
import com.simplemobiletools.commons.helpers.PROTECTION_NONE
import com.simplemobiletools.commons.helpers.ensureBackgroundThread
import com.simplemobiletools.notes.pro.extensions.notesDB
import com.simplemobiletools.notes.pro.models.Note
import java.io.OutputStream

class NotesExporter(private val context: Context) {
    enum class ExportResult {
        EXPORT_FAIL, EXPORT_OK
    }

    private val gson = Gson()

    fun exportNotes(outputStream: OutputStream?, callback: (result: ExportResult) -> Unit) {
        ensureBackgroundThread {
            if (outputStream == null) {
                callback.invoke(ExportResult.EXPORT_FAIL)
                return@ensureBackgroundThread
            }
            val writer = JsonWriter(outputStream.bufferedWriter())
            writer.use {
                try {
                    var written = 0
                    writer.beginArray()
                    val notes = context.notesDB.getNotes() as ArrayList<Note>
                    for (note in notes) {
                        if (note.protectionType === PROTECTION_NONE) {
                            val noteToSave = getNoteToExport(note)
                            writer.jsonValue(gson.toJson(noteToSave))
                            written++
                        }
                    }
                    writer.endArray()
                    callback.invoke(ExportResult.EXPORT_OK)
                } catch (e: Exception) {
                    callback.invoke(ExportResult.EXPORT_FAIL)
                }
            }
        }
    }

    private fun getNoteToExport(note: Note): Note {
        return Note(null, note.title, note.getNoteStoredValue(context) ?: "", note.type, "", PROTECTION_NONE, "")
    }
}
