package com.ssafy.facemeet.client.ui.profile

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

fun shareToInstagramStory(context: Context, imageFile: File) {
    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        imageFile
    )

    val intent = Intent("com.instagram.share.ADD_TO_STORY").apply {
        setDataAndType(uri, "image/*")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        putExtra("source_application", context.packageName)

        // ✅ 링크 추가
        putExtra("content_url", "https://play.google.com/store/apps/details?id=com.ssafy.facemeet")
    }

    context.grantUriPermission(
        "com.instagram.android",
        uri,
        Intent.FLAG_GRANT_READ_URI_PERMISSION
    )

    try {
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, "Instagram이 설치되어 있지 않습니다.", Toast.LENGTH_SHORT).show()
    }
}

fun saveBitmapToFile(context: Context, bitmap: Bitmap): File {
    val file = File(context.cacheDir, "share_result.png")
    FileOutputStream(file).use { out ->
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
    }
    return file
}
