package com.simplemobiletools.notes.pro.dialogs

import android.app.Activity
import android.content.DialogInterface.BUTTON_POSITIVE
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.helpers.PROTECTION_NONE
import com.simplemobiletools.commons.helpers.ensureBackgroundThread
import com.simplemobiletools.notes.pro.R
import com.simplemobiletools.notes.pro.extensions.config
import com.simplemobiletools.notes.pro.extensions.notesDB
import com.simplemobiletools.notes.pro.helpers.NoteType
import com.simplemobiletools.notes.pro.models.Note
import kotlinx.android.synthetic.main.dialog_new_note.view.*

class NewNoteDialog(val activity: Activity, title: String? = null, val setChecklistAsDefault: Boolean, callback: (note: Note) -> Unit) {
    init {
        val view = activity.layoutInflater.inflate(R.layout.dialog_new_note, null).apply {
            val defaultType = when {
                setChecklistAsDefault -> type_checklist.id
                activity.config.lastCreatedNoteType == NoteType.TYPE_TEXT.value -> type_text_note.id
                else -> type_checklist.id
            }

            new_note_type.check(defaultType)
        }

        view.note_title.setText(title)

        activity.getAlertDialogBuilder()
            .setPositiveButton(R.string.ok, null)
            .setNegativeButton(R.string.cancel, null)
            .apply {
                activity.setupDialogStuff(view, this, R.string.new_note) { alertDialog ->
                    alertDialog.showKeyboard(view.note_title)
                    alertDialog.getButton(BUTTON_POSITIVE).setOnClickListener {
                        val newTitle = view.note_title.value
                        ensureBackgroundThread {
                            when {
                                newTitle.isEmpty() -> activity.toast(R.string.no_title)
                                activity.notesDB.getNoteIdWithTitle(newTitle) != null -> activity.toast(R.string.title_taken)
                                else -> {
                                    val type = if (view.new_note_type.checkedRadioButtonId == view.type_checklist.id) {
                                        NoteType.TYPE_CHECKLIST.value
                                    } else {
                                        NoteType.TYPE_TEXT.value
                                    }

                                    activity.config.lastCreatedNoteType = type
                                    val newNote = Note(null, newTitle, "", type, "", PROTECTION_NONE, "")
                                    callback(newNote)
                                    alertDialog.dismiss()
                                }
                            }
                        }
                    }
                }
            }
    }
}
