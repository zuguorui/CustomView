package com.zu.customview.view

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
/**
 * Created by rikson on 2018/8/7.
 */
public class CircleLIneSeekBarKT : View
{

    var radius = -1
        set(value) {
            field = value
            invalidate()
        }

    private var lineWidth = 5
        set(value) {
            field = value
            invalidate()
        }

    private var angel = 180
        set(value) {
            field = value
            invalidate()
        }
    private var oritationAngel = 90
        set(value) {
            field = value
            invalidate()
        }

    private var thumbDrawable: Drawable? = null
        set(value) {
            field = value
            if(value != null)
            {
                invalidate()
            }
        }

    private var baseColor = 0xff888888
        set(value) {
            field = value
            invalidate()
        }
    private var progressColor = 0xffff00ff
        set(value) {
            field = value
            invalidate()
        }

    private var max = 100
        set(value) {
            field = value
            invalidate()
        }
    private var progress = 0.0f
        set(value) {
            field = value
            invalidate()
        }

    private var thumbSize = 10
        set(value) {
            field = value
            invalidate()
        }

    constructor(context: Context?) : this(context, null)
    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : this(context, attrs, defStyleAttr, 0)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)
    {

    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var heightSize:Int = MeasureSpec.getSize(heightMeasureSpec)
        var heightMode:Int = MeasureSpec.getMode(heightMeasureSpec)
        var widthSize: Int = MeasureSpec.getSize(widthMeasureSpec)
        var widthMode:Int = MeasureSpec.getMode(widthMeasureSpec)

        var x1:Int
        var y1:Int
        var x2:Int
        var x3:Int



    }

    override fun onDraw(canvas: Canvas?) {

    }
}