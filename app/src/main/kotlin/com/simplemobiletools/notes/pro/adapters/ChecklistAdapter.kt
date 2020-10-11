package com.simplemobiletools.notes.pro.adapters

import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.util.TypedValue
import android.view.Menu
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.simplemobiletools.commons.activities.BaseSimpleActivity
import com.simplemobiletools.commons.adapters.MyRecyclerViewAdapter
import com.simplemobiletools.commons.extensions.applyColorFilter
import com.simplemobiletools.commons.extensions.beVisibleIf
import com.simplemobiletools.commons.extensions.getColoredDrawableWithColor
import com.simplemobiletools.commons.extensions.removeBit
import com.simplemobiletools.commons.interfaces.ItemMoveCallback
import com.simplemobiletools.commons.interfaces.ItemTouchHelperContract
import com.simplemobiletools.commons.interfaces.StartReorderDragListener
import com.simplemobiletools.commons.views.MyRecyclerView
import com.simplemobiletools.notes.pro.R
import com.simplemobiletools.notes.pro.dialogs.RenameChecklistItemDialog
import com.simplemobiletools.notes.pro.extensions.config
import com.simplemobiletools.notes.pro.extensions.getPercentageFontSize
import com.simplemobiletools.notes.pro.helpers.DONE_CHECKLIST_ITEM_ALPHA
import com.simplemobiletools.notes.pro.interfaces.ChecklistItemsListener
import com.simplemobiletools.notes.pro.models.ChecklistItem
import kotlinx.android.synthetic.main.item_checklist.view.*
import java.util.*

class ChecklistAdapter(activity: BaseSimpleActivity, var items: ArrayList<ChecklistItem>, val listener: ChecklistItemsListener?,
                       recyclerView: MyRecyclerView, val showIcons: Boolean, itemClick: (Any) -> Unit) :
        MyRecyclerViewAdapter(activity, recyclerView, null, itemClick), ItemTouchHelperContract {

    private lateinit var crossDrawable: Drawable
    private lateinit var checkDrawable: Drawable
    private var touchHelper: ItemTouchHelper? = null
    private var startReorderDragListener: StartReorderDragListener

    init {
        setupDragListener(true)
        initDrawables()

        touchHelper = ItemTouchHelper(ItemMoveCallback(this))
        touchHelper!!.attachToRecyclerView(recyclerView)

        startReorderDragListener = object : StartReorderDragListener {
            override fun requestDrag(viewHolder: RecyclerView.ViewHolder) {
                touchHelper?.startDrag(viewHolder)
            }
        }
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

    override fun onActionModeCreated() {
        notifyDataSetChanged()
    }

    override fun onActionModeDestroyed() {
        notifyDataSetChanged()
    }

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
            setupView(itemView, item, holder)
        }
        bindViewHolder(holder)
    }

    override fun getItemCount() = items.size

    private fun initDrawables() {
        val res = activity.resources
        crossDrawable = res.getColoredDrawableWithColor(R.drawable.ic_cross_vector, res.getColor(R.color.md_red_700))
        checkDrawable = res.getColoredDrawableWithColor(R.drawable.ic_check_vector, res.getColor(R.color.md_green_700))
    }

    private fun renameChecklistItem() {
        val item = getSelectedItems().first()
        RenameChecklistItemDialog(activity, item.title) {
            val position = getSelectedItemPositions().first()
            item.title = it
            listener?.saveChecklist()
            notifyItemChanged(position)
            finishActMode()
        }
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

    private fun setupView(view: View, checklistItem: ChecklistItem, holder: ViewHolder) {
        val isSelected = selectedKeys.contains(checklistItem.id)
        view.apply {
            checklist_title.apply {
                text = checklistItem.title
                setTextColor(textColor)
                setTextSize(TypedValue.COMPLEX_UNIT_PX, context.getPercentageFontSize())
                gravity = context.config.getTextGravity()

                if (checklistItem.isDone) {
                    paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                    alpha = DONE_CHECKLIST_ITEM_ALPHA
                } else {
                    paintFlags = paintFlags.removeBit(Paint.STRIKE_THRU_TEXT_FLAG)
                    alpha = 1f
                }
            }

            checklist_image.setImageDrawable(if (checklistItem.isDone) checkDrawable else crossDrawable)
            checklist_image.beVisibleIf(showIcons)
            checklist_holder.isSelected = isSelected

            checklist_drag_handle.beVisibleIf(selectedKeys.isNotEmpty())
            checklist_drag_handle.applyColorFilter(textColor)
            checklist_drag_handle.setOnTouchListener { v, event ->
                if (event.action == MotionEvent.ACTION_DOWN) {
                    startReorderDragListener.requestDrag(holder)
                }
                false
            }
        }
    }

    override fun onRowMoved(fromPosition: Int, toPosition: Int) {
        if (fromPosition < toPosition) {
            for (i in fromPosition until toPosition) {
                Collections.swap(items, i, i + 1)
            }
        } else {
            for (i in fromPosition downTo toPosition + 1) {
                Collections.swap(items, i, i - 1)
            }
        }
        notifyItemMoved(fromPosition, toPosition)
    }

    override fun onRowSelected(myViewHolder: ViewHolder?) {
    }

    override fun onRowClear(myViewHolder: ViewHolder?) {
        listener?.saveChecklist()
    }
}
