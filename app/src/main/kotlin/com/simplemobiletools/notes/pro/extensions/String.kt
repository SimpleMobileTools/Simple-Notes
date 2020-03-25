package com.simplemobiletools.notes.pro.extensions

import android.os.Build
import android.text.Html
import androidx.annotation.RequiresApi
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.simplemobiletools.commons.helpers.isMarshmallowPlus
import com.simplemobiletools.commons.helpers.isNougatPlus
import com.simplemobiletools.notes.pro.models.ChecklistItem

fun String.parseChecklistItems(): ArrayList<ChecklistItem>? {
    if (startsWith("[{") && endsWith("}]")) {
        try {
            val checklistItemType = object : TypeToken<List<ChecklistItem>>() {}.type
            return Gson().fromJson<ArrayList<ChecklistItem>>(this, checklistItemType) ?: ArrayList(1)
        } catch (e: Exception) {
        }
    }
    return null
}

@RequiresApi(Build.VERSION_CODES.N)
fun String.toHtml() =
        if (isNougatPlus())
            Html.fromHtml(this, Html.FROM_HTML_MODE_LEGACY)
        else
            Html.fromHtml(this)

