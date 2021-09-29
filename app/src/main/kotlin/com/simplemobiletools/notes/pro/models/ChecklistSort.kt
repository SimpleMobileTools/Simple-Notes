package com.simplemobiletools.notes.pro.models

data class ChecklistSort(
    val field: ChecklistSortField,
    val direction: ChecklistSortDirection,
    val separateCheckedFromUnchecked: Boolean,
) {

    companion object {
        val DEFAULT = ChecklistSort(ChecklistSortField.TITLE, ChecklistSortDirection.ASCENDING, false)
    }

    fun getSortComparator(): Comparator<ChecklistItem> {
        return when (field) {
            ChecklistSortField.TITLE -> compareWithSortDirection { it.title }
            ChecklistSortField.DATE_CREATED -> compareWithSortDirection { it.dateCreated }
        }
    }

    private fun compareWithSortDirection(compareFunc: (ChecklistItem) -> Comparable<*>): Comparator<ChecklistItem> {
        return when (direction) {
            ChecklistSortDirection.ASCENDING -> if(separateCheckedFromUnchecked) compareBy<ChecklistItem> { it.isDone }.thenBy(compareFunc) else compareBy(compareFunc)
            ChecklistSortDirection.DESCENDING -> if(separateCheckedFromUnchecked) compareByDescending<ChecklistItem> { it.isDone }.thenByDescending(compareFunc) else compareByDescending(compareFunc)
        }
    }
}
