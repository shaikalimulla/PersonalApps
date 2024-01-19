package com.example.alimu.imagegallery.adapters

//import com.bumptech.glide.Glide
import android.content.Context
import android.graphics.BitmapFactory
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.BaseAdapter
import android.widget.ImageView

class ImagesGridAdapter(private val context: Context) : BaseAdapter() {
    private var selectedPosition = -1
    private var imageList = ArrayList<String>()

    fun getItems(): ArrayList<String> {
        return this.imageList
    }

    fun add(path: String?) {
        imageList.add(path ?: return)
    }

    fun remove(path: String?) {
        imageList.remove(path)
    }

    fun setSelectedPosition(position: Int) {
        selectedPosition = position
    }

    fun clear() {
        imageList.clear()
    }

    fun imagesExist() = imageList.isNotEmpty()

    override fun getCount(): Int {
        return imageList.size
    }

    override fun getItem(position: Int): String {
        return imageList[position]
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val imageView: ImageView
        if (convertView == null) {
            imageView = ImageView(context)
            imageView.layoutParams = AbsListView.LayoutParams(300, 300)
            imageView.scaleType = ImageView.ScaleType.CENTER_CROP
            imageView.setPadding(8, 8, 8, 8)
        } else {
            imageView = convertView as ImageView
        }
        val bmOptions = BitmapFactory.Options()
        bmOptions.inJustDecodeBounds = false
        bmOptions.inSampleSize = 4
        bmOptions.inPurgeable = true
        val bitmap = BitmapFactory.decodeFile(imageList[position], bmOptions)
        imageView.setImageBitmap(bitmap)

        /*Glide.with(imageView.context)
            .asBitmap()
            .load(bitmap)
            .into(imageView)*/

        return imageView
    }
}