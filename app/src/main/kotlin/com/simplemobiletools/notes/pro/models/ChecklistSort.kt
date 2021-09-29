package com.simplemobiletools.notes.pro.models

data class ChecklistSort(
    val field: ChecklistSortField,
    val direction: ChecklistSortDirection,
    val separateDoneFromUndone: Boolean,
) {

    fun getSortComparator(): Comparator<ChecklistItem> {
        return when (field) {
            ChecklistSortField.TITLE -> compareWithSortDirection { it.title }
            ChecklistSortField.DATE_CREATED -> compareWithSortDirection { it.dateCreated }
        }
    }

    private fun compareWithSortDirection(compareFunc: (ChecklistItem) -> Comparable<*>): Comparator<ChecklistItem> {
        return when (direction) {
            ChecklistSortDirection.ASCENDING -> if (separateDoneFromUndone) compareBy({ it.isDone }, compareFunc) else compareBy(compareFunc)
            ChecklistSortDirection.DESCENDING -> if (separateDoneFromUndone) compareByDescending<ChecklistItem> { it.isDone }.thenByDescending(compareFunc) else compareByDescending(
                compareFunc
            )
        }
    }
}
