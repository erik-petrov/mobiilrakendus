package com.example.honk.ui.goose

import android.animation.ObjectAnimator
import android.media.MediaPlayer
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.honk.R
import com.example.honk.repository.JokeRepository
import kotlinx.coroutines.launch
import android.widget.ImageButton
import kotlinx.coroutines.runBlocking
import androidx.navigation.fragment.findNavController

class GooseFragment : Fragment() {

    private lateinit var gooseImage: ImageView
    private lateinit var xpBar: ProgressBar
    private lateinit var xpText: TextView
    private lateinit var jokeText: TextView
    private var gooseXP = 50  // 0–100
    private var isHappy = true
    private var mediaPlayer: MediaPlayer? = null
    private var jr = JokeRepository()

    private val xpDecayRunnable = object : Runnable {
        override fun run() {
            gooseXP = (gooseXP - 5).coerceAtLeast(0)  // lose 5 XP every cycle
            updateGooseMood()

            // schedule again after delay
            gooseImage.postDelayed(this, 10000) // every 10 seconds (change as needed)
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_goose, container, false)

        val settingsButton = view.findViewById<ImageButton>(R.id.settingsButton)
        settingsButton.setOnClickListener {
            findNavController().navigate(R.id.settingsRealFragment)
        }

        gooseImage = view.findViewById(R.id.gooseImage)
        xpBar = view.findViewById(R.id.xpBar)
        xpText = view.findViewById(R.id.xpText)
        jokeText = view.findViewById(R.id.jokeText)

        updateJokeText()
        updateGooseMood()

        gooseImage.setOnClickListener {
            honkGoose()
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        gooseImage.postDelayed(xpDecayRunnable, 10000)
    }

    override fun onPause() {
        super.onPause()
        gooseImage.removeCallbacks(xpDecayRunnable)
    }

    private fun updateJokeText() {
        lifecycleScope.launch {
            try {
                val jk = jr.fetchShortJoke()
                jokeText.text = if (jk.isNotEmpty()) {
                    jk
                } else {
                    "ASCII silly question, get a silly ANSI."
                }
            } catch (e: Exception) {
                jokeText.text = "No joke this time :("
                e.printStackTrace()
            }
        }
    }

    private fun honkGoose() {
        // animate goose: grow and shrink
        gooseImage.animate()
            .scaleX(1.1f)
            .scaleY(1.1f)
            .setDuration(200)
            .withEndAction {
                gooseImage.animate().scaleX(1f).scaleY(1f).duration = 200
            }

        // play honk sound (optional)
        // mediaPlayer = MediaPlayer.create(requireContext(), R.raw.honk)
        // mediaPlayer?.start()

        // adjust XP randomly to simulate reactions
        gooseXP = (gooseXP + 5).coerceAtMost(100)
        updateGooseMood()
    }

    private fun updateGooseMood() {
        xpBar.progress = gooseXP
        xpText.text = "XP: $gooseXP / 100"

        isHappy = gooseXP > 40
        if (isHappy) {
            gooseImage.setImageResource(R.drawable.goose_happy)
        } else {
            gooseImage.setImageResource(R.drawable.goose_sad)
        }

        // smooth animation for XP bar
        ObjectAnimator.ofInt(xpBar, "progress", gooseXP).apply {
            duration = 500
            start()
        }
    }

    override fun onDestroy() {
        mediaPlayer?.release()
        super.onDestroy()
    }
}
