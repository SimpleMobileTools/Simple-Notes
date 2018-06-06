package com.simplemobiletools.notes.helpers

import android.os.Bundle
import com.simplemobiletools.notes.activities.MainActivity
import com.simplemobiletools.notes.adapters.NotesRecyclerAdapter
import com.simplemobiletools.notes.fragments.NoteFragment

class OpenNoteFragmentHelper {

    companion object {
        fun openNote(fragment: NoteFragment,bundle: Bundle,notesRecyclerAdapter: NotesRecyclerAdapter){
            notesRecyclerAdapter.currentlyOpenNote = fragment
            MainActivity.mainActivityInstance.supportActionBar?.setTitle(bundle.getString("title"))

            val fragmentManager = MainActivity.mainActivityInstance.supportFragmentManager
            val fragmentTransaction = fragmentManager.beginTransaction()
            fragmentTransaction.replace(android.R.id.content,fragment, EDIT_NOTE_FRAGMENT)
            fragmentTransaction.addToBackStack(null);

            fragmentTransaction.commit()
        }
    }

}