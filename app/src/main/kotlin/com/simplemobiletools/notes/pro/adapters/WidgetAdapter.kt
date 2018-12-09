package com.simplemobiletools.notes.pro.adapters

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.view.View
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.simplemobiletools.commons.extensions.adjustAlpha
import com.simplemobiletools.commons.extensions.setText
import com.simplemobiletools.commons.extensions.setTextSize
import com.simplemobiletools.notes.pro.R
import com.simplemobiletools.notes.pro.R.id.checklist_title
import com.simplemobiletools.notes.pro.R.id.widget_text_holder
import com.simplemobiletools.notes.pro.extensions.config
import com.simplemobiletools.notes.pro.extensions.getTextSize
import com.simplemobiletools.notes.pro.extensions.notesDB
import com.simplemobiletools.notes.pro.helpers.*
import com.simplemobiletools.notes.pro.models.ChecklistItem
import com.simplemobiletools.notes.pro.models.Note

class WidgetAdapter(val context: Context, val intent: Intent) : RemoteViewsService.RemoteViewsFactory {
    private val textIds = arrayOf(R.id.widget_text_left, R.id.widget_text_center, R.id.widget_text_right)
    private var widgetTextColor = context.config.widgetTextColor
    private var note: Note? = null
    private var checklistItems = ArrayList<ChecklistItem>()

    override fun getViewAt(position: Int): RemoteViews {
        val noteId = intent.getLongExtra(NOTE_ID, 0L)
        val remoteView: RemoteViews

        if (note == null) {
            return RemoteViews(context.packageName, R.layout.widget_text_layout)
        }

        val textSize = context.getTextSize() / context.resources.displayMetrics.density
        if (note!!.type == TYPE_CHECKLIST) {
            remoteView = RemoteViews(context.packageName, R.layout.item_checklist_widget).apply {
                val checklistItem = checklistItems.getOrNull(position) ?: return@apply
                setText(R.id.checklist_title, checklistItem.title)

                val widgetNewTextColor = if (checklistItem.isDone) widgetTextColor.adjustAlpha(DONE_CHECKLIST_ITEM_ALPHA) else widgetTextColor
                setTextColor(R.id.checklist_title, widgetNewTextColor)
                setTextSize(R.id.checklist_title, textSize)

                val paintFlags = if (checklistItem.isDone) Paint.STRIKE_THRU_TEXT_FLAG or Paint.ANTI_ALIAS_FLAG else 0
                setInt(R.id.checklist_title, "setPaintFlags", paintFlags)

                Intent().apply {
                    putExtra(OPEN_NOTE_ID, noteId)
                    setOnClickFillInIntent(checklist_title, this)
                }
            }
        } else {
            remoteView = RemoteViews(context.packageName, R.layout.widget_text_layout).apply {
                val noteText = note!!.getNoteStoredValue() ?: ""
                for (id in textIds) {
                    setText(id, noteText)
                    setTextColor(id, widgetTextColor)
                    setTextSize(id, textSize)
                    setViewVisibility(id, View.GONE)
                }
                setViewVisibility(getProperTextView(context), View.VISIBLE)

                Intent().apply {
                    putExtra(OPEN_NOTE_ID, noteId)
                    setOnClickFillInIntent(widget_text_holder, this)
                }
            }
        }

        return remoteView
    }

    private fun getProperTextView(context: Context) = when (context.config.gravity) {
        GRAVITY_CENTER -> R.id.widget_text_center
        GRAVITY_RIGHT -> R.id.widget_text_right
        else -> R.id.widget_text_left
    }

    override fun onCreate() {}

    override fun getLoadingView() = null

    override fun getItemId(position: Int) = position.toLong()

    override fun onDataSetChanged() {
        widgetTextColor = context.config.widgetTextColor
        val noteId = intent.getLongExtra(NOTE_ID, 0L)
        note = context.notesDB.getNoteWithId(noteId)
        if (note?.type == TYPE_CHECKLIST) {
            val checklistItemType = object : TypeToken<List<ChecklistItem>>() {}.type
            checklistItems = Gson().fromJson<ArrayList<ChecklistItem>>(note!!.value, checklistItemType) ?: ArrayList(1)
        }
    }

    override fun hasStableIds() = true

    override fun getCount(): Int {
        return if (note?.type == TYPE_CHECKLIST) {
            checklistItems.size
        } else {
            1
        }
    }

    override fun getViewTypeCount() = 2

    override fun onDestroy() {}
}
