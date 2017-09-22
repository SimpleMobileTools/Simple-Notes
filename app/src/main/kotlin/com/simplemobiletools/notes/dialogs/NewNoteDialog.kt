package com.simplemobiletools.notes.dialogs

import android.app.Activity
import android.content.DialogInterface.BUTTON_POSITIVE
import android.support.v7.app.AlertDialog
import android.view.WindowManager
import com.simplemobiletools.commons.extensions.setupDialogStuff
import com.simplemobiletools.commons.extensions.toast
import com.simplemobiletools.commons.extensions.value
import com.simplemobiletools.notes.R
import com.simplemobiletools.notes.helpers.DBHelper
import kotlinx.android.synthetic.main.new_note.view.*

class NewNoteDialog(val activity: Activity, val db: DBHelper, callback: (title: String) -> Unit) {
    init {
        val view = activity.layoutInflater.inflate(R.layout.new_note, null)

        AlertDialog.Builder(activity)
                .setPositiveButton(R.string.ok, null)
                .setNegativeButton(R.string.cancel, null)
                .create().apply {
            window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
            activity.setupDialogStuff(view, this, R.string.new_note)
            getButton(BUTTON_POSITIVE).setOnClickListener {
                val title = view.note_name.value
                when {
                    title.isEmpty() -> activity.toast(R.string.no_title)
                    db.doesTitleExist(title) -> activity.toast(R.string.title_taken)
                    else -> {
                        callback(title)
                        dismiss()
                    }
                }
            }
        }
    }
}
