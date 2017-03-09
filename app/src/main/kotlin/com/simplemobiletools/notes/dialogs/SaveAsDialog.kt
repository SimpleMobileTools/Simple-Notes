package com.simplemobiletools.notes.dialogs

import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.WindowManager
import com.simplemobiletools.commons.dialogs.FilePickerDialog
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.notes.R
import com.simplemobiletools.notes.activities.SimpleActivity
import com.simplemobiletools.notes.models.Note
import kotlinx.android.synthetic.main.dialog_save_as.view.*
import java.io.File

class SaveAsDialog(val activity: SimpleActivity, val note: Note, val callback: (savePath: String) -> Unit) {

    init {
        var realPath = File(note.path).parent
        val view = LayoutInflater.from(activity).inflate(R.layout.dialog_save_as, null).apply {
            file_path.text = activity.humanizePath(realPath)

            file_name.setText(note.title)
            file_path.setOnClickListener {
                FilePickerDialog(activity, realPath, false, false, true) {
                    file_path.text = activity.humanizePath(it)
                    realPath = it
                }
            }
        }

        AlertDialog.Builder(activity)
                .setPositiveButton(R.string.ok, null)
                .setNegativeButton(R.string.cancel, null)
                .create().apply {
            window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
            activity.setupDialogStuff(view, this, R.string.save_as)
            getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener({
                val filename = view.file_name.value

                if (filename.isEmpty()) {
                    context.toast(R.string.filename_cannot_be_empty)
                    return@setOnClickListener
                }

                val newFile = File(realPath, filename)
                if (!newFile.name.isAValidFilename()) {
                    context.toast(R.string.filename_invalid_characters)
                    return@setOnClickListener
                }

                callback.invoke(newFile.absolutePath)
                dismiss()
            })
        }
    }
}
