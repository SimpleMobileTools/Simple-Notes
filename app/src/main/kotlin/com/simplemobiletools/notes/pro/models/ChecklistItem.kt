package com.simplemobiletools.notes.pro.models

import com.simplemobiletools.commons.helpers.AlphanumericComparator

data class ChecklistItem(val id: Int, var title: String, var isDone: Boolean):Comparable<ChecklistItem> {

    companion object{
        var isSortDescending = false
    }
    override fun compareTo(other: ChecklistItem): Int {
        var result = AlphanumericComparator().compare(title.toLowerCase(), other.title.toLowerCase())
        if(isSortDescending)
            result *= -1

        return result
    }
}
