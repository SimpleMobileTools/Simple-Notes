package com.simplemobiletools.notes.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.view.ViewPager
import android.text.method.ArrowKeyMovementMethod
import android.text.method.LinkMovementMethod
import android.util.TypedValue
import android.view.*
import com.simplemobiletools.commons.dialogs.FilePickerDialog
import com.simplemobiletools.commons.dialogs.RadioGroupDialog
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.helpers.LICENSE_KOTLIN
import com.simplemobiletools.commons.helpers.LICENSE_RTL
import com.simplemobiletools.commons.helpers.LICENSE_STETHO
import com.simplemobiletools.commons.models.RadioItem
import com.simplemobiletools.commons.models.Release
import com.simplemobiletools.commons.views.MyEditText
import com.simplemobiletools.notes.BuildConfig
import com.simplemobiletools.notes.R
import com.simplemobiletools.notes.adapters.NotesPagerAdapter
import com.simplemobiletools.notes.dialogs.*
import com.simplemobiletools.notes.extensions.config
import com.simplemobiletools.notes.extensions.getTextSize
import com.simplemobiletools.notes.helpers.DBHelper
import com.simplemobiletools.notes.helpers.MIME_TEXT_PLAIN
import com.simplemobiletools.notes.helpers.OPEN_NOTE_ID
import com.simplemobiletools.notes.helpers.TYPE_NOTE
import com.simplemobiletools.notes.models.Note
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_note.*
import java.io.File
import java.nio.charset.Charset

class MainActivity : SimpleActivity(), ViewPager.OnPageChangeListener {
    val STORAGE_OPEN_FILE = 1
    val STORAGE_EXPORT_AS_FILE = 2

    lateinit var mCurrentNote: Note
    lateinit var mAdapter: NotesPagerAdapter
    lateinit var mDb: DBHelper
    lateinit var mNotes: List<Note>

    var noteViewWithTextSelected: MyEditText? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mDb = DBHelper.newInstance(applicationContext)
        initViewPager()

        pager_title_strip.setTextSize(TypedValue.COMPLEX_UNIT_PX, getTextSize())
        pager_title_strip.layoutParams.height = (pager_title_strip.height + resources.getDimension(R.dimen.activity_margin) * 2).toInt()
        checkWhatsNewDialog()
        storeStoragePaths()

        intent.apply {
            if (action == Intent.ACTION_SEND && type == MIME_TEXT_PLAIN) {
                getStringExtra(Intent.EXTRA_TEXT)?.let {
                    handleText(it)
                    intent.removeExtra(Intent.EXTRA_TEXT)
                }
            }
        }
    }

    private fun handleText(text: String) {
        val notes = mDb.getNotes()
        val list = arrayListOf<RadioItem>().apply {
            add(RadioItem(0, getString(R.string.create_new_note)))
            notes.forEachIndexed { index, note ->
                add(RadioItem(index + 1, note.title))
            }
        }

        RadioGroupDialog(this, list, -1, R.string.add_to_note) {
            if (it as Int == 0) {
                displayNewNoteDialog(text)
            } else {
                updateSelectedNote(notes[it - 1].id)
                addTextToCurrentNote(if (mCurrentNote.value.isEmpty()) text else "\n$text")
            }
        }
    }

    private fun initViewPager() {
        mNotes = mDb.getNotes()
        mCurrentNote = mNotes[0]
        var wantedNoteId = intent.getIntExtra(OPEN_NOTE_ID, -1)
        if (wantedNoteId == -1)
            wantedNoteId = config.currentNoteId

        val itemIndex = getNoteIndexWithId(wantedNoteId)

        mAdapter = NotesPagerAdapter(supportFragmentManager, mNotes, this)
        view_pager.apply {
            adapter = mAdapter
            currentItem = itemIndex
            addOnPageChangeListener(this@MainActivity)
        }
    }

    override fun onResume() {
        super.onResume()
        invalidateOptionsMenu()
        pager_title_strip.apply {
            setTextSize(TypedValue.COMPLEX_UNIT_PX, getTextSize())
            setGravity(Gravity.CENTER_VERTICAL)
            setNonPrimaryAlpha(0.4f)
            setTextColor(config.textColor)
        }
        updateTextColors(view_pager)
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
        saveCurrentNote()
        when (item.itemId) {
            R.id.open_note -> displayOpenNoteDialog()
            R.id.new_note -> displayNewNoteDialog()
            R.id.rename_note -> displayRenameDialog()
            R.id.share -> shareText()
            R.id.open_file -> tryOpenFile()
            R.id.export_as_file -> tryExportAsFile()
            R.id.delete_note -> displayDeleteNotePrompt()
            R.id.settings -> startActivity(Intent(applicationContext, SettingsActivity::class.java))
            R.id.about -> launchAbout()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    // https://code.google.com/p/android/issues/detail?id=191430 quickfix
    override fun onActionModeStarted(mode: ActionMode?) {
        super.onActionModeStarted(mode)
        currentNotesView().apply {
            if (config.clickableLinks || movementMethod == LinkMovementMethod.getInstance()) {
                movementMethod = ArrowKeyMovementMethod.getInstance()
                noteViewWithTextSelected = this
            }
        }
    }

    override fun onActionModeFinished(mode: ActionMode?) {
        super.onActionModeFinished(mode)
        if (config.clickableLinks) {
            noteViewWithTextSelected?.movementMethod = LinkMovementMethod.getInstance()
        }
    }

    private fun currentNotesView() = mAdapter.getItem(view_pager.currentItem).notes_view

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

    private fun displayNewNoteDialog(value: String = "") {
        NewNoteDialog(this, mDb) {
            val newNote = Note(0, it, value, TYPE_NOTE)
            addNewNote(newNote)
        }
    }

    private fun addNewNote(note: Note) {
        val id = mDb.insertNote(note)
        mNotes = mDb.getNotes()
        invalidateOptionsMenu()
        initViewPager()
        updateSelectedNote(id)
        view_pager.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                mAdapter.showKeyboard(getNoteIndexWithId(id))
                view_pager.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })
    }

    private fun launchAbout() {
        startAboutActivity(R.string.app_name, LICENSE_KOTLIN or LICENSE_STETHO or LICENSE_RTL, BuildConfig.VERSION_NAME)
    }

    private fun tryOpenFile() {
        if (hasWriteStoragePermission()) {
            openFile()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), STORAGE_OPEN_FILE)
        }
    }

    private fun openFile() {
        FilePickerDialog(this) {
            val file = File(it)
            if (file.isImageVideoGif()) {
                toast(R.string.invalid_file_format)
            } else if (file.length() > 10 * 1000 * 1000) {
                toast(R.string.file_too_large)
            } else if (mDb.doesTitleExist(it.getFilenameFromPath())) {
                toast(R.string.title_taken)
            } else {
                OpenFileDialog(this, it) {
                    addNewNote(it)
                }
            }
        }
    }

    private fun tryExportAsFile() {
        if (hasWriteStoragePermission()) {
            exportAsFile()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), STORAGE_EXPORT_AS_FILE)
        }
    }

    private fun exportAsFile() {
        ExportAsDialog(this, mCurrentNote) {
            if (getCurrentNoteText()?.isNotEmpty() == true)
                exportNoteValueToFile(it, getCurrentNoteText()!!)
        }
    }

    fun exportNoteValueToFile(path: String, content: String) {
        try {
            val file = File(path)
            if (file.isDirectory) {
                toast(R.string.name_taken)
                return
            }

            if (needsStupidWritePermissions(path)) {
                handleSAFDialog(file) {
                    var document = getFileDocument(path) ?: return@handleSAFDialog
                    if (!file.exists()) {
                        document = document.createFile("", file.name)
                    }
                    contentResolver.openOutputStream(document.uri).apply {
                        write(content.toByteArray(Charset.forName("UTF-8")), 0, content.length)
                        flush()
                        close()
                    }
                }
            } else {
                file.printWriter().use { out ->
                    out.write(content)
                }
            }
            noteSavedSuccessfully(path.getFilenameFromPath())
        } catch (e: Exception) {
            toast(R.string.unknown_error_occurred)
        }
    }

    fun noteSavedSuccessfully(title: String) {
        if (config.displaySuccess) {
            val message = String.format(getString(R.string.note_saved_successfully), title)
            toast(message)
        }
    }

    private fun getCurrentNoteText() = (view_pager.adapter as NotesPagerAdapter).getCurrentNoteViewText(view_pager.currentItem)

    private fun addTextToCurrentNote(text: String) = (view_pager.adapter as NotesPagerAdapter).appendText(view_pager.currentItem, text)

    private fun saveCurrentNote() = (view_pager.adapter as NotesPagerAdapter).saveCurrentNote(view_pager.currentItem)

    private fun displayDeleteNotePrompt() {
        DeleteNoteDialog(this, mCurrentNote) {
            deleteNote(it)
        }
    }

    fun deleteNote(deleteFile: Boolean) {
        if (mNotes.size <= 1)
            return

        val path = mCurrentNote.path
        mDb.deleteNote(mCurrentNote.id)
        mNotes = mDb.getNotes()

        val firstNoteId = mNotes[0].id
        updateSelectedNote(firstNoteId)
        config.widgetNoteId = firstNoteId
        invalidateOptionsMenu()
        initViewPager()

        if (deleteFile) {
            deleteFile(File(path)) {
                if (!it) {
                    toast(R.string.unknown_error_occurred)
                }
            }
        }
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
        val text = getCurrentNoteText()
        if (text == null || text.isEmpty()) {
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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (requestCode == STORAGE_OPEN_FILE) {
                openFile()
            } else if (requestCode == STORAGE_EXPORT_AS_FILE) {
                exportAsFile()
            }
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

    private fun checkWhatsNewDialog() {
        arrayListOf<Release>().apply {
            add(Release(25, R.string.release_25))
            add(Release(28, R.string.release_28))
            add(Release(29, R.string.release_29))
            checkWhatsNew(this, BuildConfig.VERSION_CODE)
        }
    }
}
