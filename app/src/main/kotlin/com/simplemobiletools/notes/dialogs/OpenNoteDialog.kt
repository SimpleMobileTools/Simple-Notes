package com.simplemobiletools.notes.dialogs

import android.app.Activity
import android.support.v7.app.AlertDialog
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RadioGroup
import com.simplemobiletools.commons.extensions.setupDialogStuff
import com.simplemobiletools.notes.R
import com.simplemobiletools.notes.extensions.config
import com.simplemobiletools.notes.helpers.DBHelper
import kotlinx.android.synthetic.main.open_note_item.view.*

class OpenNoteDialog(val activity: Activity, val callback: (checkedId: Int) -> Unit) {
    lateinit var dialog: AlertDialog

    init {
        val view = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }

        val notes = DBHelper.newInstance(activity).getNotes()
        notes.forEach {
            activity.layoutInflater.inflate(R.layout.open_note_item, null).apply {
                open_note_item_radio_button.apply {
                    text = it.title
                    isChecked = it.id == activity.config.currentNoteId
                    id = it.id

                    setOnClickListener {
                        callback.invoke(id)
                        dialog.dismiss()
                    }
                }
                view.addView(this, RadioGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
            }
        }

        dialog = AlertDialog.Builder(activity)
                .create().apply {
            activity.setupDialogStuff(view, this, R.string.pick_a_note)
        }
    }
}
