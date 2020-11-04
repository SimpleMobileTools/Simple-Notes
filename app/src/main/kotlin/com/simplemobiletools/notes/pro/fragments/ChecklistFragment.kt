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
import com.simplemobiletools.notes.pro.extensions.config
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

    lateinit var view: ViewGroup

    var items = ArrayList<ChecklistItem>()
    val checklistItems get(): String = Gson().toJson(items)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        view = inflater.inflate(R.layout.fragment_checklist, container, false) as ViewGroup
        noteId = arguments!!.getLong(NOTE_ID, 0L)
        return view
    }

    override fun onResume() {
        super.onResume()

        loadNoteById(noteId)
    }

    override fun setMenuVisibility(menuVisible: Boolean) {
        super.setMenuVisibility(menuVisible)

        if (menuVisible) {
            activity?.hideKeyboard()
        }
    }

    private fun loadNoteById(noteId: Long) {
        NotesHelper(activity!!).getNoteWithId(noteId) { storedNote ->
            if (storedNote != null && activity?.isDestroyed == false) {
                note = storedNote

                try {
                    val checklistItemType = object : TypeToken<List<ChecklistItem>>() {}.type
                    items = Gson().fromJson<ArrayList<ChecklistItem>>(storedNote.value, checklistItemType)
                        ?: ArrayList(1)
                } catch (e: Exception) {
                    migrateCheckListOnFailure(storedNote)
                }

                if (config?.moveUndoneChecklistItems == true) {
                    items.sortBy { it.isDone }
                }

                activity?.updateTextColors(view.checklist_holder)
                setupFragment()
            }
        }
    }

    private fun migrateCheckListOnFailure(note: Note) {
        items.clear()

        note.value.split("\n").map { it.trim() }.filter { it.isNotBlank() }.forEachIndexed { index, value ->
            items.add(ChecklistItem(
                id = index,
                title = value,
                isDone = false
            ))
        }

        saveChecklist()
    }

    private fun setupFragment() {
        if (activity == null || activity!!.isFinishing) {
            return
        }

        val plusIcon = resources.getColoredDrawableWithColor(R.drawable.ic_plus_vector, if (activity!!.isBlackAndWhiteTheme()) Color.BLACK else Color.WHITE)

        view.checklist_fab.apply {
            setImageDrawable(plusIcon)
            background?.applyColorFilter(activity!!.getAdjustedPrimaryColor())
            setOnClickListener {
                showNewItemDialog()
            }
        }

        view.fragment_placeholder_2.apply {
            setTextColor(activity!!.getAdjustedPrimaryColor())
            underlineText()
            setOnClickListener {
                showNewItemDialog()
            }
        }

        setupAdapter()
    }

    private fun showNewItemDialog() {
        NewChecklistItemDialog(activity as SimpleActivity) { titles ->
            var currentMaxId = items.maxBy { item -> item.id }?.id ?: 0

            titles.forEach { title ->
                title.split("\n").map { it.trim() }.filter { it.isNotBlank() }.forEach { row ->
                    items.add(ChecklistItem(currentMaxId + 1, row, false))
                    currentMaxId++
                }
            }

            saveNote()
            setupAdapter()

            (view.checklist_list.adapter as? ChecklistAdapter)?.notifyDataSetChanged()
        }
    }

    private fun setupAdapter() {
        with(view) {
            fragment_placeholder.beVisibleIf(items.isEmpty())
            fragment_placeholder_2.beVisibleIf(items.isEmpty())
            checklist_list.beVisibleIf(items.isNotEmpty())
        }

        ChecklistAdapter(
            activity = activity as SimpleActivity,
            items = items,
            listener = this,
            recyclerView = view.checklist_list,
            showIcons = true
        ) { item ->
            val clickedNote = item as ChecklistItem
            clickedNote.isDone = !clickedNote.isDone

            saveNote(items.indexOfFirst { it.id == clickedNote.id })
            context?.updateWidgets()
        }.apply {
            view.checklist_list.adapter = this
        }
    }

    private fun saveNote(refreshIndex: Int = -1) {
        ensureBackgroundThread {
            context?.let { ctx ->
                note?.let { currentNote ->
                    if (refreshIndex != -1) {
                        view.checklist_list.post {
                            view.checklist_list.adapter?.notifyItemChanged(refreshIndex)
                        }
                    }

                    currentNote.value = checklistItems
                    ctx.notesDB.insertOrUpdate(currentNote)
                    ctx.updateWidgets()
                }
            }
        }
    }

    override fun saveChecklist() {
        saveNote()
    }

    override fun refreshItems() {
        setupAdapter()
    }
}
