package com.example.musicplayerapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.WindowManager
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musicplayerapp.databinding.ActivityMainBinding
import com.example.musicplayerapp.model.Music

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding


    companion object {
        lateinit var musicList: ArrayList<Music>
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        enableEdgeToEdge()
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )

        val requestPermissionLauncher =
            registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted: Boolean ->
                if (isGranted) {

                } else {

                }
            }

        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED -> {
            }

            ActivityCompat.shouldShowRequestPermissionRationale(
                this, Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) -> {

            }

            else -> {
                requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }



        musicList = getAllAudio()



        binding.apply {
            rvMusic.layoutManager = LinearLayoutManager(this@MainActivity)
            rvMusic.adapter = MusicAdapter(this@MainActivity, musicList,::onItemClicked)
        }


    }

    private fun onItemClicked(position: Int){
        val intent = Intent(this@MainActivity,PlayerActivity::class.java)
        intent.putExtra("index",position)
        intent.putExtra("class","MusicAdapter")
        startActivity(intent)
    }

    @SuppressLint("Recycle", "Range")
    private fun getAllAudio(): ArrayList<Music> {
        val audioList = ArrayList<Music>()

        val projection = arrayOf(
            MediaStore.Audio.AudioColumns._ID,
            MediaStore.Audio.AudioColumns.TITLE,
            MediaStore.Audio.AudioColumns.DISPLAY_NAME,
            MediaStore.Audio.AudioColumns.ARTIST,
            MediaStore.Audio.AudioColumns.DATA,
            MediaStore.Audio.AudioColumns.DURATION,
            MediaStore.Audio.AudioColumns.ALBUM_ID
        )


        val selectionClause: String = "${MediaStore.Audio.AudioColumns.IS_MUSIC} = ?"
        val selectionArg = arrayOf("1")
        val sortOrder = "${MediaStore.Audio.AudioColumns.DISPLAY_NAME} ASC"

        val mCursor = this.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            selectionClause,
            selectionArg,
            sortOrder
        )

        mCursor?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns._ID)
            val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.TITLE)
            val displayNameColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.DISPLAY_NAME)
            val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.ARTIST)
            val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.DATA)
            val durationColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.DURATION)
            val albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.ALBUM_ID)

            cursor.apply {
                if (count == 0) {

                } else {
                    while (cursor.moveToNext()) {
                        val id = getLong(idColumn)
                        val title = getString(titleColumn)
                        val displayName = getString(displayNameColumn)
                        val artist = getString(artistColumn)
                        val data = getString(dataColumn)
                        val duration = getLong(durationColumn)
                        val albumId = getLong(albumIdColumn)

                        val uri = Uri.parse("content://media/external/audio/albumart")
                        val artUri = ContentUris.withAppendedId(uri,albumId)

                        audioList += Music(
                            id = id,
                            title = title,
                            displayName = displayName,
                            artist = artist,
                            data = data,
                            duration = duration,
                            uri = artUri
                        )
                    }
                }
            }
        }
        return audioList
    }
}