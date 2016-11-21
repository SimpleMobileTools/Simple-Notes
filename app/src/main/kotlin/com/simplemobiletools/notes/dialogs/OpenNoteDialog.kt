package com.simplemobiletools.notes.dialogs

import android.app.Activity
import android.app.AlertDialog
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import com.simplemobiletools.notes.Config
import com.simplemobiletools.notes.R
import com.simplemobiletools.notes.databases.DBHelper
import kotlinx.android.synthetic.main.dialog_radio_group.view.*

class OpenNoteDialog(val activity: Activity, val callback: (checkedId: Int) -> Unit) : RadioGroup.OnCheckedChangeListener {
    val dialog: AlertDialog?

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
                .setTitle(activity.resources.getString(R.string.pick_a_note))
                .setView(view)
                .create()

        dialog!!.show()
    }

    override fun onCheckedChanged(group: RadioGroup, checkedId: Int) {
        callback.invoke(checkedId)
        dialog?.dismiss()
    }
}
