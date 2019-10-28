package com.simplemobiletools.notes.pro.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.helpers.ensureBackgroundThread
import com.simplemobiletools.notes.pro.R
import com.simplemobiletools.notes.pro.activities.SimpleActivity
import com.simplemobiletools.notes.pro.adapters.ChecklistAdapter
import com.simplemobiletools.notes.pro.dialogs.NewChecklistItemDialog
import com.simplemobiletools.notes.pro.extensions.notesDB
import com.simplemobiletools.notes.pro.extensions.updateWidgets
import com.simplemobiletools.notes.pro.helpers.NOTE_ID
import com.simplemobiletools.notes.pro.helpers.NotesHelper
import com.simplemobiletools.notes.pro.interfaces.ChecklistItemsListener
import com.simplemobiletools.notes.pro.models.ChecklistItem
import com.simplemobiletools.notes.pro.models.Note
import kotlinx.android.synthetic.main.fragment_checklist.view.*

class ChecklistFragment : NoteFragment(), ChecklistItemsListener {
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

                val checklistItemType = object : TypeToken<List<ChecklistItem>>() {}.type
                items = Gson().fromJson<ArrayList<ChecklistItem>>(note!!.value, checklistItemType) ?: ArrayList(1)
                context!!.updateTextColors(view.checklist_holder)
                setupFragment()
            }
        }
    }

    override fun setMenuVisibility(menuVisible: Boolean) {
        super.setMenuVisibility(menuVisible)
        if (menuVisible) {
            activity?.hideKeyboard()
        }
    }

    private fun setupFragment() {
        val plusIcon = resources.getColoredDrawableWithColor(R.drawable.ic_plus_vector, if (context!!.isBlackAndWhiteTheme()) Color.BLACK else Color.WHITE)
        view.apply {
            checklist_fab.apply {
                setImageDrawable(plusIcon)
                background.applyColorFilter(context!!.getAdjustedPrimaryColor())
                setOnClickListener {
                    showNewItemDialog()
                }
            }

            fragment_placeholder_2.apply {
                setTextColor(context!!.getAdjustedPrimaryColor())
                underlineText()
                setOnClickListener {
                    showNewItemDialog()
                }
            }
        }
        setupAdapter()
    }

    private fun showNewItemDialog() {
        NewChecklistItemDialog(activity as SimpleActivity) {
            var currentMaxId = items.maxBy { it.id }?.id ?: 0
            it.forEach {
                val checklistItem = ChecklistItem(currentMaxId + 1, it, false)
                items.add(checklistItem)
                currentMaxId++
            }
            saveNote()
            if (items.size == it.size) {
                setupAdapter()
            } else {
                (view.checklist_list.adapter as? ChecklistAdapter)?.notifyDataSetChanged()
            }
        }
    }

    private fun setupAdapter() {
        view.apply {
            fragment_placeholder.beVisibleIf(items.isEmpty())
            fragment_placeholder_2.beVisibleIf(items.isEmpty())
            checklist_list.beVisibleIf(items.isNotEmpty())
        }

        ChecklistAdapter(activity as SimpleActivity, items, this, view.checklist_list, true) {
            val clickedNote = it as ChecklistItem
            clickedNote.isDone = !clickedNote.isDone
            saveNote(items.indexOfFirst { it.id == clickedNote.id })
            context?.updateWidgets()
        }.apply {
            view.checklist_list.adapter = this
        }
    }

    private fun saveNote(refreshIndex: Int = -1) {
        ensureBackgroundThread {
            if (note != null && context != null) {
                if (refreshIndex != -1) {
                    view.checklist_list.post {
                        view.checklist_list.adapter?.notifyItemChanged(refreshIndex)
                    }
                }

                note!!.value = getChecklistItems()
                context?.notesDB?.insertOrUpdate(note!!)
                context?.updateWidgets()
            }
        }
    }

    fun getChecklistItems() = Gson().toJson(items)

    override fun saveChecklist() {
        saveNote()
    }

    override fun refreshItems() {
        setupAdapter()
    }
}
