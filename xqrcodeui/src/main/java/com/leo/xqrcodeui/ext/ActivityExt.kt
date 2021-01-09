package com.leo.xqrcodeui.ext

import android.app.Activity
import android.content.pm.PackageManager
import android.view.View
import androidx.core.content.ContextCompat
import com.leo.xqrcodeui.R
import java.io.File

const val FLAGS_FULLSCREEN =
        View.SYSTEM_UI_FLAG_LOW_PROFILE or
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION

fun Activity.allPermissionsGranted(permissions: Array<String>): Boolean {
    return permissions.all {
        ContextCompat.checkSelfPermission(
                baseContext, it
        ) == PackageManager.PERMISSION_GRANTED
    }
}

fun Activity.getOutputDirectory(): File {
    val mediaDir = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
        externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
        }
    } else {
        filesDir
    }
    return if (mediaDir != null && mediaDir.exists())
        mediaDir else filesDir
}