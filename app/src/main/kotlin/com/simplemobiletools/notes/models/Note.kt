package com.simplemobiletools.notes.models

class Note(var id: Int, var title: String, var value: String) {

    override fun equals(o: Any?) = o != null && this.toString() == o.toString()

    override fun toString() = "Note {id=$id, title=$title, value=$value}"
}
