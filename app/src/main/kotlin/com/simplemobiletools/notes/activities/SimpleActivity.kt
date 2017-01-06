package com.simplemobiletools.notes.activities

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem

import com.simplemobiletools.notes.Config
import com.simplemobiletools.notes.R

open class SimpleActivity : AppCompatActivity() {
    lateinit var config: Config

    override fun onCreate(savedInstanceState: Bundle?) {
        config = Config.newInstance(applicationContext)
        super.onCreate(savedInstanceState)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
