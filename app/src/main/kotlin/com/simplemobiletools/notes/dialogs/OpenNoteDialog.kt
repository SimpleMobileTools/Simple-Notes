package com.simplemobiletools.notes.dialogs

import android.app.Activity
import android.support.v7.app.AlertDialog
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import com.simplemobiletools.commons.extensions.setupDialogStuff
import com.simplemobiletools.notes.R
import com.simplemobiletools.notes.helpers.DBHelper
import com.simplemobiletools.notes.helpers.Config
import kotlinx.android.synthetic.main.dialog_radio_group.view.*

class OpenNoteDialog(val activity: Activity, val callback: (checkedId: Int) -> Unit) : RadioGroup.OnCheckedChangeListener {
    val dialog: AlertDialog?
    var wasInit = false

    init {
        val config = Config.newInstance(activity)
        val view = activity.layoutInflater.inflate(R.layout.dialog_radio_group, null)
        val radioGroup = view.dialog_radio_group
        radioGroup.setOnCheckedChangeListener(this)

        val notes = DBHelper.newInstance(activity).getNotes()
        notes.forEach {
            val radioButton = activity.layoutInflater.inflate(R.layout.radio_button, null) as RadioButton
            radioButton.apply {
                text = it.title
                isChecked = it.id == config.currentNoteId
                id = it.id
            }
            radioGroup.addView(radioButton, RadioGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        }

        dialog = AlertDialog.Builder(activity)
                .create().apply {
            activity.setupDialogStuff(view, this, R.string.pick_a_note)
        }

        wasInit = true
    }

    override fun onCheckedChanged(group: RadioGroup, checkedId: Int) {
        if (wasInit) {
            callback.invoke(checkedId)
            dialog?.dismiss()
        }
    }
}
