package com.example.localplayer.contentprovider

import android.content.ContentProvider
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class PlaylistContentProvider : ContentProvider() {

    private lateinit var dbHelper: DBHelper

    companion object {
        private val DB_NAME = "playlist.db"
        private val DB_VERSION = 1


        private val SCHEME = "content://"
        private val PATH_SONGS = "/songs"
        private val AUTHORITY = "com.example.localplayer"

        val CONTENT_SONGS_URI = Uri.parse(SCHEME + AUTHORITY + PATH_SONGS)

        val PLAYLIST_TABLE_NAME = "playlist_table"
        val ID = "id"
        val TITLE = "title"
        val MUSIC_ID = "music_id"
    }

    inner class DBHelper(context: Context) : SQLiteOpenHelper(context,
        DB_NAME,null,
        DB_VERSION
    ){

        override fun onCreate(db: SQLiteDatabase?) {
            val playlistTableSql =
                "CREATE TABLE $PLAYLIST_TABLE_NAME" +
                "(" +
                " $ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                " $TITLE VARCHAR(256)," +
                " $MUSIC_ID LONG" +
                ");"
            db?.execSQL(playlistTableSql)
        }

        override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
            db?.execSQL("DROP TABLE IF EXISTS $PLAYLIST_TABLE_NAME")
            onCreate(db)
        }

    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        val cursor = dbHelper.readableDatabase.query(PLAYLIST_TABLE_NAME, arrayOf("id"),null,null,null,null,null)
        var i = selection?.toInt()?.plus(1) ?:0
        var id:Int? = null
        while (i-->0&&cursor.moveToNext()){
            id = cursor.getInt(0)
        }
        Log.d("pcp","$id $selection")
        val db = dbHelper.writableDatabase
        return db.delete(PLAYLIST_TABLE_NAME," id = $id",selectionArgs)
    }

    override fun getType(uri: Uri): String? {
        return null
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        val db = dbHelper.writableDatabase
        val id = db.insert(PLAYLIST_TABLE_NAME,null,values)
        lateinit var result:Uri
        if (id > 0)
            result = ContentUris.withAppendedId(CONTENT_SONGS_URI,id)
        return result
    }

    override fun onCreate(): Boolean {
        dbHelper = DBHelper(context!!)
        return true
    }

    override fun query(
        uri: Uri, projection: Array<String>?, selection: String?,
        selectionArgs: Array<String>?, sortOrder: String?
    ): Cursor? {
        val db = dbHelper.readableDatabase
        return db.query(PLAYLIST_TABLE_NAME,projection,selection,selectionArgs,null,null,sortOrder)
    }

    override fun update(
        uri: Uri, values: ContentValues?, selection: String?,
        selectionArgs: Array<String>?
    ): Int {
        val db = dbHelper.writableDatabase
        return db.update(PLAYLIST_TABLE_NAME,values,selection,selectionArgs)
    }
}
