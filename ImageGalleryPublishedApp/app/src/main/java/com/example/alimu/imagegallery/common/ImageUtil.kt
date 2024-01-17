package com.example.alimu.imagegallery.common

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

object ImageUtil {
    fun getUriFromFile(context: Context, authority: String, image: File): Uri {
        return FileProvider.getUriForFile(context,  authority, image)
    }
}