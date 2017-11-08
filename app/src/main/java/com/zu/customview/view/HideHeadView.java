package com.zu.customview.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.support.annotation.Px;
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

import com.zu.customview.MyLog;

/**
 * Created by zu on 2017/11/4.
 */

public class HideHeadView extends ViewGroup implements NestedScrollingParent, NestedScrollingChild {

    private MyLog log = new MyLog("HideHeadView", true);
    private final NestedScrollingChildHelper mNestedChildHelper;
    private final NestedScrollingParentHelper mNestedParentHelper;

    private boolean layouted = false;

    private View headView;
    private View contentView;

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
            if(getChildCount() == 0 || layouted)
            {
                return;
            }
            if(getChildCount() != 2)
            {
                throw new IllegalArgumentException("HideHeadLayout must have 2 views, the first is head, and the second as content");
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
        if(getChildCount() == 0)
        {
            return;
        }
        if(getChildCount() != 2)
        {
            throw new IllegalArgumentException("HideHeadLayout must have 2 views, the first is head, and the second as content");
        }
        headView = getChildAt(0);
        int selfHeight = MeasureSpec.getSize(heightMeasureSpec);
        int selfWidth = MeasureSpec.getSize(widthMeasureSpec);
        int heightSpec = getChildMeasureSpec(heightMeasureSpec, 0, headView.getLayoutParams().height);
        int widthSpec = getChildMeasureSpec(widthMeasureSpec, 0, headView.getLayoutParams().width);
        headView.measure(widthSpec, heightSpec);

        int headRemain = 0;
        if(headView instanceof HeadInterface)
        {
            headRemain = ((HeadInterface) headView).getMinVisibleHeight();
        }

        contentView = getChildAt(1);
        widthSpec = MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY);
        heightSpec = MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(heightMeasureSpec) - headRemain, MeasureSpec.EXACTLY);
        contentView.measure(widthSpec, heightSpec);

        setMeasuredDimension(selfWidth, selfHeight);
    }

    private VelocityTracker velocityTracker = null;
    private Scroller scroller = new Scroller(getContext());
    private static final int INVALID_ID = -1;
    private int mActivePointId = INVALID_ID;
    private int downY, downX, oldX, oldY, newX, newY;
    private int touchSlop = 2;
    private int lastScrollY = 0;

    private boolean isDragging = false;

    private void onMultiPointEvent(MotionEvent event)
    {
        if(event.findPointerIndex(mActivePointId) == -1)
        {
            int newIndex = event.getActionIndex();
            mActivePointId = event.getPointerId(newIndex);
            int dy = (int)event.getY(newIndex) - newY;
            int dx = (int)event.getX(newIndex) - newX;
            downY += dy;
            downX += dx;
            newY = (int)event.getY(newIndex);
            newX = (int)event.getX(newIndex);

        }
    }



    private boolean shouldScrollY(int dy)
    {
        Rect visibleRect = getVisibleRect();
        if(dy > 0)
        {
            if(headView.getTop() < visibleRect.top)
            {
                return true;
            }else
            {
                return false;
            }
        }else if(dy < 0)
        {
            if(contentView.getBottom() > visibleRect.bottom)
            {
                return true;
            }else
            {
                return false;
            }
        }


        return false;

    }

    private int computeScrollOffsetY(int dy)
    {
        int mDy = dy;
        Rect visibleRect = getVisibleRect();
        if(mDy > 0)
        {
            if(headView.getTop() + mDy > visibleRect.top)
            {
                mDy = visibleRect.top - headView.getTop();
            }
            return mDy;
        }else if(mDy < 0)
        {
            if(contentView.getBottom() + mDy < visibleRect.bottom)
            {
                mDy = visibleRect.bottom - contentView.getBottom();
            }
            return mDy;
        }else
        {
            return 0;
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if(ev.getActionMasked() == MotionEvent.ACTION_DOWN)
        {
            if(!scroller.isFinished())
            {
                scroller.forceFinished(true);
            }
        }
        return false;
    }



    //    @Override
//    public boolean onInterceptTouchEvent(MotionEvent ev) {
//        boolean intercept = false;
//        switch (ev.getActionMasked())
//        {
//            case MotionEvent.ACTION_DOWN:
//            {
//                if(!scroller.isFinished())
//                {
//                    scroller.forceFinished(true);
//                }
//                mActivePointId = ev.getPointerId(ev.getActionIndex());
//                newX = downX = oldX = (int)ev.getX();
//                newY = downY = oldY = (int)ev.getY();
//                intercept = false;
//            }
//            break;
//            case MotionEvent.ACTION_POINTER_DOWN:
//            case MotionEvent.ACTION_POINTER_UP:
//            {
//                onMultiPointEvent(ev);
//                intercept = isDragging;
//            }
//            break;
//            case MotionEvent.ACTION_MOVE:
//            {
//                oldX = newX;
//                newY = oldY;
//                int dy = newY - oldY;
//
//            }
//        }
//        return intercept;
//    }

//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//        return super.onTouchEvent(event);
//    }

    private void startScroll(int startX, int startY, int dx, int dy)
    {
        stopScroll();
        lastScrollY = startY;
        scroller.startScroll(startX, startY, dx, dy);
    }

    private void startFling(int velocityX, int velocityY)
    {
        stopScroll();
        lastScrollY = 0;
        scroller.fling(0 , 0, velocityX, velocityY, Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE);

    }

    private void stopScroll()
    {
        if(!scroller.isFinished())
        {
            scroller.forceFinished(true);
        }
    }

    @Override
    public void computeScroll() {
        if(scroller != null && scroller.computeScrollOffset())
        {
            int newScrollY = scroller.getCurrY();
            int dy = newScrollY - lastScrollY;
            int offset = computeScrollOffsetY(dy);
            if(offset != 0)
            {
//                offsetChildrenY(dy);
                scrollBy(0, -dy);
                lastScrollY = newScrollY;
                if(headView instanceof HeadInterface)
                {

                }
                invalidate();
            }else
            {
                scroller.forceFinished(true);
                lastScrollY = 0;
            }


        }
    }

    private void offsetChildrenY(int dy)
    {
        for(int i = 0; i < getChildCount(); i++)
        {
            View view = getChildAt(i);
            view.offsetTopAndBottom(dy);
        }
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

    private Rect getVisibleRect()
    {
        int mScrollY = getScrollY();
        int mScrollX = getScrollX();
        int top = getPaddingTop() + mScrollY;
        int bottom = getMeasuredHeight() - getPaddingBottom() + mScrollY;
        int left = getPaddingLeft() + mScrollX;
        int right = getMeasuredWidth() - getPaddingRight() + mScrollX;
        return new Rect(left, top, right, bottom);
    }




    private boolean parentConsumeNestedScroll = false;
    private boolean parentConsumeNestedFling = false;
    private boolean scrollOrFling = false;
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
        log.d("onStartNestedScroll");
        parentConsumeNestedScroll = startNestedScroll(nestedScrollAxes);
        if((ViewCompat.SCROLL_AXIS_VERTICAL & nestedScrollAxes) == ViewCompat.SCROLL_AXIS_VERTICAL)
        {
            return true;
        }else {
            return parentConsumeNestedScroll;
        }

    }

    @Override
    public void onNestedScrollAccepted(View child, View target, int nestedScrollAxes) {
        log.d("onNestedScrollAccepted");
        mNestedParentHelper.onNestedScrollAccepted(child, target, nestedScrollAxes);

    }

    @Override
    public void onStopNestedScroll(View target) {
        log.d("onStopNestedScroll");
        mNestedParentHelper.onStopNestedScroll(target);
        if(parentConsumeNestedScroll)
        {
            stopNestedScroll();
            parentConsumeNestedScroll = false;
        }
    }

    @Override
    public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        log.d("onNestedScroll");

        if(shouldScrollY(-dyConsumed))
        {
            int scrollOffsetY = computeScrollOffsetY(-dxConsumed);
//            offsetChildrenY(scrollOffsetY);
            scrollBy(0, -scrollOffsetY);
            dyConsumed += -scrollOffsetY;
            dyUnconsumed -= -scrollOffsetY;
        }
        if(parentConsumeNestedScroll)
        {
            dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, null);
        }
    }

    @Override
    public void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {
        log.d("onNestedPreScroll");
        if(parentConsumeNestedScroll)
        {
            int[] parentConsumed = new int[2];
            dispatchNestedPreScroll(dx, dy, parentConsumed, null);
            int restY = dy - parentConsumed[1];
            int restX = dx - parentConsumed[0];
            if(shouldScrollY(-restY))
            {
                int scrollOffset = computeScrollOffsetY(-restY);
                offsetChildrenY(scrollOffset);
                consumed[1] = parentConsumed[1] - scrollOffset;
            }
            consumed[0] = parentConsumed[0];
        }else
        {
            if(shouldScrollY(-dy))
            {
                int scrollOffset = computeScrollOffsetY(-dy);
//                offsetChildrenY(scrollOffset);
                scrollBy(0, -scrollOffset);
                log.d("scrollOffset = " + scrollOffset);
                consumed[1] = -scrollOffset;
//                consumed[1] = dy;
            }
            consumed[0] = 0;
        }

    }

    @Override
    public void scrollTo(@Px int x, @Px int y) {

        int realMoveY = y;
        if(realMoveY > headView.getBottom())
        {
            realMoveY = headView.getBottom();
        }else if(realMoveY < headView.getTop())
        {
            realMoveY = headView.getTop();
        }
        super.scrollTo(x, realMoveY);
    }

    @Override
    public boolean onNestedFling(View target, float velocityX, float velocityY, boolean consumed) {
//        if(consumed)
//        {
//            return false;
//        }

        if(Math.abs(velocityX) > Math.abs(velocityY))
        {
            return dispatchNestedFling(velocityX, velocityY, consumed);
        }else
        {
            if((-velocityY > 0 && shouldScrollY(1)) || (-velocityY < 0 && shouldScrollY(-1)))
            {
                startFling(0, -(int)velocityY);
                return true;
            }else
            {
                return dispatchNestedFling(velocityX, velocityY, consumed);
            }

        }

    }

    @Override
    public boolean onNestedPreFling(View target, float velocityX, float velocityY) {
        if(Math.abs(velocityX) > Math.abs(velocityY))
        {
            return dispatchNestedPreFling(velocityX, velocityY);
        }else
        {
            if((-velocityY > 0 && shouldScrollY(1)) || (-velocityY < 0 && shouldScrollY(-1)))
            {
                startFling(0, -(int)velocityY);
                return true;
            }else
            {

                return dispatchNestedPreFling(velocityX, velocityY);
            }

        }

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
