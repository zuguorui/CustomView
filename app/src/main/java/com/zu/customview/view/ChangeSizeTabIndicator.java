package com.zu.customview.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.zu.customview.R;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by rikson on 2018/8/3.
 */

public class ChangeSizeTabIndicator extends ViewGroup {

    private OnIndicatorClickListener mOnClickListener = null;

    private int textSize = 10;
    private int selectTextSize = 15;

    private int textColor = 0xff000000;
    private int selectTextColor = 0xff000000;

    private int textMarginInterval = 10;

    private ArrayList<String> tags = null;

    private ImageView indicatorImageView = null;

    private int currentIndex = 0;
    private float posOffset = 0;



    private int indicatorColor = 0x000000;
    private int indicatorHeight = 3;
    private int indicatorMarginText = 5;

    private static final int LAYOUT_MODE_BALANCE = 0;
    private static final int LAYOUT_MODE_STREAM = 1;
    private static final int LAYOUT_MODE_CENTER = 2;

    private int streamLeft = 0;

    private ArrayList<Pair<Integer, Integer>> streamBounds = new ArrayList<>();
    private Pair<Integer, Integer> indicatorBound = null;

    //0:balance 1:stream 2:center
    int layoutMode = 0;

    int offset = 0;




    public ChangeSizeTabIndicator(Context context) {
        this(context, null);
    }

    public ChangeSizeTabIndicator(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ChangeSizeTabIndicator(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr,0);
    }

    public ChangeSizeTabIndicator(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.ChangeSizeTabIndicator);
        textSize = array.getDimensionPixelSize(R.styleable.ChangeSizeTabIndicator_textSize, textSize);
        selectTextSize = array.getDimensionPixelSize(R.styleable.ChangeSizeTabIndicator_selectTextSize, selectTextSize);
        textColor = array.getColor(R.styleable.ChangeSizeTabIndicator_textColor, textColor);
        selectTextColor = array.getColor(R.styleable.ChangeSizeTabIndicator_selectTextColor, selectTextColor);

        layoutMode = array.getInt(R.styleable.ChangeSizeTabIndicator_layoutMode, layoutMode);

        textMarginInterval = array.getDimensionPixelOffset(R.styleable.ChangeSizeTabIndicator_textMarginInterval, textMarginInterval);


        indicatorColor = array.getColor(R.styleable.ChangeSizeTabIndicator_indicatorColor, indicatorColor);
        indicatorHeight = array.getDimensionPixelSize(R.styleable.ChangeSizeTabIndicator_indicatorHeight, indicatorHeight);
        indicatorMarginText = array.getDimensionPixelSize(R.styleable.ChangeSizeTabIndicator_indicatorMarginText, indicatorMarginText);
        streamLeft = getPaddingLeft();
        array.recycle();

        setClickable(true);
        setWillNotDraw(false);
        if(isInEditMode())
        {
            ArrayList<String> editTags = new ArrayList<>();
            for(int i = 0; i < 4; i++)
            {
                editTags.add("Tag" + i);
            }
            setTags(editTags);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if(tags == null || tags.size() == 0)
        {
            setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec));
        }else{

            Paint paint = new Paint();

            int heightSize = MeasureSpec.getSize(heightMeasureSpec);
            int heightMode = MeasureSpec.getMode(heightMeasureSpec);
            int widthSize = MeasureSpec.getSize(widthMeasureSpec);
            int widthMode = MeasureSpec.getMode(widthMeasureSpec);

            int resultHeight = getPaddingTop() + getPaddingBottom(),
                    resultWidth = getPaddingLeft() + getPaddingRight();

            int textHeight = 0, textWidth = 0;

            for(int i = 0; i < tags.size(); i++)
            {
                if(i == currentIndex)
                {
                    paint.setTextSize(computeTextSize(selectTextSize, textSize, posOffset));
                }else if(i == currentIndex + 1)
                {
                    paint.setTextSize(computeTextSize(textSize,selectTextSize, posOffset));
                }else{
                    paint.setTextSize(textSize);
                }

                textWidth = getTextWidth(paint, tags.get(i));
                resultWidth += textWidth;
            }

            int maxTextHeight = 0;
            paint.setTextSize(selectTextSize);
            maxTextHeight = getTextHeight(paint);

            paint.setTextSize(textSize);
            if(maxTextHeight < getTextHeight(paint))
            {
                maxTextHeight = getTextHeight(paint);
            }


            resultWidth += (tags.size() - 1) * textMarginInterval;
            resultHeight += maxTextHeight;
            if(indicatorHeight != 0)
            {
                resultHeight += indicatorHeight + indicatorMarginText;
            }

            if(heightMode == MeasureSpec.EXACTLY)
            {
                resultHeight = heightSize;
            }else if(heightMode == MeasureSpec.AT_MOST && resultHeight > heightSize)
            {
                resultHeight = heightSize;
            }

            if(widthMode == MeasureSpec.EXACTLY)
            {
                resultWidth = widthSize;
            }else if(widthMode == MeasureSpec.AT_MOST && resultWidth > widthSize)
            {
                resultWidth = widthSize;
            }

            setMeasuredDimension(resultWidth, resultHeight);
        }

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawContent(canvas);
    }

    private void drawContent(Canvas canvas)
    {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        int textBottom = getMeasuredHeight() - getPaddingBottom();
        int textBaseLine = 0;
        int textHeight = 0;
        if(indicatorHeight != 0)
        {
            textBottom -= indicatorHeight + indicatorMarginText;
        }
        for(int i = 0; i < tags.size(); i++)
        {
            Pair<Integer, Integer> pair = streamBounds.get(i);
            if(pair.first <= getMeasuredWidth() || pair.second >= 0)
            {
                String tag = tags.get(i);
                if(i == currentIndex)
                {
                    paint.setColor(computeColor(selectTextColor, textColor, posOffset));
                    paint.setTextSize(computeTextSize(selectTextSize, textSize, posOffset));
                }else if(i == currentIndex + 1)
                {
                    paint.setColor(computeColor(textColor, selectTextColor, posOffset));
                    paint.setTextSize(computeTextSize(textSize, selectTextSize, posOffset));
                }else{
                    paint.setColor(textColor);
                    paint.setTextSize(textSize);
                }

                textBaseLine = textBottom - getTextBottom(paint);
                canvas.drawText(tag, pair.first, textBaseLine, paint);


            }
        }
        if(indicatorHeight != 0)
        {
            int indicatorBottom = getMeasuredHeight() - getPaddingBottom();
            int indicatorTop = indicatorBottom - indicatorHeight;
            Rect rect = new Rect(indicatorBound.first, indicatorTop, indicatorBound.second, indicatorBottom);
            paint.setColor(indicatorColor);
            canvas.drawRect(rect, paint);

        }


    }

    private int getTextHeight(Paint pPaint)
    {
        Paint.FontMetrics fm = pPaint.getFontMetrics();
        return (int)(fm.bottom - fm.top);
    }

    private int getTextBottom(Paint pPaint)
    {
        return (int)pPaint.getFontMetrics().bottom;
    }

    private int getTextWidth(Paint pPaing, String s)
    {
        return (int)pPaing.measureText(s);
    }



    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if(changed)
        {
            layoutChildren();
        }
    }

    private void layoutChildren()
    {
        if(tags == null || tags.size() == 0)
        {
            return;
        }
        if(layoutMode == LAYOUT_MODE_BALANCE)
        {
            layoutBalance();
        }else if(layoutMode == LAYOUT_MODE_CENTER){
            layoutCenter();
        }else{
            layoutStream();
        }

    }

    private void layoutBalance()
    {

        computeTextViewPosBalance();
        indicatorBound = computeIndicatorPosBalance();

    }

    /**使用在ViewPager.OnPageChangeListener.onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
     * 方法中，将三个参数原样传到该函数即可。
     * @param position 当前可见的第一个页面的序号，如果positionOffset不为0,那么position + 1页面也是可见的。
     * @param positionOffset 取值范围[0, 1)，表示当前position页面的偏离范围。
     * @param positionOffsetPixels 当前position页面的偏离值。
     * */
    public void listen(int position, float positionOffset, int positionOffsetPixels){
        currentIndex = position;
        posOffset = positionOffset;
        layoutChildren();
        invalidate();

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction())
        {
            case MotionEvent.ACTION_UP:
                if(tags == null || tags.size() != 0)
                {
                    int x = (int)event.getX();
                    for(int i = 0; i < tags.size(); i++)
                    {
                        Pair<Integer, Integer> pair = streamBounds.get(i);
                        if(pair.first <= x && pair.second >= x)
                        {
                            notifyClicked(i);
                            break;
                        }
                    }
                }

        }
        return true;
    }

    public void setOnIndicatorClickListener(OnIndicatorClickListener listener)
    {
        mOnClickListener = listener;
    }

    public void removeOnIndicatorClickListener()
    {
        mOnClickListener = null;
    }

    private void notifyClicked(int id)
    {
        if(mOnClickListener != null)
        {
            mOnClickListener.onClick(id);
        }
    }

    private void layoutCenter()
    {
        computeTextViewPos();
        indicatorBound = computeIndicatorPos();
        int center = (int)((getMeasuredWidth() - getPaddingRight() - getPaddingLeft()) * 0.5f + getPaddingLeft());
        int indicatorCenter = (indicatorBound.first + indicatorBound.second) / 2;
        int offset = center - indicatorCenter;
        offsetPos(offset);
        streamLeft = streamBounds.get(0).first;
    }

    private void layoutStream()
    {
        computeTextViewPos();
        indicatorBound = computeIndicatorPos();

        int offset = 0;
        if(indicatorBound.first < getPaddingLeft())
        {
            offset = getPaddingLeft() - indicatorBound.first;
        }else if(indicatorBound.second > getMeasuredWidth() - getPaddingRight())
        {
            offset = getMeasuredWidth() - getPaddingRight() - indicatorBound.second;
        }
        offsetPos(offset);
        streamLeft = streamBounds.get(0).first;
    }

    private void offsetPos(int offset)
    {
        for(int i = 0; i < streamBounds.size(); i++)
        {
            Pair<Integer, Integer> pair = streamBounds.get(i);
            pair.first += offset;
            pair.second += offset;
        }

        if(indicatorBound != null)
        {
            indicatorBound.first += offset;
            indicatorBound.second += offset;
        }
    }

    //need to layout text view first
    private Pair<Integer, Integer> computeIndicatorPosBalance()
    {
        int blockWidth = (getMeasuredWidth() - getPaddingLeft() - getPaddingRight()) / tags.size();
        int centerX = (int)(blockWidth * (currentIndex + posOffset + 0.5f) + getPaddingLeft());
        int width = 0;
        if(posOffset < 0.01f)
        {
            Pair<Integer, Integer> pair = streamBounds.get(currentIndex);
            width = pair.second - pair.first;
        }else{
            Pair<Integer, Integer> pair1 = streamBounds.get(currentIndex);
            Pair<Integer, Integer> pair2 = streamBounds.get(currentIndex + 1);
            int startWidth = pair1.second - pair1.first;
            int endWidth = pair2.second - pair2.first;
            width = (int)(startWidth + (endWidth - startWidth) * posOffset);

        }

        int left = (int)(centerX - width * 0.5f);
        int right = left + width;
        return new Pair<>(left, right);

    }

    //need to compute text view position and pass info in
    private Pair<Integer, Integer> computeIndicatorPos()
    {
        int left = 0, right = 0;
        if(currentIndex == tags.size() - 1)
        {
            Pair<Integer, Integer> pair = streamBounds.get(currentIndex);
            left = pair.first;
            right = pair.second;
        }else{
            int center = 0;
            int width = 0;
            Pair<Integer, Integer> pair1 = streamBounds.get(currentIndex);
            Pair<Integer, Integer> pair2 = streamBounds.get(currentIndex + 1);
            int startCenter = (int)((pair1.first + pair1.second) * 0.5f);
            int endCenter = (int)((pair2.first + pair2.second) * 0.5f);
            int startWidth = pair1.second - pair1.first;
            int endWidth = pair2.second - pair2.first;
            center = (int)(startCenter + (endCenter - startCenter) * posOffset);
            width = (int)(startWidth + (endWidth - startWidth) * posOffset);
            left = (int)(center - width * 0.5f);
            right = left + width;
        }
        return new Pair<>(left, right);
    }

    private void computeTextViewPosBalance()
    {
        streamBounds.clear();
        int blockWidth = (getMeasuredWidth() - getPaddingLeft() - getPaddingRight()) / tags.size();

        int childWidth = 0, childHeight = 0;
        Paint mPaint = new Paint();

        int l, t, r, b;
        for(int i = 0; i < tags.size(); i++)
        {
            int centerX = (int)((i + 0.5f) * blockWidth + getPaddingLeft());

            if(i == currentIndex)
            {
                mPaint.setTextSize(computeTextSize(selectTextSize, textSize, posOffset));

            }else if(i == currentIndex + 1)
            {
                mPaint.setTextSize(computeTextSize(textSize, selectTextSize, posOffset));

            }else{
                mPaint.setTextSize(textSize);
            }

            childWidth = getTextWidth(mPaint, tags.get(i));
            childHeight = getTextHeight(mPaint);

            l = centerX - (int)(childWidth * 0.5);
            r = l + childWidth;
            b = getMeasuredHeight() - getPaddingBottom();
            if(indicatorHeight != 0)
            {
                b -= indicatorHeight + indicatorMarginText;
            }

            t = b - childHeight;

            Pair<Integer, Integer> pair = new Pair<>(l, r);
            streamBounds.add(pair);

        }
    }

    //at this step, views are measured
    private void computeTextViewPos()
    {
        streamBounds.clear();
        Paint mPaint = new Paint();
        int left = streamLeft;
        int textWidth = 0;

        for(int i = 0; i < tags.size(); i++) {

            if (i == currentIndex)
            {
                mPaint.setTextSize(computeTextSize(selectTextSize, textSize, posOffset));
            }else if(i == currentIndex + 1){
                mPaint.setTextSize(computeTextSize(textSize, selectTextSize, posOffset));
            }else{
                mPaint.setTextSize(textSize);
            }
            textWidth = getTextWidth(mPaint, tags.get(i));
            Pair<Integer, Integer> pair = new Pair<>(left, left + textWidth);
            streamBounds.add(pair);
            left += textWidth + textMarginInterval;
        }
    }

    private float computeTextSize(float startSize, float endSize, float progress)
    {
        return startSize + (endSize - startSize) * progress;
    }

    private int computeColor(int startColor, int endColor, float progress)
    {
        int startAlpha = (startColor >> 24) & 0xff;
        int startR = (startColor >> 16) & 0xff;
        int startG = (startColor >> 8) & 0xff;
        int startB = startColor & 0xff;

        int endAlpha = (endColor >> 24) & 0xff;
        int endR = (endColor >> 16) & 0xff;
        int endG = (endColor >> 8) & 0xff;
        int endB = endColor & 0xff;

        int resultAlpha = (int)((endAlpha - startAlpha) * progress + startAlpha);
        int resultR = (int)((endR - startR) * progress + startR);
        int resultG = (int)((endG - startG) * progress + startG);
        int resultB = (int)((endB - startB) * progress + startB);

        int resultColor = (resultAlpha << 24) | (resultR << 16) | (resultG << 8) | resultB;

        return resultColor;
    }

    public void setTags(ArrayList<String> tags)
    {
        this.tags = tags;

        invalidate();
    }



    public interface OnIndicatorClickListener{
        void onClick(int index);
    }

    private class Pair<T, S>{
        public Pair(T first, S second) {
            this.first = first;
            this.second = second;
        }

        public T first;
        public S second;
    }


}
