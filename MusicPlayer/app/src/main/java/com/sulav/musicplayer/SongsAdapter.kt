package com.sulav.musicplayer

import android.content.ContentUris
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class SongsAdapter(
    private val songs: List<Song>,
    private val onSongClick: (Song) -> Unit
): RecyclerView.Adapter<SongsAdapter.SongViewHolder>() {
    inner class SongViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        private val title: TextView = itemView.findViewById(R.id.song_title)
        private val artist: TextView = itemView.findViewById(R.id.song_artist)
        private val albumArt: ImageView = itemView.findViewById(R.id.album_art)

        fun bind(song: Song) {
            title.text = song.title
            artist.text = song.artist

            val albumArtUri = ContentUris.withAppendedId(
                Uri.parse("content://media/external/audio/albumart"),
                song.albumId
            )
            Glide.with(itemView.context)
                .load(albumArtUri)
                .placeholder(R.drawable.ic_launcher_foreground)
                .into(albumArt)
            itemView.setOnClickListener {
                onSongClick(song)
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SongViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_song, parent, false)
        return SongViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: SongViewHolder,
        position: Int
    ) {
        holder.bind(songs[position])
    }

    override fun getItemCount(): Int = songs.size
}