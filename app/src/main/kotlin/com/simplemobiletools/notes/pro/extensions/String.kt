package com.simplemobiletools.notes.pro.extensions

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.simplemobiletools.notes.pro.models.ChecklistItem

fun String.parseChecklistItems(): ArrayList<ChecklistItem>? {
    if (startsWith("[{") && endsWith("}]")) {
        try {
            val checklistItemType = object : TypeToken<List<ChecklistItem>>() {}.type
            return Gson().fromJson<ArrayList<ChecklistItem>>(this, checklistItemType) ?: null
        } catch (e: Exception) {
        }
    }
    return null
}
