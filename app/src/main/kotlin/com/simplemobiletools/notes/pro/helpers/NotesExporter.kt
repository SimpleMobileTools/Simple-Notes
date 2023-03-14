package com.simplemobiletools.notes.pro.helpers

import android.content.Context
import com.google.gson.Gson
import com.google.gson.stream.JsonWriter
import com.simplemobiletools.commons.helpers.PROTECTION_NONE
import com.simplemobiletools.commons.helpers.ensureBackgroundThread
import com.simplemobiletools.notes.pro.models.Note
import java.io.OutputStream

class NotesExporter(private val context: Context) {
    enum class ExportResult {
        EXPORT_FAIL, EXPORT_OK
    }

    private val gson = Gson()

    fun exportNotes(notes: List<Note>, unlockedNoteIds: List<Long>, outputStream: OutputStream?, callback: (result: ExportResult) -> Unit) {
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
                    for (note in notes) {
                        if (!note.isLocked() || note.id in unlockedNoteIds) {
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
