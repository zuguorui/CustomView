package com.zu.customview.view.AlbumListView;

import android.content.Context;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Checkable;
import android.widget.FrameLayout;

/**
 * Created by zu on 17-5-27.
 */

public abstract class CheckableItem extends FrameLayout implements Checkable
{

    private View contentView = null;
    public CheckableItem(@NonNull Context context) {
        this(context, null);
    }

    public CheckableItem(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CheckableItem(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public CheckableItem(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public abstract void setCheckable(boolean checkable);
    public void setContentView(View view)
    {
        if(contentView != null)
        {
            removeView(contentView);
        }
        addView(view, 0);
        contentView = view;

    }

    public View getContentView()
    {
        return contentView;
    }

}
