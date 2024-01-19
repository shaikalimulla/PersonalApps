package com.example.alimu.imagegallery.listeners

import android.view.ActionMode

interface ImagesGridListener {
    fun onItemCopyClicked(mode: ActionMode)
    fun onItemCutClicked(mode: ActionMode)
    fun onItemDeleteClicked(mode: ActionMode)
    fun onItemCheckedStateChanged(
        mode: ActionMode, position: Int, id: Long,
        checked: Boolean
    )
    fun destroyView()
}