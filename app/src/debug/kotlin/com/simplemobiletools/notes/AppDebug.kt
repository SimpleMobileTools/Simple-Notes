package com.simplemobiletools.notes

import android.app.Application
import com.facebook.stetho.Stetho

class AppDebug : Application() {
    override fun onCreate() {
        super.onCreate()
        Stetho.initializeWithDefaults(this)
    }
}