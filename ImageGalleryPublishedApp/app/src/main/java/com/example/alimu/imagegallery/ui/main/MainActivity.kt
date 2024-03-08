package com.example.alimu.imagegallery.ui.main

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
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
    private var clickImageButton: ImageButton? = null
    private lateinit var permissionResultLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var presenter: MainContract.Presenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        presenter = MainPresenter(this)

        permissionResultLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            presenter.onRequestPermissionResult()
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

    override fun showMessage(message: String) {
        Toast.makeText(
            applicationContext,
            message,
            Toast.LENGTH_LONG
        ).show()
    }

    fun goBack(view: View?) {
        back()
    }

    private fun back() {
        val fragmentManager = supportFragmentManager
        if (fragmentManager.backStackEntryCount > 1) {
            fragmentManager.popBackStack()
        } else {
            super.finish()
        }
    }

    companion object {
        var dirList = ArrayList<String>()
            private set
        var dirPathList = ArrayList<String>()
            private set
    }
}