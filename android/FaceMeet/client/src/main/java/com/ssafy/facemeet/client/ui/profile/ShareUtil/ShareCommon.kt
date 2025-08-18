package com.ssafy.facemeet.client.ui.profile.ShareUtil

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File

fun shareImageWithText(context: Context, file: File, text: String) {
    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
    )

    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "image/*"
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_TEXT, text)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    val chooser = Intent.createChooser(shareIntent, "공유하기")
    context.startActivity(chooser)
}
