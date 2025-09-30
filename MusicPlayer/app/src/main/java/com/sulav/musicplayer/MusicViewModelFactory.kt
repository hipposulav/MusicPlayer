package com.sulav.musicplayer

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class MusicViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MusicViewModel::class.java)) {
            val repo = MusicRepository(context.applicationContext)
            @Suppress("UNCHECKED_CAST")
            return MusicViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
