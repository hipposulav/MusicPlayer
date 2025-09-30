package com.sulav.musicplayer

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MusicViewModel(private val repo: MusicRepository) : ViewModel() {

    val songs: LiveData<List<Song>> = repo.getAllSongs()
    val albums: LiveData<List<Album>> = repo.getAllAlbums()
    val artists: LiveData<List<Artist>> = repo.getAllArtists()
    val currentSong = MutableLiveData<Song?>()
    val isPlaying = MutableLiveData(false)

    // Track the current index
    private var currentIndex: Int = -1

    fun getSongsForAlbum(albumName: String): LiveData<List<Song>> {
        return repo.getSongsForAlbum(albumName)
    }

    fun playSong(song: Song) {
        val list = songs.value ?: return
        currentIndex = list.indexOf(song)
        if (currentIndex != -1) {
            currentSong.value = song
            isPlaying.value = true
            repo.play(song)
        }
    }

    fun togglePlayPause() {
        val playing = !(isPlaying.value ?: false)
        isPlaying.value = playing
        repo.togglePlayPause()
    }

    fun skipNext() {
        val list = songs.value ?: return
        if (list.isEmpty()) return

        if (currentIndex == -1) currentIndex = 0
        else currentIndex = (currentIndex + 1) % list.size

        val nextSong = list[currentIndex]
        currentSong.value = nextSong
        isPlaying.value = true
        repo.play(nextSong)
    }

    fun skipPrevious() {
        val list = songs.value ?: return
        if (list.isEmpty()) return

        if (currentIndex == -1) currentIndex = 0
        else currentIndex = if (currentIndex - 1 < 0) list.size - 1 else currentIndex - 1

        val prevSong = list[currentIndex]
        currentSong.value = prevSong
        isPlaying.value = true
        repo.play(prevSong)
    }
}
