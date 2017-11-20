package com.simplemobiletools.notes.fragments

import android.graphics.Typeface
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.Editable
import android.text.TextWatcher
import android.text.method.LinkMovementMethod
import android.text.util.Linkify
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.simplemobiletools.notes.R
import com.simplemobiletools.notes.activities.MainActivity
import com.simplemobiletools.notes.extensions.*
import com.simplemobiletools.notes.helpers.DBHelper
import com.simplemobiletools.notes.helpers.GRAVITY_CENTER
import com.simplemobiletools.notes.helpers.GRAVITY_RIGHT
import com.simplemobiletools.notes.helpers.NOTE_ID
import com.simplemobiletools.notes.models.Note
import kotlinx.android.synthetic.main.fragment_note.view.*
import java.io.File

class NoteFragment : Fragment() {
    private var noteId = 0
    lateinit var note: Note
    lateinit var view: ViewGroup
    lateinit var mDb: DBHelper

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        view = inflater.inflate(R.layout.fragment_note, container, false) as ViewGroup
        noteId = arguments!!.getInt(NOTE_ID)
        mDb = context!!.dbHelper
        note = mDb.getNote(noteId) ?: return view
        retainInstance = true

        if (context!!.config.clickableLinks) {
            view.notes_view.apply {
                linksClickable = true
                autoLinkMask = Linkify.WEB_URLS or Linkify.EMAIL_ADDRESSES
                movementMethod = LinkMovementMethod.getInstance()
            }
        }

        return view
    }

    override fun setMenuVisibility(menuVisible: Boolean) {
        super.setMenuVisibility(menuVisible)
        if (noteId != 0) {
            saveText()
        }
    }

    fun getNotesView() = view.notes_view

    fun saveText() {
        if (note.path.isNotEmpty() && !File(note.path).exists())
            return

        if (context == null || activity == null)
            return

        val newText = getCurrentNoteViewText()
        val oldText = context!!.getNoteStoredValue(note)
        if (newText != oldText) {
            note.value = newText
            saveNoteValue(note)
            context!!.updateWidget()
        }
    }

    fun focusEditText() {
        view.notes_view.requestFocus()
    }

    private fun saveNoteValue(note: Note) {
        if (note.path.isEmpty()) {
            mDb.updateNoteValue(note)
            (activity as MainActivity).noteSavedSuccessfully(note.title)
        } else {
            (activity as MainActivity).exportNoteValueToFile(note.path, getCurrentNoteViewText())
        }
    }

    fun getCurrentNoteViewText() = view.notes_view?.text.toString()

    private fun getTextGravity() = when (context!!.config.gravity) {
        GRAVITY_CENTER -> Gravity.CENTER_HORIZONTAL
        GRAVITY_RIGHT -> Gravity.RIGHT
        else -> Gravity.LEFT
    }

    override fun onResume() {
        super.onResume()

        val config = context!!.config

        view.notes_view.apply {
            typeface = if (config.monospacedFont) Typeface.MONOSPACE else Typeface.DEFAULT

            val fileContents = context.getNoteStoredValue(note)

            if (fileContents == null) {
                (activity as MainActivity).deleteNote(false)
                return
            }

            setColors(config.textColor, config.primaryColor, config.backgroundColor)
            setTextSize(TypedValue.COMPLEX_UNIT_PX, context.getTextSize())
            gravity = getTextGravity()
            if (text.toString() != fileContents) {
                setText(fileContents)
                setSelection(if (config.placeCursorToEnd) text.length else 0)
            }
        }

        if (config.showWordCount) {
            view.notes_view.addTextChangedListener(textWatcher)
            view.notes_counter.visibility = View.VISIBLE
            setWordCounter(view.notes_view.text)
        }
        else {
            view.notes_counter.visibility = View.GONE
        }
    }

    override fun onPause() {
        super.onPause()
        saveText()

        removeTextWatcher()
    }

    private fun removeTextWatcher() {
        view.notes_view.removeTextChangedListener(textWatcher)
    }

    private fun setWordCounter(text: Editable) {
        val wordArray = text.toString().replace("\n", " ").split(" ")
        notes_counter.text = wordArray.count { it.isNotEmpty() }.toString()
    }

    private var textWatcher: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        }

        override fun afterTextChanged(editable: Editable) {
            setWordCounter(editable)
        }
    }
}
