package com.sulav.musicplayer

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class AlbumsFragment : Fragment(R.layout.fragment_albums) {

    private val viewModel: MusicViewModel by activityViewModels()
    private lateinit var adapter: AlbumAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = AlbumAdapter { album ->
            val bundle = bundleOf("albumName" to album.name)
            parentFragmentManager.beginTransaction()
                .setCustomAnimations(
                    R.anim.slide_in_right,  // enter
                    R.anim.slide_out_left,  // exit
                    R.anim.slide_in_right,  // popEnter
                    R.anim.slide_out_left   // popExit
                )
                .add(R.id.album_fragment_container, AlbumSongsFragment::class.java, bundle)
                .addToBackStack(null)
                .commit()
        }

        val recycler = view.findViewById<RecyclerView>(R.id.recycler_albums)
        recycler.layoutManager = GridLayoutManager(requireContext(), 2)
        recycler.adapter = adapter

        viewModel.albums.observe(viewLifecycleOwner) { albums ->
            adapter.submitList(albums)
        }
    }
}