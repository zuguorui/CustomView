package com.zu.customview.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import com.zu.customview.MyLog;
import com.zu.customview.R;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by zu on 17-3-8.
 */

public class TextViewPagerIndicator extends ViewGroup
{
    MyLog log = new MyLog("TextViewPagerIndicator", true);
    /**
     * 布局显示相关的参数
     * */
    private int textColor = Color.parseColor("#000000");
    private int backgroundColor = Color.parseColor("#ffffff");
    private int textSize = 10;
    private int indicatorColor = Color.parseColor("#ffffff");
    private int indicatorHeight = 3;
    private int interval = 10;
    private int textPadding = 0;
    private int textPaddingTop = 0;
    private int textPaddingBottom = 0;
    private int textPaddingLeft = 0;
    private int textPaddingRight = 0;
    private int selectedTextColor;
    private boolean balanceLayout = false;


    private enum IndicatorStyle
    {
        line, background
    }

    private IndicatorStyle indicatorStyle = IndicatorStyle.line;
    private int indicatorDrawable = -1;




    private ArrayList<String> tags = new ArrayList<>();
    private HashMap<String, TextView> tagMap = new HashMap<>();
    private ImageView indicatorLine;

    private int currentPosition = 0;

    private boolean expanded = false;

    private int textOffset = 0;

    private ArrayList<OnTagClickedListener> onTagClickedListeners = new ArrayList<>();


    /**
     * 事件相关参数
     * */

    private float newX, newY, lastX, lastY, dx, dy, downX, downY;

    private int touchSlop = 5;

    public TextViewPagerIndicator(Context context) {
        this(context, null);
    }

    public TextViewPagerIndicator(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TextViewPagerIndicator(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public TextViewPagerIndicator(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TextViewPagerIndicator);
        textColor = a.getColor(R.styleable.TextViewPagerIndicator_textColor, textColor);
        backgroundColor = a.getColor(R.styleable.TextViewPagerIndicator_backgroundColor, backgroundColor);
        indicatorColor = a.getColor(R.styleable.TextViewPagerIndicator_indicatorColor, indicatorColor);
        textSize = a.getDimensionPixelSize(R.styleable.TextViewPagerIndicator_textSize, textSize);
        indicatorHeight = a.getDimensionPixelSize(R.styleable.TextViewPagerIndicator_indicatorHeight, indicatorHeight);
        interval = a.getDimensionPixelSize(R.styleable.TextViewPagerIndicator_intervalBetweenTags, interval);
        selectedTextColor = a.getColor(R.styleable.TextViewPagerIndicator_selectedTextColor, indicatorColor);
        balanceLayout = a.getBoolean(R.styleable.TextViewPagerIndicator_balanceLayout, balanceLayout);
        a.recycle();
        indicatorLine = new ImageView(context);
        indicatorLine.setBackgroundColor(indicatorColor);
        this.addView(indicatorLine);

        touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        this.setClickable(true);
        textOffset = getPaddingLeft();

        if(isInEditMode())
        {
            for(int i = 0; i < 5; i++)
            {
                addTag("item" + i);
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        log.v("onMeasure");
        setWillNotDraw(true);
        int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);


        int unspecifiedWidthMeasureSpec = MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.UNSPECIFIED);
        int unspecifiedHeightMeasureSpec = MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.UNSPECIFIED);

        int atMostHeightMeasureSpec = MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.AT_MOST);


        int resultWidthSize = getPaddingLeft() + getPaddingRight();
        int resultHeightSize = getPaddingTop() + getPaddingBottom() + indicatorHeight;

        for(String tag : tags)
        {
            TextView child = tagMap.get(tag);
            ViewGroup.LayoutParams childLayoutParams = child.getLayoutParams();
            int childWidthSpec = getChildMeasureSpec(unspecifiedWidthMeasureSpec, resultWidthSize, childLayoutParams.width);
            int childHeightSpec = getChildMeasureSpec(atMostHeightMeasureSpec, resultHeightSize, childLayoutParams.height);
            child.measure(childWidthSpec, childHeightSpec);
            resultWidthSize += child.getMeasuredWidth();

//            if(resultHeightSize < (child.getMeasuredHeight() + getPaddingTop() + getPaddingBottom() + indicatorHeight))
//            {
//                resultHeightSize = child.getMeasuredHeight() + getPaddingTop() + getPaddingBottom() + indicatorHeight;
//            }
        }

        if(tags != null && tags.size() != 0)
        {
            resultHeightSize += tagMap.get(tags.get(0)).getMeasuredHeight();
            if(!balanceLayout)
            {
                resultWidthSize += (tags.size() - 1) * interval;
            }
        }


        if(widthSpecMode == MeasureSpec.AT_MOST && heightSpecMode == MeasureSpec.AT_MOST)
        {
            setMeasuredDimension(resultWidthSize > widthSize ? widthSize : resultWidthSize,
                    resultHeightSize > heightSize ? heightSize : resultHeightSize);
        }else if(widthMeasureSpec == MeasureSpec.AT_MOST)
        {
            setMeasuredDimension(resultWidthSize > widthSize ? widthSize : resultWidthSize, heightSize);
        }else if(heightMeasureSpec == MeasureSpec.AT_MOST)
        {
            setMeasuredDimension(widthSize, resultHeightSize > heightSize ? heightSize : resultHeightSize);
        }else
        {
            setMeasuredDimension(widthSize, heightSize);
        }


    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

        if(changed)
        {

            if(balanceLayout)
            {

            }else
            {

            }

//            layoutChildren(textOffset,
//                    tags.size() == 0 ? getPaddingLeft() : tagMap.get(tags.get(currentPosition)).getLeft(),
//                    tags.size() == 0 ? 0 : tagMap.get(tags.get(currentPosition)).getMeasuredWidth());
            layoutChildren(textOffset, currentPosition);
            tagMap.get(tags.get(currentPosition)).setTextColor(selectedTextColor);
        }


    }

    private void layoutChildren(int textOffset, int indicatorOffset, int indicatorLength)
    {
        log.v("layout children, textOffset = " + textOffset + ", indicatorOffset = " + indicatorOffset + ", indicatorLength = " + indicatorLength);
        if(tags.size() != 0)
        {
            int padding = (getMeasuredHeight() - getPaddingBottom() - getPaddingTop() - indicatorHeight - tagMap.get(tags.get(0)).getMeasuredHeight()) / 2;
            if(padding < 0)
            {
                padding = 0;
            }
            if(balanceLayout)
            {
                int availableWidth = getMeasuredWidth() - getPaddingLeft() - getPaddingRight();
                int totalItemWidth = 0;
                for(int i = 0; i < tags.size(); i++)
                {
                    totalItemWidth += tagMap.get(tags.get(i)).getMeasuredWidth();
                }
                int space = (availableWidth - totalItemWidth) / (tags.size() + 1);
                space = space < 0 ? 0 : space;
                textOffset = getPaddingLeft() + space;

                for(int i = 0; i < tags.size(); i++)
                {
                    TextView child = tagMap.get(tags.get(i));
                    int left = textOffset;
                    int right = textOffset + child.getMeasuredWidth();
                    child.layout(left,
                            getPaddingTop() + padding, right,
                            getPaddingTop() + child.getMeasuredHeight() + padding);

                    textOffset += space + child.getMeasuredWidth();
                }
            }else
            {
                for(String s : tags)
                {
                    TextView child = tagMap.get(s);
                    child.layout(textOffset, getPaddingTop() + padding, textOffset + child.getMeasuredWidth(), getPaddingTop() + child.getMeasuredHeight() + padding);
                    textOffset += child.getMeasuredWidth() + interval;
//                log.v(s + ": left=" + child.getLeft() + ", right=" + child.getRight() + ", top=" + child.getTop() + ", bottom=" + child.getBottom());
                }
            }

        }



        ViewGroup.LayoutParams layoutParams = indicatorLine.getLayoutParams();
        layoutParams.width = indicatorLength;
        indicatorLine.setLayoutParams(layoutParams);
        indicatorLine.layout(indicatorOffset, getMeasuredHeight() - getPaddingBottom() - indicatorHeight,
                indicatorOffset + indicatorLength, getMeasuredHeight() - getPaddingBottom());

    }

    private void layoutChildren(int textOffset, int index)
    {
        if(tags.size() != 0)
        {
            int padding = (getMeasuredHeight() - getPaddingBottom() - getPaddingTop() - indicatorHeight - tagMap.get(tags.get(0)).getMeasuredHeight()) / 2;
            if(padding < 0)
            {
                padding = 0;
            }
            if(balanceLayout)
            {
                int availableWidth = getMeasuredWidth() - getPaddingLeft() - getPaddingRight();
                int totalItemWidth = 0;
                for(int i = 0; i < tags.size(); i++)
                {
                    totalItemWidth += tagMap.get(tags.get(i)).getMeasuredWidth();
                }
                int space = (availableWidth - totalItemWidth) / (tags.size() + 1);
                space = space < 0 ? 0 : space;
                textOffset = getPaddingLeft() + space;

                for(int i = 0; i < tags.size(); i++)
                {
                    TextView child = tagMap.get(tags.get(i));
                    int left = textOffset;
                    int right = textOffset + child.getMeasuredWidth();
                    child.layout(left,
                            getPaddingTop() + padding, right,
                            getPaddingTop() + child.getMeasuredHeight() + padding);

                    textOffset += space + child.getMeasuredWidth();
                }
            }else
            {
                for(String s : tags)
                {
                    TextView child = tagMap.get(s);
                    child.layout(textOffset, getPaddingTop() + padding, textOffset + child.getMeasuredWidth(), getPaddingTop() + child.getMeasuredHeight() + padding);
                    textOffset += child.getMeasuredWidth() + interval;
//                log.v(s + ": left=" + child.getLeft() + ", right=" + child.getRight() + ", top=" + child.getTop() + ", bottom=" + child.getBottom());
                }
            }

            ViewGroup.LayoutParams layoutParams = indicatorLine.getLayoutParams();
            layoutParams.width = tagMap.get(tags.get(index)).getMeasuredWidth();
            indicatorLine.setLayoutParams(layoutParams);
            int indicatorOffset = tagMap.get(tags.get(index)).getLeft();
            indicatorLine.layout(indicatorOffset, getMeasuredHeight() - getPaddingBottom() - indicatorHeight ,
                    indicatorOffset + layoutParams.width, getMeasuredHeight() - getPaddingBottom());
        }

    }


    @Override
    protected void onDraw(Canvas canvas) {

        log.v("onDraw");
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(backgroundColor);
        Rect rect = new Rect(this.getLeft(),this.getTop(), this.getRight(), this.getBottom());
        canvas.drawRect(rect, paint);

        for(String s : tags)
        {
            TextView child = tagMap.get(s);
            if(child.getLeft() <= getRight() || child.getRight() >= getLeft())
            {
                child.draw(canvas);
            }
        }

        if(indicatorLine.getLeft() <= getRight() || indicatorLine.getRight() >= getLeft())
        {
            indicatorLine.draw(canvas);
        }



    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return true;
    }

    private boolean moved = false;
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                newX = event.getRawX();
                newY = event.getRawY();
                downX = newX;
                downY = newY;
                break;
            case MotionEvent.ACTION_MOVE:
                lastX = newX;
                lastY = newY;
                newX = event.getRawX();
                newY = event.getRawY();
                dx = newX - lastX;
                dy = newY - lastY;
                if(dx >= touchSlop)
                {
                    moved = true;
                }
                int left = tagMap.get(tags.get(0)).getLeft();
                int right = tagMap.get(tags.get(tags.size() - 1)).getRight();
                int length = right - left;
                if (length < getMeasuredWidth() - getPaddingLeft() - getPaddingRight())
                {

                }else if(left + dx > getPaddingLeft())
                {
                    left = getPaddingLeft();
                }else if(right + dx < (getMeasuredWidth() - getPaddingRight()))
                {
                    right = getMeasuredWidth() - getPaddingRight();
                    left = right - length;
                }else
                {
                    left += dx;
                }
                textOffset = left;
                layoutChildren(left, indicatorLine.getLeft() + (left - tagMap.get(tags.get(0)).getLeft()), indicatorLine.getWidth());

                break;
            case MotionEvent.ACTION_UP:
                lastX = newX;
                lastY = newY;
                newX = event.getRawX();
                newY = event.getRawY();
                dx = newX - lastX;
                dy = newY - lastY;
                /*判断为点击事件*/
                if(!moved && Math.abs(newX - downX) < touchSlop && Math.abs(newY - downY) < touchSlop)
                {

                    for(int i = 0; i < tags.size(); i++)
                    {
                        TextView child = tagMap.get(tags.get(i));
                        if(child.getLeft() <= newX && child.getRight() >= newX)
                        {
                            notifyOnTagClickedListsners(i);
                            break;
                        }
                    }
                }
                moved = false;
                break;


        }
        return super.onTouchEvent(event);
    }

    public void listen(int position, float positionOffset, int positionOffsetPixels)
    {
        currentPosition = position;
        if(positionOffset != 0)
        {
            TextView current = tagMap.get(tags.get(position));
            TextView old = tagMap.get(tags.get(position + 1));
            int spaceBetweenTags = old.getLeft() - current.getLeft();
            int lineLeft = current.getLeft() + (int)(spaceBetweenTags * positionOffset);

            int currentLength = current.getWidth();
            int oldLength = old.getWidth();
            int lineLength = currentLength + (int)((oldLength - currentLength) * positionOffset);
            int lineRight = lineLeft + lineLength;
            if(lineLength >= (getMeasuredWidth() - getPaddingLeft() - getPaddingRight()))
            {
                textOffset += getPaddingLeft() - lineLeft;
                lineLeft = getPaddingLeft();
            }else if(lineLeft < getPaddingLeft())
            {
                textOffset += getPaddingLeft() - lineLeft;
                lineLeft = getPaddingLeft();
//                lineLength = lineRight - lineLeft;
            }else if(lineRight > (getMeasuredWidth() - getPaddingRight()))
            {
                textOffset -= lineRight - (getMeasuredWidth() - getPaddingRight());
                lineRight = (getMeasuredWidth() - getPaddingRight());
//                lineLength = lineRight - lineLeft;
                lineLeft = lineRight - lineLength;
            }
            layoutChildren(textOffset, lineLeft, lineLength);

            current.setTextColor(evaluateColor(textColor, selectedTextColor, 1 - positionOffset));
            old.setTextColor(evaluateColor(textColor, selectedTextColor, positionOffset));


        }

    }



    public void addTags(String[] tags)
    {
        for(String s : tags)
        {
            addTag(s);
        }
    }

    public void addTag(String tag, int... index)
    {
        log.v("add tag");
        TextView t = new TextView(getContext());

        ViewGroup.MarginLayoutParams layoutParams = new ViewGroup.MarginLayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        t.setLayoutParams(layoutParams);
        t.setText(tag);
        t.setTextColor(textColor);
        t.setTextSize(textSize);
        t.setPadding(2,2,2,2);

        if(index != null && index.length != 0)
        {
            if(index[0] <= currentPosition)
            {
                currentPosition++;
            }
            this.tags.add(index[0], tag);

        }else
        {
            this.tags.add(tag);
        }

        this.tagMap.put(tag, t);
        this.addView(t);

    }

    public void removeTag(String tag)
    {
        tags.remove(tag);
        tagMap.remove(tag);
    }

    public int getCurrentPosition()
    {
        return currentPosition;
    }

    public void addOnTagClickedListener(OnTagClickedListener l)
    {
        if(!onTagClickedListeners.contains(l))
        {
            onTagClickedListeners.add(l);
        }
    }

    public void removeOnTagClickedListener(OnTagClickedListener l)
    {
        if(onTagClickedListeners.contains(l))
        {
            onTagClickedListeners.remove(l);
        }
    }

    private void notifyOnTagClickedListsners(int position)
    {
        if(onTagClickedListeners.size() != 0)
        {
            for(OnTagClickedListener l : onTagClickedListeners)
            {
                l.onTagClicked(position);
            }
        }
    }
    public interface OnTagClickedListener
    {
        public void onTagClicked(int position);
    }

    private int evaluateColor(int fromColor, int toColor, float percent)
    {
        int fromA = (fromColor >> 24) & 0xff;
        int fromR = (fromColor >> 16) & 0xff;
        int fromG = (fromColor >> 8) & 0xff;
        int fromB = fromColor & 0xff;

        int toA = (toColor >> 24) & 0xff;
        int toR = (toColor >> 16) & 0xff;
        int toG = (toColor >> 8) & 0xff;
        int toB = toColor & 0xff;

        int dA = (int)((toA - fromA) * percent);
        int dR = (int)((toR - fromR) * percent);
        int dG = (int)((toG - fromG) * percent);
        int dB = (int)((toB - fromB) * percent);

        int color = ((fromA + dA) << 24) | ((fromR + dR) << 16) | ((fromG + dG) << 8) | ((fromB + dB));
        return color;
    }

}
