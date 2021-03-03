package com.example.localplayer.utils

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.example.localplayer.R
import java.text.SimpleDateFormat
import java.util.*

class MusicItemAdapter(
    private val mData:List<MusicItem>,
    private val mContext:Context,
    private val resourceId:Int
) : BaseAdapter() {

    private val mInflater:LayoutInflater = LayoutInflater.from(mContext)

    @SuppressLint("SimpleDateFormat")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {

        val item = mData[position]

        val thisView = convertView?:mInflater.inflate(resourceId,parent,false)

        thisView?.run {
            val title = findViewById<TextView>(R.id.music_title)
            title?.text = item.title

            val durationTime = findViewById<TextView>(R.id.music_duration)
            val mSDF = SimpleDateFormat("mm:ss")
            val date = Date(item.duration)
            val timeString = mSDF.format(date)
            durationTime?.text = "播放时长：$timeString"

            val albumTitle = findViewById<TextView>(R.id.music_album_title)
            albumTitle?.text = item.albumTitle

            val thumb = findViewById<ImageView>(R.id.music_thumb)
            thumb?.setImageBitmap(item.albumThumb)

        }
        return thisView
    }

    override fun getItem(position: Int): Any {
        return mData[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return mData.size
    }

}