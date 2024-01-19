package com.example.alimu.imagegallery

import android.os.AsyncTask
import android.os.Handler
import android.os.Looper
import com.example.alimu.imagegallery.adapters.ImagesGridAdapter
import java.io.File

class LoadImagesAsync(
    private var imageAdapter: ImagesGridAdapter?,
    private val dirName: String?,
    private val dirPath: String?
) : AsyncTask<Void?, String?, Void?>() {
    @Deprecated("Deprecated in Java")
    override fun onPreExecute() {
        imageAdapter?.clear()
        super.onPreExecute()
    }

    @Deprecated("Deprecated in Java")
    override fun doInBackground(vararg params: Void?): Void? {
        dirPath?.let { getAllImages(it) }
        return null
    }

    fun getImagesList(): ArrayList<String>? {
        return imageAdapter?.getItems()
    }

    private fun getAllImages(path: String) {
        val mainDir = File(path)
        if (mainDir.name == dirName) {
            addImagesToGrid(mainDir)
        } else {
            val subDir = File("$path/$dirName/")
            if (subDir.exists()) {
                addImagesToGrid(subDir)
            }
        }
    }

    private fun addImagesToGrid(dir: File) {
        val remainingFiles = dir.listFiles()
        remainingFiles?.forEach { curFile ->
            if (curFile.isDirectory.not()) {
                imageAdapter?.add(curFile.absolutePath)
            }
        }

        Handler(Looper.getMainLooper()).post {
            imageAdapter?.notifyDataSetChanged()
        }
    }
}