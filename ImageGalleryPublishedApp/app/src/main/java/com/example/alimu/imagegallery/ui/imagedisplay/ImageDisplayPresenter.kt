package com.example.alimu.imagegallery.ui.imagedisplay

import android.graphics.BitmapFactory
import com.example.alimu.imagegallery.common.ImageUtil.getDecodedBitmap
import java.io.File

class ImageDisplayPresenter(
    private val view : ImageDisplayContract.View,
    private val path: String?
): ImageDisplayContract.Presenter {

    override fun start() {}

    override fun onViewInitialized() {
        if (view.isActive().not()) {
            return
        }
        val fileParent = File(path ?: return).parent
        val dirName = fileParent?.substring(fileParent.lastIndexOf(File.separator) + 1)
        view.updateTitleText(dirName)
        val bitmap = getDecodedBitmap(path)
        bitmap?.let { view.loadImage(bitmap) }
    }
}