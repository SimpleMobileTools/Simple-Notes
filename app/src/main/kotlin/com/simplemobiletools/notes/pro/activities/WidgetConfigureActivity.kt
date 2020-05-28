package com.simplemobiletools.notes.pro.activities

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.TypedValue
import android.view.Menu
import android.widget.RemoteViews
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.simplemobiletools.commons.dialogs.ColorPickerDialog
import com.simplemobiletools.commons.dialogs.RadioGroupDialog
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.helpers.IS_CUSTOMIZING_COLORS
import com.simplemobiletools.commons.helpers.ensureBackgroundThread
import com.simplemobiletools.commons.models.RadioItem
import com.simplemobiletools.notes.pro.R
import com.simplemobiletools.notes.pro.adapters.ChecklistAdapter
import com.simplemobiletools.notes.pro.extensions.config
import com.simplemobiletools.notes.pro.extensions.getPercentageFontSize
import com.simplemobiletools.notes.pro.extensions.widgetsDB
import com.simplemobiletools.notes.pro.helpers.*
import com.simplemobiletools.notes.pro.models.ChecklistItem
import com.simplemobiletools.notes.pro.models.Note
import com.simplemobiletools.notes.pro.models.Widget
import kotlinx.android.synthetic.main.widget_config.*

class WidgetConfigureActivity : SimpleActivity() {
    private var mBgAlpha = 0f
    private var mWidgetId = 0
    private var mBgColor = 0
    private var mBgColorWithoutTransparency = 0
    private var mTextColor = 0
    private var mCurrentNoteId = 0L
    private var mIsCustomizingColors = false
    private var mNotes = ArrayList<Note>()

    public override fun onCreate(savedInstanceState: Bundle?) {
        useDynamicTheme = false
        super.onCreate(savedInstanceState)
        setResult(RESULT_CANCELED)
        setContentView(R.layout.widget_config)
        initVariables()

        mWidgetId = intent.extras?.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        if (mWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID && !mIsCustomizingColors) {
            finish()
        }

        updateTextColors(notes_picker_holder)
        config_save.setOnClickListener { saveConfig() }
        config_bg_color.setOnClickListener { pickBackgroundColor() }
        config_text_color.setOnClickListener { pickTextColor() }
        notes_picker_value.setOnClickListener { showNoteSelector() }
        notes_picker_holder.background = ColorDrawable(config.backgroundColor)
    }

    override fun onResume() {
        super.onResume()
        text_note_view.setTextSize(TypedValue.COMPLEX_UNIT_PX, getPercentageFontSize())
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        updateMenuItemColors(menu)
        return super.onCreateOptionsMenu(menu)
    }

    private fun initVariables() {
        val extras = intent.extras
        if (extras?.getLong(CUSTOMIZED_WIDGET_ID, 0L) == 0L) {
            mBgColor = config.widgetBgColor
            mTextColor = config.widgetTextColor
        } else {
            mBgColor = extras?.getInt(CUSTOMIZED_WIDGET_BG_COLOR) ?: config.widgetBgColor
            mTextColor = extras?.getInt(CUSTOMIZED_WIDGET_TEXT_COLOR) ?: config.widgetTextColor
        }

        mBgAlpha = Color.alpha(mBgColor) / 255.toFloat()

        mBgColorWithoutTransparency = Color.rgb(Color.red(mBgColor), Color.green(mBgColor), Color.blue(mBgColor))
        config_bg_seekbar.apply {
            progress = (mBgAlpha * 100).toInt()

            onSeekBarChangeListener {
                mBgAlpha = it / 100f
                updateBackgroundColor()
            }
        }
        updateBackgroundColor()

        updateTextColor()
        mIsCustomizingColors = extras?.getBoolean(IS_CUSTOMIZING_COLORS) ?: false
        notes_picker_holder.beVisibleIf(!mIsCustomizingColors)

        NotesHelper(this).getNotes {
            mNotes = it
            notes_picker_holder.beVisibleIf(mNotes.size > 1 && !mIsCustomizingColors)
            updateCurrentNote(mNotes.first())
        }
    }

    private fun showNoteSelector() {
        val items = ArrayList<RadioItem>()
        mNotes.forEach {
            items.add(RadioItem(it.id!!.toInt(), it.title))
        }

        RadioGroupDialog(this, items, mCurrentNoteId.toInt()) {
            val selectedId = it as Int
            updateCurrentNote(mNotes.first { it.id!!.toInt() == selectedId })
        }
    }

    private fun updateCurrentNote(note: Note) {
        mCurrentNoteId = note.id!!
        notes_picker_value.text = note.title
        if (note.type == NoteType.TYPE_CHECKLIST.value) {
            val checklistItemType = object : TypeToken<List<ChecklistItem>>() {}.type
            val items = Gson().fromJson<ArrayList<ChecklistItem>>(note.value, checklistItemType) ?: ArrayList(1)
            items.apply {
                if (isEmpty()) {
                    add(ChecklistItem(0, "Milk", true))
                    add(ChecklistItem(1, "Butter", true))
                    add(ChecklistItem(2, "Salt", false))
                    add(ChecklistItem(3, "Water", false))
                    add(ChecklistItem(4, "Meat", true))
                }
            }

            ChecklistAdapter(this, items, null, checklist_note_view, false) {}.apply {
                updateTextColor(mTextColor)
                checklist_note_view.adapter = this
            }
            text_note_view.beGone()
            checklist_note_view.beVisible()
        } else {
            val sampleValue = if (note.value.isEmpty() || mIsCustomizingColors) getString(R.string.widget_config) else note.value
            text_note_view.text = sampleValue
            text_note_view.beVisible()
            checklist_note_view.beGone()
        }
    }

    private fun saveConfig() {
        val views = RemoteViews(packageName, R.layout.activity_main)
        views.setBackgroundColor(R.id.text_note_view, mBgColor)
        views.setBackgroundColor(R.id.checklist_note_view, mBgColor)
        AppWidgetManager.getInstance(this).updateAppWidget(mWidgetId, views)

        val extras = intent.extras
        val id = if (extras?.containsKey(CUSTOMIZED_WIDGET_KEY_ID) == true) extras.getLong(CUSTOMIZED_WIDGET_KEY_ID) else null
        mWidgetId = extras?.getInt(CUSTOMIZED_WIDGET_ID, mWidgetId) ?: mWidgetId
        mCurrentNoteId = extras?.getLong(CUSTOMIZED_WIDGET_NOTE_ID, mCurrentNoteId) ?: mCurrentNoteId
        val widget = Widget(id, mWidgetId, mCurrentNoteId, mBgColor, mTextColor)
        ensureBackgroundThread {
            widgetsDB.insertOrUpdate(widget)
        }

        storeWidgetBackground()
        requestWidgetUpdate()

        Intent().apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mWidgetId)
            setResult(Activity.RESULT_OK, this)
        }
        finish()
    }

    private fun storeWidgetBackground() {
        config.apply {
            widgetBgColor = mBgColor
            widgetTextColor = mTextColor
        }
    }

    private fun requestWidgetUpdate() {
        Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE, null, this, MyWidgetProvider::class.java).apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, intArrayOf(mWidgetId))
            sendBroadcast(this)
        }
    }

    private fun updateBackgroundColor() {
        mBgColor = mBgColorWithoutTransparency.adjustAlpha(mBgAlpha)
        text_note_view.setBackgroundColor(mBgColor)
        checklist_note_view.setBackgroundColor(mBgColor)
        config_save.setBackgroundColor(mBgColor)
        config_bg_color.setFillWithStroke(mBgColor, Color.BLACK)
    }

    private fun updateTextColor() {
        config_save.setTextColor(mTextColor)
        text_note_view.setTextColor(mTextColor)
        (checklist_note_view.adapter as? ChecklistAdapter)?.updateTextColor(mTextColor)
        config_text_color.setFillWithStroke(mTextColor, Color.BLACK)
    }

    private fun pickBackgroundColor() {
        ColorPickerDialog(this, mBgColorWithoutTransparency) { wasPositivePressed, color ->
            if (wasPositivePressed) {
                mBgColorWithoutTransparency = color
                updateBackgroundColor()
            }
        }
    }

    private fun pickTextColor() {
        ColorPickerDialog(this, mTextColor) { wasPositivePressed, color ->
            if (wasPositivePressed) {
                mTextColor = color
                updateTextColor()
            }
        }
    }
}
