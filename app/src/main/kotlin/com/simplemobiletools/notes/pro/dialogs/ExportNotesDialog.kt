package com.simplemobiletools.notes.pro.dialogs

import androidx.appcompat.app.AlertDialog
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.notes.pro.R
import com.simplemobiletools.notes.pro.activities.SimpleActivity
import com.simplemobiletools.notes.pro.databinding.DialogExportNotesBinding

class ExportNotesDialog(val activity: SimpleActivity, callback: (filename: String) -> Unit) {

    init {
        val binding = DialogExportNotesBinding.inflate(activity.layoutInflater).apply {
            exportNotesFilename.setText(
                buildString {
                    append(root.context.getString(com.simplemobiletools.commons.R.string.notes))
                    append("_")
                    append(root.context.getCurrentFormattedDateTime())
                }
            )
        }

        activity.getAlertDialogBuilder().setPositiveButton(com.simplemobiletools.commons.R.string.ok, null).setNegativeButton(com.simplemobiletools.commons.R.string.cancel, null).apply {
            activity.setupDialogStuff(binding.root, this, R.string.export_notes) { alertDialog ->
                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {

                    val filename = binding.exportNotesFilename.value
                    when {
                        filename.isEmpty() -> activity.toast(com.simplemobiletools.commons.R.string.empty_name)
                        filename.isAValidFilename() -> {
                            callback(filename)
                            alertDialog.dismiss()
                        }

                        else -> activity.toast(com.simplemobiletools.commons.R.string.invalid_name)
                    }
                }
            }
        }
    }
}

