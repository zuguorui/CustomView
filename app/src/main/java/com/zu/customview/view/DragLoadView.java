package com.zu.customview.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.FrameLayout;

/**
 * Created by zu on 2017/9/18.
 */

public abstract class DragLoadView extends FrameLayout {

    private OnLoadListener onLoadListener = null;
    private OnDragListener onDragListener = null;


    public DragLoadView(@NonNull Context context) {
        this(context, null);

    }

    public DragLoadView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DragLoadView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public DragLoadView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void setOnLoadListener(OnLoadListener loadListener)
    {
        this.onLoadListener = loadListener;
    }

    public void removeOnLoadListener()
    {
        this.onLoadListener = null;
    }

    public void setOnDragListener(OnDragListener listener)
    {
        this.onDragListener = listener;
    }

    public void removeDragListener()
    {
        this.onDragListener = null;
    }

    public void drag(float process){
        if(onDragListener != null)
        {
            onDragListener.onDrag(process);
        }
    }

    public void dragRelease(float process)
    {
        if(onDragListener != null)
        {
            onDragListener.onDragRelease(process);
        }
    }

    public void dragStart()
    {
        if(onDragListener != null)
        {
            onDragListener.onDragStart();
        }
    }

    public void loadComplete(boolean success)
    {
        if(onLoadListener != null)
        {
            onLoadListener.onLoadComplete(success);
        }
    }

    public void loadStart()
    {
        if(onLoadListener != null)
        {
            onLoadListener.onLoadStart();
        }
    }

    public void loadCancel()
    {
        if(onLoadListener != null)
        {
            onLoadListener.onLoadCancel();
        }
    }

    public abstract void viewHidden();



    public interface OnLoadListener{
        void onLoadComplete(boolean success);
        void onLoadStart();
        void onLoadCancel();
    }

    public interface OnDragListener{
        void onDrag(float process);
        void onDragRelease(float process);
        void onDragStart();
    }
}
