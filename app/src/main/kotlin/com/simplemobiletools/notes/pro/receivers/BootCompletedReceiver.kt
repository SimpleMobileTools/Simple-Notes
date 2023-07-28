package com.simplemobiletools.notes.pro.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.simplemobiletools.commons.helpers.ensureBackgroundThread

class BootCompletedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        ensureBackgroundThread {
            context.apply {
                checkAndBackupNotesOnBoot()
            }
        }
    }
}
