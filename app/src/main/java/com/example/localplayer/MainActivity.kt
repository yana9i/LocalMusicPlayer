package com.example.localplayer

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.ImageDecoder
import android.os.*
import android.provider.MediaStore
import android.util.Size
import android.view.Menu
import android.view.MenuItem
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.annotation.RequiresApi
import androidx.core.view.GravityCompat
import com.example.localplayer.activities.AlbumActivity
import com.example.localplayer.utils.MusicItem
import com.example.localplayer.utils.MusicItemAdapter
import com.example.localplayer.services.MusicService
import com.example.localplayer.activities.AllMusicActivity
import com.example.localplayer.activities.PlaylistActivity
import com.example.localplayer.activities.SettingsActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private var musicList = mutableListOf<MusicItem>()
    private lateinit var musicServiceBinder: MusicService.MusicServiceBinder

    private val stateChangeListener = object :
        MusicService.OnStateChangeListener {
        override fun onPause(musicItem: MusicItem) {
            play_btn.setBackgroundResource(R.drawable.ic_baseline_play_arrow_24)
        }

        override fun onPlay(musicItem: MusicItem) {
            play_btn.setBackgroundResource(R.drawable.ic_baseline_pause_24)
        }

        @RequiresApi(Build.VERSION_CODES.Q)
        override fun onPlayProgressChange(musicItem: MusicItem, currentPosition: Int) {
            updatePlayingInfo(musicItem,currentPosition)
        }
    }
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {

        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            musicServiceBinder = service as MusicService.MusicServiceBinder
            musicList = musicServiceBinder.getPlayList()
            musicServiceBinder.registerOnChangeListener(stateChangeListener)
        }

    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        supportActionBar?.let {
            it.setDisplayHomeAsUpEnabled(true)
            it.setHomeAsUpIndicator(R.drawable.ic_baseline_menu_24)
        }
        nav_view.setNavigationItemSelectedListener {
            drawer_layout.closeDrawers()
            when(it.itemId){
                R.id.nav_to_allMusic ->{
                    val intent = Intent(this,
                        AllMusicActivity::class.java)
                    startActivity(intent)
                }
                R.id.nav_to_playlist ->{
                    val intent = Intent(this,PlaylistActivity::class.java)
                    startActivity(intent)
                }
                R.id.nav_to_settings ->{
                    val intent = Intent(this,SettingsActivity::class.java)
                    startActivity(intent)
                }
                R.id.nav_to_albumList ->{
                    val intent = Intent(this,AlbumActivity::class.java)
                    startActivity(intent)
                }
            }
            true
        }

        val intent = Intent(this, MusicService::class.java)
        startService(intent)
        bindService(intent,serviceConnection, Context.BIND_AUTO_CREATE)

        play_btn.setOnClickListener{
            if (musicServiceBinder.isPlaying()){
                musicServiceBinder.pause()
            } else{
                musicServiceBinder.play()
            }
        }

        next_btn.setOnClickListener{
            musicServiceBinder.playNext()
        }

        pre_btn.setOnClickListener{
            musicServiceBinder.playPre()
        }

        seek_music.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {}
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                seekBar?.let { musicServiceBinder.seekTo(it.progress) }
            }
        })
    }

    override fun onResume() {
        super.onResume()
        nav_view.setCheckedItem(R.id.nav_to_nowPlaying)
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(serviceConnection)
        musicServiceBinder.unregisterOnStateChangeListener(stateChangeListener)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu,menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            android.R.id.home -> drawer_layout.openDrawer(GravityCompat.START)
        }
        return true
    }

    @SuppressLint("SimpleDateFormat")
    @RequiresApi(Build.VERSION_CODES.Q)
    private fun updatePlayingInfo(musicItem: MusicItem, currentPosition:Int){
        val durationTime = musicItem.duration
        val mSDF = SimpleDateFormat("mm:ss")
        val durationDate = Date(durationTime)
        duration_time.text = mSDF.format(durationDate)
        val currentPositionDate = Date(currentPosition.toLong())
        current_position.text = mSDF.format(currentPositionDate)

        if(musicServiceBinder.isPlaying())
            play_btn.setBackgroundResource(R.drawable.ic_baseline_pause_24)
        else
            play_btn.setBackgroundResource(R.drawable.ic_baseline_play_arrow_24)

        seek_music.max = musicItem.duration.toInt()
        seek_music.progress = currentPosition

        now_playing_music_title.text = musicItem.title
        try {
            now_playing_album.setImageBitmap(contentResolver.loadThumbnail(musicItem.albumUri, Size(800,800),null))
        } catch (e:Exception) {
            e.printStackTrace()
            now_playing_album.setImageBitmap(musicItem.albumThumb)
        }

    }

}


