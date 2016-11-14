package com.simplemobiletools.notes.activities

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import com.simplemobiletools.notes.MyWidgetProvider
import com.simplemobiletools.notes.R
import com.simplemobiletools.notes.TYPE_NOTE
import com.simplemobiletools.notes.Utils
import com.simplemobiletools.notes.databases.DBHelper
import com.simplemobiletools.notes.dialogs.OpenNoteDialog
import com.simplemobiletools.notes.dialogs.WidgetNoteDialog
import com.simplemobiletools.notes.extensions.toast
import com.simplemobiletools.notes.extensions.value
import com.simplemobiletools.notes.models.Note
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : SimpleActivity(), OpenNoteDialog.OpenNoteListener {
    private var mCurrentNote: Note? = null

    lateinit var mDb: DBHelper
    lateinit var mNotes: List<Note>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mDb = DBHelper.newInstance(applicationContext)
        mNotes = mDb.getNotes()
        updateSelectedNote(config.currentNoteId)
        notes_fab.setOnClickListener { displayNewNoteDialog() }
    }

    override fun onResume() {
        super.onResume()
        invalidateOptionsMenu()
        notes_view.setTextSize(TypedValue.COMPLEX_UNIT_PX, Utils.getTextSize(applicationContext))
    }

    override fun onPause() {
        super.onPause()
        saveText()
    }

    override fun onDestroy() {
        super.onDestroy()
        config.isFirstRun = false
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        val openNote = menu.findItem(R.id.open_note)
        openNote.isVisible = mNotes.size > 1

        val deleteNote = menu.findItem(R.id.delete_note)
        deleteNote.isVisible = mNotes.size > 1

        val changeNote = menu.findItem(R.id.change_widget_note)
        changeNote.isVisible = mNotes.size > 1

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.delete_note -> {
                displayDeleteNotePrompt()
                true
            }
            R.id.open_note -> {
                displayOpenNoteDialog()
                true
            }
            R.id.share -> {
                shareText()
                true
            }
            R.id.change_widget_note -> {
                showWidgetNotePicker()
                true
            }
            R.id.settings -> {
                startActivity(Intent(applicationContext, SettingsActivity::class.java))
                true
            }
            R.id.about -> {
                startActivity(Intent(applicationContext, AboutActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showWidgetNotePicker() {
        WidgetNoteDialog(this)
    }

    private fun updateSelectedNote(id: Int) {
        saveText()
        mCurrentNote = mDb.getNote(id)
        mNotes = mDb.getNotes()
        if (mCurrentNote != null) {
            config.currentNoteId = id
            notes_view.setText(mCurrentNote!!.value)
            current_note_title.text = mCurrentNote!!.title
        }

        current_note_label.visibility = if (mNotes.size <= 1) View.GONE else View.VISIBLE
        current_note_title.visibility = if (mNotes.size <= 1) View.GONE else View.VISIBLE
        updateWidget(applicationContext)
    }

    fun displayNewNoteDialog() {
        val newNoteView = layoutInflater.inflate(R.layout.new_note, null)

        AlertDialog.Builder(this).apply {
            setTitle(resources.getString(R.string.new_note))
            setView(newNoteView)
            setPositiveButton(R.string.ok, null)
            setNegativeButton(R.string.cancel, null)
            create().apply {
                window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
                show()
                getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                    val titleET = newNoteView.findViewById(R.id.note_name) as EditText
                    val title = titleET.value
                    if (title.isEmpty()) {
                        toast(R.string.no_title)
                    } else if (mDb.doesTitleExist(title)) {
                        toast(R.string.title_taken)
                    } else {
                        saveText()
                        val newNote = Note(0, title, "", TYPE_NOTE)
                        val id = mDb.insertNote(newNote)
                        updateSelectedNote(id)
                        dismiss()
                        invalidateOptionsMenu()
                    }
                }
            }
        }
    }

    private fun displayDeleteNotePrompt() {
        val res = resources
        AlertDialog.Builder(this).apply {
            setTitle(res.getString(R.string.delete_note_prompt_title))
            setMessage(String.format(res.getString(R.string.delete_note_prompt_message), mCurrentNote!!.title))
            setPositiveButton(R.string.ok) { dialog, which -> deleteNote() }
            setNegativeButton(R.string.cancel, null)
            show()
        }
    }

    private fun deleteNote() {
        if (mNotes.size <= 1)
            return

        mDb.deleteNote(mCurrentNote!!.id)
        mNotes = mDb.getNotes()

        val firstNoteId = mNotes[0].id
        updateSelectedNote(firstNoteId)
        config.widgetNoteId = firstNoteId
        invalidateOptionsMenu()
    }

    private fun displayOpenNoteDialog() {
        OpenNoteDialog(this)
    }

    private fun saveText() {
        if (mCurrentNote == null)
            return

        val newText = notes_view.value
        val oldText = mCurrentNote!!.value
        if (newText != oldText) {
            toast(R.string.note_saved)
            mCurrentNote!!.value = newText
            mDb.updateNote(mCurrentNote!!)
        }

        hideKeyboard()
        updateWidget(applicationContext)
    }

    private fun shareText() {
        val text = notes_view.value
        if (text.isEmpty()) {
            toast(R.string.cannot_share_empty_text)
            return
        }

        val res = resources
        val shareTitle = res.getString(R.string.share_via)
        Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_SUBJECT, res.getString(R.string.simple_note))
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
            startActivity(Intent.createChooser(this, shareTitle))
        }
    }

    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(notes_view.windowToken, 0)
    }

    override fun noteSelected(id: Int) = updateSelectedNote(id)

    fun updateWidget(context: Context) {
        val widgetManager = AppWidgetManager.getInstance(context)
        val ids = widgetManager.getAppWidgetIds(ComponentName(context, MyWidgetProvider::class.java))

        val intent = Intent(context, MyWidgetProvider::class.java)
        intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
        context.sendBroadcast(intent)
    }
}
