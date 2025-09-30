package com.sulav.musicplayer

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class SongsFragment : Fragment() {
    private val TAG: String = "SongsFragment"
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SongsAdapter
    private lateinit var viewModel: MusicViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.i(TAG, "onCreateView")
        val view = inflater.inflate(R.layout.fragment_songs, container, false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize ViewModel safely (fragment is attached)
        val factory = MusicViewModelFactory(requireContext().applicationContext)
        viewModel = ViewModelProvider(requireActivity(), factory)[MusicViewModel::class.java]

        // RecyclerView setup
        recyclerView = view.findViewById(R.id.recycler_view_songs)
        recyclerView.layoutManager = LinearLayoutManager(context)

        // Observe songs
        viewModel.songs.observe(viewLifecycleOwner) { songs ->
            adapter = SongsAdapter(songs) { song ->
                viewModel.playSong(song)
            }
            recyclerView.adapter = adapter
        }
    }
}