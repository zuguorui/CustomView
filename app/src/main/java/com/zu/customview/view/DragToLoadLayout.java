package com.zu.customview.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Rect;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.Px;
import android.support.annotation.StyleRes;
import android.support.v4.view.NestedScrollingChildHelper;
import android.support.v4.view.NestedScrollingParentHelper;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.OverScroller;
import android.widget.Scroller;

import com.zu.customview.MyLog;


/**
 * Created by zu on 2017/10/17.
 */

public class DragToLoadLayout extends FrameLayout{

    MyLog log = new MyLog("DragToLoadLayout", true);

    private DragLoadView upDragLoadView;
    private DragLoadView downDragLoadView;
    private View contentView;

    private final NestedScrollingChildHelper mNestedChildHelper;
    private final NestedScrollingParentHelper mNestedParentHelper;

    private boolean layouted = false;

    private DragLoadView.OnLoadListener upOnLoadListener = new DragLoadView.OnLoadListener() {
        @Override
        public void onLoadComplete(boolean success) {
            stopScroll();
            Rect visibleRect = getVisibleRect();
            int offset = visibleRect.top - upDragLoadView.getBottom();
            if(offset < 0)
            {
                startScroll(0, 0, 0, offset, 600);
            }
        }

        @Override
        public void onLoadStart() {
            stopScroll();
            Rect visibleRect = getVisibleRect();
            int offset = visibleRect.top - upDragLoadView.getTop();
            if(offset > 0)
            {
                startScroll(0, 0, 0, offset, 600);
            }
        }

        @Override
        public void onLoadCancel() {
            stopScroll();
            Rect visibleRect = getVisibleRect();
            int offset = visibleRect.top - upDragLoadView.getBottom();
            if(offset < 0)
            {
                startScroll(0, 0, 0, offset, 600);
            }
        }
    };

    private DragLoadView.OnLoadListener downOnLoadListener = new DragLoadView.OnLoadListener() {
        @Override
        public void onLoadComplete(boolean success) {
            stopScroll();
            Rect visibleRect = getVisibleRect();
            int offset = visibleRect.bottom - downDragLoadView.getTop();
            if(offset > 0)
            {
                startScroll(0, 0, 0, offset, 600);
            }
        }

        @Override
        public void onLoadStart() {
            stopScroll();
            Rect visibleRect = getVisibleRect();
            int offset = visibleRect.bottom - downDragLoadView.getBottom();
            if(offset < 0)
            {
                startScroll(0, 0, 0, offset, 600);
            }
        }

        @Override
        public void onLoadCancel() {
            stopScroll();
            Rect visibleRect = getVisibleRect();
            int offset = visibleRect.bottom - downDragLoadView.getTop();
            if(offset > 0)
            {
                startScroll(0, 0, 0, offset, 600);
            }
        }
    };



    public DragToLoadLayout(@NonNull Context context) {
        this(context, null);
    }

    public DragToLoadLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DragToLoadLayout(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);

    }

    public DragToLoadLayout(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mNestedChildHelper = new NestedScrollingChildHelper(this);
        mNestedParentHelper = new NestedScrollingParentHelper(this);
        setNestedScrollingEnabled(true);
    }






    private void offsetChildrenY(int offset)
    {

        for(int i = 0; i < getChildCount(); i++)
        {
            View child = getChildAt(i);
            child.offsetTopAndBottom(offset);
        }
    }

    private void notifyDragStat(boolean release)
    {
        Rect visibleRect = getVisibleRect();
        if(upDragLoadView.getBottom() > visibleRect.top)
        {
            int offset = Math.abs(upDragLoadView.getBottom() - visibleRect.top);
            int height = upDragLoadView.getMeasuredHeight();
            float process = offset * 1.0f / height;
            if(release)
            {
                upDragLoadView.dragRelease(process);
            }else
            {
                upDragLoadView.drag(process);
            }

        }else if(downDragLoadView.getTop() < visibleRect.bottom)
        {
            int offset = Math.abs(downDragLoadView.getTop() - visibleRect.bottom);
            int height = downDragLoadView.getMeasuredHeight();
            float process = offset * 1.0f / height;
            if(release)
            {
                downDragLoadView.dragRelease(process);
            }else
            {
                downDragLoadView.drag(process);
            }

        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int selfHeight = MeasureSpec.getSize(heightMeasureSpec);
        int selfWidth = MeasureSpec.getSize(widthMeasureSpec);
        if(getChildCount() == 0)
        {
            setMeasuredDimension(selfWidth, selfHeight);
            return;
        }

        checkViews();
        upDragLoadView = (DragLoadView)getChildAt(0);
        contentView = getChildAt(1);
        downDragLoadView = (DragLoadView)getChildAt(2);

        upDragLoadView.setOnLoadListener(upOnLoadListener);
        downDragLoadView.setOnLoadListener(downOnLoadListener);


        int childHeightSpec = getChildMeasureSpec(heightMeasureSpec, 0, upDragLoadView.getLayoutParams().height);
        int childWidthSpec = getChildMeasureSpec(widthMeasureSpec, 0, upDragLoadView.getLayoutParams().width);
        upDragLoadView.measure(childWidthSpec, childHeightSpec);

        childHeightSpec = MeasureSpec.makeMeasureSpec(selfHeight, MeasureSpec.AT_MOST);
        childWidthSpec = MeasureSpec.makeMeasureSpec(selfWidth, MeasureSpec.EXACTLY);
        contentView.measure(childWidthSpec, childHeightSpec);

        childHeightSpec = getChildMeasureSpec(heightMeasureSpec, 0, downDragLoadView.getLayoutParams().height);
        childWidthSpec = getChildMeasureSpec(widthMeasureSpec, 0, downDragLoadView.getLayoutParams().width);
        downDragLoadView.measure(childWidthSpec, childHeightSpec);

        setMeasuredDimension(selfWidth, selfHeight);



    }

    private boolean checkViews()
    {
        if(getChildCount() != 3)
        {
            throw new IllegalStateException("DragToLoadLayout must have 3 views, the first view is head, the second view is content, and the third view is footer");
        }
        if(!(getChildAt(0) instanceof DragLoadView) || !(getChildAt(2) instanceof DragLoadView))
        {
            throw new IllegalStateException("The first and the last view should be a instance of DragLoadView or its subclass");
        }

        return true;
    }

    /*此处需要注意，由于HideHeadLayout存在，因此该函数会经常执行，故布局函数还需重写*/
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if(changed)
        {
            if(getChildCount() == 0 || layouted)
            {
                return;
            }
            checkViews();

            Rect rect = getVisibleRect();
            upDragLoadView.layout(rect.left, rect.top - upDragLoadView.getMeasuredHeight(), rect.right, rect.top);
            contentView.layout(rect.left, rect.top, rect.right, rect.top + contentView.getMeasuredHeight());
            downDragLoadView.layout(rect.left, rect.bottom, rect.right, rect.bottom + downDragLoadView.getMeasuredHeight());
//            int top = rect.top - getChildAt(0).getMeasuredHeight();
//
//
//            int left = rect.left;
//            int right = rect.right;
//            for(int i = 0; i < getChildCount(); i++)
//            {
//                View view = getChildAt(i);
//                view.layout(left, top, right, top + view.getMeasuredHeight());
//                top += view.getMeasuredHeight();
//            }
            layouted = true;
        }

    }


    private VelocityTracker velocityTracker = null;
    private OverScroller scroller = new OverScroller(getContext());
    private boolean isTouching = false;
    private static final int INVALID_ID = -1;
    private int mActivePointId = INVALID_ID;
    private int downY, downX, oldX, oldY, newX, newY;
    private int touchSlop = 2;
    private int lastScrollY = 0;
    private boolean downInLoadView = false;
    private boolean parentConsumeNestedScroll = false;
    private boolean dispatchTouchEventFail = false;
    private boolean downInSpace = false;
    private boolean isDragging = false;


    private boolean headFirstScroll = true;
    private boolean reactOnDragHead = true;

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        MotionEvent tempEven = MotionEvent.obtain(ev);
        if(ev.getActionMasked() == MotionEvent.ACTION_DOWN)
        {
//            log.d("ACTION_DOWN");

            stopScroll();
            isTouching = true;
        }
        boolean consumed = super.dispatchTouchEvent(tempEven);
        if(!consumed)
        {
            dispatchTouchEventFail = true;
        }
        if(ev.getActionMasked() == MotionEvent.ACTION_CANCEL || ev.getActionMasked() == MotionEvent.ACTION_UP)
        {
            //                log.d("ACTION_UP");

            recycleVelocityTracker();
            isTouching = false;
            downInLoadView = false;
            isDragging = false;

            dispatchTouchEventFail = false;
            downInSpace = false;
//            animateThread.enqueueTaskDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    animatePosition();
//                }
//            }, 600);

            notifyDragStat(true);
        }

        return downInLoadView || consumed || dispatchTouchEventFail || downInSpace;

    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {

        int pointIndex = -1;
        boolean intercepted = false;
        MotionEvent tempEvent = MotionEvent.obtain(ev);

        createOrUpdateVelocityTracker(ev);

        switch (tempEvent.getActionMasked())
        {
            case MotionEvent.ACTION_DOWN:
//                log.d("ACTION_DOWN");
                mActivePointId = tempEvent.getPointerId(0);
                pointIndex = tempEvent.findPointerIndex(mActivePointId);
                if(pointIndex < 0)
                {
                    return false;
                }
                intercepted = false;
                newX = downX = (int)tempEvent.getX(pointIndex);
                newY = downY = (int)tempEvent.getY(pointIndex);
                mActivePointId = tempEvent.getPointerId(0);
                boolean downInHead = (downY + getScrollY() <= upDragLoadView.getBottom() && downY + getScrollY() >= upDragLoadView.getTop()
                        && downX + getScrollX() <= upDragLoadView.getRight() && downX + getScrollX() >= upDragLoadView.getLeft());
                boolean downInFoot = (downY + getScrollY() <= downDragLoadView.getBottom() && downY + getScrollY() >= downDragLoadView.getTop()
                        && downX + getScrollX() <= downDragLoadView.getRight() && downX + getScrollX() >= downDragLoadView.getLeft());
                downInSpace = !(downY + getScrollY() <= contentView.getBottom() && downY + getScrollY() >= contentView.getTop()
                        && downX + getScrollX() <= contentView.getRight() && downX + getScrollX() >= contentView.getLeft());
                if(downInHead || downInFoot)
                {
                    downInLoadView = true;
                    log.d("onInterceptTouchEvent downInLoadView = " + downInLoadView);
                }
                break;
//            case MotionEvent.ACTION_POINTER_DOWN:
//                log.d("ACTION_POINTER_DOWN");
//                onMultiPointEvent(tempEvent);
//                break;
            case MotionEvent.ACTION_MOVE:
//                log.d("ACTION_MOVE");
                if(mActivePointId == INVALID_ID)
                {
                    return false;
                }
                pointIndex = tempEvent.findPointerIndex(mActivePointId);
                oldX = newX;
                oldY = newY;
                newX = (int)tempEvent.getX(pointIndex);
                newY = (int)tempEvent.getY(pointIndex);
                int dy = newY - oldY;
                int disY = newY - downY;
                if(!isDragging)
                {
                    if(dy >= touchSlop || disY >= touchSlop)
                    {
                        isDragging = true;
                    }
                }
                if(isDragging && downInLoadView)
                {
                    intercepted = true;
                }
                break;
            case MotionEvent.ACTION_POINTER_UP:
                onSecondPointUp(tempEvent);
//                log.d("ACTION_POINTER_UP");
                break;
            case MotionEvent.ACTION_UP:
//                log.d("ACTION_UP");
                break;
            case MotionEvent.ACTION_CANCEL:
//                log.d("ACTION_CANCEL");
                break;
            default:
                break;


        }
        return intercepted || dispatchTouchEventFail;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean consumed = false;
        int pointIndex = -1;
        MotionEvent tempEvent = MotionEvent.obtain(event);
//        tempEvent.offsetLocation(0, getScrollY());
//        log.d("onTouchEvent ev.getY = " + (int)event.getY());
        createOrUpdateVelocityTracker(event);

        switch (tempEvent.getActionMasked())
        {
//            case MotionEvent.ACTION_DOWN:
////                log.d("ACTION_DOWN");
//                consumed = false;
//                newX = downX = (int)tempEvent.getX();
//                newY = downY = (int)tempEvent.getY();
//                mActivePointId = tempEvent.getPointerId(0);
//
//                break;
            case MotionEvent.ACTION_POINTER_DOWN:
//                log.d("ACTION_POINTER_DOWN");

                break;
            case MotionEvent.ACTION_MOVE:

//                log.d("ACTION_MOVE");
                if(mActivePointId == INVALID_ID)
                {
                    return false;
                }
                pointIndex = tempEvent.findPointerIndex(mActivePointId);
                if(pointIndex < 0)
                {
                    return false;
                }
                oldX = newX;
                oldY = newY;
                newX = (int)tempEvent.getX(pointIndex);
                newY = (int)tempEvent.getY(pointIndex);
                int dy = newY - oldY;
                int disY = newY - downY;
                if(!isDragging)
                {
                    if(Math.abs(dy) >= touchSlop || Math.abs(disY) >= touchSlop)
                    {
                        isDragging = true;
                    }
                }
                if(isDragging && (downInLoadView || downInSpace))
                {
                    if(shouldScrollY(dy) && dy != 0)
                    {
                        computeAndScrollY(dy, true);
//                        scrollBy(0, -offsetY);
                    }
                    consumed = true;
                }
                break;
            case MotionEvent.ACTION_POINTER_UP:
                onSecondPointUp(tempEvent);
//                log.d("ACTION_POINTER_UP");
                break;
            case MotionEvent.ACTION_UP:
//                log.d("ACTION_UP");
            case MotionEvent.ACTION_CANCEL:
//                log.d("ACTION_CANCEL");
                float velocityY = getYVelocity();
//                log.d("velocityY = " + velocityY);
                if(velocityY != 0f)
                {
                    startFling(0, (int)velocityY);

                }
                break;
            default:
                break;


        }
        return consumed;
    }

    @Override
    public void computeScroll() {
        if(scroller == null)
        {
            return;
        }else if(!scroller.computeScrollOffset())
        {

            return;
        }else
        {
            int newScrollY = scroller.getCurrY();

            int dy = newScrollY - lastScrollY;
            lastScrollY = newScrollY;
//            log.d("computeScroll, dy = " + dy);
            if(shouldScrollY(dy))
            {
                int offset = computeAndScrollY(dy, isTouching);
            }else
            {
                stopScroll();
                return;
            }

            invalidate();

        }
    }

    private boolean shouldScrollY(int dy)
    {
        Rect visibleRect = getVisibleRect();

        if(dy > 0)
        {
            if(upDragLoadView.getTop() < visibleRect.top)
            {
                return true;
            }else
            {
                return false;
            }
        }else if(dy < 0)
        {
            if(downDragLoadView.getBottom() > visibleRect.bottom)
            {
                return true;
            }else
            {
                return false;
            }
        }


        return true;

    }

    private void stopScroll()
    {
        if(!scroller.isFinished())
        {
            scroller.forceFinished(true);
        }
    }

    private void startScroll(int startX, int startY, int dx, int dy, int duration)
    {
        stopScroll();
        lastScrollY = startY;
        scroller.startScroll(startX, startY, dx, dy, duration);
        invalidate();
    }

    private void startFling(int velocityX, int velocityY)
    {
        stopScroll();
        lastScrollY = 0;
        scroller.fling(0 , 0, velocityX, velocityY, Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE);
        invalidate();
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

    /**
     * compute the offset and scroll children. Attention that the consumed offset may not equals the real offset of children's scrolling.
     * @param dy the offset of scroll.
     * @param hasResistance whether compute resistance. if true, means we reduce dy depends on how much the length views offset.
     *                      if false, we will offset views definitely by dy expect reaching edge.
     * @return consumed offset of dy, will not bigger than dy.
     *
     *
     * */
    private int computeAndScrollY(int dy, boolean hasResistance)
    {
        Rect visibleRect = getVisibleRect();
        int offset = 0;
        int consumed = dy;
        if(dy == 0)
        {
            return 0;
        }else if (dy < 0)
        {
            if(downDragLoadView.getBottom() <= visibleRect.bottom)
            {
                return 0;
            }
            int dis = Math.abs(downDragLoadView.getBottom() - visibleRect.bottom);
            float process = hasResistance ? dis * 1.0f / downDragLoadView.getHeight() + 0.1f : 1.0f;
            if(process > 1.0f)
            {
                process = 1.0f;
            }
            offset = (int)(process * dy);
            if(upDragLoadView.getBottom() > visibleRect.top && upDragLoadView.getBottom() + offset < visibleRect.top)
            {
                offset = visibleRect.top - upDragLoadView.getBottom();
                consumed = (int)(offset / process);
            }else if(offset + downDragLoadView.getBottom() < visibleRect.bottom)
            {
                offset = visibleRect.bottom - downDragLoadView.getBottom();
                consumed = (int)(offset / process);
            }
        }else
        {
            if(upDragLoadView.getTop() >= visibleRect.top)
            {
                return 0;
            }
            int dis = Math.abs(upDragLoadView.getTop() - visibleRect.top);
            float process = hasResistance ? dis * 1.0f / upDragLoadView.getHeight() + 0.1f : 1.0f;
            if(process > 1.0f)
            {
                process = 1.0f;
            }

            offset = (int)(process * dy);
            if(downDragLoadView.getTop() < visibleRect.bottom && downDragLoadView.getTop() + offset > visibleRect.bottom)
            {
                offset = visibleRect.bottom - downDragLoadView.getTop();
                consumed = (int)(offset / process);
            }else if(upDragLoadView.getTop() + offset > visibleRect.top)
            {
                offset = visibleRect.top - upDragLoadView.getTop();
                consumed = (int)(offset / process);
            }
        }
        offsetChildrenY(offset);
        if(upDragLoadView.getBottom() - offset > visibleRect.top && upDragLoadView.getBottom() <= visibleRect.top)
        {
            upDragLoadView.viewHidden();
        }
        if(downDragLoadView.getTop() - offset < visibleRect.bottom && downDragLoadView.getTop() >= visibleRect.bottom)
        {
            downDragLoadView.viewHidden();
        }
        if(isTouching)
        {
            notifyDragStat(false);
        }

        return consumed;
    }

    private void onSecondPointUp(MotionEvent event)
    {
        int pointIndex = event.getActionIndex();
        int pointId = event.getPointerId(pointIndex);
        if(pointId == mActivePointId)
        {
            int newPointIndex = pointIndex == 0 ? 1 : 0;
            mActivePointId = event.getPointerId(newPointIndex);
            newX = oldX = (int)event.getX(newPointIndex);
            newY = oldY = (int)event.getY(newPointIndex);
        }
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
//        waitFling = false;
        if(parentConsumeNestedScroll)
        {
            stopNestedScroll();
            parentConsumeNestedScroll = false;
        }

    }

    @Override
    public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
//        log.d("onNestedScroll");

        if(shouldScrollY(-dyUnconsumed))
        {
            int consumed = computeAndScrollY(-dyUnconsumed, true);

//            scrollBy(0, -scrollOffsetY);
            dyConsumed += -consumed;
            dyUnconsumed -= -consumed;
        }
        if(parentConsumeNestedScroll)
        {
            dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, null);
        }
    }

    @Override
    public void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {
        log.d("onNestedPreScroll, dy = " + dy);
        Rect visibleRect = getVisibleRect();
        int[] parentConsumed = new int[2];
        if(parentConsumeNestedScroll)
        {

            dispatchNestedPreScroll(dx, dy, parentConsumed, null);

        }
        if((-dy < 0 && upDragLoadView.getBottom() > visibleRect.top)
                || (-dy > 0 && downDragLoadView.getTop() < visibleRect.bottom))
        {
            int restY = dy - parentConsumed[1];
            int restX = dx - parentConsumed[0];
            if(shouldScrollY(-restY))
            {
                int consumedOffset = computeAndScrollY(-restY, true);

//                scrollBy(0, -scrollOffset);
                consumed[1] = parentConsumed[1]  + (- consumedOffset);
            }
            consumed[0] = parentConsumed[0];
        }




    }


    @Override
    public boolean onNestedFling(View target, float velocityX, float velocityY, boolean consumed) {

//        log.d("onNestedFling, velocityY = " + (int)velocityY + ", consumed = " + consumed);
        return dispatchNestedFling(velocityX, velocityY, consumed);

//        if(Math.abs(velocityX) > Math.abs(velocityY))
//        {
//            return dispatchNestedFling(velocityX, velocityY, consumed);
//        }else
//        {
//            if(((-velocityY > 0 && shouldScrollY(1)) || (-velocityY < 0 && shouldScrollY(-1))) && headFirstScroll)
//            {
//                startFling(0, -(int)velocityY);
//                return true;
//            }else
//            {
//                return dispatchNestedFling(velocityX, velocityY, consumed);
//            }
//
//        }

    }

    @Override
    public boolean onNestedPreFling(View target, float velocityX, float velocityY) {

//        log.d("onNestedPreFling, velocityY = " + (int)velocityY);
            return false;

//        if(Math.abs(velocityX) > Math.abs(velocityY))
//        {
//            return dispatchNestedPreFling(velocityX, velocityY);
//
//        }else if(dispatchNestedPreFling(velocityX, velocityY)){
//            return true;
//        }else
//        {
//            if(!headFirstScroll && -velocityY > 0 && upDragLoadView.getBottom() <= getVisibleRect().top)
//            {
//                return false;
//            }
//            if(((-velocityY > 0 && shouldScrollY(1)) || (-velocityY < 0 && shouldScrollY(-1))))
//            {
////                waitFling = !headFirstScroll;
//                startFling(0, -(int)velocityY);
////                log.d("onNestedPreFling, start fling");
//
//
//                return true;
//            }else
//            {
//
//                return dispatchNestedPreFling(velocityX, velocityY);
//
//            }
//
//        }


    }

    @Override
    public int getNestedScrollAxes() {
        return mNestedParentHelper.getNestedScrollAxes();
    }





}
