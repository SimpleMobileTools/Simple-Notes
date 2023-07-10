package com.simplemobiletools.notes.pro.models

import kotlinx.serialization.Serializable

@Serializable
enum class NoteType(val value: Int) {
    TYPE_TEXT(0),
    TYPE_CHECKLIST(1);

    companion object {
        fun fromValue(value: Int): NoteType {
            return values().find { it.value == value } ?: TYPE_TEXT
        }
    }
}
