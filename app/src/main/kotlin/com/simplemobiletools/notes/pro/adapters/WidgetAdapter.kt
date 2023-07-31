package com.simplemobiletools.notes.pro.adapters

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.view.View
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.simplemobiletools.commons.extensions.adjustAlpha
import com.simplemobiletools.commons.extensions.setText
import com.simplemobiletools.commons.extensions.setTextSize
import com.simplemobiletools.commons.helpers.SORT_BY_CUSTOM
import com.simplemobiletools.commons.helpers.WIDGET_TEXT_COLOR
import com.simplemobiletools.notes.pro.R
import com.simplemobiletools.notes.pro.R.id.widget_text_holder
import com.simplemobiletools.notes.pro.extensions.config
import com.simplemobiletools.notes.pro.extensions.getPercentageFontSize
import com.simplemobiletools.notes.pro.extensions.notesDB
import com.simplemobiletools.notes.pro.helpers.*
import com.simplemobiletools.notes.pro.models.ChecklistItem
import com.simplemobiletools.notes.pro.models.Note
import com.simplemobiletools.notes.pro.models.NoteType
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class WidgetAdapter(val context: Context, val intent: Intent) : RemoteViewsService.RemoteViewsFactory {
    private val textIds = arrayOf(
        R.id.widget_text_left, R.id.widget_text_center, R.id.widget_text_right,
        R.id.widget_text_left_monospace, R.id.widget_text_center_monospace, R.id.widget_text_right_monospace
    )
    private val checklistIds = arrayOf(
        R.id.checklist_text_left, R.id.checklist_text_center, R.id.checklist_text_right,
        R.id.checklist_text_left_monospace, R.id.checklist_text_center_monospace, R.id.checklist_text_right_monospace
    )
    private var widgetTextColor = DEFAULT_WIDGET_TEXT_COLOR
    private var note: Note? = null
    private var checklistItems = mutableListOf<ChecklistItem>()

    override fun getViewAt(position: Int): RemoteViews {
        val noteId = intent.getLongExtra(NOTE_ID, 0L)
        val remoteView: RemoteViews

        if (note == null) {
            return RemoteViews(context.packageName, R.layout.widget_text_layout)
        }

        val textSize = context.getPercentageFontSize() / context.resources.displayMetrics.density
        if (note!!.type == NoteType.TYPE_CHECKLIST) {
            remoteView = RemoteViews(context.packageName, R.layout.item_checklist_widget).apply {
                val checklistItem = checklistItems.getOrNull(position) ?: return@apply
                val widgetNewTextColor = if (checklistItem.isDone) widgetTextColor.adjustAlpha(DONE_CHECKLIST_ITEM_ALPHA) else widgetTextColor
                val paintFlags = if (checklistItem.isDone) Paint.STRIKE_THRU_TEXT_FLAG or Paint.ANTI_ALIAS_FLAG else Paint.ANTI_ALIAS_FLAG

                for (id in checklistIds) {
                    setText(id, checklistItem.title)
                    setTextColor(id, widgetNewTextColor)
                    setTextSize(id, textSize)
                    setInt(id, "setPaintFlags", paintFlags)
                    setViewVisibility(id, View.GONE)
                }

                setViewVisibility(getProperChecklistTextView(context), View.VISIBLE)

                Intent().apply {
                    putExtra(OPEN_NOTE_ID, noteId)
                    setOnClickFillInIntent(R.id.checklist_text_holder, this)
                }
            }
        } else {
            remoteView = RemoteViews(context.packageName, R.layout.widget_text_layout).apply {
                val noteText = note!!.getNoteStoredValue(context) ?: ""
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

    private fun getProperTextView(context: Context): Int {
        val gravity = context.config.gravity
        val isMonospaced = context.config.monospacedFont

        return when {
            gravity == GRAVITY_CENTER && isMonospaced -> R.id.widget_text_center_monospace
            gravity == GRAVITY_CENTER -> R.id.widget_text_center
            gravity == GRAVITY_END && isMonospaced -> R.id.widget_text_right_monospace
            gravity == GRAVITY_END -> R.id.widget_text_right
            isMonospaced -> R.id.widget_text_left_monospace
            else -> R.id.widget_text_left
        }
    }

    private fun getProperChecklistTextView(context: Context): Int {
        val gravity = context.config.gravity
        val isMonospaced = context.config.monospacedFont

        return when {
            gravity == GRAVITY_CENTER && isMonospaced -> R.id.checklist_text_center_monospace
            gravity == GRAVITY_CENTER -> R.id.checklist_text_center
            gravity == GRAVITY_END && isMonospaced -> R.id.checklist_text_right_monospace
            gravity == GRAVITY_END -> R.id.checklist_text_right
            isMonospaced -> R.id.checklist_text_left_monospace
            else -> R.id.checklist_text_left
        }
    }

    override fun onCreate() {}

    override fun getLoadingView() = null

    override fun getItemId(position: Int) = position.toLong()

    override fun onDataSetChanged() {
        widgetTextColor = intent.getIntExtra(WIDGET_TEXT_COLOR, DEFAULT_WIDGET_TEXT_COLOR)
        val noteId = intent.getLongExtra(NOTE_ID, 0L)
        note = context.notesDB.getNoteWithId(noteId)
        if (note?.type == NoteType.TYPE_CHECKLIST) {
            checklistItems = note!!.getNoteStoredValue(context)?.ifEmpty { "[]" }?.let { Json.decodeFromString(it) } ?: mutableListOf()

            // checklist title can be null only because of the glitch in upgrade to 6.6.0, remove this check in the future
            checklistItems = checklistItems.filter { it.title != null }.toMutableList() as ArrayList<ChecklistItem>
            val sorting = context.config?.sorting ?: 0
            if (sorting and SORT_BY_CUSTOM == 0) {
                checklistItems.sort()
                if (context?.config?.moveDoneChecklistItems == true) {
                    checklistItems.sortBy { it.isDone }
                }
            }
        }
    }

    override fun hasStableIds() = true

    override fun getCount(): Int {
        return if (note?.type == NoteType.TYPE_CHECKLIST) {
            checklistItems.size
        } else {
            1
        }
    }

    override fun getViewTypeCount() = 2

    override fun onDestroy() {}
}
