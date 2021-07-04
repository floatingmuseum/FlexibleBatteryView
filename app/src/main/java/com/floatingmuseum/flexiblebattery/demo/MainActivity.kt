package com.floatingmuseum.flexiblebattery.demo

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.floatingmuseum.flexiblebattery.demo.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initView()
    }

    private fun initView() {
        binding.seekbarLevel.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                binding.fbvBattery.setPowerLevel(p1)
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
            }
        })

        binding.tvBorderRed.setOnClickListener { updateBorderColor("#ffff4444") }
        binding.tvBorderBlack.setOnClickListener { updateBorderColor("#ff000000") }
        binding.tvBorderTransparent.setOnClickListener { updateBorderColor("#00000000") }

        binding.tvInsideBackgroundColorRed.setOnClickListener { updateInsideBackgroundColor("#ffff4444") }
        binding.tvInsideBackgroundColorBlack.setOnClickListener { updateInsideBackgroundColor("#ff000000") }
        binding.tvInsideBackgroundColorTransparent.setOnClickListener {
            updateInsideBackgroundColor("#00000000")
        }

        binding.tvInsideCoreColorRed.setOnClickListener { updateInsideCoreColor("#ffff4444") }
        binding.tvInsideCoreColorBlack.setOnClickListener { updateInsideCoreColor("#ff000000") }
        binding.tvInsideCoreColorGreen.setOnClickListener { updateInsideCoreColor("#ff669900") }

        binding.ivInsideCoreImageYellowBolt.setOnClickListener {
            updateInsideCoreImage(
                ContextCompat.getDrawable(this, R.drawable.flash_50_yellow)
            )
        }
        binding.ivInsideCoreImageRedBolt.setOnClickListener {
            updateInsideCoreImage(
                ContextCompat.getDrawable(this, R.drawable.flash_50_red)
            )
        }
        binding.tvInsideCoreImageNon.setOnClickListener { updateInsideCoreImage(null) }
    }

    private fun updateBorderColor(colorString: String) {
        binding.fbvBattery.setBorderColor(Color.parseColor(colorString))
    }

    private fun updateInsideBackgroundColor(colorString: String) {
        binding.fbvBattery.setInsideBackgroundColor(Color.parseColor(colorString))
    }

    private fun updateInsideCoreColor(colorString: String) {
        binding.fbvBattery.setInsideCoreColor(Color.parseColor(colorString))
    }

    private fun updateInsideCoreImage(d: Drawable?) {
        binding.fbvBattery.setCoreImage(d)
    }
}