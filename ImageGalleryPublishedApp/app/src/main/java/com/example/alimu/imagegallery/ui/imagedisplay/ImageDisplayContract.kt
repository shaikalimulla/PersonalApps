package com.example.alimu.imagegallery.ui.imagedisplay

import android.graphics.Bitmap
import com.example.alimu.imagegallery.ui.base.BasePresenter
import com.example.alimu.imagegallery.ui.base.BaseView

interface ImageDisplayContract {
    interface View : BaseView {
        fun updateTitleText(message: String?)
        fun loadImage(bitmap: Bitmap)
    }

    interface Presenter: BasePresenter {
        fun onViewInitialized()
    }
}