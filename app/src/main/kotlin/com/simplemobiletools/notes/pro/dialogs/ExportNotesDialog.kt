package com.simplemobiletools.notes.pro.dialogs

import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.notes.pro.R
import com.simplemobiletools.notes.pro.activities.SimpleActivity
import kotlinx.android.synthetic.main.dialog_export_notes.view.export_notes_filename

class ExportNotesDialog(val activity: SimpleActivity, callback: (filename: String) -> Unit) {

    init {
        val view = (activity.layoutInflater.inflate(R.layout.dialog_export_notes, null) as ViewGroup).apply {
            export_notes_filename.setText(
                buildString {
                    append(context.getString(R.string.notes))
                    append("_")
                    append(context.getCurrentFormattedDateTime())
                }
            )
        }

        activity.getAlertDialogBuilder().setPositiveButton(R.string.ok, null).setNegativeButton(R.string.cancel, null).apply {
            activity.setupDialogStuff(view, this, R.string.export_notes) { alertDialog ->
                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {

                    val filename = view.export_notes_filename.value
                    when {
                        filename.isEmpty() -> activity.toast(R.string.empty_name)
                        filename.isAValidFilename() -> {
                            callback(filename)
                            alertDialog.dismiss()
                        }

                        else -> activity.toast(R.string.invalid_name)
                    }
                }
            }
        }
    }
}

