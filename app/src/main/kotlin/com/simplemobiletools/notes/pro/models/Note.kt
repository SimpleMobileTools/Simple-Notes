package com.simplemobiletools.notes.pro.models

import java.io.File
import java.io.FileNotFoundException

data class Note(var id: Int, var title: String, var value: String, val type: Int, var path: String = "") {
    fun getNoteStoredValue(): String? {
        return if (path.isNotEmpty()) {
            return try {
                File(path).readText()
            } catch (e: FileNotFoundException) {
                null
            }
        } else {
            value
        }
    }
}
