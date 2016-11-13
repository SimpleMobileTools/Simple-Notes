package com.simplemobiletools.notes.models

class Note(var id: Int, var title: String, var value: String) {

    override fun equals(other: Any?) = other != null && this.toString() == other.toString()

    override fun toString() = "Note {id=$id, title=$title, value=$value}"
}
