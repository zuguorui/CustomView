package com.zu.customview.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.view.NestedScrollingChild;
import android.support.v4.view.NestedScrollingChildHelper;
import android.support.v4.view.NestedScrollingParent;
import android.support.v4.view.NestedScrollingParentHelper;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ScrollerCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Scroller;

/**
 * Created by zu on 2017/11/4.
 */

public class HideHeadView extends ViewGroup implements NestedScrollingParent, NestedScrollingChild {

    private final NestedScrollingChildHelper mNestedChildHelper;
    private final NestedScrollingParentHelper mNestedParentHelper;

    private boolean layouted = false;


    public HideHeadView(Context context) {
        this(context, null);
    }

    public HideHeadView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HideHeadView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public HideHeadView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mNestedChildHelper = new NestedScrollingChildHelper(this);
        mNestedParentHelper = new NestedScrollingParentHelper(this);
        setNestedScrollingEnabled(true);

    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if(changed)
        {
            if(getChildCount() != 2)
            {
                throw new IllegalArgumentException("HideHeadLayout must have 2 views, the first is head, and the second as content");
            }

            if(getChildCount() == 0 || layouted)
            {
                return;
            }
            int top = getPaddingTop();


            int left = getPaddingLeft();
            int right = getMeasuredWidth() - getPaddingRight();
            for(int i = 0; i < getChildCount(); i++)
            {
                View view = getChildAt(i);
                view.layout(left, top, right, top + view.getMeasuredHeight());
                top += view.getMeasuredHeight();
            }
            layouted = true;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if(getChildCount() != 2)
        {
            throw new IllegalArgumentException("HideHeadLayout must have 2 views, the first is head, and the second as content");
        }
        View head = getChildAt(0);
        int heightSpec = MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(heightMeasureSpec), MeasureSpec.AT_MOST);
        int widthSpec = MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY);
        head.measure(widthSpec, heightSpec);

        View content = getChildAt(1);
        heightSpec = MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(heightMeasureSpec), MeasureSpec.EXACTLY);
        content.measure(widthSpec, heightSpec);

        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec));
    }

    private VelocityTracker velocityTracker = null;
    private Scroller scroller = new Scroller(getContext());
    private static final int INVALID_ID = -1;
    private int mActivePointId = INVALID_ID;
    private int downY, downX, oldX, oldY, newX, newY;
    private int touchSlop = 2;
    private int lastScrollY = 0;

    private void onSecondPointEvent(MotionEvent event)
    {
        if(event.findPointerIndex(mActivePointId) == -1)
        {
            int newIndex = event.getActionIndex();
            mActivePointId = event.getPointerId(newIndex);

        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getActionMasked())
        {
            case MotionEvent.ACTION_DOWN:
            {
                if(!scroller.isFinished())
                {
                    scroller.forceFinished(true);
                }
                mActivePointId = ev.getPointerId(ev.getActionIndex());
                newX = downX = oldX = (int)ev.getX();
                newY = downY = oldY = (int)ev.getY();

            }
            break;
            case MotionEvent.ACTION_POINTER_DOWN:
            {

            }
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }

    private void createOrUpdateVelocityTracker(MotionEvent event)
    {
        if(velocityTracker == null)
        {
            velocityTracker = VelocityTracker.obtain();
        }
        velocityTracker.addMovement(event);
    }

    private float getYVelocity()
    {
        if(velocityTracker != null)
        {
            velocityTracker.computeCurrentVelocity(1000);
            return velocityTracker.getYVelocity();
        }else
        {
            return 0;
        }
    }

    private void recycleVelocityTracker()
    {
        if(velocityTracker != null)
        {
            velocityTracker.recycle();
            velocityTracker = null;
        }
    }

    private void flingY(float velocityY)
    {
        if(!scroller.isFinished())
        {
            scroller.forceFinished(true);
        }
        scroller.fling(0, 0, 0, (int)velocityY, Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE);

    }




    /*NestedScrollingChild APIs*/
    @Override
    public void setNestedScrollingEnabled(boolean enabled) {
        mNestedChildHelper.setNestedScrollingEnabled(enabled);
    }

    @Override
    public boolean isNestedScrollingEnabled() {
        return mNestedChildHelper.isNestedScrollingEnabled();
    }

    @Override
    public boolean startNestedScroll(int axes) {
        return mNestedChildHelper.startNestedScroll(axes);
    }

    @Override
    public void stopNestedScroll() {
        mNestedChildHelper.stopNestedScroll();
    }

    @Override
    public boolean hasNestedScrollingParent() {
        return mNestedChildHelper.hasNestedScrollingParent();
    }

    @Override
    public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int[] offsetInWindow) {
        return mNestedChildHelper.dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, offsetInWindow);
    }

    @Override
    public boolean dispatchNestedPreScroll(int dx, int dy, int[] consumed, int[] offsetInWindow) {
        return mNestedChildHelper.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow);
    }

    @Override
    public boolean dispatchNestedFling(float velocityX, float velocityY, boolean consumed) {
        return mNestedChildHelper.dispatchNestedFling(velocityX, velocityY, consumed);
    }

    @Override
    public boolean dispatchNestedPreFling(float velocityX, float velocityY) {
        return mNestedChildHelper.dispatchNestedPreFling(velocityX, velocityY);
    }


    /*NestedScrollingParent APIs*/
    @Override
    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
        return false;
    }

    @Override
    public void onNestedScrollAccepted(View child, View target, int nestedScrollAxes) {
        mNestedParentHelper.onNestedScrollAccepted(child, target, nestedScrollAxes);
    }

    @Override
    public void onStopNestedScroll(View target) {
        mNestedParentHelper.onStopNestedScroll(target);
    }

    @Override
    public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {

    }

    @Override
    public void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {

    }

    @Override
    public boolean onNestedFling(View target, float velocityX, float velocityY, boolean consumed) {
        return false;
    }

    @Override
    public boolean onNestedPreFling(View target, float velocityX, float velocityY) {
        return false;
    }

    @Override
    public int getNestedScrollAxes() {
        return mNestedParentHelper.getNestedScrollAxes();
    }

    public interface HeadInterface{
        /**
         * 在滑动head的过程中会将目前的进程情况通知给Head，以便head做出一些响应。
         * @param process:[0, 1], 完全显示时为0， 完全隐藏时为1。
         * */
        void onHideProcess(float process);
        /**
         * 获取Head至少要显示的高度，在滑动过程中会保留响应的高度，不会完全隐藏head
         *
         * @return :返回head至少要保留的可见高度，滑动时不会完全隐藏head。
         * */
        int getMinVisibleHeight();
    }


}
