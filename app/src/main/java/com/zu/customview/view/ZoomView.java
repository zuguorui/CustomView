package com.zu.customview.view;

import android.animation.ValueAnimator;
import android.content.ClipData;
import android.content.Context;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.BoolRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.method.MovementMethod;
import android.text.method.MultiTapKeyListener;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.Scroller;

import com.zu.customview.MyLog;
import com.zu.customview.R;
import com.zu.customview.utils.DrawableBitmapUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;

/**
 * Created by zu on 17-4-28.
 */

public class ZoomView extends View {

    MyLog log = new MyLog("ZoomView", true);
    Scroller mScroller = null;
    private float zoomLevel = 2.0f;
    private int minZoomLevel = 2;
    private int maxZoomLevel = 6;

    private int verticalInterval = 3;
    private int horizontalInterval = 3;

    private float heightToWidthRatio = 1.0f;
    private int viewUnVisibleEdge = 1000;

    private int touchSlop = 5;

    private int initWidth;
    private int initHeight;
    private boolean hasGetInitLayoutParams = false;
    private boolean inReMeasure = false;

    private ItemManager itemManager = new ItemManager();

    private  Bitmap defaultCheckBack = null;
    private int checkBackColor = Color.parseColor("#999999");

    private LinkedList<ItemParams> preItems = new LinkedList<>();
    private LinkedList<ItemParams> shownItems = new LinkedList<>();
    private HashMap<Integer, AnimateParams> animateParamsHashMap = new HashMap<>();
    private HashSet<Integer> checkedItems = new HashSet<>();

    private long longClickTimeGate = 500;



    private DataSetObserver dataSetObserver = new DataSetObserver() {
        @Override
        public void onChanged() {

        }

        @Override
        public void onInvalidated() {

        }
    };


    public ZoomView(Context context) {
        this(context, null);
    }

    public ZoomView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ZoomView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public ZoomView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.ZoomView);
        zoomLevel = array.getFloat(R.styleable.ZoomView_zoomLevel, zoomLevel);

        minZoomLevel = array.getInteger(R.styleable.ZoomView_minZoomLevel, minZoomLevel);
        maxZoomLevel = array.getInteger(R.styleable.ZoomView_maxZoomLevel, maxZoomLevel);
        verticalInterval = array.getDimensionPixelSize(R.styleable.ZoomView_verticalInterval, verticalInterval);
        horizontalInterval = array.getDimensionPixelSize(R.styleable.ZoomView_horizontalInterval, horizontalInterval);

        heightToWidthRatio = array.getFloat(R.styleable.ZoomView_heightToWidthRatio, heightToWidthRatio);
        checkBackColor = array.getColor(R.styleable.ZoomView_checkBackColor, checkBackColor);
        array.recycle();
        itemManager = new ItemManager();
        zoomLevel = setZoomLevel(zoomLevel);
        if(isInEditMode())
        {
            createDebugData();
        }

        mScroller = new Scroller(context);

        Drawable drawable = new ColorDrawable(checkBackColor);
        defaultCheckBack = Bitmap.createBitmap(16, 16, Bitmap.Config.ARGB_4444);
        Canvas canvas = new Canvas(defaultCheckBack);
        drawable.setBounds(0, 0, 16, 16);
        drawable.draw(canvas);


        setClickable(true);
        setLongClickable(true);

    }

    private void createDebugData()
    {
        ColorDrawable drawable = new ColorDrawable(Color.parseColor("#88777777"));
        final Bitmap bitmap = Bitmap.createBitmap(64, 64, Bitmap.Config.ARGB_4444);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, 64, 64);
        drawable.draw(canvas);

        ItemAdapter itemAdapter = new ItemAdapter() {
            int count = 1000;
            @Override
            public Object getItem(int position, ItemManager.Callback callback) {

                if(position >= 0 && position < count)
                {
                    return bitmap;
                }else
                {
                    return null;
                }
            }

            @Override
            public int getCount() {
                return count;
            }

            @Override
            public Object getItem(int position) {
                if(position >= 0 && position < count)
                {
                    return bitmap;
                }else
                {
                    return null;
                }
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                return null;
            }
        };
        setAdapter(itemAdapter);
    }


    public float setZoomLevel(float zoomLevel)
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
        layout(0, null);
        return this.zoomLevel;
    }

    public void setZoomLevelAndPostInvalidate(float zoomLevel)
    {
        setZoomLevel(zoomLevel);
        postInvalidate();
    }



    public void setAdapter(ItemAdapter adapter)
    {
        if(itemManager == null)
        {
            itemManager = new ItemManager();
        }
        itemManager.mAdapter = adapter;
        itemManager.mAdapter.registerDataSetObserver(dataSetObserver);
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

    private void notifyOnItemLongClickListeners(final int position)
    {
        if(longClickListeners == null || longClickListeners.size() == 0)
        {
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                for(OnItemLongClickListener listener : longClickListeners)
                {
                    listener.onLongClick(position);
                }
            }
        }).start();
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

    private void notifyOnItemClickListeners(final int position)
    {
        if(clickListeners == null || clickListeners.size() == 0)
        {
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                for(OnItemClickListener listener : clickListeners)
                {
                    listener.onClick(position);
                }
            }
        }).start();
    }

    public void addOnMultiCheckeListener(OnMultiCheckListener listener)
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

    private void notifyOnMultiCheckListeners()
    {
        if(multiCheckListeners == null || multiCheckListeners.size() == 0)
        {
            return;
        }
        final Integer[] s = new Integer[checkedItems.size()];
        checkedItems.toArray(s);
        new Thread(new Runnable() {
            @Override
            public void run() {
                for(OnMultiCheckListener listener : multiCheckListeners)
                {
                    listener.onMultiCheck(s);
                }
            }
        }).start();
    }

    public void setCheckedItems(int[] positions)
    {
        if(positions.length == 0)
        {
            return;
        }
        HashSet<Integer> temp = new HashSet<>();
        for(int i = 0; i < positions.length; i++)
        {
            int position = positions[i];
            temp.add(position);
            if(!checkedItems.contains(position))
            {
                AnimateParams animateParams = new AnimateParams();
                animateParams.setChecked(true);
                animateParamsHashMap.put(position, animateParams);
            }

        }

        for(Integer i : checkedItems)
        {
            if(!temp.contains(i))
            {
                AnimateParams animateParams = new AnimateParams();
                animateParams.setChecked(true);
                animateParams.checkedTime = System.currentTimeMillis() - 100000;
                animateParams.setUnChecked(true);
                animateParamsHashMap.put(i, animateParams);
            }
        }

        checkedItems = temp;

        postInvalidate();
    }

    public void addCheckedItem(int position)
    {
        if(!checkedItems.contains(position))
        {
            checkedItems.add(position);
            AnimateParams animateParams = animateParamsHashMap.get(position);
            if(animateParams == null)
            {
                animateParams = new AnimateParams();
                animateParamsHashMap.put(position, animateParams);

            }
            animateParams.setChecked(true);
        }
    }

    public void removeCheckedItem(int position)
    {
        if(checkedItems.contains(position))
        {
            checkedItems.remove(position);
            AnimateParams animateParams = animateParamsHashMap.get(position);
            if(animateParams == null)
            {
                animateParams = new AnimateParams();
                animateParamsHashMap.put(position, animateParams);
                animateParams.setChecked(true);
                animateParams.checkedTime = 0;
            }
            animateParams.setUnChecked(true);
        }
    }

    public void setMultiCheckMode(boolean b)
    {
        MULTI_CHECK_MODE_FLAG = b;
    }

    public void setContinuousCheckMode(boolean b)
    {
        CONTINUOUS_CHECK_MODE_FLAG = b;
    }

    public boolean getMultiCheckMode()
    {
        return MULTI_CHECK_MODE_FLAG;
    }



    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        log.v("onMeasure");
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        measureSelf(widthMeasureSpec, heightMeasureSpec);
        if(!hasGetInitLayoutParams)
        {
            ViewGroup.LayoutParams layoutParams = getLayoutParams();
            initHeight = layoutParams.height;
            initWidth = layoutParams.width;
            hasGetInitLayoutParams = true;
        }

    }

    private void measureSelf(int widthMeasureSpec, int heightMeasureSpec)
    {
        int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int childWidth = (widthSize - getPaddingLeft() - getPaddingRight() - (int)(zoomLevel - 1) * horizontalInterval) / (int)zoomLevel;
        int childHeight =(childWidth <= 0 ? 10 : (int)(childWidth * heightToWidthRatio));

        int totalWidth = widthSize;
        int totalHeight = 0;
        int atMost = 2 << 30;

        if(heightMeasureSpec == atMost)
        {
            log.v("AT_MOST");
        }
        if(heightSpecMode == MeasureSpec.AT_MOST)
        {
            if (shownItems != null && shownItems.size() != 0)
            {
                totalHeight = shownItems.getLast().bottom - shownItems.getFirst().top + getPaddingTop() + getPaddingBottom();
            }else
            {
                if(itemManager.mAdapter != null)
                {
                    if(itemManager.mAdapter.getCount() % (int)zoomLevel != 0)
                    {
                        totalHeight = (itemManager.mAdapter.getCount() / (int)zoomLevel + 1) * childHeight;
                    }
                    else
                    {
                        totalHeight = itemManager.mAdapter.getCount() / (int)zoomLevel * childHeight;
                    }
                }else
                {
                    totalHeight = heightSize;
                }

            }
        }else
        {
            totalHeight = heightSize;
        }
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

    private void reMeasure()
    {
        if(inReMeasure)
        {
            inReMeasure = false;
            return;
        }
        if(initHeight != ViewGroup.LayoutParams.WRAP_CONTENT)
        {
            return;
        }
        if(shownItems == null || shownItems.size() == 0)
        {
            return;
        }
        requestLayout();
        inReMeasure = true;

    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        log.v("onLayout, changed = " + changed);
        if(changed)
        {
            layout(0, null);
        }
    }

    private void layout(int moveY, Point zoomCenter)
    {
        scrollChildren(moveY);
        addAndRemoveChildren();
        layoutChildren(zoomCenter, zoomLevel);
        invalidate();
        reMeasure();


    }

    private void scrollChildren(int moveY)
    {
//        log.v("scrollChildren前: preItem.size = " + preItems.size() + ", shownItem.size = " + shownItems.size());
//        log.v("scrollChildren");
//        log.v("scroll moveY = " + moveY);
        if(itemManager == null || itemManager.mAdapter == null)
        {
            return;
        }
        if(shownItems == null  || shownItems.size() == 0)
        {
            return;
        }
        if(moveY == 0)
        {
            return;
        }
        boolean dependBottom = true;
        int realMoveY = moveY;

        if(shownItems.getFirst().position == 0)
        {
            ItemParams firstView = shownItems.getFirst();
            if(firstView.top + moveY >= getPaddingTop())
            {
                realMoveY = getPaddingTop() - firstView.top;
                dependBottom = false;
            }else
            {
                dependBottom = true;
            }
        }

        if(dependBottom)
        {
            ItemParams lastView = shownItems.getLast();
            if(lastView.position == itemManager.mAdapter.getCount() - 1)
            {
                if(lastView.bottom + moveY < getMeasuredHeight() - getPaddingBottom())
                {
                    if(lastView.bottom <= getMeasuredHeight() - getPaddingBottom())
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
                        realMoveY = getMeasuredHeight() - getPaddingBottom() - lastView.bottom;
                    }

                }
            }
        }


//        log.v("scroll realMoveY = " + realMoveY);

        for(ItemParams v : shownItems)
        {
            v.top += realMoveY;
            v.bottom += realMoveY;

        }

//        log.v("scrollChildren后: preItem.size = " + preItems.size() + ", shownItem.size = " + shownItems.size());

    }

    private boolean addAndRemoveChildren()
    {
//        log.v("addAndRemoveChildren前: preItem.size = " + preItems.size() + ", shownItem.size = " + shownItems.size());
        if(itemManager == null || itemManager.mAdapter == null)
        {
            return false;
        }
//        log.v("addAndRemoveChildren");
        boolean changed = false;
        int width = (getMeasuredWidth() - getPaddingLeft() - getPaddingRight() - (int)(zoomLevel - 1) * horizontalInterval) / (int)zoomLevel;
        int height =(int)(width * heightToWidthRatio);
        viewUnVisibleEdge = 2 * height;

        /**删除多出来的子view*/
        if(preItems != null && preItems.size() != 0)
        {
//            log.v("删除上面多出来的子view");
            do{
                ItemParams itemParams = preItems.getFirst();
                if(itemParams.bottom < -viewUnVisibleEdge)
                {
                    removeItem(itemParams);
                    preItems.removeFirst();

                }else
                {
                    break;
                }
                if(preItems.size() == 0)
                {
                    break;
                }
            }while(true);
        }
        if(preItems != null && preItems.size() != 0)
        {
//            log.v("删除下面多出来的子view");
            do{
                ItemParams itemParams = preItems.getLast();
                if(itemParams.top > getMeasuredHeight() + viewUnVisibleEdge)
                {
                    removeItem(itemParams);
                    preItems.removeLast();
                }else
                {
                    break;
                }
                if(preItems.size() == 0)
                {
                    break;
                }
            }while(true);
        }




//        log.v("add up view");
        boolean hasBeenEmpty = false;
        ItemParams itemParams = null;
        int startPosition = 0;
        int freeHeight = viewUnVisibleEdge;
        if(preItems == null || preItems.size() == 0)
        {

            freeHeight = viewUnVisibleEdge;
            startPosition = -1;
            hasBeenEmpty = true;
        }else
        {
            itemParams = preItems.getFirst();
            freeHeight = itemParams.top - verticalInterval - (-viewUnVisibleEdge);
            startPosition = itemParams.position - 1;
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
                ItemParams newItem = new ItemParams();
                newItem.position = position;


                preItems.addFirst(newItem);
                changed = true;
            }
        }


//        log.v("add below view");
        itemParams = null;
        freeHeight = getMeasuredHeight() + viewUnVisibleEdge;
        startPosition = 0;
        if(hasBeenEmpty)
        {
            startPosition = 0;
            freeHeight = getMeasuredHeight() + viewUnVisibleEdge;
        }else
        {
            itemParams = preItems.getLast();
            freeHeight = getMeasuredHeight() + viewUnVisibleEdge - itemParams.bottom - verticalInterval;
            startPosition = itemParams.position + 1;
        }
        if(freeHeight > 0)
        {
            int rows = (int)(freeHeight / (height + verticalInterval) + 1);
            int cols = (int)((getMeasuredWidth() - getPaddingRight() - getPaddingLeft() - (int)(zoomLevel - 1) * horizontalInterval) / (width));
//            log.v("add below view, row = " + rows + ", col = " + cols);
            for(int i = 0; i < rows * cols; i++)
            {
                int position = startPosition + i;
                if(position >= itemManager.mAdapter.getCount())
                {
                    break;
                }
                ItemParams newItem = new ItemParams();
                newItem.position = position;
                preItems.addLast(newItem);
                changed = true;
            }
        }
//        log.v("onScreenViews.size() = " + onScreenViews.size());
//        log.v("showViews.size() = " + showViews.size());
//        log.v("addAndRemoveChildren后: preItem.size = " + preItems.size() + ", shownItem.size = " + shownItems.size());
        return changed;

    }

    private void layoutChildren(Point zoomCenter, float zoomLevel)
    {
//        log.v("layoutChildren前: preItem.size = " + preItems.size() + ", shownItem.size = " + shownItems.size());
//        log.v("layoutChildren");
        if(itemManager == null || itemManager.mAdapter == null)
        {
            return;
        }
        if(preItems == null || preItems.size() == 0)
        {
            return;
        }


        /*确定布局的基准线*/

        ItemParams centerItem = null;
        if(zoomCenter == null)
        {
            if(shownItems != null && shownItems.size() != 0)
            {
                centerItem = shownItems.getFirst();

            }else
            {
                centerItem = null;
            }
        }else {
            if (shownItems != null && shownItems.size() != 0) {
                for (ItemParams v : shownItems) {
                    if (v.top - verticalInterval <= zoomCenter.y && v.bottom > zoomCenter.y
                            ) {
                        centerItem = v;

                        break;
                    }

                }
            } else{
                centerItem = null;
            }

            if(centerItem == null)
            {
                if(shownItems != null && shownItems.size() != 0)
                {
                    centerItem = shownItems.getFirst();
                }
            }
        }

        ArrayList<ItemParams> params = createLayoutModel(zoomLevel);

        /**
         * offset是布局模板与真正的布局之间的差距，当缩放中心不在子view上或者目前没有子view（即centerView == null）时，
         * offset的值为布局的paddingTop。不必担心会在最后的布局中多加paddingTop，因为除去没有子view的情况，如果有子view的话，
         * 而centerView == null，那么子view的上或下边界肯定已经出现在视图范围内，那么offset会在检测上下边界的过程中重新测量。
         * */
        int offset = 0;
        if(centerItem == null)
        {
            /*如果此时没有子view，那么布局模板与真正布局的位置差距就是paddingTop，如果有子view，那么offset根据子view的位置确定。*/
            if(shownItems == null || shownItems.size() == 0)
            {
                offset = getPaddingTop();
            }else
            {
                offset = 0;
            }

        }else
        {
            for(ItemParams param : params)
            {
                if(param.position == centerItem.position)
                {
                    offset = centerItem.top - param.top;
                    break;
                }
            }
//            log.v("position = " + centerView.position + ", offset = " + offset);
        }
        int firstOffset = offset;
//        log.v("first offset = " + offset);

        if(params.get(params.size() - 1).position == itemManager.mAdapter.getCount() - 1)
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

        ListIterator<ItemParams> iterator = preItems.listIterator();
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
            ItemParams v = iterator.next();
            ItemParams param = params.get(i);
//            v.view.measure(widthSpec, heightSpec);
            v.left = param.left + paddingLeft;
            v.top = param.top + offset;
            v.right = param.right + paddingLeft;
            v.bottom = param.bottom + offset;
            addItem(v);
            i++;
        }
//        log.v("layoutChildren后: preItem.size = " + preItems.size() + ", shownItem.size = " + shownItems.size());

    }

    private ArrayList<ItemParams> createLayoutModel(float zoomLevel)
    {
        if(preItems == null || preItems.size() == 0)
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
        int count = preItems.size();
        ArrayList<ItemParams> layoutModel = new ArrayList<>(count);

        for(ItemParams v : preItems)
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

            ItemParams params = new ItemParams(top, bottom, left, right, v.position);
            layoutModel.add(params);

        }

        return layoutModel;

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(shownItems == null || shownItems.size() == 0)
        {
            return;
        }
        canvas.clipRect(getPaddingLeft(), getPaddingTop(), getMeasuredWidth() - getPaddingRight(), getMeasuredHeight() - getPaddingBottom());
        ListIterator<ItemParams> iterator = shownItems.listIterator();
        int i = 0;
        while(iterator.hasNext())
        {
            ItemParams params = iterator.next();
            if(params.bottom < getPaddingTop() || params.top > getMeasuredHeight() - getPaddingBottom())
            {
                continue;
            }
            Bitmap bitmap = itemManager.getBitmap(params.position);

//            drawable.setBounds(0, 0, params.getWidth(), params.getHeight());
//            canvas.clipRect(params.left, params.top, params.right, params.bottom);
//            drawable.draw(canvas);

            drawItemWithAnimate(canvas, bitmap, params);
            canvas.getClipBounds();
            i++;

        }
//        log.v("共画出" + i +"个子view");


    }

    private void drawItemWithAnimate(Canvas canvas, Bitmap bitmap, ItemParams itemParams)
    {
        log.v("animateParamsHashMap.size = " + animateParamsHashMap.size());

        AnimateParams animateParams = animateParamsHashMap.get(itemParams.position);
        boolean animated = false;
        if(animateParams == null)
        {
            if(checkedItems.contains(itemParams.position))
            {
                log.v("checkedItems.size = " + checkedItems.size());
                animated = drawCheck(canvas, bitmap, itemParams, animateParams);
            }else
            {
                Rect rect = new Rect(itemParams.left, itemParams.top, itemParams.right, itemParams.bottom);
                canvas.drawBitmap(bitmap, null, rect, null);
                return;
            }
        }else
        {

            if(animateParams.pressed)
            {
                log.v("draw press");
                animated = drawPress(canvas, bitmap, itemParams, animateParams);
            }else if(animateParams.checked)
            {
                log.v("draw check");
                log.v(animateParams.toString());
                animated = drawCheck(canvas, bitmap, itemParams, animateParams);
            }else if(animateParams.inserting)
            {

            }else if(animateParams.deleting)
            {

            }else
            {
                Rect rect = new Rect(itemParams.left, itemParams.top, itemParams.right, itemParams.bottom);
                canvas.drawBitmap(bitmap, null, rect, null);
            }
        }
        if(!animated)
        {
            animateParamsHashMap.remove(itemParams.position);
        }
        if(animateParamsHashMap.size() != 0)
        {
            postInvalidate();
        }
    }

    private boolean drawPress(Canvas canvas, Bitmap bitmap, ItemParams itemParams, AnimateParams animateParams)
    {
        boolean animated = false;
        Rect rect = new Rect();
        animated = AnimateComputer.computePressRect(itemParams, animateParams, rect);
        canvas.drawBitmap(bitmap, null, rect, null);
        return animated;
    }

    private boolean drawCheck(Canvas canvas, Bitmap bitmap, ItemParams itemParams, AnimateParams animateParams)
    {
        boolean animated = false;
        Rect rect = new Rect();
        animated = AnimateComputer.computeCheckRect(itemParams, animateParams, rect);
        if(rect.top != itemParams.top || rect.left != itemParams.left || rect.right != itemParams.right || rect.bottom != itemParams.bottom)
        {
            Rect rect1 = new Rect(itemParams.left, itemParams.top, itemParams.right, itemParams.bottom);
            canvas.drawBitmap(defaultCheckBack, null, rect1, null);
        }
        canvas.drawBitmap(bitmap, null, rect, null);
        return animated;
    }

    private void drawInserting(Canvas canvas, Bitmap bitmap, ItemParams itemParams, AnimateParams animateParams)
    {

    }

    private void drawDeleting(Canvas canvas, Bitmap bitmap, ItemParams itemParams, AnimateParams animateParams)
    {

    }

    private void addItem(ItemParams params)
    {

        if(shownItems != null && shownItems.size() != 0)
        {
            if(shownItems.contains(params))
            {
                return;
            }
            if(params.position < shownItems.getFirst().position)
            {
                shownItems.addFirst(params);
            }else
            {

                shownItems.addLast(params);
            }
        }else
        {
            shownItems.addLast(params);
        }

    }

    private void removeItem(ItemParams params)
    {
        if(shownItems != null && shownItems.size() != 0)
        {
            shownItems.remove(params);
        }
    }

    int lastY = 0;
    @Override
    public void computeScroll() {
        super.computeScroll();
        if(mScroller.computeScrollOffset())
        {
            int newY = mScroller.getCurrY();
//            log.v("computeScroll, dy = " + (newY - lastY));

            layout(newY - lastY, null);
            lastY = newY;
        }
    }

    private float newX, newY, oldX, oldY, dx, dy, downX, downY;
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

    private boolean MULTI_CHECK_MODE_FLAG = false;
    private boolean CONTINUOUS_CHECK_MODE_FLAG = false;
    private ItemParams pressedItem = null;

    private boolean LONG_CLICKED_FLAG = false;



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
        postInvalidate();
        return true;
    }

    private boolean dealWithSingleGesture(MotionEvent event)
    {
//        log.v("single gesture");
//        log.v("single event : " + event.getAction());
        if(!isClickable() && !isLongClickable())
        {
            return false;
        }
        detectScroll(event);
        detectPress(event);

        return true;
    }

    private void detectScroll(MotionEvent event)
    {
        createVelocityTracker(event);
        switch (event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
//                Log.v("detecateScroll", "ACTION_DOWN");
                newX = event.getRawX();
                newY = event.getRawY();
                mScroller.forceFinished(true);

                break;
            case MotionEvent.ACTION_MOVE:
//                Log.v("detecateScroll", "ACTION_MOVE");
                if(zoomed == false)
                {
                    oldX = newX;
                    oldY = newY;
                    newX = event.getRawX();
                    newY = event.getRawY();
                    dx = newX - oldX;
                    dy = newY - oldY;
                    if((Math.abs(dy) >= touchSlop || moved) && !CONTINUOUS_CHECK_MODE_FLAG )
                    {
                        layout((int)dy, null);
                        moved = true;
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
//                Log.v("detecateScroll", "ACTION_UP");
                float speed = getVelocityTracker();
                recycleVelocityTracker();
//                log.v("ACTION_UP speed = " + (int)speed);
                fling((int)speed);

                break;
            default:
                break;
        }
    }

    private void detectPress(MotionEvent event)
    {
//        Log.v("detecatePress", "moved = " + moved + ", zoomed = " + zoomed);
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
                AnimateParams animateParams = animateParamsHashMap.get(pressedItem.position);
                if(animateParams != null)
                {
                    animateParams.setReleased(true);
                }
                pressedItem = null;
            }

        }
        switch (event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
//                Log.v("detecatePress", "ACTION_DOWN");
                downX = event.getX();
                downY = event.getY();
                if(!zoomed && !moved && pressedItem == null)
                {
                    pressedItem = getPressedItem((int)event.getX(), (int)event.getY());
                    if(pressedItem != null && !MULTI_CHECK_MODE_FLAG)
                    {
                        AnimateParams animateParams = animateParamsHashMap.get(pressedItem.position);
                        {
                            if(animateParams == null)
                            {
                                animateParams = new AnimateParams();
                            }
                        }
                        animateParams.setPressed(true);
                        animateParams.setReleased(false);
                        animateParamsHashMap.put(pressedItem.position, animateParams);
                    }
                }
                break;
            case MotionEvent.ACTION_MOVE:
//                Log.v("detecatePress", "ACTION_MOVE");

                oldX = newX;
                oldY = newY;
                newX = event.getRawX();
                newY = event.getRawY();
                dx = newX - oldX;
                dy = newY - oldY;
                if(MULTI_CHECK_MODE_FLAG && CONTINUOUS_CHECK_MODE_FLAG && !LONG_CLICKED_FLAG)
                {
                    int x = (int)event.getX();
                    int y = (int)event.getY();
                    checkItems((int)downX, (int)downY, x, y);
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
                        AnimateParams params = animateParamsHashMap.get(pressedItem.position);
                        if(params != null)
                        {
                            params.setReleased(true);
                        }
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


    private ItemParams getPressedItem(int x, int y)
    {
        if(shownItems == null || shownItems.size() == 0)
        {
            return null;
        }
        ItemParams result = null;
        ListIterator<ItemParams> iterator = shownItems.listIterator();
        while(iterator.hasNext())
        {
            ItemParams temp = iterator.next();
            if(temp.top <= y && temp.bottom >= y && temp.left <= x && temp.right >= x)
            {
                result = temp;
            }
        }
        return result;
    }



    private void checkItems(int startX, int startY, int endX, int endY)
    {
        if(shownItems == null || shownItems.size() == 0)
        {
            return;
        }
        if(tempCheckedItems == null)
        {
            tempCheckedItems = (HashSet<Integer>) checkedItems.clone();
        }
        int topX, topY, bottomX, bottomY;


        if(startY >= endY)
        {
            topY = endY;
            topX = endX;
            bottomY = startY;
            bottomX = startX;
        }else
        {
            topY = startY;
            topX = startX;
            bottomY = endY;
            bottomX = endX;
        }
        ListIterator<ItemParams> iterator = shownItems.listIterator();
        boolean detectFirst = false;
        boolean detectLast = false;
        while(iterator.hasNext())
        {
            ItemParams temp = iterator.next();

            if(!detectFirst)
            {
                if (temp.top <= topY && temp.bottom >= topY && temp.left <= topX && temp.right >= topX)
                {
                    detectFirst = true;
                }
            }

            boolean check = false;
            if(detectFirst && !detectLast)
            {
                /*means it is in our operate range*/
                check = !tempCheckedItems.contains(temp.position);
            }else
            {
                /*not in operate range*/
                check  = tempCheckedItems.contains(temp.position);

            }

            if(check)
            {
                if(!checkedItems.contains(temp.position))
                {
                    AnimateParams animateParams = animateParamsHashMap.get(temp.position);
                    if(animateParams == null)
                    {
                        animateParams = new AnimateParams();
                        animateParamsHashMap.put(temp.position, animateParams);
                    }
                    animateParams.setChecked(true);
                    checkedItems.add(temp.position);
                }
            }else
            {
                if(checkedItems.contains(temp.position))
                {
                    AnimateParams animateParams = animateParamsHashMap.get(temp.position);
                    if(animateParams == null)
                    {
                        animateParams = new AnimateParams();
                        animateParamsHashMap.put(temp.position, animateParams);
                        animateParams.setChecked(true);
                        animateParams.checkedTime = 0;
                    }
                    animateParams.setUnChecked(true);
                    checkedItems.remove(temp.position);
                }
            }

            if(!detectLast)
            {
                if(temp.top <= bottomY && temp.bottom >= bottomY && temp.left <= bottomX && temp.right >= bottomX)
                {
                    detectLast = true;
                }
            }


        }


        notifyOnMultiCheckListeners();


    }

    private void performClick(ItemParams clickedItem)
    {
        if(MULTI_CHECK_MODE_FLAG)
        {
            if(checkedItems.contains(clickedItem.position))
            {
                removeCheckedItem(clickedItem.position);
            }else
            {
                addCheckedItem(clickedItem.position);
            }

            notifyOnMultiCheckListeners();
        }else
        {
            notifyOnItemClickListeners(clickedItem.position);
        }
    }

    private void performLongClick(ItemParams longClickedItem)
    {
        notifyOnItemLongClickListeners(longClickedItem.position);
    }

    private boolean dealWithMultiGesture(MotionEvent event)
    {
        LONG_CLICKED_FLAG = false;
//        log.v("multi gesture");
//        log.v("event : " + event.getActionMasked());
        if(mScaleGestureDetector == null)
        {
            mScaleGestureDetector = new ScaleGestureDetector(getContext(), new MyScaleListener());
        }
        mScaleGestureDetector.onTouchEvent(event);

        return true;
    }

    private void fling(int pixelPerSecond)
    {
        if (mScroller == null)
        {
            mScroller = new Scroller(getContext());
        }

        lastY = 0;
        log.v("fling speed = " + pixelPerSecond);
        mScroller.fling(0, 0, 0, pixelPerSecond, 0, 0, -200000, 200000);


    }

    private void smoothScroll(int offset)
    {
        if (mScroller == null)
        {
            mScroller = new Scroller(getContext());
        }
        mScroller.startScroll(0, 0, 0, offset);
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
        return velocityTracker.getYVelocity();
    }

    private class MyScaleListener implements ScaleGestureDetector.OnScaleGestureListener
    {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float scale = detector.getScaleFactor();
            zoomLevel = setZoomLevel(zoomLevel / ( scale));
            Point center = new Point((int)detector.getFocusX(), (int)detector.getFocusY());
            layout(0, center);
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

    public interface OnItemClickListener
    {
        void onClick(int position);
    }

    public interface OnItemLongClickListener
    {
        void onLongClick(int position);
    }

    public interface OnMultiCheckListener
    {
        void onMultiCheck(Integer[] checkedItemsPosition);
    }

    private static class AnimateComputer{
        private static long pressDuration = 100;
        private static int pressEdge = 10;

        private static long checkDuration = 120;
        private static int checkEdge = 10;

        public static boolean computePressRect(ItemParams itemParams, AnimateParams animateParams, @NonNull Rect rect)
        {
//            Log.v("computePressRect", "pressed = " + animateParams.pressed + ", released = " + animateParams.released);
            boolean animated = false;
            int centerY = itemParams.top + (itemParams.getHeight() / 2);
            int centerX = itemParams.left + (itemParams.getWidth() / 2);
            long currentTime = System.currentTimeMillis();
            long pressedTime = animateParams.pressedTime;


            if(!animateParams.pressed)
            {
                rect.top = itemParams.top;
                rect.left = itemParams.left;
                rect.bottom = itemParams.bottom;
                rect.right = itemParams.right;
                return false;
            }
            /*percent == 0表示完全缩小，percent == 1表示正常尺寸*/
            float percent = 0;
            if(animateParams.released == false)
            {
                if(currentTime - pressedTime > pressDuration)
                {
                    percent = 0;
                }else
                {
                    percent = ((float)Math.abs(currentTime - animateParams.pressedTime - pressDuration)) / pressDuration;
                }
            }else
            {
                if(animateParams.releasedTime - pressedTime <= pressDuration)
                {
                    percent = ((float)Math.abs(currentTime - animateParams.pressedTime - pressDuration)) / pressDuration;
                }else{
                    percent = ((float)Math.abs(currentTime - animateParams.releasedTime)) / pressDuration;
                }
            }

//            float lastPercent = ((float)(animateParams.releasedTime - animateParams.pressedTime)) / pressDuration;
//            lastPercent = (lastPercent > 1.0f ? 1.0f : lastPercent);
//            percent = lastPercent * (1.0f - (currentTime - animateParams.releasedTime) / pressDuration);

            if(percent >= 1.0f)
            {
                animated = false;
            }else
            {
                animated = true;
            }
            int edge = (int)(pressEdge * (1 - percent));

            rect.top = itemParams.top + edge;
            rect.bottom = itemParams.bottom - edge;
            rect.left = itemParams.left + edge;
            rect.right = itemParams.right - edge;
            return animated;
        }

        public static boolean computeCheckRect(ItemParams itemParams, AnimateParams animateParams, @NonNull Rect rect)
        {
            boolean animated = false;
            int centerY = itemParams.top + (itemParams.getHeight() / 2);
            int centerX = itemParams.left + (itemParams.getWidth() / 2);
            long currentTime = System.currentTimeMillis();
            float percent = 0;
            if(animateParams == null)
            {
                percent = 1;
                animated = false;
            }else
            {
                /*percent == 0表示完整尺寸，percent == 1表示缩放到最小*/
                if(animateParams.unChecked == false)
                {
                    percent = ((float)Math.abs(currentTime - animateParams.checkedTime)) / checkDuration;
                }else
                {
                    float lastPercent = 1.0f;
                    if(animateParams.unCheckedTime - animateParams.checkedTime >= checkDuration)
                    {
                        lastPercent = 1.0f;
                    }else
                    {
                        lastPercent = ((float)Math.abs(animateParams.unCheckedTime - animateParams.checkedTime)) / checkDuration;
                    }
                    percent = lastPercent * (1.0f - ((float)Math.abs(currentTime - animateParams.unCheckedTime)) / checkDuration);
                }
            }

            if(percent <= 0.0f)
            {
                percent = 0.0f;
                animated = false;
            }else if(percent >= 1.0f)
            {
                percent = 1.0f;
                animated = false;
            }else
            {
                animated = true;
            }



            int edge = (int)(checkEdge * percent);

            rect.top = itemParams.top + edge;
            rect.bottom = itemParams.bottom - edge;
            rect.left = itemParams.left + edge;
            rect.right = itemParams.right - edge;
            return animated;
        }
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

    private class ItemParams
    {
        public int top;
        public int bottom;
        public int left;
        public int right;
        public int position;


        public ItemParams(int top, int bottom, int left, int right, int position) {
            this.top = top;
            this.bottom = bottom;
            this.left = left;
            this.right = right;
            this.position = position;
        }

        public ItemParams(){}

        public int getHeight()
        {
            if(position == -1)
            {
                return -1;
            }else
            {
                return bottom - top;
            }
        }

        public int getWidth()
        {
            if(position == -1)
            {
                return -1;
            }else
            {
                return right - left;
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ItemParams)) return false;

            ItemParams that = (ItemParams) o;

            if (top != that.top) return false;
            if (bottom != that.bottom) return false;
            if (left != that.left) return false;
            if (right != that.right) return false;
            return position == that.position;

        }

        @Override
        public int hashCode() {
            int result = top;
            result = 31 * result + bottom;
            result = 31 * result + left;
            result = 31 * result + right;
            result = 31 * result + position;
            return result;
        }
    }

    public class ItemManager
    {
        public int cacheSize = 12;
        public ItemAdapter mAdapter = null;
        private LinkedList<Pair<Integer, Runnable>> backgroundTasks = new LinkedList<>();
        private Thread serialTaskExecutor = null;
        private TreeMap<Integer, Bitmap> itemViews = new TreeMap<>();



        public Bitmap getBitmap(int position)
        {
            if(mAdapter == null)
            {
                return null;
            }


            if(itemViews != null && itemViews.size() != 0 && preItems != null && preItems.size() != 0)
            {
                deleteUnUsedItems();
            }
            Bitmap result = null;
            result = itemViews.get(position);
            if(result == null)
            {
                Object temp = getItemFromAdapter(position);
                if(temp == null)
                {
                    return null;
                }else if(temp instanceof Bitmap)
                {
                    result = (Bitmap) temp;
                    itemViews.put(position, result);
                }else if(temp instanceof Runnable)
                {
                    result = null;
                    backgroundTasks.addLast(new Pair<Integer, Runnable>(position, (Runnable)temp));
                }
            }
            return result;

        }

        private Object getItemFromAdapter(int position)
        {
            if(mAdapter == null)
            {
                return null;
            }
            Object result = mAdapter.getItem(position, new Callback(position));
            return result;
        }

        private void deleteUnUsedItems()
        {
            if(preItems == null || preItems.size() == 0)
            {
                return;
            }
            if(itemViews == null || itemViews.size() == 0)
            {
                return;
            }
            Object[] keys = itemViews.keySet().toArray();
            int first = preItems.getFirst().position;
            for(int i = 0; i < keys.length; i++)
            {
                int position = (int)keys[i];
                if(position < first - cacheSize)
                {
                    Bitmap o = itemViews.remove(position);
                    if(o != null)
                    {
                        o.recycle();
                    }
                }
            }

            int last = preItems.getLast().position;
            for(int i = keys.length - 1; i >= 0; i--)
            {
                int position = (int)keys[i];
                if(position > last + cacheSize)
                {
                    Bitmap o = itemViews.remove(position);
                    if(o != null)
                    {
                        o.recycle();
                    }
                }
            }
        }

        public void executeBackgroundTask()
        {
            if(backgroundTasks != null && backgroundTasks.size() != 0)
            {
                if(serialTaskExecutor == null)
                {
                    serialTaskExecutor = new Thread(){
                        @Override
                        public void run() {
                            backgroundTasks.pop().second.run();
                            executeBackgroundTask();
                        }
                    };
                }

            }
        }

        public class Callback
        {
            private int position = -1;

            public Callback(int position) {
                this.position = position;
            }

            public void call(Bitmap bitmap)
            {
                if(bitmap != null || bitmap instanceof Bitmap)
                {
                    ItemManager.this.itemViews.put(position, bitmap);
                }
            }
        }



    }

}


