package com.example.localplayer.activities

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.localplayer.R
import com.example.localplayer.services.MusicService
import com.example.localplayer.utils.MusicItem
import com.example.localplayer.utils.MusicUploadTask
import kotlinx.android.synthetic.main.activity_playlist.*
import java.text.SimpleDateFormat
import java.util.*

class PlaylistActivity : AppCompatActivity() {

    private var musicList = mutableListOf<MusicItem>()
    private lateinit var musicServiceBinder: MusicService.MusicServiceBinder
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {

        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            musicServiceBinder = service as MusicService.MusicServiceBinder
            musicList = musicServiceBinder.getPlayList()
            val layoutManager = LinearLayoutManager(this@PlaylistActivity)
            val adapter = PlaylistRecyclerListAdapter(musicList)
            playlist_list.let {
                it.layoutManager = layoutManager
                it.adapter = adapter
            }
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_playlist)
        setSupportActionBar(playlist_toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)

        val intent = Intent(this, MusicService::class.java)
        bindService(intent,serviceConnection, Context.BIND_AUTO_CREATE)
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(serviceConnection)
    }

    inner class PlaylistRecyclerListAdapter(val playlist:MutableList<MusicItem>) : RecyclerView.Adapter<PlaylistRecyclerListAdapter.ViewHolder>() {
        inner class ViewHolder (view:View):RecyclerView.ViewHolder(view) {
            val albumImage:ImageView = view.findViewById(R.id.music_thumb)
            val musicDuration:TextView = view.findViewById(R.id.music_duration)
            val musicAlbumTitle:TextView = view.findViewById(R.id.music_album_title)
            val musicTitle:TextView = view.findViewById(R.id.music_title)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.recycle_list_music_item,parent,false)
            val viewHolder = ViewHolder(view)
            viewHolder.itemView.setOnClickListener {
                val position = viewHolder.adapterPosition
                musicServiceBinder.play(musicServiceBinder.getPlayList()[position])
                finish()
            }
            viewHolder.itemView.setOnLongClickListener {
                val position = viewHolder.adapterPosition
                musicServiceBinder.deleteMusicItemFromPlaylist(musicServiceBinder.getPlayList()[position])
                notifyDataSetChanged()
                true
            }
            return  viewHolder
        }

        override fun getItemCount(): Int {
            return playlist.size
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val musicItem = playlist[position]
            holder.let {
                it.albumImage.setImageBitmap(musicItem.albumThumb)
                val mSDF = SimpleDateFormat("mm:ss")
                val date = Date(musicItem.duration)
                it.musicDuration.text = mSDF.format(date)
                it.musicAlbumTitle.text = musicItem.albumTitle
                it.musicTitle.text = musicItem.title
            }
        }


    }
}

