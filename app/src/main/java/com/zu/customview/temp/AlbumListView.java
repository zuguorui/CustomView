package com.zu.customview.temp;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Checkable;
import android.widget.ImageView;
import android.widget.Scroller;
import android.widget.TextView;


import com.zu.customview.MyLog;
import com.zu.customview.R;
import com.zu.customview.view.AlbumListView.CheckableItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.TreeMap;

/**
 * Created by zu on 17-5-12.
 */

/**
 *
 * */
public class AlbumListView extends ViewGroup {

    public enum VIEW_TYPE {ZOOM, UNZOOM}

    private MyLog log = new MyLog("AlbumListView", true);
    Scroller mScroller = null;
    /**
     * zoomLevel, means how many elements in one row. this val will only be accessed by {@link #doLayout(int, Point, float)}.
     * Other methods who want to change zoom level can first change {@link #tempZoomLevel} and then re-layout by calling
     * {@link #doLayout(int, Point, float)} or {@link #requestLayout()}.
     * */
    private float mZoomLevel = 2.0f;


    private float tempZoomLevel = mZoomLevel;
    private int minZoomLevel = 1;
    private int maxZoomLevel = 6;

    LinkedList<ViewPackager> showViews = new LinkedList<>();
    ItemViewManager itemViewManager;

    private int verticalInterval = 3;
    private int horizontalInterval = 3;

    private int viewDeleteEdge = 3000;
    private int viewAddEdge = 1000;

    private float heightToWidthRatio = 1.0f;

    private int touchSlop = 5;
    private int moveSlop = 5;
    private int initPosition = 0;

    private boolean enableChildCache = true;
    private DataSetObserver dataSetObserver = new DataSetObserver() {
        @Override
        public void onChanged() {
            dataSetChanged();
        }

        @Override
        public void onInvalidated() {
            dataInvalidated();
        }
    };

    public AlbumListView(Context context) {
        this(context, null);
    }

    public AlbumListView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AlbumListView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public AlbumListView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.AlbumListView);
        mZoomLevel = array.getFloat(R.styleable.AlbumListView_zoomLevel, mZoomLevel);
        tempZoomLevel = mZoomLevel;
        minZoomLevel = array.getInteger(R.styleable.AlbumListView_minZoomLevel, minZoomLevel);
        maxZoomLevel = array.getInteger(R.styleable.AlbumListView_maxZoomLevel, maxZoomLevel);
        verticalInterval = array.getDimensionPixelSize(R.styleable.AlbumListView_verticalInterval, verticalInterval);
        horizontalInterval = array.getDimensionPixelSize(R.styleable.AlbumListView_horizontalInterval, horizontalInterval);

        heightToWidthRatio = array.getFloat(R.styleable.AlbumListView_heightToWidthRatio, heightToWidthRatio);
        array.recycle();
        itemViewManager = new ItemViewManager();
        mScroller = new Scroller(context);
        mZoomLevel = computeZoomLevel(mZoomLevel);
        if (isInEditMode()) {
            createDebugData();
        }



    }

    private void createDebugData()
    {
        final TextView textView = new TextView(getContext());
        textView.setText("xxxxxxxx");

        ImageView imageView = new ImageView(getContext());
        imageView.setBackgroundColor(Color.parseColor("#bbbbbb"));

        final int childCountInGroup = 5;
        final int groupCount = 6;

        AlbumListViewAdapter adapter = new AlbumListViewAdapter() {
            @Override
            public int getGroupCount() {
                return groupCount;
            }

            @Override
            public int getChildrenCount(long groupId) {
                return childCountInGroup;
            }

            @Override
            public long getGroupId(long childId) {
                return (childId & 0xffffffff00000000l) >> 32;
            }

            @Override
            public int getChildPositionInGroup(long childId) {
                return (int)(childId & 0x00000000ffffffffl);

            }

            @Override
            public int getPosition(long id) {
                int groupPosition = (int)(getGroupId(id));
                int childPosition = (int)(getChildPositionInGroup(id));

                return groupPosition * childCountInGroup + childPosition;
            }

            @Override
            public long getChildIdInGroup(long groupId, int childPositionInGroup) {
                return (groupId << 32) | childPositionInGroup;
            }

            @Override
            public AlbumListView.VIEW_TYPE getViewType(long id) {
                int childId = getChildPositionInGroup(id);
                if(childId == 0)
                {
                    return AlbumListView.VIEW_TYPE.UNZOOM;
                }else
                {
                    return AlbumListView.VIEW_TYPE.ZOOM;
                }
            }

            @Override
            public int getCount() {
                return groupCount * childCountInGroup;
            }

            @Override
            public Object getItem(int position) {
                return null;
            }

            @Override
            public long getItemId(int position) {

                long group = position / childCountInGroup;
                long child = position % childCountInGroup;
                return (group << 32) | child;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                int group = position / childCountInGroup;
                int child = position % childCountInGroup;
                if(child == 0)
                {
                    if(convertView != null && convertView instanceof TextView)
                    {
                        ((TextView) convertView).setText("position: " + position);
                        return convertView;
                    }else
                    {
                        TextView t = new TextView(getContext());
                        t.setText("position: " + position);
                        convertView = t;
                    }
                }else
                {
                    if(convertView != null && convertView instanceof ImageView)
                    {

                        return convertView;
                    }else
                    {
                        ImageView i = new ImageView(getContext());
                        i.setBackgroundColor(Color.parseColor("#aaaaaa"));
                        convertView = i;

                    }
                }
                return convertView;
            }
        };

        setAdapter(adapter);
    }

    public void dataInvalidated()
    {
        removeAllViews();
        showViews.clear();
        itemViewManager.invalidated();
    }

    public void dataSetChanged()
    {
        initPosition = getOnScreenFirstItemPosition();
        itemViewManager.dataSetChanged();
        showViews.clear();
        removeAllViews();

        postInvalidate();
    }



    public void setAdapter(AlbumListViewAdapter adapter)
    {
        itemViewManager.mAdapter = adapter;
        itemViewManager.mAdapter.registerDataSetObserver(dataSetObserver);
        requestLayout();
        invalidate();
    }

    private float computeZoomLevel(float zoomLevel)
    {
        if(zoomLevel < minZoomLevel)
        {
            return minZoomLevel;
        }else if(zoomLevel > maxZoomLevel)
        {
            return maxZoomLevel;
        }else
        {
            return zoomLevel;
        }

    }

    public float getZoomLevel()
    {
        return mZoomLevel;
    }

    public void setZoomLevel(int zoomLevel)
    {
        tempZoomLevel = computeZoomLevel(zoomLevel);
//        postInvalidate();
        requestLayout();

    }

    public int getItemMinWidth()
    {
        int width = getItemWidth(maxZoomLevel);
        return width;

    }

    public int getItemMinHeight()
    {

        int height = getItemHeight(maxZoomLevel);
        return height;
    }

    public int getItemMaxWidth()
    {
        int width = getItemWidth(minZoomLevel);
        return width;
    }

    public int getItemMaxHeight()
    {
        int height = getItemHeight(minZoomLevel);
        return height;
    }

    public void setHeightToWidthRatio(float ratio)
    {
        heightToWidthRatio = ratio;
        requestLayout();
    }

    public void setItemMaxWidth(int maxWidth)
    {
        int availWidth = getMeasuredWidth() - getPaddingLeft() - getPaddingRight() - horizontalInterval;
        float temp = ((float)availWidth) / (maxWidth + horizontalInterval);
        int count = Math.round(temp);
        minZoomLevel = count;
        tempZoomLevel = computeZoomLevel(mZoomLevel);
        requestLayout();
    }

    public void setItemMinWidth(int minWidth)
    {
        int availWidth = getMeasuredWidth() - getPaddingLeft() - getPaddingRight() - horizontalInterval;
        float temp = ((float)availWidth) / (minWidth+ horizontalInterval);
        int count = Math.round(temp);
        maxZoomLevel = count;
        tempZoomLevel = computeZoomLevel(mZoomLevel);
        requestLayout();
    }

    public void setMaxZoomLevel(int maxZoomLevel)
    {
        this.maxZoomLevel = maxZoomLevel;
        tempZoomLevel = computeZoomLevel(mZoomLevel);
        requestLayout();
    }

    public int getMaxZoomLevel()
    {
        return maxZoomLevel;
    }

    public int getMinZoomLevel()
    {
        return minZoomLevel;
    }

    public void setMinZoomLevel(int minZoomLevel)
    {
        this.minZoomLevel = minZoomLevel;
        tempZoomLevel = computeZoomLevel(mZoomLevel);
        requestLayout();
    }
    public void addCheckedItems(int[] position)
    {
        if(!MULTI_CHECK_MODE_FLAG)
        {
            return;
        }
        HashSet<Integer> pos = new HashSet<>(position.length);
        for(int i = 0; i < position.length; i++)
        {
            pos.add(position[i]);
        }
        for(ViewPackager v : showViews)
        {
            if(pos.contains(v.position))
            {
                if(v.view instanceof Checkable)
                {
                    ((Checkable) v.view).setChecked(true);
                }else
                {
                    pos.remove(v.position);
                }
            }
        }
        checkedItems.addAll(pos);
    }

    public void removeCheckedItems(int[] position)
    {
        if(!MULTI_CHECK_MODE_FLAG)
        {
            return;
        }
        HashSet<Integer> pos = new HashSet<>(position.length);
        for(int i = 0; i < position.length; i++)
        {
            pos.add(position[i]);
        }
        for(ViewPackager v : showViews)
        {
            if(pos.contains(v.position))
            {
                if(v.view instanceof Checkable)
                {
                    ((Checkable) v.view).setChecked(false);
                }else
                {
                    pos.remove(v.position);
                }
            }
        }
        checkedItems.removeAll(pos);
    }

    public void setCheckedItems(int[] position)
    {
        if(!MULTI_CHECK_MODE_FLAG)
        {
            return;
        }
        final HashSet<Integer> pos = new HashSet<>(position.length);
        for(int i = 0; i < position.length; i++)
        {
            pos.add(position[i]);
        }

        for(ViewPackager v : showViews)
        {
            if(pos.contains(v.position))
            {
                if(v.view instanceof CheckableItem)
                {
                    ((Checkable) v.view).setChecked(true);
                    postInvalidate();
                }else
                {
                    pos.remove(v.position);
                }
            }
        }
        checkedItems = pos;
        notifyOnMultiCheckListeners();
    }

    public void emptyCheckedItems()
    {
//        if(!MULTI_CHECK_MODE_FLAG)
//        {
//            return;
//        }

        for(ViewPackager v : showViews)
        {
            if(v.view instanceof Checkable)
            {
                ((Checkable) v.view).setChecked(false);
            }
        }
        checkedItems.clear();
        notifyOnMultiCheckListeners();
    }

    public int[] getCheckedItems()
    {
        int[] pos = new int[checkedItems.size()];
        int i = 0;
        for(int k : checkedItems)
        {
            pos[i] = k;
        }
        return pos;
    }

    public void setMultiCheckMode(final boolean mode)
    {
        if(MULTI_CHECK_MODE_FLAG == mode)
        {
            return;
        }
        MULTI_CHECK_MODE_FLAG = mode;

        for(ViewPackager v : showViews)
        {
            if(v.view instanceof CheckableItem)
            {
                ((CheckableItem)v.view).setCheckable(mode);
                ((CheckableItem)v.view).setChecked(false);

            }
        }
//        post(new Runnable() {
//            @Override
//            public void run() {
//
//            }
//        });
    }

    public boolean isMultiCheckMode()
    {
        return MULTI_CHECK_MODE_FLAG;
    }

    private int getItemWidth(float zoomLevel)
    {
        int width = (getMeasuredWidth() - getPaddingLeft() - getPaddingRight() - (int)(zoomLevel - 1) * horizontalInterval) / (int)zoomLevel;
        return width;

    }

    private int getItemHeight(float zoomLevel)
    {
        int height =(int)(getItemWidth(zoomLevel) * heightToWidthRatio);
        return height;
    }

    public void setPositionShow(int position)
    {
        if(itemViewManager == null || itemViewManager.mAdapter == null)
        {
            return;
        }
        if(position >= itemViewManager.mAdapter.getCount())
        {
            return;
        }
        initPosition = position;
        for(ViewPackager v : showViews)
        {
            removeView(v.view);
            itemViewManager.addUnVisibleView(v.position, v);
        }
        showViews.clear();
        postInvalidate();
    }
    public int getOnScreenFirstItemPosition()
    {
        if(showViews == null || showViews.size() == 0)
        {
            return  0;
        }
        ListIterator<ViewPackager> iterator = showViews.listIterator();
        int position = 0;

        while(iterator.hasNext())
        {
            ViewPackager v = iterator.next();
            if(v.view.getBottom() >= getPaddingTop())
            {
                position = v.position;
                break;
            }

        }
        return position;
    }

    public int getOnScreenLastItemPosition()
    {
        if(showViews == null || showViews.size() == 0)
        {
            return  -1;
        }
        Iterator<ViewPackager> iterator = showViews.descendingIterator();
        int position = -1;

        while(iterator.hasNext())
        {
            ViewPackager v = iterator.next();
            if(v.view.getTop() <= getMeasuredHeight() - getPaddingBottom())
            {
                position = v.position;
                break;
            }
        }
        return position;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);

        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        if(heightMode == MeasureSpec.EXACTLY)
        {
            setMeasuredDimension(widthSize, heightSize);
        }else
        {
            if(showViews != null && showViews.size() != 0)
            {
                int top = showViews.getFirst().view.getTop();
                int bottom = showViews.getLast().view.getBottom();
                int height = bottom - top;
                height = height >= getMinimumHeight() ? height : getMinimumHeight();
                setMeasuredDimension(widthSize, height);
            }else
            {
                setMeasuredDimension(widthSize, getMinimumHeight());
            }
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        doLayout(0, null, tempZoomLevel);
    }

    private void setupItemState(ViewPackager v)
    {
        if(v == null)
        {
            return;
        }


        if(MULTI_CHECK_MODE_FLAG)
        {
            if(v.view instanceof CheckableItem)
            {
                ((CheckableItem)v.view).setCheckable(true);
                ((CheckableItem)v.view).setChecked(checkedItems.contains(v.position));
            }

        }else
        {
            if(v.view instanceof CheckableItem)
            {
                ((CheckableItem)v.view).setCheckable(false);
            }
        }

        v.view.setDrawingCacheEnabled(enableChildCache);
    }

    private void doLayout(int moveY, Point zoomCenter, float zoomLevel)
    {
        if(itemViewManager == null || itemViewManager.mAdapter == null)
        {
            return;
        }
        log.v("doLayout");
        float tempZoomLevel = computeZoomLevel(zoomLevel);
        long startTime = System.currentTimeMillis();
        scrollChildren(moveY);
        long endTime = System.currentTimeMillis();
        log.d("scroll children cost " + (endTime - startTime) + "ms");
        LinkedList<ViewPackager> aboveViews = new LinkedList<>();
        LinkedList<ViewPackager> belowViews = new LinkedList<>();
        startTime = System.currentTimeMillis();
        addAndRemoveChildren(tempZoomLevel, aboveViews, belowViews);
        endTime = System.currentTimeMillis();
        log.d("add and remove children cost " + (endTime - startTime) + "ms");
        if(showViews.size() != 0)
        {
            log.v("before layout showViews.size: " + showViews.size() + ", firstPosition: " + showViews.getFirst().position
                    + ", lastPosition: " + showViews.getLast().position);
        }else
        {
            log.v("before layout showViews.size: 0");
        }
        startTime = System.currentTimeMillis();
        layoutChildren(zoomCenter, tempZoomLevel, aboveViews, belowViews);
        endTime = System.currentTimeMillis();
        log.d("layout children cost " + (endTime - startTime) + "ms");
        if(zoomLevel - mZoomLevel <= 0.0000f)
        {
            setChildrenDrawingCacheEnabled(true);
            enableChildCache = true;
        }else
        {
            setChildrenDrawingCacheEnabled(false);
            enableChildCache = false;
        }


//        if(aboveViews.size() != 0)
//        {
//            log.v("aboveViews.size: " + aboveViews.size() + ", firstPosition: " + aboveViews.getFirst().position
//                    + ", lastPosition: " + aboveViews.getLast().position);
//        }else
//        {
//            log.v("aboveViews.size: 0");
//        }
//        if(belowViews.size() != 0)
//        {
//            log.v("belowViews.size: " + belowViews.size() + ", firstPosition: " + belowViews.getFirst().position
//                    + ", lastPosition: " + belowViews.getLast().position);
//        }else
//        {
//            log.v("belowViews.size: 0");
//        }
//        if(showViews.size() != 0)
//        {
//            log.v("after layout showViews.size: " + showViews.size() + ", firstPosition: " + showViews.getFirst().position
//                    + ", lastPosition: " + showViews.getLast().position);
//        }else
//        {
//            log.v("after layout showViews.size: 0");
//        }

        mZoomLevel = tempZoomLevel;


    }

    private boolean checkLayoutComplete(float zoomLevel)
    {
        if(itemViewManager == null || itemViewManager.mAdapter == null)
        {
            return true;
        }
        if(showViews == null || showViews.size() == 0)
        {
            return true;
        }
        AlbumListViewAdapter adapter = itemViewManager.mAdapter;
        int width = getItemWidth(zoomLevel);
        int height = getItemHeight(zoomLevel);

        ViewPackager first = showViews.getFirst();
        if(first.view.getLeft() - getPaddingLeft() >= width + horizontalInterval
                && first.view.getBottom() >= getPaddingTop())
        {
            return false;
        }

        ViewPackager last = showViews.getLast();
        if(last.view.getTop() <= getMeasuredHeight() - getPaddingBottom()
                && getMeasuredWidth() - getPaddingRight() - last.view.getRight() >= width + horizontalInterval)
        {
            if(adapter.getViewType(last.id) == VIEW_TYPE.UNZOOM)
            {
                return true;
            }else if(adapter.getChildPositionInGroup(last.id) == adapter.getChildrenCount(adapter.getGroupId(last.id)))
            {
                return true;
            }else if(last.position == adapter.getCount() - 1)
            {
                return true;
            }else
            {
                return false;
            }
        }
        return true;
    }

    private void scrollChildren(int moveY)
    {

        if(showViews == null  || showViews.size() == 0)
        {
            return;
        }
        if(moveY == 0)
        {
            return;
        }

        int realMoveY = moveY;

        ViewPackager lastView = showViews.getLast();
        if(lastView.position == itemViewManager.mAdapter.getCount() - 1)
        {
            if(lastView.view.getBottom() + realMoveY < getMeasuredHeight() - getPaddingBottom())
            {
                realMoveY = getMeasuredHeight() - getPaddingBottom() - lastView.view.getBottom();
            }
        }

        if(showViews.getFirst().position == 0)
        {
            View firstView = showViews.getFirst().view;
            if(firstView.getTop() + realMoveY >= getPaddingTop())
            {
                realMoveY = getPaddingTop() - firstView.getTop();

            }
        }
        if(realMoveY == 0)
        {
            return;
        }

//        log.v("moveY = " + moveY + ", realMoveY = " + realMoveY);

        for(ViewPackager v : showViews)
        {
            int t = v.view.getTop();
            int l = v.view.getLeft();
            int r = v.view.getRight();
            int b = v.view.getBottom();
            v.view.layout(l, t + realMoveY, r, b + realMoveY);
        }
    }

    private void addAndRemoveChildren(float zoomLevel, LinkedList<ViewPackager> aboveViews, LinkedList<ViewPackager> belowViews)
    {
        if(itemViewManager == null || itemViewManager.mAdapter == null)
        {
            return;
        }

        AlbumListViewAdapter adapter = itemViewManager.mAdapter;
        int width = (getMeasuredWidth() - getPaddingLeft() - getPaddingRight() - (int)(zoomLevel - 1) * horizontalInterval) / (int)zoomLevel;
        int height =(int)(width * heightToWidthRatio);

        viewAddEdge = height + verticalInterval;
        viewDeleteEdge = viewAddEdge;

        int standEdge = (viewDeleteEdge + viewAddEdge) / 2;

        /**删除多出来的子view*/
        if(showViews != null && showViews.size() != 0)
        {
//            log.v("删除上面多出来的子view");
            if(showViews.getFirst().view.getBottom() < getPaddingTop() - viewDeleteEdge)
            {
                int mTop = getPaddingTop() - standEdge;
                do{
                    ViewPackager viewPackager = showViews.getFirst();
                    if(viewPackager.view.getBottom() < mTop)
                    {
                        itemViewManager.addUnVisibleViewAbove(viewPackager.position, viewPackager);
                        removeView(viewPackager.view);
                        showViews.removeFirst();

                    }else
                    {
                        break;
                    }
                    if(showViews.size() == 0)
                    {
                        break;
                    }
                }while(true);
            }


        }
        if(showViews != null && showViews.size() != 0)
        {
//            log.v("删除下面多出来的子view");
            if(showViews.getLast().view.getTop() > getMeasuredHeight() - getPaddingBottom() + viewDeleteEdge)
            {
                int mBottom = getMeasuredHeight() - getPaddingBottom() + standEdge;
                do{
                    ViewPackager viewPackager = showViews.getLast();
                    if(viewPackager.view.getTop() > mBottom)
                    {
                        itemViewManager.addUnVisibleViewBlow(viewPackager.position, viewPackager);
                        removeView(viewPackager.view);
                        showViews.removeLast();
                    }else
                    {
                        break;
                    }
                    if(showViews.size() == 0)
                    {
                        break;
                    }
                }while(true);
            }

        }




        /*添加上面的view*/
//        log.v("add up view");
        boolean hasBeenEmpty = false;
        ViewPackager viewPackager = null;
        int startPosition = 0;
        int freeHeight = getPaddingTop();
        if(aboveViews == null)
        {
            aboveViews = new LinkedList<>();
        }
        if(showViews == null || showViews.size() == 0)
        {

            freeHeight = 0;
            startPosition = -1;
            hasBeenEmpty = true;
        }else
        {
            viewPackager = showViews.getFirst();
            if(viewPackager.view.getBottom() >= getPaddingTop() && viewPackager.view.getLeft() >= getPaddingLeft() + width + horizontalInterval)
            {
                freeHeight = viewPackager.view.getBottom() - verticalInterval - (getPaddingTop() - viewAddEdge);
            }else
            {
                freeHeight = viewPackager.view.getTop() - verticalInterval - (getPaddingTop() - viewAddEdge);
            }

            startPosition = viewPackager.position - 1;
        }
        if(freeHeight > 0)
        {
            freeHeight = viewPackager.view.getTop() - verticalInterval - (getPaddingTop() - standEdge);
            int rows = (int)(freeHeight / (height + verticalInterval)) + 1;
            int cols = (int)((getMeasuredWidth() - getPaddingRight() - getPaddingLeft()) / (width + horizontalInterval)) + 1;
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

                aboveViews.addFirst(newView);
                setupItemState(newView);
            }
        }


//        log.v("add below view");
        viewPackager = null;
        freeHeight = getMeasuredHeight() - getPaddingBottom();
        startPosition = 0;
        if(belowViews == null)
        {
            belowViews = new LinkedList<>();
        }
        if(showViews == null || showViews.size() == 0)
        {
            startPosition = initPosition;
            freeHeight = getMeasuredHeight() - getPaddingBottom() - getPaddingTop() + viewAddEdge;
        }else
        {
            viewPackager = showViews.getLast();
            if(viewPackager.view.getTop() <= getMeasuredHeight()
                    && viewPackager.view.getRight() <= getMeasuredWidth() - width - horizontalInterval)
            {
                if(adapter.getChildPositionInGroup(viewPackager.id) == 0
                        || adapter.getChildPositionInGroup(viewPackager.id) == adapter.getChildrenCount(adapter.getGroupId(viewPackager.id)) - 1)
                {
                    freeHeight = getMeasuredHeight() - getPaddingBottom() + viewAddEdge - viewPackager.view.getBottom() - verticalInterval;
                }else
                {
                    freeHeight = getMeasuredHeight() - getPaddingBottom() + viewAddEdge - viewPackager.view.getTop() - verticalInterval;
                }
            }
            else
            {
                freeHeight = getMeasuredHeight() - getPaddingBottom() + viewAddEdge - viewPackager.view.getBottom() - verticalInterval;
            }
            startPosition = viewPackager.position + 1;
        }
        if(freeHeight > 0)
        {
            freeHeight = freeHeight - viewAddEdge + standEdge;
            int rows = (int)(freeHeight / (height + verticalInterval)) + 1;
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

                belowViews.addLast(newView);
                setupItemState(newView);
            }
        }
        initPosition = 0;
//        log.v("onScreenViews.size() = " + onScreenViews.size());
//        log.v("showViews.size() = " + showViews.size());
    }

    private void layoutChildrenWithoutZoom(float zoomLevel, LinkedList<ViewPackager> aboveViews, LinkedList<ViewPackager> belowViews)
    {
        AlbumListViewAdapter adapter = itemViewManager.mAdapter;
        int parentWidth = getMeasuredWidth() - getPaddingLeft() - getPaddingRight();
        int base = (int)zoomLevel;
        int childWidth = (parentWidth - ((base - 1) * horizontalInterval)) / base;
        if(aboveViews != null && aboveViews.size() != 0)
        {
            int topEdge = 0;
            LinkedList<ViewPackager> groupedViews = new LinkedList<>();
            long lastId = -100l;
            ListIterator<ViewPackager> iterator = aboveViews.listIterator();
            ArrayList<ViewLayoutParams> paramses = new ArrayList<>(aboveViews.size());
            while(iterator.hasNext())
            {
                ViewPackager v = iterator.next();
                if(adapter.getGroupId(v.id) != lastId)
                {
                    if(groupedViews.size() != 0)
                    {
                        ArrayList<ViewLayoutParams> temp = layoutGroupWithoutZoom(zoomLevel, groupedViews);
                        if(temp == null)
                        {
                            return;
                        }
                        if(topEdge != 0)
                        {
                            for(ViewLayoutParams l : temp)
                            {
                                l.top += topEdge;
                                l.bottom += topEdge;
                            }
                        }
                        topEdge += temp.get(temp.size() - 1).bottom - temp.get(0).top + verticalInterval;
                        paramses.addAll(temp);
                    }
                    groupedViews.clear();
                }
                lastId = adapter.getGroupId(v.id);
                groupedViews.addLast(v);
            }

            if(groupedViews.size() != 0)
            {
                ArrayList<ViewLayoutParams> temp = layoutGroupWithoutZoom(zoomLevel, groupedViews);
                if(temp == null)
                {
                    return;
                }
                if(topEdge != 0)
                {
                    for(ViewLayoutParams l : temp)
                    {
                        l.top += topEdge;
                        l.bottom += topEdge;
                    }
                }

                paramses.addAll(temp);
            }


            int offset = 0;
            if(showViews != null && showViews.size() != 0)
            {
                ViewPackager firstView = showViews.getFirst();
                if(firstView.view.getLeft() == getPaddingLeft() || adapter.getViewType(firstView.id) == VIEW_TYPE.UNZOOM)
                {
                    offset = firstView.view.getTop() - verticalInterval - paramses.get(paramses.size() - 1).bottom;
                }else
                {
                    offset = firstView.view.getBottom()  - paramses.get(paramses.size() - 1).bottom;
                }

            }else
            {
                offset = getPaddingTop() - paramses.get(0).top;
            }


            int i = 0;
            int paddingLeft = getPaddingLeft();
            for(ViewPackager v : aboveViews)
            {
                ViewLayoutParams l = paramses.get(i);
                boolean measured = (v.view.getWidth() == l.getWidth() && v.view.getHeight() == l.getHeight());
                if(!measured)
                {
                    measureChild(v.view, l.getWidth(), l.getHeight(), true);
                }
                v.view.layout(l.left + paddingLeft, l.top + offset, l.right + paddingLeft, l.bottom + offset);
                addView(v.view);

                i++;
            }

        }

        if(belowViews != null && belowViews.size() != 0)
        {
            int topEdge = 0;
            LinkedList<ViewPackager> groupedViews = new LinkedList<>();
            long lastId = -100l;
            ListIterator<ViewPackager> iterator = belowViews.listIterator();
            ArrayList<ViewLayoutParams> paramses = new ArrayList<>(belowViews.size());
            while(iterator.hasNext())
            {
                ViewPackager v = iterator.next();
                if(adapter.getGroupId(v.id) != lastId)
                {
                    if(groupedViews.size() != 0)
                    {
                        ArrayList<ViewLayoutParams> temp = layoutGroupWithoutZoom(zoomLevel, groupedViews);
                        if(temp == null)
                        {
                            return;
                        }
                        if(topEdge != 0)
                        {
                            for(ViewLayoutParams l : temp)
                            {
                                l.top += topEdge;
                                l.bottom += topEdge;
                            }
                        }
                        topEdge += temp.get(temp.size() - 1).bottom - temp.get(0).top + verticalInterval;
                        paramses.addAll(temp);
                    }
                    groupedViews.clear();
                }
                lastId = adapter.getGroupId(v.id);
                groupedViews.addLast(v);
            }

            if(groupedViews.size() != 0)
            {
                ArrayList<ViewLayoutParams> temp = layoutGroupWithoutZoom(zoomLevel, groupedViews);
                if(temp == null)
                {
                    return;
                }
                if(topEdge != 0)
                {
                    for(ViewLayoutParams l : temp)
                    {
                        l.top += topEdge;
                        l.bottom += topEdge;
                    }
                }
                topEdge += temp.get(temp.size() - 1).bottom - temp.get(0).top + verticalInterval;
                paramses.addAll(temp);
            }


            int offset = 0;
            if(showViews != null && showViews.size() != 0)
            {
                ViewPackager lastView = showViews.getLast();
                if(lastView.view.getRight() > (getMeasuredWidth() - getPaddingRight() - horizontalInterval - childWidth)
                        || adapter.getViewType(lastView.id) == VIEW_TYPE.UNZOOM
                        || adapter.getViewType(adapter.getItemId(paramses.get(0).position)) == VIEW_TYPE.UNZOOM)
                {
                    offset = lastView.view.getBottom() + verticalInterval - paramses.get(0).top;
                }else
                {
                    offset = lastView.view.getTop() - paramses.get(0).top;
                }

            }else
            {
                offset = getPaddingTop() - paramses.get(0).top;
            }
            int i = 0;
            int paddingLeft = getPaddingLeft();
            for(ViewPackager v : belowViews)
            {
                ViewLayoutParams l = paramses.get(i);
                boolean measured = (v.view.getWidth() == l.getWidth() && v.view.getHeight() == l.getHeight());
                if(!measured)
                {
                    measureChild(v.view, l.getWidth(), l.getHeight(), true);
                }
                v.view.layout(l.left + paddingLeft, l.top + offset, l.right + paddingLeft, l.bottom + offset);
                addView(v.view);

                i++;
            }

        }

        if(aboveViews != null && aboveViews.size() != 0)
        {
            showViews.addAll(0, aboveViews);
        }
        if(belowViews != null && belowViews.size() != 0)
        {
            showViews.addAll(belowViews);
        }
    }

    private void layoutChildren(Point zoomCenter, float zoomLevel, LinkedList<ViewPackager> aboveViews, LinkedList<ViewPackager> belowViews)
    {
        if(zoomLevel - mZoomLevel == 0.0000f)
        {

            log.v("layout without zoom");
            layoutChildrenWithoutZoom(zoomLevel, aboveViews, belowViews);

        }else
        {
            log.v("layout with zoom");
            layoutChildrenWithZoom(zoomCenter, zoomLevel, aboveViews, belowViews);
        }
        /**
         * we have to check if layout complete while not zooming. For one case is while we change the orientation
         * of screen, the original zoomLevel is not same as new one. we set an initPosition and layout. But we can not
         * ensure that child of initPosition is layout to the top-left. There may be some space that we should display
         * more child view. While in zooming mode, every thing will be layout correctly in a complex but safe flow.
         * */
        if(!checkLayoutComplete(zoomLevel))
        {
            log.v("checkLayoutComplete false");
//                postInvalidate();
            post(new Runnable() {
                @Override
                public void run() {
                    requestLayout();
                }
            });
//            requestLayout();
//
        }
    }

    private void layoutChildrenWithZoom(Point zoomCenter, float zoomLevel, LinkedList<ViewPackager> aboveViews, LinkedList<ViewPackager> belowViews)
    {
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
        }else
        {
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

        ArrayList<ViewLayoutParams> params = createLayoutModel(zoomLevel, aboveViews, belowViews);
        if(params == null || params.size() == 0)
        {
            return;
        }

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

        Iterator<ViewPackager> iterator = null;
        int i = 0;
        int paddingLeft = getPaddingLeft();

        if(aboveViews != null && aboveViews.size() != 0)
        {
            iterator = aboveViews.iterator();
            while(iterator.hasNext())
            {
                ViewPackager v = iterator.next();
                ViewLayoutParams p = params.get(i);
                measureChild(v.view, p.getWidth(), p.getHeight(), true);
                addView(v.view);

                v.view.layout(p.left + paddingLeft, p.top + offset, p.right + paddingLeft, p.bottom + offset);
                i++;
            }
        }

        if(showViews != null && showViews.size() != 0)
        {
            iterator = showViews.iterator();
            while(iterator.hasNext())
            {
                ViewPackager v = iterator.next();
                ViewLayoutParams p = params.get(i);
                measureChild(v.view, p.getWidth(), p.getHeight(), true);
                v.view.layout(p.left + paddingLeft, p.top + offset, p.right + paddingLeft, p.bottom + offset);
                i++;
            }
        }

        if(belowViews != null && belowViews.size() != 0)
        {
            iterator = belowViews.iterator();
            while(iterator.hasNext())
            {
                ViewPackager v = iterator.next();
                ViewLayoutParams p = params.get(i);
                measureChild(v.view, p.getWidth(), p.getHeight(), true);
                addView(v.view);

                v.view.layout(p.left + paddingLeft, p.top + offset, p.right + paddingLeft, p.bottom + offset);
                i++;
            }
        }
        if(aboveViews != null && aboveViews.size() != 0)
        {
            showViews.addAll(0, aboveViews);
        }
        if(belowViews != null && belowViews.size() != 0)
        {
            showViews.addAll(belowViews);
        }

    }

    private ArrayList<ViewLayoutParams> createLayoutModel(float zoomLevel, LinkedList<ViewPackager> aboveViews, LinkedList<ViewPackager> belowViews)
    {
        ArrayList<ViewLayoutParams> paramses = new ArrayList<>();
        LinkedList<ViewPackager> groupedViews = new LinkedList<>();
        int top = 0;
        long lastGroupId = -100L;
        Iterator<ViewPackager> iterator;
        AlbumListViewAdapter adapter = itemViewManager.mAdapter;
        if(aboveViews != null && aboveViews.size() != 0)
        {
            iterator = aboveViews.listIterator();
            while(iterator.hasNext())
            {
                ViewPackager v = iterator.next();
                if(adapter.getGroupId(v.id) != lastGroupId)
                {
                    if(groupedViews.size() != 0)
                    {
                        ArrayList<ViewLayoutParams> temp = layoutGroupWithZoom(zoomLevel, groupedViews);
                        if(temp == null)
                        {
                            return null;
                        }
                        if(top != 0)
                        {
                            for(int i = 0; i < temp.size(); i++)
                            {
                                ViewLayoutParams p = temp.get(i);
                                p.top += top;
                                p.bottom += top;
                            }
                        }
                        top += temp.get(temp.size() - 1).bottom - temp.get(0).top + verticalInterval;
                        paramses.addAll(temp);
                    }

                    groupedViews.clear();
                }
                lastGroupId = adapter.getGroupId(v.id);
                groupedViews.addLast(v);
            }
        }

        if(showViews != null && showViews.size() != 0)
        {
            iterator = showViews.listIterator();
            while(iterator.hasNext())
            {
                ViewPackager v = iterator.next();
                if(adapter.getGroupId(v.id) != lastGroupId)
                {
                    if(groupedViews.size() != 0)
                    {
                        ArrayList<ViewLayoutParams> temp = layoutGroupWithZoom(zoomLevel, groupedViews);
                        if(temp == null)
                        {
                            return null;
                        }
                        if(top != 0)
                        {
                            for(int i = 0; i < temp.size(); i++)
                            {
                                ViewLayoutParams p = temp.get(i);
                                p.top += top;
                                p.bottom += top;
                            }
                        }

                        top += temp.get(temp.size() - 1).bottom - temp.get(0).top + verticalInterval;
                        paramses.addAll(temp);
                    }

                    groupedViews.clear();
                }
                lastGroupId = adapter.getGroupId(v.id);
                groupedViews.addLast(v);
            }
        }

        if(belowViews != null && belowViews.size() != 0)
        {
            iterator = belowViews.listIterator();
            while(iterator.hasNext())
            {
                ViewPackager v = iterator.next();
                if(adapter.getGroupId(v.id) != lastGroupId)
                {
                    if(groupedViews.size() != 0)
                    {
                        ArrayList<ViewLayoutParams> temp = layoutGroupWithZoom(zoomLevel, groupedViews);
                        if(temp == null)
                        {
                            return null;
                        }
                        if(top != 0)
                        {
                            for(int i = 0; i < temp.size(); i++)
                            {
                                ViewLayoutParams p = temp.get(i);
                                p.top += top;
                                p.bottom += top;
                            }
                        }
                        top += temp.get(temp.size() - 1).bottom - temp.get(0).top + verticalInterval;
                        paramses.addAll(temp);
                    }

                    groupedViews.clear();
                }
                lastGroupId = adapter.getGroupId(v.id);
                groupedViews.addLast(v);
            }
        }

        if(groupedViews.size() != 0)
        {
            ArrayList<ViewLayoutParams> temp = layoutGroupWithZoom(zoomLevel, groupedViews);
            if(temp == null)
            {
                return null;
            }
            for(int i = 0; i < temp.size(); i++)
            {
                ViewLayoutParams p = temp.get(i);
                p.top += top;
                p.bottom += top;
            }

            paramses.addAll(temp);
        }

        return paramses;


    }

    private ArrayList<ViewLayoutParams> layoutGroupWithZoom(float zoomLevel, LinkedList<ViewPackager> views)
    {
        if(itemViewManager == null || itemViewManager.mAdapter == null)
        {
            return null;
        }
        if(views == null || views.size() == 0)
        {
            return null;
        }
        ArrayList<ViewLayoutParams> result = new ArrayList<>(views.size());

        int parentWidth = getMeasuredWidth() - getPaddingLeft() - getPaddingRight();

        int beforeBase = (int)zoomLevel;
        int afterBase = beforeBase + 1;

        int beforeWidth = (parentWidth - ((beforeBase - 1) * horizontalInterval)) / beforeBase;
        int beforeHeight = (int)(beforeWidth * heightToWidthRatio);

        int afterWidth = (parentWidth - ((afterBase - 1) * horizontalInterval)) / afterBase;
        int afterHeight = (int)(afterWidth * heightToWidthRatio);

        float k = zoomLevel - beforeBase;

        AlbumListViewAdapter adapter = itemViewManager.mAdapter;
//        long groupId = adapter.getGroupId(views.getFirst().id);
//        int childCount = adapter.getChildrenCount(groupId);
        boolean hasUnZoomed = false;

        if(adapter.getViewType(adapter.getChildIdInGroup(adapter.getGroupId(views.getFirst().id),0)) == VIEW_TYPE.UNZOOM)
        {
            hasUnZoomed = true;
        }
        ListIterator<ViewPackager> iterator = views.listIterator();
        while(iterator.hasNext())
        {
            ViewPackager v = iterator.next();
            int position = adapter.getChildPositionInGroup(v.id);
            if(hasUnZoomed)
            {
                if(adapter.getChildPositionInGroup(v.id) == 0)
                {
                    result.add(null);
                    continue;
                }
                position -= 1;
            }

            int top, left, bottom, right, height, width;
            int beforeTop = (position / beforeBase) * (beforeHeight + verticalInterval);
            int beforeLeft = (position % beforeBase) * (beforeWidth + horizontalInterval);
            int beforeBottom = beforeTop + beforeHeight;
            int beforeRight = beforeLeft + beforeWidth;

            int afterTop = (position / afterBase) * (afterHeight + verticalInterval);
            int afterLeft = (position % afterBase) * (afterWidth + horizontalInterval);
            int afterBottom = afterTop + afterHeight;
            int afterRight = afterLeft + afterWidth;

            top = beforeTop + (int)((afterTop - beforeTop) * k);
            left = beforeLeft + (int)((afterLeft - beforeLeft) * k);
            right = beforeRight + (int)((afterRight - beforeRight) * k);
            bottom = beforeBottom + (int)((afterBottom - beforeBottom) * k);

            ViewLayoutParams params = new ViewLayoutParams(top, left, bottom, right, v.position);
            result.add(params);
        }

        int topEdge = 0;
        int offset = 0;
        int startPosition = 0;
        if(hasUnZoomed)
        {
            if(adapter.getViewType(views.getFirst().id) == VIEW_TYPE.UNZOOM)
            {
                View v = views.getFirst().view;
                measureChild(v, 0, 0, false);
                ViewLayoutParams p = new ViewLayoutParams(0, 0, v.getMeasuredHeight(), v.getMeasuredWidth(), views.getFirst().position);
                topEdge += v.getMeasuredHeight() + verticalInterval;
                startPosition = 1;
                result.set(0, p);
            }
        }

        if(startPosition < result.size())
        {
            offset = topEdge - result.get(startPosition).top;
            for(int i = startPosition; i < result.size(); i++)
            {
                ViewLayoutParams p = result.get(i);
                p.top += offset;
                p.bottom += offset;
            }
        }


        return result;
    }


    private ArrayList<ViewLayoutParams> layoutGroupWithoutZoom(float zoomLevel, LinkedList<ViewPackager> views)
    {
        if(itemViewManager == null || itemViewManager.mAdapter == null)
        {
            return null;
        }
        if(views == null || views.size() == 0)
        {
            return null;
        }
        ArrayList<ViewLayoutParams> result = new ArrayList<>(views.size());

        int parentWidth = getMeasuredWidth() - getPaddingLeft() - getPaddingRight();

        int base = (int)zoomLevel;


        int width = (parentWidth - ((base - 1) * horizontalInterval)) / base;
        int height = (int)(width * heightToWidthRatio);


        AlbumListViewAdapter adapter = itemViewManager.mAdapter;
//        long groupId = adapter.getGroupId(views.getFirst().id);
//        int childCount = adapter.getChildrenCount(groupId);
        boolean hasUnZoomed = false;

        if(adapter.getViewType(adapter.getChildIdInGroup(adapter.getGroupId(views.getFirst().id),0)) == VIEW_TYPE.UNZOOM)
        {
            hasUnZoomed = true;
        }
        ListIterator<ViewPackager> iterator = views.listIterator();
        while(iterator.hasNext())
        {
            ViewPackager v = iterator.next();
            int position = adapter.getChildPositionInGroup(v.id);
            if(hasUnZoomed)
            {
                if(adapter.getChildPositionInGroup(v.id) == 0)
                {
                    result.add(null);
                    continue;
                }
                position -= 1;
            }

            int top, left, bottom, right;

            top = (position / base) * (height + verticalInterval);
            left = (position % base) * (width + horizontalInterval);
            bottom = top + height;
            right = left + width;


            ViewLayoutParams params = new ViewLayoutParams(top, left, bottom, right, v.position);
            result.add(params);
        }

        int topEdge = 0;
        int offset = 0;
        int startPosition = 0;
        if(hasUnZoomed)
        {
            if(adapter.getViewType(views.getFirst().id) == VIEW_TYPE.UNZOOM)
            {
                View v = views.getFirst().view;
                measureChild(v, 0, 0, false);
                ViewLayoutParams p = new ViewLayoutParams(0, 0, v.getMeasuredHeight(), v.getMeasuredWidth(), views.getFirst().position);
                topEdge += v.getMeasuredHeight() + verticalInterval;
                startPosition = 1;
                result.set(0, p);
            }
        }

        if(startPosition < result.size())
        {
            offset = topEdge - result.get(startPosition).top;
            for(int i = startPosition; i < result.size(); i++)
            {
                ViewLayoutParams p = result.get(i);
                p.top += offset;
                p.bottom += offset;
            }
        }


        return result;
    }


    private void measureChild(View view, int widthSize, int heightSize, boolean zoom)
    {
        if(!zoom)
        {
            int heightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
            int widthSpec = MeasureSpec.makeMeasureSpec(getMeasuredWidth() - getPaddingLeft() - getPaddingRight(), MeasureSpec.AT_MOST);
            view.measure(widthSpec,heightSpec);
        }else
        {
            int heightSpec = MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.EXACTLY);
            int widthSpec = MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY);
            view.measure(widthSpec, heightSpec);
        }
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if(mScroller.computeScrollOffset())
        {
            final int newY = mScroller.getCurrY();
//            log.v("computeScroll, dy = " + (newY - lastY));


            post(new Runnable() {
                @Override
                public void run() {
                    doLayout(newY - lastY, null, mZoomLevel);
                    lastY = newY;
                }
            });


        }
    }

    private int newX = 0, newY = 0, oldX = 0, oldY = 0, dx = 0, dy = 0, downX = 0, downY = 0;
    private int lastY = 0;
    private VelocityTracker velocityTracker = null;
    private ScaleGestureDetector mScaleGestureDetector = null;
    /*设置zoomed是因为即使是双指手势，在之后抬起来也会出现单指的move和up动作导致画面跳动，因此必须使用这个标志位屏蔽缩放之后的滑动事件*/
    private boolean zoomed = false;
    private boolean moved = false;

    private HashSet<OnItemClickListener> clickListeners = new HashSet<>();
    private HashSet<OnMultiCheckListener> multiCheckListeners = new HashSet<>();
    private HashSet<OnItemLongClickListener> longClickListeners = new HashSet<>();
    private HashSet<Integer> tempCheckedItems = null;
    private TreeMap<Integer, Boolean> operatedItems = new TreeMap<>();

    private HashMap<Integer, AnimateParams> animateParamsHashMap = new HashMap<>();

    private boolean MULTI_CHECK_MODE_FLAG = false;
    private boolean CONTINUOUS_CHECK_MODE_FLAG = false;
    private boolean LONG_CLICKED_FLAG = false;
    private boolean handleEventQueue = false;

    private long longClickTimeGate = 500;
    private ViewPackager pressedItem = null;

    private HashSet<Integer> checkedItems = new HashSet<>();

    /**
     * When should this event will be consumed by us? If it is two point event, we must consume it to
     * zoom children. If is single point event, first we dispatch this event as normal. If no child
     * consumes this event, we consume it to action click or long click, etc. As the result, if children
     * is clickable or long clickable, our OnItemClickListener will not be triggered when a child is clicked
     *
     * First we dispatch this event as normal, if no child consume it, we set @handleEventQueue true, and
     * re-dispatch this event. The method @onInterceptTouchEvent must return true this time. and we handle
     * this event.
     * */
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        boolean consumed = false;
        consumed = super.dispatchTouchEvent(ev);
        if(!consumed)
        {
//            consumed = onUnHandleTouchEvent(ev);
            handleEventQueue = true;
            consumed = super.dispatchTouchEvent(ev);
        }
        return consumed;
    }



    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        boolean intercepted = false;
        int action = ev.getActionMasked();


        switch (action)
        {
            case MotionEvent.ACTION_DOWN:
                newX = (int)ev.getRawX();
                newY = (int)ev.getRawY();
                downX =  newX;
                downY = newY;
                mScroller.forceFinished(true);
                intercepted = false;
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:


                intercepted = false;
                break;
            case MotionEvent.ACTION_MOVE:
                oldX = newX;
                oldY = newY;
                newX = (int)ev.getRawX();
                newY = (int)ev.getRawY();
                dx = newX - oldX;
                dy = newY - oldY;

                if(Math.abs(dy) >= touchSlop || Math.abs(dx) >= touchSlop
                        || Math.abs(newX - downX) >= moveSlop || Math.abs(newY - downY) >= moveSlop)
                {
                    intercepted = true;
                }else
                {
                    if(pressedItem != null)
                    {
                        pressedItem.view.setPressed(false);
                    }
                    intercepted = false;
                }

                break;
            default:
                intercepted = false;
                break;

        }
        if(ev.getPointerCount() >= 2 || handleEventQueue == true || MULTI_CHECK_MODE_FLAG)
        {
            intercepted = true;
        }

        return intercepted;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(event.getActionMasked() == MotionEvent.ACTION_UP || event.getActionMasked() == MotionEvent.ACTION_CANCEL)
        {
            if(pressedItem != null)
            {
                pressedItem.view.setPressed(false);
            }
        }
        boolean consumed = true;
        int count = event.getPointerCount();
        if(count >= 2)
        {
            consumed = dealWithMultiPoint(event);
        }else
        {
            consumed = dealWithSinglePoint(event);
        }
        return consumed;
    }

    private boolean dealWithMultiPoint(MotionEvent event)
    {
        zoomed = true;
        if(mScaleGestureDetector == null)
        {
            mScaleGestureDetector = new ScaleGestureDetector(getContext(), new MyScaleListener());
        }
        mScaleGestureDetector.onTouchEvent(event);

        return true;
    }

    private boolean dealWithSinglePoint(MotionEvent event)
    {
//        if(!isClickable() && !isLongClickable())
//        {
//            return false;
//        }
        detectScroll(event);
        detectPress(event);

        return true;
    }

    private void detectPress(MotionEvent event)
    {
        if(!zoomed && !moved)
        {
            if(event.getEventTime() - event.getDownTime() >= longClickTimeGate && pressedItem != null)
            {
                if(MULTI_CHECK_MODE_FLAG)
                {
                    CONTINUOUS_CHECK_MODE_FLAG = true;
                }else if(!LONG_CLICKED_FLAG)
                {
                    performLongClick(pressedItem);
                    LONG_CLICKED_FLAG = true;
                }

            }
        }else
        {
            if(pressedItem != null)
            {
                pressedItem.view.setPressed(false);
                pressedItem = null;
            }

        }
        switch (event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
//                Log.v("detecatePress", "ACTION_DOWN");
                downX = (int)event.getX();
                downY = (int)event.getY();
                if(!zoomed && !moved && pressedItem == null)
                {
                    pressedItem = getPressedItem((int)event.getX(), (int)event.getY());
                    if(pressedItem != null && !MULTI_CHECK_MODE_FLAG)
                    {
                        pressedItem.view.setPressed(true);
                    }
                }
                break;
            case MotionEvent.ACTION_MOVE:
//                Log.v("detecatePress", "ACTION_MOVE");

//                oldX = newX;
//                oldY = newY;
//                newX = (int)event.getRawX();
//                newY = (int)event.getRawY();
//                dx = newX - oldX;
//                dy = newY - oldY;
                if(MULTI_CHECK_MODE_FLAG && CONTINUOUS_CHECK_MODE_FLAG && !LONG_CLICKED_FLAG)
                {
                    int x = (int)event.getX();
                    int y = (int)event.getY();
                    checkItems(x, y);
                }
                break;
            case MotionEvent.ACTION_UP:
//                Log.v("detecatePress", "ACTION_UP");
                if(!zoomed && !moved)
                {
                    if(pressedItem != null)
                    {
                        if(event.getEventTime() - event.getDownTime() < longClickTimeGate)
                        {
                            performClick(pressedItem);

                        }
                        pressedItem.view.setPressed(false);
                    }

                }

                LONG_CLICKED_FLAG = false;
                CONTINUOUS_CHECK_MODE_FLAG = false;
                pressedItem = null;
                tempCheckedItems = null;
                zoomed = false;
                moved = false;
                break;
            default:
                break;
        }
    }

    private void detectScroll(MotionEvent event)
    {
        createOrUpdateVelocityTracker(event);
        switch (event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
//                Log.v("detecateScroll", "ACTION_DOWN");
                newX = (int)event.getRawX();
                newY = (int)event.getRawY();


                break;
            case MotionEvent.ACTION_MOVE:
//                Log.v("detecateScroll", "ACTION_MOVE");
                if(zoomed == false)
                {
                    oldX = newX;
                    oldY = newY;
                    newX = (int)event.getRawX();
                    newY = (int)event.getRawY();
                    dx = newX - oldX;
                    dy = newY - oldY;
                    if((Math.abs(dy) >= touchSlop || moved) && !CONTINUOUS_CHECK_MODE_FLAG )
                    {
                        doLayout((int)dy, null, mZoomLevel);
                        moved = true;
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
//                Log.v("detecateScroll", "ACTION_UP");

                float speed = getVelocityTrackerSpeed();
                recycleVelocityTracker();
//                log.v("ACTION_UP speed = " + (int)speed);
                fling((int)speed);

                break;
            default:
                break;
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

    private float getVelocityTrackerSpeed()
    {
        velocityTracker.computeCurrentVelocity(1000);
        return velocityTracker.getYVelocity();
    }

    private void recycleVelocityTracker()
    {
        if(velocityTracker != null)
        {
            velocityTracker.recycle();

        }
        velocityTracker = null;
    }
    private void fling(int pixelPerSecond)
    {
        if (mScroller == null)
        {
            mScroller = new Scroller(getContext());
        }

        lastY = 0;
//        log.v("fling speed = " + pixelPerSecond);
        mScroller.fling(0, 0, 0, pixelPerSecond, 0, 0, Integer.MIN_VALUE, Integer.MAX_VALUE);


    }


    private void animateZoom(final Point center)
    {
        float nextStat = Math.round(mZoomLevel);
        ValueAnimator animator = ValueAnimator.ofFloat(mZoomLevel, nextStat);
        animator.setDuration(200);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {

                tempZoomLevel = computeZoomLevel((float)animation.getAnimatedValue());
                doLayout(0, center, tempZoomLevel);
            }
        });
        animator.start();
    }

    private void checkItems(int endX, int endY)
    {
        if(showViews == null || showViews.size() == 0)
        {
            return;
        }
        if(tempCheckedItems == null)
        {
            tempCheckedItems = (HashSet<Integer>) checkedItems.clone();
        }

        ViewPackager targetItem = null;

        ListIterator<ViewPackager> iterator = showViews.listIterator();

        while(iterator.hasNext())
        {
            ViewPackager temp = iterator.next();
            if(temp.view.getTop() <= endY && temp.view.getBottom() >= endY &&
                    temp.view.getLeft() <= endX && temp.view.getRight() >= endX)
            {
                targetItem = temp;

                break;
            }
        }

        if(targetItem == null)
        {
            return;
        }

        int min = Math.min(targetItem.position, pressedItem.position);
        int max = Math.max(targetItem.position, pressedItem.position);
        checkedItems.clear();

        for(Integer i : tempCheckedItems)
        {
            if(i < min || i > max)
            {
                checkedItems.add(i);
            }
        }

        for(int i = min; i <= max; i++)
        {
            if(!tempCheckedItems.contains(i))
            {
                checkedItems.add(i);
            }
        }

        for(ViewPackager v : showViews)
        {
            if(checkedItems.contains(v.position))
            {
                if(v.view instanceof Checkable)
                {
                    ((Checkable) v.view).setChecked(true);
                }else
                {
                    tempCheckedItems.remove(v.position);
                    checkedItems.remove(v.position);
                }
            }else
            {
                if(v.view instanceof Checkable)
                {
                    ((Checkable) v.view).setChecked(false);
                }
            }
        }


        notifyOnMultiCheckListeners();


    }

    private void notifyOnMultiCheckListeners()
    {
        if(multiCheckListeners == null || multiCheckListeners.size() == 0)
        {
            return;
        }
        final Integer[] s = new Integer[checkedItems.size()];
        checkedItems.toArray(s);
        for(OnMultiCheckListener listener : multiCheckListeners)
        {
            listener.onMultiCheck(s);
        }
    }

    private ViewPackager getPressedItem(int x, int y)
    {
        if(showViews == null || showViews.size() == 0)
        {
            return null;
        }
        ViewPackager result = null;
        ListIterator<ViewPackager> iterator = showViews.listIterator();
        while(iterator.hasNext())
        {
            ViewPackager temp = iterator.next();
            if(temp.view.getTop() <= y && temp.view.getBottom() >= y && temp.view.getLeft() <= x && temp.view.getRight() >= x)
            {
                result = temp;
            }
        }
        return result;
    }

    private void performLongClick(ViewPackager longClickedItem)
    {
        notifyOnItemLongClickListeners(longClickedItem);
    }

    private void performClick(ViewPackager clickedItem)
    {
        if(MULTI_CHECK_MODE_FLAG)
        {
            if(checkedItems.contains(clickedItem.position))
            {
                removeCheckedItem(clickedItem);
            }else
            {
                addCheckedItem(clickedItem);
            }

            notifyOnMultiCheckListeners();
        }else
        {
            notifyOnItemClickListeners(clickedItem);
        }
    }
    public void removeCheckedItem(ViewPackager v)
    {
        if(checkedItems.contains(v.position))
        {
            checkedItems.remove(v.position);
            if(v.view instanceof CheckableItem)
            {
                ((CheckableItem) v.view).setChecked(false);
            }
        }
    }

    public void addCheckedItem(ViewPackager v)
    {
        if(v.view instanceof CheckableItem)
        {
            ((CheckableItem) v.view).setChecked(true);
            checkedItems.add(v.position);
        }
    }

    public void addOnItemLongClickListener(OnItemLongClickListener onItemLongClickListener)
    {
        longClickListeners.add(onItemLongClickListener);
    }

    public void removeOnItemLongClickListener(OnItemLongClickListener onItemLongClickListener)
    {
        if(longClickListeners == null || longClickListeners.size() == 0)
        {
            return;
        }
        longClickListeners.remove(onItemLongClickListener);
    }

    public void addOnItemClickListener(OnItemClickListener listener)
    {
        clickListeners.add(listener);
    }

    public void removeOnItemClickListener(OnItemClickListener listener)
    {
        if(clickListeners == null || clickListeners.size() == 0)
        {
            return;
        }
        clickListeners.remove(listener);
    }

    public void addOnMultiCheckListener(OnMultiCheckListener listener)
    {
        multiCheckListeners.add(listener);
    }

    public void removeOnMultiCheckListener(OnMultiCheckListener listener)
    {
        if(multiCheckListeners == null || multiCheckListeners.size() == 0)
        {
            return;
        }
        multiCheckListeners.remove(listener);
    }

    private void notifyOnItemClickListeners(final ViewPackager v)
    {
        if(clickListeners == null || clickListeners.size() == 0)
        {
            return;
        }

        for(OnItemClickListener listener : clickListeners)
        {
            listener.onClick(v.view, v.position, v.id);
        }
    }

    private void notifyOnItemLongClickListeners(final ViewPackager v)
    {
        if(longClickListeners == null || longClickListeners.size() == 0)
        {
            return;
        }
        for(OnItemLongClickListener listener : longClickListeners)
        {
            listener.onLongClick(v.view, v.position, v.id);
        }
    }


    public interface OnItemClickListener
    {
        void onClick(View view, int position, long id);
    }

    public interface OnItemLongClickListener
    {
        void onLongClick(View view, int position, long id);
    }

    public interface OnMultiCheckListener
    {
        void onMultiCheck(Integer[] checkedItemsPosition);
    }



    private class AnimateParams
    {
        public boolean pressed = false;
        public boolean released = false;
        public boolean checked = false;
        public boolean unChecked = false;
        public boolean deleting = false;
        public boolean inserting = false;

        public long pressedTime = 0;
        public long releasedTime = 0;
        public long checkedTime = 0;
        public long unCheckedTime = 0;
        public long insertingTime = 0;
        public long deletingTime = 0;

        public void setPressed(boolean pressed) {
            this.pressed = pressed;
            pressedTime = System.currentTimeMillis();
        }

        public void setReleased(boolean released) {
            this.released = released;
            releasedTime = System.currentTimeMillis();
        }

        public void setChecked(boolean checked) {
            this.checked = checked;
            checkedTime = System.currentTimeMillis();
        }

        public void setUnChecked(boolean unChecked) {
            this.unChecked = unChecked;
            unCheckedTime = System.currentTimeMillis();
        }

        public void setDeleting(boolean deleting) {
            this.deleting = deleting;
            deletingTime = System.currentTimeMillis();
        }

        public void setInserting(boolean inserting) {
            this.inserting = inserting;
            insertingTime = System.currentTimeMillis();
        }

        @Override
        public String toString() {
            return "AnimateParams{" +
                    "pressed=" + pressed +
                    ", released=" + released +
                    ", checked=" + checked +
                    ", unChecked=" + unChecked +
                    ", deleting=" + deleting +
                    ", inserting=" + inserting +
                    '}';
        }
    }

    private class MyScaleListener implements ScaleGestureDetector.OnScaleGestureListener
    {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float scale = detector.getScaleFactor();
            tempZoomLevel = computeZoomLevel(mZoomLevel / (scale));
            Point center = new Point((int)detector.getFocusX(), (int)detector.getFocusY());
            doLayout(0, center, tempZoomLevel);
            zoomed = true;
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
        public long id;
        public View view;

        public ViewPackager(int position, long id, View view) {
            this.position = position;
            this.id = id;
            this.view = view;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ViewPackager)) return false;

            ViewPackager packager = (ViewPackager) o;

            if (position != packager.position) return false;
            if (id != packager.id) return false;
            return view != null ? view.equals(packager.view) : packager.view == null;

        }

        @Override
        public int hashCode() {
            int result = position;
            result = 31 * result + (int) (id ^ (id >>> 32));
            result = 31 * result + (view != null ? view.hashCode() : 0);
            return result;
        }
    }

    private class ItemViewManager
    {

        public AlbumListViewAdapter mAdapter;
        private LinkedList<ViewPackager> aboveUnVisibleViews = new LinkedList<>();
        private LinkedList<ViewPackager> blowUnVisibleViews = new LinkedList<>();
        private LinkedList<ViewPackager> trashViews = new LinkedList<>();
        private int trashSize = 40;
        public int UN_VISIBLE_VIEW_COUNT = 20;

        public ViewPackager getView(int position)
        {
            ViewPackager result = null;
            if((result = getViewAbove(position)) != null)
            {
//                log.v("aboveUnVisibleViews.size() = " + aboveUnVisibleViews.size());
                return result;
            }else if((result = getViewBlow(position)) != null)
            {
//                log.v("blowUnVisibleViews.size() = " + blowUnVisibleViews.size());
                return result;
            }else
            {
                result = getViewFromAdapter(position);
            }
            return result;
        }

        public ViewPackager getViewAbove(int position)
        {
            if(aboveUnVisibleViews.size() == 0 || position > aboveUnVisibleViews.getLast().position)
            {
                return null;
            }else
            {
                ViewPackager result = null;
                Iterator<ViewPackager> iterator = aboveUnVisibleViews.descendingIterator();
                while(iterator.hasNext())
                {
                    ViewPackager temp = iterator.next();
                    if(temp.position == position)
                    {
                        result = temp;
                        iterator.remove();
                        break;
                    }else if(temp.position < position)
                    {
                        break;
                    }
                }
                return result;
            }


        }

        public ViewPackager getViewBlow(int position)
        {
            if(blowUnVisibleViews.size() == 0 || position < blowUnVisibleViews.getFirst().position)
            {
                return null;
            }else
            {
                ViewPackager result = null;
                Iterator<ViewPackager> iterator = blowUnVisibleViews.iterator();
                while(iterator.hasNext())
                {
                    ViewPackager temp = iterator.next();
                    if(temp.position == position)
                    {
                        result = temp;
                        iterator.remove();
                        break;
                    }else if(temp.position > position)
                    {
                        break;
                    }
                }
                return result;
            }

        }

        public void addUnVisibleView(int position, ViewPackager viewPackager)
        {
            if(showViews != null && showViews.size() != 0)
            {
                if(position >= showViews.getLast().position)
                {
                    addUnVisibleViewBlow(position, viewPackager);
                }else
                {
                    addUnVisibleViewAbove(position, viewPackager);
                }
            }else
            {
                addUnVisibleViewAbove(position, viewPackager);
            }

        }

        public void addUnVisibleViewBlow(int position, ViewPackager viewPackager)
        {
//            log.v("blowUnVisiableViews.size() = " + blowUnVisiableViews.size());
            blowUnVisibleViews.addFirst(viewPackager);
            if(blowUnVisibleViews.size() > UN_VISIBLE_VIEW_COUNT)
            {
                addTrashView(blowUnVisibleViews.pollLast());
            }
        }

        public void addUnVisibleViewAbove(int position, ViewPackager viewPackager)
        {
//            log.v("aboveUnVisiableViews.size() = " + aboveUnVisiableViews.size());
            aboveUnVisibleViews.addLast(viewPackager);
            if(aboveUnVisibleViews.size() > UN_VISIBLE_VIEW_COUNT)
            {
                addTrashView(aboveUnVisibleViews.pollFirst());
            }
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
            result = mAdapter.getView(position, trashViews.size() == 0 ? null : trashViews.pop().view, AlbumListView.this);
//            log.v("trashViews.size() + " + trashViews.size());
            if(result == null)
            {
                return null;
            }
            ViewPackager viewPackager = new ViewPackager(position, mAdapter.getItemId(position), result);
            return viewPackager;
        }

        public void invalidated()
        {
            aboveUnVisibleViews.clear();
            blowUnVisibleViews.clear();
            trashViews.clear();
        }

        public void dataSetChanged()
        {
            aboveUnVisibleViews.clear();
            blowUnVisibleViews.clear();
        }
    }


}
