package com.simplemobiletools.notes.pro.dialogs

import android.app.Activity
import android.content.DialogInterface.BUTTON_POSITIVE
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.helpers.PROTECTION_NONE
import com.simplemobiletools.commons.helpers.ensureBackgroundThread
import com.simplemobiletools.notes.pro.R
import com.simplemobiletools.notes.pro.databinding.DialogNewNoteBinding
import com.simplemobiletools.notes.pro.extensions.config
import com.simplemobiletools.notes.pro.extensions.notesDB
import com.simplemobiletools.notes.pro.models.Note
import com.simplemobiletools.notes.pro.models.NoteType

class NewNoteDialog(val activity: Activity, title: String? = null, val setChecklistAsDefault: Boolean, callback: (note: Note) -> Unit) {
    init {
        val binding = DialogNewNoteBinding.inflate(activity.layoutInflater).apply {
            val defaultType = when {
                setChecklistAsDefault -> typeChecklist.id
                activity.config.lastCreatedNoteType == NoteType.TYPE_TEXT.value -> typeTextNote.id
                else -> typeChecklist.id
            }

            newNoteType.check(defaultType)
        }

        binding.lockedNoteTitle.setText(title)

        activity.getAlertDialogBuilder()
            .setPositiveButton(com.simplemobiletools.commons.R.string.ok, null)
            .setNegativeButton(com.simplemobiletools.commons.R.string.cancel, null)
            .apply {
                activity.setupDialogStuff(binding.root, this, R.string.new_note) { alertDialog ->
                    alertDialog.showKeyboard(binding.lockedNoteTitle)
                    alertDialog.getButton(BUTTON_POSITIVE).setOnClickListener {
                        val newTitle = binding.lockedNoteTitle.value
                        ensureBackgroundThread {
                            when {
                                newTitle.isEmpty() -> activity.toast(R.string.no_title)
                                activity.notesDB.getNoteIdWithTitle(newTitle) != null -> activity.toast(R.string.title_taken)
                                else -> {
                                    val type = if (binding.newNoteType.checkedRadioButtonId == binding.typeChecklist.id) {
                                        NoteType.TYPE_CHECKLIST
                                    } else {
                                        NoteType.TYPE_TEXT
                                    }

                                    activity.config.lastCreatedNoteType = type.value
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
