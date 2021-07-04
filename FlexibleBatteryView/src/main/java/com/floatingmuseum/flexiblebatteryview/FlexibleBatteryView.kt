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
            level = getLegalPowerLevel(xmlLevel)
            Log.d(TAG, "initAttrs()...insidePowerColor:$insidePowerColor")
            Log.d(TAG, "initAttrs()...insideBackgroundColor:$insideBackgroundColor")
            Log.d(TAG, "initAttrs()...borderColor:$borderColor")
            Log.d(TAG, "initAttrs()...borderWidth:$borderWidth")
            Log.d(TAG, "initAttrs()...insidePaddingWidth:$insidePaddingWidth")
            Log.d(TAG, "initAttrs()...headWidth:$headWidth")
            Log.d(TAG, "initAttrs()...headHeight:$headHeight")
            Log.d(TAG, "initAttrs()...level:$level")
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
            val headHeight = when {
                //use specified head height
                headHeight > 0F -> headHeight
                //use border width as head height
                borderWidth > 0 -> borderWidth
                //use 5% of whole battery height
                else -> (viewHeight * 0.05).toFloat()
            }
            Log.d(TAG, "onDraw()...headHeight:$headHeight")

            //if border width more than 0，than paint position should move some distance
            val offsetBorderWidth = borderWidth / 2

            rect.set(
                offsetBorderWidth,
                headHeight + offsetBorderWidth,
                viewWidth.toFloat() - offsetBorderWidth,
                viewHeight.toFloat() - offsetBorderWidth
            )
            Log.d(TAG, "onDraw()...border rect:$rect")
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
                rect.set(
                    borderWidth,
                    headHeight + borderWidth,
                    viewWidth.toFloat() - borderWidth,
                    viewHeight.toFloat() - borderWidth
                )
                Log.d(TAG, "onDraw()...background rect:$rect")
                canvas.drawRoundRect(rect, borderCornerRadius, borderCornerRadius, paint)
                rect.setEmpty()
            }

            //draw battery core
            paint.color = insidePowerColor
            val coreTopY = headHeight + borderWidth + insidePaddingWidth
            val coreBottomY = viewHeight.toFloat() - borderWidth - insidePaddingWidth
            //whole battery height
            val coreFullHeight = coreBottomY - coreTopY
            //unreached battery height
            val unreachedHeight = (100 - level).toFloat() / 100 * coreFullHeight
            val coreStartX = borderWidth + insidePaddingWidth
            val coreEndX = viewWidth.toFloat() - borderWidth - insidePaddingWidth
            val coreStartY = coreTopY + unreachedHeight
            rect.set(
                coreStartX,
                coreStartY,
                coreEndX,
                coreBottomY
            )
            Log.d(TAG, "onDraw()...core rect:$rect")
            canvas.drawRoundRect(rect, insideCoreCornerRadius, insideCoreCornerRadius, paint)

            //this will let image scaleStyle to FIT_XY
//            coreImageDrawable?.toBitmap()?.let { sourceBitmap ->
//                val bitmapRect = RectF(coreStartX,coreTopY,coreEndX,coreBottomY)
//                Log.d(TAG, "onDraw()...core bitmapRect:$bitmapRect")
//                canvas.drawBitmap(sourceBitmap,null,bitmapRect,paint)
//            }

            //draw inside core image
            drawCoreBitmap(it, coreStartX, coreEndX, coreTopY, coreFullHeight)

            //draw battery head
            paint.color = if (borderWidth == 0F || borderColor == Color.TRANSPARENT) {
                insideBackgroundColor
            } else {
                borderColor
            }
            val finalHeadWidth: Float = if (headWidth > 0F && headWidth <= viewWidth) {
                headWidth
            }
            //default head width is 50% of whole battery width
            else {
                (viewWidth / 2).toFloat()
            }
            Log.d(TAG, "onDraw()...finalHeadWidth:$finalHeadWidth")
            val headLeft = (viewWidth - finalHeadWidth) / 2
            val headRight = headLeft + finalHeadWidth
            rect.set(headLeft, 0F, headRight, headHeight)
            Log.d(TAG, "onDraw()...head rect:$rect")
            canvas.drawRect(rect, paint)
            rect.setEmpty()
        }
    }

    /**
     * keep image scale and zoom up，show it in the center of core
     */
    private fun drawCoreBitmap(
        canvas: Canvas,
        coreStartX: Float,
        coreEndX: Float,
        coreTopY: Float,
        coreFullHeight: Float
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

                val widthScale = coreFulWidth / rawWidth
                val heightScale = coreFullHeight / rawHeight

                //choose short side
                val smallScale = if (widthScale < heightScale) {
                    widthScale
                } else {
                    heightScale
                }

                val finalBitmapWidth = rawWidth * smallScale
                val finalBitmapHeight = rawHeight * smallScale
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