package com.zu.customview.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Scroller;

import java.util.ArrayList;

/**
 * Created by zu on 17-3-7.
 */

public class SlideLayout extends FrameLayout {

    private View slideView;
    private View contentView;

    private float newX, newY, lastX, lastY, dx, dy;
    private float slideAreaWidth = 70;

    private int slideViewWidth, rootWidth;

    private float shadowAlpha = 0.6f;

    private ImageView shadowView = null;
    private VelocityTracker velocityTracker = null;
    private float velocityGate = 500;
    private float moveGate = 5;

    private boolean moved = false;

    private int scrollDuration = 400;

    private Scroller mScroller;

    private ArrayList<OnSlideListener> listeners = new ArrayList<>();

    public SlideLayout(Context context) {
        super(context);
    }

    public SlideLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SlideLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);


    }

    public SlideLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        if(mScroller == null)
        {
            mScroller = new Scroller(context);
        }

    }



    private void initViews()
    {

        contentView = this.getChildAt(0);
        slideView = this.getChildAt(1);
        MarginLayoutParams layoutParams = (MarginLayoutParams) slideView.getLayoutParams();
        layoutParams.leftMargin = - layoutParams.width;
        slideViewWidth = layoutParams.width;
        slideView.setLayoutParams(layoutParams);

        contentView.setClickable(true);
        contentView.setLongClickable(true);

        slideView.setClickable(true);
        slideView.setLongClickable(true);

        rootWidth = getViewWidth(this);

        shadowView = new ImageView(getContext());
        shadowView.setBackgroundColor(Color.parseColor("#000000"));
        shadowView.setAlpha(0f);
        this.addView(shadowView,1);

        Log.v("Slide layout", "init view");
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if(changed)
        {
            initViews();
        }

    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if(mScroller != null && mScroller.computeScrollOffset())
        {
            int leftMargin = mScroller.getCurrX();
            setViewLeftMargin(slideView, leftMargin);
            float alpha = shadowAlpha * (1.0f - ((float) Math.abs(leftMargin))/slideViewWidth);
            shadowView.setAlpha(alpha);
            postInvalidate();
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                newX = ev.getRawX();
                newY = ev.getRawY();
                if(getViewLeftMargin(slideView) == 0 && newX > slideViewWidth)
                {
                    return true;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                lastX = newX;
                lastY = newY;
                newX = ev.getRawX();
                newY = ev.getRawY();
                dx = newX - lastX;
                dy = newY - lastY;
                if(getViewLeftMargin(slideView) == -slideViewWidth)
                {
                    if(lastX < slideAreaWidth && Math.abs(dx) > Math.abs(dy))
                    {
                        return true;
                    }
                }else
                {
                    if(Math.abs(dx) > Math.abs(dy))
                    {
                        return true;
                    }
                }
                break;
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        createVelocityTracker(event);
        switch (event.getAction())
        {
            case MotionEvent.ACTION_MOVE:
                Log.v("Slide layout", "ACTION_MOVE");

                lastX = newX;
                lastY = newY;
                newX = event.getRawX();
                newY = event.getRawY();
                dx = newX - lastX;
                dy = newY - lastY;
                if(Math.abs(dx) >= moveGate)
                {
                    moved = true;
                    scroll(dx);
                }

                break;
            case MotionEvent.ACTION_UP:
                Log.v("Slide layout", "ACTION_UP");
                lastX = newX;
                lastY = newY;
                newX = event.getRawX();
                newY = event.getRawY();
                dx = newX - lastX;
                dy = newY - lastY;
                float v = getVelocityTracker();
                Log.v("Slide layout", "v:" + v);
                Log.v("Slide layout", "dx:" + dx);
                if(Math.abs(v) > velocityGate)
                {
                    if(v < 0)
                    {
                        scrollToLeft();
                    }else
                    {
                        scrollToRight();
                    }
                }else{
                    MarginLayoutParams layoutParams2 = (MarginLayoutParams) slideView.getLayoutParams();
                    if (layoutParams2.leftMargin == 0 && newX > slideViewWidth && !moved)
                    {
                        scrollToLeft();
                    }else
                    {
                        if(Math.abs(layoutParams2.leftMargin) < Math.abs(slideViewWidth)/2)
                        {
                            scrollToRight();
                        }else
                        {
                            scrollToLeft();
                        }
                    }
                }
                recycleVelocityTracker();
                moved = false;
                break;

        }
        return true;
    }

    private int getViewWidth(View view)
    {
        MarginLayoutParams layoutParams = (MarginLayoutParams)view.getLayoutParams();
        return layoutParams.width;
    }

    private int getViewLeftMargin(View view)
    {
        MarginLayoutParams layoutParams = (MarginLayoutParams)view.getLayoutParams();
        return layoutParams.leftMargin;
    }

    private void setViewLeftMargin(View view, int leftMargin)
    {
        MarginLayoutParams layoutParams = (MarginLayoutParams)view.getLayoutParams();
        layoutParams.leftMargin = leftMargin;
        view.setLayoutParams(layoutParams);
    }

    public void scrollToLeft()
    {
//        ValueAnimator animator = ValueAnimator.ofInt(getViewLeftMargin(slideView),-slideViewWidth);
//        animator.setDuration(200);
//        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//            @Override
//            public void onAnimationUpdate(ValueAnimator animation) {
//                int leftMargin = (int)animation.getAnimatedValue();
//                setViewLeftMargin(slideView, leftMargin);
//                float alpha = shadowAlpha * (1.0f - ((float) Math.abs(leftMargin))/slideViewWidth);
//                shadowView.setAlpha(alpha);
//            }
//        });
//        animator.start();

        if(mScroller == null)
        {
            mScroller = new Scroller(getContext());
        }
        int startX = getViewLeftMargin(slideView);
        int targetX = -slideViewWidth;
        mScroller.startScroll(startX, 0, targetX - startX, 0, scrollDuration);
        postInvalidate();
    }

    private void scroll(float dx)
    {
        MarginLayoutParams layoutParams1 = (MarginLayoutParams) slideView.getLayoutParams();
        layoutParams1.leftMargin += dx;
        if(layoutParams1.leftMargin < -slideViewWidth)
        {
            layoutParams1.leftMargin = -slideViewWidth;
        }
        if(layoutParams1.leftMargin > 0)
        {
            layoutParams1.leftMargin = 0;
        }
        float alpha = shadowAlpha * (1.0f - ((float) Math.abs(layoutParams1.leftMargin))/slideViewWidth);
        shadowView.setAlpha(alpha);
        slideView.setLayoutParams(layoutParams1);
        notifyOnSlideListeners(((float) Math.abs(layoutParams1.leftMargin))/slideViewWidth);
    }

    public void scrollToRight()
    {
//        ValueAnimator animator = ValueAnimator.ofInt(getViewLeftMargin(slideView),  0);
//        animator.setDuration(200);
//        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//            @Override
//            public void onAnimationUpdate(ValueAnimator animation) {
//                int leftMargin = (int)animation.getAnimatedValue();
//                setViewLeftMargin(slideView, leftMargin);
//                float alpha = shadowAlpha * (1.0f - ((float) Math.abs(leftMargin))/slideViewWidth);
//                shadowView.setAlpha(alpha);
//            }
//        });
//        animator.start();
        if(mScroller == null)
        {
            mScroller = new Scroller(getContext());
        }
        int startX = getViewLeftMargin(slideView);
        int targetX = 0;
        mScroller.startScroll(startX, 0, targetX - startX, 0, scrollDuration);
        postInvalidate();
    }

    private void createVelocityTracker(MotionEvent event)
    {
        if(velocityTracker == null)
        {
            velocityTracker = VelocityTracker.obtain();

        }
        velocityTracker.addMovement(event);
    }

    private void recycleVelocityTracker()
    {
        if(velocityTracker != null)
        {
            velocityTracker.recycle();

        }
        velocityTracker = null;
    }

    private float getVelocityTracker()
    {
        velocityTracker.computeCurrentVelocity(1000);
        return velocityTracker.getXVelocity();
    }

    public interface OnSlideListener
    {
        /**
         * 1代表完全未滑出， 0代表完全滑出
         * */
        public void onSlide(float process);
    }

    public void addOnSlideListener(OnSlideListener listener)
    {
        listeners.add(listener);
    }

    public void removeSlideListener(OnSlideListener listener)
    {
        if(listeners != null && listeners.size() != 0)
        {
            listeners.remove(listener);
        }
    }

    private void notifyOnSlideListeners(float process)
    {
        if(listeners != null && listeners.size() != 0)
        {
            for(OnSlideListener l : listeners)
            {
                l.onSlide(process);
            }
        }
    }
}
