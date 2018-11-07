package com.simplemobiletools.notes.pro.fragments

import android.annotation.SuppressLint
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.Selection
import android.text.TextWatcher
import android.text.style.UnderlineSpan
import android.text.util.Linkify
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.annotation.RequiresApi
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.notes.pro.R
import com.simplemobiletools.notes.pro.activities.MainActivity
import com.simplemobiletools.notes.pro.extensions.config
import com.simplemobiletools.notes.pro.extensions.dbHelper
import com.simplemobiletools.notes.pro.extensions.getTextSize
import com.simplemobiletools.notes.pro.extensions.updateWidgets
import com.simplemobiletools.notes.pro.helpers.*
import com.simplemobiletools.notes.pro.models.Note
import com.simplemobiletools.notes.pro.models.TextHistory
import com.simplemobiletools.notes.pro.models.TextHistoryItem
import kotlinx.android.synthetic.main.fragment_note.*
import kotlinx.android.synthetic.main.fragment_note.view.*
import kotlinx.android.synthetic.main.note_view_horiz_scrollable.view.*
import java.io.File

// text history handling taken from https://gist.github.com/zeleven/0cfa738c1e8b65b23ff7df1fc30c9f7e
class NoteFragment : androidx.fragment.app.Fragment() {
    private val TEXT = "text"

    private var textHistory = TextHistory()
    private var isUndoOrRedo = false
    private var skipTextUpdating = false
    private var noteId = 0
    private var note: Note? = null

    lateinit var view: ViewGroup
    private lateinit var db: DBHelper

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        view = inflater.inflate(R.layout.fragment_note, container, false) as ViewGroup
        noteId = arguments!!.getInt(NOTE_ID)
        db = context!!.dbHelper
        retainInstance = true

        val layoutToInflate = if (config!!.enableLineWrap) R.layout.note_view_static else R.layout.note_view_horiz_scrollable
        inflater.inflate(layoutToInflate, view.notes_relative_layout, true)
        if (config!!.clickableLinks) {
            view.notes_view.apply {
                linksClickable = true
                autoLinkMask = Linkify.WEB_URLS or Linkify.EMAIL_ADDRESSES
                movementMethod = MyMovementMethod.getInstance()
            }
        }

        view.notes_horizontal_scrollview?.onGlobalLayout {
            view.notes_view.minWidth = view.notes_horizontal_scrollview.width
        }

        return view
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onResume() {
        super.onResume()

        NotesHelper(activity!!).getNoteWithId(noteId) {
            if (it != null) {
                note = it
                setupFragment()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (config!!.autosaveNotes) {
            saveText(false)
        }
        view.notes_view.removeTextChangedListener(textWatcher)
    }

    override fun setMenuVisibility(menuVisible: Boolean) {
        super.setMenuVisibility(menuVisible)
        if (!menuVisible && noteId != 0 && config?.autosaveNotes == true) {
            saveText(false)
        }

        if (menuVisible && noteId != 0) {
            val currentText = getCurrentNoteViewText()
            if (currentText != null) {
                (activity as MainActivity).currentNoteTextChanged(currentText, isUndoAvailable(), isRedoAvailable())
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(TEXT, getCurrentNoteViewText())
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        if (savedInstanceState != null) {
            skipTextUpdating = true
            val newText = savedInstanceState.getString(TEXT) ?: ""
            view.notes_view.setText(newText)
        }
    }

    private fun setupFragment() {
        val config = config!!
        view.notes_view.apply {
            typeface = if (config.monospacedFont) Typeface.MONOSPACE else Typeface.DEFAULT

            val fileContents = note!!.getNoteStoredValue()
            if (fileContents == null) {
                (activity as MainActivity).deleteNote(false)
                return
            }

            setColors(config.textColor, config.primaryColor, config.backgroundColor)
            setTextSize(TypedValue.COMPLEX_UNIT_PX, context.getTextSize())
            gravity = getTextGravity()
            if (text.toString() != fileContents) {
                if (!skipTextUpdating) {
                    setText(fileContents)
                }
                skipTextUpdating = false
                setSelection(if (config.placeCursorToEnd) text.length else 0)
            }

            if (config.showKeyboard) {
                requestFocus()
            }

            imeOptions = if (config.useIncognitoMode) {
                imeOptions or EditorInfo.IME_FLAG_NO_PERSONALIZED_LEARNING
            } else {
                imeOptions.removeBit(EditorInfo.IME_FLAG_NO_PERSONALIZED_LEARNING)
            }
        }

        if (config.showWordCount) {
            view.notes_counter.beVisible()
            view.notes_counter.setTextColor(config.textColor)
            setWordCounter(view.notes_view.text.toString())
        } else {
            view.notes_counter.beGone()
        }

        view.notes_view.addTextChangedListener(textWatcher)
    }

    fun getNotesView() = view.notes_view

    fun saveText(force: Boolean) {
        if (note!!.path.isNotEmpty() && !File(note!!.path).exists()) {
            return
        }

        if (context == null || activity == null) {
            return
        }

        val newText = getCurrentNoteViewText()
        val oldText = note!!.getNoteStoredValue()
        if (newText != null && (newText != oldText || force)) {
            note!!.value = newText
            saveNoteValue(note!!)
            context!!.updateWidgets()
        }
    }

    fun hasUnsavedChanges() = getCurrentNoteViewText() != note!!.getNoteStoredValue()

    fun focusEditText() {
        view.notes_view.requestFocus()
    }

    private fun saveNoteValue(note: Note) {
        if (note.path.isEmpty()) {
            db.updateNote(note)
            (activity as MainActivity).noteSavedSuccessfully(note.title)
        } else {
            val currentText = getCurrentNoteViewText()
            if (currentText != null) {
                (activity as MainActivity).exportNoteValueToFile(note.path, currentText, true)
            }
        }
    }

    fun getCurrentNoteViewText() = view.notes_view?.text?.toString()

    @SuppressLint("RtlHardcoded")
    private fun getTextGravity() = when (config!!.gravity) {
        GRAVITY_CENTER -> Gravity.CENTER_HORIZONTAL
        GRAVITY_RIGHT -> Gravity.RIGHT
        else -> Gravity.LEFT
    }

    private fun setWordCounter(text: String) {
        val words = text.replace("\n", " ").split(" ")
        notes_counter.text = words.count { it.isNotEmpty() }.toString()
    }

    fun undo() {
        val edit = textHistory.getPrevious() ?: return

        val text = view.notes_view.editableText
        val start = edit.start
        val end = start + if (edit.after != null) edit.after.length else 0

        isUndoOrRedo = true
        try {
            text.replace(start, end, edit.before)
        } catch (e: Exception) {
            activity?.showErrorToast(e)
            return
        }

        isUndoOrRedo = false

        for (span in text.getSpans(0, text.length, UnderlineSpan::class.java)) {
            text.removeSpan(span)
        }

        Selection.setSelection(text, if (edit.before == null) {
            start
        } else {
            start + edit.before.length
        })
    }

    fun redo() {
        val edit = textHistory.getNext() ?: return

        val text = view.notes_view.editableText
        val start = edit.start
        val end = start + if (edit.before != null) edit.before.length else 0

        isUndoOrRedo = true
        text.replace(start, end, edit.after)
        isUndoOrRedo = false

        for (o in text.getSpans(0, text.length, UnderlineSpan::class.java)) {
            text.removeSpan(o)
        }

        Selection.setSelection(text, if (edit.after == null) {
            start
        } else {
            start + edit.after.length
        })
    }

    fun isUndoAvailable() = textHistory.position > 0

    fun isRedoAvailable() = textHistory.position < textHistory.history.size

    private var textWatcher: TextWatcher = object : TextWatcher {
        private var beforeChange: CharSequence? = null
        private var afterChange: CharSequence? = null

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            if (!isUndoOrRedo) {
                beforeChange = s.subSequence(start, start + count)
            }
        }

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            if (!isUndoOrRedo) {
                afterChange = s.subSequence(start, start + count)
                textHistory.add(TextHistoryItem(start, beforeChange!!, afterChange!!))
            }
        }

        override fun afterTextChanged(editable: Editable) {
            val text = editable.toString()
            setWordCounter(text)
            (activity as MainActivity).currentNoteTextChanged(text, isUndoAvailable(), isRedoAvailable())
        }
    }
}
