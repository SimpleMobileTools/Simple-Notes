package com.simplemobiletools.notes.pro.fragments

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

    lateinit var view: ViewGroup

    var items = ArrayList<ChecklistItem>()
    val checklistItems get(): String = Gson().toJson(items)

    private var isSortAsc = true

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
                    items = Gson().fromJson<ArrayList<ChecklistItem>>(storedNote.value, checklistItemType) ?: ArrayList(1)

                    // checklist title can be null only because of the glitch in upgrade to 6.6.0, remove this check in the future
                    items = items.filter { it.title != null }.toMutableList() as ArrayList<ChecklistItem>
                } catch (e: Exception) {
                    migrateCheckListOnFailure(storedNote)
                }

                if (config?.moveDoneChecklistItems == true) {
                    items.sortBy { it.isDone }
                }

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

        val adjustedPrimaryColor = activity!!.getAdjustedPrimaryColor()
        view.checklist_fab.apply {
            setColors(
                activity!!.config.textColor,
                adjustedPrimaryColor,
                adjustedPrimaryColor.getContrastColor()
            )

            setOnClickListener {
                showNewItemDialog()
            }
        }

        view.fragment_placeholder.setTextColor(activity!!.config.textColor)
        view.fragment_placeholder_2.apply {
            setTextColor(adjustedPrimaryColor)
            underlineText()
            setOnClickListener {
                showNewItemDialog()
            }
        }

        checkLockState()
        setupAdapter()
    }

    override fun checkLockState() {
        view.apply {
            checklist_content_holder.beVisibleIf(!note!!.isLocked() || shouldShowLockedContent)
            checklist_fab.beVisibleIf(!note!!.isLocked() || shouldShowLockedContent)
            setupLockedViews(this, note!!)
        }
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
        }
    }

    private fun setupAdapter() {
        updateUIVisibility()

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

    fun removeDoneItems() {
        items = items.filter { !it.isDone }.toMutableList() as ArrayList<ChecklistItem>
        saveNote()
        setupAdapter()
    }

    fun sortCheckListItems() {
        if(isSortAsc) {
            items.sortBy { it.title }
            context?.toast(R.string.sorted_ascending_order)
        } else {
            items.sortByDescending { it.title }
            context?.toast(R.string.sorted_descending_order)
        }
        isSortAsc = !isSortAsc
        saveNote()
        setupAdapter()
    }

    private fun updateUIVisibility() {
        view.apply {
            fragment_placeholder.beVisibleIf(items.isEmpty())
            fragment_placeholder_2.beVisibleIf(items.isEmpty())
            checklist_list.beVisibleIf(items.isNotEmpty())
        }
    }

    override fun saveChecklist() {
        saveNote()
    }

    override fun refreshItems() {
        setupAdapter()
    }
}
