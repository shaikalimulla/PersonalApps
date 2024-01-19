package com.example.alimu.imagegallery.ui.main

import com.example.alimu.imagegallery.ui.base.BasePresenter
import com.example.alimu.imagegallery.ui.base.BaseView

interface MainContract {
    interface View : BaseView {
        fun areMediaPermissionsGranted(): Boolean
        fun showDirectoriesGridFragment()
        fun requestMediaPermissions()
    }

    interface Presenter: BasePresenter {}
}