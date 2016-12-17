package com.simplemobiletools.notes.activities

import android.content.Intent
import android.os.Bundle
import android.support.v4.view.ViewPager
import android.util.TypedValue
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.simplemobiletools.filepicker.dialogs.ConfirmationDialog
import com.simplemobiletools.filepicker.extensions.toast
import com.simplemobiletools.filepicker.extensions.value
import com.simplemobiletools.notes.R
import com.simplemobiletools.notes.TYPE_NOTE
import com.simplemobiletools.notes.adapters.NotesPagerAdapter
import com.simplemobiletools.notes.databases.DBHelper
import com.simplemobiletools.notes.dialogs.NewNoteDialog
import com.simplemobiletools.notes.dialogs.OpenNoteDialog
import com.simplemobiletools.notes.dialogs.RenameNoteDialog
import com.simplemobiletools.notes.extensions.dpToPx
import com.simplemobiletools.notes.extensions.getTextSize
import com.simplemobiletools.notes.models.Note
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_note.*

class MainActivity : SimpleActivity(), ViewPager.OnPageChangeListener {
    lateinit var mCurrentNote: Note
    lateinit var mAdapter: NotesPagerAdapter
    lateinit var mDb: DBHelper
    lateinit var mNotes: List<Note>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mDb = DBHelper.newInstance(applicationContext)
        initViewPager()

        pager_title_strip.setTextSize(TypedValue.COMPLEX_UNIT_PX, getTextSize())
        pager_title_strip.layoutParams.height = (pager_title_strip.height + resources.getDimension(R.dimen.activity_margin) * 2).toInt()
    }

    fun initViewPager() {
        mNotes = mDb.getNotes()
        mCurrentNote = mNotes[0]
        val itemIndex = getNoteIndexWithId(config.currentNoteId)

        mAdapter = NotesPagerAdapter(supportFragmentManager, mNotes)
        view_pager.apply {
            adapter = mAdapter
            currentItem = itemIndex
            addOnPageChangeListener(this@MainActivity)
        }
    }

    override fun onResume() {
        super.onResume()
        invalidateOptionsMenu()
        pager_title_strip.setTextSize(TypedValue.COMPLEX_UNIT_PX, getTextSize())
        pager_title_strip.setGravity(Gravity.CENTER_VERTICAL)
        pager_title_strip.setNonPrimaryAlpha(0.4f)
    }

    override fun onDestroy() {
        super.onDestroy()
        config.isFirstRun = false
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val shouldBeVisible = mNotes.size > 1
        menu.apply {
            findItem(R.id.rename_note).isVisible = shouldBeVisible
            findItem(R.id.open_note).isVisible = shouldBeVisible
            findItem(R.id.delete_note).isVisible = shouldBeVisible
        }

        pager_title_strip.visibility = if (shouldBeVisible) View.VISIBLE else View.GONE

        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.open_note   -> displayOpenNoteDialog()
            R.id.new_note    -> displayNewNoteDialog()
            R.id.rename_note -> displayRenameDialog()
            R.id.share       -> shareText()
            R.id.delete_note -> displayDeleteNotePrompt()
            R.id.settings ->
                startActivity(Intent(applicationContext, SettingsActivity::class.java))
            R.id.about ->
                startActivity(Intent(applicationContext, AboutActivity::class.java))
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun displayRenameDialog() {
        RenameNoteDialog(this, mDb, mCurrentNote) {
            mCurrentNote = it
            initViewPager()
        }
    }

    private fun updateSelectedNote(id: Int) {
        config.currentNoteId = id
        val index = getNoteIndexWithId(id)
        view_pager.currentItem = index
        mCurrentNote = mNotes[index]
    }

    fun displayNewNoteDialog() {
        NewNoteDialog(this, mDb) {
            val newNote = Note(0, it, "", TYPE_NOTE)
            val id = mDb.insertNote(newNote)
            mNotes = mDb.getNotes()
            invalidateOptionsMenu()
            initViewPager()
            updateSelectedNote(id)
            mAdapter.showKeyboard(getNoteIndexWithId(id))
        }
    }

    private fun displayDeleteNotePrompt() {
        val message = String.format(getString(R.string.delete_note_prompt_message), mCurrentNote.title)
        ConfirmationDialog(this, message) {
            deleteNote()
        }
    }

    private fun deleteNote() {
        if (mNotes.size <= 1)
            return

        mDb.deleteNote(mCurrentNote.id)
        mNotes = mDb.getNotes()

        val firstNoteId = mNotes[0].id
        updateSelectedNote(firstNoteId)
        config.widgetNoteId = firstNoteId
        invalidateOptionsMenu()
        initViewPager()
    }

    private fun displayOpenNoteDialog() {
        OpenNoteDialog(this) {
            updateSelectedNote(it)
        }
    }

    private fun getNoteIndexWithId(id: Int): Int {
        for (i in 0..mNotes.count() - 1) {
            if (mNotes[i].id == id) {
                mCurrentNote = mNotes[i]
                return i
            }
        }
        return 0
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

    override fun onPageScrollStateChanged(state: Int) {
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
    }

    override fun onPageSelected(position: Int) {
        mCurrentNote = mNotes[position]
        config.currentNoteId = mCurrentNote.id
    }
}
