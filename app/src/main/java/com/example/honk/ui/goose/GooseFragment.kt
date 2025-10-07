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
import com.example.honk.R

class GooseFragment : Fragment() {

    private lateinit var gooseImage: ImageView
    private lateinit var xpBar: ProgressBar
    private lateinit var xpText: TextView
    private var gooseXP = 50  // 0–100
    private var isHappy = true
    private var mediaPlayer: MediaPlayer? = null

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

        gooseImage = view.findViewById(R.id.gooseImage)
        xpBar = view.findViewById(R.id.xpBar)
        xpText = view.findViewById(R.id.xpText)

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


    private fun honkGoose() {
        // animate goose: grow and shrink
        gooseImage.animate()
            .scaleX(1.3f)
            .scaleY(1.3f)
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
