package com.simplemobiletools.notes.adapters

import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.simplemobiletools.notes.R
import com.simplemobiletools.notes.activities.MainActivity
import com.simplemobiletools.notes.fragments.NoteFragment
import com.simplemobiletools.notes.helpers.NOTE_ID
import com.simplemobiletools.notes.helpers.OpenNoteFragmentHelper
import com.simplemobiletools.notes.models.Note

class NotesRecyclerAdapter(val _notes: List<Note>) :
        RecyclerView.Adapter<NotesRecyclerAdapter.ViewHolder>() {

    class ViewHolder(val textView: TextView) : RecyclerView.ViewHolder(textView)

    lateinit var currentlyOpenNote : NoteFragment
    lateinit var notes : List<Note>

    init{
        notes = _notes // Allow changing the list from outside, in case of imports etc
    }

    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): NotesRecyclerAdapter.ViewHolder {

        val textView = LayoutInflater.from(parent.context)
                .inflate(R.layout.selectable_textview, parent, false) as TextView

        return ViewHolder(textView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.textView.text = notes[position].title
        holder.textView.setTag(position)
        var adapter : NotesRecyclerAdapter = this
        holder.textView.setOnClickListener(object : View.OnClickListener{
            override fun onClick(v: View?) {
                var fragment = NoteFragment()
                var bundle = Bundle()
                var tv = v as TextView
                var currentNotePosition = v.getTag() as Int
                bundle.putInt(NOTE_ID,notes[currentNotePosition].id)
                bundle.putString("title",tv.getText().toString())
                fragment.arguments = bundle
                MainActivity.mainActivityInstance.mCurrentNote = notes[currentNotePosition]

                OpenNoteFragmentHelper.openNote(fragment,bundle,adapter)

                            }
        })
    }

    override fun getItemCount() = notes.size
}