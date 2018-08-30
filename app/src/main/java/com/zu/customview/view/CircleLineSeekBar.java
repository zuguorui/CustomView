package com.zu.customview.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.zu.customview.R;


/**
 * Created by rikson on 2018/8/7.
 */

public class CircleLineSeekBar extends View {


    private static final String TAG = "CircleLineSeekBar";
    private int radius = -1;

    private int lineWidth = 5;

    private int angle = 180;
    private int orientationAngle = 90;

    private Drawable thumbDrawable = null;

    private int baseColor = 0xff888888;
    private int progressColor = 0xffff00ff;
    private float progress = 0.0f;

    private int thumbSize = 10;

    private int touchSlop = 50;

    private boolean hasPassedRadius = false;



    public CircleLineSeekBar(Context context) {
        this(context, null);
    }

    public CircleLineSeekBar(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircleLineSeekBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public CircleLineSeekBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.CircleLineSeekBar);

        radius = array.getDimensionPixelSize(R.styleable.CircleLineSeekBar_radius, radius);
        lineWidth = array.getDimensionPixelSize(R.styleable.CircleLineSeekBar_lineWidth, lineWidth);
        angle = array.getInt(R.styleable.CircleLineSeekBar_angle, angle);
        orientationAngle = array.getInt(R.styleable.CircleLineSeekBar_orientationAngle, orientationAngle);
        thumbDrawable = array.getDrawable(R.styleable.CircleLineSeekBar_thumbDrawable);
        baseColor = array.getColor(R.styleable.CircleLineSeekBar_baseColor, baseColor);
        progressColor = array.getColor(R.styleable.CircleLineSeekBar_progressColor, progressColor);
        progress = array.getFloat(R.styleable.CircleLineSeekBar_progress, progress);
        thumbSize = array.getDimensionPixelSize(R.styleable.CircleLineSeekBar_thumbSize, thumbSize);
        touchSlop = array.getDimensionPixelSize(R.styleable.CircleLineSeekBar_touchSlop, touchSlop);

        if(radius >= 0)
        {
            hasPassedRadius = true;
        }

        orientationAngle %= 360;
        if(orientationAngle < 0)
        {
            orientationAngle += 360;
        }
        angle %= 360;
        if(angle < 0)
        {
            angle += 360;
        }
        if(progress > 1.0f)
        {
            progress = 1.0f;
        }

        if(thumbDrawable == null)
        {
            StateListDrawable listDrawable = new StateListDrawable();

            ColorDrawable d1 = new ColorDrawable(progressColor);


            ColorDrawable d2 = new ColorDrawable(baseColor);
            listDrawable.addState(new int[]{android.R.attr.state_pressed}, d1);
            listDrawable.addState(new int[]{0}, d2);
            thumbDrawable = listDrawable;

        }

        setClickable(true);
        setLongClickable(true);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);

        int resultWidth = getPaddingLeft() + getPaddingRight();
        int resultHeight = getPaddingTop() + getPaddingBottom();

        int realRadius = 0;
        if(radius != -1)
        {
            realRadius = radius;
            resultWidth += realRadius * 2 + Math.max(lineWidth, thumbSize);
            resultHeight += realRadius * 2 + Math.max(lineWidth, thumbSize);
            if(widthMode == MeasureSpec.EXACTLY)
            {
                resultWidth = widthSize;
            }else if(widthMode == MeasureSpec.AT_MOST)
            {
                resultWidth = Math.min(resultWidth, widthSize);
            }
            if(heightMode == MeasureSpec.EXACTLY)
            {
                resultHeight = heightSize;
            }else if(heightMode == MeasureSpec.AT_MOST)
            {
                resultHeight = Math.min(heightSize, resultHeight);
            }

            setMeasuredDimension(resultWidth, resultHeight);
            return;
        }else{
            if(heightMode == MeasureSpec.UNSPECIFIED && widthMode == MeasureSpec.UNSPECIFIED)
            {
                setMeasuredDimension(widthSize, heightSize);
                return;
            }else if(heightMode == MeasureSpec.UNSPECIFIED)
            {
                resultWidth = widthSize;
                radius = (resultWidth - getPaddingLeft() - getPaddingRight() - Math.max(lineWidth, thumbSize)) / 2;
                resultHeight = getPaddingTop() + getPaddingBottom() + (resultWidth - getPaddingLeft() - getPaddingRight());
                setMeasuredDimension(resultWidth, resultHeight);
            }else if(widthMode == MeasureSpec.UNSPECIFIED)
            {
                resultHeight = heightSize;
                radius = (resultHeight - getPaddingTop() - getPaddingBottom() - Math.max(lineWidth, thumbSize)) / 2;
                resultWidth = getPaddingLeft() + getPaddingRight() + (resultHeight - getPaddingTop() - getPaddingBottom());
                setMeasuredDimension(resultWidth, resultHeight);
            }else{
                //mode == AT_MOST or mode == EXACTLY
                int minSize = Math.min(heightSize - getPaddingTop() - getPaddingBottom(), widthSize - getPaddingLeft() - getPaddingRight());
                radius = (minSize - Math.max(lineWidth, thumbSize)) / 2;
                //at this step, the width and height is min
                resultWidth = minSize + getPaddingRight() + getPaddingLeft();
                resultHeight = minSize + getPaddingTop() + getPaddingBottom();
                if(heightMode == MeasureSpec.EXACTLY)
                {
                    resultHeight = heightSize;
                }

                if(widthMode == MeasureSpec.EXACTLY)
                {
                    resultWidth = widthSize;
                }
                setMeasuredDimension(resultWidth, resultHeight);
            }
        }

    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if(!hasPassedRadius)
        {
            int avalHeight = getHeight() - getPaddingTop() - getPaddingBottom() - Math.max(lineWidth, thumbSize);
            int avalWidth = getWidth() - getPaddingLeft() - getPaddingRight() - Math.max(lineWidth, thumbSize);
            radius = Math.min(avalHeight, avalWidth) / 2;
        }


    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawContent(canvas);
    }

    private void drawContent(Canvas canvas)
    {
        //in this draw system, clockwise is positive. But in standard math, clockwise is negative. So we convert it.
        int tempOriAngle = -orientationAngle;
        tempOriAngle %= 360;
        if(tempOriAngle < 0)
        {
            tempOriAngle += 0;
        }

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStrokeWidth(lineWidth);
        paint.setStrokeCap(Paint.Cap.ROUND);

        int centerX = (getMeasuredWidth() - getPaddingLeft() - getPaddingRight()) / 2 + getPaddingLeft();
        int centerY = (getMeasuredHeight() - getPaddingTop() - getPaddingBottom()) / 2 + getPaddingTop();

        RectF area = new RectF(centerX - radius, centerY - radius, centerX + radius, centerY + radius);

        float startAngle = (tempOriAngle - angle / 2);
        float progressAngel = startAngle + progress * angle;
        float endAngel = startAngle + angle;

        paint.setColor(progressColor);
        paint.setStyle(Paint.Style.STROKE);
        Path path = new Path();
        path.addArc(area, startAngle, -startAngle + progressAngel);
        canvas.drawPath(path, paint);
        //canvas.drawArc(area, startAngle, -startAngle + progressAngel, true, paint);


        paint.setColor(baseColor);
        path.reset();
        path.addArc(area, progressAngel, -progressAngel + endAngel);
        canvas.drawPath(path, paint);
        //canvas.drawArc(area, progressAngel, -progressAngel + endAngel, true, paint);

        int thumbX = (int)(radius * Math.cos(progressAngel * Math.PI / 180));
        int thumbY = (int)(radius * Math.sin(progressAngel * Math.PI / 180));


        canvas.save();
        canvas.rotate((int)progressAngel, centerX, centerY);
        path.reset();
        //canvas.drawLine(centerX, centerY, centerX + radius, centerY, paint);

        thumbDrawable.setBounds(centerX + radius - thumbSize / 2, centerY - thumbSize / 2, centerX + radius + thumbSize / 2, centerY + thumbSize / 2);
        thumbDrawable.draw(canvas);
        canvas.restore();


    }

    boolean catchDown = false;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean result = false;
        switch (event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
            {

                float tempProcess = computeProgressByMotionEvent(event);

                if(tempProcess < 0f)
                {

                    thumbDrawable.setState(new int[]{1});
                    invalidate();

                    return false;
                }else {
                    progress = tempProcess;
                    catchDown = true;
                    thumbDrawable.setState(new int[]{android.R.attr.state_pressed});
                    invalidate();
                    result = true;
                }
            }
            break;
            case MotionEvent.ACTION_MOVE:
            {
                if(!catchDown)
                {
                    return true;
                }
                float tempProcess = computeProgressByMotionEvent(event);

                if(tempProcess < 0f)
                {
                    thumbDrawable.setState(new int[]{1});
                    invalidate();
                    result = true;
                }else {
                    progress = tempProcess;
                    invalidate();
                    result = true;
                }

            }
            break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                thumbDrawable.setState(new int[]{1});
                invalidate();
                catchDown = false;
                result = true;
                break;
            default:
                return catchDown;
        }
        return result;

    }

    private float computeProgressByMotionEvent(MotionEvent event)
    {
        float result = 0f;

        int centerX = (getMeasuredWidth() - getPaddingLeft() - getPaddingRight()) / 2 + getPaddingLeft();
        int centerY = (getMeasuredHeight() - getPaddingTop() - getPaddingBottom()) / 2 + getPaddingTop();
        Point downPoint = new Point((int)event.getX() - centerX, (int)event.getY() - centerY);
        int absDown = (int)Math.sqrt(Math.pow(downPoint.x, 2) + Math.pow(downPoint.y, 2));
        int downAngle = (int)(Math.acos(downPoint.x * 1.0f / absDown) * 180 / Math.PI);
        if(downPoint.y > 0)
        {
            downAngle = 360 - downAngle;
        }
        int endAngle = orientationAngle - angle / 2;
        downAngle = downAngle - endAngle;
        downAngle %= 360;
        if(downAngle < 0)
        {
            downAngle += 360;
        }


        if(Math.abs(absDown - radius) <= touchSlop && downAngle >= 0 && downAngle <= angle)
        {
            result = Math.abs(downAngle - angle) * 1.0f / angle;

        }else{
            result = -1f;
        }
        return result;
    }



    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
        requestLayout();
        invalidate();
    }

    public int getLineWidth() {
        return lineWidth;
    }

    public void setLineWidth(int lineWidth) {
        this.lineWidth = lineWidth;
        requestLayout();
        invalidate();
    }

    public int getAngle() {
        return angle;
    }

    public void setAngle(int angle) {
        angle = angle % 360;
        if(angle < 0)
        {
            angle += 360;
        }
        this.angle = angle;
        requestLayout();
        invalidate();
    }

    public int getOrientationAngle() {
        return orientationAngle;
    }

    public void setOrientationAngle(int orientationAngle) {
        orientationAngle %= 360;
        if(orientationAngle < 0)
        {
            orientationAngle += 360;
        }
        this.orientationAngle = orientationAngle;
        requestLayout();
        invalidate();
    }

    public Drawable getThumbDrawable() {
        return thumbDrawable;
    }

    public void setThumbDrawable(@NonNull Drawable thumbDrawable) {
        this.thumbDrawable = thumbDrawable;
        requestLayout();
        invalidate();
    }

    public int getBaseColor() {
        return baseColor;
    }

    public void setBaseColor(int baseColor) {
        this.baseColor = baseColor;
        invalidate();
    }

    public int getProgressColor() {
        return progressColor;
    }

    public void setProgressColor(int progressColor) {
        this.progressColor = progressColor;
        invalidate();
    }

    public float getProgress() {
        return progress;

    }

    public void setProgress(float progress) {
        if(progress > 1.0f)
        {
            progress = 1.0f;
        }
        if(progress < 0f)
        {
            progress = 0f;
        }
        this.progress = progress;
        invalidate();
    }

    public int getThumbSize() {
        return thumbSize;
    }

    public void setThumbSize(int thumbSize) {
        this.thumbSize = thumbSize;
        requestLayout();
        invalidate();
    }

    public int getTouchSlop() {
        return touchSlop;
    }

    public void setTouchSlop(int touchSlop) {
        this.touchSlop = touchSlop;
    }
}
