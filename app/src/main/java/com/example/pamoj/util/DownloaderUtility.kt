package com.example.pamoj.util

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment

fun downloadImage(context: Context, url: String) {
    val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    val request = DownloadManager.Request(Uri.parse(url))
        .setTitle("Download Gambar")
        .setDescription("Mengunduh gambar dari post")
        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        .setDestinationInExternalPublicDir(Environment.DIRECTORY_PICTURES, "downloaded_image_fr_stassy.jpg")
        .setAllowedOverMetered(true)


    downloadManager.enqueue(request)
}