package com.example.alimu.imagegallery.ui.directoriesgridview

import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.content.Context.LAYOUT_INFLATER_SERVICE
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.StateListDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.speech.RecognizerIntent
import android.view.Gravity
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView.OnItemClickListener
import android.widget.GridView
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.alimu.imagegallery.R
import com.example.alimu.imagegallery.adapters.DirectoriesGridAdapter
import com.example.alimu.imagegallery.common.FragmentHelper
import com.example.alimu.imagegallery.common.ImageUtil.getUriFromFile
import com.example.alimu.imagegallery.ui.imagesgridview.ImagesGridFragment
import com.example.alimu.imagegallery.ui.main.MainActivity
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException

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
class DirectoriesGridFragment : Fragment(), DirectoriesGridContract.View {
    private var clickImageButton: ImageButton? = null
    private var gridview: GridView? = null
    private var imageAdapter: DirectoriesGridAdapter? = null
    private var photoUri: Uri? = null
    private lateinit var presenter: DirectoriesGridContract.Presenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter = DirectoriesGridPresenter(this)
        presenter.start()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_directories_grid_view, container, false)
        initializeViews(savedInstanceState, view)
        setHasOptionsMenu(true)
        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_main, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_back -> {
                back()
                true
            }
            R.id.action_speech -> {
                presenter.onVoiceTextButtonClicked()
                true
            }
            R.id.action_info -> {
                presenter.onAppInfoButtonClicked()
                true
            }
            R.id.action_policy -> {
                presenter.onPrivacyInfoButtonClicked()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSaveInstanceState(saveState: Bundle) {
        super.onSaveInstanceState(saveState)
        saveState.putString(CURRENT_IMAGE_PATH, presenter.currentImagePath)
    }

    override fun onDestroyView() {
        clickImageButton = null
        gridview = null
        imageAdapter = null
        super.onDestroyView()
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val activity = activity ?: return
        when {
            requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK -> {
                sendImageToGallery(data)
            }
            requestCode == REQUEST_TAKE_PHOTO && resultCode != RESULT_OK -> {
                presenter.onPhotoRequestFailed()
                //sendBroadcastToGallery()
            }
            requestCode == REQUEST_VOICE_SPEECH && resultCode == RESULT_OK -> {
                val voiceText = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.get(0)
                Toast.makeText(activity.applicationContext, voiceText, Toast.LENGTH_LONG).show()
                if (voiceText == CAMERA_TEXT) {
                    presenter.onOpenCameraButtonClicked()
                }
            }
        }
    }

    override fun isActive() = isAdded

    override fun getImageDirectoriesPath(): HashSet<String>? {
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
            val imageCursor = activity?.contentResolver?.query(
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

    override fun setSelector() {
        gridview?.selector = StateListDrawable()
    }

    override fun showGridViewFragment(position: Int, dirList: ArrayList<String>,
                                      dirPathList: ArrayList<String>) {
        FragmentHelper.show(
            this,
            ImagesGridFragment.newInstance(dirList, dirPathList, position),
            ImagesGridFragment.TAG
        )
    }

    override fun openCamera() {
        val activity = activity ?: return
        val clickPicIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (clickPicIntent.resolveActivity(activity.packageManager) != null) {
            var image: File? = null
            try {
                image = presenter.generateImage()
            } catch (ignore : IOException) { }
            if (image != null) {
                photoUri = getUriFromFile(activity.applicationContext, "${activity.packageName}.fileprovider", image)
                clickPicIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri.toString())
                startActivityForResult(clickPicIntent, REQUEST_TAKE_PHOTO)
            }
        }
    }

    override fun refreshViews() {
        imageAdapter?.notifyDataSetChanged()
    }

    override fun updateImageAdapter(dirList: ArrayList<String>, dirSizeList: ArrayList<Int>,
                                    dirPathList: ArrayList<String>) {
        imageAdapter?.setItems(dirList, dirSizeList, dirPathList)
    }

    override fun requestVoiceSpeech() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        startActivityForResult(intent, REQUEST_VOICE_SPEECH)
    }

    override fun showAppInfo() {
        val alert = AlertDialog.Builder(activity?.applicationContext)
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
    }

    override fun showPrivacyInfo() {
        val policyUrl =
            "https://sites.google.com/s/0B6dfwLMBeuAvT3dhcGRONHBiVjQ/p/0B6dfwLMBeuAvT1hQWWEtRWZUZDg/edit"
        //"https://docs.google.com/document/d/1NhrWFxYGfkn0-QhYyOeLXOKqQOH_HbMVS_I6z3qLV4M/pub";
        //"https://docs.google.com/document/u/1/d/1NhrWFxYGfkn0-QhYyOeLXOKqQOH_HbMVS_I6z3qLV4M/pub"
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(policyUrl))
        startActivity(browserIntent)
    }

    private fun initializeViews(savedInstanceState: Bundle?, view: View) {
        val baseActivity = activity as? MainActivity
        val actionBar = baseActivity?.supportActionBar
        val inflater = actionBar?.themedContext?.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val customActionBarView = inflater.inflate(
            R.layout.custom_action_bar, null
        )
        customActionBarView.findViewById<ImageButton>(R.id.action_back)?.apply {
            this.visibility = View.INVISIBLE
        }
        customActionBarView.findViewById<TextView>(R.id.title)?.apply {
            this.layoutParams = this.layoutParams as RelativeLayout.LayoutParams
            this.gravity = Gravity.CENTER
        }
        actionBar.setDisplayShowTitleEnabled(false)
        actionBar.customView = customActionBarView
        actionBar.setDisplayShowCustomEnabled(true)
        if (savedInstanceState != null) {
            presenter.currentImagePath = savedInstanceState.getString(CURRENT_IMAGE_PATH)
        }

        gridview = view.findViewById(R.id.folderGrid)
        imageAdapter = DirectoriesGridAdapter(baseActivity.applicationContext)
        gridview?.adapter = imageAdapter
        gridview?.onItemClickListener = OnItemClickListener { parent, v, position, id ->
            presenter.onGridViewItemClicked(position)
        }
        clickImageButton = customActionBarView.findViewById<ImageButton?>(R.id.clickBtn)?.apply {
            setOnClickListener {
                presenter.onOpenCameraButtonClicked()
            }
        }
        presenter.onViewInitialized()
    }

    private fun sendImageToGallery(data: Intent?) {
        if (data?.hasExtra(BITMAP_DATA) == true) {
            val bitmap: Bitmap = data.extras?.get(BITMAP_DATA) as Bitmap
            try {
                photoUri?.let {
                    val outputStream = activity?.contentResolver?.openOutputStream(it)
                    if (outputStream != null) {
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
                    }
                    outputStream?.close()
                }
            } catch (ignore: FileNotFoundException) { }
            presenter.onPhotoReceived()
        }
    }

    /*private fun sendBroadcastToGallery() {
        val activity = activity ?: return
        val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
        val f = File(currentImagePath ?: return)
        val contentUri = getUriFromFile(activity.applicationContext,  "${activity.packageName}.fileprovider", f)
        mediaScanIntent.data = contentUri
        activity.applicationContext.sendBroadcast(mediaScanIntent)
    }*/

    private fun back() {
        val fragmentManager = parentFragmentManager
        if (fragmentManager.backStackEntryCount > 1) {
            fragmentManager.popBackStack()
        } else {
            activity?.finish()
        }
    }

    companion object {
        const val TAG = "DirectoriesGridFragment"
        const val REQUEST_TAKE_PHOTO = 1
        const val REQUEST_VOICE_SPEECH = 2
        private const val CURRENT_IMAGE_PATH = "currentImagePath"
        private const val CAMERA_TEXT = "camera"
        private const val BITMAP_DATA = "data"

        fun newInstance(): DirectoriesGridFragment {
            return DirectoriesGridFragment()
        }
    }
}