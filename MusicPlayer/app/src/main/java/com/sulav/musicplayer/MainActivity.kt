package com.sulav.musicplayer

import android.Manifest
import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.ContentUris
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class MainActivity : AppCompatActivity() {
    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout

    private lateinit var bottomSheet: LinearLayout
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>

    // 🎵 ViewModel
    private val viewModel: MusicViewModel by viewModels {
        MusicViewModelFactory(applicationContext)
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                initUI()
                observeViewModel()
            } else {
                Toast.makeText(
                    this,
                    "Permission denied. Cannot load music.",
                    Toast.LENGTH_LONG
                ).show()
                finish()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_MEDIA_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            initUI()
            observeViewModel()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_AUDIO)
        }
    }

    private fun initUI() {
        bottomSheet = findViewById(R.id.player_bottom_sheet)
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
        val dimOverlay = findViewById<View>(R.id.dim_overlay)

        val miniPlayer = bottomSheet.findViewById<View>(R.id.mini_player)
        val fullPlayer = bottomSheet.findViewById<View>(R.id.full_player)

        bottomSheetBehavior.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        miniPlayer.visibility = View.VISIBLE
                        fullPlayer.visibility = View.GONE
                        dimOverlay.visibility = View.GONE
                    }
                    BottomSheetBehavior.STATE_EXPANDED -> {
                        miniPlayer.visibility = View.GONE
                        fullPlayer.visibility = View.VISIBLE
                        dimOverlay.visibility = View.VISIBLE
                    }
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                // Optional: animate transitions
                dimOverlay.alpha = slideOffset
            }
        })

        bottomSheetBehavior.isFitToContents = true
        bottomSheetBehavior.isDraggable = true
        bottomSheetBehavior.skipCollapsed = false
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED

        // Setup ViewPager + Tabs
        viewPager = findViewById(R.id.viewPager)
        tabLayout = findViewById(R.id.tabLayout)

        for (i in 0 until tabLayout.tabCount) {
            val tab = tabLayout.getTabAt(i)
            val customView = LayoutInflater.from(this).inflate(R.layout.custom_tab, null) as TextView
            customView.text = tab?.text
            tab?.customView = customView
        }

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                val textView = tab.customView as? TextView
                textView?.let { animateTextViewSize(it, true) }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                val textView = tab.customView as? TextView
                textView?.let { animateTextViewSize(it, false) }
            }

            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        val fragments = listOf(
            SongsFragment(),
            AlbumsFragment(),
            //ArtistsFragment()
        )

        viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int = fragments.size
            override fun createFragment(position: Int): Fragment = fragments[position]
        }

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Songs"
                1 -> "Albums"
                //2 -> "Artists"
                else -> ""
            }
        }.attach()
    }

    // 🔥 Hook observers to update mini + full player
    private fun observeViewModel() {
        val miniAlbumArt = bottomSheet.findViewById<ImageView>(R.id.mini_album_art)
        val miniSongTitle = bottomSheet.findViewById<TextView>(R.id.mini_song_title)
        val miniPlayPause = bottomSheet.findViewById<ImageButton>(R.id.mini_play_pause)

        val fullAlbumArt = bottomSheet.findViewById<ImageView>(R.id.full_album_art)
        val fullSongTitle = bottomSheet.findViewById<TextView>(R.id.full_song_title)
        val playPauseBtn = bottomSheet.findViewById<ImageButton>(R.id.btn_play_pause)
        val nextBtn = bottomSheet.findViewById<ImageButton>(R.id.btn_next)
        val prevBtn = bottomSheet.findViewById<ImageButton>(R.id.btn_prev)
        val seekBar = bottomSheet.findViewById<SeekBar>(R.id.seek_bar)

        // ✅ Observe current song
        viewModel.currentSong.observe(this) { song ->
            if (song != null) {
                miniSongTitle.text = song.title
                fullSongTitle.text = song.title

                val albumArtUri = ContentUris.withAppendedId(
                    Uri.parse("content://media/external/audio/albumart"),
                    song.albumId
                )

                Glide.with(this)
                    .load(albumArtUri)
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .into(miniAlbumArt)

                Glide.with(this)
                    .load(albumArtUri)
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .into(fullAlbumArt)
            }
        }

        // ✅ Observe playback state
        viewModel.isPlaying.observe(this) { playing ->
            val icon = if (playing) androidx.media3.session.R.drawable.media3_icon_pause else androidx.media3.session.R.drawable.media3_icon_play
            miniPlayPause.setImageResource(icon)
            playPauseBtn.setImageResource(icon)
        }

        // ✅ Click listeners
        miniPlayPause.setOnClickListener { viewModel.togglePlayPause() }
        playPauseBtn.setOnClickListener { viewModel.togglePlayPause() }
        nextBtn.setOnClickListener { viewModel.skipNext() } // implement skipNext in VM
        prevBtn.setOnClickListener { viewModel.skipPrevious() } // implement skipPrevious in VM
    }

    fun animateTextViewSize(textView: TextView, isSelected: Boolean) {
        val startSize = if (isSelected) 16f else 24f   // sp
        val endSize = if (isSelected) 24f else 16f     // sp

        val animator = ValueAnimator.ofFloat(startSize, endSize)
        animator.duration = 200
        animator.addUpdateListener { valueAnimator ->
            textView.textSize = valueAnimator.animatedValue as Float
        }
        animator.start()
    }
}
