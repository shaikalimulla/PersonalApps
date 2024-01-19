package com.example.alimu.imagegallery.ui.imagesgridview

import androidx.fragment.app.Fragment
import android.app.AlertDialog
import android.content.ContentUris
import android.content.Context.LAYOUT_INFLATER_SERVICE
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.StateListDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.ActionMode
import android.view.Gravity
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView.OnItemClickListener
import android.widget.AdapterView.OnItemLongClickListener
import android.widget.GridView
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts.StartIntentSenderForResult
import androidx.annotation.RequiresApi
import androidx.core.content.res.ResourcesCompat
import com.example.alimu.imagegallery.R
import com.example.alimu.imagegallery.adapters.ImagesGridAdapter
import com.example.alimu.imagegallery.common.FragmentHelper
import com.example.alimu.imagegallery.ui.main.MainActivity
import com.example.alimu.imagegallery.listeners.ImagesGridListener
import com.example.alimu.imagegallery.listeners.MultiChoiceModeListener
import com.example.alimu.imagegallery.ui.imagedisplay.ImageDisplayFragment
import com.example.alimu.imagegallery.ui.imagesgridview.ImagesGridPresenter.Companion.OPERATION_COPY
import java.io.File

class ImagesGridFragment: Fragment(), ImagesGridContract.View, ImagesGridListener {
    private var gridview: GridView? = null
    private var imageAdapter: ImagesGridAdapter? = null
    private var dirList = ArrayList<String>()
    private var dirPathList = ArrayList<String>()
    private var dirName: String? = null
    private var dirPath: String? = null
    private var selectedImagePath: String? = null
    private var selectedItem: String? = null
    private var positionsList = ArrayList<Int>()
    private var deleteLauncher: ActivityResultLauncher<IntentSenderRequest>? = null
    private lateinit var presenter: ImagesGridContract.Presenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.getInt(EXTRAS_POSITION)?.let { position ->
            dirList = arguments?.getStringArrayList(EXTRAS_DIR_NAME) as ArrayList<String>
            dirPathList = arguments?.getStringArrayList(EXTRAS_DIR_PATH) as ArrayList<String>
            dirName = dirList[position]
            dirPath = dirPathList[position]
        }

        deleteLauncher = registerForActivityResult(StartIntentSenderForResult()) {}

        presenter = ImagesGridPresenter(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_images_grid_view, container, false)

        val baseActivity = activity as? MainActivity
        val actionBar = baseActivity?.supportActionBar
        val actionBarInflater = actionBar?.themedContext
            ?.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val customActionBarView = actionBarInflater.inflate(
            R.layout.custom_action_bar, null
        )
        val titleText = customActionBarView.findViewById<TextView>(R.id.title)
        val imgBtn = customActionBarView.findViewById<ImageButton>(R.id.clickBtn)
        imgBtn.visibility = View.GONE
        titleText.text = dirName
        val layoutParams = titleText.layoutParams as RelativeLayout.LayoutParams
        titleText.layoutParams = layoutParams
        titleText.gravity = Gravity.CENTER
        actionBar.setDisplayShowTitleEnabled(false)
        actionBar.customView = customActionBarView
        actionBar.setDisplayShowCustomEnabled(true)
        gridview = view.findViewById(R.id.image_grid_view)
        imageAdapter = ImagesGridAdapter(baseActivity.applicationContext)
        gridview?.adapter = imageAdapter
        gridview?.choiceMode = GridView.CHOICE_MODE_MULTIPLE_MODAL
        gridview?.setMultiChoiceModeListener(MultiChoiceModeListener(this))
        gridview?.isDrawSelectorOnTop = true
        gridview?.selector = ResourcesCompat.getDrawable(resources, R.drawable.highlight_image, null)
        gridview?.onItemClickListener = OnItemClickListener { parent, v, position, id ->
            presenter.onGridViewItemClicked(position)
        }
        gridview?.onItemLongClickListener = OnItemLongClickListener { parent, v, position, id ->
            presenter.onGridViewItemLongClicked(position)
            true
        }

        presenter.loadImages(imageAdapter, dirName, dirPath)

        return view
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_back -> {
                back()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroyView() {
        imageAdapter?.clear()
        gridview = null
        deleteLauncher = null
        super.onDestroyView()
    }

    fun goBack(view: View?) {
        back()
    }

    override fun isActive() = isAdded

    override fun setSelector() {
        gridview?.selector = StateListDrawable()
    }
    
    override fun setSelectedPosition(position: Int) {
        imageAdapter?.setSelectedPosition(position)
    }

    override fun showImageDisplayFragment(position: Int, imageList: ArrayList<String>) {
        FragmentHelper.show(
            this,
            ImageDisplayFragment.newInstance(imageList[position]),
            ImageDisplayFragment.TAG
        )
    }

    override fun showMessage(message: String) {
        Toast.makeText(
            activity,
            message,
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun addSelectedImages(dirName: String, path: String, operation: String) {
        positionsList.sort()
        for (i in positionsList.size - 1 downTo 0) {
            selectedItem = imageAdapter?.getItem(positionsList[i])
            val firstPosition = gridview?.firstVisiblePosition ?: return
            val lastPosition = gridview?.lastVisiblePosition ?: return
            val position = positionsList[i]
            if (position >= firstPosition && position <= lastPosition) {
                val tv = gridview?.getChildAt(position - firstPosition) as View
                tv.setBackgroundColor(Color.TRANSPARENT)
                tv.invalidate()
            }
            // copy and cut has same logic so passing "copy" here
            presenter.findImage(path, dirName, OPERATION_COPY, selectedItem)
            val fileParent = File(selectedItem?: return).parent
            val currentDirName = fileParent?.substring(fileParent.lastIndexOf(File.separator) + 1)
            if (currentDirName == dirName) {
                imageAdapter?.add(selectedImagePath)
            }
        }
        refreshViews(operation == OPERATION_COPY)
    }

    override fun proceedDeletion() {
        positionsList.sort()
        for (i in positionsList.size - 1 downTo 0) {
            selectedItem = imageAdapter?.getItem(positionsList[i])
            val firstPosition = gridview?.firstVisiblePosition ?: return
            val lastPosition = gridview?.lastVisiblePosition ?: return
            val position = positionsList[i]
            if (position >= firstPosition && position <= lastPosition) {
                val tv = gridview?.getChildAt(position - firstPosition) as View
                tv.setBackgroundColor(Color.TRANSPARENT)
                tv.invalidate()
            }
            imageAdapter?.remove(selectedItem)
            presenter.removeImageFromPhone(selectedItem)
        }
        refreshViews(true)
    }

    override fun deleteImage(file: File) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            deleteMedia(file)
        } else {
            file.delete()
        }
    }

    override fun onItemCopyClicked(mode: ActionMode) {
        // passing Activity reference as context here to apply custom styles
        val popupCopyMenu = PopupMenu(activity, activity?.findViewById(R.id.item_copy))
        var i = 0
        while (i < dirList.size) {
            popupCopyMenu.menu.add(Menu.NONE, i + 1, Menu.NONE, dirList[i])
            i++
        }
        popupCopyMenu.setOnMenuItemClickListener { menuItem ->
            // itemId starts with 1
            presenter.onItemCopyClicked(menuItem.itemId - 1,
                menuItem.title.toString(), dirPathList)
            mode.finish()
            true
        }
        popupCopyMenu.show()
    }

    override fun onItemCutClicked(mode: ActionMode) {
        val popupCutMenu = PopupMenu(activity, activity?.findViewById(R.id.item_copy))
        var i = 0
        while (i < dirList.size) {
            popupCutMenu.menu.add(Menu.NONE, i + 1, Menu.NONE, dirList[i])
            i++
        }
        popupCutMenu.setOnMenuItemClickListener { menuItem ->
            // itemId starts with 1
            presenter.onItemCutClicked(menuItem.itemId - 1,
                menuItem.title.toString(), dirPathList)
            mode.finish()
            true
        }
        popupCutMenu.show()
    }

    override fun onItemDeleteClicked(mode: ActionMode) {
        requestPermission()
        mode.finish()
    }

    override fun onItemCheckedStateChanged(
        mode: ActionMode,
        position: Int,
        id: Long,
        checked: Boolean
    ) {
        val selectCount = gridview?.checkedItemCount ?: 0
        val firstPosition = gridview?.firstVisiblePosition ?: 0
        val lastPosition = gridview?.lastVisiblePosition ?: 0
        gridview?.selector = ResourcesCompat.getDrawable(resources,
            R.drawable.highlight_image, null)
        if (checked) {
            if (position >= firstPosition && position <= lastPosition) {
                val tv = gridview?.getChildAt(position - firstPosition) as View
                tv.setBackgroundColor(Color.RED)
                tv.invalidate()
            }
            positionsList.add(position)
        } else {
            if (position >= firstPosition && position <= lastPosition) {
                val tv = gridview?.getChildAt(position - firstPosition) as View
                tv.setBackgroundColor(Color.TRANSPARENT)
                tv.invalidate()
                gridview?.selector = StateListDrawable()
            }
            positionsList.remove(position)
        }
        when (selectCount) {
            1 -> mode.subtitle = "1 item selected"
            else -> mode.subtitle = "$selectCount items selected"
        }
    }

    override fun destroyView() {
        for (i in positionsList.size - 1 downTo 0) {
            selectedItem = imageAdapter?.getItem(positionsList[i])
            val firstPosition = gridview?.firstVisiblePosition ?: return
            val lastPosition = gridview?.lastVisiblePosition ?: return
            val position = positionsList[i]
            if (position >= firstPosition && position <= lastPosition) {
                val tv = gridview?.getChildAt(position - firstPosition) as View
                tv.setBackgroundColor(Color.TRANSPARENT)
                tv.invalidate()
            }
        }
        refreshViews(true)
    }

    private fun refreshViews(clearPositionsList: Boolean) {
        imageAdapter?.notifyDataSetChanged()
        gridview?.isDrawSelectorOnTop = false
        gridview?.invalidateViews()
        if (clearPositionsList) {
            positionsList.clear()
        }
    }

    private fun requestPermission() {
        val alert = AlertDialog.Builder(context)
        alert.setMessage("Are you sure you want to delete the file. Deleting from the app will permanently delete the image from the phone.")
        alert.setCancelable(true)
        alert.setPositiveButton(
            "Yes"
        ) { _, _ -> proceedDeletion() }
        alert.setNegativeButton(
            "No"
        ) { dialog, _ ->
            dialog.cancel()
            destroyView()
            //positionsList.clear();
        }
        alert.create().show()
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun deleteMedia(file: File) {
        getUriWithAppendedId(file)?.let { uriWithAppendedId ->
            val uriList = ArrayList<Uri>()
            uriList.add(uriWithAppendedId)
            val pendingIntent = MediaStore.createDeleteRequest(
                context?.contentResolver ?: return, uriList)
            val senderRequest = IntentSenderRequest.Builder(pendingIntent.intentSender)
                .setFillInIntent(null)
                .setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION, 0)
                .build()
            deleteLauncher?.launch(senderRequest)
        }
    }

    /*private fun sendBroadcastToGallery(selectedImagePath: String?) {
        val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
        val f: File = if (selectedImagePath == null) {
            File(selectedItem ?: return)
        } else {
            File(selectedImagePath)
        }
        val contentUri = getUriFromFile(context, "${activity.packageName}.fileprovider", f)
        mediaScanIntent.data = contentUri
        context.sendBroadcast(mediaScanIntent)
    }*/

    private fun getUriWithAppendedId(file: File): Uri? {
        try {// Set up the projection (we only need the ID)
            val projection = arrayOf(MediaStore.Images.Media._ID)

            // Match on the file path
            val selection = MediaStore.Images.Media.DATA + " = ?"
            val selectionArgs = arrayOf(file.absolutePath)

            // Query for the ID of the media matching the file path
            val queryUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            val imageCursor = context?.contentResolver?.query(queryUri, projection, selection, selectionArgs, null)
            imageCursor?.let { cursor ->
                if (cursor.moveToFirst()) {
                    // We found the ID. Deleting the item via the content provider will also remove the file
                    val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID))
                    return ContentUris.withAppendedId(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        id
                    )
                }
                cursor.close()
            }
        } catch (ignore: Exception) { }
        return null
    }

    private fun back() {
        val fragmentManager = parentFragmentManager
        if (fragmentManager.backStackEntryCount > 1) {
            fragmentManager.popBackStack()
        } else {
            activity?.finish()
        }
    }

    companion object {
        const val TAG = "ImagesGridFragment"
        private const val EXTRAS_DIR_PATH = "dir_path"
        private const val EXTRAS_DIR_NAME = "dir_name"
        private const val EXTRAS_POSITION = "position"

        fun newInstance(dirList: ArrayList<String>, dirPathList: ArrayList<String>, position: Int): ImagesGridFragment {
            val imagesGridFragment = ImagesGridFragment()
            val bundle = Bundle()
            bundle.putStringArrayList(EXTRAS_DIR_NAME, dirList)
            bundle.putStringArrayList(EXTRAS_DIR_PATH, dirPathList)
            bundle.putInt(EXTRAS_POSITION, position)
            imagesGridFragment.arguments = bundle
            return imagesGridFragment
        }
    }
}