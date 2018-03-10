package com.zu.customview.view;

import android.animation.FloatEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.zu.customview.R;

/**
 * Created by rickson on 2018/3/10.
 */

public class SwitchButton extends FrameLayout {
    private  String leftLabel = "", rightLabel = "";

    private Drawable leftButtonDrawable = null, rightButtonDrawble = null;

    private float switchPercent = 0.2f;

    private Drawable shrinkLeftDrawable = null, enlargeLeftDrawable = null;
    private Drawable shrinkRightDrawable = null, enlargeRightDrawble = null;

    private int shrinkLeftColor = 0x00000000, enlargeLeftColor = 0x00000000;
    private int shrinkRightColor = 0x00000000, enlargeRightColor = 0x00000000;

    private int shrinkLeftTextColor = 0x000000, enlargeLeftTextColor = 0x000000;
    private int shrinkRightTextColor = 0x000000, enlargeRightTextColor = 0x000000;

    private float textPaddingLeft = 10, textPaddingRight = 10, textPaddingTop = 4, textPaddingBottom = 4;
    private float textSize = 10;
    private float buttonPaddingLeft = 3, buttonPaddingRight = 3, buttonPaddingTop = 3, buttonPaddingBottom = 3;
    private int switchSpeed = 10;
    private float currentPercent = 0f;


    private enum SWITCH_DIRECTION{LEFT, RIGHT, NONE}

    private SWITCH_DIRECTION switchDirection = SWITCH_DIRECTION.NONE;

    private TextView leftTextView = null, rightTextView = null;
    private ImageView leftImageView = null, rightImageView = null;

    private LayerDrawable leftBackDrawable = null, rightBackDrawable = null;

    private ValueAnimator.AnimatorUpdateListener animatorUpdateListener = new ValueAnimator.AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {

        }
    };




    public SwitchButton(Context context) {
        this(context, null);
    }

    public SwitchButton(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SwitchButton(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public SwitchButton(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.SwitchButton);
        leftLabel = array.getString(R.styleable.SwitchButton_leftLabel);
        rightLabel = array.getString(R.styleable.SwitchButton_rightLabel);

        switchPercent = array.getFloat(R.styleable.SwitchButton_switchPercent, switchPercent);
        shrinkLeftTextColor = array.getColor(R.styleable.SwitchButton_shrinkLeftTextColor, shrinkLeftTextColor);
        shrinkRightTextColor = array.getColor(R.styleable.SwitchButton_shrinkRightTextColor, shrinkRightTextColor);

        enlargeLeftTextColor = array.getColor(R.styleable.SwitchButton_enlargeLeftTextColor, enlargeLeftTextColor);
        enlargeRightTextColor = array.getColor(R.styleable.SwitchButton_enlargeRightTextColor, enlargeRightTextColor);

        shrinkLeftColor = array.getColor(R.styleable.SwitchButton_shrinkLeftColor, shrinkLeftColor);
        enlargeLeftColor = array.getColor(R.styleable.SwitchButton_enlargeLeftColor, enlargeLeftColor);

        shrinkRightColor = array.getColor(R.styleable.SwitchButton_shrinkRightColor, shrinkRightColor);
        enlargeRightColor = array.getColor(R.styleable.SwitchButton_enlargeRightColor, enlargeRightColor);

        textPaddingLeft = array.getDimension(R.styleable.SwitchButton_textPaddingLeft, textPaddingLeft);
        textPaddingRight = array.getDimension(R.styleable.SwitchButton_textPaddingRight, textPaddingRight);
        textPaddingTop = array.getDimension(R.styleable.SwitchButton_textPaddingTop, textPaddingTop);
        textPaddingBottom = array.getDimension(R.styleable.SwitchButton_textPaddingBottom, textPaddingBottom);

        buttonPaddingTop  =array.getDimension(R.styleable.SwitchButton_buttonPaddingTop, buttonPaddingTop);
        buttonPaddingBottom  =array.getDimension(R.styleable.SwitchButton_buttonPaddingBottom, buttonPaddingBottom);
        buttonPaddingLeft  =array.getDimension(R.styleable.SwitchButton_buttonPaddingLeft, buttonPaddingLeft);
        buttonPaddingRight  =array.getDimension(R.styleable.SwitchButton_buttonPaddingRight, buttonPaddingRight);

        textSize = array.getDimension(R.styleable.SwitchButton_textSize, textSize);

        leftButtonDrawable = array.getDrawable(R.styleable.SwitchButton_leftButtonDrawable);
        rightButtonDrawble = array.getDrawable(R.styleable.SwitchButton_rightLabel);
        shrinkLeftDrawable = array.getDrawable(R.styleable.SwitchButton_shrinkLeftDrawable);
        enlargeLeftDrawable = array.getDrawable(R.styleable.SwitchButton_enlargeLeftDrawable);
        shrinkRightDrawable = array.getDrawable(R.styleable.SwitchButton_shrinkRightDrawable);
        enlargeRightDrawble = array.getDrawable(R.styleable.SwitchButton_enlargeRightDrawable);

        array.recycle();

        if(shrinkLeftDrawable == null)
        {
            shrinkLeftDrawable = new ColorDrawable(shrinkLeftColor);
        }
        if(enlargeLeftDrawable == null)
        {
            enlargeLeftDrawable = new ColorDrawable(enlargeLeftColor);
        }
        if(shrinkRightDrawable == null)
        {
            shrinkRightDrawable = new ColorDrawable(shrinkRightColor);
        }
        if(enlargeRightDrawble == null)
        {
            enlargeRightDrawble = new ColorDrawable(enlargeRightColor);
        }

        leftBackDrawable = new LayerDrawable(new Drawable[]{shrinkLeftDrawable, enlargeLeftDrawable});
        rightBackDrawable = new LayerDrawable(new Drawable[]{shrinkRightDrawable, enlargeRightDrawble});

        if(switchPercent > 0.5f)
        {
            switchPercent = 1 - switchPercent;
        }
        currentPercent = switchPercent;

        leftTextView = new TextView(context);
        leftTextView.setText(leftLabel);
        leftTextView.setTextSize(textSize);
        leftTextView.setPadding((int)textPaddingLeft, (int)textPaddingTop, (int)textPaddingRight, (int)textPaddingBottom);
        leftTextView.setGravity(Gravity.CENTER);

        rightTextView = new TextView(context);
        rightTextView.setText(rightLabel);
        rightTextView.setTextSize(textSize);
        rightTextView.setPadding((int)textPaddingLeft, (int)textPaddingTop, (int)textPaddingRight, (int)textPaddingBottom);
        rightTextView.setGravity(Gravity.CENTER);

        leftImageView = new ImageView(context);
        leftImageView.setImageDrawable(leftButtonDrawable);
        leftImageView.setClickable(true);
        leftImageView.setPadding((int)buttonPaddingLeft, (int)buttonPaddingTop, (int)buttonPaddingRight, (int)buttonPaddingBottom);

        rightImageView = new ImageView(context);
        rightImageView.setImageDrawable(rightButtonDrawble);
        rightImageView.setClickable(true);
        rightImageView.setPadding((int)buttonPaddingLeft, (int)buttonPaddingTop, (int)buttonPaddingRight, (int)buttonPaddingBottom);

        addView(leftTextView);
        addView(rightTextView);
        addView(leftImageView);
        addView(rightImageView);

        MarginLayoutParams layoutParams = (MarginLayoutParams) leftTextView.getLayoutParams();
        layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT;
        layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        leftTextView.setLayoutParams(layoutParams);

        layoutParams = (MarginLayoutParams)rightTextView.getLayoutParams();
        layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT;
        layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        rightTextView.setLayoutParams(layoutParams);

        layoutParams = (MarginLayoutParams)leftImageView.getLayoutParams();
        layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT;
        layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        leftImageView.setLayoutParams(layoutParams);

        layoutParams = (MarginLayoutParams)rightImageView.getLayoutParams();
        layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT;
        layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        rightImageView.setLayoutParams(layoutParams);


        layoutParams = (MarginLayoutParams) getLayoutParams();
        layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
        setLayoutParams(layoutParams);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int maxHeight = 0;

        for(int i = 0; i < getChildCount(); i++)
        {
            View child = getChildAt(i);
            ViewGroup.LayoutParams layoutParams = child.getLayoutParams();
            int childWidthSpec = getChildMeasureSpec(widthMeasureSpec, 0, layoutParams.width);
            int childHeightSpec = getChildMeasureSpec(heightMeasureSpec, 0, layoutParams.height);
            child.measure(childWidthSpec, childHeightSpec);
            if(child.getMeasuredHeight() > maxHeight)
            {
                maxHeight = child.getMeasuredHeight();
            }
        }



        if(heightMode == MeasureSpec.AT_MOST)
        {
            if(maxHeight > height)
            {

                setMeasuredDimension(width, height);
            }else{
                setMeasuredDimension(width, maxHeight);
            }
        }else if(heightMode == MeasureSpec.UNSPECIFIED)
        {
            setMeasuredDimension(width, maxHeight);
        }else{
            setMeasuredDimension(width, height);
        }

        int buttonWidthSpec = MeasureSpec.makeMeasureSpec(getMeasuredHeight(), MeasureSpec.EXACTLY);
        int buttonHeightSpec = MeasureSpec.makeMeasureSpec(getMeasuredHeight(), MeasureSpec.EXACTLY);
        leftImageView.measure(buttonWidthSpec, buttonHeightSpec);
        rightImageView.measure(buttonWidthSpec, buttonHeightSpec);

//        int maxWidth = (int)(getMeasuredWidth() * (1 - switchPercent));
//        int leftWidthSpec = MeasureSpec.makeMeasureSpec(maxWidth - leftImageView.getMeasuredWidth(), MeasureSpec.EXACTLY);
//        int leftHeightSpec = MeasureSpec.makeMeasureSpec(leftImageView.getMeasuredHeight(), MeasureSpec.EXACTLY);
//        leftImageView.measure(leftWidthSpec, leftHeightSpec);
//
//        int rightWidthSpec = MeasureSpec.makeMeasureSpec(maxWidth - rightImageView.getMeasuredWidth(), MeasureSpec.EXACTLY);
//        int rightHeightSpec = MeasureSpec.makeMeasureSpec(rightImageView.getMeasuredHeight(), MeasureSpec.EXACTLY);
//        rightImageView.measure(rightWidthSpec, rightHeightSpec);
    }

    private void layoutChild()
    {
        int leftSpace = (int)(getWidth() * currentPercent);
        int rightSpace = getWidth() - leftSpace;
        int maxWidth = (int)(getWidth() * (1 - switchPercent));
        leftImageView.layout();
    }



}
