package com.simplemobiletools.notes.pro.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.simplemobiletools.notes.pro.R
import com.simplemobiletools.notes.pro.helpers.NOTE_ID
import com.simplemobiletools.notes.pro.helpers.NotesHelper
import com.simplemobiletools.notes.pro.models.Note

class ChecklistFragment : NoteFragment() {
    private var noteId = 0L
    private var note: Note? = null

    lateinit var view: ViewGroup

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        view = inflater.inflate(R.layout.fragment_checklist, container, false) as ViewGroup
        noteId = arguments!!.getLong(NOTE_ID)
        return view
    }

    override fun onResume() {
        super.onResume()

        NotesHelper(activity!!).getNoteWithId(noteId) {
            if (it != null) {
                note = it
                setupFragment()
            }
        }
    }

    private fun setupFragment() {

    }
}
