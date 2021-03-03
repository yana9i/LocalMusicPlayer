package com.example.localplayer.activities

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.example.localplayer.R
import kotlinx.android.synthetic.main.settings_activity.*

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings, SettingsFragment())
            .commit()
        setSupportActionBar(settings_toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)



    }

    class SettingsFragment : PreferenceFragmentCompat() {

        private lateinit var volumeChangeReceiver: BroadcastReceiver

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
            val volPreference:Preference? = findPreference("setting_vol")
            val intentFilter = IntentFilter()
            intentFilter.addAction("android.media.VOLUME_CHANGED_ACTION")
            val audioManager = context?.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            val current = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
            volPreference?.summary = "${((current.toDouble()/max.toDouble())*100).toInt()}%"
            volumeChangeReceiver =object :BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    val maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                    val currentVol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                    volPreference?.summary = "${((currentVol.toDouble()/maxVol.toDouble())*100).toInt()}%"
                }
            }
            context?.registerReceiver(volumeChangeReceiver,intentFilter)
        }

        override fun onDestroy() {
            volumeChangeReceiver.let{ context?.unregisterReceiver(it)}
            super.onDestroy()
        }

    }
}