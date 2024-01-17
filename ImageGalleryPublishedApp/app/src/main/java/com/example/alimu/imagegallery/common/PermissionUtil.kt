package com.example.alimu.imagegallery.common

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.ActivityCompat

object PermissionUtil {
    fun areMediaPermissionsGranted(context: Context): Boolean {
        return areWholePermissionsGranted(context, getPermissionsList())
    }
    
    fun requestMediaPermissions(permissionResultLauncher: ActivityResultLauncher<Array<String>>) {
        requestPermissions(getPermissionsList(), permissionResultLauncher)
    }
    
    private fun getPermissionsList(): Array<String> {
        return if (VERSION.SDK_INT <= VERSION_CODES.P) {
            arrayOf(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        } else {
            arrayOf(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }
    
    private fun areWholePermissionsGranted(context: Context, permissions: Array<String>): Boolean {
        return permissions.all { permission ->
            ActivityCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }
    }
    
    private fun requestPermissions(permissions: Array<String>, permissionResultLauncher: ActivityResultLauncher<Array<String>>) {
        permissionResultLauncher.launch(permissions)
    }
}