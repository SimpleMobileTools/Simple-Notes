package com.simplemobiletools.notes.pro.dialogs

import androidx.appcompat.app.AlertDialog
import com.simplemobiletools.commons.dialogs.FilePickerDialog
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.helpers.ensureBackgroundThread
import com.simplemobiletools.notes.pro.activities.SimpleActivity
import com.simplemobiletools.notes.pro.databinding.DialogManageAutomaticBackupsBinding
import com.simplemobiletools.notes.pro.extensions.config
import java.io.File

class ManageAutoBackupsDialog(private val activity: SimpleActivity, onSuccess: () -> Unit) {
    private val binding = DialogManageAutomaticBackupsBinding.inflate(activity.layoutInflater)
    private val view = binding.root
    private val config = activity.config
    private var backupFolder = config.autoBackupFolder

    init {
        binding.apply {
            backupNotesFolder.setText(activity.humanizePath(backupFolder))
            val filename = config.autoBackupFilename.ifEmpty {
                "${activity.getString(com.simplemobiletools.commons.R.string.notes)}_%Y%M%D_%h%m%s"
            }

            backupNotesFilename.setText(filename)
            backupNotesFilenameHint.setEndIconOnClickListener {
                DateTimePatternInfoDialog(activity)
            }

            backupNotesFilenameHint.setEndIconOnLongClickListener {
                DateTimePatternInfoDialog(activity)
                true
            }

            backupNotesFolder.setOnClickListener {
                selectBackupFolder()
            }
        }

        activity.getAlertDialogBuilder()
            .setPositiveButton(com.simplemobiletools.commons.R.string.ok, null)
            .setNegativeButton(com.simplemobiletools.commons.R.string.cancel, null)
            .apply {
                activity.setupDialogStuff(view, this, com.simplemobiletools.commons.R.string.manage_automatic_backups) { dialog ->
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                        val filename = binding.backupNotesFilename.value
                        when {
                            filename.isEmpty() -> activity.toast(com.simplemobiletools.commons.R.string.empty_name)
                            filename.isAValidFilename() -> {
                                val file = File(backupFolder, "$filename.json")
                                if (file.exists() && !file.canWrite()) {
                                    activity.toast(com.simplemobiletools.commons.R.string.name_taken)
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

                            else -> activity.toast(com.simplemobiletools.commons.R.string.invalid_name)
                        }
                    }
                }
            }
    }

    private fun selectBackupFolder() {
        activity.hideKeyboard(binding.backupNotesFilename)
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
                    binding.backupNotesFolder.setText(activity.humanizePath(path))
                }
            }
        }
    }
}
