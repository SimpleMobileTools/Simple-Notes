package com.simplemobiletools.notes.pro.activities

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.TypedValue
import android.widget.RemoteViews
import com.simplemobiletools.commons.dialogs.ColorPickerDialog
import com.simplemobiletools.commons.dialogs.RadioGroupDialog
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.helpers.IS_CUSTOMIZING_COLORS
import com.simplemobiletools.commons.models.RadioItem
import com.simplemobiletools.notes.pro.R
import com.simplemobiletools.notes.pro.extensions.config
import com.simplemobiletools.notes.pro.extensions.dbHelper
import com.simplemobiletools.notes.pro.extensions.getTextSize
import com.simplemobiletools.notes.pro.helpers.MyWidgetProvider
import com.simplemobiletools.notes.pro.models.Note
import com.simplemobiletools.notes.pro.models.Widget
import kotlinx.android.synthetic.main.widget_config.*

class WidgetConfigureActivity : SimpleActivity() {
    private var mBgAlpha = 0f
    private var mWidgetId = 0
    private var mBgColor = 0
    private var mBgColorWithoutTransparency = 0
    private var mTextColor = 0
    private var mCurrentNoteId = 0
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
        notes_view.setTextSize(TypedValue.COMPLEX_UNIT_PX, applicationContext.getTextSize())
    }

    private fun initVariables() {
        mBgColor = config.widgetBgColor
        if (mBgColor == 1) {
            mBgColor = Color.BLACK
            mBgAlpha = .2f
        } else {
            mBgAlpha = Color.alpha(mBgColor) / 255f
        }

        mBgColorWithoutTransparency = Color.rgb(Color.red(mBgColor), Color.green(mBgColor), Color.blue(mBgColor))
        config_bg_seekbar.apply {
            progress = (mBgAlpha * 100).toInt()

            onSeekBarChangeListener {
                mBgAlpha = it / 100f
                updateBackgroundColor()
            }
        }
        updateBackgroundColor()

        mTextColor = config.widgetTextColor
        updateTextColor()
        mNotes = dbHelper.getNotes()
        mIsCustomizingColors = intent.extras?.getBoolean(IS_CUSTOMIZING_COLORS) ?: false
        notes_picker_holder.beVisibleIf(mNotes.size > 1 && !mIsCustomizingColors)
        updateCurrentNote(mNotes.first())
    }

    private fun showNoteSelector() {
        val items = ArrayList<RadioItem>()
        mNotes.forEach {
            items.add(RadioItem(it.id!!, it.title))
        }

        RadioGroupDialog(this, items, mCurrentNoteId) {
            val selectedId = it as Int
            updateCurrentNote(mNotes.first { it.id == selectedId })
        }
    }

    private fun updateCurrentNote(note: Note) {
        mCurrentNoteId = note.id!!
        notes_picker_value.text = note.title
        val sampleValue = if (note.value.isEmpty() || mIsCustomizingColors) getString(R.string.widget_config) else note.value
        notes_view.text = sampleValue
    }

    private fun saveConfig() {
        val views = RemoteViews(packageName, R.layout.activity_main)
        views.setBackgroundColor(R.id.notes_view, mBgColor)
        AppWidgetManager.getInstance(this).updateAppWidget(mWidgetId, views)
        val widget = Widget(null, mWidgetId, mCurrentNoteId)
        dbHelper.insertWidget(widget)

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
        notes_view.setBackgroundColor(mBgColor)
        config_save.setBackgroundColor(mBgColor)
        config_bg_color.setFillWithStroke(mBgColor, Color.BLACK)
    }

    private fun updateTextColor() {
        config_save.setTextColor(mTextColor)
        notes_view.setTextColor(mTextColor)
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
