package com.sulav.musicplayer

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.tabs.TabLayout

class AlbumSongsFragment : Fragment(R.layout.fragment_album_songs) {

    private val TAG = "SULAV: AlbumSongsFragment"
    private val viewModel: MusicViewModel by activityViewModels()
    private lateinit var adapter: AlbumSongAdapter


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get album name from arguments
        val albumName = requireArguments().getString("albumName") ?: return
        if (albumName == null) {
            Log.e("AlbumSongsFragment", "Album name is null")
            return
        }

        // Hide MainActivity's toolbar and tabs
        (requireActivity() as MainActivity).apply {
            findViewById<TabLayout>(R.id.tabLayout)?.visibility = View.GONE
            supportActionBar?.hide()
        }
        // Setup toolbar
        val toolbar = view.findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.title = albumName
        toolbar.setNavigationOnClickListener {
            // Show MainActivity's toolbar and tabs before popping back
            (requireActivity() as MainActivity).apply {
                findViewById<TabLayout>(R.id.tabLayout)?.visibility = View.VISIBLE
                supportActionBar?.show()
            }
            parentFragmentManager.popBackStack()
        }

        // Setup RecyclerView
        adapter = AlbumSongAdapter { song ->
            viewModel.playSong(song)
        }

        view.findViewById<RecyclerView>(R.id.recycler_album_songs).apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@AlbumSongsFragment.adapter
        }

        // Load and observe data
        viewModel.getSongsForAlbum(albumName).observe(viewLifecycleOwner) { songs ->
            Log.d("AlbumSongsFragment", "Received ${songs.size} songs for album $albumName")
            adapter.submitList(songs)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Ensure views are restored when fragment is destroyed
        (requireActivity() as MainActivity).apply {
            findViewById<TabLayout>(R.id.tabLayout)?.visibility = View.VISIBLE
            supportActionBar?.show()
        }
    }
}

