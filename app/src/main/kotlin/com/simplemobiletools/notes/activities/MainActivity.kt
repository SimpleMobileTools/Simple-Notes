package com.simplemobiletools.notes.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.view.ViewPager
import android.util.TypedValue
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.simplemobiletools.commons.dialogs.ConfirmationDialog
import com.simplemobiletools.commons.dialogs.FilePickerDialog
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.helpers.LICENSE_KOTLIN
import com.simplemobiletools.commons.helpers.LICENSE_RTL
import com.simplemobiletools.commons.helpers.LICENSE_STETHO
import com.simplemobiletools.commons.models.Release
import com.simplemobiletools.notes.BuildConfig
import com.simplemobiletools.notes.R
import com.simplemobiletools.notes.adapters.NotesPagerAdapter
import com.simplemobiletools.notes.dialogs.NewNoteDialog
import com.simplemobiletools.notes.dialogs.OpenNoteDialog
import com.simplemobiletools.notes.dialogs.RenameNoteDialog
import com.simplemobiletools.notes.dialogs.SaveAsDialog
import com.simplemobiletools.notes.extensions.config
import com.simplemobiletools.notes.extensions.getTextSize
import com.simplemobiletools.notes.helpers.DBHelper
import com.simplemobiletools.notes.helpers.TYPE_NOTE
import com.simplemobiletools.notes.models.Note
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.nio.charset.Charset

class MainActivity : SimpleActivity(), ViewPager.OnPageChangeListener {
    val STORAGE_OPEN_FILE = 1
    val STORAGE_SAVE_AS_FILE = 2

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
        checkWhatsNewDialog()
        storeStoragePaths()
    }

    private fun initViewPager() {
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
        pager_title_strip.apply {
            setTextSize(TypedValue.COMPLEX_UNIT_PX, getTextSize())
            setGravity(Gravity.CENTER_VERTICAL)
            setNonPrimaryAlpha(0.4f)
            setTextColor(config.textColor)
        }
        updateTextColors(view_pager)
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
            R.id.open_note -> displayOpenNoteDialog()
            R.id.new_note -> displayNewNoteDialog()
            R.id.rename_note -> displayRenameDialog()
            R.id.share -> shareText()
            R.id.open_file -> tryOpenFile()
            R.id.save_as_file -> trySaveAsFile()
            R.id.delete_note -> displayDeleteNotePrompt()
            R.id.settings -> startActivity(Intent(applicationContext, SettingsActivity::class.java))
            R.id.about -> launchAbout()
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

    private fun displayNewNoteDialog() {
        NewNoteDialog(this, mDb) {
            val newNote = Note(0, it, "", TYPE_NOTE)
            addNewNote(newNote)
        }
    }

    private fun addNewNote(note: Note) {
        val id = mDb.insertNote(note)
        mNotes = mDb.getNotes()
        invalidateOptionsMenu()
        initViewPager()
        updateSelectedNote(id)
        mAdapter.showKeyboard(getNoteIndexWithId(id))
    }

    private fun launchAbout() {
        startAboutActivity(R.string.app_name, LICENSE_KOTLIN or LICENSE_STETHO or LICENSE_RTL, BuildConfig.VERSION_NAME)
    }

    private fun tryOpenFile() {
        if (hasReadStoragePermission()) {
            openFile()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), STORAGE_OPEN_FILE)
        }
    }

    private fun openFile() {
        FilePickerDialog(this) {
            val file = File(it)
            if (file.isImageVideoGif()) {
                toast(R.string.invalid_file_format)
                return@FilePickerDialog
            }

            if (file.length() > 10 * 1000 * 1000) {
                toast(R.string.file_too_large)
            } else {
                val filename = it.getFilenameFromPath()
                if (mDb.doesTitleExist(filename)) {
                    toast(R.string.title_taken)
                } else {
                    val content = file.readText()
                    val note = Note(0, filename, content, TYPE_NOTE, it)
                    addNewNote(note)
                }
            }
        }
    }

    private fun trySaveAsFile() {
        if (hasWriteStoragePermission()) {
            saveAsFile()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), STORAGE_SAVE_AS_FILE)
        }
    }

    private fun saveAsFile() {
        SaveAsDialog(this, mCurrentNote.title) {
            val file = File(it)
            if (file.isDirectory) {
                toast(R.string.directory_exists)
                return@SaveAsDialog
            }

            val text = getCurrentNoteText()
            if (needsStupidWritePermissions(it)) {
                if (isShowingPermDialog(file))
                    return@SaveAsDialog

                var document = getFileDocument(it, config.treeUri) ?: return@SaveAsDialog
                if (!file.exists()) {
                    document = document.createFile("", file.name)
                }
                contentResolver.openOutputStream(document.uri).apply {
                    write(text.toByteArray(Charset.forName("UTF-8")))
                    flush()
                    close()
                }
            } else {
                file.printWriter().use { out ->
                    out.write(text)
                }
            }
            toast(R.string.file_saved)
        }
    }

    private fun getCurrentNoteText() = (view_pager.adapter as NotesPagerAdapter).getCurrentNoteText(view_pager.currentItem)

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
        val text = getCurrentNoteText()
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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (requestCode == STORAGE_OPEN_FILE) {
                openFile()
            } else if (requestCode == STORAGE_SAVE_AS_FILE) {
                saveAsFile()
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
