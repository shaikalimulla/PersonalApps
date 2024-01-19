package com.example.alimu.imagegallery.ui.imagesgridview

import com.example.alimu.imagegallery.LoadImagesAsync
import com.example.alimu.imagegallery.adapters.ImagesGridAdapter
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class ImagesGridPresenter(
    private val view : ImagesGridContract.View
): ImagesGridContract.Presenter {

    private var loadImagesAsync: LoadImagesAsync? = null
    private var selectedImagePath: String? = null

    override fun start() {}

    override fun loadImages(imageAdapter: ImagesGridAdapter?, dirName: String?, dirPath: String?) {
        if (imageAdapter?.imagesExist() == false) {
            loadImagesAsync = LoadImagesAsync(imageAdapter, dirName, dirPath)
            loadImagesAsync?.execute()
        }
    }

    override fun onGridViewItemClicked(position: Int) {
        if (view.isActive()) {
            view.setSelector()
            loadImagesAsync?.getImagesList()?.let {
                view.showImageDisplayFragment(position, it)
            }
        }
    }

    override fun onGridViewItemLongClicked(position: Int) {
        if (view.isActive()) {
            view.setSelectedPosition(position)
        }
    }

    override fun findImage(path: String, dirName: String, operation: String, selectedItem: String?) {
        val mainDir = File(path)
        if (mainDir.name == dirName) {
            when (operation) {
                OPERATION_DELETE -> {
                    val remainingFiles = mainDir.listFiles()
                    remainingFiles?.forEach { curFile ->
                        if (curFile.absolutePath == selectedItem) {
                            selectedItem?.let { deleteImage(File(it)) }
                        }
                    }
                }
                OPERATION_COPY -> {
                    generateImages(mainDir, selectedItem)
                }
            }
        } else {
            val subDir = File("$path/$dirName/")
            when {
                subDir.exists() -> {
                    when (operation) {
                        OPERATION_DELETE -> {
                            val remainingFiles = subDir.listFiles()
                            remainingFiles?.forEach { curFile ->
                                if (curFile.absolutePath == selectedItem) {
                                    selectedItem?.let { deleteImage(File(it)) }
                                }
                            }
                        }
                        OPERATION_COPY -> {
                            generateImages(subDir, selectedItem)
                        }
                    }
                }
            }
        }
    }

    override fun removeImageFromPhone(path: String?) {
        val fileParent = File(path ?: return).parent
        val dirName = fileParent?.substring(fileParent.lastIndexOf(File.separator) + 1)
        findImage(fileParent ?: return, dirName ?: return, OPERATION_DELETE, path)
    }

    override fun onItemCopyClicked(itemIndex: Int, itemTitle: String, dirPathList: ArrayList<String>) {
        if (!view.isActive()) {
            return
        }
        if (itemIndex < dirPathList.size) {
            val path = dirPathList[itemIndex]
            view.addSelectedImages(itemTitle, path, OPERATION_COPY)
        } else {
            if (view.isActive()) {
                view.showMessage("Selected directory does not exist, please select another directory.",)
            }
        }
    }

    override fun onItemCutClicked(itemIndex: Int, itemTitle: String, dirPathList: ArrayList<String>) {
        if (!view.isActive()) {
            return
        }
        if (itemIndex < dirPathList.size) {
            val path = dirPathList[itemIndex]
            cutSelectedImages(itemTitle, path)
        } else {
            if (view.isActive()) {
                view.showMessage("Selected directory does not exist, please select another directory.",)
            }
        }
    }

    override fun onItemDeleteClicked(itemIndex: Int, itemTitle: String) {
        if (!view.isActive()) {
            return
        }
    }

    private fun generateImages(storageDir: File?, selectedItem: String?) {
        val fileExtension = selectedItem?.substringAfterLast(delimiter = ".", missingDelimiterValue = "Extension Not found")
        val imageFileName = selectedItem?.substring(
            selectedItem.lastIndexOf("/").plus(1),
            selectedItem.lastIndexOf(".")
        )
        try {
            val srcImage = File(selectedItem ?: return)
            val dstImage = imageFileName?.let { File.createTempFile(it, ".$fileExtension", storageDir) }
            val inStream = FileInputStream(srcImage)
            val outStream = FileOutputStream(dstImage)
            val inChannel = inStream.channel
            val outChannel = outStream.channel
            inChannel.transferTo(0, inChannel.size(), outChannel)
            inChannel.close()
            inStream.close()
            outChannel.close()
            outStream.close()
            selectedImagePath = dstImage?.absolutePath
            // Below call is not needed
            //view.sendBroadcastToGallery(selectedImagePath)
        } catch (ignore: Exception) { }
    }

    private fun deleteImage(file: File) {
        if (view.isActive()) {
            view.deleteImage(file)
        }
    }

    private fun cutSelectedImages(dirName: String, path: String) {
        if (view.isActive()) {
            view.addSelectedImages(dirName, path, OPERATION_CUT)
            view.proceedDeletion()
        }
    }

    companion object {
        const val OPERATION_COPY = "copy"
        const val OPERATION_CUT = "cut"
        const val OPERATION_DELETE = "delete"
    }
}