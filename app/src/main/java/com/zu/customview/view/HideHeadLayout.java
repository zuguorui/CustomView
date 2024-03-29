package com.zu.customview.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.StrictMode;
import android.support.annotation.Px;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ScrollView;
import android.widget.Scroller;

import androidx.core.view.NestedScrollingChild;
import androidx.core.view.NestedScrollingChildHelper;
import androidx.core.view.NestedScrollingParent;
import androidx.core.view.NestedScrollingParentHelper;
import androidx.core.view.ViewCompat;

import com.zu.customview.MyLog;

import java.util.ArrayList;

/**
 * Created by zu on 2017/11/4.
 */
/**
 * This layout scrolls views by changing mScrollY, so if you want to change the logic of dealing with touch event, keeping an eye on touch event transforming.
 * */
public class HideHeadLayout extends ViewGroup implements NestedScrollingParent, NestedScrollingChild {

    private MyLog log = new MyLog("HideHeadView", true);
    private final NestedScrollingChildHelper mNestedChildHelper;
    private final NestedScrollingParentHelper mNestedParentHelper;

    private boolean layouted = false;

    private View headView;
    private View contentView;

    private SingleTimedTaskQueue animateThread = new SingleTimedTaskQueue();



    public HideHeadLayout(Context context) {
        this(context, null);
    }

    public HideHeadLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HideHeadLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public HideHeadLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mNestedChildHelper = new NestedScrollingChildHelper(this);
        mNestedParentHelper = new NestedScrollingParentHelper(this);
        setNestedScrollingEnabled(true);
//        setClickable(true);
        if(!animateThread.isAlive())
        {
            animateThread.start();
        }
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

    private void animatePosition()
    {
        if(isScrollerRunning() || isTouching)
        {
            return;
        }
        stopScroll();
        Rect visibleRect = getVisibleRect();
        int offset = Math.abs(visibleRect.top - headView.getTop());
        int headHeight = headView.getHeight() - getHeadRemainSpace();
        if(offset <= 0 || offset >= headHeight)
        {
            return;
        }else if(headHeight - offset < offset)
        {

            startScroll(0, headView.getTop(), 0, visibleRect.top - headHeight - headView.getTop(), 600);

        }else
        {

            startScroll(0, headView.getTop(), 0, visibleRect.top - headView.getTop(), 600);

        }
    }

    private void stopAnimatePosition()
    {
        stopScroll();
    }

    private boolean isScrollerRunning()
    {
        if(scroller != null && scroller.computeScrollOffset())
        {
            return true;
        }else
        {
            return false;
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
    private boolean isTouching = false;
    private static final int INVALID_ID = -1;
    private int mActivePointId = INVALID_ID;
    private int downY, downX, oldX, oldY, newX, newY;
    private int touchSlop = 2;
    private int lastScrollY = 0;
    private boolean downInHead = false;

    private boolean isDragging = false;



    private boolean parentConsumeNestedScroll = false;
    private boolean headFirstScroll = true;
    private boolean reactOnDragHead = true;


    public void setHeadFirstScroll(boolean shouldHeadFirstScroll)
    {
        headFirstScroll = shouldHeadFirstScroll;
    }

    public boolean isHeadFirstScroll()
    {
        return headFirstScroll;
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

    private int getHeadRemainSpace()
    {
        if(headView instanceof HeadInterface)
        {
            return ((HeadInterface) headView).getMinVisibleHeight();
        }
        return 0;
    }

    private void notifyHeadHeight()
    {
        if(headView instanceof HeadInterface)
        {
            Rect rect = getVisibleRect();
            ((HeadInterface) headView).onHidenSpace(rect.top - headView.getTop());
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


        return true;

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
    public boolean dispatchTouchEvent(MotionEvent ev) {
//        log.d("before dispatchTouchEvent ev.getY = " + (int)ev.getY() );
        MotionEvent tempEvent = MotionEvent.obtain(ev);

        if(ev.getActionMasked() == MotionEvent.ACTION_DOWN)
        {
            //                log.d("ACTION_DOWN");
            stopAnimatePosition();
            stopScroll();
            isTouching = true;

            if((int)ev.getY() <= headView.getBottom() && (int)ev.getY() >= headView.getTop()
                    && (int)ev.getX() <= headView.getRight() && (int)ev.getX() >= headView.getLeft())
            {
                downInHead = true;
//                log.d(" dispatchTouchEvent downInHead = " + downInHead);
            }
        }

        boolean consumed =  super.dispatchTouchEvent(tempEvent);
//        log.d("after dispatchTouchEvent ev.getY = " + (int)ev.getY() );

        if(ev.getActionMasked() == MotionEvent.ACTION_CANCEL || ev.getActionMasked() == MotionEvent.ACTION_UP)
        {
            //                log.d("ACTION_UP");
            isTouching = false;
            downInHead = false;
            isDragging = false;
            recycleVelocityTracker();
            animateThread.enqueueTaskDelayed(new Runnable() {
                @Override
                public void run() {
                    animatePosition();
                }
            }, 600);
        }

        return downInHead || consumed;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if(!reactOnDragHead)
        {
            return false;
        }
        int pointIndex = -1;
        boolean intercepted = false;
        MotionEvent tempEvent = MotionEvent.obtain(ev);

//        log.d("onInterceptTouchEvent ev.getY = " + (int)ev.getY());
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
                if(downY + getScrollY() <= headView.getBottom() && downY + getScrollY() >= headView.getTop()
                        && downX + getScrollX() <= headView.getRight() && downX + getScrollX() >= headView.getLeft())
                {
                    downInHead = true;
                    log.d("onInterceptTouchEvent downInHead = " + downInHead);
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
                    if(Math.abs(dy) >= touchSlop || Math.abs(disY) >= touchSlop)
                    {
                        isDragging = true;
                    }
                }
                if(isDragging && downInHead)
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
        return intercepted;
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
                if(isDragging && downInHead)
                {
                    int offsetY = computeScrollOffsetY(dy);
                    if(shouldScrollY(offsetY) && offsetY != 0)
                    {
                        offsetChildrenY(offsetY);
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



    private void stopScroll()
    {
        if(!scroller.isFinished())
        {
            scroller.forceFinished(true);
        }
    }

    @Override
    public void computeScroll() {
        if(scroller == null)
        {
            return;
        }else if(!scroller.computeScrollOffset())
        {
            animateThread.enqueueTaskDelayed(new Runnable() {
                @Override
                public void run() {
                    animatePosition();
                }
            }, 600);
            return;
        }else
        {
            int newScrollY = scroller.getCurrY();

            int dy = newScrollY - lastScrollY;
//            log.d("computeScroll, dy = " + dy);
            int offset = computeScrollOffsetY(dy);
//            log.d("computeScroll, offset = " + offset);
            if(offset != 0)
            {
                offsetChildrenY(offset);
//                scrollBy(0, -offset);
                lastScrollY = newScrollY;

            }else if(!shouldScrollY(dy))
            {
                stopScroll();
                return;
            }
            invalidate();

        }
    }

    private void offsetChildrenY(int moveY)
    {
        for(int i = 0; i < getChildCount(); i++)
        {
            View view = getChildAt(i);
            view.offsetTopAndBottom(moveY);
        }
        notifyHeadHeight();

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
        stopNestedScroll();
//        waitFling = false;
        if(parentConsumeNestedScroll)
        {

            parentConsumeNestedScroll = false;
        }

    }

    @Override
    public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        log.d("onNestedScroll");

        if(shouldScrollY(-dyUnconsumed))
        {
            int scrollOffsetY = computeScrollOffsetY(-dyUnconsumed);
            offsetChildrenY(scrollOffsetY);
//            scrollBy(0, -scrollOffsetY);
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
        int[] parentConsumed = new int[2];
        if(parentConsumeNestedScroll)
        {

            dispatchNestedPreScroll(dx, dy, parentConsumed, null);

        }
        if(-dy > 0 && !headFirstScroll)
        {
            consumed[0] = parentConsumed[0];
            consumed[1] = parentConsumed[1];
            return;
        }
        int restY = dy - parentConsumed[1];
        int restX = dx - parentConsumed[0];
        if(shouldScrollY(-restY))
        {
            int scrollOffset = computeScrollOffsetY(-restY);
            offsetChildrenY(scrollOffset);

            consumed[1] = parentConsumed[1]  + (- scrollOffset);
        }
        consumed[0] = parentConsumed[0];



    }

    @Override
    public void scrollTo(@Px int x, @Px int y) {

        int realMoveY = y;
        int headRemain = getHeadRemainSpace();
        if(realMoveY > headView.getBottom() - headRemain)
        {
            realMoveY = headView.getBottom() - headRemain;
        }else if(realMoveY < headView.getTop())
        {
            realMoveY = headView.getTop();
        }
//        log.d("scrollTo, dy = " + realMoveY);
        super.scrollTo(x, realMoveY);

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


        if(Math.abs(velocityX) > Math.abs(velocityY))
        {
            return dispatchNestedPreFling(velocityX, velocityY);

        }else if(dispatchNestedPreFling(velocityX, velocityY)){
            return true;
        }else
        {
            if(!headFirstScroll && -velocityY > 0 && headView.getBottom() <= getVisibleRect().top)
            {
                return false;
            }
            if(((-velocityY > 0 && shouldScrollY(1)) || (-velocityY < 0 && shouldScrollY(-1))))
            {
//                waitFling = !headFirstScroll;
                startFling(0, -(int)velocityY);
//                log.d("onNestedPreFling, start fling");


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
         * @param hiddenHeight:被隐藏的高度。
         * */
        void onHidenSpace(int hiddenHeight);
        /**
         * 获取Head至少要显示的高度，在滑动过程中会保留响应的高度，不会完全隐藏head
         *
         * @return :返回head至少要保留的可见高度，滑动时不会完全隐藏head。
         * */
        int getMinVisibleHeight();
    }

    private class SingleTimedTaskQueue extends Thread{

        private ArrayList<Runnable> task = new ArrayList<>();
        private long delayedTime = 0l;

        private long taskEnqueueTime = 0l;
        private long taskSetTime = 0l;



        public void enqueueTask(Runnable runnable)
        {
            synchronized (task)
            {
                task.clear();
                task.add(runnable);
                delayedTime = 0l;
                taskEnqueueTime = System.currentTimeMillis();
                task.notify();
//                log.d("task notified");
            }
        }

        public void enqueueTaskDelayed(Runnable runnable, long delayedTime)
        {
            synchronized (task)
            {
//                log.d("task locked by " + Thread.currentThread().getName());
                task.clear();
                task.add(runnable);
                this.delayedTime = delayedTime;
                taskEnqueueTime = System.currentTimeMillis();
                task.notify();
//                log.d("task notified");
            }


        }

        @Override
        public void run(){
            synchronized (task){
//                log.d("task locked by " + Thread.currentThread().getName());
                while(true)
                {
                    try{
//                        log.d("task wait first");
                        if(task.size() == 0)
                        {
                            task.wait();
                        }

                        if(delayedTime == 0l)
                        {
                            post(task.get(0));
                            task.clear();
                        }else
                        {
                            while(true)
                            {
                                taskSetTime = taskEnqueueTime;
//                                log.d("task wait second");
                                task.wait(delayedTime);
                                if(taskEnqueueTime == taskSetTime)
                                {
                                    post(task.get(0));
                                    task.clear();
                                    break;
                                }
                            }

                        }

                    }catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }


            }
        }
    }


}
