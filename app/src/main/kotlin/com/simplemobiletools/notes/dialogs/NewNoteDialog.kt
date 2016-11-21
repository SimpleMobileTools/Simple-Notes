package com.simplemobiletools.notes.dialogs

import android.app.Activity
import android.app.AlertDialog
import android.view.WindowManager
import com.simplemobiletools.notes.R
import com.simplemobiletools.notes.databases.DBHelper
import com.simplemobiletools.notes.extensions.toast
import com.simplemobiletools.notes.extensions.value
import kotlinx.android.synthetic.main.new_note.view.*

class NewNoteDialog(val activity: Activity, val db: DBHelper, callback: (title: String) -> Unit) {

    init {
        val view = activity.layoutInflater.inflate(R.layout.new_note, null)

        AlertDialog.Builder(activity).apply {
            setTitle(activity.resources.getString(R.string.new_note))
            setView(view)
            setPositiveButton(R.string.ok, null)
            setNegativeButton(R.string.cancel, null)
            create().apply {
                window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
                show()
                getButton(android.support.v7.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                    val title = view.note_name.value
                    if (title.isEmpty()) {
                        activity.toast(R.string.no_title)
                    } else if (db.doesTitleExist(title)) {
                        activity.toast(R.string.title_taken)
                    } else {
                        callback.invoke(title)
                        dismiss()
                    }
                }
            }
        }
    }
}
