package com.simplemobiletools.notes.pro.dialogs

import androidx.appcompat.app.AlertDialog
import com.simplemobiletools.commons.dialogs.FilePickerDialog
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.notes.pro.R
import com.simplemobiletools.notes.pro.activities.SimpleActivity
import com.simplemobiletools.notes.pro.extensions.config
import com.simplemobiletools.notes.pro.models.Note
import kotlinx.android.synthetic.main.dialog_export_file.view.*
import java.io.File

class ExportFileDialog(val activity: SimpleActivity, val note: Note, val callback: (exportPath: String) -> Unit) {

    init {
        var realPath = File(note.path).parent ?: activity.config.lastUsedSavePath
        val view = activity.layoutInflater.inflate(R.layout.dialog_export_file, null).apply {
            file_path.text = activity.humanizePath(realPath)

            file_name.setText(note.title)
            file_extension.setText(activity.config.lastUsedExtension)
            file_path.setOnClickListener {
                FilePickerDialog(activity, realPath, false, false, true, true) {
                    file_path.text = activity.humanizePath(it)
                    realPath = it
                }
            }
        }

        AlertDialog.Builder(activity)
                .setPositiveButton(R.string.ok, null)
                .setNegativeButton(R.string.cancel, null)
                .create().apply {
                    activity.setupDialogStuff(view, this, R.string.export_as_file) {
                        showKeyboard(view.file_name)
                        getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                            val filename = view.file_name.value
                            val extension = view.file_extension.value

                            if (filename.isEmpty()) {
                                activity.toast(R.string.filename_cannot_be_empty)
                                return@setOnClickListener
                            }

                            val fullFilename = if (extension.isEmpty()) filename else "$filename.$extension"
                            if (!fullFilename.isAValidFilename()) {
                                activity.toast(String.format(activity.getString(R.string.filename_invalid_characters_placeholder, fullFilename)))
                                return@setOnClickListener
                            }

                            activity.config.lastUsedExtension = extension
                            activity.config.lastUsedSavePath = realPath
                            callback("$realPath/$fullFilename")
                            dismiss()
                        }
                    }
                }
    }
}
