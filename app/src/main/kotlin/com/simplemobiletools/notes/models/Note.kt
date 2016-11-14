package com.simplemobiletools.notes.models

class Note(var id: Int, var title: String, var value: String, val type: Int) {

    override fun equals(other: Any?) = other != null && this.toString() == other.toString()

    override fun toString() = "Note {id=$id, title=$title, value=$value, type=$type}"
}
