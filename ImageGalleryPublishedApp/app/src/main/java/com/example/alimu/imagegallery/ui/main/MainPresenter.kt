package com.example.alimu.imagegallery.ui.main

class MainPresenter (
    private val view : MainContract.View
) : MainContract.Presenter {

    override fun start() {
        if (view.isActive()) {
            if (view.areMediaPermissionsGranted()) {
                view.showDirectoriesGridFragment()
            } else {
                view.requestMediaPermissions()
            }
        }
    }
}