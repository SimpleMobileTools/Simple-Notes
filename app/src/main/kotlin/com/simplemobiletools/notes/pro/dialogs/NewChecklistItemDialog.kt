package com.simplemobiletools.notes.pro.dialogs

import android.app.Activity
import android.content.DialogInterface.BUTTON_POSITIVE
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.appcompat.widget.AppCompatEditText
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.helpers.DARK_GREY
import com.simplemobiletools.commons.helpers.SORT_BY_CUSTOM
import com.simplemobiletools.notes.pro.R
import com.simplemobiletools.notes.pro.extensions.config
import kotlinx.android.synthetic.main.dialog_new_checklist_item.view.*
import kotlinx.android.synthetic.main.item_add_checklist.view.*

class NewChecklistItemDialog(val activity: Activity, callback: (titles: ArrayList<String>) -> Unit) {
    private val titles = mutableListOf<AppCompatEditText>()
    private val view: ViewGroup = activity.layoutInflater.inflate(R.layout.dialog_new_checklist_item, null) as ViewGroup

    init {
        addNewEditText()
        val plusTextColor = if (activity.isWhiteTheme()) {
            DARK_GREY
        } else {
            activity.getProperPrimaryColor().getContrastColor()
        }

        view.apply {
            add_item.applyColorFilter(plusTextColor)
            add_item.setOnClickListener {
                addNewEditText()
            }
            settings_add_checklist_top.beVisibleIf(activity.config.sorting == SORT_BY_CUSTOM)
            settings_add_checklist_top.isChecked = activity.config.addNewChecklistItemsTop
        }

        activity.getAlertDialogBuilder()
            .setPositiveButton(R.string.ok, null)
            .setNegativeButton(R.string.cancel, null)
            .apply {
                activity.setupDialogStuff(view, this, R.string.add_new_checklist_items) { alertDialog ->
                    alertDialog.showKeyboard(titles.first())
                    alertDialog.getButton(BUTTON_POSITIVE).setOnClickListener {
                        activity.config.addNewChecklistItemsTop = view.settings_add_checklist_top.isChecked
                        when {
                            titles.all { it.text!!.isEmpty() } -> activity.toast(R.string.empty_name)
                            else -> {
                                val titles = titles.map { it.text.toString() }.filter { it.isNotEmpty() }.toMutableList() as ArrayList<String>
                                callback(titles)
                                alertDialog.dismiss()
                            }
                        }
                    }
                }
            }
    }

    private fun addNewEditText() {
        activity.layoutInflater.inflate(R.layout.item_add_checklist, null).apply {
            title_edit_text.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_NEXT || actionId == EditorInfo.IME_ACTION_DONE || actionId == KeyEvent.KEYCODE_ENTER) {
                    addNewEditText()
                    true
                } else {
                    false
                }
            }
            titles.add(title_edit_text)
            view.checklist_holder.addView(this)
            activity.updateTextColors(view.checklist_holder)
            view.dialog_holder.post {
                view.dialog_holder.fullScroll(View.FOCUS_DOWN)
                activity.showKeyboard(title_edit_text)
            }
        }
    }
}
