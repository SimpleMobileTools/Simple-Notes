package com.simplemobiletools.notes.pro.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.simplemobiletools.commons.extensions.applyColorFilter
import com.simplemobiletools.commons.extensions.getAdjustedPrimaryColor
import com.simplemobiletools.commons.extensions.getColoredDrawableWithColor
import com.simplemobiletools.commons.extensions.isBlackAndWhiteTheme
import com.simplemobiletools.notes.pro.R
import com.simplemobiletools.notes.pro.activities.SimpleActivity
import com.simplemobiletools.notes.pro.dialogs.NewChecklistItemDialog
import com.simplemobiletools.notes.pro.helpers.NOTE_ID
import com.simplemobiletools.notes.pro.helpers.NotesHelper
import com.simplemobiletools.notes.pro.models.ChecklistItem
import com.simplemobiletools.notes.pro.models.Note
import kotlinx.android.synthetic.main.fragment_checklist.view.*

class ChecklistFragment : NoteFragment() {
    private var noteId = 0L
    private var note: Note? = null
    private var items = ArrayList<ChecklistItem>()

    lateinit var view: ViewGroup

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        view = inflater.inflate(R.layout.fragment_checklist, container, false) as ViewGroup
        noteId = arguments!!.getLong(NOTE_ID)
        return view
    }

    override fun onResume() {
        super.onResume()

        NotesHelper(activity!!).getNoteWithId(noteId) {
            if (it != null && activity?.isDestroyed == false) {
                note = it
                setupFragment()
            }
        }
    }

    private fun setupFragment() {
        val plusIcon = resources.getColoredDrawableWithColor(R.drawable.ic_plus, if (context!!.isBlackAndWhiteTheme()) Color.BLACK else Color.WHITE)
        view.checklist_fab.apply {
            setImageDrawable(plusIcon)
            background.applyColorFilter(context!!.getAdjustedPrimaryColor())
            setOnClickListener {
                NewChecklistItemDialog(activity as SimpleActivity) {
                    val checklistItem = ChecklistItem(it, false)
                    items.add(checklistItem)
                }
            }
        }
    }
}
