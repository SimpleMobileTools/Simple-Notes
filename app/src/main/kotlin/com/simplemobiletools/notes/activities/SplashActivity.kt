package com.simplemobiletools.notes.activities

import android.content.Intent
import com.simplemobiletools.commons.activities.BaseSplashActivity
import com.simplemobiletools.notes.helpers.OPEN_NOTE_ID

class SplashActivity : BaseSplashActivity() {
    override fun initActivity() {
        if (intent.extras?.containsKey(OPEN_NOTE_ID) == true) {
            Intent(this, MainActivity::class.java).apply {
                putExtra(OPEN_NOTE_ID, intent.getIntExtra(OPEN_NOTE_ID, -1))
                startActivity(this)
            }
        } else {
            startActivity(Intent(this, MainActivity::class.java))
        }
        finish()
    }
}
