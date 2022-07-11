package com.simplemobiletools.notes.pro.dialogs

import android.app.Activity
import android.content.DialogInterface.BUTTON_POSITIVE
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.notes.pro.R
import kotlinx.android.synthetic.main.dialog_rename_checklist_item.view.*

class RenameChecklistItemDialog(val activity: Activity, val oldTitle: String, callback: (newTitle: String) -> Unit) {
    init {
        val view = activity.layoutInflater.inflate(R.layout.dialog_rename_checklist_item, null).apply {
            checklist_item_title.setText(oldTitle)
        }

        activity.getAlertDialogBuilder()
            .setPositiveButton(R.string.ok, null)
            .setNegativeButton(R.string.cancel, null)
            .apply {
                activity.setupDialogStuff(view, this) { alertDialog ->
                    alertDialog.showKeyboard(view.checklist_item_title)
                    alertDialog.getButton(BUTTON_POSITIVE).setOnClickListener {
                        val newTitle = view.checklist_item_title.value
                        when {
                            newTitle.isEmpty() -> activity.toast(R.string.empty_name)
                            else -> {
                                callback(newTitle)
                                alertDialog.dismiss()
                            }
                        }
                    }
                }
            }
    }
}
