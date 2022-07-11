package com.simplemobiletools.notes.pro.dialogs

import androidx.appcompat.app.AlertDialog
import com.simplemobiletools.commons.dialogs.FilePickerDialog
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.notes.pro.R
import com.simplemobiletools.notes.pro.activities.SimpleActivity
import com.simplemobiletools.notes.pro.extensions.config
import com.simplemobiletools.notes.pro.models.Note
import kotlinx.android.synthetic.main.dialog_export_files.view.*

class ExportFilesDialog(val activity: SimpleActivity, val notes: ArrayList<Note>, val callback: (parent: String, extension: String) -> Unit) {
    init {
        var realPath = activity.config.lastUsedSavePath
        val view = activity.layoutInflater.inflate(R.layout.dialog_export_files, null).apply {
            folder_path.text = activity.humanizePath(realPath)

            file_extension.setText(activity.config.lastUsedExtension)
            folder_path.setOnClickListener {
                FilePickerDialog(activity, realPath, false, false, true, true) {
                    folder_path.text = activity.humanizePath(it)
                    realPath = it
                }
            }
        }

        activity.getAlertDialogBuilder()
            .setPositiveButton(R.string.ok, null)
            .setNegativeButton(R.string.cancel, null)
            .apply {
                activity.setupDialogStuff(view, this, R.string.export_as_file) { alertDialog ->
                    alertDialog.showKeyboard(view.file_extension)
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                        activity.handleSAFDialog(realPath) {
                            val extension = view.file_extension.value
                            activity.config.lastUsedExtension = extension
                            activity.config.lastUsedSavePath = realPath
                            callback(realPath, extension)
                            alertDialog.dismiss()
                        }
                    }
                }
            }
    }
}
