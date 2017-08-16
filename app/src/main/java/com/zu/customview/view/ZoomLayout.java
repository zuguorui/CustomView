package com.zu.customview.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Scroller;

import com.zu.customview.MyLog;
import com.zu.customview.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Objects;
import java.util.TreeMap;

/**
 * Created by zu on 17-3-16.
 */

public class ZoomLayout extends ViewGroup {


    MyLog log = new MyLog("ZoomLayout", true);
    Scroller mScroller = null;
    private float zoomLevel = 2.0f;
    private int minZoomLevel = 1;
    private int maxZoomLevel = 6;
    LinkedList<ViewPackager> onScreenViews = new LinkedList<>();
    LinkedList<ViewPackager> showViews = new LinkedList<>();

    ItemViewManager itemViewManager;


    private int verticalInterval = 3;
    private int horizontalInterval = 3;

    private float heightToWidthRatio = 1.0f;
    private int viewUnVisiableEdge = 300;

    private int touchSlop = 3;

    private Drawable nullDrawable = null;


    private DataSetObserver dataSetObserver = new DataSetObserver() {
        @Override
        public void onChanged() {

        }

        @Override
        public void onInvalidated() {

        }
    };

    public ZoomLayout(Context context) {
        this(context, null);
    }

    public ZoomLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ZoomLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public ZoomLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.ZoomLayout);
        zoomLevel = array.getFloat(R.styleable.ZoomLayout_zoomLevel, zoomLevel);

        minZoomLevel = array.getInteger(R.styleable.ZoomLayout_minZoomLevel, minZoomLevel);
        maxZoomLevel = array.getInteger(R.styleable.ZoomLayout_maxZoomLevel, maxZoomLevel);
        verticalInterval = array.getDimensionPixelSize(R.styleable.ZoomLayout_verticalInterval, verticalInterval);
        horizontalInterval = array.getDimensionPixelSize(R.styleable.ZoomLayout_horizontalInterval, horizontalInterval);
        viewUnVisiableEdge = array.getDimensionPixelSize(R.styleable.ZoomLayout_viewUnVisiableEdge, viewUnVisiableEdge);
        heightToWidthRatio = array.getFloat(R.styleable.ZoomLayout_heightToWidthRatio, heightToWidthRatio);
        array.recycle();
        itemViewManager = new ItemViewManager();
        zoomLevel = setZoomLevel(zoomLevel);
        if(isInEditMode())
        {
            createDebugData();
        }

        mScroller = new Scroller(context);
        mScroller.setFriction(0.01f);
    }

    private void createDebugData()
    {

        BaseAdapter baseAdapter = new BaseAdapter() {
            int count = 100;
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
                if(position >= 0 && position < count)
                {
                    if(convertView == null)
                    {
                        convertView = new ImageView(getContext());
                        convertView.setBackgroundColor(Color.parseColor("#a0a0a0"));
                    }
                    return convertView;
                }else
                {
                    return null;
                }

            }


        };
        setAdapter(baseAdapter);
    }

    public void setAdapter(BaseAdapter adapter)
    {
        itemViewManager.mAdapter = adapter;
        itemViewManager.mAdapter.registerDataSetObserver(dataSetObserver);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int childWidth = (widthSize - getPaddingLeft() - getPaddingRight() - (int)(zoomLevel - 1) * horizontalInterval) / (int)zoomLevel;
        int childHeight =(childWidth <= 0 ? 10 : (int)(childWidth * heightToWidthRatio));

        int totalWidth = widthSize;
        int totalHeight = itemViewManager.mAdapter == null ? heightSize : (itemViewManager.mAdapter.getCount() / (int)zoomLevel * childHeight);
        if (heightSpecMode == MeasureSpec.AT_MOST)
        {
            if(totalHeight > heightSize)
            {
                setMeasuredDimension(totalWidth, heightSize);
            }else
            {
                setMeasuredDimension(totalWidth, totalHeight);
            }
        }else
        {
            setMeasuredDimension(totalWidth, heightSize);
        }

    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if(changed)
        {
            layout(0, null);
        }
    }

    private void layout(final int moveY, final Point zoomCenter)
    {
        scrollChildren(moveY);
        addAndRemoveChildren();
        layoutChildren(zoomCenter, zoomLevel);

    }


    private void scrollChildren(int moveY)
    {
//        log.v("scrollChildren");
//        log.v("scroll moveY = " + moveY);
        if(showViews == null  || showViews.size() == 0)
        {
            return;
        }
        if(moveY == 0)
        {
            return;
        }
        boolean dependBottom = true;
        int realMoveY = moveY;

        if(showViews.getFirst().position == 0)
        {
            View firstView = showViews.getFirst().view;
            if(firstView.getTop() + moveY >= getPaddingTop())
            {
                realMoveY = getPaddingTop() - firstView.getTop();
                dependBottom = false;
            }else
            {
                dependBottom = true;
            }
        }

        if(dependBottom)
        {
            ViewPackager lastView = showViews.getLast();
            if(lastView.position == itemViewManager.mAdapter.getCount() - 1)
            {
                if(lastView.view.getBottom() + moveY < getMeasuredHeight() - getPaddingBottom())
                {
                    if(lastView.view.getBottom() <= getMeasuredHeight() - getPaddingBottom())
                    {
                        if(moveY <= 0)
                        {
                            return;
                        }else
                        {
                            realMoveY = moveY;
                        }
                    }else
                    {
                        realMoveY = getMeasuredHeight() - getPaddingBottom() - lastView.view.getBottom();
                    }

                }
            }
        }


//        log.v("scroll realMoveY = " + realMoveY);

        for(ViewPackager v : showViews)
        {
            int t = v.view.getTop();
            int l = v.view.getLeft();
            int r = v.view.getRight();
            int b = v.view.getBottom();
            v.view.layout(l, t + realMoveY, r, b + realMoveY);

        }





    }

    private boolean addAndRemoveChildren()
    {
//        log.v("addAndRemoveChildren");
        boolean changed = false;
        int width = (getMeasuredWidth() - getPaddingLeft() - getPaddingRight() - (int)(zoomLevel - 1) * horizontalInterval) / (int)zoomLevel;
        int height =(int)(width * heightToWidthRatio);

        /**删除多出来的子view*/
        if(onScreenViews != null && onScreenViews.size() != 0)
        {
//            log.v("删除上面多出来的子view");
            do{
                ViewPackager viewPackager = onScreenViews.getFirst();
                if(viewPackager.view.getBottom() < -viewUnVisiableEdge)
                {
                    itemViewManager.addUnVisiableViewAbove(viewPackager.position, viewPackager);
                    removeView(viewPackager);
                    onScreenViews.removeFirst();

                }else
                {
                    break;
                }
                if(onScreenViews.size() == 0)
                {
                    break;
                }
            }while(true);
        }
        if(onScreenViews != null && onScreenViews.size() != 0)
        {
//            log.v("删除下面多出来的子view");
            do{
                ViewPackager viewPackager = onScreenViews.getLast();
                if(viewPackager.view.getTop() > getMeasuredHeight() + viewUnVisiableEdge)
                {
                    itemViewManager.addUnVisiableViewBlow(viewPackager.position, viewPackager);
                    removeView(viewPackager);
                    onScreenViews.removeLast();
                }else
                {
                    break;
                }
                if(onScreenViews.size() == 0)
                {
                    break;
                }
            }while(true);
        }




//        log.v("add up view");
        boolean hasBeenEmpty = false;
        ViewPackager viewPackager = null;
        int startPosition = 0;
        int freeHeight = viewUnVisiableEdge;
        if(onScreenViews == null || onScreenViews.size() == 0)
        {

            freeHeight = viewUnVisiableEdge;
            startPosition = -1;
            hasBeenEmpty = true;
        }else
        {
            viewPackager = onScreenViews.getFirst();
            freeHeight = viewPackager.view.getTop() - verticalInterval - (-viewUnVisiableEdge);
            startPosition = viewPackager.position - 1;
        }
        if(freeHeight > 0)
        {
            int rows = (int)(freeHeight / (height + verticalInterval) + 1);
            int cols = (int)((getMeasuredWidth() - getPaddingRight() - getPaddingLeft()) / (width + horizontalInterval) + 1);
//            log.v("add up view, row = " + rows + ", col = " + cols);
            for(int i = 0; i < rows * cols; i++)
            {
                int position = startPosition - i;
                if(position < 0)
                {
                    break;
                }
                ViewPackager newView = itemViewManager.getView(position);
                if(newView == null)
                {
                    break;
                }

                onScreenViews.addFirst(newView);
                changed = true;
            }
        }


//        log.v("add below view");
        viewPackager = null;
        freeHeight = getMeasuredHeight() + viewUnVisiableEdge;
        startPosition = 0;
        if(hasBeenEmpty)
        {
            startPosition = 0;
            freeHeight = getMeasuredHeight() + viewUnVisiableEdge;
        }else
        {
            viewPackager = onScreenViews.getLast();
            freeHeight = getMeasuredHeight() + viewUnVisiableEdge - viewPackager.view.getBottom() - verticalInterval;
            startPosition = viewPackager.position + 1;
        }
        if(freeHeight > 0)
        {
            int rows = (int)(freeHeight / (height + verticalInterval) + 1);
            int cols = (int)((getMeasuredWidth() - getPaddingRight() - getPaddingLeft() - (int)(zoomLevel - 1) * horizontalInterval) / (width));
//            log.v("add below view, row = " + rows + ", col = " + cols);
            for(int i = 0; i < rows * cols; i++)
            {
                int position = startPosition + i;
                if(position >= itemViewManager.mAdapter.getCount())
                {
                    break;
                }
                ViewPackager newView = itemViewManager.getView(position);
                if(newView == null)
                {
                    break;
                }
                onScreenViews.addLast(newView);
                changed = true;
            }
        }
//        log.v("onScreenViews.size() = " + onScreenViews.size());
//        log.v("showViews.size() = " + showViews.size());
        return changed;

    }



    private void layoutChildren(Point zoomCenter, float zoomLevel)
    {
//        log.v("layoutChildren");
        if(onScreenViews == null || onScreenViews.size() == 0)
        {
            return;
        }


        /*确定布局的基准线*/

        ViewPackager centerView = null;
        if(zoomCenter == null)
        {
            if(showViews != null && showViews.size() != 0)
            {
                centerView = showViews.getFirst();

            }else
            {
                centerView = null;
            }
        }else {
            if (showViews != null && showViews.size() != 0) {
                for (ViewPackager v : showViews) {
                    if (v.view.getTop() - verticalInterval <= zoomCenter.y && v.view.getBottom() > zoomCenter.y
                            ) {
                        centerView = v;

                        break;
                    }

                }
            } else{
                centerView = null;
            }

            if(centerView == null)
            {
                if(showViews != null && showViews.size() != 0)
                {
                    centerView = showViews.getFirst();
                }
            }
        }

        ArrayList<ViewLayoutParams> params = createLayoutModel(zoomLevel);

        /**
         * offset是布局模板与真正的布局之间的差距，当缩放中心不在子view上或者目前没有子view（即centerView == null）时，
         * offset的值为布局的paddingTop。不必担心会在最后的布局中多加paddingTop，因为除去没有子view的情况，如果有子view的话，
         * 而centerView == null，那么子view的上或下边界肯定已经出现在视图范围内，那么offset会在检测上下边界的过程中重新测量。
         * */
        int offset = 0;
        if(centerView == null)
        {
            /*如果此时没有子view，那么布局模板与真正布局的位置差距就是paddingTop，如果有子view，那么offset根据子view的位置确定。*/
            if(showViews == null || showViews.size() == 0)
            {
                offset = getPaddingTop();
            }else
            {
                offset = 0;
            }

        }else
        {
            for(ViewLayoutParams param : params)
            {
                if(param.position == centerView.position)
                {
                    offset = centerView.view.getTop() - param.top;
                    break;
                }
            }
//            log.v("position = " + centerView.position + ", offset = " + offset);
        }
        int firstOffset = offset;
//        log.v("first offset = " + offset);

        if(params.get(params.size() - 1).position == itemViewManager.mAdapter.getCount() - 1)
        {
            if(params.get(params.size() - 1).bottom + offset <= getMeasuredHeight() - getPaddingBottom())
            {
                offset = getMeasuredHeight() - getPaddingBottom() - params.get(params.size() - 1).bottom;
            }
        }
        if(params.get(0).position == 0)
        {
            if(params.get(0).top + offset >= getPaddingTop())
            {
                offset = getPaddingTop() - params.get(0).top;

            }
        }
        int secondOffset = offset;
//        log.v("second offset = " + offset);
//        if(firstOffset != secondOffset)
//        {
//            log.v("first offset = " + firstOffset + ", second offset = " + secondOffset);
//        }

        ListIterator<ViewPackager> iterator = onScreenViews.listIterator();
        if (params == null || params.size() == 0)
        {
            return;
        }
        int i = 0;
        int widthSpec = MeasureSpec.makeMeasureSpec(params.get(0).getWidth(), MeasureSpec.EXACTLY);
        int heightSpec = MeasureSpec.makeMeasureSpec(params.get(0).getHeight(), MeasureSpec.EXACTLY);
        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();
        while(iterator.hasNext())
        {
            ViewPackager v = iterator.next();
            ViewLayoutParams param = params.get(i);
//            v.view.measure(widthSpec, heightSpec);
            v.view.layout(param.left + paddingLeft, param.top + offset, param.right + paddingLeft, param.bottom + offset);
            addView(v);
            i++;
        }


    }


    public void addView(ViewPackager child) {

        if(!showViews.contains(child))
        {
            super.addView(child.view);
            showViews.add(child);
        }

    }


    public void removeView(ViewPackager child) {

        super.removeView(child.view);
        ListIterator<ViewPackager> iterator = showViews.listIterator();
        while(iterator.hasNext())
        {
            if((ViewPackager)iterator.next() == child)
            {
                iterator.remove();
                break;
            }
        }
    }

    private ArrayList<ViewLayoutParams> createLayoutModel(float zoomLevel)
    {
        if(onScreenViews == null || onScreenViews.size() == 0)
        {
            return null;
        }

        int parentWidth = getMeasuredWidth() - getPaddingLeft() - getPaddingRight();

        int beforeBase = (int)zoomLevel;
        int afterBase = beforeBase + 1;

        int beforeWidth = (parentWidth - ((beforeBase - 1) * horizontalInterval)) / beforeBase;
        int beforeHeight = (int)(beforeWidth * heightToWidthRatio);

        int afterWidth = (parentWidth - ((afterBase - 1) * horizontalInterval)) / afterBase;
        int afterHeight = (int)(afterWidth * heightToWidthRatio);

        float k = zoomLevel - beforeBase;
        int count = onScreenViews.size();
        ArrayList<ViewLayoutParams> layoutModel = new ArrayList<>(count);

        for(ViewPackager v : onScreenViews)
        {
            int top, left, bottom, right, height, width;
            int beforeTop = (v.position / beforeBase) * (beforeHeight + verticalInterval);
            int beforeLeft = (v.position % beforeBase) * (beforeWidth + horizontalInterval);
            int beforeBottom = beforeTop + beforeHeight;
            int beforeRight = beforeLeft + beforeWidth;

            int afterTop = (v.position / afterBase) * (afterHeight + verticalInterval);
            int afterLeft = (v.position % afterBase) * (afterWidth + horizontalInterval);
            int afterBottom = afterTop + afterHeight;
            int afterRight = afterLeft + afterWidth;

            top = beforeTop + (int)((afterTop - beforeTop) * k);
            left = beforeLeft + (int)((afterLeft - beforeLeft) * k);
            right = beforeRight + (int)((afterRight - beforeRight) * k);
            bottom = beforeBottom + (int)((afterBottom - beforeBottom) * k);
            height = bottom - top;
            width = right - left;

            ViewLayoutParams params = new ViewLayoutParams(top, left, bottom, right, v.position);
            layoutModel.add(params);

        }

        return layoutModel;

    }

    int lastY = 0;
    @Override
    public void computeScroll() {
//        super.computeScroll();
        if(mScroller.computeScrollOffset())
        {
            int newY = mScroller.getCurrY();
//            log.v("computeScroll, dy = " + (newY - lastY));

            layout(newY - lastY, null);
            lastY = newY;
            postInvalidate();
        }
    }

    private float setZoomLevel(float zoomLevel)
    {
        if(zoomLevel < minZoomLevel)
        {
            this.zoomLevel = minZoomLevel;
        }else if(zoomLevel > maxZoomLevel)
        {
            this.zoomLevel = maxZoomLevel;
        }else
        {
            this.zoomLevel = zoomLevel;
        }
        return this.zoomLevel;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return true;
    }


    private float newX, newY, oldX, oldY, dx, dy;
    private VelocityTracker velocityTracker = null;
    private ScaleGestureDetector mScaleGestureDetector = null;
    /*设置zoomed是因为即使是双指手势，在之后抬起来也会出现单指的move和up动作导致画面跳动，因此必须使用这个标志位屏蔽缩放之后的滑动事件*/
    private boolean zoomed = false;
    private boolean moved = false;
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int pointCount = event.getPointerCount();
        if(pointCount == 1)
        {
            dealWithSingleGesture(event);
        } else{
            zoomed = true;
            dealWithMultiGesture(event);

        }
        return true;
    }


    private boolean dealWithSingleGesture(MotionEvent event)
    {
//        log.v("single gesture");
//        log.v("single event : " + event.getAction());
        createVelocityTracker(event);
        switch (event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                newX = event.getRawX();
                newY = event.getRawY();
                mScroller.forceFinished(true);
                break;
            case MotionEvent.ACTION_MOVE:
                if(zoomed == false)
                {
                    oldX = newX;
                    oldY = newY;
                    newX = event.getRawX();
                    newY = event.getRawY();
                    dx = newX - oldX;
                    dy = newY - oldY;
                    if(Math.abs(dy) >= touchSlop)
                    {
                        layout((int)dy, null);
                        moved = true;
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                float speed = getVelocityTracker();
                recycleVelovityTracker();
                log.v("ACTION_UP speed = " + (int)speed);
                smoothScroll((int)speed);
                zoomed = false;
                if(moved == false)
                {

                }
                break;
        }
        return true;
    }

    private boolean dealWithMultiGesture(MotionEvent event)
    {
//        log.v("multi gesture");
//        log.v("event : " + event.getActionMasked());
        if(mScaleGestureDetector == null)
        {
            mScaleGestureDetector = new ScaleGestureDetector(getContext(), new MyScaleListener());
        }
        mScaleGestureDetector.onTouchEvent(event);

        return true;
    }

    private void smoothScroll(int pixelPerSecond)
    {
        if (mScroller == null)
        {
            mScroller = new Scroller(getContext());
        }

        lastY = 0;
        log.v("smoothScroll speed = " + pixelPerSecond);
        mScroller.fling(0, 0, 0, pixelPerSecond, 0, 0, -200000, 200000);


    }

    private void animateZoom(final Point center)
    {
        float nextStat = Math.round(zoomLevel);
        ValueAnimator animator = ValueAnimator.ofFloat(zoomLevel, nextStat);
        animator.setDuration(200);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                zoomLevel = setZoomLevel((float)animation.getAnimatedValue());
                layout(0, center);
            }
        });
        animator.start();
    }





    private void createVelocityTracker(MotionEvent event)
    {
        if(velocityTracker == null)
        {
            velocityTracker = VelocityTracker.obtain();

        }
        velocityTracker.addMovement(event);
    }

    private void recycleVelovityTracker()
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
        return velocityTracker.getYVelocity();
    }

    private View measureView(View view)
    {
        /*设置一个UNSPECIFIED的MeasureSpec，使得子view能按照它自然的尺寸去测量，不必受限于该view的尺寸，否则一些view的测量可能会出问题*/
        int unspecifiedWidthMeasureSpec = MeasureSpec.makeMeasureSpec(getMeasuredWidth(), MeasureSpec.UNSPECIFIED);
        int unspecifiedHeightMeasureSpec = MeasureSpec.makeMeasureSpec(getMeasuredHeight(), MeasureSpec.UNSPECIFIED);

        int zoomedChildWidth = (getMeasuredWidth() - getPaddingLeft() - getPaddingRight() - (int)(zoomLevel - 1) * horizontalInterval) / (int)zoomLevel;
        int zoomedChildHeight =(int)(zoomedChildWidth * heightToWidthRatio);

        LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.width = zoomedChildWidth;
        layoutParams.height = zoomedChildHeight;
        view.setLayoutParams(layoutParams);

        int childWidthSpec = getChildMeasureSpec(unspecifiedWidthMeasureSpec, getPaddingLeft() + getPaddingRight(), zoomedChildWidth);
        int childHeightSpec = getChildMeasureSpec(unspecifiedHeightMeasureSpec, getPaddingTop() + getPaddingBottom(), zoomedChildHeight);
        view.measure(childWidthSpec, childHeightSpec);
        return view;
    }

    private class MyScaleListener implements ScaleGestureDetector.OnScaleGestureListener
    {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float scale = detector.getScaleFactor();
            zoomLevel = setZoomLevel(zoomLevel / ( scale));
            Point center = new Point((int)detector.getFocusX(), (int)detector.getFocusY());

            layout(0, center);
            return true;
        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            Point center = new Point((int)detector.getFocusX(), (int)detector.getFocusY());
            animateZoom(center);
        }
    }


    private class ItemViewManager
    {

        public BaseAdapter mAdapter;
        private TreeMap<Integer, ViewPackager> aboveUnVisiableViews = new TreeMap<>();
        private TreeMap<Integer, ViewPackager> blowUnVisiableViews = new TreeMap<>();
        private LinkedList<ViewPackager> trashViews = new LinkedList<>();
        private int trashSize = 20;
        public int UN_VISIABLE_VIEW_COUNT = 10;

        public ViewPackager getView(int position)
        {
            ViewPackager result = null;
            if((result = getViewAbove(position)) != null)
            {
//                log.v("aboveUnVisiableViews.size() = " + aboveUnVisiableViews.size());
                return result;
            }else if((result = getViewBlow(position)) != null)
            {
//                log.v("blowUnVisiableViews.size() = " + blowUnVisiableViews.size());
                return result;
            }else
            {
                result = getViewFromAdapter(position);
            }
            return result;
        }

        public ViewPackager getViewAbove(int position)
        {
            ViewPackager result = aboveUnVisiableViews.get(position);

            if(result != null)
            {
                aboveUnVisiableViews.remove(position);
            }
            return result;
        }

        public ViewPackager getViewBlow(int position)
        {
            ViewPackager result = blowUnVisiableViews.get(position);

            if(result != null)
            {
                blowUnVisiableViews.remove(position);
            }
            return result;

        }

        public void addUnVisiableView(int position, ViewPackager viewPackager)
        {
            if(onScreenViews != null && onScreenViews.size() != 0)
            {
                if(position >= onScreenViews.getLast().position)
                {
                    addUnVisiableViewBlow(position, viewPackager);
                }else
                {
                    addUnVisiableViewAbove(position, viewPackager);
                }
            }else
            {
                addUnVisiableViewAbove(position, viewPackager);
            }

        }

        public void addUnVisiableViewBlow(int position, ViewPackager viewPackager)
        {
//            log.v("blowUnVisiableViews.size() = " + blowUnVisiableViews.size());
            if(blowUnVisiableViews.size() > UN_VISIABLE_VIEW_COUNT)
            {
                Object[] keys =  blowUnVisiableViews.keySet().toArray();

                for(int i = 0; i < blowUnVisiableViews.size() - UN_VISIABLE_VIEW_COUNT; i++)
                {
                    int key = (Integer)keys[keys.length - 1 - i];
                    addTrashView(blowUnVisiableViews.get(key));
                    blowUnVisiableViews.remove(key);
                }
            }
            blowUnVisiableViews.put(position, viewPackager);
        }

        public void addUnVisiableViewAbove(int position, ViewPackager viewPackager)
        {
//            log.v("aboveUnVisiableViews.size() = " + aboveUnVisiableViews.size());
            if(aboveUnVisiableViews.size() > UN_VISIABLE_VIEW_COUNT)
            {
                Object[] keys = aboveUnVisiableViews.keySet().toArray();

                for(int i = 0; i < aboveUnVisiableViews.size() - UN_VISIABLE_VIEW_COUNT; i++)
                {
                    int key = (Integer)keys[i];
                    addTrashView(aboveUnVisiableViews.get(key));

                    aboveUnVisiableViews.remove(key);
                }
            }
            aboveUnVisiableViews.put(position, viewPackager);
        }

        private void addTrashView(ViewPackager child)
        {
//            log.v("trashViews.size() + " + trashViews.size());
            if(trashViews.size() > trashSize)
            {
                trashViews.removeFirst();
            }
            trashViews.addLast(child);
        }

        private ViewPackager getViewFromAdapter(int position)
        {
            if(mAdapter == null)
            {
                return null;
            }
            View result;
            result = mAdapter.getView(position, trashViews.size() == 0 ? null : trashViews.pop().view, ZoomLayout.this);
//            log.v("trashViews.size() + " + trashViews.size());
            if(result == null)
            {
                return null;
            }
            ViewPackager viewPackager = new ViewPackager(position, result);
            return viewPackager;
        }
    }

    private class ViewLayoutParams
    {
        public int top;
        public int left;
        public int bottom;
        public int right;
        public int position;

        public ViewLayoutParams(int top, int left, int bottom, int right,int position) {
            this.top = top;
            this.left = left;
            this.bottom = bottom;
            this.right = right;
            this.position = position;
        }

        public int getHeight()
        {
            return bottom - top;
        }

        public int getWidth()
        {
            return right - left;
        }
    }

    private class ViewPackager
    {
        public int position;
        public View view;

        public ViewPackager(int position, View view)
        {
            this.position = position;
            this.view = view;

        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ViewPackager)) return false;

            ViewPackager that = (ViewPackager) o;

            if (position != that.position) return false;

            return view != null ? view.equals(that.view) : that.view == null;

        }

        @Override
        public int hashCode() {
            int result = position;
            result = 31 * result + (view != null ? view.hashCode() : 0);
            return result;
        }
    }



}
