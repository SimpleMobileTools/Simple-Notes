package com.simplemobiletools.notes.pro.dialogs

import android.app.Activity
import android.content.DialogInterface.BUTTON_POSITIVE
import androidx.appcompat.app.AlertDialog
import com.simplemobiletools.commons.extensions.setupDialogStuff
import com.simplemobiletools.commons.extensions.showKeyboard
import com.simplemobiletools.commons.extensions.toast
import com.simplemobiletools.commons.extensions.value
import com.simplemobiletools.commons.helpers.ensureBackgroundThread
import com.simplemobiletools.notes.pro.R
import com.simplemobiletools.notes.pro.extensions.config
import com.simplemobiletools.notes.pro.extensions.notesDB
import com.simplemobiletools.notes.pro.helpers.TYPE_CHECKLIST
import com.simplemobiletools.notes.pro.helpers.TYPE_TEXT
import com.simplemobiletools.notes.pro.models.Note
import kotlinx.android.synthetic.main.dialog_new_note.view.*

class NewNoteDialog(val activity: Activity, callback: (note: Note) -> Unit) {
    init {
        val view = activity.layoutInflater.inflate(R.layout.dialog_new_note, null).apply {
            new_note_type.check(if (activity.config.lastCreatedNoteType == TYPE_TEXT) type_text_note.id else type_checklist.id)
        }

        AlertDialog.Builder(activity)
                .setPositiveButton(R.string.ok, null)
                .setNegativeButton(R.string.cancel, null)
                .create().apply {
                    activity.setupDialogStuff(view, this, R.string.new_note) {
                        showKeyboard(view.note_title)
                        getButton(BUTTON_POSITIVE).setOnClickListener {
                            val title = view.note_title.value
                            ensureBackgroundThread {
                                when {
                                    title.isEmpty() -> activity.toast(R.string.no_title)
                                    activity.notesDB.getNoteIdWithTitle(title) != null -> activity.toast(R.string.title_taken)
                                    else -> {
                                        val type = if (view.new_note_type.checkedRadioButtonId == view.type_checklist.id) TYPE_CHECKLIST else TYPE_TEXT
                                        activity.config.lastCreatedNoteType = type
                                        val newNote = Note(null, title, "", type)
                                        callback(newNote)
                                        dismiss()
                                    }
                                }
                            }
                        }
                    }
                }
    }
}
