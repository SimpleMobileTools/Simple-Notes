package com.simplemobiletools.notes.pro.dialogs

import com.simplemobiletools.commons.extensions.beGoneIf
import com.simplemobiletools.commons.extensions.getAlertDialogBuilder
import com.simplemobiletools.commons.extensions.setupDialogStuff
import com.simplemobiletools.commons.helpers.SORT_BY_CUSTOM
import com.simplemobiletools.commons.helpers.SORT_BY_DATE_CREATED
import com.simplemobiletools.commons.helpers.SORT_BY_TITLE
import com.simplemobiletools.commons.helpers.SORT_DESCENDING
import com.simplemobiletools.notes.pro.R
import com.simplemobiletools.notes.pro.activities.SimpleActivity
import com.simplemobiletools.notes.pro.extensions.config
import kotlinx.android.synthetic.main.dialog_sort_checklist.view.*

class SortChecklistDialog(private val activity: SimpleActivity, private val callback: () -> Unit) {
    private val view = activity.layoutInflater.inflate(R.layout.dialog_sort_checklist, null)
    private val config = activity.config
    private var currSorting = config.sorting

    init {
        setupSortRadio()
        setupOrderRadio()
        setupMoveUndoneChecklistItems()

        activity.getAlertDialogBuilder()
            .setPositiveButton(R.string.ok) { _, _ -> dialogConfirmed() }
            .setNegativeButton(R.string.cancel, null)
            .apply {
                activity.setupDialogStuff(view, this, R.string.sort_by)
            }
    }

    private fun setupSortRadio() {
        val fieldRadio = view.sorting_dialog_radio_sorting
        fieldRadio.setOnCheckedChangeListener { group, checkedId ->
            val isCustomSorting = checkedId == fieldRadio.sorting_dialog_radio_custom.id
            view.sorting_dialog_radio_order.beGoneIf(isCustomSorting)
            view.sorting_dialog_order_divider.beGoneIf(isCustomSorting)
            view.move_undone_checklist_items_divider.beGoneIf(isCustomSorting)
            view.settings_move_undone_checklist_items_holder.beGoneIf(isCustomSorting)
        }

        var fieldBtn = fieldRadio.sorting_dialog_radio_title

        if (currSorting and SORT_BY_DATE_CREATED != 0) {
            fieldBtn = fieldRadio.sorting_dialog_radio_date_created
        }

        if (currSorting and SORT_BY_CUSTOM != 0) {
            fieldBtn = fieldRadio.sorting_dialog_radio_custom
        }

        fieldBtn.isChecked = true
    }

    private fun setupOrderRadio() {
        val orderRadio = view.sorting_dialog_radio_order
        var orderBtn = orderRadio.sorting_dialog_radio_ascending

        if (currSorting and SORT_DESCENDING != 0) {
            orderBtn = orderRadio.sorting_dialog_radio_descending
        }

        orderBtn.isChecked = true
    }

    private fun setupMoveUndoneChecklistItems() {
        view.settings_move_undone_checklist_items.isChecked = config.moveDoneChecklistItems
        view.settings_move_undone_checklist_items_holder.setOnClickListener {
            view.settings_move_undone_checklist_items.toggle()
        }
    }

    private fun dialogConfirmed() {
        val sortingRadio = view.sorting_dialog_radio_sorting
        var sorting = when (sortingRadio.checkedRadioButtonId) {
            R.id.sorting_dialog_radio_date_created -> SORT_BY_DATE_CREATED
            R.id.sorting_dialog_radio_custom -> SORT_BY_CUSTOM
            else -> SORT_BY_TITLE
        }

        if (sortingRadio.checkedRadioButtonId != R.id.sorting_dialog_radio_custom
            && view.sorting_dialog_radio_order.checkedRadioButtonId == R.id.sorting_dialog_radio_descending
        ) {
            sorting = sorting or SORT_DESCENDING
        }

        if (currSorting != sorting) {
            config.sorting = sorting
        }

        config.moveDoneChecklistItems = view.settings_move_undone_checklist_items.isChecked
        callback()
    }
}
