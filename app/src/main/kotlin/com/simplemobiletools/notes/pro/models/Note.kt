package com.simplemobiletools.notes.pro.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.io.File
import java.io.FileNotFoundException

@Entity(tableName = "notes", indices = [(Index(value = ["id"], unique = true))])
data class Note(
        @PrimaryKey(autoGenerate = true) var id: Long?,
        @ColumnInfo(name = "title") var title: String,
        @ColumnInfo(name = "value") var value: String,
        @ColumnInfo(name = "type") var type: Int,
        @ColumnInfo(name = "path") var path: String = "") {

    fun getNoteStoredValue(): String? {
        return if (path.isNotEmpty()) {
            try {
                File(path).readText()
            } catch (e: FileNotFoundException) {
                null
            }
        } else {
            value
        }
    }
}
