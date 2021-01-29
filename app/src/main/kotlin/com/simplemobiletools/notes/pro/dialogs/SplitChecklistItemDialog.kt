package com.simplemobiletools.notes.pro.dialogs

import android.app.Activity
import android.content.DialogInterface.BUTTON_POSITIVE
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import com.simplemobiletools.commons.extensions.setupDialogStuff
import com.simplemobiletools.commons.extensions.showKeyboard
import com.simplemobiletools.commons.extensions.value
import com.simplemobiletools.notes.pro.R
import kotlinx.android.synthetic.main.dialog_split_new_checklist_item.view.*


class SplitChecklistItemDialog(val activity: Activity, callback: (titles: ArrayList<String>) -> Unit) {
    private val splitTypes: HashMap<String, String> = hashMapOf("New Line" to "\n", "Comma" to ",")

    init {
        val view = activity.layoutInflater.inflate(R.layout.dialog_split_new_checklist_item, null)

        val dataAdapter: ArrayAdapter<String> = ArrayAdapter<String>(activity, android.R.layout.simple_spinner_item, splitTypes.toList().map { it.first })
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        view.spn_separate_by.adapter = dataAdapter

        AlertDialog.Builder(activity)
                .setPositiveButton(R.string.ok, null)
                .setNegativeButton(R.string.cancel, null)
                .create().apply {
                    activity.setupDialogStuff(view, this, R.string.paste_and_split) {
                        showKeyboard(view.checklist_item)
                        getButton(BUTTON_POSITIVE).setOnClickListener {
                            val splitRegex = splitTypes.getValue(view.spn_separate_by.selectedItem.toString())
                            val items = view.checklist_item.value.split(splitRegex)
                            callback(ArrayList(items))
                            dismiss()
                        }
                    }
                }
    }
}
