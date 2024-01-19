package com.example.alimu.imagegallery.adapters

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.example.alimu.imagegallery.R

class DirectoriesGridAdapter(private val context: Context) : BaseAdapter() {
    private var selectedPosition = -1
    private val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
    private val cacheSize = maxMemory / 8
    private val memoryCache: LruCache<String, Bitmap?> = object : LruCache<String, Bitmap?>(cacheSize) {
        override fun sizeOf(key: String?, bitmap: Bitmap?): Int {
            return bitmap?.let { it.byteCount / 1024 } ?: 0
        }
    }
    private var dirList = ArrayList<String>()
    private var dirSizeList = ArrayList<Int>()
    private var dirPathList = ArrayList<String>()

    fun setItems(dirList: ArrayList<String>, dirSizeList: ArrayList<Int>,
                 dirPathList: ArrayList<String>) {
        this.dirList = dirList
        this.dirSizeList = dirSizeList
        this.dirPathList = dirPathList
    }

    fun add(path: String) {
        dirList.add(path)
    }

    fun remove(path: String) {
        dirList.remove(path)
    }

    fun setSelectedPosition(position: Int) {
        selectedPosition = position
    }

    fun clear() {
        dirList.clear()
    }

    override fun getCount(): Int {
        return dirList.size
    }

    override fun getItem(position: Int): String {
        return dirList[position]
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var gridViewNew: View
        if (convertView == null) {
            gridViewNew = View(context)

            // get layout from mobile.xml
            val inflater = LayoutInflater.from(context)
            gridViewNew = inflater.inflate(R.layout.grid_view_layout, null)
        } else {
            gridViewNew = convertView
        }
        // set image based on selected text
        val imageView = gridViewNew.findViewById<ImageView>(R.id.img)
        var bitMap: Bitmap? = getBitmapFromMemCache()

        imageView?.layoutParams = RelativeLayout.LayoutParams(350, 350)
        imageView?.scaleType = ImageView.ScaleType.CENTER_CROP
        imageView?.setPadding(8, 8, 8, 8)
        if (bitMap != null) {
            imageView.setImageBitmap(bitMap)
        } else {
            val bmOptions = BitmapFactory.Options()
            bmOptions.inJustDecodeBounds = false
            bmOptions.inSampleSize = 4
            bmOptions.inPurgeable = true
            bitMap = BitmapFactory.decodeResource(context.resources, R.drawable.pattern)
            addBitmapToMemoryCache(bitMap)
        }

        // set value into textview
        if (position < dirList.size) {
            val folderName = gridViewNew.findViewById<TextView>(R.id.folder_name)
            folderName.text = dirList[position]
            val folderSize = gridViewNew.findViewById<TextView>(R.id.folder_size)
            folderSize.text = dirSizeList[position].toString()
        }
        return gridViewNew
    }

    private fun addBitmapToMemoryCache(bitmap: Bitmap?) {
        if (getBitmapFromMemCache() == null) {
            memoryCache.put(IMAGE_KEY, bitmap)
        }
    }

    private fun getBitmapFromMemCache(): Bitmap? {
        return memoryCache.get(IMAGE_KEY)
    }

    companion object {
        private const val IMAGE_KEY = "image_key"
    }
}