package com.simplemobiletools.notes.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import com.simplemobiletools.notes.R
import com.simplemobiletools.notes.activities.MainActivity
import com.simplemobiletools.notes.adapters.NotesRecyclerAdapter
import com.simplemobiletools.notes.extensions.dbHelper
import com.simplemobiletools.notes.models.Note

class RecyclerViewFragment : Fragment(){

    lateinit var notesRecyclerAdapter : NotesRecyclerAdapter
    lateinit var mNotes : ArrayList<Note>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        var view: RecyclerView = inflater.inflate(R.layout.activity_recycler_view_main, container, false) as RecyclerView


        var viewManager = LinearLayoutManager(MainActivity.mainActivityInstance)
        var mNotes = MainActivity.mainActivityInstance.dbHelper.getNotes()

        notesRecyclerAdapter = NotesRecyclerAdapter(mNotes)

        view.apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = notesRecyclerAdapter
        }


        return view
    }

    override fun onPrepareOptionsMenu(menu : Menu) {
        
        menu.apply {
            findItem(R.id.rename_note) .isVisible = false
            findItem(R.id.save_note) .isVisible = false
            findItem(R.id.export_as_file) .isVisible = false
            findItem(R.id.open_note) .isVisible = false
            findItem(R.id.share) .isVisible = false
            findItem(R.id.delete_note) .isVisible = false
        }
        
        return super.onPrepareOptionsMenu(menu)
    }

}