package com.simplemobiletools.notes.pro.adapters

import android.content.Context
import android.graphics.Color
import android.text.SpannableString
import android.text.style.StrikethroughSpan
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.simplemobiletools.commons.activities.BaseSimpleActivity
import com.simplemobiletools.commons.adapters.MyRecyclerViewAdapter
import com.simplemobiletools.commons.extensions.beGoneIf
import com.simplemobiletools.commons.extensions.beVisibleIf
import com.simplemobiletools.commons.extensions.getColoredDrawableWithColor
import com.simplemobiletools.commons.extensions.isBlackAndWhiteTheme
import com.simplemobiletools.commons.helpers.LOWER_ALPHA_INT
import com.simplemobiletools.commons.helpers.SORT_BY_CUSTOM
import com.simplemobiletools.commons.views.MyRecyclerView
import com.simplemobiletools.notes.pro.R
import com.simplemobiletools.notes.pro.extensions.config
import com.simplemobiletools.notes.pro.models.ChecklistItem
import com.simplemobiletools.notes.pro.models.Note
import com.simplemobiletools.notes.pro.models.NoteType
import kotlinx.android.synthetic.main.open_note_item.view.icon_lock
import kotlinx.android.synthetic.main.open_note_item.view.open_note_item_holder
import kotlinx.android.synthetic.main.open_note_item.view.open_note_item_text
import kotlinx.android.synthetic.main.open_note_item.view.open_note_item_title

class OpenNoteAdapter(
    activity: BaseSimpleActivity, var items: List<Note>,
    recyclerView: MyRecyclerView, itemClick: (Any) -> Unit
) : MyRecyclerViewAdapter(activity, recyclerView, itemClick) {
    override fun getActionMenuId() = 0

    override fun actionItemPressed(id: Int) {}

    override fun getSelectableItemCount() = itemCount

    override fun getIsItemSelectable(position: Int) = false

    override fun getItemSelectionKey(position: Int) = items.getOrNull(position)?.id?.toInt()

    override fun getItemKeyPosition(key: Int) = items.indexOfFirst { it.id?.toInt() == key }

    override fun onActionModeCreated() {}

    override fun onActionModeDestroyed() {}

    override fun prepareActionMode(menu: Menu) {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return createViewHolder(R.layout.open_note_item, parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.bindView(item, true, false) { itemView, layoutPosition ->
            setupView(itemView, item)
        }
        bindViewHolder(holder)
    }

    override fun getItemCount() = items.size

    private fun setupView(view: View, note: Note) {
        view.apply {
            setupCard(open_note_item_holder)
            open_note_item_title.apply {
                text = note.title
                setTextColor(properPrimaryColor)
            }
            val formattedText = note.getFormattedValue(context)
            open_note_item_text.beGoneIf(formattedText.isNullOrBlank() || note.isLocked())
            icon_lock.beVisibleIf(note.isLocked())
            icon_lock.setImageDrawable(activity.resources.getColoredDrawableWithColor(R.drawable.ic_lock_vector, properPrimaryColor))
            open_note_item_text.apply {
                text = formattedText
                setTextColor(textColor)
            }
        }
    }

    private fun View.setupCard(holder: View) {
        if (context.isBlackAndWhiteTheme()) {
            holder.setBackgroundResource(R.drawable.black_dialog_background)
        } else {
            val cardBackgroundColor = if (backgroundColor == Color.BLACK) {
                Color.WHITE
            } else {
                Color.BLACK
            }
            val cardBackground = if (context.config.isUsingSystemTheme) {
                R.drawable.dialog_you_background
            } else {
                R.drawable.dialog_bg
            }
            holder.background =
                activity.resources.getColoredDrawableWithColor(cardBackground, cardBackgroundColor, LOWER_ALPHA_INT)
        }
    }

    private fun Note.getFormattedValue(context: Context): CharSequence? {
        return when (type) {
            NoteType.TYPE_TEXT -> getNoteStoredValue(context)
            NoteType.TYPE_CHECKLIST -> {
                val checklistItemType = object : TypeToken<List<ChecklistItem>>() {}.type
                var items = Gson().fromJson<List<ChecklistItem>>(getNoteStoredValue(context), checklistItemType) ?: listOf()
                items = items.filter { it.title != null }.let {
                    val sorting = context.config.sorting
                    ChecklistItem.sorting = sorting
                    if (ChecklistItem.sorting and SORT_BY_CUSTOM == 0) {
                        it.sorted().let {
                            if (context.config.moveDoneChecklistItems) {
                                it.sortedBy { it.isDone }
                            } else {
                                it
                            }
                        }
                    } else {
                        it
                    }
                }
                val linePrefix = "• "
                val stringifiedItems = items.joinToString(separator = System.lineSeparator()) {
                    "${linePrefix}${it.title}"
                }

                val formattedText = SpannableString(stringifiedItems)
                var currentPos = 0
                items.forEach { item ->
                    currentPos += linePrefix.length
                    if (item.isDone) {
                        formattedText.setSpan(StrikethroughSpan(), currentPos, currentPos + item.title.length, 0)
                    }
                    currentPos += item.title.length
                    currentPos += System.lineSeparator().length
                }
                formattedText
            }
        }
    }
}
