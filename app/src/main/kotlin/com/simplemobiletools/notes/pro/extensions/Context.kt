package com.simplemobiletools.notes.pro.extensions

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.core.app.AlarmManagerCompat
import com.simplemobiletools.commons.activities.BaseSimpleActivity
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.helpers.ExportResult
import com.simplemobiletools.commons.helpers.ensureBackgroundThread
import com.simplemobiletools.commons.helpers.isRPlus
import com.simplemobiletools.notes.pro.R
import com.simplemobiletools.notes.pro.databases.NotesDatabase
import com.simplemobiletools.notes.pro.dialogs.UnlockNotesDialog
import com.simplemobiletools.notes.pro.helpers.*
import com.simplemobiletools.notes.pro.interfaces.NotesDao
import com.simplemobiletools.notes.pro.interfaces.WidgetsDao
import com.simplemobiletools.notes.pro.models.Note
import com.simplemobiletools.notes.pro.receivers.AutomaticBackupReceiver
import org.joda.time.DateTime
import java.io.File
import java.io.FileOutputStream

val Context.config: Config get() = Config.newInstance(applicationContext)

val Context.notesDB: NotesDao get() = NotesDatabase.getInstance(applicationContext).NotesDao()

val Context.widgetsDB: WidgetsDao get() = NotesDatabase.getInstance(applicationContext).WidgetsDao()

fun Context.updateWidgets() {
    val widgetIDs = AppWidgetManager.getInstance(applicationContext)?.getAppWidgetIds(ComponentName(applicationContext, MyWidgetProvider::class.java)) ?: return
    if (widgetIDs.isNotEmpty()) {
        Intent(applicationContext, MyWidgetProvider::class.java).apply {
            action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetIDs)
            sendBroadcast(this)
        }
    }
}

fun Context.getPercentageFontSize() = resources.getDimension(com.simplemobiletools.commons.R.dimen.middle_text_size) * (config.fontSizePercentage / 100f)

fun BaseSimpleActivity.requestUnlockNotes(notes: List<Note>, callback: (unlockedNotes: List<Note>) -> Unit) {
    val lockedNotes = notes.filter { it.isLocked() }
    if (lockedNotes.isNotEmpty()) {
        runOnUiThread {
            UnlockNotesDialog(this, lockedNotes, callback)
        }
    } else {
        callback(emptyList())
    }
}

fun Context.getAutomaticBackupIntent(): PendingIntent {
    val intent = Intent(this, AutomaticBackupReceiver::class.java)
    return PendingIntent.getBroadcast(this, AUTOMATIC_BACKUP_REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
}

fun Context.scheduleNextAutomaticBackup() {
    if (config.autoBackup) {
        val backupAtMillis = getNextAutoBackupTime().millis
        val pendingIntent = getAutomaticBackupIntent()
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        try {
            AlarmManagerCompat.setExactAndAllowWhileIdle(alarmManager, AlarmManager.RTC_WAKEUP, backupAtMillis, pendingIntent)
        } catch (e: Exception) {
            showErrorToast(e)
        }
    }
}

fun Context.cancelScheduledAutomaticBackup() {
    val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
    alarmManager.cancel(getAutomaticBackupIntent())
}

fun Context.checkAndBackupNotesOnBoot() {
    if (config.autoBackup) {
        val previousRealBackupTime = config.lastAutoBackupTime
        val previousScheduledBackupTime = getPreviousAutoBackupTime().millis
        val missedPreviousBackup = previousRealBackupTime < previousScheduledBackupTime
        if (missedPreviousBackup) {
            // device was probably off at the scheduled time so backup now
            backupNotes()
        }
    }
}

fun Context.backupNotes() {
    require(isRPlus())
    ensureBackgroundThread {
        val config = config
        NotesHelper(this).getNotes { notesToBackup ->
            if (notesToBackup.isEmpty()) {
                toast(com.simplemobiletools.commons.R.string.no_entries_for_exporting)
                config.lastAutoBackupTime = DateTime.now().millis
                scheduleNextAutomaticBackup()
                return@getNotes
            }


            val now = DateTime.now()
            val year = now.year.toString()
            val month = now.monthOfYear.ensureTwoDigits()
            val day = now.dayOfMonth.ensureTwoDigits()
            val hours = now.hourOfDay.ensureTwoDigits()
            val minutes = now.minuteOfHour.ensureTwoDigits()
            val seconds = now.secondOfMinute.ensureTwoDigits()

            val filename = config.autoBackupFilename
                .replace("%Y", year, false)
                .replace("%M", month, false)
                .replace("%D", day, false)
                .replace("%h", hours, false)
                .replace("%m", minutes, false)
                .replace("%s", seconds, false)

            val outputFolder = File(config.autoBackupFolder).apply {
                mkdirs()
            }

            var exportFile = File(outputFolder, "$filename.json")
            var exportFilePath = exportFile.absolutePath
            val outputStream = try {
                if (hasProperStoredFirstParentUri(exportFilePath)) {
                    val exportFileUri = createDocumentUriUsingFirstParentTreeUri(exportFilePath)
                    if (!getDoesFilePathExist(exportFilePath)) {
                        createSAFFileSdk30(exportFilePath)
                    }
                    applicationContext.contentResolver.openOutputStream(exportFileUri, "wt") ?: FileOutputStream(exportFile)
                } else {
                    var num = 0
                    while (getDoesFilePathExist(exportFilePath) && !exportFile.canWrite()) {
                        num++
                        exportFile = File(outputFolder, "${filename}_${num}.json")
                        exportFilePath = exportFile.absolutePath
                    }
                    FileOutputStream(exportFile)
                }
            } catch (e: Exception) {
                showErrorToast(e)
                scheduleNextAutomaticBackup()
                return@getNotes
            }

            val exportResult = try {
                NotesHelper(this).exportNotes(notesToBackup, outputStream)
            } catch (e: Exception) {
                showErrorToast(e)
            }

            if (exportResult == ExportResult.EXPORT_FAIL) {
                toast(com.simplemobiletools.commons.R.string.exporting_failed)
            }

            config.lastAutoBackupTime = DateTime.now().millis
            scheduleNextAutomaticBackup()
        }
    }
}
