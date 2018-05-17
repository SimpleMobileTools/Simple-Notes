package com.simplemobiletools.notes.dialogs

import android.support.v7.app.AlertDialog
import android.view.ViewGroup
import com.simplemobiletools.commons.extensions.getFilenameFromPath
import com.simplemobiletools.commons.extensions.humanizePath
import com.simplemobiletools.commons.extensions.isImageVideoGif
import com.simplemobiletools.commons.extensions.setupDialogStuff
import com.simplemobiletools.notes.R
import com.simplemobiletools.notes.activities.SimpleActivity
import com.simplemobiletools.notes.extensions.dbHelper
import com.simplemobiletools.notes.helpers.TYPE_NOTE
import com.simplemobiletools.notes.models.Note
import kotlinx.android.synthetic.main.dialog_import_folder.view.*
import java.io.File

class ImportFolderDialog(val activity: SimpleActivity, val path: String, val callback: (id: Int) -> Unit) : AlertDialog.Builder(activity) {
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
        var lastSavedNoteId = -1
        folder.listFiles { file ->
            val filename = file.path.getFilenameFromPath()
            when {
                file.isDirectory -> false
                filename.isImageVideoGif() -> false
                file.length() > 10 * 1000 * 1000 -> false
                activity.dbHelper.doesTitleExist(filename) -> false
                else -> true
            }
        }.forEach {
            val storePath = if (updateFilesOnEdit) it.path else ""
            val storeContent = if (updateFilesOnEdit) "" else it.readText()

            if (updateFilesOnEdit) {
                activity.handleSAFDialog(path) {
                    lastSavedNoteId = saveNote(storeContent, storePath)
                }
            } else {
                lastSavedNoteId = saveNote(storeContent, storePath)
            }
        }

        if (lastSavedNoteId != -1) {
            callback(lastSavedNoteId)
        }

        dialog.dismiss()
    }

    private fun saveNote(storeContent: String, storePath: String): Int {
        val filename = storePath.getFilenameFromPath()
        val note = Note(0, filename, storeContent, TYPE_NOTE, storePath)
        return activity.dbHelper.insertNote(note)
    }
}
