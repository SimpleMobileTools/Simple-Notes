package com.simplemobiletools.notes.activities

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.simplemobiletools.notes.helpers.OPEN_NOTE_ID

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
