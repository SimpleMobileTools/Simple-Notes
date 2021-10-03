package com.simplemobiletools.notes.pro.models

data class ChecklistSort(
    val field: ChecklistSortField,
    val direction: ChecklistSortDirection,
) {

    fun getSortComparator(): Comparator<ChecklistItem> {
        return when (field) {
            ChecklistSortField.TITLE -> compareWithSortDirection { it.title }
            ChecklistSortField.DATE_CREATED -> compareWithSortDirection { it.dateCreated }
        }
    }

    private fun compareWithSortDirection(compareFunc: (ChecklistItem) -> Comparable<*>): Comparator<ChecklistItem> {
        return when (direction) {
            ChecklistSortDirection.ASCENDING -> compareBy(compareFunc)
            ChecklistSortDirection.DESCENDING -> compareByDescending(compareFunc)
        }
    }
}
