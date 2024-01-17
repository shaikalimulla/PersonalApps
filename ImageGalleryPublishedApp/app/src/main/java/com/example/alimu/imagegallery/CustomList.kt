package com.example.alimu.imagegallery

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView

class CustomList(
    private val context: Activity,
    dirList: ArrayList<String>,
    dirSizeList: ArrayList<Int>
) : ArrayAdapter<String?>(
    context, R.layout.list_row_view, dirList as List<String?>
) {
    private var dirList = ArrayList<String>()
    private var dirSizeList = ArrayList<Int>()

    init {
        this.dirList = dirList
        this.dirSizeList = dirSizeList
    }

    override fun getView(position: Int, view: View?, parent: ViewGroup): View {
        val inflater = context.layoutInflater
        val rowView = inflater.inflate(R.layout.list_row_view, null, true)
        val txtTitle = rowView.findViewById<View>(R.id.txt) as TextView
        val folderSize = rowView.findViewById<View>(R.id.folder_size) as TextView
        val imageView = rowView.findViewById<View>(R.id.img) as ImageView
        txtTitle.text = dirList[position]
        folderSize.text = dirSizeList[position].toString()
        imageView.setImageResource(R.drawable.pattern)
        return rowView
    }
}