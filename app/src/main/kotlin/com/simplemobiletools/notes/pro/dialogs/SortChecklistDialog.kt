package com.simplemobiletools.notes.pro.dialogs

import android.content.DialogInterface
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import com.simplemobiletools.commons.extensions.setupDialogStuff
import com.simplemobiletools.notes.pro.R
import com.simplemobiletools.notes.pro.activities.SimpleActivity
import com.simplemobiletools.notes.pro.extensions.config
import com.simplemobiletools.notes.pro.models.ChecklistSort
import com.simplemobiletools.notes.pro.models.ChecklistSortDirection
import com.simplemobiletools.notes.pro.models.ChecklistSortField
import kotlinx.android.synthetic.main.dialog_sort_checklist.view.*

class SortChecklistDialog(private val activity: SimpleActivity, val callback: (ChecklistSort) -> Unit) {
    init {
        val config = activity.config
        val view = (activity.layoutInflater.inflate(R.layout.dialog_sort_checklist, null) as ViewGroup).apply {
            sort_field_type.check(
                when (config.checklistSortField) {
                    ChecklistSortField.TITLE -> sort_field_title.id
                    ChecklistSortField.DATE_CREATED -> sort_field_date_created.id
                }
            )

            sort_direction_type.check(
                when (config.checklistSortDirection) {
                    ChecklistSortDirection.ASCENDING -> sort_direction_asc.id
                    ChecklistSortDirection.DESCENDING -> sort_direction_desc.id
                }
            )

            separate_done_from_undone.isChecked = config.checklistSeparateDoneFromUndone
        }

        AlertDialog.Builder(activity)
            .setPositiveButton(R.string.ok, null)
            .setNegativeButton(R.string.cancel, null)
            .create().apply {
                activity.setupDialogStuff(view, this, R.string.sort_checklist) {
                    getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener {
                        val sortField = getSortField(view)
                        val sortDirection = getSortDirection(view)
                        val separateDoneFromUndone = view.separate_done_from_undone.isChecked
                        config.checklistSortField = sortField
                        config.checklistSortDirection = sortDirection
                        config.checklistSeparateDoneFromUndone = separateDoneFromUndone
                        callback.invoke(ChecklistSort(sortField, sortDirection, separateDoneFromUndone))
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
