package com.simplemobiletools.notes.pro.helpers

import android.content.Context
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import com.simplemobiletools.commons.extensions.showErrorToast
import com.simplemobiletools.commons.helpers.PROTECTION_NONE
import com.simplemobiletools.commons.helpers.ensureBackgroundThread
import com.simplemobiletools.notes.pro.extensions.notesDB
import com.simplemobiletools.notes.pro.models.Note
import java.io.File

class NotesImporter(private val context: Context) {
    enum class ImportResult {
        IMPORT_FAIL, IMPORT_OK, IMPORT_PARTIAL, IMPORT_NOTHING_NEW
    }

    private val gson = Gson()
    private var notesImported = 0
    private var notesFailed = 0

    fun importNotes(path: String, filename: String, callback: (result: ImportResult) -> Unit) {
        ensureBackgroundThread {
            try {
                val inputStream = if (path.contains("/")) {
                    File(path).inputStream()
                } else {
                    context.assets.open(path)
                }

                inputStream.bufferedReader().use { reader ->
                    val json = reader.readText()
                    val type = object : TypeToken<List<Note>>() {}.type
                    val notes = gson.fromJson<List<Note>>(json, type)
                    val totalNotes = notes.size
                    if (totalNotes <= 0) {
                        callback.invoke(ImportResult.IMPORT_NOTHING_NEW)
                        return@ensureBackgroundThread
                    }

                    for (note in notes) {
                        val exists = context.notesDB.getNoteIdWithTitle(note.title) != null
                        if (!exists) {
                            context.notesDB.insertOrUpdate(note)
                            notesImported++
                        }
                    }
                }
            } catch (e: JsonSyntaxException) {
                // Import notes expects a json with note name, content etc, but lets be more flexible and accept the basic files with note content only too
                val inputStream = if (path.contains("/")) {
                    File(path).inputStream()
                } else {
                    context.assets.open(path)
                }

                inputStream.bufferedReader().use { reader ->
                    val text = reader.readText()
                    val note = Note(null, filename, text, NUMBERED_LIST_NONE, NoteType.TYPE_TEXT.value, "", PROTECTION_NONE, "")
                    var i = 1
                    if (context.notesDB.getNoteIdWithTitle(note.title) != null) {
                        while (true) {
                            val tryTitle = "$filename ($i)"
                            if (context.notesDB.getNoteIdWithTitle(tryTitle) == null) {
                                break
                            }
                            i++
                        }

                        note.title = "$filename ($i)"
                    }

                    context.notesDB.insertOrUpdate(note)
                    notesImported++
                }

            } catch (e: Exception) {
                context.showErrorToast(e)
                notesFailed++
            }

            callback.invoke(
                when {
                    notesImported == 0 -> ImportResult.IMPORT_FAIL
                    notesFailed > 0 -> ImportResult.IMPORT_PARTIAL
                    else -> ImportResult.IMPORT_OK
                }
            )
        }
    }
}
