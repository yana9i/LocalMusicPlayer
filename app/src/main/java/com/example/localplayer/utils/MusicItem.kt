package com.example.localplayer.utils

import android.graphics.Bitmap
import android.net.Uri

data class MusicItem(
    val title: String,
    val musicId: Long,
    val musicUri: Uri,
    val albumTitle: String,
    val albumUri: Uri,
    val duration: Long,
    val albumThumb: Bitmap
)