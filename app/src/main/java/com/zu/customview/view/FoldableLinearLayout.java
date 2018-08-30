package com.zu.customview.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import com.zu.customview.R;
import com.zu.customview.utils.ChartUtil;

/**
 * Created by rikson on 2018/8/9.
 */

public class FoldableLinearLayout extends LinearLayout {
    private float foladPercent = 0.5f;
    private float currentPercent;
    private int animationDuration = 500;

    private ValueAnimator foldAnimator;
    private ValueAnimator.AnimatorUpdateListener listener = new ValueAnimator.AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            currentPercent = (float)animation.getAnimatedValue();
            requestLayout();
        }
    };
    public FoldableLinearLayout(Context context) {
        this(context, null);
    }

    public FoldableLinearLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FoldableLinearLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public FoldableLinearLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.FoldableLinearLayout);
        foladPercent = array.getFloat(R.styleable.FoldableLinearLayout_foldPercent, foladPercent);
        animationDuration = array.getInt(R.styleable.FoldableLinearLayout_animationDuration, animationDuration);
        if(foladPercent < 0)
        {
            foladPercent = 0;
        }
        if(foladPercent > 1)
        {
            foladPercent = 1;
        }

        currentPercent = foladPercent;

        foldAnimator = ValueAnimator.ofFloat(foladPercent, 1f);
        foldAnimator.setDuration(animationDuration);
        foldAnimator.addUpdateListener(listener);
        if (isInEditMode())
        {
            currentPercent = 1;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int orientation = getOrientation();
        if(orientation == HORIZONTAL)
        {
            int width = (int)(getMeasuredWidth() * currentPercent);
            setMeasuredDimension(width, getMeasuredHeight());

        }else{
            int height = (int)(getMeasuredHeight() * currentPercent);
            setMeasuredDimension(getMeasuredWidth(), height);
        }
    }

    public void expand()
    {
        foldAnimator.start();
    }

    public void shrink()
    {
        foldAnimator.reverse();
    }

}
