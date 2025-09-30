package com.sulav.musicplayer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class AlbumAdapter(
    private val onClick: (Album) -> Unit
) : ListAdapter<Album, AlbumAdapter.AlbumViewHolder>(DiffCallback()) {

    class AlbumViewHolder(view: View, val onClick: (Album) -> Unit) :
        RecyclerView.ViewHolder(view) {
        private val title = view.findViewById<TextView>(R.id.album_name)
        private val image = view.findViewById<ImageView>(R.id.album_art)
        private var currentAlbum: Album? = null

        init {
            view.setOnClickListener {
                currentAlbum?.let { onClick(it) }
            }
        }

        fun bind(album: Album) {
            currentAlbum = album
            title.text = album.name
            //image.setImageBitmap(album.artBitmap)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_album_card, parent, false)
        return AlbumViewHolder(view, onClick)
    }

    override fun onBindViewHolder(holder: AlbumViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<Album>() {
        override fun areItemsTheSame(old: Album, new: Album) = old.id == new.id
        override fun areContentsTheSame(old: Album, new: Album) = old == new
    }
}
