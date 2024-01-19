package com.example.alimu.imagegallery.listeners

import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.widget.AbsListView
import com.example.alimu.imagegallery.R

class MultiChoiceModeListener(
    private val listener: ImagesGridListener
) : AbsListView.MultiChoiceModeListener {
    private var operationSelected = false

    override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
        mode.title = "Select Items"
        mode.subtitle = "1 item selected"
        mode.menuInflater.inflate(R.menu.onselect_menu, menu)
        operationSelected = false
        return true
    }

    override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
        return true
    }

    override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.item_copy -> {
                operationSelected = true
                listener.onItemCopyClicked(mode)
                true
            }

            R.id.item_delete -> {
                operationSelected = true
                listener.onItemDeleteClicked(mode)
                true
            }

            R.id.item_cut -> {
                listener.onItemCutClicked(mode)
                true
            }

            else -> false
        }
    }

    override fun onDestroyActionMode(mode: ActionMode) {
        if (operationSelected) {
            return
        }
        listener.destroyView()
    }

    override fun onItemCheckedStateChanged(
        mode: ActionMode, position: Int, id: Long,
        checked: Boolean
    ) {
        listener.onItemCheckedStateChanged(mode, position, id, checked)
    }
}