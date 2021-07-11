package com.floatingmuseum.flexiblebattery.demo

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.floatingmuseum.flexiblebattery.demo.databinding.ActivityMainBinding
import com.floatingmuseum.flexiblebatteryview.FlexibleBatteryConfig

class MainActivity : AppCompatActivity() {

    companion object {
        private const val PLAN_A = 0
        private const val PLAN_B = 1
    }

    private lateinit var binding: ActivityMainBinding
    private var isTextUseOn = false

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
                if (binding.switchTextUse.isChecked) {
                    binding.fbvBattery.setText("$p1")
                }
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
        binding.switchTextUse.setOnCheckedChangeListener { _, on ->
            Log.d("switchTextUseLog", "...on:$on")
            if (isTextUseOn != on) {
                isTextUseOn = on
                if (isTextUseOn) {
                    val level = binding.fbvBattery.getPowerLevel()
                    binding.fbvBattery.setText("$level")
                } else {
                    binding.fbvBattery.setText("")
                }
            }
        }
        isTextUseOn = binding.switchTextUse.isEnabled
        binding.tvTextColorRed.setOnClickListener { updateTextColor("#ffff4444") }
        binding.tvTextColorBlack.setOnClickListener { updateTextColor("#ff000000") }
        binding.tvTextColorGreen.setOnClickListener { updateTextColor("#ff669900") }
        binding.tvModifyMultipleAttrPlanA.setOnClickListener { updateMultipleAttr(PLAN_A) }
        binding.tvModifyMultipleAttrPlanB.setOnClickListener { updateMultipleAttr(PLAN_B) }
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

    private fun updateTextColor(colorString: String) {
        binding.fbvBattery.setTextColor(Color.parseColor(colorString))
    }

    private fun updateMultipleAttr(plan: Int) {
        //you can't modify the core image with FlexibleBatteryConfig
        when (plan) {
            PLAN_A -> {
                binding.fbvBattery.setBatteryConfig(
                    FlexibleBatteryConfig(
                        level = 30,
                        borderColor = Color.MAGENTA,
                        insideCoreColor = Color.DKGRAY,
                        insidePaddingWidth = 10F
                    )
                )
            }
            PLAN_B -> {
                binding.fbvBattery.setBatteryConfig(
                    FlexibleBatteryConfig(
                        level = 90,
                        borderWidth = 4F,
                        borderCornerRadius = 10F,
                        insideCoreColor = Color.YELLOW,
                        insidePaddingWidth = 2F,
                        textColor = Color.WHITE,
                        textSize = 20F
                    )
                )
            }
        }
    }
}