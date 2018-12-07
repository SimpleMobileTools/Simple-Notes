package com.simplemobiletools.notes.pro.dialogs

import android.app.Activity
import android.content.DialogInterface.BUTTON_POSITIVE
import androidx.appcompat.app.AlertDialog
import com.simplemobiletools.commons.extensions.setupDialogStuff
import com.simplemobiletools.commons.extensions.showKeyboard
import com.simplemobiletools.commons.extensions.toast
import com.simplemobiletools.commons.extensions.value
import com.simplemobiletools.notes.pro.R
import kotlinx.android.synthetic.main.dialog_new_checklist_item.view.*

class NewChecklistItemDialog(val activity: Activity, callback: (title: String) -> Unit) {
    init {
        val view = activity.layoutInflater.inflate(R.layout.dialog_new_checklist_item, null)

        AlertDialog.Builder(activity)
                .setPositiveButton(R.string.ok, null)
                .setNegativeButton(R.string.cancel, null)
                .create().apply {
                    activity.setupDialogStuff(view, this, R.string.add_new_checklist_item) {
                        showKeyboard(view.checklist_item_title)
                        getButton(BUTTON_POSITIVE).setOnClickListener {
                            val title = view.checklist_item_title.value
                            when {
                                title.isEmpty() -> activity.toast(R.string.no_title)
                                else -> {
                                    callback(title)
                                    dismiss()
                                }
                            }
                        }
                    }
                }
    }
}
