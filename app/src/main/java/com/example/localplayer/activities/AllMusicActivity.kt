package com.example.localplayer.activities

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.KeyEvent
import com.example.localplayer.*
import com.example.localplayer.services.MusicService
import com.example.localplayer.utils.MusicItem
import com.example.localplayer.utils.MusicItemAdapter
import com.example.localplayer.utils.MusicUploadTask
import kotlinx.android.synthetic.main.activity_all_music.*

class AllMusicActivity : AppCompatActivity() {

    private val musicList = mutableListOf<MusicItem>()
    private lateinit var musicUploadTask: MusicUploadTask
    private lateinit var musicServiceBinder: MusicService.MusicServiceBinder
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {

        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            musicServiceBinder = service as MusicService.MusicServiceBinder
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_music)
        setSupportActionBar(all_music_toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)

        val adapter = MusicItemAdapter(
            musicList,
            this.baseContext,
            R.layout.music_item
        )
        all_music_list.adapter = adapter


        musicUploadTask = MusicUploadTask(all_music_list,musicList)
        musicUploadTask.execute(this)



        all_music_list.setOnItemClickListener { parent, view,position,id ->
            val musicItem = musicList[position]
            musicServiceBinder.addPlayList(musicItem)
        }

        val intent = Intent(this, MusicService::class.java)
        bindService(intent,serviceConnection, Context.BIND_AUTO_CREATE)

    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(serviceConnection)
        if (musicUploadTask.status == AsyncTask.Status.RUNNING)
            musicUploadTask.cancel(true)
    }


    override fun dispatchKeyEvent(event: KeyEvent?): Boolean {
        Log.d("AllMusicActivity","current focus=${window.currentFocus}")
        return super.dispatchKeyEvent(event)
    }
}