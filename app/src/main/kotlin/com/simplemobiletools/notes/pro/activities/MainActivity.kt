package com.simplemobiletools.notes.pro.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.method.ArrowKeyMovementMethod
import android.text.method.LinkMovementMethod
import android.util.TypedValue
import android.view.ActionMode
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import com.simplemobiletools.commons.dialogs.ConfirmationAdvancedDialog
import com.simplemobiletools.commons.dialogs.FilePickerDialog
import com.simplemobiletools.commons.dialogs.RadioGroupDialog
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.helpers.LICENSE_RTL
import com.simplemobiletools.commons.helpers.PERMISSION_READ_STORAGE
import com.simplemobiletools.commons.helpers.PERMISSION_WRITE_STORAGE
import com.simplemobiletools.commons.helpers.REAL_FILE_PATH
import com.simplemobiletools.commons.models.FAQItem
import com.simplemobiletools.commons.models.FileDirItem
import com.simplemobiletools.commons.models.RadioItem
import com.simplemobiletools.commons.models.Release
import com.simplemobiletools.commons.views.MyEditText
import com.simplemobiletools.notes.pro.BuildConfig
import com.simplemobiletools.notes.pro.R
import com.simplemobiletools.notes.pro.adapters.NotesPagerAdapter
import com.simplemobiletools.notes.pro.databases.NotesDatabase
import com.simplemobiletools.notes.pro.dialogs.*
import com.simplemobiletools.notes.pro.extensions.*
import com.simplemobiletools.notes.pro.helpers.*
import com.simplemobiletools.notes.pro.models.Note
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.nio.charset.Charset

class MainActivity : SimpleActivity() {
    private val EXPORT_FILE_SYNC = 1
    private val EXPORT_FILE_NO_SYNC = 2

    private lateinit var mCurrentNote: Note
    private var mNotes = ArrayList<Note>()
    private var mAdapter: NotesPagerAdapter? = null
    private var noteViewWithTextSelected: MyEditText? = null
    private var saveNoteButton: MenuItem? = null

    private var wasInit = false
    private var storedEnableLineWrap = true
    private var showSaveButton = false
    private var showUndoButton = false
    private var showRedoButton = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        appLaunched(BuildConfig.APPLICATION_ID)

        initViewPager(intent.getLongExtra(OPEN_NOTE_ID, -1L))
        pager_title_strip.setTextSize(TypedValue.COMPLEX_UNIT_PX, getTextSize())
        pager_title_strip.layoutParams.height = (pager_title_strip.height + resources.getDimension(R.dimen.activity_margin) * 2).toInt()
        checkWhatsNewDialog()
        checkIntents(intent)

        storeStateVariables()
        if (config.showNotePicker && savedInstanceState == null) {
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

    override fun onDestroy() {
        super.onDestroy()
        if (!isChangingConfigurations) {
            NotesDatabase.destroyInstance()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        menu.apply {
            findItem(R.id.undo).isVisible = showUndoButton && mCurrentNote.type == TYPE_TEXT
            findItem(R.id.redo).isVisible = showRedoButton && mCurrentNote.type == TYPE_TEXT
        }

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
            saveNoteButton!!.isVisible = !config.autosaveNotes && showSaveButton && mCurrentNote.type == TYPE_TEXT
        }

        pager_title_strip.beVisibleIf(shouldBeVisible)
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (config.autosaveNotes) {
            saveCurrentNote(false)
        }

        when (item.itemId) {
            R.id.open_note -> displayOpenNoteDialog()
            R.id.save_note -> saveNote()
            R.id.undo -> undo()
            R.id.redo -> redo()
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

    override fun onBackPressed() {
        if (!config.autosaveNotes && mAdapter?.anyHasUnsavedChanges() == true) {
            ConfirmationAdvancedDialog(this, "", R.string.unsaved_changes_warning, R.string.save, R.string.discard) {
                if (it) {
                    mAdapter?.saveAllFragmentTexts()
                }
                super.onBackPressed()
            }
        } else {
            super.onBackPressed()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val wantedNoteId = intent.getLongExtra(OPEN_NOTE_ID, -1L)
        view_pager.currentItem = getWantedNoteIndex(wantedNoteId)
        checkIntents(intent)
    }

    private fun checkIntents(intent: Intent) {
        intent.apply {
            if (action == Intent.ACTION_SEND && type == MIME_TEXT_PLAIN) {
                getStringExtra(Intent.EXTRA_TEXT)?.let {
                    handleTextIntent(it)
                    intent.removeExtra(Intent.EXTRA_TEXT)
                }
            }

            if (action == Intent.ACTION_VIEW) {
                val realPath = intent.getStringExtra(REAL_FILE_PATH)
                if (realPath != null) {
                    val file = File(realPath)
                    handleUri(Uri.fromFile(file))
                } else {
                    handleUri(data)
                }
                intent.removeCategory(Intent.CATEGORY_DEFAULT)
                intent.action = null
            }
        }
    }

    private fun storeStateVariables() {
        config.apply {
            storedEnableLineWrap = enableLineWrap
        }
    }

    private fun handleTextIntent(text: String) {
        NotesHelper(this).getNotes {
            val notes = it
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
                    updateSelectedNote(notes[it - 1].id!!)
                    addTextToCurrentNote(if (mCurrentNote.value.isEmpty()) text else "\n$text")
                }
            }
        }
    }

    private fun handleUri(uri: Uri) {
        NotesHelper(this).getNoteIdWithPath(uri.path) {
            if (it != null && it > 0L) {
                updateSelectedNote(it)
                return@getNoteIdWithPath
            }

            handlePermission(PERMISSION_WRITE_STORAGE) {
                if (it) {
                    NotesHelper(this).getNotes {
                        mNotes = it
                        importUri(uri)
                    }
                }
            }
        }
    }

    private fun initViewPager(wantedNoteId: Long? = null) {
        NotesHelper(this).getNotes {
            mNotes = it
            invalidateOptionsMenu()
            mCurrentNote = mNotes[0]
            mAdapter = NotesPagerAdapter(supportFragmentManager, mNotes, this)
            view_pager.apply {
                adapter = mAdapter
                currentItem = getWantedNoteIndex(wantedNoteId)
                config.currentNoteId = mCurrentNote.id!!

                onPageChangeListener {
                    mCurrentNote = mNotes[it]
                    config.currentNoteId = mCurrentNote.id!!
                    invalidateOptionsMenu()
                }
            }

            if (!config.showKeyboard || mCurrentNote.type == TYPE_CHECKLIST) {
                hideKeyboard()
            }
        }
    }

    private fun getWantedNoteIndex(wantedNoteId: Long?): Int {
        intent.removeExtra(OPEN_NOTE_ID)
        val noteIdToOpen = if (wantedNoteId == null || wantedNoteId == -1L) config.currentNoteId else wantedNoteId
        return getNoteIndexWithId(noteIdToOpen)
    }

    private fun currentNotesView() = if (view_pager == null) {
        null
    } else {
        mAdapter?.getCurrentNotesView(view_pager.currentItem)
    }

    private fun displayRenameDialog() {
        RenameNoteDialog(this, mCurrentNote, getCurrentNoteText()) {
            mCurrentNote = it
            initViewPager(mCurrentNote.id)
        }
    }

    private fun updateSelectedNote(id: Long) {
        config.currentNoteId = id
        if (mNotes.isEmpty()) {
            NotesHelper(this).getNotes {
                mNotes = it
                updateSelectedNote(id)
            }
        } else {
            val index = getNoteIndexWithId(id)
            view_pager.currentItem = index
            mCurrentNote = mNotes[index]
        }
    }

    private fun displayNewNoteDialog(value: String = "") {
        NewNoteDialog(this) {
            it.value = value
            addNewNote(it)
        }
    }

    private fun addNewNote(note: Note) {
        NotesHelper(this).insertOrUpdateNote(note) {
            val newNoteId = it
            showSaveButton = false
            initViewPager(newNoteId)
            updateSelectedNote(newNoteId)
            view_pager.onGlobalLayout {
                mAdapter?.focusEditText(getNoteIndexWithId(newNoteId))
            }
        }
    }

    private fun launchAbout() {
        val licenses = LICENSE_RTL

        val faqItems = arrayListOf(
                FAQItem(R.string.faq_1_title_commons, R.string.faq_1_text_commons),
                FAQItem(R.string.faq_4_title_commons, R.string.faq_4_text_commons),
                FAQItem(R.string.faq_2_title_commons, R.string.faq_2_text_commons),
                FAQItem(R.string.faq_6_title_commons, R.string.faq_6_text_commons)
        )

        startAboutActivity(R.string.app_name, licenses, BuildConfig.VERSION_NAME, faqItems, true)
    }

    private fun tryOpenFile() {
        handlePermission(PERMISSION_WRITE_STORAGE) {
            if (it) {
                openFile()
            }
        }
    }

    private fun openFile() {
        FilePickerDialog(this, canAddShowHiddenButton = true) {
            openFile(it, true) {
                Thread {
                    val fileText = it.readText().trim()
                    val checklistItems = fileText.parseChecklistItems()
                    if (checklistItems != null) {
                        val note = Note(null, it.absolutePath.getFilenameFromPath().substringBeforeLast('.'), fileText, TYPE_CHECKLIST)
                        addNewNote(note)
                    } else {
                        runOnUiThread {
                            OpenFileDialog(this, it.path) {
                                addNewNote(it)
                            }
                        }
                    }
                }.start()
            }
        }
    }

    private fun openFile(path: String, checkTitle: Boolean, onChecksPassed: (file: File) -> Unit) {
        val file = File(path)
        if (path.isMediaFile()) {
            toast(R.string.invalid_file_format)
        } else if (file.length() > 10 * 1000 * 1000) {
            toast(R.string.file_too_large)
        } else if (checkTitle && mNotes.any { it.title.equals(path.getFilenameFromPath(), true) }) {
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

    private fun importUri(uri: Uri) {
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
            val title = path.getFilenameFromPath()
            val fileText = it.readText().trim()
            val checklistItems = fileText.parseChecklistItems()
            val note = if (checklistItems != null) {
                Note(null, title.substringBeforeLast('.'), fileText, TYPE_CHECKLIST)
            } else {
                Note(null, title, "", TYPE_TEXT, path)
            }

            if (mNotes.any { it.title.equals(note.title, true) }) {
                note.title += " (file)"
            }

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
        FilePickerDialog(this, pickFile = false, canAddShowHiddenButton = true) {
            openFolder(it) {
                ImportFolderDialog(this, it.path) {
                    NotesHelper(this).getNotes {
                        mNotes = it
                        showSaveButton = false
                        initViewPager()
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
            val textToExport = if (mCurrentNote.type == TYPE_TEXT) getCurrentNoteText() else mCurrentNote.value
            if (textToExport == null || textToExport.isEmpty()) {
                toast(R.string.unknown_error_occurred)
            } else if (mCurrentNote.type == TYPE_TEXT) {
                showExportFilePickUpdateDialog(it, textToExport)
            } else {
                tryExportNoteValueToFile(it, textToExport, true)
            }
        }
    }

    private fun showExportFilePickUpdateDialog(exportPath: String, textToExport: String) {
        val items = arrayListOf(
                RadioItem(EXPORT_FILE_SYNC, getString(R.string.update_file_at_note)),
                RadioItem(EXPORT_FILE_NO_SYNC, getString(R.string.only_export_file_content)))

        RadioGroupDialog(this, items) {
            val syncFile = it as Int == EXPORT_FILE_SYNC
            tryExportNoteValueToFile(exportPath, textToExport, true) {
                if (syncFile) {
                    mCurrentNote.path = exportPath
                    mCurrentNote.value = ""
                } else {
                    mCurrentNote.path = ""
                    mCurrentNote.value = textToExport
                }

                getPagerAdapter().updateCurrentNoteData(view_pager.currentItem, mCurrentNote.path, mCurrentNote.value)
                NotesHelper(this).insertOrUpdateNote(mCurrentNote)
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
            val items = arrayListOf(
                    RadioItem(EXPORT_FILE_SYNC, getString(R.string.update_file_at_note)),
                    RadioItem(EXPORT_FILE_NO_SYNC, getString(R.string.only_export_file_content)))

            RadioGroupDialog(this, items) {
                val syncFile = it as Int == EXPORT_FILE_SYNC
                var failCount = 0
                NotesHelper(this).getNotes {
                    mNotes = it
                    mNotes.forEachIndexed { index, note ->
                        val filename = if (extension.isEmpty()) note.title else "${note.title}.$extension"
                        val file = File(parent, filename)
                        if (!filename.isAValidFilename()) {
                            toast(String.format(getString(R.string.filename_invalid_characters_placeholder, filename)))
                        } else {
                            val noteStoredValue = note.getNoteStoredValue() ?: ""
                            tryExportNoteValueToFile(file.absolutePath, note.value, false) {
                                if (syncFile) {
                                    note.path = file.absolutePath
                                    note.value = ""
                                } else {
                                    note.path = ""
                                    note.value = noteStoredValue
                                }

                                NotesHelper(this).insertOrUpdateNote(note)
                                if (mCurrentNote.id == note.id) {
                                    mCurrentNote.value = note.value
                                    mCurrentNote.path = note.path
                                    getPagerAdapter().updateCurrentNoteData(view_pager.currentItem, mCurrentNote.path, mCurrentNote.value)
                                }

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
        }
    }

    fun tryExportNoteValueToFile(path: String, content: String, showSuccessToasts: Boolean, callback: ((success: Boolean) -> Unit)? = null) {
        handlePermission(PERMISSION_WRITE_STORAGE) {
            if (it) {
                exportNoteValueToFile(path, content, showSuccessToasts, callback)
            }
        }
    }

    private fun exportNoteValueToFile(path: String, content: String, showSuccessToasts: Boolean, callback: ((success: Boolean) -> Unit)? = null) {
        try {
            if (File(path).isDirectory) {
                toast(R.string.name_taken)
                return
            }

            if (needsStupidWritePermissions(path)) {
                handleSAFDialog(path) {
                    var document = getDocumentFile(File(path).parent) ?: return@handleSAFDialog
                    if (!File(path).exists()) {
                        document = document.createFile("", path.getFilenameFromPath())!!
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

    private fun getPagerAdapter() = view_pager.adapter as NotesPagerAdapter

    private fun getCurrentNoteText() = getPagerAdapter().getCurrentNoteViewText(view_pager.currentItem)

    private fun addTextToCurrentNote(text: String) = getPagerAdapter().appendText(view_pager.currentItem, text)

    private fun saveCurrentNote(force: Boolean) = getPagerAdapter().saveCurrentNote(view_pager.currentItem, force)

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
        Thread {
            notesDB.deleteNote(note)
            widgetsDB.deleteNoteWidgets(note.id!!)
            refreshNotes(note, deleteFile)
        }.start()
    }

    private fun refreshNotes(note: Note, deleteFile: Boolean) {
        NotesHelper(this).getNotes {
            mNotes = it
            val firstNoteId = mNotes[0].id
            updateSelectedNote(firstNoteId!!)
            if (config.widgetNoteId == note.id) {
                config.widgetNoteId = mCurrentNote.id!!
                updateWidgets()
            }

            initViewPager()

            if (deleteFile) {
                deleteFile(FileDirItem(note.path, note.title)) {
                    if (!it) {
                        toast(R.string.unknown_error_occurred)
                    }
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
        saveCurrentNote(true)
        showSaveButton = false
        invalidateOptionsMenu()
    }

    private fun undo() {
        mAdapter?.undo(view_pager.currentItem)
    }

    private fun redo() {
        mAdapter?.redo(view_pager.currentItem)
    }

    private fun getNoteIndexWithId(id: Long): Int {
        for (i in 0 until mNotes.count()) {
            if (mNotes[i].id == id) {
                mCurrentNote = mNotes[i]
                return i
            }
        }
        return 0
    }

    private fun shareText() {
        val text = if (mCurrentNote.type == TYPE_TEXT) getCurrentNoteText() else mCurrentNote.value
        if (text == null || text.isEmpty()) {
            toast(R.string.cannot_share_empty_text)
            return
        }

        val res = resources
        val shareTitle = res.getString(R.string.share_via)
        Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_SUBJECT, mCurrentNote.title)
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
            startActivity(Intent.createChooser(this, shareTitle))
        }
    }

    fun currentNoteTextChanged(newText: String, showUndo: Boolean, showRedo: Boolean) {
        var shouldRecreateMenu = false
        if (showUndo != showUndoButton) {
            showUndoButton = showUndo
            shouldRecreateMenu = true
        }

        if (showRedo != showRedoButton) {
            showRedoButton = showRedo
            shouldRecreateMenu = true
        }

        if (!config.autosaveNotes) {
            showSaveButton = newText != mCurrentNote.value
            if (showSaveButton != saveNoteButton?.isVisible) {
                shouldRecreateMenu = true
            }
        }

        if (shouldRecreateMenu) {
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
            add(Release(49, R.string.release_49))
            add(Release(51, R.string.release_51))
            add(Release(57, R.string.release_57))
            checkWhatsNew(this, BuildConfig.VERSION_CODE)
        }
    }
}
