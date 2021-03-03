package com.example.localplayer.services

import android.app.Service
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.util.Size
import androidx.annotation.RequiresApi
import androidx.preference.Preference
import androidx.preference.PreferenceManager
import com.example.localplayer.utils.MusicItem
import com.example.localplayer.contentprovider.PlaylistContentProvider
import java.io.FileNotFoundException
import java.io.IOException

class MusicService : Service() {

    private val binder = MusicServiceBinder()
    private val playList:MutableList<MusicItem> = mutableListOf()
    private lateinit var resolver:ContentResolver
    private val musicPlayer:MediaPlayer = MediaPlayer().apply { setOnCompletionListener {
        val isAutoPlayNext = PreferenceManager.getDefaultSharedPreferences(this@MusicService)
        if (isAutoPlayNext.getBoolean("auto_play_next",true))
            binder.playNext()
    } }
    private val onStateChangeListenerList:MutableList<OnStateChangeListener> = mutableListOf()

    private var isPausing = false
    private var nowPlaying: MusicItem? = null

    companion object{
        private val MSG_PROGRESS_UPDATE = 0
    }

    private val handler = object :Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when (msg.what) {
                MSG_PROGRESS_UPDATE -> {
                    for(l in onStateChangeListenerList)
                        nowPlaying?.let { l.onPlayProgressChange(it,musicPlayer.currentPosition) }

                    sendEmptyMessageDelayed(MSG_PROGRESS_UPDATE,1000)
                }
            }
        }
    }



    interface OnStateChangeListener {
        fun onPlayProgressChange(musicItem: MusicItem, currentPosition:Int)
        fun onPlay(musicItem: MusicItem)
        fun onPause(musicItem: MusicItem)
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate() {
        super.onCreate()



        resolver = contentResolver

        playList.clear()
        val cursor = resolver.query(PlaylistContentProvider.CONTENT_SONGS_URI,null,null,null,null)
        cursor?.run {
            while (moveToNext()){
                val musicId = getString(getColumnIndexOrThrow(PlaylistContentProvider.MUSIC_ID))
                val musicItem = queryMusicItemById(musicId.toLong())
                if (musicItem == null)
                    resolver.delete(PlaylistContentProvider.CONTENT_SONGS_URI,"id = $musicId",null)
                else
                    playList.add(musicItem)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        musicPlayer.release()
        onStateChangeListenerList.clear()
        handler.removeMessages(MSG_PROGRESS_UPDATE)
    }

    inner class MusicServiceBinder : Binder(){


        fun addPlayList(item: MusicItem){
            playList.add(playList.size,item)
            val contentValues = ContentValues().apply {
                put(PlaylistContentProvider.TITLE,item.title)
                put(PlaylistContentProvider.MUSIC_ID,item.musicId)
            }
            resolver.insert(PlaylistContentProvider.CONTENT_SONGS_URI,contentValues)
        }

        fun play(){
            if (nowPlaying == null && playList.size>0)
                nowPlaying = playList[0]

            if(isPausing)
                playMusicItem(nowPlaying,false)
            else
                playMusicItem(nowPlaying,true)
        }

        fun play(musicItem: MusicItem){
            playMusicItem(musicItem,true)
            nowPlaying = musicItem
            for(l in onStateChangeListenerList)
                l.onPlayProgressChange(musicItem,musicPlayer.currentPosition)
        }

        fun playNext(){
            val currentIndex = playList.indexOf(nowPlaying)
            if ( currentIndex < playList.size - 1){
                nowPlaying = playList[currentIndex+1]
                playMusicItem(nowPlaying,true)
            }
        }

        fun playPre(){
            val currentIndex = playList.indexOf(nowPlaying)
            if(currentIndex -1 >= 0){
                nowPlaying = playList[currentIndex-1]
                playMusicItem(nowPlaying,true)
            }
        }

        fun pause(){
            musicPlayer.pause()
            for(l in onStateChangeListenerList)
                nowPlaying?.let { l.onPause(it) }
            isPausing = true
        }

        fun seekTo(pos:Int) {
            musicPlayer.seekTo(pos)
        }

        fun registerOnChangeListener(l: OnStateChangeListener){
            onStateChangeListenerList.add(l)
        }

        fun unregisterOnStateChangeListener(l: OnStateChangeListener){
            onStateChangeListenerList.remove(l)
        }

        fun getCurrentMusic(): MusicItem? {
            return nowPlaying
        }

        fun deleteMusicItemFromPlaylist(musicItem: MusicItem){
            resolver.delete(PlaylistContentProvider.CONTENT_SONGS_URI,"${playList.indexOf(musicItem)}",null)
            playList.remove(musicItem)
        }

        fun isPlaying():Boolean{
            return  musicPlayer.isPlaying
        }

        fun getPlayList():MutableList<MusicItem>{
            return playList
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun queryMusicItemById(queryMusicId: Long): MusicItem? {
        lateinit var musicItem: MusicItem

        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Albums.ALBUM_ID,
            MediaStore.Audio.Media.ALBUM
        )

        val selection = MediaStore.Audio.Media._ID + " = $queryMusicId"
        val sortOrder = MediaStore.Audio.Media.DEFAULT_SORT_ORDER
        val cursor = resolver.query(uri,projection,selection,null,sortOrder)
        cursor?.run {
            while (moveToNext()){
                val title = getString(getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE))
                val musicId = getString(getColumnIndexOrThrow(MediaStore.Audio.Media._ID))
                val musicUri = Uri.withAppendedPath(uri,musicId)
                val duration = getString(getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION))
                val albumTitle = getString(getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM))
                val albumId = getString(getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM_ID))
                val albumUri = Uri.withAppendedPath(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,albumId)

                var albumBitmap =  BitmapFactory.decodeResource(Resources.getSystem(),android.R.drawable.ic_media_play)
                try {
                    albumBitmap = contentResolver.loadThumbnail(albumUri, Size(200,200),null)
                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                }
                finally {
                    musicItem = MusicItem(
                        title,
                        musicId.toLong(),
                        musicUri,
                        albumTitle,
                        albumUri,
                        duration.toLong(),
                        albumBitmap
                    )
                }
            }
        }
        cursor?.close()
        return  musicItem
    }

    private fun prepareToPlay(musicItem: MusicItem){
        try {
            musicPlayer.run {
                reset()
                setDataSource(this@MusicService,musicItem.musicUri)
                prepare()
            }
        } catch (e:IOException){
            e.printStackTrace()
        }
    }

    private fun playMusicItem(musicItem: MusicItem?, reload:Boolean){
        musicItem?.run {
            if(reload)
                prepareToPlay(musicItem)
            musicPlayer.start()
            handler.removeMessages(MSG_PROGRESS_UPDATE)
            handler.sendEmptyMessage(MSG_PROGRESS_UPDATE)
            for (l in onStateChangeListenerList)
                l.onPlay(musicItem)
        }
        isPausing = false
    }
}
