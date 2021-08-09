package com.zu.customview.view.AlbumListView;

/**
 * Created by zu on 17-7-6.
 */

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Scroller;

import com.zu.customview.MyLog;
import com.zu.customview.R;
import com.zu.customview.view.ZoomView;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

import io.reactivex.internal.operators.maybe.MaybeZipArray;

import static com.zu.customview.view.AlbumListView.ImageAdapter.VIEW_TYPE_UNZOOM;
import static com.zu.customview.view.AlbumListView.ImageAdapter.VIEW_TYPE_ZOOM;

import androidx.recyclerview.widget.RecyclerView;

public class ZoomLayoutManager extends RecyclerView.LayoutManager {
    private MyLog log = new MyLog("ZoomLayoutManager", true);
    private ChildSizeHelper childSizeHelper = new ChildSizeHelper();
    private Context mContext = null;

    private int downAddEdge = 10;
    private int upAddEdge = -10;

    private ImageAdapter adapter = null;
    private float mZoomLevel = 2.0f;
    private float tempZoomLevel = mZoomLevel;
    private int minZoomLevel = 1;
    private int maxZoomLevel = 6;
    private int verticalSpace = 3;
    private int horizontalSpace = 3;
    private float heightToWidthRatio = 1.0f;
    private static final int DEFAULT_VERTICAL_SPACE = 3;
    private static final int DEFAULT_HORIZONTAL_SPACE = 3;
    private static final float DEFAULT_HEIGHT_TO_WIDTH_RATIO = 1.0f;
    private RecyclerView.Recycler mRecycler = null;
    private ZoomOnTouchListener zoomOnTouchListener = null;

    private boolean scrolling = false;
    private boolean zooming = false;

    private OnItemClickListener onItemClickListener;
    private OnItemLongClickListener onItemLongClickListener;
    private OnItemCheckedListener onItemCheckedListener;
    public void setOnItemClickListener(OnItemClickListener listener)
    {
        this.onItemClickListener = listener;
    }

    public void cleanOnItemClickListener()
    {
        onItemClickListener = null;
    }

    private void notifyOnItemClickListener(int position)
    {
        if(onItemClickListener != null)
        {
            onItemClickListener.onItemClicked(position);
        }
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener)
    {
        this.onItemLongClickListener = listener;
    }

    public void cleanOnItemLongClickListener()
    {
        this.onItemLongClickListener = null;
    }

    private void notifyOnItemLongClickListener(int position)
    {
        if(onItemLongClickListener != null)
        {
            onItemLongClickListener.onItemLongClicked(position);
        }
    }

    public void setOnItemCheckedListener(OnItemCheckedListener listener)
    {
        this.onItemCheckedListener = listener;
    }

    public void cleanOnItemCheckedListener()
    {
        onItemCheckedListener = null;
    }

    private void notifyOnItemCheckedListener(int[] checkedItemPosition)
    {
        if(onItemCheckedListener != null)
        {
            onItemCheckedListener.onItemChecked(checkedItemPosition);
        }
    }


    public ZoomLayoutManager(Context context, float zoomLevel, int maxZoomLevel, int minZoomLevel, ImageAdapter adapter)
    {
        this(context, zoomLevel, maxZoomLevel, minZoomLevel, DEFAULT_VERTICAL_SPACE, DEFAULT_HORIZONTAL_SPACE, DEFAULT_HEIGHT_TO_WIDTH_RATIO, adapter);
    }

    public ZoomLayoutManager(Context context, float zoomLevel, int maxZoomLevel, int minZoomLevel, int verticalSpace, int horizontalSpace, float heightToWidthRatio, ImageAdapter adapter)
    {
        mContext = context;
        this.mZoomLevel = zoomLevel;
        this.maxZoomLevel = maxZoomLevel;
        this.minZoomLevel = minZoomLevel;
        this.verticalSpace = verticalSpace;
        this.horizontalSpace = horizontalSpace;
        this.heightToWidthRatio = heightToWidthRatio;

        mZoomLevel = computeZoomLevel(mZoomLevel);
        zoomOnTouchListener = new ZoomOnTouchListener();

        setAdapter(adapter);
    }

    public void setAdapter(ImageAdapter adapter)
    {
        this.adapter = adapter;
        this.adapter.setZoomLayoutManager(ZoomLayoutManager.this);
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

    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        RecyclerView.LayoutParams layoutParams = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        return layoutParams;
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        if(scrolling || zooming)
        {
            return;
        }

        mRecycler = recycler;
        if(getItemCount() <= 0 || adapter == null)
        {
            return;
        }
        Rect visibleRect = getVisibleRect();
        int offset = visibleRect.top;
        int startPosition = 0;
        if(getChildCount() != 0)
        {
            View view = getChildAt(0);
            offset = getDecoratedTop(view);
            startPosition = getPosition(view);

        }

        detachAndScrapAttachedViews(recycler);
//        fillDown(startPosition, offset, recycler, state);
//        fill(startPosition, offset, recycler);
        fillDown(startPosition, offset, recycler);
        log.d("onLayoutChildren, child count = " + getChildCount());

    }



    private void fillDown(int startPosition, int offset, RecyclerView.Recycler recycler)
    {
        log.d("fillDown, startPosition = " + startPosition + ", offset = " + offset);
        Rect visibleRect = getVisibleRect();

        while(true)
        {
            LinkedList<Pair<Integer, Rect>> layoutRects = getGroupedLayoutRectsDown(startPosition, offset);
            log.d("fillDown, layoutRects.size = " + layoutRects.size());
            if(layoutRects == null || layoutRects.size() == 0)
            {
                return;
            }

            int mOffset = -1;
            boolean startFromUnZoom = adapter.getItemViewType(startPosition) == VIEW_TYPE_UNZOOM;

            for(Pair<Integer, Rect> p : layoutRects)
            {
                if(p.first == startPosition)
                {
                    mOffset = offset - p.second.top;
                    break;
                }
            }

            Iterator<Pair<Integer, Rect>> iterator = null;
            iterator = layoutRects.iterator();

            while(iterator.hasNext())
            {
                Pair<Integer, Rect> p = iterator.next();
                View existView = findViewByPosition(p.first);
                if(existView != null)
                {
                    if(getItemViewType(existView) == VIEW_TYPE_UNZOOM)
                    {
                        mOffset += getDecoratedMeasuredHeight(existView) + verticalSpace;
                    }
                    continue;
                }
                View view = getAndSetViewForPosition(recycler, p.first);
                Rect layout = p.second;
                if(view == null)
                {
                    return;
                }

                int type = getItemViewType(view);
                if(type == VIEW_TYPE_UNZOOM)
                {
                    addView(view);
                    measureChild(false, visibleRect.width(), 0, view);
                    int height = getDecoratedMeasuredHeight(view);
                    int width = getDecoratedMeasuredWidth(view);
                    if(startFromUnZoom)
                    {
                        layoutDecorated(view, visibleRect.left, layout.top + mOffset,
                                visibleRect.left + width, layout.top + mOffset + height);
                        mOffset += height + verticalSpace;
                    }else
                    {
                        layoutDecorated(view, visibleRect.left, layout.top + mOffset - height,
                                visibleRect.left + width, layout.top + mOffset);
                    }


                }else if(type == VIEW_TYPE_ZOOM)
                {
                    addView(view);
                    measureChild(true, layout.width(), layout.height(), view);
                    layoutDecorated(view, layout.left + visibleRect.left, layout.top + mOffset,
                            layout.right + visibleRect.left, layout.bottom + mOffset);
                }
            }

            if(getChildCount() != 0)
            {


                View lastView = getChildAt(getChildCount() - 1);
                if(getDecoratedTop(lastView) < visibleRect.bottom)
                {
                    startPosition = getPosition(lastView) + 1;
                    offset = getDecoratedBottom(lastView) + verticalSpace;
                    if(startPosition < getItemCount())
                    {
                        log.d("getLayoutDown");
                        continue;
                    }
                }


            }
            break;


        }
    }

    private void fillUp(int startPosition, int offset, RecyclerView.Recycler recycler)
    {
        log.d("fillUp, startPosition = " + startPosition + ", offset = " + offset);

        Rect visibleRect = getVisibleRect();
        while(true)
        {
            LinkedList<Pair<Integer, Rect>> layoutRects = getGroupedLayoutRectsUp(startPosition, offset);

            if(layoutRects == null || layoutRects.size() == 0)
            {
                return;
            }
            log.d("fillUp, layoutRects.size = " + layoutRects.size() + ", minPosition = " + layoutRects.getFirst().first
                    + "maxPosition = " + layoutRects.getLast().first);
            int mOffset = -1;
            boolean startFromUnZoom = adapter.getItemViewType(startPosition) == VIEW_TYPE_UNZOOM;

            for(Pair<Integer, Rect> p : layoutRects)
            {
                if(p.first == startPosition)
                {
                    mOffset = offset - p.second.top;
                    break;
                }
            }

            Iterator<Pair<Integer, Rect>> iterator = null;
            iterator = layoutRects.descendingIterator();
            while(iterator.hasNext())
            {
                Pair<Integer, Rect> p = iterator.next();
                View existView = findViewByPosition(p.first);
                if(existView != null)
                {
                    if(getItemViewType(existView) == VIEW_TYPE_UNZOOM)
                    {
                        mOffset -= getDecoratedMeasuredHeight(existView) - verticalSpace;
                    }
                    continue;
                }
                View view = getAndSetViewForPosition(recycler, p.first);
                Rect layout = p.second;
                if(view == null)
                {
                    return;
                }

                int type = getItemViewType(view);
                if(type == VIEW_TYPE_UNZOOM)
                {
                    addView(view, 0);

                    measureChild(false, visibleRect.width(), 0, view);
                    int height = getDecoratedMeasuredHeight(view);
                    int width = getDecoratedMeasuredWidth(view);
                    if(startFromUnZoom)
                    {
                        layoutDecorated(view, visibleRect.left, layout.top + mOffset,
                                visibleRect.left + width, layout.top + mOffset + height);
                        mOffset += height + verticalSpace;
                    }else
                    {
                        layoutDecorated(view, visibleRect.left, layout.top + mOffset - height,
                                visibleRect.left + width, layout.top + mOffset);
                    }


                }else if(type == VIEW_TYPE_ZOOM)
                {
                    addView(view, 0);
                    measureChild(true, layout.width(), layout.height(), view);
                    layoutDecorated(view, layout.left + visibleRect.left, layout.top + mOffset,
                            layout.right + visibleRect.left, layout.bottom + mOffset);
                }
            }

            if(getChildCount() != 0)
            {
                View firstView = getChildAt(0);
                if(getDecoratedBottom(firstView) > visibleRect.top)
                {
                    if(adapter.isGrouped())
                    {
                        startPosition = getPosition(firstView);
                        int index = adapter.getChildIndex(adapter.getItemId(startPosition));
                        if(index == 0)
                        {
                            startPosition -= 1;
                            offset = getDecoratedTop(firstView) - verticalSpace - childSizeHelper.getHeight();
                        }else
                        {
                            offset = getDecoratedTop(firstView);
                        }


                        if(startPosition >= 0)
                        {
                            log.d("getLayoutUp");

                            continue;
                        }
                    }

                }
            }
            break;


        }
    }

//    private LinkedList<Pair<Integer, Rect>> getGroupedLayout(int startPosition, int offset)
//    {
//        log.d("getGroupedLayout, startPosition = " + startPosition + ", offset = " + offset);
//        Rect visibleRect = getVisibleRect();
//        int beforeBase = (int)mZoomLevel;
//        int afterBase = beforeBase + 1;
//        float k = mZoomLevel - beforeBase;
//        int childHeight = childSizeHelper.getHeight();
//        int childWidth = childSizeHelper.getWidth();
//        int beforeWidth = childSizeHelper.getWidth(beforeBase);
//        int beforeHeight = childSizeHelper.getHeight(beforeBase);
//        int afterHeight = childSizeHelper.getHeight(afterBase);
//        int afterWidth = childSizeHelper.getWidth(afterBase);
//
//        int mapGroupIndex = adapter.getGroupIndex(adapter.getItemId(startPosition));
//
//
//        LinkedList<Pair<Integer, Rect>> result = new LinkedList<>();
//        LinkedList<Pair<Integer, Rect>> downResult = new LinkedList<>();
//        LinkedList<Pair<Integer, Rect>> upResult = new LinkedList<>();
//
//        int upRestSpace = offset - (visibleRect.top);
//        int downRestSpace = visibleRect.bottom - offset;
//        int upStartPosition = startPosition - 1;
//        int downStartPosition = startPosition;
//
//        boolean grouped = adapter.isGrouped();
//
//        int totalOffset = 0;
//
//
//        int downEdge = 0;
//        while(downEdge < downRestSpace)
//        {
//            if(downStartPosition >= getItemCount())
//            {
//                break;
//            }
//            long id = adapter.getItemId(downStartPosition);
//            int groupIndex = adapter.getGroupIndex(id);
//            int childIndex = adapter.getChildIndex(id);
//            int type = adapter.getItemViewType(downStartPosition);
//            if(groupIndex != mapGroupIndex)
//            {
//                break;
//            }
//            if(k == 0.0f)
//            {
//                if(type == VIEW_TYPE_UNZOOM)
//                {
//                    downResult.addLast(new Pair<>(downStartPosition, new Rect(0, 0, 0, 0)));
//                }else if(type == VIEW_TYPE_ZOOM)
//                {
//                    if(grouped)
//                    {
//                        childIndex -= 1;
//                    }
//                    int top = (childIndex / beforeBase) * (childHeight + verticalSpace);
//                    int left = (childIndex % beforeBase) * (childWidth + horizontalSpace);
//                    int bottom = top + childHeight;
//                    int right = left + childWidth;
//
//                    downResult.addLast(new Pair<>(downStartPosition, new Rect(left, top, right, bottom)));
//                }
//            }else
//            {
//                if(type == VIEW_TYPE_UNZOOM)
//                {
//                    downResult.addLast(new Pair<Integer, Rect>(downStartPosition, new Rect(0, 0, 0, 0)));
//                }else if(type == VIEW_TYPE_ZOOM)
//                {
//                    if(grouped)
//                    {
//                        childIndex -= 1;
//                    }
//                    int beforeTop = (childIndex / beforeBase) * (beforeHeight + childIndex);
//                    int beforeLeft = (childIndex % beforeBase) * (beforeWidth + childIndex);
//                    int beforeBottom = beforeTop + beforeHeight;
//                    int beforeRight = beforeLeft + beforeWidth;
//
//                    int afterTop = (childIndex / afterBase) * (afterHeight + childIndex);
//                    int afterLeft = (childIndex % afterBase) * (afterWidth + childIndex);
//                    int afterBottom = afterTop + afterHeight;
//                    int afterRight = afterLeft + afterWidth;
//
//                    int top = beforeTop + (int)((afterTop - beforeTop) * k);
//                    int left = beforeLeft + (int)((afterLeft - beforeLeft) * k);
//                    int right = beforeRight + (int)((afterRight - beforeRight) * k);
//                    int bottom = beforeBottom + (int)((afterBottom - beforeBottom) * k);
//                    downResult.addLast(new Pair<Integer, Rect>(downStartPosition, new Rect(left, top, right, bottom)));
//                }
//            }
//
//            if(downResult.size() <= 1)
//            {
//                downEdge = 0;
//            }else
//            {
//                int first = downResult.getFirst().second.top;
//                int last = downResult.getLast().second.top;
//                downEdge = Math.abs(last - first);
//            }
//            downStartPosition ++;
//
//
//        }
//
//        int upEdge = 0;
//        while(upEdge < upRestSpace)
//        {
//            if(upStartPosition < 0)
//            {
//                break;
//            }
//            long id = adapter.getItemId(upStartPosition);
//            int groupIndex = adapter.getGroupIndex(id);
//            int childIndex = adapter.getChildIndex(id);
//            int type = adapter.getItemViewType(upStartPosition);
//            if(groupIndex != mapGroupIndex)
//            {
//                break;
//            }
//            if(k == 0.0f)
//            {
//                if(type == VIEW_TYPE_UNZOOM)
//                {
//                    upResult.addFirst(new Pair<>(upStartPosition, new Rect(0, 0, 0, 0)));
//                }else if(type == VIEW_TYPE_ZOOM)
//                {
//                    if(grouped)
//                    {
//                        childIndex -= 1;
//                    }
//                    int top = (childIndex / beforeBase) * (childHeight + verticalSpace);
//                    int left = (childIndex % beforeBase) * (childWidth + horizontalSpace);
//                    int bottom = top + childHeight;
//                    int right = left + childWidth;
//
//                    upResult.addFirst(new Pair<>(upStartPosition, new Rect(left, top, right, bottom)));
//                }
//            }else
//            {
//                if(type == VIEW_TYPE_UNZOOM)
//                {
//                    upResult.addFirst(new Pair<Integer, Rect>(upStartPosition, new Rect(0, 0, 0, 0)));
//                }else if(type == VIEW_TYPE_ZOOM)
//                {
//                    if(grouped)
//                    {
//                        childIndex -= 1;
//                    }
//                    int beforeTop = (childIndex / beforeBase) * (beforeHeight + childIndex);
//                    int beforeLeft = (childIndex % beforeBase) * (beforeWidth + childIndex);
//                    int beforeBottom = beforeTop + beforeHeight;
//                    int beforeRight = beforeLeft + beforeWidth;
//
//                    int afterTop = (childIndex / afterBase) * (afterHeight + childIndex);
//                    int afterLeft = (childIndex % afterBase) * (afterWidth + childIndex);
//                    int afterBottom = afterTop + afterHeight;
//                    int afterRight = afterLeft + afterWidth;
//
//                    int top = beforeTop + (int)((afterTop - beforeTop) * k);
//                    int left = beforeLeft + (int)((afterLeft - beforeLeft) * k);
//                    int right = beforeRight + (int)((afterRight - beforeRight) * k);
//                    int bottom = beforeBottom + (int)((afterBottom - beforeBottom) * k);
//                    upResult.addFirst(new Pair<Integer, Rect>(upStartPosition, new Rect(left, top, right, bottom)));
//                }
//            }
//
//            if(upResult.size() <= 1)
//            {
//                upEdge = 0;
//            }else
//            {
//                int first = upResult.getFirst().second.top;
//                int last = upResult.getLast().second.top;
//                upEdge = Math.abs(last - first);
//            }
//            upStartPosition --;
//        }
//        result.addAll(upResult);
//        result.addAll(downResult);
//        return result;
//
//    }


//    private void fillDown(int startPosition, int offset, RecyclerView.Recycler recycler, RecyclerView.State state)
//    {
////        log.d("fillDown");
//        Rect visibleRect = getVisibleRect();
//        while(true)
//        {
//            boolean startFromUnZoom = adapter.getItemViewType(startPosition) == VIEW_TYPE_UNZOOM;
//            int mOffset = offset;
//            LinkedList<Pair<Integer, Rect>> layoutRects = getGroupedLayoutRectsDown(startPosition, offset);
//            if(layoutRects != null && layoutRects.size() > 0)
//            {
//
//                for(Pair<Integer, Rect> p : layoutRects)
//                {
//                    if(p.first == startPosition)
//                    {
//                        mOffset = offset - p.second.top;
//                        break;
//                    }
//                }
//            }else
//            {
//                return;
//            }
//
//            for(Pair<Integer, Rect> p : layoutRects)
//            {
//                int position = p.first;
//                Rect layoutRect = p.second;
//                View view  = recycler.getViewForPosition(position);
//                if(view == null)
//                {
//                    return;
//                }
//                if(findViewByPosition(position) != null)
//                {
//                    if(getItemViewType(view) == VIEW_TYPE_UNZOOM)
//                    {
//                        mOffset += getDecoratedMeasuredHeight(view) + verticalSpace;
//                    }
//                    continue;
//                }
//
//                if(getItemViewType(view) == VIEW_TYPE_UNZOOM)
//                {
//                    addView(view);
//                    measureChild(false, visibleRect.width(), 0, view);
//                    int height = getDecoratedMeasuredHeight(view);
//                    int width = getDecoratedMeasuredWidth(view);
//                    layoutDecorated(view, visibleRect.left, mOffset, visibleRect.right + width, mOffset + height);
//                    mOffset += height + verticalSpace;
//
//                }else
//                {
//                    addView(view);
//                    measureChild(true, layoutRect.width(), layoutRect.height(), view);
//                    layoutDecorated(view, layoutRect.left, layoutRect.top + mOffset, layoutRect.right, layoutRect.bottom + mOffset);
//                }
//
//            }
//
//            if(getChildCount() > 0)
//            {
//                View view = getChildAt(getChildCount() - 1);
//                offset = getDecoratedBottom(view) + verticalSpace;
//                startPosition = getPosition(view) + 1;
//            }
//
//        }
//    }
//
//    private void fillUp(int startPosition, int offset, RecyclerView.Recycler recycler, RecyclerView.State state)
//    {
//        log.d("fillUp");
//        Rect visibleRect = getVisibleRect();
//        while(true)
//        {
//            int mOffset = 0;
//            if(offset < visibleRect.top)
//            {
//                break;
//            }
//            LinkedList<Pair<Integer, Rect>> layoutRects = getGroupedLayoutRectsUp(startPosition, offset);
//            if(layoutRects != null && layoutRects.size() > 0)
//            {
//                mOffset = offset - layoutRects.getFirst().second.bottom;
//            }
//
//
//            for(Pair<Integer, Rect> p : layoutRects)
//            {
//                int position = p.first;
//                Rect layoutRect = p.second;
//                View view  = recycler.getViewForPosition(position);
//                if(view == null)
//                {
//                    return;
//                }
//
//                if(findViewByPosition(position) != null)
//                {
//                    if(getItemViewType(view) == VIEW_TYPE_UNZOOM)
//                    {
//                        mOffset -= getDecoratedMeasuredHeight(view) + verticalSpace;
//                    }
//                    continue;
//                }
//                if(getItemViewType(view) == VIEW_TYPE_UNZOOM)
//                {
//
//                    addView(view, 0);
//                    measureChild(false, visibleRect.width(), 0, view);
//                    int height = getDecoratedMeasuredHeight(view);
//                    int width = getDecoratedMeasuredWidth(view);
//                    layoutDecorated(view, visibleRect.left, mOffset - height, visibleRect.right + width, mOffset);
//                    mOffset -= height + verticalSpace;
//
//                }else
//                {
//                    addView(view, 0);
//                    measureChild(true, layoutRect.width(), layoutRect.height(), view);
//                    layoutDecorated(view, layoutRect.left, layoutRect.top + mOffset, layoutRect.right, layoutRect.bottom + mOffset);
//                }
//
//            }
//            if(getChildCount() > 0)
//            {
//                View view = getChildAt(0);
//                offset = getDecoratedTop(view) - verticalSpace;
//                startPosition = getPosition(view) - 1;
//                if(startPosition < 0)
//                {
//                    return;
//                }
//            }
//
//        }
//    }

    private void measureChild(boolean exactly, int width, int height, View view)
    {
//        log.d("measureChild");
        if(exactly)
        {
            int heightSpec = View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY);
            int widthSpec = View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY);
            view.measure(widthSpec, heightSpec);
        }else
        {
            int heightSpec = View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.UNSPECIFIED);
            int widthSpec = View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.AT_MOST);
            view.measure(widthSpec, heightSpec);
        }
    }

    public ZoomOnTouchListener getZoomOnTouchListener()
    {
        return zoomOnTouchListener;
    }



    private LinkedList<Pair<Integer, Rect>> getGroupedLayoutRectsDown(int startPosition, int offset)
    {
        log.d("getGroupedLayoutRectsDown");
        int beforeBase = (int)mZoomLevel;
        int afterBase = beforeBase + 1;
        float k = mZoomLevel - beforeBase;

        Rect visibleRect = getVisibleRect();
        LinkedList<Pair<Integer, Rect>> result = new LinkedList<Pair<Integer, Rect>>();

        boolean grouped = adapter.isGrouped();
        int mapGroupIndex = adapter.getGroupIndex(adapter.getItemId(startPosition));

        int childHeight = childSizeHelper.getHeight();
        int childWidth = childSizeHelper.getWidth();

        int restSpace = visibleRect.bottom - offset;



        if(k == 0.0f)
        {


            int mappedSpace = 0;
            while(mappedSpace < restSpace)
            {
                if(startPosition >= getItemCount())
                {
                    break;
                }
                long id = adapter.getItemId(startPosition);
                int groupIndex = adapter.getGroupIndex(id);
                int childIndex = adapter.getChildIndex(id);
                int type = adapter.getItemViewType(startPosition);

                if(groupIndex != mapGroupIndex)
                {
                    break;
                }


                if(type == VIEW_TYPE_UNZOOM)
                {
                    result.addLast(new Pair<Integer, Rect>(startPosition, new Rect(0, 0, 0, 0)));
                }else if(type == VIEW_TYPE_ZOOM)
                {
                    if(grouped)
                    {
                        childIndex -= 1;
                    }
                    int top = (childIndex / beforeBase) * (childHeight + verticalSpace);
                    int left = (childIndex % beforeBase) * (childWidth + horizontalSpace);
                    int bottom = top + childHeight;
                    int right = left + childWidth;

                    result.addLast(new Pair<Integer, Rect>(startPosition, new Rect(left, top, right, bottom)));
                }
                if(result.size() <= 1)
                {
                    mappedSpace = 0;
                }else
                {
                    int first = result.getFirst().second.top;
                    int last = result.getLast().second.top;
                    mappedSpace = last - first;
                }
                startPosition++;
                if(startPosition >= getItemCount())
                {
                    return result;
                }
            }


        }else
        {
            int beforeWidth = childSizeHelper.getWidth(beforeBase);
            int beforeHeight = childSizeHelper.getHeight(beforeBase);
            int afterHeight = childSizeHelper.getHeight(afterBase);
            int afterWidth = childSizeHelper.getWidth(afterBase);



            int mappedSpace = 0;
            while(mappedSpace <= restSpace)
            {
                if(startPosition >= getItemCount())
                {
                    break;
                }
                long id = adapter.getItemId(startPosition);
                int groupIndex = adapter.getGroupIndex(id);
                int childIndex = adapter.getChildIndex(id);
                int type = adapter.getItemViewType(startPosition);

                if(groupIndex != mapGroupIndex)
                {
                    break;
                }


                if(type == VIEW_TYPE_UNZOOM)
                {
                    result.addLast(new Pair<Integer, Rect>(startPosition, new Rect(0, 0, 0, 0)));
                }else if(type == VIEW_TYPE_ZOOM)
                {
                    if(grouped)
                    {
                        childIndex -= 1;
                    }
                    int beforeTop = (childIndex / beforeBase) * (beforeHeight + verticalSpace);
                    int beforeLeft = (childIndex % beforeBase) * (beforeWidth + horizontalSpace);
                    int beforeBottom = beforeTop + beforeHeight;
                    int beforeRight = beforeLeft + beforeWidth;

                    int afterTop = (childIndex / afterBase) * (afterHeight + verticalSpace);
                    int afterLeft = (childIndex % afterBase) * (afterWidth + horizontalSpace);
                    int afterBottom = afterTop + afterHeight;
                    int afterRight = afterLeft + afterWidth;

                    int top = beforeTop + (int)((afterTop - beforeTop) * k);
                    int left = beforeLeft + (int)((afterLeft - beforeLeft) * k);
                    int right = beforeRight + (int)((afterRight - beforeRight) * k);
                    int bottom = beforeBottom + (int)((afterBottom - beforeBottom) * k);
                    result.addLast(new Pair<Integer, Rect>(startPosition, new Rect(left, top, right, bottom)));
                }
                if(result.size() <= 1)
                {
                    mappedSpace = 0;
                }else
                {
                    int first = result.getFirst().second.top;
                    int last = result.getLast().second.top;
                    mappedSpace = last - first;
                }
                startPosition++;
                if(startPosition >= getItemCount())
                {
                    return result;
                }
            }
        }
        return result;
    }

    private LinkedList<Pair<Integer, Rect>> getGroupedLayoutRectsUp(int startPosition, int offset)
    {
        log.d("getGroupedLayoutRectsUp");
        int beforeBase = (int)mZoomLevel;
        int afterBase = beforeBase + 1;
        float k = mZoomLevel - beforeBase;

        Rect visibleRect = getVisibleRect();
        LinkedList<Pair<Integer, Rect>> result = new LinkedList<Pair<Integer, Rect>>();

        boolean grouped = adapter.isGrouped();
        int mapGroupIndex = adapter.getGroupIndex(adapter.getItemId(startPosition));

        int childHeight = childSizeHelper.getHeight();
        int childWidth = childSizeHelper.getWidth();

        int restSpace = offset - (visibleRect.top) + childHeight;



        if(restSpace <= 0)
        {
            return result;
        }

        if(k == 0.0f)
        {


            int mappedSpace = 0;
            while(mappedSpace < restSpace)
            {
                if(startPosition >= getItemCount())
                {
                    break;
                }
                long id = adapter.getItemId(startPosition);
                int groupIndex = adapter.getGroupIndex(id);
                int childIndex = adapter.getChildIndex(id);
                int type = adapter.getItemViewType(startPosition);

                if(groupIndex != mapGroupIndex)
                {
                    break;
                }


                if(type == VIEW_TYPE_UNZOOM)
                {
                    result.addFirst(new Pair<Integer, Rect>(startPosition, new Rect(0, 0, 0, 0)));
                }else if(type == VIEW_TYPE_ZOOM)
                {
                    if(grouped)
                    {
                        childIndex -= 1;
                    }
                    int top = (childIndex / beforeBase) * (childHeight + verticalSpace);
                    int left = (childIndex % beforeBase) * (childWidth + horizontalSpace);
                    int bottom = top + childHeight;
                    int right = left + childWidth;

                    result.addFirst(new Pair<Integer, Rect>(startPosition, new Rect(left, top, right, bottom)));
                }
                if(result.size() <= 1)
                {
                    mappedSpace = 0;
                }else
                {
                    int first = result.getFirst().second.bottom;
                    int last = result.getLast().second.top;
                    mappedSpace = Math.abs(last - first);
                }
                startPosition--;
                if(startPosition < 0)
                {
                    return result;
                }
            }


        }else
        {
            int beforeWidth = childSizeHelper.getWidth(beforeBase);
            int beforeHeight = childSizeHelper.getHeight(beforeBase);
            int afterHeight = childSizeHelper.getHeight(afterBase);
            int afterWidth = childSizeHelper.getWidth(afterBase);



            int mappedSpace = 0;
            while(mappedSpace <= restSpace)
            {
                if(startPosition >= getItemCount())
                {
                    break;
                }
                long id = adapter.getItemId(startPosition);
                int groupIndex = adapter.getGroupIndex(id);
                int childIndex = adapter.getChildIndex(id);
                int type = adapter.getItemViewType(startPosition);

                if(groupIndex != mapGroupIndex)
                {
                    break;
                }


                if(type == VIEW_TYPE_UNZOOM)
                {
                    result.addFirst(new Pair<Integer, Rect>(startPosition, new Rect(0, 0, 0, 0)));
                }else if(type == VIEW_TYPE_ZOOM)
                {
                    if(grouped)
                    {
                        childIndex -= 1;
                    }
                    int beforeTop = (childIndex / beforeBase) * (beforeHeight + verticalSpace);
                    int beforeLeft = (childIndex % beforeBase) * (beforeWidth + horizontalSpace);
                    int beforeBottom = beforeTop + beforeHeight;
                    int beforeRight = beforeLeft + beforeWidth;

                    int afterTop = (childIndex / afterBase) * (afterHeight + verticalSpace);
                    int afterLeft = (childIndex % afterBase) * (afterWidth + horizontalSpace);
                    int afterBottom = afterTop + afterHeight;
                    int afterRight = afterLeft + afterWidth;

                    int top = beforeTop + (int)((afterTop - beforeTop) * k);
                    int left = beforeLeft + (int)((afterLeft - beforeLeft) * k);
                    int right = beforeRight + (int)((afterRight - beforeRight) * k);
                    int bottom = beforeBottom + (int)((afterBottom - beforeBottom) * k);
                    result.addFirst(new Pair<Integer, Rect>(startPosition, new Rect(left, top, right, bottom)));
                }
                if(result.size() <= 1)
                {
                    mappedSpace = 0;
                }else
                {
                    int first = result.getFirst().second.bottom;
                    int last = result.getLast().second.top;
                    mappedSpace = Math.abs(last - first);
                }
                startPosition--;
                if(startPosition < 0)
                {
                    return result;
                }
            }
        }
        return result;
    }

    private void fillUpWhenScroll(RecyclerView.Recycler recycler)
    {
        if(getChildCount() == 0)
        {
            return;
        }

        Rect visibleRect = getVisibleRect();

        View firstView = getChildAt(0);
        int firstPosition = getPosition(firstView);
        int firstType = getItemViewType(firstView);
        int rightOffset = getDecoratedLeft(firstView) - horizontalSpace;
        int bottomOffset = getDecoratedBottom(firstView);
        while(true)
        {



            int nextPosition = firstPosition - 1;
            int nextType = adapter.getItemViewType(nextPosition);
            if(nextPosition < 0)
            {
                return;
            }
            if(firstType == VIEW_TYPE_UNZOOM)
            {

                if(nextType == VIEW_TYPE_UNZOOM)
                {
                    rightOffset = visibleRect.right;
                    bottomOffset = getDecoratedTop(firstView) - verticalSpace;
                }else if(nextType == VIEW_TYPE_ZOOM)
                {
                    int base = (int)mZoomLevel;
                    int childIndex = adapter.getChildIndex(adapter.getItemId(nextPosition));
                    if(adapter.isGrouped())
                    {
                        childIndex--;
                    }
                    int left = (childIndex % base) * (childSizeHelper.getWidth() + horizontalSpace);
                    int right = left + childSizeHelper.getWidth();

                    bottomOffset = getDecoratedTop(firstView) - verticalSpace;
                    rightOffset = right;
                }

            }else if(nextType == VIEW_TYPE_UNZOOM)
            {
                rightOffset = visibleRect.right;
                bottomOffset = getDecoratedTop(firstView) - verticalSpace;
            }else
            {
                if(rightOffset - visibleRect.left < childSizeHelper.getWidth())
                {
                    rightOffset = visibleRect.right;
                    bottomOffset = getDecoratedTop(firstView) - verticalSpace;
                }
            }

            View nextView = getAndSetViewForPosition(recycler, nextPosition);
            if(nextType == VIEW_TYPE_ZOOM)
            {
                addView(nextView, 0);
                measureChild(true, childSizeHelper.getWidth(), childSizeHelper.getHeight(), nextView);
                layoutDecorated(nextView, rightOffset - childSizeHelper.getWidth(), bottomOffset - childSizeHelper.getHeight(),
                        rightOffset, bottomOffset);

            }else if (nextType == VIEW_TYPE_UNZOOM)
            {
                addView(nextView, 0);
                measureChild(false, visibleRect.width(), 0, nextView);
                layoutDecorated(nextView, visibleRect.left, bottomOffset  - getDecoratedMeasuredHeight(nextView), visibleRect.right, bottomOffset);

            }

            firstPosition = nextPosition;
            firstType = nextType;
            rightOffset = getDecoratedLeft(nextView) - horizontalSpace;
            bottomOffset = getDecoratedBottom(nextView);
            firstView = nextView;
            if(bottomOffset < visibleRect.top)
            {
                break;
            }

        }
    }

    private void fillDownWhenScroll(RecyclerView.Recycler recycler)
    {
        if(getChildCount() == 0)
        {
            return;
        }

        Rect visibleRect = getVisibleRect();
        int itemCount = getItemCount();

        View lastView = getChildAt(getChildCount() - 1);
        int lastPosition = getPosition(lastView);
        int lastType = getItemViewType(lastView);
        int leftOffset = getDecoratedRight(lastView) + horizontalSpace;
        int topOffset = getDecoratedTop(lastView);
        while(true)
        {


            if(visibleRect.right - leftOffset < childSizeHelper.getWidth())
            {
                leftOffset = visibleRect.left;
                topOffset = getDecoratedBottom(lastView) + verticalSpace;
            }
            int nextPosition = lastPosition + 1;
            int nextType = adapter.getItemViewType(nextPosition);
            if(nextPosition >= itemCount)
            {
                return;
            }
            if(lastType == VIEW_TYPE_UNZOOM || nextType == VIEW_TYPE_UNZOOM)
            {

                leftOffset = visibleRect.left;
                topOffset = getDecoratedBottom(lastView) + verticalSpace;

            }

            View nextView = getAndSetViewForPosition(recycler, nextPosition);
            if(nextType == VIEW_TYPE_ZOOM)
            {
                addView(nextView);
                measureChild(true, childSizeHelper.getWidth(), childSizeHelper.getHeight(), nextView);
                layoutDecorated(nextView, leftOffset, topOffset,
                        leftOffset + childSizeHelper.getWidth(), topOffset + childSizeHelper.getHeight());

            }else if (nextType == VIEW_TYPE_UNZOOM)
            {
                addView(nextView);
                measureChild(false, visibleRect.width(), 0, nextView);
                layoutDecorated(nextView, visibleRect.left, topOffset, visibleRect.right, topOffset + getDecoratedMeasuredHeight(nextView));

            }

            lastPosition = nextPosition;
            lastType = nextType;
            leftOffset = getDecoratedRight(nextView) + horizontalSpace;
            topOffset = getDecoratedTop(nextView);
            lastView = nextView;
            if(topOffset > visibleRect.bottom)
            {
                break;
            }
        }
    }





    private Rect getVisibleRect()
    {
        int top = getPaddingTop();
        int bottom = getHeight() - getPaddingBottom() - getPaddingTop();
        int left = getPaddingLeft();
        int right = getWidth() - getPaddingRight() - getPaddingLeft();
        return new Rect(left, top, right, bottom);
    }

    private void zoomChild(Point zoomCenter)
    {
        zooming = true;
        if(getChildCount() == 0)
        {
            return;
        }
        log.d("zoom child count = " + getChildCount());
        View centerChild = null;
        for(int i = 0; i < getChildCount(); i++)
        {
            View view = getChildAt(i);
            if(getDecoratedTop(view) <= zoomCenter.y && getDecoratedBottom(view) + verticalSpace > zoomCenter.y)
            {
                centerChild = view;
            }
        }
        if(centerChild == null)
        {
            centerChild = getChildAt(getChildCount() - 1);
        }
        int offset = getDecoratedTop(centerChild);
        int position = getPosition(centerChild);
        detachAndScrapAttachedViews(mRecycler);
        log.d("after detach, scrap children = " + getChildCount());
        fillDown(position, offset, mRecycler);
        fillUp(position, offset, mRecycler);

        checkBoundsAndMoveChildren();
        removeChildrenFromTop(mRecycler);
        removeChildrenFromBottom(mRecycler);
        zooming = false;
    }

    private void checkBoundsAndMoveChildren()
    {
        if(getChildCount() == 0)
        {
            return;
        }
        Rect visibleRect = getVisibleRect();
        int realMoveY = 0;
        View first = getChildAt(0);
        View last = getChildAt(getChildCount() - 1);
        if(getPosition(last) == getItemCount() - 1)
        {
            if(getDecoratedBottom(last) + realMoveY < visibleRect.bottom)
            {
                realMoveY = visibleRect.bottom - getDecoratedBottom(last);
            }
        }

        if(getPosition(first) == 0)
        {
            if(getDecoratedTop(first) + realMoveY > visibleRect.top)
            {
                realMoveY = visibleRect.top - getDecoratedTop(first);
            }
        }

        if(realMoveY != 0)
        {
            offsetChildrenVertical(realMoveY);
        }
    }

    private void animateZoom(final Point center)
    {
        float nextStat = Math.round(mZoomLevel);
        ValueAnimator animator = ValueAnimator.ofFloat(mZoomLevel, nextStat);
        animator.setDuration(200);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {

                mZoomLevel = computeZoomLevel((float)animation.getAnimatedValue());
                zoomChild(center);
            }
        });
        animator.start();
    }

    @Override
    public boolean canScrollVertically() {
        return true;
    }

    @Override
    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
        scrolling = true;
        log.d("scrollVerticallyBy, dy = " + dy);
        log.d("scrap children = " + getChildCount());
        if(getItemCount() <= 0 || state.isPreLayout() || dy == 0)
        {
            return 0;
        }
        int realMoveY =  - dy;
        Rect visibleRect = getVisibleRect();
        View last = getChildAt(getChildCount() - 1);
        View first = getChildAt(0);
        if(last == null || first == null)
        {
            return 0;
        }
        if(getPosition(last) == getItemCount() - 1)
        {
            if(getDecoratedBottom(last) + realMoveY < visibleRect.bottom)
            {
                realMoveY = visibleRect.bottom - getDecoratedBottom(last);
            }
        }

        if(getPosition(first) == 0)
        {
            if(getDecoratedTop(first) + realMoveY > visibleRect.top)
            {
                realMoveY = visibleRect.top - getDecoratedTop(first);
            }
        }


        if(realMoveY != 0)
        {
            offsetChildrenVertical(realMoveY);
//            checkAndRemoveChild(0, recycler);
            if(realMoveY > 0)
            {
                first = getChildAt(0);
                int offset = getDecoratedTop(first);
                int position = getPosition(first);
                fillUpWhenScroll(recycler);
                removeChildrenFromBottom(recycler);
//                fillUp(position, offset, recycler);

            }else
            {
                last = getChildAt(getChildCount() - 1);
                int offset = getDecoratedTop(last);
                int position = getPosition(last);
                fillDownWhenScroll(recycler);
                removeChildrenFromTop(recycler);
//                fillDown(position, offset, recycler);

            }

        }

        scrolling = false;
        return -realMoveY;
    }

    @Override
    public void collectAdjacentPrefetchPositions(int dx, int dy, RecyclerView.State state, LayoutPrefetchRegistry layoutPrefetchRegistry) {
        log.d("collectAdjacentPrefetchPositions, dy = " + dy);
        dy = - dy;
        mRecycler.getScrapList();
        if(dy == 0 || getChildCount() == 0)
        {
            return;
        }else if(dy < 0)
        {
            View last = getChildAt(getChildCount() - 1);
            int lastPosition = getPosition(last);
            if(lastPosition == getItemCount() - 1)
            {
                return;
            }
//            layoutPrefetchRegistry.addPosition(lastPosition + 1, -dy);
        }else
        {
            View first = getChildAt(0);
            int firstPosition = getPosition(first);
            if(firstPosition == 0)
            {
                return;
            }
//            layoutPrefetchRegistry.addPosition(firstPosition - 1, dy);
        }
    }





    private void removeChildrenFromTop(RecyclerView.Recycler recycler)
    {
        Rect visibleRect = getVisibleRect();
        while(true)
        {
            if(getChildCount() <= 0)
            {
                return;
            }
            View view = getChildAt(0);
            if(getDecoratedBottom(view) < visibleRect.top)
            {
                removeAndRecycleView(view, recycler);
            }else
            {
                return;
            }

        }
    }

    private void removeChildrenFromBottom(RecyclerView.Recycler recycler)
    {
        Rect visibleRect = getVisibleRect();
        while(true)
        {
            if(getChildCount() <= 0)
            {
                return;
            }
            View view = getChildAt(getChildCount() - 1);
            if(getDecoratedTop(view) > visibleRect.bottom)
            {
                removeAndRecycleView(view, recycler);
            }else
            {
                return;
            }

        }
    }

    private HashSet<Integer> checkedItems = new HashSet<>();
    private HashSet<Integer> tempCheckedItems ;
    private boolean IN_MULTI_CHECK_MODE = false;
    private boolean IN_CONTINUE_CHECK_MODE = false;
    private int downPosition = -1;

    public boolean isChecked(int position)
    {
        if(checkedItems == null || checkedItems.size() == 0)
        {
            return false;
        }
        if(checkedItems.contains(position))
        {
            return true;
        }else
        {
            return false;
        }
    }

    private View getAndSetViewForPosition(RecyclerView.Recycler recycler, int position)
    {
        View view = recycler.getViewForPosition(position);
        if(view == null)
        {
            return null;
        }
        if(IN_MULTI_CHECK_MODE)
        {

            if(view instanceof CheckableView)
            {
                ((CheckableView) view).setCheckable(true);
                if(isChecked(position))
                {
                    ((CheckableView) view).setChecked(true);
                }else
                {
                    ((CheckableView) view).setChecked(false);
                }

            }
        }else
        {
            if(view instanceof CheckableView)
            {
                ((CheckableView) view).setCheckable(false);
            }
        }


        return  view;
    }

    private int getPressedPosition(int x, int y)
    {
        if(getChildCount() == 0)
        {
            return -1;
        }
        int result = -1;
        for(int i = 0; i < getChildCount(); i++)
        {
            View temp = getChildAt(i);
            if(getDecoratedTop(temp) <= y && getDecoratedBottom(temp) >= y &&
                    getDecoratedLeft(temp) <= x && getDecoratedRight(temp) >= x)
            {
                result = getPosition(temp);
            }
        }
        return result;
    }

    private void checkItems(int endX, int endY)
    {
        if(getChildCount() == 0)
        {
            return;
        }
//        if(tempCheckedItems == null)
//        {
//            tempCheckedItems = (HashSet<Integer>) checkedItems.clone();
//        }



        int k = getPressedPosition(endX, endY);
        if(k < 0)
        {
            return;
        }

        int min = Math.min(k, downPosition);
        int max = Math.max(k, downPosition);


        for(int i = 0; i < getChildCount(); i++)
        {
            View view = getChildAt(i);
            int position = getPosition(view);
            if(position >= min && position <= max)
            {

                if(tempCheckedItems.contains(position))
                {
                    if(view instanceof CheckableItem)
                    {
                        ((CheckableItem) view).setChecked(false);
                        checkedItems.remove(position);
                    }
                }else
                {
                    if(view instanceof CheckableItem)
                    {
                        ((CheckableItem) view).setChecked(true);
                        checkedItems.add(position);
                    }
                }
            }else
            {
                if(!tempCheckedItems.contains(position))
                {
                    if(view instanceof CheckableItem)
                    {
                        ((CheckableItem) view).setChecked(false);
                        checkedItems.remove(position);
                    }
                }else
                {
                    if(view instanceof CheckableItem)
                    {
                        ((CheckableItem) view).setChecked(true);
                        checkedItems.add(position);
                    }
                }
            }
        }

//        for(int i : tempCheckedItems) {
//            if (i < min || i > max)
//            {
//                checkedItems.add(i);
//            }
//        }

//        for(Integer i : tempCheckedItems)
//        {
//            if(i < min || i > max)
//            {
//                checkedItems.add(i);
//            }
//        }
//
//        for(int i = min; i <= max; i++)
//        {
//            if(!tempCheckedItems.contains(i))
//            {
//                checkedItems.add(i);
//            }
//        }

//        for(int i = 0; i < getChildCount(); i++)
//        {
//            View child = getChildAt(i);
//            int position = getPosition(child);
//            if(checkedItems.contains(position))
//            {
//                if(child instanceof CheckableItem)
//                {
//                    ((CheckableItem) child).setChecked(true);
//                }else
//                {
//                    tempCheckedItems.remove(position);
//                    checkedItems.remove(position);
//                }
//            }else
//            {
//                if(child instanceof CheckableItem)
//                {
//                    ((CheckableItem) child).setChecked(false);
//                }
//            }
//        }

        int[] result = new int[checkedItems.size()];
        int i = 0;
        for(int s : checkedItems)
        {
            result[i++] = s;
        }
        notifyOnItemCheckedListener(result);
    }

    public void setMultiCheckMode(boolean enable)
    {
        if(IN_MULTI_CHECK_MODE == enable)
        {
            return;
        }
        IN_MULTI_CHECK_MODE = enable;
        for(int i = 0; i < getChildCount(); i++)
        {
            View child = getChildAt(i);
            if(child instanceof CheckableItem)
            {
                ((CheckableItem) child).setCheckable(enable);
            }
        }
        if(!enable)
        {
            checkedItems.clear();
        }
    }

    public void addCheckedItems(int[] position)
    {
        if(!IN_MULTI_CHECK_MODE || position.length == 0)
        {
            return;
        }
        HashSet<Integer> pos = new HashSet<>(position.length);
        for(int i = 0; i < position.length; i++)
        {
            pos.add(position[i]);
        }
        for(int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            int p = getPosition(child);
            if (pos.contains(p)) {
                if (child instanceof CheckableItem) {
                    ((CheckableItem) child).setChecked(true);
                    checkedItems.add(p);
                }
            }

        }

    }



    public void onItemLongClicked(int position)
    {
        if(!IN_MULTI_CHECK_MODE)
        {
            notifyOnItemLongClickListener(position);
        }else
        {
            View targetView = null;
            for(int i = 0; i < getChildCount(); i++)
            {
                View temp = getChildAt(i);
                if(getPosition(temp) == position)
                {
                    targetView = temp;
                    break;
                }
            }
            if(targetView == null)
            {
                return;
            }
            if(targetView instanceof CheckableItem)
            {
                ((CheckableItem) targetView).setChecked(true);

                if(tempCheckedItems == null)
                {
                    tempCheckedItems = new HashSet<>();
                }
                tempCheckedItems =  (HashSet<Integer>) checkedItems.clone();
                checkedItems.add(position);
                downPosition = position;
                IN_CONTINUE_CHECK_MODE = true;
                int[] temp = new int[checkedItems.size()];
                int j = 0;
                for(int i : checkedItems)
                {
                    temp[j++] = i;
                }
                notifyOnItemCheckedListener(temp);
            }



        }

    }

    public void onItemClicked(int position)
    {
        if(IN_MULTI_CHECK_MODE)
        {
            View targetView = null;
            for(int i = 0; i < getChildCount(); i++)
            {
                View temp = getChildAt(i);
                if(getPosition(temp) == position)
                {
                    targetView = temp;
                    break;
                }
            }
            if(targetView == null)
            {
                return;
            }
            if(targetView instanceof CheckableItem)
            {
                if(((CheckableItem) targetView).isChecked())
                {
                    ((CheckableItem) targetView).setChecked(false);
                    checkedItems.remove(position);
                }else
                {
                    ((CheckableItem) targetView).setChecked(true);
                    checkedItems.add(position);
                }


                int[] temp = new int[checkedItems.size()];
                int j = 0;
                for(int i : checkedItems)
                {
                    temp[j++] = i;
                }
                notifyOnItemCheckedListener(temp);
            }
        }else
        {
            notifyOnItemClickListener(position);
        }
    }

    public interface OnItemClickListener
    {
        void onItemClicked(int position);
    }

    public interface OnItemLongClickListener
    {
        void onItemLongClicked(int position);
    }

    public interface OnItemCheckedListener
    {
        void onItemChecked(int[] checkedItemPosition);
    }


    private class ChildSizeHelper
    {
        private int widthCache = 0;
        private int heightCache = 0;
        private float zoomLevelCache = 0.0f;

        public int getWidth()
        {
            if(mZoomLevel != zoomLevelCache)
            {
                calculateSize();
            }
            return widthCache;
        }

        public int getHeight()
        {
            if(mZoomLevel != zoomLevelCache)
            {
                calculateSize();
            }
            return heightCache;
        }

        private void calculateSize()
        {
            Rect visibleRect = getVisibleRect();
            widthCache = (int)((visibleRect.width() - horizontalSpace * (mZoomLevel - 1.0f)) / mZoomLevel);
            heightCache = (int)(widthCache * heightToWidthRatio);
            zoomLevelCache = mZoomLevel;
        }

        public int getWidth(float zoomLevel)
        {
            Rect visibleRect = getVisibleRect();
            int result = (int)((visibleRect.width() - horizontalSpace * (mZoomLevel - 1.0f)) / mZoomLevel);
            return result;
        }

        public int getHeight(float zoomLevel)
        {
            int width = getWidth(zoomLevel);
            int height = (int)(width * heightToWidthRatio);
            return height;
        }
    }

    public class ZoomOnTouchListener implements View.OnTouchListener
    {
        ScaleGestureDetector scaleGestureDetector = new ScaleGestureDetector(mContext, new ScaleGestureDetector.OnScaleGestureListener() {
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                float scale = detector.getScaleFactor();
                mZoomLevel = computeZoomLevel(mZoomLevel / (scale));
                Point center = new Point((int)detector.getFocusX(), (int)detector.getFocusY());
                zoomChild(center);
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
        });
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            int pointCount = event.getPointerCount();
            if(pointCount >= 2)
            {
                scaleGestureDetector.onTouchEvent(event);
                return true;
            }else
            {
                if(event.getActionMasked() == MotionEvent.ACTION_UP)
                {
                    IN_CONTINUE_CHECK_MODE = false;

                }
                if(IN_CONTINUE_CHECK_MODE)
                {
                    checkItems((int)event.getX(), (int)event.getY());
                    return true;
                }
            }
            return false;
        }
    }


}
