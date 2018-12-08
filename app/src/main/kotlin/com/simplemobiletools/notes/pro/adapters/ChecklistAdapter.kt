package com.simplemobiletools.notes.pro.adapters

import android.graphics.Paint
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import com.simplemobiletools.commons.activities.BaseSimpleActivity
import com.simplemobiletools.commons.adapters.MyRecyclerViewAdapter
import com.simplemobiletools.commons.interfaces.RefreshRecyclerViewListener
import com.simplemobiletools.commons.views.MyRecyclerView
import com.simplemobiletools.notes.pro.R
import com.simplemobiletools.notes.pro.models.ChecklistItem
import kotlinx.android.synthetic.main.item_checklist.view.*
import java.util.*

class ChecklistAdapter(activity: BaseSimpleActivity, var items: ArrayList<ChecklistItem>, val listener: RefreshRecyclerViewListener?,
                       recyclerView: MyRecyclerView, itemClick: (Any) -> Unit) : MyRecyclerViewAdapter(activity, recyclerView, null, itemClick) {

    init {
        setupDragListener(true)
    }

    override fun getActionMenuId() = R.menu.cab_delete_only

    override fun actionItemPressed(id: Int) {}

    override fun getSelectableItemCount() = items.size

    override fun getIsItemSelectable(position: Int) = true

    override fun getItemSelectionKey(position: Int) = items.getOrNull(position)?.id

    override fun getItemKeyPosition(key: Int) = items.indexOfFirst { it.id == key }

    override fun prepareActionMode(menu: Menu) {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = createViewHolder(R.layout.item_checklist, parent)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.bindView(item, true, true) { itemView, layoutPosition ->
            setupView(itemView, item)
        }
        bindViewHolder(holder)
    }

    override fun getItemCount() = items.size

    private fun setupView(view: View, checklistItem: ChecklistItem) {
        val isSelected = selectedKeys.contains(checklistItem.id)
        view.apply {
            checklist_title.apply {
                text = checklistItem.title
                setTextColor(textColor)

                if (checklistItem.isDone) {
                    paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
                    alpha = 0.4f
                } else {
                    paintFlags = 0
                    alpha = 1f
                }
            }
            checklist_holder.isSelected = isSelected
        }
    }
}
