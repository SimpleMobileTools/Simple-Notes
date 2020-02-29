package com.simplemobiletools.notes.pro.dialogs

import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import com.simplemobiletools.commons.extensions.getFilenameFromPath
import com.simplemobiletools.commons.extensions.humanizePath
import com.simplemobiletools.commons.extensions.setupDialogStuff
import com.simplemobiletools.notes.pro.R
import com.simplemobiletools.notes.pro.activities.SimpleActivity
import com.simplemobiletools.notes.pro.helpers.NoteType
import com.simplemobiletools.notes.pro.models.Note
import kotlinx.android.synthetic.main.dialog_open_file.*
import kotlinx.android.synthetic.main.dialog_open_file.view.*
import java.io.File

class OpenFileDialog(val activity: SimpleActivity, val path: String, val callback: (note: Note) -> Unit) : AlertDialog.Builder(activity) {
    private var dialog: AlertDialog

    init {
        val view = (activity.layoutInflater.inflate(R.layout.dialog_open_file, null) as ViewGroup).apply {
            open_file_filename.text = activity.humanizePath(path)
        }

        dialog = AlertDialog.Builder(activity)
                .setPositiveButton(R.string.ok, null)
                .setNegativeButton(R.string.cancel, null)
                .create().apply {
                    activity.setupDialogStuff(view, this, R.string.open_file) {
                        getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                            val updateFileOnEdit = view.open_file_type.checkedRadioButtonId == open_file_update_file.id
                            val storePath = if (updateFileOnEdit) path else ""
                            val storeContent = if (updateFileOnEdit) "" else File(path).readText()

                            if (updateFileOnEdit) {
                                activity.handleSAFDialog(path) {
                                    saveNote(storeContent, storePath)
                                }
                            } else {
                                saveNote(storeContent, storePath)
                            }
                        }
                    }
                }
    }

    private fun saveNote(storeContent: String, storePath: String) {
        val filename = path.getFilenameFromPath()
        val note = Note(null, filename, storeContent, NoteType.TYPE_TEXT.value, storePath)
        callback(note)
        dialog.dismiss()
    }
}
