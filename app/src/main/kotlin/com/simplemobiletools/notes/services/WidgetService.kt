package com.simplemobiletools.notes.services

import android.content.Intent
import android.widget.RemoteViewsService
import com.simplemobiletools.notes.adapters.WidgetAdapter

class WidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent) = WidgetAdapter(applicationContext, intent)
}
