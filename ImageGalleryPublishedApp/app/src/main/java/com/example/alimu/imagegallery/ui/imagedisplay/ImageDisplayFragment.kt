package com.example.alimu.imagegallery.ui.imagedisplay

import android.content.Context.LAYOUT_INFLATER_SERVICE
import android.graphics.Bitmap
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.alimu.imagegallery.R
import com.example.alimu.imagegallery.ui.main.MainActivity

class ImageDisplayFragment: Fragment(), ImageDisplayContract.View {
    private lateinit var presenter: ImageDisplayContract.Presenter
    private var titleText: TextView? = null
    private var imageView: ImageView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val path = arguments?.getString(EXTRAS_FILE_PATH, null)
        presenter = ImageDisplayPresenter(this, path)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_image_display, container, false)

        val baseActivity = activity as? MainActivity
        val actionBar = baseActivity?.supportActionBar
        val actionBarInflater = actionBar?.themedContext
            ?.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val customActionBarView = actionBarInflater.inflate(
            R.layout.custom_action_bar, null
        )
        titleText = customActionBarView.findViewById(R.id.title)
        val layoutParams = titleText?.layoutParams as? RelativeLayout.LayoutParams
        layoutParams?.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE)
        titleText?.layoutParams = layoutParams
        titleText?.gravity = Gravity.CENTER
        customActionBarView.findViewById<ImageButton>(R.id.clickBtn)?.apply {
            visibility = View.GONE
        }
        actionBar.setDisplayShowTitleEnabled(false)
        actionBar.customView = customActionBarView
        actionBar.setDisplayShowCustomEnabled(true)
        imageView = view.findViewById(R.id.imgDisplay)
        presenter.onViewInitialized()
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
        titleText = null
        imageView = null
        super.onDestroyView()
    }

    fun goBack() {
        back()
    }

    override fun isActive() = isAdded

    override fun updateTitleText(message: String?) {
        titleText?.text = message
    }

    override fun loadImage(bitmap: Bitmap) {
        imageView?.setImageBitmap(bitmap)
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
        const val TAG = "ImageDisplayFragment"
        private const val EXTRAS_FILE_PATH = "path"

        fun newInstance(path: String?): ImageDisplayFragment {
            val imageDisplayFragment = ImageDisplayFragment()
            val bundle = Bundle()
            bundle.putString(EXTRAS_FILE_PATH, path)
            imageDisplayFragment.arguments = bundle
            return imageDisplayFragment
        }
    }
}