package com.simplemobiletools.notes

import android.app.Application
import com.facebook.stetho.Stetho
import com.simplemobiletools.commons.extensions.checkUseEnglish

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Stetho.initializeWithDefaults(this)
        }

        checkUseEnglish()
    }
}
