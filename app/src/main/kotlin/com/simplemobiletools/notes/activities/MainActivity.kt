package com.simplemobiletools.notes.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.view.ViewPager
import android.text.method.ArrowKeyMovementMethod
import android.text.method.LinkMovementMethod
import android.util.TypedValue
import android.view.ActionMode
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import com.simplemobiletools.commons.dialogs.FilePickerDialog
import com.simplemobiletools.commons.dialogs.RadioGroupDialog
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.helpers.*
import com.simplemobiletools.commons.models.FAQItem
import com.simplemobiletools.commons.models.FileDirItem
import com.simplemobiletools.commons.models.RadioItem
import com.simplemobiletools.commons.models.Release
import com.simplemobiletools.commons.views.MyEditText
import com.simplemobiletools.notes.BuildConfig
import com.simplemobiletools.notes.R
import com.simplemobiletools.notes.adapters.NotesPagerAdapter
import com.simplemobiletools.notes.dialogs.*
import com.simplemobiletools.notes.extensions.config
import com.simplemobiletools.notes.extensions.dbHelper
import com.simplemobiletools.notes.extensions.getTextSize
import com.simplemobiletools.notes.extensions.updateWidget
import com.simplemobiletools.notes.helpers.MIME_TEXT_PLAIN
import com.simplemobiletools.notes.helpers.OPEN_NOTE_ID
import com.simplemobiletools.notes.helpers.TYPE_NOTE
import com.simplemobiletools.notes.models.Note
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.nio.charset.Charset

class MainActivity : SimpleActivity(), ViewPager.OnPageChangeListener {
    private var mAdapter: NotesPagerAdapter? = null

    lateinit var mCurrentNote: Note
    private var mNotes = ArrayList<Note>()

    private var noteViewWithTextSelected: MyEditText? = null
    private var wasInit = false
    private var storedEnableLineWrap = true
    private var showSaveButton = false
    private var saveNoteButton: MenuItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        appLaunched(BuildConfig.APPLICATION_ID)

        initViewPager()

        pager_title_strip.setTextSize(TypedValue.COMPLEX_UNIT_PX, getTextSize())
        pager_title_strip.layoutParams.height = (pager_title_strip.height + resources.getDimension(R.dimen.activity_margin) * 2).toInt()
        checkWhatsNewDialog()

        intent.apply {
            if (action == Intent.ACTION_SEND && type == MIME_TEXT_PLAIN) {
                getStringExtra(Intent.EXTRA_TEXT)?.let {
                    handleText(it)
                    intent.removeExtra(Intent.EXTRA_TEXT)
                }
            }

            if (action == Intent.ACTION_VIEW) {
                handleUri(data)
                intent.removeCategory(Intent.CATEGORY_DEFAULT)
                intent.action = null
            }
        }

        storeStateVariables()
        if (config.showNotePicker) {
            displayOpenNoteDialog()
        }
        wasInit = true
        checkAppOnSDCard()
    }

    override fun onResume() {
        super.onResume()
        if (storedEnableLineWrap != config.enableLineWrap) {
            initViewPager()
        }

        invalidateOptionsMenu()
        pager_title_strip.apply {
            setTextSize(TypedValue.COMPLEX_UNIT_PX, getTextSize())
            setGravity(Gravity.CENTER_VERTICAL)
            setNonPrimaryAlpha(0.4f)
            setTextColor(config.textColor)
        }
        updateTextColors(view_pager)
    }

    override fun onPause() {
        super.onPause()
        storeStateVariables()
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
            findItem(R.id.export_all_notes).isVisible = shouldBeVisible

            saveNoteButton = findItem(R.id.save_note)
            saveNoteButton!!.isVisible = !config.autosaveNotes && showSaveButton
        }

        pager_title_strip.beVisibleIf(shouldBeVisible)
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (config.autosaveNotes) {
            saveCurrentNote()
        }

        when (item.itemId) {
            R.id.open_note -> displayOpenNoteDialog()
            R.id.save_note -> saveNote()
            R.id.new_note -> displayNewNoteDialog()
            R.id.rename_note -> displayRenameDialog()
            R.id.share -> shareText()
            R.id.open_file -> tryOpenFile()
            R.id.import_folder -> tryOpenFolder()
            R.id.export_as_file -> tryExportAsFile()
            R.id.export_all_notes -> tryExportAllNotes()
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
        if (wasInit) {
            currentNotesView()?.apply {
                if (config.clickableLinks || movementMethod is LinkMovementMethod) {
                    movementMethod = ArrowKeyMovementMethod.getInstance()
                    noteViewWithTextSelected = this
                }
            }
        }
    }

    override fun onActionModeFinished(mode: ActionMode?) {
        super.onActionModeFinished(mode)
        if (config.clickableLinks) {
            noteViewWithTextSelected?.movementMethod = LinkMovementMethod.getInstance()
        }
    }

    private fun storeStateVariables() {
        config.apply {
            storedEnableLineWrap = enableLineWrap
        }
    }

    private fun handleText(text: String) {
        val notes = dbHelper.getNotes()
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

    private fun handleUri(uri: Uri) {
        val id = dbHelper.getNoteId(uri.path)

        if (dbHelper.isValidId(id)) {
            updateSelectedNote(id)
            return
        }

        handlePermission(PERMISSION_WRITE_STORAGE) {
            if (it) {
                importFileWithSync(uri)
            }
        }
    }

    private fun initViewPager() {
        mNotes = dbHelper.getNotes()
        mCurrentNote = mNotes[0]
        var wantedNoteId = intent.getIntExtra(OPEN_NOTE_ID, -1)
        if (wantedNoteId == -1) {
            wantedNoteId = config.currentNoteId
        }

        val itemIndex = getNoteIndexWithId(wantedNoteId)

        mAdapter = NotesPagerAdapter(supportFragmentManager, mNotes, this)
        view_pager.apply {
            adapter = mAdapter
            currentItem = itemIndex
            addOnPageChangeListener(this@MainActivity)
        }

        if (!config.showKeyboard) {
            hideKeyboard()
        }
    }

    private fun currentNotesView() = if (view_pager == null) {
        null
    } else {
        mAdapter?.getCurrentNotesView(view_pager.currentItem)
    }

    private fun displayRenameDialog() {
        RenameNoteDialog(this, mCurrentNote) {
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
        NewNoteDialog(this, dbHelper) {
            val newNote = Note(0, it, value, TYPE_NOTE)
            addNewNote(newNote)
        }
    }

    private fun addNewNote(note: Note) {
        val id = dbHelper.insertNote(note)
        mNotes = dbHelper.getNotes()
        showSaveButton = false
        invalidateOptionsMenu()
        initViewPager()
        updateSelectedNote(id)
        view_pager.onGlobalLayout {
            mAdapter?.focusEditText(getNoteIndexWithId(id))
        }
    }

    private fun launchAbout() {
        val faqItems = arrayListOf(
                FAQItem(R.string.faq_1_title_commons, R.string.faq_1_text_commons),
                FAQItem(R.string.faq_4_title_commons, R.string.faq_4_text_commons),
                FAQItem(R.string.faq_2_title_commons, R.string.faq_2_text_commons)
        )
        startAboutActivity(R.string.app_name, LICENSE_STETHO or LICENSE_RTL or LICENSE_LEAK_CANARY, BuildConfig.VERSION_NAME, faqItems)
    }

    private fun tryOpenFile() {
        handlePermission(PERMISSION_WRITE_STORAGE) {
            if (it) {
                openFile()
            }
        }
    }

    private fun openFile() {
        FilePickerDialog(this) {
            openFile(it, true, {
                OpenFileDialog(this, it.path) {
                    addNewNote(it)
                }
            })
        }
    }

    private fun openFile(path: String, checkTitle: Boolean, onChecksPassed: (file: File) -> Unit) {
        val file = File(path)
        if (path.isImageVideoGif()) {
            toast(R.string.invalid_file_format)
        } else if (file.length() > 10 * 1000 * 1000) {
            toast(R.string.file_too_large)
        } else if (checkTitle && dbHelper.doesTitleExist(path.getFilenameFromPath())) {
            toast(R.string.title_taken)
        } else {
            onChecksPassed(file)
        }
    }

    private fun openFolder(path: String, onChecksPassed: (file: File) -> Unit) {
        val file = File(path)
        if (file.isDirectory) {
            onChecksPassed(file)
        }
    }

    private fun importFileWithSync(uri: Uri) {
        when (uri.scheme) {
            "file" -> openPath(uri.path)
            "content" -> {
                val realPath = getRealPathFromURI(uri)
                if (realPath != null) {
                    openPath(realPath)
                } else {
                    R.string.unknown_error_occurred
                }
            }
        }
    }

    private fun openPath(path: String) {
        openFile(path, false) {
            var title = path.getFilenameFromPath()
            if (dbHelper.doesTitleExist(title))
                title += " (file)"

            val note = Note(0, title, "", TYPE_NOTE, path)
            addNewNote(note)
        }
    }

    private fun tryOpenFolder() {
        handlePermission(PERMISSION_READ_STORAGE) {
            if (it) {
                openFolder()
            }
        }
    }

    private fun openFolder() {
        FilePickerDialog(this, pickFile = false) {
            openFolder(it) {
                ImportFolderDialog(this, it.path) {
                    mNotes = dbHelper.getNotes()
                    showSaveButton = false
                    invalidateOptionsMenu()
                    initViewPager()
                    updateSelectedNote(it)
                    view_pager.onGlobalLayout {
                        mAdapter?.focusEditText(getNoteIndexWithId(it))
                    }
                }
            }
        }
    }

    private fun tryExportAsFile() {
        handlePermission(PERMISSION_WRITE_STORAGE) {
            if (it) {
                exportAsFile()
            }
        }
    }

    private fun exportAsFile() {
        ExportFileDialog(this, mCurrentNote) {
            if (getCurrentNoteText()?.isNotEmpty() == true) {
                exportNoteValueToFile(it, getCurrentNoteText()!!, true)
            }
        }
    }

    private fun tryExportAllNotes() {
        handlePermission(PERMISSION_WRITE_STORAGE) {
            if (it) {
                exportAllNotes()
            }
        }
    }

    private fun exportAllNotes() {
        ExportFilesDialog(this, mNotes) { parent, extension ->
            var failCount = 0
            mNotes = dbHelper.getNotes()
            mNotes.forEachIndexed { index, note ->
                val filename = if (extension.isEmpty()) note.title else "${note.title}.$extension"
                val file = File(parent, filename)
                if (!filename.isAValidFilename()) {
                    toast(String.format(getString(R.string.filename_invalid_characters_placeholder, filename)))
                } else {
                    exportNoteValueToFile(file.absolutePath, note.value, false) {
                        if (!it) {
                            failCount++
                        }

                        if (index == mNotes.size - 1) {
                            toast(if (failCount == 0) R.string.exporting_successful else R.string.exporting_some_entries_failed)
                        }
                    }
                }
            }
        }
    }

    fun exportNoteValueToFile(path: String, content: String, showSuccessToasts: Boolean, callback: ((success: Boolean) -> Unit)? = null) {
        try {
            if (getIsPathDirectory(path)) {
                toast(R.string.name_taken)
                return
            }

            if (needsStupidWritePermissions(path)) {
                handleSAFDialog(path) {
                    var document = getDocumentFile(path) ?: return@handleSAFDialog
                    if (!getDoesFilePathExist(path)) {
                        document = document.createFile("", path.getFilenameFromPath())
                    }
                    contentResolver.openOutputStream(document.uri).apply {
                        write(content.toByteArray(Charset.forName("UTF-8")), 0, content.length)
                        flush()
                        close()
                    }
                    if (showSuccessToasts) {
                        noteExportedSuccessfully(path.getFilenameFromPath())
                    }
                    callback?.invoke(true)
                }
            } else {
                val file = File(path)
                file.printWriter().use { out ->
                    out.write(content)
                }
                if (showSuccessToasts) {
                    noteExportedSuccessfully(path.getFilenameFromPath())
                }
                callback?.invoke(true)
            }
        } catch (e: Exception) {
            showErrorToast(e)
            callback?.invoke(false)
        }
    }

    private fun noteExportedSuccessfully(title: String) {
        val message = String.format(getString(R.string.note_exported_successfully), title)
        toast(message)
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
        if (mNotes.size <= 1) {
            return
        }

        if (!deleteFile) {
            doDeleteNote(mCurrentNote, deleteFile)
        } else {
            handleSAFDialog(mCurrentNote.path) {
                doDeleteNote(mCurrentNote, deleteFile)
            }
        }
    }

    private fun doDeleteNote(note: Note, deleteFile: Boolean) {
        dbHelper.deleteNote(mCurrentNote.id)
        mNotes = dbHelper.getNotes()

        val firstNoteId = mNotes[0].id
        updateSelectedNote(firstNoteId)
        if (config.widgetNoteId == note.id) {
            config.widgetNoteId = mCurrentNote.id
            updateWidget()
        }
        invalidateOptionsMenu()
        initViewPager()

        if (deleteFile) {
            deleteFile(FileDirItem(note.path, note.title)) {
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

    private fun saveNote() {
        saveCurrentNote()
        showSaveButton = false
        invalidateOptionsMenu()
    }

    private fun getNoteIndexWithId(id: Int): Int {
        for (i in 0 until mNotes.count()) {
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

    override fun onPageScrollStateChanged(state: Int) {
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
    }

    override fun onPageSelected(position: Int) {
        mCurrentNote = mNotes[position]
        config.currentNoteId = mCurrentNote.id
    }

    fun currentNoteTextChanged(newText: String) {
        showSaveButton = newText != mCurrentNote.value
        if (showSaveButton != saveNoteButton?.isVisible) {
            invalidateOptionsMenu()
        }
    }

    private fun checkWhatsNewDialog() {
        arrayListOf<Release>().apply {
            add(Release(25, R.string.release_25))
            add(Release(28, R.string.release_28))
            add(Release(29, R.string.release_29))
            add(Release(39, R.string.release_39))
            add(Release(45, R.string.release_45))
            checkWhatsNew(this, BuildConfig.VERSION_CODE)
        }
    }
}
