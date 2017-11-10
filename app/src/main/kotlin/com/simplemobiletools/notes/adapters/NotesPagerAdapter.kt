package com.simplemobiletools.notes.adapters

import android.app.Activity
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.util.SparseArray
import android.view.ViewGroup
import com.simplemobiletools.commons.extensions.showErrorToast
import com.simplemobiletools.notes.fragments.NoteFragment
import com.simplemobiletools.notes.helpers.NOTE_ID
import com.simplemobiletools.notes.models.Note

class NotesPagerAdapter(fm: FragmentManager, val notes: List<Note>, val activity: Activity) : FragmentStatePagerAdapter(fm) {
    var fragments: SparseArray<NoteFragment> = SparseArray(5)

    override fun getCount() = notes.size

    override fun getItem(position: Int): Fragment {
        val bundle = Bundle()
        val id = notes[position].id
        bundle.putInt(NOTE_ID, id)

        if (fragments.get(position) != null)
            return fragments[position]

        val fragment = NoteFragment()
        fragment.arguments = bundle
        fragments.put(position, fragment)
        return fragment
    }

    override fun getPageTitle(position: Int) = notes[position].title

    fun getCurrentNoteViewText(position: Int) = fragments[position]?.getCurrentNoteViewText()

    fun appendText(position: Int, text: String) = fragments[position]?.getNotesView()?.append(text)

    fun saveCurrentNote(position: Int) = fragments[position]?.saveText()

    fun focusEditText(position: Int) = fragments[position]?.focusEditText()

    override fun finishUpdate(container: ViewGroup) {
        try {
            super.finishUpdate(container)
        } catch (e: Exception) {
            activity.showErrorToast(e)
        }
    }
}
