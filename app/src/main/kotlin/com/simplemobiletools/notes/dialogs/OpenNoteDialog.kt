package com.simplemobiletools.notes.dialogs

import android.app.Activity
import android.view.ViewGroup
import android.widget.RadioGroup
import androidx.appcompat.app.AlertDialog
import com.simplemobiletools.commons.extensions.applyColorFilter
import com.simplemobiletools.commons.extensions.beVisibleIf
import com.simplemobiletools.commons.extensions.setupDialogStuff
import com.simplemobiletools.commons.extensions.toast
import com.simplemobiletools.notes.R
import com.simplemobiletools.notes.extensions.config
import com.simplemobiletools.notes.extensions.dbHelper
import kotlinx.android.synthetic.main.dialog_open_note.view.*
import kotlinx.android.synthetic.main.open_note_item.view.*

class OpenNoteDialog(val activity: Activity, val callback: (checkedId: Int) -> Unit) {
    lateinit var dialog: AlertDialog

    init {
        val view = activity.layoutInflater.inflate(R.layout.dialog_open_note, null)
        val textColor = activity.config.textColor
        val notes = activity.dbHelper.getNotes()
        notes.forEach {
            activity.layoutInflater.inflate(R.layout.open_note_item, null).apply {
                val note = it
                open_note_item_radio_button.apply {
                    text = note.title
                    isChecked = note.id == activity.config.currentNoteId
                    id = note.id

                    setOnClickListener {
                        callback(id)
                        dialog.dismiss()
                    }
                }
                open_note_item_icon.apply {
                    beVisibleIf(note.path.isNotEmpty())
                    applyColorFilter(textColor)
                    setOnClickListener {
                        activity.toast(note.path)
                    }
                }
                view.dialog_open_note_linear.addView(this, RadioGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
            }
        }

        dialog = AlertDialog.Builder(activity)
                .create().apply {
                    activity.setupDialogStuff(view, this, R.string.pick_a_note)
                }
    }
}
