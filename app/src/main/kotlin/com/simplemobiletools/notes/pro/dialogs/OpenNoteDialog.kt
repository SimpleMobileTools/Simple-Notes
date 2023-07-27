package com.simplemobiletools.notes.pro.dialogs

import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.simplemobiletools.commons.activities.BaseSimpleActivity
import com.simplemobiletools.commons.extensions.getAlertDialogBuilder
import com.simplemobiletools.commons.extensions.setupDialogStuff
import com.simplemobiletools.commons.views.AutoStaggeredGridLayoutManager
import com.simplemobiletools.notes.pro.R
import com.simplemobiletools.notes.pro.adapters.OpenNoteAdapter
import com.simplemobiletools.notes.pro.helpers.NotesHelper
import com.simplemobiletools.notes.pro.models.Note
import kotlinx.android.synthetic.main.dialog_open_note.view.dialog_open_note_list

class OpenNoteDialog(val activity: BaseSimpleActivity, val callback: (checkedId: Long, newNote: Note?) -> Unit) {
    private var dialog: AlertDialog? = null

    init {
        val view = activity.layoutInflater.inflate(R.layout.dialog_open_note, null)

        val noteItemWidth = activity.resources.getDimensionPixelSize(R.dimen.grid_note_item_width)
        view.dialog_open_note_list.layoutManager = AutoStaggeredGridLayoutManager(noteItemWidth, StaggeredGridLayoutManager.VERTICAL)

        NotesHelper(activity).getNotes {
            initDialog(it, view)
        }
    }

    private fun initDialog(notes: List<Note>, view: View) {
        view.dialog_open_note_list.adapter = OpenNoteAdapter(activity, notes, view.dialog_open_note_list) {
            if (it is Note) {
                callback(it.id!!, null)
                dialog?.dismiss()
            } else {
                NewNoteDialog(activity, setChecklistAsDefault = false) {
                    callback(0, it)
                    dialog?.dismiss()
                }
            }
        }

        activity.getAlertDialogBuilder()
            .setNegativeButton(R.string.cancel, null)
            .apply {
                activity.setupDialogStuff(view, this, R.string.open_note) { alertDialog ->
                    dialog = alertDialog
                }
            }
    }
}
