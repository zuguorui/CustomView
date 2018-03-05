package com.zu.customview.view;

import android.animation.FloatEvaluator;
import android.animation.IntEvaluator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.widget.AppCompatButton;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;


import com.zu.customview.MyLog;
import com.zu.customview.R;

import java.util.LinkedList;

/**
 * Created by rickson on 2018/3/1.
 */

public class RoundWaveButton extends FrameLayout {

    MyLog log = new MyLog("RoundWaveButton", true);
    private int waveColor = 0xff4596;
    private boolean autoAnimWave = true;
    private boolean animWaveWhenClick = true;

    private int timeInterval = 1000;//How many milliseconds show another wave after last one
    private int speed = 20;//How much pixes wave enlarge radius every per 100 milliseconds;

    private int tailLength = 250;

    private int totalTime = 0;

    private Interpolator interpolator = new DecelerateInterpolator();
    private FloatEvaluator evaluator = new FloatEvaluator();

    private Bitmap wave = null;

//    private Bitmap[] backBitmapBuffer = new Bitmap[2];
//    private Canvas[] backCanvasBuffer = new Canvas[2];
//    private BitmapDrawable[] backDrawableBuffer = new BitmapDrawable[2];

    private int centerX = 0, centerY = 0;

    private int outRadius = 0, inRadius = 0;

    private int oldOutRadius = 0;
    private int oldWidth = 0, oldHeight = 0;

    private boolean animAlpha = true;

//    private DrawBackgroundThread waveThread = null;

//    private Object waitObject = new Object();



    private LinkedList<WaveInfo> waveInfos = new LinkedList<>();

    public RoundWaveButton(Context context) {
        this(context, null);
    }

    public RoundWaveButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RoundWaveButton(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public RoundWaveButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.RoundWaveButton);
        waveColor = array.getColor(R.styleable.RoundWaveButton_waveColor, waveColor);
        autoAnimWave = array.getBoolean(R.styleable.RoundWaveButton_autoAnimWave, autoAnimWave);
        animWaveWhenClick = array.getBoolean(R.styleable.RoundWaveButton_animWaveWhenClick, animWaveWhenClick);
        tailLength = (int)array.getDimension(R.styleable.RoundWaveButton_tailLength, tailLength);
        timeInterval = array.getInt(R.styleable.RoundWaveButton_timeInterval, timeInterval);
        animAlpha = array.getBoolean(R.styleable.RoundWaveButton_animaAlpha, animAlpha);
        speed = array.getInt(R.styleable.RoundWaveButton_speed, speed);
        if(array.getResourceId(R.styleable.RoundWaveButton_interpolator, Integer.MIN_VALUE) != Integer.MIN_VALUE)
        {
            interpolator = AnimationUtils.loadInterpolator(context, array.getResourceId(R.styleable.RoundWaveButton_interpolator, Integer.MIN_VALUE));
        }
        array.recycle();

        setClickable(true);

        setWillNotDraw(false);


    }

    public int getWaveColor() {
        return waveColor;
    }

    public void setWaveColor(int waveColor) {
        this.waveColor = waveColor;
    }

    public boolean isAutoAnimWave() {
        return autoAnimWave;
    }

    public void setAutoAnimWave(boolean autoAnimWave) {
        this.autoAnimWave = autoAnimWave;
        invalidate();
    }

    public boolean isAnimWaveWhenClick() {
        return animWaveWhenClick;
    }

    public void setAnimWaveWhenClick(boolean animWaveWhenClick) {
        this.animWaveWhenClick = animWaveWhenClick;

    }

    public int getTimeInterval() {
        return timeInterval;
    }

    public void setTimeInterval(int timeInterval) {
        this.timeInterval = timeInterval;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public int getTailLength() {
        return tailLength;
    }

    public void setTailLength(int tailLength) {
        this.tailLength = tailLength;
    }

    public Interpolator getInterpolator() {
        return interpolator;
    }

    public void setInterpolator(Interpolator interpolator) {
        this.interpolator = interpolator;
    }

    public FloatEvaluator getEvaluator() {
        return evaluator;
    }

    public void setEvaluator(FloatEvaluator evaluator) {
        this.evaluator = evaluator;
    }

    public boolean isAnimAlpha() {
        return animAlpha;
    }

    public void setAnimAlpha(boolean animAlpha) {
        this.animAlpha = animAlpha;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if(getChildCount() != 1)
        {
            throw new IllegalStateException("This view should only have one child");
        }
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        int resultWidth = 0, resultHeight = 0;


        View child = getChildAt(0);
        MarginLayoutParams layoutParams = (MarginLayoutParams)child.getLayoutParams();


        int horPadding = getPaddingRight() + getPaddingLeft() + layoutParams.rightMargin + layoutParams.leftMargin;
        int verPadding = getPaddingTop() + getPaddingBottom() + layoutParams.topMargin + layoutParams.bottomMargin;
        int childWidthSpec = getChildMeasureSpec(widthMeasureSpec,horPadding, layoutParams.width);
        int childHeightSpec = getChildMeasureSpec(heightMeasureSpec, verPadding, layoutParams.height);
        child.measure(childWidthSpec, childHeightSpec);

        if(child.getMeasuredWidth() > child.getMeasuredHeight())
        {
            childHeightSpec = MeasureSpec.makeMeasureSpec(child.getMeasuredWidth(), MeasureSpec.EXACTLY);
        }else{
            childWidthSpec = MeasureSpec.makeMeasureSpec(child.getMeasuredHeight(), MeasureSpec.EXACTLY);
        }
        child.measure(childWidthSpec, childHeightSpec);

        if(widthMode == MeasureSpec.EXACTLY)
        {
            resultWidth = width;
        }else if(widthMode == MeasureSpec.AT_MOST)
        {
            if(width - horPadding > child.getMeasuredWidth())
            {
                resultWidth = child.getMeasuredWidth() + horPadding;
            }else{
                resultWidth = width;
            }
        }else{
            resultWidth = child.getMeasuredWidth() + horPadding;
        }

        if(heightMode == MeasureSpec.EXACTLY)
        {
            resultHeight = height;
        }else if(heightMode == MeasureSpec.AT_MOST)
        {
            if(height - verPadding > child.getMeasuredHeight())
            {
                resultHeight = child.getMeasuredHeight() + verPadding;
            }else{
                resultHeight = height;
            }
        }else{
            resultHeight = child.getMeasuredHeight() + verPadding;
        }

        setMeasuredDimension(resultWidth, resultHeight);

        if(resultHeight != oldHeight || resultWidth != oldWidth)
        {
            oldHeight = resultHeight;
            oldWidth = resultWidth;
//            for(int i = 0; i < backBitmapBuffer.length; i++)
//            {
//                if(backBitmapBuffer[i] != null)
//                {
//                    backBitmapBuffer[i].recycle();
//                    backBitmapBuffer[i] = null;
//                    backCanvasBuffer[i] = null;
//                    backDrawableBuffer[i] = null;
//
//
//                }
//                backBitmapBuffer[i] = Bitmap.createBitmap(resultWidth, resultHeight, Bitmap.Config.ARGB_8888);
//                backCanvasBuffer[i] = new Canvas(backBitmapBuffer[i]);
//                backDrawableBuffer[i] = new BitmapDrawable(getContext().getResources(), backBitmapBuffer[i]);
//
//                setBackground(backDrawableBuffer[0]);
//            }

        }






    }



    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

        if(changed)
        {
            if(getChildCount() != 1)
            {
                throw new IllegalStateException("This view should only have one child");
            }
            super.onLayout(changed, l, t, r, b);
            View child = getChildAt(0);
            int left = child.getLeft();
            int top = child.getTop();
            int right = child.getRight();
            int bottom = child.getBottom();

            centerX = (left + right) / 2;
            centerY = (top + bottom) / 2;
            inRadius = child.getMeasuredWidth() / 2;

            int topSpace = centerY;
            int bottomSpace = getMeasuredHeight() - centerY;
            int leftSpace = centerX;
            int rightSpace = getMeasuredWidth() - centerX;

            outRadius = Math.max(Math.max(topSpace, bottomSpace), Math.max(leftSpace, rightSpace));

            totalTime = (outRadius - inRadius) / speed * 100;
            if(outRadius > 2 * oldOutRadius)
            {
                if(wave != null && !wave.isRecycled())
                {
                    wave.recycle();
                }
                oldOutRadius = outRadius;
                wave = Bitmap.createBitmap(outRadius * 2, outRadius * 2, Bitmap.Config.ARGB_8888);
                drawWaveSrc(new Canvas(wave));
            }
        }
    }

    private void drawWaveSrc(Canvas canvas)
    {
        int color1 = waveColor & 0x00ffffff;
        int[] colors = new int[]{color1, color1, waveColor};
        float ratio = (outRadius - tailLength) * 1.0f / outRadius;
        if(ratio < 0)
        {
            ratio = 0f;
        }
        float[] positions = new float[]{0f, ratio, 1f};
        RadialGradient radialGradient = new RadialGradient(outRadius, outRadius, outRadius, colors, positions, Shader.TileMode.CLAMP);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setShader(radialGradient);
        canvas.drawCircle(outRadius, outRadius, outRadius, paint);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        View child = getChildAt(0);
        switch (ev.getActionMasked())
        {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_UP:
            {
                if(animWaveWhenClick)
                {
                    updateWaveInfos(true);
                    invalidate();
                }
            }
        }

        return false;
    }

    private void updateWaveInfos(boolean forceAdd)
    {
        long time = System.currentTimeMillis();
        addWaveInfo(forceAdd, time);
        removeWaveInfo(time);
        computeWaveInfos(time);
    }

    private void drawBackground(Canvas canvas)
    {
        if(waveInfos.size() == 0)
        {
            return;
        }
        Paint mBitPaint = new Paint();
        mBitPaint.setAntiAlias(true);

//        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        canvas.save();
        Path path = new Path();
        path.addCircle(centerX, centerY, inRadius, Path.Direction.CW);
        canvas.clipPath(path);
        path.reset();
        path.addCircle(centerX, centerY, outRadius, Path.Direction.CW);
//
        canvas.clipPath(path, Region.Op.REVERSE_DIFFERENCE);

        Rect src = new Rect(0,0,wave.getWidth(), wave.getHeight());

        for(WaveInfo waveInfo : waveInfos)
        {
            mBitPaint.setAlpha(waveInfo.alpha);
            RectF dest = new RectF(centerX - waveInfo.radius, centerY - waveInfo.radius, centerX + waveInfo.radius, centerY + waveInfo.radius);
            canvas.drawBitmap(wave, src, dest, mBitPaint);

        }
        canvas.restore();
    }

    private void computeWaveInfos(long time)
    {
        log.d("waveInfos.size = " + waveInfos.size());

        for(WaveInfo waveInfo : waveInfos)
        {
            float process = (time - waveInfo.createTime) * 1.0f / totalTime;
            float fraction = interpolator.getInterpolation(process);

            int radius = evaluator.evaluate(fraction, inRadius, outRadius).intValue();
            waveInfo.radius = radius;
            if(animAlpha)
            {
                waveInfo.alpha = evaluator.evaluate(fraction, 255, 0).intValue();
            }

        }
    }

    private void addWaveInfo(boolean force, long time)
    {

        if(force)
        {
            WaveInfo waveInfo = new WaveInfo();
            waveInfo.createTime = time;
            waveInfos.add(waveInfo);
            return;
        }else{
            if(autoAnimWave)
            {
                if(waveInfos.size() == 0)
                {
                    WaveInfo waveInfo = new WaveInfo();
                    waveInfo.createTime = time;
                    waveInfos.add(waveInfo);
                    return;
                }else{
                    WaveInfo last = waveInfos.getLast();
                    if(time - last.createTime < timeInterval)
                    {
                        return;
                    }else{
                        WaveInfo waveInfo = new WaveInfo();
                        waveInfo.createTime = time;
                        waveInfos.add(waveInfo);
                        log.d("add waveinfo");
                    }
                }
            }

        }
    }


    @Override
    protected void onDraw(Canvas canvas) {


        super.onDraw(canvas);
        if(animWaveWhenClick || autoAnimWave)
        {
            updateWaveInfos(false);
            drawBackground(canvas);
            postInvalidateDelayed(5);
        }


    }

    private void removeWaveInfo(long time)
    {

        while(true)
        {
            if(waveInfos.size() == 0)
            {
                return;
            }
            WaveInfo first = waveInfos.getFirst();
            if(time - first.createTime > totalTime)
            {
                waveInfos.remove(0);
                log.d("remove waveinfo");
            }else{
                return;
            }
        }
    }

    private class WaveInfo{
        public long createTime = 0;
        public int radius = 0;
        public int alpha = 255;
    }


}
