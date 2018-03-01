package com.zu.customview.view;

import android.animation.FloatEvaluator;
import android.animation.IntEvaluator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.widget.AppCompatButton;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ThemedSpinnerAdapter;

import com.zu.customview.R;

import java.util.LinkedList;

/**
 * Created by rickson on 2018/3/1.
 */

public class RoundWaveButton extends ViewGroup {
    private int waveColor = 0xff4596;
    private boolean autoAnimWave = true;
    private boolean animWaveWhenClick = true;

    private int timeInterval = 500;//How many milliseconds show another wave after last one
    private int speed = 20;//How much pixes wave enlarge radius every per 10 milliseconds;

    private int tailLength = 10;

    private int totalTime = 0;

    private Interpolator interpolator = new DecelerateInterpolator();
    private FloatEvaluator evaluator = new FloatEvaluator();

    private Bitmap wave = null;

    private BitmapDrawable drawable = null;

    private int centerX = 0, centerY = 0;

    private int outRadius = 0, inRadius = 0;

    private int lastOutRadius = 0;

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
        tailLength = array.getInt(R.styleable.RoundWaveButton_tailLength, tailLength);
        if(array.getResourceId(R.styleable.RoundWaveButton_interpolator, Integer.MIN_VALUE) != Integer.MIN_VALUE)
        {
            interpolator = AnimationUtils.loadInterpolator(context, array.getResourceId(R.styleable.RoundWaveButton_interpolator, Integer.MIN_VALUE));
        }
        array.recycle();

        setClickable(true);



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
        MarginLayoutParams layoutParams = (MarginLayoutParams) child.getLayoutParams();

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








    }



    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

        if(changed)
        {
            if(getChildCount() != 1)
            {
                throw new IllegalStateException("This view should only have one child");
            }
            View child = getChildAt(0);
            MarginLayoutParams layoutParams = (MarginLayoutParams)child.getLayoutParams();
            int left = getPaddingLeft() + layoutParams.leftMargin;
            int top = getPaddingTop() + layoutParams.topMargin;
            int right = left + child.getMeasuredWidth();
            int bottom = top + child.getMeasuredHeight();
            child.layout(left, top, right, bottom);

            centerX = (left + right) / 2;
            centerY = (top + bottom) / 2;
            inRadius = child.getMeasuredWidth() / 2;

            int topSpace = centerY;
            int bottomSpace = getMeasuredHeight() - centerY;
            int leftSpace = centerX;
            int rightSpace = getMeasuredWidth() - centerX;

            outRadius = Math.max(Math.max(topSpace, bottomSpace), Math.max(leftSpace, rightSpace));

            totalTime = (outRadius - inRadius) / speed * 10;
            if(outRadius > 2 * lastOutRadius)
            {
                if(wave != null && !wave.isRecycled())
                {
                    wave.recycle();
                }
                lastOutRadius = outRadius;
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
        float[] positions = new float[]{0f, ratio, 1f};
        RadialGradient radialGradient = new RadialGradient(outRadius, outRadius, outRadius, colors, positions, Shader.TileMode.CLAMP);
        Paint paint = new Paint();
        paint.setShader(radialGradient);
        canvas.drawCircle(outRadius, outRadius, outRadius, paint);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        View child = getChildAt(0);
        switch (ev.getActionMasked())
        {
            case MotionEvent.ACTION_DOWN:
            {

            }
        }

        return false;
    }

    private void computeWaveInfos()
    {

    }

    private void addWaveInfo(boolean force)
    {
        long time = System.currentTimeMillis();
        if(force || waveInfos.size() == 0)
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
            }
        }
    }

    private void removeWaveInfo()
    {
        if(waveInfos.size() == 0)
        {
            return;
        }
        long time = System.currentTimeMillis();
        while(true)
        {
            WaveInfo first = waveInfos.getFirst();
            if(first.radius - tailLength > outRadius)
            {
                waveInfos.remove(0);
            }
        }
    }

    private class WaveInfo{
        public long createTime = 0;
        public int radius = 0;
    }

    private class DrawBackgroundThread extends Thread{
        private Object waitObject = null;
        private boolean running = false;

        public DrawBackgroundThread(Object waitObject)
        {
            this.waitObject = waitObject;


        }

        private boolean isRunning()
        {
            return running;
        }

        @Override
        public void run() {
            while(true)
            {
                synchronized (waitObject)
                {

                }
            }
        }
    }
}
