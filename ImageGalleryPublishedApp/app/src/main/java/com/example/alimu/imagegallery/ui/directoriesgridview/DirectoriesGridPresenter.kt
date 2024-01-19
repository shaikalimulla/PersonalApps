package com.example.alimu.imagegallery.ui.directoriesgridview

import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DirectoriesGridPresenter(
    private val view : DirectoriesGridContract.View
): DirectoriesGridContract.Presenter {

    private var dirList = ArrayList<String>()
    private var dirSizeList = ArrayList<Int>()
    private var dirPathList = ArrayList<String>()

    override fun start() {
        if (view.isActive().not()) {
            return
        }
        dirList.clear()
        dirSizeList.clear()
        dirPathList.clear()

        val imagesDirPath = view.getImageDirectoriesPath()
        imagesDirPath?.forEach { path ->
            processImagesPath(path)
        }
    }

    override var currentImagePath: String? = null
        get() = field
        set(value) { field = value }

    override fun onViewInitialized() {
        if (view.isActive()) {
            view.updateImageAdapter(dirList, dirSizeList, dirPathList)
        }
    }

    override fun onGridViewItemClicked(position: Int) {
        if (view.isActive()) {
            view.setSelector()
            view.showGridViewFragment(position, dirList, dirPathList)
        }
    }

    override fun onOpenCameraButtonClicked() {
        if (view.isActive()) {
            view.openCamera()
        }
    }

    @Throws(IOException::class)
    override fun generateImage(): File {
        val time = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CANADA).format(Date())
        val imageFileName = PHOTO_PREFIX + time + "_"
        val picIndex = dirList.indexOf("Pictures")
        val picPath = dirPathList[picIndex]
        val image = File.createTempFile(imageFileName, ".jpg", File(picPath))
        currentImagePath = image.absolutePath
        return image
    }

    override fun onPhotoReceived() {
        val picIndex = dirList.indexOf(PICTURES_DIR_NAME)
        dirSizeList[picIndex] = dirSizeList[picIndex] + 1
        if (view.isActive()) {
            view.refreshViews()
        }
    }

    override fun onPhotoRequestFailed() {
        currentImagePath?.let { File(it).delete() }
    }

    override fun onVoiceTextButtonClicked() {
        if (view.isActive()) {
            view.requestVoiceSpeech()
        }
    }

    override fun onAppInfoButtonClicked() {
        if (view.isActive()) {
            view.showAppInfo()
        }
    }

    override fun onPrivacyInfoButtonClicked() {
        if (view.isActive()) {
            view.showPrivacyInfo()
        }
    }

    private fun processImagesPath(path: String) {
        var numOfFiles = 0
        val localDirList = ArrayList<String>()
        val localDirSizeList = ArrayList<Int>()
        val localDirPathList = ArrayList<String>()
        val mainDir = File(path)
        localDirList.add(mainDir.name)
        localDirPathList.add(path)
        val files = mainDir.listFiles()
        if (mainDir.isDirectory) {
            if (files == null) {
                return
            }
        }
        files?.forEach { file ->
            if (file.isDirectory) {
                localDirList.add(file.name)
            } else {
                numOfFiles++
            }
        }
        //localDirSizeList.add(numOfFiles)
        var startIndex = 0
        if (numOfFiles > 0) {
            localDirSizeList.add(numOfFiles)
            // Already added main directory so start traversing from next element
            startIndex = 1
        } else {
            // Remove folders with 0 size
            localDirList.remove(mainDir.name)
            localDirPathList.remove(path)
        }
        var nextElement = startIndex
        val localDirListSize = localDirList.size
        for (i in startIndex until localDirListSize) {
            val pathName =  "$path/${localDirList[nextElement]}/"
            val targetDir = File(pathName)
            //path + "/"+ localDirList[i] +"/"
            val remainingFiles = targetDir.listFiles()
            remainingFiles?.let {
                val dirSize = it.size
                if (dirSize > 0) {
                    localDirPathList.add(pathName)
                    localDirSizeList.add(dirSize)
                    nextElement+=1
                } else {
                    // Remove folders with 0 size
                    localDirList.removeAt(nextElement)
                }
            }
        }
        dirList.addAll(localDirList)
        dirSizeList.addAll(localDirSizeList)
        dirPathList.addAll(localDirPathList)
    }

    companion object {
        private const val PHOTO_PREFIX = "quick_pic_plus"
        private const val PICTURES_DIR_NAME = "Pictures"
    }
}