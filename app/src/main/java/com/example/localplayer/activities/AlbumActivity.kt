package com.example.localplayer.activities

import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.localplayer.R
import kotlinx.android.synthetic.main.activity_album.*
import java.lang.Exception

class AlbumActivity : AppCompatActivity() {

    inner class albumObj(val albumUri:Uri, val albumTitle:String)

    private val albumList:MutableList<albumObj> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_album)
        setSupportActionBar(album_toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)


        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(MediaStore.Audio.Albums._ID,MediaStore.Audio.Albums.ALBUM)
        val cursor = contentResolver.query(uri,projection,null,null,null)
        cursor?.run {
            loop@ while (moveToNext()){
                 for (item in albumList)
                    if (item.albumTitle == getString(1))
                        continue@loop
                albumList.add(albumObj(Uri.withAppendedPath(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, getString(0)),getString(1)))
            }
        }
        val layoutManager = StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL)
        val adapter = AlbumListAdapter(albumList)
        album_list.layoutManager = layoutManager
        album_list.adapter = adapter
    }

    inner class AlbumListAdapter(val albumList:MutableList<albumObj>):RecyclerView.Adapter<AlbumListAdapter.ViewHolder>(){

        inner class ViewHolder (view: View):RecyclerView.ViewHolder(view) {
            val albumImage:ImageView = view.findViewById(R.id.album_list_image)
            val albumTitle:TextView = view.findViewById(R.id.album_list_title)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.album_item,parent,false)
            return ViewHolder(view)
        }

        override fun getItemCount(): Int {
            return albumList.size
        }

        @RequiresApi(Build.VERSION_CODES.Q)
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val albumObj = albumList[position]
            holder.albumTitle.text = albumObj.albumTitle
            try {
                holder.albumImage.setImageBitmap(contentResolver.loadThumbnail(albumObj.albumUri, Size(400,400),null))
            } catch (e:Exception) {
                e.printStackTrace()
                holder.albumImage.setImageBitmap(BitmapFactory.decodeResource(resources,android.R.drawable.ic_media_play))
            }
        }

    }

}