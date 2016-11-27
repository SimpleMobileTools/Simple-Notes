package com.simplemobiletools.notes.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.simplemobiletools.filepicker.extensions.value
import com.simplemobiletools.notes.NOTE_ID
import com.simplemobiletools.notes.R
import com.simplemobiletools.notes.databases.DBHelper
import com.simplemobiletools.notes.extensions.getTextGravity
import com.simplemobiletools.notes.extensions.getTextSize
import com.simplemobiletools.notes.extensions.updateWidget
import com.simplemobiletools.notes.models.Note
import kotlinx.android.synthetic.main.fragment_note.view.*

class NoteFragment : Fragment() {
    var noteId = 0
    lateinit var view: ViewGroup
    lateinit var note: Note
    lateinit var mDb: DBHelper

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        view = inflater.inflate(R.layout.fragment_note, container, false) as ViewGroup
        noteId = arguments.getInt(NOTE_ID)
        mDb = DBHelper.newInstance(context)
        note = mDb.getNote(noteId) ?: return view

        view.notes_view.setText(note.value)
        return view
    }

    fun saveText() {
        val newText = view.notes_view.value
        val oldText = note.value
        if (newText != oldText) {
            note.value = newText
            mDb.updateNote(note)
            context.updateWidget()
        }
    }

    override fun onResume() {
        super.onResume()
        view.notes_view.setTextSize(TypedValue.COMPLEX_UNIT_PX, context.getTextSize())
        view.notes_view.gravity = context.getTextGravity()
    }

    override fun onPause() {
        super.onPause()
        saveText()
    }
}
