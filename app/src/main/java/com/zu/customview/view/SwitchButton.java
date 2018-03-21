package com.zu.customview.view;

import android.animation.FloatEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
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

import com.zu.customview.MyLog;
import com.zu.customview.R;

/**
 * Created by rickson on 2018/3/10.
 */

public class SwitchButton extends FrameLayout {
    private MyLog log = new MyLog("SwitchButton", true);

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
    private float switchSpeed = 0.1f;
    private float currentPercent = 0f;


    private enum SWITCH_DIRECTION{LEFT, RIGHT, NONE}

    private SWITCH_DIRECTION switchDirection = SWITCH_DIRECTION.NONE;

    private TextView leftTextView = null, rightTextView = null;
    private ImageView leftImageView = null, rightImageView = null;

    private long lastDrawTime = 0;

    private LayerDrawable leftBackDrawable = null, rightBackDrawable = null;



    private SwitchButtonListener mListener = null;




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

        float textPadding = array.getDimension(R.styleable.SwitchButton_textPadding, -1f);
        if(textPadding != -1f)
        {
            textPaddingLeft = textPaddingRight = textPaddingTop = textPaddingBottom = textPadding;
        }


        buttonPaddingTop  =array.getDimension(R.styleable.SwitchButton_buttonPaddingTop, buttonPaddingTop);
        buttonPaddingBottom  =array.getDimension(R.styleable.SwitchButton_buttonPaddingBottom, buttonPaddingBottom);
        buttonPaddingLeft  =array.getDimension(R.styleable.SwitchButton_buttonPaddingLeft, buttonPaddingLeft);
        buttonPaddingRight  =array.getDimension(R.styleable.SwitchButton_buttonPaddingRight, buttonPaddingRight);

        float buttonPadding = array.getDimension(R.styleable.SwitchButton_buttonPadding, -1f);
        if(buttonPadding != -1)
        {
            buttonPaddingLeft = buttonPaddingRight = buttonPaddingTop = buttonPaddingBottom = buttonPadding;
        }

        textSize = array.getDimension(R.styleable.SwitchButton_textSize, textSize);

        leftButtonDrawable = array.getDrawable(R.styleable.SwitchButton_leftButtonDrawable);
        rightButtonDrawble = array.getDrawable(R.styleable.SwitchButton_rightButtonDrawable);
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
        leftTextView.setClickable(true);
        leftTextView.setOnClickListener(new OnLeftTextViewClickListener());

        rightTextView = new TextView(context);
        rightTextView.setText(rightLabel);
        rightTextView.setTextSize(textSize);
        rightTextView.setPadding((int)textPaddingLeft, (int)textPaddingTop, (int)textPaddingRight, (int)textPaddingBottom);
        rightTextView.setGravity(Gravity.CENTER);
        rightTextView.setClickable(true);
        rightTextView.setOnClickListener(new OnRightTextViewClickListener());

        leftImageView = new ImageView(context);
        leftImageView.setImageDrawable(leftButtonDrawable);
        leftImageView.setClickable(true);
        leftImageView.setPadding((int)buttonPaddingLeft, (int)buttonPaddingTop, (int)buttonPaddingRight, (int)buttonPaddingBottom);
        leftImageView.setOnClickListener(new OnLeftImageViewClickListener());

        rightImageView = new ImageView(context);
        rightImageView.setImageDrawable(rightButtonDrawble);
        rightImageView.setClickable(true);
        rightImageView.setPadding((int)buttonPaddingLeft, (int)buttonPaddingTop, (int)buttonPaddingRight, (int)buttonPaddingBottom);
        rightImageView.setOnClickListener(new OnRightImageViewClickListener());

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


//        layoutParams = (MarginLayoutParams) getLayoutParams();
//        layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
//        setLayoutParams(layoutParams);

        mListener = new SwitchButtonListenerImpl();
        setWillNotDraw(false);



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

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        layoutChild();
    }

    @Override
    protected void onDraw(Canvas canvas) {

        super.onDraw(canvas);
        computeProgress();
        layoutChild();
        drawBackgroundDrawable(canvas);
        drawLine(canvas);
        invalidate();
//        canvas.drawColor(0xffffff00);
//        drawBackgroundDrawable(canvas);



    }

    public String getLeftLabel() {
        return leftLabel;
    }

    public void setLeftLabel(String leftLabel) {
        this.leftLabel = leftLabel;
        leftTextView.setText(leftLabel);
    }

    public String getRightLabel() {
        return rightLabel;
    }

    public void setRightLabel(String rightLabel) {
        this.rightLabel = rightLabel;
        rightTextView.setText(rightLabel);
    }

    private void computeProgress()
    {
        if(switchDirection == SWITCH_DIRECTION.LEFT && currentPercent <= switchPercent)
        {
            switchDirection = SWITCH_DIRECTION.NONE;
            currentPercent = switchPercent;
            mListener.endSwitch(SWITCH_DIRECTION.LEFT);
            return;
        }else if(switchDirection == SWITCH_DIRECTION.RIGHT && currentPercent >= (1 - switchPercent))
        {
            switchDirection = SWITCH_DIRECTION.NONE;
            currentPercent = 1 - switchPercent;
            mListener.endSwitch(SWITCH_DIRECTION.RIGHT);
            return;
        }else if(switchDirection == SWITCH_DIRECTION.NONE){
            if(currentPercent == switchPercent || currentPercent == 1 - switchPercent)
            {
                return;
            }

            if(currentPercent < 0.5f)
            {
                switchDirection = SWITCH_DIRECTION.LEFT;
            }else{
                switchDirection = SWITCH_DIRECTION.RIGHT;
            }
        }
        long time = System.currentTimeMillis();
        float movePercent = (time - lastDrawTime) * 1f / 50 * (switchSpeed);
        if(switchDirection == SWITCH_DIRECTION.LEFT)
        {
            if(currentPercent == 1 - switchPercent)
            {
                mListener.beginSwitch(SWITCH_DIRECTION.LEFT);
                movePercent = 0.01f;
            }
            currentPercent -= movePercent;

        }else{
            if(currentPercent == switchPercent)
            {
                mListener.beginSwitch(SWITCH_DIRECTION.RIGHT);
                movePercent = 0.01f;
            }
            currentPercent += movePercent;
        }
        if(currentPercent < switchPercent)
        {
            currentPercent = switchPercent;
        }

        if(currentPercent > 1 - switchPercent)
        {
            currentPercent = 1 - switchPercent;
        }
        lastDrawTime = time;



    }

    private void drawBackgroundDrawable(Canvas canvas)
    {
        float percent = (currentPercent - switchPercent) / (1 - 2 * switchPercent);
        int alpha = (int)(percent * 255);
        leftBackDrawable.getDrawable(0).setAlpha(255 - alpha);
        leftBackDrawable.getDrawable(1).setAlpha(alpha);
        rightBackDrawable.getDrawable(0).setAlpha(alpha);
        rightBackDrawable.getDrawable(1).setAlpha(255 - alpha);

        int position = (int)(getWidth() * currentPercent);
//        canvas.save();
//        canvas.clipRect(0, 0, position, getHeight());
        leftBackDrawable.setBounds(0, 0, position, getHeight());
        leftBackDrawable.draw(canvas);
//        canvas.restore();
//        canvas.save();
//        canvas.clipRect(position, 0, getWidth(), getHeight());
        rightBackDrawable.setBounds(position, 0, getWidth(), getHeight());
        rightBackDrawable.draw(canvas);
//        canvas.restore();
//        canvas.drawColor(0xffff0000);

    }

    private void drawLine(Canvas canvas)
    {
        int padding = 20;
        Paint paint = new Paint();
        paint.setColor(0xffffffff);
        paint.setStrokeWidth(2);
        int position = leftImageView.getRight();
        canvas.drawLine(position, padding, position, getHeight() - padding, paint);
        position = rightImageView.getLeft();
        canvas.drawLine(position, padding, position, getHeight() - padding, paint);
    }

    private void layoutChild()
    {
        int position = (int)(getWidth() * currentPercent);

        int maxWidth = (int)(getWidth() * (1 - switchPercent));
        int left = position - maxWidth;
        int right = left + leftImageView.getMeasuredWidth();
        int top = 0;
        int bottom = top + getHeight();
        leftImageView.layout(left, top, right, bottom);

        right = position + maxWidth;
        left = right - rightImageView.getMeasuredWidth();
        rightImageView.layout(left, top, right, bottom);


        left = leftImageView.getRight() < 0 ? 0 : leftImageView.getRight();
        right = position;

        int widthSpec = MeasureSpec.makeMeasureSpec(right - left, MeasureSpec.EXACTLY);
        int heightSpec = MeasureSpec.makeMeasureSpec(getHeight(), MeasureSpec.EXACTLY);

        leftTextView.measure(widthSpec, heightSpec);
        leftTextView.layout(left, 0, right, getHeight());


        left = position;
        right = rightImageView.getLeft() > getWidth() ? getWidth() : rightImageView.getLeft();
        widthSpec = MeasureSpec.makeMeasureSpec(right - left, MeasureSpec.EXACTLY);
        rightTextView.measure(widthSpec, heightSpec);


        rightTextView.layout(left, 0, right, getHeight());
    }

    private void setSwitchButtonListener(SwitchButtonListener listener)
    {
        mListener = listener;
    }

    private void removeSwitchButtonListener()
    {
        mListener = new SwitchButtonListenerImpl();

    }

    private class OnLeftImageViewClickListener implements OnClickListener{
        @Override
        public void onClick(View v) {
            mListener.onLeftButtonClicked();

        }
    }

    private class OnRightImageViewClickListener implements OnClickListener{
        @Override
        public void onClick(View v) {
            mListener.onRightButtonClicked();
        }
    }

    private class OnLeftTextViewClickListener implements OnClickListener{
        @Override
        public void onClick(View v) {
            switchDirection = SWITCH_DIRECTION.RIGHT;
            invalidate();
        }
    }

    private class OnRightTextViewClickListener implements OnClickListener{
        @Override
        public void onClick(View v) {
            switchDirection = SWITCH_DIRECTION.LEFT;
            invalidate();
        }
    }

    public interface SwitchButtonListener{
        void beginSwitch(SWITCH_DIRECTION direction);
        void switching(SWITCH_DIRECTION direction, float process);
        void endSwitch(SWITCH_DIRECTION direction);
        void onLeftButtonClicked();
        void onRightButtonClicked();
    }

    private class SwitchButtonListenerImpl implements SwitchButtonListener
    {
        public void beginSwitch(SWITCH_DIRECTION direction)
        {
            log.d("beginSwitch, direction = " + direction);
        }
        public void switching(SWITCH_DIRECTION direction, float process){
            log.d("switching, direction = " + direction + ", process = " + process);
        }
        public void endSwitch(SWITCH_DIRECTION direction){
            log.d("endSwitch, direction = " + direction);
        }
        public void onLeftButtonClicked(){
            log.d("onLeftButtonClicked");
        }
        public void onRightButtonClicked(){
            log.d("onRightButtonClicked");
        }
    }


}
