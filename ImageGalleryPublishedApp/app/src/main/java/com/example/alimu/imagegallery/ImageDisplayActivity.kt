package com.example.alimu.imagegallery

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.io.File

class ImageDisplayActivity : AppCompatActivity() {
    private var path: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_display)
        val inflater = supportActionBar?.themedContext
            ?.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val customActionBarView = inflater.inflate(
            R.layout.custom_action_bar, null
        )
        val titleText = customActionBarView.findViewById<TextView>(R.id.title)
        val imgBtn = customActionBarView.findViewById<ImageButton>(R.id.clickBtn)
        imgBtn.visibility = View.GONE
        path = intent.extras?.getString("path")
        val fileParent = File(path ?: return).parent
        val dirName = fileParent?.substring(fileParent.lastIndexOf(File.separator) + 1)
        titleText.text = dirName
        val layoutParams = titleText.layoutParams as RelativeLayout.LayoutParams
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE)
        titleText.layoutParams = layoutParams
        titleText.gravity = Gravity.CENTER
        val actionBar = supportActionBar
        actionBar?.setDisplayShowTitleEnabled(false)
        actionBar?.customView = customActionBarView
        actionBar?.setDisplayShowCustomEnabled(true)
        val imageView = findViewById<ImageView>(R.id.imgDisplay)
        val bmOptions = BitmapFactory.Options()
        bmOptions.inJustDecodeBounds = false
        bmOptions.inSampleSize = 4
        bmOptions.inPurgeable = true
        val bitmap = BitmapFactory.decodeFile(path, bmOptions)
        imageView.setImageBitmap(bitmap)
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
}