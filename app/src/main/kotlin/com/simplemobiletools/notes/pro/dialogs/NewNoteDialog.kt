package com.simplemobiletools.notes.pro.dialogs

import android.app.Activity
import android.content.DialogInterface.BUTTON_POSITIVE
import androidx.appcompat.app.AlertDialog
import com.simplemobiletools.commons.extensions.setupDialogStuff
import com.simplemobiletools.commons.extensions.showKeyboard
import com.simplemobiletools.commons.extensions.toast
import com.simplemobiletools.commons.extensions.value
import com.simplemobiletools.notes.pro.R
import com.simplemobiletools.notes.pro.helpers.DBHelper
import kotlinx.android.synthetic.main.dialog_new_note.view.*

class NewNoteDialog(val activity: Activity, val db: DBHelper, callback: (title: String) -> Unit) {
    init {
        val view = activity.layoutInflater.inflate(R.layout.dialog_new_note, null)

        AlertDialog.Builder(activity)
                .setPositiveButton(R.string.ok, null)
                .setNegativeButton(R.string.cancel, null)
                .create().apply {
                    activity.setupDialogStuff(view, this, R.string.new_note) {
                        showKeyboard(view.note_name)
                        getButton(BUTTON_POSITIVE).setOnClickListener {
                            val title = view.note_name.value
                            when {
                                title.isEmpty() -> activity.toast(R.string.no_title)
                                db.doesNoteTitleExist(title) -> activity.toast(R.string.title_taken)
                                else -> {
                                    callback(title)
                                    dismiss()
                                }
                            }
                        }
                    }
                }
    }
}
