package com.example.alimu.imagegallery.ui.imagesgridview

import com.example.alimu.imagegallery.adapters.ImagesGridAdapter
import com.example.alimu.imagegallery.ui.base.BasePresenter
import com.example.alimu.imagegallery.ui.base.BaseView
import java.io.File

interface ImagesGridContract {
    interface View : BaseView {
        fun setSelector()
        fun setSelectedPosition(position: Int)
        fun showImageDisplayFragment(position: Int, imageList: ArrayList<String>)
        fun showMessage(message: String)
        fun addSelectedImages(dirName: String, path: String, operation: String)
        fun proceedDeletion()
        fun deleteImage(file: File)
    }

    interface Presenter: BasePresenter {
        fun loadImages(imageAdapter: ImagesGridAdapter?, dirName: String?, dirPath: String?)
        fun onGridViewItemClicked(position: Int)
        fun onGridViewItemLongClicked(position: Int)
        fun findImage(path: String, dirName: String, operation: String, selectedItem: String?)
        fun removeImageFromPhone(path: String?)
        fun onItemCopyClicked(itemIndex: Int, itemTitle: String, dirPathList: ArrayList<String>)
        fun onItemCutClicked(itemIndex: Int, itemTitle: String, dirPathList: ArrayList<String>)
        fun onItemDeleteClicked(itemIndex: Int, itemTitle: String)
    }
}