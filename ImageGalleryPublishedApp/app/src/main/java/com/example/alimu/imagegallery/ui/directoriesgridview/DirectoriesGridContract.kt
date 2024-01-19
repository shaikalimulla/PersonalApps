package com.example.alimu.imagegallery.ui.directoriesgridview

import com.example.alimu.imagegallery.ui.base.BasePresenter
import com.example.alimu.imagegallery.ui.base.BaseView
import java.io.File

interface DirectoriesGridContract {
    interface View : BaseView {
        fun getImageDirectoriesPath(): HashSet<String>?
        fun setSelector()
        fun showGridViewFragment(position: Int, dirList: ArrayList<String>, dirPathList: ArrayList<String>)
        fun openCamera()
        fun refreshViews()
        fun updateImageAdapter(dirList: ArrayList<String>, dirSizeList: ArrayList<Int>,
                               dirPathList: ArrayList<String>)
        fun requestVoiceSpeech()
        fun showAppInfo()
        fun showPrivacyInfo()
    }

    interface Presenter: BasePresenter {
        var currentImagePath: String?
        fun onViewInitialized()
        fun onGridViewItemClicked(position: Int)
        fun onOpenCameraButtonClicked()
        fun generateImage(): File
        fun onPhotoReceived()
        fun onPhotoRequestFailed()
        fun onVoiceTextButtonClicked()
        fun onAppInfoButtonClicked()
        fun onPrivacyInfoButtonClicked()
    }
}