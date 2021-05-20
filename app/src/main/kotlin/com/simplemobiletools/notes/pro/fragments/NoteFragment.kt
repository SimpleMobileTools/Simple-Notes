package com.simplemobiletools.notes.pro.fragments

import android.util.TypedValue
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.simplemobiletools.commons.dialogs.SecurityDialog
import com.simplemobiletools.commons.extensions.applyColorFilter
import com.simplemobiletools.commons.extensions.beVisibleIf
import com.simplemobiletools.commons.extensions.getAdjustedPrimaryColor
import com.simplemobiletools.commons.helpers.PROTECTION_NONE
import com.simplemobiletools.notes.pro.extensions.config
import com.simplemobiletools.notes.pro.extensions.getPercentageFontSize
import com.simplemobiletools.notes.pro.models.Note
import kotlinx.android.synthetic.main.fragment_checklist.view.*

abstract class NoteFragment : Fragment() {
    protected var shouldShowLockedContent = false
    protected var note: Note? = null

    protected fun setupLockedViews(view: ViewGroup, note: Note) {
        view.apply {
            note_locked_layout.beVisibleIf(note.isLocked() && !shouldShowLockedContent)
            note_locked_image.applyColorFilter(config!!.textColor)

            note_locked_label.setTextColor(context!!.config.textColor)
            note_locked_label.setTextSize(TypedValue.COMPLEX_UNIT_PX, context.getPercentageFontSize())

            note_locked_show.setTextColor(context!!.getAdjustedPrimaryColor())
            note_locked_show.setTextSize(TypedValue.COMPLEX_UNIT_PX, context.getPercentageFontSize())
            note_locked_show.setOnClickListener {
                handleUnlocking()
            }
        }
    }

    fun handleUnlocking(callback: (() -> Unit)? = null) {
        if (callback != null && (note!!.protectionType == PROTECTION_NONE || shouldShowLockedContent)) {
            callback()
            return
        }

        SecurityDialog(activity!!, note!!.protectionHash, note!!.protectionType) { hash, type, success ->
            if (success) {
                shouldShowLockedContent = true
                checkLockState()
                callback?.invoke()
            }
        }
    }

    abstract fun checkLockState()
}
