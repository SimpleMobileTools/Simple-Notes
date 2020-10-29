package com.simplemobiletools.notes.pro.adapters

import android.app.Activity
import android.os.Bundle
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.simplemobiletools.commons.extensions.showErrorToast
import com.simplemobiletools.notes.pro.fragments.ChecklistFragment
import com.simplemobiletools.notes.pro.fragments.NoteFragment
import com.simplemobiletools.notes.pro.fragments.TextFragment
import com.simplemobiletools.notes.pro.helpers.NOTE_ID
import com.simplemobiletools.notes.pro.helpers.NoteType
import com.simplemobiletools.notes.pro.models.Note

class NotesPagerAdapter(fm: FragmentManager, val notes: List<Note>, val activity: Activity) : FragmentStatePagerAdapter(fm) {
    private var fragments: HashMap<Int, NoteFragment> = LinkedHashMap()

    override fun getCount() = notes.size

    override fun getItem(position: Int): NoteFragment {
        val bundle = Bundle()
        val note = notes[position]
        val id = note.id
        if (id != null) {
            bundle.putLong(NOTE_ID, id)
        }

        if (fragments.containsKey(position)) {
            return fragments[position]!!
        }

        val fragment = if (note.type == NoteType.TYPE_TEXT.value) TextFragment() else ChecklistFragment()
        fragment.arguments = bundle
        fragments[position] = fragment
        return fragment
    }

    override fun getPageTitle(position: Int) = notes[position].title

    fun updateCurrentNoteData(position: Int, path: String, value: String) {
        (fragments[position] as? TextFragment)?.apply {
            updateNotePath(path)
            updateNoteValue(value)
        }
    }

    fun isChecklistFragment(position: Int): Boolean = (fragments[position] is ChecklistFragment)

    fun textFragment(position: Int): TextFragment? = (fragments[position] as? TextFragment)

    fun getCurrentNotesView(position: Int) = (fragments[position] as? TextFragment)?.getNotesView()

    fun getCurrentNoteViewText(position: Int) = (fragments[position] as? TextFragment)?.getCurrentNoteViewText()

    fun appendText(position: Int, text: String) = (fragments[position] as? TextFragment)?.getNotesView()?.append(text)

    fun saveCurrentNote(position: Int, force: Boolean) = (fragments[position] as? TextFragment)?.saveText(force)

    fun focusEditText(position: Int) = (fragments[position] as? TextFragment)?.focusEditText()

    fun anyHasUnsavedChanges() = fragments.values.any { (it as? TextFragment)?.hasUnsavedChanges() == true }

    fun saveAllFragmentTexts() = fragments.values.forEach { (it as? TextFragment)?.saveText(false) }

    fun getNoteChecklistRawItems(position: Int) = (fragments[position] as? ChecklistFragment)?.items

    fun getNoteChecklistItems(position: Int) = (fragments[position] as? ChecklistFragment)?.checklistItems

    fun undo(position: Int) = (fragments[position] as? TextFragment)?.undo()

    fun redo(position: Int) = (fragments[position] as? TextFragment)?.redo()

    override fun finishUpdate(container: ViewGroup) {
        try {
            super.finishUpdate(container)
        } catch (e: Exception) {
            activity.showErrorToast(e)
        }
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        super.destroyItem(container, position, `object`)
        fragments.remove(position)
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val fragment = super.instantiateItem(container, position) as NoteFragment
        fragments[position] = fragment
        return fragment
    }
}
