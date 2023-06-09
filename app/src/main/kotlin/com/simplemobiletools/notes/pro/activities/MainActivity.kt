package com.simplemobiletools.notes.pro.activities

import android.accounts.NetworkErrorException
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.graphics.drawable.LayerDrawable
import android.net.Uri
import android.os.Bundle
import android.print.PrintAttributes
import android.print.PrintManager
import android.text.method.ArrowKeyMovementMethod
import android.text.method.LinkMovementMethod
import android.util.TypedValue
import android.view.ActionMode
import android.view.Gravity
import android.view.MenuItem
import android.view.inputmethod.EditorInfo
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.TooltipCompat
import com.simplemobiletools.commons.dialogs.*
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.helpers.*
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
import com.simplemobiletools.notes.pro.fragments.TextFragment
import com.simplemobiletools.notes.pro.helpers.*
import com.simplemobiletools.notes.pro.helpers.NotesImporter.ImportResult
import com.simplemobiletools.notes.pro.models.Note
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.item_checklist.*
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.nio.charset.Charset
import java.util.*

class MainActivity : SimpleActivity() {
    private val EXPORT_FILE_SYNC = 1
    private val EXPORT_FILE_NO_SYNC = 2

    private val IMPORT_FILE_SYNC = 1
    private val IMPORT_FILE_NO_SYNC = 2

    private val PICK_OPEN_FILE_INTENT = 1
    private val PICK_EXPORT_FILE_INTENT = 2

    private val PICK_IMPORT_NOTES_INTENT = 3
    private val PICK_EXPORT_NOTES_INTENT = 4

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
    private var searchIndex = 0
    private var searchMatches = emptyList<Int>()
    private var isSearchActive = false

    private lateinit var searchQueryET: MyEditText
    private lateinit var searchPrevBtn: ImageView
    private lateinit var searchNextBtn: ImageView
    private lateinit var searchClearBtn: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        isMaterialActivity = true
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        appLaunched(BuildConfig.APPLICATION_ID)
        setupOptionsMenu()
        refreshMenuItems()

        updateMaterialActivityViews(main_coordinator, null, useTransparentNavigation = false, useTopSearchMenu = false)

        searchQueryET = findViewById(R.id.search_query)
        searchPrevBtn = findViewById(R.id.search_previous)
        searchNextBtn = findViewById(R.id.search_next)
        searchClearBtn = findViewById(R.id.search_clear)

        initViewPager(intent.getLongExtra(OPEN_NOTE_ID, -1L))
        pager_tab_strip.drawFullUnderline = false
        pager_tab_strip.setTextSize(TypedValue.COMPLEX_UNIT_PX, getPercentageFontSize())
        pager_tab_strip.layoutParams.height =
            (pager_tab_strip.height + resources.getDimension(R.dimen.activity_margin) * 2 * (config.fontSizePercentage / 100f)).toInt()
        checkWhatsNewDialog()
        checkIntents(intent)

        storeStateVariables()
        if (config.showNotePicker && savedInstanceState == null) {
            displayOpenNoteDialog()
        }

        wasInit = true

        checkAppOnSDCard()
        setupSearchButtons()

        if (isPackageInstalled("com.simplemobiletools.notes")) {
            val dialogText = getString(R.string.upgraded_from_free_notes)
            ConfirmationDialog(this, dialogText, 0, R.string.ok, 0, false) {}
        }
    }

    override fun onResume() {
        super.onResume()
        setupToolbar(main_toolbar)
        if (storedEnableLineWrap != config.enableLineWrap) {
            initViewPager()
        }

        refreshMenuItems()
        pager_tab_strip.apply {
            setTextSize(TypedValue.COMPLEX_UNIT_PX, getPercentageFontSize())
            setGravity(Gravity.CENTER_VERTICAL)
            setNonPrimaryAlpha(0.4f)
            setTextColor(getProperPrimaryColor())
            tabIndicatorColor = getProperPrimaryColor()
        }
        updateTextColors(view_pager)

        checkShortcuts()

        search_wrapper.setBackgroundColor(getProperStatusBarColor())
        val contrastColor = getProperPrimaryColor().getContrastColor()
        arrayListOf(searchPrevBtn, searchNextBtn, searchClearBtn).forEach {
            it.applyColorFilter(contrastColor)
        }

        updateTopBarColors(main_toolbar, getProperBackgroundColor())
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

    private fun refreshMenuItems() {
        val multipleNotesExist = mNotes.size > 1
        val isCurrentItemChecklist = isCurrentItemChecklist()

        main_toolbar.menu.apply {
            findItem(R.id.undo).apply {
                isVisible = showUndoButton && mCurrentNote.type == NoteType.TYPE_TEXT.value
                icon?.alpha = if (isEnabled) 255 else 127
            }

            findItem(R.id.redo).apply {
                isVisible = showRedoButton && mCurrentNote.type == NoteType.TYPE_TEXT.value
                icon?.alpha = if (isEnabled) 255 else 127
            }

            findItem(R.id.rename_note).isVisible = multipleNotesExist
            findItem(R.id.open_note).isVisible = multipleNotesExist
            findItem(R.id.delete_note).isVisible = multipleNotesExist
            findItem(R.id.export_all_notes).isVisible = multipleNotesExist
            findItem(R.id.open_search).isVisible = !isCurrentItemChecklist
            findItem(R.id.remove_done_items).isVisible = isCurrentItemChecklist
            findItem(R.id.sort_checklist).isVisible = isCurrentItemChecklist
            findItem(R.id.import_folder).isVisible = !isQPlus()
            findItem(R.id.import_notes).isVisible = isQPlus()
            findItem(R.id.lock_note).isVisible = mNotes.isNotEmpty() && (::mCurrentNote.isInitialized && !mCurrentNote.isLocked())
            findItem(R.id.unlock_note).isVisible = mNotes.isNotEmpty() && (::mCurrentNote.isInitialized && mCurrentNote.isLocked())
            findItem(R.id.more_apps_from_us).isVisible = !resources.getBoolean(R.bool.hide_google_relations)

            saveNoteButton = findItem(R.id.save_note)
            saveNoteButton!!.isVisible =
                !config.autosaveNotes && showSaveButton && (::mCurrentNote.isInitialized && mCurrentNote.type == NoteType.TYPE_TEXT.value)
        }

        pager_tab_strip.beVisibleIf(multipleNotesExist)
    }

    private fun setupOptionsMenu() {
        main_toolbar.setOnMenuItemClickListener { menuItem ->
            if (config.autosaveNotes && menuItem.itemId != R.id.undo && menuItem.itemId != R.id.redo) {
                saveCurrentNote(false)
            }

            val fragment = getCurrentFragment()
            when (menuItem.itemId) {
                R.id.open_search -> fragment?.handleUnlocking { openSearch() }
                R.id.open_note -> displayOpenNoteDialog()
                R.id.save_note -> fragment?.handleUnlocking { saveNote() }
                R.id.undo -> undo()
                R.id.redo -> redo()
                R.id.new_note -> displayNewNoteDialog()
                R.id.rename_note -> fragment?.handleUnlocking { displayRenameDialog() }
                R.id.share -> fragment?.handleUnlocking { shareText() }
                R.id.cab_create_shortcut -> createShortcut()
                R.id.lock_note -> lockNote()
                R.id.unlock_note -> unlockNote()
                R.id.open_file -> tryOpenFile()
                R.id.import_folder -> openFolder()
                R.id.export_as_file -> fragment?.handleUnlocking { tryExportAsFile() }
                R.id.export_all_notes -> tryExportNotes()
                R.id.import_notes -> tryImportNotes()
                R.id.print -> fragment?.handleUnlocking { printText() }
                R.id.delete_note -> fragment?.handleUnlocking { displayDeleteNotePrompt() }
                R.id.more_apps_from_us -> launchMoreAppsFromUsIntent()
                R.id.settings -> launchSettings()
                R.id.about -> launchAbout()
                R.id.remove_done_items -> fragment?.handleUnlocking { removeDoneItems() }
                R.id.sort_checklist -> fragment?.handleUnlocking { displaySortChecklistDialog() }
                else -> return@setOnMenuItemClickListener false
            }
            return@setOnMenuItemClickListener true
        }
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
        } else if (isSearchActive) {
            closeSearch()
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)
        if (requestCode == PICK_OPEN_FILE_INTENT && resultCode == RESULT_OK && resultData != null && resultData.data != null) {
            importUri(resultData.data!!)
        } else if (requestCode == PICK_EXPORT_FILE_INTENT && resultCode == Activity.RESULT_OK && resultData != null && resultData.data != null && mNotes.isNotEmpty()) {
            val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            applicationContext.contentResolver.takePersistableUriPermission(resultData.data!!, takeFlags)
            showExportFilePickUpdateDialog(resultData.dataString!!, getCurrentNoteValue())
        } else if (requestCode == PICK_EXPORT_NOTES_INTENT && resultCode == Activity.RESULT_OK && resultData != null && resultData.data != null) {
            val outputStream = contentResolver.openOutputStream(resultData.data!!)
            exportNotesTo(outputStream)
        } else if (requestCode == PICK_IMPORT_NOTES_INTENT && resultCode == Activity.RESULT_OK && resultData != null && resultData.data != null) {
            tryImportingAsJson(resultData.data!!)
        }
    }

    private fun isCurrentItemChecklist() = if (::mCurrentNote.isInitialized) mCurrentNote.type == NoteType.TYPE_CHECKLIST.value else false

    @SuppressLint("NewApi")
    private fun checkShortcuts() {
        val appIconColor = config.appIconColor
        if (isNougatMR1Plus() && config.lastHandledShortcutColor != appIconColor) {
            val newTextNote = getNewTextNoteShortcut(appIconColor)
            val newChecklist = getNewChecklistShortcut(appIconColor)

            try {
                shortcutManager.dynamicShortcuts = Arrays.asList(newTextNote, newChecklist)
                config.lastHandledShortcutColor = appIconColor
            } catch (ignored: Exception) {
            }
        }
    }

    @SuppressLint("NewApi")
    private fun getNewTextNoteShortcut(appIconColor: Int): ShortcutInfo {
        val shortLabel = getString(R.string.text_note)
        val longLabel = getString(R.string.new_text_note)
        val drawable = resources.getDrawable(R.drawable.shortcut_plus)
        (drawable as LayerDrawable).findDrawableByLayerId(R.id.shortcut_plus_background).applyColorFilter(appIconColor)
        val bmp = drawable.convertToBitmap()

        val intent = Intent(this, MainActivity::class.java)
        intent.action = Intent.ACTION_VIEW
        intent.putExtra(NEW_TEXT_NOTE, true)
        return ShortcutInfo.Builder(this, SHORTCUT_NEW_TEXT_NOTE)
            .setShortLabel(shortLabel)
            .setLongLabel(longLabel)
            .setIcon(Icon.createWithBitmap(bmp))
            .setIntent(intent)
            .build()
    }

    @SuppressLint("NewApi")
    private fun getNewChecklistShortcut(appIconColor: Int): ShortcutInfo {
        val shortLabel = getString(R.string.checklist)
        val longLabel = getString(R.string.new_checklist)
        val drawable = resources.getDrawable(R.drawable.shortcut_check)
        (drawable as LayerDrawable).findDrawableByLayerId(R.id.shortcut_plus_background).applyColorFilter(appIconColor)
        val bmp = drawable.convertToBitmap()

        val intent = Intent(this, MainActivity::class.java)
        intent.action = Intent.ACTION_VIEW
        intent.putExtra(NEW_CHECKLIST, true)
        return ShortcutInfo.Builder(this, SHORTCUT_NEW_CHECKLIST)
            .setShortLabel(shortLabel)
            .setLongLabel(longLabel)
            .setIcon(Icon.createWithBitmap(bmp))
            .setIntent(intent)
            .build()
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
                val isFromHistory = intent.flags and Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY != 0
                if (!isFromHistory) {
                    if (realPath != null && hasPermission(PERMISSION_READ_STORAGE)) {
                        val file = File(realPath)
                        handleUri(Uri.fromFile(file))
                    } else if (intent.getBooleanExtra(NEW_TEXT_NOTE, false)) {
                        val newTextNote = Note(null, getCurrentFormattedDateTime(), "", NoteType.TYPE_TEXT.value, "", PROTECTION_NONE, "")
                        addNewNote(newTextNote)
                    } else if (intent.getBooleanExtra(NEW_CHECKLIST, false)) {
                        val newChecklist = Note(null, getCurrentFormattedDateTime(), "", NoteType.TYPE_CHECKLIST.value, "", PROTECTION_NONE, "")
                        addNewNote(newChecklist)
                    } else {
                        handleUri(data!!)
                    }
                }
                intent.removeCategory(Intent.CATEGORY_DEFAULT)
                intent.action = null
                intent.removeExtra(NEW_CHECKLIST)
                intent.removeExtra(NEW_TEXT_NOTE)
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
        NotesHelper(this).getNoteIdWithPath(uri.path!!) {
            if (it != null && it > 0L) {
                updateSelectedNote(it)
                return@getNoteIdWithPath
            }

            NotesHelper(this).getNotes {
                mNotes = it
                importUri(uri)
            }
        }
    }

    private fun initViewPager(wantedNoteId: Long? = null) {
        NotesHelper(this).getNotes { notes ->
            notes.filter { it.shouldBeUnlocked(this) }
                .forEach(::removeProtection)

            mNotes = notes
            mCurrentNote = mNotes[0]
            mAdapter = NotesPagerAdapter(supportFragmentManager, mNotes, this)
            view_pager.apply {
                adapter = mAdapter
                currentItem = getWantedNoteIndex(wantedNoteId)
                config.currentNoteId = mCurrentNote.id!!

                onPageChangeListener {
                    mCurrentNote = mNotes[it]
                    config.currentNoteId = mCurrentNote.id!!
                    refreshMenuItems()
                }
            }

            if (!config.showKeyboard || mCurrentNote.type == NoteType.TYPE_CHECKLIST.value) {
                hideKeyboard()
            }
            refreshMenuItems()
        }
    }

    private fun setupSearchButtons() {
        searchQueryET.onTextChangeListener {
            searchTextChanged(it)
        }

        searchPrevBtn.run {
            setOnClickListener {
                goToPrevSearchResult()
            }
            contentDescription = getString(R.string.previous)
            TooltipCompat.setTooltipText(this, getString(R.string.previous))
        }

        searchNextBtn.run {
            setOnClickListener {
                goToNextSearchResult()
            }
            contentDescription = getString(R.string.next)
            TooltipCompat.setTooltipText(this, getString(R.string.next))
        }

        searchClearBtn.run {
            setOnClickListener {
                closeSearch()
            }
            contentDescription = getString(R.string.cancel)
            TooltipCompat.setTooltipText(this, getString(R.string.cancel))
        }

        view_pager.onPageChangeListener {
            currentTextFragment?.removeTextWatcher()
            currentNotesView()?.let { noteView ->
                noteView.text!!.clearBackgroundSpans()
            }

            closeSearch()
            currentTextFragment?.setTextWatcher()
        }

        searchQueryET.setOnEditorActionListener(TextView.OnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                searchNextBtn.performClick()
                return@OnEditorActionListener true
            }

            false
        })
    }

    private fun searchTextChanged(text: String) {
        currentNotesView()?.let { noteView ->
            currentTextFragment?.removeTextWatcher()
            noteView.text!!.clearBackgroundSpans()

            if (text.isNotBlank() && text.length > 1) {
                searchMatches = noteView.value.searchMatches(text)
                noteView.highlightText(text, getProperPrimaryColor())
            }

            currentTextFragment?.setTextWatcher()

            if (searchMatches.isNotEmpty()) {
                noteView.requestFocus()
                noteView.setSelection(searchMatches.getOrNull(searchIndex) ?: 0)
            }

            searchQueryET.postDelayed({
                searchQueryET.requestFocus()
            }, 50)
        }
    }

    private fun goToPrevSearchResult() {
        currentNotesView()?.let { noteView ->
            if (searchIndex > 0) {
                searchIndex--
            } else {
                searchIndex = searchMatches.lastIndex
            }

            selectSearchMatch(noteView)
        }
    }

    private fun goToNextSearchResult() {
        currentNotesView()?.let { noteView ->
            if (searchIndex < searchMatches.lastIndex) {
                searchIndex++
            } else {
                searchIndex = 0
            }

            selectSearchMatch(noteView)
        }
    }

    private fun getCurrentFragment() = mAdapter?.getFragment(view_pager.currentItem)

    private val currentTextFragment: TextFragment? get() = mAdapter?.textFragment(view_pager.currentItem)

    private fun selectSearchMatch(editText: MyEditText) {
        if (searchMatches.isNotEmpty()) {
            editText.requestFocus()
            editText.setSelection(searchMatches.getOrNull(searchIndex) ?: 0)
        } else {
            hideKeyboard()
        }
    }

    private fun openSearch() {
        isSearchActive = true
        search_wrapper.fadeIn()
        showKeyboard(searchQueryET)

        currentNotesView()?.let { noteView ->
            noteView.requestFocus()
            noteView.setSelection(0)
        }

        searchQueryET.postDelayed({
            searchQueryET.requestFocus()
        }, 250)
    }

    private fun closeSearch() {
        searchQueryET.text?.clear()
        isSearchActive = false
        search_wrapper.fadeOut()
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

    private fun displayNewNoteDialog(value: String = "", title: String? = null, path: String = "", setChecklistAsDefault: Boolean = false) {
        NewNoteDialog(this, title, setChecklistAsDefault) {
            it.value = value
            it.path = path
            addNewNote(it)
        }
    }

    private fun addNewNote(note: Note) {
        NotesHelper(this).insertOrUpdateNote(note) {
            val newNoteId = it
            showSaveButton = false
            showUndoButton = false
            showRedoButton = false
            initViewPager(newNoteId)
            updateSelectedNote(newNoteId)
            view_pager.onGlobalLayout {
                mAdapter?.focusEditText(getNoteIndexWithId(newNoteId))
            }
        }
    }

    private fun launchSettings() {
        hideKeyboard()
        startActivity(Intent(applicationContext, SettingsActivity::class.java))
    }

    private fun launchAbout() {
        val licenses = LICENSE_RTL

        val faqItems = arrayListOf(
            FAQItem(R.string.faq_1_title_commons, R.string.faq_1_text_commons),
            FAQItem(R.string.faq_1_title, R.string.faq_1_text)
        )

        if (!resources.getBoolean(R.bool.hide_google_relations)) {
            faqItems.add(FAQItem(R.string.faq_2_title_commons, R.string.faq_2_text_commons))
            faqItems.add(FAQItem(R.string.faq_6_title_commons, R.string.faq_6_text_commons))
            faqItems.add(FAQItem(R.string.faq_7_title_commons, R.string.faq_7_text_commons))
            faqItems.add(FAQItem(R.string.faq_10_title_commons, R.string.faq_10_text_commons))
        }

        startAboutActivity(R.string.app_name, licenses, BuildConfig.VERSION_NAME, faqItems, true)
    }

    private fun tryOpenFile() {
        hideKeyboard()
        if (hasPermission(PERMISSION_READ_STORAGE)) {
            openFile()
        } else {
            Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "text/*"

                try {
                    startActivityForResult(this, PICK_OPEN_FILE_INTENT)
                } catch (e: ActivityNotFoundException) {
                    toast(R.string.system_service_disabled, Toast.LENGTH_LONG)
                } catch (e: Exception) {
                    showErrorToast(e)
                }
            }
        }
    }

    private fun openFile() {
        FilePickerDialog(this, canAddShowHiddenButton = true) {
            checkFile(it, true) {
                ensureBackgroundThread {
                    val fileText = it.readText().trim()
                    val checklistItems = fileText.parseChecklistItems()
                    if (checklistItems != null) {
                        val title = it.absolutePath.getFilenameFromPath().substringBeforeLast('.')
                        val note = Note(null, title, fileText, NoteType.TYPE_CHECKLIST.value, "", PROTECTION_NONE, "")
                        runOnUiThread {
                            OpenFileDialog(this, it.path) {
                                displayNewNoteDialog(note.value, title = it.title, it.path, setChecklistAsDefault = true)
                            }
                        }
                    } else {
                        runOnUiThread {
                            OpenFileDialog(this, it.path) {
                                displayNewNoteDialog(it.value, title = it.title, it.path)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun checkFile(path: String, checkTitle: Boolean, onChecksPassed: (file: File) -> Unit) {
        val file = File(path)
        if (path.isMediaFile()) {
            toast(R.string.invalid_file_format)
        } else if (file.length() > 1000 * 1000) {
            toast(R.string.file_too_large)
        } else if (checkTitle && mNotes.any { it.title.equals(path.getFilenameFromPath(), true) }) {
            toast(R.string.title_taken)
        } else {
            onChecksPassed(file)
        }
    }

    private fun checkUri(uri: Uri, onChecksPassed: () -> Unit) {
        val inputStream = try {
            contentResolver.openInputStream(uri) ?: return
        } catch (e: Exception) {
            showErrorToast(e)
            return
        }

        if (inputStream.available() > 1000 * 1000) {
            toast(R.string.file_too_large)
        } else {
            onChecksPassed()
        }
    }

    private fun openFolder(path: String, onChecksPassed: (file: File) -> Unit) {
        val file = File(path)
        if (file.isDirectory) {
            onChecksPassed(file)
        }
    }

    private fun importUri(uri: Uri) {
        tryImportingAsJson(uri, force = true, showToasts = false) { success ->
            if (success) {
                return@tryImportingAsJson
            }

            when (uri.scheme) {
                "file" -> openPath(uri.path!!)
                "content" -> {
                    val realPath = getRealPathFromURI(uri)
                    if (hasPermission(PERMISSION_READ_STORAGE)) {
                        if (realPath != null) {
                            openPath(realPath)
                        } else {
                            R.string.unknown_error_occurred
                        }
                    } else if (realPath != null && realPath != "") {
                        checkFile(realPath, false) {
                            addNoteFromUri(uri, realPath.getFilenameFromPath())
                        }
                    } else {
                        checkUri(uri) {
                            addNoteFromUri(uri)
                        }
                    }
                }
            }
        }
    }

    private fun addNoteFromUri(uri: Uri, filename: String? = null) {
        val noteTitle = when {
            filename?.isEmpty() == false -> filename
            uri.toString().startsWith("content://") -> getFilenameFromContentUri(uri) ?: getNewNoteTitle()
            else -> getNewNoteTitle()
        }

        val inputStream = contentResolver.openInputStream(uri)
        val content = inputStream?.bufferedReader().use { it!!.readText() }
        val checklistItems = content.parseChecklistItems()

        // if we got here by some other app invoking the file open intent, we have no permission for updating the original file itself
        // we can do it only after using "Export as file" or "Open file" from our app
        val canSyncNoteWithFile = if (hasPermission(PERMISSION_WRITE_STORAGE)) {
            true
        } else {
            try {
                val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                applicationContext.contentResolver.takePersistableUriPermission(uri, takeFlags)
                true
            } catch (e: Exception) {
                false
            }
        }

        val noteType = if (checklistItems != null) NoteType.TYPE_CHECKLIST.value else NoteType.TYPE_TEXT.value
        if (!canSyncNoteWithFile) {
            val note = Note(null, noteTitle, content, noteType, "", PROTECTION_NONE, "")
            displayNewNoteDialog(note.value, title = noteTitle, "")
        } else {
            val items = arrayListOf(
                RadioItem(IMPORT_FILE_SYNC, getString(R.string.update_file_at_note)),
                RadioItem(IMPORT_FILE_NO_SYNC, getString(R.string.only_import_file_content))
            )

            RadioGroupDialog(this, items) {
                val syncFile = it as Int == IMPORT_FILE_SYNC
                val path = if (syncFile) uri.toString() else ""
                val note = Note(null, noteTitle, content, noteType, "", PROTECTION_NONE, "")
                displayNewNoteDialog(note.value, title = noteTitle, path)
            }
        }
    }

    private fun openPath(path: String) {
        checkFile(path, false) {
            val title = path.getFilenameFromPath()
            try {
                val fileText = it.readText().trim()
                val checklistItems = fileText.parseChecklistItems()
                val note = if (checklistItems != null) {
                    Note(null, title.substringBeforeLast('.'), fileText, NoteType.TYPE_CHECKLIST.value, "", PROTECTION_NONE, "")
                } else {
                    Note(null, title, "", NoteType.TYPE_TEXT.value, path, PROTECTION_NONE, "")
                }

                if (mNotes.any { it.title.equals(note.title, true) }) {
                    note.title += " (file)"
                }

                addNewNote(note)
            } catch (e: Exception) {
                showErrorToast(e)
            }
        }
    }

    private fun openFolder() {
        handlePermission(PERMISSION_READ_STORAGE) { hasPermission ->
            if (hasPermission) {
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
            } else {
                toast(R.string.no_storage_permissions)
            }
        }
    }

    private fun getNewNoteTitle(): String {
        val base = getString(R.string.text_note)
        var i = 1
        while (true) {
            val tryTitle = "$base $i"
            if (mNotes.none { it.title == tryTitle }) {
                return tryTitle
            }
            i++
        }
    }

    private fun tryExportAsFile() {
        hideKeyboard()
        if (hasPermission(PERMISSION_WRITE_STORAGE)) {
            exportAsFile()
        } else {
            Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                type = "text/*"
                putExtra(Intent.EXTRA_TITLE, "${mCurrentNote.title.removeSuffix(".txt")}.txt")
                addCategory(Intent.CATEGORY_OPENABLE)

                try {
                    startActivityForResult(this, PICK_EXPORT_FILE_INTENT)
                } catch (e: ActivityNotFoundException) {
                    toast(R.string.system_service_disabled, Toast.LENGTH_LONG)
                } catch (e: NetworkErrorException) {
                    toast(getString(R.string.cannot_load_over_internet), Toast.LENGTH_LONG)
                } catch (e: Exception) {
                    showErrorToast(e)
                }
            }
        }
    }

    private fun exportAsFile() {
        ExportFileDialog(this, mCurrentNote) {
            val textToExport = if (mCurrentNote.type == NoteType.TYPE_TEXT.value) getCurrentNoteText() else mCurrentNote.value
            if (textToExport == null || textToExport.isEmpty()) {
                toast(R.string.unknown_error_occurred)
            } else if (mCurrentNote.type == NoteType.TYPE_TEXT.value) {
                showExportFilePickUpdateDialog(it, textToExport)
            } else {
                tryExportNoteValueToFile(it, mCurrentNote.title, textToExport, true)
            }
        }
    }

    private fun tryExportNotes() {
        if (isQPlus()) {
            hideKeyboard()
            val fileName = "${getString(R.string.notes)}_${getCurrentFormattedDateTime()}"
            Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                type = EXPORT_MIME_TYPE
                putExtra(Intent.EXTRA_TITLE, fileName)
                addCategory(Intent.CATEGORY_OPENABLE)

                try {
                    startActivityForResult(this, PICK_EXPORT_NOTES_INTENT)
                } catch (e: ActivityNotFoundException) {
                    toast(R.string.system_service_disabled, Toast.LENGTH_LONG)
                } catch (e: Exception) {
                    showErrorToast(e)
                }
            }
        } else {
            tryExportAllNotesBelowQ()
        }
    }

    private fun requestUnlockNotes(callback: (unlockedNoteIds: List<Long>) -> Unit) {
        val lockedNotes = mNotes.filter { it.isLocked() }
        if (lockedNotes.isNotEmpty()) {
            runOnUiThread {
                UnlockNotesDialog(this, lockedNotes, callback)
            }
        } else {
            callback(emptyList())
        }
    }

    private fun exportNotesTo(outputStream: OutputStream?) {
        ensureBackgroundThread {
            NotesHelper(this).getNotes {
                mNotes = it
                requestUnlockNotes { unlockedNoteIds ->
                    toast(R.string.exporting)
                    val notesExporter = NotesExporter(this)
                    notesExporter.exportNotes(mNotes, unlockedNoteIds, outputStream) { result ->
                        val toastId = when (result) {
                            NotesExporter.ExportResult.EXPORT_OK -> R.string.exporting_successful
                            else -> R.string.exporting_failed
                        }

                        toast(toastId)
                    }
                }
            }
        }
    }

    private fun tryImportNotes() {
        hideKeyboard()
        Intent(Intent.ACTION_GET_CONTENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = EXPORT_MIME_TYPE

            try {
                startActivityForResult(this, PICK_IMPORT_NOTES_INTENT)
            } catch (e: ActivityNotFoundException) {
                toast(R.string.system_service_disabled, Toast.LENGTH_LONG)
            } catch (e: Exception) {
                showErrorToast(e)
            }
        }
    }

    private fun tryImportingAsJson(uri: Uri, force: Boolean = false, showToasts: Boolean = true, callback: ((success: Boolean) -> Unit)? = null) {
        val path: String
        val filename: String
        when (uri.scheme) {
            "file" -> {
                path = uri.path!!
                filename = path.getFilenameFromPath()
            }
            "content" -> {
                val tempFile = getTempFile("messages", "backup.txt")
                if (tempFile == null) {
                    maybeToast(R.string.unknown_error_occurred, showToasts)
                    callback?.invoke(false)
                    return
                }

                try {
                    filename = getFilenameFromUri(uri)
                    val inputStream = contentResolver.openInputStream(uri)
                    val out = FileOutputStream(tempFile)
                    inputStream!!.copyTo(out)
                    path = tempFile.absolutePath
                } catch (e: Exception) {
                    showErrorToast(e)
                    callback?.invoke(false)
                    return
                }
            }
            else -> {
                maybeToast(R.string.invalid_file_format, showToasts)
                callback?.invoke(false)
                return
            }
        }

        maybeToast(R.string.importing, showToasts)
        ensureBackgroundThread {
            NotesImporter(this).importNotes(path, filename, force) { importResult ->
                if (importResult == ImportResult.IMPORT_FAIL) {
                    maybeToast(R.string.no_new_items, showToasts)
                    runOnUiThread { callback?.invoke(false) }
                    return@importNotes
                }

                toast(
                    when (importResult) {
                        ImportResult.IMPORT_OK -> R.string.importing_successful
                        ImportResult.IMPORT_PARTIAL -> R.string.importing_some_entries_failed
                        else -> R.string.no_new_items
                    }
                )
                initViewPager()
                runOnUiThread { callback?.invoke(true) }
            }
        }
    }

    private fun maybeToast(id: Int, show: Boolean) {
        if (show) {
            toast(id)
        }
    }

    private fun showExportFilePickUpdateDialog(exportPath: String, textToExport: String) {
        val items = arrayListOf(
            RadioItem(EXPORT_FILE_SYNC, getString(R.string.update_file_at_note)),
            RadioItem(EXPORT_FILE_NO_SYNC, getString(R.string.only_export_file_content))
        )

        RadioGroupDialog(this, items) {
            val syncFile = it as Int == EXPORT_FILE_SYNC
            tryExportNoteValueToFile(exportPath, mCurrentNote.title, textToExport, true) { exportedSuccessfully ->
                if (exportedSuccessfully) {
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
    }

    private fun tryExportAllNotesBelowQ() {
        handlePermission(PERMISSION_WRITE_STORAGE) {
            if (it) {
                exportAllNotesBelowQ()
            } else {
                toast(R.string.no_storage_permissions)
            }
        }
    }

    private fun exportAllNotesBelowQ() {
        ensureBackgroundThread {
            NotesHelper(this).getNotes { notes ->
                mNotes = notes
                requestUnlockNotes { unlockedNoteIds ->
                    ExportFilesDialog(this, mNotes) { parent, extension ->
                        val items = arrayListOf(
                            RadioItem(EXPORT_FILE_SYNC, getString(R.string.update_file_at_note)),
                            RadioItem(EXPORT_FILE_NO_SYNC, getString(R.string.only_export_file_content))
                        )

                        RadioGroupDialog(this, items) { any ->
                            val syncFile = any as Int == EXPORT_FILE_SYNC
                            var failCount = 0
                            mNotes.filter { !it.isLocked() || it.id in unlockedNoteIds }.forEachIndexed { index, note ->
                                val filename = if (extension.isEmpty()) note.title else "${note.title}.$extension"
                                val file = File(parent, filename)
                                if (!filename.isAValidFilename()) {
                                    toast(String.format(getString(R.string.filename_invalid_characters_placeholder, filename)))
                                } else {
                                    val noteStoredValue = note.getNoteStoredValue(this) ?: ""
                                    tryExportNoteValueToFile(file.absolutePath, mCurrentNote.title, note.value, false) { exportedSuccessfully ->
                                        if (exportedSuccessfully) {
                                            if (syncFile) {
                                                note.path = file.absolutePath
                                                note.value = ""
                                            } else {
                                                note.path = ""
                                                note.value = noteStoredValue
                                            }

                                            NotesHelper(this).insertOrUpdateNote(note)
                                        }

                                        if (mCurrentNote.id == note.id) {
                                            mCurrentNote.value = note.value
                                            mCurrentNote.path = note.path
                                            getPagerAdapter().updateCurrentNoteData(view_pager.currentItem, mCurrentNote.path, mCurrentNote.value)
                                        }

                                        if (!exportedSuccessfully) {
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
        }
    }

    fun tryExportNoteValueToFile(path: String, title: String, content: String, showSuccessToasts: Boolean, callback: ((success: Boolean) -> Unit)? = null) {
        if (path.startsWith("content://")) {
            exportNoteValueToUri(Uri.parse(path), title, content, showSuccessToasts, callback)
        } else {
            handlePermission(PERMISSION_WRITE_STORAGE) {
                if (it) {
                    exportNoteValueToFile(path, content, showSuccessToasts, callback)
                }
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
                    val document = if (File(path).exists()) {
                        getDocumentFile(path) ?: return@handleSAFDialog
                    } else {
                        val parent = getDocumentFile(File(path).parent) ?: return@handleSAFDialog
                        parent.createFile("", path.getFilenameFromPath())!!
                    }

                    contentResolver.openOutputStream(document.uri)!!.apply {
                        val byteArray = content.toByteArray(Charset.forName("UTF-8"))
                        write(byteArray, 0, byteArray.size)
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
                file.writeText(content)

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

    private fun exportNoteValueToUri(uri: Uri, title: String, content: String, showSuccessToasts: Boolean, callback: ((success: Boolean) -> Unit)? = null) {
        try {
            val outputStream = contentResolver.openOutputStream(uri, "rwt")
            outputStream!!.bufferedWriter().use { out ->
                out.write(content)
            }
            if (showSuccessToasts) {
                noteExportedSuccessfully(title)
            }
            callback?.invoke(true)
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

    private fun printText() {
        try {
            val webView = WebView(this)
            webView.webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest) = false

                override fun onPageFinished(view: WebView, url: String) {
                    createWebPrintJob(view)
                }
            }

            webView.loadData(getPrintableText().replace("#", "%23"), "text/plain", "UTF-8")
        } catch (e: Exception) {
            showErrorToast(e)
        }
    }

    private fun createWebPrintJob(webView: WebView) {
        val jobName = mCurrentNote.title
        val printAdapter = webView.createPrintDocumentAdapter(jobName)

        (getSystemService(Context.PRINT_SERVICE) as? PrintManager)?.apply {
            try {
                print(jobName, printAdapter, PrintAttributes.Builder().build())
            } catch (e: IllegalStateException) {
                showErrorToast(e)
            }
        }
    }

    private fun getPagerAdapter() = view_pager.adapter as NotesPagerAdapter

    private fun getCurrentNoteText() = getPagerAdapter().getCurrentNoteViewText(view_pager.currentItem)

    private fun getCurrentNoteValue(): String {
        return if (mCurrentNote.type == NoteType.TYPE_TEXT.value) {
            getCurrentNoteText() ?: ""
        } else {
            getPagerAdapter().getNoteChecklistItems(view_pager.currentItem) ?: ""
        }
    }

    private fun getPrintableText(): String {
        return if (mCurrentNote.type == NoteType.TYPE_TEXT.value) {
            getCurrentNoteText() ?: ""
        } else {
            var printableText = ""
            getPagerAdapter().getNoteChecklistRawItems(view_pager.currentItem)?.forEach {
                printableText += "${it.title}\n\n"
            }
            printableText
        }
    }

    private fun addTextToCurrentNote(text: String) = getPagerAdapter().appendText(view_pager.currentItem, text)

    private fun saveCurrentNote(force: Boolean) {
        getPagerAdapter().saveCurrentNote(view_pager.currentItem, force)
        if (mCurrentNote.type == NoteType.TYPE_CHECKLIST.value) {
            mCurrentNote.value = getPagerAdapter().getNoteChecklistItems(view_pager.currentItem) ?: ""
        }
    }

    private fun displayDeleteNotePrompt() {
        DeleteNoteDialog(this, mCurrentNote) {
            deleteNote(it, mCurrentNote)
        }
    }

    fun deleteNote(deleteFile: Boolean, note: Note) {
        if (mNotes.size <= 1 || note != mCurrentNote) {
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
        ensureBackgroundThread {
            val currentNoteIndex = mNotes.indexOf(note)
            val noteToRefresh = mNotes[if (currentNoteIndex > 0) currentNoteIndex - 1 else currentNoteIndex + 1]

            notesDB.deleteNote(note)
            widgetsDB.deleteNoteWidgets(note.id!!)

            refreshNotes(noteToRefresh, deleteFile)
        }
    }

    private fun refreshNotes(note: Note, deleteFile: Boolean) {
        NotesHelper(this).getNotes {
            mNotes = it
            val noteId = note.id
            updateSelectedNote(noteId!!)
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

            if (it.size == 1 && config.showNotePicker) {
                config.showNotePicker = false
            }
        }
    }

    private fun displayOpenNoteDialog() {
        OpenNoteDialog(this) { noteId, newNote ->
            if (newNote == null) {
                updateSelectedNote(noteId)
            } else {
                addNewNote(newNote)
            }
        }
    }

    private fun saveNote() {
        saveCurrentNote(true)
        showSaveButton = false
        refreshMenuItems()
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
        val text = if (mCurrentNote.type == NoteType.TYPE_TEXT.value) getCurrentNoteText() else mCurrentNote.value
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

    @SuppressLint("NewApi")
    private fun createShortcut() {
        val manager = getSystemService(ShortcutManager::class.java)
        if (manager.isRequestPinShortcutSupported) {
            val note = mCurrentNote
            val drawable = resources.getDrawable(R.drawable.shortcut_note).mutate()
            val appIconColor = baseConfig.appIconColor
            (drawable as LayerDrawable).findDrawableByLayerId(R.id.shortcut_plus_background).applyColorFilter(appIconColor)

            val intent = Intent(this, SplashActivity::class.java)
            intent.action = Intent.ACTION_VIEW
            intent.putExtra(OPEN_NOTE_ID, note.id)
            intent.flags = intent.flags or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NO_HISTORY

            val shortcut = ShortcutInfo.Builder(this, note.hashCode().toString())
                .setShortLabel(mCurrentNote.title)
                .setIcon(Icon.createWithBitmap(drawable.convertToBitmap()))
                .setIntent(intent)
                .build()

            manager.requestPinShortcut(shortcut, null)
        }
    }

    private fun lockNote() {
        ConfirmationDialog(this, "", R.string.locking_warning, R.string.ok, R.string.cancel) {
            SecurityDialog(this, "", SHOW_ALL_TABS) { hash, type, success ->
                if (success) {
                    mCurrentNote.protectionHash = hash
                    mCurrentNote.protectionType = type
                    NotesHelper(this).insertOrUpdateNote(mCurrentNote) {
                        refreshMenuItems()
                    }
                }
            }
        }
    }

    private fun unlockNote() {
        performSecurityCheck(
            protectionType = mCurrentNote.protectionType,
            requiredHash = mCurrentNote.protectionHash,
            successCallback = { _, _ -> removeProtection(mCurrentNote) }
        )
    }

    private fun removeProtection(note: Note) {
        note.protectionHash = ""
        note.protectionType = PROTECTION_NONE
        NotesHelper(this).insertOrUpdateNote(note) {
            if (note == mCurrentNote) {
                getCurrentFragment()?.apply {
                    shouldShowLockedContent = true
                    checkLockState()
                }
                refreshMenuItems()
            }
        }
    }

    fun currentNoteTextChanged(newText: String, showUndo: Boolean, showRedo: Boolean) {
        if (!isSearchActive) {
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
                refreshMenuItems()
            }
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
            add(Release(62, R.string.release_62))
            add(Release(64, R.string.release_64))
            add(Release(67, R.string.release_67))
            add(Release(81, R.string.release_81))
            add(Release(86, R.string.release_86))
            checkWhatsNew(this, BuildConfig.VERSION_CODE)
        }
    }

    private fun removeDoneItems() {
        getPagerAdapter().removeDoneCheckListItems(view_pager.currentItem)
    }

    private fun displaySortChecklistDialog() {
        SortChecklistDialog(this) {
            getPagerAdapter().refreshChecklist(view_pager.currentItem)
        }
    }
}
