package com.zu.customview.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.Scroller;

import com.zu.customview.MyLog;
import com.zu.customview.R;

import java.util.Iterator;
import java.util.LinkedList;

/**
 * Created by zu on 17-6-6.
 */

public class ImageSwitchView extends ViewGroup {

    private MyLog log = new MyLog("ImageSwitchView", true);
    private LinkedList<ViewPackager> showViews = new LinkedList<>();
    private ItemManager itemManager = null;
    private float MIN_SCALE = 0.5f;
    private float mScale = 1.0f;
    private int horizontalInterval = 10;

    public ImageSwitchView(Context context) {
        this(context, null);
    }

    public ImageSwitchView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ImageSwitchView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public ImageSwitchView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        itemManager = new ItemManager();
        mScroller = new Scroller(context);
        createDebugView();

    }

    private void createDebugView()
    {
        BaseAdapter adapter = new BaseAdapter() {
            int count = 10;
            @Override
            public int getCount() {
                return count;
            }

            @Override
            public Object getItem(int position) {
                return null;
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if(position < 0 || position >= count)
                {
                    return null;
                }
                if(convertView == null)
                {
                    ImageView i = new ImageView(ImageSwitchView.this.getContext());
                    i.setBackgroundColor(Color.RED);
                    convertView = i;
                }
                return convertView;

            }
        };
        setAdapter(adapter);
    }

    public void setAdapter(Adapter adapter)
    {
        itemManager.mAdapter = adapter;
        requestLayout();
        invalidate();
    }


    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if(itemManager == null || itemManager.mAdapter == null)
        {
            return;
        }
        doLayout(0);
    }

    private void doLayout(int moveX)
    {
        scrollItem(moveX);
        addAndRemoveItem();
    }

    private void scrollItem(int moveX)
    {
        if(showViews == null || showViews.size() == 0)
        {
            return;
        }

        if(itemManager == null || itemManager.mAdapter == null)
        {
            return;
        }

        if(moveX == 0)
        {
            return;
        }

        int realMoveX = moveX;
        Rect rect = getVisibleRect();
        int centerX = rect.centerX();
        int centerY = rect.centerY();
        if(moveX < 0)
        {
            if(showViews.getLast().position == itemManager.mAdapter.getCount() - 1)
            {
                Rect itemRect = getItemLayoutRect(showViews.getLast().view);
                if (itemRect.centerX() + moveX < centerX)
                {
                    realMoveX = centerX - itemRect.centerX();
                }
            }
        }else{
            if(showViews.getFirst().position == 0)
            {
                Rect itemRect = getItemLayoutRect(showViews.getFirst().view);
                if(itemRect.centerX() + moveX > centerX)
                {
                    realMoveX = centerX - itemRect.centerX();
                }
            }
        }
        if(realMoveX == 0)
        {
            return;
        }

        Iterator<ViewPackager> iterator = showViews.iterator();
        while(iterator.hasNext())
        {
            View view = iterator.next().view;
            int l = view.getLeft();
            int r = view.getRight();
            int t = view.getTop();
            int b = view.getBottom();
            view.layout(l + realMoveX, t, r + realMoveX, b);
        }

    }

//    private void zoomItem(Point zoomCenter, float scale)
//    {
//        if(showViews.size() == 0)
//        {
//            return;
//        }
//
//        Iterator<ViewPackager> iterator = showViews.iterator();
//        while(iterator.hasNext())
//        {
//
//        }
//    }

    private void addAndRemoveItem()
    {
        Rect rect = getVisibleRect();
        int itemWidth = (int)(rect.width() * mScale);
        int itemHeight = (int)(rect.height() * mScale);
        if(showViews.size() != 0)
        {
            Iterator<ViewPackager> iterator = showViews.iterator();
            while(iterator.hasNext())
            {
                ViewPackager vp = iterator.next();
                if(vp.view.getRight() < rect.left)
                {
                    if(vp.view instanceof ImageCheckView)
                    {
                        ((ImageCheckView) vp.view).resetScale();
                    }
                    iterator.remove();
                    itemManager.addUnvisibleView(vp);
                    removeView(vp.view);
                }else
                {
                    break;
                }
            }
        }

        if(showViews.size() != 0)
        {
            Iterator<ViewPackager> iterator = showViews.descendingIterator();
            while(iterator.hasNext())
            {
                ViewPackager vp = iterator.next();
                if(vp.view.getLeft() > rect.right)
                {
                    if(vp.view instanceof ImageCheckView)
                    {
                        ((ImageCheckView) vp.view).resetScale();
                    }
                    iterator.remove();
                    itemManager.addUnvisibleView(vp);
                    removeView(vp.view);
                }else
                {
                    break;
                }
            }
        }
        /*add fore view*/
        int leftEdge = 0;
        int startPosition;
        if(showViews.size() == 0)
        {
            leftEdge = rect.centerX() + (itemWidth / 2);
            startPosition = 0;
        }else
        {
            leftEdge = showViews.getFirst().view.getLeft() - horizontalInterval;
            startPosition = showViews.getFirst().position - 1;
        }

        while(leftEdge >= rect.left && startPosition >= 0)
        {
            ViewPackager viewPackager = itemManager.getViewPackager(startPosition);
            if(viewPackager == null)
            {
                break;
            }
            measureItem(viewPackager.view, itemWidth, itemHeight);
            int top = rect.top;
            int left = leftEdge - itemWidth;
            int bottom = rect.bottom;
            int right = leftEdge;
            viewPackager.view.layout(left, top, right, bottom);
            showViews.addFirst(viewPackager);
            addView(viewPackager.view);
            startPosition--;
            leftEdge -= itemWidth + horizontalInterval;
        }

        int rightEdge;
        int totalCount = itemManager.mAdapter.getCount();
        if(showViews.size() == 0)
        {
            rightEdge = rect.centerX() - (itemWidth / 2);
            startPosition = 0;
        }else
        {
            rightEdge = showViews.getLast().view.getRight() + horizontalInterval;
            startPosition = showViews.getLast().position + 1;
        }

        while(rightEdge <= rect.right && startPosition < totalCount)
        {
            ViewPackager viewPackager = itemManager.getViewPackager(startPosition);
            if(viewPackager == null)
            {
                break;
            }
            measureItem(viewPackager.view, itemWidth, itemHeight);
            int top = rect.top;
            int left = rightEdge;
            int bottom = rect.bottom;
            int right = rightEdge + itemWidth;
            viewPackager.view.layout(left, top, right, bottom);
            showViews.addLast(viewPackager);
            addView(viewPackager.view);
            startPosition++;
            rightEdge += itemWidth + horizontalInterval;
        }
    }

    private Rect getLayoutRect()
    {
        int top = 0;
        int bottom = getHeight();
        int left = 0;
        int right = getWidth();
        Rect rect = new Rect(left, top, right, bottom);
        return rect;
    }

    private Rect getVisibleRect()
    {
        int top = getPaddingTop();
        int bottom = getHeight() - getPaddingBottom();
        int left = getPaddingLeft();
        int right = getWidth() - getPaddingRight();
        Rect rect = new Rect(left, top, right, bottom);
        return rect;
    }

    private Rect getItemLayoutRect(View view)
    {
        int top = view.getTop();
        int bottom = view.getBottom();
        int right = view.getRight();
        int left = view.getLeft();
        Rect rect = new Rect(left, top, right, bottom);
        return rect;
    }

    private void measureItem(View view, int width, int height)
    {
        int heightSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
        int widthSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);
        view.measure(widthSpec, heightSpec);
    }

    private int newX = 0, newY = 0, oldX = 0, oldY = 0, dx = 0, dy = 0, downX = 0, downY = 0;
    private boolean handleEventQueue = false;
    private int touchSlop = 3;
    private VelocityTracker mVelocityTracker = null;
    private float speedGate = 300;
    private Scroller mScroller = null;
    private int lastX;
    private boolean firstHandleEvent = true;
    private long lastId = 0;

    @Override
    public void computeScroll() {
        super.computeScroll();
        boolean more = mScroller.computeScrollOffset();
//        log.v("mScroller.computeScrollOffset() = " + more);
        if(more)
        {

            final int currX = mScroller.getCurrX();
//            log.v("computeScroll, currX = " + currX);
            doLayout(currX - lastX);
            lastX = currX;
            postInvalidate();

        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        boolean consumed = false;
        consumed = super.dispatchTouchEvent(ev);
        if(!consumed)
        {
            handleEventQueue = true;
            consumed = super.dispatchTouchEvent(ev);
        }
        return consumed;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getActionMasked())
        {
            case MotionEvent.ACTION_DOWN:
                log.v("ACTION_DOWN");

                if(mScroller != null)
                {
                    mScroller.forceFinished(true);
                }
                break;
            default:
                break;
        }
//        if(showViews != null && showViews.size() != 0 && handleEventQueue)
//        {
//            Iterator<ViewPackager> iterator = showViews.iterator();
//            Rect rect = getVisibleRect();
//            int centerX = rect.centerX();
//            while(iterator.hasNext())
//            {
//                ViewPackager temp = iterator.next();
//                Rect itemRect = getItemLayoutRect(temp.view);
//                if(itemRect.centerX() == centerX)
//                {
//                    firstHandleEvent = true;
//                    handleEventQueue = false;
//                    return false;
//                }
//            }
//        }
        return handleEventQueue;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
//        if(!isClickable() && !isLongClickable())
//        {
//            return false;
//        }
        boolean consumed = true;
        int count = event.getPointerCount();
        createOrInitVelocityTracker(event);
        switch (event.getActionMasked())
        {
            case MotionEvent.ACTION_DOWN:
                log.v("ACTION_DOWN");
                newX = (int)event.getX();
                newY = (int)event.getY();

                if(mScroller != null)
                {
                    mScroller.forceFinished(true);
                }
                break;

            case MotionEvent.ACTION_MOVE:
//                log.v("ACTION_MOVE");
                long id = event.getPointerId(0);
                if(firstHandleEvent || id != lastId)
                {
                    newX = (int)event.getX();
                    newY = (int)event.getY();
                    firstHandleEvent = false;
                    lastId = id;
                }
                oldX = newX;
                oldY = newY;

                newX = (int)event.getX();
                newY = (int)event.getY();
                dx = newX - oldX;
                dy = newY - oldY;
                if(Math.abs(dx) >= touchSlop)
                {

                    doLayout(dx);

                }
                break;
            case MotionEvent.ACTION_UP:
                log.v("ACTION_UP");
                firstHandleEvent = true;
                handleEventQueue = false;
                float speed = getXVelocity();
                recycleVelocityTracker();
                smoothScroll(speed);
                break;
            default:
                break;
        }
        return consumed;

    }

    private void smoothScroll(float speedPerSecond)
    {
        if(showViews == null || showViews.size() == 0)
        {
            return;
        }
        if(mScroller == null)
        {
            mScroller = new Scroller(getContext());
        }

        Rect rect = getVisibleRect();
        int centerX = rect.centerX();
        ViewPackager next = null;
        if(Math.abs(speedPerSecond) >= speedGate)
        {
            if(speedPerSecond > 0.0f)
            {
                Iterator<ViewPackager> iterator = showViews.iterator();
                while(iterator.hasNext())
                {
                    ViewPackager temp = iterator.next();
                    Rect itemRect = getItemLayoutRect(temp.view);
                    if(itemRect.right + horizontalInterval > rect.left)
                    {
                        next = temp;
                        break;
                    }
                }
            }else
            {
                Iterator<ViewPackager> iterator = showViews.descendingIterator();
                while(iterator.hasNext())
                {
                    ViewPackager temp = iterator.next();
                    Rect itemRect = getItemLayoutRect(temp.view);
                    if(itemRect.left < rect.right)
                    {
                        next = temp;
                        break;
                    }
                }
            }
        }else
        {
            Iterator<ViewPackager> iterator = showViews.iterator();
            while(iterator.hasNext())
            {
                ViewPackager temp = iterator.next();
                Rect itemRect = getItemLayoutRect(temp.view);
                if(itemRect.left <= rect.centerX() && itemRect.right + horizontalInterval >= rect.centerX())
                {
                    next = temp;
                    break;
                }
            }
        }

        if(next == null)
        {
            return;
        }
        Rect itemRect = getItemLayoutRect(next.view);
        int offset = rect.centerX() - itemRect.centerX();
        log.v("offset = " + offset);
        lastX = 0;
        mScroller.startScroll(0, 0, offset, 0);
//        mScroller.fling(0, 0, (int)speedPerSecond, 0, Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE);
        postInvalidate();
    }

    private void createOrInitVelocityTracker(MotionEvent event)
    {
        if(mVelocityTracker == null)
        {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);
    }

    private void recycleVelocityTracker()
    {
        if(mVelocityTracker != null)
        {
            mVelocityTracker.recycle();
        }
        mVelocityTracker = null;
    }

    private float getXVelocity()
    {
        if(mVelocityTracker == null)
        {
            return 0;
        }
        mVelocityTracker.computeCurrentVelocity(1000);
        return mVelocityTracker.getXVelocity();
    }

    private class ItemManager
    {
        public Adapter mAdapter = null;
        private LinkedList<ViewPackager> foreViews = new LinkedList<>();
        private LinkedList<ViewPackager> backViews = new LinkedList<>();
        private int trashSize = 2;
        public void addUnvisibleView(ViewPackager viewPackager)
        {
            if(viewPackager == null)
            {
                return;
            }
            int position = viewPackager.position;
            if(showViews == null || showViews.size() == 0)
            {
                foreViews.addLast(viewPackager);

            }else
            {
                if(position >= showViews.getLast().position)
                {
                    backViews.addFirst(viewPackager);
                }else
                {
                    foreViews.addLast(viewPackager);
                }
            }
            if(foreViews.size() > trashSize)
            {
                foreViews.removeFirst();
            }
            if(backViews.size() > trashSize)
            {
                backViews.removeLast();
            }
        }

        public ViewPackager getViewPackager(int position)
        {
            ViewPackager result = null;
            if(showViews == null || showViews.size() == 0)
            {
                result = getFromFore(position);
                if(result == null)
                {
                    result = getFromBack(position);
                }
                if(result == null)
                {
                    result = getFromAdapter(position);
                }
            }else
            {
                if(position >= showViews.getLast().position)
                {
                    result = getFromBack(position);
                }else if(position <= showViews.getFirst().position)
                {
                    result = getFromFore(position);
                }

                if(result == null)
                {
                    result = getFromAdapter(position);
                }
            }
            return result;
        }

        private ViewPackager getFromFore(int position)
        {
            if(foreViews == null || foreViews.size() == 0)
            {
                return null;
            }
            ViewPackager result = null;
            Iterator<ViewPackager> iterator = foreViews.descendingIterator();
            while(iterator.hasNext())
            {
                ViewPackager temp = iterator.next();
                if(temp.position == position)
                {
                    result = temp;
                    iterator.remove();
                }
            }
            return result;
        }

        private ViewPackager getFromBack(int position)
        {
            if(backViews == null || backViews.size() == 0)
            {
                return null;
            }
            ViewPackager result = null;
            Iterator<ViewPackager> iterator = backViews.iterator();
            while(iterator.hasNext())
            {
                ViewPackager temp = iterator.next();
                if(temp.position == position)
                {
                    result = temp;
                    iterator.remove();
                }
            }
            return result;
        }

        private ViewPackager getFromAdapter(int position)
        {
            if(mAdapter == null)
            {
                return null;
            }
            ViewPackager trash = null;
            ViewPackager result = null;
            if(showViews != null && showViews.size() != 0)
            {
                if(position <= showViews.getFirst().position)
                {
                    if(foreViews.size() != 0)
                    {
                        trash = foreViews.removeFirst();
                    }
                }else
                {
                    if(backViews.size() != 0)
                    {
                        trash = backViews.removeLast();
                    }
                }
            }

            View view = mAdapter.getView(position, trash == null ? null : trash.view, ImageSwitchView.this);
            if(view != null)
            {
                result = new ViewPackager(position, mAdapter.getItemId(position), view);
            }
            return result;
        }


    }

    private class ViewPackager{
        public int position;
        public long id;
        public View view;

        public ViewPackager(int position, long id, View view) {
            this.position = position;
            this.id = id;
            this.view = view;
        }

        public ViewPackager()
        {

        }
    }
}
