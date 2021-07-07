package com.floatingmuseum.flexiblebatteryview

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.core.graphics.drawable.toBitmap

/**
 * Created by Floatingmuseum on 2021/6/28.
 */
class FlexibleBatteryView : View {

    companion object {
        private const val TAG = "FlexibleBatteryView"

        private const val DIRECTION_UP = 0
        private const val DIRECTION_LEFT = 1
        private const val DIRECTION_RIGHT = 2

        private const val CORE_IMAGE_SCALE_TYPE_FIT_CENTER = 0
        private const val CORE_IMAGE_SCALE_TYPE_FIT_XY = 1
    }

    private var insidePowerColor = Color.WHITE
    private var insideBackgroundColor = Color.TRANSPARENT
    private var borderColor = Color.WHITE
    private var borderWidth = 0F
    private var borderCornerRadius = 0F
    private var insideCoreCornerRadius = 0F
    private var insidePaddingWidth = 0F
    private var headWidth = 0F
    private var headHeight = 0F
    private var level = 0
    private lateinit var paint: Paint
    private val rect = RectF()
    private var coreImageDrawable: Drawable? = null
    private var lastCoreDrawableHash = -1
    private var lastFinalBitmap: Bitmap? = null
    private var lastFinalBitmapDrawStartX = (-1).toFloat()
    private var lastFinalBitmapDrawStartY = (-1).toFloat()
    private var viewWidth = 0
    private var viewHeight = 0
    private var coreImageScaleType = CORE_IMAGE_SCALE_TYPE_FIT_CENTER
    private var direction = DIRECTION_UP

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attributeSet: AttributeSet?) : this(context, attributeSet, 0)
    constructor(context: Context, attributeSet: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attributeSet,
        defStyleAttr
    ) {
        initAttrs(attributeSet)
        initPaint()
    }

    private fun initAttrs(attributeSet: AttributeSet?) {
        attributeSet?.let { attr ->
            val typedArray = context.obtainStyledAttributes(attr, R.styleable.FlexibleBatteryView)
            insidePowerColor =
                typedArray.getColor(R.styleable.FlexibleBatteryView_inside_power_color, Color.WHITE)
            insideBackgroundColor =
                typedArray.getColor(
                    R.styleable.FlexibleBatteryView_inside_background_color,
                    Color.TRANSPARENT
                )
            borderColor =
                typedArray.getColor(R.styleable.FlexibleBatteryView_border_color, Color.WHITE)
            borderWidth = typedArray.getDimension(R.styleable.FlexibleBatteryView_border_width, 0F)
            borderCornerRadius =
                typedArray.getDimension(R.styleable.FlexibleBatteryView_border_corner_radius, 0F)
            insideCoreCornerRadius =
                typedArray.getDimension(R.styleable.FlexibleBatteryView_inside_core_radius, 0F)
            insidePaddingWidth =
                typedArray.getDimension(R.styleable.FlexibleBatteryView_inside_padding, 0F)
            headWidth = typedArray.getDimension(R.styleable.FlexibleBatteryView_head_width, 0F)
            headHeight = typedArray.getDimension(R.styleable.FlexibleBatteryView_head_height, 0F)
            coreImageDrawable = typedArray.getDrawable(R.styleable.FlexibleBatteryView_core_image)
            val xmlLevel = typedArray.getInt(R.styleable.FlexibleBatteryView_power_level, 0)
            coreImageScaleType =
                typedArray.getInt(R.styleable.FlexibleBatteryView_core_image_scale_type, CORE_IMAGE_SCALE_TYPE_FIT_CENTER)
            direction =  typedArray.getInt(R.styleable.FlexibleBatteryView_direction, DIRECTION_UP)
            level = getLegalPowerLevel(xmlLevel)
            Log.d(TAG, "initAttrs()...direction:$direction")
            Log.d(TAG, "initAttrs()...insidePowerColor:$insidePowerColor")
            Log.d(TAG, "initAttrs()...insideBackgroundColor:$insideBackgroundColor")
            Log.d(TAG, "initAttrs()...borderColor:$borderColor")
            Log.d(TAG, "initAttrs()...borderWidth:$borderWidth")
            Log.d(TAG, "initAttrs()...insidePaddingWidth:$insidePaddingWidth")
            Log.d(TAG, "initAttrs()...headWidth:$headWidth")
            Log.d(TAG, "initAttrs()...headHeight:$headHeight")
            Log.d(TAG, "initAttrs()...level:$level")
            Log.d(TAG, "initAttrs()...coreImageScaleType:$coreImageScaleType")
            typedArray.recycle()
        }
    }

    private fun initPaint() {
        paint = Paint()
        paint.isAntiAlias = true
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        viewWidth = measuredWidth
        viewHeight = measuredHeight
        Log.d(TAG, "onMeasure()...viewWidth:$viewWidth...viewHeight:$viewHeight")
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        Log.d(TAG, "onDraw()...viewWidth:$viewWidth...viewHeight:$viewHeight")
        Log.d(TAG, "onDraw()...canvas:$canvas")
        canvas?.let {
            //draw battery border without head
            paint.color = borderColor
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = borderWidth
            val finalHeadHeight = calcHeadHeight()
            Log.d(TAG, "onDraw()...headHeight:$finalHeadHeight")

            //if border width more than 0，than paint position should move some distance
            val offsetBorderWidth = borderWidth / 2

            buildBorderRect(offsetBorderWidth, finalHeadHeight)
            canvas.drawRoundRect(rect, borderCornerRadius, borderCornerRadius, paint)
            rect.setEmpty()

            //change paint style to FILL for next operations
            paint.style = Paint.Style.FILL

            //draw inside background
            //if inside padding is 0 or inside background is transparent，then no background needed
            if (insidePaddingWidth > 0 && insideBackgroundColor != Color.TRANSPARENT) {
                paint.color = insideBackgroundColor
                paint.strokeWidth = 0F
                paint.style = Paint.Style.FILL_AND_STROKE
                buildBackgroundRect(finalHeadHeight)
                canvas.drawRoundRect(rect, borderCornerRadius, borderCornerRadius, paint)
                rect.setEmpty()
            }

            //draw battery core
            paint.color = insidePowerColor

            val coreSize = buildCoreRect(finalHeadHeight)
            canvas.drawRoundRect(rect, insideCoreCornerRadius, insideCoreCornerRadius, paint)
            rect.setEmpty()
            //draw inside core image
            drawCoreBitmap(it, coreSize[0], coreSize[1], coreSize[2], coreSize[3])

            //draw battery head
            paint.color = if (borderWidth == 0F || borderColor == Color.TRANSPARENT) {
                insideBackgroundColor
            } else {
                borderColor
            }
            buildHeadRect(finalHeadHeight)
            canvas.drawRect(rect, paint)
            rect.setEmpty()
        }
    }

    private fun calcHeadHeight(): Float {
        return when {
            //use specified head height
            headHeight > 0F -> headHeight
            //use border width as head height
            borderWidth > 0 -> borderWidth
            //use 5% of whole battery height
            direction == DIRECTION_UP -> (viewHeight * 0.05).toFloat()
            //use 5% of whole battery width
            direction == DIRECTION_LEFT  -> (viewWidth * 0.05).toFloat()
            direction == DIRECTION_RIGHT -> (viewWidth * 0.05).toFloat()
            else -> 0F
        }
    }

    private fun buildBorderRect(offsetBorderWidth: Float, headHeight: Float) {
        var left = 0F
        var top = 0F
        var right = 0F
        var bottom = 0F
        when (direction) {
            DIRECTION_UP -> {
                left = offsetBorderWidth
                top = headHeight + offsetBorderWidth
                right = viewWidth.toFloat() - offsetBorderWidth
                bottom = viewHeight.toFloat() - offsetBorderWidth
            }
            DIRECTION_LEFT -> {
                left = headHeight + offsetBorderWidth
                top = offsetBorderWidth
                right = viewWidth.toFloat() - offsetBorderWidth
                bottom = viewHeight.toFloat() - offsetBorderWidth
            }
            DIRECTION_RIGHT -> {
                left = offsetBorderWidth
                top = offsetBorderWidth
                right = viewWidth.toFloat() - headHeight - offsetBorderWidth
                bottom = viewHeight.toFloat() - offsetBorderWidth
            }
        }
        rect.set(left, top, right, bottom)
        Log.d(TAG, "buildBorderRect()...rect:$rect")
    }

    private fun buildBackgroundRect(headHeight: Float) {
        var left = 0F
        var top = 0F
        var right = 0F
        var bottom = 0F
        when (direction) {
            DIRECTION_UP -> {
                left = borderWidth
                top = headHeight + borderWidth
                right = viewWidth.toFloat() - borderWidth
                bottom = viewHeight.toFloat() - borderWidth
            }
            DIRECTION_LEFT -> {
                left = headHeight + borderWidth
                top = borderWidth
                right = viewWidth.toFloat() - borderWidth
                bottom = viewHeight.toFloat() - borderWidth
            }
            DIRECTION_RIGHT -> {
                left = borderWidth
                top = borderWidth
                right = viewWidth.toFloat() - headHeight - borderWidth
                bottom = viewHeight.toFloat() - borderWidth
            }
        }
        rect.set(left, top, right, bottom)
        Log.d(TAG, "buildBackgroundRect()...rect:$rect")
    }

    private fun buildCoreRect(headHeight: Float) : Array<Float> {
        Log.d(TAG, "buildCoreRect()...direction:$direction")
        var left = 0F
        var top = 0F
        var right = 0F
        var bottom = 0F
        var coreLeftX = 0F
        var coreTopY = 0F
        var coreRightX = 0F
        var coreBottomY = 0F
        when (direction) {
            DIRECTION_UP -> {
                 coreTopY = headHeight + borderWidth + insidePaddingWidth
                 coreBottomY = viewHeight.toFloat() - borderWidth - insidePaddingWidth
                //whole battery length
                val coreFullLength = coreBottomY - coreTopY
                //unreached battery length
                val unreachedLength = (100 - level).toFloat() / 100 * coreFullLength
                 coreLeftX = borderWidth + insidePaddingWidth
                coreRightX = viewWidth.toFloat() - borderWidth - insidePaddingWidth
                val coreStartDrawY = coreTopY + unreachedLength
                left = coreLeftX
                top = coreStartDrawY
                right = coreRightX
                bottom = coreBottomY
            }
            DIRECTION_LEFT -> {
                coreTopY =  borderWidth + insidePaddingWidth
                coreBottomY = viewHeight.toFloat() - borderWidth - insidePaddingWidth
                coreLeftX = headHeight + borderWidth + insidePaddingWidth
                coreRightX = viewWidth.toFloat() - borderWidth - insidePaddingWidth
                //whole battery length
                val coreFullLength = coreRightX - coreLeftX
                //unreached battery length
                val unreachedLength = (100 - level).toFloat() / 100 * coreFullLength
                val coreStartDrawX = coreLeftX + unreachedLength

                left = coreStartDrawX
                top = coreTopY
                right = coreRightX
                bottom = coreBottomY
            }
            DIRECTION_RIGHT -> {
                coreTopY =  borderWidth + insidePaddingWidth
                coreBottomY = viewHeight.toFloat() - borderWidth - insidePaddingWidth
                coreLeftX = borderWidth + insidePaddingWidth
                coreRightX = viewWidth.toFloat() - headHeight - borderWidth - insidePaddingWidth
                //whole battery length
                val coreFullLength = coreRightX - coreLeftX
                //unreached battery length
                val unreachedLength = (100 - level).toFloat() / 100 * coreFullLength
                val coreEndDrawX = coreFullLength - unreachedLength + coreLeftX

                left = coreLeftX
                top = coreTopY
                right = coreEndDrawX
                bottom = coreBottomY
            }
        }
        rect.set(left, top, right, bottom)
        Log.d(TAG, "buildCoreRect()...rect:$rect")
        Log.d(TAG, "buildCoreRect()...coreLeftX:$coreLeftX...coreTopY:$coreTopY...coreRightX:$coreRightX...coreBottomY:$coreBottomY")
        return arrayOf(coreLeftX, coreTopY, coreRightX, coreBottomY)
    }

    private fun buildHeadRect(finalHeadHeight: Float) {
        var finalHeadWidth= 0F
        when (direction) {
            DIRECTION_UP -> {
                finalHeadWidth = if (headWidth > 0F && headWidth <= viewWidth) {
                    headWidth
                }
                //default head width is 50% of whole battery width
                else {
                    (viewWidth / 2).toFloat()
                }
            }
            DIRECTION_LEFT, DIRECTION_RIGHT -> {
                finalHeadWidth = if (headWidth > 0F && headWidth <= viewHeight) {
                    headWidth
                }
                //default head width is 50% of whole battery width
                else {
                    (viewHeight / 2).toFloat()
                }
            }
        }
        Log.d(TAG, "buildHeadRect()...finalHeadWidth:$finalHeadWidth")
        var left = 0F
        var top = 0F
        var right = 0F
        var bottom = 0F
        when (direction) {
            DIRECTION_UP -> {
                left = (viewWidth - finalHeadWidth) / 2
                right = left + finalHeadWidth
                bottom = finalHeadHeight
            }
            DIRECTION_LEFT -> {
                top = (viewHeight - finalHeadWidth) / 2
                right = finalHeadHeight
                bottom = top + finalHeadWidth
            }
            DIRECTION_RIGHT -> {
                top = (viewHeight - finalHeadWidth) / 2
                left = viewWidth - finalHeadHeight
                right = viewWidth.toFloat()
                bottom = top + finalHeadWidth
            }
        }
        Log.d(TAG, "buildHeadRect()...rect:$rect")
        rect.set(left, top, right, bottom)
    }

    /**
     * keep image scale and zoom up，show it in the center of core
     */
    private fun drawCoreBitmap(
        canvas: Canvas,
        coreStartX: Float,
        coreTopY: Float,
        coreEndX: Float,
        coreBottomY: Float
    ) {
        val currentDrawableHash = coreImageDrawable?.hashCode() ?: -1
        var useOldBitmap = false
        Log.d(TAG, "drawCoreBitmap()...currentDrawableHash:$currentDrawableHash")
        Log.d(TAG, "drawCoreBitmap()...lastCoreDrawableHash:$lastCoreDrawableHash")

        if (currentDrawableHash != -1 && currentDrawableHash == lastCoreDrawableHash) {
            useOldBitmap = true
        } else {
            lastCoreDrawableHash = currentDrawableHash
            if (lastCoreDrawableHash == -1) {
                lastFinalBitmap = null
                lastFinalBitmapDrawStartX = (-1).toFloat()
                lastFinalBitmapDrawStartY = (-1).toFloat()
            }
        }
        Log.d(TAG, "drawCoreBitmap()...useOldBitmap:$useOldBitmap")
        if (useOldBitmap) {
            lastFinalBitmap?.let {
                canvas.drawBitmap(it, lastFinalBitmapDrawStartX, lastFinalBitmapDrawStartY, paint)
            }
        } else {
            coreImageDrawable?.toBitmap()?.let { sourceBitmap ->
                Log.d(TAG, "drawCoreBitmap()...sourceBitmap:${sourceBitmap.hashCode()}")

                val rawWidth = sourceBitmap.width
                val rawHeight = sourceBitmap.height

                val coreFulWidth = coreEndX - coreStartX
                val coreFullHeight = coreBottomY - coreTopY

                val widthScale = coreFulWidth / rawWidth
                val heightScale = coreFullHeight / rawHeight
                var finalBitmapWidth = coreEndX - coreStartX
                var finalBitmapHeight = coreFullHeight - coreTopY
                if (coreImageScaleType == CORE_IMAGE_SCALE_TYPE_FIT_CENTER) {
                    //choose short side
                    val smallScale = if (widthScale < heightScale) {
                        widthScale
                    } else {
                        heightScale
                    }
                    finalBitmapWidth = rawWidth * smallScale
                    finalBitmapHeight = rawHeight * smallScale
                } else if (coreImageScaleType == CORE_IMAGE_SCALE_TYPE_FIT_XY) {
                    //do not need change finalBitmapWidth and finalBitmapHeight for fitXY
                }

                Log.d(TAG, "drawCoreBitmap()...rawWidth:$rawWidth...rawHeight:$rawHeight")
                Log.d(TAG, "drawCoreBitmap()...finalBitmapWidth:$finalBitmapWidth...finalBitmapHeight:$finalBitmapHeight")
                // calc draw position X
                lastFinalBitmapDrawStartX = coreFulWidth / 2 - finalBitmapWidth / 2 + coreStartX
                // calc draw position Y
                lastFinalBitmapDrawStartY = coreFullHeight / 2 - finalBitmapHeight / 2 + coreTopY

                val finalBitmap = Bitmap.createScaledBitmap(
                    sourceBitmap,
                    finalBitmapWidth.toInt(),
                    finalBitmapHeight.toInt(),
                    true
                )
                lastFinalBitmap = finalBitmap
                canvas.drawBitmap(
                    finalBitmap,
                    lastFinalBitmapDrawStartX,
                    lastFinalBitmapDrawStartY,
                    paint
                )
            }
        }
    }

    /**
     * battery level between 0 to 100
     */
    fun setPowerLevel(powerLevel: Int) {
        if (powerLevel != level) {
            level = getLegalPowerLevel(powerLevel)
            invalidate()
        }
    }

    fun getPowerLevel() = level

    private fun getLegalPowerLevel(powerLevel: Int): Int {
        return when {
            powerLevel < 0 -> 0
            powerLevel > 100 -> 100
            else -> powerLevel
        }
    }

    fun setBorderColor(color: Int) {
        if (borderColor != color) {
            borderColor = color
            invalidate()
        }
    }

    fun setInsideBackgroundColor(color: Int) {
        if (insideBackgroundColor != color) {
            insideBackgroundColor = color
            invalidate()
        }
    }

    fun setInsideCoreColor(color: Int) {
        if (insidePowerColor != color) {
            insidePowerColor = color
            invalidate()
        }
    }

    fun setCoreImage(d: Drawable?) {
        coreImageDrawable = d
        invalidate()
    }
}