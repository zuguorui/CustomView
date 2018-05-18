package com.zu.customview.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import com.zu.customview.MyLog;
import com.zu.customview.R;

/**
 * Created by rickson on 2018/3/20.
 */

public class SwitchLayout extends FrameLayout {
    private MyLog log = new MyLog("SwitchLayout", true);

    private float switchPercent = 0.2f;

    public enum SWITCH_DIRECTION{LEFT, RIGHT, NONE}
    private SWITCH_DIRECTION switchDirection = SWITCH_DIRECTION.NONE;
    private long lastDrawTime = 0;
    private float switchSpeed = 0.1f;
    private float currentPercent = 0f;

    private SwitchLayoutListener mListener = null;

    private int itemSpace = 20;

    private GestureDetector gestureDetector;
    private GestureDetector.SimpleOnGestureListener gestureListener = new GestureDetector.SimpleOnGestureListener(){
        @Override
        public boolean onSingleTapUp(MotionEvent e) {

            int x = (int)e.getX();
            int position = (int)(getWidth() * currentPercent);
            if(x > position)
            {
                switchDirection = SWITCH_DIRECTION.LEFT;
                postInvalidate();
            }else{
                switchDirection = SWITCH_DIRECTION.RIGHT;
                postInvalidate();
            }
            log.d("single tap occure");
            return false;
        }
    };


    public SwitchLayout(@NonNull Context context) {
        this(context, null);
    }

    public SwitchLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SwitchLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public SwitchLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.SwitchLayout);

        switchPercent = array.getFloat(R.styleable.SwitchLayout_switchPercent, switchPercent);
        itemSpace = (int)array.getDimension(R.styleable.SwitchLayout_itemSpace, itemSpace);
        switchSpeed = array.getFloat(R.styleable.SwitchLayout_switchSpeed, switchSpeed);
        array.recycle();

        if(switchPercent > 0.5f)
        {
            switchPercent = 1f - switchPercent;
        }
        currentPercent = 1f - switchPercent;

        mListener = new SwitchLayoutListenerImpl();

        gestureDetector = new GestureDetector(context, gestureListener);

        setWillNotDraw(false);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);

        int childWidth = (int)(width * (1 - switchPercent)) - itemSpace / 2;
        int childWidthMode = MeasureSpec.EXACTLY;

        int childWidthSpec = MeasureSpec.makeMeasureSpec(childWidth, childWidthMode);

        int maxHeight = 0;

        for(int i = 0; i < getChildCount(); i++)
        {
            View child = getChildAt(i);
            MarginLayoutParams layoutParams = (MarginLayoutParams) child.getLayoutParams();

            int childHeightSpec = getChildMeasureSpec(heightMeasureSpec, 0, layoutParams.height);
            child.measure(childWidthSpec,childHeightSpec);

            if(maxHeight < child.getMeasuredHeight())
            {
                maxHeight = child.getMeasuredHeight();
            }
        }

        if(heightMode == MeasureSpec.EXACTLY)
        {
            setMeasuredDimension(width, height);
        }else if(heightMode == MeasureSpec.AT_MOST){
            if(height > maxHeight)
            {
                setMeasuredDimension(width, maxHeight);
            }else{
                setMeasuredDimension(width, height);
            }
        }else{
            setMeasuredDimension(width, maxHeight);
        }


    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        layoutChild();
    }

    @Override
    protected void onDraw(Canvas canvas) {

        super.onDraw(canvas);
        computeProgress();
        layoutChild();
        invalidate();

    }

    private int downX, downY, upX, upY;
    private int touchSlop = 3;
    private int movedX = 0, movedY = 0;
    private long downTime = 0;
    private long longPressGate = 500;
    private int moveGate = 20;
    private int oldX, oldY, newX, newY;

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
//        gestureDetector.onTouchEvent(ev);
        log.d("onInterceptTouchEvent");
        switch(ev.getActionMasked())
        {
            case MotionEvent.ACTION_DOWN:
                downX = (int)ev.getX();
                downY = (int)ev.getY();
                newX = downX;
                newY = downY;
                downTime = System.currentTimeMillis();
                movedX = 0;
                movedY = 0;
                break;
            case MotionEvent.ACTION_MOVE:
                oldX = newX;
                oldY = newY;
                newX = (int)ev.getX();
                newY = (int)ev.getY();
                int dx = Math.abs(newX - oldX), dy = Math.abs(newY - oldY);
                if(dx > touchSlop)
                {
                    movedX += dx;
                }
                if(dy > touchSlop)
                {
                    movedY += dy;
                }
                break;
            case MotionEvent.ACTION_UP:
                upX = (int)ev.getX();
                upY = (int)ev.getY();
                long upTime = System.currentTimeMillis();
                if(upTime - downTime < longPressGate)
                {
                    log.d("upTime - downTime = " + (upTime - downTime));
                    if(movedX < moveGate && movedY < moveGate)
                    {

                        int position = (int)(getWidth() * currentPercent);
                        if(upX > position)
                        {
                            log.d("switch to left");
                            switchDirection = SWITCH_DIRECTION.LEFT;
                            invalidate();
                        }else{
                            log.d("switch to right");
                            switchDirection = SWITCH_DIRECTION.RIGHT;
                            invalidate();
                        }
                        log.d("single tap occure");
                    }
                }
        }
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return true;
    }

    private void layoutChild()
    {
        if(getChildCount() != 2)
        {
            return;
        }
        View leftView = getChildAt(0);
        View rightView = getChildAt(1);
        int position = (int)(getWidth() * currentPercent);

        int maxWidth = (int)(getWidth() * (1 - switchPercent));
        int left = position - maxWidth;
        int right = left + leftView.getMeasuredWidth();
        int top = 0;
        int bottom = top + getHeight();
        leftView.layout(left, top, right, bottom);

        right = position + maxWidth;
        left = right - rightView.getMeasuredWidth();
        rightView.layout(left, top, right, bottom);

    }

    private void computeProgress()
    {
        if(switchDirection == SWITCH_DIRECTION.LEFT && currentPercent <= switchPercent)
        {
            switchDirection = SWITCH_DIRECTION.NONE;
            currentPercent = switchPercent;
            mListener.endSwitch(SWITCH_DIRECTION.LEFT);
            return;
        }else if(switchDirection == SWITCH_DIRECTION.RIGHT && currentPercent >= (1 - switchPercent))
        {
            switchDirection = SWITCH_DIRECTION.NONE;
            currentPercent = 1 - switchPercent;
            mListener.endSwitch(SWITCH_DIRECTION.RIGHT);
            return;
        }else if(switchDirection == SWITCH_DIRECTION.NONE){
            if(currentPercent == switchPercent || currentPercent == 1 - switchPercent)
            {
                return;
            }

            if(currentPercent < 0.5f)
            {
                switchDirection = SWITCH_DIRECTION.LEFT;
            }else{
                switchDirection = SWITCH_DIRECTION.RIGHT;
            }
        }
        long time = System.currentTimeMillis();
        float movePercent = (time - lastDrawTime) * 1f / 50 * (switchSpeed);
        if(switchDirection == SWITCH_DIRECTION.LEFT)
        {
            if(currentPercent == 1 - switchPercent)
            {
                mListener.beginSwitch(SWITCH_DIRECTION.LEFT);
                movePercent = 0.01f;
            }
            currentPercent -= movePercent;

        }else{
            if(currentPercent == switchPercent)
            {
                mListener.beginSwitch(SWITCH_DIRECTION.RIGHT);
                movePercent = 0.01f;
            }
            currentPercent += movePercent;
        }
        if(currentPercent < switchPercent)
        {
            currentPercent = switchPercent;
        }

        if(currentPercent > 1 - switchPercent)
        {
            currentPercent = 1 - switchPercent;
        }

        if(currentPercent > switchPercent && currentPercent < (1f - switchSpeed))
        {
            float percent = (currentPercent - switchPercent) / (1 - 2 * switchPercent);
            mListener.switching(switchDirection, percent);
        }
        lastDrawTime = time;


    }

    public float getSwitchPercent()
    {
        return switchPercent;
    }

    public float getCurrentPercent()
    {
        return currentPercent;
    }

    public void setSwitchLayoutListener(SwitchLayoutListener listener)
    {
        mListener = listener;
    }

    public void removeSwitchLayoutListener()
    {
        mListener = new SwitchLayoutListenerImpl();

    }

    public interface SwitchLayoutListener{
        void beginSwitch(SWITCH_DIRECTION direction);
        void switching(SWITCH_DIRECTION direction, float process);
        void endSwitch(SWITCH_DIRECTION direction);
    }

    private class SwitchLayoutListenerImpl implements SwitchLayoutListener
    {
        public void beginSwitch(SWITCH_DIRECTION direction)
        {
            log.d("beginSwitch, direction = " + direction);
        }
        public void switching(SWITCH_DIRECTION direction, float process){
            log.d("switching, direction = " + direction + ", process = " + process);
        }
        public void endSwitch(SWITCH_DIRECTION direction){
            log.d("endSwitch, direction = " + direction);
        }
    }


}
