package com.sulav.musicplayer

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.common.MediaItem
import kotlinx.coroutines.Dispatchers

class MusicRepository(private val context: Context) {
    private val TAG: String = "MusicRepository"
    private val player = ExoPlayer.Builder(context.applicationContext).build()

    fun getAllSongs(): LiveData<List<Song>> {
        Log.i(TAG, "get all songs")
        val liveData = MutableLiveData<List<Song>>()

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA
        )

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        val songList = mutableListOf<Song>()

        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val cursor = context.contentResolver.query(
            uri,
            projection,
            selection,
            null,
            sortOrder
        )

        cursor?.use {
            val idCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val albumIdCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val durationCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val dataCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)

            while (it.moveToNext()) {
                val contentUri = ContentUris.withAppendedId(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, it.getLong(idCol)
                )
                val song = Song(
                    id = it.getLong(idCol),
                    title = it.getString(titleCol),
                    artist = it.getString(artistCol),
                    album = it.getString(albumCol),
                    albumId = it.getLong(albumIdCol),
                    duration = it.getLong(durationCol),
                    uri = contentUri,
                    data = it.getString(dataCol)
                )
                songList.add(song)
                Log.i(TAG, "Song: ${song.title}")
            }
        }
        liveData.postValue(songList)
        return liveData
    }

    fun getAllAlbums(): LiveData<List<Album>> {
        val liveData = MutableLiveData<List<Album>>()
        val albumList = mutableListOf<Album>()

        val projection = arrayOf(
            MediaStore.Audio.Albums._ID,
            MediaStore.Audio.Albums.ALBUM,
            MediaStore.Audio.Albums.ARTIST
        )

        val uri = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI
        val sortOrder = "${MediaStore.Audio.Albums.ALBUM} ASC"

        val cursor = context.contentResolver.query(uri, projection, null, null, sortOrder)

        cursor?.use {
            val idCol = it.getColumnIndexOrThrow(MediaStore.Audio.Albums._ID)
            val albumCol = it.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM)
            val artistCol = it.getColumnIndexOrThrow(MediaStore.Audio.Albums.ARTIST)

            while (it.moveToNext()) {
                val albumId = it.getLong(idCol)
                val albumName = it.getString(albumCol)
                val artistName = it.getString(artistCol)

                // Build content URI for album art (modern approach)
                val albumArtUri = ContentUris.withAppendedId(
                    Uri.parse("content://media/external/audio/albumart"),
                    albumId
                )

                val album = Album(
                    id = albumId,
                    name = albumName,
                    artist = artistName,
                    albumArtUri = albumArtUri
                )
                albumList.add(album)
            }
        }

        liveData.postValue(albumList)
        return liveData
    }


    fun getAllArtists(): LiveData<List<Artist>> {
        val liveData = MutableLiveData<List<Artist>>()
        val artistList = mutableListOf<Artist>()

        val projection = arrayOf(
            MediaStore.Audio.Artists._ID,
            MediaStore.Audio.Artists.ARTIST,
            MediaStore.Audio.Artists.NUMBER_OF_ALBUMS,
            MediaStore.Audio.Artists.NUMBER_OF_TRACKS
        )

        val uri = MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI
        val cursor = context.contentResolver.query(uri, projection, null, null, MediaStore.Audio.Artists.ARTIST + " ASC")

        cursor?.use {
            val idCol = it.getColumnIndexOrThrow(MediaStore.Audio.Artists._ID)
            val nameCol = it.getColumnIndexOrThrow(MediaStore.Audio.Artists.ARTIST)
            val albumsCol = it.getColumnIndexOrThrow(MediaStore.Audio.Artists.NUMBER_OF_ALBUMS)
            val tracksCol = it.getColumnIndexOrThrow(MediaStore.Audio.Artists.NUMBER_OF_TRACKS)

            while (it.moveToNext()) {
                val artist = Artist(
                    id = it.getLong(idCol),
                    name = it.getString(nameCol),
                    numberOfAlbums = it.getInt(albumsCol),
                    numberOfTracks = it.getInt(tracksCol)
                )
                artistList.add(artist)
            }
        }

        liveData.postValue(artistList)
        return liveData
    }

    fun getSongsForAlbum(albumName: String): LiveData<List<Song>> {
        val liveData = MutableLiveData<List<Song>>()
        val songList = mutableListOf<Song>()

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA
        )

        val selection = "${MediaStore.Audio.Media.ALBUM} = ?"
        val selectionArgs = arrayOf(albumName)
        val sortOrder = "${MediaStore.Audio.Media.TRACK} ASC"

        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val cursor = context.contentResolver.query(
            uri,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )

        cursor?.use {
            val idCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val albumIdCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val durationCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val dataCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)

            while (it.moveToNext()) {
                val contentUri = ContentUris.withAppendedId(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, it.getLong(idCol)
                )
                val song = Song(
                    id = it.getLong(idCol),
                    title = it.getString(titleCol),
                    artist = it.getString(artistCol),
                    album = it.getString(albumCol),
                    albumId = it.getLong(albumIdCol),
                    duration = it.getLong(durationCol),
                    uri = contentUri,
                    data = it.getString(dataCol)
                )
                songList.add(song)
            }
        }
        liveData.postValue(songList)
        return liveData
    }


    fun play(song: Song) {
        val mediaItem = MediaItem.fromUri(song.uri)
        player.setMediaItem(mediaItem)
        player.prepare()
        player.play()
    }

    fun togglePlayPause() {
        if (player.isPlaying) player.pause() else player.play()
    }
}