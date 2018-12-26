package com.simplemobiletools.notes.pro.dialogs

import android.app.Activity
import android.content.DialogInterface.BUTTON_POSITIVE
import androidx.appcompat.app.AlertDialog
import com.simplemobiletools.commons.extensions.setupDialogStuff
import com.simplemobiletools.commons.extensions.showKeyboard
import com.simplemobiletools.commons.extensions.toast
import com.simplemobiletools.commons.extensions.value
import com.simplemobiletools.notes.pro.R
import kotlinx.android.synthetic.main.dialog_rename_checklist_item.view.*

class RenameChecklistItemDialog(val activity: Activity, val oldTitle: String, callback: (newTitle: String) -> Unit) {
    init {
        val view = activity.layoutInflater.inflate(R.layout.dialog_rename_checklist_item, null).apply {
            checklist_item_title.setText(oldTitle)
        }

        AlertDialog.Builder(activity)
                .setPositiveButton(R.string.ok, null)
                .setNegativeButton(R.string.cancel, null)
                .create().apply {
                    activity.setupDialogStuff(view, this) {
                        showKeyboard(view.checklist_item_title)
                        getButton(BUTTON_POSITIVE).setOnClickListener {
                            val newTitle = view.checklist_item_title.value
                            when {
                                newTitle.isEmpty() -> activity.toast(R.string.empty_name)
                                else -> {
                                    callback(newTitle)
                                    dismiss()
                                }
                            }
                        }
                    }
                }
    }
}
