package com.simplemobiletools.notes.dialogs

import android.app.Activity
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.ViewGroup
import com.simplemobiletools.commons.extensions.getFilenameFromPath
import com.simplemobiletools.commons.extensions.humanizePath
import com.simplemobiletools.commons.extensions.setupDialogStuff
import com.simplemobiletools.notes.R
import com.simplemobiletools.notes.helpers.TYPE_NOTE
import com.simplemobiletools.notes.models.Note
import kotlinx.android.synthetic.main.dialog_open_file.view.*
import java.io.File

class OpenFileDialog(val activity: Activity, val path: String, val callback: (note: Note) -> Unit) : AlertDialog.Builder(activity) {
    init {
        val view = (LayoutInflater.from(activity).inflate(R.layout.dialog_open_file, null) as ViewGroup).apply {
            open_file_filename.text = activity.humanizePath(path)
        }

        AlertDialog.Builder(activity)
                .setPositiveButton(R.string.ok, null)
                .setNegativeButton(R.string.cancel, null)
                .create().apply {
            activity.setupDialogStuff(view, this, R.string.open_file)
            getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener({
                val file = File(path)
                val filename = path.getFilenameFromPath()
                val content = file.readText()
                val note = Note(0, filename, content, TYPE_NOTE, path)
                callback.invoke(note)
                dismiss()
            })
        }
    }
}
