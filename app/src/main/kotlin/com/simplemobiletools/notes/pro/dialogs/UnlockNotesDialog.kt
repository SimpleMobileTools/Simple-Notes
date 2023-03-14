package com.simplemobiletools.notes.pro.dialogs

import android.content.DialogInterface
import android.view.ViewGroup
import com.simplemobiletools.commons.activities.BaseSimpleActivity
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.notes.pro.R
import com.simplemobiletools.notes.pro.models.Note
import kotlinx.android.synthetic.main.dialog_unlock_notes.view.*
import kotlinx.android.synthetic.main.item_locked_note.view.*

class UnlockNotesDialog(val activity: BaseSimpleActivity, notes: List<Note>, callback: (unlockedNoteIds: List<Long>) -> Unit) {
    private val view = activity.layoutInflater.inflate(R.layout.dialog_unlock_notes, null) as ViewGroup
    private val redColor = activity.getColor(R.color.md_red)
    private val greenColor = activity.getColor(R.color.md_green)
    private val unlockedNoteIds = mutableListOf<Long>()

    init {
        for (note in notes) {
            addLockedNoteView(note)
        }
        activity.getAlertDialogBuilder()
            .setPositiveButton(R.string.ok, null)
            .setNegativeButton(R.string.cancel, null)
            .apply {
                activity.setupDialogStuff(view, this, R.string.unlock_notes, cancelOnTouchOutside = false) { alertDialog ->
                    alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener {
                        callback(unlockedNoteIds)
                        alertDialog.dismiss()
                    }
                }
            }
    }

    private fun addLockedNoteView(note: Note) {
        activity.layoutInflater.inflate(R.layout.item_locked_note, null).apply {
            view.notes_holder.addView(this)
            activity.updateTextColors(view.notes_holder)
            locked_note_title.text = note.title
            locked_unlocked_image.applyColorFilter(redColor)
            locked_note_holder.setOnClickListener {
                if (note.id !in unlockedNoteIds) {
                    activity.performSecurityCheck(
                        protectionType = note.protectionType,
                        requiredHash = note.protectionHash,
                        successCallback = { _, _ ->
                            unlockedNoteIds.add(note.id!!)
                            locked_unlocked_image.apply {
                                setImageResource(R.drawable.ic_lock_open_vector)
                                applyColorFilter(greenColor)
                            }
                        }
                    )
                } else {
                    unlockedNoteIds.remove(note.id)
                    locked_unlocked_image.apply {
                        setImageResource(R.drawable.ic_lock_vector)
                        applyColorFilter(redColor)
                    }
                }
            }
        }
    }
}
