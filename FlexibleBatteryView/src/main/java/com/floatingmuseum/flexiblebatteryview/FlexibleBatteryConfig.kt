package com.floatingmuseum.flexiblebatteryview

/**
 * Created by Floatingmuseum on 2021/7/8.
 *
 * you can't modify the core image with FlexibleBatteryConfig
 */
data class FlexibleBatteryConfig(
    val level: Int? = null,
    val borderColor: Int? = null,
    val borderWidth: Float? = null,
    val borderCornerRadius: Float? = null,
    val insideBackgroundColor: Int? = null,
    val insidePaddingWidth: Float? = null,
    val insideCoreColor: Int? = null,
    val insideCoreCornerRadius: Float? = null,
    val headHeight: Float? = null,
    val headWidth: Float? = null,
    val direction: Int? = null,
    val coreImageScaleType: Int? = null,
    val text: String? = null,
    val textColor: Int? = null,
    val textSize: Float? = null,
)
