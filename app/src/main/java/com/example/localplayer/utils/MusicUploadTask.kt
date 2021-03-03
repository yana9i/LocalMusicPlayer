package com.example.localplayer.utils

import android.app.Activity
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.provider.MediaStore
import android.util.Size
import android.widget.ListView
import androidx.annotation.RequiresApi
import java.io.FileNotFoundException

class MusicUploadTask(
    private val musicListView: ListView,
    private val musicList:MutableList<MusicItem>
) : AsyncTask<Activity, MusicItem,Unit>(){

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun doInBackground(vararg params: Activity?) {
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Albums.ALBUM_ID,
            MediaStore.Audio.Media.ALBUM
        )

        val selection = MediaStore.Audio.Media.DATA + " like \"%"+"/Music"+"%\""
        val sortOrder = MediaStore.Audio.Media.DEFAULT_SORT_ORDER
        val contentResolver = params[0]?.contentResolver
        val cursor = contentResolver?.query(uri,projection,selection,null,sortOrder)
        cursor?.run {
            while (moveToNext()){
                val title = getString(getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE))
                val musicId = getString(getColumnIndexOrThrow(MediaStore.Audio.Media._ID))
                val musicUri = Uri.withAppendedPath(uri,musicId)
                val duration = getString(getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION))
                val albumTitle = getString(getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM))
                val albumId = getString(getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM_ID))
                val albumUri = Uri.withAppendedPath(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,albumId)

                var albumBitmap =  BitmapFactory.decodeResource(params[0]!!.resources,android.R.drawable.ic_media_play)
                try {
                    albumBitmap =  contentResolver.loadThumbnail(albumUri, Size(200,200),null)
                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                }
                finally {
                    val musicItem = MusicItem(
                        title,
                        musicId.toLong(),
                        musicUri,
                        albumTitle,
                        albumUri,
                        duration.toLong(),
                        albumBitmap
                    )
                    publishProgress(musicItem)
                }
            }
        }
        cursor?.close()
    }

    override fun onProgressUpdate(vararg values: MusicItem?) {
        val data = values[0]

        data?.run { musicList.add(data) }
        val adapter = musicListView.adapter as MusicItemAdapter
        adapter
        adapter.notifyDataSetChanged()
    }

}