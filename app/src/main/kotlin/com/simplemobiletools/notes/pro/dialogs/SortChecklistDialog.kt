package com.simplemobiletools.notes.pro.dialogs

import android.content.DialogInterface
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import com.simplemobiletools.commons.extensions.setupDialogStuff
import com.simplemobiletools.notes.pro.R
import com.simplemobiletools.notes.pro.activities.SimpleActivity
import com.simplemobiletools.notes.pro.models.ChecklistSort
import com.simplemobiletools.notes.pro.models.ChecklistSortDirection
import com.simplemobiletools.notes.pro.models.ChecklistSortField
import kotlinx.android.synthetic.main.dialog_sort_checklist.view.separate_items_checkbox
import kotlinx.android.synthetic.main.dialog_sort_checklist.view.sort_direction_type
import kotlinx.android.synthetic.main.dialog_sort_checklist.view.sort_field_type

class SortChecklistDialog(private val activity: SimpleActivity, val callback: (ChecklistSort) -> Unit) {
    init {
        val view = (activity.layoutInflater.inflate(R.layout.dialog_sort_checklist, null) as ViewGroup)

        AlertDialog.Builder(activity)
            .setPositiveButton(R.string.ok, null)
            .setNegativeButton(R.string.cancel, null)
            .create().apply {
                activity.setupDialogStuff(view, this, R.string.sort_checklist) {
                    getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener {
                        callback.invoke(
                            ChecklistSort(
                                field = getSortField(view),
                                direction = getSortDirection(view),
                                separateCheckedFromUnchecked = view.separate_items_checkbox.isChecked
                            )
                        )
                        dismiss()
                    }
                }
            }
    }

    private fun getSortField(view: View): ChecklistSortField {
        return when (view.sort_field_type.checkedRadioButtonId) {
            R.id.sort_field_title -> ChecklistSortField.TITLE
            else -> ChecklistSortField.DATE_CREATED
        }
    }

    private fun getSortDirection(view: View): ChecklistSortDirection {
        return when (view.sort_direction_type.checkedRadioButtonId) {
            R.id.sort_direction_asc -> ChecklistSortDirection.ASCENDING
            else -> ChecklistSortDirection.DESCENDING
        }
    }
}
