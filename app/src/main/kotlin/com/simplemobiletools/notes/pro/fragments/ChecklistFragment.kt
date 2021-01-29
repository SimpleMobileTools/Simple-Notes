package com.simplemobiletools.notes.pro.fragments

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
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
import com.simplemobiletools.notes.pro.dialogs.SplitChecklistItemDialog
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

    private var isRotate = true

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
        val pasteIcon = resources.getColoredDrawableWithColor(R.drawable.ic_paste_vector, if (activity!!.isBlackAndWhiteTheme()) Color.BLACK else Color.WHITE)

        view.checklist_fab.apply {
            setImageDrawable(plusIcon)
            background?.applyColorFilter(activity!!.getAdjustedPrimaryColor())
            setOnClickListener {
                showHideSubMenu()
            }
        }

        view.fab_add.apply {
            setImageDrawable(plusIcon)
            background?.applyColorFilter(activity!!.getAdjustedPrimaryColor())
            setOnClickListener {
                showNewItemDialog()
            }
        }

        view.fab_paste.apply {
            setImageDrawable(pasteIcon)
            background?.applyColorFilter(activity!!.getAdjustedPrimaryColor())
            setOnClickListener {
                showPasteCheckListDialog()
            }
        }

        view.fragment_placeholder_2.apply {
            setTextColor(activity!!.getAdjustedPrimaryColor())
            underlineText()
            setOnClickListener {
                showHideSubMenu()
            }
        }

        setupAdapter()
    }

    private fun showHideSubMenu() {
        isRotate = rotateFab(view.checklist_fab, !isRotate)

        if (isRotate) {
            showSubMenu(view.fab_paste_container)
            showSubMenu(view.fab_add_container)
        } else {
            hideSubMenu(view.fab_paste_container)
            hideSubMenu(view.fab_add_container)
        }
    }

    private fun showNewItemDialog() {
        showHideSubMenu()
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

    private fun showPasteCheckListDialog() {
        showHideSubMenu()
        SplitChecklistItemDialog(activity as SimpleActivity) { titles ->
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

    private fun rotateFab(v: View, rotate: Boolean): Boolean {
        v.animate().setDuration(200).rotation(if (rotate) 135f else 0f)
        return rotate
    }

    private fun showSubMenu(v: View) {
        v.visibility = View.VISIBLE
        v.alpha = 0f
        v.translationY = v.height.toFloat()
        v.animate()
                .setDuration(200)
                .translationY(0f)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator?) {
                        super.onAnimationEnd(animation)
                    }
                })
                .alpha(1f)
                .start()
    }

    private fun hideSubMenu(v: View) {
        v.visibility = View.VISIBLE
        v.alpha = 1f
        v.translationY = 0f
        v.animate()
                .setDuration(200)
                .translationY(v.height.toFloat())
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        v.visibility = View.GONE
                        super.onAnimationEnd(animation)
                    }
                }).alpha(0f)
                .start()
    }
}
