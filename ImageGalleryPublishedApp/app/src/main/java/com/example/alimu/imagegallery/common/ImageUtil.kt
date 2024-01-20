package com.example.alimu.imagegallery.common

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

object ImageUtil {
    fun getUriFromFile(context: Context, authority: String, image: File): Uri {
        return FileProvider.getUriForFile(context,  authority, image)
    }

    fun getDecodedBitmap(path: String): Bitmap? {
        val bmOptions = BitmapFactory.Options()
        bmOptions.inJustDecodeBounds = false
        bmOptions.inSampleSize = 4
        bmOptions.inPurgeable = true
        return BitmapFactory.decodeFile(path, bmOptions)
    }
}