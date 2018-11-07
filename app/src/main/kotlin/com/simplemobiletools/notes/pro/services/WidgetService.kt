package com.simplemobiletools.notes.pro.services

import android.content.Intent
import android.widget.RemoteViewsService
import com.simplemobiletools.notes.pro.adapters.WidgetAdapter

class WidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent) = WidgetAdapter(applicationContext, intent)
}
