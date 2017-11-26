package com.zu.customview.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Rect;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.support.v4.view.NestedScrollingChildHelper;
import android.support.v4.view.NestedScrollingParentHelper;
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

    private boolean isOnTouch = false;
    private boolean layouted = false;

    private DragLoadView.OnLoadListener upOnLoadListsner = new DragLoadView.OnLoadListener() {
        @Override
        public void onLoadComplete(boolean success) {

        }

        @Override
        public void onLoadStart() {

        }

        @Override
        public void onLoadCancel() {

        }
    };

    private DragLoadView.OnLoadListener downOnLoadListener = new DragLoadView.OnLoadListener() {
        @Override
        public void onLoadComplete(boolean success) {

        }

        @Override
        public void onLoadStart() {

        }

        @Override
        public void onLoadCancel() {

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
                upDragLoadView.onDragRelease(process);
            }else
            {
                upDragLoadView.onDrag(process);
            }

        }else if(downDragLoadView.getTop() < visibleRect.bottom)
        {
            int offset = Math.abs(downDragLoadView.getTop() - visibleRect.bottom);
            int height = downDragLoadView.getMeasuredHeight();
            float process = offset * 1.0f / height;
            if(release)
            {
                downDragLoadView.onDragRelease(process);
            }else
            {
                downDragLoadView.onDrag(process);
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
        int childHeightSpec = getChildMeasureSpec(heightMeasureSpec, 0, upDragLoadView.getLayoutParams().height);
        int childWidthSpec = getChildMeasureSpec(widthMeasureSpec, 0, upDragLoadView.getLayoutParams().width);
        upDragLoadView.measure(childWidthSpec, childHeightSpec);

        childHeightSpec = MeasureSpec.makeMeasureSpec(selfHeight, MeasureSpec.EXACTLY);
        childWidthSpec = MeasureSpec.makeMeasureSpec(selfWidth, MeasureSpec.EXACTLY);
        contentView.measure(childWidthSpec, childHeightSpec);

        childHeightSpec = getChildMeasureSpec(selfHeight, 0, downDragLoadView.getLayoutParams().height);
        childWidthSpec = getChildMeasureSpec(selfWidth, 0, downDragLoadView.getLayoutParams().width);
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
            int top = rect.top - getChildAt(0).getMeasuredHeight();


            int left = rect.left;
            int right = rect.right;
            for(int i = 0; i < getChildCount(); i++)
            {
                View view = getChildAt(i);
                view.layout(left, top, right, top + view.getMeasuredHeight());
                top += view.getMeasuredHeight();
            }
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
    private boolean downInHead = false;

    private boolean isDragging = false;


    private boolean headFirstScroll = true;
    private boolean reactOnDragHead = true;

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        MotionEvent tempEven = MotionEvent.obtain(ev);

        boolean consumed = super.dispatchTouchEvent(tempEven);


        return consumed;

    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return false;
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

    private int computeAndScrollY(int dy)
    {
        Rect visibleRect = getVisibleRect();
        int offset = 0;
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
            float process = dis * 1.0f / downDragLoadView.getHeight() + 0.1f;
            if(process > 1.0f)
            {
                process = 1.0f;
            }
            offset = (int)(process * dy);
            if(offset + downDragLoadView.getBottom() < visibleRect.bottom)
            {
                offset = visibleRect.bottom - downDragLoadView.getBottom();
            }
        }else
        {
            if(upDragLoadView.getTop() >= visibleRect.top)
            {
                return 0;
            }
            int dis = Math.abs(upDragLoadView.getTop() - visibleRect.top);
            float process = dis * 1.0f / upDragLoadView.getHeight() + 0.1f;
            if(process > 1.0f)
            {
                process = 1.0f;
            }

            offset = (int)(process * dy);
            if(upDragLoadView.getTop() + offset > visibleRect.top)
            {
                offset = visibleRect.top - upDragLoadView.getTop();
            }
        }
        offsetChildrenY(offset);
        return offset;
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








}
