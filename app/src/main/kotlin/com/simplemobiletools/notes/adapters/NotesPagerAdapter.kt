package com.simplemobiletools.notes.adapters

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.util.SparseArray
import com.simplemobiletools.notes.fragments.NoteFragment
import com.simplemobiletools.notes.helpers.NOTE_ID
import com.simplemobiletools.notes.models.Note

class NotesPagerAdapter(fm: FragmentManager, private val notes: List<Note>) : FragmentStatePagerAdapter(fm) {
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

    fun getCurrentNoteViewText(position: Int) = fragments[position].getCurrentNoteViewText()

    fun showKeyboard(position: Int) = fragments[position]?.showKeyboard()
}
