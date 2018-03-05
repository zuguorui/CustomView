package com.zu.customview.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.RadioGroup;

/**
 * Created by rickson on 2018/3/3.
 */

public class FlowRadioGroup extends RadioGroup {

    private int lineSpace = 10, elementSpace = 10;

    public FlowRadioGroup(Context context) {
        this(context, null);
    }

    public FlowRadioGroup(Context context, AttributeSet attrs) {
        super(context, attrs);

        lineSpace = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, lineSpace, context.getResources().getDisplayMetrics());
        elementSpace = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, elementSpace, context.getResources().getDisplayMetrics());
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if(getOrientation() == VERTICAL)
        {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }else{
            int height = MeasureSpec.getSize(heightMeasureSpec);
            int heightMode = MeasureSpec.getMode(heightMeasureSpec);
            int width = MeasureSpec.getSize(widthMeasureSpec);
            int widthMode = MeasureSpec.getMode(widthMeasureSpec);


            int leftEdge = getPaddingTop(), topEdge = getPaddingTop();
            int maxLineHeight = 0, maxLineWidth = 0;

            int childWidthSpec = 0, childHeightSpec = 0;


            for(int i = 0; i < getChildCount(); i++)
            {
                View child = getChildAt(i);
                MarginLayoutParams layoutParams = (MarginLayoutParams) child.getLayoutParams();

                if(widthMode == MeasureSpec.UNSPECIFIED)
                {
                    childWidthSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
                }else{
                    int horUsed = getPaddingLeft() + getPaddingRight() + layoutParams.leftMargin + layoutParams.rightMargin;
                    childWidthSpec = MeasureSpec.makeMeasureSpec(width - horUsed, MeasureSpec.AT_MOST);
                }

                if(heightMode == MeasureSpec.UNSPECIFIED)
                {
                    childHeightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
                }else{
                    int verUsed = getPaddingTop() + getPaddingBottom() + layoutParams.topMargin + layoutParams.bottomMargin;
                    childHeightSpec = MeasureSpec.makeMeasureSpec(height - verUsed, MeasureSpec.AT_MOST);
                }


                child.measure(childWidthSpec, childHeightSpec);

                int h = child.getMeasuredHeight() + layoutParams.topMargin + layoutParams.bottomMargin;
                int w = child.getMeasuredWidth() + layoutParams.leftMargin + layoutParams.rightMargin;

                if(widthMode == MeasureSpec.UNSPECIFIED)
                {
                    leftEdge += w + elementSpace;
                    if(maxLineWidth < leftEdge + getPaddingRight())
                    {
                        maxLineWidth = leftEdge + getPaddingRight();
                    }
                }else{
                    if(leftEdge + w + getPaddingRight() <= width)
                    {
                        leftEdge += w + elementSpace;
                    }else{
                        if(leftEdge + getPaddingRight() > maxLineWidth)
                        {
                            maxLineWidth = leftEdge + getPaddingRight();
                        }
                        topEdge += maxLineHeight + lineSpace;
                        leftEdge = getPaddingLeft() + w + elementSpace;
                        maxLineHeight = 0;
                    }
                }
                if(h > maxLineHeight)
                {
                    maxLineHeight = h;
                }

            }
            //此处计算的宽高都是按照wrap_content计算的
            int resultWidth = maxLineWidth;
            int resultHeight = topEdge + maxLineHeight + getPaddingBottom();

            if(widthMode == MeasureSpec.EXACTLY)
            {
                resultWidth = width;
            }

            if(heightMode == MeasureSpec.EXACTLY)
            {
                resultHeight = height;
            }

            setMeasuredDimension(resultWidth, resultHeight);

        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if(getOrientation() == VERTICAL)
        {
            super.onLayout(changed, l, t, r, b);
        }else{
            int height = getMeasuredHeight();
            int width = getMeasuredWidth();

            int leftEdge = getPaddingTop(), topEdge = getPaddingTop();
            int maxLineHeight = 0, maxLineWidth = 0;

            for(int i = 0; i < getChildCount(); i++)
            {
                View child = getChildAt(i);
                MarginLayoutParams layoutParams = (MarginLayoutParams) child.getLayoutParams();

                int h = child.getMeasuredHeight() + layoutParams.topMargin + layoutParams.bottomMargin;
                int w = child.getMeasuredWidth() + layoutParams.leftMargin + layoutParams.rightMargin;

                int top = 0, left = 0, bottom = 0, right = 0;

                if(leftEdge + w + getPaddingRight() <= width)
                {
                    top = topEdge + layoutParams.topMargin;
                    bottom = top + child.getMeasuredHeight();
                    left = leftEdge + layoutParams.leftMargin;
                    right = left + child.getMeasuredWidth();

                    leftEdge += w + elementSpace;
                }else{
                    if(leftEdge + getPaddingRight() > maxLineWidth)
                    {
                        maxLineWidth = leftEdge + getPaddingRight();
                    }
                    leftEdge = getPaddingLeft();
                    topEdge += maxLineHeight + lineSpace;

                    top = topEdge + layoutParams.topMargin;
                    bottom = top + child.getMeasuredHeight();
                    left = leftEdge + layoutParams.leftMargin;
                    right = left + child.getMeasuredWidth();

                    leftEdge += w + elementSpace;
                    maxLineHeight = 0;
                }

                child.layout(left, top, right, bottom);
                if(h > maxLineHeight)
                {
                    maxLineHeight = h;
                }
            }

        }
    }
}
