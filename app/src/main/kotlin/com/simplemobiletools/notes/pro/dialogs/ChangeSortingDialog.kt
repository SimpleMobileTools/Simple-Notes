package com.simplemobiletools.notes.pro.dialogs

import android.app.Activity
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.simplemobiletools.commons.extensions.setupDialogStuff
import com.simplemobiletools.notes.pro.R
import kotlinx.android.synthetic.main.dialog_change_sorting.view.*

class ChangeSortingDialog(val activity: Activity, val callback: (Boolean) -> Unit) {
    private var view: View

    init {
        view = activity.layoutInflater.inflate(R.layout.dialog_change_sorting, null)

        AlertDialog.Builder(activity)
            .setPositiveButton(R.string.ok) { _, _ -> dialogConfirmed() }
            .setNegativeButton(R.string.cancel, null)
            .create().apply {
                activity.setupDialogStuff(view, this, R.string.sort_by)
            }

        setupOrderRadio()
    }

    private fun setupOrderRadio() {
        val orderRadio = view.sorting_dialog_radio_order
        val orderBtn = orderRadio.sorting_dialog_radio_ascending
        orderBtn.isChecked = true
    }

    private fun dialogConfirmed() {
        val isDescending = view.sorting_dialog_radio_order.checkedRadioButtonId == R.id.sorting_dialog_radio_descending
        callback(isDescending)
    }
}
