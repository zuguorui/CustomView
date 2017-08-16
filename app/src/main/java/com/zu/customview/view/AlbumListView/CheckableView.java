package com.zu.customview.view.AlbumListView;

import android.content.Context;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.util.AttributeSet;
import android.widget.CheckBox;

/**
 * Created by zu on 17-7-24.
 */

public class CheckableView extends CheckableItem {
    private CheckBox checkBox;
    private boolean checkable = false;



    public CheckableView(@NonNull Context context) {
        this(context, null);
    }

    public CheckableView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CheckableView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public CheckableView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        checkBox = new CheckBox(context);
        MarginLayoutParams layoutParams = new MarginLayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        layoutParams.leftMargin = getPaddingLeft();
        layoutParams.topMargin = getPaddingTop();
        checkBox.setLayoutParams(layoutParams);
        addView(checkBox);

        checkBox.setVisibility(INVISIBLE);
    }

    @Override
    public void setCheckable(boolean checkable) {
        if(checkable)
        {
            checkBox.setVisibility(VISIBLE);
        }else
        {
            checkBox.setVisibility(INVISIBLE);
        }
        this.checkable = checkable;



    }

    @Override
    public void setChecked(boolean checked) {
        if(checkable)
        {
            checkBox.setChecked(checked);
        }
    }

    @Override
    public boolean isChecked() {
        if(!checkable)
        {
            return false;
        }
        return checkBox.isChecked();
    }

    @Override
    public void toggle() {
        if(checkable)
        {
            checkBox.setChecked(!checkBox.isChecked());
        }
    }
}
