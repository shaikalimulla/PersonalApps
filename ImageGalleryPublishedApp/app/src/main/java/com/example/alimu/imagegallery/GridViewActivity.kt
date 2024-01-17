package com.example.alimu.imagegallery

//import com.bumptech.glide.Glide
import android.app.AlertDialog
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.StateListDrawable
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.view.ActionMode
import android.view.Gravity
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.AdapterView.OnItemClickListener
import android.widget.AdapterView.OnItemLongClickListener
import android.widget.BaseAdapter
import android.widget.GridView
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts.StartIntentSenderForResult
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.example.alimu.imagegallery.MainActivity.Companion.EXTRAS_DIR_NAME
import com.example.alimu.imagegallery.MainActivity.Companion.EXTRAS_DIR_PATH
import com.example.alimu.imagegallery.MainActivity.Companion.REQUEST_REFRESH
import com.example.alimu.imagegallery.common.ImageUtil.getUriFromFile
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class GridViewActivity : AppCompatActivity() {
    private var imageList = ArrayList<String>()
    private var gridview: GridView? = null
    private var imageAdapter: ImageAdapter? = null
    private var positionsList = ArrayList<Int>()
    private var selectedImagePath: String? = null
    private var selectCount = 0
    private var dirName: String? = null
    private var dirPath: String? = null
    private var loadFilesAsync: LoadFilesAsync? = null
    private var deleteLauncher: ActivityResultLauncher<IntentSenderRequest>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_grid_view)
        val inflater = supportActionBar?.themedContext
            ?.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val customActionBarView = inflater.inflate(
            R.layout.custom_action_bar, null
        )
        val titleText = customActionBarView.findViewById<TextView>(R.id.title)
        val imgBtn = customActionBarView.findViewById<ImageButton>(R.id.clickBtn)
        imgBtn.visibility = View.GONE
        dirName = intent.extras?.getString(EXTRAS_DIR_NAME)
        dirPath = intent.extras?.getString(EXTRAS_DIR_PATH)
        titleText.text = dirName
        val layoutParams = titleText.layoutParams as RelativeLayout.LayoutParams
        titleText.layoutParams = layoutParams
        titleText.gravity = Gravity.CENTER
        val actionBar = supportActionBar
        actionBar?.setDisplayShowTitleEnabled(false)
        actionBar?.customView = customActionBarView
        actionBar?.setDisplayShowCustomEnabled(true)
        gridview = findViewById(R.id.imgGrid)
        imageAdapter = ImageAdapter(this)
        gridview?.adapter = imageAdapter
        gridview?.choiceMode = GridView.CHOICE_MODE_MULTIPLE_MODAL
        gridview?.setMultiChoiceModeListener(MultiChoiceModeListener())
        gridview?.isDrawSelectorOnTop = true
        gridview?.selector = ResourcesCompat.getDrawable(resources, R.drawable.highlight_image, null)
        loadFilesAsync = LoadFilesAsync(imageAdapter, dirName, dirPath)
        loadFilesAsync?.execute()
        gridview?.onItemClickListener = OnItemClickListener { parent, v, position, id ->
            gridview?.selector = StateListDrawable()
            val intent = Intent(applicationContext, ImageDisplayActivity::class.java)
            intent.putExtra(EXTRAS_FILE_PATH, imageList[position])
            startActivity(intent)
        }
        gridview?.onItemLongClickListener = OnItemLongClickListener { parent, v, position, id ->
            imageAdapter?.setSelectedPosition(position)
            true
        }
        deleteLauncher = registerForActivityResult(StartIntentSenderForResult()) {}
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(applicationContext, MainActivity::class.java)
        startActivityForResult(intent, REQUEST_REFRESH)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_back -> {
                onBackPressed()
                true
            }

            R.id.action_home -> {
                val homeIntent = Intent(this, MainActivity::class.java)
                homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(homeIntent)
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    fun goBack(view: View?) {
        onBackPressed()
    }

    inner class MultiChoiceModeListener : AbsListView.MultiChoiceModeListener {
        private var selecteditem: String? = null
        private var dirName: String? = null
        private var operationSelected = false
        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            mode.title = "Select Items"
            mode.subtitle = "1 item selected"
            val inflater = menuInflater
            inflater.inflate(R.menu.onselect_menu, menu)
            operationSelected = false
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
            return true
        }

        private fun sendBroadcastToGallery(selectedImagePath: String?) {
            val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
            val f: File = if (selectedImagePath == null) {
                File(selecteditem ?: return)
            } else {
                File(selectedImagePath)
            }
            val contentUri = getUriFromFile(applicationContext, "$packageName.fileprovider", f)
            mediaScanIntent.data = contentUri
            applicationContext.sendBroadcast(mediaScanIntent)
        }

        private fun generateImages(storageDir: File?) {
            val fileExtension = selecteditem?.substringAfterLast(delimiter = ".", missingDelimiterValue = "Extension Not found")
            val imageFileName = selecteditem?.substring(
                selecteditem?.lastIndexOf("/")?.plus(1) ?: return,
                selecteditem?.lastIndexOf(".") ?: return
            )
            try {
                val srcImage = File(selecteditem ?: return)
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
                sendBroadcastToGallery(selectedImagePath)
            } catch (ignore: Exception) { }
        }

        private fun getUriWithAppendedId(file: File): Uri? {
            try {// Set up the projection (we only need the ID)
                val projection = arrayOf(MediaStore.Images.Media._ID)

                // Match on the file path
                val selection = MediaStore.Images.Media.DATA + " = ?"
                val selectionArgs = arrayOf(file.absolutePath)

                // Query for the ID of the media matching the file path
                val queryUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                val imageCursor = contentResolver.query(queryUri, projection, selection, selectionArgs, null)
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

        @RequiresApi(Build.VERSION_CODES.R)
        private fun deleteMedia(file: File) {
            getUriWithAppendedId(file)?.let { uriWithAppendedId ->
                val uriList = ArrayList<Uri>()
                uriList.add(uriWithAppendedId)
                val pendingIntent = MediaStore.createDeleteRequest(contentResolver, uriList)
                val senderRequest = IntentSenderRequest.Builder(pendingIntent.intentSender)
                    .setFillInIntent(null)
                    .setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION, 0)
                    .build()
                deleteLauncher?.launch(senderRequest)
            }
        }

        private fun findImage(path: String, dirName: String, operation: String) {
            val mainDir = File(path)
            if (mainDir.name == dirName) {
                when (operation) {
                    OPERATION_DELETE -> {
                        val remainingFiles = mainDir.listFiles()
                        remainingFiles?.forEach { curFile ->
                            if (curFile.absolutePath == selecteditem) {
                                selecteditem?.let { deleteImage(File(it)) }
                            }
                        }
                    }
                    OPERATION_COPY -> {
                        generateImages(mainDir)
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
                                    if (curFile.absolutePath == selecteditem) {
                                        selecteditem?.let { deleteImage(File(it)) }
                                    }
                                }
                            }
                            OPERATION_COPY -> {
                                generateImages(subDir)
                            }
                        }
                    }
                }
            }
        }

        private fun deleteImage(file: File) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                deleteMedia(file)
            } else {
                file.delete()
            }
        }

        private fun removeImageFromPhone(path: String?) {
            val fileParent = File(path ?: return).parent
            dirName = fileParent?.substring(fileParent.lastIndexOf(File.separator) + 1)
            findImage(fileParent ?: return, dirName ?: return, OPERATION_DELETE)
        }

        private fun proceedDeletion() {
            positionsList.sort()
            for (i in positionsList.size - 1 downTo 0) {
                selecteditem = imageAdapter?.getItem(positionsList[i])
                val firstPosition = gridview?.firstVisiblePosition ?: return
                val lastPosition = gridview?.lastVisiblePosition ?: return
                val position = positionsList[i]
                if (position >= firstPosition && position <= lastPosition) {
                    val tv = gridview?.getChildAt(position - firstPosition) as View
                    tv.setBackgroundColor(Color.TRANSPARENT)
                    tv.invalidate()
                }
                imageAdapter?.remove(selecteditem)
                removeImageFromPhone(selecteditem)
            }
            imageAdapter?.notifyDataSetChanged()
            gridview?.isDrawSelectorOnTop = false
            gridview?.invalidateViews()
            positionsList.clear()
        }

        private fun requestPermission() {
            val alert = AlertDialog.Builder(this@GridViewActivity)
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

        private fun addSelectedImages(dirName: String, path: String, operation: String) {
            positionsList.sort()
            for (i in positionsList.size - 1 downTo 0) {
                selecteditem = imageAdapter?.getItem(positionsList[i])
                val firstPosition = gridview?.firstVisiblePosition ?: return
                val lastPosition = gridview?.lastVisiblePosition ?: return
                val position = positionsList[i]
                if (position >= firstPosition && position <= lastPosition) {
                    val tv = gridview?.getChildAt(position - firstPosition) as View
                    tv.setBackgroundColor(Color.TRANSPARENT)
                    tv.invalidate()
                }
                // copy and cut has same logic so passing "copy" here
                findImage(path, dirName, OPERATION_COPY)
                val fileParent = File(selecteditem?: return).parent
                val currentDirName = fileParent?.substring(fileParent.lastIndexOf(File.separator) + 1)
                if (currentDirName == dirName) {
                    imageAdapter?.add(selectedImagePath)
                }
            }
            imageAdapter?.notifyDataSetChanged()
            gridview?.isDrawSelectorOnTop = false
            gridview?.invalidateViews()
            if (operation == OPERATION_COPY) {
                positionsList.clear()
            }
        }

        private fun cutSelectedImages(dirName: String, path: String) {
            addSelectedImages(dirName, path, OPERATION_CUT)
            proceedDeletion()
        }

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            return when (item.itemId) {
                R.id.item_copy -> {
                    operationSelected = true
                    val popupCopyMenu = PopupMenu(this@GridViewActivity, findViewById(R.id.item_copy))
                    var i = 0
                    while (i < MainActivity.dirList.size) {
                        popupCopyMenu.menu.add(Menu.NONE, i + 1, Menu.NONE, MainActivity.dirList[i])
                        i++
                    }
                    popupCopyMenu.setOnMenuItemClickListener { menuItem ->
                        val itemIndex = menuItem.itemId - 1 // itemId starts with 1
                        if (itemIndex < MainActivity.dirPathList.size) {
                            val path = MainActivity.dirPathList[itemIndex]
                            addSelectedImages(menuItem.title.toString(), path, OPERATION_COPY)
                        } else {
                            Toast.makeText(
                                this@GridViewActivity,
                                "Selected directory does not exist, please select another directory.",
                                Toast.LENGTH_SHORT
                            ).show()    
                        }
                        mode.finish()
                        true
                    }
                    popupCopyMenu.show()
                    true
                }

                R.id.item_delete -> {
                    operationSelected = true
                    requestPermission()
                    mode.finish()
                    true
                }

                R.id.item_cut -> {
                    val popupCutMenu = PopupMenu(this@GridViewActivity, findViewById(R.id.item_copy))
                    var i = 0
                    while (i < MainActivity.dirList.size) {
                        popupCutMenu.menu.add(Menu.NONE, i + 1, Menu.NONE, MainActivity.dirList[i])
                        i++
                    }
                    popupCutMenu.setOnMenuItemClickListener { menuItem ->
                        val itemIndex = menuItem.itemId - 1 // itemId starts with 1
                        if (itemIndex < MainActivity.dirPathList.size) {
                            val path = MainActivity.dirPathList[itemIndex]
                            cutSelectedImages(menuItem.title.toString(), path)
                        } else {
                            Toast.makeText(
                                this@GridViewActivity,
                                "Selected directory does not exist, please select another directory.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        mode.finish()
                        true
                    }
                    popupCutMenu.show()
                    true
                }

                else -> false
            }
        }

        private fun destroyView() {
            for (i in positionsList.size - 1 downTo 0) {
                selecteditem = imageAdapter?.getItem(positionsList[i])
                val firstPosition = gridview?.firstVisiblePosition ?: return
                val lastPosition = gridview?.lastVisiblePosition ?: return
                val position = positionsList[i]
                if (position >= firstPosition && position <= lastPosition) {
                    val tv = gridview?.getChildAt(position - firstPosition) as View
                    tv.setBackgroundColor(Color.TRANSPARENT)
                    tv.invalidate()
                }
            }
            imageAdapter?.notifyDataSetChanged()
            gridview?.isDrawSelectorOnTop = false
            gridview?.invalidateViews()
            positionsList.clear()
        }

        override fun onDestroyActionMode(mode: ActionMode) {
            if (operationSelected) {
                return
            }
            destroyView()
        }

        override fun onItemCheckedStateChanged(
            mode: ActionMode, position: Int, id: Long,
            checked: Boolean
        ) {
            selectCount = gridview?.checkedItemCount ?: 0
            val firstPosition = gridview?.firstVisiblePosition ?: 0
            val lastPosition = gridview?.lastVisiblePosition ?: 0
            gridview?.selector = ResourcesCompat.getDrawable(resources, R.drawable.highlight_image, null)
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
    }

    class LoadFilesAsync(
        private var imageAdapter: ImageAdapter?,
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
    }

    inner class ImageAdapter(private val cont: Context) : BaseAdapter() {
        private var selectedPosition = -1
        override fun getCount(): Int {
            return imageList.size
        }

        override fun getItem(position: Int): String {
            return imageList[position]
        }

        override fun getItemId(position: Int): Long {
            return 0
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

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val imageView: ImageView
            if (convertView == null) {
                imageView = ImageView(cont)
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

    companion object {
        private const val OPERATION_COPY = "copy"
        private const val OPERATION_CUT = "cut"
        private const val OPERATION_DELETE = "delete"
        private const val EXTRAS_FILE_PATH = "path"
    }
}