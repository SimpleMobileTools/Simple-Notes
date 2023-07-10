package com.simplemobiletools.notes.pro.models

import com.simplemobiletools.commons.helpers.AlphanumericComparator
import com.simplemobiletools.commons.helpers.SORT_BY_TITLE
import com.simplemobiletools.commons.helpers.SORT_DESCENDING
import kotlinx.serialization.Serializable

@Serializable
data class ChecklistItem(
    val id: Int,
    val dateCreated: Long = 0L,
    var title: String,
    var isDone: Boolean
) : Comparable<ChecklistItem> {

    companion object {
        var sorting = 0
    }

    override fun compareTo(other: ChecklistItem): Int {
        var result = when {
            sorting and SORT_BY_TITLE != 0 -> AlphanumericComparator().compare(title.lowercase(), other.title.lowercase())
            else -> dateCreated.compareTo(other.dateCreated)
        }

        if (sorting and SORT_DESCENDING != 0) {
            result *= -1
        }

        return result
    }
}
