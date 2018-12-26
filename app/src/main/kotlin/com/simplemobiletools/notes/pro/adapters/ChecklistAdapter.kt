package com.simplemobiletools.notes.pro.adapters

import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import com.simplemobiletools.commons.activities.BaseSimpleActivity
import com.simplemobiletools.commons.adapters.MyRecyclerViewAdapter
import com.simplemobiletools.commons.extensions.beVisibleIf
import com.simplemobiletools.commons.extensions.getColoredDrawableWithColor
import com.simplemobiletools.commons.views.MyRecyclerView
import com.simplemobiletools.notes.pro.R
import com.simplemobiletools.notes.pro.helpers.DONE_CHECKLIST_ITEM_ALPHA
import com.simplemobiletools.notes.pro.interfaces.ChecklistItemsListener
import com.simplemobiletools.notes.pro.models.ChecklistItem
import kotlinx.android.synthetic.main.item_checklist.view.*
import java.util.*

class ChecklistAdapter(activity: BaseSimpleActivity, var items: ArrayList<ChecklistItem>, val listener: ChecklistItemsListener?,
                       recyclerView: MyRecyclerView, val showIcons: Boolean, itemClick: (Any) -> Unit) : MyRecyclerViewAdapter(activity, recyclerView, null, itemClick) {

    private lateinit var crossDrawable: Drawable
    private lateinit var checkDrawable: Drawable

    init {
        setupDragListener(true)
        initDrawables()
    }

    override fun getActionMenuId() = R.menu.cab_checklist

    override fun actionItemPressed(id: Int) {
        if (selectedKeys.isEmpty()) {
            return
        }

        when (id) {
            R.id.cab_rename -> renameChecklistItem()
            R.id.cab_delete -> deleteSelection()
        }
    }

    override fun getSelectableItemCount() = items.size

    override fun getIsItemSelectable(position: Int) = true

    override fun getItemSelectionKey(position: Int) = items.getOrNull(position)?.id

    override fun getItemKeyPosition(key: Int) = items.indexOfFirst { it.id == key }

    override fun prepareActionMode(menu: Menu) {
        val selectedItems = getSelectedItems()
        if (selectedItems.isEmpty()) {
            return
        }

        menu.findItem(R.id.cab_rename).isVisible = isOneItemSelected()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = createViewHolder(R.layout.item_checklist, parent)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.bindView(item, true, true) { itemView, layoutPosition ->
            setupView(itemView, item)
        }
        bindViewHolder(holder)
    }

    override fun getItemCount() = items.size

    private fun initDrawables() {
        val res = activity.resources
        crossDrawable = res.getColoredDrawableWithColor(R.drawable.ic_cross_big, res.getColor(R.color.theme_dark_red_primary_color))
        checkDrawable = res.getColoredDrawableWithColor(R.drawable.ic_check_big, res.getColor(R.color.md_green_700))
    }

    private fun renameChecklistItem() {

    }

    private fun deleteSelection() {
        val removeItems = ArrayList<ChecklistItem>(selectedKeys.size)
        val positions = ArrayList<Int>()
        selectedKeys.forEach {
            val key = it
            val position = items.indexOfFirst { it.id == key }
            if (position != -1) {
                positions.add(position)

                val favorite = getItemWithKey(key)
                if (favorite != null) {
                    removeItems.add(favorite)
                }
            }
        }

        positions.sortDescending()
        removeSelectedItems(positions)

        items.removeAll(removeItems)
        listener?.saveChecklist()
        if (items.isEmpty()) {
            listener?.refreshItems()
        }
    }

    private fun getItemWithKey(key: Int): ChecklistItem? = items.firstOrNull { it.id == key }

    private fun getSelectedItems() = items.filter { selectedKeys.contains(it.id) } as ArrayList<ChecklistItem>

    private fun setupView(view: View, checklistItem: ChecklistItem) {
        val isSelected = selectedKeys.contains(checklistItem.id)
        view.apply {
            checklist_title.apply {
                text = checklistItem.title
                setTextColor(textColor)

                if (checklistItem.isDone) {
                    paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
                    alpha = DONE_CHECKLIST_ITEM_ALPHA
                } else {
                    paintFlags = 0
                    alpha = 1f
                }
            }

            checklist_image.setImageDrawable(if (checklistItem.isDone) checkDrawable else crossDrawable)
            checklist_image.beVisibleIf(showIcons)
            checklist_holder.isSelected = isSelected
        }
    }
}
