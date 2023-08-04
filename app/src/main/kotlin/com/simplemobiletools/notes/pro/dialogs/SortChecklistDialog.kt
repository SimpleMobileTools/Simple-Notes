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
import com.simplemobiletools.notes.pro.databinding.DialogSortChecklistBinding
import com.simplemobiletools.notes.pro.extensions.config

class SortChecklistDialog(private val activity: SimpleActivity, private val callback: () -> Unit) {
    private val binding = DialogSortChecklistBinding.inflate(activity.layoutInflater)
    private val view = binding.root
    private val config = activity.config
    private var currSorting = config.sorting

    init {
        setupSortRadio()
        setupOrderRadio()
        setupMoveUndoneChecklistItems()

        activity.getAlertDialogBuilder()
            .setPositiveButton(com.simplemobiletools.commons.R.string.ok) { _, _ -> dialogConfirmed() }
            .setNegativeButton(com.simplemobiletools.commons.R.string.cancel, null)
            .apply {
                activity.setupDialogStuff(view, this, com.simplemobiletools.commons.R.string.sort_by)
            }
    }

    private fun setupSortRadio() {
        val fieldRadio = binding.sortingDialogRadioSorting
        fieldRadio.setOnCheckedChangeListener { group, checkedId ->
            val isCustomSorting = checkedId == binding.sortingDialogRadioCustom.id
            binding.sortingDialogRadioOrder.beGoneIf(isCustomSorting)
            binding.sortingDialogOrderDivider.beGoneIf(isCustomSorting)
            binding.moveUndoneChecklistItemsDivider.beGoneIf(isCustomSorting)
            binding.settingsMoveUndoneChecklistItemsHolder.beGoneIf(isCustomSorting)
        }

        var fieldBtn = binding.sortingDialogRadioTitle

        if (currSorting and SORT_BY_DATE_CREATED != 0) {
            fieldBtn = binding.sortingDialogRadioDateCreated
        }

        if (currSorting and SORT_BY_CUSTOM != 0) {
            fieldBtn = binding.sortingDialogRadioCustom
        }

        fieldBtn.isChecked = true
    }

    private fun setupOrderRadio() {
        var orderBtn = binding.sortingDialogRadioAscending

        if (currSorting and SORT_DESCENDING != 0) {
            orderBtn = binding.sortingDialogRadioDescending
        }

        orderBtn.isChecked = true
    }

    private fun setupMoveUndoneChecklistItems() {
        binding.settingsMoveUndoneChecklistItems.isChecked = config.moveDoneChecklistItems
        binding.settingsMoveUndoneChecklistItemsHolder.setOnClickListener {
            binding.settingsMoveUndoneChecklistItems.toggle()
        }
    }

    private fun dialogConfirmed() {
        val sortingRadio = binding.sortingDialogRadioSorting
        var sorting = when (sortingRadio.checkedRadioButtonId) {
            R.id.sorting_dialog_radio_date_created -> SORT_BY_DATE_CREATED
            R.id.sorting_dialog_radio_custom -> SORT_BY_CUSTOM
            else -> SORT_BY_TITLE
        }

        if (sortingRadio.checkedRadioButtonId != R.id.sorting_dialog_radio_custom
            && binding.sortingDialogRadioOrder.checkedRadioButtonId == R.id.sorting_dialog_radio_descending
        ) {
            sorting = sorting or SORT_DESCENDING
        }

        if (currSorting != sorting) {
            config.sorting = sorting
        }

        config.moveDoneChecklistItems = binding.settingsMoveUndoneChecklistItems.isChecked
        callback()
    }
}
