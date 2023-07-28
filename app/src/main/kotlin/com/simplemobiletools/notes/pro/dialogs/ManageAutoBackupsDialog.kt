package com.simplemobiletools.notes.pro.dialogs

import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import com.simplemobiletools.commons.dialogs.FilePickerDialog
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.helpers.ensureBackgroundThread
import com.simplemobiletools.notes.pro.R
import com.simplemobiletools.notes.pro.activities.SimpleActivity
import com.simplemobiletools.notes.pro.extensions.config
import kotlinx.android.synthetic.main.dialog_manage_automatic_backups.view.backup_notes_filename
import kotlinx.android.synthetic.main.dialog_manage_automatic_backups.view.backup_notes_filename_hint
import kotlinx.android.synthetic.main.dialog_manage_automatic_backups.view.backup_notes_folder
import java.io.File

class ManageAutoBackupsDialog(private val activity: SimpleActivity, onSuccess: () -> Unit) {
    private val view = (activity.layoutInflater.inflate(R.layout.dialog_manage_automatic_backups, null) as ViewGroup)
    private val config = activity.config
    private var backupFolder = config.autoBackupFolder

    init {
        view.apply {
            backup_notes_folder.setText(activity.humanizePath(backupFolder))
            val filename = config.autoBackupFilename.ifEmpty {
                "${activity.getString(R.string.notes)}_%Y%M%D_%h%m%s"
            }

            backup_notes_filename.setText(filename)
            backup_notes_filename_hint.setEndIconOnClickListener {
                DateTimePatternInfoDialog(activity)
            }

            backup_notes_filename_hint.setEndIconOnLongClickListener {
                DateTimePatternInfoDialog(activity)
                true
            }

            backup_notes_folder.setOnClickListener {
                selectBackupFolder()
            }
        }

        activity.getAlertDialogBuilder()
            .setPositiveButton(R.string.ok, null)
            .setNegativeButton(R.string.cancel, null)
            .apply {
                activity.setupDialogStuff(view, this, R.string.manage_automatic_backups) { dialog ->
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                        val filename = view.backup_notes_filename.value
                        when {
                            filename.isEmpty() -> activity.toast(R.string.empty_name)
                            filename.isAValidFilename() -> {
                                val file = File(backupFolder, "$filename.json")
                                if (file.exists() && !file.canWrite()) {
                                    activity.toast(R.string.name_taken)
                                    return@setOnClickListener
                                }

                                ensureBackgroundThread {
                                    config.apply {
                                        autoBackupFolder = backupFolder
                                        autoBackupFilename = filename
                                    }

                                    activity.runOnUiThread {
                                        onSuccess()
                                    }

                                    dialog.dismiss()
                                }
                            }

                            else -> activity.toast(R.string.invalid_name)
                        }
                    }
                }
            }
    }

    private fun selectBackupFolder() {
        activity.hideKeyboard(view.backup_notes_filename)
        FilePickerDialog(activity, backupFolder, false, showFAB = true) { path ->
            activity.handleSAFDialog(path) { grantedSAF ->
                if (!grantedSAF) {
                    return@handleSAFDialog
                }

                activity.handleSAFDialogSdk30(path) { grantedSAF30 ->
                    if (!grantedSAF30) {
                        return@handleSAFDialogSdk30
                    }

                    backupFolder = path
                    view.backup_notes_folder.setText(activity.humanizePath(path))
                }
            }
        }
    }
}
