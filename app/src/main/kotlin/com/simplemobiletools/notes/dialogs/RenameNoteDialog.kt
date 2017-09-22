package com.simplemobiletools.notes.dialogs

import android.content.DialogInterface.BUTTON_POSITIVE
import android.support.v7.app.AlertDialog
import android.view.WindowManager
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.notes.R
import com.simplemobiletools.notes.activities.SimpleActivity
import com.simplemobiletools.notes.helpers.DBHelper
import com.simplemobiletools.notes.models.Note
import kotlinx.android.synthetic.main.new_note.view.*
import java.io.File

class RenameNoteDialog(val activity: SimpleActivity, val db: DBHelper, val note: Note, callback: (note: Note) -> Unit) {

    init {
        val view = activity.layoutInflater.inflate(R.layout.rename_note, null)
        view.note_name.setText(note.title)

        AlertDialog.Builder(activity)
                .setPositiveButton(R.string.ok, null)
                .setNegativeButton(R.string.cancel, null)
                .create().apply {
            window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
            activity.setupDialogStuff(view, this, R.string.rename_note)
            getButton(BUTTON_POSITIVE).setOnClickListener({
                val title = view.note_name.value
                when {
                    title.isEmpty() -> activity.toast(R.string.no_title)
                    db.doesTitleExist(title) -> activity.toast(R.string.title_taken)
                    else -> {
                        note.title = title
                        val path = note.path
                        if (path.isNotEmpty()) {
                            if (title.isEmpty()) {
                                activity.toast(R.string.filename_cannot_be_empty)
                                return@setOnClickListener
                            }

                            val file = File(path)
                            val newFile = File(file.parent, title)
                            if (!newFile.name.isAValidFilename()) {
                                activity.toast(R.string.invalid_name)
                                return@setOnClickListener
                            }

                            activity.renameFile(file, newFile) {
                                if (it) {
                                    note.path = newFile.absolutePath
                                    db.updateNotePath(note)
                                } else {
                                    activity.toast(R.string.rename_file_error)
                                    return@renameFile
                                }
                            }
                        }
                        db.updateNoteTitle(note)
                        dismiss()
                        callback(note)
                    }
                }
            })
        }
    }
}
