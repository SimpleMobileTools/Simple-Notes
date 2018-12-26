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

class NewChecklistItemDialog(val activity: Activity, callback: (titles: ArrayList<String>) -> Unit) {
    init {
        val view = activity.layoutInflater.inflate(R.layout.dialog_new_checklist_item, null)

        AlertDialog.Builder(activity)
                .setPositiveButton(R.string.ok, null)
                .setNegativeButton(R.string.cancel, null)
                .create().apply {
                    activity.setupDialogStuff(view, this, R.string.add_new_checklist_items) {
                        showKeyboard(view.checklist_item_title_1)
                        getButton(BUTTON_POSITIVE).setOnClickListener {
                            val title1 = view.checklist_item_title_1.value
                            val title2 = view.checklist_item_title_2.value
                            val title3 = view.checklist_item_title_3.value
                            when {
                                title1.isEmpty() && title2.isEmpty() && title3.isEmpty() -> activity.toast(R.string.empty_name)
                                else -> {
                                    val titles = arrayListOf(title1, title2, title3).filter { it.isNotEmpty() }.toMutableList() as ArrayList<String>
                                    callback(titles)
                                    dismiss()
                                }
                            }
                        }
                    }
                }
    }
}
