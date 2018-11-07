package com.simplemobiletools.notes.pro.dialogs

import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import com.simplemobiletools.commons.extensions.getFilenameFromPath
import com.simplemobiletools.commons.extensions.humanizePath
import com.simplemobiletools.commons.extensions.isMediaFile
import com.simplemobiletools.commons.extensions.setupDialogStuff
import com.simplemobiletools.notes.pro.R
import com.simplemobiletools.notes.pro.activities.SimpleActivity
import com.simplemobiletools.notes.pro.extensions.dbHelper
import com.simplemobiletools.notes.pro.extensions.notesDB
import com.simplemobiletools.notes.pro.helpers.TYPE_NOTE
import com.simplemobiletools.notes.pro.models.Note
import kotlinx.android.synthetic.main.dialog_import_folder.view.*
import java.io.File

class ImportFolderDialog(val activity: SimpleActivity, val path: String, val callback: () -> Unit) : AlertDialog.Builder(activity) {
    private var dialog: AlertDialog

    init {
        val view = (activity.layoutInflater.inflate(R.layout.dialog_import_folder, null) as ViewGroup).apply {
            open_file_filename.text = activity.humanizePath(path)
        }

        dialog = AlertDialog.Builder(activity)
                .setPositiveButton(R.string.ok, null)
                .setNegativeButton(R.string.cancel, null)
                .create().apply {
                    activity.setupDialogStuff(view, this, R.string.import_folder) {
                        getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                            val updateFilesOnEdit = view.open_file_type.checkedRadioButtonId == R.id.open_file_update_file
                            saveFolder(updateFilesOnEdit)
                        }
                    }
                }
    }

    private fun saveFolder(updateFilesOnEdit: Boolean) {
        val folder = File(path)
        folder.listFiles { file ->
            val filename = file.path.getFilenameFromPath()
            when {
                file.isDirectory -> false
                filename.isMediaFile() -> false
                file.length() > 10 * 1000 * 1000 -> false
                activity.dbHelper.doesNoteTitleExist(filename) -> false
                else -> true
            }
        }.forEach {
            val storePath = if (updateFilesOnEdit) it.absolutePath else ""
            val title = it.absolutePath.getFilenameFromPath()
            val value = if (updateFilesOnEdit) "" else it.readText()

            if (updateFilesOnEdit) {
                activity.handleSAFDialog(path) {
                    saveNote(title, value, storePath)
                }
            } else {
                saveNote(title, value, storePath)
            }
        }

        callback()
        dialog.dismiss()
    }

    private fun saveNote(title: String, value: String, path: String) {
        val note = Note(null, title, value, TYPE_NOTE, path)
        Thread {
            activity.notesDB.insertOrUpdate(note)
        }
    }
}
