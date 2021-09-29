package com.simplemobiletools.notes.pro.models

data class ChecklistItem(val id: Int, val dateCreated: Long = 0L, var title: String, var isDone: Boolean)
