package com.simplemobiletools.notes.views.dialogs

import android.app.Activity
import android.app.AlertDialog
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import com.simplemobiletools.notes.Config
import com.simplemobiletools.notes.R
import com.simplemobiletools.notes.databases.DBHelper

class OpenNoteDialog(val activity: Activity) : AlertDialog.Builder(activity), RadioGroup.OnCheckedChangeListener {
    val dialog: AlertDialog?

    init {
        val config = Config.newInstance(context)
        val view = activity.layoutInflater.inflate(R.layout.dialog_radio_group, null) as RadioGroup
        view.setOnCheckedChangeListener(this)

        val db = DBHelper.newInstance(context)
        val notes = db.notes
        notes.forEach {
            val radioButton = activity.layoutInflater.inflate(R.layout.radio_button, null) as RadioButton
            radioButton.apply {
                text = it.title
                isChecked = it.id == config.currentNoteId
                id = it.id
            }
            view.addView(radioButton, RadioGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        }

        dialog = AlertDialog.Builder(activity)
                .setTitle(activity.resources.getString(R.string.pick_a_note))
                .setView(view)
                .create()

        dialog?.show()
    }

    override fun onCheckedChanged(group: RadioGroup, checkedId: Int) {
        (activity as OpenNoteListener).noteSelected(checkedId)
        dialog?.dismiss()
    }

    interface OpenNoteListener {
        fun noteSelected(id: Int)
    }
}
