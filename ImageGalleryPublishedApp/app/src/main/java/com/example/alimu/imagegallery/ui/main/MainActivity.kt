package com.example.alimu.imagegallery.ui.main

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.LruCache
import android.view.KeyEvent
import android.view.View
import android.widget.GridView
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.alimu.imagegallery.R
import com.example.alimu.imagegallery.common.FragmentHelper
import com.example.alimu.imagegallery.common.PermissionUtil.areMediaPermissionsGranted
import com.example.alimu.imagegallery.common.PermissionUtil.requestMediaPermissions
import com.example.alimu.imagegallery.ui.directoriesgridview.DirectoriesGridFragment

/*
References:
1. https://developer.android.com/guide/topics/ui/layout/listview.html
2. https://developer.android.com/guide/topics/ui/layout/gridview.html
3. https://developer.android.com/training/camera/photobasics.html
4. https://material.io/icons/#ic_content_cut
5. https://www.google.com/search?q=folder+icons&espv=2&biw=1366&bih=662&source=lnms&tbm=isch&sa=X&ved=0ahUKEwj0q9yBgM3QAhXBxYMKHU37ALUQ_AUIBigB#tbm=isch&q=folder+icons&chips=q:folder+icons,g_2:red&imgrc=LV4afJEUIaWGkM%3A
6. https://www.google.com/search?q=camera+icon+512X512&tbm=isch&tbs=rimg:CXlwiaLMFoJjIjiY0k8oLS8FEgXWMk6itMyuEmnjqVfSQmKc6DBQaHJmkMxLqp0HMpRtAto8LRUNpVk6B1lPvDIKtyoSCZjSTygtLwUSEbj0sfoZnNbaKhIJBdYyTqK0zK4Rd_1XzoLb3s5cqEgkSaeOpV9JCYhGGcj5RjqrbwioSCZzoMFBocmaQEfeo9vSS2twWKhIJzEuqnQcylG0RjGT7YTftjIQqEgkC2jwtFQ2lWRESMfff4058-SoSCToHWU-8Mgq3ERZzhH5qSw-R&tbo=u#imgrc=eXCJoswWgmO_pM%3A
7. https://images.search.yahoo.com/yhs/search;_ylt=A0LEVvcQDkFY5SMAlcEnnIlQ?p=gallery+icon+512+512&fr=yhs-mozilla-002&fr2=piv-web&hspart=mozilla&hsimp=yhs-002#id=38&iurl=https%3A%2F%2Fcdn2.iconfinder.com%2Fdata%2Ficons%2Fios-7-style-metro-ui-icons%2F512%2FMetroUI_Windows8_Photos.png&action=click
8. https://www.google.com/search?q=images+folder&espv=2&biw=1366&bih=662&source=lnms&tbm=isch&sa=X&ved=0ahUKEwjZ6dmXpY7RAhUo1oMKHdRJAQQQ_AUIBigB#q=pictures+folder&tbm=isch&tbs=rimg:Ce26CZhuaLFtIjgw0WBr-Q4yRbiXSMGMZLdczhe9abcf-ATEjPg1HtVOQPlTnggVyNUj3r1qR8LaCzDmMYJN5rNmcioSCTDRYGv5DjJFEYuJyVsgLrqcKhIJuJdIwYxkt1wR8_1VDJrW3lj4qEgnOF71ptx_14BBGEXUahkVSFHioSCcSM-DUe1U5AET5Glhv8I0-CKhIJ-VOeCBXI1SMRxwqEGHM34ukqEgnevWpHwtoLMBElznHw_198xjioSCeYxgk3ms2ZyEb536eI_1SXv2&imgdii=TZsqZwaX9GaUDM%3A%3BTZsqZwaX9GaUDM%3A%3BXsthefvqhUZAHM%3A&imgrc=TZsqZwaX9GaUDM%3A
*/
class MainActivity : AppCompatActivity(), MainContract.View {
    var dirSizeList = ArrayList<Int>()
    @JvmField
    var currentImagePath: String? = null
    private var clickImageButton: ImageButton? = null
    private var gridview: GridView? = null
    //private var imageAdapter: ImageAdapter? = null
    private var memoryCache: LruCache<String, Bitmap?>? = null
    private lateinit var permissionResultLauncher: ActivityResultLauncher<Array<String>>
    private var photoUri: Uri? = null
    private lateinit var presenter: MainContract.Presenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        presenter = MainPresenter(this)

        permissionResultLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            onRequestPermissionResult()
        }

        presenter.start()
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 0 && grantResults.isNotEmpty()) {
            var permissionGrantedCount = 0
            grantResults.forEach { value ->
                if (value == PackageManager.PERMISSION_GRANTED) {
                    permissionGrantedCount++
                }
            }
            if (grantResults.size == permissionGrantedCount) {
                // All permissions granted
                clickImageButton?.isEnabled = true
                val intent = Intent(applicationContext, MainActivity::class.java)
                startActivity(intent)
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        back()
        return true
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && event?.repeatCount == 0) {
            back()

            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun isActive(): Boolean {
        return !isFinishing && !isDestroyed
    }

    override fun areMediaPermissionsGranted() = areMediaPermissionsGranted(this.applicationContext)

    override fun showDirectoriesGridFragment() {
        if (isActive().not()) {
            return
        }
        FragmentHelper.showAsTop(
            this,
            DirectoriesGridFragment.newInstance(),
            DirectoriesGridFragment.TAG
        )
    }

    override fun requestMediaPermissions() {
        requestMediaPermissions(permissionResultLauncher)
    }

    fun goBack(view: View?) {
        back()
    }

    private fun onRequestPermissionResult() {
        if (areMediaPermissionsGranted(this.applicationContext)) {
            showDirectoriesGridFragment()
        } else {
            Toast.makeText(
                applicationContext,
                "Please accept media permissions to continue the app",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun back() {
        val fragmentManager = supportFragmentManager
        if (fragmentManager.backStackEntryCount > 1) {
            fragmentManager.popBackStack()
        } else {
            super.finish()
        }
    }

    /*override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = MenuInflater(this)
        inflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_back -> {
                back()
                true
            }

            R.id.action_speech -> {
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
                intent.putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                )
                startActivityForResult(intent, REQUEST_VOICE_SPEECH)
                true
            }

            R.id.action_info -> {
                val alert = AlertDialog.Builder(this@MainActivity)
                alert.setMessage(
                    "1. This app will get all the images present in different folders from the phone and displays folder structure with images count as list view in the home page. \n" +
                            "2. Tap on any folder from the list to load images present in it in the gridview. \n" +
                            "3. Select any image in the gridview to open in full screen mode in separate page.\n" +
                            "4. Tap on Camera icon at the top left screen to open Camera to click instant picture. \n" +
                            "5. Tap on mic button at top right to open Google voice which opens Camera when you say \" Camera \". \n" +
                            "6. All the pictures taken from this app through camera will be stored in Pictures folder by default.\n" +
                            "7. Use long press gesture on image to perform operations like Copy, Cut and Delete. You can select multiple images.\n" +
                            "8. Use Back arrow icon present at top right of the screen to navigate back to previous page. \n" +
                            "9. Overflow icon at the top right have two options Info and Home. Tap on Info to view this page which summarizes app details. \n" +
                            "10. Tap on Home option to navigate back to home page from any screen. "
                )
                alert.setCancelable(true)
                alert.setNegativeButton(
                    "Ok"
                ) { dialog, _ -> dialog.cancel() }
                alert.create().show()
                true
            }

            R.id.action_home -> {
                val homeIntent = Intent(this, MainActivity::class.java)
                homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(homeIntent)
                true
            }

            R.id.action_policy -> {
                val policyUrl =
                    "https://sites.google.com/s/0B6dfwLMBeuAvT3dhcGRONHBiVjQ/p/0B6dfwLMBeuAvT1hQWWEtRWZUZDg/edit"
                //"https://docs.google.com/document/d/1NhrWFxYGfkn0-QhYyOeLXOKqQOH_HbMVS_I6z3qLV4M/pub";
                //"https://docs.google.com/document/u/1/d/1NhrWFxYGfkn0-QhYyOeLXOKqQOH_HbMVS_I6z3qLV4M/pub"
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(policyUrl))
                startActivity(browserIntent)
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    fun goBack(view: View?) {
        back()
    }

    fun clickPic(view: View?) {
        val clickPicIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (clickPicIntent.resolveActivity(packageManager) != null) {
            var image: File? = null
            try {
                image = generateImage()
            } catch (e : IOException) {
                e.printStackTrace()
            }
            if (image != null) {
                photoUri = getUriFromFile(applicationContext, "$packageName.fileprovider", image)
                clickPicIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri.toString())
                startActivityForResult(clickPicIntent, REQUEST_TAKE_PHOTO)
            }
        }
    }

    public override fun onSaveInstanceState(saveState: Bundle) {
        super.onSaveInstanceState(saveState)
        saveState.putString(CURRENT_IMAGE_PATH, currentImagePath)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when {
            requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK -> {
                sendImageToGallery(data)
            }
            requestCode == REQUEST_TAKE_PHOTO && resultCode != RESULT_OK -> {
                currentImagePath?.let { File(it).delete() }
                sendBroadcastToGallery()
            }
            requestCode == REQUEST_VOICE_SPEECH && resultCode == RESULT_OK -> {
                val voiceText = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.get(0)
                Toast.makeText(applicationContext, voiceText, Toast.LENGTH_LONG).show()
                if (voiceText == CAMERA_TEXT) {
                    val c: Context = this
                    val view = View.inflate(c, R.layout.activity_main, null)
                    clickPic(view)
                }
            }
            requestCode == REQUEST_REFRESH && resultCode == RESULT_OK -> {
                gridview = findViewById(R.id.folderGrid)
                imageAdapter = ImageAdapter(this)
                gridview?.adapter = imageAdapter
            }
        }
    }

    private fun initializeViews(savedInstanceState: Bundle?) {
        val inflater = supportActionBar?.themedContext?.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val customActionBarView = inflater.inflate(
            R.layout.custom_action_bar, null
        )
        if (this.javaClass.simpleName == TAG) {
            val imgBtn = customActionBarView.findViewById<ImageButton>(R.id.action_back)
            imgBtn.visibility = View.INVISIBLE
        }
        val titleText = customActionBarView.findViewById<TextView>(R.id.title)
        val layoutParams = titleText.layoutParams as RelativeLayout.LayoutParams
        titleText.layoutParams = layoutParams
        titleText.gravity = Gravity.CENTER
        val actionBar = supportActionBar
        actionBar?.setDisplayShowTitleEnabled(false)
        actionBar?.customView = customActionBarView
        actionBar?.setDisplayShowCustomEnabled(true)
        if (savedInstanceState != null) {
            currentImagePath = savedInstanceState.getString(CURRENT_IMAGE_PATH)
        }
        dirList.clear()
        dirPathList.clear()

        val imagesDirPath = getImageDirectoriesPath()
        imagesDirPath?.forEach { path ->
            getFolderSize(path)
        }

        val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
        val cacheSize = maxMemory / 8
        memoryCache = object : LruCache<String, Bitmap?>(cacheSize) {
            override fun sizeOf(key: String?, bitmap: Bitmap?): Int {
                return bitmap?.let { it.byteCount / 1024 } ?: 0
            }
        }
        gridview = findViewById(R.id.folderGrid)
        imageAdapter = ImageAdapter(this)
        gridview?.adapter = imageAdapter
        gridview?.onItemClickListener = OnItemClickListener { parent, v, position, id ->
            gridview?.selector = StateListDrawable()
            val intent = Intent(applicationContext, GridViewActivity::class.java)
            intent.putExtra(EXTRAS_DIR_NAME, dirList[position])
            intent.putExtra(EXTRAS_DIR_PATH, dirPathList[position])
            startActivity(intent)
        }
        clickImageButton = customActionBarView.findViewById(R.id.clickBtn)
    }

    private fun getImageDirectoriesPath(): HashSet<String>? {
        val dirPathSet = hashSetOf<String>()
        val dcimDirPath = "/${Environment.DIRECTORY_DCIM}/"
        val picturesPath = "/${Environment.DIRECTORY_PICTURES}/"
        try {
            val projection = arrayOf(
                MediaStore.Images.ImageColumns._ID,
                MediaStore.Images.ImageColumns.DATA,
                MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME,
                MediaStore.Images.ImageColumns.DATE_TAKEN,
                MediaStore.Images.ImageColumns.MIME_TYPE
            )
            val imageCursor = contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC"
            )
            imageCursor?.let { cursor ->
                cursor.moveToFirst()
                do {
                    val path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA))
                    when {
                        path.contains(dcimDirPath) -> {
                            val substringPath = File(path).parent?.substringBefore(dcimDirPath).plus(dcimDirPath)
                            dirPathSet.add(substringPath)
                        }
                        path.contains(picturesPath) -> {
                            val substringPath = File(path).parent?.substringBefore(picturesPath).plus(picturesPath)
                            dirPathSet.add(substringPath)
                        }
                    }
                } while (cursor.moveToNext())
                cursor.close()
                return dirPathSet
            }
        } catch (ignore: Exception) { }
        return null
    }

    @Throws(IOException::class)
    private fun generateImage(): File {
        val time = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CANADA).format(Date())
        val imageFileName = PHOTO_PREFIX + time + "_"
        val picIndex = dirList.indexOf("Pictures")
        val picPath = dirPathList[picIndex]
        val image = File.createTempFile(imageFileName, ".jpg", File(picPath))
        currentImagePath = image.absolutePath
        return image
    }

    private fun sendImageToGallery(data: Intent?) {
        if (data?.hasExtra("data") == true) {
            val bitmap: Bitmap = data.extras?.get("data") as Bitmap
            try {
                photoUri?.let {
                    val outputStream = contentResolver.openOutputStream(it)
                    if (outputStream != null) {
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
                    }
                    outputStream?.close()
                }
            } catch (ignore: FileNotFoundException) { }

            val picIndex = dirList.indexOf("Pictures")
            dirSizeList[picIndex] = dirSizeList[picIndex] + 1
            imageAdapter?.notifyDataSetChanged()
        }
    }

    private fun sendBroadcastToGallery() {
        val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
        val f = File(currentImagePath ?: return)
        val contentUri = getUriFromFile(applicationContext,  "$packageName.fileprovider", f)
        mediaScanIntent.data = contentUri
        applicationContext.sendBroadcast(mediaScanIntent)
    }

    private fun getFolderSize(path: String) {
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

    private fun back() {
        val fragmentManager = supportFragmentManager
        if (fragmentManager.backStackEntryCount > 1) {
            fragmentManager.popBackStack()
        } else {
            super.finish()
        }
    }

    inner class ImageAdapter(private val cont: Context) : BaseAdapter() {
        private var selectedPosition = -1
        override fun getCount(): Int {
            return dirList.size
        }

        override fun getItem(position: Int): String {
            return dirList[position]
        }

        override fun getItemId(position: Int): Long {
            return 0
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

        private fun addBitmapToMemoryCache(bitmap: Bitmap?) {
            if (getBitmapFromMemCache() == null) {
                memoryCache?.put(IMAGE_KEY, bitmap)
            }
        }

        private fun getBitmapFromMemCache(): Bitmap? {
            return memoryCache?.get(IMAGE_KEY)
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var gridViewNew: View
            if (convertView == null) {
                gridViewNew = View(cont)

                // get layout from mobile.xml
                val inflater = this@MainActivity.layoutInflater
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
                bitMap = BitmapFactory.decodeResource(resources, R.drawable.pattern)
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
    }*/

    companion object {
        var dirList = ArrayList<String>()
            private set
        var dirPathList = ArrayList<String>()
            private set
        const val REQUEST_TAKE_PHOTO = 1
        const val REQUEST_VOICE_SPEECH = 2
        const val REQUEST_REFRESH = 12
        const val EXTRAS_DIR_PATH = "dir_path"
        const val EXTRAS_DIR_NAME = "dir_name"
        private const val TAG = "MainActivity"
        private const val PHOTO_PREFIX = "quick_pic_plus"
        private const val CURRENT_IMAGE_PATH = "currentImagePath"
        private const val IMAGE_KEY = "image_key"
        private const val CAMERA_TEXT = "camera"
    }
}